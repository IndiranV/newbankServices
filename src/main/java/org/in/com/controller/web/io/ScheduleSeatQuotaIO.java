package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleSeatQuotaIO extends BaseIO {
	private String seatName;
	private String seatCode;
	private UserIO user;
	private GroupIO group;
	private int releaseMinutes;
	private String remarks;
	private StationIO fromStation;
	private StationIO toStation;
	private String visibilityType;
	private String updatedBy;
	private String updatedAt;
	private String refferenceType;
	private List<UserIO> userList;
	private List<GroupIO> groupList;
	private List<RouteIO> routeList;
	private List<UserIO> routeUsers;
	private List<OrganizationIO> organizations;

}