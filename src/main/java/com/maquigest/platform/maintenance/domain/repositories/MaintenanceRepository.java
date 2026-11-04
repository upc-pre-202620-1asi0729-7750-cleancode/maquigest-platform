package com.maquigest.platform.maintenance.domain.repositories;
import com.maquigest.platform.maintenance.domain.model.aggregates.Maintenance;
import java.util.*;
public interface MaintenanceRepository {
  Optional<Maintenance> findById(Long id);
  Optional<Maintenance> findByIdForUpdate(Long id);
  List<Maintenance> findByOwner(Long owner);
  List<Maintenance> findByEquipmentId(Long equipmentId);
  Maintenance save(Maintenance maintenance);
}
