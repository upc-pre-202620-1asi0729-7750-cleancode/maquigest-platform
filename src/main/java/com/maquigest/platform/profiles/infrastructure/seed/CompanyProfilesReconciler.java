package com.maquigest.platform.profiles.infrastructure.seed;
import com.maquigest.platform.profiles.application.commandservices.CompanyProfileCommandService;
import org.slf4j.Logger;import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
/** Existing IAM users get profiles once. Periodic reconciliation covers future signups without IAM code changes.
 * Existing profiles are never replaced; in a future distributed deployment replace polling with IAM events. */
@Component @EnableScheduling
public class CompanyProfilesReconciler {
    private static final Logger log=LoggerFactory.getLogger(CompanyProfilesReconciler.class);
    private final CompanyProfileCommandService commands;
    public CompanyProfilesReconciler(CompanyProfileCommandService commands){this.commands=commands;}
    @EventListener(ApplicationReadyEvent.class)
    public void onReady(){reconcile();}
    @Scheduled(fixedDelayString="${maquigest.profiles.reconcile-interval-ms:60000}",initialDelayString="${maquigest.profiles.reconcile-initial-delay-ms:60000}")
    public void reconcile(){
        int created=commands.provisionMissingFromIam();
        if(created>0)log.info("Profiles provisioned for {} existing IAM company accounts",created);
    }
}
