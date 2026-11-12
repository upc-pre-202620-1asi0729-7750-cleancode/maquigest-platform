package com.maquigest.platform.rentals.interfaces.rest.resources;
import java.time.Instant;
public record EquipmentReturnResource(Long id,Long rentalId,Instant returnedAt,String notes,boolean maintenanceRequired) { }
