package com.maquigest.platform.profiles.domain.model.commands;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
public record UpdateCompanyProfileCommand(Long id,Long userId,String firstName,String lastName,String contactEmail,
        String phoneNumber,String companyName,CompanyAddress address) {
    public UpdateCompanyProfileCommand {
        if (id == null || id <= 0) throw new IllegalArgumentException("id must be positive");
        if (userId == null || userId <= 0) throw new IllegalArgumentException("userId must be positive");
        new com.maquigest.platform.profiles.domain.model.valueobjects.PersonName(firstName, lastName);
        new com.maquigest.platform.profiles.domain.model.valueobjects.EmailAddress(contactEmail);
        new com.maquigest.platform.profiles.domain.model.valueobjects.PhoneNumber(phoneNumber);
        if (companyName == null || companyName.isBlank() || companyName.trim().length() > 180) throw new IllegalArgumentException("companyName is required (max 180)");
        if (address == null) throw new IllegalArgumentException("address is required");
    }
}
