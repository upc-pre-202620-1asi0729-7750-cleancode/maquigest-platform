package com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories;

import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/** Spring Data repository for IAM users. */
@Repository
public interface UserPersistenceRepository extends JpaRepository<UserPersistenceEntity, Long> {
    boolean existsByEmailIgnoreCase(String email);

    Optional<UserPersistenceEntity> findByEmailIgnoreCase(String email);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from UserPersistenceEntity u where lower(u.email) = lower(:email)")
    Optional<UserPersistenceEntity> findByEmailForUpdate(@org.springframework.data.repository.query.Param("email") String email);

    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("select u from UserPersistenceEntity u where u.id = :id")
    Optional<UserPersistenceEntity> findByIdForUpdate(@org.springframework.data.repository.query.Param("id") Long id);
}
