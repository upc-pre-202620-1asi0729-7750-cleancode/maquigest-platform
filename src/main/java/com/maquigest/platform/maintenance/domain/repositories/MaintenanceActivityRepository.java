package com.maquigest.platform.maintenance.domain.repositories;
import com.maquigest.platform.maintenance.domain.model.entities.MaintenanceActivity;
import java.util.*;
public interface MaintenanceActivityRepository {
  MaintenanceActivity save(MaintenanceActivity activity);
  List<MaintenanceActivity> findByMaintenanceId(Long maintenanceId);
}
