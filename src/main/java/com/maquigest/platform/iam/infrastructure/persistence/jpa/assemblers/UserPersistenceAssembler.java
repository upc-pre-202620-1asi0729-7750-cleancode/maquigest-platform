package com.maquigest.platform.iam.infrastructure.persistence.jpa.assemblers;

import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;

import java.util.HashSet;
import java.util.stream.Collectors;

/** Static assembler across the IAM domain/persistence boundary. */
public final class UserPersistenceAssembler {
    private UserPersistenceAssembler() {
    }

    public static User toDomainFromPersistence(UserPersistenceEntity entity) {
        if (entity == null) return null;
        var roles = entity.getRoles().stream()
                .map(RolePersistenceAssembler::toDomainFromPersistence)
                .collect(Collectors.toSet());
        return User.reconstitute(entity.getId(), entity.getFirstName(), entity.getLastName(),
                entity.getEmail(), entity.getPassword(), entity.getCompanyName(),
                roles, entity.getStatus());
    }

    public static UserPersistenceEntity toPersistenceFromDomain(User user) {
        if (user == null) return null;
        var entity = new UserPersistenceEntity();
        if (user.getId() != null) {
            entity.setId(user.getId());
        }
        entity.setFirstName(user.getFirstName());
        entity.setLastName(user.getLastName());
        entity.setEmail(user.getEmail());
        entity.setPassword(user.getPassword());
        entity.setCompanyName(user.getCompanyName());
        entity.setStatus(user.getStatus());
        entity.setRoles(user.getRoles() == null ? new HashSet<>() : user.getRoles().stream()
                .map(RolePersistenceAssembler::toPersistenceFromDomain)
                .collect(Collectors.toSet()));
        return entity;
    }
}
