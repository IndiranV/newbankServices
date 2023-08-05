package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusVehicleVanPickupIO extends BaseIO {
	private StationIO station;
	private TripStatusIO tripStatus;
	private TripVanInfoIO tripVanInfo;
	private List<ScheduleIO> schedules;
	private int seatCount;
}
