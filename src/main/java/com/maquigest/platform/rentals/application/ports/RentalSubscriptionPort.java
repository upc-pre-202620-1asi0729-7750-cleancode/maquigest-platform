package com.maquigest.platform.rentals.application.ports;
public interface RentalSubscriptionPort {
    boolean hasActiveSubscription(Long rentalCompanyUserId);
    boolean canManageRentals(Long rentalCompanyUserId);
}
