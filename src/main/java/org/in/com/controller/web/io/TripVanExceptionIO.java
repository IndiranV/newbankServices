package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripVanExceptionIO extends BaseIO {
	private String tripDate;
	private BusVehicleVanPickupIO vanPickup;
	private List<ScheduleIO> schedules;
	private AuditIO audit;
}
