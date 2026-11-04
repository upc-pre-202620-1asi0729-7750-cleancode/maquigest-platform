package com.maquigest.platform.rentals.domain.model.aggregates;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class RentalRequestTests {
    private static RentalRequest pending(){return new RentalRequest(3L,10L,1L,2L,
        new RentalPeriod(LocalDate.of(2026,11,16),LocalDate.of(2026,11,18)),
        RentalRequestStatus.PENDING,Instant.now());}
    @Test void approvingRaisesDomainEventAndCannotBeRepeated(){
        var request=pending();request.approve();assertEquals(RentalRequestStatus.APPROVED,request.getStatus());
        assertEquals(1,request.domainEvents().size());
        assertThrows(IllegalStateException.class,request::approve);
        assertThrows(IllegalStateException.class,request::reject);
    }
    @Test void rejectionIsTerminal(){var r=pending();r.reject();assertEquals(RentalRequestStatus.REJECTED,r.getStatus());
        assertThrows(IllegalStateException.class,r::approve);}
}
