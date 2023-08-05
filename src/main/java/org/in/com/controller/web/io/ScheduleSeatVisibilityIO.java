package org.in.com.controller.web.io;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleSeatVisibilityIO extends BaseIO {
	private ScheduleIO schedule;
	private GroupIO group;
	private UserIO user;
	private String roleType;
	private String visibilityType;
	private BusIO bus;
	private List<BusSeatLayoutIO> busSeatLayout;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int releaseMinutes;
	private StationIO fromStation;
	private StationIO toStation;
	private String updatedBy;
	private String updatedAt;
	private String lookupCode;
	private String remarks;
	private List<ScheduleSeatVisibilityIO> overrideList = new ArrayList<ScheduleSeatVisibilityIO>();
	private List<UserIO> userList;
	private List<GroupIO> groupList;
	private List<RouteIO> routeList;
	private List<UserIO> routeUsers;
	private List<OrganizationIO> organizations;
}