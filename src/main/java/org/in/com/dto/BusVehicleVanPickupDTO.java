package org.in.com.dto;

import java.util.List;

import org.in.com.dto.enumeration.TripStatusEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusVehicleVanPickupDTO extends BaseDTO<BusVehicleVanPickupDTO> {
	private StationDTO station;
	private List<ScheduleDTO> schedules;
	private TripStatusEM tripStatus;
	private int seatCount;
}
