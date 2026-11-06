package com.maquigest.platform.subscriptions.infrastructure.acl.iam;

import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class SubscriptionOwnershipPolicy {
    private final IamActorFacade actors;

    public SubscriptionOwnershipPolicy(IamActorFacade actors) {
        this.actors = actors;
    }

    public Optional<Long> authenticatedOwnerId(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return Optional.empty();
        }
        return actors.findActiveByEmail(authentication.getName())
            .filter(user -> user.rentalCompany() || user.administrator())
            .map(IamActorFacade.ActorSnapshot::userId);
    }

    public boolean canManage(Authentication auth, Long userId) {
        return userId != null && authenticatedOwnerId(auth).filter(userId::equals).isPresent();
    }
}
