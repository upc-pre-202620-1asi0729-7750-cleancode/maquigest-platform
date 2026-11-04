package com.maquigest.platform.maintenance.domain.model.aggregates;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
import com.maquigest.platform.maintenance.domain.model.events.*;
import java.time.*;
import java.util.Objects;
public final class Maintenance extends AbstractDomainAggregateRoot<Maintenance> {
    private final Long id,equipmentId,ownerUserId;
    private final String type;
    private final Instant performedAt;
    private MaintenanceStatus status;
    private String technician;
    private Instant acknowledgedAt;
    public Maintenance(Long id,Long equipmentId,Long ownerUserId,Instant performedAt,String type,
                       MaintenanceStatus status,String technician,Instant acknowledgedAt) {
        if(id!=null&&id<=0)throw new IllegalArgumentException("Invalid maintenance id");
        if(equipmentId==null||equipmentId<=0||ownerUserId==null||ownerUserId<=0)throw new IllegalArgumentException("Invalid equipment or owner");
        this.id=id;this.equipmentId=equipmentId;this.ownerUserId=ownerUserId;
        this.performedAt=Objects.requireNonNull(performedAt,"performedAt");
        this.type=new MaintenanceType(type).value();this.status=Objects.requireNonNull(status,"status");
        this.technician=technician==null?"":technician.trim();this.acknowledgedAt=acknowledgedAt;
        if(this.technician.length()>180)throw new IllegalArgumentException("Technician name is too long");
        if(acknowledgedAt!=null&&this.technician.isBlank())throw new IllegalArgumentException("Acknowledgement requires a technician");
    }
    public static Maintenance register(Long equipmentId,Long ownerUserId,Instant when,String type,MaintenanceStatus status) {
        if(status!=MaintenanceStatus.SCHEDULED&&status!=MaintenanceStatus.COMPLETED)
            throw new IllegalArgumentException("New maintenance must be SCHEDULED or COMPLETED");
        if(when==null)throw new IllegalArgumentException("Maintenance date is required");
        LocalDate date=when.atZone(ZoneId.of("America/Lima")).toLocalDate();
        LocalDate today=LocalDate.now(ZoneId.of("America/Lima"));
        if(status==MaintenanceStatus.SCHEDULED&&date.isBefore(today))
            throw new IllegalArgumentException("Scheduled maintenance cannot be in the past");
        if(status==MaintenanceStatus.COMPLETED&&date.isAfter(today))
            throw new IllegalArgumentException("Completed maintenance cannot be in the future");
        var maintenance=new Maintenance(null,equipmentId,ownerUserId,when,type,status,"",null);
        maintenance.registerDomainEvent(new MaintenanceCreatedEvent(equipmentId,status.name()));return maintenance;
    }
    public void changeStatus(MaintenanceStatus target,String assigned,String notes,Instant at){
        if(target==null||at==null)throw new IllegalArgumentException("Target status and time required");
        if(status==MaintenanceStatus.COMPLETED)throw new IllegalStateException("Completed maintenance cannot change");
        if(status==MaintenanceStatus.SCHEDULED&&target!=MaintenanceStatus.IN_PROGRESS&&target!=MaintenanceStatus.COMPLETED)
            throw new IllegalStateException("Invalid maintenance transition");
        if(status==MaintenanceStatus.IN_PROGRESS&&target!=MaintenanceStatus.COMPLETED)
            throw new IllegalStateException("Invalid maintenance transition");
        if(status==MaintenanceStatus.SCHEDULED &&
           at.atZone(ZoneId.of("America/Lima")).toLocalDate().isBefore(
               performedAt.atZone(ZoneId.of("America/Lima")).toLocalDate()))
            throw new IllegalStateException(
                "Scheduled maintenance cannot advance before its scheduled date");
        if(assigned!=null&&!assigned.isBlank()){
            if(assigned.trim().length()>180)throw new IllegalArgumentException("Technician name too long");
            technician=assigned.trim();
        }
        status=target;registerDomainEvent(new MaintenanceStatusChangedEvent(id,equipmentId,status.name()));
    }
    public void assign(String name){
        if(name==null||name.isBlank()||name.trim().length()>180)throw new IllegalArgumentException("Invalid technician");
        technician=name.trim();
    }
    public void acknowledge(Instant at){
        if(technician.isBlank())throw new IllegalStateException("Assign a technician first");
        if(acknowledgedAt!=null)throw new IllegalStateException("Already acknowledged");
        acknowledgedAt=Objects.requireNonNull(at);
    }
    public Long getId(){return id;} public Long getEquipmentId(){return equipmentId;}
    public Long getOwnerUserId(){return ownerUserId;} public Instant getPerformedAt(){return performedAt;}
    public String getType(){return type;} public MaintenanceStatus getStatus(){return status;}
    public String getTechnician(){return technician;} public Instant getAcknowledgedAt(){return acknowledgedAt;}
}
