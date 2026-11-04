package com.maquigest.platform.maintenance.infrastructure.acl.rentals;
import com.maquigest.platform.rentals.domain.repositories.*;
import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import com.maquigest.platform.rentals.domain.model.entities.*;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import org.junit.jupiter.api.Test;
import java.time.*;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class RentalsMaintenanceAclAdapterTests {
    @Test void completedRentalUsesActualReturnTimestampInHistory() {
        var rentals=mock(RentalRepository.class);
        var deliveries=mock(DeliveryRepository.class);
        var returns=mock(EquipmentReturnRepository.class);
        var rented=new Rental(10L,7L,3L,2L,1L,99L,
            new RentalPeriod(LocalDate.of(2026,11,25),LocalDate.of(2026,11,27)),RentalStatus.COMPLETED);
        var actualReturn=Instant.parse("2026-11-04T12:00:00Z");
        when(rentals.findVisibleTo(1L)).thenReturn(List.of(rented));
        when(returns.findByRentalId(10L)).thenReturn(Optional.of(new EquipmentReturn(1L,10L,actualReturn,"",false)));
        var adapter=new RentalsMaintenanceAclAdapter(rentals,deliveries,returns);
        var result=adapter.history(3L,1L);
        assertEquals(1,result.size());
        assertEquals(actualReturn,result.getFirst().occurredAt());
        assertEquals("COMPLETED",result.getFirst().status());
    }
}
