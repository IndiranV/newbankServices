package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatFareDTO;
import org.in.com.dto.StationDTO;

public interface ScheduleSeatFareService {
	public List<ScheduleSeatFareDTO> get(AuthDTO authDTO, ScheduleSeatFareDTO dto);

	public ScheduleSeatFareDTO Update(AuthDTO authDTO, ScheduleSeatFareDTO dto);

	public List<ScheduleSeatFareDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO);

	public List<ScheduleSeatFareDTO> getActiveScheduleSeatFare(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public List<ScheduleSeatFareDTO> processScheduleSeatFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, StationDTO fromStationDTO, StationDTO toStationDTO, List<ScheduleSeatFareDTO> activeScheduleSeatFare);

}
