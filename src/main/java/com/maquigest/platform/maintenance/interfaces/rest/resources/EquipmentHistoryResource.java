package com.maquigest.platform.maintenance.interfaces.rest.resources;
import java.util.List;
public record EquipmentHistoryResource(Long equipmentId,String equipmentCode,String equipmentName,List<HistoryEntry> entries) {
    public record HistoryEntry(Long id,String kind,String occurredAt,String description,String status) { }
}
