package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleTagDTO;

public interface ScheduleTagService extends BaseService<ScheduleTagDTO> {

	public void getScheduleTagsById(AuthDTO authDTO, List<ScheduleTagDTO> scheduleTagList);
	
	public void getScheduleTagsByCode(AuthDTO authDTO, List<ScheduleTagDTO> scheduleTagList);
}
