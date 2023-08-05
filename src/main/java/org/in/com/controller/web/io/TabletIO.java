package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TabletIO extends BaseIO {
	private UserIO user;
	private AppStoreDetailsIO storeDetails;
	private BusVehicleIO busVehicle;
	private String mobileNumber;
	private int mobileVerifyFlag;
	private String syncTime;
	private String model;
	private String version;
	private String remarks;
}
