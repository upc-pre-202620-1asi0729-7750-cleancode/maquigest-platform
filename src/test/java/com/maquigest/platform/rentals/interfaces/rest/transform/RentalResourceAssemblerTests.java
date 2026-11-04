package com.maquigest.platform.rentals.interfaces.rest.transform;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RentalResourceAssemblerTests {
    @Test void limaDatesRoundtripWhenEndInstantIsNextUtcDay(){
        var period=RentalResourceAssembler.parsePeriod(Instant.parse("2026-11-16T05:00:00Z"),
            Instant.parse("2026-11-19T04:59:59.999Z"));
        assertEquals(LocalDate.of(2026,11,16),period.startDate());
        assertEquals(LocalDate.of(2026,11,18),period.endDate());
    }
}
