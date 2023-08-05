package org.in.com.dto;

import java.util.List;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class TripVanExceptionDTO extends BaseDTO<TripVanExceptionDTO> {
	private DateTime tripDate;
	private BusVehicleVanPickupDTO vanPickup;
	private List<ScheduleDTO> schedules;
	private AuditDTO audit;
}
