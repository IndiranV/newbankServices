package org.in.com.aggregator.dp;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONObject;

public interface DynamicPricingFactoryInterface {
	// Provide already registered stage details of Service
	public JSONObject getAlreadyRegisteredScheduleStage(AuthDTO authDTO, IntegrationDTO integration, ScheduleDTO scheduleDTO, DateTime tripDate);

	// Activate the DP Service Stages
	public JSONObject registerScheduleStage(AuthDTO authDTO, IntegrationDTO integration, ScheduleDynamicStageFareDTO scheduleDynamicStageFare);

	// Register to open trip on particular date or selected dates
	public JSONObject registerNewTripOpen(AuthDTO authDTO, IntegrationDTO integration, ScheduleDTO schedule, DateTime endDate);

	// Pull DP fare of particular date for Trip
	public JSONObject getScheduleStageFare(AuthDTO authDTO, IntegrationDTO integration, ScheduleDTO schedule, DateTime tripDate);

	// Push ticket booking/cancel events to DPE
	public void updateOccupancyStatus(AuthDTO authDTO, IntegrationDTO integration, TicketDTO ticket);

	// Push Seat block/release events to DPE
	public void updateSeatVisibilityStatus(AuthDTO authDTO, IntegrationDTO integration, StationDTO fromStation, StationDTO toStation, ScheduleSeatVisibilityDTO scheduleSeatVisibility);

	// Add Via routes/stages
	public JSONObject registerScheduleViaStage(AuthDTO authDTO, IntegrationDTO integration, ScheduleDynamicStageFareDTO stage, List<ScheduleDynamicStageFareDetailsDTO> stageFares);

	// remove Via routes/stages
	public JSONObject unRegisterScheduleViaStage(AuthDTO authDTO, IntegrationDTO integration, ScheduleDynamicStageFareDTO stage, List<ScheduleDynamicStageFareDetailsDTO> stageFares);

	// Notify Bus Type change event
	public void notifyBusTypeChange(AuthDTO authDTO, IntegrationDTO integration, ScheduleDTO schedule, ScheduleBusOverrideDTO overrideBus);

}
