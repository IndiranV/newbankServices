package org.in.com.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TabletDTO extends BaseDTO<TabletDTO> {
	private BusVehicleDTO busVehicle;
	private UserDTO user;
	private String mobileNumber;
	private int mobileVerifyFlag;
	private String model;
	private String version;
	private String remarks;
}
