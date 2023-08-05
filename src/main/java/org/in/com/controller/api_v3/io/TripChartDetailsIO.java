package org.in.com.controller.api_v3.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.UserIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripChartDetailsIO extends BaseIO {
	private String seatName;
	private String userName;
	private String ticketCode;
	private String bookedDate;
	private String passengerName;
	private int passengerAge;
	private String gender;
	private String passengerMobile;
	private StationIO fromStation;
	private StationIO toStation;
	private StationPointIO boardingPoint;
	private UserIO bookedBy;
	private int boardingPointMinutes;
}
