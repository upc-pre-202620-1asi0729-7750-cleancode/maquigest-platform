package com.maquigest.platform.inventory.domain.model.aggregates;

import com.maquigest.platform.inventory.domain.model.commands.CreateEquipmentCommand;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class EquipmentOperationalTests {
    private static Equipment newEquipment() {
        return new Equipment(new CreateEquipmentCommand(1L,"EQ-2026","Excavadora","Descripcion",1L,
            "Lima",new BigDecimal("10.00"),new BigDecimal("50.00")));
    }
    @Test void cannotEnterRentedStateWithoutReservation() {
        var equipment = newEquipment();
        assertThrows(IllegalStateException.class, () -> equipment.changeOperationalStatus(EquipmentStatus.RENTED));
    }
    @Test void reservesInclusiveDatesAndRejectsOverlap() {
        var equipment = newEquipment();
        equipment.reservePeriod(LocalDate.parse("2026-11-10"),LocalDate.parse("2026-11-12"));
        assertEquals(1,equipment.getAvailabilityBlocks().size());
        assertFalse(equipment.isAvailableFor(LocalDate.parse("2026-11-12"),LocalDate.parse("2026-11-14")));
        assertTrue(equipment.isAvailableFor(LocalDate.parse("2026-11-13"),LocalDate.parse("2026-11-14")));
        assertThrows(IllegalStateException.class, () -> equipment.reservePeriod(
            LocalDate.parse("2026-11-12"),LocalDate.parse("2026-11-14")));
    }
    @Test void enforcesTransitionsAndPreservesReservations() {
        var equipment = newEquipment();
        equipment.reservePeriod(LocalDate.parse("2026-11-10"),LocalDate.parse("2026-11-12"));
        equipment.changeOperationalStatus(EquipmentStatus.RENTED);
        assertEquals(EquipmentStatus.RENTED,equipment.getStatus());
        equipment.changeOperationalStatus(EquipmentStatus.RENTED); // idempotent
        equipment.changeOperationalStatus(EquipmentStatus.MAINTENANCE);
        assertThrows(IllegalStateException.class, () -> equipment.changeOperationalStatus(EquipmentStatus.RENTED));
        assertEquals(1,equipment.getAvailabilityBlocks().size());
    }
}
