package com.maquigest.platform.inventory.application.acl;

import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import com.maquigest.platform.inventory.interfaces.acl.InventoryMaintenanceFailure;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryMaintenanceFacadeImplTests {
    private final EquipmentRepository repo = mock(EquipmentRepository.class);
    private final InventoryMaintenanceFacadeImpl facade = new InventoryMaintenanceFacadeImpl(repo);

    private Equipment given(Long id, Long owner, EquipmentStatus status) {
        var equipment = mock(Equipment.class);
        when(equipment.getId()).thenReturn(id);
        when(equipment.getUserId()).thenReturn(owner);
        when(equipment.getStatus()).thenReturn(status);
        when(equipment.getCode()).thenReturn("EXC-001");
        when(equipment.getName()).thenReturn("Excavator");
        when(repo.findByIdForUpdate(id)).thenReturn(Optional.of(equipment));
        return equipment;
    }

    @Test void invalidInspectIdIsRejectedBeforeQuery() {
        var error = assertThrows(InventoryMaintenanceFailure.class, () -> facade.inspect(0L));
        assertEquals(InventoryMaintenanceFailure.Kind.INVALID, error.kind());
        verifyNoInteractions(repo);
    }

    @Test void inspectReturnsPublicSnapshot() {
        var equipment = given(3L, 1L, EquipmentStatus.AVAILABLE);
        when(repo.findById(3L)).thenReturn(Optional.of(equipment));
        var snapshot = facade.inspect(3L);
        assertEquals(1L, snapshot.ownerUserId());
        assertEquals("AVAILABLE", snapshot.status());
        verify(repo, never()).save(any());
    }

    @Test void lockUsesExclusiveRepositoryLookup() {
        given(3L, 1L, EquipmentStatus.AVAILABLE);
        assertEquals(3L, facade.lock(3L).id());
        verify(repo).findByIdForUpdate(3L);
        verify(repo, never()).findById(3L);
    }

    @Test void missingEquipmentIsNotFound() {
        var error = assertThrows(InventoryMaintenanceFailure.class, () -> facade.lock(15L));
        assertEquals(InventoryMaintenanceFailure.Kind.NOT_FOUND, error.kind());
    }

    @Test void wrongOwnerCannotMarkMaintenance() {
        given(3L, 1L, EquipmentStatus.AVAILABLE);
        var error = assertThrows(InventoryMaintenanceFailure.class, () -> facade.markMaintenance(3L, 2L));
        assertEquals(InventoryMaintenanceFailure.Kind.FORBIDDEN, error.kind());
        verify(repo, never()).save(any());
    }

    @Test void rentedEquipmentCannotEnterMaintenance() {
        given(3L, 1L, EquipmentStatus.RENTED);
        var error = assertThrows(InventoryMaintenanceFailure.class, () -> facade.markMaintenance(3L, 1L));
        assertEquals(InventoryMaintenanceFailure.Kind.CONFLICT, error.kind());
        assertEquals("Rented equipment cannot enter maintenance directly", error.getMessage());
        verify(repo, never()).save(any());
    }

    @Test void alreadyMaintenanceIsIdempotent() {
        given(3L, 1L, EquipmentStatus.MAINTENANCE);
        facade.markMaintenance(3L, 1L);
        verify(repo, never()).save(any());
    }

    @Test void availableEquipmentTransitionsToMaintenance() {
        var equipment = given(3L, 1L, EquipmentStatus.AVAILABLE);
        facade.markMaintenance(3L, 1L);
        verify(equipment).changeOperationalStatus(EquipmentStatus.MAINTENANCE);
        verify(repo).save(equipment);
    }

    @Test void onlyMaintenanceEquipmentCanReactivate() {
        given(3L, 1L, EquipmentStatus.AVAILABLE);
        var error = assertThrows(InventoryMaintenanceFailure.class, () -> facade.reactivate(3L, 1L));
        assertEquals(InventoryMaintenanceFailure.Kind.CONFLICT, error.kind());
    }

    @Test void reactivateMovesEquipmentToAvailable() {
        var equipment = given(3L, 1L, EquipmentStatus.MAINTENANCE);
        facade.reactivate(3L, 1L);
        verify(equipment).changeOperationalStatus(EquipmentStatus.AVAILABLE);
        verify(repo).save(equipment);
    }
}
