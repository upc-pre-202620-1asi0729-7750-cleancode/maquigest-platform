package com.maquigest.platform.maintenance.application.internal.commandservices;
import com.maquigest.platform.maintenance.application.ports.*;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.aggregates.*;
import com.maquigest.platform.maintenance.domain.repositories.*;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class IncidentCommandServiceTests {
 @Test void blockingIncidentChangesInventoryAndPersists(){
   var incidents=mock(IncidentRepository.class);var maintenances=mock(MaintenanceRepository.class);
   var inventory=mock(MaintenanceInventoryPort.class);var rentals=mock(MaintenanceRentalPort.class);
   when(inventory.lock(3L)).thenReturn(new MaintenanceInventoryPort.EquipmentFacts(3L,1L,"EQ-3","Excavator","AVAILABLE"));
   when(incidents.save(any())).thenAnswer(i->i.getArgument(0));
   var result=new IncidentTransactionalExecutor(incidents,maintenances,mock(MaintenanceActivityRepository.class),inventory,rentals)
       .handle(new ReportIncidentCommand(3L,"Hydraulic failure",true),1L);
   assertTrue(result.isBlocksRental());verify(inventory).markMaintenance(3L,1L);verify(incidents).save(any());
 }
 @Test void activeRentalPreventsBlockingIncidentAndInventoryChange(){
   var incidents=mock(IncidentRepository.class);var maintenances=mock(MaintenanceRepository.class);
   var inventory=mock(MaintenanceInventoryPort.class);var rentals=mock(MaintenanceRentalPort.class);
   when(inventory.lock(3L)).thenReturn(new MaintenanceInventoryPort.EquipmentFacts(3L,1L,"EQ-3","Excavator","RENTED"));
   when(rentals.hasActiveRental(3L,1L)).thenReturn(true);
   var service=new IncidentTransactionalExecutor(incidents,maintenances,mock(MaintenanceActivityRepository.class),inventory,rentals);
   assertEquals(409,com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode(assertThrows(MaintenanceException.class, () -> service.handle(
       new ReportIncidentCommand(3L,"Failure",true),1L)).code()).value());
   verify(inventory,never()).markMaintenance(any(),any());verifyNoInteractions(incidents);
 }
 @Test void actorCannotModifyOtherOwnersIncident(){
   var incidents=mock(IncidentRepository.class);var maintenances=mock(MaintenanceRepository.class);
   var inventory=mock(MaintenanceInventoryPort.class);var rentals=mock(MaintenanceRentalPort.class);
   var incident=new Incident(5L,3L,1L,"Damage",Instant.now(),false,
       com.maquigest.platform.maintenance.domain.model.valueobjects.IncidentStatus.OPEN,null);
   when(incidents.findById(5L)).thenReturn(Optional.of(incident));
   when(inventory.lock(3L)).thenReturn(new MaintenanceInventoryPort.EquipmentFacts(3L,1L,"EQ-3","Excavator","AVAILABLE"));
   var service=new IncidentTransactionalExecutor(incidents,maintenances,mock(MaintenanceActivityRepository.class),inventory,rentals);
   assertEquals(403,com.maquigest.platform.shared.interfaces.rest.transform.ProblemDetailFromApplicationErrorAssembler.toStatusFromErrorCode(assertThrows(MaintenanceException.class, () -> service.handle(
       new PatchIncidentCommand(5L,true,null,null),2L)).code()).value());
   verify(incidents,never()).save(any());
 }
}
