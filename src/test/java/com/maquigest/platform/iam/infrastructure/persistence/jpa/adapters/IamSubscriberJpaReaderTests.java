package com.maquigest.platform.iam.infrastructure.persistence.jpa.adapters;

import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IamSubscriberJpaReaderTests {
    private final EntityManager em = mock(EntityManager.class);
    private final IamSubscriberJpaReader reader = new IamSubscriberJpaReader(em);

    private static UserPersistenceEntity user(UserStatus status, Roles role) {
        var user = new UserPersistenceEntity();
        user.setStatus(status);
        var userRole = new RolePersistenceEntity();
        userRole.setName(role);
        user.setRoles(Set.of(userRole));
        return user;
    }

    @Test
    void eligibleRentalCompanyAndAdminOnlyWhenActive() {
        when(em.find(UserPersistenceEntity.class, 1L))
                .thenReturn(user(UserStatus.ACTIVE, Roles.ROLE_RENTAL_COMPANY));
        when(em.find(UserPersistenceEntity.class, 2L))
                .thenReturn(user(UserStatus.ACTIVE, Roles.ROLE_ADMIN));
        when(em.find(UserPersistenceEntity.class, 3L))
                .thenReturn(user(UserStatus.ACTIVE, Roles.ROLE_CONSTRUCTION_COMPANY));
        when(em.find(UserPersistenceEntity.class, 4L))
                .thenReturn(user(UserStatus.INACTIVE, Roles.ROLE_RENTAL_COMPANY));
        assertTrue(reader.isActiveRentalCompany(1L));
        assertTrue(reader.isActiveRentalCompany(2L));
        assertFalse(reader.isActiveRentalCompany(3L));
        assertFalse(reader.isActiveRentalCompany(4L));
        assertFalse(reader.isActiveRentalCompany(5L));
    }

    @Test
    void nullReadDoesNotTouchTheDatabase() {
        assertFalse(reader.isActiveRentalCompany(null));
        verifyNoInteractions(em);
    }

    @Test
    void writeLockUsesPessimisticWriteForEligibleUser() {
        when(em.find(UserPersistenceEntity.class, 7L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(user(UserStatus.ACTIVE, Roles.ROLE_RENTAL_COMPANY));
        assertTrue(reader.lockActiveRentalCompany(7L));
        verify(em).find(UserPersistenceEntity.class, 7L, LockModeType.PESSIMISTIC_WRITE);
        verifyNoMoreInteractions(em);
    }

    @Test
    void lockedIneligibleOrMissingUserIsRejected() {
        when(em.find(UserPersistenceEntity.class, 8L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(user(UserStatus.INACTIVE, Roles.ROLE_RENTAL_COMPANY));
        when(em.find(UserPersistenceEntity.class, 9L, LockModeType.PESSIMISTIC_WRITE))
                .thenReturn(user(UserStatus.ACTIVE, Roles.ROLE_CONSTRUCTION_COMPANY));
        assertFalse(reader.lockActiveRentalCompany(8L));
        assertFalse(reader.lockActiveRentalCompany(9L));
        assertFalse(reader.lockActiveRentalCompany(10L));
    }

    @Test
    void invalidLockIdsDoNotTouchDatabase() {
        assertFalse(reader.lockActiveRentalCompany(null));
        assertFalse(reader.lockActiveRentalCompany(0L));
        assertFalse(reader.lockActiveRentalCompany(-5L));
        verifyNoInteractions(em);
    }
}
