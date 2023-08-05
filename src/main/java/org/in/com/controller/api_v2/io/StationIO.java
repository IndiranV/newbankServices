package org.in.com.controller.api_v2.io;

import java.util.List;

import lombok.Data;

@Data
public class StationIO {
	private String name;
	private String code;
	private String dateTime;
	private StationPointIO stationPoints;
	private List<StationPointIO> stationPoint;
}
