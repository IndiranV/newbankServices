package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleStationPointIO extends BaseIO {
	private ScheduleIO schedule;
	private StationIO station;
	private StationPointIO stationPoint;
	private int minitues;
	private String creditDebitFlag;// CR/DR
	private String activeFrom;
	private String activeTo;
	private List<String> tripDates;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleStationPointIO> overrideList;
	private int boardingFlag;
	private int droppingFlag;
	private List<StationPointIO> stationPointList;
	private List<ScheduleIO> scheduleList;
	private int releaseMinutes;
	private BusVehicleVanPickupIO vanRoute;
	// ALL - All Station Point, VAN - Van Pickup Point
	private String stationPointType; 
	private BigDecimal fare;
	private String mobileNumber;
	private List<BaseIO> amenities;
	private String address;
}