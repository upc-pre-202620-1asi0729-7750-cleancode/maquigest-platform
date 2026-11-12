package com.maquigest.platform.inventory.domain.model.aggregates;
import com.maquigest.platform.inventory.domain.model.commands.*;
import com.maquigest.platform.inventory.domain.model.entities.AvailabilityBlock;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
class EquipmentTests {
    static CreateEquipmentCommand newCommand() {
        return new CreateEquipmentCommand(1L,"EXC-001","Excavadora","Movimientos de tierra",1L,
            "Lima, La Molina",new BigDecimal("850.00"),new BigDecimal("5100.00"));
    }
    @Test void newEquipmentAlwaysStartsAvailable() {
        var equipment=new Equipment(newCommand());
        assertNull(equipment.getId());
        assertEquals(EquipmentStatus.AVAILABLE,equipment.getStatus());
        assertEquals(1L,equipment.getUserId());
        assertTrue(equipment.getAvailabilityBlocks().isEmpty());
    }
    @Test void validatesRatesAndRequiredFields() {
        assertThrows(IllegalArgumentException.class,()->new Equipment(
            new CreateEquipmentCommand(1L," ","a","b",1L,"Lima",BigDecimal.ONE,BigDecimal.ONE)));
        assertThrows(IllegalArgumentException.class,()->new Equipment(
            new CreateEquipmentCommand(1L,"X","a","b",1L,"Lima",BigDecimal.ZERO,BigDecimal.ONE)));
    }
    @Test void availabilityUsesInclusiveDatesAndOperationalStatus() {
        var asset=Equipment.reconstitute(5L,1L,"X","Excavadora","Desc",1L,"Lima",
            new com.maquigest.platform.inventory.domain.model.valueobjects.RentalRate(BigDecimal.ONE,BigDecimal.TEN),
            EquipmentStatus.AVAILABLE,List.of());
        asset.addAvailabilityBlock(new AvailabilityBlock(8L,LocalDate.parse("2026-11-10"),LocalDate.parse("2026-11-12")));
        assertFalse(asset.isAvailableFor(LocalDate.parse("2026-11-12"),LocalDate.parse("2026-11-13")));
        assertTrue(asset.isAvailableFor(LocalDate.parse("2026-11-13"),LocalDate.parse("2026-11-14")));
        assertThrows(IllegalArgumentException.class,()->asset.addAvailabilityBlock(
            new AvailabilityBlock(null,LocalDate.parse("2026-11-11"),LocalDate.parse("2026-11-13"))));
        asset.changeOperationalStatus(EquipmentStatus.MAINTENANCE);
        assertFalse(asset.isAvailableFor(LocalDate.parse("2026-11-15"),LocalDate.parse("2026-11-16")));
        assertFalse(asset.canBeDeleted());
    }
    @Test void patchLeavesOwnerStatusAndBlocksUnchanged() {
        var asset=Equipment.reconstitute(5L,1L,"X","Old name","Desc",1L,"Lima",
            new com.maquigest.platform.inventory.domain.model.valueobjects.RentalRate(BigDecimal.ONE,BigDecimal.TEN),
            EquipmentStatus.RENTED,List.of(new AvailabilityBlock(8L,LocalDate.parse("2026-11-10"),LocalDate.parse("2026-11-12"))));
        asset.updateEditableFields(new UpdateEquipmentCommand(5L,"Y","New name","New description",2L,"Arequipa",
            BigDecimal.TEN,new BigDecimal("50.00")));
        assertEquals("Y",asset.getCode());
        assertEquals(1L,asset.getUserId());
        assertEquals(EquipmentStatus.RENTED,asset.getStatus());
        assertEquals(1,asset.getAvailabilityBlocks().size());
        assertThrows(UnsupportedOperationException.class,()->asset.getAvailabilityBlocks().clear());
    }
}
