package org.in.com.cache.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class ScheduleTicketTransferTermsCacheDTO {
	private int id;
	private int minutes;
	private int allowBookedUser;
	private BigDecimal chargeAmount = BigDecimal.ZERO;
	private String chargeTypeCode;
	private String code;
	private String minutesType;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private List<String> scheduleList;
	private List<String> routeList;
	private List<String> groupList;
	private List<String> bookedUserGroups;
	private List<ScheduleTicketTransferTermsCacheDTO> overrideList;
}