package org.in.com.aggregator.mercservices;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.NamespaceProfileDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleTripStageFareDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketHistoryDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.utils.DateUtil;
import org.in.com.utils.JsonArrayBuilder;
import org.in.com.utils.JsonBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class MercServiceImpl implements MercService {
	public static Map<String, String> config = new HashMap<>();

	@Autowired
	MercCommunicator mercCommunicator;

	@Async
	public void indexTicketHistory(AuthDTO auth, TicketDTO Ticket, TicketHistoryDTO history) {
		String indexDate =  Ticket.getTicketAt().format("YYMM");

		UserDTO nativeUser = auth.getUser();
		JsonArrayBuilder indexJson = new JsonArrayBuilder()
				.add(new JsonBuilder()
						.add("code", history.getCode())
						.add("namespace", auth.getNamespaceCode())
						.add("username", history.getUser().getName())
						.add("usercode", history.getUser().getCode())
						.add("eventcode", history.getStatus().getCode())
						.add("log", history.getLog())
						.add("addtionalevent", history.getAddtionalEvent())
						.add("updatedat", history.getDatetime())
						.add("updatedby", nativeUser.getName() + nativeUser.getLastname()).toJson());

		mercCommunicator.index(auth, MercEntityTypeEM.TICKET_HISTORY, indexDate, indexJson);
	}

	@Async
	public void indexFareHistory(AuthDTO auth, ScheduleDTO schedule, TripDTO trip, List<ScheduleDynamicStageFareDetailsDTO> dynamicStageFareRepoList) {
		try {
			String indexDate = trip.getTripDate().format("YYMM");
			String dateTime = DateUtil.convertDateTime(DateUtil.NOW());
			JsonArrayBuilder indexJson = new JsonArrayBuilder();
			for (ScheduleDynamicStageFareDetailsDTO fareDetails : dynamicStageFareRepoList) {
				Map<String, BigDecimal> minmaxFare = fareDetails.getMinMaxFare();
				indexJson.add(new JsonBuilder()
								.add("code", trip.getCode())
								.add("tripdate", trip.getTripDate().format("YYYY-MM-DD"))
								.add("schedule", schedule.getCode())
								.add("fromstation", fareDetails.getFromStation().getCode())
								.add("tostation", fareDetails.getToStation().getCode())
								.add("minfare", minmaxFare.get("minFare"))
								.add("maxfare", minmaxFare.get("maxFare"))
								.add("updatedby", fareDetails.getDynamicPriceProvider().getCode())
								.add("updatedat", dateTime)
								.add("faresource", fareDetails.getDynamicPriceProvider().getCode()).toJson());
			}
			mercCommunicator.index(auth, MercEntityTypeEM.FARE_HISTORY, indexDate, indexJson);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Async
	public void indexFareHistory(AuthDTO authDTO, ScheduleDTO schedule, TripDTO trip, ScheduleTripStageFareDTO quickFareOverride) {
		try {
			String indexDate = trip.getTripDate().format("YYMM");
			String dateTime = DateUtil.convertDateTime(DateUtil.NOW());
			JsonArrayBuilder indexJson = new JsonArrayBuilder();
			for (ScheduleTripStageFareDTO fareDetails : quickFareOverride.getList()) {
				Map<String, BigDecimal> minmaxFare = fareDetails.getRoute().getMinMaxFare();
				indexJson.add(new JsonBuilder()
						.add("code", trip.getCode())
						.add("tripdate", trip.getTripDate().format("YYYY-MM-DD"))
						.add("schedule", schedule.getCode())
						.add("fromstation", fareDetails.getRoute().getFromStation().getCode())
						.add("tostation", fareDetails.getRoute().getToStation().getCode())
						.add("minfare", minmaxFare.get("minFare"))
						.add("maxfare", minmaxFare.get("maxFare"))
						.add("updatedat", dateTime)
						.add("updatedby", authDTO.getUser().getName())
						.add("faresource", "quickfare").toJson());
			}
			mercCommunicator.index(authDTO, MercEntityTypeEM.FARE_HISTORY, indexDate, indexJson);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Async
	public void indexMenuPrivilegeAuditHistory(AuthDTO authDTO, MenuDTO menuDTO, String action) {
		String indexDate = DateUtil.NOW().format("YYMM");
		JsonArrayBuilder indexJson = new JsonArrayBuilder()
				        .add(new JsonBuilder()
						.add("code", menuDTO.getCode())
						.add("menuname", menuDTO.getName())
                        .add("producttype", menuDTO.getProductType().getCode())
                        .add("producttypename", menuDTO.getProductType().getName())
						.add("namespace", authDTO.getNamespace().getName())
						.add("updatedby", authDTO.getUser().getCode())
						.add("updatedbyname", authDTO.getUser().getUsername())
						.add("action", action)
						.add("updatedat", indexDate).toJson()); 
		 mercCommunicator.index(authDTO, MercEntityTypeEM.MENU_AUDIT_HISTORY, indexDate, indexJson);				
	}
	
	@Async
	public void indexNamespaceProfileHistory(AuthDTO authDTO, NamespaceProfileDTO profileDTO, JSONArray namespaceHistory) {
		String indexDate = DateUtil.NOW().format("YYMM");
		JsonArray historyArray = convertJSONArrayToJsonArray(namespaceHistory);
		JsonArrayBuilder indexJson = new JsonArrayBuilder()
		                             .add(new JsonBuilder()
		                             .add("namespace", authDTO.getNamespace().getCode())
		                             .add("namespacename", authDTO.getNamespace().getName())
		                             .add("data", historyArray)
		                             .add("updatedby", authDTO.getUser().getCode())
		     						 .add("updatedbyname", authDTO.getUser().getUsername())
		                             .add("updatedat", indexDate).toJson());
		mercCommunicator.index(authDTO, MercEntityTypeEM.NAMESPACE_PROFILE_HISTORY, indexDate, indexJson);		
	}


	private JsonArray convertJSONArrayToJsonArray(JSONArray namespaceHistory) {
       
        List<JSONObject> netSfJsonObjects = new ArrayList<>();
        for (Object object : namespaceHistory) {
            JSONObject netSfJsonObject = (JSONObject) object;
            netSfJsonObjects.add(netSfJsonObject);
        }

        JsonArray gsonJsonArray = new JsonArray();
        for (JSONObject netSfJsonObject : netSfJsonObjects) {
            JsonObject gsonJsonObject = new com.google.gson.JsonParser().parse(netSfJsonObject.toString()).getAsJsonObject();
            gsonJsonArray.add(gsonJsonObject);
        }
		return gsonJsonArray;
	}

	public List<TicketHistoryDTO> searchTicketHistory(AuthDTO auth, TicketDTO Ticket) {
		String postUrl = "";
		JsonObject jsonObject = mercCommunicator.search(auth, MercEntityTypeEM.TICKET_HISTORY, postUrl, null);
		return null;
	}

}
