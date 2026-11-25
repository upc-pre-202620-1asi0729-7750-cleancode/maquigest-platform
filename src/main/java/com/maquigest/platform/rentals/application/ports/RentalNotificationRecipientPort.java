package com.maquigest.platform.rentals.application.ports;

import java.util.Optional;

public interface RentalNotificationRecipientPort {
    Optional<String> findActiveEmail(Long userId);
}
