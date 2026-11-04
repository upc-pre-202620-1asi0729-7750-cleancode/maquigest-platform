package com.maquigest.platform.profiles.infrastructure.persistence.jpa.embeddables;
import jakarta.persistence.*;
import lombok.Getter;import lombok.Setter;import lombok.NoArgsConstructor;
@Embeddable @Getter @Setter @NoArgsConstructor
public class CompanyAddressPersistenceEmbeddable {
    @Column(name="address_street",length=250,nullable=false) private String street="";
    @Column(name="address_district",length=120,nullable=false) private String district="";
    @Column(name="address_city",length=120,nullable=false) private String city="";
    @Column(name="address_country",length=120,nullable=false) private String country="";
    @Column(name="address_latitude",nullable=false) private double latitude;
    @Column(name="address_longitude",nullable=false) private double longitude;
}
