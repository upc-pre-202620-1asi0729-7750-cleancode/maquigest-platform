package com.maquigest.platform.profiles.infrastructure.persistence.jpa.adapters;

import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.model.commands.CreateCompanyProfileCommand;
import com.maquigest.platform.profiles.domain.model.commands.UpdateCompanyProfileCommand;
import com.maquigest.platform.profiles.domain.model.events.CompanyProfileCreatedEvent;
import com.maquigest.platform.profiles.domain.model.events.CompanyProfileUpdatedEvent;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
import com.maquigest.platform.profiles.infrastructure.persistence.jpa.entities.CompanyProfilePersistenceEntity;
import com.maquigest.platform.profiles.infrastructure.persistence.jpa.repositories.CompanyProfilePersistenceRepository;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/** One domain notification per persisted profile transition, after identity assignment. */
class CompanyProfileRepositoryEventPublicationTests {
    @Test void persistedCreationPublishesExactlyOnceWithAssignedIdAndClearsEvents() {
        var jpa = mock(CompanyProfilePersistenceRepository.class);
        var springPublisher = mock(ApplicationEventPublisher.class);
        var adapter = new CompanyProfileRepositoryImpl(jpa, new DomainEventPublisher(springPublisher));
        when(jpa.saveAndFlush(any(CompanyProfilePersistenceEntity.class))).thenAnswer(inv -> {
            CompanyProfilePersistenceEntity entity = inv.getArgument(0);
            entity.setId(55L);
            return entity;
        });
        var profile = new CompanyProfile(command());
        assertNull(profile.getId());
        assertTrue(profile.domainEvents().isEmpty());

        CompanyProfile returned = adapter.save(profile);

        assertEquals(55L, profile.getId());
        assertEquals(55L, returned.getId());
        verify(springPublisher, times(1)).publishEvent(new CompanyProfileCreatedEvent(55L, 7L));
        verifyNoMoreInteractions(springPublisher);
        assertTrue(profile.domainEvents().isEmpty());
    }

    @Test void updatePublishesOnlyUpdatedEventAndClearsEvents() {
        var jpa = mock(CompanyProfilePersistenceRepository.class);
        var springPublisher = mock(ApplicationEventPublisher.class);
        var adapter = new CompanyProfileRepositoryImpl(jpa, new DomainEventPublisher(springPublisher));
        var existing = new CompanyProfilePersistenceEntity();
        existing.setId(55L);
        when(jpa.findById(55L)).thenReturn(Optional.of(existing));
        when(jpa.saveAndFlush(any(CompanyProfilePersistenceEntity.class)))
            .thenAnswer(inv -> inv.getArgument(0));
        CompanyProfile profile = CompanyProfile.reconstitute(55L, command());
        profile.update(new UpdateCompanyProfileCommand(55L, 7L, "Juan", "Perez",
            "juan@example.com", "", "Example Company", CompanyAddress.empty()));

        adapter.save(profile);

        verify(springPublisher, times(1)).publishEvent(new CompanyProfileUpdatedEvent(55L, 7L));
        verifyNoMoreInteractions(springPublisher);
        assertTrue(profile.domainEvents().isEmpty());
    }

    private CreateCompanyProfileCommand command() {
        return new CreateCompanyProfileCommand(7L, "Juan", "Perez", "juan@example.com",
            "", "Example Company", CompanyAddress.empty());
    }
}
