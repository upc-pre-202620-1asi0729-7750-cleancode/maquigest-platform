package com.maquigest.platform.shared.architecture;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import com.maquigest.platform.inventory.domain.model.valueobjects.EquipmentStatus;
import com.maquigest.platform.profiles.domain.model.valueobjects.CompanyAddress;
import com.maquigest.platform.maintenance.domain.model.valueobjects.MaintenanceStatus;
import com.maquigest.platform.maintenance.domain.model.valueobjects.IncidentStatus;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalPeriod;
import com.maquigest.platform.rentals.domain.model.valueobjects.RentalRequestStatus;
import static org.junit.jupiter.api.Assertions.*;
import com.maquigest.platform.inventory.domain.model.commands.CreateEquipmentCommand;
import com.maquigest.platform.inventory.domain.model.commands.UpdateEquipmentCommand;
import com.maquigest.platform.inventory.domain.model.commands.ReserveEquipmentPeriodCommand;
import com.maquigest.platform.inventory.domain.model.commands.ReleaseEquipmentAvailabilityBlockCommand;
import com.maquigest.platform.inventory.domain.model.commands.ChangeEquipmentOperationalStatusCommand;
import com.maquigest.platform.inventory.domain.model.queries.GetAllEquipmentQuery;
import com.maquigest.platform.inventory.domain.model.queries.GetEquipmentByIdQuery;
import com.maquigest.platform.profiles.domain.model.commands.CreateCompanyProfileCommand;
import com.maquigest.platform.profiles.domain.model.commands.UpdateCompanyProfileCommand;
import com.maquigest.platform.profiles.domain.model.queries.GetAllCompanyProfilesQuery;
import com.maquigest.platform.profiles.domain.model.queries.GetCompanyProfileByIdQuery;
import com.maquigest.platform.profiles.domain.model.queries.GetCompanyProfileByUserIdQuery;
import com.maquigest.platform.maintenance.domain.model.commands.RegisterMaintenanceCommand;
import com.maquigest.platform.maintenance.domain.model.commands.ReportIncidentCommand;
import com.maquigest.platform.maintenance.domain.model.commands.AdvanceMaintenanceCommand;
import com.maquigest.platform.maintenance.domain.model.commands.PatchIncidentCommand;
import com.maquigest.platform.maintenance.domain.model.commands.ReactivateEquipmentCommand;
import com.maquigest.platform.maintenance.domain.model.queries.GetEquipmentHistoryQuery;
import com.maquigest.platform.maintenance.domain.model.queries.GetIncidentByIdQuery;
import com.maquigest.platform.maintenance.domain.model.queries.GetMaintenanceActivitiesQuery;
import com.maquigest.platform.maintenance.domain.model.queries.GetMaintenanceByIdQuery;
import com.maquigest.platform.maintenance.domain.model.queries.GetMaintenancesQuery;
import com.maquigest.platform.maintenance.domain.model.queries.GetIncidentsQuery;
import com.maquigest.platform.rentals.domain.model.commands.RegisterDeliveryCommand;
import com.maquigest.platform.rentals.domain.model.commands.RegisterReturnCommand;
import com.maquigest.platform.rentals.domain.model.commands.RescheduleRentalRequestCommand;
import com.maquigest.platform.rentals.domain.model.commands.ResolveRentalRequestCommand;
import com.maquigest.platform.rentals.domain.model.queries.GetDeliveriesQuery;
import com.maquigest.platform.rentals.domain.model.queries.GetEquipmentReturnsQuery;
import com.maquigest.platform.rentals.domain.model.queries.GetRentalRequestsQuery;
import com.maquigest.platform.rentals.domain.model.queries.GetRentalsQuery;
import com.maquigest.platform.rentals.domain.model.queries.GetRentalByIdQuery;
import com.maquigest.platform.rentals.domain.model.queries.GetRentalRequestByIdQuery;
import com.maquigest.platform.subscriptions.domain.model.commands.CancelUserSubscriptionCommand;
import com.maquigest.platform.subscriptions.domain.model.commands.CreateSubscriptionCheckoutCommand;

class CqrsRecordValidationTests {
    private static final LocalDate DAY = LocalDate.of(2026, 11, 25);
    private static final Instant AT = Instant.parse("2026-11-25T12:00:00Z");
    private static final RentalPeriod PERIOD = new RentalPeriod(DAY, DAY.plusDays(1));
    @Test void validatesCreateEquipmentCommand() {
        assertDoesNotThrow(() -> new CreateEquipmentCommand(1L, "EQ-1", "Excavator", "Description", 2L, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new CreateEquipmentCommand(0L, "EQ-1", "Excavator", "Description", 2L, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new CreateEquipmentCommand(1L, " ", "Excavator", "Description", 2L, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new CreateEquipmentCommand(1L, "EQ-1", null, "Description", 2L, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new CreateEquipmentCommand(1L, "EQ-1", "Excavator", "", 2L, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new CreateEquipmentCommand(1L, "EQ-1", "Excavator", "Description", null, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new CreateEquipmentCommand(1L, "EQ-1", "Excavator", "Description", 2L, " ", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new CreateEquipmentCommand(1L, "EQ-1", "Excavator", "Description", 2L, "Lima", BigDecimal.ZERO, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new CreateEquipmentCommand(1L, "EQ-1", "Excavator", "Description", 2L, "Lima", BigDecimal.ONE, null));
    }
    @Test void validatesUpdateEquipmentCommand() {
        assertDoesNotThrow(() -> new UpdateEquipmentCommand(1L, "EQ-1", "Excavator", "Description", 2L, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new UpdateEquipmentCommand(0L, "EQ-1", "Excavator", "Description", 2L, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new UpdateEquipmentCommand(1L, " ", "Excavator", "Description", 2L, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new UpdateEquipmentCommand(1L, "EQ-1", null, "Description", 2L, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new UpdateEquipmentCommand(1L, "EQ-1", "Excavator", "", 2L, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new UpdateEquipmentCommand(1L, "EQ-1", "Excavator", "Description", null, "Lima", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new UpdateEquipmentCommand(1L, "EQ-1", "Excavator", "Description", 2L, " ", BigDecimal.ONE, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new UpdateEquipmentCommand(1L, "EQ-1", "Excavator", "Description", 2L, "Lima", BigDecimal.ZERO, BigDecimal.TEN));
        assertThrows(IllegalArgumentException.class, () -> new UpdateEquipmentCommand(1L, "EQ-1", "Excavator", "Description", 2L, "Lima", BigDecimal.ONE, null));
    }
    @Test void validatesReserveEquipmentPeriodCommand() {
        assertDoesNotThrow(() -> new ReserveEquipmentPeriodCommand(1L, DAY, DAY));
        assertThrows(IllegalArgumentException.class, () -> new ReserveEquipmentPeriodCommand(0L, DAY, DAY));
        assertThrows(IllegalArgumentException.class, () -> new ReserveEquipmentPeriodCommand(1L, null, DAY));
        assertThrows(IllegalArgumentException.class, () -> new ReserveEquipmentPeriodCommand(1L, DAY, null));
        assertThrows(IllegalArgumentException.class, () -> new ReserveEquipmentPeriodCommand(1L, DAY, DAY.minusDays(1)));
    }
    @Test void validatesReleaseEquipmentAvailabilityBlockCommand() {
        assertDoesNotThrow(() -> new ReleaseEquipmentAvailabilityBlockCommand(1L, 2L));
        assertThrows(IllegalArgumentException.class, () -> new ReleaseEquipmentAvailabilityBlockCommand(0L, 2L));
        assertThrows(IllegalArgumentException.class, () -> new ReleaseEquipmentAvailabilityBlockCommand(1L, null));
        assertThrows(IllegalArgumentException.class, () -> new ReleaseEquipmentAvailabilityBlockCommand(1L, -1L));
    }
    @Test void validatesChangeEquipmentOperationalStatusCommand() {
        assertDoesNotThrow(() -> new ChangeEquipmentOperationalStatusCommand(1L, EquipmentStatus.AVAILABLE));
        assertThrows(IllegalArgumentException.class, () -> new ChangeEquipmentOperationalStatusCommand(0L, EquipmentStatus.AVAILABLE));
        assertThrows(IllegalArgumentException.class, () -> new ChangeEquipmentOperationalStatusCommand(1L, null));
    }
    @Test void validatesGetAllEquipmentQuery() {
        assertDoesNotThrow(() -> new GetAllEquipmentQuery(null));
        assertThrows(IllegalArgumentException.class, () -> new GetAllEquipmentQuery(0L));
        assertThrows(IllegalArgumentException.class, () -> new GetAllEquipmentQuery(-1L));
    }
    @Test void validatesGetEquipmentByIdQuery() {
        assertDoesNotThrow(() -> new GetEquipmentByIdQuery(1L));
        assertThrows(IllegalArgumentException.class, () -> new GetEquipmentByIdQuery(null));
        assertThrows(IllegalArgumentException.class, () -> new GetEquipmentByIdQuery(0L));
        assertThrows(IllegalArgumentException.class, () -> new GetEquipmentByIdQuery(-1L));
    }
    @Test void validatesCreateCompanyProfileCommand() {
        assertDoesNotThrow(() -> new CreateCompanyProfileCommand(1L, "Ana", "Mora", "ana@example.com", null, "Company", CompanyAddress.empty()));
        assertThrows(IllegalArgumentException.class, () -> new CreateCompanyProfileCommand(0L, "Ana", "Mora", "ana@example.com", null, "Company", CompanyAddress.empty()));
        assertThrows(IllegalArgumentException.class, () -> new CreateCompanyProfileCommand(1L, " ", "Mora", "ana@example.com", null, "Company", CompanyAddress.empty()));
        assertThrows(IllegalArgumentException.class, () -> new CreateCompanyProfileCommand(1L, "Ana", "Mora", "invalid", null, "Company", CompanyAddress.empty()));
        assertThrows(IllegalArgumentException.class, () -> new CreateCompanyProfileCommand(1L, "Ana", "Mora", "ana@example.com", null, null, CompanyAddress.empty()));
        assertThrows(IllegalArgumentException.class, () -> new CreateCompanyProfileCommand(1L, "Ana", "Mora", "ana@example.com", null, "Company", null));
    }
    @Test void validatesUpdateCompanyProfileCommand() {
        assertDoesNotThrow(() -> new UpdateCompanyProfileCommand(1L, 1L, "Ana", "Mora", "ana@example.com", null, "Company", CompanyAddress.empty()));
        assertThrows(IllegalArgumentException.class, () -> new UpdateCompanyProfileCommand(0L, 1L, "Ana", "Mora", "ana@example.com", null, "Company", CompanyAddress.empty()));
        assertThrows(IllegalArgumentException.class, () -> new UpdateCompanyProfileCommand(1L, 1L, " ", "Mora", "ana@example.com", null, "Company", CompanyAddress.empty()));
        assertThrows(IllegalArgumentException.class, () -> new UpdateCompanyProfileCommand(1L, 1L, "Ana", "Mora", "invalid", null, "Company", CompanyAddress.empty()));
        assertThrows(IllegalArgumentException.class, () -> new UpdateCompanyProfileCommand(1L, 1L, "Ana", "Mora", "ana@example.com", null, null, CompanyAddress.empty()));
        assertThrows(IllegalArgumentException.class, () -> new UpdateCompanyProfileCommand(1L, 1L, "Ana", "Mora", "ana@example.com", null, "Company", null));
    }
    @Test void validatesGetAllCompanyProfilesQuery() {
        assertDoesNotThrow(() -> new GetAllCompanyProfilesQuery(null));
        assertThrows(IllegalArgumentException.class, () -> new GetAllCompanyProfilesQuery(0L));
        assertThrows(IllegalArgumentException.class, () -> new GetAllCompanyProfilesQuery(-1L));
    }
    @Test void validatesGetCompanyProfileByIdQuery() {
        assertDoesNotThrow(() -> new GetCompanyProfileByIdQuery(1L));
        assertThrows(IllegalArgumentException.class, () -> new GetCompanyProfileByIdQuery(null));
        assertThrows(IllegalArgumentException.class, () -> new GetCompanyProfileByIdQuery(0L));
        assertThrows(IllegalArgumentException.class, () -> new GetCompanyProfileByIdQuery(-1L));
    }
    @Test void validatesGetCompanyProfileByUserIdQuery() {
        assertDoesNotThrow(() -> new GetCompanyProfileByUserIdQuery(1L));
        assertThrows(IllegalArgumentException.class, () -> new GetCompanyProfileByUserIdQuery(null));
        assertThrows(IllegalArgumentException.class, () -> new GetCompanyProfileByUserIdQuery(0L));
        assertThrows(IllegalArgumentException.class, () -> new GetCompanyProfileByUserIdQuery(-1L));
    }
    @Test void validatesRegisterMaintenanceCommand() {
        assertDoesNotThrow(() -> new RegisterMaintenanceCommand(1L, AT, "Preventive", MaintenanceStatus.SCHEDULED));
        assertThrows(IllegalArgumentException.class, () -> new RegisterMaintenanceCommand(0L, AT, "Preventive", MaintenanceStatus.SCHEDULED));
        assertThrows(IllegalArgumentException.class, () -> new RegisterMaintenanceCommand(1L, null, "Preventive", MaintenanceStatus.SCHEDULED));
        assertThrows(IllegalArgumentException.class, () -> new RegisterMaintenanceCommand(1L, AT, " ", MaintenanceStatus.SCHEDULED));
        assertThrows(IllegalArgumentException.class, () -> new RegisterMaintenanceCommand(1L, AT, "Preventive", null));
    }
    @Test void validatesReportIncidentCommand() {
        assertDoesNotThrow(() -> new ReportIncidentCommand(1L, "Leak", false));
        assertThrows(IllegalArgumentException.class, () -> new ReportIncidentCommand(null, "Leak", false));
        assertThrows(IllegalArgumentException.class, () -> new ReportIncidentCommand(1L, " ", false));
    }
    @Test void validatesAdvanceMaintenanceCommand() {
        assertDoesNotThrow(() -> new AdvanceMaintenanceCommand(1L, null, null, true, null));
        assertThrows(IllegalArgumentException.class, () -> new AdvanceMaintenanceCommand(0L, null, null, true, null));
    }
    @Test void validatesPatchIncidentCommand() {
        assertDoesNotThrow(() -> new PatchIncidentCommand(1L, null, IncidentStatus.RESOLVED, null));
        assertThrows(IllegalArgumentException.class, () -> new PatchIncidentCommand(0L, null, IncidentStatus.RESOLVED, null));
    }
    @Test void validatesReactivateEquipmentCommand() {
        assertDoesNotThrow(() -> new ReactivateEquipmentCommand(1L, false));
        assertThrows(IllegalArgumentException.class, () -> new ReactivateEquipmentCommand(null, true));
        assertThrows(IllegalArgumentException.class, () -> new ReactivateEquipmentCommand(0L, false));
    }
    @Test void validatesGetEquipmentHistoryQuery() {
        assertDoesNotThrow(() -> new GetEquipmentHistoryQuery(1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetEquipmentHistoryQuery(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetEquipmentHistoryQuery(0L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetEquipmentHistoryQuery(-1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetEquipmentHistoryQuery(1L, null));
        assertThrows(IllegalArgumentException.class, () -> new GetEquipmentHistoryQuery(1L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new GetEquipmentHistoryQuery(1L, -1L));
    }
    @Test void validatesGetIncidentByIdQuery() {
        assertDoesNotThrow(() -> new GetIncidentByIdQuery(1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetIncidentByIdQuery(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetIncidentByIdQuery(0L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetIncidentByIdQuery(-1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetIncidentByIdQuery(1L, null));
        assertThrows(IllegalArgumentException.class, () -> new GetIncidentByIdQuery(1L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new GetIncidentByIdQuery(1L, -1L));
    }
    @Test void validatesGetMaintenanceActivitiesQuery() {
        assertDoesNotThrow(() -> new GetMaintenanceActivitiesQuery(1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenanceActivitiesQuery(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenanceActivitiesQuery(0L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenanceActivitiesQuery(-1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenanceActivitiesQuery(1L, null));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenanceActivitiesQuery(1L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenanceActivitiesQuery(1L, -1L));
    }
    @Test void validatesGetMaintenanceByIdQuery() {
        assertDoesNotThrow(() -> new GetMaintenanceByIdQuery(1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenanceByIdQuery(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenanceByIdQuery(0L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenanceByIdQuery(-1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenanceByIdQuery(1L, null));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenanceByIdQuery(1L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenanceByIdQuery(1L, -1L));
    }
    @Test void validatesGetMaintenancesQuery() {
        assertDoesNotThrow(() -> new GetMaintenancesQuery(1L, null));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenancesQuery(null, null));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenancesQuery(0L, null));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenancesQuery(-1L, null));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenancesQuery(1L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new GetMaintenancesQuery(1L, -1L));
    }
    @Test void validatesGetIncidentsQuery() {
        assertDoesNotThrow(() -> new GetIncidentsQuery(1L, null, true));
        assertThrows(IllegalArgumentException.class, () -> new GetIncidentsQuery(0L, null, true));
        assertThrows(IllegalArgumentException.class, () -> new GetIncidentsQuery(1L, 0L, true));
    }
    @Test void validatesRegisterDeliveryCommand() {
        assertDoesNotThrow(() -> new RegisterDeliveryCommand(1L, 2L, AT, null));
        assertThrows(IllegalArgumentException.class, () -> new RegisterDeliveryCommand(0L, 2L, AT, null));
        assertThrows(IllegalArgumentException.class, () -> new RegisterDeliveryCommand(1L, null, AT, null));
        assertThrows(IllegalArgumentException.class, () -> new RegisterDeliveryCommand(1L, 2L, null, null));
    }
    @Test void validatesRegisterReturnCommand() {
        assertDoesNotThrow(() -> new RegisterReturnCommand(1L, 2L, AT, null, false));
        assertThrows(IllegalArgumentException.class, () -> new RegisterReturnCommand(0L, 2L, AT, null, false));
        assertThrows(IllegalArgumentException.class, () -> new RegisterReturnCommand(1L, null, AT, null, false));
        assertThrows(IllegalArgumentException.class, () -> new RegisterReturnCommand(1L, 2L, null, null, false));
    }
    @Test void validatesRescheduleRentalRequestCommand() {
        assertDoesNotThrow(() -> new RescheduleRentalRequestCommand(1L, 2L, PERIOD));
        assertThrows(IllegalArgumentException.class, () -> new RescheduleRentalRequestCommand(0L, 2L, PERIOD));
        assertThrows(IllegalArgumentException.class, () -> new RescheduleRentalRequestCommand(1L, 0L, PERIOD));
        assertThrows(IllegalArgumentException.class, () -> new RescheduleRentalRequestCommand(1L, 2L, null));
    }
    @Test void validatesResolveRentalRequestCommand() {
        assertDoesNotThrow(() -> new ResolveRentalRequestCommand(1L, 2L, RentalRequestStatus.APPROVED));
        assertThrows(IllegalArgumentException.class, () -> new ResolveRentalRequestCommand(0L, 2L, RentalRequestStatus.APPROVED));
        assertThrows(IllegalArgumentException.class, () -> new ResolveRentalRequestCommand(1L, null, RentalRequestStatus.APPROVED));
        assertThrows(IllegalArgumentException.class, () -> new ResolveRentalRequestCommand(1L, 2L, null));
    }
    @Test void validatesGetDeliveriesQuery() {
        assertDoesNotThrow(() -> new GetDeliveriesQuery(1L));
        assertThrows(IllegalArgumentException.class, () -> new GetDeliveriesQuery(null));
        assertThrows(IllegalArgumentException.class, () -> new GetDeliveriesQuery(0L));
        assertThrows(IllegalArgumentException.class, () -> new GetDeliveriesQuery(-1L));
    }
    @Test void validatesGetEquipmentReturnsQuery() {
        assertDoesNotThrow(() -> new GetEquipmentReturnsQuery(1L));
        assertThrows(IllegalArgumentException.class, () -> new GetEquipmentReturnsQuery(null));
        assertThrows(IllegalArgumentException.class, () -> new GetEquipmentReturnsQuery(0L));
        assertThrows(IllegalArgumentException.class, () -> new GetEquipmentReturnsQuery(-1L));
    }
    @Test void validatesGetRentalRequestsQuery() {
        assertDoesNotThrow(() -> new GetRentalRequestsQuery(1L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalRequestsQuery(null));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalRequestsQuery(0L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalRequestsQuery(-1L));
    }
    @Test void validatesGetRentalsQuery() {
        assertDoesNotThrow(() -> new GetRentalsQuery(1L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalsQuery(null));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalsQuery(0L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalsQuery(-1L));
    }
    @Test void validatesGetRentalByIdQuery() {
        assertDoesNotThrow(() -> new GetRentalByIdQuery(1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalByIdQuery(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalByIdQuery(0L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalByIdQuery(-1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalByIdQuery(1L, null));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalByIdQuery(1L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalByIdQuery(1L, -1L));
    }
    @Test void validatesGetRentalRequestByIdQuery() {
        assertDoesNotThrow(() -> new GetRentalRequestByIdQuery(1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalRequestByIdQuery(null, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalRequestByIdQuery(0L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalRequestByIdQuery(-1L, 1L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalRequestByIdQuery(1L, null));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalRequestByIdQuery(1L, 0L));
        assertThrows(IllegalArgumentException.class, () -> new GetRentalRequestByIdQuery(1L, -1L));
    }
    @Test void validatesCancelUserSubscriptionCommand() {
        assertDoesNotThrow(() -> new CancelUserSubscriptionCommand(1L, 2L));
        assertThrows(IllegalArgumentException.class, () -> new CancelUserSubscriptionCommand(0L, 2L));
        assertThrows(IllegalArgumentException.class, () -> new CancelUserSubscriptionCommand(1L, null));
    }
    @Test void validatesCreateSubscriptionCheckoutCommand() {
        assertDoesNotThrow(() -> new CreateSubscriptionCheckoutCommand(1L, 2L));
        assertThrows(IllegalArgumentException.class, () -> new CreateSubscriptionCheckoutCommand(0L, 2L));
        assertThrows(IllegalArgumentException.class, () -> new CreateSubscriptionCheckoutCommand(1L, null));
    }
}
