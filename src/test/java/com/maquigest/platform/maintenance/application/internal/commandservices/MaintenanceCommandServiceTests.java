package com.maquigest.platform.maintenance.application.internal.commandservices;
import com.maquigest.platform.maintenance.application.ports.*;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.domain.model.commands.*;
import com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.maquigest.platform.maintenance.domain.repositories.*;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class MaintenanceCommandServiceTests {
 @Test void growthPlanIsMandatory(){
   var m=mock(MaintenanceRepository.class);var a=mock(MaintenanceActivityRepository.class);
   var i=mock(MaintenanceInventoryPort.class);var r=mock(MaintenanceRentalPort.class);var s=mock(MaintenanceSubscriptionPort.class);
   when(i.lock(3L)).thenReturn(new MaintenanceInventoryPort.EquipmentFacts(3L,1L,"EQ-3","Excavator","AVAILABLE"));
   var service=new MaintenanceCommandServiceImpl(m,a,i,r,s);
   assertEquals(403,assertThrows(MaintenanceException.class,()->service.handle(new RegisterMaintenanceCommand(
       3L,Instant.now(),"Oil change",MaintenanceStatus.COMPLETED),1L)).status().value());
   verifyNoInteractions(m,a);
 }
 @Test void completedMaintenanceCreatesTrackingActivity(){
   var m=mock(MaintenanceRepository.class);var a=mock(MaintenanceActivityRepository.class);
   var i=mock(MaintenanceInventoryPort.class);var r=mock(MaintenanceRentalPort.class);var s=mock(MaintenanceSubscriptionPort.class);
   when(i.lock(3L)).thenReturn(new MaintenanceInventoryPort.EquipmentFacts(3L,1L,"EQ-3","Excavator","AVAILABLE"));
   when(s.hasActiveGrowth(1L)).thenReturn(true);
   when(m.save(any())).thenAnswer(inv->{var v=(com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance)inv.getArgument(0);
       return new com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance(7L,v.getEquipmentId(),v.getOwnerUserId(),v.getPerformedAt(),v.getType(),v.getStatus(),"",null);});
   var result=new MaintenanceCommandServiceImpl(m,a,i,r,s).handle(new RegisterMaintenanceCommand(
       3L,Instant.now(),"Oil change",MaintenanceStatus.COMPLETED),1L);
   assertEquals(7L,result.getId());verify(a).save(any(MaintenanceActivity.class));
 }
}
