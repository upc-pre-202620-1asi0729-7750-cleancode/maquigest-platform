package com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.PasswordRecoveryPersistenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface PasswordRecoveryPersistenceRepository extends JpaRepository<PasswordRecoveryPersistenceEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PasswordRecoveryPersistenceEntity p where p.userId = :id")
    Optional<PasswordRecoveryPersistenceEntity> findByUserIdForUpdate(@Param("id") Long id);
    @Query("select p.userId from PasswordRecoveryPersistenceEntity p where p.tokenHash = :hash")
    Optional<Long> findUserIdByTokenHash(@Param("hash") String hash);
}
