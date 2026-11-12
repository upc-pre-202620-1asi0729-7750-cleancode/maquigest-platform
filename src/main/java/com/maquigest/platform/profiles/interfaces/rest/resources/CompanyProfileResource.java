package com.maquigest.platform.profiles.interfaces.rest.resources;
public record CompanyProfileResource(Long id,Long userId,String firstName,String lastName,
        String contactEmail,String phoneNumber,String companyName,CompanyAddressResource address) { }
