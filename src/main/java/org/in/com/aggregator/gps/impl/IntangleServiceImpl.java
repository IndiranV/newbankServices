package org.in.com.aggregator.gps.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.GPSLocationDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripDTO;
import org.in.com.utils.DateUtil;
import org.in.com.utils.HttpServiceClient;

import com.google.gson.Gson;

import net.sf.json.JSONObject;

public class IntangleServiceImpl implements GPSInterface {
	Map<String, String> keyMap = new HashMap<String, String>();
	private final String RTINTANGLEURL = "http://vts-api.rajeshbus.com/intangles/get-vehicle-geo-data/";

	public IntangleServiceImpl() {
	}

	public GPSLocationDTO getVehicleLocation(String operatorCode, String deviceCode, String vehicleRegistrationeNumber) {

		String url = RTINTANGLEURL + deviceCode;
		GPSLocationDTO gpsLocationDTO = new GPSLocationDTO();
		try {
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get(url);
			JSONObject jsonObject = JSONObject.fromObject(jsonData);
			if (jsonObject.getInt("status") == 1) {
				JSONObject json =   jsonObject.getJSONObject("data");
				gpsLocationDTO.setRegisterNumber(json.has("vehicle_id") ? json.getString("vehicle_id") : null);
				gpsLocationDTO.setLatitude(json.has("geo") ? json.getJSONObject("geo").getString("lat") : null);
				gpsLocationDTO.setLongitude(json.has("geo") ? json.getJSONObject("geo").getString("lng") : null);
				gpsLocationDTO.setUpdatedTime(DateUtil.getEpochToDatetime(json.getLong("time")).format("YYYY-MM-DD hh:mm:ss"));
				gpsLocationDTO.setSpeed(json.has("sp") ? Float.parseFloat(json.getString("sp")) : null);
			}
		}
		catch (Exception e) {
			System.out.println(url + " - ");
			e.printStackTrace();
		}
		return gpsLocationDTO;
	}

	@Override
	public void updateGeoTripDetails(AuthDTO authDTO, TripChartDTO tripChartDTO) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeGeoTripDetails(AuthDTO authDTO, TripDTO tripDTO) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<GPSLocationDTO> getAllDevice(String apiKey) {
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String a[]) {
		IntangleServiceImpl implInstance = new IntangleServiceImpl();
		GPSLocationDTO gpsLocationDTO = implInstance.getVehicleLocation("rajeshtransports", "AP03TC1111", "");
		System.out.println(new Gson().toJson(gpsLocationDTO));
	}
}
