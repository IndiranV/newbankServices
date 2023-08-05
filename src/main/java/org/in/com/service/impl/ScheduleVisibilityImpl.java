package org.in.com.service.impl;

import java.util.List;

import org.in.com.cache.UserCache;
import org.in.com.dao.ScheduleVisibilityDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.UserDTO;
import org.in.com.service.ScheduleVisibilityService;
import org.springframework.stereotype.Service;

@Service
public class ScheduleVisibilityImpl extends UserCache implements ScheduleVisibilityService {

	@Override
	public List<UserDTO> get(AuthDTO authDTO, String scheduleCode) {
		ScheduleVisibilityDAO visibilityDAO = new ScheduleVisibilityDAO();
		return visibilityDAO.get(authDTO, scheduleCode);
	}

	@Override
	public boolean Update(AuthDTO authDTO, String scheduleCode, List<UserDTO> userList) {
		ScheduleVisibilityDAO visibilityDAO = new ScheduleVisibilityDAO();
		return visibilityDAO.getIUD(authDTO, scheduleCode, userList);
	}

	@Override
	public List<ScheduleDTO> getUserActiveSchedule(AuthDTO authDTO) {
		ScheduleVisibilityDAO visibilityDAO = new ScheduleVisibilityDAO();
		return visibilityDAO.getUserActiveSchedule(authDTO);
	}

}
