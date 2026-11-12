package com.maquigest.platform.maintenance.infrastructure.acl.rentals;
import com.maquigest.platform.rentals.interfaces.acl.RentalsMaintenanceFacade;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RentalsMaintenanceReturnAclTests {
  @Test void mapsPublishedReturnSnapshotWithoutExposingRentalsAggregate() {
    var facade=mock(RentalsMaintenanceFacade.class);
    var returnedAt=Instant.parse("2026-11-11T12:30:00Z");
    when(facade.latestReturn(6L,10L)).thenReturn(Optional.of(new RentalsMaintenanceFacade.LatestReturn(returnedAt,true)));
    var evidence=new RentalsMaintenanceAclAdapter(facade).latestReturn(6L,10L);
    assertTrue(evidence.isPresent());
    assertEquals(returnedAt,evidence.get().returnedAt());
    assertTrue(evidence.get().maintenanceRequired());
  }
}
