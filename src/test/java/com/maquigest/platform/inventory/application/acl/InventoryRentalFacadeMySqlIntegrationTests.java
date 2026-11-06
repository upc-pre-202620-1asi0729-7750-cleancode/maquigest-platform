package com.maquigest.platform.inventory.application.acl;

import com.maquigest.platform.inventory.interfaces.acl.InventoryRentalFacade;
import com.maquigest.platform.inventory.interfaces.acl.InventoryRentalFailure;
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
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: only runs on a new dedicated MySQL test database, never on production. */
@SpringBootTest
@EnabledIfSystemProperty(named = "maquigest.inventoryLockTests", matches = "true")
class InventoryRentalFacadeMySqlIntegrationTests {
    @Autowired private InventoryRentalFacade facade;
    @Autowired private PlatformTransactionManager transactions;
    @Autowired private JdbcTemplate jdbc;

    private void requireDedicatedDb() {
        String expected = System.getProperty("maquigest.inventoryExpectedDb", "");
        assertTrue(expected.matches("maquigest_ddd_fase1d_[a-f0-9]{8}"),
            "Use a new dedicated maquigest_ddd_fase1d_XXXXXXXX database");
        assertEquals(expected, jdbc.queryForObject("SELECT DATABASE()", String.class));
    }

    @Test void reservationRequiresAnExistingTransaction() {
        requireDedicatedDb();
        assertThrows(IllegalTransactionStateException.class,
            () -> facade.reserve(9999999L, 70001L, LocalDate.now().plusDays(5), LocalDate.now().plusDays(6)));
    }

    @Test void secondReservationWaitsForInnoDbLockAndThenConflicts() throws Exception {
        requireDedicatedDb();
        Long categoryId = jdbc.queryForObject("SELECT MIN(id) FROM equipment_categories", Long.class);
        assertNotNull(categoryId, "Category seeder did not run");
        var keys = new GeneratedKeyHolder();
        long owner = 70001L;
        String code = "DDD-" + UUID.randomUUID().toString().substring(0, 8);
        jdbc.update(connection -> {
            var stmt = connection.prepareStatement("""
                INSERT INTO equipment (created_at, updated_at, category_id, code, daily_rate,
                    description, location, name, status, user_id, version, weekly_rate)
                VALUES (NOW(6), NOW(6), ?, ?, 100, 'DDL test fixture', 'Lima',
                    'Concurrency fixture', 'AVAILABLE', ?, 0, 700)
                """, Statement.RETURN_GENERATED_KEYS);
            stmt.setLong(1, categoryId);
            stmt.setString(2, code);
            stmt.setLong(3, owner);
            return stmt;
        }, keys);
        Long equipmentId = Objects.requireNonNull(keys.getKey()).longValue();
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = start.plusDays(1);
        var firstReserved = new CountDownLatch(1);
        var allowFirstCommit = new CountDownLatch(1);
        var secondStarted = new CountDownLatch(1);
        try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
            var first = executor.submit(() -> new TransactionTemplate(transactions).execute(status -> {
                Long reservation = facade.reserve(equipmentId, owner, start, end);
                firstReserved.countDown();
                try {
                    if (!allowFirstCommit.await(10, TimeUnit.SECONDS))
                        throw new IllegalStateException("Timed out waiting to finish first transaction");
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(ex);
                }
                return reservation;
            }));
            try {
                assertTrue(firstReserved.await(10, TimeUnit.SECONDS), "First reservation did not finish");
                var second = executor.submit(() -> new TransactionTemplate(transactions).execute(status -> {
                    secondStarted.countDown();
                    return facade.reserve(equipmentId, owner, start, end);
                }));
                assertTrue(secondStarted.await(5, TimeUnit.SECONDS));
                assertThrows(TimeoutException.class, () -> second.get(350, TimeUnit.MILLISECONDS),
                    "Second transaction should block while the first holds PESSIMISTIC_WRITE");
                allowFirstCommit.countDown();
                assertNotNull(first.get(10, TimeUnit.SECONDS));
                var failure = assertThrows(ExecutionException.class, () -> second.get(10, TimeUnit.SECONDS));
                assertInstanceOf(InventoryRentalFailure.class, failure.getCause());
                assertEquals(InventoryRentalFailure.Kind.CONFLICT,
                    ((InventoryRentalFailure) failure.getCause()).kind());
            } finally {
                allowFirstCommit.countDown();
            }
        } finally {
            jdbc.update("DELETE FROM equipment_availability_blocks WHERE equipment_id = ?", equipmentId);
            jdbc.update("DELETE FROM equipment WHERE id = ?", equipmentId);
        }
    }
}
