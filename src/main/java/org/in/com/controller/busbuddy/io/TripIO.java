package org.in.com.controller.busbuddy.io;

import java.util.List;

import org.in.com.controller.web.io.TripInfoIO;

import lombok.Data;

@Data
public class TripIO {
	private String tripCode;
	private String tripStartDate;
	private String tripCloseTime;
	private String syncTime;
	private BaseIO route;
	private ScheduleIO schedule;
	private BusIO bus;
	private List<StageIO> stageList;
	private TripInfoIO tripInfo;
}
