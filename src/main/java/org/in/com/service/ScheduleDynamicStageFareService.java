package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONObject;

public interface ScheduleDynamicStageFareService {
	public ScheduleDynamicStageFareDTO updateScheduleDynamicStageFareDetails(AuthDTO authDTO, ScheduleDynamicStageFareDTO scheduleSeatFare);

	public void addScheduleDynamicPriceException(AuthDTO authDTO, ScheduleDynamicStageFareDTO scheduleSeatFare, TripDTO trip);

	public List<ScheduleDynamicStageFareDTO> getScheduleStageFare(AuthDTO authDTO, ScheduleDTO schedule);

	public JSONObject getScheduleStageTripDPRawFare(AuthDTO authDTO, ScheduleDTO schedule);

	public void dynamicFareProcess(AuthDTO authDTO, ScheduleDynamicStageFareDTO scheduleSeatFare, ScheduleDynamicStageFareDTO scheduleDynamicStageFareDTO);

	public ScheduleDynamicStageFareDetailsDTO getScheduleDynamicStageFare(AuthDTO authDTO, ScheduleDTO schedule, StationDTO fromStationDTO, StationDTO toStationDTO);

	public ScheduleDynamicStageFareDetailsDTO getDynamicPricingTripStageFareDetails(AuthDTO authDTO, ScheduleDTO schedule, ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails);

	public ScheduleDynamicStageFareDetailsDTO getDynamicPricingTripStageFareDetailsV2(AuthDTO authDTO, ScheduleDTO schedule, ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails, List<ScheduleDynamicStageFareDetailsDTO> scheduleDynamicStageFareList);

	public Map<String, ScheduleDynamicStageFareDetailsDTO> getDynamicPricingTripStageFareDetailsV3(AuthDTO authDTO, ScheduleDTO schedule, ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails);

	public List<Map<String, ?>> getScheduleDynamicFareDetails(AuthDTO authDTO, ScheduleDTO schedule);

	public void notifyFareChangeQueue(AuthDTO authDTO, ScheduleDTO schedule);

	public void updateTicketStatus(AuthDTO authDTO, TicketDTO ticketDTO);

	public void updateSeatStatus(AuthDTO authDTO, ScheduleSeatVisibilityDTO scheduleSeatVisibility);

	public List<ScheduleDynamicStageFareDetailsDTO> getScheduleDynamicStageTripFareDetails(AuthDTO authDTO, ScheduleDTO schedule);

	public void notifyUpdateTripStageFareChange(AuthDTO authDTO, TripDTO tripDTO, List<StageDTO> stageList);

	public void openChart(AuthDTO authDTO, DateTime dateTime);

	public void processFareChangeQueueJob(AuthDTO authDTO);

	public void notifyBusTypeChange(AuthDTO authDTO, ScheduleDTO schedule, ScheduleBusOverrideDTO busOverride);
}
