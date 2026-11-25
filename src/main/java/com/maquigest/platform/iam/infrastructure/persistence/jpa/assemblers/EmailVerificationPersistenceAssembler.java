package com.maquigest.platform.iam.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.iam.domain.model.aggregates.EmailVerification;
import com.maquigest.platform.iam.domain.model.valueobjects.VerificationTokenHash;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.EmailVerificationPersistenceEntity;
public final class EmailVerificationPersistenceAssembler {
    private EmailVerificationPersistenceAssembler() { }
    public static EmailVerification toDomain(EmailVerificationPersistenceEntity entity) {
        return EmailVerification.reconstitute(entity.getId(), entity.getUserId(),
                new VerificationTokenHash(entity.getTokenHash()), entity.getIssuedAt(), entity.getExpiresAt(), entity.getConsumedAt());
    }
    public static void copyToPersistence(EmailVerification value, EmailVerificationPersistenceEntity entity) {
        entity.setUserId(value.getUserId());
        entity.setTokenHash(value.getTokenHash().value());
        entity.setIssuedAt(value.getIssuedAt());
        entity.setExpiresAt(value.getExpiresAt());
        entity.setConsumedAt(value.getConsumedAt());
    }
}
