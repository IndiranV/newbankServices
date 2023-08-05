package org.in.com.controller.api_v3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.in.com.aggregator.redbus.RedbusVendorKeyEM;
import org.in.com.constants.Text;
import org.in.com.controller.api.io.BaseIO;
import org.in.com.controller.api_v3.io.BusIO;
import org.in.com.controller.api_v3.io.BusSeatLayoutIO;
import org.in.com.controller.api_v3.io.BusSeatTypeIO;
import org.in.com.controller.api_v3.io.DynamicFareIO;
import org.in.com.controller.api_v3.io.ResponseIO;
import org.in.com.controller.api_v3.io.ScheduleIO;
import org.in.com.controller.api_v3.io.SeatGendarStatusIO;
import org.in.com.controller.api_v3.io.SeatStatusIO;
import org.in.com.controller.api_v3.io.StageFareIO;
import org.in.com.controller.api_v3.io.StageIO;
import org.in.com.controller.api_v3.io.StationIO;
import org.in.com.controller.api_v3.io.TicketDetailsIO;
import org.in.com.controller.api_v3.io.TicketIO;
import org.in.com.controller.api_v3.io.TripIO;
import org.in.com.controller.api_v3.io.TripStatusIO;
import org.in.com.controller.api_v3.io.TripV2IO;
import org.in.com.controller.web.BaseController;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.BusService;
import org.in.com.service.BusmapService;
import org.in.com.service.DynamicPricingService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleVisibilityService;
import org.in.com.service.SearchService;
import org.in.com.service.StationService;
import org.in.com.utils.BitsEnDecrypt;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/api/3.0/json/{operatorCode}/{username}/{apiToken}/dp")
public class ApiDynamicPricingController extends BaseController {
	private static final Logger DYNAMIC_LOGGER = LoggerFactory.getLogger("org.in.com.aggregator.dynamic.pricing");

	public static Map<String, Integer> ConcurrentRequests = new ConcurrentHashMap<String, Integer>();

	@Autowired
	AuthService authService;
	@Autowired
	ScheduleDynamicStageFareService scheduleDynamicStageFareService;
	@Autowired
	ScheduleVisibilityService visibilityService;
	@Autowired
	SearchService searchService;
	@Autowired
	DynamicPricingService dynamicPricingService;
	@Autowired
	BusService busService;
	@Autowired
	StationService stationService;
	@Autowired
	BusmapService busmapService;
	@Autowired
	ScheduleService scheduleService;

	@RequestMapping(value = "/report/schedule/{scheduleCode}/transaction", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, ?>>> getTransactionTicketDetails(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("scheduleCode") String scheduleCode, @RequestParam(required = true) String fromDate, @RequestParam(required = true) String toDate) throws Exception {
		DYNAMIC_LOGGER.info("History - Schedule {} fromDate {} toDate {}", scheduleCode, fromDate, toDate);
		validateMandatory(operatorCode, username, apiToken);
		if (!DateUtil.isValidDate(fromDate) || !DateUtil.isValidDate(toDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}

		if (fromDate != null && toDate != null && DateUtil.getDayDifferent(DateUtil.getDateTime(fromDate), DateUtil.getDateTime(toDate)) > 31) {
			throw new ServiceException(ErrorCode.INVALID_DATE_RANGE);
		}

		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);

		/** Check Schedule Visibility Permission */
		boolean isScheduleAccessable = Text.FALSE;

		List<ScheduleDTO> accessableSchedules = visibilityService.getUserActiveSchedule(authDTO);
		for (ScheduleDTO accessableSchedule : accessableSchedules) {
			if (scheduleCode.equals(accessableSchedule.getCode())) {
				isScheduleAccessable = Text.TRUE;
				break;
			}
		}

		if (!isScheduleAccessable) {
			throw new ServiceException(ErrorCode.INVALID_CODE, "Schedule invalid or unauthorized access");
		}

		ScheduleDTO schedule = new ScheduleDTO();
		schedule.setCode(scheduleCode);
		schedule.setActiveFrom(fromDate);
		schedule.setActiveTo(toDate);

		List<Map<String, ?>> result = scheduleDynamicStageFareService.getScheduleDynamicFareDetails(authDTO, schedule);
		DYNAMIC_LOGGER.info("History Successfully retrived..{0} {1} {2}", scheduleCode, fromDate, toDate);
		return ResponseIO.success(result);
	}

	// Sciative integrated this API
	@RequestMapping(value = "/schedule/trip/{tripDate}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripV2IO>> getScheduleActiveTrips(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("tripDate") String tripDate, int days, String filterType, String[] scheduleCode) throws Exception {
		DYNAMIC_LOGGER.info("active Trip {} Schedule {} filterType {}", tripDate, scheduleCode, filterType);
		validateMandatory(operatorCode, username, apiToken);
		List<TripV2IO> tripList = new ArrayList<>();
		if (!DateUtil.isValidDate(tripDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		DateTime tripTravelDate = new DateTime(tripDate);
		if (DateUtil.getDayDifferent(DateUtil.NOW(), tripTravelDate) < 0) {
			throw new ServiceException(ErrorCode.TRIP_DATE_OVER);
		}
		if (!StringUtil.isContains(",ROUTE,STAGE,", filterType)) {
			throw new ServiceException(ErrorCode.INVALID_CODE, "Filter type must be ROUTE/STAGE");

		}
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		List<DateTime> tripDateList = DateUtil.getDateListV2(tripTravelDate, days);

		List<TripDTO> finalList = dynamicPricingService.getDateWiseDPTripList(authDTO, tripDateList, filterType, scheduleCode);

		for (TripDTO tripDTO : finalList) {
			TripV2IO tripIO = new TripV2IO();
			ScheduleIO schedule = new ScheduleIO();
			schedule.setCode(tripDTO.getSchedule().getCode());
			schedule.setName(tripDTO.getSchedule().getName());
			schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
			tripIO.setSchedule(schedule);
			tripIO.setTripDate(tripDTO.getTripDate().format("YYYY-MM-DD"));

			List<StageIO> stageList = new ArrayList<>();

			// Stage
			for (StageDTO stageDTO : tripDTO.getStageList()) {
				if (stageDTO.getActiveFlag() == -1) {
					continue;
				}
				// Bus
				BusIO busIO = new BusIO();
				busIO.setBusType(BitsUtil.getBusCategoryUsingEM(tripDTO.getBus().getCategoryCode()));
				busIO.setTotalSeatCount(tripDTO.getBus().getReservableLayoutSeatCount());
				busIO.setCode(tripDTO.getBus().getCode());

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
				stageIO.setFromStation(fromStation);
				stageIO.setToStation(toStation);

				// ---------------------------
				Map<String, Integer> availableMap = new HashMap<String, Integer>();
				Map<String, List<BusSeatLayoutDTO>> seatFareMap = new HashMap<String, List<BusSeatLayoutDTO>>();
				if (stageDTO.getBus() != null) {
					List<BusSeatLayoutIO> seatLayoutList = new ArrayList<>();
					for (BusSeatLayoutDTO layoutDTO : stageDTO.getBus().getBusSeatLayoutDTO().getList()) {
						BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
						layoutIO.setCode(layoutDTO.getCode());
						layoutIO.setSeatName(layoutDTO.getName());
						layoutIO.setColPos(layoutDTO.getColPos());
						layoutIO.setRowPos(layoutDTO.getRowPos());
						layoutIO.setSeatPos(1);
						layoutIO.setLayer(layoutDTO.getLayer());
						layoutIO.setOrientation(layoutDTO.getOrientation());
						SeatGendarStatusIO seatGendarIO = new SeatGendarStatusIO();
						if (layoutDTO.getSeatGendar() != null) {
							seatGendarIO.setCode(layoutDTO.getSeatGendar().getCode());
							seatGendarIO.setName(layoutDTO.getSeatGendar().getName());
						}
						else if (layoutDTO.getSeatStatus() != null) {
							seatGendarIO.setCode(layoutDTO.getSeatStatus().getCode());
							seatGendarIO.setName(layoutDTO.getSeatStatus().getDescription());
						}
						layoutIO.setSeatGendar(seatGendarIO);

						// Seat Status and Preference
						SeatStatusIO seatStatusIO = new SeatStatusIO();
						if (layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.AVAILABLE_ALL.getCode()) && layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getCode().equals(SeatGendarEM.FEMALE.getCode())) {
							seatStatusIO.setCode(SeatStatusEM.AVAILABLE_FEMALE.getCode());
							seatStatusIO.setName(SeatStatusEM.AVAILABLE_FEMALE.getDescription());
						}
						else if (layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.AVAILABLE_ALL.getCode()) && layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getCode().equals(SeatGendarEM.MALE.getCode())) {
							seatStatusIO.setCode(SeatStatusEM.AVAILABLE_MALE.getCode());
							seatStatusIO.setName(SeatStatusEM.AVAILABLE_MALE.getDescription());
						}
						else {
							seatStatusIO.setCode(layoutDTO.getSeatStatus().getCode());
							seatStatusIO.setName(layoutDTO.getSeatStatus().getDescription());
						}
						layoutIO.setSeatStatus(seatStatusIO);

						// Stage Fare and Schedule Seat Fare included
						layoutIO.setSeatFare(layoutDTO.getFare().setScale(0, RoundingMode.HALF_UP));
						layoutIO.setServiceTax(tripDTO.getSchedule().getTax().getServiceTax().compareTo(BigDecimal.ZERO) == 1 ? layoutIO.getSeatFare().divide(new BigDecimal(100), 2, RoundingMode.CEILING).multiply(tripDTO.getSchedule().getTax().getServiceTax()) : BigDecimal.ZERO);

						// Set seat status Blocked id Fare amount is Zero
						if (layoutIO.getSeatFare().doubleValue() == 0) {
							seatStatusIO.setCode(SeatStatusEM.BLOCKED.getCode());
							seatStatusIO.setName(SeatStatusEM.BLOCKED.getDescription());
							layoutIO.setSeatStatus(seatStatusIO);
							layoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
						}

						if (layoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_YOU || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_ALL || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_MALE || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_FEMALE) {
							availableMap.put(layoutDTO.getBusSeatType().getCode(), availableMap.get(layoutDTO.getBusSeatType().getCode()) == null ? 0 : availableMap.get(layoutDTO.getBusSeatType().getCode()) + 1);
						}
						BusSeatTypeIO seatTypeIO = new BusSeatTypeIO();
						seatTypeIO.setCode(layoutDTO.getBusSeatType().getCode());
						seatTypeIO.setName(layoutDTO.getBusSeatType().getName());
						layoutIO.setBusSeatType(seatTypeIO);
						seatLayoutList.add(layoutIO);

					}
					busIO.setSeatLayoutList(seatLayoutList);
					stageIO.setBus(busIO);
				}
				stageList.add(stageIO);
			}
			tripIO.setStageList(stageList);

			tripIO.setTripCode(tripDTO.getCode());
			// Trip Status
			TripStatusIO tripStatusIO = new TripStatusIO();
			if (tripDTO.getTripStatus() != null) {
				tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
				tripStatusIO.setName(tripDTO.getTripStatus().getName());
			}
			tripIO.setTripStatus(tripStatusIO);

			tripList.add(tripIO);
		}

		DYNAMIC_LOGGER.info("schedule Active Tripsretrived..{} Trip Count:{} ", tripDate, tripList.size());
		return ResponseIO.success(tripList);

	}

	@RequestMapping(value = "/schedule/trip/ticket", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TicketIO>> getTripTicketDetails(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @RequestBody List<String> triplist, String syncTime) throws Exception {
		validateMandatory(operatorCode, username, apiToken);
		Map<String, TicketIO> ticketMap = new HashMap<>();
		String syncDateTime = BitsEnDecrypt.getBase64URLDecoder(syncTime);
		DYNAMIC_LOGGER.info("trip Stage Fare Change - {} -{}", triplist, syncDateTime);

		if (!DateUtil.isValidDateTime(syncDateTime)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		List<TripDTO> tripListDate = dynamicPricingService.getBookedBlockedTickets(authDTO, triplist, DateUtil.getDateTime(syncDateTime));

		for (TripDTO tripDTO : tripListDate) {
			for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {
				if (ticketMap.get(ticketDetailsDTO.getTicketCode()) == null) {
					TicketIO ticket = new TicketIO();
					ticket.setCode(ticketDetailsDTO.getTicketCode());
					ticket.setTripCode(tripDTO.getCode());
					StationIO fromStation = new StationIO();
					fromStation.setCode(ticketDetailsDTO.getFromStation().getCode());
					StationIO toStation = new StationIO();
					toStation.setCode(ticketDetailsDTO.getToStation().getCode());
					ticket.setFromStation(fromStation);
					ticket.setToStation(toStation);
					ticket.setTicketAt(ticketDetailsDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));

					List<TicketDetailsIO> ticketDetails = new ArrayList<>();
					TicketDetailsIO seatDetails = new TicketDetailsIO();
					seatDetails.setSeatCode(ticketDetailsDTO.getSeatCode());
					seatDetails.setSeatName(ticketDetailsDTO.getSeatName());
					seatDetails.setSeatFare(ticketDetailsDTO.getSeatFare().setScale(2, RoundingMode.HALF_UP));
					seatDetails.setServiceTax(ticketDetailsDTO.getAcBusTax().setScale(2, RoundingMode.HALF_UP));
					SeatStatusIO seatStatus = new SeatStatusIO();
					seatStatus.setCode(ticketDetailsDTO.getTicketStatus().getCode());
					seatDetails.setSeatStatus(seatStatus);
					ticketDetails.add(seatDetails);
					ticket.setTicketDetails(ticketDetails);
					ticketMap.put(ticketDetailsDTO.getTicketCode(), ticket);
				}
				else if (ticketMap.get(ticketDetailsDTO.getTicketCode()) != null) {
					TicketIO ticket = ticketMap.get(ticketDetailsDTO.getTicketCode());
					TicketDetailsIO seatDetails = new TicketDetailsIO();
					seatDetails.setSeatCode(ticketDetailsDTO.getSeatCode());
					seatDetails.setSeatName(ticketDetailsDTO.getSeatName());
					seatDetails.setSeatFare(ticketDetailsDTO.getSeatFare().setScale(2, RoundingMode.HALF_UP));
					seatDetails.setServiceTax(ticketDetailsDTO.getAcBusTax().setScale(2, RoundingMode.HALF_UP));
					SeatStatusIO seatStatus = new SeatStatusIO();
					seatStatus.setCode(ticketDetailsDTO.getTicketStatus().getCode());
					seatDetails.setSeatStatus(seatStatus);

					ticket.getTicketDetails().add(seatDetails);
					ticketMap.put(ticketDetailsDTO.getTicketCode(), ticket);
				}
			}
		}

		return ResponseIO.success(new ArrayList<TicketIO>(ticketMap.values()));

	}

	@RequestMapping(value = "/schedule/trip/{tripCode}/stage/fare/change", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateTripStageFarePush(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("tripCode") String tripCode, @RequestBody JSONObject jsonObject) throws Exception {
		String requestProcessCode = TokenGenerator.generateCode("DPFUC");
		try {
			DYNAMIC_LOGGER.info("{} trip Stage Fare Change- {}  Req Start - {}", requestProcessCode, tripCode, jsonObject.toString());
			validateMandatory(operatorCode, username, apiToken);

			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);
			authDTO.getAdditionalAttribute().put("requestProcessCode", requestProcessCode);

			if (!jsonObject.getString("tripCode").equals(tripDTO.getCode())) {
				throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
			}
			tripDTO.setTripDate(DateUtil.getDateTime(jsonObject.getString("tripDate")));

			List<StageDTO> stageList = new ArrayList<StageDTO>();
			for (Object stageObj : jsonObject.getJSONArray("stage")) {
				JSONObject stageFare = (JSONObject) stageObj;
				StationDTO fromStation = new StationDTO();
				fromStation.setCode(stageFare.getJSONObject("fromStation").getString("code"));
				StationDTO toStation = new StationDTO();
				toStation.setCode(stageFare.getJSONObject("toStation").getString("code"));
				List<BusSeatLayoutDTO> seatLayoutList = new ArrayList<BusSeatLayoutDTO>();
				for (Object seatFareObj : stageFare.getJSONArray("seatFare")) {
					JSONObject seatFare = (JSONObject) seatFareObj;
					BusSeatLayoutDTO busSeatLayout = new BusSeatLayoutDTO();
					busSeatLayout.setFare(new BigDecimal(seatFare.getInt("fare")));
					busSeatLayout.setCode(seatFare.getString("seatCode"));
					busSeatLayout.setName(seatFare.getString("seatName"));
					seatLayoutList.add(busSeatLayout);
				}
				BusDTO bus = new BusDTO();
				BusSeatLayoutDTO busSeatLayout = new BusSeatLayoutDTO();
				busSeatLayout.setList(seatLayoutList);
				bus.setBusSeatLayoutDTO(busSeatLayout);
				StageDTO stage = new StageDTO();

				StageStationDTO fromStageStation = new StageStationDTO();
				fromStageStation.setStation(fromStation);
				stage.setFromStation(fromStageStation);

				StageStationDTO toStageStation = new StageStationDTO();
				toStageStation.setStation(toStation);
				stage.setToStation(toStageStation);

				stage.setBus(bus);
				stageList.add(stage);
			}

			scheduleDynamicStageFareService.notifyUpdateTripStageFareChange(authDTO, tripDTO, stageList);
		}
		catch (ServiceException e) {
			DYNAMIC_LOGGER.error("{} trip Stage Fare Change - {} - Req End with Error- {}", requestProcessCode, tripCode, e.getErrorCode().toString());
			throw e;
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.error("{} trip Stage Fare Change - {} - Req End with Error- {}", requestProcessCode, tripCode, e.getMessage());
			throw e;
		}
		finally {
			DYNAMIC_LOGGER.info("{} trip Stage Fare Change - {} - Req End", requestProcessCode, tripCode);
		}
		return ResponseIO.success(new BaseIO());
	}

	@RequestMapping(value = "/trip/{fromCode}/{toCode}/{tripDate}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripIO>> getSearchResult(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("fromCode") String fromCode, @PathVariable("toCode") String toCode, @PathVariable("tripDate") String tripDate) throws Exception {
		checkConcurrentRequests(operatorCode, username, apiToken);

		List<TripIO> tripList = new ArrayList<>();
		try {
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			if (!DateUtil.isValidDate(tripDate)) {
				throw new ServiceException(ErrorCode.INVALID_DATE, "Requried format is yyyy-MM-dd");
			}
			DateTime tripTravelDate = new DateTime(tripDate);
			if (DateUtil.getDayDifferent(DateUtil.NOW(), tripTravelDate) < 0) {
				throw new ServiceException(ErrorCode.TRIP_DATE_OVER);
			}
			if (DateUtil.getDayDifferent(DateUtil.NOW(), tripTravelDate) > authDTO.getNamespace().getProfile().getAdvanceBookingDays()) {
				throw new ServiceException(ErrorCode.MAX_ADVANCE_BOOKING_DAYS, authDTO.getNamespace().getProfile().getAdvanceBookingDays() + " days");
			}
			if (fromCode.length() <= 6 || toCode.length() <= 6) {
				throw new ServiceException(ErrorCode.INVALID_STATION);
			}

			Map<String, List<String>> mapList = stationService.getCommerceRoutes(authDTO);
			if (mapList.get(fromCode) == null || !mapList.get(fromCode).toString().contains(toCode)) {
				throw new ServiceException(ErrorCode.ROUTE_NOT_FOUND, authDTO.getNamespace().getProfile().getAdvanceBookingDays() + " days");
			}

			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(tripTravelDate);
			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setCode(fromCode);
			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setCode(toCode);
			searchDTO.setFromStation(fromStationDTO);
			searchDTO.setToStation(toStationDTO);

			List<TripDTO> list = searchService.getSearch(authDTO, searchDTO);
			for (TripDTO tripDTO : list) {
				TripIO tripIO = new TripIO();
				ScheduleIO schedule = new ScheduleIO();
				if (tripDTO.getSchedule() == null) {
					System.out.println("Request for getSearchResult with apiToken " + operatorCode + " - " + username + " - " + " Route: " + fromCode + " - " + toCode + "-" + tripDate + "-" + ConcurrentRequests.get(apiToken));
				}
				schedule.setCode(tripDTO.getSchedule().getCode());
				schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
				tripIO.setSchedule(schedule);
				if (tripDTO.getTripStatus().getCode().equals(TripStatusEM.TRIP_YET_OPEN.getCode())) {
					continue;
				}
				// Trip Status
				TripStatusIO tripStatusIO = new TripStatusIO();
				tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
				tripStatusIO.setName(tripDTO.getTripStatus().getName());
				tripIO.setTripStatus(tripStatusIO);

				int Hours = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) / 60;
				int Minutes = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) % 60;
				tripIO.setTravelTime(Hours + " : " + Minutes);
				tripIO.setCloseTime(tripDTO.getTripCloseTime().format("YYYY-MM-DD hh:mm:ss"));
				// Bus
				BusIO busIO = new BusIO();
				busIO.setCode(tripDTO.getBus().getCode());
				busIO.setBusType(busService.getBusCategoryByCode(tripDTO.getBus().getCategoryCode()));
				busIO.setTotalSeatCount(tripDTO.getBus().getBusSeatLayoutDTO().getList().size());
				Map<String, Integer> availableMap = new HashMap<String, Integer>();
				Map<String, List<BusSeatLayoutDTO>> seatFareMap = new HashMap<String, List<BusSeatLayoutDTO>>();
				for (BusSeatLayoutDTO layoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
					BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
					layoutIO.setCode(layoutDTO.getCode());
					layoutIO.setSeatName(layoutDTO.getName());
					layoutIO.setColPos(layoutDTO.getColPos());
					layoutIO.setRowPos(layoutDTO.getRowPos());
					layoutIO.setLayer(layoutDTO.getLayer());
					BusSeatTypeIO seatStatus = new BusSeatTypeIO();
					seatStatus.setCode(layoutDTO.getBusSeatType().getCode());
					seatStatus.setName(layoutDTO.getBusSeatType().getName());
					layoutIO.setBusSeatType(seatStatus);
					if (layoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_YOU || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_ALL || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_MALE || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_FEMALE) {
						availableMap.put(layoutDTO.getBusSeatType().getCode(), availableMap.get(layoutDTO.getBusSeatType().getCode()) == null ? 1 : availableMap.get(layoutDTO.getBusSeatType().getCode()) + 1);

						// Schedule Seat Fare
						if (layoutDTO.getFare() != null) {
							if (seatFareMap.get(layoutDTO.getBusSeatType().getCode()) == null) {
								List<BusSeatLayoutDTO> seatFareList = new ArrayList<BusSeatLayoutDTO>();
								seatFareList.add(layoutDTO);
								seatFareMap.put(layoutDTO.getBusSeatType().getCode(), seatFareList);
							}
							else if (seatFareMap.get(layoutDTO.getBusSeatType().getCode()) != null) {
								List<BusSeatLayoutDTO> seatFareList = seatFareMap.get(layoutDTO.getBusSeatType().getCode());
								seatFareList.add(layoutDTO);
								seatFareMap.put(layoutDTO.getBusSeatType().getCode(), seatFareList);
							}
						}
					}
				}

				tripIO.setBus(busIO);
				tripIO.setTripCode(tripDTO.getCode());
				tripIO.setTravelDate(searchDTO.getTravelDate().format("YYYY-MM-DD"));
				// Stage
				if (tripDTO.getStage() != null) {
					tripIO.setTripStageCode(tripDTO.getStage().getCode());
					StationIO fromStation = new StationIO();
					StationIO toStation = new StationIO();
					fromStation.setCode(tripDTO.getStage().getFromStation().getStation().getCode());
					fromStation.setName(tripDTO.getStage().getFromStation().getStation().getName());
					fromStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					toStation.setCode(tripDTO.getStage().getToStation().getStation().getCode());
					toStation.setName(tripDTO.getStage().getToStation().getStation().getName());
					toStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					List<StageFareIO> stageFareList = new ArrayList<>();
					for (StageFareDTO fareDTO : tripDTO.getStage().getStageFare()) {
						StageFareIO stageFareIO = new StageFareIO();
						stageFareIO.setFare(fareDTO.getFare());
						stageFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
						stageFareIO.setSeatName(fareDTO.getBusSeatType().getName());

						if (availableMap.get(fareDTO.getBusSeatType().getCode()) != null) {
							stageFareIO.setAvailableSeatCount(availableMap.get(fareDTO.getBusSeatType().getCode()));
						}
						// Schedule Seat Fare
						if (seatFareMap.get(fareDTO.getBusSeatType().getCode()) != null) {
							List<BusSeatLayoutDTO> seatFareList = seatFareMap.get(fareDTO.getBusSeatType().getCode());
							Map<BigDecimal, Integer> seatFareCount = new HashMap<BigDecimal, Integer>();
							for (BusSeatLayoutDTO layoutDTO : seatFareList) {
								if (seatFareCount.get(layoutDTO.getFare()) != null) {
									seatFareCount.put(layoutDTO.getFare(), seatFareCount.get(layoutDTO.getFare()) + 1);
								}
								else {
									seatFareCount.put(layoutDTO.getFare(), 1);
								}
							}
							List<BigDecimal> fareList = new ArrayList<BigDecimal>(seatFareCount.keySet());
							for (BigDecimal fare : fareList) {
								StageFareIO seatFareIO = new StageFareIO();
								seatFareIO.setFare(fare);
								// seatFareIO.setDiscountFare(fareDTO.getDiscountFare());
								seatFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
								seatFareIO.setSeatName(fareDTO.getBusSeatType().getName());
								seatFareIO.setAvailableSeatCount(seatFareCount.get(fare));
								stageFareIO.setAvailableSeatCount(stageFareIO.getAvailableSeatCount() - seatFareCount.get(fare));
								seatFareIO.setFare(seatFareIO.getFare().setScale(0, RoundingMode.HALF_UP));
								stageFareList.add(seatFareIO);
							}
						}
						stageFareIO.setFare(stageFareIO.getFare().setScale(0, RoundingMode.HALF_UP));
						stageFareList.add(stageFareIO);
					}
					tripIO.setStageFare(stageFareList);

					tripIO.setFromStation(fromStation);
					tripIO.setToStation(toStation);
				}
				tripList.add(tripIO);
			}
		}
		catch (ServiceException e) {
			DYNAMIC_LOGGER.error("V3 SearchResult:" + operatorCode + " - " + username + " - " + fromCode + "-" + toCode + "-" + tripDate + " #: " + tripList.size() + " e:" + e.getErrorCode().toString() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.error("V3 SearchResult:" + operatorCode + " - " + username + " - " + fromCode + "-" + toCode + "-" + tripDate + " #: " + tripList.size() + " e:" + e.getMessage() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		finally {
			releaseConcurrentRequests(apiToken);
		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/busmap/{tripCode}/{fromStationCode}/{toStationCode}/{travelDate}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TripIO> getBusmap(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("tripCode") String tripCode, @PathVariable("fromStationCode") String fromStationCode, @PathVariable("toStationCode") String toStationCode, @PathVariable("travelDate") String travelDate) throws Exception {
		checkConcurrentRequests(operatorCode, username, apiToken);
		TripIO tripIO = new TripIO();
		try {
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			TripDTO tripStageDTO = new TripDTO();
			tripStageDTO.setCode(tripCode);

			SearchDTO searchDTO = new SearchDTO();
			DateTime tripTravelDate = new DateTime(travelDate);
			searchDTO.setTravelDate(tripTravelDate);
			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setCode(fromStationCode);
			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setCode(toStationCode);
			searchDTO.setFromStation(fromStationDTO);
			searchDTO.setToStation(toStationDTO);
			tripStageDTO.setSearch(searchDTO);
			TripDTO tripDTO = busmapService.getSearchBusmapV3(authDTO, tripStageDTO);

			ScheduleIO schedule = new ScheduleIO();
			if (tripDTO == null || tripDTO.getSchedule() == null) {
				System.out.println("Error: 0098A1 " + operatorCode + " - " + username + " - " + "Stage Code: " + tripCode + "--" + ConcurrentRequests.get(apiToken));
			}
			schedule.setCode(tripDTO.getSchedule().getCode());
			schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
			tripIO.setSchedule(schedule);

			// Trip Status
			TripStatusIO tripStatusIO = new TripStatusIO();
			tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
			tripStatusIO.setName(tripDTO.getTripStatus().getName());
			tripIO.setTripStatus(tripStatusIO);

			int Hours = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) / 60;
			int Minutes = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) % 60;
			tripIO.setTravelTime(Hours + ":" + Minutes);
			// Bus
			BusIO busIO = new BusIO();
			busIO.setCode(tripDTO.getBus().getCode());
			busIO.setBusType(BitsUtil.getBusCategoryUsingEM(tripDTO.getBus().getCategoryCode()));
			busIO.setTotalSeatCount(tripDTO.getBus().getBusSeatLayoutDTO().getList().size());
			List<BusSeatLayoutIO> seatLayoutList = new ArrayList<>();
			Map<String, Integer> availableMap = new HashMap<String, Integer>();
			Map<String, StageFareDTO> fareMap = new HashMap<>();
			// Group Wise Fare and Default Fare
			for (StageFareDTO fareDTO : tripDTO.getStage().getStageFare()) {
				if (fareDTO.getGroup().getId() != 0) {
					fareMap.put(fareDTO.getGroup().getId() + fareDTO.getBusSeatType().getCode(), fareDTO);
				}
				else {
					fareMap.put(fareDTO.getBusSeatType().getCode(), fareDTO);
				}
			}
			for (BusSeatLayoutDTO layoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
				BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
				layoutIO.setCode(layoutDTO.getCode());
				layoutIO.setSeatName(layoutDTO.getName());
				layoutIO.setColPos(layoutDTO.getColPos());
				layoutIO.setRowPos(layoutDTO.getRowPos());
				layoutIO.setSeatPos(1);
				layoutIO.setLayer(layoutDTO.getLayer());
				layoutIO.setOrientation(layoutDTO.getOrientation());
				SeatGendarStatusIO seatGendarIO = new SeatGendarStatusIO();
				if (layoutDTO.getSeatGendar() != null) {
					seatGendarIO.setCode(layoutDTO.getSeatGendar().getCode());
					seatGendarIO.setName(layoutDTO.getSeatGendar().getName());
				}
				else if (layoutDTO.getSeatStatus() != null) {
					seatGendarIO.setCode(layoutDTO.getSeatStatus().getCode());
					seatGendarIO.setName(layoutDTO.getSeatStatus().getDescription());
				}
				layoutIO.setSeatGendar(seatGendarIO);

				// Seat Status and Preference
				SeatStatusIO seatStatusIO = new SeatStatusIO();
				if (layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.AVAILABLE_ALL.getCode()) && layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getCode().equals(SeatGendarEM.FEMALE.getCode())) {
					seatStatusIO.setCode(SeatStatusEM.AVAILABLE_FEMALE.getCode());
					seatStatusIO.setName(SeatStatusEM.AVAILABLE_FEMALE.getDescription());
				}
				else if (layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.AVAILABLE_ALL.getCode()) && layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getCode().equals(SeatGendarEM.MALE.getCode())) {
					seatStatusIO.setCode(SeatStatusEM.AVAILABLE_MALE.getCode());
					seatStatusIO.setName(SeatStatusEM.AVAILABLE_MALE.getDescription());
				}
				else if (layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.BLOCKED.getCode()) || layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.BOOKED.getCode()) || layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.PHONE_BLOCKED.getCode()) || layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.QUOTA_SEAT.getCode()) || layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.TEMP_BLOCKED.getCode()) || layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.UN_KNOWN.getCode()) || layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.ALLOCATED_OTHER.getCode())) {
					seatStatusIO.setCode(SeatStatusEM.BOOKED.getCode());
					seatStatusIO.setName(SeatStatusEM.BOOKED.getDescription());
				}
				else {
					seatStatusIO.setCode(layoutDTO.getSeatStatus().getCode());
					seatStatusIO.setName(layoutDTO.getSeatStatus().getDescription());
				}
				layoutIO.setSeatStatus(seatStatusIO);

				StageFareDTO stageFareDTO = fareMap.get(authDTO.getGroup().getId() + layoutDTO.getBusSeatType().getCode()) != null ? fareMap.get(authDTO.getGroup().getId() + layoutDTO.getBusSeatType().getCode()) : fareMap.get(layoutDTO.getBusSeatType().getCode()) != null ? fareMap.get(layoutDTO.getBusSeatType().getCode()) : null;
				if (stageFareDTO != null) {
					// Stage Fare and Schedule Seat Fare included
					layoutIO.setSeatFare(layoutDTO.getFare() != null ? layoutDTO.getFare() : stageFareDTO.getFare());
					layoutIO.setSeatFare(layoutIO.getSeatFare().setScale(0, RoundingMode.HALF_UP));
					layoutIO.setServiceTax(tripDTO.getSchedule().getTax().getServiceTax().compareTo(BigDecimal.ZERO) == 1 ? layoutIO.getSeatFare().divide(new BigDecimal(100), 2, RoundingMode.CEILING).multiply(tripDTO.getSchedule().getTax().getServiceTax()) : BigDecimal.ZERO);
					layoutIO.setDiscountFare(stageFareDTO.getDiscountFare() != null ? stageFareDTO.getDiscountFare() : BigDecimal.ZERO);
				}
				else {
					layoutIO.setSeatFare(BigDecimal.ZERO);
					layoutIO.setServiceTax(BigDecimal.ZERO);
					layoutIO.setDiscountFare(BigDecimal.ZERO);
				}
				// Set seat status Blocked id Fare amount is Zero
				if (layoutIO.getSeatFare().doubleValue() == 0) {
					seatStatusIO.setCode(SeatStatusEM.BLOCKED.getCode());
					seatStatusIO.setName(SeatStatusEM.BLOCKED.getDescription());
					layoutIO.setSeatStatus(seatStatusIO);
					layoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
				}

				if (layoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_YOU || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_ALL || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_MALE || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_FEMALE) {
					availableMap.put(layoutDTO.getBusSeatType().getCode(), availableMap.get(layoutDTO.getBusSeatType().getCode()) == null ? 0 : availableMap.get(layoutDTO.getBusSeatType().getCode()) + 1);
				}
				if (layoutDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_LOWER_SLEEPER.getId() || layoutDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_UPPER_SLEEPER.getId()) {
					layoutDTO.setBusSeatType(BusSeatTypeEM.SLEEPER);
				}
				BusSeatTypeIO seatTypeIO = new BusSeatTypeIO();
				seatTypeIO.setCode(layoutDTO.getBusSeatType().getCode());
				seatTypeIO.setName(layoutDTO.getBusSeatType().getName());
				layoutIO.setBusSeatType(seatTypeIO);

				seatLayoutList.add(layoutIO);
			}
			busIO.setSeatLayoutList(seatLayoutList);
			tripIO.setBus(busIO);
			tripIO.setTripCode(tripDTO.getCode());
			tripIO.setTravelDate(tripDTO.getSearch().getTravelDate().format("YYYY-MM-DD"));
			// Stage
			if (tripDTO.getStage() != null) {
				tripIO.setTripStageCode(tripDTO.getStage().getCode());
				StationIO fromStation = new StationIO();
				StationIO toStation = new StationIO();
				fromStation.setCode(tripDTO.getStage().getFromStation().getStation().getCode());
				fromStation.setName(tripDTO.getStage().getFromStation().getStation().getName());
				fromStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				toStation.setCode(tripDTO.getStage().getToStation().getStation().getCode());
				toStation.setName(tripDTO.getStage().getToStation().getStation().getName());
				toStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				List<StageFareIO> stageFareList = new ArrayList<>();
				for (StageFareDTO fareDTO : tripDTO.getStage().getStageFare()) {
					StageFareIO stageFareIO = new StageFareIO();
					stageFareIO.setFare(fareDTO.getFare());
					stageFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
					stageFareIO.setSeatName(fareDTO.getBusSeatType().getName());
					if (fareDTO.getGroup() != null) {
						stageFareIO.setGroupName(fareDTO.getGroup().getName());
					}
					if (availableMap.get(fareDTO.getBusSeatType().getCode()) != null) {
						stageFareIO.setAvailableSeatCount(availableMap.get(fareDTO.getBusSeatType().getCode()));
					}
					stageFareIO.setFare(stageFareIO.getFare().setScale(0, RoundingMode.HALF_UP));
					stageFareList.add(stageFareIO);
				}
				tripIO.setStageFare(stageFareList);

				tripIO.setFromStation(fromStation);
				tripIO.setToStation(toStation);

			}
		}
		catch (ServiceException e) {
			DYNAMIC_LOGGER.error("V3 Busmap:" + operatorCode + " - " + username + " - " + tripCode + "-" + fromStationCode + "-" + toStationCode + "-" + travelDate + e.getErrorCode().toString() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		catch (Exception e) {
			DYNAMIC_LOGGER.error("V3 Busmap:" + operatorCode + " - " + username + " - " + tripCode + "-" + fromStationCode + "-" + toStationCode + "-" + travelDate + e.getMessage() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		finally {
			releaseConcurrentRequests(apiToken);
		}
		return ResponseIO.success(tripIO);
	}

	@RequestMapping(value = "/integration/fare/change/notify", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> notifyFareChange(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @RequestBody DynamicFareIO dynamicFare) throws Exception {
		DYNAMIC_LOGGER.info("Notify - Schedule {} Trip Date {}", dynamicFare.getScheduleCode(), dynamicFare.getTripDate());
		validateMandatory(operatorCode, username, apiToken);
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);

		RedbusVendorKeyEM redbusVendorKeyEM = RedbusVendorKeyEM.getRedbusVendorKeyEM(operatorCode);
		if (redbusVendorKeyEM == null || !redbusVendorKeyEM.getVendorKey().equals(dynamicFare.getVendorKey())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}

		ScheduleDTO schedule = new ScheduleDTO();
		schedule.setCode(dynamicFare.getScheduleCode());
		schedule.setTripDate(DateUtil.getDateTime(dynamicFare.getTripDate()));

		scheduleDynamicStageFareService.notifyFareChangeQueue(authDTO, schedule);
		DYNAMIC_LOGGER.info("Notify fare change completed...");
		return ResponseIO.success(new BaseIO());
	}

	@RequestMapping(value = "/schedule/bus/{buscode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BusIO> getBuslayout(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("buscode") String buscode) throws Exception {
		DYNAMIC_LOGGER.info("bus layout - {}", buscode);
		validateMandatory(operatorCode, username, apiToken);
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		List<BusSeatLayoutIO> layoutIOList = new ArrayList<BusSeatLayoutIO>();
		BusDTO busDTO = new BusDTO();
		busDTO.setCode(buscode);
		busDTO = busService.getBus(authDTO, busDTO);
		BusIO bus = new BusIO();
		bus.setCode(busDTO.getCode());
		bus.setName(busDTO.getName());
		bus.setBusType(busService.getBusCategoryUsingEM(busDTO.getCategoryCode()));
		for (BusSeatLayoutDTO layoutDTO : busDTO.getBusSeatLayoutDTO().getList()) {
			BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
			layoutIO.setCode(layoutDTO.getCode());
			layoutIO.setSeatName(layoutDTO.getName());
			BusSeatTypeIO seatTypeIO = new BusSeatTypeIO();
			if (layoutDTO.getBusSeatType() != null) {
				seatTypeIO.setCode(layoutDTO.getBusSeatType().getCode());
				seatTypeIO.setName(layoutDTO.getBusSeatType().getName());
				layoutIO.setBusSeatType(seatTypeIO);
			}
			layoutIO.setColPos(layoutDTO.getColPos());
			layoutIO.setRowPos(layoutDTO.getRowPos());
			layoutIO.setLayer(layoutDTO.getLayer());
			layoutIO.setOrientation(layoutDTO.getOrientation());
			layoutIOList.add(layoutIO);
		}
		bus.setSeatLayoutList(layoutIOList);
		return ResponseIO.success(bus);

	}

	@RequestMapping(value = "/schedule/route/details/{scheduleCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, Object>> getScheduleDetails(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("scheduleCode") String scheduleCode) throws Exception {
		DYNAMIC_LOGGER.info("Schedule Route Details - {}", scheduleCode);
		Map<String, Object> data = new HashMap<>();
		validateMandatory(operatorCode, username, apiToken);
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		ScheduleDTO schedule = new ScheduleDTO();
		schedule.setCode(scheduleCode);
		schedule.setActiveFlag(1);
		schedule = scheduleService.getScheduleDetails(authDTO, schedule);
		JSONObject scheObj = new JSONObject();
		scheObj.put("code", schedule.getCode());
		scheObj.put("serviceNumber", schedule.getServiceNumber());
		scheObj.put("displayName", StringUtil.isNotNull(schedule.getApiDisplayName()) ? schedule.getApiDisplayName() : schedule.getName());
		data.put("schedule", scheObj);

		JSONArray stageList = new JSONArray();
		int sequence = 1;
		for (StageDTO stage : schedule.getStageList()) {
			JSONObject stageObj = new JSONObject();
			JSONObject fromObj = new JSONObject();
			JSONObject toObj = new JSONObject();
			fromObj.put("code", stage.getFromStation().getStation().getCode());
			fromObj.put("name", stage.getFromStation().getStation().getName());
			fromObj.put("time", DateUtil.addMinituesToDate(DateUtil.NOW().getStartOfDay(), stage.getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
			toObj.put("code", stage.getToStation().getStation().getCode());
			toObj.put("name", stage.getToStation().getStation().getName());
			toObj.put("time", DateUtil.addMinituesToDate(DateUtil.NOW().getStartOfDay(), stage.getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));

			stageObj.put("fromStation", fromObj);
			stageObj.put("toStation", toObj);
			stageObj.put("sequence", sequence);
			stageObj.put("isMainRoute", schedule.getAdditionalAttributes().get(stage.getFromStation().getStation().getId() + "_" + stage.getToStation().getStation().getId()) != null ? Text.TRUE : Text.FALSE);
			stageList.add(stageObj);
			sequence++;
		}
		data.put("route", stageList);

		JSONObject busObj = new JSONObject();
		busObj.put("code", schedule.getScheduleBus().getBus().getCode());
		busObj.put("busType", BitsUtil.getBusCategoryUsingEM(schedule.getScheduleBus().getBus().getCategoryCode()));
		busObj.put("seatCount", schedule.getScheduleBus().getBus().getSeatCount());
		data.put("bus", busObj);
		return ResponseIO.success(data);
	}

	private boolean validateMandatory(String operatorCode, String username, String apiToken) throws Exception {
		if (StringUtil.isNull(operatorCode)) {
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
		}
		if (StringUtil.isNull(username)) {
			throw new ServiceException(ErrorCode.USER_INVALID_USERNAME);
		}
		if (StringUtil.isNull(apiToken)) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		return true;
	}

	public static synchronized boolean checkConcurrentRequests(String operatorCode, String username, String apiToken) {
		if (ConcurrentRequests.get(apiToken) != null && ConcurrentRequests.get(apiToken) > 5) {
			DYNAMIC_LOGGER.error("Error reached Max Concurrent Request CC800:" + operatorCode + " - " + username + "-->" + ConcurrentRequests.get(apiToken));
			System.out.println(DateUtil.NOW() + " CCRT01 - " + operatorCode + " - " + username + " - reached Max Concurrent DP Request");
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

	public static synchronized boolean releaseConcurrentRequests(String apiToken) {
		if (ConcurrentRequests.get(apiToken) != null) {
			if (ConcurrentRequests.get(apiToken) > 0) {
				ConcurrentRequests.put(apiToken, ConcurrentRequests.get(apiToken) - 1);
			}
		}
		return true;
	}
}
