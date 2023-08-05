package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.UserIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripChartDetailsIO extends BaseIO {
	private String seatCode;
	private String seatName;
	private String userName;
	private String ticketCode;
	private String bookedDate;
	private String passengerName;
	private int passengerAge;
	private BigDecimal seatFare;
	private BigDecimal acBusTax;
	private String gender;
	private String passengerMobile;
	private String alternateMobile;
	private StationIO fromStation;
	private StationIO toStation;
	private StationPointIO boardingPoint;
	private StationPointIO droppingPoint;
	private String remarks;
	private UserIO bookedBy;
	private String bookedType;
	private String travelStatusCode;
	private String ticketStatusCode;
	private String idProof;
	private int boardingPointMinutes;
	private String deviceMedium;
}
