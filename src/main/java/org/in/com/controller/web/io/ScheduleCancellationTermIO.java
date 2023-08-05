package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleCancellationTermIO extends BaseIO {
	private ScheduleIO schedule;
	private CancellationTermIO cancellationTerm;
	private GroupIO group;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleCancellationTermIO> overrideList;

}