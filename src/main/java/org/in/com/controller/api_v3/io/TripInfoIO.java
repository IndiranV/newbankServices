package org.in.com.controller.api_v3.io;

import lombok.Data;

@Data
public class TripInfoIO {
	private String driverName;
	private String driverMobile;
	private BusVehicleIO busVehicle;
	private String tripCloseDateTime;
}
