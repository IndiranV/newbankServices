package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.UserDTO;

public interface ScheduleVisibilityService {
	public List<UserDTO> get(AuthDTO authDTO, String scheduleCode);

	public List<ScheduleDTO> getUserActiveSchedule(AuthDTO authDTO);

	public boolean Update(AuthDTO authDTO, String scheduleCode, List<UserDTO> userList);

}
