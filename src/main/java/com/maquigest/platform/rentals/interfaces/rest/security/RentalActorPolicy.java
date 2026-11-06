package com.maquigest.platform.rentals.interfaces.rest.security;

import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RentalActorPolicy {
    public record Actor(Long userId, boolean constructionCompany, boolean rentalCompany) { }

    private final IamActorFacade actors;

    public RentalActorPolicy(IamActorFacade actors) {
        this.actors = actors;
    }

    public Optional<Actor> current(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) return Optional.empty();
        return actors.findActiveByEmail(auth.getName())
            .map(account -> new Actor(account.userId(), account.constructionCompany(), account.rentalCompany()))
            .filter(actor -> actor.constructionCompany() || actor.rentalCompany());
    }
}
