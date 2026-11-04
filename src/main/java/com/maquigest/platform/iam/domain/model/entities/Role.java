package com.maquigest.platform.iam.domain.model.entities;

import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import lombok.*;

import java.util.List;

/**
 * Role domain entity.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@With
@EqualsAndHashCode
@ToString
public class Role {
    private Long id;
    private Roles name;

    public Role(Roles name) {
        this.name = name;
    }

    public String getStringName() {
        return name.name();
    }

    public static Role getDefaultRole() {
        return new Role(Roles.ROLE_USER);
    }

    public static Role toRoleFromName(String name) {
        return new Role(Roles.valueOf(name));
    }

    public static List<Role> validateRoleSet(List<Role> roles) {
        if (roles == null || roles.isEmpty()) {
            return List.of(getDefaultRole());
        }
        return roles;
    }
}
