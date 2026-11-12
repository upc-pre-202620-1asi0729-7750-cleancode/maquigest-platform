package com.maquigest.platform.iam.application.internal.eventhandlers;

import com.maquigest.platform.iam.application.commandservices.RoleCommandService;
import com.maquigest.platform.iam.domain.model.commands.SeedRolesCommand;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;

/**
 * Application lifecycle handler that ensures IAM roles are seeded when the application is ready.
 */
@Service
@Slf4j
public class ApplicationReadyEventHandler {
    private final RoleCommandService roleCommandService;

    public ApplicationReadyEventHandler(RoleCommandService roleCommandService) {
        this.roleCommandService = roleCommandService;
    }

    @EventListener
    public void on(ApplicationReadyEvent event) {
        var applicationName = event.getApplicationContext().getId();
        log.info("Starting to verify if roles seeding is needed for {} at {}", applicationName, currentTimestamp());
        var seedRolesCommand = new SeedRolesCommand();
        roleCommandService.handle(seedRolesCommand);
        log.info("Roles seeding verification finished for {} at {}", applicationName, currentTimestamp());
    }

    private Timestamp currentTimestamp() {
        return new Timestamp(System.currentTimeMillis());
    }
}
