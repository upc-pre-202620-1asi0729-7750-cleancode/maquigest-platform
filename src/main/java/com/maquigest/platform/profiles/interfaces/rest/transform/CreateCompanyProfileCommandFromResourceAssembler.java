package com.maquigest.platform.profiles.interfaces.rest.transform;
import com.maquigest.platform.profiles.domain.model.commands.CreateCompanyProfileCommand;
import com.maquigest.platform.profiles.interfaces.rest.resources.CreateCompanyProfileResource;
public final class CreateCompanyProfileCommandFromResourceAssembler {
    private CreateCompanyProfileCommandFromResourceAssembler(){ }
    public static CreateCompanyProfileCommand toCommandFromResource(CreateCompanyProfileResource r){
        return new CreateCompanyProfileCommand(r.userId(),r.firstName(),r.lastName(),r.contactEmail(),
            r.phoneNumber(),r.companyName(),r.address().toDomain());
    }
}
