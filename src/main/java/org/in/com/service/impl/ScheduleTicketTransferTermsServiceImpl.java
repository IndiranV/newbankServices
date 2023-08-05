package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.TripCache;
import org.in.com.cache.dto.ScheduleTicketTransferTermsCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.dao.ScheduleTicketTransferTermsDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleTicketTransferTermsService;
import org.in.com.service.StationService;
import org.in.com.service.TicketService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

@Service
public class ScheduleTicketTransferTermsServiceImpl extends ScheduleCache implements ScheduleTicketTransferTermsService {
	private static String CACHEKEY = "STRST_";

	@Autowired
	ScheduleTicketTransferTermsDAO ticketRescheduleTermsDAO;
	@Autowired
	TicketService ticketService;
	@Autowired
	GroupService groupService;
	@Autowired
	StationService stationService;

	@Override
	public ScheduleTicketTransferTermsDTO updateScheduleTicketTransferTerms(AuthDTO authDTO, ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO) {
		for (RouteDTO routeDTO : scheduleTicketTransferTermsDTO.getRouteList()) {
			routeDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
			routeDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
		}
		ScheduleTicketTransferTermsDTO ticketTransferTermsDTO = ticketRescheduleTermsDAO.updateScheduleTicketTransferTerms(authDTO, scheduleTicketTransferTermsDTO);
		EhcacheManager.getScheduleEhCache().remove(CACHEKEY + authDTO.getNamespaceCode());
		return ticketTransferTermsDTO;
	}

	@Override
	public List<ScheduleTicketTransferTermsDTO> getAllScheduleTicketTransferTerms(AuthDTO authDTO) {
		List<ScheduleTicketTransferTermsDTO> list = ticketRescheduleTermsDAO.getAllScheduleTicketTransferTerms(authDTO);
		for (ScheduleTicketTransferTermsDTO scheduleTicketRescheduleTermsDTO : list) {
			for (ScheduleDTO scheduleDTO : scheduleTicketRescheduleTermsDTO.getScheduleList()) {
				scheduleDTO = getScheduleDTO(authDTO, scheduleDTO);
			}
			for (GroupDTO group : scheduleTicketRescheduleTermsDTO.getGroupList()) {
				group = groupService.getGroup(authDTO, group);
			}
			for (GroupDTO group : scheduleTicketRescheduleTermsDTO.getBookedUserGroups()) {
				group = groupService.getGroup(authDTO, group);
			}
			for (RouteDTO routeDTO : scheduleTicketRescheduleTermsDTO.getRouteList()) {
				routeDTO.setFromStation(getStationDTObyId(routeDTO.getFromStation()));
				routeDTO.setToStation(getStationDTObyId(routeDTO.getToStation()));
			}
			for (ScheduleTicketTransferTermsDTO overrideTicketTransferTerms : scheduleTicketRescheduleTermsDTO.getOverrideList()) {
				for (ScheduleDTO scheduleDTO : overrideTicketTransferTerms.getScheduleList()) {
					scheduleDTO = getScheduleDTO(authDTO, scheduleDTO);
				}
				for (GroupDTO group : overrideTicketTransferTerms.getGroupList()) {
					group = groupService.getGroup(authDTO, group);
				}
				for (GroupDTO group : overrideTicketTransferTerms.getBookedUserGroups()) {
					group = groupService.getGroup(authDTO, group);
				}
				for (RouteDTO routeDTO : overrideTicketTransferTerms.getRouteList()) {
					routeDTO.setFromStation(getStationDTObyId(routeDTO.getFromStation()));
					routeDTO.setToStation(getStationDTObyId(routeDTO.getToStation()));
				}
			}
		}
		return list;
	}

	@Override
	public ScheduleTicketTransferTermsDTO getScheduleTicketTransferTermsBySchedule(AuthDTO authDTO, ScheduleDTO schedule, StationDTO fromStation, StationDTO toStation) {
		List<ScheduleTicketTransferTermsDTO> scheduleTicketRescheduleTermsList = null;
		String cacheKey = CACHEKEY + authDTO.getNamespaceCode();
		Element element = EhcacheManager.getScheduleEhCache().get(cacheKey);
		if (element != null) {
			List<ScheduleTicketTransferTermsCacheDTO> ticketRescheduleTermsCacheList = (List<ScheduleTicketTransferTermsCacheDTO>) element.getObjectValue();
			scheduleTicketRescheduleTermsList = bindScheduleTicketTransferTermsFromCache(ticketRescheduleTermsCacheList);
		}
		else {
			scheduleTicketRescheduleTermsList = ticketRescheduleTermsDAO.getScheduleTicketTransferTerms(authDTO);
			// Save to Cache
			List<ScheduleTicketTransferTermsCacheDTO> ticketRescheduleTermsCacheList = bindScheduleTicketTransferTermsToCache(scheduleTicketRescheduleTermsList);
			element = new Element(cacheKey, ticketRescheduleTermsCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		ScheduleTicketTransferTermsDTO scheduleTicketRescheduleTerms = null;
		for (Iterator<ScheduleTicketTransferTermsDTO> iterator = scheduleTicketRescheduleTermsList.iterator(); iterator.hasNext();) {
			ScheduleTicketTransferTermsDTO scheduleTicketRescheduleTermsDTO = iterator.next();
			DateTime dateTime = schedule.getTripDate();
			if (scheduleTicketRescheduleTermsDTO.getActiveFrom() == null || scheduleTicketRescheduleTermsDTO.getActiveTo() == null || StringUtil.isNull(scheduleTicketRescheduleTermsDTO.getDayOfWeek())) {
				iterator.remove();
				continue;
			}
			// common validations
			if (!dateTime.gteq(scheduleTicketRescheduleTermsDTO.getActiveFrom())) {
				iterator.remove();
				continue;
			}
			if (!dateTime.lteq(scheduleTicketRescheduleTermsDTO.getActiveTo())) {
				iterator.remove();
				continue;
			}
			if (scheduleTicketRescheduleTermsDTO.getDayOfWeek() != null && scheduleTicketRescheduleTermsDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (scheduleTicketRescheduleTermsDTO.getDayOfWeek() != null && scheduleTicketRescheduleTermsDTO.getDayOfWeek().substring(dateTime.getWeekDay() - 1, dateTime.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Validate User Group
			if (!scheduleTicketRescheduleTermsDTO.getGroupList().isEmpty() && existGroupInGroupList(scheduleTicketRescheduleTermsDTO.getGroupList(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}
			// Validate Schedule
			if (!scheduleTicketRescheduleTermsDTO.getScheduleList().isEmpty() && existScheduleInScheduleList(scheduleTicketRescheduleTermsDTO.getScheduleList(), schedule) == null) {
				iterator.remove();
				continue;
			}
			// Validate Route
			if (!scheduleTicketRescheduleTermsDTO.getRouteList().isEmpty() && !existStageInRouteList(scheduleTicketRescheduleTermsDTO.getRouteList(), fromStation, toStation)) {
				iterator.remove();
				continue;
			}

			// Exception and override
			for (Iterator<ScheduleTicketTransferTermsDTO> overrideIterator = scheduleTicketRescheduleTermsDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleTicketTransferTermsDTO overrideTicketTransferTermsDTO = overrideIterator.next();
				if (overrideTicketTransferTermsDTO.getActiveFrom() == null || overrideTicketTransferTermsDTO.getActiveTo() == null || StringUtil.isNull(overrideTicketTransferTermsDTO.getDayOfWeek())) {
					overrideIterator.remove();
					continue;
				}
				// common validations
				if (!dateTime.gteq(overrideTicketTransferTermsDTO.getActiveFrom())) {
					overrideIterator.remove();
					continue;
				}
				if (!dateTime.lteq(overrideTicketTransferTermsDTO.getActiveTo())) {
					overrideIterator.remove();
					continue;
				}
				if (overrideTicketTransferTermsDTO.getDayOfWeek() != null && overrideTicketTransferTermsDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideTicketTransferTermsDTO.getDayOfWeek() != null && overrideTicketTransferTermsDTO.getDayOfWeek().substring(dateTime.getWeekDay() - 1, dateTime.getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}

				// Remove if Exceptions
				if (overrideTicketTransferTermsDTO.getChargeAmount().intValue() == -1) {
					// Validate Route
					if (overrideTicketTransferTermsDTO.getRouteList().isEmpty() || existStageInRouteList(overrideTicketTransferTermsDTO.getRouteList(), fromStation, toStation)) {
						iterator.remove();
						break;
					}
					// Validate User Group
					if (overrideTicketTransferTermsDTO.getGroupList().isEmpty() || existGroupInGroupList(overrideTicketTransferTermsDTO.getGroupList(), authDTO.getGroup()) != null) {
						iterator.remove();
						break;
					}
					// Validate Schedule
					if (overrideTicketTransferTermsDTO.getScheduleList().isEmpty() || existScheduleInScheduleList(overrideTicketTransferTermsDTO.getScheduleList(), schedule) != null) {
						iterator.remove();
						break;
					}
				}
				else {
					if (overrideTicketTransferTermsDTO.getRouteList().isEmpty() || existStageInRouteList(overrideTicketTransferTermsDTO.getRouteList(), fromStation, toStation)) {
						scheduleTicketRescheduleTermsDTO.setChargeAmount(overrideTicketTransferTermsDTO.getChargeAmount());
					}
					else if (overrideTicketTransferTermsDTO.getGroupList().isEmpty() || existGroupInGroupList(overrideTicketTransferTermsDTO.getGroupList(), authDTO.getGroup()) != null) {
						scheduleTicketRescheduleTermsDTO.setChargeAmount(overrideTicketTransferTermsDTO.getChargeAmount());
					}
					else if (overrideTicketTransferTermsDTO.getScheduleList().isEmpty() || existScheduleInScheduleList(overrideTicketTransferTermsDTO.getScheduleList(), schedule) != null) {
						scheduleTicketRescheduleTermsDTO.setChargeAmount(overrideTicketTransferTermsDTO.getChargeAmount());
					}
				}
			}
		}
		// Sorting
		Collections.sort(scheduleTicketRescheduleTermsList, new Comparator<ScheduleTicketTransferTermsDTO>() {
			@Override
			public int compare(ScheduleTicketTransferTermsDTO t1, ScheduleTicketTransferTermsDTO t2) {
				return new CompareToBuilder().append(t2.getActiveFrom(), t1.getActiveFrom()).append(t2.getActiveTo(), t1.getActiveTo()).toComparison();
			}
		});

		if (!scheduleTicketRescheduleTermsList.isEmpty()) {
			scheduleTicketRescheduleTerms = scheduleTicketRescheduleTermsList.get(Numeric.ZERO_INT);
		}
		return scheduleTicketRescheduleTerms;
	}

	private List<ScheduleTicketTransferTermsCacheDTO> bindScheduleTicketTransferTermsToCache(List<ScheduleTicketTransferTermsDTO> scheduleTicketTransferTermsList) {
		List<ScheduleTicketTransferTermsCacheDTO> ticketRescheduleTermsCacheList = new ArrayList<ScheduleTicketTransferTermsCacheDTO>();
		for (ScheduleTicketTransferTermsDTO scheduleTicketRescheduleTermsDTO : scheduleTicketTransferTermsList) {
			ScheduleTicketTransferTermsCacheDTO rescheduleTermsCacheDTO = new ScheduleTicketTransferTermsCacheDTO();
			rescheduleTermsCacheDTO.setId(scheduleTicketRescheduleTermsDTO.getId());
			rescheduleTermsCacheDTO.setCode(scheduleTicketRescheduleTermsDTO.getCode());
			rescheduleTermsCacheDTO.setMinutes(scheduleTicketRescheduleTermsDTO.getMinutes());
			rescheduleTermsCacheDTO.setChargeAmount(scheduleTicketRescheduleTermsDTO.getChargeAmount());
			rescheduleTermsCacheDTO.setDayOfWeek(scheduleTicketRescheduleTermsDTO.getDayOfWeek());
			rescheduleTermsCacheDTO.setMinutesType(scheduleTicketRescheduleTermsDTO.getMinutesType().getCode());
			rescheduleTermsCacheDTO.setChargeTypeCode(scheduleTicketRescheduleTermsDTO.getChargeType().getCode());
			rescheduleTermsCacheDTO.setAllowBookedUser(scheduleTicketRescheduleTermsDTO.getAllowBookedUser());

			List<String> scheduleList = new ArrayList<String>();
			for (ScheduleDTO scheduleDTO : scheduleTicketRescheduleTermsDTO.getScheduleList()) {
				scheduleList.add(scheduleDTO.getCode());
			}
			rescheduleTermsCacheDTO.setScheduleList(scheduleList);

			List<String> routes = new ArrayList<String>();
			for (RouteDTO routeDTO : scheduleTicketRescheduleTermsDTO.getRouteList()) {
				String route = routeDTO.getFromStation().getId() + "_" + routeDTO.getToStation().getId();
				routes.add(route);
			}
			rescheduleTermsCacheDTO.setRouteList(routes);

			List<String> groupList = new ArrayList<String>();
			for (GroupDTO groupDTO : scheduleTicketRescheduleTermsDTO.getGroupList()) {
				groupList.add(groupDTO.getCode());
			}
			rescheduleTermsCacheDTO.setGroupList(groupList);

			List<String> bookedGroupList = new ArrayList<String>();
			for (GroupDTO groupDTO : scheduleTicketRescheduleTermsDTO.getBookedUserGroups()) {
				bookedGroupList.add(groupDTO.getCode());
			}
			rescheduleTermsCacheDTO.setBookedUserGroups(bookedGroupList);

			rescheduleTermsCacheDTO.setActiveFrom(scheduleTicketRescheduleTermsDTO.getActiveFrom().format("YYYY-MM-DD"));
			rescheduleTermsCacheDTO.setActiveTo(scheduleTicketRescheduleTermsDTO.getActiveTo().format("YYYY-MM-DD"));

			List<ScheduleTicketTransferTermsCacheDTO> overrideList = new ArrayList<ScheduleTicketTransferTermsCacheDTO>();
			for (ScheduleTicketTransferTermsDTO overrideTicketTransferTerms : scheduleTicketRescheduleTermsDTO.getOverrideList()) {
				ScheduleTicketTransferTermsCacheDTO ticketTransferTermsDTO = new ScheduleTicketTransferTermsCacheDTO();
				ticketTransferTermsDTO.setCode(overrideTicketTransferTerms.getCode());
				ticketTransferTermsDTO.setMinutes(overrideTicketTransferTerms.getMinutes());
				ticketTransferTermsDTO.setChargeAmount(overrideTicketTransferTerms.getChargeAmount());
				ticketTransferTermsDTO.setDayOfWeek(overrideTicketTransferTerms.getDayOfWeek());
				ticketTransferTermsDTO.setMinutesType(overrideTicketTransferTerms.getMinutesType().getCode());
				ticketTransferTermsDTO.setChargeTypeCode(overrideTicketTransferTerms.getChargeType().getCode());
				ticketTransferTermsDTO.setAllowBookedUser(overrideTicketTransferTerms.getAllowBookedUser());

				List<String> scheduleCacheList = new ArrayList<String>();
				for (ScheduleDTO scheduleDTO : overrideTicketTransferTerms.getScheduleList()) {
					scheduleCacheList.add(scheduleDTO.getCode());
				}
				ticketTransferTermsDTO.setScheduleList(scheduleCacheList);

				List<String> overrideRoutes = new ArrayList<String>();
				for (RouteDTO routeDTO : overrideTicketTransferTerms.getRouteList()) {
					String route = routeDTO.getFromStation().getId() + "_" + routeDTO.getToStation().getId();
					overrideRoutes.add(route);
				}
				ticketTransferTermsDTO.setRouteList(overrideRoutes);

				List<String> groupCacheCodeList = new ArrayList<String>();
				for (GroupDTO groupDTO : overrideTicketTransferTerms.getGroupList()) {
					groupCacheCodeList.add(groupDTO.getCode());
				}
				ticketTransferTermsDTO.setGroupList(groupCacheCodeList);

				List<String> bookedroupCacheCodeList = new ArrayList<String>();
				for (GroupDTO groupDTO : overrideTicketTransferTerms.getBookedUserGroups()) {
					bookedroupCacheCodeList.add(groupDTO.getCode());
				}
				ticketTransferTermsDTO.setBookedUserGroups(bookedroupCacheCodeList);

				ticketTransferTermsDTO.setActiveFrom(overrideTicketTransferTerms.getActiveFrom().format("YYYY-MM-DD"));
				ticketTransferTermsDTO.setActiveTo(overrideTicketTransferTerms.getActiveTo().format("YYYY-MM-DD"));
				overrideList.add(ticketTransferTermsDTO);
			}
			rescheduleTermsCacheDTO.setOverrideList(overrideList);

			ticketRescheduleTermsCacheList.add(rescheduleTermsCacheDTO);
		}
		return ticketRescheduleTermsCacheList;
	}

	private List<ScheduleTicketTransferTermsDTO> bindScheduleTicketTransferTermsFromCache(List<ScheduleTicketTransferTermsCacheDTO> ticketTransferTermsCacheList) {
		List<ScheduleTicketTransferTermsDTO> scheduleTicketRescheduleTermsList = new ArrayList<ScheduleTicketTransferTermsDTO>();
		for (ScheduleTicketTransferTermsCacheDTO rescheduleTermsCacheDTO : ticketTransferTermsCacheList) {
			ScheduleTicketTransferTermsDTO scheduleTicketRescheduleTermsDTO = new ScheduleTicketTransferTermsDTO();
			scheduleTicketRescheduleTermsDTO.setId(rescheduleTermsCacheDTO.getId());
			scheduleTicketRescheduleTermsDTO.setCode(rescheduleTermsCacheDTO.getCode());
			scheduleTicketRescheduleTermsDTO.setMinutes(rescheduleTermsCacheDTO.getMinutes());
			scheduleTicketRescheduleTermsDTO.setChargeAmount(rescheduleTermsCacheDTO.getChargeAmount());
			scheduleTicketRescheduleTermsDTO.setDayOfWeek(rescheduleTermsCacheDTO.getDayOfWeek());
			scheduleTicketRescheduleTermsDTO.setMinutesType(MinutesTypeEM.getMinutesTypeEM(rescheduleTermsCacheDTO.getMinutesType()));
			scheduleTicketRescheduleTermsDTO.setChargeType(FareTypeEM.getFareTypeEM(rescheduleTermsCacheDTO.getChargeTypeCode()));
			scheduleTicketRescheduleTermsDTO.setAllowBookedUser(rescheduleTermsCacheDTO.getAllowBookedUser());

			List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
			for (String scheduleCache : rescheduleTermsCacheDTO.getScheduleList()) {
				ScheduleDTO schedule = new ScheduleDTO();
				schedule.setCode(scheduleCache);
				scheduleList.add(schedule);
			}
			scheduleTicketRescheduleTermsDTO.setScheduleList(scheduleList);

			List<RouteDTO> routeList = new ArrayList<RouteDTO>();
			for (String route : rescheduleTermsCacheDTO.getRouteList()) {
				RouteDTO routeDTO = new RouteDTO();

				StationDTO fromStation = new StationDTO();
				fromStation.setId(Integer.valueOf(route.split("_")[0]));
				routeDTO.setFromStation(getStationDTObyId(fromStation));
				routeDTO.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setId(Integer.valueOf(route.split("_")[1]));
				routeDTO.setToStation(getStationDTObyId(toStation));
				routeDTO.setToStation(toStation);
				routeList.add(routeDTO);
			}
			scheduleTicketRescheduleTermsDTO.setRouteList(routeList);

			List<GroupDTO> groupList = new ArrayList<GroupDTO>();
			for (String groupCodes : rescheduleTermsCacheDTO.getGroupList()) {
				GroupDTO group = new GroupDTO();
				group.setCode(groupCodes);
				groupList.add(group);
			}
			scheduleTicketRescheduleTermsDTO.setGroupList(groupList);

			List<GroupDTO> bookedUserGroupList = new ArrayList<GroupDTO>();
			for (String groupCodes : rescheduleTermsCacheDTO.getBookedUserGroups()) {
				GroupDTO group = new GroupDTO();
				group.setCode(groupCodes);
				bookedUserGroupList.add(group);
			}
			scheduleTicketRescheduleTermsDTO.setBookedUserGroups(bookedUserGroupList);

			scheduleTicketRescheduleTermsDTO.setActiveFrom(new DateTime(rescheduleTermsCacheDTO.getActiveFrom()));
			scheduleTicketRescheduleTermsDTO.setActiveTo(new DateTime(rescheduleTermsCacheDTO.getActiveTo()));

			List<ScheduleTicketTransferTermsDTO> overrideList = new ArrayList<ScheduleTicketTransferTermsDTO>();
			for (ScheduleTicketTransferTermsCacheDTO overrideTicketTransferTerms : rescheduleTermsCacheDTO.getOverrideList()) {
				ScheduleTicketTransferTermsDTO ticketTransferTermsDTO = new ScheduleTicketTransferTermsDTO();
				ticketTransferTermsDTO.setCode(overrideTicketTransferTerms.getCode());
				ticketTransferTermsDTO.setMinutes(overrideTicketTransferTerms.getMinutes());
				ticketTransferTermsDTO.setChargeAmount(overrideTicketTransferTerms.getChargeAmount());
				ticketTransferTermsDTO.setDayOfWeek(overrideTicketTransferTerms.getDayOfWeek());
				ticketTransferTermsDTO.setMinutesType(MinutesTypeEM.getMinutesTypeEM(overrideTicketTransferTerms.getMinutesType()));
				ticketTransferTermsDTO.setChargeType(FareTypeEM.getFareTypeEM(overrideTicketTransferTerms.getChargeTypeCode()));
				ticketTransferTermsDTO.setAllowBookedUser(overrideTicketTransferTerms.getAllowBookedUser());

				List<ScheduleDTO> scheduleCacheList = new ArrayList<ScheduleDTO>();
				for (String scheduleCodes : overrideTicketTransferTerms.getScheduleList()) {
					ScheduleDTO schedule = new ScheduleDTO();
					schedule.setCode(scheduleCodes);
					scheduleCacheList.add(schedule);
				}
				ticketTransferTermsDTO.setScheduleList(scheduleCacheList);

				List<RouteDTO> overrideRouteList = new ArrayList<RouteDTO>();
				if (overrideTicketTransferTerms.getRouteList() != null && !overrideTicketTransferTerms.getRouteList().isEmpty()) {
					for (String route : overrideTicketTransferTerms.getRouteList()) {
						RouteDTO routeDTO = new RouteDTO();

						StationDTO fromStation = new StationDTO();
						fromStation.setId(Integer.valueOf(route.split("_")[0]));
						routeDTO.setFromStation(getStationDTObyId(fromStation));
						routeDTO.setFromStation(fromStation);

						StationDTO toStation = new StationDTO();
						toStation.setId(Integer.valueOf(route.split("_")[1]));
						routeDTO.setToStation(getStationDTObyId(toStation));
						routeDTO.setToStation(toStation);
						overrideRouteList.add(routeDTO);
					}
				}
				ticketTransferTermsDTO.setRouteList(overrideRouteList);

				List<GroupDTO> groupCacheList = new ArrayList<GroupDTO>();
				for (String groupCodes : overrideTicketTransferTerms.getGroupList()) {
					GroupDTO group = new GroupDTO();
					group.setCode(groupCodes);
					groupCacheList.add(group);
				}
				ticketTransferTermsDTO.setGroupList(groupCacheList);

				List<GroupDTO> overrideBookedUserGroupList = new ArrayList<GroupDTO>();
				for (String groupCodes : overrideTicketTransferTerms.getBookedUserGroups()) {
					GroupDTO group = new GroupDTO();
					group.setCode(groupCodes);
					overrideBookedUserGroupList.add(group);
				}
				ticketTransferTermsDTO.setBookedUserGroups(overrideBookedUserGroupList);

				ticketTransferTermsDTO.setActiveFrom(new DateTime(overrideTicketTransferTerms.getActiveFrom()));
				ticketTransferTermsDTO.setActiveTo(new DateTime(overrideTicketTransferTerms.getActiveTo()));
				overrideList.add(ticketTransferTermsDTO);
			}
			scheduleTicketRescheduleTermsDTO.setOverrideList(overrideList);

			scheduleTicketRescheduleTermsList.add(scheduleTicketRescheduleTermsDTO);
		}
		return scheduleTicketRescheduleTermsList;
	}

	private ScheduleTicketTransferTermsCacheDTO bindScheduleTicketTransferTermsToCache(ScheduleTicketTransferTermsDTO scheduleTicketRescheduleTermsDTO) {
		ScheduleTicketTransferTermsCacheDTO rescheduleTermsCacheDTO = new ScheduleTicketTransferTermsCacheDTO();
		rescheduleTermsCacheDTO.setId(scheduleTicketRescheduleTermsDTO.getId());
		rescheduleTermsCacheDTO.setCode(scheduleTicketRescheduleTermsDTO.getCode());
		rescheduleTermsCacheDTO.setMinutes(scheduleTicketRescheduleTermsDTO.getMinutes());
		rescheduleTermsCacheDTO.setChargeAmount(scheduleTicketRescheduleTermsDTO.getChargeAmount());
		rescheduleTermsCacheDTO.setDayOfWeek(scheduleTicketRescheduleTermsDTO.getDayOfWeek());
		rescheduleTermsCacheDTO.setMinutesType(scheduleTicketRescheduleTermsDTO.getMinutesType().getCode());
		rescheduleTermsCacheDTO.setChargeTypeCode(scheduleTicketRescheduleTermsDTO.getChargeType().getCode());
		rescheduleTermsCacheDTO.setAllowBookedUser(scheduleTicketRescheduleTermsDTO.getAllowBookedUser());

		List<String> scheduleList = new ArrayList<String>();
		for (ScheduleDTO scheduleDTO : scheduleTicketRescheduleTermsDTO.getScheduleList()) {
			scheduleList.add(scheduleDTO.getCode());
		}
		rescheduleTermsCacheDTO.setScheduleList(scheduleList);

		List<String> routes = new ArrayList<String>();
		for (RouteDTO routeDTO : scheduleTicketRescheduleTermsDTO.getRouteList()) {
			String route = routeDTO.getFromStation().getId() + "_" + routeDTO.getToStation().getId();
			routes.add(route);
		}
		rescheduleTermsCacheDTO.setRouteList(routes);

		List<String> groupList = new ArrayList<String>();
		for (GroupDTO groupDTO : scheduleTicketRescheduleTermsDTO.getGroupList()) {
			groupList.add(groupDTO.getCode());
		}
		rescheduleTermsCacheDTO.setGroupList(groupList);

		List<String> bookedUserGroupList = new ArrayList<String>();
		for (GroupDTO groupDTO : scheduleTicketRescheduleTermsDTO.getBookedUserGroups()) {
			bookedUserGroupList.add(groupDTO.getCode());
		}
		rescheduleTermsCacheDTO.setBookedUserGroups(bookedUserGroupList);

		rescheduleTermsCacheDTO.setActiveFrom(scheduleTicketRescheduleTermsDTO.getActiveFrom().format("YYYY-MM-DD"));
		rescheduleTermsCacheDTO.setActiveTo(scheduleTicketRescheduleTermsDTO.getActiveTo().format("YYYY-MM-DD"));

		List<ScheduleTicketTransferTermsCacheDTO> overrideList = new ArrayList<ScheduleTicketTransferTermsCacheDTO>();
		for (ScheduleTicketTransferTermsDTO overrideTicketTransferTerms : scheduleTicketRescheduleTermsDTO.getOverrideList()) {
			ScheduleTicketTransferTermsCacheDTO ticketTransferTermsDTO = new ScheduleTicketTransferTermsCacheDTO();
			ticketTransferTermsDTO.setCode(overrideTicketTransferTerms.getCode());
			ticketTransferTermsDTO.setMinutes(overrideTicketTransferTerms.getMinutes());
			ticketTransferTermsDTO.setChargeAmount(overrideTicketTransferTerms.getChargeAmount());
			ticketTransferTermsDTO.setDayOfWeek(overrideTicketTransferTerms.getDayOfWeek());
			ticketTransferTermsDTO.setMinutesType(overrideTicketTransferTerms.getMinutesType().getCode());
			ticketTransferTermsDTO.setChargeTypeCode(overrideTicketTransferTerms.getChargeType().getCode());
			ticketTransferTermsDTO.setAllowBookedUser(overrideTicketTransferTerms.getAllowBookedUser());

			List<String> scheduleCacheList = new ArrayList<String>();
			for (ScheduleDTO scheduleDTO : overrideTicketTransferTerms.getScheduleList()) {
				scheduleCacheList.add(scheduleDTO.getCode());
			}
			ticketTransferTermsDTO.setScheduleList(scheduleCacheList);

			List<String> overrideRoutes = new ArrayList<String>();
			for (RouteDTO routeDTO : overrideTicketTransferTerms.getRouteList()) {
				String route = routeDTO.getFromStation().getId() + "_" + routeDTO.getToStation().getId();
				overrideRoutes.add(route);
			}
			ticketTransferTermsDTO.setRouteList(overrideRoutes);

			List<String> groupCacheCodeList = new ArrayList<String>();
			for (GroupDTO groupDTO : overrideTicketTransferTerms.getGroupList()) {
				groupCacheCodeList.add(groupDTO.getCode());
			}
			ticketTransferTermsDTO.setGroupList(groupCacheCodeList);

			List<String> overrideBookedUserGroupList = new ArrayList<String>();
			for (GroupDTO groupDTO : overrideTicketTransferTerms.getBookedUserGroups()) {
				overrideBookedUserGroupList.add(groupDTO.getCode());
			}
			ticketTransferTermsDTO.setBookedUserGroups(overrideBookedUserGroupList);

			ticketTransferTermsDTO.setActiveFrom(overrideTicketTransferTerms.getActiveFrom().format("YYYY-MM-DD"));
			ticketTransferTermsDTO.setActiveTo(overrideTicketTransferTerms.getActiveTo().format("YYYY-MM-DD"));
			overrideList.add(ticketTransferTermsDTO);
		}
		rescheduleTermsCacheDTO.setOverrideList(overrideList);

		return rescheduleTermsCacheDTO;
	}

	private ScheduleTicketTransferTermsDTO bindScheduleTicketTransferTermsFromCache(ScheduleTicketTransferTermsCacheDTO rescheduleTermsCacheDTO) {
		ScheduleTicketTransferTermsDTO scheduleTicketRescheduleTermsDTO = new ScheduleTicketTransferTermsDTO();
		scheduleTicketRescheduleTermsDTO.setId(rescheduleTermsCacheDTO.getId());
		scheduleTicketRescheduleTermsDTO.setCode(rescheduleTermsCacheDTO.getCode());
		scheduleTicketRescheduleTermsDTO.setMinutes(rescheduleTermsCacheDTO.getMinutes());
		scheduleTicketRescheduleTermsDTO.setChargeAmount(rescheduleTermsCacheDTO.getChargeAmount());
		scheduleTicketRescheduleTermsDTO.setDayOfWeek(rescheduleTermsCacheDTO.getDayOfWeek());
		scheduleTicketRescheduleTermsDTO.setMinutesType(MinutesTypeEM.getMinutesTypeEM(rescheduleTermsCacheDTO.getMinutesType()));
		scheduleTicketRescheduleTermsDTO.setChargeType(FareTypeEM.getFareTypeEM(rescheduleTermsCacheDTO.getChargeTypeCode()));
		scheduleTicketRescheduleTermsDTO.setAllowBookedUser(rescheduleTermsCacheDTO.getAllowBookedUser());

		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		for (String scheduleCache : rescheduleTermsCacheDTO.getScheduleList()) {
			ScheduleDTO schedule = new ScheduleDTO();
			schedule.setCode(scheduleCache);
			scheduleList.add(schedule);
		}
		scheduleTicketRescheduleTermsDTO.setScheduleList(scheduleList);

		List<RouteDTO> routeList = new ArrayList<RouteDTO>();
		for (String route : rescheduleTermsCacheDTO.getRouteList()) {
			RouteDTO routeDTO = new RouteDTO();

			StationDTO fromStation = new StationDTO();
			fromStation.setId(Integer.valueOf(route.split("_")[0]));
			routeDTO.setFromStation(getStationDTObyId(fromStation));
			routeDTO.setFromStation(fromStation);

			StationDTO toStation = new StationDTO();
			toStation.setId(Integer.valueOf(route.split("_")[1]));
			routeDTO.setToStation(getStationDTObyId(toStation));
			routeDTO.setToStation(toStation);
			routeList.add(routeDTO);
		}
		scheduleTicketRescheduleTermsDTO.setRouteList(routeList);

		List<GroupDTO> groupList = new ArrayList<GroupDTO>();
		for (String groupCodes : rescheduleTermsCacheDTO.getGroupList()) {
			GroupDTO group = new GroupDTO();
			group.setCode(groupCodes);
			groupList.add(group);
		}
		scheduleTicketRescheduleTermsDTO.setGroupList(groupList);

		List<GroupDTO> bookedGroupList = new ArrayList<GroupDTO>();
		for (String groupCodes : rescheduleTermsCacheDTO.getBookedUserGroups()) {
			GroupDTO group = new GroupDTO();
			group.setCode(groupCodes);
			bookedGroupList.add(group);
		}
		scheduleTicketRescheduleTermsDTO.setBookedUserGroups(bookedGroupList);

		scheduleTicketRescheduleTermsDTO.setActiveFrom(new DateTime(rescheduleTermsCacheDTO.getActiveFrom()));
		scheduleTicketRescheduleTermsDTO.setActiveTo(new DateTime(rescheduleTermsCacheDTO.getActiveTo()));

		List<ScheduleTicketTransferTermsDTO> overrideList = new ArrayList<ScheduleTicketTransferTermsDTO>();
		for (ScheduleTicketTransferTermsCacheDTO overrideTicketTransferTerms : rescheduleTermsCacheDTO.getOverrideList()) {
			ScheduleTicketTransferTermsDTO ticketTransferTermsDTO = new ScheduleTicketTransferTermsDTO();
			ticketTransferTermsDTO.setCode(overrideTicketTransferTerms.getCode());
			ticketTransferTermsDTO.setMinutes(overrideTicketTransferTerms.getMinutes());
			ticketTransferTermsDTO.setChargeAmount(overrideTicketTransferTerms.getChargeAmount());
			ticketTransferTermsDTO.setDayOfWeek(overrideTicketTransferTerms.getDayOfWeek());
			ticketTransferTermsDTO.setMinutesType(MinutesTypeEM.getMinutesTypeEM(overrideTicketTransferTerms.getMinutesType()));
			ticketTransferTermsDTO.setChargeType(FareTypeEM.getFareTypeEM(overrideTicketTransferTerms.getChargeTypeCode()));
			ticketTransferTermsDTO.setAllowBookedUser(overrideTicketTransferTerms.getAllowBookedUser());

			List<ScheduleDTO> scheduleCacheList = new ArrayList<ScheduleDTO>();
			for (String scheduleCodes : overrideTicketTransferTerms.getScheduleList()) {
				ScheduleDTO schedule = new ScheduleDTO();
				schedule.setCode(scheduleCodes);
				scheduleCacheList.add(schedule);
			}
			ticketTransferTermsDTO.setScheduleList(scheduleCacheList);

			List<RouteDTO> overrideRouteList = new ArrayList<RouteDTO>();
			if (overrideTicketTransferTerms.getRouteList() != null && !overrideTicketTransferTerms.getRouteList().isEmpty()) {
				for (String route : overrideTicketTransferTerms.getRouteList()) {
					RouteDTO routeDTO = new RouteDTO();

					StationDTO fromStation = new StationDTO();
					fromStation.setId(Integer.valueOf(route.split("_")[0]));
					routeDTO.setFromStation(getStationDTObyId(fromStation));
					routeDTO.setFromStation(fromStation);

					StationDTO toStation = new StationDTO();
					toStation.setId(Integer.valueOf(route.split("_")[1]));
					routeDTO.setToStation(getStationDTObyId(toStation));
					routeDTO.setToStation(toStation);
					overrideRouteList.add(routeDTO);
				}
			}
			ticketTransferTermsDTO.setRouteList(overrideRouteList);

			List<GroupDTO> groupCacheList = new ArrayList<GroupDTO>();
			for (String groupCodes : overrideTicketTransferTerms.getGroupList()) {
				GroupDTO group = new GroupDTO();
				group.setCode(groupCodes);
				groupCacheList.add(group);
			}
			ticketTransferTermsDTO.setGroupList(groupCacheList);

			List<GroupDTO> bookedCacheGroupList = new ArrayList<GroupDTO>();
			for (String groupCodes : overrideTicketTransferTerms.getBookedUserGroups()) {
				GroupDTO group = new GroupDTO();
				group.setCode(groupCodes);
				bookedCacheGroupList.add(group);
			}
			ticketTransferTermsDTO.setBookedUserGroups(bookedCacheGroupList);

			ticketTransferTermsDTO.setActiveFrom(new DateTime(overrideTicketTransferTerms.getActiveFrom()));
			ticketTransferTermsDTO.setActiveTo(new DateTime(overrideTicketTransferTerms.getActiveTo()));
			overrideList.add(ticketTransferTermsDTO);
		}
		scheduleTicketRescheduleTermsDTO.setOverrideList(overrideList);

		return scheduleTicketRescheduleTermsDTO;
	}

	@Override
	public ScheduleTicketTransferTermsDTO getScheduleTicketTransferTermsByTicket(AuthDTO authDTO, TicketDTO ticketDTO) {
		ScheduleTicketTransferTermsDTO ticketTransferTermsDTO = null;
		if (ticketDTO.getId() == 0) {
			ticketService.getTicketStatus(authDTO, ticketDTO);
		}

		if (ticketDTO.getScheduleTicketTransferTerms() != null && ticketDTO.getScheduleTicketTransferTerms().getId() != 0) {
			ticketTransferTermsDTO = getTicketTransferTermsByTicket(authDTO, ticketDTO.getScheduleTicketTransferTerms());
		}
		else if (ticketDTO.getTripDate().compareTo(DateUtil.getDateTime("2021-04-29")) < 0) {
			TripCache tripCache = new TripCache();
			TripDTO tripDTO = tripCache.getTripDTO(authDTO, ticketDTO.getTripDTO());

			ScheduleCache scheduleCache = new ScheduleCache();
			ScheduleDTO scheduleDTO = scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());
			scheduleDTO.setTripDate(tripDTO.getTripDate());

			ticketDTO.setFromStation(getStationDTObyId(ticketDTO.getFromStation()));
			ticketDTO.setToStation(getStationDTObyId(ticketDTO.getToStation()));

			ticketTransferTermsDTO = getScheduleTicketTransferTermsBySchedule(authDTO, scheduleDTO, ticketDTO.getFromStation(), ticketDTO.getToStation());
		}
		return ticketTransferTermsDTO;
	}

	public ScheduleTicketTransferTermsDTO getTicketTransferTermsByTicket(AuthDTO authDTO, ScheduleTicketTransferTermsDTO scheduleTicketTransferTerms) {
		ScheduleTicketTransferTermsDTO scheduleTicketRescheduleTermsDTO = null;
		String cacheKey = CACHEKEY + scheduleTicketTransferTerms.getId();
		Element element = EhcacheManager.getScheduleEhCache().get(cacheKey);
		if (element != null) {
			ScheduleTicketTransferTermsCacheDTO ticketTransferTermsCacheDTO = (ScheduleTicketTransferTermsCacheDTO) element.getObjectValue();
			scheduleTicketRescheduleTermsDTO = bindScheduleTicketTransferTermsFromCache(ticketTransferTermsCacheDTO);
		}
		else {
			scheduleTicketRescheduleTermsDTO = ticketRescheduleTermsDAO.getScheduleTicketTransferTermsById(authDTO, scheduleTicketTransferTerms);
			// Save to Cache
			ScheduleTicketTransferTermsCacheDTO ticketTransferTermsCacheDTO = bindScheduleTicketTransferTermsToCache(scheduleTicketRescheduleTermsDTO);
			element = new Element(cacheKey, ticketTransferTermsCacheDTO);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		return scheduleTicketRescheduleTermsDTO;
	}

	private boolean existStageInRouteList(List<RouteDTO> routeList, StationDTO fromStationDTO, StationDTO toStationDTO) {
		boolean status = false;
		for (RouteDTO routeDTO : routeList) {
			if (routeDTO.getFromStation().getId() == fromStationDTO.getId() && routeDTO.getToStation().getId() == toStationDTO.getId()) {
				status = true;
				break;
			}
		}
		return status;
	}

	protected GroupDTO existGroupInGroupList(List<GroupDTO> groupList, GroupDTO groupDTO) {
		GroupDTO existingGroup = null;
		for (GroupDTO group : groupList) {
			if (group.getId() != 0 && groupDTO.getId() != 0 && group.getId() == groupDTO.getId()) {
				existingGroup = group;
				break;
			}
			else if (StringUtil.isNotNull(group.getCode()) && StringUtil.isNotNull(groupDTO.getCode()) && group.getCode().equals(groupDTO.getCode())) {
				existingGroup = group;
				break;
			}
		}
		return existingGroup;
	}

	protected ScheduleDTO existScheduleInScheduleList(List<ScheduleDTO> scheduleList, ScheduleDTO scheduleDTO) {
		ScheduleDTO existingSchedule = null;
		for (ScheduleDTO schedule : scheduleList) {
			if (schedule.getId() != 0 && scheduleDTO.getId() != 0 && schedule.getId() == scheduleDTO.getId()) {
				existingSchedule = schedule;
				break;
			}
			else if (StringUtil.isNotNull(schedule.getCode()) && StringUtil.isNotNull(scheduleDTO.getCode()) && schedule.getCode().equals(scheduleDTO.getCode())) {
				existingSchedule = schedule;
				break;
			}
		}
		return existingSchedule;
	}
}
