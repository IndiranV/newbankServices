package org.in.com.aggregator.redbus;

import java.util.List;
import java.util.Map;

import org.in.com.aggregator.sciative.SciativeCommunicator;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.enumeration.DynamicPriceProviderEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.service.ScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONObject;

@Service
public class DynamicFareServiceImpl implements DynamicFareService {

	@Autowired
	ScheduleService scheduleService;

	@Override
	public void activateStageFareAPI(AuthDTO authDTO, ScheduleDynamicStageFareDTO stageFareDTO) {
		try {
			if (stageFareDTO.getActiveFlag() == 1) {
				RedbusCommunicator communicator = new RedbusCommunicator();
				JSONObject response = communicator.registerActivateStageFare(authDTO.getNamespaceCode(), stageFareDTO);
				if (response != null && response.has("status_code") && response.getString("status_code").equals("201")) {
					stageFareDTO.setStatus(1);
				}
				else {
					stageFareDTO.setStatus(0);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void pushNewServiceAlert(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime endDate) {
		RedbusCommunicator communicator = new RedbusCommunicator();
		communicator.pushNewServiceAlert(authDTO.getNamespaceCode(), scheduleDTO, endDate);
	}

	@Override
	public List<BusSeatLayoutDTO> getStageFare(AuthDTO authDTO, ScheduleDTO schedule, DateTime tripDate) {
		RedbusCommunicator communicator = new RedbusCommunicator();
		JSONObject response = communicator.getStageFare(authDTO, schedule, tripDate);
		DataConvertor convertor = new DataConvertor();
		return convertor.getStageFare(schedule, response);
	}

	public JSONObject getScheduleStageTripDPRawFare(AuthDTO authDTO, ScheduleDTO schedule, DateTime tripDate) {
		RedbusCommunicator communicator = new RedbusCommunicator();
		JSONObject response = communicator.getStageFare(authDTO, schedule, tripDate);
		return response;
	}

	@Override
	public void updateTicketStatus(AuthDTO authDTO, ScheduleDynamicStageFareDetailsDTO dynamicStageFare, TicketDTO ticketDTO) {
		if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.TICKET_TRANSFERRED.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.TRIP_CANCELLED.getId()) {
			if (dynamicStageFare.getDynamicPriceProvider().getId() == DynamicPriceProviderEM.REDBUS.getId()) {
				RedbusCommunicator communicator = new RedbusCommunicator();
				communicator.updateTicketStatus(authDTO, ticketDTO);
			}
			else if (dynamicStageFare.getDynamicPriceProvider().getId() == DynamicPriceProviderEM.SCIATIVE.getId()) {
				SciativeCommunicator communicator = new SciativeCommunicator();
				communicator.updateTicketStatus(authDTO, ticketDTO);
			}
		}
	}

	@Override
	public void updateSeatStatus(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation, ScheduleSeatVisibilityDTO scheduleSeatVisibility) {
		RedbusCommunicator communicator = new RedbusCommunicator();
		communicator.updateSeatStatus(authDTO, fromStation, toStation, scheduleSeatVisibility);
	}

	@Override
	public void updateScheduleStageFare(AuthDTO authDTO, ScheduleDynamicStageFareDTO stageFareDTO, List<ScheduleDynamicStageFareDetailsDTO> stageFares) {
		RedbusCommunicator communicator = new RedbusCommunicator();
		communicator.updateScheduleStageFare(authDTO.getNamespaceCode(), stageFareDTO, stageFares);
	}

	@Override
	public void removeScheduleStageFare(AuthDTO authDTO, ScheduleDynamicStageFareDTO stageFareDTO, List<ScheduleDynamicStageFareDetailsDTO> stageFares) {
		RedbusCommunicator communicator = new RedbusCommunicator();
		communicator.removeScheduleStageFare(authDTO.getNamespaceCode(), stageFareDTO, stageFares);
	}

	@Override
	public Map<String, String> getActiveRoutes(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate) {
		RedbusCommunicator communicator = new RedbusCommunicator();
		JSONObject response = communicator.getActiveRoutes(authDTO, scheduleDTO, tripDate);
		DataConvertor convertor = new DataConvertor();
		return convertor.getActiveRoutes(response);
	}

	@Override
	public void updateDynamicPriceStatus(AuthDTO authDTO, ScheduleDynamicStageFareDTO scheduleSeatFare) {
		RedbusCommunicator communicator = new RedbusCommunicator();
		communicator.updateDynamicPriceStatus(authDTO, scheduleSeatFare);
	}

}
