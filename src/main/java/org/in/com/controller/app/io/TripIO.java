package org.in.com.controller.app.io;

import java.util.List;

import lombok.Data;

import org.in.com.controller.web.io.AmenitiesIO;
import org.in.com.controller.web.io.BaseIO;

@Data
public class TripIO {
	private String tripCode;
	private String travelDate;
	private String tripStageCode;
	private List<StageIO> stageList;
	private int bookedSeatCount;
	private int availableSeatCount;
	private int travelStopCount;
	private String syncTime;

	// Search Result
	private List<StageFareIO> stageFare;
	private String travelTime;
	private String closeTime;
	private BusIO bus;
	private List<TicketIO> bookedTicketDetails;
	private ScheduleIO schedule;
	private StationIO fromStation;
	private StationIO toStation;
	private TripStatusIO tripStatus;
	private List<AmenitiesIO> amenities;
	private List<BaseIO> activities;
	// via stages
	private List<StationIO> viaStations;

	// Ticket Transfer Terms
	private ScheduleTicketTransferTermsIO ticketTransferTerms;
	private List<ScheduleDiscountIO> discountList;

}
