package org.in.com.service.impl;

import java.util.Iterator;
import java.util.List;
import java.util.TimeZone;

import org.in.com.cache.EventNotificationConfigCache;
import org.in.com.constants.Numeric;
import org.in.com.dao.EventNotificationConfigDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.EventNotificationConfigDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NotificationTemplateConfigDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.EventNotificationEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.EventNotificationConfigService;
import org.in.com.service.GroupService;
import org.in.com.service.NotificationConfigService;
import org.in.com.service.ScheduleService;
import org.in.com.service.StationService;
import org.in.com.service.TripService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import hirondelle.date4j.DateTime;

@Service
public class EventNotificationConfigImpl extends EventNotificationConfigCache implements EventNotificationConfigService {

	@Autowired
	ScheduleService scheduleService;
	@Autowired
	StationService stationService;
	@Autowired
	GroupService groupService;
	@Autowired
	NotificationConfigService notificationConfigService;
	@Autowired
	TripService tripService;

	private static final Logger logger = LoggerFactory.getLogger("org.in.com.service.impl.EventNotificationConfigImpl");

	@Override
	public List<EventNotificationConfigDTO> get(AuthDTO authDTO, EventNotificationConfigDTO notificationConfigDTO) {
		EventNotificationConfigDAO notificationConfigDAO = new EventNotificationConfigDAO();
		notificationConfigDAO.getNotificationConfig(authDTO, notificationConfigDTO);
		if (notificationConfigDTO.getId() != 0) {
			NotificationTemplateConfigDTO templateConfigDTO = getEventNotificationDefaultConfig(authDTO, notificationConfigDTO);
			notificationConfigDTO.setTemplateConfig(templateConfigDTO);
			for (ScheduleDTO scheduleDTO : notificationConfigDTO.getSchedule()) {
				scheduleService.getSchedule(authDTO, scheduleDTO);
			}
			for (RouteDTO routeDTO : notificationConfigDTO.getRoute()) {
				stationService.getStation(routeDTO.getFromStation());
				stationService.getStation(routeDTO.getToStation());
			}
			for (GroupDTO groupDTO : notificationConfigDTO.getGroupList()) {
				groupService.getGroup(authDTO, groupDTO);
			}
		}
		return null;
	}

	@Override
	public List<EventNotificationConfigDTO> getAll(AuthDTO authDTO) {
		EventNotificationConfigDAO notificationConfigDAO = new EventNotificationConfigDAO();
		List<EventNotificationConfigDTO> notificationConfigList = notificationConfigDAO.getAll(authDTO);
		for (EventNotificationConfigDTO notificationConfigDTO : notificationConfigList) {
			for (ScheduleDTO scheduleDTO : notificationConfigDTO.getSchedule()) {
				scheduleService.getSchedule(authDTO, scheduleDTO);
			}
			for (RouteDTO routeDTO : notificationConfigDTO.getRoute()) {
				stationService.getStation(routeDTO.getFromStation());
				stationService.getStation(routeDTO.getToStation());
			}
			for (GroupDTO groupDTO : notificationConfigDTO.getGroupList()) {
				groupService.getGroup(authDTO, groupDTO);
			}
			NotificationTemplateConfigDTO templateConfigDTO = getEventNotificationDefaultConfig(authDTO, notificationConfigDTO);
			notificationConfigDTO.setTemplateConfig(templateConfigDTO);
		}
		return notificationConfigList;
	}

	@Override
	public EventNotificationConfigDTO Update(AuthDTO authDTO, EventNotificationConfigDTO notificationConfigDTO) {
		EventNotificationConfigDAO notificationConfigDAO = new EventNotificationConfigDAO();
		if (notificationConfigDTO.getActiveFlag() == Numeric.ONE_INT) {
			if (StringUtil.isNotNull(notificationConfigDTO.getTemplateConfig().getCode())) {
				NotificationTemplateConfigDTO templateConfigDTO = getEventNotificationDefaultConfig(authDTO, notificationConfigDTO);
				notificationConfigDTO.setTemplateConfig(templateConfigDTO);
			}
			if (notificationConfigDTO.getTemplateConfig().getId() == 0) {
				throw new ServiceException(ErrorCode.NOTIFICATION_CONFIG_NOT_FOUND);
			}
			for (ScheduleDTO scheduleDTO : notificationConfigDTO.getSchedule()) {
				scheduleService.getSchedule(authDTO, scheduleDTO);
			}
			for (RouteDTO routeDTO : notificationConfigDTO.getRoute()) {
				stationService.getStation(routeDTO.getFromStation());
				stationService.getStation(routeDTO.getToStation());
			}
			for (GroupDTO groupDTO : notificationConfigDTO.getGroupList()) {
				groupService.getGroup(authDTO, groupDTO);
			}
		}
		notificationConfigDAO.updateNotificationConfig(authDTO, notificationConfigDTO);
		// clear cache
		removeEventNotificationConfigCache(authDTO);
		return notificationConfigDTO;
	}

	@Override
	public EventNotificationConfigDTO getActiveNotificationConfig(AuthDTO authDTO, TripDTO tripDTO, StationDTO fromStation, StationDTO toStation, EventNotificationEM notificationEvent, String ticketCode) {
		EventNotificationConfigDTO notificationConfig = null;
		try {
			List<EventNotificationConfigDTO> notificationConfigList = getEventNotificationConfig(authDTO);

			TripDTO repoTrip = null;
			if ((StringUtil.isNull(tripDTO.getTripMinutes()) || tripDTO.getTripMinutes() == 0) || (tripDTO.getSchedule() == null || tripDTO.getSchedule().getId() == 0)) {
				repoTrip = new TripDTO();
				repoTrip.setCode(tripDTO.getCode());
				repoTrip = tripService.getTripDTOwithScheduleDetails(authDTO, repoTrip);
			}
			if (repoTrip == null) {
				repoTrip = tripDTO;
			}
			DateTime travelDate = repoTrip.getTripDateTimeV2();
			for (Iterator<EventNotificationConfigDTO> iterator = notificationConfigList.iterator(); iterator.hasNext();) {
				EventNotificationConfigDTO notificationConfigDTO = iterator.next();
				DateTime now = DateTime.now(TimeZone.getDefault());
				logger.info("******** Begin  *********");
				logger.info(" configCode {}, PNR {}", notificationConfigDTO.getCode(), ticketCode);
				// common validations
				EventNotificationEM notificationEventEM = BitsUtil.isNotificationEventExists(notificationConfigDTO.getEvents(), notificationEvent);
				if (!notificationConfigDTO.getEvents().isEmpty() && notificationEventEM == null) {
					iterator.remove();
					continue;
				}
				if (notificationConfigDTO.getActiveFlag() != 1) {
					iterator.remove();
					continue;
				}
				if (notificationConfigDTO.getActiveFrom() != null && !travelDate.gteq(notificationConfigDTO.getActiveFrom().getStartOfDay())) {
					iterator.remove();
					continue;
				}
				if (notificationConfigDTO.getActiveTo() != null && !travelDate.lteq(notificationConfigDTO.getActiveTo().getEndOfDay())) {
					iterator.remove();
					continue;
				}
				if (notificationConfigDTO.getDayOfWeek() != null && notificationConfigDTO.getDayOfWeek().length() != 7) {
					iterator.remove();
					continue;
				}
				if (notificationConfigDTO.getDayOfWeek() != null && notificationConfigDTO.getDayOfWeek().substring(travelDate.getWeekDay() - 1, travelDate.getWeekDay()).equals("0")) {
					iterator.remove();
					continue;
				}
				// Check for group level or should be default
				if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !notificationConfigDTO.getGroupList().isEmpty() && BitsUtil.isGroupExists(notificationConfigDTO.getGroupList(), authDTO.getGroup()) == null) {
					iterator.remove();
					continue;
				}
				DeviceMediumEM deviceMediumEM = BitsUtil.isDeviceMediumExists(notificationConfigDTO.getDeviceMedium(), authDTO.getDeviceMedium());
				if (!notificationConfigDTO.getDeviceMedium().isEmpty() && deviceMediumEM == null) {
					iterator.remove();
					continue;
				}

				logger.info("--start minute validation begin--");
				int minutiesDifferent = DateUtil.getMinutiesDifferent(now, travelDate);
				logger.info("minutes difference: {}", minutiesDifferent);
				if (notificationConfigDTO.getStartMinitues() != 0 && notificationConfigDTO.getStartMinitues() < minutiesDifferent) {
					iterator.remove();
					continue;
				}
				logger.info("--start minute validation end--");
				logger.info("--end minute validation begin--");
				if (notificationConfigDTO.getEndMinitues() != 0 && notificationConfigDTO.getEndMinitues() > minutiesDifferent) {
					iterator.remove();
					continue;
				}
				logger.info("--end minute validation end--");
				logger.info("--schedule and route validation begin--");
				ScheduleDTO scheduleDTO = BitsUtil.isScheduleExists(notificationConfigDTO.getSchedule(), repoTrip.getSchedule());
				RouteDTO routeDTO = BitsUtil.isRouteExists(notificationConfigDTO.getRoute(), fromStation, toStation);
				if (notificationConfigDTO.getSchedule() != null && !notificationConfigDTO.getSchedule().isEmpty() && scheduleDTO == null && !notificationConfigDTO.getRoute().isEmpty() && routeDTO == null) {
					iterator.remove();
					continue;
				}
				logger.info("--schedule and route validation end--");
				NotificationTemplateConfigDTO templateConfigDTO = getEventNotificationDefaultConfig(authDTO, notificationConfigDTO);
				notificationConfigDTO.setTemplateConfig(templateConfigDTO);
				logger.info("********** End **********");
			}
			// unique template
			if (!notificationConfigList.isEmpty()) {
				notificationConfig = Iterables.getLast(notificationConfigList, null);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.error("error: " + e.getMessage());
		}
		return notificationConfig;
	}

	private List<EventNotificationConfigDTO> getEventNotificationConfig(AuthDTO authDTO) {
		List<EventNotificationConfigDTO> notificationConfigList = getCacheEventNotificationConfig(authDTO);
		if (notificationConfigList.isEmpty()) {
			notificationConfigList = getAll(authDTO);

			if (!notificationConfigList.isEmpty()) {
				putCacheEventNotificationConfig(authDTO, notificationConfigList);
			}
		}
		return notificationConfigList;
	}

	private NotificationTemplateConfigDTO getEventNotificationDefaultConfig(AuthDTO authDTO, EventNotificationConfigDTO notificationConfigDTO) {
		NotificationTemplateConfigDTO templateConfigDTO = notificationConfigService.getNotificationTemplateConfigByCode(authDTO, notificationConfigDTO.getTemplateConfig());

		return templateConfigDTO;
	}

}
