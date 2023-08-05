package org.in.com.aggregator.redbus;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;

public interface DynamicFareService {

	public void activateStageFareAPI(AuthDTO authDTO, ScheduleDynamicStageFareDTO stageFareDTO);

	public void pushNewServiceAlert(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime endDate);

	public List<BusSeatLayoutDTO> getStageFare(AuthDTO authDTO, ScheduleDTO schedule, DateTime tripDate);

	public JSONObject getScheduleStageTripDPRawFare(AuthDTO authDTO, ScheduleDTO schedule, DateTime tripDate);

	public void updateTicketStatus(AuthDTO authDTO, ScheduleDynamicStageFareDetailsDTO dynamicStageFare, TicketDTO ticketDTO);

	public void updateSeatStatus(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation, ScheduleSeatVisibilityDTO scheduleSeatVisibility);

	public void updateScheduleStageFare(AuthDTO authDTO, ScheduleDynamicStageFareDTO stageFareDTO, List<ScheduleDynamicStageFareDetailsDTO> stageFares);

	public void removeScheduleStageFare(AuthDTO authDTO, ScheduleDynamicStageFareDTO stageFareDTO, List<ScheduleDynamicStageFareDetailsDTO> stageFares);

	public Map<String, String> getActiveRoutes(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate);

	public void updateDynamicPriceStatus(AuthDTO authDTO, ScheduleDynamicStageFareDTO scheduleSeatFare);
}
