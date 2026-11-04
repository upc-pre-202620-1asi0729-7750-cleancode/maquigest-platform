package com.maquigest.platform.maintenance.interfaces.rest.transform;
import com.maquigest.platform.maintenance.application.exceptions.MaintenanceException;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MaintenanceResourceAssemblerTests {
 @Test void validatesISODate(){assertEquals(2026,MaintenanceResourceAssembler.parse("2026-11-04T12:00:00Z","performedAt").atZone(java.time.ZoneOffset.UTC).getYear());
   assertEquals(400,assertThrows(MaintenanceException.class,()->MaintenanceResourceAssembler.parse("not-a-date","performedAt")).status().value());}
}
