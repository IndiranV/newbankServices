package org.in.com.controller.busbuddy.io;

import java.util.List;

import lombok.Data;

@Data
public class StationPointIO {
	private String code;
	private String name;
	private String stageName;
	private String landmark;
	private String dateTime;
	private List<TripChartDetailsIO> seats;
	private String latitude;
	private String longitude;
}
