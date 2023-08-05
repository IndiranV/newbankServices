package org.in.com.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.cache.ScheduleTagCache;
import org.in.com.dao.ScheduleTagDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleTagDTO;
import org.in.com.service.ScheduleTagService;
import org.springframework.stereotype.Service;

@Service
public class ScheduleTagServiceImpl extends ScheduleTagCache implements ScheduleTagService {

	public List<ScheduleTagDTO> get(AuthDTO authDTO, ScheduleTagDTO scheduleTagDTO) {
		ScheduleTagDAO scheduleTagDAO = new ScheduleTagDAO();
		scheduleTagDAO.getScheduleTag(authDTO, scheduleTagDTO);
		return null;
	}

	public List<ScheduleTagDTO> getAll(AuthDTO authDTO) {
		ScheduleTagDAO scheduleTagDAO = new ScheduleTagDAO();
		return scheduleTagDAO.getAll(authDTO);
	}

	public ScheduleTagDTO Update(AuthDTO authDTO, ScheduleTagDTO scheduleTagDTO) {
		ScheduleTagDAO scheduleTagDAO = new ScheduleTagDAO();
		scheduleTagDAO.getScheduleTagUpdate(authDTO, scheduleTagDTO);
		// clear cache
		removeScheduleTags(authDTO);
		return scheduleTagDTO;
	}

	public void getScheduleTagsById(AuthDTO authDTO, List<ScheduleTagDTO> scheduleTagList) {
		Map<Integer, ScheduleTagDTO> scheduleTagMap = new HashMap<Integer, ScheduleTagDTO>();
		List<ScheduleTagDTO> tagCacheList = getScheduleTags(authDTO);
		for (ScheduleTagDTO scheduleTagDTO : tagCacheList) {
			scheduleTagMap.put(scheduleTagDTO.getId(), scheduleTagDTO);
		}

		for (ScheduleTagDTO scheduleTag : scheduleTagList) {
			ScheduleTagDTO scheduleTagCache = scheduleTagMap.get(scheduleTag.getId());
			if (scheduleTagCache == null) {
				continue;
			}
			scheduleTag.setCode(scheduleTagCache.getCode());
			scheduleTag.setName(scheduleTagCache.getName());
			scheduleTag.setActiveFlag(scheduleTagCache.getActiveFlag());
		}
	}

	public void getScheduleTagsByCode(AuthDTO authDTO, List<ScheduleTagDTO> scheduleTagList) {
		Map<String, ScheduleTagDTO> scheduleTagMap = new HashMap<String, ScheduleTagDTO>();
		List<ScheduleTagDTO> tagCacheList = getScheduleTags(authDTO);
		for (ScheduleTagDTO scheduleTagDTO : tagCacheList) {
			scheduleTagMap.put(scheduleTagDTO.getCode(), scheduleTagDTO);
		}

		for (ScheduleTagDTO scheduleTag : scheduleTagList) {
			ScheduleTagDTO scheduleTagCache = scheduleTagMap.get(scheduleTag.getCode());
			if (scheduleTagCache == null) {
				continue;
			}
			scheduleTag.setId(scheduleTagCache.getId());
			scheduleTag.setActiveFlag(scheduleTagCache.getActiveFlag());
		}
	}

}
