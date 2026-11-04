package com.maquigest.platform.subscriptions.infrastructure.acl.iam;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.maquigest.platform.subscriptions.application.ports.IamSubscriberPort;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.springframework.stereotype.Component;
import java.util.Objects;
/** ACL: IAM is referenced only by Infrastructure. */
@Component
public class IamSubscriberAclAdapter implements IamSubscriberPort {
    private final EntityManager entityManager;
    public IamSubscriberAclAdapter(EntityManager entityManager) { this.entityManager=entityManager; }
    @Override public boolean isActiveRentalCompany(Long userId) {
        return eligible(userId==null?null:entityManager.find(UserPersistenceEntity.class,userId));
    }
    @Override public boolean lockActiveRentalCompany(Long userId) {
        if (userId == null || userId <= 0) return false;
        return eligible(entityManager.find(UserPersistenceEntity.class,userId,LockModeType.PESSIMISTIC_WRITE));
    }
    private static boolean eligible(UserPersistenceEntity user) {
        return user!=null && user.getStatus()==UserStatus.ACTIVE && user.getRoles().stream().anyMatch(
            role -> role.getName()==Roles.ROLE_RENTAL_COMPANY || role.getName()==Roles.ROLE_ADMIN);
    }
}
