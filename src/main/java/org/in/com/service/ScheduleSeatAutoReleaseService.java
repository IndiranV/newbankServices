package org.in.com.service;

import hirondelle.date4j.DateTime;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatAutoReleaseDTO;

public interface ScheduleSeatAutoReleaseService {
	public List<ScheduleSeatAutoReleaseDTO> getAllScheduleSeatAutoRelease(AuthDTO authDTO);

	public ScheduleSeatAutoReleaseDTO Update(AuthDTO authDTO, ScheduleSeatAutoReleaseDTO dto);

	public List<ScheduleSeatAutoReleaseDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public List<ScheduleSeatAutoReleaseDTO> getByScheduleTripDate(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate);

}
