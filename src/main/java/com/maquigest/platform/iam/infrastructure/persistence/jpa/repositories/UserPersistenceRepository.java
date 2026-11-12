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
}
