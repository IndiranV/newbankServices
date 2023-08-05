package org.in.com.controller.api_v2.io;

import lombok.Data;

import org.in.com.controller.web.io.BusVehicleIO;

@Data
public class TripInfoIO {
	private String driverName;
	private String driverMobile;
	private String remarks;
	private String[] notificationStatus;
	private BusVehicleIO busVehicle;
	private String tripCloseDateTime;
}
