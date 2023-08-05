package org.in.com.controller.app.io;

import lombok.Data;

@Data
public class OrganizationIO {
	private String code;
	private String name;
	private String address1;
	private String address2;
	private String contact;
	private StationIO station;
	private String pincode;
	private String latitude;
	private String longitude;
	private String shortCode;
}
