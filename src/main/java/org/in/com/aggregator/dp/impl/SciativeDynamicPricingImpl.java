package org.in.com.aggregator.dp.impl;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.in.com.aggregator.dp.DynamicPricingFactoryInterface;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.utils.DateUtil;
import org.in.com.utils.HttpServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SciativeDynamicPricingImpl implements DynamicPricingFactoryInterface {
	private static final Logger DYNAMIC_LOGGER = LoggerFactory.getLogger("org.in.com.aggregator.dynamic.pricing");

	private static final String TICKET_UPDATE_URL = "http://ezeeinfo.viaje.ai/update_events";
	private static final String WEBHOOK_UPDATE_URL = "http://notifications.viaje.ai/update_notifications";

	@Override
	public JSONObject getAlreadyRegisteredScheduleStage(AuthDTO authDTO, IntegrationDTO integration, ScheduleDTO scheduleDTO, DateTime tripDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject registerScheduleStage(AuthDTO authDTO, IntegrationDTO integration, ScheduleDynamicStageFareDTO scheduleDynamicStageFare) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject registerNewTripOpen(AuthDTO authDTO, IntegrationDTO integration, ScheduleDTO schedule, DateTime endDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject getScheduleStageFare(AuthDTO authDTO, IntegrationDTO integration, ScheduleDTO schedule, DateTime tripDate) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateOccupancyStatus(AuthDTO authDTO, IntegrationDTO integration, TicketDTO ticket) {

		String response = Text.EMPTY;
		JSONObject jsonObject = new JSONObject();
		try {

			jsonObject.put("tripDate", DateUtil.convertDate(ticket.getTripDTO().getTripDate()));
			jsonObject.put("scheduleCode", ticket.getTripDTO().getSchedule().getCode());
			jsonObject.put("tripCode", ticket.getTripDTO().getCode());
			jsonObject.put("travelDate", DateUtil.convertDate(ticket.getTripDate()));
			jsonObject.put("ticketCode", ticket.getCode());

			jsonObject.put("fromStationCode", ticket.getFromStation().getCode());
			jsonObject.put("toStationCode", ticket.getToStation().getCode());

			String bookingChannel = ticket.getDeviceMedium().getId() == 2 || ticket.getDeviceMedium().getId() == 3 || ticket.getDeviceMedium().getId() == 4 ? "Online Booking" : ticket.getDeviceMedium().getId() == 1 ? "Offline Booking" : "Offline Booking";
			jsonObject.put("bookingChannel", bookingChannel);

			JSONArray seatDetails = new JSONArray();
			for (TicketDetailsDTO ticketDetailsDTO : ticket.getTicketDetails()) {
				JSONObject seatObject = new JSONObject();
				if (ticket.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticket.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() || ticket.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() || ticket.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
					seatObject.put("seatCode", ticketDetailsDTO.getSeatCode());
					seatObject.put("seatName", ticketDetailsDTO.getSeatName());
					seatObject.put("seatFare", ticketDetailsDTO.getSeatFare());
					seatObject.put("seatStatus", ticketDetailsDTO.getTicketStatus().getCode());
					seatObject.put("serviceTax", ticketDetailsDTO.getAcBusTax());
					seatDetails.add(seatObject);
				}
			}
			jsonObject.put("ticketDetails", seatDetails);
			jsonObject.put("status", ticket.getTicketStatus().getCode());
			HttpClient client = new HttpServiceClient().getHttpClient(3, 3);
			HttpPost httpPost = new HttpPost(TICKET_UPDATE_URL);
			httpPost.addHeader("accept", MediaType.APPLICATION_JSON_VALUE);
			httpPost.addHeader("content-type", MediaType.APPLICATION_JSON_VALUE);
			httpPost.addHeader(HttpHeaders.AUTHORIZATION, "B5380E17FABCB162E3DA54853CC57C6F@gdj7F662951744E0BA739AB5C95E4DCD");
			StringEntity input = new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
			input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
			httpPost.setEntity(input);

			HttpResponse responseData = client.execute(httpPost);
			HttpEntity entity = responseData.getEntity();
			response = EntityUtils.toString(entity, "UTF-8");
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.error("Sciative {} {} - {}", authDTO.getNamespaceCode(), jsonObject.toString(), response);
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
		finally {
			DYNAMIC_LOGGER.info("Sciative Status: {} {} - {}", authDTO.getNamespaceCode(), jsonObject.toString(), response);
		}

	}

	@Override
	public void updateSeatVisibilityStatus(AuthDTO authDTO, IntegrationDTO integration, StationDTO fromStation, StationDTO toStation, ScheduleSeatVisibilityDTO scheduleSeatVisibility) {
		// TODO Auto-generated method stub

	}

	@Override
	public JSONObject registerScheduleViaStage(AuthDTO authDTO, IntegrationDTO integration, ScheduleDynamicStageFareDTO stage, List<ScheduleDynamicStageFareDetailsDTO> stageFares) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public JSONObject unRegisterScheduleViaStage(AuthDTO authDTO, IntegrationDTO integration, ScheduleDynamicStageFareDTO stage, List<ScheduleDynamicStageFareDetailsDTO> stageFares) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void notifyBusTypeChange(AuthDTO authDTO, IntegrationDTO integration, ScheduleDTO schedule, ScheduleBusOverrideDTO overrideBus) {
		String response = Text.EMPTY;
		JSONObject jsonObject = new JSONObject();
		try {
			jsonObject.put("param", 2);
			jsonObject.put("route_id", schedule.getCode());
			jsonObject.put("operator_id", integration.getAccount());
			jsonObject.put("from_date:", DateUtil.convertDate(overrideBus.getActiveFromDateTime()));
			jsonObject.put("to_date:", DateUtil.convertDate(overrideBus.getActiveToDateTime()));
			jsonObject.put("message:", "Bustype changed");

			HttpClient client = new HttpServiceClient().getHttpClient(3, 3);
			HttpPost httpPost = new HttpPost(WEBHOOK_UPDATE_URL);
			httpPost.addHeader("accept", MediaType.APPLICATION_JSON_VALUE);
			httpPost.addHeader("content-type", MediaType.APPLICATION_JSON_VALUE);
			httpPost.addHeader(HttpHeaders.AUTHORIZATION, "92VJUE0FBP09X86UECE9UAP577N65CVPT5R58S722JWBCKVC3BL76TORF01818LI2");
			StringEntity input = new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
			input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
			httpPost.setEntity(input);

			HttpResponse responseData = client.execute(httpPost);
			HttpEntity entity = responseData.getEntity();
			response = EntityUtils.toString(entity, "UTF-8");
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.error("Sciative {} {} - {}", authDTO.getNamespaceCode(), jsonObject.toString(), response);
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
		finally {
			DYNAMIC_LOGGER.info("Sciative Status: {} {} - {}", authDTO.getNamespaceCode(), jsonObject.toString(), response);
		}
	}
}
