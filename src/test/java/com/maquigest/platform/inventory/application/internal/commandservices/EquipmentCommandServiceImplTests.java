package com.maquigest.platform.inventory.application.internal.commandservices;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.commands.CreateEquipmentCommand;
import com.maquigest.platform.inventory.domain.repositories.*;
import com.maquigest.platform.shared.application.result.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
class EquipmentCommandServiceImplTests {
    EquipmentRepository assets;
    EquipmentCategoryRepository categories;
    EquipmentCommandServiceImpl service;
    @BeforeEach void setup() {
        assets=mock(EquipmentRepository.class);
        categories=mock(EquipmentCategoryRepository.class);
        service=new EquipmentCommandServiceImpl(assets,categories);
    }
    static CreateEquipmentCommand command() {
        return new CreateEquipmentCommand(1L,"EXC-001","Excavadora","Descripcion",1L,"Lima",
            new BigDecimal("850.00"),new BigDecimal("5100.00"));
    }
    @Test void missingCategoryIsNotFoundAndDoesNotPersist() {
        when(categories.existsById(1L)).thenReturn(false);
        var result=service.handle(command());
        assertInstanceOf(Result.Failure.class,result);
        assertEquals("EQUIPMENTCATEGORY_NOT_FOUND",
            ((com.maquigest.platform.shared.application.result.ApplicationError)((Result.Failure<?,?>)result).error()).code());
        verify(assets,never()).save(any());
    }
    @Test void duplicateCodeIsConflictAndDoesNotPersist() {
        when(categories.existsById(1L)).thenReturn(true);
        when(assets.existsByUserIdAndCodeIgnoreCase(1L,"EXC-001")).thenReturn(true);
        var result=service.handle(command());
        assertInstanceOf(Result.Failure.class,result);
        verify(assets,never()).save(any());
    }
    @Test void createsPersistedEquipmentAndReturnsId() {
        when(categories.existsById(1L)).thenReturn(true);
        when(assets.save(any())).thenAnswer(call->{
            Equipment entity=call.getArgument(0);
            return Equipment.reconstitute(10L,entity.getUserId(),entity.getCode(),entity.getName(),
                entity.getDescription(),entity.getCategoryId(),entity.getLocation(),entity.getRentalRate(),
                entity.getStatus(),List.of());
        });
        var result=service.handle(command());
        assertInstanceOf(Result.Success.class,result);
        assertEquals(10L,((Result.Success<?,?>)result).value());
    }
}
