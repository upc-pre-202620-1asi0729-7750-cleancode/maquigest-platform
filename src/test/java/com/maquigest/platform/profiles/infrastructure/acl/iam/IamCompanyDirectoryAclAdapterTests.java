package com.maquigest.platform.profiles.infrastructure.acl.iam;

import com.maquigest.platform.iam.interfaces.acl.IamCompanyDirectoryFacade;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IamCompanyDirectoryAclAdapterTests {
    private final IamCompanyDirectoryFacade iamFacade = mock(IamCompanyDirectoryFacade.class);
    private final IamCompanyDirectoryAclAdapter adapter = new IamCompanyDirectoryAclAdapter(iamFacade);

    @Test
    void convertsPublishedIamIdentityToProfilesLanguage() {
        when(iamFacade.findActiveCompany(12L)).thenReturn(Optional.of(
                new IamCompanyDirectoryFacade.CompanyIdentity(12L,"Luz","Diaz","luz@example.com","Luz SAC")));
        var local = adapter.findActiveCompany(12L).orElseThrow();
        assertEquals(Long.valueOf(12L), local.userId());
        assertEquals("Luz", local.firstName());
        assertEquals("Diaz", local.lastName());
        assertEquals("luz@example.com", local.email());
        assertEquals("Luz SAC", local.companyName());
    }

    @Test
    void preservesEmptyResponseAndCollectionContents() {
        when(iamFacade.findActiveCompany(88L)).thenReturn(Optional.empty());
        assertTrue(adapter.findActiveCompany(88L).isEmpty());
        when(iamFacade.allActiveCompanies()).thenReturn(List.of(
                new IamCompanyDirectoryFacade.CompanyIdentity(1L,"Ana","Mora","a@example.com","A SAC")));
        assertEquals(Long.valueOf(1L), adapter.allActiveCompanies().getFirst().userId());
    }
}
