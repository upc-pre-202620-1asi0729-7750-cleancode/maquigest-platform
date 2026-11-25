package com.maquigest.platform.shared.architecture;

import com.maquigest.platform.MaquigestPlatformApplication;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.entities.AuditableAbstractPersistenceEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceAuditingConventionTests {
    private static final Path ROOT = Path.of("src", "main", "java");

    @Test void everyPersistenceEntityHasAnExplicitAuditingClassification() throws Exception {
        var audited = new HashSet<String>();
        var exceptions = new HashSet<String>();
        try (var paths = Files.walk(ROOT)) {
            for (var path : paths.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> p.getParent().toString().replace('\\', '/').endsWith("/infrastructure/persistence/jpa/entities")).toList()) {
                var name = ROOT.relativize(path).toString().replace('\\', '.').replace('/', '.').replaceFirst("\\.java$", "");
                var entity = Class.forName(name);
                if (!entity.isAnnotationPresent(Entity.class)) continue;
                var table = entity.getAnnotation(Table.class);
                assertNotNull(table, name);
                var target = AuditableAbstractPersistenceEntity.class.isAssignableFrom(entity) ? audited : exceptions;
                assertTrue(target.add(table.name()), name);
            }
        }
        assertEquals(Set.of("users", "email_verifications", "password_recoveries", "iam_notifications",
                "equipment", "equipment_categories", "equipment_availability_blocks", "inventory_notifications",
                "company_profiles", "profile_notifications", "maintenance_notifications", "rental_notifications",
                "subscription_plans", "user_subscriptions", "subscription_checkouts", "subscriptions_notifications"), audited);
        assertEquals(Set.of("roles", "maintenance_incidents", "maintenance_records", "maintenance_activities",
                "rentals", "rental_requests", "deliveries", "equipment_returns", "rental_request_submission_guards"), exceptions);
    }

    @Test void sharedAuditingBaseAndBootstrapFollowJpaConvention() throws Exception {
        var base = AuditableAbstractPersistenceEntity.class;
        assertTrue(MaquigestPlatformApplication.class.isAnnotationPresent(EnableJpaAuditing.class));
        assertTrue(base.isAnnotationPresent(MappedSuperclass.class));
        var listeners = base.getAnnotation(EntityListeners.class);
        assertNotNull(listeners);
        assertTrue(Set.of(listeners.value()).contains(AuditingEntityListener.class));
        var created = base.getDeclaredField("createdAt");
        var updated = base.getDeclaredField("updatedAt");
        assertTrue(created.isAnnotationPresent(CreatedDate.class));
        assertTrue(updated.isAnnotationPresent(LastModifiedDate.class));
        assertFalse(created.getAnnotation(Column.class).nullable());
        assertFalse(created.getAnnotation(Column.class).updatable());
        assertFalse(updated.getAnnotation(Column.class).nullable());
        assertTrue(updated.getAnnotation(Column.class).updatable());
    }
}
