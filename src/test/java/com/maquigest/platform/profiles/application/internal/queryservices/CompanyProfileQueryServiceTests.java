package com.maquigest.platform.profiles.application.internal.queryservices;
import com.maquigest.platform.profiles.domain.model.queries.*;
import com.maquigest.platform.profiles.domain.repositories.CompanyProfileRepository;
import org.junit.jupiter.api.Test;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class CompanyProfileQueryServiceTests {
    @Test void userFilterMustNotReturnOtherCompanyProfiles(){
        var repo=mock(CompanyProfileRepository.class);
        var service=new CompanyProfileQueryServiceImpl(repo);
        when(repo.findByUserId(1L)).thenReturn(Optional.empty());
        assertTrue(service.handle(new GetAllCompanyProfilesQuery(1L)).isEmpty());
        verify(repo,never()).findAll();
    }
}
