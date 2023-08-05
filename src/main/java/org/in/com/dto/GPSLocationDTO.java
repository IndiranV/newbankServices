package org.in.com.dto;

import org.in.com.exception.ErrorCode;

import lombok.Data;

@Data
public class GPSLocationDTO {

	private String latitude;
	private String longitude;
	private String updatedTime;
	private String registerNumber;
	private String address;
	private float speed;
	private boolean ignition;

	/** Address */
	private String road;
	private String area;
	private String landmark;
	private String city;
	private String state;
	private String postalCode;
	
	private ErrorCode error;
}
