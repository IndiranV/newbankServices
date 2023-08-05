package org.in.com.aggregator.gps.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.cache.CacheCentral;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.GPSLocationDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TravelStopsDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.impl.BusImpl;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.HttpServiceClient;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class GEOServiceImpl extends CacheCentral implements GPSInterface {

	private static String API_URL = "http://geo.ezeebits.com/geoservices";

	public GPSLocationDTO getVehicleLocation(String operatorCode, String deviceCode, String vehicleNumber) {
		JSONObject json = getDeviceLocation(operatorCode, deviceCode);
		return getDeviceLocation(json);
	}

	private JSONObject getDeviceLocation(String operatorCode, String deviceCode) {
		JSONObject jsonObject = null;
		try {
			String url = "/api/1.0/json/" + operatorCode + "/location/" + deviceCode + "/track";
			HttpServiceClient httpClient = new HttpServiceClient();
			String response = httpClient.get(API_URL + url);
			jsonObject = JSONObject.fromObject(response);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.GPS_DEVICE_LOCATION_NOT_FOUND);
		}
		return jsonObject;
	}

	private GPSLocationDTO getDeviceLocation(JSONObject geoObject) {
		GPSLocationDTO gpsLocationDTO = new GPSLocationDTO();
		try {
			if (Numeric.ONE_INT != geoObject.getInt("status")) {
				throw new ServiceException(ErrorCode.GPS_DEVICE_LOCATION_NOT_FOUND);
			}
			JSONObject jsonObject = geoObject.getJSONObject("data");
			gpsLocationDTO.setLatitude(jsonObject.has("latitude") ? jsonObject.getString("latitude") : null);
			gpsLocationDTO.setLongitude(jsonObject.has("longitude") ? jsonObject.getString("longitude") : null);
			gpsLocationDTO.setRegisterNumber(jsonObject.has("regNo") ? jsonObject.getString("regNo") : null);
			gpsLocationDTO.setSpeed(jsonObject.has("speed") ? Float.parseFloat(jsonObject.getString("speed")) : null);
			gpsLocationDTO.setAddress(jsonObject.has("address") ? jsonObject.getString("address") : null);
			gpsLocationDTO.setUpdatedTime(jsonObject.has("time") ? jsonObject.getString("time") : null);
			gpsLocationDTO.setIgnition(jsonObject.has("ignition") ? jsonObject.getBoolean("ignition") : null);
			gpsLocationDTO.setRoad(jsonObject.has("road") ? jsonObject.getString("road") : Text.EMPTY);
			gpsLocationDTO.setArea(jsonObject.has("area") ? jsonObject.getString("area") : Text.EMPTY);
			gpsLocationDTO.setLandmark(jsonObject.has("landmark") ? jsonObject.getString("landmark") : Text.EMPTY);
			gpsLocationDTO.setCity(jsonObject.has("city") ? jsonObject.getString("city") : Text.EMPTY);
			gpsLocationDTO.setState(jsonObject.has("state") ? jsonObject.getString("state") : Text.EMPTY);
			gpsLocationDTO.setPostalCode(jsonObject.has("postalCode") ? jsonObject.getString("postalCode") : Text.EMPTY);
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
	public void updateGeoTripDetails(AuthDTO authDTO, TripChartDTO tripChartDTO) {
		try {
			String url = "/api/1.0/json/" + authDTO.getNamespaceCode() + "/trip/details/update";
			JSONObject tripDetails = getGeoTripDetailsJSON(authDTO, tripChartDTO);
			HttpServiceClient httpClient = new HttpServiceClient();
			String response = httpClient.post(API_URL + url, tripDetails.toString(), "application/json");
			JSONObject jsonObject = JSONObject.fromObject(response);
			if (Numeric.ONE_INT != jsonObject.getInt("status")) {
				System.out.println("GEOERROR: " + API_URL + url + "\n" + tripDetails + " - " + jsonObject);
			}
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	private JSONObject getGeoTripDetailsJSON(AuthDTO authDTO, TripChartDTO tripChartDTO) {
		JSONObject tripDetails = new JSONObject();
		TripDTO tripDTO = tripChartDTO.getTrip();
		if (tripDTO.getTripInfo() != null) {
			tripDetails.put("driverName", tripDTO.getTripInfo().getDriverName());
			tripDetails.put("driverMobileNumber", tripDTO.getTripInfo().getDriverMobile());

			if (tripDTO.getTripInfo().getBusVehicle() != null) {
				JSONObject vehicleJSON = new JSONObject();
				JSONObject deviceJSON = new JSONObject();
				deviceJSON.put("code", tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode());
				vehicleJSON.put("device", deviceJSON);
				tripDetails.put("vehicle", vehicleJSON);

				BusDTO busDTO = getBusDTObyId(authDTO, tripDTO.getBus());
				BusImpl busImpl = new BusImpl();
				tripDetails.put("busType", busImpl.getBusCategoryByCode(busDTO.getCategoryCode()));

				tripDetails.put("scheduleCode", tripDTO.getSchedule().getCode());
				tripDetails.put("scheduleName", tripDTO.getSchedule().getName());
				tripDetails.put("serviceNumber", tripDTO.getSchedule().getServiceNumber());

				DateTime dateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getTripMinutes());
				tripDetails.put("tripCode", tripDTO.getCode());
				tripDetails.put("tripDateTime", dateTime.format("YYYY-MM-DD hh:mm:ss"));
				tripDetails.put("activeFlag", 1);
			}

			// Passenger Count
			Map<Integer, Integer> boardingPointPassenger = new HashMap<>();
			Map<Integer, Integer> droppingPointPassenger = new HashMap<>();
			for (TripChartDetailsDTO chartDetailsDTO : tripChartDTO.getTicketDetailsList()) {
				if (chartDetailsDTO.getBoardingPoint() == null || chartDetailsDTO.getBoardingPoint().getId() == 0 || chartDetailsDTO.getDroppingPoint() == null || chartDetailsDTO.getDroppingPoint().getId() == 0) {
					continue;
				}

				int boardingKey = chartDetailsDTO.getBoardingPoint().getId();
				int droppingKey = chartDetailsDTO.getDroppingPoint().getId();

				// Boarding
				if (boardingPointPassenger.get(boardingKey) == null) {
					boardingPointPassenger.put(boardingKey, Numeric.ONE_INT);
				}
				else if (boardingPointPassenger.get(boardingKey) != null) {
					int stationPointPassengerCount = boardingPointPassenger.get(boardingKey);
					boardingPointPassenger.put(boardingKey, stationPointPassengerCount + Numeric.ONE_INT);
				}
				// Dropping
				if (droppingPointPassenger.get(droppingKey) == null) {
					droppingPointPassenger.put(droppingKey, Numeric.ONE_INT);
				}
				else if (droppingPointPassenger.get(droppingKey) != null) {
					int stationPointPassengerCount = droppingPointPassenger.get(droppingKey);
					droppingPointPassenger.put(droppingKey, stationPointPassengerCount + Numeric.ONE_INT);
				}
			}

			JSONArray stationPoints = new JSONArray();
			// Bus Vehicle Van Pickup Station Point
			Map<String, StationPointDTO> vanPickupStationPointMap = BitsUtil.getBusVehicleVanPickupStationPoint(authDTO, tripDTO.getSchedule());

			ScheduleStationDTO fromStation = BitsUtil.getOriginStation(tripDTO.getStationList());
			ScheduleStationDTO toStation = BitsUtil.getDestinationStation(tripDTO.getStationList());
			if (fromStation != null && fromStation.getStation() != null && toStation != null && toStation.getStation() != null) {
				tripDetails.put("fromStationName", fromStation.getStation().getName());
				tripDetails.put("toStationName", toStation.getStation().getName());
			}
			else {
				tripDetails.put("fromStationName", tripDTO.getSearch().getFromStation().getName());
				tripDetails.put("toStationName", tripDTO.getSearch().getFromStation().getName());
			}

			// Sorting
			Comparator<StageDTO> comp = new BeanComparator("stageSequence");
			Collections.sort(tripDTO.getStageList(), comp);

			// stage
			List<StageStationDTO> stationList = BitsUtil.getStageStations(tripDTO.getStageList());
			List<StationPointDTO> stationPointDTOList = new ArrayList<StationPointDTO>();

			// Sorting
			Comparator<StageStationDTO> stageCom = new BeanComparator("minitues");
			Collections.sort(stationList, stageCom);

			int sequence = 0;
			Map<Integer, List<StationPointDTO>> travelStopMap = getTravelStops(tripDTO);
			for (StageStationDTO stageStationDTO : stationList) {
				StationDTO stationDTO = stageStationDTO.getStation();
				List<StationPointDTO> stationPointList = stageStationDTO.getStationPoint();

				if (!travelStopMap.isEmpty() && travelStopMap.get(stationDTO.getId()) != null) {
					stationPointList.addAll(travelStopMap.get(stationDTO.getId()));
				}
				// Sorting
				Comparator<StationPointDTO> stationPointCom = new BeanComparator("minitues");
				Collections.sort(stationPointList, stationPointCom);

				Map<String, StationPointDTO> stationPointMap = new HashMap<String, StationPointDTO>();
				for (StationPointDTO pointDTO : stationPointList) {
					if (vanPickupStationPointMap.get(pointDTO.getCode()) != null) {
						continue;
					}
					// Avoid Duplicate Station Point
					if (stationPointMap.get(pointDTO.getCode()) != null) {
						continue;
					}

					StationPointDTO stationPointDTO = new StationPointDTO();
					stationPointDTO.setName(pointDTO.getName());
					stationPointDTO.setMinitues(stageStationDTO.getMinitues() + pointDTO.getMinitues());
					stationPointDTOList.add(stationPointDTO);

					sequence = sequence + Numeric.ONE_INT;

					JSONObject stationPoint = new JSONObject();
					stationPoint.put("stationName", stationDTO.getName());
					stationPoint.put("stationCode", stationDTO.getCode());
					// -1 in boarding & dropping point passenger count is to
					// identify the travel stop
					int boardingCount = boardingPointPassenger.get(pointDTO.getId()) != null ? boardingPointPassenger.get(pointDTO.getId()) : pointDTO.getActiveFlag() == -1 ? -1 : Numeric.ZERO_INT;
					int droppingCount = droppingPointPassenger.get(pointDTO.getId()) != null ? droppingPointPassenger.get(pointDTO.getId()) : pointDTO.getActiveFlag() == -1 ? -1 : Numeric.ZERO_INT;
					stationPoint.put("name", pointDTO.getName());
					stationPoint.put("code", pointDTO.getCode());
					stationPoint.put("boardingSeatCount", boardingCount);
					stationPoint.put("droppingSeatCount", droppingCount);
					stationPoint.put("latitude", pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
					stationPoint.put("longitude", pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
					stationPoint.put("expectedTime", stageStationDTO.getMinitues() + pointDTO.getMinitues());
					stationPoint.put("sequence", sequence);
					stationPoint.put("activeFlag", 1);
					stationPoints.add(stationPoint);

					stationPointMap.put(pointDTO.getCode(), pointDTO);
				}
			}
			tripDetails.put("stationPoints", stationPoints);

			if (!stationPointDTOList.isEmpty()) {
				DateTime originDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), BitsUtil.getOriginStationPoint(stationPointDTOList).getMinitues());
				DateTime destinationDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), BitsUtil.getDestinationStationPoint(stationPointDTOList).getMinitues());
				tripDetails.put("originDateTime", originDateTime.format("YYYY-MM-DD hh:mm:ss"));
				tripDetails.put("destinationDateTime", destinationDateTime.format("YYYY-MM-DD hh:mm:ss"));
			}
			else {
				tripDetails.put("originDateTime", tripDTO.getTripDate().getStartOfDay().format("YYYY-MM-DD hh:mm:ss"));
				tripDetails.put("destinationDateTime", tripDTO.getTripDate().getStartOfDay().format("YYYY-MM-DD hh:mm:ss"));
			}
			tripDetails.put("vendors", tripChartDTO.getVendorList());
		}
		return tripDetails;
	}

	private Map<Integer, List<StationPointDTO>> getTravelStops(TripDTO tripDTO) {
		Map<Integer, List<StationPointDTO>> stationPointmap = new HashMap<Integer, List<StationPointDTO>>();
		if (tripDTO.getSchedule().getTravelStopsList() != null) {
			for (TravelStopsDTO travelStop : tripDTO.getSchedule().getTravelStopsList()) {
				int key = travelStop.getStation().getId();

				StationPointDTO stationPoint = new StationPointDTO();
				stationPoint.setName(travelStop.getName());
				stationPoint.setCode(travelStop.getCode());
				stationPoint.setLatitude(travelStop.getLatitude());
				stationPoint.setLongitude(travelStop.getLongitude());
				stationPoint.setMinitues(travelStop.getTravelMinutes());
				stationPoint.setActiveFlag(-1);

				if (stationPointmap.get(key) == null) {
					List<StationPointDTO> stop = new ArrayList<StationPointDTO>();
					stop.add(stationPoint);
					stationPointmap.put(key, stop);
				}
				else {
					List<StationPointDTO> stop = stationPointmap.get(key);
					stop.add(stationPoint);
					stationPointmap.put(key, stop);
				}
			}
		}
		return stationPointmap;
	}

	@Override
	public void removeGeoTripDetails(AuthDTO authDTO, TripDTO tripDTO) {
		try {
			String url = "/api/1.0/json/" + authDTO.getNamespaceCode() + "/trip/" + tripDTO.getCode() + "/details/remove";
			HttpServiceClient httpClient = new HttpServiceClient();
			httpClient.post(API_URL + url, new JSONObject().toString(), "application/json");
		}
		catch (Exception e) {
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public List<GPSLocationDTO> getAllDevice(String apiKey) {
		return null;
	}

}
