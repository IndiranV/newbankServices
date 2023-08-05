package org.in.com.service;

import hirondelle.date4j.DateTime;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.StationDTO;

public interface ScheduleStageService {
	public List<ScheduleStageDTO> get(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public ScheduleStageDTO Update(AuthDTO authDTO, ScheduleStageDTO dto);

	public List<ScheduleStageDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO);

	public List<ScheduleStageDTO> getByScheduleTripDate(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate);

	public List<ScheduleDTO> getScheduleSearchStage(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation);
	
	public List<ScheduleStageDTO> getScheduleStageV2(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public void removeScheduleSearchStageCache(AuthDTO authDTO, ScheduleDTO scheduleDTO);

}
