package com.maquigest.platform.rentals.infrastructure.acl.inventory;
import com.maquigest.platform.rentals.application.exceptions.RentalsException;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.entities.AvailabilityBlock;
import com.maquigest.platform.inventory.domain.model.valueobjects.*;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import java.math.BigDecimal;
import java.time.*;
import java.util.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class InventoryRentalAclAdapterTests {
    private static Equipment equipment(){return Equipment.reconstitute(8L,1L,"EXC-01","Excavadora","Prueba",2L,
        "Lima",new RentalRate(new BigDecimal("900"),new BigDecimal("5400")),EquipmentStatus.AVAILABLE,
        List.of(new AvailabilityBlock(77L,LocalDate.of(2026,11,16),LocalDate.of(2026,11,18))));}
    @Test void onlyOwnerCanReserveAndAlreadyBookedDatesFail(){
        var repo=mock(EquipmentRepository.class);
        when(repo.findByIdForUpdate(8L)).thenReturn(Optional.of(equipment()));
        var acl=new InventoryRentalAclAdapter(repo);
        var days=new RentalPeriod(LocalDate.of(2026,11,17),LocalDate.of(2026,11,18));
        assertEquals(403,assertThrows(RentalsException.class,()->acl.reserve(8L,2L,days)).status().value());
        assertEquals(409,assertThrows(RentalsException.class,()->acl.reserve(8L,1L,days)).status().value());
        verify(repo,never()).save(any());
    }
    @Test void releasingHistoricBlockRequiresRentedAndCorrectOwnership(){
        var repo=mock(EquipmentRepository.class);
        var original=equipment();original.changeOperationalStatus(EquipmentStatus.RENTED);
        when(repo.findByIdForUpdate(8L)).thenReturn(Optional.of(original));
        when(repo.save(any())).thenAnswer(a->a.getArgument(0));
        var acl=new InventoryRentalAclAdapter(repo);
        acl.completeReturn(8L,1L,77L,false);
        assertEquals(EquipmentStatus.AVAILABLE,original.getStatus());
        assertTrue(original.getAvailabilityBlocks().isEmpty());
        assertEquals(1,original.getAllAvailabilityBlocks().size());
        assertNotNull(original.getAllAvailabilityBlocks().get(0).getReleasedAt());
    }
}
