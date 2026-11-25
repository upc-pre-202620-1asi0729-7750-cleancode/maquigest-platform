package com.maquigest.platform.rentals.infrastructure.persistence.jpa.adapters;

import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus;
import com.maquigest.platform.rentals.domain.repositories.RentalRequestRepository;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.assemblers.RentalRequestPersistenceAssembler;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories.RentalRequestPersistenceRepository;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories.RentalRequestSubmissionGuardPersistenceRepository;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class RentalRequestRepositoryImpl implements RentalRequestRepository {
    private final RentalRequestPersistenceRepository jpa;
    private final RentalRequestSubmissionGuardPersistenceRepository submissionGuards;
    private final DomainEventPublisher publisher;

    public RentalRequestRepositoryImpl(RentalRequestPersistenceRepository jpa,
                                       RentalRequestSubmissionGuardPersistenceRepository submissionGuards,
                                       DomainEventPublisher publisher) {
        this.jpa = jpa;
        this.submissionGuards = submissionGuards;
        this.publisher = publisher;
    }

    @Override
    public void lockRequestSubmission(Long equipmentId, Long requesterUserId) {
        // The UNIQUE pair is inserted/updated atomically; InnoDB holds the X-lock
        // until the surrounding Spring transaction completes.
        submissionGuards.acquirePairLock(equipmentId, requesterUserId);
    }

    @Override
    public Optional<RentalRequest> findById(Long id) {
        return jpa.findById(id).map(RentalRequestPersistenceAssembler::toDomain);
    }

    @Override
    public Optional<RentalRequest> findByIdForUpdate(Long id) {
        return jpa.findByIdForUpdate(id).map(RentalRequestPersistenceAssembler::toDomain);
    }

    @Override
    public List<RentalRequest> findVisibleTo(Long actorId) {
        return jpa.findByConstructionUserIdOrRentalCompanyUserIdOrderByIdDesc(actorId, actorId)
            .stream().map(RentalRequestPersistenceAssembler::toDomain).toList();
    }

    @Override
    public boolean existsOverlappingActiveRequest(Long equipmentId, Long requesterUserId,
                                                   RentalPeriod period, Long excludedRequestId) {
        return jpa.countOverlappingActiveRequests(equipmentId, requesterUserId,
            List.of(RentalRequestStatus.PENDING, RentalRequestStatus.APPROVED),
            period.startDate(), period.endDate(), excludedRequestId == null ? 0L : excludedRequestId) > 0;
    }

    @Override
    public RentalRequest save(RentalRequest request) {
        var entity = request.getId() == null
            ? RentalRequestPersistenceAssembler.toNewPersistence(request)
            : jpa.findById(request.getId()).orElseThrow();
        entity.setStatus(request.getStatus());
        entity.setStartDate(request.getPeriod().startDate());
        entity.setEndDate(request.getPeriod().endDate());
        var saved = jpa.saveAndFlush(entity);
        var persisted = RentalRequestPersistenceAssembler.toDomain(saved);
        if (request.getId() == null) {
            persisted.onCreated();
            publisher.publishAndClearEvents(persisted);
        } else {
            publisher.publishAndClearEvents(request);
        }
        return persisted;
    }
}
