package com.maquigest.platform.inventory.infrastructure.acl.iam;
import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.entities.Role;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.commands.CreateEquipmentCommand;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class InventoryOwnershipPolicyTests {
    @Test void doesNotAllowForgedOwnerId() {
        var users=mock(UserRepository.class);
        var equipment=mock(EquipmentRepository.class);
        var user=User.reconstitute(1L,"Ana","Garcia","ana@example.com","hash","Rental SAC",
            Set.of(new Role(Roles.ROLE_RENTAL_COMPANY)),null);
        when(users.findByEmail("ana@example.com")).thenReturn(Optional.of(user));
        var policy=new InventoryOwnershipPolicy(users,equipment);
        var auth=new UsernamePasswordAuthenticationToken("ana@example.com",null,List.of());
        assertTrue(policy.canCreate(auth,1L));
        assertFalse(policy.canCreate(auth,2L));
        var asset=new Equipment(new CreateEquipmentCommand(2L,"EXC-002","Other asset","Desc",1L,"Lima",BigDecimal.ONE,BigDecimal.TEN));
        // Domain id is null before saving; lookup still works because the policy compares owner ids.
        when(equipment.findById(99L)).thenReturn(Optional.of(asset));
        assertFalse(policy.canManage(auth,99L));
    }
    @Test void constructionCompanyCannotOperateRentalCompanyEquipment() {
        var users=mock(UserRepository.class);
        var equipment=mock(EquipmentRepository.class);
        var constructionUser=User.reconstitute(42L,"Construction","User","constructor@example.com",
            "hash","Construcciones SAC",Set.of(new Role(Roles.ROLE_CONSTRUCTION_COMPANY)),null);
        when(users.findByEmail("constructor@example.com")).thenReturn(Optional.of(constructionUser));
        var rentalAsset=new Equipment(new CreateEquipmentCommand(1L,"EXC-01","Rental asset","Desc",
            1L,"Lima",BigDecimal.ONE,BigDecimal.TEN));
        when(equipment.findById(9L)).thenReturn(Optional.of(rentalAsset));
        var policy=new InventoryOwnershipPolicy(users,equipment);
        var authentication=new UsernamePasswordAuthenticationToken("constructor@example.com",null,List.of());
        assertFalse(policy.canManage(authentication,9L));
        assertFalse(policy.canCreate(authentication,42L));
    }
}
