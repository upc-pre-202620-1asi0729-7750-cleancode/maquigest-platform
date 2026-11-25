package com.maquigest.platform.maintenance.domain.model.aggregates;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
import com.maquigest.platform.maintenance.domain.model.events.*;
import java.time.*;
import java.util.Objects;
public final class Maintenance extends AbstractDomainAggregateRoot<Maintenance> {
    private final Long id;
    private final EquipmentId equipmentId;
    private final OwnerUserId ownerUserId;
    private final MaintenanceType type;
    private final Instant performedAt;
    private MaintenanceStatus status;
    private TechnicianName technician;
    private Instant acknowledgedAt;
    public Maintenance(Long id,Long equipmentId,Long ownerUserId,Instant performedAt,String type,
                       MaintenanceStatus status,String technician,Instant acknowledgedAt) {
        if(id!=null&&id<=0)throw new IllegalArgumentException("Invalid maintenance id");
        if(equipmentId==null||equipmentId<=0||ownerUserId==null||ownerUserId<=0)throw new IllegalArgumentException("Invalid equipment or owner");
        this.id=id;this.equipmentId=new EquipmentId(equipmentId);this.ownerUserId=new OwnerUserId(ownerUserId);
        this.performedAt=Objects.requireNonNull(performedAt,"performedAt");
        this.type=new MaintenanceType(type);this.status=Objects.requireNonNull(status,"status");
        this.technician=new TechnicianName(technician);this.acknowledgedAt=acknowledgedAt;
        if(acknowledgedAt!=null&&this.technician.value().isBlank())throw new IllegalArgumentException("Acknowledgement requires a technician");
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
            technician=new TechnicianName(assigned);
        }
        status=target;registerDomainEvent(new MaintenanceStatusChangedEvent(id,equipmentId.value(),status.name()));
    }
    public void assign(String name){
        if(name==null||name.isBlank()||name.trim().length()>180)throw new IllegalArgumentException("Invalid technician");
        technician=new TechnicianName(name);
    }
    public void acknowledge(Instant at){
        if(technician.value().isBlank())throw new IllegalStateException("Assign a technician first");
        if(acknowledgedAt!=null)throw new IllegalStateException("Already acknowledged");
        acknowledgedAt=Objects.requireNonNull(at);
    }
    public Long getId(){return id;} public Long getEquipmentId(){return equipmentId.value();}
    public Long getOwnerUserId(){return ownerUserId.value();} public Instant getPerformedAt(){return performedAt;}
    public String getType(){return type.value();} public MaintenanceStatus getStatus(){return status;}
    public String getTechnician(){return technician.value();} public Instant getAcknowledgedAt(){return acknowledgedAt;}
    /** Register a local notification fact after persistence assigns the identity. */
    public void onNotificationPersisted(Long persistedId) {
        if (persistedId == null || persistedId < 1 || (id != null && !id.equals(persistedId)))
            throw new IllegalArgumentException("Invalid persisted aggregate id");
        boolean created = domainEvents().stream().anyMatch(MaintenanceCreatedEvent.class::isInstance);
        boolean changed = domainEvents().stream().anyMatch(MaintenanceStatusChangedEvent.class::isInstance);
        if (created && status == MaintenanceStatus.SCHEDULED)
            registerDomainEvent(new MaintenanceNotificationRequestedEvent(persistedId, ownerUserId.value(), NotificationType.MAINTENANCE_SCHEDULED));
        if ((created || changed) && status == MaintenanceStatus.COMPLETED)
            registerDomainEvent(new MaintenanceNotificationRequestedEvent(persistedId, ownerUserId.value(), NotificationType.MAINTENANCE_COMPLETED));
    }
}
