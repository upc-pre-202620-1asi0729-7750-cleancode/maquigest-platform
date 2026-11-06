package com.maquigest.platform.iam.application.ports;

import java.util.List;
import java.util.Optional;

/**
 * IAM-owned outbound query port. Its adapter may read JPA rows, but the
 * application layer only receives immutable IAM-local value snapshots.
 */
public interface IamCompanyDirectoryReader {
    record CompanyRow(Long id, String firstName, String lastName,
                      String email, String companyName) { }

    Optional<CompanyRow> findActiveCompany(Long userId);

    List<CompanyRow> allActiveCompanies();
}
