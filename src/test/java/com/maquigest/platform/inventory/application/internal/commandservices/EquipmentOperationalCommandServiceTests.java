package com.maquigest.platform.inventory.application.internal.commandservices;

import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.commands.*;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import com.maquigest.platform.inventory.domain.repositories.EquipmentCategoryRepository;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import com.maquigest.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EquipmentOperationalCommandServiceTests {
    EquipmentRepository repository;
    EquipmentCommandServiceImpl service;
    Equipment equipment;
    @BeforeEach void setup() {
        repository = mock(EquipmentRepository.class);
        var categories = mock(EquipmentCategoryRepository.class);
        service = new EquipmentCommandServiceImpl(repository,categories);
        equipment = Equipment.reconstitute(5L,1L,"X","Excavadora","Desc",1L,"Lima",
            new com.maquigest.platform.inventory.domain.model.valueobjects.RentalRate(
                BigDecimal.ONE,BigDecimal.TEN),EquipmentStatus.AVAILABLE,java.util.List.of());
        when(repository.findByIdForUpdate(5L)).thenReturn(Optional.of(equipment));
        when(repository.save(any())).thenAnswer(call -> call.getArgument(0));
    }
    @Test void reservesAndPersistsUnderExclusiveAccess() {
        var result=service.handle(new ReserveEquipmentPeriodCommand(5L,
            LocalDate.parse("2026-11-10"),LocalDate.parse("2026-11-12")));
        assertInstanceOf(Result.Success.class,result);
        verify(repository).findByIdForUpdate(5L);
        verify(repository).save(any());
        assertEquals(1,equipment.getAvailabilityBlocks().size());
    }
    @Test void rejectsOverlapWithoutSaving() {
        equipment.reservePeriod(LocalDate.parse("2026-11-10"),LocalDate.parse("2026-11-12"));
        var result=service.handle(new ReserveEquipmentPeriodCommand(5L,
            LocalDate.parse("2026-11-12"),LocalDate.parse("2026-11-13")));
        assertInstanceOf(Result.Failure.class,result);
        verify(repository,never()).save(any());
    }
    @Test void rejectsRentedTransitionWithoutBooking() {
        var result=service.handle(new ChangeEquipmentOperationalStatusCommand(5L,EquipmentStatus.RENTED));
        assertInstanceOf(Result.Failure.class,result);
        verify(repository,never()).save(any());
    }
}
