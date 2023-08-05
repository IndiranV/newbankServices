package org.in.com.service.impl;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.sf.ehcache.Element;

import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleTimeOverrideCacheDTO;
import org.in.com.dao.ScheduleTimeOverrideDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleTimeOverrideDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.OverrideTypeEM;
import org.in.com.service.ScheduleTimeOverrideService;
import org.in.com.service.StationService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleTimeOverrideImpl extends CacheCentral implements ScheduleTimeOverrideService {
	@Autowired
	StationService stationService;

	private static String CACHEKEY = "SHTOR";

	public List<ScheduleTimeOverrideDTO> get(AuthDTO authDTO, ScheduleTimeOverrideDTO timeOverrideDTO) {
		ScheduleTimeOverrideDAO dao = new ScheduleTimeOverrideDAO();
		List<ScheduleTimeOverrideDTO> list = dao.get(authDTO, timeOverrideDTO);
		for (ScheduleTimeOverrideDTO controlDTO : list) {
			if (controlDTO.getStation() != null && controlDTO.getStation().getId() != 0) {
				controlDTO.setStation(getStationDTObyId(controlDTO.getStation()));
			}
			if (controlDTO.getUpdatedUser() != null && controlDTO.getUpdatedUser().getId() != 0) {
				controlDTO.setUpdatedUser(getUserDTOById(authDTO, controlDTO.getUpdatedUser()));
			}
		}
		return list;
	}

	public ScheduleTimeOverrideDTO Update(AuthDTO authDTO, ScheduleTimeOverrideDTO timeOverrideDTO) {
		if (timeOverrideDTO.getStation() != null && StringUtil.isNotNull(timeOverrideDTO.getStation().getCode())) {
			timeOverrideDTO.setStation(stationService.getStation(timeOverrideDTO.getStation()));
		}
		ScheduleTimeOverrideDAO dao = new ScheduleTimeOverrideDAO();
		dao.getIUD(authDTO, timeOverrideDTO);
		// clear cache
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.removeScheduleDTO(authDTO, timeOverrideDTO.getSchedule());

		return timeOverrideDTO;
	}

	public List<ScheduleTimeOverrideDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {

		String key = CACHEKEY + scheduleDTO.getCode();
		List<ScheduleTimeOverrideDTO> fareOverrideDTOList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleTimeOverrideCacheDTO> scheduleSeatVisibilityCacheList = (List<ScheduleTimeOverrideCacheDTO>) element.getObjectValue();
			fareOverrideDTOList = bindTimeOverrideFromCacheObject(scheduleSeatVisibilityCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleTimeOverrideDAO timeOverrideDAO = new ScheduleTimeOverrideDAO();
			fareOverrideDTOList = timeOverrideDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule station Point Cache
			List<ScheduleTimeOverrideCacheDTO> scheduleSeatVisibilityCacheList = bindFareAutoOverrideToCacheObject(fareOverrideDTOList);
			element = new Element(key, scheduleSeatVisibilityCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		// Validate all Seat visibility
		for (Iterator<ScheduleTimeOverrideDTO> iterator = fareOverrideDTOList.iterator(); iterator.hasNext();) {
			ScheduleTimeOverrideDTO fareOverrideDTO = iterator.next();
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
			// if (fromStationDTO.getId() ==
			// fareOverrideDTO.getStation().getId() && toStationDTO.getId()
			// == fareOverrideDTO.getStation().getId()) {
			// iterator.remove();
			// continue;
			// }

			// Exceptions and Override
			for (Iterator<ScheduleTimeOverrideDTO> overrideIterator = fareOverrideDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleTimeOverrideDTO fareAutoOverrideExceptionDTO = overrideIterator.next();
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
				if (fareAutoOverrideExceptionDTO.getOverrideMinutes() == -1) {
					iterator.remove();
					break;
				}
				else {
					// Apply Override
					fareOverrideDTO.setOverrideType(fareAutoOverrideExceptionDTO.getOverrideType());
					fareOverrideDTO.setOverrideMinutes(fareAutoOverrideExceptionDTO.getOverrideMinutes());
				}
			}
		}
		// Identify and remove the generic fare
		if (fareOverrideDTOList.size() >= 2) {
			// Sorting, find most recent close
			Collections.sort(fareOverrideDTOList, new Comparator<ScheduleTimeOverrideDTO>() {
				public int compare(final ScheduleTimeOverrideDTO object1, final ScheduleTimeOverrideDTO object2) {
					return Integer.compare(DateUtil.getDayDifferent(new DateTime(object1.getActiveFrom()), new DateTime(object1.getActiveTo())), DateUtil.getDayDifferent(new DateTime(object2.getActiveFrom()), new DateTime(object2.getActiveTo())));
				}
			});

		}
		return fareOverrideDTOList;

	}

	private List<ScheduleTimeOverrideCacheDTO> bindFareAutoOverrideToCacheObject(List<ScheduleTimeOverrideDTO> ScheduleFareAutoOverrideDTOList) {
		List<ScheduleTimeOverrideCacheDTO> seatVisibilityCacheList = new ArrayList<>();
		// copy to cache
		if (ScheduleFareAutoOverrideDTOList != null && !ScheduleFareAutoOverrideDTOList.isEmpty()) {
			for (ScheduleTimeOverrideDTO fareAutoOverrideDTO : ScheduleFareAutoOverrideDTOList) {
				ScheduleTimeOverrideCacheDTO seatAutooverrideCacheDTO = new ScheduleTimeOverrideCacheDTO();

				seatAutooverrideCacheDTO.setActiveFlag(fareAutoOverrideDTO.getActiveFlag());
				seatAutooverrideCacheDTO.setId(fareAutoOverrideDTO.getId());
				seatAutooverrideCacheDTO.setCode(fareAutoOverrideDTO.getCode());
				seatAutooverrideCacheDTO.setActiveFrom(fareAutoOverrideDTO.getActiveFrom());
				seatAutooverrideCacheDTO.setActiveTo(fareAutoOverrideDTO.getActiveTo());
				seatAutooverrideCacheDTO.setDayOfWeek(fareAutoOverrideDTO.getDayOfWeek());

				seatAutooverrideCacheDTO.setStationId(fareAutoOverrideDTO.getStation().getId());
				seatAutooverrideCacheDTO.setOverrideMinutes(fareAutoOverrideDTO.getOverrideMinutes());
				seatAutooverrideCacheDTO.setOverrideTypeCode(fareAutoOverrideDTO.getOverrideType().getCode());
				seatAutooverrideCacheDTO.setReactionFlag(fareAutoOverrideDTO.isReactionFlag());

				List<ScheduleTimeOverrideCacheDTO> overrideControlList = new ArrayList<>();
				if (fareAutoOverrideDTO.getOverrideList() != null && !fareAutoOverrideDTO.getOverrideList().isEmpty()) {
					for (ScheduleTimeOverrideDTO overrideSeatVisibilityDTO : fareAutoOverrideDTO.getOverrideList()) {
						ScheduleTimeOverrideCacheDTO cacheDTO = new ScheduleTimeOverrideCacheDTO();
						cacheDTO.setActiveFlag(overrideSeatVisibilityDTO.getActiveFlag());
						cacheDTO.setId(overrideSeatVisibilityDTO.getId());
						cacheDTO.setCode(overrideSeatVisibilityDTO.getCode());
						cacheDTO.setActiveFrom(overrideSeatVisibilityDTO.getActiveFrom());
						cacheDTO.setActiveTo(overrideSeatVisibilityDTO.getActiveTo());
						cacheDTO.setDayOfWeek(overrideSeatVisibilityDTO.getDayOfWeek());

						cacheDTO.setOverrideMinutes(overrideSeatVisibilityDTO.getOverrideMinutes());
						cacheDTO.setOverrideTypeCode(overrideSeatVisibilityDTO.getOverrideType().getCode());
						cacheDTO.setReactionFlag(overrideSeatVisibilityDTO.isReactionFlag());

						overrideControlList.add(cacheDTO);
					}
					seatAutooverrideCacheDTO.setOverrideList(overrideControlList);
				}
				seatVisibilityCacheList.add(seatAutooverrideCacheDTO);
			}
		}
		return seatVisibilityCacheList;
	}

	private List<ScheduleTimeOverrideDTO> bindTimeOverrideFromCacheObject(List<ScheduleTimeOverrideCacheDTO> seatAutoReleaseCacheDTOList) {
		List<ScheduleTimeOverrideDTO> timeOverrideDTOList = new ArrayList<>();
		if (seatAutoReleaseCacheDTOList != null && !seatAutoReleaseCacheDTOList.isEmpty()) {
			// copy from cache
			for (ScheduleTimeOverrideCacheDTO timeOverrideCacheDTO : seatAutoReleaseCacheDTOList) {
				ScheduleTimeOverrideDTO timeOverrideDTO = new ScheduleTimeOverrideDTO();
				timeOverrideDTO.setCode(timeOverrideCacheDTO.getCode());
				timeOverrideDTO.setActiveFrom(timeOverrideCacheDTO.getActiveFrom());
				timeOverrideDTO.setActiveTo(timeOverrideCacheDTO.getActiveTo());
				timeOverrideDTO.setDayOfWeek(timeOverrideCacheDTO.getDayOfWeek());
				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(timeOverrideCacheDTO.getStationId());
				timeOverrideDTO.setStation(stationDTO);
				timeOverrideDTO.setOverrideMinutes(timeOverrideCacheDTO.getOverrideMinutes());
				timeOverrideDTO.setOverrideType(OverrideTypeEM.getOverrideTypeEM(timeOverrideCacheDTO.getOverrideTypeCode()));
				timeOverrideDTO.setReactionFlag(timeOverrideCacheDTO.isReactionFlag());

				List<ScheduleTimeOverrideDTO> overrideControlList = new ArrayList<>();
				if (timeOverrideCacheDTO.getOverrideList() != null && !timeOverrideCacheDTO.getOverrideList().isEmpty()) {
					for (ScheduleTimeOverrideCacheDTO overRideCacheDTO : timeOverrideCacheDTO.getOverrideList()) {
						ScheduleTimeOverrideDTO overrideTimeDTO = new ScheduleTimeOverrideDTO();
						overrideTimeDTO.setCode(overRideCacheDTO.getCode());
						overrideTimeDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideTimeDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideTimeDTO.setOverrideMinutes(overRideCacheDTO.getOverrideMinutes());
						overrideTimeDTO.setOverrideType(OverrideTypeEM.getOverrideTypeEM(overRideCacheDTO.getOverrideTypeCode()));

						overrideControlList.add(overrideTimeDTO);
					}
					timeOverrideDTO.setOverrideList(overrideControlList);
				}
				timeOverrideDTOList.add(timeOverrideDTO);
			}
		}
		return timeOverrideDTOList;
	}
}
