package com.maquigest.platform.profiles.infrastructure.acl.iam;

import com.maquigest.platform.iam.interfaces.acl.IamCompanyDirectoryFacade;
import com.maquigest.platform.profiles.application.ports.IamCompanyDirectoryPort;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Profiles ACL: translates IAM's published contract to Profiles' own port.
 * It never reads IAM domain aggregates, roles, or JPA entities directly.
 */
@Component
public class IamCompanyDirectoryAclAdapter implements IamCompanyDirectoryPort {
    private final IamCompanyDirectoryFacade iamDirectory;

    public IamCompanyDirectoryAclAdapter(IamCompanyDirectoryFacade iamDirectory) {
        this.iamDirectory = iamDirectory;
    }

    @Override
    public Optional<CompanyIdentity> findActiveCompany(Long userId) {
        return iamDirectory.findActiveCompany(userId)
                .map(IamCompanyDirectoryAclAdapter::snapshot);
    }

    @Override
    public List<CompanyIdentity> allActiveCompanies() {
        return iamDirectory.allActiveCompanies().stream()
                .map(IamCompanyDirectoryAclAdapter::snapshot)
                .toList();
    }

    private static CompanyIdentity snapshot(IamCompanyDirectoryFacade.CompanyIdentity iamCompany) {
        return new CompanyIdentity(iamCompany.userId(), iamCompany.firstName(), iamCompany.lastName(),
                iamCompany.email(), iamCompany.companyName());
    }
}
