package com.maquigest.platform.subscriptions.domain.model.aggregates;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.PlanStatus;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;
class SubscriptionPlanTests {
    @Test void activeCatalogPlanIsEligible() {
        var plan = new SubscriptionPlan(1L,"Essential","Inventory",new BigDecimal("79.00"),"PEN","MONTHLY",PlanStatus.ACTIVE);
        assertTrue(plan.isActive());
        assertEquals("PEN",plan.getPriceCurrency());
    }
    @Test void invalidPriceCannotBecomeAPublishedPlan() {
        assertThrows(IllegalArgumentException.class, ()->
            new SubscriptionPlan(null,"Essential","Inventory",new BigDecimal("-1"),"PEN","MONTHLY",PlanStatus.ACTIVE));
    }
}
