package com.maquigest.platform.profiles.interfaces.rest.transform;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.model.commands.CreateCompanyProfileCommand;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CompanyProfileResourceAssemblerTests {
    @Test void exposesExactAngularJsonShape(){
        var profile=CompanyProfile.reconstitute(5L,new CreateCompanyProfileCommand(2L,"Juan","Perez","juan@example.com","555","Builders",CompanyAddress.empty()));
        var json=CompanyProfileResourceFromEntityAssembler.toResourceFromEntity(profile);
        assertEquals(5L,json.id());assertEquals(2L,json.userId());
        assertNotNull(json.address());assertEquals("",json.address().country());
    }
}
