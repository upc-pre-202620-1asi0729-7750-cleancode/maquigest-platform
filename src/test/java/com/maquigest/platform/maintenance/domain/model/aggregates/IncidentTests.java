package com.maquigest.platform.maintenance.domain.model.aggregates;
import com.maquigest.platform.maintenance.domain.model.aggregates.Incident;
import com.maquigest.platform.maintenance.domain.model.valueobjects.IncidentStatus;
import java.time.*;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
class IncidentTests {
 @Test void reportingAndResolvingPreserveAuditAndRestrictions(){
   var incident=Incident.report(3L,1L,"Hydraulic leak",true,Instant.now());
   assertTrue(incident.isBlocksRental());assertEquals(IncidentStatus.OPEN,incident.getStatus());
   incident.resolve(Instant.now().plusMillis(1));
   assertEquals(IncidentStatus.RESOLVED,incident.getStatus());assertNotNull(incident.getResolvedAt());
   assertThrows(IllegalStateException.class,()->incident.resolve(Instant.now()));
 }
 @Test void restrictionsCannotBeDuplicatedOrAppliedToResolvedIncident(){
   var incident=Incident.report(3L,1L,"Damage",false,Instant.now());
   incident.requireRestriction();assertTrue(incident.isBlocksRental());
   assertThrows(IllegalStateException.class,incident::requireRestriction);
   incident.resolve(Instant.now());assertThrows(IllegalStateException.class,incident::requireRestriction);
 }
}
