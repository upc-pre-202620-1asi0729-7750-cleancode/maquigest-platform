package com.maquigest.platform.profiles.application.ports;
import java.util.List;import java.util.Optional;
/** Published-language snapshot; application does not import IAM aggregates. */
public interface IamCompanyDirectoryPort {
    record CompanyIdentity(Long userId,String firstName,String lastName,String email,String companyName) { }
    Optional<CompanyIdentity> findActiveCompany(Long userId);
    List<CompanyIdentity> allActiveCompanies();
}
