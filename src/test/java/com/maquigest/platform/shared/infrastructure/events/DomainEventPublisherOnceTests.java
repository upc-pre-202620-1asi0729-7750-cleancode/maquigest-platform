package com.maquigest.platform.shared.infrastructure.events;

import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.model.commands.CreateCompanyProfileCommand;
import com.maquigest.platform.profiles.domain.model.events.CompanyProfileCreatedEvent;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DomainEventPublisherOnceTests {
    @Test void repeatedDispatchDoesNotRepublishConsumedEvent() {
        var publisher = mock(ApplicationEventPublisher.class);
        var events = new DomainEventPublisher(publisher);
        var profile = CompanyProfile.reconstitute(77L, new CreateCompanyProfileCommand(
            3L, "Ana", "Perez", "ana@example.com", "", "Company", CompanyAddress.empty()));
        profile.onCreated();
        events.publishAndClearEvents(profile);
        events.publishAndClearEvents(profile);
        verify(publisher, times(1)).publishEvent(new CompanyProfileCreatedEvent(77L, 3L));
        assertTrue(profile.domainEvents().isEmpty());
    }

    @Test void nullAggregateProducesNoEvents() {
        var publisher = mock(ApplicationEventPublisher.class);
        new DomainEventPublisher(publisher).publishAndClearEvents(null);
        verifyNoInteractions(publisher);
    }
}
