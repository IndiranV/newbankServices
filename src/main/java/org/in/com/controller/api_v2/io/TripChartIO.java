package org.in.com.controller.api_v2.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusVehicleIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripChartIO extends BaseIO {

	private BusIO bus;
	private TripIO trip;
	private List<TripChartDetailsIO> ticketDetailsList;

	// tripchart
	private String driverName;
	private String remarks;
	private String driverPhoneNumber;
	private BusVehicleIO busVehicle;
}
