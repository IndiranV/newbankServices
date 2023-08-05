package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleSeatPreferenceIO extends BaseIO {
	private ScheduleIO schedule;
	private BusIO bus;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String preferenceGendar;
	private String lookupCode;
	private List<BusSeatLayoutIO> busSeatLayout;
	private List<GroupIO> groupList;
	private List<ScheduleSeatPreferenceIO> overrideList;
	private AuditIO audit;
}