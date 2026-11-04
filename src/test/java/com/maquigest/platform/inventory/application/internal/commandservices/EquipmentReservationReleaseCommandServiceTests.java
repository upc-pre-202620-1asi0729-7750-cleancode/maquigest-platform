package com.maquigest.platform.inventory.application.internal.commandservices;

import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.entities.AvailabilityBlock;
import com.maquigest.platform.inventory.domain.model.commands.ReleaseEquipmentAvailabilityBlockCommand;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import com.maquigest.platform.inventory.domain.model.valueobjects.RentalRate;
import com.maquigest.platform.inventory.domain.repositories.EquipmentCategoryRepository;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import com.maquigest.platform.shared.application.result.Result;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EquipmentReservationReleaseCommandServiceTests {
    @Test void releaseAcquiresExclusiveLockAndSavesHistoricalBlock() {
        var repository=mock(EquipmentRepository.class);
        var service=new EquipmentCommandServiceImpl(repository,mock(EquipmentCategoryRepository.class));
        var item=Equipment.reconstitute(5L,1L,"EQ-2026","Excavadora","Desc",1L,"Lima",
            new RentalRate(BigDecimal.TEN,BigDecimal.valueOf(50)),EquipmentStatus.AVAILABLE,
            List.of(new AvailabilityBlock(9L,LocalDate.parse("2026-11-16"),LocalDate.parse("2026-11-20"))));
        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.of(item));
        when(repository.save(item)).thenReturn(item);
        var result=service.handle(new ReleaseEquipmentAvailabilityBlockCommand(5L,9L));
        assertInstanceOf(Result.Success.class,result);
        verify(repository).findByIdForUpdate(5L);
        verify(repository).save(item);
        assertTrue(item.getAvailabilityBlocks().isEmpty());
        assertNotNull(item.getAllAvailabilityBlocks().getFirst().getReleasedAt());
    }
    @Test void unknownBlockDoesNotChangeDatabase() {
        var repository=mock(EquipmentRepository.class);
        var service=new EquipmentCommandServiceImpl(repository,mock(EquipmentCategoryRepository.class));
        var item=Equipment.reconstitute(5L,1L,"EQ-2026","Excavadora","Desc",1L,"Lima",
            new RentalRate(BigDecimal.TEN,BigDecimal.valueOf(50)),EquipmentStatus.AVAILABLE,List.of());
        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.of(item));
        var result=service.handle(new ReleaseEquipmentAvailabilityBlockCommand(5L,9L));
        assertInstanceOf(Result.Failure.class,result);
        verify(repository,never()).save(any());
    }
}
