package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.in.com.constants.Numeric;
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
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class EventNotificationConfigDAO {
	public void getNotificationConfig(AuthDTO authDTO, EventNotificationConfigDTO notificationConfigDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, event, notification_type, schedule_ids, route, device_medium, notification_medium, group_ids, active_from, active_to, day_of_week, start_minitues, end_minitues, template_id, active_flag FROM event_notification_config WHERE code = ? AND namespace_id = ? AND active_flag = 1");
			selectPS.setString(1, notificationConfigDTO.getCode());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				notificationConfigDTO.setId(selectRS.getInt("id"));
				notificationConfigDTO.setCode(selectRS.getString("code"));
				notificationConfigDTO.setEvents(convertEvent(selectRS.getString("event")));
				notificationConfigDTO.setNotificationType(NotificationTypeEM.getNotificationTypeEM(selectRS.getInt("notification_type")));

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("group_ids"));
				List<ScheduleDTO> sheduleList = convertScheduleList(selectRS.getString("schedule_ids"));
				List<RouteDTO> routeList = convertRouteList(selectRS.getString("route"));
				List<DeviceMediumEM> deviceMediums = convertDeviceMediumList(selectRS.getString("device_medium"));
				List<NotificationMediumEM> notificationMediums = convertNotificationMediumList(selectRS.getString("notification_medium"));

				notificationConfigDTO.setGroupList(groupList);
				notificationConfigDTO.setSchedule(sheduleList);
				notificationConfigDTO.setRoute(routeList);
				notificationConfigDTO.setDeviceMedium(deviceMediums);
				notificationConfigDTO.setNotificationMedium(notificationMediums);

				notificationConfigDTO.setActiveFrom(DateUtil.getDateTime(selectRS.getString("active_from")));
				notificationConfigDTO.setActiveTo(DateUtil.getDateTime(selectRS.getString("active_to")));
				notificationConfigDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				notificationConfigDTO.setStartMinitues(selectRS.getInt("start_minitues"));
				notificationConfigDTO.setEndMinitues(selectRS.getInt("end_minitues"));

				NotificationTemplateConfigDTO templateConfig = new NotificationTemplateConfigDTO();
				templateConfig.setId(selectRS.getInt("template_id"));
				notificationConfigDTO.setTemplateConfig(templateConfig);

				notificationConfigDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public List<EventNotificationConfigDTO> getNotificationConfigByEvent(AuthDTO authDTO, TicketStatusEM ticketStatusEM) {
		List<EventNotificationConfigDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, event, notification_type, schedule_ids, route, device_medium, notification_medium, group_ids, active_from, active_to, day_of_week, start_minitues, end_minitues, template_id, active_flag FROM event_notification_config WHERE ticket_event_id = ? AND namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, ticketStatusEM.getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				EventNotificationConfigDTO notificationConfigDTO = new EventNotificationConfigDTO();
				notificationConfigDTO.setCode(selectRS.getString("code"));
				notificationConfigDTO.setEvents(convertEvent(selectRS.getString("event")));
				notificationConfigDTO.setNotificationType(NotificationTypeEM.getNotificationTypeEM(selectRS.getInt("notification_type")));

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("group_ids"));
				List<ScheduleDTO> sheduleList = convertScheduleList(selectRS.getString("schedule_ids"));
				List<RouteDTO> routeList = convertRouteList(selectRS.getString("route"));
				List<DeviceMediumEM> deviceMediums = convertDeviceMediumList(selectRS.getString("device_medium"));
				List<NotificationMediumEM> notificationMediums = convertNotificationMediumList(selectRS.getString("notification_medium"));

				notificationConfigDTO.setGroupList(groupList);
				notificationConfigDTO.setSchedule(sheduleList);
				notificationConfigDTO.setRoute(routeList);
				notificationConfigDTO.setDeviceMedium(deviceMediums);
				notificationConfigDTO.setNotificationMedium(notificationMediums);

				notificationConfigDTO.setActiveFrom(DateUtil.getDateTime(selectRS.getString("active_from")));
				notificationConfigDTO.setActiveTo(DateUtil.getDateTime(selectRS.getString("active_to")));
				notificationConfigDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				notificationConfigDTO.setStartMinitues(selectRS.getInt("start_minitues"));
				notificationConfigDTO.setEndMinitues(selectRS.getInt("end_minitues"));

				NotificationTemplateConfigDTO templateConfig = new NotificationTemplateConfigDTO();
				templateConfig.setId(selectRS.getInt("template_id"));
				notificationConfigDTO.setTemplateConfig(templateConfig);

				notificationConfigDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(notificationConfigDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public List<EventNotificationConfigDTO> getAll(AuthDTO authDTO) {
		List<EventNotificationConfigDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, event, notification_type, schedule_ids, route, device_medium, notification_medium, group_ids, active_from, active_to, day_of_week, start_minitues, end_minitues, template_id, active_flag FROM event_notification_config WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				EventNotificationConfigDTO notificationConfigDTO = new EventNotificationConfigDTO();
				notificationConfigDTO.setCode(selectRS.getString("code"));
				notificationConfigDTO.setEvents(convertEvent(selectRS.getString("event")));
				notificationConfigDTO.setNotificationType(NotificationTypeEM.getNotificationTypeEM(selectRS.getInt("notification_type")));

				List<GroupDTO> groupList = convertGroupList(selectRS.getString("group_ids"));
				List<ScheduleDTO> sheduleList = convertScheduleList(selectRS.getString("schedule_ids"));
				List<RouteDTO> routeList = convertRouteList(selectRS.getString("route"));
				List<DeviceMediumEM> deviceMediums = convertDeviceMediumList(selectRS.getString("device_medium"));
				List<NotificationMediumEM> notificationMediums = convertNotificationMediumList(selectRS.getString("notification_medium"));

				notificationConfigDTO.setGroupList(groupList);
				notificationConfigDTO.setSchedule(sheduleList);
				notificationConfigDTO.setRoute(routeList);
				notificationConfigDTO.setDeviceMedium(deviceMediums);
				notificationConfigDTO.setNotificationMedium(notificationMediums);

				notificationConfigDTO.setActiveFrom(DateUtil.getDateTime(selectRS.getString("active_from")));
				notificationConfigDTO.setActiveTo(DateUtil.getDateTime(selectRS.getString("active_to")));
				notificationConfigDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				notificationConfigDTO.setStartMinitues(selectRS.getInt("start_minitues"));
				notificationConfigDTO.setEndMinitues(selectRS.getInt("end_minitues"));

				NotificationTemplateConfigDTO templateConfig = new NotificationTemplateConfigDTO();
				templateConfig.setId(selectRS.getInt("template_id"));
				notificationConfigDTO.setTemplateConfig(templateConfig);

				notificationConfigDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(notificationConfigDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public EventNotificationConfigDTO updateNotificationConfig(AuthDTO authDTO, EventNotificationConfigDTO notificationConfigDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_EVENT_NOTIFICATION_CONFIG(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?)}");
			int pindex = 0;
			callableStatement.setString(++pindex, notificationConfigDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, notificationConfigDTO.getEventToString());
			callableStatement.setInt(++pindex, notificationConfigDTO.getNotificationType() != null ? notificationConfigDTO.getNotificationType().getId() : 0);
			callableStatement.setInt(++pindex, notificationConfigDTO.getTemplateConfig().getId());
			callableStatement.setString(++pindex, notificationConfigDTO.getScheduleIds());
			callableStatement.setString(++pindex, notificationConfigDTO.getRoutes());
			callableStatement.setString(++pindex, notificationConfigDTO.getGroupIds());
			callableStatement.setString(++pindex, notificationConfigDTO.getDeviceMediums());
			callableStatement.setString(++pindex, notificationConfigDTO.getNotificationMediums());
			callableStatement.setString(++pindex, DateUtil.convertDate(notificationConfigDTO.getActiveFrom()));
			callableStatement.setString(++pindex, DateUtil.convertDate(notificationConfigDTO.getActiveTo()));
			callableStatement.setString(++pindex, notificationConfigDTO.getDayOfWeek());
			callableStatement.setInt(++pindex, notificationConfigDTO.getStartMinitues());
			callableStatement.setInt(++pindex, notificationConfigDTO.getEndMinitues());
			callableStatement.setInt(++pindex, notificationConfigDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				notificationConfigDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return notificationConfigDTO;
	}

	private List<DeviceMediumEM> convertDeviceMediumList(String deviceMediumCodes) {
		List<DeviceMediumEM> deviceMediumList = new ArrayList<>();
		if (StringUtil.isNotNull(deviceMediumCodes)) {
			List<String> deviceMediums = Arrays.asList(deviceMediumCodes.split(Text.COMMA));
			if (deviceMediums != null) {
				for (String deviceMediumId : deviceMediums) {
					if (StringUtil.isNull(deviceMediumId)) {
						continue;
					}
					deviceMediumList.add(DeviceMediumEM.getDeviceMediumEM(Integer.valueOf(deviceMediumId)));
				}
			}
		}
		return deviceMediumList;
	}

	private List<EventNotificationEM> convertEvent(String events) {
		List<EventNotificationEM> eventList = new ArrayList<>();
		if (StringUtil.isNotNull(events)) {
			List<String> events1 = Arrays.asList(events.split(Text.COMMA));
			if (events1 != null) {
				for (String event1 : events1) {
					if (StringUtil.isNull(event1)) {
						continue;
					}
					eventList.add(EventNotificationEM.getNotificationEventEM(event1));
				}
			}
		}
		return eventList;
	}

	private List<NotificationMediumEM> convertNotificationMediumList(String mediumIds) {
		List<NotificationMediumEM> deviceMediumList = new ArrayList<>();
		if (StringUtil.isNotNull(mediumIds)) {
			List<String> notificatinoMediums = Arrays.asList(mediumIds.split(Text.COMMA));
			if (notificatinoMediums != null) {
				for (String mediumId : notificatinoMediums) {
					if (StringUtil.isNull(mediumId)) {
						continue;
					}
					deviceMediumList.add(NotificationMediumEM.getNotificationMediumEM(Integer.valueOf(mediumId)));
				}
			}
		}
		return deviceMediumList;
	}

	private List<GroupDTO> convertGroupList(String groups) {
		List<GroupDTO> groupList = new ArrayList<>();
		if (StringUtil.isNotNull(groups)) {
			List<String> groupIds = Arrays.asList(groups.split(Text.COMMA));
			if (groupIds != null) {
				for (String groupId : groupIds) {
					if (StringUtil.isNull(groupId) || groupId.equals(Numeric.ZERO)) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(StringUtil.getIntegerValue(groupId));
					groupList.add(groupDTO);
				}
			}
		}
		return groupList;
	}

	private List<ScheduleDTO> convertScheduleList(String schedules) {
		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		if (StringUtil.isNotNull(schedules)) {
			List<String> scheduleIds = Arrays.asList(schedules.split(Text.COMMA));
			if (scheduleIds != null) {
				for (String scheduleId : scheduleIds) {
					if (StringUtil.isNull(scheduleId) || scheduleId.equals(Numeric.ZERO)) {
						continue;
					}
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setId(StringUtil.getIntegerValue(scheduleId));
					scheduleList.add(scheduleDTO);
				}
			}
		}
		return scheduleList;
	}

	private List<RouteDTO> convertRouteList(String routes) {
		List<RouteDTO> routeList = new ArrayList<RouteDTO>();
		if (StringUtil.isNotNull(routes)) {
			List<String> routeIds = Arrays.asList(routes.split(Text.COMMA));
			if (routeIds != null) {
				for (String route : routeIds) {
					if (StringUtil.isNull(route) || route.split("\\_").length != 2) {
						continue;
					}
					RouteDTO routeDTO = new RouteDTO();
					StationDTO fromStation = new StationDTO();
					StationDTO toStation = new StationDTO();

					fromStation.setId(StringUtil.getIntegerValue(route.split("\\_")[0]));
					toStation.setId(StringUtil.getIntegerValue(route.split("\\_")[1]));

					routeDTO.setFromStation(fromStation);
					routeDTO.setToStation(toStation);
					routeList.add(routeDTO);
				}
			}
		}
		return routeList;
	}

}
