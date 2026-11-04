package com.maquigest.platform.rentals.domain.model.valueobjects;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
class RentalPeriodTests {
    @Test void inclusiveDatesOverlapAtSharedBoundary() {
        var a=new RentalPeriod(LocalDate.of(2026,11,16),LocalDate.of(2026,11,18));
        var b=new RentalPeriod(LocalDate.of(2026,11,18),LocalDate.of(2026,11,20));
        assertTrue(a.overlaps(b));
        assertFalse(a.overlaps(new RentalPeriod(LocalDate.of(2026,11,19),LocalDate.of(2026,11,20))));
    }
    @Test void invalidDatesAreRejected(){assertThrows(IllegalArgumentException.class,
        ()->new RentalPeriod(LocalDate.of(2026,11,20),LocalDate.of(2026,11,19)));}
}
