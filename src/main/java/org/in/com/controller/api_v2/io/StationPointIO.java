package org.in.com.controller.api_v2.io;

import java.util.List;

import lombok.Data;

@Data
public class StationPointIO {
	private String code;
	private String name;
	private String latitude;
	private String longitude;
	private String address;
	private String landmark;
	private String number;
	private String dateTime;
	private List<String> seatList;
}
