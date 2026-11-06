package com.maquigest.platform.iam.infrastructure.persistence.jpa.adapters;

import com.maquigest.platform.iam.interfaces.acl.IamSubscriberFacade;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.sql.Statement;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Opt-in integration tests against a dedicated MySQL InnoDB database.
 * No production code or schema changes. Never enable against the business database.
 */
@SpringBootTest
@EnabledIfSystemProperty(named = "maquigest.iamLockTests", matches = "true")
class IamSubscriberFacadeMySqlIntegrationTests {

    @Autowired private IamSubscriberFacade facade;
    @Autowired private PlatformTransactionManager transactionManager;
    @Autowired private JdbcTemplate jdbc;

    private void requireDedicatedDatabase() {
        String expected = System.getProperty("maquigest.iamExpectedDb", "");
        assertTrue(expected.matches("maquigest_ddd_[a-z0-9_]+"),
                "Pass -Dmaquigest.iamExpectedDb=<dedicated maquigest_ddd_... database>");
        assertEquals(expected, jdbc.queryForObject("SELECT DATABASE()", String.class),
                "Refusing to operate on a database other than the dedicated test database");
    }

    @Test
    void lockingRequiresAnExistingTransaction() {
        requireDedicatedDatabase();
        assertThrows(IllegalTransactionStateException.class,
                () -> facade.lockActiveRentalCompany(987654321L));
    }

    @Test
    void secondTransactionWaitsUntilFirstSubscriptionLockCommits() throws Exception {
        requireDedicatedDatabase();

        Long roleId = jdbc.queryForObject(
                "SELECT id FROM roles WHERE name = 'ROLE_RENTAL_COMPANY'", Long.class);
        assertNotNull(roleId, "The standard IAM role seeder must have created ROLE_RENTAL_COMPANY");

        String email = "ddd-lock-" + UUID.randomUUID() + "@example.invalid";
        var keys = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement("""
                INSERT INTO users
                    (first_name, last_name, email, password, company_name, status, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, 'ACTIVE', NOW(6), NOW(6))
                """, Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, "DDD");
            statement.setString(2, "Concurrency Test");
            statement.setString(3, email);
            statement.setString(4, "not-used-for-authentication");
            statement.setString(5, "DDD Test Only");
            return statement;
        }, keys);

        Long userId = Objects.requireNonNull(keys.getKey(), "MySQL did not return the test user ID")
                .longValue();
        TransactionStatus first = null;
        try {
            jdbc.update("INSERT INTO user_roles (user_id, role_id) VALUES (?, ?)", userId, roleId);
            first = transactionManager.getTransaction(new DefaultTransactionDefinition());

            assertTrue(facade.lockActiveRentalCompany(userId),
                    "An ACTIVE rental company should be eligible for subscriptions");

            try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                var started = new CountDownLatch(1);
                var future = executor.submit(() -> {
                    var template = new TransactionTemplate(transactionManager);
                    return template.execute(status -> {
                        started.countDown();
                        return facade.lockActiveRentalCompany(userId);
                    });
                });
                try {
                    assertTrue(started.await(5, TimeUnit.SECONDS),
                            "The competing transaction did not start");
                    assertThrows(TimeoutException.class,
                            () -> future.get(500, TimeUnit.MILLISECONDS),
                            "The competing transaction must wait for the first write lock");

                    transactionManager.commit(first);
                    first = null;
                    assertEquals(Boolean.TRUE, future.get(10, TimeUnit.SECONDS),
                            "The competing transaction should succeed after commit");
                } finally {
                    // Always release the first lock before closing the executor, including on failure.
                    if (first != null && !first.isCompleted()) {
                        transactionManager.rollback(first);
                        first = null;
                    }
                }
            }
        } finally {
            if (first != null && !first.isCompleted()) {
                transactionManager.rollback(first);
            }
            // Only remove the test-owned user and its role membership.
            jdbc.update("DELETE FROM user_roles WHERE user_id = ?", userId);
            jdbc.update("DELETE FROM users WHERE id = ?", userId);
        }
    }
}
