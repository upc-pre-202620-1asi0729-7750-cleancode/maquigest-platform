package com.maquigest.platform.iam.application.acl;

import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/** Implements IAM's published identity language within IAM itself. */
@Service
public class IamActorFacadeImpl implements IamActorFacade {
    private final UserRepository users;

    public IamActorFacadeImpl(UserRepository users) {
        this.users = users;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ActorSnapshot> findActiveByEmail(String email) {
        if (email == null) return Optional.empty();
        return users.findByEmail(email)
            .filter(user -> user.getStatus() == UserStatus.ACTIVE)
            .map(user -> new ActorSnapshot(
                user.getId(),
                hasRole(user, Roles.ROLE_RENTAL_COMPANY),
                hasRole(user, Roles.ROLE_CONSTRUCTION_COMPANY),
                hasRole(user, Roles.ROLE_ADMIN)
            ));
    }

    private static boolean hasRole(User user, Roles role) {
        return user.getRoles().stream().anyMatch(candidate -> candidate.getName() == role);
    }
}
