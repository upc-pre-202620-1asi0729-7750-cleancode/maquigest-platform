package com.maquigest.platform.inventory.application.queryservices;
import com.maquigest.platform.inventory.domain.model.entities.EquipmentCategory;
import com.maquigest.platform.inventory.domain.model.queries.GetAllEquipmentCategoriesQuery;
import java.util.List;
public interface EquipmentCategoryQueryService {
    List<EquipmentCategory> handle(GetAllEquipmentCategoriesQuery query);
}
