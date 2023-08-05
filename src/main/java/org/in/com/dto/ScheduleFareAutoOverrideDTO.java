package org.in.com.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.FareOverrideModeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleFareAutoOverrideDTO extends BaseDTO<ScheduleFareAutoOverrideDTO> {
	private ScheduleDTO schedule;
	private List<GroupDTO> groupList;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int overrideMinutes;
	private FareOverrideModeEM fareOverrideMode;
	private BigDecimal fare;
	private List<BusSeatTypeEM> busSeatType;
	private List<BusSeatTypeFareDTO> busSeatTypeFare;
	private String lookupCode;
	private String tag;
	private List<ScheduleFareAutoOverrideDTO> overrideList = new ArrayList<ScheduleFareAutoOverrideDTO>();
	private List<RouteDTO> routeList;
	private AuditDTO audit;

	public String getRouteStationList() {
		StringBuilder route = new StringBuilder();
		if (routeList != null) {
			for (RouteDTO routeDTO : routeList) {
				if (routeDTO.getFromStation().getId() != 0 && routeDTO.getToStation().getId() != 0) {
					route.append(routeDTO.getFromStation().getId());
					route.append("_");
					route.append(routeDTO.getToStation().getId());
					route.append(Text.COMMA);
				}
			}
		}
		return route.toString();
	}

	public String getGroups() {
		StringBuilder group = new StringBuilder();
		if (groupList != null) {
			for (GroupDTO groupDTO : groupList) {
				if (groupDTO.getId() != 0) {
					group.append(groupDTO.getId());
					group.append(Text.COMMA);
				}
			}
		}
		return group.toString();
	}

	public String getBusSeatTypeIds() {
		StringBuilder seatType = new StringBuilder();
		if (busSeatType != null) {
			for (BusSeatTypeEM busSeatTypeEM : busSeatType) {
				if (busSeatTypeEM.getId() != 0) {
					seatType.append(busSeatTypeEM.getId());
					seatType.append(Text.COMMA);
				}
			}
		}
		return seatType.toString();
	}

	public String getBusSeatTypeFareDetails() {
		StringBuilder seatTypeFareBuilder = new StringBuilder();
		if (busSeatTypeFare != null && !busSeatTypeFare.isEmpty()) {
			for (BusSeatTypeFareDTO seatTypeFare : busSeatTypeFare) {
				seatTypeFareBuilder.append(seatTypeFare.getBusSeatType().getId());
				seatTypeFareBuilder.append(Text.COLON);
				seatTypeFareBuilder.append(seatTypeFare.getFare());
				seatTypeFareBuilder.append(Text.COMMA);
			}
		}
		else {
			seatTypeFareBuilder.append(Text.NA);
		}
		return seatTypeFareBuilder.toString();
	}

	public boolean isFareOverrideException() {
		boolean fareExceptionFlag = false;
		if (busSeatTypeFare != null && !busSeatTypeFare.isEmpty()) {
			for (BusSeatTypeFareDTO seatTypeFare : busSeatTypeFare) {
				if (seatTypeFare.getFare().intValue() == -1) {
					fareExceptionFlag = true;
				}
			}
		}
		return fareExceptionFlag;
	}

	public String getBusSeatTypeFareKey() {
		StringBuilder seatTypeFareBuilder = new StringBuilder();
		if (busSeatTypeFare != null && !busSeatTypeFare.isEmpty()) {
			for (BusSeatTypeFareDTO seatTypeFare : busSeatTypeFare) {
				seatTypeFareBuilder.append(seatTypeFare.getBusSeatType().getCode());
				seatTypeFareBuilder.append(Text.UNDER_SCORE);
				seatTypeFareBuilder.append(seatTypeFare.getFare());
				seatTypeFareBuilder.append(Text.UNDER_SCORE);
			}
		}
		else {
			seatTypeFareBuilder.append(Text.NA);
		}
		return seatTypeFareBuilder.toString();
	}
}