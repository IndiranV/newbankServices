package org.in.com.controller.busbuddy.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TripChartDetailsIO {
	private String seatCode;
	private String seatName;
	private String ticketCode;
	private String bookedDate;
	private String passengerName;
	private int passengerAge;
	private BigDecimal seatFare;
	private BigDecimal acBusTax;
	private String gender;
	private String passengerMobile;
	private StationIO fromStation;
	private StationIO toStation;
	private StationPointIO boardingPoint;
	private StationPointIO droppingPoint;
	private String remarks;
	private BaseIO bookedBy;
	private String bookedType;
	private String travelStatusCode;
	private String ticketStatusCode;
	private String updatedAt;
}
