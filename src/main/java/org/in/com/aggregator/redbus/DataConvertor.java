package org.in.com.aggregator.redbus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.ScheduleDTO;

public class DataConvertor {
	SimpleDateFormat formatter = new SimpleDateFormat("YYYY-MM-DD hh:mm:ss");

	public List<BusSeatLayoutDTO> getStageFare(ScheduleDTO schedule, JSONObject jsonObject) {
		List<BusSeatLayoutDTO> seatFareList = new ArrayList<BusSeatLayoutDTO>();
		try {
			if (jsonObject.has("fareList") && jsonObject.getJSONArray("fareList") != null && jsonObject.getJSONArray("fareList").size() > 0) {
				JSONArray jsonArray = jsonObject.getJSONArray("fareList");
				for (Object routeData : jsonArray) {
					JSONObject routeJSON = (JSONObject) routeData;
					if (routeJSON == null || !routeJSON.has("routes") || routeJSON.getJSONArray("routes") == null || routeJSON.getJSONArray("routes").size() < 1) {
						continue;
					}
					for (Object routeData1 : routeJSON.getJSONArray("routes")) {
						JSONObject routeJSON1 = (JSONObject) routeData1;
						if (routeJSON1 == null || !routeJSON1.has("fares") || routeJSON1.getJSONArray("fares") == null || routeJSON1.getJSONArray("fares").size() < 1) {
							continue;
						}

						JSONArray fareArray = routeJSON1.getJSONArray("fares");
						for (Object fareData : fareArray) {
							JSONObject fareJSON = (JSONObject) fareData;

							BusSeatLayoutDTO busSeatLayoutDTO = new BusSeatLayoutDTO();
							busSeatLayoutDTO.setCode(routeJSON1.getString("routeId"));
							busSeatLayoutDTO.setName(fareJSON.getString("seatName"));
							busSeatLayoutDTO.setFare(new BigDecimal(fareJSON.getString("fare")).setScale(0, RoundingMode.HALF_UP));
							seatFareList.add(busSeatLayoutDTO);
						}
					}
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return seatFareList;
	}

	public Map<String, String> getActiveRoutes(JSONObject jsonObject) {
		Map<String, String> routes = new HashMap<>();
		try {
			if (jsonObject != null && jsonObject.has("routes") && jsonObject.getJSONArray("routes") != null) {
				JSONArray jsonArray = jsonObject.getJSONArray("routes");
				for (Object routeData : jsonArray) {
					String route = routeData.toString();
					routes.put(route, route);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return routes;
	}
}
