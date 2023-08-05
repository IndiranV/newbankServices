package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleTimeOverrideIO extends BaseIO {
	private ScheduleIO schedule;
	private StationIO station;
	private int overrideMinutes;
	private String overrideType;
	private boolean reactionFlag;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleTimeOverrideIO> overrideList;
	private UserIO updatedUser;
	private String updatedAt;

}