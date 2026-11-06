package com.maquigest.platform.rentals.infrastructure.persistence.jpa.repositories;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Opt-in isolated MySQL schema test: no production data, no DDL, every fixture rolled back. */
class AggregatePersistenceContractMySqlTests {
    private static Connection isolatedConnection() throws SQLException {
        Assumptions.assumeTrue(Boolean.getBoolean("maquigest.phase4PersistenceTests"),
            "Opt-in: -Dmaquigest.phase4PersistenceTests=true");
        String expected = System.getProperty("maquigest.phase4ExpectedDb", "");
        Assumptions.assumeTrue(expected.matches("maquigest_ddd_fase4_[a-z0-9]{8}"),
            "A dedicated Phase 4 database name is required");
        String url = System.getenv("SPRING_DATASOURCE_URL");
        Assumptions.assumeTrue(url != null && url.startsWith("jdbc:mysql://localhost:3306/" + expected + "?"),
            "Database JDBC URL must target the dedicated Phase 4 database");
        Connection connection=DriverManager.getConnection(url,
            System.getenv().getOrDefault("DATABASE_USER", "root"),
            System.getenv().getOrDefault("DATABASE_PASSWORD", ""));
        if (!expected.equals(connection.getCatalog())) {
            connection.close();
            throw new IllegalStateException("Connected to an unexpected database");
        }
        connection.setAutoCommit(false);
        return connection;
    }

    private static void insertDelivery(Connection connection, long rentalId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO deliveries (rental_id, delivered_at, notes) VALUES (?, CURRENT_TIMESTAMP(6), ?)") ) {
            statement.setLong(1,rentalId); statement.setString(2,"Phase 4 transaction fixture");
            statement.executeUpdate();
        }
    }
    private static void insertReturn(Connection connection, long rentalId) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO equipment_returns (rental_id, returned_at, notes, maintenance_required) " +
                "VALUES (?, CURRENT_TIMESTAMP(6), ?, false)") ) {
            statement.setLong(1,rentalId);statement.setString(2,"Phase 4 transaction fixture");
            statement.executeUpdate();
        }
    }
    private static long arbitraryRentalId() {
        return 7_000_000_000_000L + (UUID.randomUUID().getMostSignificantBits() & 0x00FFFFFFL);
    }

    @Test void oneDeliveryPerRentalIsEnforcedByInnoDbUniqueConstraint() throws Exception {
        try (Connection connection=isolatedConnection()) {
            try {
                long rentalId=arbitraryRentalId();
                insertDelivery(connection,rentalId);
                SQLException failure=assertThrows(SQLException.class,()->insertDelivery(connection,rentalId));
                assertEquals("23000",failure.getSQLState());
            } finally { connection.rollback(); }
        }
    }

    @Test void oneReturnPerRentalIsEnforcedByInnoDbUniqueConstraint() throws Exception {
        try (Connection connection=isolatedConnection()) {
            try {
                long rentalId=arbitraryRentalId();
                insertReturn(connection,rentalId);
                SQLException failure=assertThrows(SQLException.class,()->insertReturn(connection,rentalId));
                assertEquals("23000",failure.getSQLState());
            } finally { connection.rollback(); }
        }
    }

    @Test void activityHistoryAllowsSeveralRowsForOneMaintenanceRoot() throws Exception {
        try (Connection connection=isolatedConnection()) {
            try {
                long maintenanceId=arbitraryRentalId();
                try (PreparedStatement insert=connection.prepareStatement(
                    "INSERT INTO maintenance_activities (maintenance_id,status,occurred_at,technician,notes) " +
                    "VALUES (?,'IN_PROGRESS',CURRENT_TIMESTAMP(6),'',?)")) {
                    insert.setLong(1,maintenanceId);insert.setString(2,"First history");insert.executeUpdate();
                    insert.setLong(1,maintenanceId);insert.setString(2,"Second history");insert.executeUpdate();
                }
                try (PreparedStatement count=connection.prepareStatement(
                    "SELECT COUNT(*) FROM maintenance_activities WHERE maintenance_id=?")) {
                    count.setLong(1,maintenanceId);
                    try (ResultSet result=count.executeQuery()) {
                        assertTrue(result.next());
                        assertEquals(2L,result.getLong(1));
                    }
                }
            } finally { connection.rollback(); }
        }
    }
}
