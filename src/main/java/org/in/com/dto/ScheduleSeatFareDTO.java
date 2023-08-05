package org.in.com.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.FareOverrideTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleSeatFareDTO extends BaseDTO<ScheduleSeatFareDTO> {
	private ScheduleDTO schedule;
	private List<GroupDTO> groups;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private BusDTO bus;
	private BigDecimal seatFare;
	private FareTypeEM fareType;
	private FareOverrideTypeEM fareOverrideType;
	private List<RouteDTO> routes;

	private String lookupCode;
	private List<ScheduleSeatFareDTO> overrideList = new ArrayList<ScheduleSeatFareDTO>();

	public String getGroupIds() {
		StringBuilder groupId = new StringBuilder();
		if (groups != null) {
			for (GroupDTO groupDTO : groups) {
				if (groupDTO.getId() != 0) {
					groupId.append(groupDTO.getId()).append(Text.COMMA);
				}
			}
		}
		else {
			groupId.append(Text.NA);
		}
		return groupId.toString();
	}

	public String getRouteStationList() {
		StringBuilder route = new StringBuilder();
		if (routes != null) {
			for (RouteDTO routeDTO : routes) {
				if (routeDTO.getFromStation().getId() != 0 && routeDTO.getToStation().getId() != 0) {
					route.append(routeDTO.getFromStation().getId()).append(Text.UNDER_SCORE).append(routeDTO.getToStation().getId()).append(Text.COMMA);
				}
			}
		}
		else {
			route.append(Text.NA);
		}
		return route.toString();
	}
}