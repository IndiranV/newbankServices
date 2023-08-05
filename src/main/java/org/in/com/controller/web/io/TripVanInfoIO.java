package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripVanInfoIO extends BaseIO {
	private String mobileNumber;
	private String tripDate;
	private VehicleDriverIO driver;
	private BusVehicleIO vehicle;
	private BusVehicleVanPickupIO vanPickup;
	private BaseIO notificationStatus;
	private TripVanExceptionIO tripVanException;
}
