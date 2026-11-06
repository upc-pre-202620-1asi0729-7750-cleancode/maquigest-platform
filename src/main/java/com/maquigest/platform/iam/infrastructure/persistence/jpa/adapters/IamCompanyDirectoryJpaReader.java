package com.maquigest.platform.iam.infrastructure.persistence.jpa.adapters;

import com.maquigest.platform.iam.application.ports.IamCompanyDirectoryReader;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.iam.domain.model.valueobjects.UserStatus;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.repositories.UserPersistenceRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Retains the original directory filtering/query behavior *inside IAM*.
 * No IAM persistence entity or repository is exposed to Profiles.
 */
@Repository
public class IamCompanyDirectoryJpaReader implements IamCompanyDirectoryReader {
    private final UserPersistenceRepository users;

    public IamCompanyDirectoryJpaReader(UserPersistenceRepository users) {
        this.users = users;
    }

    @Override
    public Optional<CompanyRow> findActiveCompany(Long userId) {
        if (userId == null || userId <= 0) return Optional.empty();
        return users.findById(userId)
                .filter(IamCompanyDirectoryJpaReader::eligible)
                .map(IamCompanyDirectoryJpaReader::snapshot);
    }

    @Override
    public List<CompanyRow> allActiveCompanies() {
        return users.findAll().stream()
                .filter(IamCompanyDirectoryJpaReader::eligible)
                .map(IamCompanyDirectoryJpaReader::snapshot)
                .toList();
    }

    private static boolean eligible(UserPersistenceEntity user) {
        return user.getStatus() == UserStatus.ACTIVE
                && user.getRoles().stream().anyMatch(role ->
                role.getName() == Roles.ROLE_RENTAL_COMPANY
                        || role.getName() == Roles.ROLE_CONSTRUCTION_COMPANY);
    }

    private static CompanyRow snapshot(UserPersistenceEntity user) {
        return new CompanyRow(user.getId(), user.getFirstName(), user.getLastName(),
                user.getEmail(), user.getCompanyName());
    }
}
