package com.maquigest.platform.iam.domain.repositories;
import com.maquigest.platform.iam.domain.model.aggregates.EmailVerification;
import com.maquigest.platform.iam.domain.model.valueobjects.VerificationTokenHash;
import java.util.Optional;
public interface EmailVerificationRepository {
    Optional<EmailVerification> findByUserIdForUpdate(Long userId);
    Optional<Long> findUserIdByTokenHash(VerificationTokenHash hash);
    EmailVerification save(EmailVerification verification);
}
