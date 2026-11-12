package com.maquigest.platform.maintenance.infrastructure.acl.rentals;

import com.maquigest.platform.rentals.interfaces.acl.RentalsMaintenanceFacade;
import org.junit.jupiter.api.Test;
import java.time.Instant;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalsMaintenanceAclAdapterTests {
    private final RentalsMaintenanceFacade facade=mock(RentalsMaintenanceFacade.class);
    private final RentalsMaintenanceAclAdapter adapter=new RentalsMaintenanceAclAdapter(facade);

    @Test void delegatesActiveRentalCheck() {
        when(facade.hasActiveRental(3L,1L)).thenReturn(true);
        assertTrue(adapter.hasActiveRental(3L,1L));
        verify(facade).hasActiveRental(3L,1L);
    }
    @Test void mapsImmutableRentalHistoryWithoutChangingTimestamp() {
        Instant returned=Instant.parse("2026-11-04T12:00:00Z");
        when(facade.history(3L,1L)).thenReturn(List.of(new RentalsMaintenanceFacade.RentalHistory(
            10L, returned, "Rental #10 (2026-11-25 to 2026-11-27)","COMPLETED")));
        var result=adapter.history(3L,1L);
        assertEquals(1,result.size());
        assertEquals(returned,result.getFirst().occurredAt());
        assertEquals("COMPLETED",result.getFirst().status());
        assertEquals("Rental #10 (2026-11-25 to 2026-11-27)",result.getFirst().description());
    }
}
