package com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.EmailVerificationPersistenceEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface EmailVerificationPersistenceRepository extends JpaRepository<EmailVerificationPersistenceEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from EmailVerificationPersistenceEntity p where p.userId = :id")
    Optional<EmailVerificationPersistenceEntity> findByUserIdForUpdate(@Param("id") Long id);
    @Query("select p.userId from EmailVerificationPersistenceEntity p where p.tokenHash = :hash")
    Optional<Long> findUserIdByTokenHash(@Param("hash") String hash);
}
