package com.maquigest.platform.maintenance.domain.model.valueobjects;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class MaintenanceTypeTests {
 @Test void normalizesValidType(){assertEquals("Oil change",new MaintenanceType("  Oil change  ").value());}
 @Test void rejectsEmptyAndOversizedTypes(){assertThrows(IllegalArgumentException.class,()->new MaintenanceType(" "));
   assertThrows(IllegalArgumentException.class,()->new MaintenanceType("x".repeat(121)));}
}
