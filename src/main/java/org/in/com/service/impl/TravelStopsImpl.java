package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.TravelStopsCacheDTO;
import org.in.com.dao.TravelStopsDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TravelStopsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.StationService;
import org.in.com.service.TravelStopsService;
import org.in.com.service.TripService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.ehcache.Element;

@Service
public class TravelStopsImpl extends BaseImpl implements TravelStopsService {
	private static String CACHEKEY = "SHTSTOP";
	@Autowired
	TripService tripService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	StationService stationService;

	@Override
	public List<TravelStopsDTO> get(AuthDTO authDTO, TravelStopsDTO dto) {
		TravelStopsDAO dao = new TravelStopsDAO();
		return dao.getStop(authDTO, dto);
	}

	@Override
	public List<TravelStopsDTO> getAll(AuthDTO authDTO) {
		TravelStopsDAO dao = new TravelStopsDAO();
		return dao.getAllStop(authDTO);
	}

	@Override
	public TravelStopsDTO Update(AuthDTO authDTO, TravelStopsDTO dto) {
		TravelStopsDAO dao = new TravelStopsDAO();
		dao.update(authDTO, dto);
		/** Clear cache */
		clearScheduleStopCache();
		return dto;
	}

	@Override
	public List<TravelStopsDTO> getScheduleStop(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		TravelStopsDAO dao = new TravelStopsDAO();
		List<TravelStopsDTO> list = dao.getScheduleStop(authDTO, scheduleDTO);
		for (TravelStopsDTO stopsDTO : list) {
			stopsDTO.setStation(getStationDTObyId(stopsDTO.getStation()));
		}
		return list;
	}

	@Override
	public TravelStopsDTO mapScheduleStops(AuthDTO authDTO, ScheduleDTO scheduleDTO, TravelStopsDTO stopDTO) {
		TravelStopsDAO dao = new TravelStopsDAO();
		if (stopDTO.getStation() != null && StringUtil.isNotNull(stopDTO.getStation().getCode())) {
			stopDTO.setStation(stationService.getStation(stopDTO.getStation()));
		}
		stopDTO = dao.mapScheduleStops(authDTO, scheduleDTO, stopDTO);
		String key = CACHEKEY + scheduleDTO.getCode();
		EhcacheManager.getScheduleEhCache().remove(key);
		return stopDTO;
	}

	@Override
	public List<TravelStopsDTO> getScheduleTripStop(AuthDTO authDTO, TripDTO tripDTO) {
		List<TravelStopsDTO> stopsList = new ArrayList<TravelStopsDTO>();

		tripDTO = tripService.getTripDTO(authDTO, tripDTO);

		ScheduleCache scheduleCache = new ScheduleCache();
		ScheduleDTO scheduleDTO = scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());

		Comparator<ScheduleStationDTO> comp = new BeanComparator("stationSequence");
		Map<Integer, ScheduleStationDTO> stationMap = new HashMap<>();

		List<ScheduleStationDTO> stationList = scheduleStationService.getByScheduleTripDate(authDTO, scheduleDTO, tripDTO.getTripDate());
		Collections.sort(stationList, comp);
		for (ScheduleStationDTO stageStationDTO : stationList) {
			stationMap.put(stageStationDTO.getStation().getId(), stageStationDTO);
		}
		ScheduleStationDTO fromStation = stationMap.get(stationService.getStation(tripDTO.getSearch().getFromStation()).getId());
		ScheduleStationDTO toStation = stationMap.get(stationService.getStation(tripDTO.getSearch().getToStation()).getId());
		List<TravelStopsDTO> travelList = getByScheduleCode(authDTO, scheduleDTO);

		for (TravelStopsDTO stopsDTO : travelList) {
			ScheduleStationDTO stopStationDTO = stationMap.get(stopsDTO.getStation().getId());
			if (stopStationDTO != null && stopStationDTO.getStationSequence() >= fromStation.getStationSequence() && stopStationDTO.getStationSequence() < toStation.getStationSequence()) {
				stopsDTO.setTravelStopTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stopStationDTO.getMinitues() + stopsDTO.getTravelMinutes()));
				stopsDTO.setStation(getStationDTObyId(stopsDTO.getStation()));
				stopsList.add(stopsDTO);
			}
		}
		return stopsList;
	}

	public List<TravelStopsDTO> getByScheduleCode(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String key = CACHEKEY + scheduleDTO.getCode();
		List<TravelStopsDTO> travelStopList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<TravelStopsCacheDTO> scheduleTravelStopsCacheList = (List<TravelStopsCacheDTO>) element.getObjectValue();
			travelStopList = bindTravelStopsFromCacheObject(scheduleTravelStopsCacheList);
		}
		else if (StringUtil.isNotNull(scheduleDTO.getCode())) {
			TravelStopsDAO travelStopsDAO = new TravelStopsDAO();
			travelStopList = travelStopsDAO.getScheduleStop(authDTO, scheduleDTO);
			// Save to schedule station Point Cache
			List<TravelStopsCacheDTO> scheduleTravelStopsCacheList = bindTravelStopsToCacheObject(travelStopList);
			element = new Element(key, scheduleTravelStopsCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		return travelStopList;
	}

	private void clearScheduleStopCache() {
		List<String> keys = EhcacheManager.getScheduleEhCache().getKeys();
		for (String key : keys) {
			if (!key.startsWith(CACHEKEY)) {
				continue;
			}
			EhcacheManager.getScheduleEhCache().remove(key);
		}
	}

	private List<TravelStopsDTO> bindTravelStopsFromCacheObject(List<TravelStopsCacheDTO> scheduleTravelStopsCacheList) {
		List<TravelStopsDTO> scheduleTravelStopsList = new ArrayList<>();
		if (scheduleTravelStopsCacheList != null && !scheduleTravelStopsCacheList.isEmpty()) {
			// copy from cache
			for (TravelStopsCacheDTO stopsCache : scheduleTravelStopsCacheList) {
				TravelStopsDTO travelStopsDTO = new TravelStopsDTO();
				travelStopsDTO.setCode(stopsCache.getCode());
				travelStopsDTO.setName(stopsCache.getName());
				travelStopsDTO.setAmenities(stopsCache.getAmenities());
				travelStopsDTO.setLandmark(stopsCache.getLandmark());
				travelStopsDTO.setLatitude(stopsCache.getLatitude());
				travelStopsDTO.setLongitude(stopsCache.getLongitude());
				travelStopsDTO.setMinutes(stopsCache.getMinutes());
				travelStopsDTO.setRestRoom(stopsCache.getRestRoom());
				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(stopsCache.getStationId());
				travelStopsDTO.setStation(stationDTO);
				travelStopsDTO.setTravelMinutes(stopsCache.getTravelMinutes());
				travelStopsDTO.setRemarks(stopsCache.getRemarks());
				scheduleTravelStopsList.add(travelStopsDTO);
			}
		}
		return scheduleTravelStopsList;
	}

	private List<TravelStopsCacheDTO> bindTravelStopsToCacheObject(List<TravelStopsDTO> travelStopList) {
		List<TravelStopsCacheDTO> scheduleTravelStopsCacheList = new ArrayList<>();
		if (travelStopList != null && !travelStopList.isEmpty()) {
			// copy from cache
			for (TravelStopsDTO stopsDTO : travelStopList) {
				TravelStopsCacheDTO stopsCache = new TravelStopsCacheDTO();
				stopsCache.setCode(stopsDTO.getCode());
				stopsCache.setName(stopsDTO.getName());
				stopsCache.setAmenities(stopsDTO.getAmenities());
				stopsCache.setLandmark(stopsDTO.getLandmark());
				stopsCache.setLatitude(stopsDTO.getLatitude());
				stopsCache.setLongitude(stopsDTO.getLongitude());
				stopsCache.setMinutes(stopsDTO.getMinutes());
				stopsCache.setStationId(stopsDTO.getStation().getId());
				stopsCache.setTravelMinutes(stopsDTO.getTravelMinutes());
				stopsCache.setRestRoom(stopsDTO.getRestRoom());
				stopsCache.setRemarks(stopsDTO.getRemarks());
				scheduleTravelStopsCacheList.add(stopsCache);
			}
		}
		return scheduleTravelStopsCacheList;
	}

	@Override
	public List<TravelStopsDTO> getScheduleStopV2(AuthDTO authDTO, ScheduleDTO scheduleDTO, SearchDTO searchDTO) {
		List<TravelStopsDTO> stopsList = new ArrayList<TravelStopsDTO>();
		try {
			Comparator<ScheduleStationDTO> comp = new BeanComparator("stationSequence");
			Map<Integer, ScheduleStationDTO> stationMap = new HashMap<>();

			Collections.sort(scheduleDTO.getStationList(), comp);
			for (ScheduleStationDTO stageStationDTO : scheduleDTO.getStationList()) {
				stationMap.put(stageStationDTO.getStation().getId(), stageStationDTO);
			}
			ScheduleStationDTO fromStationDTO = stationMap.get(stationService.getStation(searchDTO.getFromStation()).getId());
			ScheduleStationDTO toStationDTO = stationMap.get(stationService.getStation(searchDTO.getToStation()).getId());
			List<TravelStopsDTO> travelList = getByScheduleCode(authDTO, scheduleDTO);

			for (TravelStopsDTO stopsDTO : travelList) {
				ScheduleStationDTO stopStationDTO = stationMap.get(stopsDTO.getStation().getId());
				if (stopStationDTO != null && stopStationDTO.getStationSequence() >= fromStationDTO.getStationSequence() && stopStationDTO.getStationSequence() < toStationDTO.getStationSequence()) {
					stopsDTO.setTravelStopTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stopStationDTO.getMinitues() + stopsDTO.getTravelMinutes()));
					stopsDTO.setStation(getStationDTObyId(stopsDTO.getStation()));
					stopsList.add(stopsDTO);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return stopsList;
	}
}
