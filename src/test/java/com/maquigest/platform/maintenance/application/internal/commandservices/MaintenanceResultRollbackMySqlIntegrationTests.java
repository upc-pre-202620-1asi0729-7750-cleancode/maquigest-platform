package com.maquigest.platform.maintenance.application.internal.commandservices;

import com.maquigest.platform.maintenance.application.commandservices.MaintenanceCommandService;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.application.ports.MaintenanceInventoryPort;
import com.maquigest.platform.maintenance.application.ports.MaintenanceSubscriptionPort;
import com.maquigest.platform.maintenance.domain.model.commands.RegisterMaintenanceCommand;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.maquigest.platform.maintenance.domain.repositories.MaintenanceActivityRepository;
import com.maquigest.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** A later expected error must return Result.failure AFTER the real JPA transaction rolls back. */
@SpringBootTest
@EnabledIfSystemProperty(named="maquigest.phase3RollbackTests",matches="true")
class MaintenanceResultRollbackMySqlIntegrationTests {
    @Autowired private MaintenanceCommandService commands;
    @Autowired private JdbcTemplate jdbc;
    @MockitoBean private MaintenanceInventoryPort inventory;
    @MockitoBean private MaintenanceSubscriptionPort subscriptions;
    @MockitoBean private MaintenanceActivityRepository activities;

    @Test void failureAfterMaintenanceSaveRollsBackBeforeReturningResultFailure() {
        String expectedDb = System.getProperty("maquigest.phase3ExpectedDb", "");
        assertTrue(expectedDb.matches("maquigest_ddd_fase3_[a-f0-9]{8}"));
        assertEquals(expectedDb, jdbc.queryForObject("SELECT DATABASE()", String.class));
        // Equipment foreign keys are intentionally absent from maintenance_records; use
        // a fresh unused ID to isolate the check from all existing fixtures.
        long equipmentId = 600000000L + Math.abs(UUID.randomUUID().getLeastSignificantBits() % 100000000L);
        long owner = 70001L;
        when(inventory.lock(equipmentId)).thenReturn(
                new MaintenanceInventoryPort.EquipmentFacts(equipmentId, owner, "ROLLBACK", "Test machine", "AVAILABLE"));
        when(subscriptions.hasActiveGrowth(owner)).thenReturn(true);
        when(activities.save(any())).thenThrow(MaintenanceException.conflict("Synthetic activity write failed"));
        Integer before = jdbc.queryForObject(
                "SELECT COUNT(*) FROM maintenance_records WHERE equipment_id = ?", Integer.class, equipmentId);

        var result = commands.handle(new RegisterMaintenanceCommand(
                equipmentId, Instant.now(), "TEST ROLLBACK", MaintenanceStatus.COMPLETED), owner);
        var failure = assertInstanceOf(Result.Failure.class, result);
        assertEquals("MAINTENANCE_CONFLICT", failure.error() instanceof com.maquigest.platform.shared.application.result.ApplicationError
                ? ((com.maquigest.platform.shared.application.result.ApplicationError) failure.error()).code() : null);
        verify(activities).save(any()); // Reached AFTER the first JPA save.
        Integer after = jdbc.queryForObject(
                "SELECT COUNT(*) FROM maintenance_records WHERE equipment_id = ?", Integer.class, equipmentId);
        assertEquals(before, after, "No maintenance row may survive a late failure");
    }
}
