package org.in.com.service;

import hirondelle.date4j.DateTime;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleDTO;

public interface ScheduleBusOverrideService {
	public void updateScheduleBusOverride(AuthDTO authDTO, ScheduleBusOverrideDTO scheduleBusOverride);

	public ScheduleBusOverrideDTO getBusOverrideBySchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public List<ScheduleBusOverrideDTO> getScheduleBusOverride(AuthDTO authDTO, ScheduleDTO scheduleDTO);
	
	public List<ScheduleBusOverrideDTO> getBusOverrideByScheduleV2(AuthDTO authDTO, ScheduleDTO scheduleDTO);

	public BusDTO applyScheduleBusOverride(AuthDTO authDTO, ScheduleDTO scheduleDTO, BusDTO regularBusDTO);
	
	public Map<String, ScheduleDTO> applyBusOverrideV2(AuthDTO authDTO, BusDTO busDTO);
	
	public ScheduleBusOverrideDTO getScheduleBusOverride(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime fromDate, DateTime toDate);
	
	public List<ScheduleBusOverrideDTO> getUpcomingBusOverrides(AuthDTO authDTO, ScheduleDTO scheduleDTO);

}
