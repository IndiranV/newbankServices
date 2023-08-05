package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleSeatVisibilityDTO extends BaseDTO<ScheduleSeatVisibilityDTO> {
	private ScheduleDTO schedule;
	private String refferenceType;
	private String visibilityType;
	private BusDTO bus;
	private int releaseMinutes;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private String updatedBy;
	private String updatedAt;
	private String remarks;
	private List<ScheduleSeatVisibilityDTO> overrideList = new ArrayList<ScheduleSeatVisibilityDTO>();
	private List<GroupDTO> groupList;
	private List<UserDTO> userList;
	private List<RouteDTO> routeList;
	private List<UserDTO> routeUsers;
	private List<OrganizationDTO> organizations;

	public String getGroups() {
		StringBuilder groups = new StringBuilder();
		if (groupList != null) {
			for (GroupDTO group : groupList) {
				if (group.getId() == 0) {
					continue;
				}
				groups.append(group.getId());
				groups.append(Text.COMMA);
			}
		}
		return groups.toString();
	}

	public String getUsers() {
		StringBuilder users = new StringBuilder();
		if (userList != null) {
			for (UserDTO user : userList) {
				if (user.getId() == 0) {
					continue;
				}
				users.append(user.getId());
				users.append(Text.COMMA);
			}
		}
		return users.toString();
	}

	public String getRouteStationList() {
		StringBuilder route = new StringBuilder();
		if (routeList != null) {
			for (RouteDTO routeDTO : routeList) {
				if (routeDTO.getFromStation().getId() == 0 || routeDTO.getToStation().getId() == 0) {
					continue;
				}
				route.append(routeDTO.getFromStation().getId());
				route.append(Text.UNDER_SCORE);
				route.append(routeDTO.getToStation().getId());
				route.append(Text.COMMA);
			}
		}
		return route.toString();
	}
	
	public String getRouteUsersCodes() {
		StringBuilder routeUsersCodes = new StringBuilder();
		if (routeUsersCodes != null) {
			for (UserDTO userDTO : routeUsers) {
				if (StringUtil.isNull(userDTO.getCode())) {
					continue;
				}
				routeUsersCodes.append(userDTO.getCode());
				routeUsersCodes.append(Text.COMMA);
			}
		}
		return routeUsersCodes.toString();
	}
	
	public String getOrganizationIds() {
		StringBuilder route = new StringBuilder();
		if (organizations != null) {
			for (OrganizationDTO organization : organizations) {
				if (organization.getId() == 0) {
					continue;
				}
				route.append(organization.getId());
				route.append(Text.COMMA);
			}
		}
		return route.toString();
	}
}