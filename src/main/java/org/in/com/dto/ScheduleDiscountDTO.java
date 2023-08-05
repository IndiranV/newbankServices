package org.in.com.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.AuthenticationTypeEM;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleDiscountDTO extends BaseDTO<ScheduleDiscountDTO> {
	private List<ScheduleDTO> scheduleList;
	private List<GroupDTO> groupList;
	private BigDecimal discountValue;
	private int percentageFlag;
	private AuthenticationTypeEM authenticationType;
	private DeviceMediumEM deviceMedium;
	private DateTypeEM dateType;
	private String activeFrom;
	private String activeTo;
	private int activeFromMinutes;
	private int activeToMinutes;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleDiscountDTO> overrideList = new ArrayList<ScheduleDiscountDTO>();
	private int afterBookingMinutes;
	private int advanceBookingDays;
	private int femaleDiscountFlag;

	public String getSchedules() {
		StringBuilder scheduels = new StringBuilder();
		if (scheduleList != null) {
			for (ScheduleDTO scheduleDTO : scheduleList) {
				scheduels.append(scheduleDTO.getCode());
				scheduels.append(Text.COMMA);
			}
		}
		return scheduels.toString();
	}

	public String getGroups() {
		StringBuilder groups = new StringBuilder();
		if (groupList != null) {
			for (GroupDTO groupDTO : groupList) {
				groups.append(groupDTO.getCode());
				groups.append(Text.COMMA);
			}
		}
		return groups.toString();
	}
}