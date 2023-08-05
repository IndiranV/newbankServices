package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleStageIO extends BaseIO {
	private ScheduleIO schedule;
	private StationIO fromStation;
	private StationIO toStation;
	private BusSeatTypeIO busSeatType;
	private GroupIO group;
	private List<BusSeatTypeFareIO> busSeatTypeFare;
	private double fare;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleStageIO> overrideList;
}