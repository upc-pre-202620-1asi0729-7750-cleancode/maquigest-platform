package com.maquigest.platform.iam.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.iam.domain.model.aggregates.PasswordRecovery;
import com.maquigest.platform.iam.domain.model.valueobjects.RecoveryTokenHash;
import com.maquigest.platform.iam.domain.repositories.PasswordRecoveryRepository;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.assemblers.PasswordRecoveryPersistenceAssembler;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.PasswordRecoveryPersistenceEntity;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories.PasswordRecoveryPersistenceRepository;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public class PasswordRecoveryRepositoryImpl implements PasswordRecoveryRepository {
    private final PasswordRecoveryPersistenceRepository persistence;
    private final DomainEventPublisher domainEventPublisher;
    public PasswordRecoveryRepositoryImpl(PasswordRecoveryPersistenceRepository persistence,
            DomainEventPublisher domainEventPublisher) {
        this.persistence = persistence;
        this.domainEventPublisher = domainEventPublisher;
    }
    @Override public Optional<PasswordRecovery> findByUserIdForUpdate(Long id) {
        return persistence.findByUserIdForUpdate(id).map(PasswordRecoveryPersistenceAssembler::toDomain);
    }
    @Override public Optional<Long> findUserIdByTokenHash(RecoveryTokenHash hash) {
        return persistence.findUserIdByTokenHash(hash.value());
    }
    @Override public PasswordRecovery save(PasswordRecovery value) {
        var entity = value.getId() == null ? new PasswordRecoveryPersistenceEntity() : persistence.findById(value.getId()).orElseThrow();
        PasswordRecoveryPersistenceAssembler.copyToPersistence(value, entity);
        var saved = PasswordRecoveryPersistenceAssembler.toDomain(persistence.saveAndFlush(entity));
        domainEventPublisher.publishAndClearEvents(value);
        return saved;
    }
}
