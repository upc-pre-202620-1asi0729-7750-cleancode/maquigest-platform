package com.maquigest.platform.profiles.domain.model.commands;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
public record UpdateCompanyProfileCommand(Long id,Long userId,String firstName,String lastName,String contactEmail,
        String phoneNumber,String companyName,CompanyAddress address) { }
