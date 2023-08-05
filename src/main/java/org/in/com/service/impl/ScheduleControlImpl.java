package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleControlCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.dao.ScheduleControlDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StationDTO;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleActivityService;
import org.in.com.service.ScheduleControlService;
import org.in.com.service.StationService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

@Service
public class ScheduleControlImpl extends CacheCentral implements ScheduleControlService {
	@Autowired
	ScheduleActivityService scheduleActivityService;
	@Autowired
	GroupService groupService;
	@Autowired
	StationService stationService;

	public List<ScheduleControlDTO> get(AuthDTO authDTO, ScheduleControlDTO dto) {
		ScheduleControlDAO dao = new ScheduleControlDAO();
		List<ScheduleControlDTO> list = dao.get(authDTO, dto);
		for (ScheduleControlDTO controlDTO : list) {
			if (controlDTO.getGroup() != null) {
				controlDTO.setGroup(groupService.getGroup(authDTO, controlDTO.getGroup()));
			}
			for (ScheduleControlDTO overrideControlDTO : controlDTO.getOverrideList()) {
				if (overrideControlDTO.getGroup() != null) {
					overrideControlDTO.setGroup(groupService.getGroup(authDTO, overrideControlDTO.getGroup()));
				}
			}
			if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0) {
				controlDTO.setFromStation(getStationDTObyId(controlDTO.getFromStation()));
			}
			if (controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0) {
				controlDTO.setToStation(getStationDTObyId(controlDTO.getToStation()));
			}
		}
		return list;
	}

	public ScheduleControlDTO Update(AuthDTO authDTO, ScheduleControlDTO dto) {
		ScheduleControlDAO dao = new ScheduleControlDAO();
		for (ScheduleControlDTO controlDTO : dto.getList()) {
			if (controlDTO.getFromStation() != null && StringUtil.isNotNull(controlDTO.getFromStation().getCode())) {
				controlDTO.setFromStation(stationService.getStation(controlDTO.getFromStation()));
			}
			if (controlDTO.getToStation() != null && StringUtil.isNotNull(controlDTO.getToStation().getCode())) {
				controlDTO.setToStation(stationService.getStation(controlDTO.getToStation()));
			}
			// Activity Activity Log
			scheduleActivityService.scheduleBookingControlActivity(authDTO, controlDTO);
		}
		dao.getIUD(authDTO, dto);

		ScheduleCache scheduleCache = new ScheduleCache();
		for (ScheduleControlDTO controlDTO : dto.getList()) {
			scheduleCache.removeScheduleDTO(authDTO, controlDTO.getSchedule());
		}
		return dto;
	}

	@Override
	public List<ScheduleControlDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO) {
		String CACHEKEY = "SHCON" + scheduleDTO.getCode();
		List<ScheduleControlDTO> scheduleControlDTOList = null;
		List<ScheduleControlCacheDTO> scheduleControlCacheList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleControlCacheList = (List<ScheduleControlCacheDTO>) element.getObjectValue();
			scheduleControlDTOList = bindScheduleControlFromCacheObject(scheduleControlCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleControlDAO controlDAO = new ScheduleControlDAO();
			scheduleControlDTOList = controlDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule Cache
			scheduleControlCacheList = bindScheduleControlToCacheObject(scheduleControlDTOList);
			element = new Element(CACHEKEY, scheduleControlCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		else if (scheduleDTO != null) {
			EhcacheManager.getScheduleEhCache().remove(CACHEKEY);
		}
		return scheduleControlDTOList;
	}

	@Override
	public List<ScheduleControlDTO> getByScheduleTripDate(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate) {
		String CACHEKEY = "SHCON" + scheduleDTO.getCode();
		List<ScheduleControlDTO> scheduleControlDTOList = null;
		List<ScheduleControlCacheDTO> scheduleControlCacheList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleControlCacheList = (List<ScheduleControlCacheDTO>) element.getObjectValue();
			scheduleControlDTOList = bindScheduleControlFromCacheObject(scheduleControlCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleControlDAO controlDAO = new ScheduleControlDAO();
			scheduleControlDTOList = controlDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule Cache
			scheduleControlCacheList = bindScheduleControlToCacheObject(scheduleControlDTOList);
			element = new Element(CACHEKEY, scheduleControlCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		// Validate all Booking Control

		boolean groupLevelFound = false;
		for (Iterator<ScheduleControlDTO> itrControlDTO = scheduleControlDTOList.iterator(); itrControlDTO.hasNext();) {
			ScheduleControlDTO controlDTO = itrControlDTO.next();
			// common validations
			if (controlDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(controlDTO.getActiveFrom()))) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(controlDTO.getActiveTo()))) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getDayOfWeek() != null && controlDTO.getDayOfWeek().length() != 7) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getDayOfWeek() != null && controlDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
				itrControlDTO.remove();
				continue;
			}
			// alternate days
			if (controlDTO.getDayOfWeek().equals("ALRNATE") && !DateUtil.isFallonAlternateDays(new DateTime(controlDTO.getActiveFrom()), tripDate)) {
				itrControlDTO.remove();
				continue;
			}
			// Check for group level or should be default
			if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0 && controlDTO.getGroup().getId() != authDTO.getGroup().getId()) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0 && controlDTO.getGroup().getId() == authDTO.getGroup().getId()) {
				groupLevelFound = true;
			}
			// Override and Exceptions
			for (Iterator<ScheduleControlDTO> overrideItrControlDTO = controlDTO.getOverrideList().iterator(); overrideItrControlDTO.hasNext();) {
				ScheduleControlDTO overrideControlDTO = overrideItrControlDTO.next();
				// common validations
				if (overrideControlDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(overrideControlDTO.getActiveFrom()))) {
					overrideItrControlDTO.remove();
					continue;
				}
				if (overrideControlDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(overrideControlDTO.getActiveTo()))) {
					overrideItrControlDTO.remove();
					continue;
				}
				if (overrideControlDTO.getDayOfWeek() != null && overrideControlDTO.getDayOfWeek().length() != 7) {
					overrideItrControlDTO.remove();
					continue;
				}
				if (overrideControlDTO.getDayOfWeek() != null && overrideControlDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					overrideItrControlDTO.remove();
					continue;
				}
				// exceptions/Override for Group Level
				if (overrideControlDTO.getGroup() != null && overrideControlDTO.getGroup().getId() != 0 && overrideControlDTO.getGroup().getId() != authDTO.getGroup().getId()) {
					overrideItrControlDTO.remove();
					continue;
				}
				// Remove if Exceptions
				if (overrideControlDTO.getOpenMinitues() == -1) {
					itrControlDTO.remove();
					break;
				}
				// Apply Override
				controlDTO.setOpenMinitues(overrideControlDTO.getOpenMinitues());
				controlDTO.setCloseMinitues(overrideControlDTO.getCloseMinitues());
				controlDTO.setAllowBookingFlag(overrideControlDTO.getAllowBookingFlag());
			}
		}
		// Group level validation and check exception
		if (scheduleControlDTOList.size() > 1 && groupLevelFound) {
			// Check for group based schedule Control
			// remove default control, if group level found
			for (Iterator<ScheduleControlDTO> iterator = scheduleControlDTOList.iterator(); iterator.hasNext();) {
				ScheduleControlDTO controlDTO = iterator.next();
				if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != authDTO.getGroup().getId()) {
					iterator.remove();
					continue;
				}
				if (controlDTO.getGroup() == null || controlDTO.getGroup().getId() == 0) {
					iterator.remove();
					continue;
				}
			}
		}
		return scheduleControlDTOList;
	}

	@Override
	public List<ScheduleControlDTO> getAllGroupTripScheduleControl(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate) {
		String CACHEKEY = "SHCON" + scheduleDTO.getCode();
		List<ScheduleControlDTO> scheduleControlDTOList = null;
		List<ScheduleControlCacheDTO> scheduleControlCacheList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleControlCacheList = (List<ScheduleControlCacheDTO>) element.getObjectValue();
			scheduleControlDTOList = bindScheduleControlFromCacheObject(scheduleControlCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleControlDAO controlDAO = new ScheduleControlDAO();
			scheduleControlDTOList = controlDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule Cache
			scheduleControlCacheList = bindScheduleControlToCacheObject(scheduleControlDTOList);
			element = new Element(CACHEKEY, scheduleControlCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		// Validate all Booking Control
		for (Iterator<ScheduleControlDTO> itrControlDTO = scheduleControlDTOList.iterator(); itrControlDTO.hasNext();) {
			ScheduleControlDTO controlDTO = itrControlDTO.next();
			// common validations
			if (controlDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(controlDTO.getActiveFrom()))) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(controlDTO.getActiveTo()))) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getDayOfWeek() != null && controlDTO.getDayOfWeek().length() != 7) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getDayOfWeek() != null && controlDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
				itrControlDTO.remove();
				continue;
			}
			// alternate days
			if (controlDTO.getDayOfWeek().equals("ALRNATE") && !DateUtil.isFallonAlternateDays(new DateTime(controlDTO.getActiveFrom()), tripDate)) {
				itrControlDTO.remove();
				continue;
			}
			// Override and Exceptions
			for (Iterator<ScheduleControlDTO> overrideItrControlDTO = controlDTO.getOverrideList().iterator(); overrideItrControlDTO.hasNext();) {
				ScheduleControlDTO overrideControlDTO = overrideItrControlDTO.next();
				// common validations
				if (overrideControlDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(overrideControlDTO.getActiveFrom()))) {
					overrideItrControlDTO.remove();
					continue;
				}
				if (overrideControlDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(overrideControlDTO.getActiveTo()))) {
					overrideItrControlDTO.remove();
					continue;
				}
				if (overrideControlDTO.getDayOfWeek() != null && overrideControlDTO.getDayOfWeek().length() != 7) {
					overrideItrControlDTO.remove();
					continue;
				}
				if (overrideControlDTO.getDayOfWeek() != null && overrideControlDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					overrideItrControlDTO.remove();
					continue;
				}
				// Remove if Exceptions
				if (overrideControlDTO.getOpenMinitues() == -1) {
					controlDTO.setAllowBookingFlag(-1);
					// itrControlDTO.remove();
					continue;
				}
				// Apply Override
				controlDTO.setOpenMinitues(overrideControlDTO.getOpenMinitues());
				controlDTO.setCloseMinitues(overrideControlDTO.getCloseMinitues());
				controlDTO.setAllowBookingFlag(overrideControlDTO.getAllowBookingFlag());
			}
			if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0) {
				controlDTO.setFromStation(getStationDTObyId(controlDTO.getFromStation()));
				controlDTO.setToStation(getStationDTObyId(controlDTO.getToStation()));
			}
			if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0) {
				controlDTO.setGroup(groupService.getGroup(authDTO, controlDTO.getGroup()));
			}
		}
		return scheduleControlDTOList;
	}

	private List<ScheduleControlCacheDTO> bindScheduleControlToCacheObject(List<ScheduleControlDTO> scheduleControlDTOList) {
		List<ScheduleControlCacheDTO> scheduleControlCacheList = new ArrayList<>();
		// copy to cache
		if (scheduleControlDTOList != null && !scheduleControlDTOList.isEmpty()) {
			for (ScheduleControlDTO controlDTO : scheduleControlDTOList) {
				ScheduleControlCacheDTO controlCacheDTO = new ScheduleControlCacheDTO();
				controlCacheDTO.setCode(controlDTO.getCode());
				controlCacheDTO.setActiveFrom(controlDTO.getActiveFrom());
				controlCacheDTO.setActiveTo(controlDTO.getActiveTo());
				controlCacheDTO.setDayOfWeek(controlDTO.getDayOfWeek());
				controlCacheDTO.setAllowBookingFlag(controlDTO.getAllowBookingFlag());
				controlCacheDTO.setCloseMinitues(controlDTO.getCloseMinitues());
				controlCacheDTO.setOpenMinitues(controlDTO.getOpenMinitues());
				controlCacheDTO.setGroupId(controlDTO.getGroup() != null ? controlDTO.getGroup().getId() : 0);
				controlCacheDTO.setFromStationId(controlDTO.getFromStation() != null ? controlDTO.getFromStation().getId() : 0);
				controlCacheDTO.setToStationId(controlDTO.getToStation() != null ? controlDTO.getToStation().getId() : 0);
				List<ScheduleControlCacheDTO> overrideControlList = new ArrayList<>();
				if (controlDTO.getOverrideList() != null && !controlDTO.getOverrideList().isEmpty()) {
					for (ScheduleControlDTO scheduleControlDTO : controlDTO.getOverrideList()) {
						ScheduleControlCacheDTO cacheDTO = new ScheduleControlCacheDTO();
						cacheDTO.setCode(scheduleControlDTO.getCode());
						cacheDTO.setActiveFrom(scheduleControlDTO.getActiveFrom());
						cacheDTO.setActiveTo(scheduleControlDTO.getActiveTo());
						cacheDTO.setDayOfWeek(scheduleControlDTO.getDayOfWeek());
						cacheDTO.setAllowBookingFlag(scheduleControlDTO.getAllowBookingFlag());
						cacheDTO.setCloseMinitues(scheduleControlDTO.getCloseMinitues());
						cacheDTO.setOpenMinitues(scheduleControlDTO.getOpenMinitues());
						cacheDTO.setGroupId(scheduleControlDTO.getGroup() != null ? controlDTO.getGroup().getId() : 0);
						cacheDTO.setFromStationId(scheduleControlDTO.getFromStation() != null ? scheduleControlDTO.getFromStation().getId() : 0);
						cacheDTO.setToStationId(scheduleControlDTO.getToStation() != null ? scheduleControlDTO.getToStation().getId() : 0);
						overrideControlList.add(cacheDTO);
					}
					controlCacheDTO.setOverrideListControlCacheDTO(overrideControlList);
				}
				scheduleControlCacheList.add(controlCacheDTO);
			}
		}
		return scheduleControlCacheList;
	}

	private List<ScheduleControlDTO> bindScheduleControlFromCacheObject(List<ScheduleControlCacheDTO> controlCacheDTOList) {
		List<ScheduleControlDTO> scheduleControlDTOList = new ArrayList<>();
		if (controlCacheDTOList != null && !controlCacheDTOList.isEmpty()) {
			// copy from cache
			for (ScheduleControlCacheDTO controlCache : controlCacheDTOList) {
				ScheduleControlDTO controlDTO = new ScheduleControlDTO();
				controlDTO.setCode(controlCache.getCode());
				controlDTO.setActiveFlag(Numeric.ONE_INT);
				controlDTO.setActiveFrom(controlCache.getActiveFrom());
				controlDTO.setActiveTo(controlCache.getActiveTo());
				controlDTO.setDayOfWeek(controlCache.getDayOfWeek());
				controlDTO.setAllowBookingFlag(controlCache.getAllowBookingFlag());
				controlDTO.setCloseMinitues(controlCache.getCloseMinitues());
				controlDTO.setOpenMinitues(controlCache.getOpenMinitues());
				if (controlCache.getGroupId() != 0) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(controlCache.getGroupId());
					controlDTO.setGroup(groupDTO);
				}
				if (controlCache.getFromStationId() != 0 && controlCache.getToStationId() != 0) {
					StationDTO fromStationDTO = new StationDTO();
					StationDTO toStationDTO = new StationDTO();
					fromStationDTO.setId(controlCache.getFromStationId());
					toStationDTO.setId(controlCache.getToStationId());
					controlDTO.setFromStation(fromStationDTO);
					controlDTO.setToStation(toStationDTO);
				}
				controlDTO.setLookupCode(controlDTO.getLookupCode());
				List<ScheduleControlDTO> overrideControlList = new ArrayList<>();
				if (controlCache.getOverrideListControlCacheDTO() != null && !controlCache.getOverrideListControlCacheDTO().isEmpty()) {
					for (ScheduleControlCacheDTO overRideCacheDTO : controlCache.getOverrideListControlCacheDTO()) {
						ScheduleControlDTO overrideControlDTO = new ScheduleControlDTO();
						overrideControlDTO.setCode(overRideCacheDTO.getCode());
						overrideControlDTO.setActiveFlag(Numeric.ONE_INT);
						overrideControlDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideControlDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideControlDTO.setDayOfWeek(overRideCacheDTO.getDayOfWeek());
						overrideControlDTO.setAllowBookingFlag(overRideCacheDTO.getAllowBookingFlag());
						overrideControlDTO.setCloseMinitues(overRideCacheDTO.getCloseMinitues());
						overrideControlDTO.setOpenMinitues(overRideCacheDTO.getOpenMinitues());
						if (overRideCacheDTO.getGroupId() != 0) {
							GroupDTO groupDTO = new GroupDTO();
							groupDTO.setId(overRideCacheDTO.getGroupId());
							overrideControlDTO.setGroup(groupDTO);
						}
						if (overRideCacheDTO.getFromStationId() != 0 && overRideCacheDTO.getToStationId() != 0) {
							StationDTO fromStationDTO = new StationDTO();
							StationDTO toStationDTO = new StationDTO();
							fromStationDTO.setId(overRideCacheDTO.getFromStationId());
							toStationDTO.setId(overRideCacheDTO.getToStationId());
							overrideControlDTO.setFromStation(fromStationDTO);
							overrideControlDTO.setToStation(toStationDTO);
						}
						overrideControlList.add(overrideControlDTO);
					}
					controlDTO.setOverrideList(overrideControlList);
				}
				scheduleControlDTOList.add(controlDTO);
			}
		}
		return scheduleControlDTOList;
	}

}
