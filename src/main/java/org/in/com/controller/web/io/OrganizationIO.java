package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrganizationIO extends BaseIO {
	private String address1;
	private String address2;
	private String contact;
	private StationIO station;
	private int userCount;
	private String pincode;
	private String latitude;
	private String longitude;
	private String shortCode;
	private int workingMinutes;
}
