package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleTripStageFareDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleFareTemplateDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.TripDTO;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;

public interface ScheduleTripFareService {
	public List<StageDTO> getScheduleTripFare(AuthDTO authDTO, TripDTO tripDTO);

	public void addScheduleTripFare(AuthDTO authDTO, TripDTO tripDTO, List<ScheduleTripStageFareDTO> fareList);

	public List<TripDTO> getScheduleTripFareV2(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime fromDate, DateTime toDate, List<String> tripDateList, boolean includeTicketBookings);

	public Map<String, String> applyScheduleTripFareTemplate(AuthDTO authDTO, TripDTO tripDTO, ScheduleFareTemplateDTO fareTemplate);

	public List<ScheduleDTO> getScheduleOccupancy(AuthDTO authDTO);

	public void syncTripFareToScheduleStageFare(AuthDTO authDTO, TripDTO tripDTO);

	public JSONArray getScheduleOccupancyAnalytics(AuthDTO authDTO, DateTime fromDate, DateTime toDate);
}
