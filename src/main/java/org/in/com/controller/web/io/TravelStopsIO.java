package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TravelStopsIO extends BaseIO {
	private String landmark;
	private String latitude;
	private String longitude;
	private int travelMinutes;
	private StationIO stations;
	private List<String> amenities;
	private String restRoom;
	private int minutes;
	private String remarks;
}
