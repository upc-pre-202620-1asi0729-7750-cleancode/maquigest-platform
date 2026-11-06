package com.maquigest.platform.profiles.infrastructure.events;
import com.maquigest.platform.profiles.domain.model.events.CompanyProfileCreatedEvent;
import com.maquigest.platform.profiles.interfaces.events.CompanyProfileIntegrationEvent;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import static org.mockito.Mockito.*;
class CompanyProfileIntegrationEventBridgeTests {
    @Test void translatesDomainEventIntoIntegrationNotification(){
        var publisher=mock(ApplicationEventPublisher.class);
        new CompanyProfileIntegrationEventBridge(publisher).created(new CompanyProfileCreatedEvent(3L,2L));
        verify(publisher).publishEvent(any(CompanyProfileIntegrationEvent.class));
    }
}
