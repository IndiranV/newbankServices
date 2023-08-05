package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketPhoneBookControlIO extends BaseIO {
	private GroupIO group;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int allowMinutes;
	private int blockMinutes;
	private String blockMinutesType;// AM/PM/MIN
	private String lookupCode;

	private List<TicketPhoneBookControlIO> overrideList;

	private String dateType;
	private String refferenceType;
	private int maxSlabValueLimit;
	private String slabCalenderMode;
	private String slabCalenderType;
	private String slabMode;
	private UserIO user;

	private List<ScheduleIO> scheduleList;
	private List<RouteIO> routeList;
	private TicketStatusIO ticketStatus;
	private int respectiveFlag;
}
