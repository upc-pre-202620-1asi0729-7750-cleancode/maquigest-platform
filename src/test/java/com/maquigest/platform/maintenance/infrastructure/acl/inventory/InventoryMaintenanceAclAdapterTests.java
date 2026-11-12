package com.maquigest.platform.maintenance.infrastructure.acl.inventory;

import com.maquigest.platform.inventory.interfaces.acl.InventoryMaintenanceFacade;
import com.maquigest.platform.inventory.interfaces.acl.InventoryMaintenanceFailure;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class InventoryMaintenanceAclAdapterTests {
    private final InventoryMaintenanceFacade facade = mock(InventoryMaintenanceFacade.class);
    private final InventoryMaintenanceAclAdapter adapter = new InventoryMaintenanceAclAdapter(facade);

    @Test void inspectMapsEquipmentFacts() {
        when(facade.inspect(3L)).thenReturn(new InventoryMaintenanceFacade.EquipmentSnapshot(3L, 1L,"EXC-001","Excavator","AVAILABLE"));
        var facts = adapter.inspect(3L);
        assertEquals(1L, facts.ownerUserId());
        assertEquals("EXC-001", facts.code());
    }
    @Test void lockDelegatesAndMapsFacts() {
        when(facade.lock(3L)).thenReturn(new InventoryMaintenanceFacade.EquipmentSnapshot(3L, 1L,"E","Name","MAINTENANCE"));
        assertEquals("MAINTENANCE", adapter.lock(3L).status());
        verify(facade).lock(3L);
    }
    @Test void rentedEquipmentCannotBeMarkedMaintenance() {
        doThrow(InventoryMaintenanceFailure.conflict("Rented equipment cannot enter maintenance directly"))
            .when(facade).markMaintenance(3L,1L);
        assertEquals(409, com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode(assertThrows(MaintenanceException.class, () -> adapter.markMaintenance(3L,1L)).code()).value());
    }
    @Test void mapsMissingEquipmentToExistingMaintenance404() {
        when(facade.inspect(3L)).thenThrow(InventoryMaintenanceFailure.notFound());
        var error=assertThrows(MaintenanceException.class, () -> adapter.inspect(3L));
        assertEquals(404,com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode(error.code()).value());
        assertEquals("MAINTENANCE_NOT_FOUND",error.code());
        assertEquals("Equipment was not found",error.getMessage());
    }
    @Test void mapsInvalidEquipmentTo400() {
        when(facade.lock(-1L)).thenThrow(InventoryMaintenanceFailure.invalid("Invalid equipment id"));
        assertEquals(400, com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode(assertThrows(MaintenanceException.class, () -> adapter.lock(-1L)).code()).value());
    }
    @Test void mapsWrongOwnerTo403() {
        doThrow(InventoryMaintenanceFailure.forbidden()).when(facade).reactivate(3L,2L);
        assertEquals(403,com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode(assertThrows(MaintenanceException.class, () -> adapter.reactivate(3L,2L)).code()).value());
    }
    @Test void mutatingOperationsDelegate() {
        adapter.markMaintenance(3L,1L);
        adapter.reactivate(3L,1L);
        verify(facade).markMaintenance(3L,1L);
        verify(facade).reactivate(3L,1L);
    }
}
