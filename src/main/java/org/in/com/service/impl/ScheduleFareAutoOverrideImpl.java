package org.in.com.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleFareAutoOverrideCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.ScheduleFareAutoOverrideDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatTypeFareDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.FareOverrideModeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusService;
import org.in.com.service.FareRuleService;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleBusOverrideService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleFareOverrideService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleTripStageFareService;
import org.in.com.service.StationService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

@Service
public class ScheduleFareAutoOverrideImpl extends CacheCentral implements ScheduleFareOverrideService {
	@Autowired
	GroupService groupService;
	@Autowired
	ScheduleBusService scheduleBusService;
	@Autowired
	ScheduleStageService schedulestageService;
	@Autowired
	FareRuleService fareRuleService;
	@Autowired
	ScheduleBusOverrideService scheduleBusOverrideService;
	@Autowired
	BusService busService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	StationService stationService;
	@Autowired
	ScheduleTripStageFareService tripStageFareService;
	private static String CACHEKEY = "SHFARR";

	public List<ScheduleFareAutoOverrideDTO> get(AuthDTO authDTO, ScheduleFareAutoOverrideDTO scheduleFareAutoOverrideDTO) {
		ScheduleFareAutoOverrideDAO dao = new ScheduleFareAutoOverrideDAO();
		List<ScheduleFareAutoOverrideDTO> list = dao.get(authDTO, scheduleFareAutoOverrideDTO);

		ScheduleBusDTO scheduleBus = scheduleBusService.getByScheduleId(authDTO, scheduleFareAutoOverrideDTO.getSchedule());
		List<BusSeatTypeEM> busSeatTypes = new ArrayList<>(scheduleBus.getBus().getUniqueBusType().values());

		for (ScheduleFareAutoOverrideDTO scheduleFareAutoOverride : list) {
			for (GroupDTO groupDTO : scheduleFareAutoOverride.getGroupList()) {
				GroupDTO groupCache = groupService.getGroup(authDTO, groupDTO);
				groupDTO.setCode(groupCache.getCode());
				groupDTO.setName(groupCache.getName());
				groupDTO.setLevel(groupCache.getLevel());
			}
			if (scheduleFareAutoOverride.getAudit() != null && scheduleFareAutoOverride.getAudit().getUser() != null && scheduleFareAutoOverride.getAudit().getUser().getId() != 0) {
				scheduleFareAutoOverride.getAudit().setUser(getUserDTOById(authDTO, scheduleFareAutoOverride.getAudit().getUser()));
			}
			if (scheduleFareAutoOverride.getBusSeatType().isEmpty() && scheduleFareAutoOverride.getBusSeatTypeFare().isEmpty() && scheduleBus != null && scheduleBus.getBus() != null) {
				convertBusSeatTypeFares(scheduleFareAutoOverride, busSeatTypes);
			}
			for (ScheduleFareAutoOverrideDTO overrideScheduleFareOverride : scheduleFareAutoOverride.getOverrideList()) {
				for (GroupDTO groupDTO : overrideScheduleFareOverride.getGroupList()) {
					GroupDTO groupCache = groupService.getGroup(authDTO, groupDTO);
					groupDTO.setCode(groupCache.getCode());
					groupDTO.setName(groupCache.getName());
					groupDTO.setLevel(groupCache.getLevel());
				}
				if (overrideScheduleFareOverride.getAudit() != null && overrideScheduleFareOverride.getAudit().getUser() != null && overrideScheduleFareOverride.getAudit().getUser().getId() != 0) {
					overrideScheduleFareOverride.getAudit().setUser(getUserDTOById(authDTO, overrideScheduleFareOverride.getAudit().getUser()));
				}
				for (RouteDTO routeDTO : overrideScheduleFareOverride.getRouteList()) {
					routeDTO.setFromStation(getStationDTObyId(routeDTO.getFromStation()));
					routeDTO.setToStation(getStationDTObyId(routeDTO.getToStation()));
				}
				if (overrideScheduleFareOverride.getBusSeatType().isEmpty() && overrideScheduleFareOverride.getBusSeatTypeFare().isEmpty() && scheduleBus != null && scheduleBus.getBus() != null) {
					convertBusSeatTypeFares(overrideScheduleFareOverride, busSeatTypes);
				}
			}
			for (RouteDTO routeDTO : scheduleFareAutoOverride.getRouteList()) {
				routeDTO.setFromStation(getStationDTObyId(routeDTO.getFromStation()));
				routeDTO.setToStation(getStationDTObyId(routeDTO.getToStation()));
			}
		}
		return list;
	}

	private void convertBusSeatTypeFares(ScheduleFareAutoOverrideDTO scheduleFareAutoOverride, List<BusSeatTypeEM> busSeatTypes) {
		for (BusSeatTypeEM busSeatTypeEM : busSeatTypes) {
			BusSeatTypeFareDTO busSeatTypeFare = new BusSeatTypeFareDTO();
			busSeatTypeFare.setFare(scheduleFareAutoOverride.getFare());
			busSeatTypeFare.setBusSeatType(busSeatTypeEM);
			scheduleFareAutoOverride.getBusSeatTypeFare().add(busSeatTypeFare);
		}
	}

	public ScheduleFareAutoOverrideDTO Update(AuthDTO authDTO, ScheduleFareAutoOverrideDTO scheduleFareAutoOverride) {
		ScheduleFareAutoOverrideDAO dao = new ScheduleFareAutoOverrideDAO();
		for (RouteDTO routeDTO : scheduleFareAutoOverride.getRouteList()) {
			routeDTO.setFromStation(getStationDTO(routeDTO.getFromStation()));
			routeDTO.setToStation(getStationDTO(routeDTO.getToStation()));
		}

		// Route Minimum & Maximum Fare Validation
		if (scheduleFareAutoOverride.getActiveFlag() == 1 && !authDTO.getNamespace().getProfile().getFareRule().isEmpty()) {
			applyFareRule(authDTO, scheduleFareAutoOverride);
		}

		for (GroupDTO groupDTO : scheduleFareAutoOverride.getGroupList()) {
			GroupDTO groupCache = groupService.getGroup(authDTO, groupDTO);
			groupDTO.setId(groupCache.getId());
		}

		dao.getIUD(authDTO, scheduleFareAutoOverride);
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.removeScheduleDTO(authDTO, scheduleFareAutoOverride.getSchedule());

		return scheduleFareAutoOverride;
	}

	public void UpdateV2(AuthDTO authDTO, List<ScheduleFareAutoOverrideDTO> scheduleFareAutoOverrideList) {
		for (ScheduleFareAutoOverrideDTO scheduleFareAutoOverride : scheduleFareAutoOverrideList) {
			for (RouteDTO routeDTO : scheduleFareAutoOverride.getRouteList()) {
				routeDTO.setFromStation(getStationDTO(routeDTO.getFromStation()));
				routeDTO.setToStation(getStationDTO(routeDTO.getToStation()));
			}
			for (GroupDTO groupDTO : scheduleFareAutoOverride.getGroupList()) {
				GroupDTO groupCache = groupService.getGroup(authDTO, groupDTO);
				groupDTO.setId(groupCache.getId());
			}
		}
		ScheduleFareAutoOverrideDAO dao = new ScheduleFareAutoOverrideDAO();
		dao.UpdateIUDV2(authDTO, scheduleFareAutoOverrideList);

		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.removeScheduleDTO(authDTO, scheduleFareAutoOverrideList.get(0).getSchedule());
	}

	public List<ScheduleFareAutoOverrideDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO) {
		List<ScheduleFareAutoOverrideDTO> searchFareAutoOverrides = tripStageFareService.getTripStageActiveFare(authDTO, scheduleDTO, fromStationDTO, toStationDTO);

		if (searchFareAutoOverrides == null || searchFareAutoOverrides.isEmpty()) {
			searchFareAutoOverrides = getBySchedule(authDTO, scheduleDTO, fromStationDTO, toStationDTO);
		}
		return searchFareAutoOverrides;
	}

	private List<ScheduleFareAutoOverrideDTO> getBySchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO) {
		List<ScheduleFareAutoOverrideDTO> fareOverrideDTOList = getFareOverrideByScheduleId(authDTO, scheduleDTO);

		// Validate all fare override
		for (Iterator<ScheduleFareAutoOverrideDTO> iterator = fareOverrideDTOList.iterator(); iterator.hasNext();) {
			ScheduleFareAutoOverrideDTO fareOverrideDTO = iterator.next();
			// common validations
			if (fareOverrideDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(fareOverrideDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(fareOverrideDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getDayOfWeek() != null && fareOverrideDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getDayOfWeek() != null && fareOverrideDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Group List
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !fareOverrideDTO.getGroupList().isEmpty() && existGroupInGroupList(fareOverrideDTO.getGroupList(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
				iterator.remove();
				continue;
			}

			// Route List
			if (!fareOverrideDTO.getRouteList().isEmpty() && !existStageInRouteList(fareOverrideDTO.getRouteList(), fromStationDTO, toStationDTO)) {
				iterator.remove();
				continue;
			}

			// Exceptions and Override
			for (Iterator<ScheduleFareAutoOverrideDTO> overrideIterator = fareOverrideDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleFareAutoOverrideDTO fareAutoOverrideExceptionDTO = overrideIterator.next();
				if (!scheduleDTO.getTripDate().gteq(new DateTime(fareAutoOverrideExceptionDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!scheduleDTO.getTripDate().lteq(new DateTime(fareAutoOverrideExceptionDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (fareAutoOverrideExceptionDTO.getDayOfWeek() != null && fareAutoOverrideExceptionDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (fareAutoOverrideExceptionDTO.getDayOfWeek() != null && fareAutoOverrideExceptionDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}

				// Apply Exceptions
				if (fareAutoOverrideExceptionDTO.getFare().intValue() == -1) {
					if (fareOverrideDTO.getFareOverrideMode().getId() == FareOverrideModeEM.SCHEDULE_FARE.getId() || fareOverrideDTO.getFareOverrideMode().getId() == FareOverrideModeEM.SCHEDULE_FARE_V2.getId()) {
						if (fareAutoOverrideExceptionDTO.getRouteList().isEmpty() || existStageInRouteList(fareAutoOverrideExceptionDTO.getRouteList(), fromStationDTO, toStationDTO)) {
							if (fareAutoOverrideExceptionDTO.getGroupList().isEmpty() || existGroupInGroupList(fareAutoOverrideExceptionDTO.getGroupList(), authDTO.getGroup()) != null) {
								iterator.remove();
								break;
							}
						}

					}
					else if (fareOverrideDTO.getFareOverrideMode().getId() == FareOverrideModeEM.SEARCH_FARE.getId()) {
						if (fareAutoOverrideExceptionDTO.getRouteList().isEmpty() || (!fareAutoOverrideExceptionDTO.getRouteList().isEmpty() && existStageInRouteList(fareAutoOverrideExceptionDTO.getRouteList(), fromStationDTO, toStationDTO))) {
							if (fareAutoOverrideExceptionDTO.getGroupList().isEmpty() || (!fareAutoOverrideExceptionDTO.getGroupList().isEmpty() && existGroupInGroupList(fareAutoOverrideExceptionDTO.getGroupList(), authDTO.getGroup()) != null)) {
								iterator.remove();
								break;
							}
						}
					}
				}
				else {
					if (!fareAutoOverrideExceptionDTO.getRouteList().isEmpty() && !existStageInRouteList(fareAutoOverrideExceptionDTO.getRouteList(), fromStationDTO, toStationDTO)) {
						if (fareAutoOverrideExceptionDTO.getGroupList().isEmpty() || (!fareAutoOverrideExceptionDTO.getGroupList().isEmpty() && existGroupInGroupList(fareAutoOverrideExceptionDTO.getGroupList(), authDTO.getGroup()) == null)) {
							overrideIterator.remove();
							continue;
						}
					}
					// Apply Override
					fareOverrideDTO.setFare(fareAutoOverrideExceptionDTO.getFare());
					for (BusSeatTypeFareDTO busSeatTypeFareDTO : fareAutoOverrideExceptionDTO.getBusSeatTypeFare()) {
						for (BusSeatTypeEM busSeatTypeEM : fareOverrideDTO.getBusSeatType()) {
							if (busSeatTypeEM.getId() == busSeatTypeFareDTO.getBusSeatType().getId()) {
								fareOverrideDTO.setFare(busSeatTypeFareDTO.getFare());
								break;
							}
						}
					}

					if (fareOverrideDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
						overrideIterator.remove();
						continue;
					}

					fareOverrideDTO.setOverrideMinutes(fareAutoOverrideExceptionDTO.getOverrideMinutes());
					fareOverrideDTO.setRouteList(fareAutoOverrideExceptionDTO.getRouteList());
					fareOverrideDTO.setGroupList(fareAutoOverrideExceptionDTO.getGroupList());
					fareOverrideDTO.setActiveFrom(fareAutoOverrideExceptionDTO.getActiveFrom());
					fareOverrideDTO.setActiveTo(fareAutoOverrideExceptionDTO.getActiveTo());
				}
			}
		}
		// Identify and remove the generic fare
		if (fareOverrideDTOList.size() >= 2) {

			// Sorting, find most recent close date
			Collections.sort(fareOverrideDTOList, new Comparator<ScheduleFareAutoOverrideDTO>() {
				public int compare(final ScheduleFareAutoOverrideDTO object1, final ScheduleFareAutoOverrideDTO object2) {
					return Integer.compare(DateUtil.getDayDifferent(new DateTime(object1.getActiveFrom()), new DateTime(object1.getActiveTo())), DateUtil.getDayDifferent(new DateTime(object2.getActiveFrom()), new DateTime(object2.getActiveTo())));
				}
			});
		}
		return fareOverrideDTOList;
	}

	public List<ScheduleFareAutoOverrideDTO> getTripScheduleActiveFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO, Map<Integer, GroupDTO> groupMap, List<BusSeatTypeEM> busSeatTypes) {
		List<ScheduleFareAutoOverrideDTO> searchFareAutoOverrides = tripStageFareService.getTripStageActiveFare(authDTO, scheduleDTO, fromStationDTO, toStationDTO);

		if (searchFareAutoOverrides == null || searchFareAutoOverrides.isEmpty()) {
			searchFareAutoOverrides = getScheduleActiveFare(authDTO, scheduleDTO, fromStationDTO, toStationDTO, groupMap, busSeatTypes);
		}
		return searchFareAutoOverrides;
	}

	private List<ScheduleFareAutoOverrideDTO> getScheduleActiveFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO, Map<Integer, GroupDTO> groupMap, List<BusSeatTypeEM> busSeatTypes) {
		List<ScheduleFareAutoOverrideDTO> fareOverrideDTOList = getFareOverrideByScheduleId(authDTO, scheduleDTO);

		// Validate all Fare Override
		for (Iterator<ScheduleFareAutoOverrideDTO> iterator = fareOverrideDTOList.iterator(); iterator.hasNext();) {
			ScheduleFareAutoOverrideDTO fareOverrideDTO = iterator.next();
			// common validations
			if (fareOverrideDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(fareOverrideDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(fareOverrideDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getDayOfWeek() != null && fareOverrideDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getDayOfWeek() != null && fareOverrideDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Group List
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !fareOverrideDTO.getGroupList().isEmpty() && existGroupInGroupList(fareOverrideDTO.getGroupList(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
				iterator.remove();
				continue;
			}

			// Route List
			if (!fareOverrideDTO.getRouteList().isEmpty() && !existStageInRouteList(fareOverrideDTO.getRouteList(), fromStationDTO, toStationDTO)) {
				iterator.remove();
				continue;
			}
			BusSeatTypeEM busSeatTypeEM = BitsUtil.existBusSeatTypeList(fareOverrideDTO.getBusSeatType(), busSeatTypes);
			if (!fareOverrideDTO.getBusSeatType().isEmpty() && busSeatTypeEM == null) {
				iterator.remove();
				continue;
			}

			// Exceptions and Override
			for (Iterator<ScheduleFareAutoOverrideDTO> overrideIterator = fareOverrideDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleFareAutoOverrideDTO fareAutoOverrideExceptionDTO = overrideIterator.next();

				if (!scheduleDTO.getTripDate().gteq(new DateTime(fareAutoOverrideExceptionDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!scheduleDTO.getTripDate().lteq(new DateTime(fareAutoOverrideExceptionDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (fareAutoOverrideExceptionDTO.getDayOfWeek() != null && fareAutoOverrideExceptionDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (fareAutoOverrideExceptionDTO.getDayOfWeek() != null && fareAutoOverrideExceptionDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}

				// Apply Exceptions
				if (fareAutoOverrideExceptionDTO.getFare().intValue() == -1) {
					if (fareAutoOverrideExceptionDTO.getRouteList().isEmpty() || (!fareAutoOverrideExceptionDTO.getRouteList().isEmpty() && existStageInRouteList(fareAutoOverrideExceptionDTO.getRouteList(), fromStationDTO, toStationDTO))) {
						if (fareAutoOverrideExceptionDTO.getGroupList().isEmpty() || (!fareAutoOverrideExceptionDTO.getGroupList().isEmpty() && existGroupInGroupList(fareAutoOverrideExceptionDTO.getGroupList(), authDTO.getGroup()) != null)) {
							iterator.remove();
							break;
						}
					}
				}
				else {
					if (fareAutoOverrideExceptionDTO.getRouteList().isEmpty() || (!fareAutoOverrideExceptionDTO.getRouteList().isEmpty() && !existStageInRouteList(fareAutoOverrideExceptionDTO.getRouteList(), fromStationDTO, toStationDTO))) {
						if (fareAutoOverrideExceptionDTO.getGroupList().isEmpty() || (!fareAutoOverrideExceptionDTO.getGroupList().isEmpty() && existGroupInGroupList(fareAutoOverrideExceptionDTO.getGroupList(), authDTO.getGroup()) == null)) {
							overrideIterator.remove();
							continue;
						}
					}

					// Apply Override
					fareOverrideDTO.setFare(fareAutoOverrideExceptionDTO.getFare());
					for (BusSeatTypeFareDTO busSeatTypeFareDTO : fareAutoOverrideExceptionDTO.getBusSeatTypeFare()) {
						for (BusSeatTypeEM busSeatType : fareOverrideDTO.getBusSeatType()) {
							if (busSeatType.getId() == busSeatTypeFareDTO.getBusSeatType().getId()) {
								fareOverrideDTO.setFare(busSeatTypeFareDTO.getFare());
								break;
							}
						}
					}

					if (fareOverrideDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
						overrideIterator.remove();
						continue;
					}

					fareOverrideDTO.setOverrideMinutes(fareAutoOverrideExceptionDTO.getOverrideMinutes());
					fareOverrideDTO.setRouteList(fareAutoOverrideExceptionDTO.getRouteList());
					fareOverrideDTO.setGroupList(fareAutoOverrideExceptionDTO.getGroupList());
					fareOverrideDTO.setActiveFrom(fareAutoOverrideExceptionDTO.getActiveFrom());
					fareOverrideDTO.setActiveTo(fareAutoOverrideExceptionDTO.getActiveTo());
				}
			}
		}
		return fareOverrideDTOList;
	}

	public List<ScheduleFareAutoOverrideDTO> getByScheduleTripDate(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate) {
		String key = CACHEKEY + scheduleDTO.getCode();
		List<ScheduleFareAutoOverrideDTO> fareOverrideDTOList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleFareAutoOverrideCacheDTO> scheduleFareOverrideCacheList = (List<ScheduleFareAutoOverrideCacheDTO>) element.getObjectValue();
			fareOverrideDTOList = bindFareAutoOverrideFromCacheObject(scheduleFareOverrideCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleFareAutoOverrideDAO fareOverrideDAO = new ScheduleFareAutoOverrideDAO();
			fareOverrideDTOList = fareOverrideDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule station Point Cache
			List<ScheduleFareAutoOverrideCacheDTO> scheduleFareOverrideCacheList = bindFareAutoOverrideToCacheObject(fareOverrideDTOList);
			element = new Element(key, scheduleFareOverrideCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		List<ScheduleFareAutoOverrideDTO> searchFareOverrides = tripStageFareService.getTripStageActiveFare(authDTO, scheduleDTO);
		if (searchFareOverrides != null && !searchFareOverrides.isEmpty()) {
			fareOverrideDTOList.addAll(searchFareOverrides);
		}

		// Validate all Fare Override
		for (Iterator<ScheduleFareAutoOverrideDTO> iterator = fareOverrideDTOList.iterator(); iterator.hasNext();) {
			ScheduleFareAutoOverrideDTO fareOverrideDTO = iterator.next();
			// common validations
			if (fareOverrideDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(fareOverrideDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(fareOverrideDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getDayOfWeek() != null && fareOverrideDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getDayOfWeek() != null && fareOverrideDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Group List
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !fareOverrideDTO.getGroupList().isEmpty() && existGroupInGroupList(fareOverrideDTO.getGroupList(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
				iterator.remove();
				continue;
			}

			// Exceptions and Override
			for (Iterator<ScheduleFareAutoOverrideDTO> overrideIterator = fareOverrideDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleFareAutoOverrideDTO fareAutoOverrideExceptionDTO = overrideIterator.next();
				if (!tripDate.gteq(new DateTime(fareAutoOverrideExceptionDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!tripDate.lteq(new DateTime(fareAutoOverrideExceptionDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (fareAutoOverrideExceptionDTO.getDayOfWeek() != null && fareAutoOverrideExceptionDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (fareAutoOverrideExceptionDTO.getDayOfWeek() != null && fareAutoOverrideExceptionDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}

				// Apply Exceptions
				if (fareAutoOverrideExceptionDTO.getFare().intValue() == -1) {
					if (fareAutoOverrideExceptionDTO.getGroupList().isEmpty() || existGroupInGroupList(fareAutoOverrideExceptionDTO.getGroupList(), authDTO.getGroup()) != null) {
						iterator.remove();
						break;
					}
				}
				else {
					// Apply Override
					if (!fareAutoOverrideExceptionDTO.getGroupList().isEmpty() && existGroupInGroupList(fareAutoOverrideExceptionDTO.getGroupList(), authDTO.getGroup()) == null) {
						overrideIterator.remove();
						continue;
					}
					fareOverrideDTO.setFare(fareAutoOverrideExceptionDTO.getFare());
					for (BusSeatTypeFareDTO busSeatTypeFareDTO : fareAutoOverrideExceptionDTO.getBusSeatTypeFare()) {
						for (BusSeatTypeEM busSeatType : fareOverrideDTO.getBusSeatType()) {
							if (busSeatType.getId() == busSeatTypeFareDTO.getBusSeatType().getId()) {
								fareOverrideDTO.setFare(busSeatTypeFareDTO.getFare());
								break;
							}
						}
					}

					if (fareOverrideDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
						overrideIterator.remove();
						continue;
					}

					fareOverrideDTO.setOverrideMinutes(fareAutoOverrideExceptionDTO.getOverrideMinutes());
					fareOverrideDTO.setRouteList(fareAutoOverrideExceptionDTO.getRouteList());
					fareOverrideDTO.setGroupList(fareAutoOverrideExceptionDTO.getGroupList());
					fareOverrideDTO.setActiveFrom(fareAutoOverrideExceptionDTO.getActiveFrom());
					fareOverrideDTO.setActiveTo(fareAutoOverrideExceptionDTO.getActiveTo());
				}
			}
		}
		return fareOverrideDTOList;
	}

	private List<ScheduleFareAutoOverrideDTO> getFareOverrideByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String key = CACHEKEY + scheduleDTO.getCode();
		List<ScheduleFareAutoOverrideDTO> fareOverrideDTOList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleFareAutoOverrideCacheDTO> scheduleFareOverrideCacheList = (List<ScheduleFareAutoOverrideCacheDTO>) element.getObjectValue();
			fareOverrideDTOList = bindFareAutoOverrideFromCacheObject(scheduleFareOverrideCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleFareAutoOverrideDAO fareAutoOverrideDAO = new ScheduleFareAutoOverrideDAO();
			fareOverrideDTOList = fareAutoOverrideDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule station Point Cache
			List<ScheduleFareAutoOverrideCacheDTO> scheduleFareOverrideCacheList = bindFareAutoOverrideToCacheObject(fareOverrideDTOList);
			element = new Element(key, scheduleFareOverrideCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		return fareOverrideDTOList;
	}

	private List<ScheduleFareAutoOverrideCacheDTO> bindFareAutoOverrideToCacheObject(List<ScheduleFareAutoOverrideDTO> ScheduleFareAutoOverrideDTOList) {
		List<ScheduleFareAutoOverrideCacheDTO> fareOverrideCacheList = new ArrayList<>();
		// copy to cache
		if (ScheduleFareAutoOverrideDTOList != null && !ScheduleFareAutoOverrideDTOList.isEmpty()) {
			for (ScheduleFareAutoOverrideDTO fareAutoOverrideDTO : ScheduleFareAutoOverrideDTOList) {
				ScheduleFareAutoOverrideCacheDTO autoFareOverrideCacheDTO = new ScheduleFareAutoOverrideCacheDTO();

				autoFareOverrideCacheDTO.setActiveFlag(fareAutoOverrideDTO.getActiveFlag());
				autoFareOverrideCacheDTO.setId(fareAutoOverrideDTO.getId());
				autoFareOverrideCacheDTO.setCode(fareAutoOverrideDTO.getCode());
				autoFareOverrideCacheDTO.setActiveFrom(fareAutoOverrideDTO.getActiveFrom());
				autoFareOverrideCacheDTO.setActiveTo(fareAutoOverrideDTO.getActiveTo());
				autoFareOverrideCacheDTO.setDayOfWeek(fareAutoOverrideDTO.getDayOfWeek());
				autoFareOverrideCacheDTO.setTag(fareAutoOverrideDTO.getTag());
				autoFareOverrideCacheDTO.setFareOverrideModeCode(fareAutoOverrideDTO.getFareOverrideMode().getCode());
				autoFareOverrideCacheDTO.setSeatTypeFareDetails(fareAutoOverrideDTO.getBusSeatTypeFareDetails());
				autoFareOverrideCacheDTO.setFare(fareAutoOverrideDTO.getFare());
				autoFareOverrideCacheDTO.setOverrideMinutes(fareAutoOverrideDTO.getOverrideMinutes());

				List<String> routes = new ArrayList<String>();
				for (RouteDTO routeDTO : fareAutoOverrideDTO.getRouteList()) {
					String route = routeDTO.getFromStation().getId() + "_" + routeDTO.getToStation().getId();
					routes.add(route);
				}
				List<Integer> groups = new ArrayList<Integer>();
				for (GroupDTO groupDTO : fareAutoOverrideDTO.getGroupList()) {
					groups.add(groupDTO.getId());
				}
				List<Integer> busSeatTypes = new ArrayList<Integer>();
				for (BusSeatTypeEM busSeatTypeEM : fareAutoOverrideDTO.getBusSeatType()) {
					busSeatTypes.add(busSeatTypeEM.getId());
				}

				autoFareOverrideCacheDTO.setRouteList(routes);
				autoFareOverrideCacheDTO.setGroupList(groups);
				autoFareOverrideCacheDTO.setBusSeatTypes(busSeatTypes);

				List<ScheduleFareAutoOverrideCacheDTO> overrideControlList = new ArrayList<>();
				for (ScheduleFareAutoOverrideDTO overrideFareOverrideDTO : fareAutoOverrideDTO.getOverrideList()) {
					ScheduleFareAutoOverrideCacheDTO cacheDTO = new ScheduleFareAutoOverrideCacheDTO();
					cacheDTO.setActiveFlag(overrideFareOverrideDTO.getActiveFlag());
					cacheDTO.setId(overrideFareOverrideDTO.getId());
					cacheDTO.setCode(overrideFareOverrideDTO.getCode());
					cacheDTO.setActiveFrom(overrideFareOverrideDTO.getActiveFrom());
					cacheDTO.setActiveTo(overrideFareOverrideDTO.getActiveTo());
					cacheDTO.setDayOfWeek(overrideFareOverrideDTO.getDayOfWeek());
					cacheDTO.setTag(overrideFareOverrideDTO.getTag());
					cacheDTO.setFareOverrideModeCode(overrideFareOverrideDTO.getFareOverrideMode().getCode());
					cacheDTO.setSeatTypeFareDetails(overrideFareOverrideDTO.getBusSeatTypeFareDetails());

					cacheDTO.setFare(overrideFareOverrideDTO.getFare());
					cacheDTO.setOverrideMinutes(overrideFareOverrideDTO.getOverrideMinutes());

					List<String> overrideRoutes = new ArrayList<String>();
					for (RouteDTO routeDTO : overrideFareOverrideDTO.getRouteList()) {
						String route = routeDTO.getFromStation().getId() + "_" + routeDTO.getToStation().getId();
						overrideRoutes.add(route);
					}
					List<Integer> overrideGroups = new ArrayList<Integer>();
					for (GroupDTO groupDTO : overrideFareOverrideDTO.getGroupList()) {
						overrideGroups.add(groupDTO.getId());
					}
					List<Integer> overrideBusSeatTypes = new ArrayList<Integer>();
					for (BusSeatTypeEM busSeatTypeEM : overrideFareOverrideDTO.getBusSeatType()) {
						overrideBusSeatTypes.add(busSeatTypeEM.getId());
					}
					cacheDTO.setRouteList(overrideRoutes);
					cacheDTO.setGroupList(overrideGroups);
					cacheDTO.setBusSeatTypes(overrideBusSeatTypes);

					overrideControlList.add(cacheDTO);
				}
				autoFareOverrideCacheDTO.setOverrideList(overrideControlList);
				fareOverrideCacheList.add(autoFareOverrideCacheDTO);
			}
		}
		return fareOverrideCacheList;
	}

	private List<ScheduleFareAutoOverrideDTO> bindFareAutoOverrideFromCacheObject(List<ScheduleFareAutoOverrideCacheDTO> fareAutoOverrideCacheDTOList) {
		List<ScheduleFareAutoOverrideDTO> ScheduleFareAutoOverrideDTOList = new ArrayList<>();
		if (fareAutoOverrideCacheDTOList != null && !fareAutoOverrideCacheDTOList.isEmpty()) {
			// copy from cache
			for (ScheduleFareAutoOverrideCacheDTO fareAutoCache : fareAutoOverrideCacheDTOList) {
				ScheduleFareAutoOverrideDTO autoOverrideDTO = new ScheduleFareAutoOverrideDTO();
				autoOverrideDTO.setCode(fareAutoCache.getCode());
				autoOverrideDTO.setActiveFrom(fareAutoCache.getActiveFrom());
				autoOverrideDTO.setActiveTo(fareAutoCache.getActiveTo());
				autoOverrideDTO.setDayOfWeek(fareAutoCache.getDayOfWeek());
				autoOverrideDTO.setActiveFlag(fareAutoCache.getActiveFlag());
				autoOverrideDTO.setFare(fareAutoCache.getFare());
				autoOverrideDTO.setOverrideMinutes(fareAutoCache.getOverrideMinutes());
				autoOverrideDTO.setTag(fareAutoCache.getTag());
				autoOverrideDTO.setFareOverrideMode(FareOverrideModeEM.getFareOverrideModeEM(fareAutoCache.getFareOverrideModeCode()));
				autoOverrideDTO.setBusSeatTypeFare(convertBusSeatTypeFareList(fareAutoCache.getSeatTypeFareDetails()));
				List<RouteDTO> routeList = new ArrayList<RouteDTO>();
				for (String route : fareAutoCache.getRouteList()) {
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

				List<GroupDTO> groupList = new ArrayList<>();
				for (Integer groupId : fareAutoCache.getGroupList()) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(groupId);
					groupList.add(groupDTO);
				}
				List<BusSeatTypeEM> busSeatTypes = new ArrayList<>();
				for (Integer busSeatTypeId : fareAutoCache.getBusSeatTypes()) {
					busSeatTypes.add(BusSeatTypeEM.getBusSeatTypeEM(busSeatTypeId));
				}
				autoOverrideDTO.setRouteList(routeList);
				autoOverrideDTO.setGroupList(groupList);
				autoOverrideDTO.setLookupCode(autoOverrideDTO.getLookupCode());
				autoOverrideDTO.setBusSeatType(busSeatTypes);

				List<ScheduleFareAutoOverrideDTO> overrideControlList = new ArrayList<>();
				if (fareAutoCache.getOverrideList() != null && !fareAutoCache.getOverrideList().isEmpty()) {
					for (ScheduleFareAutoOverrideCacheDTO overRideCacheDTO : fareAutoCache.getOverrideList()) {
						ScheduleFareAutoOverrideDTO overrideFareDTO = new ScheduleFareAutoOverrideDTO();
						overrideFareDTO.setCode(overRideCacheDTO.getCode());
						overrideFareDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideFareDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideFareDTO.setDayOfWeek(overRideCacheDTO.getDayOfWeek());
						overrideFareDTO.setTag(overRideCacheDTO.getTag());
						overrideFareDTO.setFareOverrideMode(FareOverrideModeEM.getFareOverrideModeEM(overRideCacheDTO.getFareOverrideModeCode()));
						overrideFareDTO.setBusSeatTypeFare(convertBusSeatTypeFareList(overRideCacheDTO.getSeatTypeFareDetails()));

						List<RouteDTO> overrideRouteList = new ArrayList<RouteDTO>();
						if (fareAutoCache.getRouteList() != null && !fareAutoCache.getRouteList().isEmpty()) {
							for (String route : overRideCacheDTO.getRouteList()) {
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

						List<GroupDTO> overrideGroupList = new ArrayList<>();
						for (Integer groupId : overRideCacheDTO.getGroupList()) {
							GroupDTO groupDTO = new GroupDTO();
							groupDTO.setId(groupId);
							overrideGroupList.add(groupDTO);
						}

						List<BusSeatTypeEM> overrideBusSeatTypes = new ArrayList<>();
						for (Integer busSeatTypeId : overRideCacheDTO.getBusSeatTypes()) {
							overrideBusSeatTypes.add(BusSeatTypeEM.getBusSeatTypeEM(busSeatTypeId));
						}

						overrideFareDTO.setRouteList(overrideRouteList);
						overrideFareDTO.setGroupList(overrideGroupList);
						overrideFareDTO.setBusSeatType(overrideBusSeatTypes);
						overrideFareDTO.setFare(overRideCacheDTO.getFare());
						overrideFareDTO.setOverrideMinutes(overRideCacheDTO.getOverrideMinutes());

						overrideControlList.add(overrideFareDTO);
					}
					autoOverrideDTO.setOverrideList(overrideControlList);
				}
				ScheduleFareAutoOverrideDTOList.add(autoOverrideDTO);
			}
		}
		return ScheduleFareAutoOverrideDTOList;
	}

	private boolean existStageInRouteList(List<RouteDTO> routeList, StationDTO fromStationDTO, StationDTO toStationDTO) {
		boolean status = false;
		// Route List
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
		// Route List
		for (GroupDTO group : groupList) {
			if (group.getId() != 0 && groupDTO.getId() != 0 && group.getId() == groupDTO.getId()) {
				existingGroup = group;
				break;
			}
		}
		return existingGroup;
	}

	public List<ScheduleFareAutoOverrideDTO> getTripScheduleDateRangeActiveFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime activeFromDate, DateTime activeToDate) {
		List<ScheduleFareAutoOverrideDTO> fareOverrideDTOList = getFareOverrideByScheduleId(authDTO, scheduleDTO);

		// Validate all Fare Override
		for (Iterator<ScheduleFareAutoOverrideDTO> iterator = fareOverrideDTOList.iterator(); iterator.hasNext();) {
			ScheduleFareAutoOverrideDTO fareOverrideDTO = iterator.next();
			// common validations
			if (fareOverrideDTO.getActiveFrom() == null || fareOverrideDTO.getActiveTo() == null) {
				iterator.remove();
				continue;
			}
			if (!activeFromDate.lteq(new DateTime(fareOverrideDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (!activeToDate.gteq(new DateTime(fareOverrideDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getDayOfWeek() != null && fareOverrideDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
				iterator.remove();
				continue;
			}

			// Exceptions and Override
			for (Iterator<ScheduleFareAutoOverrideDTO> overrideIterator = fareOverrideDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleFareAutoOverrideDTO fareAutoOverrideExceptionDTO = overrideIterator.next();

				if (!activeFromDate.gteq(new DateTime(fareAutoOverrideExceptionDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!activeToDate.lteq(new DateTime(fareAutoOverrideExceptionDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (fareAutoOverrideExceptionDTO.getDayOfWeek() != null && fareAutoOverrideExceptionDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
			}
		}
		return fareOverrideDTOList;
	}

	@Override
	public List<ScheduleFareAutoOverrideDTO> processTripScheduleActiveFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, List<ScheduleFareAutoOverrideDTO> fareOverrideDTOList, StationDTO fromStationDTO, StationDTO toStationDTO, Map<Integer, GroupDTO> groupMap, BusSeatTypeEM busSeatType) {
		List<ScheduleFareAutoOverrideDTO> finalOverrideList = new ArrayList<ScheduleFareAutoOverrideDTO>();

		// Validate all Fare Override
		for (ScheduleFareAutoOverrideDTO fareOverrideDTO : fareOverrideDTOList) {
			// common validations
			if (fareOverrideDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(fareOverrideDTO.getActiveFrom()))) {
				continue;
			}
			if (fareOverrideDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(fareOverrideDTO.getActiveTo()))) {
				continue;
			}
			if (fareOverrideDTO.getDayOfWeek() != null && fareOverrideDTO.getDayOfWeek().length() != 7) {
				continue;
			}
			if (fareOverrideDTO.getDayOfWeek() != null && fareOverrideDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
				continue;
			}
			// Group List
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !fareOverrideDTO.getGroupList().isEmpty() && existGroupInGroupList(fareOverrideDTO.getGroupList(), authDTO.getGroup()) == null) {
				continue;
			}
			if (fareOverrideDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
				continue;
			}

			// Route List
			if (!fareOverrideDTO.getRouteList().isEmpty() && !existStageInRouteList(fareOverrideDTO.getRouteList(), fromStationDTO, toStationDTO)) {
				continue;
			}
			BusSeatTypeEM busSeatTypeEM = BitsUtil.existBusSeatType(fareOverrideDTO.getBusSeatType(), busSeatType);
			if (busSeatTypeEM == null) {
				continue;
			}

			// Exceptions and Override
			for (Iterator<ScheduleFareAutoOverrideDTO> overrideIterator = fareOverrideDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleFareAutoOverrideDTO fareAutoOverrideExceptionDTO = overrideIterator.next();

				if (!scheduleDTO.getTripDate().gteq(new DateTime(fareAutoOverrideExceptionDTO.getActiveFrom()))) {
					continue;
				}
				if (!scheduleDTO.getTripDate().lteq(new DateTime(fareAutoOverrideExceptionDTO.getActiveTo()))) {
					continue;
				}
				if (fareAutoOverrideExceptionDTO.getDayOfWeek() != null && fareAutoOverrideExceptionDTO.getDayOfWeek().length() != 7) {
					continue;
				}
				if (fareAutoOverrideExceptionDTO.getDayOfWeek() != null && fareAutoOverrideExceptionDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					continue;
				}

				// Apply Exceptions
				if (fareAutoOverrideExceptionDTO.getFare().intValue() == -1) {
					if (fareAutoOverrideExceptionDTO.getRouteList().isEmpty() || existStageInRouteList(fareAutoOverrideExceptionDTO.getRouteList(), fromStationDTO, toStationDTO)) {
						break;
					}

					if (fareAutoOverrideExceptionDTO.getGroupList().isEmpty() || existGroupInGroupList(fareAutoOverrideExceptionDTO.getGroupList(), authDTO.getGroup()) != null) {
						break;
					}
				}
				else {
					if (!fareAutoOverrideExceptionDTO.getRouteList().isEmpty() && !existStageInRouteList(fareAutoOverrideExceptionDTO.getRouteList(), fromStationDTO, toStationDTO)) {
						continue;
					}
					if (!fareAutoOverrideExceptionDTO.getGroupList().isEmpty() && existGroupInGroupList(fareAutoOverrideExceptionDTO.getGroupList(), authDTO.getGroup()) == null) {
						continue;
					}
					// Apply Override
					fareOverrideDTO.setFare(fareAutoOverrideExceptionDTO.getFare());
					for (BusSeatTypeFareDTO busSeatTypeFareDTO : fareAutoOverrideExceptionDTO.getBusSeatTypeFare()) {
						for (BusSeatTypeEM busSeatType1 : fareOverrideDTO.getBusSeatType()) {
							if (busSeatType1.getId() == busSeatTypeFareDTO.getBusSeatType().getId()) {
								fareOverrideDTO.setFare(busSeatTypeFareDTO.getFare());
								break;
							}
						}
					}

					if (fareOverrideDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
						continue;
					}

					fareOverrideDTO.setOverrideMinutes(fareAutoOverrideExceptionDTO.getOverrideMinutes());
					fareOverrideDTO.setRouteList(fareAutoOverrideExceptionDTO.getRouteList());
					fareOverrideDTO.setGroupList(fareAutoOverrideExceptionDTO.getGroupList());
					fareOverrideDTO.setActiveFrom(fareAutoOverrideExceptionDTO.getActiveFrom());
					fareOverrideDTO.setActiveTo(fareAutoOverrideExceptionDTO.getActiveTo());
				}
			}
			finalOverrideList.add(fareOverrideDTO);
		}
		return finalOverrideList;
	}

	private List<BusSeatTypeFareDTO> convertBusSeatTypeFareList(String seatTypeFareDetails) {
		List<BusSeatTypeFareDTO> busSeatTypeFareList = new ArrayList<>();
		if (StringUtil.isNotNull(seatTypeFareDetails)) {
			List<String> busSeatTypefare = Arrays.asList(seatTypeFareDetails.split(Text.COMMA));
			for (String seatTypeFare : busSeatTypefare) {
				BusSeatTypeFareDTO busSeatTypeFareDTO = new BusSeatTypeFareDTO();
				busSeatTypeFareDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(Integer.parseInt(seatTypeFare.split(Text.COLON)[0])));
				busSeatTypeFareDTO.setFare(new BigDecimal(seatTypeFare.split(Text.COLON)[1]));
				busSeatTypeFareList.add(busSeatTypeFareDTO);
			}
		}
		return busSeatTypeFareList;
	}

	public void applyFareRule(AuthDTO authDTO, ScheduleFareAutoOverrideDTO fareAutoOverrideDTO) {
		/** Schedule Details */
		fareAutoOverrideDTO.setSchedule(scheduleService.getSchedule(authDTO, fareAutoOverrideDTO.getSchedule()));

		/** Schedule Bus */
		ScheduleBusDTO scheduleBusDTO = scheduleBusService.getByScheduleId(authDTO, fareAutoOverrideDTO.getSchedule());
		BusDTO busDTO = scheduleBusDTO.getBus();

		ScheduleBusOverrideDTO scheduleBusOverrideDTO = scheduleBusOverrideService.getScheduleBusOverride(authDTO, fareAutoOverrideDTO.getSchedule(), DateUtil.getDateTime(fareAutoOverrideDTO.getActiveFrom()), DateUtil.getDateTime(fareAutoOverrideDTO.getActiveTo()));
		if (scheduleBusOverrideDTO != null) {
			busDTO = scheduleBusOverrideDTO.getBus();
		}

		busDTO = busService.getBus(authDTO, busDTO);

		StringBuilder errorMessage = new StringBuilder();
		if (fareAutoOverrideDTO.getRouteList().isEmpty()) {
			List<ScheduleStageDTO> stages = schedulestageService.get(authDTO, fareAutoOverrideDTO.getSchedule());
			Map<String, ScheduleStageDTO> stagesMap = new HashMap<>();
			for (ScheduleStageDTO stage : stages) {
				stagesMap.put(stage.getFromStation().getCode() + "_" + stage.getToStation().getCode(), stage);
			}

			stages = new ArrayList<>(stagesMap.values());
			for (ScheduleStageDTO stage : stages) {
				validateRouteFare(authDTO, stage.getFromStation(), stage.getToStation(), fareAutoOverrideDTO, busDTO, errorMessage);
			}
		}
		else if (!fareAutoOverrideDTO.getRouteList().isEmpty()) {
			for (RouteDTO routeDTO : fareAutoOverrideDTO.getRouteList()) {
				validateRouteFare(authDTO, routeDTO.getFromStation(), routeDTO.getToStation(), fareAutoOverrideDTO, busDTO, errorMessage);
			}
		}

		if (StringUtil.isNotNull(errorMessage.toString())) {
			throw new ServiceException(ErrorCode.ROUTE_FARE_OUT_OF_RANGE, errorMessage.toString());
		}
	}

	private void validateRouteFare(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation, ScheduleFareAutoOverrideDTO fareAutoOverrideDTO, BusDTO busDTO, StringBuilder errorMessage) {
		FareRuleDetailsDTO fareRuleDetails = fareRuleService.getStageFareRuleDetails(authDTO, authDTO.getNamespace().getProfile().getFareRule(), fromStation, toStation);

		StageFareDTO stageFareDTO = new StageFareDTO();
		for (BusSeatTypeFareDTO busSeatTypeFare : fareAutoOverrideDTO.getBusSeatTypeFare()) {
			stageFareDTO.setBusSeatType(busSeatTypeFare.getBusSeatType());
			stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, busDTO, fareRuleDetails);

			if (fareRuleDetails.getId() != 0 && (stageFareDTO.getMinFare().intValue() != 0 && busSeatTypeFare.getFare().intValue() < stageFareDTO.getMinFare().intValue()) || (stageFareDTO.getMaxFare().intValue() != 0 && busSeatTypeFare.getFare().intValue() > stageFareDTO.getMaxFare().intValue())) {
				errorMessage.append(fromStation.getName() + " - " + toStation.getName() + " Fare excepted: (" + stageFareDTO.getMinFare() + " - " + stageFareDTO.getMaxFare() + ") but requested is: " + busSeatTypeFare.getFare() + Text.SINGLE_SPACE + Text.NEW_LINE + Text.SINGLE_SPACE);
				break;
			}
		}
	}
}
