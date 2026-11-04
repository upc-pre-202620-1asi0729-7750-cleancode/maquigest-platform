package com.maquigest.platform.inventory.interfaces.rest;
import com.maquigest.platform.inventory.application.queryservices.EquipmentCategoryQueryService;
import com.maquigest.platform.inventory.domain.model.queries.GetAllEquipmentCategoriesQuery;
import com.maquigest.platform.inventory.interfaces.rest.transform.EquipmentCategoryResourceFromEntityAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;
/** Angular expects an array of category resources. */
@RestController
@RequestMapping(value="/api/v1/equipment-categories",produces=APPLICATION_JSON_VALUE)
@Tag(name="Equipment Categories",description="Inventory category catalogue")
public class EquipmentCategoriesController {
    private final EquipmentCategoryQueryService queries;
    public EquipmentCategoriesController(EquipmentCategoryQueryService queries) { this.queries=queries; }
    @GetMapping
    @Operation(summary="Get all equipment categories")
    public ResponseEntity<?> getCategories() {
        return ResponseEntity.ok(queries.handle(new GetAllEquipmentCategoriesQuery()).stream()
            .map(EquipmentCategoryResourceFromEntityAssembler::toResourceFromEntity).toList());
    }
}
