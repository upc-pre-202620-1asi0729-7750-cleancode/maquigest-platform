package com.maquigest.platform.inventory.infrastructure.acl.iam;

import com.maquigest.platform.iam.interfaces.acl.IamActorFacade;
import com.maquigest.platform.iam.interfaces.acl.IamActorFacade.ActorSnapshot;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.commands.CreateEquipmentCommand;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryOwnershipPolicyTests {
    private static UsernamePasswordAuthenticationToken auth(String email) {
        return new UsernamePasswordAuthenticationToken(email, null, List.of());
    }

    @Test void rejectsForgedOwnerId() {
        var iam = mock(IamActorFacade.class);
        var equipment = mock(EquipmentRepository.class);
        when(iam.findActiveByEmail("ana@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(1L, true, false, false)));
        var policy = new InventoryOwnershipPolicy(iam, equipment);
        assertTrue(policy.canCreate(auth("ana@example.com"), 1L));
        assertFalse(policy.canCreate(auth("ana@example.com"), 2L));
        var asset = new Equipment(new CreateEquipmentCommand(2L, "EXC-002", "Other asset", "Desc",
            1L, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        when(equipment.findById(99L)).thenReturn(Optional.of(asset));
        assertFalse(policy.canManage(auth("ana@example.com"), 99L));
    }

    @Test void constructionCompanyCannotOperateRentalEquipment() {
        var iam = mock(IamActorFacade.class);
        var equipment = mock(EquipmentRepository.class);
        when(iam.findActiveByEmail("constructor@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(42L, false, true, false)));
        var policy = new InventoryOwnershipPolicy(iam, equipment);
        assertFalse(policy.canCreate(auth("constructor@example.com"), 42L));
        assertFalse(policy.canManage(auth("constructor@example.com"), 9L));
        verifyNoInteractions(equipment);
    }

    @Test void administratorCanManageExistingAssetButNotInventedOwnerId() {
        var iam = mock(IamActorFacade.class);
        var equipment = mock(EquipmentRepository.class);
        when(iam.findActiveByEmail("admin@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(5L, false, false, true)));
        var asset = new Equipment(new CreateEquipmentCommand(1L, "EXC-03", "Other asset", "Desc",
            1L, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        when(equipment.findById(17L)).thenReturn(Optional.of(asset));
        var policy = new InventoryOwnershipPolicy(iam, equipment);
        assertTrue(policy.canManage(auth("admin@example.com"), 17L));
        assertFalse(policy.canCreate(auth("admin@example.com"), 99L));
        assertTrue(policy.canCreate(auth("admin@example.com"), 5L));
    }

    @Test void rejectsUnauthenticatedAndMissingEquipment() {
        var iam = mock(IamActorFacade.class);
        var equipment = mock(EquipmentRepository.class);
        var policy = new InventoryOwnershipPolicy(iam, equipment);
        assertFalse(policy.canCreate(null, 1L));
        assertFalse(policy.canManage(null, 1L));
        when(iam.findActiveByEmail("ana@example.com"))
            .thenReturn(Optional.of(new ActorSnapshot(1L, true, false, false)));
        assertFalse(policy.canManage(auth("ana@example.com"), 99L));
    }
}
