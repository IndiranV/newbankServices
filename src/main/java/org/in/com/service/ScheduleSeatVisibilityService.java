package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;

public interface ScheduleSeatVisibilityService {
	public List<ScheduleSeatVisibilityDTO> get(AuthDTO authDTO, ScheduleSeatVisibilityDTO scheduleSeatVisibilityDTO);

	public ScheduleSeatVisibilityDTO Update(AuthDTO authDTO, ScheduleSeatVisibilityDTO scheduleSeatVisibilityDTO);

	public List<ScheduleSeatVisibilityDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public List<ScheduleSeatVisibilityDTO> getSeatVisibilities(AuthDTO authDTO, ScheduleDTO scheduleDTO);

}
