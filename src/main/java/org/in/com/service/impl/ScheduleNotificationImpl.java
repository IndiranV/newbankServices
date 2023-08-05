package org.in.com.service.impl;

import java.util.List;

import org.in.com.cache.CacheCentral;
import org.in.com.dao.ScheduleNotificationDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleNotificationDTO;
import org.in.com.service.ScheduleNotificationService;
import org.springframework.stereotype.Service;

@Service
public class ScheduleNotificationImpl extends CacheCentral implements ScheduleNotificationService {

	@Override
	public ScheduleNotificationDTO getNotificationUID(AuthDTO authDTO, ScheduleNotificationDTO notificationDTO) {
		ScheduleNotificationDAO dao = new ScheduleNotificationDAO();
		dao.getScheduleNotificationUID(authDTO, notificationDTO);
		return notificationDTO;
	}

	@Override
	public List<ScheduleNotificationDTO> getAllNotifications(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleNotificationDAO dao = new ScheduleNotificationDAO();
		return dao.getAllNotifications(scheduleDTO);
	}
}
