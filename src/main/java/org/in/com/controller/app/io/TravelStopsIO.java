package org.in.com.controller.app.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class TravelStopsIO extends BaseIO {
	private String landmark;
	private String latitude;
	private String longitude;
	private String restRoom;
  	private String travelStopTime;
	private StationIO stations;
	private List<String> amenities;
	private int minutes;
	private String remarks;
}
