package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleTimeOverrideDTO;

public interface ScheduleTimeOverrideService {
	public List<ScheduleTimeOverrideDTO> get(AuthDTO authDTO, ScheduleTimeOverrideDTO timeOverrideDTO);

	public ScheduleTimeOverrideDTO Update(AuthDTO authDTO, ScheduleTimeOverrideDTO timeOverrideDTO);

	public List<ScheduleTimeOverrideDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO);

}
