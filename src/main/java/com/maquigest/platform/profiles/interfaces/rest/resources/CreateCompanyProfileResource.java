package com.maquigest.platform.profiles.interfaces.rest.resources;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
public record CreateCompanyProfileResource(
    @NotNull @Positive Long userId,
    @NotBlank String firstName,@NotBlank String lastName,
    @NotBlank @Email String contactEmail,String phoneNumber,
    @NotBlank String companyName,@NotNull @Valid CompanyAddressResource address) { }
