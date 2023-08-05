package org.in.com.controller.api.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StationIO extends BaseIO {
	private String dateTime;
	private BaseIO state;
	private StationPointIO stationPoints;
	private List<StationPointIO> stationPoint;
}
