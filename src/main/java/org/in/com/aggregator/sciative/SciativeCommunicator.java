package org.in.com.aggregator.sciative;

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
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.utils.DateUtil;
import org.in.com.utils.HttpServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class SciativeCommunicator {
	private static final Logger DYNAMIC_LOGGER = LoggerFactory.getLogger("org.in.com.aggregator.dynamic.pricing");

	private static final String TICKET_UPDATE_URL = "http://ezeeinfo.viaje.ai/update_events";

	public void updateTicketStatus(AuthDTO authDTO, TicketDTO ticketDTO) {
		String response = Text.EMPTY;
		JSONObject jsonObject = new JSONObject();
		try {

			jsonObject.put("tripDate", DateUtil.convertDate(ticketDTO.getTripDTO().getTripDate()));
			jsonObject.put("scheduleCode", ticketDTO.getTripDTO().getSchedule().getCode());
			jsonObject.put("tripCode", ticketDTO.getTripDTO().getCode());
			jsonObject.put("travelDate", DateUtil.convertDate(ticketDTO.getTripDate()));
			jsonObject.put("ticketCode", ticketDTO.getCode());

			jsonObject.put("fromStationCode", ticketDTO.getFromStation().getCode());
			jsonObject.put("toStationCode", ticketDTO.getToStation().getCode());

			String bookingChannel = ticketDTO.getDeviceMedium().getId() == 2 || ticketDTO.getDeviceMedium().getId() == 3 || ticketDTO.getDeviceMedium().getId() == 4 ? "Online Booking" : ticketDTO.getDeviceMedium().getId() == 1 ? "Offline Booking" : "Offline Booking";
			jsonObject.put("bookingChannel", bookingChannel);

			JSONArray seatDetails = new JSONArray();
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				JSONObject seatObject = new JSONObject();
				if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.TICKET_TRANSFERRED.getId()) {
					seatObject.put("seatCode", ticketDetailsDTO.getSeatCode());
					seatObject.put("seatName", ticketDetailsDTO.getSeatName());
					seatObject.put("seatFare", ticketDetailsDTO.getSeatFare());
					seatObject.put("seatStatus", ticketDetailsDTO.getTicketStatus().getCode());
					seatObject.put("serviceTax", ticketDetailsDTO.getAcBusTax());
					seatDetails.add(seatObject);
				}
			}
			jsonObject.put("ticketDetails", seatDetails);
			jsonObject.put("status", ticketDTO.getTicketStatus().getCode());
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

}
