package com.maquigest.platform.shared.architecture;

import com.maquigest.platform.iam.domain.model.aggregates.User;
import com.maquigest.platform.iam.domain.model.entities.Role;
import com.maquigest.platform.iam.domain.model.valueobjects.EmailAddress;
import com.maquigest.platform.iam.domain.model.valueobjects.Roles;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import com.maquigest.platform.inventory.domain.model.commands.UpdateEquipmentCommand;
import com.maquigest.platform.inventory.domain.model.valueobjects.*;
import com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance;
import com.maquigest.platform.maintenance.domain.model.valueobjects.*;
import com.maquigest.platform.profiles.domain.model.aggregates.CompanyProfile;
import com.maquigest.platform.profiles.domain.model.commands.*;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
import com.maquigest.platform.profiles.domain.model.valueobjects.PhoneNumber;
import com.maquigest.platform.subscriptions.domain.model.aggregates.SubscriptionPlan;
import com.maquigest.platform.subscriptions.domain.model.valueobjects.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class DomainValueObjectsTests {
    @Test void emailPreservesIamNormalizationAndValidation() {
        assertEquals(new EmailAddress("ada@example.com"), new EmailAddress(" ADA@Example.COM "));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(null));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress(" "));
        assertThrows(IllegalArgumentException.class, () -> new EmailAddress("invalid"));
    }

    @Test void equipmentCodePreservesCaseAndLength() {
        assertEquals(new EquipmentCode("Exc-01"), new EquipmentCode(" Exc-01 "));
        assertEquals(60, new EquipmentCode("A".repeat(60)).value().length());
        assertThrows(IllegalArgumentException.class, () -> new EquipmentCode("A".repeat(61)));
        assertThrows(IllegalArgumentException.class, () -> new EquipmentCode(" "));
        assertThrows(IllegalArgumentException.class, () -> new EquipmentCode(null));
    }

    @Test void phoneRemainsOptionalAndSupportsExistingFormats() {
        assertEquals(new PhoneNumber(""), new PhoneNumber(null));
        assertEquals(new PhoneNumber("+51 999-123-456"), new PhoneNumber(" +51 999-123-456 "));
        assertEquals(40, new PhoneNumber("9".repeat(40)).value().length());
        assertThrows(IllegalArgumentException.class, () -> new PhoneNumber("9".repeat(41)));
    }

    @Test void technicianRemainsOptionalAndBounded() {
        assertEquals(new TechnicianName(""), new TechnicianName(null));
        assertEquals(new TechnicianName("Ana"), new TechnicianName(" Ana "));
        assertEquals(180, new TechnicianName("A".repeat(180)).value().length());
        assertThrows(IllegalArgumentException.class, () -> new TechnicianName("A".repeat(181)));
    }

    @Test void billingCyclePreservesPreviouslyAcceptedValues() {
        assertEquals(new BillingCycle("monthly"), new BillingCycle(" monthly "));
        assertEquals("YEARLY", new BillingCycle("YEARLY").value());
        assertThrows(IllegalArgumentException.class, () -> new BillingCycle(null));
        assertThrows(IllegalArgumentException.class, () -> new BillingCycle(" "));
    }

    @Test void maintenanceTypeKeepsExistingValidation() {
        assertEquals(new MaintenanceType("Inspection"), new MaintenanceType(" Inspection "));
        assertThrows(IllegalArgumentException.class, () -> new MaintenanceType(" "));
        assertThrows(IllegalArgumentException.class, () -> new MaintenanceType("A".repeat(121)));
    }

    @Test void aggregatesRetainValueObjectsInternally() throws NoSuchFieldException {
        assertEquals(EmailAddress.class, User.class.getDeclaredField("email").getType());
        assertEquals(EquipmentCode.class, Equipment.class.getDeclaredField("code").getType());
        assertEquals(PhoneNumber.class, CompanyProfile.class.getDeclaredField("phoneNumber").getType());
        assertEquals(MaintenanceType.class, Maintenance.class.getDeclaredField("type").getType());
        assertEquals(TechnicianName.class, Maintenance.class.getDeclaredField("technician").getType());
        assertEquals(BillingCycle.class, SubscriptionPlan.class.getDeclaredField("billingCycle").getType());
    }

    @Test void userKeepsStringEmailContract() {
        var user = new User("Ada", "Mora", " ADA@Example.COM ", "hash", "Obras", new Role(Roles.ROLE_USER));
        assertEquals("ada@example.com", user.getEmail());
    }

    @Test void equipmentKeepsStringCodeThroughReconstitutionAndUpdate() {
        var equipment = Equipment.reconstitute(5L, 1L, " X ", "Machine", "Description", 1L, "Lima",
            new RentalRate(BigDecimal.ONE, BigDecimal.TEN), EquipmentStatus.AVAILABLE, List.of());
        assertEquals("X", equipment.getCode());
        equipment.updateEditableFields(new UpdateEquipmentCommand(5L, " Y ", "Machine", "Description", 1L,
            "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertEquals("Y", equipment.getCode());
    }

    @Test void profileKeepsOptionalPhoneContractOnUpdate() {
        var profile = CompanyProfile.reconstitute(5L, new CreateCompanyProfileCommand(7L, "Ana", "Mora",
            "ana@example.com", null, "Obras", CompanyAddress.empty()));
        assertEquals("", profile.getPhoneNumber());
        profile.update(new UpdateCompanyProfileCommand(5L, 7L, "Ana", "Mora", "ana@example.com",
            " +51 999 ", "Obras", CompanyAddress.empty()));
        assertEquals("+51 999", profile.getPhoneNumber());
    }

    @Test void maintenanceKeepsTechnicianAndTypeContracts() {
        var now = Instant.now();
        var maintenance = new Maintenance(1L, 2L, 3L, now, " Inspection ", MaintenanceStatus.SCHEDULED, null, null);
        assertEquals("Inspection", maintenance.getType());
        assertEquals("", maintenance.getTechnician());
        assertThrows(IllegalStateException.class, () -> maintenance.acknowledge(now));
        maintenance.assign(" Ana ");
        maintenance.acknowledge(now);
        assertEquals("Ana", maintenance.getTechnician());
        maintenance.changeStatus(MaintenanceStatus.IN_PROGRESS, null, "", now);
        assertEquals("Ana", maintenance.getTechnician());
    }

    @Test void planKeepsStringBillingCycleContract() {
        var plan = new SubscriptionPlan(1L, "Essential", "Inventory", BigDecimal.ONE, "PEN", " monthly ", PlanStatus.ACTIVE);
        assertEquals("monthly", plan.getBillingCycle());
    }
}
