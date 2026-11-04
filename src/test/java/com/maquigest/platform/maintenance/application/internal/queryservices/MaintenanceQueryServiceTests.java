package com.maquigest.platform.maintenance.application.internal.queryservices;
import com.maquigest.platform.maintenance.application.ports.*;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.domain.model.aggregates.*;
import com.maquigest.platform.maintenance.domain.model.queries.*;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
import com.maquigest.platform.maintenance.domain.repositories.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class MaintenanceQueryServiceTests {
 @Test void constructionCompanySeesOnlyActiveBlockingIncidents(){
   var incidents=mock(IncidentRepository.class);var m=mock(MaintenanceRepository.class);
   var a=mock(MaintenanceActivityRepository.class);var i=mock(MaintenanceInventoryPort.class);
   var r=mock(MaintenanceRentalPort.class);
   when(i.inspect(3L)).thenReturn(new MaintenanceInventoryPort.EquipmentFacts(3L,1L,"EQ-3","Excavator","MAINTENANCE"));
   var open=new Incident(1L,3L,1L,"Failure",Instant.now(),true,IncidentStatus.OPEN,null);
   var other=new Incident(2L,3L,1L,"Note",Instant.now(),false,IncidentStatus.OPEN,null);
   when(incidents.findByEquipmentId(3L)).thenReturn(List.of(open,other));
   var result=new MaintenanceQueryServiceImpl(incidents,m,a,i,r).handle(new GetIncidentsQuery(2L,3L,false));
   assertEquals(1,result.size());assertTrue(result.getFirst().isBlocksRental());
 }
 @Test void ownerHistoryCombinesAllContexts(){
   var incidents=mock(IncidentRepository.class);var m=mock(MaintenanceRepository.class);
   var a=mock(MaintenanceActivityRepository.class);var i=mock(MaintenanceInventoryPort.class);
   var r=mock(MaintenanceRentalPort.class);
   when(i.inspect(3L)).thenReturn(new MaintenanceInventoryPort.EquipmentFacts(3L,1L,"EQ-3","Excavator","AVAILABLE"));
   when(incidents.findByEquipmentId(3L)).thenReturn(List.of(new Incident(4L,3L,1L,"Leak",Instant.now(),false,IncidentStatus.OPEN,null)));
   when(m.findByEquipmentId(3L)).thenReturn(List.of());
   when(r.history(3L,1L)).thenReturn(List.of(new MaintenanceRentalPort.RentalHistory(1L,Instant.now(),"Rental","COMPLETED")));
   var history=new MaintenanceQueryServiceImpl(incidents,m,a,i,r).handle(new GetEquipmentHistoryQuery(3L,1L));
   assertEquals("EQ-3",history.equipmentCode());assertEquals(2,history.entries().size());
 }
 @Test void completedScheduledMaintenanceUsesActualTransitionDateInHistory(){
   var incidents=mock(IncidentRepository.class);var m=mock(MaintenanceRepository.class);
   var a=mock(MaintenanceActivityRepository.class);var i=mock(MaintenanceInventoryPort.class);
   var r=mock(MaintenanceRentalPort.class);
   var performedAt=Instant.now().plusSeconds(86400L*30);
   var actual=Instant.now();
   when(i.inspect(3L)).thenReturn(new MaintenanceInventoryPort.EquipmentFacts(3L,1L,"EQ-3","Excavator","AVAILABLE"));
   var maintenance=new Maintenance(2L,3L,1L,performedAt,"Inspection",MaintenanceStatus.COMPLETED,"Technician",null);
   when(m.findByEquipmentId(3L)).thenReturn(List.of(maintenance));
   when(a.findByMaintenanceId(2L)).thenReturn(List.of(
       new com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity(1L,2L,MaintenanceStatus.SCHEDULED,actual.minusSeconds(10),"","Registered maintenance"),
       new com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity(2L,2L,MaintenanceStatus.COMPLETED,actual,"Technician","Finished")));
   var history=new MaintenanceQueryServiceImpl(incidents,m,a,i,r).handle(new GetEquipmentHistoryQuery(3L,1L));
   assertEquals(actual,history.entries().getFirst().occurredAt());
 }
}
