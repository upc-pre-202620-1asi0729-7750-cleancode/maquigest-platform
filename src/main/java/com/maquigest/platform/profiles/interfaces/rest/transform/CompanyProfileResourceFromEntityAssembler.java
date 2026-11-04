package com.maquigest.platform.profiles.interfaces.rest.transform;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.interfaces.rest.resources.*;
public final class CompanyProfileResourceFromEntityAssembler {
    private CompanyProfileResourceFromEntityAssembler() { }
    public static CompanyProfileResource toResourceFromEntity(CompanyProfile p){
        return new CompanyProfileResource(p.getId(),p.getUserId(),p.getFirstName(),p.getLastName(),
            p.getContactEmail(),p.getPhoneNumber(),p.getCompanyName(),CompanyAddressResource.fromDomain(p.getAddress()));
    }
}
