package com.maquigest.platform.profiles.infrastructure.persistence.jpa.assemblers;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.model.commands.CreateCompanyProfileCommand;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
import com.maquigest.platform.profiles.infrastructure.persistence.jpa.embeddables.CompanyAddressPersistenceEmbeddable;
import com.maquigest.platform.profiles.infrastructure.persistence.jpa.entities.CompanyProfilePersistenceEntity;
public final class CompanyProfilePersistenceAssembler {
    private CompanyProfilePersistenceAssembler() { }
    public static CompanyProfile toDomain(CompanyProfilePersistenceEntity e){
        var a=e.getAddress();
        return CompanyProfile.reconstitute(e.getId(),new CreateCompanyProfileCommand(e.getUserId(),
            e.getFirstName(),e.getLastName(),e.getContactEmail(),e.getPhoneNumber(),
            e.getCompanyName(),new CompanyAddress(a.getStreet(),a.getDistrict(),a.getCity(),
                a.getCountry(),a.getLatitude(),a.getLongitude())));
    }
    /** Merge editable values into existing JPA entity, preserving id and @Version. */
    public static CompanyProfilePersistenceEntity toPersistence(CompanyProfile d,CompanyProfilePersistenceEntity e){
        e.setUserId(d.getUserId());e.setFirstName(d.getFirstName());e.setLastName(d.getLastName());
        e.setContactEmail(d.getContactEmail());e.setPhoneNumber(d.getPhoneNumber());
        e.setCompanyName(d.getCompanyName());
        var a=new CompanyAddressPersistenceEmbeddable();
        a.setStreet(d.getAddress().street());a.setDistrict(d.getAddress().district());
        a.setCity(d.getAddress().city());a.setCountry(d.getAddress().country());
        a.setLatitude(d.getAddress().latitude());a.setLongitude(d.getAddress().longitude());
        e.setAddress(a);return e;
    }
}
