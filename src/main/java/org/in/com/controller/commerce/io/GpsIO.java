package org.in.com.controller.commerce.io;

import java.util.List;

import lombok.Data;

@Data
public class GpsIO {
	private String latitude;
	private String longitude;
	private String updatedTime;
	private String trackingCloseTime;
	private String driverName;
	private String remarks;
	private String driverMobile;
	private String registationNumber;
	private String deviceCode;
	private String vendorCode;
	private String address;
	private OperatorIO operator;
	private StationIO fromStation;
	private StationIO toStation;
	private BusIO bus;
	private float speed;
	private TripIO trip;
	private int ignition;
	private List<TicketDetailsIO> ticketDetails;

	/** Address */
	private String road;
	private String area;
	private String landmark;
	private String city;
	private String state;
	private String postalCode;
	
	private String errorCode;
	private String errorDesc;
}
