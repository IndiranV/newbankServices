package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.EventNotificationConfigIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.NotificationConfigIO;
import org.in.com.controller.web.io.NotificationTemplateConfigIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.RouteIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.StationIO;
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
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.EventNotificationConfigService;
import org.in.com.service.NotificationConfigService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/event/notification/config")
public class EventNotificationConfigController extends BaseController {

	@Autowired
	EventNotificationConfigService notificationConfigService;
	@Autowired
	NotificationConfigService notificationService;

	@RequestMapping(value = "/{configCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<EventNotificationConfigIO> getNotificationConfig(@PathVariable("authtoken") String authtoken, @PathVariable("configCode") String configCode) throws Exception {
		EventNotificationConfigIO notificationConfigIO = new EventNotificationConfigIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			EventNotificationConfigDTO notificationConfig = new EventNotificationConfigDTO();
			notificationConfig.setCode(configCode);
			notificationConfigService.get(authDTO, notificationConfig);
			if (notificationConfig.getId() != 0) {
				notificationConfigIO.setCode(notificationConfig.getCode());
				notificationConfigIO.setActiveFrom(DateUtil.convertDate(notificationConfig.getActiveFrom()));
				notificationConfigIO.setActiveTo(DateUtil.convertDate(notificationConfig.getActiveTo()));
				notificationConfigIO.setStartMinitues(notificationConfig.getStartMinitues());
				notificationConfigIO.setEndMinitues(notificationConfig.getEndMinitues());
				notificationConfigIO.setDayOfWeek(notificationConfig.getDayOfWeek());

				BaseIO notificationType = new BaseIO();
				notificationType.setCode(notificationConfig.getNotificationType().getCode());
				notificationType.setName(notificationConfig.getNotificationType().getDescription());
				notificationConfigIO.setNotificationType(notificationType);

				List<String> deviceMediums = new ArrayList<>();
				for (DeviceMediumEM deviceMedium : notificationConfig.getDeviceMedium()) {
					deviceMediums.add(deviceMedium.getCode());
				}
				notificationConfigIO.setDeviceMedium(deviceMediums);

				List<String> notificationMediums = new ArrayList<>();
				for (NotificationMediumEM notificationMedium : notificationConfig.getNotificationMedium()) {
					notificationMediums.add(notificationMedium.getCode());
				}
				notificationConfigIO.setNotificationMedium(notificationMediums);

				List<GroupIO> groupList = new ArrayList<>();
				for (GroupDTO groupDTO : notificationConfig.getGroupList()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(groupDTO.getCode());
					groupIO.setName(groupDTO.getName());
					groupList.add(groupIO);
				}
				notificationConfigIO.setGroupList(groupList);

				List<ScheduleIO> scheduleList = new ArrayList<>();
				for (ScheduleDTO scheduleDTO : notificationConfig.getSchedule()) {
					ScheduleIO scheduleIO = new ScheduleIO();
					scheduleIO.setCode(scheduleDTO.getCode());
					scheduleIO.setName(scheduleDTO.getName());
					scheduleIO.setServiceNumber(scheduleDTO.getServiceNumber());
					scheduleList.add(scheduleIO);
				}
				notificationConfigIO.setSchedule(scheduleList);

				List<RouteIO> routeList = new ArrayList<>();
				for (RouteDTO routeDTO : notificationConfig.getRoute()) {
					RouteIO routeIO = new RouteIO();
					StationIO fromStation = new StationIO();
					fromStation.setCode(routeDTO.getFromStation().getCode());
					fromStation.setName(routeDTO.getFromStation().getName());
					routeIO.setFromStation(fromStation);

					StationIO toStation = new StationIO();
					toStation.setCode(routeDTO.getToStation().getCode());
					toStation.setName(routeDTO.getToStation().getName());
					routeIO.setToStation(toStation);
					routeList.add(routeIO);
				}
				notificationConfigIO.setRoute(routeList);

				List<BaseIO> notificationEventList = new ArrayList<>();
				for (EventNotificationEM notificationEventEM : notificationConfig.getEvents()) {
					BaseIO notificationEventIO = new BaseIO();
					notificationEventIO.setCode(notificationEventEM.getCode());
					notificationEventIO.setName(notificationEventEM.getName());
					notificationEventList.add(notificationEventIO);
				}
				notificationConfigIO.setEvents(notificationEventList);

				NotificationTemplateConfigIO templateConfig = new NotificationTemplateConfigIO();
				templateConfig.setCode(notificationConfig.getTemplateConfig().getCode());
				templateConfig.setName(notificationConfig.getTemplateConfig().getName());
				templateConfig.setContent(notificationConfig.getTemplateConfig().getContent());
				templateConfig.setTemplateDltCode(notificationConfig.getTemplateConfig().getTemplateDltCode());
				notificationConfigIO.setTemplateConfig(templateConfig);

				notificationConfigIO.setActiveFlag(notificationConfig.getActiveFlag());
			}
		}
		return ResponseIO.success(notificationConfigIO);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<EventNotificationConfigIO> updateNotificationConfig(@PathVariable("authtoken") String authtoken, @RequestBody EventNotificationConfigIO notificationConfigIO) throws Exception {
		EventNotificationConfigIO notificationConfig = new EventNotificationConfigIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<EventNotificationEM> notificationEventList = new ArrayList<>();
		for (BaseIO eventIO : notificationConfigIO.getEvents()) {
			EventNotificationEM notificationEventEM = notificationConfigIO.getEvents() != null ? EventNotificationEM.getNotificationEventEM(eventIO.getCode()) : null;
			if (notificationEventEM != null) {
				notificationEventList.add(notificationEventEM);
			}
		}

		NotificationTypeEM notificationType = notificationConfigIO.getNotificationType() != null ? NotificationTypeEM.getNotificationTypeEM(notificationConfigIO.getNotificationType().getCode()) : null;
		if (notificationConfigIO.getActiveFlag() == Numeric.ONE_INT && (notificationEventList.isEmpty() || notificationType == null)) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}

		EventNotificationConfigDTO notificatinoConfigDTO = new EventNotificationConfigDTO();
		notificatinoConfigDTO.setCode(notificationConfigIO.getCode());
		notificatinoConfigDTO.setActiveFrom(DateUtil.getDateTime(notificationConfigIO.getActiveFrom()));
		notificatinoConfigDTO.setActiveTo(DateUtil.getDateTime(notificationConfigIO.getActiveTo()));
		notificatinoConfigDTO.setStartMinitues(notificationConfigIO.getStartMinitues());
		notificatinoConfigDTO.setEndMinitues(notificationConfigIO.getEndMinitues());
		notificatinoConfigDTO.setDayOfWeek(notificationConfigIO.getDayOfWeek());
		notificatinoConfigDTO.setEvents(notificationEventList);
		notificatinoConfigDTO.setNotificationType(notificationType);

		List<DeviceMediumEM> deviceMediums = new ArrayList<DeviceMediumEM>();
		if (notificationConfigIO.getDeviceMedium() != null) {
			for (String deviceMediumCode : notificationConfigIO.getDeviceMedium()) {
				DeviceMediumEM deviceMedium = DeviceMediumEM.getDeviceMediumEM(deviceMediumCode);
				if (deviceMedium == null) {
					continue;
				}
				deviceMediums.add(deviceMedium);
			}
		}
		notificatinoConfigDTO.setDeviceMedium(deviceMediums);

		List<NotificationMediumEM> notificationMediums = new ArrayList<NotificationMediumEM>();
		if (notificationConfigIO.getNotificationMedium() != null) {
			for (String mediumCode : notificationConfigIO.getNotificationMedium()) {
				NotificationMediumEM notificationMedium = NotificationMediumEM.getNotificationMediumEM(mediumCode);
				if (notificationMedium == null) {
					continue;
				}
				notificationMediums.add(notificationMedium);
			}
		}
		notificatinoConfigDTO.setNotificationMedium(notificationMediums);

		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		if (notificationConfigIO.getSchedule() != null) {
			for (ScheduleIO scheduleIO : notificationConfigIO.getSchedule()) {
				if (StringUtil.isNull(scheduleIO.getCode())) {
					continue;
				}
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(scheduleIO.getCode());
				scheduleList.add(scheduleDTO);
			}
		}
		notificatinoConfigDTO.setSchedule(scheduleList);

		List<RouteDTO> routeList = new ArrayList<RouteDTO>();
		if (notificationConfigIO.getRoute() != null) {
			for (RouteIO routeIO : notificationConfigIO.getRoute()) {
				if ((routeIO.getFromStation() == null || routeIO.getToStation() == null) || (StringUtil.isNull(routeIO.getFromStation().getCode()) || StringUtil.isNull(routeIO.getToStation().getCode()))) {
					continue;
				}
				RouteDTO routeDTO = new RouteDTO();
				StationDTO fromStation = new StationDTO();
				fromStation.setCode(routeIO.getFromStation().getCode());
				routeDTO.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setCode(routeIO.getToStation().getCode());
				routeDTO.setToStation(toStation);
				routeList.add(routeDTO);
			}
		}
		notificatinoConfigDTO.setRoute(routeList);

		List<GroupDTO> groupList = new ArrayList<>();
		if (notificationConfigIO.getGroupList() != null) {
			for (GroupIO groupIO : notificationConfigIO.getGroupList()) {
				if (StringUtil.isNull(groupIO.getCode())) {
					continue;
				}
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(groupIO.getCode());
				groupList.add(groupDTO);
			}
		}
		notificatinoConfigDTO.setGroupList(groupList);

		NotificationTemplateConfigDTO templateConfigDTO = new NotificationTemplateConfigDTO();
		if (notificationConfigIO.getTemplateConfig() != null) {
			templateConfigDTO.setCode(notificationConfigIO.getTemplateConfig().getCode());
		}
		notificatinoConfigDTO.setTemplateConfig(templateConfigDTO);

		notificatinoConfigDTO.setActiveFlag(notificationConfigIO.getActiveFlag());
		notificationConfigService.Update(authDTO, notificatinoConfigDTO);
		notificationConfig.setCode(notificatinoConfigDTO.getCode());
		return ResponseIO.success(notificationConfig);
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<EventNotificationConfigIO>> getAllNotificationConfig(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<EventNotificationConfigIO> notificationConfigList = new ArrayList<EventNotificationConfigIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<EventNotificationConfigDTO> list = notificationConfigService.getAll(authDTO);
			for (EventNotificationConfigDTO notificationConfig : list) {
				EventNotificationConfigIO notificationConfigIO = new EventNotificationConfigIO();
				notificationConfigIO.setCode(notificationConfig.getCode());
				notificationConfigIO.setActiveFrom(DateUtil.convertDate(notificationConfig.getActiveFrom()));
				notificationConfigIO.setActiveTo(DateUtil.convertDate(notificationConfig.getActiveTo()));
				notificationConfigIO.setStartMinitues(notificationConfig.getStartMinitues());
				notificationConfigIO.setEndMinitues(notificationConfig.getEndMinitues());
				notificationConfigIO.setDayOfWeek(notificationConfig.getDayOfWeek());

				BaseIO notificationType = new BaseIO();
				notificationType.setCode(notificationConfig.getNotificationType().getCode());
				notificationType.setName(notificationConfig.getNotificationType().getDescription());
				notificationConfigIO.setNotificationType(notificationType);

				List<BaseIO> notificationEventList = new ArrayList<>();
				for (EventNotificationEM notificationEventEM : notificationConfig.getEvents()) {
					BaseIO notificationEventIO = new BaseIO();
					notificationEventIO.setCode(notificationEventEM.getCode());
					notificationEventIO.setName(notificationEventEM.getName());
					notificationEventList.add(notificationEventIO);
				}
				notificationConfigIO.setEvents(notificationEventList);

				List<String> deviceMediums = new ArrayList<>();
				for (DeviceMediumEM deviceMedium : notificationConfig.getDeviceMedium()) {
					deviceMediums.add(deviceMedium.getCode());
				}
				notificationConfigIO.setDeviceMedium(deviceMediums);

				List<String> notificationMediums = new ArrayList<>();
				for (NotificationMediumEM notificationMedium : notificationConfig.getNotificationMedium()) {
					notificationMediums.add(notificationMedium.getCode());
				}
				notificationConfigIO.setNotificationMedium(notificationMediums);

				List<GroupIO> groupList = new ArrayList<>();
				for (GroupDTO groupDTO : notificationConfig.getGroupList()) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(groupDTO.getCode());
					groupIO.setName(groupDTO.getName());
					groupList.add(groupIO);
				}
				notificationConfigIO.setGroupList(groupList);

				List<ScheduleIO> scheduleList = new ArrayList<>();
				for (ScheduleDTO scheduleDTO : notificationConfig.getSchedule()) {
					ScheduleIO scheduleIO = new ScheduleIO();
					scheduleIO.setCode(scheduleDTO.getCode());
					scheduleIO.setName(scheduleDTO.getName());
					scheduleIO.setServiceNumber(scheduleDTO.getServiceNumber());
					scheduleList.add(scheduleIO);
				}
				notificationConfigIO.setSchedule(scheduleList);

				List<RouteIO> routeList = new ArrayList<>();
				for (RouteDTO routeDTO : notificationConfig.getRoute()) {
					RouteIO routeIO = new RouteIO();
					StationIO fromStation = new StationIO();
					fromStation.setCode(routeDTO.getFromStation().getCode());
					fromStation.setName(routeDTO.getFromStation().getName());
					routeIO.setFromStation(fromStation);

					StationIO toStation = new StationIO();
					toStation.setCode(routeDTO.getToStation().getCode());
					toStation.setName(routeDTO.getToStation().getName());
					routeIO.setToStation(toStation);
					routeList.add(routeIO);
				}
				notificationConfigIO.setRoute(routeList);

				NotificationTemplateConfigIO templateConfig = new NotificationTemplateConfigIO();
				templateConfig.setCode(notificationConfig.getTemplateConfig().getCode());
				templateConfig.setName(notificationConfig.getTemplateConfig().getName());
				templateConfig.setContent(notificationConfig.getTemplateConfig().getContent());
				templateConfig.setTemplateDltCode(notificationConfig.getTemplateConfig().getTemplateDltCode());
				notificationConfigIO.setTemplateConfig(templateConfig);
				notificationConfigIO.setActiveFlag(notificationConfig.getActiveFlag());
				notificationConfigList.add(notificationConfigIO);
			}
		}
		return ResponseIO.success(notificationConfigList);
	}

	@RequestMapping(value = "/template/config", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<NotificationTemplateConfigIO>> getAllNotificationTemplateSettings(@PathVariable("authtoken") String authtoken, String notificationTypeCode) throws Exception {
		List<NotificationTemplateConfigIO> smsTemplateConfigList = new ArrayList<NotificationTemplateConfigIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		NotificationTypeEM notificationTypeEM = NotificationTypeEM.getNotificationTypeEM(notificationTypeCode);
		List<NotificationTemplateConfigDTO> list = notificationService.getNotificationTemplateConfigList(authDTO, notificationTypeEM);
		for (NotificationTemplateConfigDTO smsTemplateConfigDTO : list) {
			NotificationTemplateConfigIO smsTemplateConfigIO = new NotificationTemplateConfigIO();
			smsTemplateConfigIO.setCode(smsTemplateConfigDTO.getCode());
			smsTemplateConfigIO.setName(smsTemplateConfigDTO.getName());
			smsTemplateConfigIO.setTemplateDltCode(smsTemplateConfigDTO.getTemplateDltCode());

			BaseIO notificationType = new BaseIO();
			notificationType.setCode(smsTemplateConfigDTO.getNotificationType().getCode());
			notificationType.setName(smsTemplateConfigDTO.getNotificationType().getDescription());
			smsTemplateConfigIO.setNotificationType(notificationType);

			smsTemplateConfigIO.setContent(smsTemplateConfigDTO.getContent());
			smsTemplateConfigIO.setActiveFlag(smsTemplateConfigDTO.getActiveFlag());

			NotificationConfigIO smsConfigIO = new NotificationConfigIO();
			smsConfigIO.setCode(smsTemplateConfigDTO.getNotificationSMSConfig().getCode());
			smsConfigIO.setEntityCode(smsTemplateConfigDTO.getNotificationSMSConfig().getEntityCode());
			smsConfigIO.setHeaderDltCode(smsTemplateConfigDTO.getNotificationSMSConfig().getHeaderDltCode());
			smsConfigIO.setHeader(smsTemplateConfigDTO.getNotificationSMSConfig().getHeader());
			smsTemplateConfigIO.setNotificationSMSConfig(smsConfigIO);

			smsTemplateConfigList.add(smsTemplateConfigIO);
		}
		return ResponseIO.success(smsTemplateConfigList);
	}

}
