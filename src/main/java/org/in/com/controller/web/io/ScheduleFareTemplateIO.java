package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleFareTemplateIO extends BaseIO {
	private BusIO bus;
	private List<RouteIO> stageFare;
	private String fromDate;
	private String toDate;
	private List<String> tripDates;
	private String dayOfWeek;
	private AuditIO audit;
}
