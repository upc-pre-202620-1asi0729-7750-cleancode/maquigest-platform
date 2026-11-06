package com.maquigest.platform.rentals.infrastructure.acl.inventory;

import com.maquigest.platform.inventory.interfaces.acl.InventoryRentalFacade;
import com.maquigest.platform.inventory.interfaces.acl.InventoryRentalFailure;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.junit.jupiter.api.Test;

class InventoryRentalAclAdapterTests {
    private static final LocalDate FROM = LocalDate.of(2026, 11, 16);
    private static final LocalDate TO = LocalDate.of(2026, 11, 18);
    private static final RentalPeriod PERIOD = new RentalPeriod(FROM, TO);

    @Test void translatesPublishedInventorySnapshotWithoutImportingInventoryDomain() {
        var facade = mock(InventoryRentalFacade.class);
        when(facade.inspect(8L, FROM, TO)).thenReturn(new InventoryRentalFacade.EquipmentSnapshot(8L, 1L, true));
        var facts = new InventoryRentalAclAdapter(facade).inspect(8L, PERIOD);
        assertEquals(8L, facts.id());
        assertEquals(1L, facts.ownerUserId());
        assertTrue(facts.availableForPeriod());
    }

    @Test void reservePassesDatesAndReturnsExactBlockIdentifier() {
        var facade = mock(InventoryRentalFacade.class);
        when(facade.reserve(8L, 1L, FROM, TO)).thenReturn(81L);
        assertEquals(81L, new InventoryRentalAclAdapter(facade).reserve(8L, 1L, PERIOD));
    }

    @Test void mapsPublishedForbiddenConflictAndNotFoundToExistingRentalsHttpErrors() {
        var facade = mock(InventoryRentalFacade.class);
        when(facade.reserve(8L, 2L, FROM, TO)).thenThrow(InventoryRentalFailure.forbidden());
        when(facade.reserve(8L, 1L, FROM, TO)).thenThrow(InventoryRentalFailure.conflict("Overlap"));
        when(facade.inspect(8L, FROM, TO)).thenThrow(InventoryRentalFailure.notFound());
        var acl = new InventoryRentalAclAdapter(facade);
        assertEquals(403, assertThrows(RentalsException.class, () -> acl.reserve(8L, 2L, PERIOD)).status().value());
        assertEquals(409, assertThrows(RentalsException.class, () -> acl.reserve(8L, 1L, PERIOD)).status().value());
        assertEquals(404, assertThrows(RentalsException.class, () -> acl.inspect(8L, PERIOD)).status().value());
    }

    @Test void forwardsDeliveryAndReturnToFacadeWithoutChangingArguments() {
        var facade = mock(InventoryRentalFacade.class);
        var acl = new InventoryRentalAclAdapter(facade);
        acl.markDelivered(8L, 1L, 77L);
        acl.completeReturn(8L, 1L, 77L, true);
        verify(facade).markDelivered(8L, 1L, 77L);
        verify(facade).completeReturn(8L, 1L, 77L, true);
    }
}
