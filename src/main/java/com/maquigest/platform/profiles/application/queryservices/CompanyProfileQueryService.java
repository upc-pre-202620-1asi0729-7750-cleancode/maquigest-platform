package com.maquigest.platform.profiles.application.queryservices;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.model.queries.*;
import java.util.List;import java.util.Optional;
public interface CompanyProfileQueryService {
    List<CompanyProfile> handle(GetAllCompanyProfilesQuery query);
    Optional<CompanyProfile> handle(GetCompanyProfileByIdQuery query);
    Optional<CompanyProfile> handle(GetCompanyProfileByUserIdQuery query);
}
