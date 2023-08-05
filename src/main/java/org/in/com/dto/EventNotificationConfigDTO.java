package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.EventNotificationEM;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.utils.StringUtil;

import com.google.common.collect.Maps;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class EventNotificationConfigDTO extends BaseDTO<EventNotificationConfigDTO> {
	private int startMinitues;
	private int endMinitues;
	private DateTime activeFrom;
	private DateTime activeTo;
	private String dayOfWeek;
	private NotificationTypeEM notificationType;
	private List<DeviceMediumEM> deviceMedium;
	private List<NotificationMediumEM> notificationMedium;
	private List<GroupDTO> groupList;
	private List<ScheduleDTO> schedule;
	private List<RouteDTO> route;
	private List<EventNotificationEM> events;
	private NotificationTemplateConfigDTO templateConfig;

	public String getDeviceMediums() {
		StringBuilder medium = new StringBuilder();
		if (deviceMedium != null) {
			for (DeviceMediumEM deviceMediumEM : deviceMedium) {
				medium.append(deviceMediumEM.getId());
				medium.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(medium.toString()) ? medium.toString() : Text.NA;
	}

	public List<String> getDeviceMediumCodes() {
		List<String> deviceMediumCodes = new ArrayList<>();
		if (deviceMedium != null) {
			for (DeviceMediumEM deviceMediumEM : deviceMedium) {
				deviceMediumCodes.add(deviceMediumEM.getCode());
			}
		}
		return deviceMediumCodes;
	}

	public String getNotificationMediums() {
		StringBuilder medium = new StringBuilder();
		if (notificationMedium != null) {
			for (NotificationMediumEM notificationMediumEM : notificationMedium) {
				medium.append(notificationMediumEM.getId());
				medium.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(medium.toString()) ? medium.toString() : Text.NA;
	}

	public String getGroupIds() {
		StringBuilder groups = new StringBuilder();
		if (groupList != null) {
			for (GroupDTO groupDTO : groupList) {
				if (groupDTO.getId() == 0) {
					continue;
				}
				groups.append(groupDTO.getId());
				groups.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(groups.toString()) ? groups.toString() : Text.NA;
	}

	public String getScheduleIds() {
		StringBuilder schedules = new StringBuilder();
		if (schedule != null) {
			for (ScheduleDTO shceduleDTO : schedule) {
				if (shceduleDTO.getId() == 0) {
					continue;
				}
				schedules.append(shceduleDTO.getId());
				schedules.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(schedules.toString()) ? schedules.toString() : Text.NA;
	}

	public String getRoutes() {
		StringBuilder routes = new StringBuilder();
		if (route != null) {
			for (RouteDTO routeDTO : route) {
				if (routeDTO.getFromStation().getId() == 0 || routeDTO.getToStation().getId() == 0) {
					continue;
				}
				routes.append(routeDTO.getFromStation().getId());
				routes.append(Text.UNDER_SCORE);
				routes.append(routeDTO.getToStation().getId());
				routes.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(routes.toString()) ? routes.toString() : Text.NA;
	}

	public Map<String, NotificationMediumEM> getNotificationMediumMap() {
		Map<String, NotificationMediumEM> notificationMediumMap = Maps.newHashMap();
		if (notificationMedium != null && !notificationMedium.isEmpty()) {
			for (NotificationMediumEM medium : notificationMedium) {
				notificationMediumMap.put(medium.getCode(), medium);
			}
		}
		return notificationMediumMap;
	}

	public String getEventToString() {
		StringBuilder event1 = new StringBuilder();
		if (events != null) {
			for (EventNotificationEM event : events) {
				event1.append(event.getCode());
				event1.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(event1.toString()) ? event1.toString() : Text.NA;
	}

}
