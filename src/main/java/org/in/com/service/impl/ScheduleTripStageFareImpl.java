package org.in.com.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanUtils;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleTripStageFareCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.ScheduleFareAutoOverrideDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.ScheduleTripStageFareDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.FareOverrideModeEM;
import org.in.com.exception.ServiceException;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleTripStageFareService;
import org.in.com.service.StationService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.ehcache.Element;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class ScheduleTripStageFareImpl extends CacheCentral implements ScheduleTripStageFareService {
	@Autowired
	StationService stationService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	UserService userService;
	private static String CACHEKEY = "QUICKFARE_";

	@Override
	public List<ScheduleFareAutoOverrideDTO> getTripStageActiveFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO) {
		List<ScheduleFareAutoOverrideDTO> searchFareAutoOverrides = getScheduleTripStageFareBySchedule(authDTO, scheduleDTO);
		if (searchFareAutoOverrides != null && !searchFareAutoOverrides.isEmpty()) {
			for (Iterator<ScheduleFareAutoOverrideDTO> iterator = searchFareAutoOverrides.iterator(); iterator.hasNext();) {
				ScheduleFareAutoOverrideDTO fareOverrideDTO = iterator.next();
				// common validations
				if (fareOverrideDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
					iterator.remove();
					continue;
				}
				// Route List
				if (!fareOverrideDTO.getRouteList().isEmpty() && BitsUtil.isRouteExists(fareOverrideDTO.getRouteList(), fromStationDTO, toStationDTO) == null) {
					iterator.remove();
					continue;
				}
			}
		}

		return searchFareAutoOverrides;
	}

	public List<ScheduleFareAutoOverrideDTO> getTripStageActiveFare(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleFareAutoOverrideDTO> searchFareAutoOverrides = getScheduleTripStageFareBySchedule(authDTO, scheduleDTO);
		if (searchFareAutoOverrides != null && !searchFareAutoOverrides.isEmpty()) {
			for (Iterator<ScheduleFareAutoOverrideDTO> iterator = searchFareAutoOverrides.iterator(); iterator.hasNext();) {
				ScheduleFareAutoOverrideDTO fareOverrideDTO = iterator.next();
				// common validations
				if (fareOverrideDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
					iterator.remove();
					continue;
				}
			}
		}

		return searchFareAutoOverrides;
	}

	private List<ScheduleFareAutoOverrideDTO> getScheduleTripStageFareBySchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleFareAutoOverrideDTO> fareOverrideDTOList = null;
		ScheduleTripStageFareDTO scheduleTripStageFare = null;
		String key = CACHEKEY + scheduleDTO.getCode() + "_" + DateUtil.convertDate(scheduleDTO.getTripDate());
		Element element = EhcacheManager.getscheduleTripStageFareEhCache().get(key);

		if (element != null) {
			ScheduleTripStageFareCacheDTO stageFareCache = (ScheduleTripStageFareCacheDTO) element.getObjectValue();
			scheduleTripStageFare = bindScheduleTripFareFromCacheObject(stageFareCache);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleFareAutoOverrideDAO fareAutoOverrideDAO = new ScheduleFareAutoOverrideDAO();
			scheduleTripStageFare = fareAutoOverrideDAO.getSearchScheduleFareAutoOverride(authDTO, scheduleDTO);

			// Save to schedule Trip Stage Cache
			ScheduleTripStageFareCacheDTO tripStageFareCacheDTO = bindScheduleTripFareToCacheObject(scheduleTripStageFare);
			element = new Element(key, tripStageFareCacheDTO);
			EhcacheManager.getscheduleTripStageFareEhCache().put(element);
		}
		if (StringUtil.isNotNull(scheduleTripStageFare.getCode())) {
			List<ScheduleTripStageFareDTO> tripStageFareList = convertStringToQuickFare(scheduleTripStageFare);
			fareOverrideDTOList = convertQuickFareToFareAutoOverride(tripStageFareList);
		}
		return fareOverrideDTOList;
	}

	@Override
	public void updateQuickFare(AuthDTO authDTO, ScheduleTripStageFareDTO quickFareOverrideDTO) {
		ScheduleFareAutoOverrideDAO dao = new ScheduleFareAutoOverrideDAO();
		dao.updateSearchFareAutoOverride(authDTO, quickFareOverrideDTO);

		// add to cache while save in repo
		String key = CACHEKEY + quickFareOverrideDTO.getSchedule().getCode() + "_" + DateUtil.convertDate(quickFareOverrideDTO.getSchedule().getTripDate());
		// EhcacheManager.getscheduleTripStageFareEhCache().remove(key);

		// Reload Quick fare to cache
		ScheduleFareAutoOverrideDAO fareAutoOverrideDAO = new ScheduleFareAutoOverrideDAO();
		ScheduleTripStageFareDTO scheduleTripStageFare = fareAutoOverrideDAO.getSearchScheduleFareAutoOverride(authDTO, quickFareOverrideDTO.getSchedule());

		ScheduleTripStageFareCacheDTO tripStageFareCacheDTO = bindScheduleTripFareToCacheObject(scheduleTripStageFare);
		Element element = new Element(key, tripStageFareCacheDTO);
		EhcacheManager.getscheduleTripStageFareEhCache().put(element);
	}

	@Override
	public void updateQuickFareV2(AuthDTO authDTO, List<ScheduleTripStageFareDTO> quickFareOverrides) {
		ScheduleFareAutoOverrideDAO dao = new ScheduleFareAutoOverrideDAO();
		dao.updateQuickFare(authDTO, quickFareOverrides);

		for (ScheduleTripStageFareDTO quickFareOverrideDTO : quickFareOverrides) {
			String key = CACHEKEY + quickFareOverrideDTO.getSchedule().getCode() + "_" + DateUtil.convertDate(quickFareOverrideDTO.getSchedule().getTripDate());
			EhcacheManager.getscheduleTripStageFareEhCache().remove(key);
		}
	}

	@Override
	public List<ScheduleFareAutoOverrideDTO> processScheduleTripStageFare(AuthDTO authDTO, ScheduleDTO schedule, List<ScheduleFareAutoOverrideDTO> tripStageFareList, StationDTO fromStation, StationDTO toStation) {
		List<ScheduleFareAutoOverrideDTO> finalFareList = new ArrayList<ScheduleFareAutoOverrideDTO>();
		if (tripStageFareList != null && !tripStageFareList.isEmpty()) {
			for (ScheduleFareAutoOverrideDTO fareOverrideDTO : tripStageFareList) {
				// common validations
				if (fareOverrideDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
					continue;
				}
				// Route List
				if (!fareOverrideDTO.getRouteList().isEmpty() && BitsUtil.isRouteExists(fareOverrideDTO.getRouteList(), fromStation, toStation) == null) {
					continue;
				}
				finalFareList.add(fareOverrideDTO);
			}
		}

		return finalFareList;
	}

	@Override
	public List<ScheduleTripStageFareDTO> getScheduleTripStageFare(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleFareAutoOverrideDAO fareAutoOverrideDAO = new ScheduleFareAutoOverrideDAO();
		ScheduleTripStageFareDTO tripStageFare = fareAutoOverrideDAO.getSearchScheduleFareAutoOverride(authDTO, scheduleDTO);
		List<ScheduleTripStageFareDTO> tripStageFareList = new ArrayList<ScheduleTripStageFareDTO>();
		if (tripStageFare.getId() != 0) {
			tripStageFareList = convertStringToQuickFare(tripStageFare);
		}
		return tripStageFareList;
	}

	private ScheduleTripStageFareDTO bindScheduleTripFareFromCacheObject(ScheduleTripStageFareCacheDTO stageFareCache) {
		ScheduleTripStageFareDTO stageFare = new ScheduleTripStageFareDTO();
		stageFare.setCode(stageFareCache.getCode());
		stageFare.setTripDate(stageFareCache.getTripDate());
		stageFare.setFareDetails(stageFareCache.getFareDetails());
		return stageFare;
	}

	private ScheduleTripStageFareCacheDTO bindScheduleTripFareToCacheObject(ScheduleTripStageFareDTO scheduleTripStageFare) {
		ScheduleTripStageFareCacheDTO fareCache = new ScheduleTripStageFareCacheDTO();
		fareCache.setCode(scheduleTripStageFare.getCode());
		fareCache.setTripDate(scheduleTripStageFare.getTripDate());
		fareCache.setFareDetails(scheduleTripStageFare.getFareDetails());
		return fareCache;
	}

	private List<ScheduleTripStageFareDTO> convertStringToQuickFare(ScheduleTripStageFareDTO quickFareOverrideDTO) {
		List<ScheduleTripStageFareDTO> fareDetailsList = new ArrayList<>();
		try {
			List<String> routeFareDetails = new ArrayList<String>();
			if (StringUtil.isNotNull(quickFareOverrideDTO.getFareDetails())) {
				routeFareDetails.addAll(Arrays.asList(quickFareOverrideDTO.getFareDetails().split("\\|")));
			}
			for (String routeFareOverride : routeFareDetails) {
				ScheduleTripStageFareDTO fareAutoOverrideDTO = new ScheduleTripStageFareDTO();
				fareAutoOverrideDTO.setCode(quickFareOverrideDTO.getCode());
				fareAutoOverrideDTO.setActiveFlag(quickFareOverrideDTO.getActiveFlag());
				fareAutoOverrideDTO.setTripDate(quickFareOverrideDTO.getTripDate());
				fareAutoOverrideDTO.setSchedule(quickFareOverrideDTO.getSchedule());
				fareAutoOverrideDTO.setAudit(quickFareOverrideDTO.getAudit());

				String[] routeFareDetails1 = routeFareOverride.split("\\-");
				if (routeFareDetails1.length != 2) {
					continue;
				}
				String route = routeFareDetails1[0];
				String[] routes = route.split("_");
				RouteDTO routeDTO = new RouteDTO();

				StationDTO FromstationDTO = new StationDTO();
				FromstationDTO.setId(Integer.valueOf(routes[0]));
				routeDTO.setFromStation(FromstationDTO);

				StationDTO TostationDTO = new StationDTO();
				TostationDTO.setId(Integer.valueOf(routes[1]));
				routeDTO.setToStation(TostationDTO);

				String fares = routeFareDetails1[1];
				List<String> busSeatTypeFareDetails = Arrays.asList(fares.split(","));
				List<StageFareDTO> stageBusTypeFare = new ArrayList<StageFareDTO>();
				for (String routeFareAutoOverride1 : busSeatTypeFareDetails) {
					String[] routeFareDetails2 = routeFareAutoOverride1.split("\\:");

					if (routeFareDetails2.length != 2) {
						continue;
					}
					StageFareDTO seatTypeFare = new StageFareDTO();
					seatTypeFare.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(routeFareDetails2[0]));
					seatTypeFare.setFare(StringUtil.getBigDecimalValue(routeFareDetails2[1]).setScale(0, BigDecimal.ROUND_DOWN));
					stageBusTypeFare.add(seatTypeFare);
				}
				if (stageBusTypeFare.isEmpty()) {
					continue;
				}
				routeDTO.setStageFare(stageBusTypeFare);
				fareAutoOverrideDTO.setRoute(routeDTO);
				fareDetailsList.add(fareAutoOverrideDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return fareDetailsList;
	}

	private List<ScheduleFareAutoOverrideDTO> convertQuickFareToFareAutoOverride(List<ScheduleTripStageFareDTO> fareAutoOverrides) {
		List<ScheduleFareAutoOverrideDTO> fareOverrideDTOList = new ArrayList<>();
		try {
			for (ScheduleTripStageFareDTO quickFareOverrideDTO2 : fareAutoOverrides) {
				Map<BigDecimal, ScheduleFareAutoOverrideDTO> fareOverrideMap = new HashMap<>();
				for (StageFareDTO seatTypeFareDTO : quickFareOverrideDTO2.getRoute().getStageFare()) {
					BigDecimal fare = seatTypeFareDTO.getFare().setScale(0, BigDecimal.ROUND_DOWN);
					if (fareOverrideMap.get(fare) == null) {
						ScheduleFareAutoOverrideDTO scheduleFareAutoOverride = new ScheduleFareAutoOverrideDTO();

						BeanUtils.copyProperties(scheduleFareAutoOverride, quickFareOverrideDTO2);

						scheduleFareAutoOverride.setFare(seatTypeFareDTO.getFare());
						scheduleFareAutoOverride.setFareOverrideMode(FareOverrideModeEM.SEARCH_FARE);
						scheduleFareAutoOverride.setTag(Text.SEARCH);
						scheduleFareAutoOverride.setActiveFrom(quickFareOverrideDTO2.getTripDate());
						scheduleFareAutoOverride.setActiveTo(quickFareOverrideDTO2.getTripDate());
						scheduleFareAutoOverride.setDayOfWeek("1111111");

						List<RouteDTO> routeList = new ArrayList<RouteDTO>();
						RouteDTO routeDTO = new RouteDTO();
						routeDTO.setFromStation(stationService.getStation(quickFareOverrideDTO2.getRoute().getFromStation()));
						routeDTO.setToStation(stationService.getStation(quickFareOverrideDTO2.getRoute().getToStation()));
						routeList.add(routeDTO);

						scheduleFareAutoOverride.setGroupList(new ArrayList<GroupDTO>());
						scheduleFareAutoOverride.setRouteList(routeList);

						List<BusSeatTypeEM> busSeatTypes = new ArrayList<>();
						busSeatTypes.add(seatTypeFareDTO.getBusSeatType());
						scheduleFareAutoOverride.setBusSeatType(busSeatTypes);

						fareOverrideMap.put(fare, scheduleFareAutoOverride);
					}
					else {
						ScheduleFareAutoOverrideDTO fareAutoOverrideDTO = fareOverrideMap.get(fare);
						fareAutoOverrideDTO.getBusSeatType().add(seatTypeFareDTO.getBusSeatType());
						fareOverrideMap.put(fare, fareAutoOverrideDTO);
					}
				}

				fareOverrideDTOList.addAll(new ArrayList<>(fareOverrideMap.values()));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return fareOverrideDTOList;
	}

	@Override
	public List<ScheduleTripStageFareDTO> getScheduleTripStageFareV2(AuthDTO authDTO, ScheduleDTO scheduleDTO, String fromDate, String toDate, String tripCode) {
		List<ScheduleTripStageFareDTO> fareDetailsList = new ArrayList<ScheduleTripStageFareDTO>();
		ScheduleCache scheduleCache = new ScheduleCache();
		if (scheduleDTO != null && StringUtil.isNotNull(scheduleDTO.getCode())) {
			scheduleCache.getScheduleDTO(authDTO, scheduleDTO);
		}
		ScheduleFareAutoOverrideDAO fareAutoOverrideDAO = new ScheduleFareAutoOverrideDAO();
		List<ScheduleTripStageFareDTO> list = fareAutoOverrideDAO.getScheduleTripStageFare(authDTO, scheduleDTO, fromDate, toDate, tripCode);
		for (ScheduleTripStageFareDTO scheduleTripStageFareDTO : list) {
			scheduleCache.getScheduleDTObyId(authDTO, scheduleTripStageFareDTO.getSchedule());
			List<ScheduleTripStageFareDTO> tripStageFareList = convertStringToQuickFareV2(authDTO, scheduleTripStageFareDTO);
			fareDetailsList.addAll(tripStageFareList);
		}
		return fareDetailsList;
	}

	private List<ScheduleTripStageFareDTO> convertStringToQuickFareV2(AuthDTO authDTO, ScheduleTripStageFareDTO quickFareOverrideDTO) {
		List<ScheduleTripStageFareDTO> fareDetailsList = new ArrayList<>();
		try {
			List<String> routeFareDetails = new ArrayList<String>();
			if (StringUtil.isNotNull(quickFareOverrideDTO.getFareDetails())) {
				routeFareDetails.addAll(Arrays.asList(quickFareOverrideDTO.getFareDetails().split("\\|")));
			}
			for (String routeFareOverride : routeFareDetails) {
				ScheduleTripStageFareDTO fareAutoOverrideDTO = new ScheduleTripStageFareDTO();
				fareAutoOverrideDTO.setCode(quickFareOverrideDTO.getCode());
				fareAutoOverrideDTO.setActiveFlag(quickFareOverrideDTO.getActiveFlag());
				fareAutoOverrideDTO.setTripDate(quickFareOverrideDTO.getTripDate());
				fareAutoOverrideDTO.setSchedule(quickFareOverrideDTO.getSchedule());
				fareAutoOverrideDTO.setAudit(quickFareOverrideDTO.getAudit());

				String[] routeFareDetails1 = routeFareOverride.split("\\-");
				if (routeFareDetails1.length != 2) {
					continue;
				}
				String route = routeFareDetails1[0];
				String[] routes = route.split("_");
				RouteDTO routeDTO = new RouteDTO();

				StationDTO FromstationDTO = new StationDTO();
				FromstationDTO.setId(Integer.valueOf(routes[0]));
				stationService.getStation(FromstationDTO);
				routeDTO.setFromStation(FromstationDTO);

				StationDTO TostationDTO = new StationDTO();
				TostationDTO.setId(Integer.valueOf(routes[1]));
				stationService.getStation(TostationDTO);
				routeDTO.setToStation(TostationDTO);

				String fares = routeFareDetails1[1];
				List<String> busSeatTypeFareDetails = Arrays.asList(fares.split(","));
				List<StageFareDTO> stageBusTypeFare = new ArrayList<StageFareDTO>();
				for (String routeFareAutoOverride1 : busSeatTypeFareDetails) {
					String[] routeFareDetails2 = routeFareAutoOverride1.split("\\:");

					if (routeFareDetails2.length != 2) {
						continue;
					}
					StageFareDTO seatTypeFare = new StageFareDTO();
					seatTypeFare.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(routeFareDetails2[0]));
					seatTypeFare.setFare(StringUtil.getBigDecimalValue(routeFareDetails2[1]).setScale(0, BigDecimal.ROUND_DOWN));
					stageBusTypeFare.add(seatTypeFare);
				}
				if (stageBusTypeFare.isEmpty()) {
					continue;
				}
				routeDTO.setStageFare(stageBusTypeFare);
				fareAutoOverrideDTO.setRoute(routeDTO);

				if (fareAutoOverrideDTO.getAudit().getUser() != null && fareAutoOverrideDTO.getAudit().getUser().getId() != 0) {
					fareAutoOverrideDTO.getAudit().setUser(getUserDTOById(authDTO, fareAutoOverrideDTO.getAudit().getUser()));
				}
				fareDetailsList.add(fareAutoOverrideDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return fareDetailsList;
	}

	@Override
	public List<ScheduleTripStageFareDTO> getScheduleTripStageFares(AuthDTO authDTO, ScheduleDTO scheduleDTO, String fromDate, String toDate) {
		scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);

		ScheduleFareAutoOverrideDAO fareAutoOverrideDAO = new ScheduleFareAutoOverrideDAO();
		List<ScheduleTripStageFareDTO> tripStageFares = fareAutoOverrideDAO.getScheduleTripStageFares(authDTO, scheduleDTO, fromDate, toDate);
		List<ScheduleTripStageFareDTO> finalTripStageFares = new ArrayList<ScheduleTripStageFareDTO>();
		for (ScheduleTripStageFareDTO scheduleTripStageFareDTO : tripStageFares) {
			finalTripStageFares.addAll(convertStringToQuickFare(scheduleTripStageFareDTO));
		}
		for (ScheduleTripStageFareDTO scheduleTripStageFareDTO : finalTripStageFares) {
			scheduleTripStageFareDTO.getRoute().setFromStation(stationService.getStation(scheduleTripStageFareDTO.getRoute().getFromStation()));
			scheduleTripStageFareDTO.getRoute().setToStation(stationService.getStation(scheduleTripStageFareDTO.getRoute().getToStation()));
			scheduleTripStageFareDTO.getAudit().setUser(userService.getUser(authDTO, scheduleTripStageFareDTO.getAudit().getUser()));
		}
		return finalTripStageFares;
	}

	@Override
	public JSONArray getScheduleTripStageFareHistory(AuthDTO authDTO, ScheduleDTO scheduleDTO, String fromDate, String toDate, String tripCode) {
		ScheduleFareAutoOverrideDAO fareAutoOverrideDAO = new ScheduleFareAutoOverrideDAO();
		ScheduleCache scheduleCache = new ScheduleCache();
		if (scheduleDTO != null) {
			scheduleCache.getScheduleDTO(authDTO, scheduleDTO);
		}
		List<ScheduleTripStageFareDTO> list = fareAutoOverrideDAO.getScheduleTripStageFareHistory(authDTO, scheduleDTO, fromDate, toDate, tripCode);

		JSONArray fareHistoryArray = new JSONArray();
		for (ScheduleTripStageFareDTO scheduleTripStageFareDTO : list) {
			scheduleCache.getScheduleDTObyId(authDTO, scheduleTripStageFareDTO.getSchedule());
			scheduleTripStageFareDTO.getRoute().setFromStation(getStationDTObyId(scheduleTripStageFareDTO.getRoute().getFromStation()));
			scheduleTripStageFareDTO.getRoute().setToStation(getStationDTObyId(scheduleTripStageFareDTO.getRoute().getToStation()));
			scheduleTripStageFareDTO.getAudit().setUser(userService.getUser(authDTO, scheduleTripStageFareDTO.getAudit().getUser()));
			
			JSONObject jsonData = new JSONObject();
			jsonData.put("tripCode", scheduleTripStageFareDTO.getCode());
			jsonData.put("tripDate", scheduleTripStageFareDTO.getTripDate());
			
			JSONObject scheduleJson = new JSONObject();
			scheduleJson.put("code", scheduleTripStageFareDTO.getSchedule().getCode());
			scheduleJson.put("name", scheduleTripStageFareDTO.getSchedule().getName());
			scheduleJson.put("serviceNo", scheduleTripStageFareDTO.getSchedule().getServiceNumber());
			jsonData.put("schedule", scheduleJson);
			
			JSONObject fromStationJson = new JSONObject();
			fromStationJson.put("code", scheduleTripStageFareDTO.getRoute().getFromStation().getCode());
			fromStationJson.put("name", scheduleTripStageFareDTO.getRoute().getFromStation().getName());
			jsonData.put("fromStation", fromStationJson);
			
			JSONObject toStationJson = new JSONObject();
			toStationJson.put("code", scheduleTripStageFareDTO.getRoute().getToStation().getCode());
			toStationJson.put("name", scheduleTripStageFareDTO.getRoute().getToStation().getName());
			jsonData.put("toStation", toStationJson);
		
			JSONArray stageFareArray = new JSONArray();
			for (String seatWiseFareDetails : scheduleTripStageFareDTO.getFareDetails().split(Text.COMMA)) {
				String[] seatWiseFareDetails2 = seatWiseFareDetails.split("\\:");

				if (seatWiseFareDetails2.length != 2) {
					continue;
				}
				
				BusSeatTypeEM seatTypeEM = BusSeatTypeEM.getBusSeatTypeEM(seatWiseFareDetails2[0]);
				String fareDetails = seatWiseFareDetails2[1];

				JSONObject seatTypeFareJson = new JSONObject();
				seatTypeFareJson.put("seatTypeCode", seatTypeEM.getCode());
				seatTypeFareJson.put("oldFare", fareDetails.contains("_") ? fareDetails.split("\\_")[0] : BigDecimal.ZERO);
				seatTypeFareJson.put("newFare", fareDetails.split("\\_").length > 1 ? fareDetails.split("\\_")[1] : fareDetails.split("\\_")[0]);
				stageFareArray.add(seatTypeFareJson);
			}
			jsonData.put("seatTypeFareList", stageFareArray);
			
			JSONObject updatedByJson = new JSONObject();
			if (scheduleTripStageFareDTO.getAudit().getUser() != null && StringUtil.isNotNull(scheduleTripStageFareDTO.getAudit().getUser().getCode())) {
				updatedByJson.put("code", scheduleTripStageFareDTO.getAudit().getUser().getCode());
				updatedByJson.put("name", scheduleTripStageFareDTO.getAudit().getUser().getName());
			}

			JSONObject auditJson = new JSONObject();
			auditJson.put("user", updatedByJson);
			auditJson.put("updatedAt", scheduleTripStageFareDTO.getAudit().getUpdatedAt());
			jsonData.put("audit", auditJson);
			fareHistoryArray.add(jsonData);
		}
		
		return fareHistoryArray;
	}

}
