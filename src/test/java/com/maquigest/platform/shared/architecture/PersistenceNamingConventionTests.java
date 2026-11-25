package com.maquigest.platform.shared.architecture;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.JoinTable;
import com.maquigest.platform.iam.infrastructure.persistence.jpa.entities.UserPersistenceEntity;
import com.maquigest.platform.shared.infrastructure.persistence.jpa.configuration.strategy.SnakeCaseWithPluralizedTablePhysicalNamingStrategy;
import org.hibernate.boot.model.naming.Identifier;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import java.lang.reflect.ParameterizedType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class PersistenceNamingConventionTests {
    private static final Path ROOT = Path.of("src", "main", "java");

    private List<Class<?>> classesIn(String packageSuffix) throws Exception {
        try (var paths = Files.walk(ROOT)) {
            var result = new java.util.ArrayList<Class<?>>();
            for (var path : paths.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> p.getParent().toString().replace('\\', '/').endsWith(packageSuffix)).toList()) {
                String name = ROOT.relativize(path).toString().replace('\\', '.').replace('/', '.').replaceFirst("\\.java$", "");
                result.add(Class.forName(name));
            }
            return result;
        }
    }

    @Test void springDataRepositoriesFollowPersistenceRepositoryConvention() throws Exception {
        var repositories = classesIn("/infrastructure/persistence/jpa/repositories");
        assertTrue(repositories.size() >= 24);
        for (var repository : repositories) {
            assertTrue(repository.isInterface(), repository.getName());
            assertTrue(JpaRepository.class.isAssignableFrom(repository), repository.getName());
            var generic = (ParameterizedType) repository.getGenericInterfaces()[0];
            var entity = (Class<?>) generic.getActualTypeArguments()[0];
            assertTrue(entity.isAnnotationPresent(Entity.class), entity.getName());
            assertTrue(entity.getSimpleName().endsWith("PersistenceEntity"), entity.getName());
            assertEquals(entity.getSimpleName().replaceFirst("PersistenceEntity$", "PersistenceRepository"), repository.getSimpleName());
        }
    }

    @Test void explicitPhysicalTableNamesRemainCompatibleWithExistingDatabase() throws Exception {
        var strategy = new SnakeCaseWithPluralizedTablePhysicalNamingStrategy();
        var tables = new HashSet<String>();
        for (var entity : classesIn("/infrastructure/persistence/jpa/entities")) {
            if (!entity.isAnnotationPresent(Entity.class)) continue;
            var table = entity.getAnnotation(Table.class);
            assertNotNull(table, entity.getName());
            assertTrue(tables.add(table.name()), table.name());
            assertEquals(table.name(), strategy.toPhysicalTableName(Identifier.toIdentifier(table.name()), null).getText());
        }
        assertEquals(Set.of(
            "company_profiles", "deliveries", "email_verifications", "equipment",
            "equipment_availability_blocks", "equipment_categories", "equipment_returns",
            "iam_notifications", "inventory_notifications", "maintenance_activities",
            "maintenance_incidents", "maintenance_notifications", "maintenance_records",
            "password_recoveries", "profile_notifications", "rental_notifications",
            "rental_request_submission_guards", "rental_requests", "rentals", "roles",
            "subscription_checkouts", "subscription_plans", "subscriptions_notifications",
            "user_subscriptions", "users"), tables);
        var join = UserPersistenceEntity.class.getDeclaredField("roles").getAnnotation(JoinTable.class);
        assertNotNull(join);
        assertEquals("user_roles", join.name());
        assertEquals(join.name(), strategy.toPhysicalTableName(Identifier.toIdentifier(join.name()), null).getText());
    }
}
