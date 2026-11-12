package com.maquigest.platform.iam.application.acl;

import com.maquigest.platform.iam.application.ports.IamCompanyDirectoryReader;
import com.maquigest.platform.iam.interfaces.acl.IamCompanyDirectoryFacade;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/** Provides the stable IAM published language to other bounded contexts. */
@Service
@Transactional(readOnly = true)
public class IamCompanyDirectoryFacadeImpl implements IamCompanyDirectoryFacade {
    private final IamCompanyDirectoryReader directoryReader;

    public IamCompanyDirectoryFacadeImpl(IamCompanyDirectoryReader directoryReader) {
        this.directoryReader = directoryReader;
    }

    @Override
    public Optional<CompanyIdentity> findActiveCompany(Long userId) {
        return directoryReader.findActiveCompany(userId).map(IamCompanyDirectoryFacadeImpl::snapshot);
    }

    @Override
    public List<CompanyIdentity> allActiveCompanies() {
        return directoryReader.allActiveCompanies().stream()
                .map(IamCompanyDirectoryFacadeImpl::snapshot)
                .toList();
    }

    private static CompanyIdentity snapshot(IamCompanyDirectoryReader.CompanyRow row) {
        return new CompanyIdentity(row.id(), row.firstName(), row.lastName(), row.email(), row.companyName());
    }
}
