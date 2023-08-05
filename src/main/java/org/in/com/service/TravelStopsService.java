package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.TravelStopsDTO;
import org.in.com.dto.TripDTO;

public interface TravelStopsService extends BaseService<TravelStopsDTO> {

	public List<TravelStopsDTO> getScheduleStop(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public TravelStopsDTO mapScheduleStops(AuthDTO authDTO, ScheduleDTO scheduleDTO, TravelStopsDTO stopDTO);

	public List<TravelStopsDTO> getScheduleTripStop(AuthDTO authDTO, TripDTO tripDTO);

	public List<TravelStopsDTO> getScheduleStopV2(AuthDTO authDTO, ScheduleDTO scheduleDTO, SearchDTO searchDTO);
	
	public List<TravelStopsDTO> getByScheduleCode(AuthDTO authDTO, ScheduleDTO scheduleDTO);

}
