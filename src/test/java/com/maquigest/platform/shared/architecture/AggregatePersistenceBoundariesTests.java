package com.maquigest.platform.shared.architecture;

import com.maquigest.platform.rentals.application.internal.commandservices.RentalLifecycleTransactionalExecutor;
import com.maquigest.platform.maintenance.application.internal.commandservices.MaintenanceTransactionalExecutor;
import com.maquigest.platform.rentals.domain.model.aggregates.Rental;
import com.maquigest.platform.rentals.domain.model.entities.Delivery;
import com.maquigest.platform.rentals.domain.model.entities.EquipmentReturn;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.DeliveryPersistenceEntity;
import com.maquigest.platform.rentals.infrastructure.persistence.jpa.entities.EquipmentReturnPersistenceEntity;
import com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity;
import com.maquigest.platform.shared.domain.model.aggregates.AbstractDomainAggregateRoot;
import jakarta.persistence.Column;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;
import static org.junit.jupiter.api.Assertions.*;

/** Reject accidental breaks to the documented aggregate and persistence boundary. */
class AggregatePersistenceBoundariesTests {
    @Test void rentalIsRootAndHandoverRecordsRemainChildValueRecords() {
        assertTrue(AbstractDomainAggregateRoot.class.isAssignableFrom(Rental.class));
        assertTrue(Delivery.class.isRecord());
        assertTrue(EquipmentReturn.class.isRecord());
        assertTrue(MaintenanceActivity.class.isRecord());
        assertFalse(AbstractDomainAggregateRoot.class.isAssignableFrom(Delivery.class));
        assertFalse(AbstractDomainAggregateRoot.class.isAssignableFrom(EquipmentReturn.class));
    }

    @Test void childHandoverTablesKeepOneToOneRentalUniqueness() throws Exception {
        Column delivery=DeliveryPersistenceEntity.class.getDeclaredField("rentalId").getAnnotation(Column.class);
        Column returned=EquipmentReturnPersistenceEntity.class.getDeclaredField("rentalId").getAnnotation(Column.class);
        assertNotNull(delivery);
        assertNotNull(returned);
        assertTrue(delivery.unique() && !delivery.nullable());
        assertTrue(returned.unique() && !returned.nullable());
    }

    @Test void lifecycleAndMaintenanceRootWritesRemainTransactional() throws Exception {
        Method delivery = RentalLifecycleTransactionalExecutor.class.getDeclaredMethod("handle",
            com.maquigest.platform.rentals.domain.model.commands.RegisterDeliveryCommand.class);
        Method returned = RentalLifecycleTransactionalExecutor.class.getDeclaredMethod("handle",
            com.maquigest.platform.rentals.domain.model.commands.RegisterReturnCommand.class);
        assertNotNull(delivery.getAnnotation(Transactional.class));
        assertNotNull(returned.getAnnotation(Transactional.class));
        assertNotNull(MaintenanceTransactionalExecutor.class.getAnnotation(Transactional.class));
    }
}
