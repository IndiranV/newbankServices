package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserDetailsIO extends BaseIO {
	private String address1;
	private String address2;
	private String landmark;
	private String pincode;
	private UserIO user;
	private StationIO station;
	private StateIO state;
	private StationAreaIO stationArea;
}
