package com.maquigest.platform.inventory.interfaces.rest.transform;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.entities.AvailabilityBlock;
import com.maquigest.platform.inventory.interfaces.rest.resources.*;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
/** REST adapter. Equipment blocks are calendar-day ranges in Peru time (UTC-05). */
public final class EquipmentResourceFromEntityAssembler {
    private static final ZoneId BUSINESS_ZONE = ZoneId.of("America/Lima");
    private EquipmentResourceFromEntityAssembler() { }
    public static EquipmentResource toResourceFromEntity(Equipment e) {
        return new EquipmentResource(e.getId(),e.getUserId(),e.getCode(),e.getName(),
            e.getDescription(),e.getCategoryId(),e.getLocation(),e.getRentalRate().dailyRate(),
            e.getRentalRate().weeklyRate(),e.getStatus(),e.getAvailabilityBlocks().stream()
                .map(EquipmentResourceFromEntityAssembler::block).toList());
    }
    private static AvailabilityBlockResource block(AvailabilityBlock b) {
        var start = b.getStartDate().atStartOfDay(BUSINESS_ZONE).format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        var end = b.getEndDate().atTime(23,59,59,999_000_000).atZone(BUSINESS_ZONE)
            .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        return new AvailabilityBlockResource(b.getId(),start,end);
    }
}
