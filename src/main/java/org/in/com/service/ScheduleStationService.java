package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.StationDTO;

import hirondelle.date4j.DateTime;

public interface ScheduleStationService {
	public List<ScheduleStationDTO> get(AuthDTO authDTO, ScheduleStationDTO dto);

	public ScheduleStationDTO Update(AuthDTO authDTO, ScheduleStationDTO dto);

	public List<ScheduleStationDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public List<ScheduleStationDTO> getScheduleStation(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public List<ScheduleStationDTO> getByScheduleTripDate(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate);

	public void isStationUsed(AuthDTO authDTO, ScheduleDTO schedule, StationDTO station);
}
