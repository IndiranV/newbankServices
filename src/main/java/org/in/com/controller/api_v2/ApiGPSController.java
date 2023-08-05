package org.in.com.controller.api_v2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.in.com.aggregator.bits.BitsService;
import org.in.com.constants.Text;
import org.in.com.controller.api_v2.io.BusIO;
import org.in.com.controller.api_v2.io.ResponseIO;
import org.in.com.controller.api_v2.io.ScheduleIO;
import org.in.com.controller.api_v2.io.StageIO;
import org.in.com.controller.api_v2.io.StationIO;
import org.in.com.controller.api_v2.io.StationPointIO;
import org.in.com.controller.api_v2.io.TripChartDetailsIO;
import org.in.com.controller.api_v2.io.TripChartIO;
import org.in.com.controller.api_v2.io.TripIO;
import org.in.com.controller.api_v2.io.TripInfoIO;
import org.in.com.controller.api_v2.io.TripStatusIO;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.BusVehicleIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.BusService;
import org.in.com.service.ScheduleVisibilityService;
import org.in.com.service.SearchService;
import org.in.com.service.TripService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/api/2.0/json/{apiToken}")
public class ApiGPSController extends BaseController {
	public static Map<String, Integer> ConcurrentRequests = new ConcurrentHashMap<String, Integer>();

	@Autowired
	AuthService authService;
	@Autowired
	BusService busService;
	@Autowired
	TripService tripService;
	@Autowired
	ScheduleVisibilityService visibilityService;
	@Autowired
	SearchService searchService;
	@Autowired
	BitsService bitsService;
	private static final Logger gpslogger = LoggerFactory.getLogger("org.in.com.controller.gpsAPIAccess");

	/*
	 * For GPS Trip call
	 */

	@RequestMapping(value = "/trip/{tripDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripIO>> getAllTrips(@PathVariable("apiToken") String apiToken, @PathVariable("tripDate") String tripDate) throws Exception {
		gpslogger.info("Request for GPS" + apiToken + " " + tripDate);

		checkConcurrentGPSRequests(apiToken);
		List<TripIO> tripList = new ArrayList<>();
		System.out.println("API V2 getAllTrips ---------------------------" + apiToken);

		try {
			AuthDTO authDTO = authService.APIAuthendtication(apiToken);

			if (!DateUtil.isValidDate(tripDate)) {
				throw new ServiceException(ErrorCode.INVALID_DATE);
			}
			DateTime dateTime = new DateTime(tripDate);
			if (DateUtil.getDayDifferent(dateTime, DateUtil.NOW()) != 0) {
				throw new ServiceException(ErrorCode.INVALID_DATE);
			}
			int privilegeType = isGPSShareTripChart(authDTO);
			if (privilegeType == 0) {
				throw new ServiceException(ErrorCode.GPS_NO_SCHEDULE_NOT_FOUND);
			}
			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(new DateTime(tripDate));
			List<TripDTO> finalList = searchService.getAllTrips(authDTO, searchDTO);

			for (TripDTO tripDTO : finalList) {
				TripIO tripIO = new TripIO();
				ScheduleIO schedule = new ScheduleIO();
				schedule.setCode(tripDTO.getSchedule().getCode());
				schedule.setName(tripDTO.getSchedule().getName());
				schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
				schedule.setDisplayName(tripDTO.getSchedule().getDisplayName());
				tripIO.setSchedule(schedule);

				// Bus
				BusIO busIO = new BusIO();
				busIO.setName(tripDTO.getBus().getName());
				busIO.setBusType(BitsUtil.getBusCategoryUsingEM(tripDTO.getBus().getCategoryCode()));
				busIO.setDisplayName(tripDTO.getBus().getDisplayName());
				tripIO.setBus(busIO);

				List<StageIO> stageList = new ArrayList<>();

				// Stage
				for (StageDTO stageDTO : tripDTO.getStageList()) {
					StageIO stageIO = new StageIO();
					StationIO fromStation = new StationIO();
					StationIO toStation = new StationIO();
					fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
					fromStation.setName(stageDTO.getFromStation().getStation().getName());
					fromStation.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					toStation.setCode(stageDTO.getToStation().getStation().getCode());
					toStation.setName(stageDTO.getToStation().getStation().getName());
					stageIO.setStageSequence(stageDTO.getStageSequence());
					toStation.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));

					// Copy Trip informations
					if (tripDTO.getTripInfo() != null) {
						TripInfoIO tripInfo = new TripInfoIO();
						tripInfo.setDriverMobile(tripDTO.getTripInfo().getDriverMobile());
						tripInfo.setDriverName(tripDTO.getTripInfo().getDriverName());
						tripInfo.setTripCloseDateTime(tripDTO.getTripInfo().getTripCloseDateTime() != null ? tripDTO.getTripInfo().getTripCloseDateTime().format(Text.DATE_TIME_DATE4J) : null);
						if (tripDTO.getTripInfo().getBusVehicle() != null) {
							BusVehicleIO busVehicleIO = new BusVehicleIO();
							busVehicleIO.setName(tripDTO.getTripInfo().getBusVehicle().getName());
							busVehicleIO.setCode(tripDTO.getTripInfo().getBusVehicle().getCode());
							busVehicleIO.setRegistrationDate(tripDTO.getTripInfo().getBusVehicle().getRegistrationDate());
							busVehicleIO.setRegistationNumber(tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
							busVehicleIO.setGpsDeviceCode(tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode());
							tripInfo.setBusVehicle(busVehicleIO);
						}
						tripIO.setTripInfo(tripInfo);
					}
					stageIO.setCode(stageDTO.getCode());
					List<StationPointIO> fromStationPoint = new ArrayList<>();
					for (StationPointDTO pointDTO : stageDTO.getFromStation().getStationPoint()) {
						StationPointIO pointIO = new StationPointIO();
						pointIO.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
						pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
						pointIO.setCode(pointDTO.getCode());
						pointIO.setName(pointDTO.getName());
						pointIO.setLandmark(pointDTO.getLandmark());
						pointIO.setAddress(pointDTO.getAddress());
						pointIO.setNumber(pointDTO.getNumber());
						fromStationPoint.add(pointIO);
					}
					List<StationPointIO> toStationPoint = new ArrayList<>();
					for (StationPointDTO pointDTO : stageDTO.getToStation().getStationPoint()) {
						StationPointIO pointIO = new StationPointIO();
						pointIO.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
						pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
						pointIO.setCode(pointDTO.getCode());
						pointIO.setName(pointDTO.getName());
						pointIO.setLandmark(pointDTO.getLandmark());
						pointIO.setAddress(pointDTO.getAddress());
						pointIO.setNumber(pointDTO.getNumber());
						toStationPoint.add(pointIO);
					}
					fromStation.setStationPoint(fromStationPoint);
					toStation.setStationPoint(toStationPoint);
					stageIO.setFromStation(fromStation);
					stageIO.setToStation(toStation);
					stageList.add(stageIO);
				}
				tripIO.setStageList(stageList);
				tripIO.setTripCode(tripDTO.getCode());
				// Trip Status
				TripStatusIO tripStatusIO = new TripStatusIO();
				tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
				tripStatusIO.setName(tripDTO.getTripStatus().getName());
				tripIO.setTripStatus(tripStatusIO);
				tripList.add(tripIO);
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		finally {
			releaseConcurrentGPSRequests(apiToken);
		}
		gpslogger.info("Response for GPS");
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/{tripCode}/passengerDetails", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TripChartIO> getTripTravellerDetails(@PathVariable("apiToken") String apiToken, @PathVariable("tripCode") String tripCode) throws Exception {
		gpslogger.info("Request for passenger with apiToken " + apiToken);
		checkConcurrentGPSRequests(apiToken);
		AuthDTO authDTO = authService.APIAuthendtication(apiToken);
		TripChartIO tripchartIO = new TripChartIO();
		System.out.println("API V2 getTripTravellerDetails ---------------------------" + apiToken);
		try {
			int privilegeType = isGPSShareTripChart(authDTO);
			if (privilegeType == 0) {
				throw new ServiceException(ErrorCode.GPS_NO_SCHEDULE_NOT_FOUND);
			}
			TripChartDTO tripChartDTO = new TripChartDTO();
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);

			tripChartDTO = tripService.getTripChart(authDTO, tripDTO);

			Map<Integer, StationPointDTO> stationPointMap = new HashMap<Integer, StationPointDTO>();
			TripIO tripIO = new TripIO();
			ScheduleIO schedule = new ScheduleIO();
			schedule.setCode(tripDTO.getSchedule().getCode());
			schedule.setName(tripDTO.getSchedule().getName());
			schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
			schedule.setDisplayName(tripDTO.getSchedule().getDisplayName());
			tripIO.setSchedule(schedule);

			// Bus
			BusIO busIO = new BusIO();
			busIO.setName(tripDTO.getBus().getName());
			busIO.setBusType(BitsUtil.getBusCategoryUsingEM(tripDTO.getBus().getCategoryCode()));
			busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
			tripIO.setBus(busIO);
			// Copy Trip informations
			if (tripDTO.getTripInfo() != null) {
				TripInfoIO tripInfo = new TripInfoIO();
				tripInfo.setDriverMobile(tripDTO.getTripInfo().getDriverMobile());
				tripInfo.setDriverName(tripDTO.getTripInfo().getDriverName());
				tripInfo.setTripCloseDateTime(tripDTO.getTripInfo().getTripCloseDateTime() != null ? tripDTO.getTripInfo().getTripCloseDateTime().format(Text.DATE_TIME_DATE4J) : null);
				if (tripChartDTO.getTrip().getTripInfo().getBusVehicle() != null) {
					BusVehicleIO busVehicleIO = new BusVehicleIO();
					busVehicleIO.setName(tripChartDTO.getTrip().getTripInfo().getBusVehicle().getName());
					busVehicleIO.setCode(tripChartDTO.getTrip().getTripInfo().getBusVehicle().getCode());
					busVehicleIO.setRegistrationDate(tripChartDTO.getTrip().getTripInfo().getBusVehicle().getRegistrationDate());
					busVehicleIO.setRegistationNumber(tripChartDTO.getTrip().getTripInfo().getBusVehicle().getRegistationNumber());
					busVehicleIO.setGpsDeviceCode(tripChartDTO.getTrip().getTripInfo().getBusVehicle().getGpsDeviceCode());
					tripInfo.setBusVehicle(busVehicleIO);
				}
				tripIO.setTripInfo(tripInfo);
			}
			tripchartIO.setTrip(tripIO);

			// Stage
			for (StageDTO stageDTO : tripDTO.getStageList()) {
				StageIO stageIO = new StageIO();
				StationIO fromStation = new StationIO();
				StationIO toStation = new StationIO();
				fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
				fromStation.setName(stageDTO.getFromStation().getStation().getName());
				fromStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				toStation.setCode(stageDTO.getToStation().getStation().getCode());
				toStation.setName(stageDTO.getToStation().getStation().getName());
				stageIO.setStageSequence(stageDTO.getStageSequence());
				toStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));

				stageIO.setCode(stageDTO.getCode());
				List<StationPointIO> fromStationPoint = new ArrayList<>();
				for (StationPointDTO pointDTO : stageDTO.getFromStation().getStationPoint()) {
					StationPointIO pointIO = new StationPointIO();
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
					pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
					pointIO.setCode(pointDTO.getCode());
					pointIO.setName(pointDTO.getName());
					pointIO.setLandmark(pointDTO.getLandmark());
					pointIO.setAddress(pointDTO.getAddress());
					pointIO.setNumber(pointDTO.getNumber());
					fromStationPoint.add(pointIO);
					stationPointMap.put(pointDTO.getId(), pointDTO);
				}
				List<StationPointIO> toStationPoint = new ArrayList<>();
				for (StationPointDTO pointDTO : stageDTO.getToStation().getStationPoint()) {
					StationPointIO pointIO = new StationPointIO();
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
					pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
					pointIO.setCode(pointDTO.getCode());
					pointIO.setName(pointDTO.getName());
					pointIO.setLandmark(pointDTO.getLandmark());
					pointIO.setAddress(pointDTO.getAddress());
					pointIO.setNumber(pointDTO.getNumber());
					toStationPoint.add(pointIO);
				}
			}

			List<TripChartDetailsIO> ticketDetailsList = new ArrayList<TripChartDetailsIO>();
			for (TripChartDetailsDTO chartDTO : tripChartDTO.getTicketDetailsList()) {
				TripChartDetailsIO io = new TripChartDetailsIO();
				io.setSeatName(chartDTO.getSeatName());
				io.setTicketCode(chartDTO.getTicketCode());
				// io.setBookedDate(chartDTO.getTicketAt().toString());
				io.setPassengerName(chartDTO.getPassengerName());
				// io.setPassengerAge(chartDTO.getPassengerAge());
				io.setGender(chartDTO.getSeatGendar().getCode());
				io.setPassengerMobile(chartDTO.getPassengerMobile());
				StationIO fromStationIO = new StationIO();
				StationIO toStationIO = new StationIO();
				fromStationIO.setCode(chartDTO.getFromStation().getCode());
				fromStationIO.setName(chartDTO.getFromStation().getName());
				toStationIO.setCode(chartDTO.getToStation().getCode());
				toStationIO.setName(chartDTO.getToStation().getName());
				io.setFromStation(fromStationIO);
				io.setToStation(toStationIO);
				StationPointIO stationPointIO = new StationPointIO();
				if (stationPointMap.get(chartDTO.getBoardingPoint().getId()) != null) {
					StationPointDTO stationPointDTO = stationPointMap.get(chartDTO.getBoardingPoint().getId());
					stationPointIO.setName(stationPointDTO.getName());
					stationPointIO.setCode(stationPointDTO.getCode());
					stationPointIO.setAddress(stationPointDTO.getAddress());
					stationPointIO.setLandmark(stationPointDTO.getLandmark());
				}
				io.setBoardingPoint(stationPointIO);

				// only they booked ticket
				if (privilegeType == 1 && authDTO.getUser().getUsername().equals(chartDTO.getUser().getUsername())) {
					ticketDetailsList.add(io);
				} // All booked ticket
				else if (privilegeType == 2) {
					ticketDetailsList.add(io);
				}
			}
			tripchartIO.setTicketDetailsList(ticketDetailsList);
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		finally {
			releaseConcurrentGPSRequests(apiToken);
		}
		gpslogger.info("Response for getSearchResult done");
		return ResponseIO.success(tripchartIO);
	}

	public static synchronized boolean releaseConcurrentGPSRequests(String apiToken) {
		if (ConcurrentRequests.get(apiToken) != null) {
			if (ConcurrentRequests.get(apiToken) > 0) {
				ConcurrentRequests.put(apiToken, ConcurrentRequests.get(apiToken) - 1);
			}
		}
		return true;
	}

	public static synchronized boolean checkConcurrentGPSRequests(String apiToken) {
		if (ConcurrentRequests.get(apiToken) != null && ConcurrentRequests.get(apiToken) > 2) {
			gpslogger.error("Error reached Max GPS Concurrent Request :" + apiToken + "-->" + ConcurrentRequests.get(apiToken));
			throw new ServiceException(ErrorCode.REACHED_MAX_CONCURRENT_REQUESTS);
		}
		if (ConcurrentRequests.get(apiToken) != null) {
			ConcurrentRequests.put(apiToken, ConcurrentRequests.get(apiToken) + 1);
		}
		else {
			ConcurrentRequests.put(apiToken, 1);
		}
		return true;
	}

	private int isGPSShareTripList(AuthDTO authDTO) {
		JSONObject jsonObject = bitsService.getBitsConfigure(authDTO);
		if (jsonObject != null && jsonObject.has("tripChart")) {
			JSONObject tripChart = jsonObject.getJSONObject("tripChart");
			return tripChart.getInt("gpsShareTripList");
		}
		return 0;
	}

	private int isGPSShareTripChart(AuthDTO authDTO) {
		JSONObject jsonObject = bitsService.getBitsConfigure(authDTO);
		if (jsonObject != null && jsonObject.has("tripChart")) {
			JSONObject tripChart = jsonObject.getJSONObject("tripChart");
			return tripChart.getInt("gpsShareTripChart");
		}
		return 0;
	}

}
