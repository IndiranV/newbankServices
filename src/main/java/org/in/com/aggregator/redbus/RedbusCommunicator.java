package org.in.com.aggregator.redbus;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.message.BasicHeader;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class RedbusCommunicator {
	private static final Logger DYNAMIC_LOGGER = LoggerFactory.getLogger("org.in.com.aggregator.dynamic.pricing");

	private static final String REGISTERATION = "http://dpe.redbus.in:8005/dp/v2/register";
	private static final String OPEN_CHART = "http://dpe.redbus.in:8005/dp/v2/openchart";
	private static final String GET_FARES = "http://dpe.redbus.in:8005/dp/faresDetailsWithDate";
	private static final String UPDATE_SALES = "http://dpe.redbus.in:8005/dp/v2/seatsStatus";
	private static final String UPDATE_VIA_ROUTES = "http://dpe.redbus.in:8005/dp/v2/addViaRoute";
	private static final String REMOVE_VIA_ROUTES = "http://dpe.redbus.in:8005/dp/v2/removeViaRoute";
	private static final String ACTIVE_ROUTES = "http://dpe.redbus.in:8005/dp/v2/getActiveRoutes";
	private static final String UPDATE_DP_STATUS = "http://dpe.redbus.in:8005/dp/v2/dpState";

	private static final String ROUTE_ID = "rId";
	private static final String CHART_TILL_OPEN = "chart_open_till";
	private static final String GDS_KEY = "347E6FADB2915F38DA5ECBD3EDA4E414";
	private static final String HYPHEN_SPACE = " - ";

	public JSONObject registerActivateStageFare(String namespaceCode, ScheduleDynamicStageFareDTO scheduleDynamicStageFare) {
		JSONObject response = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		try {
			RedbusVendorKeyEM redbusVendorKey = RedbusVendorKeyEM.getRedbusVendorKeyEM(namespaceCode);

			jsonObject.put(ROUTE_ID, scheduleDynamicStageFare.getSchedule().getCode());
			jsonObject.put("start_date", DateUtil.convertDate(DateUtil.getDateTime(scheduleDynamicStageFare.getActiveFrom())));
			jsonObject.put("end_date", DateUtil.convertDate(DateUtil.getDateTime(scheduleDynamicStageFare.getActiveTo())));
			jsonObject.put(CHART_TILL_OPEN, DateUtil.convertDate(DateUtil.getDateTime(scheduleDynamicStageFare.getActiveTo())));

			Comparator<ScheduleStationDTO> comp = new BeanComparator("stationSequence");
			Collections.sort(scheduleDynamicStageFare.getSchedule().getStationList(), comp);

			JSONArray stations = new JSONArray();
			for (ScheduleStationDTO scheduleStationDTO : scheduleDynamicStageFare.getSchedule().getStationList()) {
				stations.add(scheduleStationDTO.getStation().getCode());
			}
			jsonObject.put("city_sequence", stations);

			JSONArray routes = new JSONArray();
			for (ScheduleDynamicStageFareDetailsDTO scheduleStageDTO : scheduleDynamicStageFare.getStageFare()) {
				JSONObject route = new JSONObject();
				route.put("route", scheduleStageDTO.getFromStation().getCode() + Text.HYPHEN + scheduleStageDTO.getToStation().getCode() + Text.HYPHEN + scheduleDynamicStageFare.getSchedule().getCode());
				route.put("day_identifier", 1);
				routes.add(route);
			}
			jsonObject.put("bData", routes);
			List<ScheduleStationDTO> activeStationList = scheduleDynamicStageFare.getSchedule().getStationList().stream().filter(p -> p.getActiveFlag() == 1).collect(Collectors.toList());
			ScheduleStationDTO fromStation = BitsUtil.getOriginStation(activeStationList);
			ScheduleStationDTO toStation = BitsUtil.getDestinationStation(activeStationList);
			jsonObject.put("full_route", fromStation.getStation().getCode() + Text.HYPHEN + toStation.getStation().getCode());

			String apiData = makeAPIRequestConnection(REGISTERATION, jsonObject, redbusVendorKey, "NA");
			response = JSONObject.fromObject(apiData);
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.info(jsonObject + HYPHEN_SPACE + response);
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
		return response;
	}

	public JSONObject pushNewServiceAlert(String namespaceCode, ScheduleDTO scheduleDTO, DateTime endDate) {
		JSONObject response = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		try {
			RedbusVendorKeyEM redbusVendorKey = RedbusVendorKeyEM.getRedbusVendorKeyEM(namespaceCode);

			jsonObject.put(ROUTE_ID, scheduleDTO.getCode());
			jsonObject.put(CHART_TILL_OPEN, DateUtil.convertDate(endDate));
			jsonObject.put("chart_open_on_specific_dates", new JSONArray());

			String apiData = makeAPIRequestConnection(OPEN_CHART, jsonObject, redbusVendorKey, "NA");
			response = JSONObject.fromObject(apiData);
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.info(jsonObject + HYPHEN_SPACE + response);
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
		return response;
	}

	public JSONObject getStageFare(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate) {
		JSONObject response = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		try {
			RedbusVendorKeyEM redbusVendorKey = RedbusVendorKeyEM.getRedbusVendorKeyEM(authDTO.getNamespaceCode());
			if (redbusVendorKey == null) {
				throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
			}
			jsonObject.put(ROUTE_ID, scheduleDTO.getCode());
			JSONArray dates = new JSONArray();
			dates.add(DateUtil.convertDate(tripDate));

			jsonObject.put("Doj", dates);

			String apiData = makeAPIRequestConnection(GET_FARES, jsonObject, redbusVendorKey, "NA");
			response = JSONObject.fromObject(apiData);
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.info(jsonObject + HYPHEN_SPACE + response);
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
		return response;
	}

	public void updateTicketStatus(AuthDTO authDTO, TicketDTO ticketDTO) {
		String response = Text.EMPTY;
		JSONObject jsonObject = new JSONObject();
		try {
			RedbusVendorKeyEM redbusVendorKey = RedbusVendorKeyEM.getRedbusVendorKeyEM(authDTO.getNamespaceCode());
			if (redbusVendorKey == null) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}
			// Ticket transfer details
			Map<String, String> additionalAttribute = authDTO.getAdditionalAttribute();
			String ticketTransfer = StringUtil.isNotNull(additionalAttribute.get(Text.TRANSFER_BOOKING_CANCEL)) && additionalAttribute.get(Text.TRANSFER_BOOKING_CANCEL).equals("Transfer") ? "|" + additionalAttribute.get(Text.TRANSFER_BOOKING_CANCEL) : Text.EMPTY;

			jsonObject.put(ROUTE_ID, ticketDTO.getTripDTO().getSchedule().getCode());
			jsonObject.put("serviceId", ticketDTO.getTripDTO().getSchedule().getCode());
			// doj -- Requested Trip Date
			jsonObject.put("doj", DateUtil.convertDate(ticketDTO.getTripDTO().getTripDate()));
			if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.TRIP_CANCELLED.getId()) {
				jsonObject.put("tripCancelled", true);
			}
			JSONArray allocatedData = new JSONArray();
			JSONObject allocatedDataJSON = new JSONObject();
			allocatedDataJSON.put("srcId", ticketDTO.getFromStation().getCode());
			allocatedDataJSON.put("destId", ticketDTO.getToStation().getCode());

			List<String> bookedSeats = new ArrayList<>();
			List<String> cancelledSeats = new ArrayList<>();
			List<String> blockedSeats = new ArrayList<>();
			List<String> swapSeats = new ArrayList<>();
			String bookingChannel = ticketDTO.getDeviceMedium().getId() == 2 || ticketDTO.getDeviceMedium().getId() == 3 || ticketDTO.getDeviceMedium().getId() == 4 ? "Online Booking" : ticketDTO.getDeviceMedium().getId() == 1 ? "Offline Booking" : "Offline Booking";
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					bookedSeats.add(ticketDetailsDTO.getSeatName() + Text.VERTICAL_BAR + ticketDetailsDTO.getSeatFare() + Text.VERTICAL_BAR + bookingChannel + Text.VERTICAL_BAR + ticketDetailsDTO.getSeatTypeName() + Text.VERTICAL_BAR + DateUtil.convertDateTime(DateUtil.NOW()) + Text.VERTICAL_BAR + ticketDetailsDTO.getSeatGendar().getCode() + ticketTransfer);
				}
				else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TICKET_TRANSFERRED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TRIP_CANCELLED.getId()) {
					cancelledSeats.add(ticketDetailsDTO.getSeatName() + Text.VERTICAL_BAR + ticketDetailsDTO.getSeatFare() + Text.VERTICAL_BAR + bookingChannel + Text.VERTICAL_BAR + ticketDetailsDTO.getSeatTypeName() + Text.VERTICAL_BAR + DateUtil.convertDateTime(DateUtil.NOW()) + Text.VERTICAL_BAR + ticketDetailsDTO.getSeatGendar().getCode() + ticketTransfer);
				}
				else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId()) {
					blockedSeats.add(ticketDetailsDTO.getSeatName() + Text.VERTICAL_BAR + ticketDetailsDTO.getSeatFare() + Text.VERTICAL_BAR + bookingChannel + Text.VERTICAL_BAR + ticketDetailsDTO.getSeatTypeName() + Text.VERTICAL_BAR + DateUtil.convertDateTime(DateUtil.NOW()) + Text.VERTICAL_BAR + ticketDetailsDTO.getSeatGendar().getCode() + ticketTransfer);
				}
				else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TICKET_SEAT_SWAP.getId()) {
					swapSeats.add(ticketDetailsDTO.getName() + Text.HYPHEN + ticketDetailsDTO.getSeatName());
				}
			}
			allocatedDataJSON.put("bookedSeats", bookedSeats);
			allocatedDataJSON.put("cancelledSeats", cancelledSeats);
			allocatedDataJSON.put("blockedSeats", blockedSeats);
			if (!swapSeats.isEmpty()) {
				allocatedDataJSON.put("swapSeats", swapSeats);
			}
			allocatedData.add(allocatedDataJSON);
			jsonObject.put("allocatedData", allocatedData);

			response = makeAPIRequestConnection(UPDATE_SALES, jsonObject, redbusVendorKey, ticketDTO.getCode());
		}
		catch (ServiceException e) {
			DYNAMIC_LOGGER.error("NS: {} PNR: {} - {}", authDTO.getNamespaceCode(), ticketDTO.getCode(), " Vendor key not found");
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.info(jsonObject + HYPHEN_SPACE + response);
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	public void updateSeatStatus(AuthDTO authDTO, StationDTO fromStation, StationDTO toStation, ScheduleSeatVisibilityDTO scheduleSeatVisibility) {
		String response = Text.EMPTY;
		JSONObject jsonObject = new JSONObject();
		try {
			RedbusVendorKeyEM redbusVendorKey = RedbusVendorKeyEM.getRedbusVendorKeyEM(authDTO.getNamespaceCode());

			jsonObject.put(ROUTE_ID, scheduleSeatVisibility.getSchedule().getCode());
			jsonObject.put("serviceId", scheduleSeatVisibility.getSchedule().getCode());
			jsonObject.put("doj", DateUtil.convertDate(DateUtil.getDateTime(scheduleSeatVisibility.getActiveFrom())));

			JSONArray allocatedData = new JSONArray();
			JSONObject allocatedDataJSON = new JSONObject();
			allocatedDataJSON.put("srcId", fromStation.getCode());
			allocatedDataJSON.put("destId", toStation.getCode());

			List<String> blockedSeats = new ArrayList<>();
			for (BusSeatLayoutDTO seatLayoutDTO : scheduleSeatVisibility.getBus().getBusSeatLayoutDTO().getList()) {
				blockedSeats.add(seatLayoutDTO.getName() + Text.VERTICAL_BAR + Numeric.ZERO);
			}

			if ("HIDE".equals(scheduleSeatVisibility.getVisibilityType())) {
				allocatedDataJSON.put("blockedSeats", new ArrayList<>());
				allocatedDataJSON.put("cancelledSeats", blockedSeats);
			}
			else {
				allocatedDataJSON.put("blockedSeats", blockedSeats);
				allocatedDataJSON.put("cancelledSeats", new ArrayList<>());
			}

			allocatedDataJSON.put("bookedSeats", new ArrayList<>());
			allocatedData.add(allocatedDataJSON);
			jsonObject.put("allocatedData", allocatedData);

			response = makeAPIRequestConnection(UPDATE_SALES, jsonObject, redbusVendorKey, "NA");
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.info(jsonObject + HYPHEN_SPACE + response);
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
	}

	public JSONObject updateScheduleStageFare(String namespaceCode, ScheduleDynamicStageFareDTO stageFareDTO, List<ScheduleDynamicStageFareDetailsDTO> stageFares) {
		JSONObject response = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		try {
			RedbusVendorKeyEM redbusVendorKey = RedbusVendorKeyEM.getRedbusVendorKeyEM(namespaceCode);

			jsonObject.put(ROUTE_ID, stageFareDTO.getSchedule().getCode());
			jsonObject.put("route_active_from", DateUtil.convertDate(DateUtil.getDateTime(stageFareDTO.getActiveFrom())));

			Comparator<ScheduleStationDTO> comp = new BeanComparator("stationSequence");
			Collections.sort(stageFareDTO.getSchedule().getStationList(), comp);

			JSONArray stations = new JSONArray();
			for (ScheduleStationDTO scheduleStationDTO : stageFareDTO.getSchedule().getStationList()) {
				stations.add(scheduleStationDTO.getStation().getCode());
			}
			jsonObject.put("city_sequence", stations);

			JSONArray routes = new JSONArray();
			for (ScheduleDynamicStageFareDetailsDTO scheduleDynamicStageFareDetails : stageFares) {
				JSONObject route = new JSONObject();
				route.put("route", scheduleDynamicStageFareDetails.getFromStation().getCode() + Text.HYPHEN + scheduleDynamicStageFareDetails.getToStation().getCode() + Text.HYPHEN + stageFareDTO.getSchedule().getCode());
				route.put("day_identifier", 1);
				routes.add(route);
			}
			jsonObject.put("bData", routes);

			String apiData = makeAPIRequestConnection(UPDATE_VIA_ROUTES, jsonObject, redbusVendorKey, "NA");
			response = JSONObject.fromObject(apiData);
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.info(jsonObject + HYPHEN_SPACE + response);
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
		return response;
	}

	public JSONObject getActiveRoutes(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime tripDate) {
		JSONObject response = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		try {
			RedbusVendorKeyEM redbusVendorKey = RedbusVendorKeyEM.getRedbusVendorKeyEM(authDTO.getNamespaceCode());

			jsonObject.put(ROUTE_ID, scheduleDTO.getCode());
			jsonObject.put("doj", DateUtil.convertDate(tripDate));

			String apiData = makeAPIRequestConnection(ACTIVE_ROUTES, jsonObject, redbusVendorKey, "NA");
			response = JSONObject.fromObject(apiData);
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.info(jsonObject + HYPHEN_SPACE + response);
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
		return response;
	}

	public JSONObject removeScheduleStageFare(String namespaceCode, ScheduleDynamicStageFareDTO stageFareDTO, List<ScheduleDynamicStageFareDetailsDTO> stageFares) {
		JSONObject response = new JSONObject();
		JSONObject jsonObject = new JSONObject();
		try {
			RedbusVendorKeyEM redbusVendorKey = RedbusVendorKeyEM.getRedbusVendorKeyEM(namespaceCode);

			jsonObject.put(ROUTE_ID, stageFareDTO.getSchedule().getCode());
			jsonObject.put("route_removed_from", DateUtil.convertDate(DateUtil.getDateTime(stageFareDTO.getActiveFrom())));

			Comparator<ScheduleStationDTO> comp = new BeanComparator("stationSequence");
			Collections.sort(stageFareDTO.getSchedule().getStationList(), comp);

			JSONArray stations = new JSONArray();
			for (ScheduleStationDTO scheduleStationDTO : stageFareDTO.getSchedule().getStationList()) {
				stations.add(scheduleStationDTO.getStation().getCode());
			}
			jsonObject.put("city_sequence", stations);

			JSONArray routes = new JSONArray();
			for (ScheduleDynamicStageFareDetailsDTO scheduleDynamicStageFareDetails : stageFares) {
				JSONObject route = new JSONObject();
				route.put("route", scheduleDynamicStageFareDetails.getFromStation().getCode() + Text.HYPHEN + scheduleDynamicStageFareDetails.getToStation().getCode() + Text.HYPHEN + stageFareDTO.getSchedule().getCode());
				route.put("day_identifier", 1);
				routes.add(route);
			}
			jsonObject.put("bData", routes);
			String apiData = makeAPIRequestConnection(REMOVE_VIA_ROUTES, jsonObject, redbusVendorKey, "NA");
			response = JSONObject.fromObject(apiData);
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.info(jsonObject + HYPHEN_SPACE + response);
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
		return response;
	}

	private String makeAPIRequestConnection(String url, JSONObject jsonObject, RedbusVendorKeyEM vendorKey, String loghint) {
		String response = null;
		try {
			HttpClient client = new HttpServiceClient().getHttpClient();
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("vendor-key", vendorKey.getVendorKey());
			httpPost.addHeader("gds-key", GDS_KEY);
			httpPost.addHeader("accept", MediaType.APPLICATION_JSON_VALUE);
			httpPost.addHeader("content-type", MediaType.APPLICATION_JSON_VALUE);
			StringEntity input = new StringEntity(jsonObject.toString(), ContentType.APPLICATION_JSON);
			input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE));
			httpPost.setEntity(input);

			HttpResponse responseData = client.execute(httpPost);
			HttpEntity entity = responseData.getEntity();
			response = EntityUtils.toString(entity, "UTF-8");
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.error("{} - {} - {} - {} - {}", vendorKey, url, loghint, jsonObject, e.getMessage());
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
		finally {
			DYNAMIC_LOGGER.info("{} - {} - {} - {} - {}", vendorKey, url, loghint, jsonObject, StringUtil.substring(response, 100));
		}
		return response;
	}

	public JSONObject updateTripCancel(AuthDTO authDTO, TicketDTO ticketDTO, String flag) {
		JSONObject json = null;
		JSONObject ticket = new JSONObject();
		try {
			String url = "http://gds.redbus.com/post/rb_ias_bus_cancelled_notification";
			ticket.put("email", ticketDTO.getPassengerEmailId());
			ticket.put("mobileNo", new BigDecimal(ticketDTO.getPassengerMobile()).setScale(0));
			ticket.put("PNR", ticketDTO.getCode());
			ticket.put("operatorId", authDTO.getNamespaceCode());
			ticket.put("doj", ticketDTO.getTripDate().format("YYYY-MM-DD"));
			ticket.put("sourceId", ticketDTO.getFromStation().getCode());
			ticket.put("destinationId", ticketDTO.getToStation().getCode());

			List<String> seats = new ArrayList<String>();
			for (TicketDetailsDTO detailsDTO : ticketDTO.getTicketDetails()) {
				seats.add(detailsDTO.getSeatCode().trim());
			}
			ticket.put("seats", seats);
			ticket.put("flag", flag);

			DYNAMIC_LOGGER.info(url + HYPHEN_SPACE + ticket);

			HttpClient client = new HttpServiceClient().getHttpClient();
			HttpPost httpPost = new HttpPost(url);
			httpPost.addHeader("accept", "application/json");
			httpPost.addHeader("content-type", "application/json");
			StringEntity input = new StringEntity(ticket.toString(), ContentType.APPLICATION_JSON);
			input.setContentType(new BasicHeader(HTTP.CONTENT_TYPE, "application/json"));
			httpPost.setEntity(input);

			HttpResponse responseData = client.execute(httpPost);
			HttpEntity entity = responseData.getEntity();
			String response = EntityUtils.toString(entity, "UTF-8");
			DYNAMIC_LOGGER.info(ticketDTO.getCode() + " Response: ", response);

			json = JSONObject.fromObject(response);
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.info(ticket + HYPHEN_SPACE + json);
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
		return json;
	}

	public JSONObject updateDynamicPriceStatus(AuthDTO authDTO, ScheduleDynamicStageFareDTO scheduleSeatFare) {
		JSONObject jsonObject = new JSONObject();
		JSONObject response = new JSONObject();
		try {
			RedbusVendorKeyEM redbusVendorKey = RedbusVendorKeyEM.getRedbusVendorKeyEM(authDTO.getNamespaceCode());
			JSONArray jsonArray = new JSONArray();
			jsonArray.add(scheduleSeatFare.getActiveFrom());
			
			jsonObject.put("rId", scheduleSeatFare.getSchedule().getCode());
			jsonObject.put("state_changed_for_specific_days", jsonArray);
			jsonObject.put("start_date", scheduleSeatFare.getActiveFrom());
			jsonObject.put("end_date", scheduleSeatFare.getActiveTo());
			jsonObject.put("state", scheduleSeatFare.getStatus() == 1 ? "ENABLED" : "DISABLED");
			
			String apiData = makeAPIRequestConnection(UPDATE_DP_STATUS, jsonObject, redbusVendorKey, "NA");
			response = JSONObject.fromObject(apiData);
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.info(jsonObject + HYPHEN_SPACE + response);
			DYNAMIC_LOGGER.error("", e);
			e.printStackTrace();
		}
		return response;
	}
}
