package com.maquigest.platform.profiles.interfaces.rest.resources;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
import jakarta.validation.constraints.*;
public record CompanyAddressResource(@Size(max=250) String street,@Size(max=120) String district,
                                     @Size(max=120) String city,@Size(max=120) String country,
                                     @DecimalMin("-90.0") @DecimalMax("90.0") double latitude,
                                     @DecimalMin("-180.0") @DecimalMax("180.0") double longitude) {
    public CompanyAddress toDomain(){return new CompanyAddress(street,district,city,country,latitude,longitude);}
    public static CompanyAddressResource fromDomain(CompanyAddress a){
        return new CompanyAddressResource(a.street(),a.district(),a.city(),a.country(),a.latitude(),a.longitude());
    }
}
