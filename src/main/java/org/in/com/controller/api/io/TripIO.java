package org.in.com.controller.api.io;

import java.math.BigDecimal;
import java.util.List;

import org.in.com.controller.api_v3.io.OperatorIO;
import org.in.com.controller.web.io.AmenitiesIO;

import lombok.Data;

@Data
public class TripIO {
	private String tripCode;
	private String travelDate;
	private String displayName;
	private List<StageIO> stageList;
	private int cancelledSeatCount;
	private int bookedSeatCount;
	private BigDecimal bookedAmount;
	private BigDecimal cancelledAmount;
	private int availableSeatCount;
	private int travelStopCount;

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
	private List<StationIO> viaStations;
	private OperatorIO operator;

}
