package com.maquigest.platform.rentals.interfaces.rest.transform;
import com.maquigest.platform.rentals.domain.model.aggregates.*;
import com.maquigest.platform.rentals.domain.model.entities.*;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.interfaces.rest.resources.*;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import java.time.*;
public final class RentalResourceAssembler {
    private static final ZoneId LIMA=ZoneId.of("America/Lima");
    private RentalResourceAssembler() { }
    public static RentalPeriod parsePeriod(Instant start,Instant end) {
        if(start==null||end==null) throw RentalsException.invalid("startDate and endDate are required");
        var range=new RentalPeriod(start.atZone(LIMA).toLocalDate(),end.atZone(LIMA).toLocalDate());
        if(start.isAfter(end)) throw RentalsException.invalid("Invalid rental period");
        return range;
    }
    private static Instant periodStart(RentalPeriod p){return p.startDate().atStartOfDay(LIMA).toInstant();}
    private static Instant periodEnd(RentalPeriod p){return p.endDate().plusDays(1).atStartOfDay(LIMA).toInstant().minusMillis(1);}
    public static RentalRequestResource from(RentalRequest r) {
        return new RentalRequestResource(r.getId(),r.getEquipmentId(),r.getConstructionUserId(),
          r.getRentalCompanyUserId(),periodStart(r.getPeriod()),periodEnd(r.getPeriod()),r.getStatus(),r.getCreatedAt());
    }
    public static RentalResource from(Rental r) {
        return new RentalResource(r.getId(),r.getEquipmentId(),r.getConstructionUserId(),
          r.getRentalCompanyUserId(),periodStart(r.getPeriod()),periodEnd(r.getPeriod()),r.getStatus());
    }
    public static DeliveryResource from(Delivery d){return new DeliveryResource(d.id(),d.rentalId(),d.deliveredAt(),d.notes());}
    public static EquipmentReturnResource from(EquipmentReturn r){return new EquipmentReturnResource(
        r.id(),r.rentalId(),r.returnedAt(),r.notes(),r.maintenanceRequired());}
}
