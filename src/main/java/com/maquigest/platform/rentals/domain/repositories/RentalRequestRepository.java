package com.maquigest.platform.rentals.domain.repositories;

import com.maquigest.platform.rentals.domain.model.aggregates.RentalRequest;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import java.util.List;
import java.util.Optional;

public interface RentalRequestRepository {
    Optional<RentalRequest> findById(Long id);
    Optional<RentalRequest> findByIdForUpdate(Long id);
    List<RentalRequest> findVisibleTo(Long actorUserId);
    /** Only PENDING or APPROVED requests owned by the same requester count as conflicts. */
    boolean existsOverlappingActiveRequest(Long equipmentId, Long requesterUserId,
                                            RentalPeriod period, Long excludedRequestId);
    /**
     * Serializes request writes for one equipment/requester pair in the caller's transaction.
     * Acquire this lock BEFORE querying overlapping PENDING/APPROVED requests.
     * It also protects the case where no matching rental request exists yet.
     */
    void lockRequestSubmission(Long equipmentId, Long requesterUserId);

    RentalRequest save(RentalRequest request);
}
