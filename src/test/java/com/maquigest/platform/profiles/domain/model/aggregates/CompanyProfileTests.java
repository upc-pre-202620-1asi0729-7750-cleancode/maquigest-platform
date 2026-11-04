package com.maquigest.platform.profiles.domain.model.aggregates;
import com.maquigest.platform.profiles.domain.model.commands.*;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CompanyProfileTests {
    private CreateCompanyProfileCommand cmd(){return new CreateCompanyProfileCommand(7L,"Ana","Mora","ana@example.com","","Obras",CompanyAddress.empty());}
    @Test void createsValidProfileAndForbidsIdentityChanges(){
        var p=new CompanyProfile(cmd());p.assignId(5L);
        assertThrows(IllegalStateException.class,()->p.assignId(6L));
        assertThrows(IllegalArgumentException.class,()->p.update(new UpdateCompanyProfileCommand(5L,8L,"Ana","Mora","ana@example.com","","Obras",CompanyAddress.empty())));
        assertEquals(7L,p.getUserId());
    }
    @Test void updatesContactWithoutChangingIamUserId(){
        var p=CompanyProfile.reconstitute(5L,cmd());
        p.update(new UpdateCompanyProfileCommand(5L,7L,"Anita","Mora","mail@example.com","999","Obras",CompanyAddress.empty()));
        assertEquals("Anita",p.getFirstName());assertEquals("999",p.getPhoneNumber());
        assertEquals(1,p.domainEvents().size());
    }
    @Test void rejectsInvalidCompanyName(){
        assertThrows(IllegalArgumentException.class,()->new CompanyProfile(new CreateCompanyProfileCommand(7L,"Ana","Mora","ana@example.com",""," ",CompanyAddress.empty())));
    }
}
