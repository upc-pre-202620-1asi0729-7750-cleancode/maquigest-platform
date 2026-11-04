package com.maquigest.platform.profiles.application.internal.commandservices;
import com.maquigest.platform.profiles.application.ports.IamCompanyDirectoryPort;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.model.commands.*;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
import com.maquigest.platform.profiles.domain.repositories.CompanyProfileRepository;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.*;
class CompanyProfileCommandServiceTests {
    private final CompanyProfileRepository repo=mock(CompanyProfileRepository.class);
    private final IamCompanyDirectoryPort iam=mock(IamCompanyDirectoryPort.class);
    private final CompanyProfileCommandServiceImpl service=new CompanyProfileCommandServiceImpl(repo,iam);
    private final IamCompanyDirectoryPort.CompanyIdentity user=new IamCompanyDirectoryPort.CompanyIdentity(1L,"Carlos","Prueba","carlos@example.com","Maquinaria");
    @Test void rejectsUnknownIamUser(){
        when(iam.findActiveCompany(1L)).thenReturn(Optional.empty());
        var r=service.handle(new CreateCompanyProfileCommand(1L,"Carlos","Prueba","carlos@example.com","","Maquinaria",CompanyAddress.empty()));
        assertTrue(r.isFailure());verify(repo,never()).save(any());
    }
    @Test void rejectsDuplicateProfile(){
        when(iam.findActiveCompany(1L)).thenReturn(Optional.of(user));
        when(repo.existsByUserId(1L)).thenReturn(true);
        assertTrue(service.handle(new CreateCompanyProfileCommand(1L,"Carlos","Prueba","carlos@example.com","","Maquinaria",CompanyAddress.empty())).isFailure());
    }
    @Test void provisionsOnceWithoutOverwritingExistingProfile(){
        when(iam.allActiveCompanies()).thenReturn(List.of(user));
        when(repo.existsByUserId(1L)).thenReturn(false,true);
        when(repo.save(any())).thenAnswer(inv->inv.getArgument(0));
        assertEquals(1,service.provisionMissingFromIam());
        assertEquals(0,service.provisionMissingFromIam());
        verify(repo,times(1)).save(any());
    }
    @Test void preventsChangingUserId(){
        var existing=CompanyProfile.reconstitute(9L,new CreateCompanyProfileCommand(1L,"Carlos","Prueba","carlos@example.com","","Maquinaria",CompanyAddress.empty()));
        when(repo.findById(9L)).thenReturn(Optional.of(existing));
        assertTrue(service.handle(new UpdateCompanyProfileCommand(9L,2L,"Carlos","Prueba","carlos@example.com","","Maquinaria",CompanyAddress.empty())).isFailure());
        verify(repo,never()).save(any());
    }
}
