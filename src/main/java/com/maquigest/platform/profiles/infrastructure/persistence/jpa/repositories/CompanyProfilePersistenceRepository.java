package com.maquigest.platform.profiles.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.profiles.infrastructure.persistence.jpa.entities.CompanyProfilePersistenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
public interface CompanyProfilePersistenceRepository extends JpaRepository<CompanyProfilePersistenceEntity,Long> {
    Optional<CompanyProfilePersistenceEntity> findByUserId(Long userId);
    boolean existsByUserId(Long userId);
}
