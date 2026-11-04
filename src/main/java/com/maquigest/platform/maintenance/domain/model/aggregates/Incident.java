package com.maquigest.platform.maintenance.domain.model.aggregates;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import com.maquigest.platform.maintenance.domain.model.valueobjects.IncidentStatus;
import com.maquigest.platform.maintenance.domain.model.events.*;
import java.time.Instant;
import java.util.Objects;
public final class Incident extends AbstractDomainAggregateRoot<Incident> {
    private final Long id, equipmentId, ownerUserId;
    private final String description;
    private final Instant reportedAt;
    private boolean blocksRental;
    private IncidentStatus status;
    private Instant resolvedAt;
    public Incident(Long id,Long equipmentId,Long ownerUserId,String description,Instant reportedAt,
                    boolean blocksRental, IncidentStatus status, Instant resolvedAt) {
        if(id!=null&&id<=0)throw new IllegalArgumentException("Incident id must be positive");
        this.id=id;this.equipmentId=positive(equipmentId,"equipmentId");
        this.ownerUserId=positive(ownerUserId,"ownerUserId");
        if(description==null||description.isBlank()||description.trim().length()>3000)
            throw new IllegalArgumentException("Incident description is required (max 3000)");
        this.description=description.trim();
        this.reportedAt=Objects.requireNonNull(reportedAt,"reportedAt");
        if(reportedAt.isAfter(Instant.now().plusSeconds(60)))throw new IllegalArgumentException("Report date cannot be in the future");
        this.blocksRental=blocksRental;this.status=Objects.requireNonNull(status,"status");
        this.resolvedAt=resolvedAt;
        if((status==IncidentStatus.RESOLVED)!=(resolvedAt!=null))
            throw new IllegalArgumentException("Incident status and resolution date disagree");
        if(resolvedAt!=null&&resolvedAt.isBefore(reportedAt))throw new IllegalArgumentException("Resolution precedes report");
    }
    private static Long positive(Long id,String name){if(id==null||id<=0)throw new IllegalArgumentException(name+" must be positive");return id;}
    public static Incident report(Long equipmentId,Long ownerUserId,String description,boolean blocksRental,Instant at){
        var result=new Incident(null,equipmentId,ownerUserId,description,at,blocksRental,IncidentStatus.OPEN,null);
        result.registerDomainEvent(new IncidentReportedEvent(equipmentId,blocksRental)); return result;
    }
    public void requireRestriction(){
        if(status!=IncidentStatus.OPEN)throw new IllegalStateException("Only OPEN incidents may block rentals");
        if(blocksRental)throw new IllegalStateException("Incident already blocks rentals");
        blocksRental=true;
    }
    public void resolve(Instant at){
        if(status!=IncidentStatus.OPEN)throw new IllegalStateException("Incident already resolved");
        if(at==null||at.isBefore(reportedAt))throw new IllegalArgumentException("Invalid resolution timestamp");
        status=IncidentStatus.RESOLVED;resolvedAt=at;
        registerDomainEvent(new IncidentResolvedEvent(id,equipmentId));
    }
    public Long getId(){return id;} public Long getEquipmentId(){return equipmentId;}
    public Long getOwnerUserId(){return ownerUserId;} public String getDescription(){return description;}
    public Instant getReportedAt(){return reportedAt;} public boolean isBlocksRental(){return blocksRental;}
    public IncidentStatus getStatus(){return status;} public Instant getResolvedAt(){return resolvedAt;}
}
