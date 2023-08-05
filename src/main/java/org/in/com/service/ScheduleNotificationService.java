package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleNotificationDTO;

public interface ScheduleNotificationService {

	public ScheduleNotificationDTO getNotificationUID(AuthDTO authDTO, ScheduleNotificationDTO scheduleNotificationDTO);

	public List<ScheduleNotificationDTO> getAllNotifications(AuthDTO authDTO, ScheduleDTO scheduleDTO);
}
