package com.maquigest.platform.inventory.domain.repositories;
import com.maquigest.platform.inventory.domain.model.aggregates.Equipment;
import java.util.List;
import java.util.Optional;
public interface EquipmentRepository {
    Optional<Equipment> findById(Long id);
    List<Equipment> findAll();
    List<Equipment> findByUserId(Long userId);
    Equipment save(Equipment equipment);
    boolean existsByUserIdAndCodeIgnoreCase(Long userId, String code);
    boolean existsByUserIdAndCodeIgnoreCaseAndIdNot(Long userId, String code, Long id);
    void deleteById(Long id);
}
