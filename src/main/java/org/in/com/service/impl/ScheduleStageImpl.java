package org.in.com.service.impl;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Element;

import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleStageCacheDTO;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ScheduleStageDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatTypeFareDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleActivityService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.StationService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleStageImpl extends BaseImpl implements ScheduleStageService {
	String SEARCH_CACHEKEY = "SEC_STAGE_";
	@Autowired
	ScheduleActivityService scheduleMonitorService;
	@Autowired
	GroupService groupService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	StationService stationService;

	public List<ScheduleStageDTO> get(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleStageDAO dao = new ScheduleStageDAO();
		List<ScheduleStageDTO> list = dao.get(authDTO, scheduleDTO);

		Map<Integer, ScheduleStationDTO> stationMap = new HashMap<Integer, ScheduleStationDTO>();
		if (!list.isEmpty()) {
			List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, scheduleDTO);
			for (ScheduleStationDTO stationDTO : stationList) {
				stationMap.put(stationDTO.getStation().getId(), stationDTO);
			}
		}
		for (Iterator<ScheduleStageDTO> itrStage = list.iterator(); itrStage.hasNext();) {
			ScheduleStageDTO stageDTO = itrStage.next();
			if (stationMap.get(stageDTO.getFromStation().getId()) == null || stationMap.get(stageDTO.getToStation().getId()) == null) {
				itrStage.remove();
				continue;
			}
			stageDTO.setFromStation(getStationDTObyId(stageDTO.getFromStation()));
			stageDTO.setToStation(getStationDTObyId(stageDTO.getToStation()));
			// stageDTO.setBusSeatType(getBusSeatTypeDTO(stageDTO.getBusSeatType().getCode()));
			if (stageDTO.getGroup() != null) {
				stageDTO.setGroup(groupService.getGroup(authDTO, stageDTO.getGroup()));
			}
			for (ScheduleStageDTO overridestageDTO : stageDTO.getOverrideList()) {
				overridestageDTO.setFromStation(getStationDTObyId(overridestageDTO.getFromStation()));
				overridestageDTO.setToStation(getStationDTObyId(overridestageDTO.getToStation()));
				// stageDTO.setBusSeatType(getBusSeatTypeDTO(stageDTO.getBusSeatType().getCode()));
				if (overridestageDTO.getGroup() != null) {
					overridestageDTO.setGroup(groupService.getGroup(authDTO, overridestageDTO.getGroup()));
				}
			}
			stageDTO.setFromStationSequence(stationMap.get(stageDTO.getFromStation().getId()) != null ? stationMap.get(stageDTO.getFromStation().getId()).getStationSequence() : 0);
			stageDTO.setToStationSequence(stationMap.get(stageDTO.getToStation().getId()) != null ? stationMap.get(stageDTO.getToStation().getId()).getStationSequence() : 0);
		}
		return list;
	}

	public ScheduleStageDTO Update(AuthDTO authDTO, ScheduleStageDTO scheduleStageFareDTO) {
		ScheduleStageDAO dao = new ScheduleStageDAO();
		StringBuilder errors = new StringBuilder();
		for (ScheduleStageDTO fareDTO : scheduleStageFareDTO.getList()) {
			fareDTO.setFromStation(getStationDTO(fareDTO.getFromStation()));
			fareDTO.setToStation(getStationDTO(fareDTO.getToStation()));

			if (fareDTO.getActiveFlag() == Numeric.ONE_INT) {
				updateRoute(authDTO, fareDTO);
			}

			/** Route Minimum & Maximum Fare Validation */
			validateRouteFare(authDTO, fareDTO, errors);

			// Activity Activity Log
			scheduleMonitorService.scheduleStageActivity(authDTO, fareDTO);
		}
		if (StringUtil.isNotNull(errors.toString())) {
			throw new ServiceException(ErrorCode.ROUTE_FARE_OUT_OF_RANGE, errors.toString());
		}

		dao.getIUD(authDTO, scheduleStageFareDTO);
		ScheduleCache scheduleCache = new ScheduleCache();
		String key = authDTO.getNamespace().getCode() + Constants.NAMESPACE_ROUTE;
		EhcacheManager.getCommerceStaticEhCache().remove(key);
		scheduleCache.removeScheduleDTO(authDTO, scheduleStageFareDTO.getSchedule());
		removeSearchStage(authDTO, scheduleStageFareDTO.getList());
		return scheduleStageFareDTO;
	}

	private void removeSearchStage(AuthDTO authDTO, List<ScheduleStageDTO> stages) {
		for (ScheduleStageDTO fareDTO : stages) {
			EhcacheManager.getSearchStageCache().remove(SEARCH_CACHEKEY + authDTO.getNamespaceCode() + "_" + fareDTO.getFromStation().getId() + "_" + fareDTO.getToStation().getId());
			EhcacheManager.getSearchStageCache().remove(SEARCH_CACHEKEY + authDTO.getNamespaceCode() + "_0_" + fareDTO.getToStation().getId());
			EhcacheManager.getSearchStageCache().remove(SEARCH_CACHEKEY + authDTO.getNamespaceCode() + "_" + fareDTO.getFromStation().getId() + "_0");
			EhcacheManager.getSearchStageCache().remove(SEARCH_CACHEKEY + authDTO.getNamespaceCode() + "_0_0");
		}
	}

	public List<ScheduleStageDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO) {

		String CACHEKEY = "SHSTG" + scheduleDTO.getCode();
		List<ScheduleStageDTO> scheduleStageDTOList = null;
		List<ScheduleStageCacheDTO> scheduleStationCacheList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleStationCacheList = (List<ScheduleStageCacheDTO>) element.getObjectValue();
			scheduleStageDTOList = bindScheduleStageFromCacheObject(scheduleStationCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleStageDAO scheduleStageDAO = new ScheduleStageDAO();
			List<ScheduleStageDTO> scheduleStageList = scheduleStageDAO.getByScheduleId(authDTO, scheduleDTO);
			scheduleStageDTOList = getUniqueStageList(authDTO, scheduleStageList);

			// Save to schedule station Point Cache
			scheduleStationCacheList = bindScheduleStageToCacheObject(scheduleStageDTOList);
			element = new Element(CACHEKEY, scheduleStationCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		for (Iterator<ScheduleStageDTO> iterator = scheduleStageDTOList.iterator(); iterator.hasNext();) {
			ScheduleStageDTO scheduleStageDTO = iterator.next();
			if (scheduleStageDTO.getFromStation().getId() != fromStationDTO.getId() || scheduleStageDTO.getToStation().getId() != toStationDTO.getId()) {
				iterator.remove();
				continue;
			}
		}
		return scheduleStageDTOList;

	}

	public List<ScheduleDTO> getScheduleSearchStage(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation) {
		String CACHEKEY = SEARCH_CACHEKEY + authDTO.getNamespaceCode() + "_" + fromStation.getId() + "_" + toStation.getId();
		Element element = EhcacheManager.getSearchStageCache().get(CACHEKEY);
		List<ScheduleStageDTO> searchStageList = null;
		if (element != null) {
			List<ScheduleStageCacheDTO> searchStageCacheList = (List<ScheduleStageCacheDTO>) element.getObjectValue();
			searchStageList = bindScheduleStageFromCacheObject(searchStageCacheList);
		}
		else {
			ScheduleStageDAO stageDAO = new ScheduleStageDAO();
			List<ScheduleStageDTO> searchStages = stageDAO.getStageByStationID(authDTO, fromStation, toStation);
			searchStageList = getUniqueStageList(authDTO, searchStages);
			List<ScheduleStageCacheDTO> searchStageCacheList = bindScheduleStageToCacheObject(searchStageList);
			EhcacheManager.getSearchStageCache().put(new Element(CACHEKEY, searchStageCacheList));
		}

		Map<Integer, ScheduleDTO> scheduleGroupMap = new HashMap<>();
		for (Iterator<ScheduleStageDTO> itrStage = searchStageList.iterator(); itrStage.hasNext();) {
			ScheduleStageDTO scheduleStageDTO = itrStage.next();
			if (scheduleStageDTO.getFare() == 0) {
				itrStage.remove();
				continue;
			}

			// Group the Stage by schedule Id
			if (scheduleGroupMap.get(scheduleStageDTO.getSchedule().getId()) == null) {
				ScheduleDTO scheduleDTO = scheduleStageDTO.getSchedule();
				List<ScheduleStageDTO> scheduleStageList = new ArrayList<>();
				scheduleStageList.add(scheduleStageDTO);
				scheduleDTO.setScheduleStageList(scheduleStageList);
				scheduleGroupMap.put(scheduleStageDTO.getSchedule().getId(), scheduleDTO);
			}
			else {
				ScheduleDTO scheduleDTO = scheduleGroupMap.get(scheduleStageDTO.getSchedule().getId());
				scheduleDTO.getScheduleStageList().add(scheduleStageDTO);
				scheduleGroupMap.put(scheduleStageDTO.getSchedule().getId(), scheduleDTO);
			}
		}
		return new ArrayList<ScheduleDTO>(scheduleGroupMap.values());
	}

	public List<ScheduleStageDTO> getByScheduleTripDate(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate) {

		String CACHEKEY = "SHSTG" + scheduleDTO.getCode();
		List<ScheduleStageDTO> scheduleStageDTOList = null;
		List<ScheduleStageCacheDTO> scheduleStationCacheList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleStationCacheList = (List<ScheduleStageCacheDTO>) element.getObjectValue();
			scheduleStageDTOList = bindScheduleStageFromCacheObject(scheduleStationCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleStageDAO scheduleStageDAO = new ScheduleStageDAO();
			List<ScheduleStageDTO> scheduleStageList = scheduleStageDAO.getByScheduleId(authDTO, scheduleDTO);
			scheduleStageDTOList = getUniqueStageList(authDTO, scheduleStageList);

			// Save to schedule station Point Cache
			scheduleStationCacheList = bindScheduleStageToCacheObject(scheduleStageDTOList);
			element = new Element(CACHEKEY, scheduleStationCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		// Validate All stages
		for (Iterator<ScheduleStageDTO> iterator = scheduleStageDTOList.iterator(); iterator.hasNext();) {
			ScheduleStageDTO stageDTO = iterator.next();
			DateTime scheduleFromDate = new DateTime(stageDTO.getActiveFrom());
			DateTime scheduleEndDate = new DateTime(stageDTO.getActiveTo());

			if (!tripDate.lteq(scheduleEndDate)) {
				iterator.remove();
				continue;
			}
			if (tripDate.lt(scheduleFromDate)) {
				iterator.remove();
				continue;
			}
			if (stageDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Remove If fare is Zero
			if (stageDTO.getFare() == 0) {
				iterator.remove();
			}

			// Exceptions and Override
			for (Iterator<ScheduleStageDTO> overrideIterator = stageDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleStageDTO OverrideScheduleStageDTO = overrideIterator.next();
				if (!tripDate.gteq(new DateTime(OverrideScheduleStageDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!tripDate.lteq(new DateTime(OverrideScheduleStageDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (OverrideScheduleStageDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (OverrideScheduleStageDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}
				// If exceptions
				if (OverrideScheduleStageDTO.getFare() == -1) {
					iterator.remove();
					break;
				}
				// exceptions/Override for Group Level
				if (OverrideScheduleStageDTO.getGroup().getId() != 0 && OverrideScheduleStageDTO.getGroup().getId() != authDTO.getGroup().getId()) {
					overrideIterator.remove();
					continue;
				}
				// Apply Override
				stageDTO.setFare(OverrideScheduleStageDTO.getFare());
				for (BusSeatTypeFareDTO busSeatTypeFareDTO : OverrideScheduleStageDTO.getBusSeatTypeFare()) {
					if (stageDTO.getBusSeatType().getId() == busSeatTypeFareDTO.getBusSeatType().getId()) {
						stageDTO.setFare(busSeatTypeFareDTO.getFare().doubleValue());
						break;
					}
				}

				if (stageDTO.getFare() == 0) {
					overrideIterator.remove();
					continue;
				}
			}
			// Identify and set specific fare
			if (stageDTO.getOverrideList().size() >= 2) {
				ScheduleStageDTO recentScheduleStageDTO = null;
				for (ScheduleStageDTO scheduleStageDTO : stageDTO.getOverrideList()) {
					if (recentScheduleStageDTO == null) {
						recentScheduleStageDTO = scheduleStageDTO;
					}
					if (DateUtil.getDayDifferent(new DateTime(scheduleStageDTO.getActiveFrom()), new DateTime(scheduleStageDTO.getActiveTo())) <= DateUtil.getDayDifferent(new DateTime(recentScheduleStageDTO.getActiveFrom()), new DateTime(recentScheduleStageDTO.getActiveTo()))) {
						recentScheduleStageDTO = scheduleStageDTO;
					}
				}
				stageDTO.setFare(recentScheduleStageDTO.getFare());
			}
		}
		return scheduleStageDTOList;
	}

	public void removeScheduleSearchStageCache(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleStageDAO scheduleStageDAO = new ScheduleStageDAO();
		List<ScheduleStageDTO> scheduleStageDTOList = scheduleStageDAO.get(authDTO, scheduleDTO);

		removeSearchStage(authDTO, scheduleStageDTOList);
	}

	@Override
	public List<ScheduleStageDTO> getScheduleStageV2(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		Map<String, ScheduleStageDTO> scheduleStageMap = new HashMap<String, ScheduleStageDTO>();

		List<ScheduleStageDTO> list = get(authDTO, scheduleDTO);
		for (ScheduleStageDTO stageDTO : list) {
			String key = stageDTO.getFromStation().getId() + Text.UNDER_SCORE + stageDTO.getToStation().getId();

			if (scheduleStageMap.get(key) != null && stageDTO.getFare() != 0) {
				ScheduleStageDTO existStageDTO = scheduleStageMap.get(key);
				BusSeatTypeFareDTO busSeatTypeFare = new BusSeatTypeFareDTO();
				busSeatTypeFare.setBusSeatType(stageDTO.getBusSeatType());
				busSeatTypeFare.setFare(BigDecimal.valueOf(stageDTO.getFare()));
				existStageDTO.getBusSeatTypeFare().add(busSeatTypeFare);

				scheduleStageMap.put(key, existStageDTO);
			}
			else {
				if (stageDTO.getBusSeatTypeFare().isEmpty() && stageDTO.getFare() != 0) {
					BusSeatTypeFareDTO busSeatTypeFare = new BusSeatTypeFareDTO();
					busSeatTypeFare.setBusSeatType(stageDTO.getBusSeatType());
					busSeatTypeFare.setFare(BigDecimal.valueOf(stageDTO.getFare()));
					stageDTO.getBusSeatTypeFare().add(busSeatTypeFare);
				}
				scheduleStageMap.put(key, stageDTO);
			}

		}
		return new ArrayList<ScheduleStageDTO>(scheduleStageMap.values());

	}

	private List<ScheduleStageDTO> getUniqueStageList(AuthDTO authDTO, List<ScheduleStageDTO> stageList) {
		List<ScheduleStageDTO> uniqueStageList = new ArrayList<>();
		Map<String, ScheduleStageDTO> uniqueScheduleStage = new HashMap<>();
		ScheduleCache scheduleCache = new ScheduleCache();
		for (ScheduleStageDTO stageDTO : stageList) {
			ScheduleDTO schedule = scheduleCache.getScheduleDTObyId(authDTO, stageDTO.getSchedule());
			if (schedule.getActiveFlag() != 1 || !schedule.getPreRequrities().equals("000000") || DateUtil.getDateTime(schedule.getActiveTo()).getStartOfDay().lt(DateUtil.NOW().minusDays(Numeric.TWO_INT).getStartOfDay())) {
				continue;
			}
			String key = stageDTO.getSchedule().getId() + Text.UNDER_SCORE + stageDTO.getFromStation().getId() + Text.UNDER_SCORE + stageDTO.getToStation().getId() + Text.UNDER_SCORE + stageDTO.getGroup().getId() + Text.UNDER_SCORE + stageDTO.getBusSeatType().getCode() + Text.UNDER_SCORE + stageDTO.getLookupCode();
			if (uniqueScheduleStage.get(key) != null) {
				continue;
			}
			uniqueStageList.add(stageDTO);
			uniqueScheduleStage.put(key, stageDTO);
		}
		return uniqueStageList;
	}

	private List<ScheduleStageCacheDTO> bindScheduleStageToCacheObject(List<ScheduleStageDTO> stationDTOList) {
		List<ScheduleStageCacheDTO> scheduleControlCacheList = new ArrayList<>();
		// copy to cache
		if (stationDTOList != null && !stationDTOList.isEmpty()) {
			for (ScheduleStageDTO stageDTO : stationDTOList) {
				ScheduleStageCacheDTO stationCacheDTO = new ScheduleStageCacheDTO();
				stationCacheDTO.setActiveFlag(stageDTO.getActiveFlag());
				stationCacheDTO.setId(stageDTO.getId());
				// stationCacheDTO.setCode(stageDTO.getCode());
				stationCacheDTO.setActiveFrom(stageDTO.getActiveFrom());
				stationCacheDTO.setActiveTo(stageDTO.getActiveTo());
				stationCacheDTO.setDayOfWeek(stageDTO.getDayOfWeek());
				stationCacheDTO.setFare(stageDTO.getFare());
				if (stageDTO.getBusSeatType() != null) {
					stationCacheDTO.setBusSeatTypeCode(stageDTO.getBusSeatType().getCode());
				}
				if (stageDTO.getFromStation() != null) {
					stationCacheDTO.setFromStationId(stageDTO.getFromStation().getId());
				}
				if (stageDTO.getToStation() != null) {
					stationCacheDTO.setToStationId(stageDTO.getToStation().getId());
				}
				if (stageDTO.getGroup() != null) {
					stationCacheDTO.setGroupId(stageDTO.getGroup().getId());
				}
				stationCacheDTO.setScheduleId(stageDTO.getSchedule().getId());
				List<ScheduleStageCacheDTO> overrideControlList = new ArrayList<>();
				if (stageDTO.getOverrideList() != null && !stageDTO.getOverrideList().isEmpty()) {
					for (ScheduleStageDTO overrideStageDTO : stageDTO.getOverrideList()) {
						ScheduleStageCacheDTO cacheDTO = new ScheduleStageCacheDTO();
						cacheDTO.setActiveFlag(overrideStageDTO.getActiveFlag());
						cacheDTO.setId(overrideStageDTO.getId());
						// cacheDTO.setCode(overrideStageDTO.getCode());
						cacheDTO.setActiveFrom(overrideStageDTO.getActiveFrom());
						cacheDTO.setActiveTo(overrideStageDTO.getActiveTo());
						cacheDTO.setDayOfWeek(overrideStageDTO.getDayOfWeek());
						cacheDTO.setFare(overrideStageDTO.getFare());
						if (overrideStageDTO.getBusSeatType() != null) {
							cacheDTO.setBusSeatTypeCode(overrideStageDTO.getBusSeatType().getCode());
						}
						if (overrideStageDTO.getFromStation() != null) {
							cacheDTO.setFromStationId(overrideStageDTO.getFromStation().getId());
						}
						if (overrideStageDTO.getToStation() != null) {
							cacheDTO.setToStationId(overrideStageDTO.getToStation().getId());
						}
						if (overrideStageDTO.getGroup() != null) {
							cacheDTO.setGroupId(overrideStageDTO.getGroup().getId());
						}
						overrideControlList.add(cacheDTO);
					}
					stationCacheDTO.setOverrideList(overrideControlList);
				}
				scheduleControlCacheList.add(stationCacheDTO);
			}
		}
		return scheduleControlCacheList;
	}

	private List<ScheduleStageDTO> bindScheduleStageFromCacheObject(List<ScheduleStageCacheDTO> scheduleStationCacheList) {

		List<ScheduleStageDTO> scheduleStageDTOList = new ArrayList<>();
		if (scheduleStationCacheList != null && !scheduleStationCacheList.isEmpty()) {
			// copy from cache
			for (ScheduleStageCacheDTO stageCache : scheduleStationCacheList) {
				ScheduleStageDTO stageDTO = new ScheduleStageDTO();
				stageDTO.setId(stageCache.getId());
				// stageDTO.setCode(stageCache.getCode());
				stageDTO.setActiveFlag(stageCache.getActiveFlag());
				stageDTO.setActiveFrom(stageCache.getActiveFrom());
				stageDTO.setActiveTo(stageCache.getActiveTo());
				stageDTO.setDayOfWeek(stageCache.getDayOfWeek());
				stageDTO.setFare(stageCache.getFare());
				{
					StationDTO fromStationDTO = new StationDTO();
					fromStationDTO.setId(stageCache.getFromStationId());
					stageDTO.setFromStation(fromStationDTO);

					StationDTO toStationDTO = new StationDTO();
					toStationDTO.setId(stageCache.getToStationId());
					stageDTO.setToStation(toStationDTO);

					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(stageCache.getGroupId());
					stageDTO.setGroup(groupDTO);

					stageDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(stageCache.getBusSeatTypeCode()));

					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setId(stageCache.getScheduleId());
					stageDTO.setSchedule(scheduleDTO);
				}
				List<ScheduleStageDTO> overrideControlList = new ArrayList<>();
				if (stageCache.getOverrideList() != null && !stageCache.getOverrideList().isEmpty()) {
					for (ScheduleStageCacheDTO overRideCacheDTO : stageCache.getOverrideList()) {
						ScheduleStageDTO overrideStageDTO = new ScheduleStageDTO();
						overrideStageDTO.setId(overRideCacheDTO.getId());
						// overrideStageDTO.setCode(overRideCacheDTO.getCode());
						overrideStageDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideStageDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideStageDTO.setDayOfWeek(overRideCacheDTO.getDayOfWeek());
						overrideStageDTO.setFare(overRideCacheDTO.getFare());
						{
							StationDTO fromStationDTO = new StationDTO();
							fromStationDTO.setId(overRideCacheDTO.getFromStationId());
							overrideStageDTO.setFromStation(fromStationDTO);
							StationDTO toStationDTO = new StationDTO();
							toStationDTO.setId(overRideCacheDTO.getToStationId());
							overrideStageDTO.setToStation(toStationDTO);
							GroupDTO groupDTO = new GroupDTO();
							groupDTO.setId(overRideCacheDTO.getId());
							overrideStageDTO.setGroup(groupDTO);
							stageDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(stageCache.getBusSeatTypeCode()));
						}
						overrideControlList.add(overrideStageDTO);
					}
					stageDTO.setOverrideList(overrideControlList);
				}
				scheduleStageDTOList.add(stageDTO);
			}
		}
		return scheduleStageDTOList;
	}

	private void updateRoute(AuthDTO authDTO, ScheduleStageDTO fareDTO) {
		try {
			RouteDTO routeDTO = new RouteDTO();

			StationDTO fromStation = new StationDTO();
			fromStation.setCode(fareDTO.getFromStation().getCode());
			routeDTO.setFromStation(fromStation);

			StationDTO toStation = new StationDTO();
			toStation.setCode(fareDTO.getToStation().getCode());
			routeDTO.setToStation(toStation);

			routeDTO.setMinFare(150);
			routeDTO.setMaxFare(10000);
			routeDTO.setActiveFlag(Numeric.ONE_INT);

			stationService.updateRoute(authDTO, routeDTO);

			/** Activate Routes */
			List<RouteDTO> routes = new ArrayList<>();
			routes.add(routeDTO);
			stationService.updateRouteStatus(authDTO, routes, 1);
		}
		catch (Exception e) {

		}
	}

	private void validateRouteFare(AuthDTO authDTO, ScheduleStageDTO fareDTO, StringBuilder errors) {
		String error = Text.EMPTY;
		RouteDTO routeDTO = stationService.getRouteDTO(authDTO, fareDTO.getFromStation(), fareDTO.getToStation());

		if (routeDTO != null) {
			if (fareDTO.getBusSeatTypeFare() != null && !fareDTO.getBusSeatTypeFare().isEmpty()) {
				for (BusSeatTypeFareDTO seatTypeFare : fareDTO.getBusSeatTypeFare()) {
					if (seatTypeFare.getFare().compareTo(BigDecimal.valueOf(routeDTO.getMinFare())) < 0 || seatTypeFare.getFare().compareTo(BigDecimal.valueOf(routeDTO.getMaxFare())) > 0) {
						error = fareDTO.getFromStation().getName() + " to " + fareDTO.getToStation().getName() + ", Expected fare is " + routeDTO.getMinFare() + " - " + routeDTO.getMaxFare() + ". But given " + seatTypeFare.getFare();
						errors.append(error);
						errors.append(Text.VERTICAL_BAR);
					}
				}
			}
			else if (fareDTO.getFare() < routeDTO.getMinFare() || fareDTO.getFare() > routeDTO.getMaxFare()) {
				error = fareDTO.getFromStation().getName() + " to " + fareDTO.getToStation().getName() + ", Expected fare is " + routeDTO.getMinFare() + " - " + routeDTO.getMaxFare() + ". But given " + fareDTO.getFare();
				errors.append(error);
				errors.append(Text.VERTICAL_BAR);
			}
		}
	}
}
