package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ActiveSchedulesIO extends BaseIO {
	private ScheduleIO schedule;
	private List<ScheduleStageIO> scheduleStage;
	private List<ScheduleBusIO> scheduleBus;
	private List<ScheduleStationIO> scheduleStation;
	private List<ScheduleStationPointIO> scheduleStationPoint;
	private List<ScheduleCancellationTermIO> cancellationTerm;
	private List<ScheduleControlIO> scheduleControl;
}
