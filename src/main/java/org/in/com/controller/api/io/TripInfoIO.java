package org.in.com.controller.api.io;

import lombok.Data;

@Data
public class TripInfoIO {
	private String driverName;
	private String driverMobile;
	private String remarks;
	private String[] notificationStatus;
	private BusVehicleIO busVehicle;
}
