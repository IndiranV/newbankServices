package org.in.com.aggregator.orbit;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.dto.StationAreaDTO;
import org.in.com.dto.StationDTO;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class OrbitDataConvertor {

	public List<StationAreaDTO> getStationArea(JSONObject stationAreaObject) {
		List<StationAreaDTO> list = new ArrayList<StationAreaDTO>();
		try {
			if (Numeric.ONE_INT == stationAreaObject.getInt("status")) {
				JSONArray jsonArray = stationAreaObject.getJSONArray("data");
				for (Object stationArea : jsonArray) {
					StationAreaDTO stationAreaDTO = new StationAreaDTO();
					JSONObject jsonObject = (JSONObject) stationArea;
					stationAreaDTO.setName(jsonObject.getString("name"));
					stationAreaDTO.setCode(jsonObject.getString("code"));
					stationAreaDTO.setLatitude(jsonObject.getString("latitude"));
					stationAreaDTO.setLongitude(jsonObject.getString("longitude"));
					stationAreaDTO.setRadius(jsonObject.getInt("radius"));
					stationAreaDTO.setActiveFlag(jsonObject.getInt("activeFlag"));

					JSONObject stationObject = jsonObject.getJSONObject("station");
					StationDTO stationDTO = new StationDTO();
					stationDTO.setName(stationObject.getString("name"));
					stationDTO.setCode(stationObject.getString("code"));
					stationDTO.setActiveFlag(stationObject.getInt("activeFlag"));
					stationAreaDTO.setStation(stationDTO);
					list.add(stationAreaDTO);
				}
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return list;
	}
}