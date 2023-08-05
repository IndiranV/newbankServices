package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.in.com.cache.EhcacheManager;
import org.in.com.cache.dto.ScheduleSeatAutoReleaseCacheDTO;
import org.in.com.dao.ScheduleSeatAutoReleaseDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatAutoReleaseDTO;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.ReleaseModeEM;
import org.in.com.dto.enumeration.ReleaseTypeEM;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleSeatAutoReleaseService;
import org.in.com.service.ScheduleService;
import org.in.com.utils.BitsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

@Service
public class ScheduleSeatAutoReleaseImpl implements ScheduleSeatAutoReleaseService {
	@Autowired
	GroupService groupService;
	@Lazy
	@Autowired
	ScheduleService scheduleService;
	private static String CACHEKEY = "SHSEAR";

	@Override
	public List<ScheduleSeatAutoReleaseDTO> getAllScheduleSeatAutoRelease(AuthDTO authDTO) {
		ScheduleSeatAutoReleaseDAO dao = new ScheduleSeatAutoReleaseDAO();
		List<ScheduleSeatAutoReleaseDTO> list = dao.getAllScheduleSeatAutoRelease(authDTO);
		for (ScheduleSeatAutoReleaseDTO seatAutoReleaseDTO : list) {
			for (GroupDTO groupDTO : seatAutoReleaseDTO.getGroups()) {
				groupDTO = groupService.getGroup(authDTO, groupDTO);
			}
			for (ScheduleDTO scheduleDTO : seatAutoReleaseDTO.getSchedules()) {
				scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);
			}
			for (ScheduleSeatAutoReleaseDTO overrideAutoReleaseDTO : seatAutoReleaseDTO.getOverrideList()) {
				for (GroupDTO groupDTO : overrideAutoReleaseDTO.getGroups()) {
					groupDTO = groupService.getGroup(authDTO, groupDTO);
				}
				for (ScheduleDTO scheduleDTO : overrideAutoReleaseDTO.getSchedules()) {
					scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);
				}
			}

		}

		return list;
	}

	@Override
	public ScheduleSeatAutoReleaseDTO Update(AuthDTO authDTO, ScheduleSeatAutoReleaseDTO autoReleaseDTO) {
		for (GroupDTO groupDTO : autoReleaseDTO.getGroups()) {
			groupDTO = groupService.getGroup(authDTO, groupDTO);
		}
		for (ScheduleDTO scheduleDTO : autoReleaseDTO.getSchedules()) {
			scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);
		}

		ScheduleSeatAutoReleaseDAO dao = new ScheduleSeatAutoReleaseDAO();
		dao.getIUD(authDTO, autoReleaseDTO);

		EhcacheManager.getScheduleEhCache().remove(CACHEKEY + authDTO.getNamespaceCode());
		return autoReleaseDTO;
	}

	@Override
	public List<ScheduleSeatAutoReleaseDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String key = CACHEKEY + authDTO.getNamespaceCode();
		List<ScheduleSeatAutoReleaseDTO> seatAutoReleaseDTOList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleSeatAutoReleaseCacheDTO> scheduleSeatAutoReleaseCacheList = (List<ScheduleSeatAutoReleaseCacheDTO>) element.getObjectValue();
			seatAutoReleaseDTOList = bindSeatAutoReleaseFromCacheObject(scheduleSeatAutoReleaseCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleSeatAutoReleaseDAO releaseDAO = new ScheduleSeatAutoReleaseDAO();
			seatAutoReleaseDTOList = releaseDAO.getAllScheduleSeatAutoRelease(authDTO);

			List<ScheduleSeatAutoReleaseCacheDTO> scheduleSeatAutoReleaseCacheList = bindSeatAutoReleaseToCacheObject(seatAutoReleaseDTOList);
			element = new Element(key, scheduleSeatAutoReleaseCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		// Validate all Seat visibility
		for (Iterator<ScheduleSeatAutoReleaseDTO> iterator = seatAutoReleaseDTOList.iterator(); iterator.hasNext();) {
			ScheduleSeatAutoReleaseDTO autoReleaseDTO = iterator.next();
			if (!autoReleaseDTO.getSchedules().isEmpty() && BitsUtil.isScheduleExists(autoReleaseDTO.getSchedules(), scheduleDTO) == null) {
				iterator.remove();
				continue;
			}
			if (autoReleaseDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(autoReleaseDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (autoReleaseDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(autoReleaseDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (autoReleaseDTO.getDayOfWeek() != null && autoReleaseDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (autoReleaseDTO.getDayOfWeek() != null && autoReleaseDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}

			// Exceptions and Override
			for (Iterator<ScheduleSeatAutoReleaseDTO> overrideIterator = autoReleaseDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleSeatAutoReleaseDTO overrideSeatAutoReleaseDTO = overrideIterator.next();
				if (!scheduleDTO.getTripDate().gteq(new DateTime(overrideSeatAutoReleaseDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!scheduleDTO.getTripDate().lteq(new DateTime(overrideSeatAutoReleaseDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideSeatAutoReleaseDTO.getDayOfWeek() != null && overrideSeatAutoReleaseDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideSeatAutoReleaseDTO.getDayOfWeek() != null && overrideSeatAutoReleaseDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}
				// Apply Exceptions
				if (overrideSeatAutoReleaseDTO.getList() == null || overrideSeatAutoReleaseDTO.getList().isEmpty()) {
					iterator.remove();
					break;
				}
				else {
					// Apply Override
					autoReleaseDTO.setList(overrideSeatAutoReleaseDTO.getList());
				}

			}
		}
		return seatAutoReleaseDTOList;
	}

	private List<ScheduleSeatAutoReleaseCacheDTO> bindSeatAutoReleaseToCacheObject(List<ScheduleSeatAutoReleaseDTO> seatAutoReleaseDTOList) {
		List<ScheduleSeatAutoReleaseCacheDTO> seatVisibilityCacheList = new ArrayList<>();
		// copy to cache
		if (seatAutoReleaseDTOList != null && !seatAutoReleaseDTOList.isEmpty()) {
			for (ScheduleSeatAutoReleaseDTO seatVisibilityDTO : seatAutoReleaseDTOList) {
				ScheduleSeatAutoReleaseCacheDTO seatVisibilityCacheDTO = new ScheduleSeatAutoReleaseCacheDTO();
				seatVisibilityCacheDTO.setActiveFlag(seatVisibilityDTO.getActiveFlag());
				seatVisibilityCacheDTO.setId(seatVisibilityDTO.getId());
				seatVisibilityCacheDTO.setCode(seatVisibilityDTO.getCode());
				seatVisibilityCacheDTO.setActiveFrom(seatVisibilityDTO.getActiveFrom());
				seatVisibilityCacheDTO.setActiveTo(seatVisibilityDTO.getActiveTo());
				seatVisibilityCacheDTO.setDayOfWeek(seatVisibilityDTO.getDayOfWeek());

				seatVisibilityCacheDTO.setReleaseMinutes(seatVisibilityDTO.getReleaseMinutes());
				seatVisibilityCacheDTO.setMinutesTypeCode(seatVisibilityDTO.getMinutesTypeEM().getCode());
				seatVisibilityCacheDTO.setReleaseModeCode(seatVisibilityDTO.getReleaseModeEM().getCode());
				seatVisibilityCacheDTO.setReleaseTypeCode(seatVisibilityDTO.getReleaseTypeEM().getCode());

				List<Integer> groups = new ArrayList<Integer>();
				for (GroupDTO groupDTO : seatVisibilityDTO.getGroups()) {
					groups.add(groupDTO.getId());
				}
				seatVisibilityCacheDTO.setGroupId(groups);

				List<Integer> schedules = new ArrayList<Integer>();
				for (ScheduleDTO scheduleDTO : seatVisibilityDTO.getSchedules()) {
					schedules.add(scheduleDTO.getId());
				}
				seatVisibilityCacheDTO.setScheduleId(schedules);

				List<ScheduleSeatAutoReleaseCacheDTO> overrideControlList = new ArrayList<>();
				if (seatVisibilityDTO.getOverrideList() != null && !seatVisibilityDTO.getOverrideList().isEmpty()) {
					for (ScheduleSeatAutoReleaseDTO overrideSeatVisibilityDTO : seatVisibilityDTO.getOverrideList()) {
						ScheduleSeatAutoReleaseCacheDTO cacheDTO = new ScheduleSeatAutoReleaseCacheDTO();
						cacheDTO.setActiveFlag(overrideSeatVisibilityDTO.getActiveFlag());
						cacheDTO.setId(overrideSeatVisibilityDTO.getId());
						cacheDTO.setCode(overrideSeatVisibilityDTO.getCode());
						cacheDTO.setActiveFrom(overrideSeatVisibilityDTO.getActiveFrom());
						cacheDTO.setActiveTo(overrideSeatVisibilityDTO.getActiveTo());
						cacheDTO.setDayOfWeek(overrideSeatVisibilityDTO.getDayOfWeek());

						// cacheDTO.setReleaseMinutes(overrideSeatVisibilityDTO.getReleaseMinutes());
						// cacheDTO.setMinutesTypeCode(overrideSeatVisibilityDTO.getMinutesTypeEM().getCode());
						// cacheDTO.setReleaseModeCode(overrideSeatVisibilityDTO.getReleaseModeEM().getCode());
						//
						List<Integer> overrideGroups = new ArrayList<Integer>();
						for (GroupDTO groupDTO : overrideSeatVisibilityDTO.getGroups()) {
							overrideGroups.add(groupDTO.getId());
						}
						cacheDTO.setGroupId(overrideGroups);

						List<Integer> overrideSchedules = new ArrayList<Integer>();
						for (ScheduleDTO scheduleDTO : overrideSeatVisibilityDTO.getSchedules()) {
							overrideSchedules.add(scheduleDTO.getId());
						}
						cacheDTO.setScheduleId(overrideSchedules);
						overrideControlList.add(cacheDTO);
					}
					seatVisibilityCacheDTO.setOverrideList(overrideControlList);
				}
				seatVisibilityCacheList.add(seatVisibilityCacheDTO);
			}
		}
		return seatVisibilityCacheList;
	}

	private List<ScheduleSeatAutoReleaseDTO> bindSeatAutoReleaseFromCacheObject(List<ScheduleSeatAutoReleaseCacheDTO> seatAutoReleaseCacheDTOList) {
		List<ScheduleSeatAutoReleaseDTO> ScheduleSeatAutoReleaseDTOList = new ArrayList<>();
		if (seatAutoReleaseCacheDTOList != null && !seatAutoReleaseCacheDTOList.isEmpty()) {
			// copy from cache
			for (ScheduleSeatAutoReleaseCacheDTO seatReleaseCache : seatAutoReleaseCacheDTOList) {
				ScheduleSeatAutoReleaseDTO seatAutoReleaseDTO = new ScheduleSeatAutoReleaseDTO();
				seatAutoReleaseDTO.setCode(seatReleaseCache.getCode());
				seatAutoReleaseDTO.setActiveFrom(seatReleaseCache.getActiveFrom());
				seatAutoReleaseDTO.setActiveTo(seatReleaseCache.getActiveTo());
				seatAutoReleaseDTO.setDayOfWeek(seatReleaseCache.getDayOfWeek());

				seatAutoReleaseDTO.setMinutesTypeEM(MinutesTypeEM.getMinutesTypeEM(seatReleaseCache.getMinutesTypeCode()));
				seatAutoReleaseDTO.setReleaseMinutes(seatReleaseCache.getReleaseMinutes());
				seatAutoReleaseDTO.setReleaseModeEM(ReleaseModeEM.getReleaseModeEM(seatReleaseCache.getReleaseModeCode()));
				seatAutoReleaseDTO.setReleaseTypeEM(ReleaseTypeEM.getReleaseTypeEM(seatReleaseCache.getReleaseTypeCode()));

				List<GroupDTO> groups = new ArrayList<>();
				for (Integer groupId : seatReleaseCache.getGroupId()) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(groupId);
					groups.add(groupDTO);
				}
				seatAutoReleaseDTO.setGroups(groups);

				List<ScheduleDTO> schedules = new ArrayList<>();
				for (Integer scheduleId : seatReleaseCache.getScheduleId()) {
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setId(scheduleId);
					schedules.add(scheduleDTO);
				}
				seatAutoReleaseDTO.setSchedules(schedules);

				seatAutoReleaseDTO.setLookupCode(seatAutoReleaseDTO.getLookupCode());
				List<ScheduleSeatAutoReleaseDTO> overrideControlList = new ArrayList<>();
				if (seatReleaseCache.getOverrideList() != null && !seatReleaseCache.getOverrideList().isEmpty()) {
					for (ScheduleSeatAutoReleaseCacheDTO overRideCacheDTO : seatReleaseCache.getOverrideList()) {
						ScheduleSeatAutoReleaseDTO overrideControlDTO = new ScheduleSeatAutoReleaseDTO();
						overrideControlDTO.setCode(overRideCacheDTO.getCode());
						overrideControlDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideControlDTO.setActiveTo(overRideCacheDTO.getActiveTo());

						overrideControlDTO.setMinutesTypeEM(MinutesTypeEM.getMinutesTypeEM(overRideCacheDTO.getMinutesTypeCode()));
						overrideControlDTO.setReleaseMinutes(overRideCacheDTO.getReleaseMinutes());
						overrideControlDTO.setReleaseModeEM(ReleaseModeEM.getReleaseModeEM(overRideCacheDTO.getReleaseModeCode()));
						overrideControlDTO.setReleaseTypeEM(ReleaseTypeEM.getReleaseTypeEM(overRideCacheDTO.getReleaseTypeCode()));

						List<GroupDTO> overrideGroups = new ArrayList<>();
						for (Integer groupId : overRideCacheDTO.getGroupId()) {
							GroupDTO groupDTO = new GroupDTO();
							groupDTO.setId(groupId);
							overrideGroups.add(groupDTO);
						}
						overrideControlDTO.setGroups(overrideGroups);

						List<ScheduleDTO> overrideSchedules = new ArrayList<>();
						for (Integer scheduleId : overRideCacheDTO.getScheduleId()) {
							ScheduleDTO scheduleDTO = new ScheduleDTO();
							scheduleDTO.setId(scheduleId);
							overrideSchedules.add(scheduleDTO);
						}
						overrideControlDTO.setSchedules(overrideSchedules);
						overrideControlList.add(overrideControlDTO);
					}
					seatAutoReleaseDTO.setOverrideList(overrideControlList);
				}
				ScheduleSeatAutoReleaseDTOList.add(seatAutoReleaseDTO);
			}
		}
		return ScheduleSeatAutoReleaseDTOList;
	}

	@Override
	public List<ScheduleSeatAutoReleaseDTO> getByScheduleTripDate(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate) {
		String key = CACHEKEY + authDTO.getNamespaceCode();
		List<ScheduleSeatAutoReleaseDTO> seatAutoReleaseDTOList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleSeatAutoReleaseCacheDTO> scheduleSeatAutoReleaseCacheList = (List<ScheduleSeatAutoReleaseCacheDTO>) element.getObjectValue();
			seatAutoReleaseDTOList = bindSeatAutoReleaseFromCacheObject(scheduleSeatAutoReleaseCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleSeatAutoReleaseDAO releaseDAO = new ScheduleSeatAutoReleaseDAO();
			seatAutoReleaseDTOList = releaseDAO.getAllScheduleSeatAutoRelease(authDTO);

			List<ScheduleSeatAutoReleaseCacheDTO> scheduleSeatAutoReleaseCacheList = bindSeatAutoReleaseToCacheObject(seatAutoReleaseDTOList);
			element = new Element(key, scheduleSeatAutoReleaseCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		// Validate all Seat visibility
		for (Iterator<ScheduleSeatAutoReleaseDTO> iterator = seatAutoReleaseDTOList.iterator(); iterator.hasNext();) {
			ScheduleSeatAutoReleaseDTO autoReleaseDTO = iterator.next();
			// common validations
			if (!autoReleaseDTO.getSchedules().isEmpty() && BitsUtil.isScheduleExists(autoReleaseDTO.getSchedules(), scheduleDTO) == null) {
				iterator.remove();
				continue;
			}
			if (autoReleaseDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(autoReleaseDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (autoReleaseDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(autoReleaseDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (autoReleaseDTO.getDayOfWeek() != null && autoReleaseDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (autoReleaseDTO.getDayOfWeek() != null && autoReleaseDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}

			// Exceptions and Override
			for (Iterator<ScheduleSeatAutoReleaseDTO> overrideIterator = autoReleaseDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleSeatAutoReleaseDTO overrideSeatAutoReleaseDTO = overrideIterator.next();
				if (!tripDate.gteq(new DateTime(overrideSeatAutoReleaseDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!tripDate.lteq(new DateTime(overrideSeatAutoReleaseDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideSeatAutoReleaseDTO.getDayOfWeek() != null && overrideSeatAutoReleaseDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideSeatAutoReleaseDTO.getDayOfWeek() != null && overrideSeatAutoReleaseDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}
				// Apply Exceptions
				if (overrideSeatAutoReleaseDTO.getList() == null || overrideSeatAutoReleaseDTO.getList().isEmpty()) {
					iterator.remove();
					break;
				}
				else {
					// Apply Override
					autoReleaseDTO.setList(overrideSeatAutoReleaseDTO.getList());
				}

			}
		}
		return seatAutoReleaseDTOList;
	}
}
