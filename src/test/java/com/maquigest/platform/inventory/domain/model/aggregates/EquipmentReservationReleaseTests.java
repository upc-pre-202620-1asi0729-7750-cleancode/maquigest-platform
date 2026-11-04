package com.maquigest.platform.inventory.domain.model.aggregates;

import com.maquigest.platform.inventory.domain.model.entities.AvailabilityBlock;
import com.maquigest.platform.inventory.domain.model.commands.CreateEquipmentCommand;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class EquipmentReservationReleaseTests {
    private static Equipment equipment() {
        return Equipment.reconstitute(30L,1L,"EQ-TEST","Excavadora","Desc",1L,"Lima",
            new com.maquigest.platform.inventory.domain.model.valueobjects.RentalRate(
                BigDecimal.TEN,BigDecimal.valueOf(50)),EquipmentStatus.AVAILABLE,
            List.of(new AvailabilityBlock(77L,LocalDate.parse("2026-11-16"),LocalDate.parse("2026-11-20"))));
    }
    @Test void releaseMakesPeriodAvailableButPreservesHistory() {
        var item=equipment();
        var instant=Instant.parse("2026-11-04T13:00:00Z");
        assertFalse(item.isAvailableFor(LocalDate.parse("2026-11-20"),LocalDate.parse("2026-11-20")));
        item.releaseAvailabilityBlock(77L,instant);
        assertTrue(item.isAvailableFor(LocalDate.parse("2026-11-20"),LocalDate.parse("2026-11-20")));
        assertTrue(item.getAvailabilityBlocks().isEmpty());
        assertEquals(1,item.getAllAvailabilityBlocks().size());
        assertEquals(instant,item.getAllAvailabilityBlocks().getFirst().getReleasedAt());
        assertFalse(item.canBeDeleted()); // do not erase rental history
    }
    @Test void repeatedReleaseDoesNotOverwriteAuditTime() {
        var item=equipment();
        item.releaseAvailabilityBlock(77L,Instant.parse("2026-11-04T13:00:00Z"));
        item.releaseAvailabilityBlock(77L,Instant.parse("2026-11-05T13:00:00Z"));
        assertEquals(Instant.parse("2026-11-04T13:00:00Z"),item.getAllAvailabilityBlocks().getFirst().getReleasedAt());
    }
    @Test void activeBlockCanReuseDatesOfReleasedHistoricalBlock() {
        var item=equipment();
        item.releaseAvailabilityBlock(77L,Instant.parse("2026-11-04T13:00:00Z"));
        item.reservePeriod(LocalDate.parse("2026-11-16"),LocalDate.parse("2026-11-20"));
        assertEquals(1,item.getAvailabilityBlocks().size());
        assertEquals(2,item.getAllAvailabilityBlocks().size());
    }
    @Test void rejectsReleaseWhileRented() {
        var item=equipment();
        item.changeOperationalStatus(EquipmentStatus.RENTED);
        assertThrows(IllegalStateException.class,()->item.releaseAvailabilityBlock(77L,Instant.now()));
        assertEquals(1,item.getAvailabilityBlocks().size());
    }
    @Test void rejectsWrongBlockId() {
        assertThrows(java.util.NoSuchElementException.class,()->equipment().releaseAvailabilityBlock(999L,Instant.now()));
    }
}
