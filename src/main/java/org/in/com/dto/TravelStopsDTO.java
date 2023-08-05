package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TravelStopsDTO extends BaseDTO<TravelStopsDTO> {

	private String landmark;
	private String latitude;
	private String longitude;
	private int travelMinutes;
	private DateTime travelStopTime;
	private StationDTO station;
	private List<String> amenities;
	private int minutes;
 	private String restRoom;
 	private String remarks;
}
