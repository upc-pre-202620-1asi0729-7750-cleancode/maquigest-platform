package com.maquigest.platform.rentals.application.internal.commandservices;

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
public class RentalRequestTransactionalExecutor {
    private static final ZoneId LIMA = ZoneId.of("America/Lima");
    private final RentalRequestRepository requests;
    private final RentalRepository rentals;
    private final RentalEquipmentPort equipment;
    private final RentalSubscriptionPort subscriptions;

    public RentalRequestTransactionalExecutor(RentalRequestRepository requests, RentalRepository rentals,
        RentalEquipmentPort equipment, RentalSubscriptionPort subscriptions) {
        this.requests = requests;
        this.rentals = rentals;
        this.equipment = equipment;
        this.subscriptions = subscriptions;
    }

    private static void requireValidDates(RentalPeriod period) {
        if (period == null) throw RentalsException.invalid("Rental period is required");
        if (period.startDate().isBefore(LocalDate.now(LIMA)))
            throw RentalsException.invalid("Rental start date cannot be in the past");
    }

    private void rejectRequesterOverlap(Long equipmentId, Long requesterUserId,
                                        RentalPeriod period, Long excludedRequestId) {
        if (requests.existsOverlappingActiveRequest(equipmentId, requesterUserId, period, excludedRequestId))
            throw RentalsException.conflict(
                "You already have a pending or approved request for this equipment and an overlapping period");
    }

        @Transactional
    public RentalRequest handle(CreateRentalRequestCommand command) {
        if (command == null || command.constructionUserId() == null || command.constructionUserId() <= 0 ||
            command.equipmentId() == null || command.equipmentId() <= 0 ||
            command.rentalCompanyUserId() == null || command.rentalCompanyUserId() <= 0)
            throw RentalsException.invalid("Valid requester, equipment and rental company are required");
        requireValidDates(command.period());
        if (command.constructionUserId().equals(command.rentalCompanyUserId()))
            throw RentalsException.invalid("A construction company cannot rent equipment from itself");
        // Must run before the overlap SELECT: MySQL REPEATABLE READ must not have an old snapshot.
        // The row lock is held until this @Transactional method commits or rolls back.
        requests.lockRequestSubmission(command.equipmentId(), command.constructionUserId());
        rejectRequesterOverlap(command.equipmentId(), command.constructionUserId(), command.period(), null);
        var facts = equipment.inspect(command.equipmentId(), command.period());
        if (!facts.ownerUserId().equals(command.rentalCompanyUserId()))
            throw RentalsException.invalid("Equipment does not belong to the designated rental company");
        if (!facts.availableForPeriod()) throw RentalsException.conflict("Equipment is unavailable for this period");
        if (!subscriptions.hasActiveSubscription(command.rentalCompanyUserId()))
            throw RentalsException.conflict("Rental company needs an active subscription");
        var request = new RentalRequest(null, command.equipmentId(), command.constructionUserId(),
            command.rentalCompanyUserId(), command.period(), RentalRequestStatus.PENDING, Instant.now());
        return requests.save(request);
    }

        @Transactional
    public RentalRequest handle(RescheduleRentalRequestCommand command) {
        if (command == null || command.requestId() == null || command.requestId() <= 0 ||
            command.actorUserId() == null || command.actorUserId() <= 0)
            throw RentalsException.invalid("Valid request id and requester are required");
        requireValidDates(command.period());
        var request = requests.findByIdForUpdate(command.requestId())
            .orElseThrow(() -> RentalsException.notFound("RentalRequest"));
        if (!request.getConstructionUserId().equals(command.actorUserId()))
            throw RentalsException.forbidden();
        if (request.getStatus() != RentalRequestStatus.PENDING)
            throw RentalsException.conflict("Only PENDING requests can change dates");
        // POST and PATCH must use the same lock key; otherwise concurrent operations can race.
        requests.lockRequestSubmission(request.getEquipmentId(), request.getConstructionUserId());
        // A no-op edit is safe and avoids an unnecessary DB update.
        if (request.getPeriod().equals(command.period())) return request;
        rejectRequesterOverlap(request.getEquipmentId(), request.getConstructionUserId(),
            command.period(), request.getId());
        var facts = equipment.inspect(request.getEquipmentId(), command.period());
        if (!facts.ownerUserId().equals(request.getRentalCompanyUserId()))
            throw RentalsException.invalid("Equipment does not belong to the designated rental company");
        if (!facts.availableForPeriod()) throw RentalsException.conflict("Equipment is unavailable for this period");
        if (!subscriptions.hasActiveSubscription(request.getRentalCompanyUserId()))
            throw RentalsException.conflict("Rental company needs an active subscription");
        request.reschedule(command.period());
        return requests.save(request);
    }

        @Transactional
    public RentalRequest handle(ResolveRentalRequestCommand command) {
        if (command == null || command.requestId() == null || command.requestId() <= 0 || command.targetStatus() == null)
            throw RentalsException.invalid("Valid request id and target status are required");
        var request = requests.findByIdForUpdate(command.requestId())
            .orElseThrow(() -> RentalsException.notFound("RentalRequest"));
        if (!request.getRentalCompanyUserId().equals(command.actorUserId())) throw RentalsException.forbidden();
        if (request.getStatus() != RentalRequestStatus.PENDING)
            throw RentalsException.conflict("Only PENDING requests can be resolved");
        if (command.targetStatus() == RentalRequestStatus.REJECTED) {
            request.reject();
            return requests.save(request);
        }
        if (command.targetStatus() != RentalRequestStatus.APPROVED)
            throw RentalsException.invalid("Target status must be APPROVED or REJECTED");
        if (!subscriptions.canManageRentals(request.getRentalCompanyUserId()))
            throw RentalsException.forbidden();
        // Locking and reserving Inventory happens in the SAME local Spring transaction.
        Long blockId = equipment.reserve(request.getEquipmentId(), request.getRentalCompanyUserId(), request.getPeriod());
        if (rentals.findByRequestId(request.getId()).isPresent())
            throw RentalsException.conflict("This request already has a confirmed rental");
        var rental = new Rental(null, request.getId(), request.getEquipmentId(),
            request.getConstructionUserId(), request.getRentalCompanyUserId(), blockId,
            request.getPeriod(), RentalStatus.CONFIRMED);
        rentals.save(rental);
        request.approve();
        return requests.save(request);
    }
}
