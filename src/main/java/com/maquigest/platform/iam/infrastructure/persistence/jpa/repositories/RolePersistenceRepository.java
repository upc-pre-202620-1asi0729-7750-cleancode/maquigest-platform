package com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories;

import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.RolePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data repository for IAM role persistence entities.
 */
@Repository
public interface RolePersistenceRepository extends JpaRepository<RolePersistenceEntity, Long> {
    Optional<RolePersistenceEntity> findByName(Roles name);

    boolean existsByName(Roles name);
}
