package com.maquigest.platform.iam.interfaces.acl;

import java.util.List;
import java.util.Optional;

/**
 * Published IAM contract for reading active company identity data.
 * Consumers see only immutable snapshots, never IAM domain or persistence types.
 */
public interface IamCompanyDirectoryFacade {
    record CompanyIdentity(Long userId, String firstName, String lastName,
                           String email, String companyName) { }

    Optional<CompanyIdentity> findActiveCompany(Long userId);

    List<CompanyIdentity> allActiveCompanies();
}
