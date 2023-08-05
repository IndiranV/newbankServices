package org.in.com.aggregator.gps.impl;

import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.GPSLocationDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.StringUtil;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class PlayGpsServiceImpl implements GPSInterface {
	private final String PLAY_INTANGLE_URL = "http://vtsapi.rathimeenatravels.in/playgps";

	public PlayGpsServiceImpl() {
	}

	public GPSLocationDTO getVehicleLocation(String operatorCode, String deviceCode, String vehicleRegistrationeNumber) {
		if (StringUtil.isNull(vehicleRegistrationeNumber)) {
			throw new ServiceException(ErrorCode.INVALID_VEHICLE_CODE);
		}
		String url = PLAY_INTANGLE_URL + "/" + operatorCode + "/livedata/" + vehicleRegistrationeNumber;
		GPSLocationDTO gpsLocationDTO = new GPSLocationDTO();
		try {
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get(url);
			JSONObject jsonObject = JSONObject.fromObject(jsonData);
			if (jsonObject.getInt("status") == 1) {
				JSONObject json = jsonObject.getJSONObject("data");
				gpsLocationDTO.setRegisterNumber(json.has("regno") ? json.getString("regno") : null);
				gpsLocationDTO.setLatitude(json.has("lat") ? json.getString("lat") : null);
				gpsLocationDTO.setLongitude(json.has("long") ? json.getString("long") : null);
				gpsLocationDTO.setUpdatedTime(json.has("gpstime") ? DateUtil.parseDateFormat(json.getString("gpstime"), "dd-MM-yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss") : null);
				gpsLocationDTO.setSpeed(json.has("speed") ? Float.parseFloat(json.getString("speed")) : null);
			}
		}
		catch (ServiceException e) {
			throw e;
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
	public List<GPSLocationDTO> getAllDevice(String operatorCode) {
		List<GPSLocationDTO> locationList = new ArrayList<GPSLocationDTO>();
		String url = PLAY_INTANGLE_URL + "/" + operatorCode + "/livedata";
		try {
			HttpServiceClient httpClient = new HttpServiceClient();
			String jsonData = httpClient.get(url);
			JSONObject jsonObject = JSONObject.fromObject(jsonData);
			if (jsonObject.getInt("status") == 1) {
				JSONArray jsonArray = jsonObject.getJSONArray("data");
				for (Object object : jsonArray) {
					JSONObject json = (JSONObject) object;
					GPSLocationDTO gpsLocationDTO = new GPSLocationDTO();
					gpsLocationDTO.setRegisterNumber(json.has("regno") ? json.getString("regno") : null);
					gpsLocationDTO.setLatitude(json.has("lat") ? json.getString("lat") : null);
					gpsLocationDTO.setLongitude(json.has("long") ? json.getString("long") : null);
					gpsLocationDTO.setUpdatedTime(json.has("gpstime") ? DateUtil.parseDateFormat(json.getString("gpstime"), "dd-MM-yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss") : null);
					gpsLocationDTO.setSpeed(json.has("speed") ? Float.parseFloat(json.getString("speed")) : null);
					locationList.add(gpsLocationDTO);
				}
			}
		}
		catch (Exception e) {
			System.out.println(url + " - ");
			e.printStackTrace();
		}
		return locationList;
	}

}
