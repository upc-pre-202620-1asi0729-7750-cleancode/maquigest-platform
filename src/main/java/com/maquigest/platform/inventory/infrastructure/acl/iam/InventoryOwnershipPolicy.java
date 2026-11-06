package com.maquigest.platform.inventory.infrastructure.acl.iam;

import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.Optional;

/** Inventory authorization policy consuming only IAM's published identity snapshot. */
@Component
public class InventoryOwnershipPolicy {
    private final IamActorFacade actors;
    private final EquipmentRepository equipment;

    public InventoryOwnershipPolicy(IamActorFacade actors, EquipmentRepository equipment) {
        this.actors = actors;
        this.equipment = equipment;
    }

    private Optional<IamActorFacade.ActorSnapshot> currentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return Optional.empty();
        }
        return actors.findActiveByEmail(authentication.getName());
    }

    public boolean canCreate(Authentication authentication, Long proposedOwnerId) {
        return proposedOwnerId != null && proposedOwnerId > 0 && currentUser(authentication)
            .filter(user -> user.rentalCompany() || user.administrator())
            .map(user -> proposedOwnerId.equals(user.userId()))
            .orElse(false);
    }

    public boolean canManage(Authentication authentication, Long equipmentId) {
        if (equipmentId == null || equipmentId <= 0) return false;
        var maybeUser = currentUser(authentication);
        if (maybeUser.isEmpty()) return false;
        var user = maybeUser.get();
        if (!user.rentalCompany() && !user.administrator()) return false;
        return equipment.findById(equipmentId)
            .map(asset -> user.administrator() || user.userId().equals(asset.getUserId()))
            .orElse(false);
    }
}
