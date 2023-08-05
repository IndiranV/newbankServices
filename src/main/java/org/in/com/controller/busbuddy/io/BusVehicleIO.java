package org.in.com.controller.busbuddy.io;

import lombok.Data;

@Data
public class BusVehicleIO {
	private String code;
	private String name;
	private String registrationDate;
	private String registationNumber;
	private String licNumber;
	private String gpsDeviceCode;
	private String mobileNumber;
	private BusIO bus;
	private BaseIO gpsDeviceVendor;
	private BaseIO vehicleType;
}
