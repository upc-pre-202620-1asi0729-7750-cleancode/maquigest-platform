package com.maquigest.platform.inventory.infrastructure.acl.iam;
import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import java.util.Optional;
/** Inventory's Anti-Corruption Layer for IAM identity and permissions.
 *  Never import User or Spring Security types into Inventory Domain. */
@Component
public class InventoryOwnershipPolicy {
    private final UserRepository users;
    private final EquipmentRepository equipment;
    public InventoryOwnershipPolicy(UserRepository users, EquipmentRepository equipment) {
        this.users=users; this.equipment=equipment;
    }
    private Optional<User> currentUser(Authentication authentication) {
        if (authentication==null || !authentication.isAuthenticated() || authentication.getName()==null)
            return Optional.empty();
        return users.findByEmail(authentication.getName()).filter(u->u.getStatus()==UserStatus.ACTIVE);
    }
    private static boolean hasRole(User user, Roles role) {
        return user.getRoles().stream().anyMatch(r -> r.getName()==role);
    }
    public boolean canCreate(Authentication authentication, Long proposedOwnerId) {
        return proposedOwnerId!=null && proposedOwnerId>0 && currentUser(authentication)
            .filter(user -> hasRole(user,Roles.ROLE_RENTAL_COMPANY) || hasRole(user,Roles.ROLE_ADMIN))
            .map(user -> proposedOwnerId.equals(user.getId())).orElse(false);
    }
    public boolean canManage(Authentication authentication, Long equipmentId) {
        if (equipmentId==null || equipmentId<=0) return false;
        var maybeUser = currentUser(authentication);
        if (maybeUser.isEmpty()) return false;
        var user = maybeUser.get();
        if (!hasRole(user,Roles.ROLE_RENTAL_COMPANY) && !hasRole(user,Roles.ROLE_ADMIN)) return false;
        return equipment.findById(equipmentId).map(asset ->
            hasRole(user,Roles.ROLE_ADMIN) || user.getId().equals(asset.getUserId())).orElse(false);
    }
}
