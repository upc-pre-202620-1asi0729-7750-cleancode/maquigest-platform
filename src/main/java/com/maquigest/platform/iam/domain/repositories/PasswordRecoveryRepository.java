package com.maquigest.platform.iam.domain.repositories;
import com.maquigest.platform.iam.domain.model.aggregates.PasswordRecovery;
import com.maquigest.platform.iam.domain.model.valueobjects.RecoveryTokenHash;
import java.util.Optional;
public interface PasswordRecoveryRepository {
    Optional<PasswordRecovery> findByUserIdForUpdate(Long userId);
    Optional<Long> findUserIdByTokenHash(RecoveryTokenHash hash);
    PasswordRecovery save(PasswordRecovery recovery);
}
