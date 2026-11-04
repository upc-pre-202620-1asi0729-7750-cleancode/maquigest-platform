package com.maquigest.platform.rentals.application.internal.queryservices;
import com.maquigest.platform.rentals.application.queryservices.RentalQueryService;
import com.maquigest.platform.rentals.domain.model.aggregates.*;
import com.maquigest.platform.rentals.domain.model.queries.*;
import com.maquigest.platform.rentals.domain.model.entities.*;
import com.maquigest.platform.rentals.domain.repositories.*;
import java.util.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service @Transactional(readOnly=true)
public class RentalQueryServiceImpl implements RentalQueryService {
    private final RentalRequestRepository requests;
    private final RentalRepository rentals;
    private final DeliveryRepository deliveries;
    private final EquipmentReturnRepository returns;
    public RentalQueryServiceImpl(RentalRequestRepository requests,RentalRepository rentals,
            DeliveryRepository deliveries,EquipmentReturnRepository returns) {
        this.requests=requests;this.rentals=rentals;this.deliveries=deliveries;this.returns=returns;
    }
    public List<RentalRequest> handle(GetRentalRequestsQuery q){return requests.findVisibleTo(q.actorUserId());}
    public List<Rental> handle(GetRentalsQuery q){return rentals.findVisibleTo(q.actorUserId());}
    public Optional<RentalRequest> handle(GetRentalRequestByIdQuery q){return requests.findById(q.id())
        .filter(r->r.getConstructionUserId().equals(q.actorUserId())||r.getRentalCompanyUserId().equals(q.actorUserId()));}
    public Optional<Rental> handle(GetRentalByIdQuery q){return rentals.findById(q.id())
        .filter(r->r.getConstructionUserId().equals(q.actorUserId())||r.getRentalCompanyUserId().equals(q.actorUserId()));}
    public List<Delivery> deliveriesFor(Long actorUserId){return deliveries.findByRentalIds(
        rentals.findVisibleTo(actorUserId).stream().map(Rental::getId).toList());}
    public List<EquipmentReturn> returnsFor(Long actorUserId){return returns.findByRentalIds(
        rentals.findVisibleTo(actorUserId).stream().map(Rental::getId).toList());}
}
