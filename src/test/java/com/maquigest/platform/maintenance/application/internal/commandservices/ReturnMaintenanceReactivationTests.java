package com.maquigest.platform.maintenance.application.internal.commandservices;
import com.maquigest.platform.maintenance.application.ports.*;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance;
import com.maquigest.platform.maintenance.domain.model.commands.ReactivateEquipmentCommand;
import com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.maquigest.platform.maintenance.domain.repositories.*;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class ReturnMaintenanceReactivationTests {
  private final IncidentRepository incidents=mock(IncidentRepository.class);
  private final MaintenanceRepository maintenances=mock(MaintenanceRepository.class);
  private final MaintenanceActivityRepository activities=mock(MaintenanceActivityRepository.class);
  private final MaintenanceInventoryPort inventory=mock(MaintenanceInventoryPort.class);
  private final MaintenanceRentalPort rentals=mock(MaintenanceRentalPort.class);
  private IncidentTransactionalExecutor service() {
    when(inventory.lock(6L)).thenReturn(new MaintenanceInventoryPort.EquipmentFacts(6L,10L,"MG-CAR-002","Komatsu","MAINTENANCE"));
    return new IncidentTransactionalExecutor(incidents,maintenances,activities,inventory,rentals);
  }
  @Test void completedMaintenanceAfterRequiredReturnCanReactivate() {
    var returnedAt=Instant.parse("2026-11-11T14:00:00Z");
    when(rentals.latestReturn(6L,10L)).thenReturn(Optional.of(new MaintenanceRentalPort.LatestReturn(returnedAt,true)));
    var completed=new Maintenance(10L,6L,10L,returnedAt.plusSeconds(60),"preventivo",MaintenanceStatus.COMPLETED,"Technician",null);
    when(maintenances.findByEquipmentId(6L)).thenReturn(List.of(completed));
    when(activities.findByMaintenanceId(10L)).thenReturn(List.of(new MaintenanceActivity(3L,10L,MaintenanceStatus.COMPLETED,returnedAt.plusSeconds(60),"Technician","Completed")));
    service().handle(new ReactivateEquipmentCommand(6L,true),10L);
    verify(inventory).reactivate(6L,10L);
  }
  @Test void refusesReturnWithoutMaintenanceRequirement() {
    when(rentals.latestReturn(6L,10L)).thenReturn(Optional.of(new MaintenanceRentalPort.LatestReturn(Instant.now(),false)));
    assertThrows(MaintenanceException.class,()->service().handle(new ReactivateEquipmentCommand(6L,true),10L));
    verify(inventory,never()).reactivate(any(),any());
  }
  @Test void refusesUnfinishedWorkAfterReturn() {
    when(rentals.latestReturn(6L,10L)).thenReturn(Optional.of(new MaintenanceRentalPort.LatestReturn(Instant.now().minusSeconds(200),true)));
    when(maintenances.findByEquipmentId(6L)).thenReturn(List.of());
    assertThrows(MaintenanceException.class,()->service().handle(new ReactivateEquipmentCommand(6L,true),10L));
    verify(inventory,never()).reactivate(any(),any());
  }
  @Test void refusesOpenBlockingIncidentEvenWhenReturnWorkComplete() {
    when(incidents.hadBlocking(6L)).thenReturn(true);
    when(incidents.hasOpenBlocking(6L)).thenReturn(true);
    assertThrows(MaintenanceException.class,()->service().handle(new ReactivateEquipmentCommand(6L,true),10L));
    verify(inventory,never()).reactivate(any(),any());
  }
  @Test void retainsExistingResolvedBlockingIncidentPath() {
    when(incidents.hadBlocking(6L)).thenReturn(true);
    service().handle(new ReactivateEquipmentCommand(6L,true),10L);
    verify(inventory).reactivate(6L,10L);
    verify(rentals,never()).latestReturn(any(),any());
  }
}
