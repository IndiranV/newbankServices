package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketPhoneBookCancelControlIO extends BaseIO {
	private String refferenceType;
	private List<UserIO> userList;
	private List<GroupIO> groupList;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int tripMinuteFlag;
	private int policyMinute;
	private String policyPattern;
	private List<ScheduleIO> scheduleList;
	private List<RouteIO> routeList;

}
