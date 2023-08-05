package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleControlIO extends BaseIO {
	private ScheduleIO schedule;
	private GroupIO group;
	private int openMinitues;
	private int closeMinitues;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int allowBookingFlag;
	private StationIO fromStation;
	private StationIO toStation;
	private String lookupCode;
	private List<ScheduleControlIO> overrideList;

}