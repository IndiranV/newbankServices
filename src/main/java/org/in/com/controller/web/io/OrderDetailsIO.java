package org.in.com.controller.web.io;

import java.util.List;
import java.util.Map;

import lombok.Data;

@Data
public class OrderDetailsIO {
	private String tripCode;
	private String tripStageCode;
	private String ticketCode;
	private StationIO fromStation;
	private StationIO toStation;
	private String travelDate;
	private String travelTime;
	private BusIO bus;
	private ScheduleIO schedule;
	private StationPointIO boardingPoint;
	private StationPointIO droppingPoint;
	private String journeyType;// OW/RT
	private List<TicketDetailsIO> ticketDetails;
	// Phone booking
	private String offlineUserCode;
	private Boolean bookByMyAccountFlag;
	private Boolean overrideFlag;
	private String passengerMobile;
	private String remarks;
	private Map<String, String> additionalAttributes;
}
