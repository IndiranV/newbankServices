package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.in.com.cache.BusCache;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleSeatFareCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.dao.ScheduleSeatFareDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.FareOverrideTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleSeatFareService;
import org.in.com.service.ScheduleTripService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

@Service
public class ScheduleSeatFareImpl extends CacheCentral implements ScheduleSeatFareService {
	@Autowired
	GroupService groupService;
	@Autowired
	ScheduleTripService scheduleTripService;
	private static String CACHEKEY = "SHSTFR";

	public List<ScheduleSeatFareDTO> get(AuthDTO authDTO, ScheduleSeatFareDTO dto) {
		ScheduleSeatFareDAO dao = new ScheduleSeatFareDAO();
		List<ScheduleSeatFareDTO> list = dao.get(authDTO, dto);
		BusCache cache = new BusCache();
		BusDTO busDTO = null;

		for (ScheduleSeatFareDTO controlDTO : list) {
			for (GroupDTO groupDTO : controlDTO.getGroups()) {
				GroupDTO groupCache = groupService.getGroup(authDTO, groupDTO);
				groupDTO.setCode(groupCache.getCode());
				groupDTO.setName(groupCache.getName());
				groupDTO.setLevel(groupCache.getLevel());
			}

			for (ScheduleSeatFareDTO overrideControlDTO : controlDTO.getOverrideList()) {
				for (GroupDTO groupDTO : overrideControlDTO.getGroups()) {
					GroupDTO groupCache = groupService.getGroup(authDTO, groupDTO);
					groupDTO.setCode(groupCache.getCode());
					groupDTO.setName(groupCache.getName());
					groupDTO.setLevel(groupCache.getLevel());
				}
			}
			for (RouteDTO routeDTO : controlDTO.getRoutes()) {
				routeDTO.setFromStation(getStationDTObyId(routeDTO.getFromStation()));
				routeDTO.setToStation(getStationDTObyId(routeDTO.getToStation()));
			}
			if (busDTO == null || busDTO.getId() != controlDTO.getBus().getId()) {
				busDTO = new BusDTO();
				busDTO.setId(controlDTO.getBus().getId());
				busDTO = cache.getBusDTObyId(authDTO, busDTO);
				controlDTO.getBus().setDisplayName(busDTO.getDisplayName());
				controlDTO.getBus().setCategoryCode(busDTO.getCategoryCode());
				controlDTO.getBus().setName(busDTO.getName());
				controlDTO.getBus().setCode(busDTO.getCode());
			}
			controlDTO.getBus().getBusSeatLayoutDTO().setList(filterByCode(busDTO.getBusSeatLayoutDTO().getList(), controlDTO.getBus().getBusSeatLayoutDTO().getList()));

		}
		return list;
	}

	public ScheduleSeatFareDTO Update(AuthDTO authDTO, ScheduleSeatFareDTO dto) {
		ScheduleSeatFareDAO dao = new ScheduleSeatFareDAO();

		for (RouteDTO routeDTO : dto.getRoutes()) {
			routeDTO.setFromStation(getStationDTO(routeDTO.getFromStation()));
			routeDTO.setToStation(getStationDTO(routeDTO.getToStation()));
		}
		for (GroupDTO groupDTO : dto.getGroups()) {
			GroupDTO groupCache = groupService.getGroup(authDTO, groupDTO);
			groupDTO.setId(groupCache.getId());
		}

		if (dto.getActiveFlag() == 1 && authDTO.getNamespace().getProfile().getFareRule() != null && !authDTO.getNamespace().getProfile().getFareRule().isEmpty() && authDTO.getNamespace().getProfile().isFareRuleExceptionGroupEnabled(dto.getGroups())) {
			scheduleTripService.validateSeatFareWithFareRule(authDTO, dto);
		}
		dao.getIUD(authDTO, dto);
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.removeScheduleDTO(authDTO, dto.getSchedule());
		return dto;
	}

	@Override
	public List<ScheduleSeatFareDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO) {
		String key = CACHEKEY + scheduleDTO.getCode();
		List<ScheduleSeatFareDTO> seatVisibilityDTOList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleSeatFareCacheDTO> scheduleSeatVisibilityCacheList = (List<ScheduleSeatFareCacheDTO>) element.getObjectValue();
			seatVisibilityDTOList = bindSeatVisibilityFromCacheObject(scheduleSeatVisibilityCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleSeatFareDAO seatFareDAO = new ScheduleSeatFareDAO();
			seatVisibilityDTOList = seatFareDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule station Point Cache
			List<ScheduleSeatFareCacheDTO> scheduleSeatVisibilityCacheList = bindSeatVisibilityToCacheObject(seatVisibilityDTOList);
			element = new Element(key, scheduleSeatVisibilityCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		// Validate all Seat visibility
		for (Iterator<ScheduleSeatFareDTO> iterator = seatVisibilityDTOList.iterator(); iterator.hasNext();) {
			ScheduleSeatFareDTO seatFareDTO = iterator.next();
			// common validations
			if (seatFareDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(seatFareDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (seatFareDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(seatFareDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (seatFareDTO.getDayOfWeek() != null && seatFareDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (seatFareDTO.getDayOfWeek() != null && seatFareDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Group List
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !seatFareDTO.getGroups().isEmpty() && existGroupInGroupList(seatFareDTO.getGroups(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}

			// Route List
			if (!seatFareDTO.getRoutes().isEmpty() && !existStageInRouteList(seatFareDTO.getRoutes(), fromStationDTO, toStationDTO)) {
				iterator.remove();
				continue;
			}

			// Exceptions and Override
			for (Iterator<ScheduleSeatFareDTO> overrideIterator = seatFareDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleSeatFareDTO overrideSeatVisibilityDTO = overrideIterator.next();
				if (!scheduleDTO.getTripDate().gteq(new DateTime(overrideSeatVisibilityDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!scheduleDTO.getTripDate().lteq(new DateTime(overrideSeatVisibilityDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideSeatVisibilityDTO.getDayOfWeek() != null && overrideSeatVisibilityDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideSeatVisibilityDTO.getDayOfWeek() != null && overrideSeatVisibilityDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}
				// Apply Exceptions
				if (overrideSeatVisibilityDTO.getBus() == null || overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO() == null || overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList() == null || overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList().isEmpty()) {
					iterator.remove();
					break;
				}
				else {
					// Apply Override
					seatFareDTO.setBus(overrideSeatVisibilityDTO.getBus());
				}
			}
		}

		// Identify and remove the generic fare
		if (seatVisibilityDTOList.size() >= 2) {

			Map<String, ScheduleSeatFareDTO> routeSeatFareMap = new HashMap<String, ScheduleSeatFareDTO>();
			Map<String, ScheduleSeatFareDTO> groupSeatFareMap = new HashMap<String, ScheduleSeatFareDTO>();
			for (ScheduleSeatFareDTO autoOverrideDTO : seatVisibilityDTOList) {
				if (!autoOverrideDTO.getRoutes().isEmpty()) {
					for (BusSeatLayoutDTO layoutDTO : autoOverrideDTO.getBus().getBusSeatLayoutDTO().getList()) {
						routeSeatFareMap.put(layoutDTO.getCode(), autoOverrideDTO);
					}
				}
				else if (!autoOverrideDTO.getGroups().isEmpty()) {
					for (BusSeatLayoutDTO layoutDTO : autoOverrideDTO.getBus().getBusSeatLayoutDTO().getList()) {
						groupSeatFareMap.put(layoutDTO.getCode(), autoOverrideDTO);
					}
				}
			}
			for (Iterator<ScheduleSeatFareDTO> iterator = seatVisibilityDTOList.iterator(); iterator.hasNext();) {
				ScheduleSeatFareDTO seatFareDTO = iterator.next();

				for (Iterator<BusSeatLayoutDTO> seatIterator = seatFareDTO.getBus().getBusSeatLayoutDTO().getList().iterator(); seatIterator.hasNext();) {
					BusSeatLayoutDTO layoutDTO = seatIterator.next();
					if (seatFareDTO.getRouteStationList().isEmpty() && routeSeatFareMap.get(layoutDTO.getCode()) != null) {
						seatIterator.remove();
						continue;
					}
					else if (seatFareDTO.getGroups().isEmpty() && groupSeatFareMap.get(layoutDTO.getCode()) != null) {
						seatIterator.remove();
						continue;
					}
				}
			}

		}
		return seatVisibilityDTOList;
	}

	public List<ScheduleSeatFareDTO> getActiveScheduleSeatFare(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String key = CACHEKEY + scheduleDTO.getCode();
		List<ScheduleSeatFareDTO> seatVisibilityDTOList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleSeatFareCacheDTO> scheduleSeatVisibilityCacheList = (List<ScheduleSeatFareCacheDTO>) element.getObjectValue();
			seatVisibilityDTOList = bindSeatVisibilityFromCacheObject(scheduleSeatVisibilityCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleSeatFareDAO seatFareDAO = new ScheduleSeatFareDAO();
			seatVisibilityDTOList = seatFareDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule station Point Cache
			List<ScheduleSeatFareCacheDTO> scheduleSeatVisibilityCacheList = bindSeatVisibilityToCacheObject(seatVisibilityDTOList);
			element = new Element(key, scheduleSeatVisibilityCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		// Validate all Seat visibility
		for (Iterator<ScheduleSeatFareDTO> iterator = seatVisibilityDTOList.iterator(); iterator.hasNext();) {
			ScheduleSeatFareDTO seatFareDTO = iterator.next();
			// common validations
			if (seatFareDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(seatFareDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (seatFareDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(seatFareDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (seatFareDTO.getDayOfWeek() != null && seatFareDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (seatFareDTO.getDayOfWeek() != null && seatFareDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Group List
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !seatFareDTO.getGroups().isEmpty() && existGroupInGroupList(seatFareDTO.getGroups(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}

			// Exceptions and Override
			for (Iterator<ScheduleSeatFareDTO> overrideIterator = seatFareDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleSeatFareDTO overrideSeatVisibilityDTO = overrideIterator.next();
				if (!scheduleDTO.getTripDate().gteq(new DateTime(overrideSeatVisibilityDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!scheduleDTO.getTripDate().lteq(new DateTime(overrideSeatVisibilityDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideSeatVisibilityDTO.getDayOfWeek() != null && overrideSeatVisibilityDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideSeatVisibilityDTO.getDayOfWeek() != null && overrideSeatVisibilityDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}
				// Apply Exceptions
				if (overrideSeatVisibilityDTO.getBus() == null || overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO() == null || overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList() == null || overrideSeatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList().isEmpty()) {
					iterator.remove();
					break;
				}
				else {
					// Apply Override
					seatFareDTO.setBus(overrideSeatVisibilityDTO.getBus());
				}
			}
		}
		return seatVisibilityDTOList;
	}

	@Override
	public List<ScheduleSeatFareDTO> processScheduleSeatFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO, List<ScheduleSeatFareDTO> activeScheduleSeatFare) {
		List<ScheduleSeatFareDTO> scheduleSeatFareList = new ArrayList<ScheduleSeatFareDTO>();
		// Validate all Seat visibility
		for (ScheduleSeatFareDTO seatFareDTO : activeScheduleSeatFare) {
			// Route List
			if (!seatFareDTO.getRoutes().isEmpty() && !existStageInRouteList(seatFareDTO.getRoutes(), fromStationDTO, toStationDTO)) {
				continue;
			}
			scheduleSeatFareList.add(seatFareDTO);
		}

		// Identify and remove the generic fare
		if (scheduleSeatFareList.size() >= 2) {

			Map<String, ScheduleSeatFareDTO> routeSeatFareMap = new HashMap<String, ScheduleSeatFareDTO>();
			Map<String, ScheduleSeatFareDTO> groupSeatFareMap = new HashMap<String, ScheduleSeatFareDTO>();
			for (ScheduleSeatFareDTO autoOverrideDTO : scheduleSeatFareList) {
				if (!autoOverrideDTO.getRoutes().isEmpty()) {
					for (BusSeatLayoutDTO layoutDTO : autoOverrideDTO.getBus().getBusSeatLayoutDTO().getList()) {
						routeSeatFareMap.put(layoutDTO.getCode(), autoOverrideDTO);
					}
				}
				else if (!autoOverrideDTO.getGroups().isEmpty()) {
					for (BusSeatLayoutDTO layoutDTO : autoOverrideDTO.getBus().getBusSeatLayoutDTO().getList()) {
						groupSeatFareMap.put(layoutDTO.getCode(), autoOverrideDTO);
					}
				}
			}
			for (Iterator<ScheduleSeatFareDTO> iterator = scheduleSeatFareList.iterator(); iterator.hasNext();) {
				ScheduleSeatFareDTO seatFareDTO = iterator.next();

				for (Iterator<BusSeatLayoutDTO> seatIterator = seatFareDTO.getBus().getBusSeatLayoutDTO().getList().iterator(); seatIterator.hasNext();) {
					BusSeatLayoutDTO layoutDTO = seatIterator.next();
					if (seatFareDTO.getRouteStationList().isEmpty() && routeSeatFareMap.get(layoutDTO.getCode()) != null) {
						seatIterator.remove();
						continue;
					}
					else if (seatFareDTO.getGroups().isEmpty() && groupSeatFareMap.get(layoutDTO.getCode()) != null) {
						seatIterator.remove();
						continue;
					}
				}
			}

		}
		return scheduleSeatFareList;
	}

	private List<ScheduleSeatFareCacheDTO> bindSeatVisibilityToCacheObject(List<ScheduleSeatFareDTO> scheduleDiscountDTOList) {
		List<ScheduleSeatFareCacheDTO> seatVisibilityCacheList = new ArrayList<>();
		// copy to cache
		if (scheduleDiscountDTOList != null && !scheduleDiscountDTOList.isEmpty()) {
			for (ScheduleSeatFareDTO seatFareDTO : scheduleDiscountDTOList) {
				ScheduleSeatFareCacheDTO seatFareCacheDTO = new ScheduleSeatFareCacheDTO();
				seatFareCacheDTO.setCode(seatFareDTO.getCode());
				seatFareCacheDTO.setActiveFrom(seatFareDTO.getActiveFrom());
				seatFareCacheDTO.setActiveTo(seatFareDTO.getActiveTo());
				seatFareCacheDTO.setDayOfWeek(seatFareDTO.getDayOfWeek());
				seatFareCacheDTO.setSeatFare(seatFareDTO.getSeatFare());
				seatFareCacheDTO.setFareOverrideTypeCode(seatFareDTO.getFareOverrideType().getCode());
				seatFareCacheDTO.setFareTypeCode(seatFareDTO.getFareType().getCode());
				List<String> routes = new ArrayList<String>();
				for (RouteDTO routeDTO : seatFareDTO.getRoutes()) {
					String route = routeDTO.getFromStation().getId() + "_" + routeDTO.getToStation().getId();
					routes.add(route);
				}
				List<Integer> groups = new ArrayList<Integer>();
				for (GroupDTO groupDTO : seatFareDTO.getGroups()) {
					groups.add(groupDTO.getId());
				}
				seatFareCacheDTO.setRouteList(routes);
				seatFareCacheDTO.setGroupList(groups);

				StringBuilder seatCodes = new StringBuilder();
				if (seatFareDTO.getBus() != null && seatFareDTO.getBus().getBusSeatLayoutDTO() != null && seatFareDTO.getBus().getBusSeatLayoutDTO().getList() != null) {
					for (BusSeatLayoutDTO layoutDTO : seatFareDTO.getBus().getBusSeatLayoutDTO().getList()) {
						if (seatCodes.length() > 0) {
							seatCodes.append(",");
						}
						seatCodes.append(layoutDTO.getCode());
					}
				}
				seatFareCacheDTO.setSeatCodeList(seatCodes.toString());
				seatFareCacheDTO.setBusId(seatFareDTO.getBus().getId());

				List<ScheduleSeatFareCacheDTO> overrideControlList = new ArrayList<>();
				if (seatFareDTO.getOverrideList() != null && !seatFareDTO.getOverrideList().isEmpty()) {
					for (ScheduleSeatFareDTO seatFareExceptionDTO : seatFareDTO.getOverrideList()) {
						ScheduleSeatFareCacheDTO cacheDTO = new ScheduleSeatFareCacheDTO();
						cacheDTO.setCode(seatFareExceptionDTO.getCode());
						cacheDTO.setActiveFrom(seatFareExceptionDTO.getActiveFrom());
						cacheDTO.setActiveTo(seatFareExceptionDTO.getActiveTo());
						cacheDTO.setDayOfWeek(seatFareExceptionDTO.getDayOfWeek());
						cacheDTO.setSeatFare(seatFareExceptionDTO.getSeatFare());

						List<String> overrideRoutes = new ArrayList<String>();
						for (RouteDTO routeDTO : seatFareExceptionDTO.getRoutes()) {
							String route = routeDTO.getFromStation().getId() + "_" + routeDTO.getToStation().getId();
							overrideRoutes.add(route);
						}
						List<Integer> overrideGroups = new ArrayList<Integer>();
						for (GroupDTO groupDTO : seatFareExceptionDTO.getGroups()) {
							overrideGroups.add(groupDTO.getId());
						}
						cacheDTO.setRouteList(overrideRoutes);
						cacheDTO.setGroupList(overrideGroups);

						StringBuilder orverrideSeatCodes = new StringBuilder();
						if (seatFareExceptionDTO.getBus() != null && seatFareExceptionDTO.getBus().getBusSeatLayoutDTO() != null && seatFareExceptionDTO.getBus().getBusSeatLayoutDTO().getList() != null) {
							for (BusSeatLayoutDTO layoutDTO : seatFareExceptionDTO.getBus().getBusSeatLayoutDTO().getList()) {
								if (orverrideSeatCodes.length() > 0) {
									orverrideSeatCodes.append(",");
								}
								orverrideSeatCodes.append(layoutDTO.getCode());
							}
						}
						cacheDTO.setSeatCodeList(orverrideSeatCodes.toString());
						cacheDTO.setBusId(seatFareDTO.getBus().getId());

						overrideControlList.add(cacheDTO);
					}
					seatFareCacheDTO.setOverrideList(overrideControlList);
				}
				seatVisibilityCacheList.add(seatFareCacheDTO);
			}
		}
		return seatVisibilityCacheList;
	}

	private List<ScheduleSeatFareDTO> bindSeatVisibilityFromCacheObject(List<ScheduleSeatFareCacheDTO> seatFareCacheDTOList) {
		List<ScheduleSeatFareDTO> ScheduleSeatFareDTOList = new ArrayList<>();
		if (seatFareCacheDTOList != null && !seatFareCacheDTOList.isEmpty()) {
			// copy from cache
			for (ScheduleSeatFareCacheDTO seatFareCache : seatFareCacheDTOList) {
				ScheduleSeatFareDTO seatFareDTO = new ScheduleSeatFareDTO();
				seatFareDTO.setCode(seatFareCache.getCode());
				seatFareDTO.setActiveFrom(seatFareCache.getActiveFrom());
				seatFareDTO.setActiveTo(seatFareCache.getActiveTo());
				seatFareDTO.setDayOfWeek(seatFareCache.getDayOfWeek());
				seatFareDTO.setFareOverrideType(FareOverrideTypeEM.getFareOverrideTypeEM(seatFareCache.getFareOverrideTypeCode()));
				seatFareDTO.setFareType(FareTypeEM.getFareTypeEM(seatFareCache.getFareTypeCode()));
				seatFareDTO.setSeatFare(seatFareCache.getSeatFare());
				seatFareDTO.setActiveFlag(Numeric.ONE_INT);

				List<RouteDTO> routeList = new ArrayList<RouteDTO>();
				for (String route : seatFareCache.getRouteList()) {
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
				for (Integer groupId : seatFareCache.getGroupList()) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(groupId);
					groupList.add(groupDTO);
				}
				seatFareDTO.setRoutes(routeList);
				seatFareDTO.setGroups(groupList);

				if (seatFareCache.getBusId() != 0) {
					BusDTO busDTO = new BusDTO();
					busDTO.setId(seatFareCache.getBusId());

					List<BusSeatLayoutDTO> seatlist = new ArrayList<>();
					if (StringUtil.isNotNull(seatFareCache.getSeatCodeList())) {
						String[] seatCodes = seatFareCache.getSeatCodeList().split(",");
						for (String seatCode : seatCodes) {
							if (StringUtil.isNotNull(seatCode)) {
								BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
								busSeatTypeDTO.setCode(seatCode);
								seatlist.add(busSeatTypeDTO);
							}
						}
					}
					BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
					busSeatTypeDTO.setList(seatlist);
					busDTO.setBusSeatLayoutDTO(busSeatTypeDTO);
					seatFareDTO.setBus(busDTO);
				}
				seatFareDTO.setLookupCode(seatFareDTO.getLookupCode());
				List<ScheduleSeatFareDTO> overrideControlList = new ArrayList<>();
				if (seatFareCache.getOverrideList() != null && !seatFareCache.getOverrideList().isEmpty()) {
					for (ScheduleSeatFareCacheDTO overRideCacheDTO : seatFareCache.getOverrideList()) {
						ScheduleSeatFareDTO overrideControlDTO = new ScheduleSeatFareDTO();
						overrideControlDTO.setCode(overRideCacheDTO.getCode());
						overrideControlDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideControlDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideControlDTO.setSeatFare(overRideCacheDTO.getSeatFare());
						overrideControlDTO.setActiveFlag(Numeric.ONE_INT);

						List<RouteDTO> overrideRouteList = new ArrayList<RouteDTO>();
						if (overRideCacheDTO.getRouteList() != null && !overRideCacheDTO.getRouteList().isEmpty()) {
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
						overrideControlDTO.setRoutes(overrideRouteList);
						overrideControlDTO.setGroups(overrideGroupList);

						if (overRideCacheDTO.getBusId() != 0) {
							BusDTO busDTO = new BusDTO();
							busDTO.setId(overRideCacheDTO.getBusId());

							List<BusSeatLayoutDTO> seatlist = new ArrayList<>();
							if (StringUtil.isNotNull(overRideCacheDTO.getSeatCodeList())) {
								String[] seatCodes = overRideCacheDTO.getSeatCodeList().split(",");
								for (String seatCode : seatCodes) {
									if (StringUtil.isNotNull(seatCode)) {
										BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
										busSeatTypeDTO.setCode(seatCode);
										seatlist.add(busSeatTypeDTO);
									}
								}
							}
							BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
							busSeatTypeDTO.setList(seatlist);
							busDTO.setBusSeatLayoutDTO(busSeatTypeDTO);
							overrideControlDTO.setBus(busDTO);
						}

						overrideControlList.add(overrideControlDTO);
					}
					seatFareDTO.setOverrideList(overrideControlList);
				}
				ScheduleSeatFareDTOList.add(seatFareDTO);
			}
		}
		return ScheduleSeatFareDTOList;
	}

	private List<BusSeatLayoutDTO> filterByCode(List<BusSeatLayoutDTO> Orglist, List<BusSeatLayoutDTO> fillerlist) {
		List<BusSeatLayoutDTO> list = new ArrayList<>();
		for (BusSeatLayoutDTO fitterdto : fillerlist) {
			for (BusSeatLayoutDTO dto : Orglist) {
				if (dto.getCode().equals(fitterdto.getCode())) {
					list.add(dto);
				}
			}
		}
		return list;
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
}
