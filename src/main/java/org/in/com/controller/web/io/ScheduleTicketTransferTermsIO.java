package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleTicketTransferTermsIO extends BaseIO {
	private int minutes;
	private int allowBookedUser;
	private BaseIO minutesType;
	private BigDecimal chargeAmount;
	private BaseIO chargeType;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private List<ScheduleIO> scheduleList;
	private List<RouteIO> routeList;
	private List<GroupIO> groupList;
	private List<GroupIO> bookedUserGroups;
	private List<ScheduleTicketTransferTermsIO> overrideList;
	private String lookupCode;
}