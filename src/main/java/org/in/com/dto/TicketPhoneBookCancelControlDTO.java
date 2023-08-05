package org.in.com.dto;

import java.util.List;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class TicketPhoneBookCancelControlDTO extends BaseDTO<TicketPhoneBookCancelControlDTO> {
	private String refferenceType;
	private List<UserDTO> userList;
	private List<GroupDTO> groupList;
	private DateTime activeFrom;
	private DateTime activeTo;
	private String dayOfWeek;
	private int tripStageFlag;
	private int policyMinute;
	private String policyPattern;
	private List<ScheduleDTO> scheduleList;
	private List<RouteDTO> routeList;
}
