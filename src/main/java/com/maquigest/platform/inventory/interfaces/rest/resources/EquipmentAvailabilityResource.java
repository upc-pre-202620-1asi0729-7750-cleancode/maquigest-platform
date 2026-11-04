package com.maquigest.platform.inventory.interfaces.rest.resources;
import java.time.LocalDate;
public record EquipmentAvailabilityResource(Long equipmentId,LocalDate startDate,LocalDate endDate,boolean available) { }
