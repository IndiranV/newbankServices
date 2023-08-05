package org.in.com.aggregator.dp;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONObject;

public interface DynamicPricingFactoryService {

	// Provide already registered stage details of Service
	public JSONObject getAlreadyRegisteredScheduleStage(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate);

	// Activate the DP Service Stages
	public JSONObject registerScheduleStage(AuthDTO authDTO, ScheduleDynamicStageFareDTO scheduleDynamicStageFare);

	// Register to open trip on particular date or selected dates
	public JSONObject registerNewTripOpen(AuthDTO authDTO, ScheduleDTO schedule, DateTime endDate);

	// Pull DP fare of particular date for Trip
	public JSONObject getScheduleStageFare(AuthDTO authDTO, ScheduleDTO schedule, DateTime tripDate);

	// Push ticket booking/cancel events to DPE
	public void updateOccupancyStatus(AuthDTO authDTO, ScheduleDynamicStageFareDTO dynamicStageFare, TicketDTO ticket);

	// Push Seat block/release events to DPE
	public void updateSeatVisibilityStatus(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation, ScheduleSeatVisibilityDTO scheduleSeatVisibility);

	// Add Via routes/stages
	public JSONObject registerScheduleViaStage(AuthDTO authDTO, ScheduleDynamicStageFareDTO stage, List<ScheduleDynamicStageFareDetailsDTO> stageFares);

	// remove Via routes/stages
	public JSONObject unRegisterScheduleViaStage(AuthDTO authDTO, ScheduleDynamicStageFareDTO stage, List<ScheduleDynamicStageFareDetailsDTO> stageFares);

	// Notify Bus Type change event
	public void notifyBusTypeChange(AuthDTO authDTO, ScheduleDTO schedule, ScheduleDynamicStageFareDetailsDTO dynamicStageFare, ScheduleBusOverrideDTO bus);

}
