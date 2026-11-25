package com.maquigest.platform.iam.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.iam.domain.model.aggregates.PasswordRecovery;
import com.maquigest.platform.iam.domain.model.valueobjects.RecoveryTokenHash;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.PasswordRecoveryPersistenceEntity;
public final class PasswordRecoveryPersistenceAssembler {
    private PasswordRecoveryPersistenceAssembler() { }
    public static PasswordRecovery toDomain(PasswordRecoveryPersistenceEntity entity) {
        return PasswordRecovery.reconstitute(entity.getId(), entity.getUserId(),
                new RecoveryTokenHash(entity.getTokenHash()), entity.getIssuedAt(), entity.getExpiresAt(), entity.getConsumedAt());
    }
    public static void copyToPersistence(PasswordRecovery value, PasswordRecoveryPersistenceEntity entity) {
        entity.setUserId(value.getUserId());
        entity.setTokenHash(value.getTokenHash().value());
        entity.setIssuedAt(value.getIssuedAt());
        entity.setExpiresAt(value.getExpiresAt());
        entity.setConsumedAt(value.getConsumedAt());
    }
}
