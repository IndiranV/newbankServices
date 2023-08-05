package org.in.com.controller.busbuddy.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripChartV2IO extends BaseIO {
	private String datetime;
	private String tripCode;
	private String tripBusCode;
	private String fromTime;
	private String endTime;
	private String syncTime;
	private TripIO trip;
	private ScheduleIO schedule;
	private List<TripChartDetailsIO> ticketDetails;
	private List<StageIO> stages;
}
