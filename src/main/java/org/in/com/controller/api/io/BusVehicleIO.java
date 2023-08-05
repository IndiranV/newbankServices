package org.in.com.controller.api.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusVehicleIO extends BaseIO {
	private String registrationDate;
	private String registationNumber;
	private String licNumber;
	private String gpsDeviceCode;
	private String mobileNumber;
	private BusIO bus;
	private BaseIO vehicleType;
}
