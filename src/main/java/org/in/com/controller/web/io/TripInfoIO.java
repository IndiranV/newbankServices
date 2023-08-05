package org.in.com.controller.web.io;

import lombok.Data;

import org.in.com.controller.web.io.BusVehicleIO;

@Data
public class TripInfoIO {
	private String driverName;
	private String driverMobile;
	private String remarks;
	private String[] notificationStatus;
	private BusVehicleIO busVehicle;
	private String tripStartDateTime;
	private String tripCloseDateTime;
	private String driverName2;
	private String driverMobile2;
	private String attenderName;
	private String attenderMobile;
	private String captainName;
	private String captainMobile;
	private VehicleDriverIO primaryDriver;
	private VehicleDriverIO secondaryDriver;
	private VehicleAttendantIO attendant;
	private VehicleAttendantIO captain;

	//Bus Buddy
	private String startOdometer;
	private String startDateTime;
	private String endOdometer;
	private String endDateTime;
}
