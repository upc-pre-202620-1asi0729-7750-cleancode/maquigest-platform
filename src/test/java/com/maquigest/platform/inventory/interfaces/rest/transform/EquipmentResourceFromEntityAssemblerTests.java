package com.maquigest.platform.inventory.interfaces.rest.transform;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.commands.CreateEquipmentCommand;
import com.maquigest.platform.inventory.domain.model.entities.AvailabilityBlock;
import com.maquigest.platform.inventory.domain.model.valueobjects.*;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class EquipmentResourceFromEntityAssemblerTests {
    @Test void serializableResourceUsesAngularFieldNames() {
        Equipment e=Equipment.reconstitute(10L,1L,"EXC-001","Excavadora","Desc",1L,"La Molina",
            new RentalRate(new BigDecimal("850.00"),new BigDecimal("5100.00")),EquipmentStatus.AVAILABLE,
            List.of(new AvailabilityBlock(9L,LocalDate.parse("2026-11-10"),LocalDate.parse("2026-11-12"))));
        var r=EquipmentResourceFromEntityAssembler.toResourceFromEntity(e);
        assertEquals(10L,r.id());
        assertEquals(1L,r.userId());
        assertEquals("AVAILABLE",r.status().name());
        assertEquals("2026-11-10T00:00:00-05:00",r.availabilityBlocks().get(0).startDate());
        assertEquals("2026-11-12T23:59:59.999-05:00",r.availabilityBlocks().get(0).endDate());
    }
}
