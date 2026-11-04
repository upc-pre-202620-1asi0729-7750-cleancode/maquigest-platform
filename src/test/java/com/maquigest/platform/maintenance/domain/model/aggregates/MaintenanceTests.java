package com.maquigest.platform.maintenance.domain.model.aggregates;
import com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MaintenanceTests {
 @Test void scheduleThenStartThenComplete(){
   var date=Instant.now();
   var m=Maintenance.register(3L,1L,date,"Oil and filter change",MaintenanceStatus.SCHEDULED);
   m.assign("Technician A");m.acknowledge(Instant.now());
   m.changeStatus(MaintenanceStatus.IN_PROGRESS,null,"Started",Instant.now());
   assertEquals(MaintenanceStatus.IN_PROGRESS,m.getStatus());
   m.changeStatus(MaintenanceStatus.COMPLETED,null,"Finished",Instant.now());
   assertEquals(MaintenanceStatus.COMPLETED,m.getStatus());
   assertThrows(IllegalStateException.class,()->m.changeStatus(MaintenanceStatus.IN_PROGRESS,null,"",Instant.now()));
 }
 @Test void cannotRegisterPastSchedule(){
   assertThrows(IllegalArgumentException.class,()->Maintenance.register(3L,1L,
       Instant.now().minusSeconds(86400*5),"Oil change",MaintenanceStatus.SCHEDULED));
 }
 @Test void acknowledgementRequiresAssignment(){
   var m=Maintenance.register(3L,1L,Instant.now(),"Inspection",MaintenanceStatus.COMPLETED);
   assertThrows(IllegalStateException.class,()->m.acknowledge(Instant.now()));
 }

 @Test void cannotStartFutureScheduledMaintenance() {
   var lima=ZoneId.of("America/Lima");
   var date=LocalDate.now(lima).plusDays(5)
       .atTime(12,0).atZone(lima).toInstant();

   var maintenance=Maintenance.register(
       3L,1L,date,"Inspeccion futura",MaintenanceStatus.SCHEDULED);

   assertThrows(IllegalStateException.class, () ->
       maintenance.changeStatus(
           MaintenanceStatus.IN_PROGRESS,
           "Tecnico QA",
           "Inicio anticipado",
           Instant.now()
       )
   );

   assertEquals(MaintenanceStatus.SCHEDULED,maintenance.getStatus());
 }

 @Test void cannotCompleteFutureScheduledMaintenance() {
   var lima=ZoneId.of("America/Lima");
   var date=LocalDate.now(lima).plusDays(5)
       .atTime(12,0).atZone(lima).toInstant();

   var maintenance=Maintenance.register(
       3L,1L,date,"Inspeccion futura",MaintenanceStatus.SCHEDULED);

   assertThrows(IllegalStateException.class, () ->
       maintenance.changeStatus(
           MaintenanceStatus.COMPLETED,
           "Tecnico QA",
           "Finalizacion anticipada",
           Instant.now()
       )
   );

   assertEquals(MaintenanceStatus.SCHEDULED,maintenance.getStatus());
 }
}
