package com.maquigest.platform.rentals.application.acl;
import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import com.maquigest.platform.rentals.domain.model.entities.EquipmentReturn;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalStatus;
import com.maquigest.platform.rentals.domain.repositories.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RentalsMaintenanceReturnProjectionTests {
  @Test void choosesLatestCompletedReturnAndPreservesMaintenanceFlag() {
    var rentals=mock(RentalRepository.class);
    var deliveries=mock(DeliveryRepository.class);
    var returns=mock(EquipmentReturnRepository.class);
    Rental first=new Rental(20L,7L,6L,2L,10L,99L,
        new RentalPeriod(LocalDate.of(2026,11,1),LocalDate.of(2026,11,3)),RentalStatus.COMPLETED);
    Rental second=new Rental(21L,7L,6L,2L,10L,99L,
        new RentalPeriod(LocalDate.of(2026,11,4),LocalDate.of(2026,11,6)),RentalStatus.COMPLETED);
    when(rentals.findVisibleTo(10L)).thenReturn(List.of(first,second));
    var early=Instant.parse("2026-11-03T20:00:00Z");
    var latest=Instant.parse("2026-11-06T20:00:00Z");
    when(returns.findByRentalId(20L)).thenReturn(Optional.of(new EquipmentReturn(1L,20L,early,"",true)));
    when(returns.findByRentalId(21L)).thenReturn(Optional.of(new EquipmentReturn(2L,21L,latest,"",false)));
    var result=new RentalsMaintenanceFacadeImpl(rentals,deliveries,returns).latestReturn(6L,10L);
    assertTrue(result.isPresent());
    assertEquals(latest,result.get().returnedAt());
    assertFalse(result.get().maintenanceRequired());
  }
}
