package com.maquigest.platform.inventory.application.internal.queryservices;
import com.maquigest.platform.inventory.application.queryservices.EquipmentQueryService;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.queries.*;
import com.maquigest.platform.inventory.domain.repositories.EquipmentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
@Service
@Transactional(readOnly = true)
public class EquipmentQueryServiceImpl implements EquipmentQueryService {
    private final EquipmentRepository repository;
    public EquipmentQueryServiceImpl(EquipmentRepository repository) { this.repository = repository; }
    @Override public Optional<Equipment> handle(GetEquipmentByIdQuery query) { return repository.findById(query.equipmentId()); }
    @Override public List<Equipment> handle(GetAllEquipmentQuery query) {
        return query.userId() == null ? repository.findAll() : repository.findByUserId(query.userId());
    }
    @Override public Optional<Boolean> handle(CheckEquipmentAvailabilityQuery query) {
        return repository.findById(query.equipmentId())
            .map(equipment -> equipment.isAvailableFor(query.startDate(), query.endDate()));
    }
}
