package com.maquigest.platform.rentals.domain.repositories;
import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import java.util.*;
public interface RentalRequestRepository {
    Optional<RentalRequest> findById(Long id);
    Optional<RentalRequest> findByIdForUpdate(Long id);
    List<RentalRequest> findVisibleTo(Long actorUserId);
    RentalRequest save(RentalRequest request);
}
