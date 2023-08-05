package org.in.com.controller.web.io;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleBusOverrideIO extends BaseIO {
	private ScheduleIO schedule;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private BusIO bus;
	private NamespaceTaxIO tax;
	private String lookupCode;
	private List<String> tripDates;
	private List<ScheduleBusOverrideIO> overrideList = new ArrayList<ScheduleBusOverrideIO>();
}
