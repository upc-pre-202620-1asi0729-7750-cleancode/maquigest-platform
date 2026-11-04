package com.maquigest.platform.maintenance.infrastructure.acl.inventory;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class InventoryMaintenanceAclAdapterTests {
 @Test void rentedEquipmentCannotBeMarkedMaintenance(){
   var repo=mock(EquipmentRepository.class);var equipment=mock(Equipment.class);
   when(repo.findByIdForUpdate(3L)).thenReturn(Optional.of(equipment));
   when(equipment.getUserId()).thenReturn(1L);when(equipment.getStatus()).thenReturn(EquipmentStatus.RENTED);
   var adapter=new InventoryMaintenanceAclAdapter(repo);
   assertEquals(409,assertThrows(MaintenanceException.class,()->adapter.markMaintenance(3L,1L)).status().value());
   verify(repo,never()).save(any());
 }
}
