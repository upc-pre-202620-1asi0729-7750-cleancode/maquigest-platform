package com.maquigest.platform.profiles.infrastructure.persistence.jpa;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.model.commands.CreateCompanyProfileCommand;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
import com.maquigest.platform.profiles.infrastructure.persistence.jpa.assemblers.CompanyProfilePersistenceAssembler;
import com.maquigest.platform.profiles.infrastructure.persistence.jpa.entities.CompanyProfilePersistenceEntity;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class CompanyProfilePersistenceAssemblerTests {
    @Test void roundTripKeepsCoordinatesAndIamUserId(){
        var profile=CompanyProfile.reconstitute(3L,new CreateCompanyProfileCommand(8L,"Luis","Lopez","luis@example.com","123","Constructora",new CompanyAddress("Av 1","Surco","Lima","Peru",-12.1,-77.0)));
        var entity=CompanyProfilePersistenceAssembler.toPersistence(profile,new CompanyProfilePersistenceEntity());
        entity.setId(3L);
        var actual=CompanyProfilePersistenceAssembler.toDomain(entity);
        assertEquals(8L,actual.getUserId());assertEquals("Surco",actual.getAddress().district());
        assertEquals(-12.1,actual.getAddress().latitude(),0.00001);
    }
}
