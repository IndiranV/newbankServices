package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleSeatAutoReleaseIO extends BaseIO {
	private List<ScheduleIO> schedules;
	private List<GroupIO> groups;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int releaseMinutes;
	private String releaseMode;// Schedule/Stage
	private String releaseType;// ACAT/HIDE/PHONE
	private String lookupCode;
	private String minutesType;// AM/PM/MIN

	private List<ScheduleSeatAutoReleaseIO> overrideList;
}