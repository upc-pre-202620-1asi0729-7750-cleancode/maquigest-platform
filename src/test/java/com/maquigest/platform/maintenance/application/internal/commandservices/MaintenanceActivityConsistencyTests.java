package com.maquigest.platform.maintenance.application.internal.commandservices;

import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.application.ports.MaintenanceInventoryPort;
import com.maquigest.platform.maintenance.application.ports.MaintenanceRentalPort;
import com.maquigest.platform.maintenance.application.ports.MaintenanceSubscriptionPort;
import com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance;
import com.maquigest.platform.maintenance.domain.model.commands.RegisterMaintenanceCommand;
import com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.maquigest.platform.maintenance.domain.repositories.MaintenanceActivityRepository;
import com.maquigest.platform.maintenance.domain.repositories.MaintenanceRepository;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** MaintenanceActivity is an append-only child/history written in its root's transaction. */
class MaintenanceActivityConsistencyTests {
    private MaintenanceRepository maintenances;
    private MaintenanceActivityRepository activities;
    private MaintenanceInventoryPort inventory;
    private MaintenanceRentalPort rentals;
    private MaintenanceSubscriptionPort subscriptions;
    private MaintenanceTransactionalExecutor executor;

    @BeforeEach void setup() {
        maintenances=mock(MaintenanceRepository.class);
        activities=mock(MaintenanceActivityRepository.class);
        inventory=mock(MaintenanceInventoryPort.class);
        rentals=mock(MaintenanceRentalPort.class);
        subscriptions=mock(MaintenanceSubscriptionPort.class);
        executor=new MaintenanceTransactionalExecutor(maintenances,activities,inventory,rentals,subscriptions);
        when(inventory.lock(3L)).thenReturn(new MaintenanceInventoryPort.EquipmentFacts(3L,1L,"EQ-3","Excavator","AVAILABLE"));
    }

    @Test void registeringMaintenanceWritesHistoryWithThePersistedRootIdentifier() {
        when(subscriptions.hasActiveGrowth(1L)).thenReturn(true);
        when(maintenances.save(any())).thenAnswer(inv -> {
            Maintenance source=inv.getArgument(0);
            return new Maintenance(80L,source.getEquipmentId(),source.getOwnerUserId(),
                source.getPerformedAt(),source.getType(),source.getStatus(),source.getTechnician(),source.getAcknowledgedAt());
        });
        Maintenance maintenance = executor.handle(new RegisterMaintenanceCommand(
            3L, Instant.now(), "Inspection", MaintenanceStatus.COMPLETED), 1L);
        assertEquals(80L, maintenance.getId());
        verify(activities, times(1)).save(argThat((MaintenanceActivity item) ->
            item.maintenanceId().equals(80L) && item.status()==MaintenanceStatus.COMPLETED));
    }

    @Test void nonOwnerCannotCreateOrWriteMaintenanceHistory() {
        MaintenanceException failure=assertThrows(MaintenanceException.class,
            () -> executor.handle(new RegisterMaintenanceCommand(3L,Instant.now(),"Inspection",MaintenanceStatus.COMPLETED),2L));
        assertEquals("MAINTENANCE_FORBIDDEN",failure.code());
        verifyNoInteractions(maintenances,activities);
    }

    @Test void insufficientSubscriptionCannotWriteHistory() {
        MaintenanceException failure=assertThrows(MaintenanceException.class,
            () -> executor.handle(new RegisterMaintenanceCommand(3L,Instant.now(),"Inspection",MaintenanceStatus.COMPLETED),1L));
        assertEquals("MAINTENANCE_FORBIDDEN",failure.code());
        verifyNoInteractions(maintenances,activities);
    }

    @Test void historyEntityRejectsInvalidReferenceAndUnboundedNote() {
        assertThrows(IllegalArgumentException.class, () ->
            new MaintenanceActivity(null,-1L,MaintenanceStatus.SCHEDULED,Instant.now(),"",""));
        assertThrows(IllegalArgumentException.class, () ->
            new MaintenanceActivity(null,1L,MaintenanceStatus.SCHEDULED,Instant.now(),"","x".repeat(2001)));
    }
}
