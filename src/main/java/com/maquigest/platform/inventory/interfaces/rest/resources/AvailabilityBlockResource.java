package com.maquigest.platform.inventory.interfaces.rest.resources;
/** ISO-8601 offsets are used because Angular parses the dates with new Date(). */
public record AvailabilityBlockResource(Long id, String startDate, String endDate) { }
