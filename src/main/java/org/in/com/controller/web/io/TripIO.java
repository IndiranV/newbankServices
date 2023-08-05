package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.Data;
import net.sf.json.JSONObject;

@Data
public class TripIO {
	private String tripCode;
	private String travelDate;
	private String tripStageCode;
	private List<StageIO> stageList;
	private int bookedSeatCount;
	private int availableSeatCount;
	private int multiStageBookedSeatCount;
	private BigDecimal totalBookedAmount;
	private BigDecimal revenueAmount;
	private StageIO stage;
	private JSONObject breakeven;
	private Map<String, String> additionalAttributes;
	private String remarks;

	// Search Result
	private List<StageFareIO> stageFare;
	private String travelTime;
	private String closeTime;
	private BusIO bus;
	private ScheduleIO schedule;
	private StationIO fromStation;
	private StationIO toStation;
	private TripStatusIO tripStatus;
	private TripInfoIO tripInfo;
	private List<AmenitiesIO> amenities;
	// via stages
	private List<StationIO> viaStations;
	private List<EventTriggerIO> eventList;
	private List<TripScheduleControlIO> statusList;
	private JSONObject revenue;

}
