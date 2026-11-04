package com.maquigest.platform.rentals.interfaces.rest.resources;
import java.time.Instant;
public record DeliveryResource(Long id,Long rentalId,Instant deliveredAt,String notes) { }
