package org.in.com.controller.busbuddy.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripChartIO extends BaseIO {
	private String datetime;
	private String tripCode;
	private String tripBusCode;
	private String fromTime;
	private String endTime;
	private String syncTime;
	private List<StationPointIO> boardingPoints;
	private List<StationPointIO> droppingPoints;
	private TripIO trip;
	private ScheduleIO schedule;
	private List<BusSeatLayoutIO> vacantSeats;
	private List<StageIO> stages;
}
