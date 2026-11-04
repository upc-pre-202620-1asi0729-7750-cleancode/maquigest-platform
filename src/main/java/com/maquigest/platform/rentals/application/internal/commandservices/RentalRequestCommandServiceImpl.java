package com.maquigest.platform.rentals.application.internal.commandservices;
import com.maquigest.platform.rentals.application.commandservices.RentalRequestCommandService;
import com.maquigest.platform.rentals.application.ports.*;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.aggregates.*;
import com.maquigest.platform.rentals.domain.model.commands.*;
import com.maquigest.platform.rentals.domain.model.valueobjects.*;
import com.maquigest.platform.rentals.domain.repositories.*;
import java.time.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class RentalRequestCommandServiceImpl implements RentalRequestCommandService {
    private static final ZoneId LIMA=ZoneId.of("America/Lima");
    private final RentalRequestRepository requests;
    private final RentalRepository rentals;
    private final RentalEquipmentPort equipment;
    private final RentalSubscriptionPort subscriptions;
    public RentalRequestCommandServiceImpl(RentalRequestRepository requests, RentalRepository rentals,
        RentalEquipmentPort equipment, RentalSubscriptionPort subscriptions) {
        this.requests=requests;this.rentals=rentals;this.equipment=equipment;this.subscriptions=subscriptions;
    }
    @Override @Transactional
    public RentalRequest handle(CreateRentalRequestCommand command) {
        if(command==null || command.constructionUserId()==null || command.constructionUserId()<=0 || command.period()==null)
            throw RentalsException.invalid("Valid requester and period are required");
        if(command.period().startDate().isBefore(LocalDate.now(LIMA)))
            throw RentalsException.invalid("Rental start date cannot be in the past");
        var facts=equipment.inspect(command.equipmentId(),command.period());
        if(!facts.ownerUserId().equals(command.rentalCompanyUserId()))
            throw RentalsException.invalid("Equipment does not belong to the designated rental company");
        if(!facts.availableForPeriod()) throw RentalsException.conflict("Equipment is unavailable for this period");
        if(!subscriptions.hasActiveSubscription(command.rentalCompanyUserId()))
            throw RentalsException.conflict("Rental company needs an active subscription");
        var request=new RentalRequest(null,command.equipmentId(),command.constructionUserId(),
            command.rentalCompanyUserId(),command.period(),RentalRequestStatus.PENDING,Instant.now());
        return requests.save(request);
    }
    @Override @Transactional
    public RentalRequest handle(ResolveRentalRequestCommand command) {
        if(command==null || command.requestId()==null || command.requestId()<=0 || command.targetStatus()==null)
            throw RentalsException.invalid("Valid request id and target status are required");
        var request=requests.findByIdForUpdate(command.requestId())
            .orElseThrow(()->RentalsException.notFound("RentalRequest"));
        if(!request.getRentalCompanyUserId().equals(command.actorUserId())) throw RentalsException.forbidden();
        if(request.getStatus()!=RentalRequestStatus.PENDING)
            throw RentalsException.conflict("Only PENDING requests can be resolved");
        if(command.targetStatus()==RentalRequestStatus.REJECTED) {
            request.reject();return requests.save(request);
        }
        if(command.targetStatus()!=RentalRequestStatus.APPROVED)
            throw RentalsException.invalid("Target status must be APPROVED or REJECTED");
        if(!subscriptions.canManageRentals(request.getRentalCompanyUserId()))
            throw RentalsException.forbidden();
        // Locking and reserving Inventory happens in the SAME local Spring transaction.
        // Any exception after this point causes the reservation to roll back.
        Long blockId=equipment.reserve(request.getEquipmentId(),request.getRentalCompanyUserId(),request.getPeriod());
        if(rentals.findByRequestId(request.getId()).isPresent())
            throw RentalsException.conflict("This request already has a confirmed rental");
        var rental=new Rental(null,request.getId(),request.getEquipmentId(),
            request.getConstructionUserId(),request.getRentalCompanyUserId(),blockId,
            request.getPeriod(),RentalStatus.CONFIRMED);
        rentals.save(rental);
        request.approve();
        return requests.save(request);
    }
}
