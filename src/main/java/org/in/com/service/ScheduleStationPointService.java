package org.in.com.service;

import hirondelle.date4j.DateTime;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;

public interface ScheduleStationPointService {
	public List<ScheduleStationPointDTO> get(AuthDTO authDTO, ScheduleStationPointDTO dto);

	public ScheduleStationPointDTO Update(AuthDTO authDTO, ScheduleStationPointDTO dto);

	public boolean CheckStationPointUsed(AuthDTO authDTO, StationPointDTO dto);

	public List<ScheduleStationPointDTO> getScheduleStationPoint(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public List<ScheduleStationPointDTO> getByScheduleTripDate(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate);

	public void updateScheduleStationPointException(AuthDTO authDTO, ScheduleStationPointDTO stationPointDTO);

	public List<ScheduleStationPointDTO> getScheduleStationPointException(AuthDTO authDTO);

	public List<ScheduleStationPointDTO> getActiveScheduleStationPointList(AuthDTO authDTO, ScheduleDTO scheduleDTO, SearchDTO searchDTO, Map<Integer, ScheduleStationDTO> stationMap);

	public List<BusVehicleVanPickupDTO> getVanPickupStationPoints(AuthDTO authDTO, StationDTO stationDTO);
}
