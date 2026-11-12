package com.maquigest.platform.maintenance.domain.repositories;
import com.maquigest.platform.maintenance.domain.model.aggregates.Incident;
import java.util.*;
public interface IncidentRepository {
  Optional<Incident> findById(Long id);
  Optional<Incident> findByIdForUpdate(Long id);
  List<Incident> findByOwner(Long owner);
  List<Incident> findByEquipmentId(Long equipmentId);
  boolean hasOpenBlocking(Long equipmentId);
  boolean hadBlocking(Long equipmentId);
  Incident save(Incident incident);
}
