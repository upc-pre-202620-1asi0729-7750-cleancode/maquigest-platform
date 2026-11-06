package com.maquigest.platform.iam.application.acl;

import com.maquigest.platform.iam.application.ports.IamCompanyDirectoryReader;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class IamCompanyDirectoryFacadeImplTests {
    private final IamCompanyDirectoryReader reader = mock(IamCompanyDirectoryReader.class);
    private final IamCompanyDirectoryFacadeImpl facade = new IamCompanyDirectoryFacadeImpl(reader);

    @Test
    void delegatesIdAndPreservesSnapshotFields() {
        when(reader.findActiveCompany(7L)).thenReturn(Optional.of(
                new IamCompanyDirectoryReader.CompanyRow(7L, "Ana", "Mora", "ana@example.com", "Maqui SAC")));

        var identity = facade.findActiveCompany(7L).orElseThrow();
        assertEquals(Long.valueOf(7L), identity.userId());
        assertEquals("Ana", identity.firstName());
        assertEquals("Mora", identity.lastName());
        assertEquals("ana@example.com", identity.email());
        assertEquals("Maqui SAC", identity.companyName());
        verify(reader).findActiveCompany(7L);
    }

    @Test
    void preservesEmptyAndOrdering() {
        when(reader.findActiveCompany(99L)).thenReturn(Optional.empty());
        assertTrue(facade.findActiveCompany(99L).isEmpty());
        when(reader.allActiveCompanies()).thenReturn(List.of(
                new IamCompanyDirectoryReader.CompanyRow(3L,"A","X","a@example.com","A SAC"),
                new IamCompanyDirectoryReader.CompanyRow(4L,"B","Y","b@example.com","B SAC")));
        assertEquals(List.of(3L, 4L), facade.allActiveCompanies().stream()
                .map(company -> company.userId()).toList());
    }
}
