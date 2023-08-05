package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleVirtualSeatBlockIO extends BaseIO {
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleVirtualSeatBlockIO> exceptionList;
	private List<ScheduleIO> scheduleList;
	private int refreshMinutes;
	private List<GroupIO> groupList;
	private List<String> occuapancyblockPercentage;

}