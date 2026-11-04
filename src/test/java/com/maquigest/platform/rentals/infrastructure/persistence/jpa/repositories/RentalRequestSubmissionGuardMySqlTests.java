package com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Opt-in real InnoDB test. Does not create or modify a business rental request.
 * Run with -Dmaquigest.mysqlConcurrencyTests=true and provide URL/user/password.
 */
class RentalRequestSubmissionGuardMySqlTests {
    private static final String UPSERT = """
        INSERT INTO rental_request_submission_guards (equipment_id, construction_user_id)
        VALUES (?, ?) ON DUPLICATE KEY UPDATE equipment_id = equipment_id
        """;

    private static void lockPair(Connection connection, long equipmentId, long requesterId)
            throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(UPSERT)) {
            statement.setLong(1, equipmentId);
            statement.setLong(2, requesterId);
            statement.executeUpdate();
        }
    }

    @Test
    void secondTransactionWaitsForTheFirstInnoDbGuardLock() throws Exception {
        Assumptions.assumeTrue(Boolean.getBoolean("maquigest.mysqlConcurrencyTests"),
            "Enable this opt-in integration test with -Dmaquigest.mysqlConcurrencyTests=true");

        String url = System.getProperty("maquigest.mysqlUrl");
        Assumptions.assumeTrue(url != null && url.startsWith("jdbc:mysql:"),
            "Provide -Dmaquigest.mysqlUrl=jdbc:mysql://localhost:3306/<database>");
        String username = System.getenv().getOrDefault("DATABASE_USER", "root");
        String password = System.getenv().getOrDefault("DATABASE_PASSWORD", "");
        // Dedicated, high positive identifiers: avoids interacting with existing rental records.
        long equipmentId = 9_000_000_000_000L + System.nanoTime() % 1_000_000_000L;
        long requesterId = 8_000_000_000_000L + System.nanoTime() % 1_000_000_000L;

        try (Connection first = DriverManager.getConnection(url, username, password);
             Connection second = DriverManager.getConnection(url, username, password)) {
            first.setAutoCommit(false);
            second.setAutoCommit(false);
            try {
                lockPair(first, equipmentId, requesterId);
                CountDownLatch aboutToLock = new CountDownLatch(1);
                // Release the first lock in a finally block *before* closing the executor;
                // otherwise an assertion failure could leave its JDBC worker blocked indefinitely.
                try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
                    try {
                        var future = executor.submit(() -> {
                            aboutToLock.countDown();
                            lockPair(second, equipmentId, requesterId);
                            return true;
                        });
                        assertTrue(aboutToLock.await(5, TimeUnit.SECONDS));
                        assertThrows(TimeoutException.class, () -> future.get(350, TimeUnit.MILLISECONDS),
                            "Second transaction should wait while the first transaction owns the row");
                        first.commit();
                        assertTrue(future.get(10, TimeUnit.SECONDS));
                        second.commit();
                    } finally {
                        first.rollback();
                    }
                }
            } finally {
                // Rollback safely whether committed or not; cleanup happens in a separate transaction.
                first.rollback();
                second.rollback();
            }
        } finally {
            try (Connection cleanup = DriverManager.getConnection(url, username, password);
                 PreparedStatement delete = cleanup.prepareStatement(
                    "DELETE FROM rental_request_submission_guards WHERE equipment_id = ? AND construction_user_id = ?")) {
                delete.setLong(1, equipmentId);
                delete.setLong(2, requesterId);
                delete.executeUpdate();
            }
        }
    }
}
