package com.maquigest.platform.iam.infrastructure.persistence.jpa.adapters;
import com.maquigest.platform.iam.domain.model.aggregates.EmailVerification;
import com.maquigest.platform.iam.domain.model.valueobjects.VerificationTokenHash;
import com.maquigest.platform.iam.domain.repositories.EmailVerificationRepository;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.assemblers.EmailVerificationPersistenceAssembler;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.EmailVerificationPersistenceEntity;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories.EmailVerificationPersistenceRepository;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;
import java.util.Optional;
@Repository
public class EmailVerificationRepositoryImpl implements EmailVerificationRepository {
    private final EmailVerificationPersistenceRepository persistence;
    private final DomainEventPublisher domainEventPublisher;
    public EmailVerificationRepositoryImpl(EmailVerificationPersistenceRepository persistence,
            DomainEventPublisher domainEventPublisher) {
        this.persistence = persistence;
        this.domainEventPublisher = domainEventPublisher;
    }
    @Override public Optional<EmailVerification> findByUserIdForUpdate(Long id) {
        return persistence.findByUserIdForUpdate(id).map(EmailVerificationPersistenceAssembler::toDomain);
    }
    @Override public Optional<Long> findUserIdByTokenHash(VerificationTokenHash hash) {
        return persistence.findUserIdByTokenHash(hash.value());
    }
    @Override public EmailVerification save(EmailVerification value) {
        var entity = value.getId() == null ? new EmailVerificationPersistenceEntity() : persistence.findById(value.getId()).orElseThrow();
        EmailVerificationPersistenceAssembler.copyToPersistence(value, entity);
        var saved = EmailVerificationPersistenceAssembler.toDomain(persistence.saveAndFlush(entity));
        domainEventPublisher.publishAndClearEvents(value);
        return saved;
    }
}
