package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;

import hirondelle.date4j.DateTime;

public interface ScheduleService {
	public List<ScheduleDTO> get(AuthDTO authDTO, ScheduleDTO dto);

	public ScheduleDTO getSchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public ScheduleDTO Update(AuthDTO authDTO, ScheduleDTO dto);

	public List<ScheduleDTO> getClosed(AuthDTO authDTO);

	public List<ScheduleDTO> getExpire(AuthDTO authDTO);

	public List<ScheduleDTO> getPartial(AuthDTO authDTO);

	public List<ScheduleDTO> getActive(AuthDTO authDTO, DateTime activeDate);

	public boolean getRefresh(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public boolean clearScheduleCache(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public ScheduleDTO clone(AuthDTO authDTO, ScheduleDTO scheduleDTO, List<String> entityList);

	public ScheduleDTO reverseClone(AuthDTO authDTO, ScheduleDTO scheduleDTO, List<String> entityList);

	public ScheduleDTO getActiveSchedule(AuthDTO authDTO, ScheduleDTO schedule);

	public ScheduleDTO getScheduleDetails(AuthDTO authDTO, ScheduleDTO schedule);

}
