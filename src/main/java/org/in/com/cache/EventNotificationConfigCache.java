package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.EventNotificationConfigCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.EventNotificationConfigDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.EventNotificationEM;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.utils.DateUtil;

import net.sf.ehcache.Element;

public class EventNotificationConfigCache {

	private static final String CACHE_KEY = "_NTF_CONFIG";

	public List<EventNotificationConfigDTO> getCacheEventNotificationConfig(AuthDTO authDTO) {
		String key = authDTO.getNamespaceCode() + CACHE_KEY;

		List<EventNotificationConfigDTO> configList = new ArrayList<EventNotificationConfigDTO>();
		Element element = EhcacheManager.getEventNotificationConfigCache().get(key);
		if (element != null) {
			List<EventNotificationConfigCacheDTO> cacheList = (List<EventNotificationConfigCacheDTO>) element.getObjectValue();
			copyNotificationConfigFromCache(cacheList, configList);
		}
		return configList;
	}

	public void putCacheEventNotificationConfig(AuthDTO authDTO, List<EventNotificationConfigDTO> configList) {
		String key = authDTO.getNamespaceCode() + CACHE_KEY;

		List<EventNotificationConfigCacheDTO> configCacheList = copyNotificationConfigToCache(configList);
		Element element = new Element(key, configCacheList);
		EhcacheManager.getEventNotificationConfigCache().put(element);
	}

	private List<EventNotificationConfigCacheDTO> copyNotificationConfigToCache(List<EventNotificationConfigDTO> configList) {
		List<EventNotificationConfigCacheDTO> configCahceList = new ArrayList<EventNotificationConfigCacheDTO>();
		for (EventNotificationConfigDTO configDTO : configList) {

			EventNotificationConfigCacheDTO configCahceDTO = new EventNotificationConfigCacheDTO();
			configCahceDTO.setCode(configDTO.getCode());
			configCahceDTO.setActiveFrom(DateUtil.convertDate(configDTO.getActiveFrom()));
			configCahceDTO.setActiveTo(DateUtil.convertDate(configDTO.getActiveTo()));
			configCahceDTO.setStartMinitues(configDTO.getStartMinitues());
			configCahceDTO.setEndMinitues(configDTO.getEndMinitues());
			configCahceDTO.setDayOfWeek(configDTO.getDayOfWeek());
			configCahceDTO.setNotificationTypeCode(configDTO.getNotificationType().getCode());

			List<Integer> events = new ArrayList<>();
			for (EventNotificationEM notificationEventEM : configDTO.getEvents()) {
				events.add(notificationEventEM.getId());
			}
			configCahceDTO.setNotificationEventIds(events);

			List<String> deviceMediums = new ArrayList<>();
			for (DeviceMediumEM deviceMedium : configDTO.getDeviceMedium()) {
				deviceMediums.add(deviceMedium.getCode());
			}
			configCahceDTO.setDeviceMedium(deviceMediums);

			List<String> notificationMediums = new ArrayList<>();
			for (NotificationMediumEM notificationMedium : configDTO.getNotificationMedium()) {
				notificationMediums.add(notificationMedium.getCode());
			}
			configCahceDTO.setNotificationMedium(notificationMediums);

			List<Integer> scheduleList = new ArrayList<>();
			for (ScheduleDTO schedule : configDTO.getSchedule()) {
				scheduleList.add(schedule.getId());
			}
			configCahceDTO.setScheduleIds(scheduleList);

			List<String> routeList = new ArrayList<>();
			for (RouteDTO routeDTO : configDTO.getRoute()) {
				if (routeDTO.getFromStation().getId() == 0 || routeDTO.getToStation().getId() == 0) {
					continue;
				}
				routeList.add(routeDTO.getFromStation().getId() + Text.UNDER_SCORE + routeDTO.getToStation().getId());
			}
			configCahceDTO.setRoutes(routeList);

			List<Integer> groupList = new ArrayList<>();
			for (GroupDTO group : configDTO.getGroupList()) {
				groupList.add(group.getId());
			}
			configCahceDTO.setGroupIds(groupList);

			configCahceDTO.setTemplateId(configDTO.getTemplateConfig().getId());

			configCahceDTO.setActiveFlag(configDTO.getActiveFlag());
			configCahceList.add(configCahceDTO);
		}
		return configCahceList;
	}

	private void copyNotificationConfigFromCache(List<EventNotificationConfigCacheDTO> configCacheList, List<EventNotificationConfigDTO> configList) {
		if (configCacheList != null) {
			for (EventNotificationConfigCacheDTO cahceDTO : configCacheList) {
				EventNotificationConfigDTO configDTO = new EventNotificationConfigDTO();
				configDTO.setCode(cahceDTO.getCode());
				configDTO.setActiveFrom(DateUtil.getDateTime(cahceDTO.getActiveFrom()));
				configDTO.setActiveTo(DateUtil.getDateTime(cahceDTO.getActiveTo()));
				configDTO.setStartMinitues(cahceDTO.getStartMinitues());
				configDTO.setEndMinitues(cahceDTO.getEndMinitues());
				configDTO.setDayOfWeek(cahceDTO.getDayOfWeek());
				configDTO.setNotificationType(NotificationTypeEM.getNotificationTypeEM(cahceDTO.getNotificationTypeCode()));

				List<EventNotificationEM> events = new ArrayList<>();
				for (Integer event : cahceDTO.getNotificationEventIds()) {
					EventNotificationEM notificationEventEM = EventNotificationEM.getNotificationEventEM(event);
					if (notificationEventEM != null) {
						events.add(notificationEventEM);
					}
				}
				configDTO.setEvents(events);

				List<DeviceMediumEM> deviceMediums = new ArrayList<DeviceMediumEM>();
				for (String deviceMediumCode : cahceDTO.getDeviceMedium()) {
					DeviceMediumEM deviceMedium = DeviceMediumEM.getDeviceMediumEM(deviceMediumCode);
					if (deviceMedium == null) {
						continue;
					}
					deviceMediums.add(deviceMedium);
				}
				configDTO.setDeviceMedium(deviceMediums);

				List<NotificationMediumEM> notificationMediums = new ArrayList<NotificationMediumEM>();
				for (String mediumCode : cahceDTO.getNotificationMedium()) {
					NotificationMediumEM notificationMedium = NotificationMediumEM.getNotificationMediumEM(mediumCode);
					if (notificationMedium == null) {
						continue;
					}
					notificationMediums.add(notificationMedium);
				}
				configDTO.setNotificationMedium(notificationMediums);

				List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
				for (Integer scheduleId : cahceDTO.getScheduleIds()) {
					if (scheduleId == 0) {
						continue;
					}
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setId(scheduleId);
					scheduleList.add(scheduleDTO);
				}
				configDTO.setSchedule(scheduleList);

				List<RouteDTO> routeList = new ArrayList<RouteDTO>();
				for (String routes : cahceDTO.getRoutes()) {
					if (routes.split("\\_").length != 2) {
						continue;
					}
					RouteDTO routeDTO = new RouteDTO();
					StationDTO fromStation = new StationDTO();
					fromStation.setId(Integer.valueOf(routes.split("\\_")[0]));
					routeDTO.setFromStation(fromStation);

					StationDTO toStation = new StationDTO();
					toStation.setId(Integer.valueOf(routes.split("\\_")[1]));
					routeDTO.setToStation(toStation);
					routeList.add(routeDTO);
				}
				configDTO.setRoute(routeList);

				List<GroupDTO> groupList = new ArrayList<>();
				for (Integer groupId : cahceDTO.getGroupIds()) {
					if (groupId == 0) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(groupId);
					groupList.add(groupDTO);
				}
				configDTO.setGroupList(groupList);

				NotificationTemplateConfigDTO templateConfigDTO = new NotificationTemplateConfigDTO();
				templateConfigDTO.setId(cahceDTO.getTemplateId());
				configDTO.setTemplateConfig(templateConfigDTO);

				configDTO.setActiveFlag(cahceDTO.getActiveFlag());
				configList.add(configDTO);
			}
		}
	}

	public void removeEventNotificationConfigCache(AuthDTO authDTO) {
		String key = authDTO.getNamespaceCode() + CACHE_KEY;
		EhcacheManager.getEventNotificationConfigCache().remove(key);
	}
}
