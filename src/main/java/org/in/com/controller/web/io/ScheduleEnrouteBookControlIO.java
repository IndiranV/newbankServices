package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleEnrouteBookControlIO extends BaseIO {
	private List<StageIO> stageList;
	private int releaseMinutes;
	private String dayOfWeek;
	private BaseIO enRouteType;
	private ScheduleIO schedule;
}
