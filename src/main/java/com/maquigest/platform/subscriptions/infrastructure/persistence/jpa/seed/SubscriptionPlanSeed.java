package com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.seed;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.PlanStatus;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.entities.SubscriptionPlanPersistenceEntity;
import com.maquigest.platform.subscriptions.infrastructure.persistence.jpa.repositories.SubscriptionPlanPersistenceRepository;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
/** Idempotent catalog seed based on the existing MaquiGest mock API. */
@Component
public class SubscriptionPlanSeed {
    private final SubscriptionPlanPersistenceRepository plans;
    public SubscriptionPlanSeed(SubscriptionPlanPersistenceRepository plans) { this.plans=plans; }
    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void onReady() {
        createIfAbsent("Essential", "Start organizing your equipment and reservation requests.", "79.00");
        createIfAbsent("Professional", "Centralize your company's day-to-day rental operations.", "149.00");
        createIfAbsent("Growth", "Expand your operational oversight and manage equipment maintenance.", "249.00");
    }
    private void createIfAbsent(String name, String description, String amount) {
        if (plans.findByNameIgnoreCase(name).isPresent()) return;
        var entity=new SubscriptionPlanPersistenceEntity();
        entity.setName(name);entity.setDescription(description);entity.setPriceAmount(new BigDecimal(amount));
        entity.setPriceCurrency("PEN");entity.setBillingCycle("MONTHLY");entity.setStatus(PlanStatus.ACTIVE);
        plans.save(entity);
    }
}
