package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StationIO extends BaseIO {
	private StateIO state;
	private String dateTime;
	private StationPointIO stationPoints;
	private List<StationPointIO> stationPoint;
	private int apiFlag;
	private String latitude;
	private String longitude;
	private int radius;
	private List<BaseIO> relatedStations;
}
