package org.in.com.aggregator.backup;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.utils.DateUtil;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class DRServiceImpl implements DRService {

	@Async
	public void flushTicketDetails(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			JSONObject dataModel = new JSONObject();
			dataModel.put("namespace_code", authDTO.getNamespace().getCode());
			dataModel.put("namespace_name", authDTO.getNamespace().getName());
			dataModel.put("ticket_code", ticketDTO.getCode());
			dataModel.put("travel_date", ticketDTO.getTripDate().format("YYYY-MM-DD"));
			dataModel.put("travel_minutes", "" + ticketDTO.getTravelMinutes());
			dataModel.put("from_station_code", ticketDTO.getFromStation().getCode());
			dataModel.put("from_station_name", ticketDTO.getFromStation().getName());
			dataModel.put("to_station_code", ticketDTO.getToStation().getCode());
			dataModel.put("to_station_name", ticketDTO.getToStation().getCode());
			dataModel.put("boarding_point_code", ticketDTO.getBoardingPoint().getCode());
			dataModel.put("boarding_point_name", ticketDTO.getBoardingPoint().getName());
			dataModel.put("boarding_point_minutes", "" + ticketDTO.getBoardingPoint().getMinitues());
			dataModel.put("bus_code", ticketDTO.getTripDTO().getBus().getCode());
			dataModel.put("bus_type", ticketDTO.getTripDTO().getBus().getCategoryCode());
			dataModel.put("schedule_code", ticketDTO.getTripDTO().getSchedule() != null ? ticketDTO.getTripDTO().getSchedule().getCode() : "NA");
			dataModel.put("schedule_name", ticketDTO.getTripDTO().getSchedule() != null ? ticketDTO.getTripDTO().getSchedule().getName() : "NA");
			dataModel.put("trip_code", ticketDTO.getTripDTO().getCode());
			dataModel.put("trip_stage_code", ticketDTO.getTripDTO().getStage().getCode());
			dataModel.put("mobile_number", ticketDTO.getPassengerMobile());
			dataModel.put("email_id", ticketDTO.getPassengerEmailId());
			dataModel.put("device_medium", ticketDTO.getDeviceMedium().getCode());
			dataModel.put("ticket_status_code", ticketDTO.getTicketStatus().getCode());
			dataModel.put("user_code", authDTO.getUserCode());
			dataModel.put("user_name", authDTO.getUser().getUsername());
			dataModel.put("user_role_code", authDTO.getUser().getUserRole().getCode() + "-" + authDTO.getGroup().getCode());
			dataModel.put("user_role_name", authDTO.getGroup().getName());
			dataModel.put("created_at", DateUtil.NOW().format("YYYY-MM-DD hh:mm:ss"));
			JSONObject ticketDetailsModel = new JSONObject();
			JSONArray ticketDetailsArray = new JSONArray();
			for (TicketDetailsDTO detailsDTO : ticketDTO.getTicketDetails()) {
				ticketDetailsModel.put("seat_code", detailsDTO.getSeatCode());
				ticketDetailsModel.put("seat_name", detailsDTO.getSeatName());
				ticketDetailsModel.put("seat_gender_code", detailsDTO.getSeatGendar().getCode());
				ticketDetailsModel.put("seat_fare", detailsDTO.getSeatFare().toString());
				ticketDetailsModel.put("ac_bus_tax", detailsDTO.getAcBusTax().toString());
				ticketDetailsModel.put("passenger_name", detailsDTO.getPassengerName());
				ticketDetailsModel.put("passenger_age", "" + detailsDTO.getPassengerAge());
				ticketDetailsModel.put("seat_status_code", detailsDTO.getTicketStatus().getCode());
				ticketDetailsArray.add(ticketDetailsModel);
			}
			dataModel.put("seats", ticketDetailsArray);

//			HttpServiceClient httpClient = new HttpServiceClient();
//			httpClient.post("http://dr.ezeebits.com/ers", dataModel.toString());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
