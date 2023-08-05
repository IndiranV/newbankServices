package org.in.com.controller.app.io;

import java.util.List;

import lombok.Data;

@Data
public class StationIO {
	private String name;
	private String code;
	private String dateTime;
	private StateIO state;
	private StationPointIO stationPoints;
	private List<StationPointIO> stationPoint;
}
