package com.maquigest.platform.profiles.interfaces.rest.security;

import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import com.maquigest.platform.profiles.domain.repositories.CompanyProfileRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Objects;

/** Never trust userId sent in a request instead of the authenticated IAM identity. */
@Component
public class ProfileOwnershipPolicy {
    private final IamActorFacade actors;
    private final CompanyProfileRepository profiles;

    public ProfileOwnershipPolicy(IamActorFacade actors, CompanyProfileRepository profiles) {
        this.actors = actors;
        this.profiles = profiles;
    }

    public boolean canCreate(Authentication auth, Long userId) {
        return owns(auth, userId);
    }

    public boolean canModify(Authentication auth, Long profileId) {
        if (profileId == null || profileId <= 0) return false;
        return profiles.findById(profileId)
            .map(profile -> owns(auth, profile.getUserId()))
            .orElse(false);
    }

    private boolean owns(Authentication auth, Long targetUserId) {
        if (auth == null || !auth.isAuthenticated() || auth.getName() == null) return false;
        return actors.findActiveByEmail(auth.getName())
            .filter(user -> user.constructionCompany() || user.rentalCompany())
            .map(user -> Objects.equals(user.userId(), targetUserId))
            .orElse(false);
    }
}
