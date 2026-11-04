package com.maquigest.platform.inventory.infrastructure.persistence.jpa;

import com.maquigest.platform.inventory.infrastructure.persistence.jpa.assemblers.EquipmentPersistenceAssembler;
import com.maquigest.platform.inventory.infrastructure.persistence.jpa.entities.AvailabilityBlockPersistenceEntity;
import com.maquigest.platform.inventory.infrastructure.persistence.jpa.entities.EquipmentPersistenceEntity;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

class EquipmentReservationReleasePersistenceAssemblerTests {
    @Test void historicalRowIsPreservedAndExcludedFromActiveReservations() {
        var persistence = new EquipmentPersistenceEntity();
        persistence.setId(5L);
        persistence.setUserId(1L);
        persistence.setCode("EQ-TEST");
        persistence.setName("Excavadora");
        persistence.setDescription("Descripcion");
        persistence.setCategoryId(1L);
        persistence.setLocation("Lima");
        persistence.setDailyRate(BigDecimal.valueOf(100));
        persistence.setWeeklyRate(BigDecimal.valueOf(600));
        persistence.setStatus(EquipmentStatus.AVAILABLE);
        var child=new AvailabilityBlockPersistenceEntity();
        child.setId(9L);
        child.setStartDate(LocalDate.parse("2026-11-16"));
        child.setEndDate(LocalDate.parse("2026-11-20"));
        persistence.addAvailabilityBlock(child);

        var domain=EquipmentPersistenceAssembler.toDomainFromPersistence(persistence);
        domain.releaseAvailabilityBlock(9L, Instant.parse("2026-11-04T13:00:00Z"));
        EquipmentPersistenceAssembler.copyToExistingPersistence(domain,persistence);

        assertEquals(1,persistence.getAvailabilityBlocks().size(),"History row must not be deleted");
        assertEquals(9L,persistence.getAvailabilityBlocks().getFirst().getId());
        assertEquals(Instant.parse("2026-11-04T13:00:00Z"),
            persistence.getAvailabilityBlocks().getFirst().getReleasedAt());
        var reloaded=EquipmentPersistenceAssembler.toDomainFromPersistence(persistence);
        assertTrue(reloaded.getAvailabilityBlocks().isEmpty(),"Released period is not active");
        assertEquals(1,reloaded.getAllAvailabilityBlocks().size(),"Audit row remains in domain");
    }
}
