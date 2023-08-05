package org.in.com.controller.api_v2.io;

import java.util.List;

import lombok.Data;

import org.in.com.controller.web.io.AmenitiesIO;

@Data
public class TripIO {
	private String tripCode;
	private String tripStageCode;
	private String displayName;
	private List<StageIO> stageList;

	// Search Result
	private List<StageFareIO> stageFare;
	private String travelTime;
	private BusIO bus;
	private ScheduleIO schedule;
	private StationIO fromStation;
	private StationIO toStation;
	private TripStatusIO tripStatus;
	private OperatorIO operator;
	private TripInfoIO tripInfo;
	private List<AmenitiesIO> amenities;
	private CancellationTermIO cancellationTerm;

}
