package com.maquigest.platform.maintenance.domain.model.entities;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import java.time.Instant;
public record MaintenanceActivity(Long id,Long maintenanceId,MaintenanceStatus status,Instant occurredAt,
                                  String technician,String notes) {
    public MaintenanceActivity {
        if(id!=null&&id<=0)throw new IllegalArgumentException("Invalid activity id");
        if(maintenanceId!=null&&maintenanceId<=0)throw new IllegalArgumentException("Invalid maintenance id");
        if(status==null||occurredAt==null)throw new IllegalArgumentException("Activity status and date are required");
        technician=technician==null?"":technician.trim();notes=notes==null?"":notes.trim();
        if(technician.length()>180||notes.length()>2000)throw new IllegalArgumentException("Activity note or technician exceeds limit");
    }
}
