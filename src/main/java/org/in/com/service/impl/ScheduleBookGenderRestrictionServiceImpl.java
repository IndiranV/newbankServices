package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.beanutils.BeanUtils;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleBookGenderRestrictionCacheDTO;
import org.in.com.dao.ScheduleBookGenderRestrictionDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleBookGenderRestrictionDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TripDTO;
import org.in.com.exception.ServiceException;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleBookGenderRestrictionService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import net.sf.ehcache.Element;

@Service
public class ScheduleBookGenderRestrictionServiceImpl extends ScheduleCache implements ScheduleBookGenderRestrictionService {
	@Autowired
	GroupService groupService;
	private static String CACHEKEY = "SHBGR_";

	@Override
	public List<ScheduleBookGenderRestrictionDTO> get(AuthDTO authDTO, ScheduleBookGenderRestrictionDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<ScheduleBookGenderRestrictionDTO> getAll(AuthDTO authDTO) {
		List<ScheduleBookGenderRestrictionDTO> list = new ArrayList<>();
		try {
			ScheduleBookGenderRestrictionDAO scheduleBookGenderRestrictionDAO = new ScheduleBookGenderRestrictionDAO();
			list = scheduleBookGenderRestrictionDAO.getScheduleBookGenderRestriction(authDTO);
			for (ScheduleBookGenderRestrictionDTO scheduleBookGenderRestrictionDTO : list) {
				for (ScheduleDTO scheduleDTO : scheduleBookGenderRestrictionDTO.getScheduleList()) {
					getScheduleDTO(authDTO, scheduleDTO);
				}
				for (GroupDTO groupDTO : scheduleBookGenderRestrictionDTO.getGroupList()) {
					GroupDTO groupCache = groupService.getGroup(authDTO, groupDTO);
					BeanUtils.copyProperties(groupDTO, groupCache);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	@Override
	public ScheduleBookGenderRestrictionDTO Update(AuthDTO authDTO, ScheduleBookGenderRestrictionDTO scheduleBookGenderRestriction) {
		ScheduleBookGenderRestrictionDAO scheduleBookGenderRestrictionDAO = new ScheduleBookGenderRestrictionDAO();
		scheduleBookGenderRestrictionDAO.updateScheduleBookGenderRestriction(authDTO, scheduleBookGenderRestriction);

		// Clear Cache
		EhcacheManager.getScheduleEhCache().remove(CACHEKEY + authDTO.getNamespaceCode());
		return scheduleBookGenderRestriction;
	}

	@Override
	public ScheduleBookGenderRestrictionDTO getScheduleBookGenderRestrictionBySchedule(AuthDTO authDTO, TripDTO tripDTO) {
		ScheduleBookGenderRestrictionDTO scheduleBookGenderRestrictionDTO = null;
		String cacheKey = CACHEKEY + authDTO.getNamespaceCode();
		List<ScheduleBookGenderRestrictionDTO> scheduleBookGenderRestrictionList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(cacheKey);
		ScheduleDTO scheduleDTO = tripDTO.getSchedule();

		if (element != null) {
			List<ScheduleBookGenderRestrictionCacheDTO> scheduleBookGenderRestrictionCacheList = (List<ScheduleBookGenderRestrictionCacheDTO>) element.getObjectValue();
			scheduleBookGenderRestrictionList = bindScheduleBookGenderRestrictionFromCache(authDTO, scheduleBookGenderRestrictionCacheList);
		}
		else {
			ScheduleBookGenderRestrictionDAO scheduleBookGenderRestrictionDAO = new ScheduleBookGenderRestrictionDAO();
			scheduleBookGenderRestrictionList = scheduleBookGenderRestrictionDAO.getScheduleBookGenderRestriction(authDTO);
			List<ScheduleBookGenderRestrictionCacheDTO> scheduleBookGenderRestrictionCacheList = copyScheduleBookGenderRestrictionToCache(authDTO, scheduleBookGenderRestrictionList);
			element = new Element(cacheKey, scheduleBookGenderRestrictionCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		for (Iterator<ScheduleBookGenderRestrictionDTO> iterator = scheduleBookGenderRestrictionList.iterator(); iterator.hasNext();) {
			ScheduleBookGenderRestrictionDTO bookGenderRestrictionDTO = iterator.next();

			if (bookGenderRestrictionDTO.getDayOfWeek() != null && bookGenderRestrictionDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (bookGenderRestrictionDTO.getDayOfWeek() != null && bookGenderRestrictionDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Group List
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !bookGenderRestrictionDTO.getGroupList().isEmpty() && existGroupInGroupList(bookGenderRestrictionDTO.getGroupList(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}
			// Schedule List
			if (!bookGenderRestrictionDTO.getScheduleList().isEmpty() && existScheduleInScheduleList(bookGenderRestrictionDTO.getScheduleList(), scheduleDTO) == null) {
				iterator.remove();
				continue;
			}
			// Release Time
			int minutesDiff = DateUtil.getMinutiesDifferent(DateUtil.NOW(), tripDTO.getTripDateTimeV2());
			if (bookGenderRestrictionDTO.getReleaseMinutes() != 0 && bookGenderRestrictionDTO.getReleaseMinutes() >= minutesDiff) {
				iterator.remove();
				continue;
			}
		}

		if (!scheduleBookGenderRestrictionList.isEmpty()) {
			scheduleBookGenderRestrictionDTO = Iterables.getFirst(scheduleBookGenderRestrictionList, null);
		}

		return scheduleBookGenderRestrictionDTO;
	}

	private List<ScheduleBookGenderRestrictionCacheDTO> copyScheduleBookGenderRestrictionToCache(AuthDTO authDTO, List<ScheduleBookGenderRestrictionDTO> scheduleBookGenderRestrictionList) {
		List<ScheduleBookGenderRestrictionCacheDTO> scheduleBookGenderRestrictionCacheList = new ArrayList<>();
		for (ScheduleBookGenderRestrictionDTO scheduleBookGenderRestrictionDTO : scheduleBookGenderRestrictionList) {
			ScheduleBookGenderRestrictionCacheDTO scheduleBookGenderRestrictionCacheDTO = new ScheduleBookGenderRestrictionCacheDTO();
			scheduleBookGenderRestrictionCacheDTO.setCode(scheduleBookGenderRestrictionDTO.getCode());
			scheduleBookGenderRestrictionCacheDTO.setDayOfWeek(scheduleBookGenderRestrictionDTO.getDayOfWeek());
			scheduleBookGenderRestrictionCacheDTO.setReleaseMinutes(scheduleBookGenderRestrictionDTO.getReleaseMinutes());
			scheduleBookGenderRestrictionCacheDTO.setFemaleSeatCount(scheduleBookGenderRestrictionDTO.getFemaleSeatCount());
			scheduleBookGenderRestrictionCacheDTO.setSeatTypeGroupModel(scheduleBookGenderRestrictionDTO.getSeatTypeGroupModel());

			List<String> scheduleList = new ArrayList<>();
			for (ScheduleDTO scheduleDTO : scheduleBookGenderRestrictionDTO.getScheduleList()) {
				if (StringUtil.isNull(scheduleDTO.getCode())) {
					continue;
				}
				scheduleList.add(scheduleDTO.getCode());
			}

			List<String> groupList = new ArrayList<>();
			for (GroupDTO groupDTO : scheduleBookGenderRestrictionDTO.getGroupList()) {
				if (StringUtil.isNull(groupDTO.getCode())) {
					continue;
				}
				groupList.add(groupDTO.getCode());
			}

			scheduleBookGenderRestrictionCacheDTO.setScheduleList(scheduleList);
			scheduleBookGenderRestrictionCacheDTO.setGroupList(groupList);
			scheduleBookGenderRestrictionCacheDTO.setActiveFlag(scheduleBookGenderRestrictionDTO.getActiveFlag());
			scheduleBookGenderRestrictionCacheList.add(scheduleBookGenderRestrictionCacheDTO);
		}
		return scheduleBookGenderRestrictionCacheList;
	}

	private List<ScheduleBookGenderRestrictionDTO> bindScheduleBookGenderRestrictionFromCache(AuthDTO authDTO, List<ScheduleBookGenderRestrictionCacheDTO> scheduleBookGenderRestrictionCacheList) {
		List<ScheduleBookGenderRestrictionDTO> scheduleBookGenderRestrictionList = new ArrayList<>();
		for (ScheduleBookGenderRestrictionCacheDTO scheduleBookGenderRestrictionCacheDTO : scheduleBookGenderRestrictionCacheList) {
			ScheduleBookGenderRestrictionDTO scheduleBookGenderRestrictionDTO = new ScheduleBookGenderRestrictionDTO();
			scheduleBookGenderRestrictionDTO.setCode(scheduleBookGenderRestrictionCacheDTO.getCode());
			scheduleBookGenderRestrictionDTO.setDayOfWeek(scheduleBookGenderRestrictionCacheDTO.getDayOfWeek());
			scheduleBookGenderRestrictionDTO.setReleaseMinutes(scheduleBookGenderRestrictionCacheDTO.getReleaseMinutes());
			scheduleBookGenderRestrictionDTO.setFemaleSeatCount(scheduleBookGenderRestrictionCacheDTO.getFemaleSeatCount());
			scheduleBookGenderRestrictionDTO.setSeatTypeGroupModel(scheduleBookGenderRestrictionCacheDTO.getSeatTypeGroupModel());
			
			List<ScheduleDTO> scheduleList = new ArrayList<>();
			for (String scheduleCode : scheduleBookGenderRestrictionCacheDTO.getScheduleList()) {
				if (StringUtil.isNull(scheduleCode)) {
					continue;
				}
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(scheduleCode);
				scheduleList.add(scheduleDTO);
			}

			List<GroupDTO> groupList = new ArrayList<>();
			for (String groupCode : scheduleBookGenderRestrictionCacheDTO.getGroupList()) {
				if (StringUtil.isNull(groupCode)) {
					continue;
				}
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(groupCode);
				groupList.add(groupDTO);
			}

			scheduleBookGenderRestrictionDTO.setScheduleList(scheduleList);
			scheduleBookGenderRestrictionDTO.setGroupList(groupList);
			scheduleBookGenderRestrictionDTO.setActiveFlag(scheduleBookGenderRestrictionCacheDTO.getActiveFlag());
			scheduleBookGenderRestrictionList.add(scheduleBookGenderRestrictionDTO);
		}
		return scheduleBookGenderRestrictionList;
	}

	protected GroupDTO existGroupInGroupList(List<GroupDTO> groupList, GroupDTO groupDTO) {
		GroupDTO existingGroup = null;
		// Group List
		for (GroupDTO group : groupList) {
			if (group.getCode().equals(groupDTO.getCode())) {
				existingGroup = group;
				break;
			}
		}
		return existingGroup;
	}

	protected ScheduleDTO existScheduleInScheduleList(List<ScheduleDTO> scheduleList, ScheduleDTO scheduleDTO) {
		ScheduleDTO existingSchedule = null;
		// Schedule List
		for (ScheduleDTO schedule : scheduleList) {
			if (schedule.getCode().equals(scheduleDTO.getCode())) {
				existingSchedule = schedule;
				break;
			}
		}
		return existingSchedule;
	}
}
