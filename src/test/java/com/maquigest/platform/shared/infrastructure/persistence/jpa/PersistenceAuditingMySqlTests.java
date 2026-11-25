package com.maquigest.platform.shared.infrastructure.persistence.jpa;

import com.maquigest.platform.inventory.infrastructure.persistence.jpa.entities.EquipmentCategoryPersistenceEntity;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/** Opt-in: use only a dedicated Phase 4 schema. The fixture is rolled back. */
@SpringBootTest
@EnabledIfSystemProperty(named = "maquigest.phase4AuditingTests", matches = "true")
class PersistenceAuditingMySqlTests {
    @PersistenceContext
    private EntityManager entityManager;

    @DynamicPropertySource
    static void isolatedDatabase(DynamicPropertyRegistry registry) {
        var expected = System.getProperty("maquigest.phase4ExpectedDb", "");
        assertTrue(expected.matches("maquigest_ddd_fase4_[a-f0-9]{8}"), "Dedicated schema required");
        var url = System.getenv("SPRING_DATASOURCE_URL");
        assertNotNull(url);
        assertEquals("jdbc:mysql://localhost:3306/" + expected +
                "?createDatabaseIfNotExist=true&allowPublicKeyRetrieval=true&serverTimezone=UTC&useSSL=false", url);
        registry.add("spring.datasource.url", () -> url);
        registry.add("maquigest.email.enabled", () -> false);
        registry.add("maquigest.stripe.enabled", () -> false);
        registry.add("maquigest.stripe.required", () -> false);
        registry.add("maquigest.maps.enabled", () -> false);
    }

    @Test
    @Transactional
    void auditingPopulatesCreationAndPreservesItWhileAdvancingModification() {
        var expected = System.getProperty("maquigest.phase4ExpectedDb", "");
        assertEquals(expected, entityManager.createNativeQuery("SELECT DATABASE()").getSingleResult());

        var category = new EquipmentCategoryPersistenceEntity();
        category.setName("QA auditing " + UUID.randomUUID());
        entityManager.persist(category);
        entityManager.flush();
        var id = category.getId();
        assertNotNull(id);
        entityManager.clear();
        var stored = entityManager.find(EquipmentCategoryPersistenceEntity.class, id);
        assertNotNull(stored.getCreatedAt());
        assertNotNull(stored.getUpdatedAt());
        assertFalse(stored.getUpdatedAt().before(stored.getCreatedAt()));

        // Make only our fixture look historical to avoid a wall-clock sleep and
        // timestamp-precision races; never backfill real records in this test.
        assertEquals(1, entityManager.createNativeQuery(
                "UPDATE equipment_categories SET created_at = '2000-01-01 00:00:00', updated_at = '2000-01-01 00:00:00' WHERE id = :id")
                .setParameter("id", id).executeUpdate());
        entityManager.clear();
        var historical = entityManager.find(EquipmentCategoryPersistenceEntity.class, id);
        var originalCreated = historical.getCreatedAt().getTime();
        var originalUpdated = historical.getUpdatedAt().getTime();
        historical.setName("QA updated " + UUID.randomUUID());
        entityManager.flush();
        entityManager.clear();
        var modified = entityManager.find(EquipmentCategoryPersistenceEntity.class, id);
        assertEquals(originalCreated, modified.getCreatedAt().getTime());
        assertTrue(modified.getUpdatedAt().getTime() > originalUpdated);
        assertTrue(modified.getName().startsWith("QA updated "));
    }
}
