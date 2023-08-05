package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtraCommissionDTO extends BaseDTO<ExtraCommissionDTO> {
	private FareTypeEM commissionValueType;
	private BigDecimal commissionValue = BigDecimal.ZERO;
	private List<GroupDTO> group;
	private List<UserDTO> user;
	private String refferenceType;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private DateTypeEM dateType;
	private String lookupCode;
	private List<ScheduleDTO> scheduleList;
	private List<RouteDTO> routeList;
	private List<ExtraCommissionDTO> overrideList = new ArrayList<ExtraCommissionDTO>();
	private ExtraCommissionSlabDTO commissionSlab;
	private BigDecimal maxCommissionLimit = BigDecimal.ZERO;
	private BigDecimal minTicketFare = BigDecimal.ZERO;
	private BigDecimal maxExtraCommissionAmount = BigDecimal.ZERO;
	private int minSeatCount;
	private int overrideCommissionFlag;

	public String getScheduleCode() {
		StringBuilder builder = new StringBuilder();
		if (scheduleList != null && !scheduleList.isEmpty()) {
			for (ScheduleDTO scheduleDTO : scheduleList) {
				builder.append(scheduleDTO.getCode());
			}
		}
		return builder.toString();
	}

	public DateTime getActiveFromDate() {
		return new DateTime(activeFrom);
	}

	public DateTime getActiveToDate() {
		return new DateTime(activeTo);
	}

}
