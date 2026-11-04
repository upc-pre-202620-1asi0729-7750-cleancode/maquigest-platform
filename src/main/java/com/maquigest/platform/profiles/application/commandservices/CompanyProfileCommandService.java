package com.maquigest.platform.profiles.application.commandservices;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.model.commands.*;
import com.maquigest.platform.shared.application.result.*;
public interface CompanyProfileCommandService {
    Result<CompanyProfile,ApplicationError> handle(CreateCompanyProfileCommand command);
    Result<CompanyProfile,ApplicationError> handle(UpdateCompanyProfileCommand command);
    /** Reconcile existing IAM users without overwriting manually updated profiles. */
    int provisionMissingFromIam();
}
