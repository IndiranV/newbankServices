package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.utils.StringUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleTicketTransferTermsDTO extends BaseDTO<ScheduleTicketTransferTermsDTO> {
	private int minutes;
	//allow only own Booked ticket flag
	private int allowBookedUser;
	private BigDecimal chargeAmount = BigDecimal.ZERO;
	private FareTypeEM chargeType;
	private MinutesTypeEM minutesType;
	private DateTime activeFrom;
	private DateTime activeTo;
	private DateTime dateTime;
	private String dayOfWeek;
	private List<ScheduleDTO> scheduleList;
	private List<RouteDTO> routeList;
	private List<GroupDTO> groupList;
	//allow other group Booked ticket transfer
	private List<GroupDTO> bookedUserGroups;
	private String lookupCode;
	private List<ScheduleTicketTransferTermsDTO> overrideList = new ArrayList<ScheduleTicketTransferTermsDTO>();

	public String getScheduleCodes() {
		StringBuilder scheduleCodes = new StringBuilder();
		if (scheduleList != null) {
			for (ScheduleDTO schedule : scheduleList) {
				if (StringUtil.isNull(schedule.getCode())) {
					continue;
				}
				scheduleCodes.append(schedule.getCode());
				scheduleCodes.append(Text.COMMA);
			}
		}
		if (routeList != null) {
			for (RouteDTO routeDTO : routeList) {
				if (routeDTO.getFromStation().getId() != 0 && routeDTO.getToStation().getId() != 0) {
					scheduleCodes.append(routeDTO.getFromStation().getId());
					scheduleCodes.append("_");
					scheduleCodes.append(routeDTO.getToStation().getId());
					scheduleCodes.append(Text.COMMA);
				}
			}
		}
		return String.valueOf(scheduleCodes);
	}

	public String getGroupCodes() {
		StringBuilder groupCodes = new StringBuilder();
		if (groupList != null) {
			for (GroupDTO group : groupList) {
				if (StringUtil.isNull(group.getCode())) {
					continue;
				}
				groupCodes.append(group.getCode());
				groupCodes.append(Text.COMMA);
			}
		}
		return String.valueOf(groupCodes);
	}

	public String getBookedGroupCodes() {
		StringBuilder groupCodes = new StringBuilder();
		if (bookedUserGroups != null) {
			for (GroupDTO group : bookedUserGroups) {
				if (StringUtil.isNull(group.getCode())) {
					continue;
				}
				groupCodes.append(group.getCode());
				groupCodes.append(Text.COMMA);
			}
		}
		return String.valueOf(groupCodes);
	}
}