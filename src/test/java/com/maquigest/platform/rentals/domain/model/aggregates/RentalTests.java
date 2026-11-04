package com.maquigest.platform.rentals.domain.model.aggregates;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RentalTests {
    private static Rental confirmed(){return new Rental(1L,2L,3L,4L,5L,6L,
        new RentalPeriod(LocalDate.of(2026,11,16),LocalDate.of(2026,11,18)),RentalStatus.CONFIRMED);}
    @Test void lifecycleRequiresDeliveryBeforeReturn(){var r=confirmed();
        assertThrows(IllegalStateException.class,r::complete);
        r.deliver();assertEquals(RentalStatus.ACTIVE,r.getStatus());
        r.complete();assertEquals(RentalStatus.COMPLETED,r.getStatus());
        assertThrows(IllegalStateException.class,r::deliver);
        assertEquals(2,r.domainEvents().size());
    }
}
