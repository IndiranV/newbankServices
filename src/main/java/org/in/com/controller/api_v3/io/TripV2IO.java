package org.in.com.controller.api_v3.io;

import java.util.List;

import lombok.Data;

@Data
public class TripV2IO {
	private String tripCode;
	private String tripDate;
	private String displayName;
	private List<StageIO> stageList;

	// Search Result
	private ScheduleIO schedule;
	private TripStatusIO tripStatus;
	private List<TripActivitiesIO> activities;

}
