package com.maquigest.platform.inventory.application.internal.commandservices;
import com.maquigest.platform.inventory.application.commandservices.EquipmentCategoryCommandService;
import com.maquigest.platform.inventory.domain.model.commands.SeedEquipmentCategoriesCommand;
import com.maquigest.platform.inventory.domain.model.entities.EquipmentCategory;
import com.maquigest.platform.inventory.domain.repositories.EquipmentCategoryRepository;
import com.maquigest.platform.shared.application.result.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EquipmentCategoryCommandServiceImpl implements EquipmentCategoryCommandService {
    private static final String[] DEFAULT_CATEGORIES = {"Excavators", "Loaders", "Compactors", "Generators"};
    private final EquipmentCategoryRepository repository;
    public EquipmentCategoryCommandServiceImpl(EquipmentCategoryRepository repository) { this.repository = repository; }
    @Override @Transactional
    public Result<Integer, ApplicationError> handle(SeedEquipmentCategoriesCommand command) {
        int created = 0;
        for (String name : DEFAULT_CATEGORIES) {
            if (!repository.existsByName(name)) {
                repository.save(new EquipmentCategory(null, name));
                created++;
            }
        }
        return Result.success(created);
    }
}
