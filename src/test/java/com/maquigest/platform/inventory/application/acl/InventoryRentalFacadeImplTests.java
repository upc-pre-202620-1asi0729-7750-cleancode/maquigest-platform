package com.maquigest.platform.inventory.application.acl;

import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.entities.AvailabilityBlock;
import com.maquigest.platform.inventory.domain.model.valueobjects.*;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import com.maquigest.platform.inventory.interfaces.acl.InventoryRentalFailure;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryRentalFacadeImplTests {
    private static final LocalDate FROM = LocalDate.of(2026, 11, 16);
    private static final LocalDate TO = LocalDate.of(2026, 11, 18);

    private static Equipment equipment(EquipmentStatus status, List<AvailabilityBlock> blocks) {
        return Equipment.reconstitute(8L, 1L, "EXC-01", "Excavadora", "Prueba", 2L,
            "Lima", new RentalRate(new BigDecimal("900"), new BigDecimal("5400")), status, blocks);
    }

    @Test void inspectUsesNonLockingReadAndReturnsSnapshot() {
        var repo = mock(EquipmentRepository.class);
        when(repo.findById(8L)).thenReturn(Optional.of(equipment(EquipmentStatus.AVAILABLE, List.of())));
        var snapshot = new InventoryRentalFacadeImpl(repo).inspect(8L, FROM, TO);
        assertEquals(8L, snapshot.id());
        assertEquals(1L, snapshot.ownerUserId());
        assertTrue(snapshot.availableForPeriod());
        verify(repo, never()).findByIdForUpdate(any());
    }

    @Test void onlyOwnerMayReserveAndOverlappingDatesFail() {
        var repo = mock(EquipmentRepository.class);
        when(repo.findByIdForUpdate(8L)).thenReturn(Optional.of(
            equipment(EquipmentStatus.AVAILABLE, List.of(new AvailabilityBlock(77L, FROM, TO)))));
        var facade = new InventoryRentalFacadeImpl(repo);
        assertEquals(InventoryRentalFailure.Kind.FORBIDDEN,
            assertThrows(InventoryRentalFailure.class, () -> facade.reserve(8L, 2L, FROM, TO)).kind());
        assertEquals(InventoryRentalFailure.Kind.CONFLICT,
            assertThrows(InventoryRentalFailure.class, () -> facade.reserve(8L, 1L, FROM, TO)).kind());
        verify(repo, never()).save(any());
    }

    @Test void reservationReturnsPersistedAvailabilityBlockIdentifier() {
        var repo = mock(EquipmentRepository.class);
        var original = equipment(EquipmentStatus.AVAILABLE, List.of());
        var persisted = equipment(EquipmentStatus.AVAILABLE, List.of(new AvailabilityBlock(81L, FROM, TO)));
        when(repo.findByIdForUpdate(8L)).thenReturn(Optional.of(original));
        when(repo.save(any())).thenReturn(persisted);
        assertEquals(81L, new InventoryRentalFacadeImpl(repo).reserve(8L, 1L, FROM, TO));
        verify(repo).save(original);
    }

    @Test void returnReleasesOnlyItsHistoricalBlock() {
        var repo = mock(EquipmentRepository.class);
        var original = equipment(EquipmentStatus.RENTED, List.of(new AvailabilityBlock(77L, FROM, TO)));
        when(repo.findByIdForUpdate(8L)).thenReturn(Optional.of(original));
        when(repo.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        new InventoryRentalFacadeImpl(repo).completeReturn(8L, 1L, 77L, false);
        assertEquals(EquipmentStatus.AVAILABLE, original.getStatus());
        assertTrue(original.getAvailabilityBlocks().isEmpty());
        assertEquals(1, original.getAllAvailabilityBlocks().size());
        assertNotNull(original.getAllAvailabilityBlocks().getFirst().getReleasedAt());
    }

    @Test void onlyActiveReservationCanBeDelivered() {
        var repo = mock(EquipmentRepository.class);
        when(repo.findByIdForUpdate(8L)).thenReturn(Optional.of(equipment(EquipmentStatus.AVAILABLE, List.of())));
        var facade = new InventoryRentalFacadeImpl(repo);
        assertEquals(InventoryRentalFailure.Kind.CONFLICT,
            assertThrows(InventoryRentalFailure.class, () -> facade.markDelivered(8L, 1L, 77L)).kind());
        verify(repo, never()).save(any());
    }

    @Test void missingEquipmentReportsContextNeutralNotFound() {
        var repo = mock(EquipmentRepository.class);
        when(repo.findByIdForUpdate(8L)).thenReturn(Optional.empty());
        assertEquals(InventoryRentalFailure.Kind.NOT_FOUND,
            assertThrows(InventoryRentalFailure.class,
                () -> new InventoryRentalFacadeImpl(repo).reserve(8L, 1L, FROM, TO)).kind());
    }
}
