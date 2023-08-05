package org.in.com.controller.api_v2.io;

import java.util.List;

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
	private Boolean bookByMyAccountFlag;
	private Boolean overrideFlag;

	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append(" - ").append(tripStageCode).append(" - ").append(boardingPoint.getCode()).append(" - ").append(droppingPoint.getCode());
		if (ticketDetails != null && !ticketDetails.isEmpty()) {
			for (TicketDetailsIO ticket : ticketDetails) {
				string.append(" {" + ticket.getSeatCode());
				string.append(" " + ticket.getPassengerName());
				string.append(" " + ticket.getPassengerAge());
				string.append(" " + ticket.getPassengerGendar() + "} ");
			}
		}
		return string.toString();
	}
}
