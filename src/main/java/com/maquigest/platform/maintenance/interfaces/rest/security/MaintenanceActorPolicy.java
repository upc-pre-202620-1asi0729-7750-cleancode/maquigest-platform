package com.maquigest.platform.maintenance.interfaces.rest.security;

import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceActorPolicy {
    public record Actor(Long userId, boolean rentalCompany, boolean constructionCompany) { }

    private final IamActorFacade actors;

    public MaintenanceActorPolicy(IamActorFacade actors) {
        this.actors = actors;
    }

    public Actor current(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw MaintenanceException.forbidden();
        }
        return actors.findActiveByEmail(authentication.getName())
            .map(account -> new Actor(account.userId(), account.rentalCompany(), account.constructionCompany()))
            .filter(actor -> actor.rentalCompany() || actor.constructionCompany())
            .orElseThrow(MaintenanceException::forbidden);
    }

    public Actor requireOwner(Authentication authentication) {
        var actor = current(authentication);
        if (!actor.rentalCompany()) throw MaintenanceException.forbidden();
        return actor;
    }
}
