package com.maquigest.platform.iam.infrastructure.persistence.jpa.adapters;

import com.maquigest.platform.iam.application.ports.IamSubscriberReader;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Repository;

/**
 * Maintains the original subscriber eligibility rules and PESSIMISTIC_WRITE
 * row locking entirely within IAM infrastructure.
 */
@Repository
public class IamSubscriberJpaReader implements IamSubscriberReader {
    private final EntityManager entityManager;

    public IamSubscriberJpaReader(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    @Override
    public boolean isActiveRentalCompany(Long userId) {
        return eligible(userId == null ? null : entityManager.find(UserPersistenceEntity.class, userId));
    }

    @Override
    public boolean lockActiveRentalCompany(Long userId) {
        if (userId == null || userId <= 0) return false;
        return eligible(entityManager.find(UserPersistenceEntity.class, userId, LockModeType.PESSIMISTIC_WRITE));
    }

    private static boolean eligible(UserPersistenceEntity user) {
        return user != null
                && user.getStatus() == UserStatus.ACTIVE
                && user.getRoles().stream().anyMatch(role ->
                    role.getName() == Roles.ROLE_RENTAL_COMPANY
                            || role.getName() == Roles.ROLE_ADMIN);
    }
}
