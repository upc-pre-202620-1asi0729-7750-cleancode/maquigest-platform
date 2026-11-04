package com.maquigest.platform.iam.infrastructure.persistence.jpa.adapters;

import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.repositories.UserRepository;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.assemblers.UserPersistenceAssembler;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories.RolePersistenceRepository;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import com.maquigest.platform.shared.infrastructure.events.DomainEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.stream.Collectors;

/** Repository adapter that bridges the IAM domain port and JPA. */
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final UserPersistenceRepository userPersistenceRepository;
    private final RolePersistenceRepository rolePersistenceRepository;
    private final DomainEventPublisher domainEventPublisher;

    public UserRepositoryImpl(UserPersistenceRepository userPersistenceRepository,
                              RolePersistenceRepository rolePersistenceRepository,
                              DomainEventPublisher domainEventPublisher) {
        this.userPersistenceRepository = userPersistenceRepository;
        this.rolePersistenceRepository = rolePersistenceRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    @Override
    public User save(User user) {
        var entity = UserPersistenceAssembler.toPersistenceFromDomain(user);
        // Associate already-persisted lookup roles; never INSERT role duplicates from sign-up.
        entity.setRoles(entity.getRoles().stream()
                .map(role -> rolePersistenceRepository.findByName(role.getName())
                        .orElseThrow(() -> new IllegalArgumentException("Unknown role: " + role.getName())))
                .collect(Collectors.toSet()));
        var savedUser = UserPersistenceAssembler.toDomainFromPersistence(
                userPersistenceRepository.save(entity));
        domainEventPublisher.publishAndClearEvents(user);
        return savedUser;
    }

    @Override
    public boolean existsByEmail(String email) {
        return userPersistenceRepository.existsByEmailIgnoreCase(email);
    }
}
