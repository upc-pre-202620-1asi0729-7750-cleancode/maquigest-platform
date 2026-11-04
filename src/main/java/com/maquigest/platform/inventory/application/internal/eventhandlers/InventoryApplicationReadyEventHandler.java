package com.maquigest.platform.inventory.application.internal.eventhandlers;
import com.maquigest.platform.inventory.application.commandservices.EquipmentCategoryCommandService;
import com.maquigest.platform.inventory.domain.model.commands.SeedEquipmentCategoriesCommand;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
/** Idempotent catalogue seeding, same bootstrap pattern as IAM Roles. */
@Service
public class InventoryApplicationReadyEventHandler {
    private final EquipmentCategoryCommandService service;
    public InventoryApplicationReadyEventHandler(EquipmentCategoryCommandService service) { this.service = service; }
    @EventListener
    public void on(ApplicationReadyEvent event) { service.handle(new SeedEquipmentCategoriesCommand()); }
}
