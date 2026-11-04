package com.maquigest.platform.subscriptions.infrastructure.acl.iam;
import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Optional;
@Component
public class SubscriptionOwnershipPolicy {
    private final UserRepository users;
    public SubscriptionOwnershipPolicy(UserRepository users) { this.users=users; }
    public Optional<Long> authenticatedOwnerId(Authentication authentication) {
        if (authentication==null || !authentication.isAuthenticated() || authentication.getName()==null) return Optional.empty();
        return users.findByEmail(authentication.getName())
            .filter(user->user.getStatus()==UserStatus.ACTIVE)
            .filter(user->user.getRoles().stream().anyMatch(role ->
                role.getName()==Roles.ROLE_RENTAL_COMPANY || role.getName()==Roles.ROLE_ADMIN))
            .map(User::getId);
    }
    public boolean canManage(Authentication auth, Long userId) {
        return userId != null && authenticatedOwnerId(auth).filter(userId::equals).isPresent();
    }
}
