package org.in.com.controller.web.io;

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
	private String lastAssignedDate;
	private BusIO bus;
	private BaseIO gpsDeviceVendor;
	private BaseIO vehicleType;
}
