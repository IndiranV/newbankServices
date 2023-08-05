package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripChartIO extends BaseIO {

	private BusIO bus;
	private TripIO trip;
	private List<TripChartDetailsIO> ticketDetailsList;

	// trip chart
	private String driverName;
	private String remarks;
	private String driverMobile;
	private BusVehicleIO busVehicle;
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
	private String notificationBusContactType;
}
