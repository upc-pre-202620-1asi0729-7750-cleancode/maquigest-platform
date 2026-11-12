package com.maquigest.platform.rentals.domain.repositories;
import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import java.util.*;
public interface RentalRepository {
    Optional<Rental> findById(Long id);
    Optional<Rental> findByIdForUpdate(Long id);
    Optional<Rental> findByRequestId(Long requestId);
    List<Rental> findVisibleTo(Long actorUserId);
    Rental save(Rental rental);
}
