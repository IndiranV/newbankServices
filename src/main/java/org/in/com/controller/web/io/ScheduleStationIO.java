package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleStationIO extends BaseIO {
	private ScheduleIO schedule;
	private StationIO station;
	private int minitues;
	private int stationSequence;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleStationIO> overrideList;
	private String mobileNumber;
}