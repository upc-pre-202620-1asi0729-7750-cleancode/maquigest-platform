package com.maquigest.platform.inventory.application.acl;

import com.maquigest.platform.inventory.interfaces.acl.InventoryMaintenanceFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.*;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in, only against a freshly created maquigest_ddd_fase1e_XXXXXXXX MySQL database. */
@SpringBootTest
@EnabledIfSystemProperty(named="maquigest.maintenanceLockTests",matches="true")
class InventoryMaintenanceFacadeMySqlIntegrationTests {
    @Autowired private InventoryMaintenanceFacade facade;
    @Autowired private PlatformTransactionManager transactions;
    @Autowired private JdbcTemplate jdbc;

    private void requireDedicatedDb() {
        String expected=System.getProperty("maquigest.maintenanceExpectedDb","");
        assertTrue(expected.matches("maquigest_ddd_fase1e_[a-f0-9]{8}"),"Use dedicated Fase 1E database");
        assertEquals(expected,jdbc.queryForObject("SELECT DATABASE()",String.class));
    }
    @Test void maintenanceLockRequiresExistingTransaction() {
        requireDedicatedDb();
        assertThrows(IllegalTransactionStateException.class,()->facade.lock(9999999L));
        assertThrows(IllegalTransactionStateException.class,()->facade.markMaintenance(9999999L,70001L));
    }
    @Test void secondMaintenanceLockWaitsUntilCommitAndObservesNewStatus() throws Exception {
        requireDedicatedDb();
        Long categoryId=jdbc.queryForObject("SELECT MIN(id) FROM equipment_categories", Long.class);
        assertNotNull(categoryId);
        var holder=new GeneratedKeyHolder();
        Long owner=70001L;
        String code="MAI-"+UUID.randomUUID().toString().substring(0,8);
        jdbc.update(c->{
            var statement=c.prepareStatement("""
                INSERT INTO equipment (created_at, updated_at, category_id, code, daily_rate,
                    description, location, name, status, user_id, version, weekly_rate)
                VALUES (NOW(6), NOW(6), ?, ?, 100, 'Maintenance lock fixture', 'Lima',
                    'Maintenance fixture', 'AVAILABLE', ?, 0, 700)
                """,Statement.RETURN_GENERATED_KEYS);
            statement.setLong(1,categoryId);
            statement.setString(2,code);
            statement.setLong(3,owner);
            return statement;
        },holder);
        Long equipmentId=Objects.requireNonNull(holder.getKey()).longValue();
        var firstLocked=new CountDownLatch(1);
        var allowCommit=new CountDownLatch(1);
        var secondStarted=new CountDownLatch(1);
        try(var executor=Executors.newVirtualThreadPerTaskExecutor()) {
            var first=executor.submit(()->new TransactionTemplate(transactions).execute(status->{
                facade.lock(equipmentId);
                facade.markMaintenance(equipmentId,owner);
                firstLocked.countDown();
                try {
                    if(!allowCommit.await(10,TimeUnit.SECONDS)) throw new IllegalStateException("Commit wait timed out");
                } catch(InterruptedException ex) {Thread.currentThread().interrupt();throw new IllegalStateException(ex);}
                return true;
            }));
            try {
                assertTrue(firstLocked.await(10,TimeUnit.SECONDS),"First transaction did not lock equipment");
                var second=executor.submit(()->new TransactionTemplate(transactions).execute(status->{
                    secondStarted.countDown();
                    return facade.lock(equipmentId).status();
                }));
                assertTrue(secondStarted.await(5,TimeUnit.SECONDS));
                assertThrows(TimeoutException.class,()->second.get(350,TimeUnit.MILLISECONDS),
                    "Second transaction should wait for PESSIMISTIC_WRITE");
                allowCommit.countDown();
                assertTrue(first.get(10,TimeUnit.SECONDS));
                assertEquals("MAINTENANCE",second.get(10,TimeUnit.SECONDS));
            } finally { allowCommit.countDown(); }
        } finally {
            jdbc.update("DELETE FROM equipment WHERE id = ?",equipmentId);
        }
    }
}
