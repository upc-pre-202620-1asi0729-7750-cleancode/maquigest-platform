package com.maquigest.platform.profiles.interfaces.rest.transform;
import com.maquigest.platform.profiles.domain.model.commands.UpdateCompanyProfileCommand;
import com.maquigest.platform.profiles.interfaces.rest.resources.UpdateCompanyProfileResource;
public final class UpdateCompanyProfileCommandFromResourceAssembler {
    private UpdateCompanyProfileCommandFromResourceAssembler(){ }
    public static UpdateCompanyProfileCommand toCommandFromResource(Long id,UpdateCompanyProfileResource r){
        return new UpdateCompanyProfileCommand(id,r.userId(),r.firstName(),r.lastName(),r.contactEmail(),
            r.phoneNumber(),r.companyName(),r.address().toDomain());
    }
}
