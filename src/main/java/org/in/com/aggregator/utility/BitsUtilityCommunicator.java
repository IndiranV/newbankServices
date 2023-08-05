package org.in.com.aggregator.utility;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
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
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.utils.DateUtil;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class BitsUtilityCommunicator {

	private final String API_URL = "http://utility.ezeebits.com";
	public static Logger UTILITY_LOGGER = LoggerFactory.getLogger("org.in.com.aggregator.utility.BitsUtilityCommunicator");

	public JSONObject updateTripCancel(AuthDTO authDTO, NamespaceDTO namespace, TicketDTO ticketDTO) {
		JSONObject json = null;
		JSONObject jsonObject = new JSONObject();
		String url = "/send/confirm-trip-cancel";
		try {
			String aliasCode = namespace != null && StringUtil.isNotNull(namespace.getAliasCode()) ? namespace.getAliasCode() : Text.EMPTY;
			jsonObject.put("operatorCode", authDTO.getNamespaceCode() + aliasCode);
			jsonObject.put("operatorName", authDTO.getNamespace().getName());
			jsonObject.put("operatorMail", authDTO.getNamespace().getProfile().getEmailCopyAddress());

			if (authDTO.getNamespace().getProfile().getState() != null) {
				jsonObject.put("operatorState", authDTO.getNamespace().getProfile().getState().getName());
				jsonObject.put("operatorStateCode", authDTO.getNamespace().getProfile().getState().getCode());
			}

			JSONObject ticket = new JSONObject();
			ticket.put("mobile_number", ticketDTO.getPassengerMobile());
			ticket.put("email", ticketDTO.getPassengerEmailId());
			ticket.put("transaction_date", DateUtil.convertDateTime(ticketDTO.getTicketAt()));
			ticket.put("from_station_code", ticketDTO.getFromStation().getCode());
			ticket.put("from_station_name", ticketDTO.getFromStation().getName());
			ticket.put("updated_user_name", authDTO.getUser().getName());
			ticket.put("ticket_code", ticketDTO.getCode());
			ticket.put("user_code", ticketDTO.getTicketUser().getCode());
			ticket.put("user_name", ticketDTO.getTicketUser().getName());
			ticket.put("trip_date_time", DateUtil.convertDateTime(ticketDTO.getTripDateTime()));
			ticket.put("ac_bus_tax", ticketDTO.getAcBusTax().toString());
			ticket.put("travel_date", DateUtil.convertDate(ticketDTO.getTripDate()));
			ticket.put("seat_code", ticketDTO.getSeatCodes());
			ticket.put("ticket_status_code", ticketDTO.getTicketStatus().getCode());
			ticket.put("passenger_name", ticketDTO.getPassengerName());
			ticket.put("trip_cancel_at", DateUtil.convertDateTime(DateUtil.NOW()));
			ticket.put("seat_count", String.valueOf(ticketDTO.getTicketDetails().size()));
			ticket.put("seat_name", ticketDTO.getSeatNames());
			ticket.put("to_station_name", ticketDTO.getToStation().getName());
			ticket.put("to_station_code", ticketDTO.getToStation().getCode());
			ticket.put("trip_code", ticketDTO.getTripDTO().getCode());
			ticket.put("ticket_amount", ticketDTO.getTotalSeatFare().toString());
			ticket.put("user_group_name", ticketDTO.getTicketUser().getGroup().getName());
			ticket.put("service_number", ticketDTO.getServiceNo());
			ticket.put("flag", "refund");
			ticket.put("username", ticketDTO.getTicketUser().getUsername());
			ticket.put("api_token", ticketDTO.getTicketUser().getApiToken());
			ticket.put("refund_amount", ticketDTO.getTicketXaction().getRefundAmount() != null ? ticketDTO.getTicketXaction().getRefundAmount().toString() : ticketDTO.getRefundAmount().toString());

			List<String> userTags = new ArrayList<String>();
			for (UserTagEM userTag : ticketDTO.getTicketUser().getUserTags()) {
				userTags.add(userTag.getCode());
			}
			ticket.put("user_tag", userTags);
			jsonObject.put("params", ticket);

			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.post(API_URL + url, jsonObject.toString(), "application/json");
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			UTILITY_LOGGER.error("Error01 - Ticket:{} {} Request:{} Response:{}", ticketDTO.getCode(), url, jsonObject, json);
			e.printStackTrace();
		}
		finally {
			UTILITY_LOGGER.info("refund - Ticket:{} {} Request:{} Response:{}", ticketDTO.getCode(), url, jsonObject, json);
		}
		return json;
	}
	
	public JSONObject updateTicketTransfer(AuthDTO authDTO, NamespaceDTO namespace, TicketDTO ticketDTO) {
		JSONObject json = null;
		JSONObject jsonObject = new JSONObject();
		String url = "/send/confirm-ticket-transfer";
		try {
			String aliasCode = namespace != null && StringUtil.isNotNull(namespace.getAliasCode()) ? namespace.getAliasCode() : Text.EMPTY;
			jsonObject.put("operatorCode", authDTO.getNamespaceCode() + aliasCode);
			jsonObject.put("operatorName", authDTO.getNamespace().getName());
			jsonObject.put("operatorMail", authDTO.getNamespace().getProfile().getEmailCopyAddress());
			jsonObject.put("operatorState", authDTO.getNamespace().getProfile().getState().getName());
			jsonObject.put("operatorStateCode", authDTO.getNamespace().getProfile().getState().getCode());

			JSONObject ticket = new JSONObject();
			ticket.put("mobile_number", ticketDTO.getPassengerMobile());
			ticket.put("email", ticketDTO.getPassengerEmailId());
			ticket.put("transaction_date", DateUtil.convertDateTime(ticketDTO.getTicketAt()));
			ticket.put("from_station_code", ticketDTO.getFromStation().getCode());
			ticket.put("from_station_name", ticketDTO.getFromStation().getName());
			ticket.put("updated_user_name", authDTO.getUser().getName());
			ticket.put("ticket_code", ticketDTO.getCode());
			ticket.put("user_code", ticketDTO.getTicketUser().getCode());
			ticket.put("user_name", ticketDTO.getTicketUser().getName());
			ticket.put("trip_date_time", DateUtil.convertDateTime(ticketDTO.getTripDateTime()));
			ticket.put("ac_bus_tax", ticketDTO.getAcBusTax().toString());
			ticket.put("travel_date", DateUtil.convertDate(ticketDTO.getTripDate()));
			ticket.put("seat_code", ticketDTO.getSeatCodes());
			ticket.put("ticket_status_code", ticketDTO.getTicketStatus().getCode());
			ticket.put("passenger_name", ticketDTO.getPassengerName());
			ticket.put("trip_cancel_at", DateUtil.convertDateTime(DateUtil.NOW()));
			ticket.put("seat_count", String.valueOf(ticketDTO.getTicketDetails().size()));
			ticket.put("seat_name", ticketDTO.getSeatNames());
			ticket.put("to_station_name", ticketDTO.getToStation().getName());
			ticket.put("to_station_code", ticketDTO.getToStation().getCode());
			ticket.put("trip_code", ticketDTO.getTripDTO().getCode());
			ticket.put("ticket_amount", ticketDTO.getTotalSeatFare().toString());
			ticket.put("user_group_name", ticketDTO.getTicketUser().getGroup().getName());
			ticket.put("service_number", ticketDTO.getServiceNo());
			ticket.put("flag", "alternate");

			List<String> userTags = new ArrayList<String>();
			for (UserTagEM userTag : ticketDTO.getTicketUser().getUserTags()) {
				userTags.add(userTag.getCode());
			}
			ticket.put("user_tag", userTags);
			jsonObject.put("params", ticket);

			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.post(API_URL + url, jsonObject.toString(), "application/json");
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			UTILITY_LOGGER.error("Errortransfer - Ticket:{} {} Request:{} Response:{}", ticketDTO.getCode(), url, jsonObject, json);
			e.printStackTrace();
		}
		finally {
			UTILITY_LOGGER.info("transfer - Ticket:{} {} Request:{} Response:{}", ticketDTO.getCode(), url, jsonObject, json);
		}
		return json;
	}

	/** Push Service to OTA */
	public JSONObject pushInventoryChangesEvent(AuthDTO authDTO, List<StageDTO> stages) {
		JSONObject jsonObject = new JSONObject();
		JSONObject responseJSON = null;
		try {
			String url = "https://utility.ezeebits.com/send/push-service-to-aggregator";

			jsonObject.put("action_type", "SEARCH");

			JSONArray data = new JSONArray();
			for (StageDTO stageDTO : stages) {
				JSONObject dataJSON = new JSONObject();
				dataJSON.put("doj", DateUtil.convertDate(stageDTO.getTravelDate()));
				dataJSON.put("source", stageDTO.getFromStation().getStation().getCode());
				dataJSON.put("destination", stageDTO.getToStation().getStation().getCode());
				dataJSON.put("operatorcode", stageDTO.getCode());
				data.add(dataJSON);
			}
			jsonObject.put("data", data);
			jsonObject.put("activity_type", authDTO.getAdditionalAttribute().get("activity_type"));

			UTILITY_LOGGER.info("Push Trip{} - {}", url, jsonObject);

			HttpClient client = new HttpServiceClient().getHttpClient();
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("accept", MediaType.APPLICATION_JSON_VALUE);
			httpPost.addHeader("content-type", MediaType.APPLICATION_JSON_VALUE);

			StringEntity input = new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
			input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
			httpPost.setEntity(input);

			HttpResponse responseData = client.execute(httpPost);
			HttpEntity entity = responseData.getEntity();
			String response = EntityUtils.toString(entity, "UTF-8");
			responseJSON = JSONObject.fromObject(response);
			UTILITY_LOGGER.info("Push Trip Response {}", responseJSON);
		}
		catch (Exception e) {
			UTILITY_LOGGER.error("", e);
			e.printStackTrace();
		}
		return responseJSON;
	}

}
