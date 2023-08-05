package org.in.com.service;

import hirondelle.date4j.DateTime;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StationDTO;

public interface ScheduleControlService {
	public List<ScheduleControlDTO> get(AuthDTO authDTO, ScheduleControlDTO dto);

	public ScheduleControlDTO Update(AuthDTO authDTO, ScheduleControlDTO dto);

	public List<ScheduleControlDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO);

	public List<ScheduleControlDTO> getByScheduleTripDate(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate);

	public List<ScheduleControlDTO> getAllGroupTripScheduleControl(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate);
}
