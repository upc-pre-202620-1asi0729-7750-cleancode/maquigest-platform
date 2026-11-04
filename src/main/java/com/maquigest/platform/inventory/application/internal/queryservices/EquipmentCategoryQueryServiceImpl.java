package com.maquigest.platform.inventory.application.internal.queryservices;
import com.maquigest.platform.inventory.application.queryservices.EquipmentCategoryQueryService;
import com.maquigest.platform.inventory.domain.model.entities.EquipmentCategory;
import com.maquigest.platform.inventory.domain.model.queries.GetAllEquipmentCategoriesQuery;
import com.maquigest.platform.inventory.domain.repositories.EquipmentCategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
@Service
@Transactional(readOnly = true)
public class EquipmentCategoryQueryServiceImpl implements EquipmentCategoryQueryService {
    private final EquipmentCategoryRepository repository;
    public EquipmentCategoryQueryServiceImpl(EquipmentCategoryRepository repository) { this.repository = repository; }
    @Override public List<EquipmentCategory> handle(GetAllEquipmentCategoriesQuery query) { return repository.findAll(); }
}
