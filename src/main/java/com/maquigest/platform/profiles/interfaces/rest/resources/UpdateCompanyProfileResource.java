package com.maquigest.platform.profiles.interfaces.rest.resources;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
/** Angular sends full ProfileResource in PUT, including id and IAM userId. */
public record UpdateCompanyProfileResource(
    Long id,@NotNull @Positive Long userId,
    @NotBlank String firstName,@NotBlank String lastName,
    @NotBlank @Email String contactEmail,String phoneNumber,
    @NotBlank String companyName,@NotNull @Valid CompanyAddressResource address) { }
