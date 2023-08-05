package org.in.com.aggregator.gps.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Numeric;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GPSLocationDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.HttpServiceClient;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class JTrackServiceImpl implements GPSInterface {
	private static final Logger logger = LoggerFactory.getLogger("org.in.com.aggregator.bits.trackbus");
	Map<String, String> keyMap = new HashMap<String, String>();

	private final String JTECHURL = "http://43.204.3.122:9491/easyinfo";

	public JTrackServiceImpl() {
		keyMap.put("parveen", "94c99e9373efc7ee94c3a2f0926b2fa1");
		keyMap.put("parveenbits", "94c99e9373efc7ee94c3a2f0926b2fa1");
	}

	public GPSLocationDTO getVehicleLocation(String operatorCode, String deviceCode, String vehicleRegistrationeNumber) {
		GPSLocationDTO gpsLocationDTO = getDeviceByVehicle(keyMap.get(operatorCode), StringUtil.removeSymbol(vehicleRegistrationeNumber));
		return gpsLocationDTO;
	}

	private GPSLocationDTO getDeviceByVehicle(String apiKey, String vehicleNumber) {
		GPSLocationDTO gpsLocationDTO = new GPSLocationDTO();
		JSONObject json = getDevice(apiKey, vehicleNumber);
		try {
			if (Numeric.ONE_INT != json.getInt("status")) {
				throw new ServiceException(ErrorCode.GPS_DEVICE_LOCATION_NOT_FOUND);
			}
			JSONObject jsonObject = (JSONObject) json.getJSONArray("data").get(0);
			gpsLocationDTO.setRegisterNumber(jsonObject.has("vehicle_number") ? jsonObject.getString("vehicle_number") : null);
			gpsLocationDTO.setLatitude(jsonObject.has("lat_message") ? jsonObject.getString("lat_message") : null);
			gpsLocationDTO.setLongitude(jsonObject.has("lon_message") ? jsonObject.getString("lon_message") : null);
			gpsLocationDTO.setUpdatedTime(jsonObject.has("created_date") ? DateUtil.parseDateFormat(jsonObject.getString("created_date"), "dd-MM-yyyy HH:mm:ss", "yyyy-MM-dd HH:mm:ss") : null);
			gpsLocationDTO.setSpeed(jsonObject.has("speed") ? Float.parseFloat(jsonObject.getString("speed")) : null);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return gpsLocationDTO;
	}

	@Override
	public List<GPSLocationDTO> getAllDevice(String apiKey) {
		List<GPSLocationDTO> GPSLocationList = new ArrayList<GPSLocationDTO>();
		JSONObject json = getDevice(apiKey, null);
		try {
			if (Numeric.ONE_INT != json.getInt("status")) {
				throw new ServiceException(ErrorCode.GPS_DEVICE_LOCATION_NOT_FOUND);
			}
			JSONArray jsonArray = json.getJSONArray("data");
			for (Object object : jsonArray) {
				JSONObject jsonObject = (JSONObject) object;
				GPSLocationDTO gpsLocationDTO = new GPSLocationDTO();
				gpsLocationDTO.setRegisterNumber(jsonObject.has("vehicle_number") ? jsonObject.getString("vehicle_number") : null);
				gpsLocationDTO.setLatitude(jsonObject.has("lat_message") ? jsonObject.getString("lat_message") : null);
				gpsLocationDTO.setLongitude(jsonObject.has("lon_message") ? jsonObject.getString("lon_message") : null);
				gpsLocationDTO.setUpdatedTime(jsonObject.has("created_date") ? jsonObject.getString("created_date") : null);
				gpsLocationDTO.setSpeed(jsonObject.has("speed") ? Float.parseFloat(jsonObject.getString("speed")) : null);
				GPSLocationList.add(gpsLocationDTO);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		return GPSLocationList;
	}

	private JSONObject getDevice(String apiKey, String vehicleNumber) {
		JSONObject json = new JSONObject();
		try {
			HttpServiceClient httpClient = new HttpServiceClient();
			Map<String, String> headerMap = new HashMap<String, String>();
			headerMap.put("api", apiKey);
			headerMap.put("vehicle_number", vehicleNumber);
			String jsonData = httpClient.get(JTECHURL, null, headerMap);
			json = JSONObject.fromObject(jsonData);
		}
		catch (Exception e) {
			logger.error("vehicle: " + vehicleNumber + " - " + e.getMessage());
		}
		finally {
			logger.info("vehicle: " + vehicleNumber + " - " + json.toString());
		}
		return json;
	}

	@Override
	public void updateGeoTripDetails(AuthDTO authDTO, TripChartDTO tripChartDTO) {
		// TODO Auto-generated method stub

	}

	@Override
	public void removeGeoTripDetails(AuthDTO authDTO, TripDTO tripDTO) {
		// TODO Auto-generated method stub

	}

}
