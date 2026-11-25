package com.maquigest.platform.shared.architecture;

import org.junit.jupiter.api.Test;
import java.util.function.Function;
import static org.junit.jupiter.api.Assertions.*;

class ExternalIdentityValueObjectsTests {
    private static void assertIdentity(Function<Long, ?> factory) {
        assertEquals(factory.apply(42L), factory.apply(42L));
        assertNotEquals(factory.apply(42L), factory.apply(43L));
        assertEquals(factory.apply(42L).hashCode(), factory.apply(42L).hashCode());
        assertThrows(IllegalArgumentException.class, () -> factory.apply(null));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(0L));
        assertThrows(IllegalArgumentException.class, () -> factory.apply(-1L));
    }

    @Test void profilesUserIdHasValueSemanticsAndRejectsInvalidIds() {
        assertIdentity(com.maquigest.platform.profiles.domain.model.valueobjects.UserId::new);
        assertEquals(42L, new com.maquigest.platform.profiles.domain.model.valueobjects.UserId(42L).value());
    }

    @Test void inventoryUserIdHasValueSemanticsAndRejectsInvalidIds() {
        assertIdentity(com.maquigest.platform.inventory.domain.model.valueobjects.UserId::new);
        assertEquals(42L, new com.maquigest.platform.inventory.domain.model.valueobjects.UserId(42L).value());
    }

    @Test void subscriptionsUserIdHasValueSemanticsAndRejectsInvalidIds() {
        assertIdentity(com.maquigest.platform.subscriptions.domain.model.valueobjects.UserId::new);
        assertEquals(42L, new com.maquigest.platform.subscriptions.domain.model.valueobjects.UserId(42L).value());
    }

    @Test void maintenanceEquipmentIdHasValueSemanticsAndRejectsInvalidIds() {
        assertIdentity(com.maquigest.platform.maintenance.domain.model.valueobjects.EquipmentId::new);
        assertEquals(42L, new com.maquigest.platform.maintenance.domain.model.valueobjects.EquipmentId(42L).value());
    }

    @Test void maintenanceOwnerUserIdHasValueSemanticsAndRejectsInvalidIds() {
        assertIdentity(com.maquigest.platform.maintenance.domain.model.valueobjects.OwnerUserId::new);
        assertEquals(42L, new com.maquigest.platform.maintenance.domain.model.valueobjects.OwnerUserId(42L).value());
    }

    @Test void rentalsEquipmentIdHasValueSemanticsAndRejectsInvalidIds() {
        assertIdentity(com.maquigest.platform.rentals.domain.model.valueobjects.EquipmentId::new);
        assertEquals(42L, new com.maquigest.platform.rentals.domain.model.valueobjects.EquipmentId(42L).value());
    }

    @Test void rentalsConstructionUserIdHasValueSemanticsAndRejectsInvalidIds() {
        assertIdentity(com.maquigest.platform.rentals.domain.model.valueobjects.ConstructionUserId::new);
        assertEquals(42L, new com.maquigest.platform.rentals.domain.model.valueobjects.ConstructionUserId(42L).value());
    }

    @Test void rentalsRentalCompanyUserIdHasValueSemanticsAndRejectsInvalidIds() {
        assertIdentity(com.maquigest.platform.rentals.domain.model.valueobjects.RentalCompanyUserId::new);
        assertEquals(42L, new com.maquigest.platform.rentals.domain.model.valueobjects.RentalCompanyUserId(42L).value());
    }

    @Test void rentalsAvailabilityBlockIdHasValueSemanticsAndRejectsInvalidIds() {
        assertIdentity(com.maquigest.platform.rentals.domain.model.valueobjects.AvailabilityBlockId::new);
        assertEquals(42L, new com.maquigest.platform.rentals.domain.model.valueobjects.AvailabilityBlockId(42L).value());
    }

    @Test void aggregatesKeepExternalIdentitiesInTheirOwnContext() throws NoSuchFieldException {
        assertEquals(com.maquigest.platform.profiles.domain.model.valueobjects.UserId.class,
            com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile.class.getDeclaredField("userId").getType());
        assertEquals(com.maquigest.platform.inventory.domain.model.valueobjects.UserId.class,
            com.maquigest.platform.inventory.domain.model.aggregates.Equipment.class.getDeclaredField("userId").getType());
        assertEquals(com.maquigest.platform.subscriptions.domain.model.valueobjects.UserId.class,
            com.maquigest.platform.subscriptions.domain.model.aggregates.UserSubscription.class.getDeclaredField("userId").getType());
        assertEquals(com.maquigest.platform.subscriptions.domain.model.valueobjects.UserId.class,
            com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout.class.getDeclaredField("userId").getType());
        assertEquals(com.maquigest.platform.maintenance.domain.model.valueobjects.EquipmentId.class,
            com.maquigest.platform.maintenance.domain.model.aggregates.Incident.class.getDeclaredField("equipmentId").getType());
        assertEquals(com.maquigest.platform.maintenance.domain.model.valueobjects.OwnerUserId.class,
            com.maquigest.platform.maintenance.domain.model.aggregates.Incident.class.getDeclaredField("ownerUserId").getType());
        assertEquals(com.maquigest.platform.maintenance.domain.model.valueobjects.EquipmentId.class,
            com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance.class.getDeclaredField("equipmentId").getType());
        assertEquals(com.maquigest.platform.maintenance.domain.model.valueobjects.OwnerUserId.class,
            com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance.class.getDeclaredField("ownerUserId").getType());
        assertEquals(com.maquigest.platform.rentals.domain.model.valueobjects.EquipmentId.class,
            com.maquigest.platform.rentals.domain.model.aggregates.Rental.class.getDeclaredField("equipmentId").getType());
        assertEquals(com.maquigest.platform.rentals.domain.model.valueobjects.ConstructionUserId.class,
            com.maquigest.platform.rentals.domain.model.aggregates.Rental.class.getDeclaredField("constructionUserId").getType());
        assertEquals(com.maquigest.platform.rentals.domain.model.valueobjects.RentalCompanyUserId.class,
            com.maquigest.platform.rentals.domain.model.aggregates.Rental.class.getDeclaredField("rentalCompanyUserId").getType());
        assertEquals(com.maquigest.platform.rentals.domain.model.valueobjects.AvailabilityBlockId.class,
            com.maquigest.platform.rentals.domain.model.aggregates.Rental.class.getDeclaredField("availabilityBlockId").getType());
        assertEquals(com.maquigest.platform.rentals.domain.model.valueobjects.EquipmentId.class,
            com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest.class.getDeclaredField("equipmentId").getType());
        assertEquals(com.maquigest.platform.rentals.domain.model.valueobjects.ConstructionUserId.class,
            com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest.class.getDeclaredField("constructionUserId").getType());
        assertEquals(com.maquigest.platform.rentals.domain.model.valueobjects.RentalCompanyUserId.class,
            com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest.class.getDeclaredField("rentalCompanyUserId").getType());
    }
    @Test void sameNumericParticipantCannotBypassRentalRequestInvariant() {
        var period = new com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod(
            java.time.LocalDate.of(2026, 11, 26), java.time.LocalDate.of(2026, 11, 27));
        assertThrows(IllegalArgumentException.class, () ->
            new com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest(
                null, 10L, 7L, 7L, period,
                com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus.PENDING,
                java.time.Instant.now()));
    }

    @Test void checkoutPreservesNumericOwnerOnIssueAndReconstitution() {
        var expiry = java.time.Instant.now().plusSeconds(600);
        var checkout = com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout.issue(
            7L, 2L, 1000L, expiry);
        assertEquals(7L, checkout.getUserId());
        var restored = com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionCheckout.reconstitute(
            1L, checkout.getReference(), 7L, 2L, 1000L, expiry, null,
            com.maquigest.platform.subscriptions.domain.model.valueobjects.CheckoutStatus.PENDING, null);
        assertEquals(7L, restored.getUserId());
    }
}
