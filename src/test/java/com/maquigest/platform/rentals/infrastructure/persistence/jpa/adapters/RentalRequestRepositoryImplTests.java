package com.maquigest.platform.rentals.infrastructure.persistence.jpa.adapters;

import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.RentalRequestPersistenceEntity;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories.RentalRequestJpaRepository;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories.RentalRequestSubmissionGuardJpaRepository;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RentalRequestRepositoryImplTests {
    @Test void pairLockIsDelegatedOnlyToTheInfrastructureGuardRepository() {
        var jpa = mock(RentalRequestJpaRepository.class);
        var guards = mock(RentalRequestSubmissionGuardJpaRepository.class);
        var publisher = mock(DomainEventPublisher.class);
        var adapter = new RentalRequestRepositoryImpl(jpa, guards, publisher);
        adapter.lockRequestSubmission(3L, 2L);
        verify(guards).acquirePairLock(3L, 2L);
        verifyNoInteractions(jpa, publisher);
    }

    @Test void changingPendingRequestPersistsBothDates() {
        var jpa = mock(RentalRequestJpaRepository.class);
        var publisher = mock(DomainEventPublisher.class);
        var entity = new RentalRequestPersistenceEntity();
        entity.setId(7L);
        entity.setEquipmentId(3L);
        entity.setConstructionUserId(2L);
        entity.setRentalCompanyUserId(1L);
        entity.setStartDate(LocalDate.of(2026, 11, 7));
        entity.setEndDate(LocalDate.of(2026, 11, 9));
        entity.setStatus(RentalRequestStatus.PENDING);
        entity.setCreatedAt(Instant.parse("2026-11-04T10:00:00Z"));
        var changedPeriod = new RentalPeriod(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));
        var aggregate = new RentalRequest(7L, 3L, 2L, 1L, changedPeriod,
            RentalRequestStatus.PENDING, entity.getCreatedAt());
        when(jpa.findById(7L)).thenReturn(Optional.of(entity));
        when(jpa.saveAndFlush(entity)).thenReturn(entity);
        var result = new RentalRequestRepositoryImpl(jpa, mock(RentalRequestSubmissionGuardJpaRepository.class), publisher).save(aggregate);
        assertEquals(changedPeriod.startDate(), entity.getStartDate());
        assertEquals(changedPeriod.endDate(), entity.getEndDate());
        assertEquals(7L, result.getId());
        verify(jpa).saveAndFlush(entity);
    }

    @Test void overlapQueryChecksOnlyPendingAndApprovedRequests() {
        var jpa = mock(RentalRequestJpaRepository.class);
        var publisher = mock(DomainEventPublisher.class);
        var period = new RentalPeriod(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 11, 12));
        when(jpa.countOverlappingActiveRequests(3L, 2L,
            List.of(RentalRequestStatus.PENDING, RentalRequestStatus.APPROVED),
            period.startDate(), period.endDate(), 0L)).thenReturn(1L);
        assertTrue(new RentalRequestRepositoryImpl(jpa, mock(RentalRequestSubmissionGuardJpaRepository.class), publisher)
            .existsOverlappingActiveRequest(3L, 2L, period, null));
        verify(jpa).countOverlappingActiveRequests(3L, 2L,
            List.of(RentalRequestStatus.PENDING, RentalRequestStatus.APPROVED),
            period.startDate(), period.endDate(), 0L);
    }
}
