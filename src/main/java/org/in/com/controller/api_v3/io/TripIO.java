package org.in.com.controller.api_v3.io;

import java.util.List;
import java.util.Map;

import lombok.Data;

import org.in.com.controller.web.io.AmenitiesIO;

@Data
public class TripIO {
	private String tripCode;
	private String tripStageCode;
	private String travelDate;
	private String displayName;
	private List<StageIO> stageList;

	// Search Result
	private List<StageFareIO> stageFare;
	private String travelTime;
	private String closeTime;
	private BusIO bus;
	private ScheduleIO schedule;
	private StationIO fromStation;
	private StationIO toStation;
	private TripStatusIO tripStatus;
	private OperatorIO operator;
	private TripInfoIO tripInfo;
	private List<AmenitiesIO> amenities;
	private List<TripActivitiesIO> activities;
	private CancellationTermIO cancellationTerm;
	private Map<String, String> additionalAttributes;
	private List<StationIO> viaStations;

	// Ticket Transfer Terms
	private ScheduleTicketTransferTermsIO ticketTransferTerms;

}
