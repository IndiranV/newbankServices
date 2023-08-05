package org.in.com.controller.api_v3.io;

import lombok.Data;

@Data
public class BusVehicleIO {
	private String name;
	private String code;
	private String registrationDate;
	private String registationNumber;
	private String gpsDeviceCode;
}
