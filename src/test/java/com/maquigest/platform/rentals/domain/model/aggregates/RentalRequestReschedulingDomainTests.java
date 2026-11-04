package com.maquigest.platform.rentals.domain.model.aggregates;

import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus;
import java.time.Instant;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class RentalRequestReschedulingDomainTests {
    @Test void pendingRequestCanChangeDatesWithoutChangingIdentity() {
        var first = new RentalPeriod(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12));
        var second = new RentalPeriod(LocalDate.of(2026, 12, 15), LocalDate.of(2026, 12, 16));
        var request = new RentalRequest(7L, 3L, 2L, 1L, first, RentalRequestStatus.PENDING, Instant.now());
        request.reschedule(second);
        assertEquals(second, request.getPeriod());
        assertEquals(7L, request.getId());
    }

    @Test void approvedAndRejectedRequestsCannotReschedule() {
        var period = new RentalPeriod(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12));
        for (var status : new RentalRequestStatus[] {RentalRequestStatus.APPROVED, RentalRequestStatus.REJECTED}) {
            var request = new RentalRequest(7L, 3L, 2L, 1L, period, status, Instant.now());
            assertThrows(IllegalStateException.class, () -> request.reschedule(period));
        }
    }
}
