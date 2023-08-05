package org.in.com.controller.api_v3;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang.WordUtils;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.api_v3.io.BusIO;
import org.in.com.controller.api_v3.io.BusSeatLayoutIO;
import org.in.com.controller.api_v3.io.BusSeatTypeIO;
import org.in.com.controller.api_v3.io.CancellationPolicyIO;
import org.in.com.controller.api_v3.io.CancellationTermIO;
import org.in.com.controller.api_v3.io.CommissionIO;
import org.in.com.controller.api_v3.io.NamespaceTaxIO;
import org.in.com.controller.api_v3.io.OperatorIO;
import org.in.com.controller.api_v3.io.OrderDetailsIO;
import org.in.com.controller.api_v3.io.OrderIO;
import org.in.com.controller.api_v3.io.OrderTransferDetailsIO;
import org.in.com.controller.api_v3.io.ResponseIO;
import org.in.com.controller.api_v3.io.ScheduleIO;
import org.in.com.controller.api_v3.io.ScheduleTicketTransferTermsIO;
import org.in.com.controller.api_v3.io.SeatGendarStatusIO;
import org.in.com.controller.api_v3.io.SeatStatusIO;
import org.in.com.controller.api_v3.io.StageFareIO;
import org.in.com.controller.api_v3.io.StateIO;
import org.in.com.controller.api_v3.io.StationIO;
import org.in.com.controller.api_v3.io.StationPointIO;
import org.in.com.controller.api_v3.io.TicketDetailsIO;
import org.in.com.controller.api_v3.io.TicketIO;
import org.in.com.controller.api_v3.io.TicketStatusIO;
import org.in.com.controller.api_v3.io.TransactionModeIO;
import org.in.com.controller.api_v3.io.TransactionTypeIO;
import org.in.com.controller.api_v3.io.TripActivitiesIO;
import org.in.com.controller.api_v3.io.TripIO;
import org.in.com.controller.api_v3.io.TripStatusIO;
import org.in.com.controller.api_v3.io.UserTransactionIO;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.AmenitiesIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.CancellationPolicyDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.CommissionDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.StationPointAmenitiesEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.dto.enumeration.TripActivitiesEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.BlockSeatsService;
import org.in.com.service.BusService;
import org.in.com.service.BusmapService;
import org.in.com.service.CancelTicketService;
import org.in.com.service.CancellationTermsService;
import org.in.com.service.CommissionService;
import org.in.com.service.ConfirmSeatsService;
import org.in.com.service.InventoryService;
import org.in.com.service.ScheduleFareOverrideService;
import org.in.com.service.SearchService;
import org.in.com.service.StationService;
import org.in.com.service.TicketEditService;
import org.in.com.service.TicketFailureService;
import org.in.com.service.TicketService;
import org.in.com.service.TicketTransferService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
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
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/api/3.0/json/{operatorCode}/{username}/{apiToken}")
public class ApiV3Controller extends BaseController {
	public static Map<String, Integer> ConcurrentRequests = new ConcurrentHashMap<String, Integer>();
	public static int concurrentRequestsLimit = 10;

	@Autowired
	AuthService authService;
	@Autowired
	SearchService searchService;
	@Autowired
	InventoryService inventoryService;
	@Autowired
	BusmapService busmapService;
	@Autowired
	BlockSeatsService blockSeatsService;
	@Autowired
	ConfirmSeatsService confirmSeatsService;
	@Autowired
	CancelTicketService cancelTicketService;
	@Autowired
	TripService tripService;
	@Autowired
	CancellationTermsService termsService;
	@Autowired
	StationService stationService;
	@Autowired
	BusService busService;
	@Autowired
	ScheduleFareOverrideService fareOverrideService;
	@Autowired
	CommissionService commissionService;
	@Autowired
	TicketTransferService ticketTransferService;
	@Autowired
	TicketService ticketService;
	@Autowired
	UserService userService;
	@Autowired
	TicketFailureService ticketFailureService;
	@Autowired
	TicketEditService ticketEditService;

	private static final Logger loggerapi = LoggerFactory.getLogger("org.in.com.controller.api_v2");
	private static final Logger apiTransactionlogger = LoggerFactory.getLogger("org.in.com.controller.api_v2_trans");

	@RequestMapping(value = "/operator", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<OperatorIO> getOperator(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken) throws Exception {
		OperatorIO operator = new OperatorIO();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			operator.setCode(authDTO.getNamespace().getCode() + authDTO.getAliasNamespaceCode());
			operator.setName(authDTO.getNamespace().getName());
		}
		catch (ServiceException e) {
			throw e;
		}
		return ResponseIO.success(operator);
	}

	@RequestMapping(value = "/operator/commission", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<CommissionIO>> getUserCommission(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken) throws Exception {
		List<CommissionIO> commission = new ArrayList<CommissionIO>();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			UserDTO userDTO = new UserDTO();
			userDTO.setUsername(username);
			userDTO = getUserDTO(authDTO, userDTO);
			List<CommissionDTO> list = commissionService.getCommerceCommission(authDTO, userDTO);
			for (CommissionDTO commissionDTO : list) {
				CommissionIO commissionio = new CommissionIO();
				commissionio.setValue(commissionDTO.getCommissionValue());
				commissionio.setCommissionType(commissionDTO.getCommissionType().getCode());
				commission.add(commissionio);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		return ResponseIO.success(commission);
	}

	@RequestMapping(value = "/station", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationIO>> getStations(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken) throws Exception {
		checkConcurrentRequests(operatorCode, username, apiToken, "Station");
		List<StationDTO> list = null;
		List<StationIO> IOlist = new ArrayList<StationIO>();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			list = stationService.getCommerceStation(authDTO);
			for (StationDTO stationDTO : list) {
				StationIO stationIO = new StationIO();
				stationIO.setCode(stationDTO.getCode());
				stationIO.setName(stationDTO.getName());
				StateIO state = new StateIO();
				state.setCode(stationDTO.getState().getCode());
				state.setName(stationDTO.getState().getName());
				stationIO.setState(state);
				IOlist.add(stationIO);
			}
		}
		catch (ServiceException e) {
			loggerapi.error("V3 getStations: " + operatorCode + " - " + username + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		finally {
			releaseConcurrentRequests(apiToken);
		}
		return ResponseIO.success(IOlist);
	}

	@RequestMapping(value = "/route", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, List<String>>> getRoutes(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken) throws Exception {
		checkConcurrentRequests(operatorCode, username, apiToken, "Route");
		Map<String, List<String>> MapList = null;
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			MapList = stationService.getCommerceRoutes(authDTO);
		}
		catch (ServiceException e) {
			loggerapi.error("V3 getRoutes: " + operatorCode + " - " + username + " - " + ConcurrentRequests.get(apiToken));
			throw e;
		}
		finally {
			releaseConcurrentRequests(apiToken);
		}
		return ResponseIO.success(MapList);
	}

	@RequestMapping(value = "/search/{fromCode}/{toCode}/{tripDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripIO>> getSearchResult(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("fromCode") String fromCode, @PathVariable("toCode") String toCode, @PathVariable("tripDate") String tripDate) throws Exception {
		String data = "search" + Text.HYPHEN + fromCode + Text.HYPHEN + toCode + Text.HYPHEN + tripDate;
		checkConcurrentRequests(operatorCode, username, apiToken, data);

		List<TripIO> tripList = new ArrayList<>();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
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
			if (StringUtil.isNull(fromCode) || StringUtil.isNull(toCode) || fromCode.length() <= 7 || toCode.length() <= 7 || fromCode.equals(toCode)) {
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
				// schedule.setServiceTax(tripDTO.getSchedule().getAcBusTax());
				if (tripDTO.getSchedule().getTax().getId() != 0) {
					NamespaceTaxIO tax = new NamespaceTaxIO();
					tax.setGstin(tripDTO.getSchedule().getTax().getGstin());
					tax.setCgstValue(tripDTO.getSchedule().getTax().getCgstValue());
					tax.setSgstValue(tripDTO.getSchedule().getTax().getSgstValue());
					tax.setUgstValue(tripDTO.getSchedule().getTax().getUgstValue());
					tax.setIgstValue(tripDTO.getSchedule().getTax().getIgstValue());
					tax.setTradeName(tripDTO.getSchedule().getTax().getTradeName());
					schedule.setTax(tax);
				}
				tripIO.setSchedule(schedule);
				tripIO.setDisplayName(tripDTO.getSchedule().getApiDisplayName());
				if (tripDTO.getTripStatus().getCode().equals(TripStatusEM.TRIP_YET_OPEN.getCode())) {
					continue;
				}
				// Trip Status
				TripStatusIO tripStatusIO = new TripStatusIO();
				tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
				tripStatusIO.setName(tripDTO.getTripStatus().getName());
				tripIO.setTripStatus(tripStatusIO);

				List<AmenitiesIO> amenitiesList = new ArrayList<AmenitiesIO>();
				for (AmenitiesDTO amenitiesDTO : tripDTO.getAmenities()) {
					AmenitiesIO amenities = new AmenitiesIO();
					amenities.setCode(amenitiesDTO.getCode());
					amenities.setName(amenitiesDTO.getName());
					amenitiesList.add(amenities);
				}
				tripIO.setAmenities(amenitiesList);

				tripIO.setAdditionalAttributes(tripDTO.getAdditionalAttributes());

				List<TripActivitiesIO> activities = new ArrayList<TripActivitiesIO>();
				for (TripActivitiesEM activitiesEM : tripDTO.getActivities()) {
					TripActivitiesIO activitiesIO = new TripActivitiesIO();
					activitiesIO.setCode(activitiesEM.getCode());
					activitiesIO.setName(activitiesEM.getName());
					activities.add(activitiesIO);
				}
				tripIO.setActivities(activities);

				OperatorIO operatorIO = new OperatorIO();
				operatorIO.setCode(StringUtil.isNull(authDTO.getAliasNamespaceCode(), authDTO.getNamespace().getCode()));
				operatorIO.setName(authDTO.getNamespace().getName());
				tripIO.setOperator(operatorIO);

				if (tripDTO.getSchedule().getTicketTransferTerms() != null) {
					ScheduleTicketTransferTermsIO ticketTransferTerms = new ScheduleTicketTransferTermsIO();
					ScheduleTicketTransferTermsDTO ticketTransferTermsDTO = tripDTO.getSchedule().getTicketTransferTerms();
					ticketTransferTerms.setChargeAmount(ticketTransferTermsDTO.getChargeAmount());
					ticketTransferTerms.setAllowedTill(ticketTransferTermsDTO.getDateTime().format(Text.DATE_TIME_DATE4J));
					ticketTransferTerms.setTransferable(Numeric.ONE_INT);
					ticketTransferTerms.setChargeType(ticketTransferTermsDTO.getChargeType().getCode());
					tripIO.setTicketTransferTerms(ticketTransferTerms);
				}
				int Hours = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) / 60;
				int Minutes = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) % 60;
				tripIO.setTravelTime(Hours + " : " + Minutes);
				tripIO.setCloseTime(tripDTO.getTripCloseTime().format("YYYY-MM-DD hh:mm:ss"));
				// Bus
				BusIO busIO = new BusIO();
				busIO.setName(tripDTO.getBus().getName());
				busIO.setCode(tripDTO.getBus().getCode());
				busIO.setCategoryCode(tripDTO.getBus().getCategoryCode());
				busIO.setBusType(busService.getBusCategoryByCode(tripDTO.getBus().getCategoryCode()));
				busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
				busIO.setTotalSeatCount(tripDTO.getBus().getBusSeatLayoutDTO().getList().size());
				Map<String, Integer> availableMap = new HashMap<String, Integer>();
				Map<String, List<BusSeatLayoutDTO>> seatFareMap = new HashMap<String, List<BusSeatLayoutDTO>>();
				for (BusSeatLayoutDTO layoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
					if (layoutDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_LOWER_SLEEPER.getId() || layoutDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_UPPER_SLEEPER.getId()) {
						layoutDTO.setBusSeatType(BusSeatTypeEM.SLEEPER);
					}
					else if (layoutDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_SEMI_SLEEPER.getId()) {
						layoutDTO.setBusSeatType(BusSeatTypeEM.SEMI_SLEEPER);
					}
					else if (layoutDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_SEATER.getId()) {
						layoutDTO.setBusSeatType(BusSeatTypeEM.SEATER);
					}

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

					Map<Integer, BigDecimal> busTypeFareMap = new HashMap<>();
					for (StageFareDTO fareDTO : tripDTO.getStage().getStageFare()) {
						if (fareDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_LOWER_SLEEPER.getId() || fareDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_UPPER_SLEEPER.getId()) {
							fareDTO.setBusSeatType(BusSeatTypeEM.SLEEPER);
						}
						else if (fareDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_SEMI_SLEEPER.getId()) {
							fareDTO.setBusSeatType(BusSeatTypeEM.SEMI_SLEEPER);
						}
						else if (fareDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_SEATER.getId()) {
							fareDTO.setBusSeatType(BusSeatTypeEM.SEATER);
						}
						if (busTypeFareMap.containsKey(fareDTO.getBusSeatType().getId()) && fareDTO.getFare().compareTo(busTypeFareMap.get(fareDTO.getBusSeatType().getId())) < 0) {
							continue;
						}
						busTypeFareMap.put(fareDTO.getBusSeatType().getId(), fareDTO.getFare());
					}

					List<StageFareIO> stageFareList = new ArrayList<>();
					List<Integer> busSeatTypes = new ArrayList<>();
					for (StageFareDTO fareDTO : tripDTO.getStage().getStageFare()) {
						if (busSeatTypes.contains(fareDTO.getBusSeatType().getId()) || (busTypeFareMap.containsKey(fareDTO.getBusSeatType().getId()) && fareDTO.getFare().compareTo(busTypeFareMap.get(fareDTO.getBusSeatType().getId())) < 0)) {
							continue;
						}

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
						busSeatTypes.add(fareDTO.getBusSeatType().getId());
					}
					tripIO.setStageFare(stageFareList);
					List<StationPointIO> fromStationPoint = new ArrayList<>();
					for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
						StationPointIO pointIO = new StationPointIO();
						if (pointDTO.getCreditDebitFlag().equals("Cr")) {
							pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						}
						else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
							pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						}
						pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
						pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
						pointIO.setCode(pointDTO.getCode());
						pointIO.setName(pointDTO.getName());
						pointIO.setLandmark(pointDTO.getLandmark());
						pointIO.setAddress(pointDTO.getAddress());
						pointIO.setNumber(pointDTO.getNumber());
						pointIO.setAdditionalFare(pointDTO.getFare());

						List<BaseIO> pointAmenitiesList = new ArrayList<>();
						for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(pointDTO.getAmenities())) {
							BaseIO amenitiesIO = new BaseIO();
							amenitiesIO.setCode(amenitiesEM.getCode());
							amenitiesIO.setName(amenitiesEM.getName());
							pointAmenitiesList.add(amenitiesIO);
						}
						pointIO.setAmenities(pointAmenitiesList);
						fromStationPoint.add(pointIO);
					}
					List<StationPointIO> toStationPoint = new ArrayList<>();
					for (StationPointDTO pointDTO : tripDTO.getStage().getToStation().getStationPoint()) {
						StationPointIO pointIO = new StationPointIO();
						if (pointDTO.getCreditDebitFlag().equals("Cr")) {
							pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						}
						else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
							pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getToStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						}
						pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
						pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
						pointIO.setCode(pointDTO.getCode());
						pointIO.setName(pointDTO.getName());
						pointIO.setLandmark(pointDTO.getLandmark());
						pointIO.setAddress(pointDTO.getAddress());
						pointIO.setNumber(pointDTO.getNumber());
						pointIO.setAdditionalFare(pointDTO.getFare());

						List<BaseIO> pointAmenitiesList = new ArrayList<>();
						for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(pointDTO.getAmenities())) {
							BaseIO amenitiesIO = new BaseIO();
							amenitiesIO.setCode(amenitiesEM.getCode());
							amenitiesIO.setName(amenitiesEM.getName());
							pointAmenitiesList.add(amenitiesIO);
						}
						pointIO.setAmenities(pointAmenitiesList);
						toStationPoint.add(pointIO);
					}
					fromStation.setStationPoint(fromStationPoint);
					toStation.setStationPoint(toStationPoint);

					tripIO.setFromStation(fromStation);
					tripIO.setToStation(toStation);

					// via Station Sorting
					Comparator<ScheduleStationDTO> comp = new BeanComparator("stationSequence");
					Collections.sort(tripDTO.getStationList(), comp);
					List<StationIO> viaStation = new ArrayList<StationIO>();
					for (ScheduleStationDTO stationDTO : tripDTO.getStationList()) {
						if (stationDTO.getActiveFlag() == 1) {
							StationIO station = new StationIO();
							station.setCode(stationDTO.getStation().getCode());
							station.setName(stationDTO.getStation().getName());
							station.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stationDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
							viaStation.add(station);
						}
					}

					tripIO.setViaStations(viaStation);

					// Copy Cancellation Terms
					CancellationTermIO cancellationTermIO = new CancellationTermIO();
					cancellationTermIO.setCode(tripDTO.getCancellationTerm().getCode());
					cancellationTermIO.setDatetime(tripDTO.getAdditionalAttributes().get(Constants.CANCELLATION_DATETIME));
					// cancellationTermIO.setInstantCancellationMinutes(BitsUtil.getInstantCancellationMinutes(authDTO));
					List<CancellationPolicyIO> policyIOs = new ArrayList<CancellationPolicyIO>();
					for (CancellationPolicyDTO policyDTO : tripDTO.getCancellationTerm().getPolicyList()) {
						CancellationPolicyIO policyIO = new CancellationPolicyIO();
						policyIO.setCode(StringUtil.getRightPart(tripDTO.getCancellationTerm().getCode(), Numeric.THREE_INT) + policyDTO.getFromValue() + policyDTO.getToValue() + Math.abs(policyDTO.getDeductionValue().intValue()) + policyDTO.getPercentageFlag() + policyDTO.getPolicyPattern());
						policyIO.setFromValue(policyDTO.getFromValue());
						policyIO.setToValue(policyDTO.getToValue());
						policyIO.setDeductionAmount(policyDTO.getDeductionValue());
						policyIO.setPercentageFlag(policyDTO.getPercentageFlag());
						policyIO.setPolicyPattern(policyDTO.getPolicyPattern());
						policyIOs.add(policyIO);
					}
					cancellationTermIO.setPolicyList(policyIOs);
					tripIO.setCancellationTerm(cancellationTermIO);
				}
				tripList.add(tripIO);
			}
		}
		catch (ServiceException e) {
			loggerapi.error("V3 SearchResult:" + operatorCode + " - " + username + " - " + fromCode + "-" + toCode + "-" + tripDate + " #: " + tripList.size() + " e:" + e.getErrorCode().toString() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		catch (Exception e) {
			loggerapi.error("V3 SearchResult:" + operatorCode + " - " + username + " - " + fromCode + "-" + toCode + "-" + tripDate + " #: " + tripList.size() + " e:" + e.getMessage() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		finally {
			releaseConcurrentRequests(apiToken);
		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/busmap/{tripCode}/{fromStationCode}/{toStationCode}/{travelDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TripIO> getBusmap(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("tripCode") String tripCode, @PathVariable("fromStationCode") String fromStationCode, @PathVariable("toStationCode") String toStationCode, @PathVariable("travelDate") String travelDate) throws Exception {
		String data = "Busmap" + Text.HYPHEN + tripCode + Text.HYPHEN + travelDate + Text.HYPHEN + fromStationCode + Text.HYPHEN + toStationCode;
		checkConcurrentRequests(operatorCode, username, apiToken, data);
		TripIO tripIO = new TripIO();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			if (fromStationCode.length() <= 7 || toStationCode.length() <= 7) {
				throw new ServiceException(ErrorCode.INVALID_STATION);
			}
			if (tripCode.length() <= 10) {
				throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
			}
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
			// schedule.setServiceTax(tripDTO.getSchedule().getAcBusTax());
			if (tripDTO.getSchedule().getTax().getId() != 0) {
				NamespaceTaxIO tax = new NamespaceTaxIO();
				tax.setGstin(tripDTO.getSchedule().getTax().getGstin());
				tax.setCgstValue(tripDTO.getSchedule().getTax().getCgstValue());
				tax.setSgstValue(tripDTO.getSchedule().getTax().getSgstValue());
				tax.setUgstValue(tripDTO.getSchedule().getTax().getUgstValue());
				tax.setIgstValue(tripDTO.getSchedule().getTax().getIgstValue());
				tax.setTradeName(tripDTO.getSchedule().getTax().getTradeName());
				schedule.setTax(tax);
			}
			tripIO.setSchedule(schedule);

			OperatorIO operatorIO = new OperatorIO();
			operatorIO.setCode(StringUtil.isNull(authDTO.getAliasNamespaceCode(), authDTO.getNamespace().getCode()));
			operatorIO.setName(authDTO.getNamespace().getName());
			tripIO.setOperator(operatorIO);

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
			busIO.setName(tripDTO.getBus().getName());
			busIO.setCode(tripDTO.getBus().getCode());
			busIO.setCategoryCode(tripDTO.getBus().getCategoryCode());
			busIO.setBusType(BitsUtil.getBusCategoryUsingEM(tripDTO.getBus().getCategoryCode()));
			busIO.setDisplayName(StringUtil.isNotNull(tripDTO.getBus().getDisplayName()) ? tripDTO.getBus().getDisplayName() : Text.EMPTY);
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
					if (layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.BLOCKED.getCode()) || layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.UN_KNOWN.getCode()) || layoutDTO.getSeatStatus().getCode().equals(SeatStatusEM.ALLOCATED_OTHER.getCode())) {
						seatGendarIO.setCode(SeatGendarEM.MALE.getCode());
						seatGendarIO.setName(SeatGendarEM.MALE.getName());
					}
					else {
						seatGendarIO.setCode(layoutDTO.getSeatStatus().getCode());
						seatGendarIO.setName(layoutDTO.getSeatStatus().getDescription());
					}
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
					if (layoutDTO.getDiscountFare().compareTo(BigDecimal.ZERO) > 0) {
						layoutIO.setDiscountFare(layoutDTO.getDiscountFare());
					}
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
				else if (layoutDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_SEMI_SLEEPER.getId()) {
					layoutDTO.setBusSeatType(BusSeatTypeEM.SEMI_SLEEPER);
				}
				else if (layoutDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_SEATER.getId()) {
					layoutDTO.setBusSeatType(BusSeatTypeEM.SEATER);
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

				List<StationPointIO> fromStationPoint = new ArrayList<>();
				for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
					StationPointIO pointIO = new StationPointIO();
					if (pointDTO.getCreditDebitFlag().equals("Cr")) {
						pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
						pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
					pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
					pointIO.setCode(pointDTO.getCode());
					pointIO.setName(pointDTO.getName());
					pointIO.setLandmark(pointDTO.getLandmark());
					pointIO.setAddress(pointDTO.getAddress());
					pointIO.setNumber(pointDTO.getNumber());
					pointIO.setAdditionalFare(pointDTO.getFare());

					List<BaseIO> pointAmenitiesList = new ArrayList<>();
					for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(pointDTO.getAmenities())) {
						BaseIO amenitiesIO = new BaseIO();
						amenitiesIO.setCode(amenitiesEM.getCode());
						amenitiesIO.setName(amenitiesEM.getName());
						pointAmenitiesList.add(amenitiesIO);
					}
					pointIO.setAmenities(pointAmenitiesList);
					fromStationPoint.add(pointIO);
				}
				List<StationPointIO> toStationPoint = new ArrayList<>();
				for (StationPointDTO pointDTO : tripDTO.getStage().getToStation().getStationPoint()) {
					StationPointIO pointIO = new StationPointIO();
					if (pointDTO.getCreditDebitFlag().equals("Cr")) {
						pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
						pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getToStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
					pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
					pointIO.setCode(pointDTO.getCode());
					pointIO.setName(pointDTO.getName());
					pointIO.setLandmark(pointDTO.getLandmark());
					pointIO.setAddress(pointDTO.getAddress());
					pointIO.setNumber(pointDTO.getNumber());
					pointIO.setAdditionalFare(pointDTO.getFare());

					List<BaseIO> pointAmenitiesList = new ArrayList<>();
					for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(pointDTO.getAmenities())) {
						BaseIO amenitiesIO = new BaseIO();
						amenitiesIO.setCode(amenitiesEM.getCode());
						amenitiesIO.setName(amenitiesEM.getName());
						pointAmenitiesList.add(amenitiesIO);
					}
					pointIO.setAmenities(pointAmenitiesList);
					toStationPoint.add(pointIO);
				}
				fromStation.setStationPoint(fromStationPoint);
				toStation.setStationPoint(toStationPoint);

				tripIO.setFromStation(fromStation);
				tripIO.setToStation(toStation);

				// Copy Cancellation Terms
				CancellationTermIO cancellationTermIO = new CancellationTermIO();
				CancellationTermDTO termDTO = termsService.getCancellationTermsByTripDTO(authDTO, authDTO.getUser(), tripDTO);
				List<CancellationPolicyIO> policyIOs = new ArrayList<CancellationPolicyIO>();
				cancellationTermIO.setCode(termDTO.getCode());
				cancellationTermIO.setDatetime(tripDTO.getAdditionalAttributes().get(Constants.CANCELLATION_DATETIME));
				// cancellationTermIO.setInstantCancellationMinutes(BitsUtil.getInstantCancellationMinutes(authDTO));
				for (CancellationPolicyDTO policyDTO : termDTO.getPolicyList()) {
					CancellationPolicyIO policyIO = new CancellationPolicyIO();
					policyIO.setCode(StringUtil.getRightPart(termDTO.getCode(), Numeric.THREE_INT) + policyDTO.getFromValue() + policyDTO.getToValue() + Math.abs(policyDTO.getDeductionValue().intValue()) + policyDTO.getPercentageFlag() + policyDTO.getPolicyPattern());
					policyIO.setFromValue(policyDTO.getFromValue());
					policyIO.setToValue(policyDTO.getToValue());
					policyIO.setDeductionAmount(policyDTO.getDeductionValue());
					policyIO.setPercentageFlag(policyDTO.getPercentageFlag());
					policyIO.setPolicyPattern(policyDTO.getPolicyPattern());
					policyIOs.add(policyIO);
				}
				cancellationTermIO.setPolicyList(policyIOs);
				tripIO.setCancellationTerm(cancellationTermIO);
			}
		}
		catch (ServiceException e) {
			loggerapi.error("V3 Busmap:" + operatorCode + " - " + username + " - " + tripCode + "-" + fromStationCode + "-" + toStationCode + "-" + travelDate + e.getErrorCode().toString() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		catch (Exception e) {
			loggerapi.error("V3 Busmap:" + operatorCode + " - " + username + " - " + tripCode + "-" + fromStationCode + "-" + toStationCode + "-" + travelDate + e.getMessage() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		finally {
			releaseConcurrentRequests(apiToken);
		}
		return ResponseIO.success(tripIO);
	}

	@RequestMapping(value = "/search/busmap/{fromCode}/{toCode}/{tripDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripIO>> getSearchBusmap(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("fromCode") String fromCode, @PathVariable("toCode") String toCode, @PathVariable("tripDate") String tripDate) throws Exception {
		String data = "searchbusmap" + Text.HYPHEN + fromCode + Text.HYPHEN + toCode + Text.HYPHEN + tripDate;
		checkConcurrentRequests(operatorCode, username, apiToken, data);

		List<TripIO> tripList = new ArrayList<>();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
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
				// schedule.setServiceTax(tripDTO.getSchedule().getAcBusTax());
				if (tripDTO.getSchedule().getTax().getId() != 0) {
					NamespaceTaxIO tax = new NamespaceTaxIO();
					tax.setGstin(tripDTO.getSchedule().getTax().getGstin());
					tax.setCgstValue(tripDTO.getSchedule().getTax().getCgstValue());
					tax.setSgstValue(tripDTO.getSchedule().getTax().getSgstValue());
					tax.setUgstValue(tripDTO.getSchedule().getTax().getUgstValue());
					tax.setIgstValue(tripDTO.getSchedule().getTax().getIgstValue());
					tax.setTradeName(tripDTO.getSchedule().getTax().getTradeName());
					schedule.setTax(tax);
				}
				tripIO.setSchedule(schedule);
				tripIO.setDisplayName(tripDTO.getSchedule().getApiDisplayName());
				if (tripDTO.getTripStatus().getCode().equals(TripStatusEM.TRIP_YET_OPEN.getCode())) {
					continue;
				}
				// Trip Status
				TripStatusIO tripStatusIO = new TripStatusIO();
				tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
				tripStatusIO.setName(tripDTO.getTripStatus().getName());
				tripIO.setTripStatus(tripStatusIO);

				List<AmenitiesIO> amenitiesList = new ArrayList<AmenitiesIO>();
				for (AmenitiesDTO amenitiesDTO : tripDTO.getAmenities()) {
					AmenitiesIO amenities = new AmenitiesIO();
					amenities.setCode(amenitiesDTO.getCode());
					amenities.setName(amenitiesDTO.getName());
					amenitiesList.add(amenities);
				}
				tripIO.setAmenities(amenitiesList);

				tripIO.setAdditionalAttributes(tripDTO.getAdditionalAttributes());

				List<TripActivitiesIO> activities = new ArrayList<TripActivitiesIO>();
				for (TripActivitiesEM activitiesEM : tripDTO.getActivities()) {
					TripActivitiesIO activitiesIO = new TripActivitiesIO();
					activitiesIO.setCode(activitiesEM.getCode());
					activitiesIO.setName(activitiesEM.getName());
					activities.add(activitiesIO);
				}
				tripIO.setActivities(activities);

				OperatorIO operatorIO = new OperatorIO();
				operatorIO.setCode(StringUtil.isNull(authDTO.getAliasNamespaceCode(), authDTO.getNamespace().getCode()));
				operatorIO.setName(authDTO.getNamespace().getName());
				tripIO.setOperator(operatorIO);

				if (tripDTO.getSchedule().getTicketTransferTerms() != null) {
					ScheduleTicketTransferTermsIO ticketTransferTerms = new ScheduleTicketTransferTermsIO();
					ScheduleTicketTransferTermsDTO ticketTransferTermsDTO = tripDTO.getSchedule().getTicketTransferTerms();
					ticketTransferTerms.setChargeAmount(ticketTransferTermsDTO.getChargeAmount());
					DateTime originStationDateTime = null;
					if (authDTO.getNamespace().getProfile().getCancellationTimeType().equals(Constants.STAGE)) {
						originStationDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues());
					}
					else {
						originStationDateTime = BitsUtil.getOriginScheduleStationTime(tripDTO.getStationList(), tripDTO.getTripDate());
					}
					ticketTransferTerms.setAllowedTill(DateUtil.minusMinituesToDate(originStationDateTime, ticketTransferTermsDTO.getMinutes()).format(Text.DATE_TIME_DATE4J));
					ticketTransferTerms.setTransferable(Numeric.ONE_INT);
					ticketTransferTerms.setChargeType(ticketTransferTermsDTO.getChargeType().getCode());
					tripIO.setTicketTransferTerms(ticketTransferTerms);
				}
				int Hours = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) / 60;
				int Minutes = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) % 60;
				tripIO.setTravelTime(Hours + " : " + Minutes);
				tripIO.setCloseTime(tripDTO.getTripCloseTime().format("YYYY-MM-DD hh:mm:ss"));
				// Bus
				BusIO busIO = new BusIO();
				busIO.setName(tripDTO.getBus().getName());
				busIO.setCode(tripDTO.getBus().getCode());
				busIO.setCategoryCode(tripDTO.getBus().getCategoryCode());
				busIO.setBusType(busService.getBusCategoryByCode(tripDTO.getBus().getCategoryCode()));
				busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
				busIO.setTotalSeatCount(tripDTO.getBus().getBusSeatLayoutDTO().getList().size());
				Map<String, Integer> availableMap = new HashMap<String, Integer>();
				Map<String, List<BusSeatLayoutDTO>> seatFareMap = new HashMap<String, List<BusSeatLayoutDTO>>();

				//
				List<BusSeatLayoutIO> seatLayoutList = new ArrayList<>();
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
						if (layoutDTO.getDiscountFare().compareTo(BigDecimal.ZERO) > 0) {
							layoutIO.setDiscountFare(layoutDTO.getDiscountFare());
						}
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
					else if (layoutDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_SEMI_SLEEPER.getId()) {
						layoutDTO.setBusSeatType(BusSeatTypeEM.SEMI_SLEEPER);
					}
					else if (layoutDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_SEATER.getId()) {
						layoutDTO.setBusSeatType(BusSeatTypeEM.SEATER);
					}
					BusSeatTypeIO seatTypeIO = new BusSeatTypeIO();
					seatTypeIO.setCode(layoutDTO.getBusSeatType().getCode());
					seatTypeIO.setName(layoutDTO.getBusSeatType().getName());
					layoutIO.setBusSeatType(seatTypeIO);

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
					seatLayoutList.add(layoutIO);
				}
				busIO.setSeatLayoutList(seatLayoutList);

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

						// Map releated bus seat layout
						if (fareDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_LOWER_SLEEPER.getId() || fareDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_UPPER_SLEEPER.getId()) {
							fareDTO.setBusSeatType(BusSeatTypeEM.SLEEPER);
						}
						else if (fareDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_SEMI_SLEEPER.getId()) {
							fareDTO.setBusSeatType(BusSeatTypeEM.SEMI_SLEEPER);
						}
						else if (fareDTO.getBusSeatType().getId() == BusSeatTypeEM.SINGLE_SEATER.getId()) {
							fareDTO.setBusSeatType(BusSeatTypeEM.SEATER);
						}

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
					List<StationPointIO> fromStationPoint = new ArrayList<>();
					for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
						StationPointIO pointIO = new StationPointIO();
						if (pointDTO.getCreditDebitFlag().equals("Cr")) {
							pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						}
						else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
							pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						}
						pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
						pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
						pointIO.setCode(pointDTO.getCode());
						pointIO.setName(pointDTO.getName());
						pointIO.setLandmark(pointDTO.getLandmark());
						pointIO.setAddress(pointDTO.getAddress());
						pointIO.setNumber(pointDTO.getNumber());
						pointIO.setAdditionalFare(pointDTO.getFare());
						fromStationPoint.add(pointIO);
					}
					List<StationPointIO> toStationPoint = new ArrayList<>();
					for (StationPointDTO pointDTO : tripDTO.getStage().getToStation().getStationPoint()) {
						StationPointIO pointIO = new StationPointIO();
						if (pointDTO.getCreditDebitFlag().equals("Cr")) {
							pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						}
						else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
							pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getToStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						}
						pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
						pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
						pointIO.setCode(pointDTO.getCode());
						pointIO.setName(pointDTO.getName());
						pointIO.setLandmark(pointDTO.getLandmark());
						pointIO.setAddress(pointDTO.getAddress());
						pointIO.setNumber(pointDTO.getNumber());
						pointIO.setAdditionalFare(pointDTO.getFare());
						toStationPoint.add(pointIO);
					}
					fromStation.setStationPoint(fromStationPoint);
					toStation.setStationPoint(toStationPoint);

					tripIO.setFromStation(fromStation);
					tripIO.setToStation(toStation);
					// Copy Cancellation Terms
					CancellationTermIO cancellationTermIO = new CancellationTermIO();
					cancellationTermIO.setCode(tripDTO.getCancellationTerm().getCode());
					cancellationTermIO.setDatetime(tripDTO.getAdditionalAttributes().get(Constants.CANCELLATION_DATETIME));
					// cancellationTermIO.setInstantCancellationMinutes(BitsUtil.getInstantCancellationMinutes(authDTO));
					List<CancellationPolicyIO> policyIOs = new ArrayList<CancellationPolicyIO>();
					for (CancellationPolicyDTO policyDTO : tripDTO.getCancellationTerm().getPolicyList()) {
						CancellationPolicyIO policyIO = new CancellationPolicyIO();
						policyIO.setCode(StringUtil.getRightPart(tripDTO.getCancellationTerm().getCode(), Numeric.THREE_INT) + policyDTO.getFromValue() + policyDTO.getToValue() + Math.abs(policyDTO.getDeductionValue().intValue()) + policyDTO.getPercentageFlag() + policyDTO.getPolicyPattern());
						policyIO.setFromValue(policyDTO.getFromValue());
						policyIO.setToValue(policyDTO.getToValue());
						policyIO.setDeductionAmount(policyDTO.getDeductionValue());
						policyIO.setPercentageFlag(policyDTO.getPercentageFlag());
						policyIO.setPolicyPattern(policyDTO.getPolicyPattern());
						policyIOs.add(policyIO);
					}
					cancellationTermIO.setPolicyList(policyIOs);
					tripIO.setCancellationTerm(cancellationTermIO);
				}
				tripList.add(tripIO);
			}
		}
		catch (ServiceException e) {
			loggerapi.error("V3 SearchResult:" + operatorCode + " - " + username + " - " + fromCode + "-" + toCode + "-" + tripDate + " #: " + tripList.size() + " e:" + e.getErrorCode().toString() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		catch (Exception e) {
			loggerapi.error("V3 SearchResult:" + operatorCode + " - " + username + " - " + fromCode + "-" + toCode + "-" + tripDate + " #: " + tripList.size() + " e:" + e.getMessage() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		finally {
			releaseConcurrentRequests(apiToken);
		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/ticket/blocking", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_ATOM_XML_VALUE })
	@ResponseBody
	public ResponseIO<OrderIO> blockSeats(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @RequestBody OrderIO orderIO) throws Exception {
		String errorMsg = null;
		OrderIO ticketOrderIO = new OrderIO();
		ValidateMandatory(operatorCode, username, apiToken);
		DeviceMediumEM deviceMedium = BitsUtil.getDeviceMedium(orderIO.getAdditionalAttributes(), DeviceMediumEM.API_USER);
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken, deviceMedium);
		try {
			ValidateBlockingDetails(authDTO, orderIO);

			BookingDTO bookingDTO = new BookingDTO();
			{
				OrderDetailsIO orderDetails = orderIO.getOrderDetails();
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setPassengerMobile(orderIO.getMobileNumber());
				ticketDTO.setPassengerEmailId(orderIO.getEmailId());

				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(orderDetails.getTripCode());
				ticketDTO.setTripDTO(tripDTO);
				SearchDTO searchDTO = new SearchDTO();
				DateTime tripTravelDate = new DateTime(orderDetails.getTravelDate());
				searchDTO.setTravelDate(tripTravelDate);
				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setCode(orderDetails.getFromStation().getCode());
				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setCode(orderDetails.getToStation().getCode());
				searchDTO.setFromStation(fromStationDTO);
				searchDTO.setToStation(toStationDTO);
				tripDTO.setSearch(searchDTO);

				ticketDTO.setJourneyType(JourneyTypeEM.ONWARD_TRIP);
				ticketDTO.setRemarks(orderIO.getAgentTicketNumber());
				ticketDTO.setDeviceMedium(authDTO.getDeviceMedium());
				StationPointDTO boardingPointDTO = new StationPointDTO();
				StationPointDTO droppingPointDTO = new StationPointDTO();
				boardingPointDTO.setCode(orderDetails.getBoardingPoint().getCode());
				boardingPointDTO.setFare(orderDetails.getBoardingPoint().getAdditionalFare());
				droppingPointDTO.setCode(orderDetails.getDroppingPoint().getCode());
				droppingPointDTO.setFare(orderDetails.getDroppingPoint().getAdditionalFare());

				List<TicketDetailsDTO> passengerDetails = new ArrayList<TicketDetailsDTO>();
				for (TicketDetailsIO passDetails : orderDetails.getTicketDetails()) {
					TicketDetailsDTO tdDTO = new TicketDetailsDTO();
					tdDTO.setSeatCode(passDetails.getSeatCode().trim());
					tdDTO.setPassengerName(StringUtil.substring(StringUtil.removeSymbolWithSpace(passDetails.getPassengerName()), 60));
					tdDTO.setPassengerAge(passDetails.getPassengerAge());
					tdDTO.setSeatFare(passDetails.getSeatFare());
					tdDTO.setSeatName(passDetails.getSeatName());
					tdDTO.setTravelStatus(TravelStatusEM.YET_BOARD);
					// ticketDTO.setRemarks(ticketDTO.getRemarks() + "-" +
					// passDetails.getSeatFare());

					if (StringUtil.isNull(passDetails.getPassengerGendar()) || SeatGendarEM.getSeatGendarEM(passDetails.getPassengerGendar()) == null) {
						throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER, "Gendar code should be M or F");
					}
					tdDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(passDetails.getPassengerGendar()));
					passengerDetails.add(tdDTO);
				}

				ticketDTO.setTicketDetails(passengerDetails);
				ticketDTO.setBoardingPoint(boardingPointDTO);
				ticketDTO.setDroppingPoint(droppingPointDTO);
				ticketDTO.setTicketStatus(TicketStatusEM.TMP_BLOCKED_TICKET);

				bookingDTO.addTicketDTO(ticketDTO);
			}
			bookingDTO.setAggregate(new HashMap<String, String>());
			bookingDTO.setAdditionalAttributes(orderIO.getAdditionalAttributes() != null ? orderIO.getAdditionalAttributes() : new HashMap<String, String>());
			bookingDTO.setBookAfterTripTimeFlag(false);

			// Block Ticket Process
			blockSeatsService.blockSeatsV3(authDTO, bookingDTO);

			if (bookingDTO.isPaymentGatewayProcessFlag()) {
				throw new ServiceException(ErrorCode.LOW_AVAILABLE_BALANCE, ticketOrderIO);
			}
			ticketOrderIO.setCode(bookingDTO.getCode());
			ticketOrderIO.setBlockingLiveTime(bookingDTO.getBlockingLiveTime().format("YYYY-MM-DD hh:mm:ss"));
			ticketOrderIO.setCurrentBalance(authDTO.getCurrnetBalance());
			OrderDetailsIO orderDetails = new OrderDetailsIO();
			List<TicketDetailsIO> detailsList = new ArrayList<TicketDetailsIO>();
			TicketDTO ticketDTO = bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP);
			for (TicketDetailsDTO detailsDTO : ticketDTO.getTicketDetails()) {
				TicketDetailsIO details = new TicketDetailsIO();
				details.setSeatCode(detailsDTO.getSeatCode());
				details.setSeatFare(detailsDTO.getSeatFare());
				details.setPassengerAge(detailsDTO.getPassengerAge());
				details.setSeatName(detailsDTO.getSeatName());
				details.setServiceTax(detailsDTO.getAcBusTax());
				detailsList.add(details);
			}
			orderDetails.setTicketDetails(detailsList);
			ticketOrderIO.setOrderDetails(orderDetails);
		}
		catch (ServiceException e) {
			errorMsg = e.getErrorCode().toString();
			apiTransactionlogger.error("V3 BlockSeats: " + operatorCode + " - " + username + " - " + orderIO.toJSON() + " - " + ticketOrderIO.getCode() + " " + ticketOrderIO.getBlockingLiveTime() + " - " + errorMsg);
			ticketFailureService.saveFailureLog(authDTO, e.getErrorCode().getCode(), "BLOCK", e.getErrorCode().getMessage() + (StringUtil.isNotNull(e.getData()) ? ", " + e.getData().toString() : Text.EMPTY), orderIO.toJSON());
			throw e;
		}
		catch (Exception e) {
			errorMsg = e.getMessage();
			apiTransactionlogger.error("V3 BlockSeats: " + operatorCode + " - " + username + " - " + orderIO.toJSON() + " - " + ticketOrderIO.getCode() + " " + ticketOrderIO.getBlockingLiveTime() + " - " + errorMsg);
			ticketFailureService.saveFailureLog(authDTO, ErrorCode.UNABLE_TO_BLOCK_TICKET.getCode(), "BLOCK", errorMsg, orderIO.toJSON());
			throw new ServiceException(ErrorCode.UNABLE_TO_BLOCK_TICKET);
		}
		finally {
			apiTransactionlogger.info("V3 BlockSeats: " + operatorCode + " - " + username + " - " + orderIO.toJSON() + " - " + ticketOrderIO.getCode() + " " + ticketOrderIO.getBlockingLiveTime());
		}
		return ResponseIO.success(ticketOrderIO);
	}

	@RequestMapping(value = "/ticket/confirm/{ticketCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TicketIO> ConfirmSeat(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("ticketCode") String ticketCode, String referenceTicketNumber, String mobileNumber, String emailId, @RequestParam(required = false, defaultValue = "API") String deviceMedium) throws Exception {
		TicketIO ticketIO = new TicketIO();
		String errorMsg = null;
		ValidateMandatory(operatorCode, username, apiToken);
		DeviceMediumEM device = DeviceMediumEM.getDeviceMediumEM(deviceMedium);
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken, device);
		try {
			if (StringUtil.isNull(ticketCode)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE, ticketCode);
			}
			boolean isAllowMaskedMobileNumber = BitsUtil.isTagExists(authDTO.getUser().getUserTags(), Constants.MASK_MOBILE_NUMBER_TAG);
			if (!isAllowMaskedMobileNumber) {
				mobileNumber = Text.NA;
			}
			if (StringUtil.isNotNull(mobileNumber) && !StringUtil.isValidMobileNumber(mobileNumber)) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER, mobileNumber);
			}
			BookingDTO bookingDTO = confirmSeatsService.confirmBooking(authDTO, ticketCode, null, mobileNumber, emailId);
			TicketDTO ticketDTO = bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP);
			ticketIO.setCode(ticketDTO.getCode());
			StationIO FromStationIO = new StationIO();
			FromStationIO.setCode(ticketDTO.getFromStation().getCode());
			FromStationIO.setName(ticketDTO.getFromStation().getName());
			StationPointIO fromStationPointIO = new StationPointIO();
			fromStationPointIO.setCode(ticketDTO.getBoardingPoint().getCode());
			fromStationPointIO.setName(ticketDTO.getBoardingPoint().getName());
			fromStationPointIO.setDateTime(ticketDTO.getBoardingPointDateTime().format("YYYY-MM-DD hh:mm:ss"));
			// To Station Point
			StationIO toStationIO = new StationIO();
			toStationIO.setCode(ticketDTO.getToStation().getCode());
			toStationIO.setName(ticketDTO.getToStation().getName());
			StationPointIO toStationPointIO = new StationPointIO();
			toStationPointIO.setCode(ticketDTO.getDroppingPoint().getCode());
			toStationPointIO.setName(ticketDTO.getDroppingPoint().getName());
			toStationPointIO.setDateTime(ticketDTO.getDroppingPointDateTime().format("YYYY-MM-DD hh:mm:ss"));
			FromStationIO.setStationPoints(fromStationPointIO);
			toStationIO.setStationPoints(toStationPointIO);
			ticketIO.setFromStation(FromStationIO);
			ticketIO.setToStation(toStationIO);
			ticketIO.setTravelDate(ticketDTO.getTripDateTime().format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setReportingTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getReportingMinutes()).format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setRemarks(ticketDTO.getRemarks());
			ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());
			ticketIO.setTotalFare(ticketDTO.getTotalFare());
			ticketIO.setTransactionDate(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));

			OperatorIO operatorIO = new OperatorIO();
			operatorIO.setCode(StringUtil.isNull(authDTO.getAliasNamespaceCode(), authDTO.getNamespace().getCode()));
			operatorIO.setName(authDTO.getNamespace().getName());
			ticketIO.setOperator(operatorIO);

			// Ticket status
			TicketStatusIO ticketStatusIO = new TicketStatusIO();
			ticketStatusIO.setCode(ticketDTO.getTicketStatus().getCode());
			ticketStatusIO.setName(ticketDTO.getTicketStatus().getDescription());
			ticketIO.setTicketStatus(ticketStatusIO);

			List<TicketDetailsIO> detailsList = new ArrayList<TicketDetailsIO>();
			for (TicketDetailsDTO detailsDTO : ticketDTO.getTicketDetails()) {
				TicketDetailsIO details = new TicketDetailsIO();
				details.setSeatCode(detailsDTO.getSeatCode());
				details.setSeatFare(detailsDTO.getSeatFare());
				details.setSeatName(detailsDTO.getSeatName());
				details.setServiceTax(detailsDTO.getAcBusTax());
				SeatStatusIO seatStatus = new SeatStatusIO();
				seatStatus.setCode(ticketDTO.getTicketStatus().getCode());
				seatStatus.setName(ticketDTO.getTicketStatus().getDescription());
				details.setSeatStatus(seatStatus);
				detailsList.add(details);
			}
			ticketIO.setTicketDetails(detailsList);
			// transaction Payment Mode
			TransactionModeIO transactionModeIO = new TransactionModeIO();
			transactionModeIO.setCode(ticketDTO.getTransactionMode().getCode());
			transactionModeIO.setName(ticketDTO.getTransactionMode().getName());
			ticketIO.setTransactionMode(transactionModeIO);

			// Cancellation Policy
			List<CancellationPolicyIO> cancelPolicyList = new ArrayList<CancellationPolicyIO>();
			CancellationTermIO cancellationTermIO = new CancellationTermIO();
			cancellationTermIO.setCode(ticketDTO.getCancellationTerm().getCode());
			cancellationTermIO.setDatetime(ticketDTO.getTripDTO().getAdditionalAttributes().get(Constants.CANCELLATION_DATETIME));
			for (CancellationPolicyDTO cancellationPolicyDTO : ticketDTO.getCancellationTerm().getPolicyList()) {
				if (cancellationPolicyDTO.getPolicyId() == 0) {
					continue;
				}
				CancellationPolicyIO cancellPolicyIO = new CancellationPolicyIO();
				cancellPolicyIO.setCode(StringUtil.generateCancellationPolicyCode(ticketDTO.getCancellationTerm(), cancellationPolicyDTO));
				cancellPolicyIO.setFromValue(cancellationPolicyDTO.getFromValue());
				cancellPolicyIO.setToValue(cancellationPolicyDTO.getToValue());
				cancellPolicyIO.setDeductionAmount(cancellationPolicyDTO.getDeductionValue());
				cancellPolicyIO.setPercentageFlag(cancellationPolicyDTO.getPercentageFlag());
				cancellPolicyIO.setPolicyPattern(cancellationPolicyDTO.getPolicyPattern());
				cancelPolicyList.add(cancellPolicyIO);
			}
			cancellationTermIO.setPolicyList(cancelPolicyList);
			ticketIO.setCancellationTerms(cancellationTermIO);

			// copy User Transaction Details
			List<UserTransactionIO> userTransactionIOList = new ArrayList<>();
			UserTransactionIO userTransactionIO = new UserTransactionIO();
			TransactionTypeIO transactionTypeIO = new TransactionTypeIO();
			if (ticketDTO.getUserTransaction() != null) {
				transactionTypeIO.setName(ticketDTO.getUserTransaction().getTransactionType().getName());
				transactionTypeIO.setCreditDebitFlag(ticketDTO.getUserTransaction().getTransactionType().getCreditDebitFlag());
				transactionTypeIO.setCode(ticketDTO.getUserTransaction().getTransactionType().getCode());
				userTransactionIO.setCreditAmount(ticketDTO.getUserTransaction().getCreditAmount());
				userTransactionIO.setDebitAmount(ticketDTO.getUserTransaction().getDebitAmount());
				userTransactionIO.setTdsTax(ticketDTO.getUserTransaction().getTdsTax());
				userTransactionIO.setTransactionAmount(ticketDTO.getUserTransaction().getTransactionAmount());
				userTransactionIOList.add(userTransactionIO);
			}
			ticketIO.setUsertransaction(userTransactionIOList);
		}
		catch (ServiceException e) {
			errorMsg = e.getErrorCode().toString();
			apiTransactionlogger.error("V3 Confirm Seat:" + operatorCode + " - " + username + " - " + ticketCode + e.getErrorCode().toString());
			ticketFailureService.saveFailureLog(authDTO, e.getErrorCode().getCode(), "CONFM", e.getErrorCode().getMessage() + (StringUtil.isNotNull(e.getData()) ? ", " + e.getData().toString() : Text.EMPTY), "Ticket Code: " + ticketCode + ", Ref. Ticket No: " + referenceTicketNumber);
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			errorMsg = e.getMessage();
			apiTransactionlogger.error("V3 ConfirmSeat:" + operatorCode + " - " + username + " - " + ticketCode + errorMsg);
			ticketFailureService.saveFailureLog(authDTO, ErrorCode.UNABLE_TO_CONFIRM_TICKET.getCode(), "CONFM", errorMsg, "Ticket Code: " + ticketCode + ", Ref. Ticket No: " + referenceTicketNumber);
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
		}
		finally {
			apiTransactionlogger.info("ConfirmSeat: " + operatorCode + " - " + username + " - " + ticketIO.getPassegerMobleNo() + " - " + ticketCode + " - " + referenceTicketNumber);
		}
		return ResponseIO.success(ticketIO);
	}

	private boolean ValidateBlockingDetails(AuthDTO authDTO, OrderIO orderIO) throws Exception {

		if (orderIO == null || orderIO.getOrderDetails() == null) {
			throw new ServiceException(ErrorCode.INVALID_ORDER_DETAILS);
		}
		OrderDetailsIO orderDetails = orderIO.getOrderDetails();

		if (StringUtil.isNull(orderDetails.getTripCode())) {
			throw new ServiceException(ErrorCode.TRIP_CODE);
		}
		if (StringUtil.isNull(orderDetails.getTravelDate())) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		if (orderDetails.getFromStation() == null || orderDetails.getToStation() == null || StringUtil.isNull(orderDetails.getFromStation().getCode()) || StringUtil.isNull(orderDetails.getToStation().getCode())) {
			throw new ServiceException(ErrorCode.INVALID_STATION);
		}
		if (StringUtil.isNull(orderIO.getEmailId())) {
			throw new ServiceException(ErrorCode.INVALID_EMAIL_ID);
		}
		orderIO.setEmailId(orderIO.getEmailId().trim());
		if (!StringUtil.isValidEmailId(orderIO.getEmailId())) {
			throw new ServiceException(ErrorCode.INVALID_EMAIL_ID);
		}

		// Allow Mask mobile number only approved OTA
		boolean isAllowMaskedMobileNumber = BitsUtil.isTagExists(authDTO.getUser().getUserTags(), Constants.MASK_MOBILE_NUMBER_TAG);
		boolean isValidMobileNumber = StringUtil.isValidMobileNumber(orderIO.getMobileNumber());
		if (isAllowMaskedMobileNumber && !StringUtil.isValidMaskedMobileNumber(orderIO.getMobileNumber()) && !isValidMobileNumber) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER, "Invalid masked mobile number");
		}
		else if (!isAllowMaskedMobileNumber && !isValidMobileNumber) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}

		if (orderDetails.getBoardingPoint() == null || StringUtil.isNull(orderDetails.getBoardingPoint().getCode())) {
			throw new ServiceException(ErrorCode.STATION_POINT);
		}
		if (orderDetails.getDroppingPoint() == null || StringUtil.isNull(orderDetails.getDroppingPoint().getCode())) {
			throw new ServiceException(ErrorCode.STATION_POINT);
		}
		if (StringUtil.isNull(orderIO.getAgentTicketNumber())) {
			throw new ServiceException(ErrorCode.API_TICKET_CODE);
		}
		if (orderDetails.getTicketDetails() == null || orderDetails.getTicketDetails().isEmpty()) {
			throw new ServiceException(ErrorCode.SEAT_NOT_AVAILABLE);
		}
		if (orderDetails.getTicketDetails().size() > authDTO.getNamespace().getProfile().getMaxSeatPerTransaction()) {
			throw new ServiceException(ErrorCode.MAX_SEAT_PER_TRANSACTION);
		}
		List<String> uniqueList = new ArrayList<String>();
		for (TicketDetailsIO seatInfo : orderDetails.getTicketDetails()) {

			if (StringUtil.isNull(seatInfo.getSeatCode())) {
				throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
			}
			if (StringUtil.isNull(seatInfo.getSeatName())) {
				throw new ServiceException(ErrorCode.INVALID_SEAT_NAME);
			}
			if (StringUtil.isNull(seatInfo.getPassengerName())) {
				throw new ServiceException(ErrorCode.INVALID_PASSENGER_NAME);
			}
			if (seatInfo.getPassengerAge() <= 0 || seatInfo.getPassengerAge() > 100) {
				throw new ServiceException(ErrorCode.INVALID_PASSENGER_AGE);
			}
			if (seatInfo.getSeatFare().compareTo(BigDecimal.TEN) == -1) {
				throw new ServiceException(ErrorCode.SELECTED_STEAT_FARE_ZERO);
			}
			if (StringUtil.isNull(seatInfo.getPassengerGendar())) {
				throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
			}
			if (uniqueList.contains(seatInfo.getSeatCode().trim())) {
				throw new ServiceException(ErrorCode.DUPLICATE_SEAT_CODE, seatInfo.getSeatCode());
			}
			uniqueList.add(seatInfo.getSeatCode().trim());
			seatInfo.setPassengerName(StringUtil.substring(StringUtil.removeUnknownSymbol(seatInfo.getPassengerName()), 59));
			seatInfo.setPassengerName(WordUtils.capitalize(seatInfo.getPassengerName()));
		}
		return true;
	}

	public static synchronized boolean checkConcurrentRequests(String operatorCode, String username, String apiToken, String data) {
		if (ConcurrentRequests.get(apiToken) != null && ConcurrentRequests.get(apiToken) > concurrentRequestsLimit) {
			loggerapi.error("Error reached Max Concurrent Request CC800:" + operatorCode + " - " + username + "-->" + ConcurrentRequests.get(apiToken));
			System.out.println(DateUtil.NOW() + " CCRT01 - " + operatorCode + " - " + username + " - reached Max Concurrent Request - " + data);
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

	@RequestMapping(value = "/ticket/cancel/{ticketCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TicketIO> isCancelTicket(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("ticketCode") String ticketCode, String referenceTicketNumber) throws Exception {
		TicketIO ticketIO = new TicketIO();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			if (StringUtil.isNull(ticketCode)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE, ticketCode);
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO.setOverideFlag(false);
			ticketDTO = cancelTicketService.TicketIsCancel(authDTO, ticketDTO);

			StationIO fromStationIO = new StationIO();
			StationIO toStationIO = new StationIO();

			fromStationIO.setCode(ticketDTO.getFromStation().getCode());
			fromStationIO.setName(ticketDTO.getFromStation().getName());

			toStationIO.setCode(ticketDTO.getToStation().getCode());
			toStationIO.setName(ticketDTO.getToStation().getName());
			ticketIO.setFromStation(fromStationIO);
			ticketIO.setToStation(toStationIO);

			// Mapping passenger details
			List<TicketDetailsIO> ticketDetailsIO = new ArrayList<TicketDetailsIO>();
			if (ticketDTO.getTicketDetails() != null) {
				for (TicketDetailsDTO dto : ticketDTO.getTicketDetails()) {
					TicketDetailsIO tickDetailsIO = new TicketDetailsIO();
					tickDetailsIO.setSeatName(dto.getSeatName());
					tickDetailsIO.setSeatCode(dto.getSeatCode());
					tickDetailsIO.setSeatType(dto.getSeatType());
					SeatStatusIO ticketStatus = new SeatStatusIO();
					ticketStatus.setCode(dto.getTicketStatus().getCode());
					ticketStatus.setName(dto.getTicketStatus().getDescription());
					tickDetailsIO.setSeatStatus(ticketStatus);
					tickDetailsIO.setPassengerName(dto.getPassengerName());
					tickDetailsIO.setPassengerAge(dto.getPassengerAge());
					tickDetailsIO.setPassengerGendar(dto.getSeatGendar().getCode());
					tickDetailsIO.setSeatFare(dto.getSeatFare());
					tickDetailsIO.setRefundAmount(dto.getRefundAmount().setScale(2, RoundingMode.HALF_UP));
					tickDetailsIO.setServiceTax(dto.getAcBusTax());
					tickDetailsIO.setCancellationCharges(dto.getCancellationCharges().setScale(2, RoundingMode.HALF_UP));
					ticketDetailsIO.add(tickDetailsIO);
				}
			}
			List<CancellationPolicyIO> cancelPolicyList = new ArrayList<CancellationPolicyIO>();
			CancellationTermIO cancellationTermIO = new CancellationTermIO();
			cancellationTermIO.setCode(ticketDTO.getCancellationTerm().getCode());
			cancellationTermIO.setDatetime(ticketDTO.getTripDTO().getAdditionalAttributes().get(Constants.CANCELLATION_DATETIME));
			// cancellationTermIO.setInstantCancellationTill(StringUtil.isNotNull(ticketDTO.getInstantCancellationTill())
			// ?
			// ticketDTO.getInstantCancellationTill().format(Text.DATE_TIME_DATE4J)
			// : null);
			for (CancellationPolicyDTO cancellationPolicyDTO : ticketDTO.getCancellationTerm().getPolicyList()) {
				CancellationPolicyIO cancellPolicyIO = new CancellationPolicyIO();
				if (cancellationPolicyDTO.getPolicyId() == 0) {
					continue;
				}
				cancellPolicyIO.setCode(StringUtil.generateCancellationPolicyCode(ticketDTO.getCancellationTerm(), cancellationPolicyDTO));
				cancellPolicyIO.setFromValue(cancellationPolicyDTO.getFromValue());
				cancellPolicyIO.setToValue(cancellationPolicyDTO.getToValue());
				cancellPolicyIO.setDeductionAmount(cancellationPolicyDTO.getDeductionValue());
				cancellPolicyIO.setPercentageFlag(cancellationPolicyDTO.getPercentageFlag());
				cancellPolicyIO.setPolicyPattern(cancellationPolicyDTO.getPolicyPattern());
				cancelPolicyList.add(cancellPolicyIO);
			}
			cancellationTermIO.setPolicyList(cancelPolicyList);

			ticketIO.setTravelDate(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes()).format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setTravelTime(ticketDTO.getTripTime());
			ticketIO.setTripCode(ticketDTO.getTripDTO().getCode());
			ticketIO.setTripStageCode(ticketDTO.getTripDTO().getStage().getCode());
			ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());

			TicketStatusIO ticketStatus = new TicketStatusIO();
			ticketStatus.setCode(ticketDTO.getTicketStatus().getCode());
			ticketStatus.setName(ticketDTO.getTicketStatus().getDescription());
			ticketIO.setTicketStatus(ticketStatus);

			OperatorIO operatorIO = new OperatorIO();
			operatorIO.setCode(StringUtil.isNull(authDTO.getAliasNamespaceCode(), authDTO.getNamespace().getCode()));
			operatorIO.setName(authDTO.getNamespace().getName());
			ticketIO.setOperator(operatorIO);

			ticketIO.setTotalFare(ticketDTO.getTotalFare());
			ticketIO.setJourneyType(ticketDTO.getJourneyType().getCode());
			ticketIO.setTicketDetails(ticketDetailsIO);
			ticketIO.setCancellationTerms(cancellationTermIO);
			ticketIO.setRemarks(ticketDTO.getRemarks());
			ticketIO.setTransactionDate(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
		}
		catch (ServiceException e) {
			apiTransactionlogger.error("Error is Cancel:" + operatorCode + "-" + ticketCode + e.getErrorCode().toString() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		catch (Exception e) {
			apiTransactionlogger.error("V3 isCancel: " + operatorCode + "-" + ticketCode + e.getMessage() + "--" + ConcurrentRequests.get(apiToken));
			throw new ServiceException(ErrorCode.CANCELLATION_NOT_ALLOWED);
		}
		finally {
			apiTransactionlogger.info("V3 isCancel: " + operatorCode + " - " + username + " - " + ticketCode + " - " + referenceTicketNumber + " Response : " + ticketIO.toJSON());
		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/ticket/cancel/{ticketCode}/confirm", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TicketIO> ConfirmCancelTicket(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("ticketCode") String ticketCode, String seatCodeList, String referenceTicketNumber, String ctpc, String cca, @RequestParam(required = false, defaultValue = "API") String deviceMedium) throws Exception {
		TicketIO ticketIO = new TicketIO();
		ValidateMandatory(operatorCode, username, apiToken);
		DeviceMediumEM device = DeviceMediumEM.getDeviceMediumEM(deviceMedium);
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken, device);
		try {
			if (StringUtil.isNull(ticketCode)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE, ticketCode);
			}
			if (BitsUtil.isTagExists(authDTO.getUser().getUserTags(), UserTagEM.API_USER_PT) && (StringUtil.isNull(ctpc) || StringUtil.isNull(cca) || !StringUtil.isNumeric(cca))) {
				throw new ServiceException(ErrorCode.INVALID_ORDER_DETAILS, "ctpc & cca should be valid");
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO.setOverideFlag(false);
			List<TicketDetailsDTO> detailsList = new ArrayList<>();
			List<String> uniqueList = new ArrayList<String>();
			for (int i = 0; i < seatCodeList.split(",").length; i++) {
				TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
				ticketDetailsDTO.setSeatCode(seatCodeList.split(",")[i]);
				detailsList.add(ticketDetailsDTO);

				if (uniqueList.contains(ticketDetailsDTO.getSeatCode())) {
					throw new ServiceException(ErrorCode.DUPLICATE_SEAT_CODE, ticketDetailsDTO.getSeatCode());
				}
				uniqueList.add(ticketDetailsDTO.getSeatCode());
			}
			ticketDTO.setTicketDetails(detailsList);

			// Cancellation services call
			if (seatCodeList.isEmpty()) {
				throw new ServiceException(ErrorCode.INVALID_SEAT_CODE, "Seat code is requried Parameter");
			}

			Map<String, String> additionalAttribute = new HashMap<>();
			if (StringUtil.isNotNull(ctpc)) {
				additionalAttribute.put("CTPC", ctpc);
			}
			if (StringUtil.isNotNull(cca) && StringUtil.isNumeric(cca)) {
				additionalAttribute.put("CCA", cca);
			}
			TicketDTO repositoryTicketDTO = cancelTicketService.TicketConfirmCancel(authDTO, ticketDTO, additionalAttribute);
			ticketIO.setCode(repositoryTicketDTO.getCode());
			ticketIO.setTotalRefundAmount(repositoryTicketDTO.getRefundAmount());
			ticketIO.setCancellationCharge(repositoryTicketDTO.getCancellationCharges());
		}
		catch (ServiceException e) {
			apiTransactionlogger.error("Error Confirm Cancel Seat:" + operatorCode + "-" + ticketCode + "-" + seatCodeList + "-" + e.getErrorCode().toString() + "--" + ConcurrentRequests.get(apiToken));
			ticketFailureService.saveFailureLog(authDTO, e.getErrorCode().getCode(), "CANCL", e.getErrorCode().getMessage() + (StringUtil.isNotNull(e.getData()) ? ", " + e.getData().toString() : Text.EMPTY), "Ticket Code: " + ticketCode + ", Seat Code: " + seatCodeList + ", Ref. Ticket No: " + referenceTicketNumber + ", CTPC: " + ctpc + ", CCA: " + cca);
			throw new ServiceException(e.getErrorCode(), e.getData());
		}
		catch (Exception e) {
			apiTransactionlogger.error("V3 ConfirmCancelSeat:" + operatorCode + "-" + ticketCode + "-" + seatCodeList + "-" + e.getMessage() + "--" + ConcurrentRequests.get(apiToken));
			ticketFailureService.saveFailureLog(authDTO, ErrorCode.CANCELLATION_NOT_ALLOWED.getCode(), "CANCL", e.getMessage(), "Ticket Code: " + ticketCode + ", Seat Code: " + seatCodeList + ", Ref. Ticket No: " + referenceTicketNumber + ", CTPC: " + ctpc + ", CCA: " + cca);
			throw new ServiceException(ErrorCode.CANCELLATION_NOT_ALLOWED);
		}
		finally {
			apiTransactionlogger.info("V3 confirmCancel: " + operatorCode + " - " + username + " - " + ticketCode + " - " + seatCodeList + referenceTicketNumber + " - " + ctpc + " - Rs." + cca + " Response : " + ticketIO.toJSON());
		}
		return ResponseIO.success(ticketIO);
	}

	private boolean ValidateMandatory(String operatorCode, String username, String apiToken) throws Exception {
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

	@RequestMapping(value = "/ticket/{ticketCode}/transfer", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ScheduleTicketTransferTermsIO> isTransferTicket(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		ScheduleTicketTransferTermsIO scheduleTicketTransferTerms = new ScheduleTicketTransferTermsIO();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			if (StringUtil.isNull(ticketCode)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE, ticketCode);
			}
			if (!isAllowApiTicketTransfer(authDTO)) {
				System.out.println("TER001 :" + operatorCode + " - " + apiToken + " - " + DateUtil.NOW() + username + " - " + ticketCode);
				// throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO = ticketTransferService.isTransferTicket(authDTO, ticketDTO);
			scheduleTicketTransferTerms.setChargeAmount(scheduleTicketTransferTermsDTO.getChargeAmount());
			scheduleTicketTransferTerms.setChargeType(scheduleTicketTransferTermsDTO.getChargeType().getCode());
			scheduleTicketTransferTerms.setAllowedTill(scheduleTicketTransferTermsDTO.getDateTime().format(Text.DATE_TIME_DATE4J));
			scheduleTicketTransferTerms.setTransferable(1);
		}
		catch (ServiceException e) {
			apiTransactionlogger.error("V3 IsTransferTicket:" + operatorCode + "-" + ticketCode + "-" + e.getErrorCode().toString() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		finally {
			apiTransactionlogger.info("V3 isTransferTicket: " + operatorCode + " - " + username + " - " + " PNR: " + ticketCode + "--" + ConcurrentRequests.get(apiToken) + " - " + scheduleTicketTransferTerms.getAllowedTill() + " - " + scheduleTicketTransferTerms.getTransferable() + " - " + scheduleTicketTransferTerms.getChargeAmount());
		}
		return ResponseIO.success(scheduleTicketTransferTerms);
	}

	@RequestMapping(value = "/ticket/{ticketCode}/transfer/confirm", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TicketIO> transferTicket(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("ticketCode") String ticketCode, @RequestBody OrderTransferDetailsIO transferDetails) throws Exception {
		ValidateMandatoryV3(operatorCode, username, apiToken, ticketCode, transferDetails);
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
		TicketIO ticketIO = new TicketIO();
		try {
			if (!isAllowApiTicketTransfer(authDTO)) {
				System.out.println("TER002 :" + operatorCode + " - " + apiToken + " - " + DateUtil.NOW() + username);
				// throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}

			TicketDTO transferDTO = new TicketDTO();
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(transferDetails.getTripCode());

			SearchDTO searchDTO = new SearchDTO();
			DateTime tripTravelDate = new DateTime(transferDetails.getTravelDate());
			searchDTO.setTravelDate(tripTravelDate);

			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setCode(transferDetails.getFromStation().getCode());
			searchDTO.setFromStation(fromStationDTO);

			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setCode(transferDetails.getToStation().getCode());
			searchDTO.setToStation(toStationDTO);

			tripDTO.setSearch(searchDTO);
			transferDTO.setTripDTO(tripDTO);

			List<TicketDetailsDTO> transferSeatDetailsList = new ArrayList<TicketDetailsDTO>();
			for (String seatCode : transferDetails.getTransferSeatCode()) {
				TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
				detailsDTO.setSeatCode(seatCode);
				transferSeatDetailsList.add(detailsDTO);

			}
			transferDTO.setTicketDetails(transferSeatDetailsList);
			transferDTO.setMyAccountFlag(false);

			if (transferDetails.getBoardingPoint() != null) {
				StationPointDTO boardingPointDTO = new StationPointDTO();
				boardingPointDTO.setCode(transferDetails.getBoardingPoint().getCode());
				transferDTO.setBoardingPoint(boardingPointDTO);
			}
			if (transferDetails.getDroppingPoint() != null) {
				StationPointDTO droppingPointDTO = new StationPointDTO();
				droppingPointDTO.setCode(transferDetails.getDroppingPoint().getCode());
				transferDTO.setDroppingPoint(droppingPointDTO);
			}

			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
			for (String seatCode : transferDetails.getSeatCode()) {
				TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
				detailsDTO.setSeatCode(seatCode);
				seatDetailsList.add(detailsDTO);
			}
			ticketDTO.setTicketDetails(seatDetailsList);
			if (seatDetailsList.size() != transferSeatDetailsList.size() || seatDetailsList.isEmpty() || transferSeatDetailsList.isEmpty()) {
				throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
			}

			Map<String, Boolean> additionalAttribute = new HashMap<String, Boolean>();
			additionalAttribute.put("captureFareDifferece", Text.TRUE);
			additionalAttribute.put("notification", Text.FALSE);
			additionalAttribute.put("captureTransferCharge", Text.TRUE);
			additionalAttribute.put("captureLowFareDifferece", Text.FALSE);

			TicketDTO transferedDTO = ticketTransferService.transferTicket(authDTO, ticketDTO, transferDTO, additionalAttribute);

			ticketIO.setCode(transferedDTO.getCode());
			StationIO FromStationIO = new StationIO();
			FromStationIO.setCode(transferedDTO.getFromStation().getCode());
			FromStationIO.setName(transferedDTO.getFromStation().getName());
			StationPointIO fromStationPointIO = new StationPointIO();
			fromStationPointIO.setCode(transferedDTO.getBoardingPoint().getCode());
			fromStationPointIO.setName(transferedDTO.getBoardingPoint().getName());
			fromStationPointIO.setLandmark(transferedDTO.getBoardingPoint().getLandmark());
			fromStationPointIO.setLongitude(transferedDTO.getBoardingPoint().getLongitude());
			fromStationPointIO.setNumber(transferedDTO.getBoardingPoint().getNumber());
			fromStationPointIO.setDateTime(transferedDTO.getBoardingPointDateTime().format("YYYY-MM-DD hh:mm:ss"));
			fromStationPointIO.setAddress(transferedDTO.getBoardingPoint().getAddress());
			// To Station Point
			StationIO toStationIO = new StationIO();
			toStationIO.setCode(transferedDTO.getToStation().getCode());
			toStationIO.setName(transferedDTO.getToStation().getName());
			StationPointIO toStationPointIO = new StationPointIO();
			toStationPointIO.setCode(transferedDTO.getDroppingPoint().getCode());
			toStationPointIO.setName(transferedDTO.getDroppingPoint().getName());
			toStationPointIO.setLandmark(transferedDTO.getDroppingPoint().getLandmark());
			toStationPointIO.setLongitude(transferedDTO.getDroppingPoint().getLongitude());
			toStationPointIO.setNumber(transferedDTO.getDroppingPoint().getNumber());
			toStationPointIO.setDateTime(transferedDTO.getDroppingPointDateTime().format("YYYY-MM-DD hh:mm:ss"));
			toStationPointIO.setAddress(transferedDTO.getDroppingPoint().getAddress());
			FromStationIO.setStationPoints(fromStationPointIO);
			toStationIO.setStationPoints(toStationPointIO);
			ticketIO.setFromStation(FromStationIO);
			ticketIO.setToStation(toStationIO);
			ticketIO.setTravelDate(transferedDTO.getTripDate().format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setReportingTime(DateUtil.addMinituesToDate(transferedDTO.getTripDate(), transferedDTO.getReportingMinutes()).format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setRemarks(transferedDTO.getRemarks());
			ticketIO.setPassegerEmailId(transferedDTO.getPassengerEmailId());
			ticketIO.setPassegerMobleNo(transferedDTO.getPassengerMobile());

			ticketIO.setTransactionDate(transferedDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
			// Ticket status
			TicketStatusIO ticketStatusIO = new TicketStatusIO();
			ticketStatusIO.setCode(transferedDTO.getTicketStatus().getCode());
			ticketStatusIO.setName(transferedDTO.getTicketStatus().getDescription());
			ticketIO.setTicketStatus(ticketStatusIO);
		}
		catch (ServiceException e) {
			apiTransactionlogger.error("V3 ConfirmTransferTicket:" + operatorCode + "-" + ticketCode + "-" + e.getErrorCode().toString() + "--" + ConcurrentRequests.get(apiToken));
			ticketFailureService.saveFailureLog(authDTO, e.getErrorCode().getCode(), "TRNSR", e.getErrorCode().getMessage() + (StringUtil.isNotNull(e.getData()) ? ", " + e.getData().toString() : Text.EMPTY), transferDetails.toJSON());
			throw e;
		}
		finally {
			apiTransactionlogger.info("V3 ConfirmTransferTicket: " + operatorCode + " - " + username + " - " + " Route: " + ticketCode + "--" + ConcurrentRequests.get(apiToken) + " - " + transferDetails.toJSON() + " " + ticketIO.getCode() + " Response : " + ticketIO.toJSON());
		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/ticket/{ticketCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TicketIO> getTicketDetails(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		TicketIO ticketIO = new TicketIO();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			if (StringUtil.isNull(ticketCode)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketService.getTicketStatus(authDTO, ticketDTO);

			ticketIO.setCode(ticketDTO.getCode());
			StationIO FromStationIO = new StationIO();
			FromStationIO.setCode(ticketDTO.getFromStation().getCode());
			FromStationIO.setName(ticketDTO.getFromStation().getName());
			StationPointIO fromStationPointIO = new StationPointIO();
			fromStationPointIO.setCode(ticketDTO.getBoardingPoint().getCode());
			fromStationPointIO.setName(ticketDTO.getBoardingPoint().getName());
			fromStationPointIO.setDateTime(ticketDTO.getBoardingPointDateTime().format("YYYY-MM-DD hh:mm:ss"));
			// To Station Point
			StationIO toStationIO = new StationIO();
			toStationIO.setCode(ticketDTO.getToStation().getCode());
			toStationIO.setName(ticketDTO.getToStation().getName());
			StationPointIO toStationPointIO = new StationPointIO();
			toStationPointIO.setCode(ticketDTO.getDroppingPoint().getCode());
			toStationPointIO.setName(ticketDTO.getDroppingPoint().getName());
			toStationPointIO.setDateTime(ticketDTO.getDroppingPointDateTime().format("YYYY-MM-DD hh:mm:ss"));
			FromStationIO.setStationPoints(fromStationPointIO);
			toStationIO.setStationPoints(toStationPointIO);
			ticketIO.setFromStation(FromStationIO);
			ticketIO.setToStation(toStationIO);
			ticketIO.setTravelDate(ticketDTO.getTripDateTime().format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setReportingTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getReportingMinutes()).format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setRemarks(ticketDTO.getRemarks());
			ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());
			ticketIO.setTotalFare(ticketDTO.getTotalFare());
			ticketIO.setTransactionDate(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));

			OperatorIO operatorIO = new OperatorIO();
			operatorIO.setCode(StringUtil.isNull(authDTO.getAliasNamespaceCode(), authDTO.getNamespace().getCode()));
			operatorIO.setName(authDTO.getNamespace().getName());
			ticketIO.setOperator(operatorIO);

			// Ticket status
			TicketStatusIO ticketStatusIO = new TicketStatusIO();
			ticketStatusIO.setCode(ticketDTO.getTicketStatus().getCode());
			ticketStatusIO.setName(ticketDTO.getTicketStatus().getDescription());
			ticketIO.setTicketStatus(ticketStatusIO);

			List<TicketDetailsIO> detailsList = new ArrayList<TicketDetailsIO>();
			for (TicketDetailsDTO detailsDTO : ticketDTO.getTicketDetails()) {
				TicketDetailsIO details = new TicketDetailsIO();
				details.setSeatCode(detailsDTO.getSeatCode());
				details.setSeatFare(detailsDTO.getSeatFare());
				details.setSeatName(detailsDTO.getSeatName());
				details.setServiceTax(detailsDTO.getAcBusTax());
				SeatStatusIO seatStatus = new SeatStatusIO();
				seatStatus.setCode(ticketDTO.getTicketStatus().getCode());
				seatStatus.setName(ticketDTO.getTicketStatus().getDescription());
				details.setSeatStatus(seatStatus);
				detailsList.add(details);
			}
			ticketIO.setTicketDetails(detailsList);
		}
		catch (ServiceException e) {
			apiTransactionlogger.error("V3 getTicketDetails: " + operatorCode + " - " + username + " - " + ticketCode);
			throw e;
		}
		finally {
			apiTransactionlogger.info("V3 getTicketDetails: " + operatorCode + " - " + username + " - " + ticketCode);
		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/ticket/{ticketCode}/tentative/release", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> releaseTentativeBlockTicket(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("ticketCode") String ticketCode, String reason) throws Exception {
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			if (StringUtil.isNull(ticketCode)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO.setBookingCode(ticketCode);
			ticketDTO.setRemarks(reason);
			ticketService.releaseTentativeBlockTicket(authDTO, ticketDTO);

		}
		catch (ServiceException e) {
			throw e;
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/ticket/{ticketCode}/refference/{vendorpnr}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateVendorRefferenceTicketPNR(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("ticketCode") String ticketCode, @PathVariable("vendorpnr") String vendorpnr) throws Exception {
		try {
			System.out.println(ticketCode + " - " + vendorpnr);
		}
		catch (ServiceException e) {
			throw e;
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/user/profile/balance", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<JSONObject> getUserCurrentBalance(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken) throws Exception {
		JSONObject userBalance = new JSONObject();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);

			BigDecimal balance = userService.getCurrentCreditBalace(authDTO, authDTO.getUser());
			userBalance.put("userBalance", balance);
		}
		catch (ServiceException e) {
			apiTransactionlogger.error("V3 getUserCurrentBalance: " + operatorCode + " - " + username + " - " + userBalance);
			throw e;
		}
		finally {
			apiTransactionlogger.info("V3 getUserCurrentBalance: " + operatorCode + " - " + username + " - " + userBalance);
		}
		return ResponseIO.success(userBalance);
	}

	@RequestMapping(value = "/ticket/edit/{ticketCode}/boardingPoint/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> editBoardingPoint(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("ticketCode") String ticketCode, String oldBoardingPointCode, String newBoardingPointCode) throws Exception {
		BaseIO response = new BaseIO();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			if (StringUtil.isNull(ticketCode) || StringUtil.isNull(newBoardingPointCode) || StringUtil.isNull(oldBoardingPointCode)) {
				ResponseIO.failure("ED01", "Requried parameter, should not be null");
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0 || (ticketDTO.getTicketStatus().getId() != 1 && ticketDTO.getTicketStatus().getId() != 5)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}

			TripDTO tripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());
			StationPointDTO oldStationPoint = null;
			StationPointDTO newStationPoint = null;
			for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
				if (pointDTO.getCode().equals(oldBoardingPointCode)) {
					oldStationPoint = pointDTO;
					continue;
				}
				if (pointDTO.getCode().equals(newBoardingPointCode)) {
					newStationPoint = pointDTO;
					continue;
				}
			}
			if (newStationPoint != null && newStationPoint.getId() != ticketDTO.getBoardingPoint().getId()) {
				ticketDTO.setBoardingPoint(newStationPoint);
				ticketDTO.getBoardingPoint().setMinitues(tripDTO.getStage().getFromStation().getMinitues() + ticketDTO.getBoardingPoint().getMinitues());
				String event = "edit Boarding Point : " + (oldStationPoint != null ? oldStationPoint.getName() : "") + " changed to " + newStationPoint.getName();
				ticketEditService.editBoardingPoint(authDTO, ticketDTO, event, Numeric.ZERO_INT);
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}
			response.setActiveFlag(1);
		}
		catch (ServiceException e) {
			apiTransactionlogger.error("V3 editBoardingPoint: " + operatorCode + " - " + username + " - " + ticketCode);
			throw e;
		}
		finally {
			apiTransactionlogger.info("V3 editBoardingPoint: " + operatorCode + " - " + username + " - " + ticketCode);
		}
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/ticket/edit/{ticketCode}/dropingPoint/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> dropingPoint(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("ticketCode") String ticketCode, String oldDropingPointCode, String newDropingPointCode) throws Exception {
		BaseIO response = new BaseIO();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			if (StringUtil.isNull(ticketCode) || StringUtil.isNull(newDropingPointCode) || StringUtil.isNull(oldDropingPointCode)) {
				ResponseIO.failure("ED01", "Requried parameter, should not be null");
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			TripDTO tripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());
			StationPointDTO oldDropingPoint = null;
			StationPointDTO newDropingPoint = null;
			for (StationPointDTO pointDTO : tripDTO.getStage().getToStation().getStationPoint()) {
				if (pointDTO.getCode().equals(oldDropingPointCode)) {
					oldDropingPoint = pointDTO;
					continue;
				}
				if (pointDTO.getCode().equals(newDropingPointCode)) {
					newDropingPoint = pointDTO;
					continue;
				}
			}
			if (oldDropingPoint != null && newDropingPoint != null && oldDropingPoint.getId() == ticketDTO.getDroppingPoint().getId()) {
				ticketDTO.setDroppingPoint(newDropingPoint);
				String event = "edit Droping Point : " + oldDropingPoint.getName() + " changed to " + newDropingPoint.getName();
				ticketEditService.editDroppingPoint(authDTO, ticketDTO, event, Numeric.ZERO_INT);
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}
			response.setActiveFlag(1);
		}
		catch (ServiceException e) {
			apiTransactionlogger.error("V3 editDroppingPoint: " + operatorCode + " - " + username + " - " + ticketCode);
			throw e;
		}
		finally {
			apiTransactionlogger.info("V3 editDroppingPoint: " + operatorCode + " - " + username + " - " + ticketCode);
		}

		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/ticket/edit/{ticketCode}/mobile/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> passengerMobile(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("ticketCode") String ticketCode, String oldMobileNumber, String newMobileNumber) throws Exception {
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			if (StringUtil.isNull(ticketCode) || StringUtil.isNull(oldMobileNumber) || StringUtil.isNull(newMobileNumber)) {
				ResponseIO.failure("ED01", "Requried parameter, should not be null");
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0 || (ticketDTO.getTicketStatus().getId() != 1 && ticketDTO.getTicketStatus().getId() != 5)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			if (ticketDTO.getPassengerMobile().equals(oldMobileNumber) && StringUtil.isNumeric(newMobileNumber)) {
				ticketDTO.setPassengerMobile(newMobileNumber);
				String event = "edit Mobile Number : " + oldMobileNumber + " changed to " + newMobileNumber;
				ticketEditService.editMobileNumber(authDTO, ticketDTO, event, Numeric.ZERO_INT);
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}
		}
		catch (ServiceException e) {
			apiTransactionlogger.error("V3 editMobileNumber: " + operatorCode + " - " + username + " - " + ticketCode);
			throw e;
		}
		finally {
			apiTransactionlogger.info("V3 editMobileNumber: " + operatorCode + " - " + username + " - " + ticketCode);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/inventory/index/{travelDate}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripIO>> getInventoryIndex(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("travelDate") String travelDate) throws Exception {
		String data = "Trip Stage list" + Text.HYPHEN + Text.HYPHEN + travelDate;
		checkConcurrentRequests(operatorCode, username, apiToken, data);

		List<TripIO> tripList = new ArrayList<>();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			if (!DateUtil.isValidDate(travelDate)) {
				throw new ServiceException(ErrorCode.INVALID_DATE, "Requried format is yyyy-MM-dd");
			}
			DateTime tripTravelDate = new DateTime(travelDate);
			if (DateUtil.getDayDifferent(DateUtil.NOW(), tripTravelDate) < 0) {
				throw new ServiceException(ErrorCode.TRIP_DATE_OVER);
			}
			if (DateUtil.getDayDifferent(DateUtil.NOW(), tripTravelDate) > authDTO.getNamespace().getProfile().getAdvanceBookingDays()) {
				throw new ServiceException(ErrorCode.MAX_ADVANCE_BOOKING_DAYS, authDTO.getNamespace().getProfile().getAdvanceBookingDays() + " days");
			}
			List<String> scheduleCodes = new ArrayList<>();
			List<TripDTO> list = inventoryService.getScheduleTripStageList(authDTO, tripTravelDate, scheduleCodes);
			for (TripDTO tripDTO : list) {
				TripIO tripIO = new TripIO();
				ScheduleIO schedule = new ScheduleIO();
				schedule.setCode(tripDTO.getSchedule().getCode());
				schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
				tripIO.setSchedule(schedule);
				tripIO.setDisplayName(tripDTO.getSchedule().getApiDisplayName());
				if (tripDTO.getTripStatus().getCode().equals(TripStatusEM.TRIP_YET_OPEN.getCode())) {
					continue;
				}
				// Trip Status
				TripStatusIO tripStatusIO = new TripStatusIO();
				tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
				tripStatusIO.setName(tripDTO.getTripStatus().getName());
				tripIO.setTripStatus(tripStatusIO);

				OperatorIO operatorIO = new OperatorIO();
				operatorIO.setCode(StringUtil.isNull(authDTO.getAliasNamespaceCode(), authDTO.getNamespace().getCode()));
				operatorIO.setName(authDTO.getNamespace().getName());
				tripIO.setOperator(operatorIO);

				// int Hours = (int)
				// (tripDTO.getStage().getToStation().getMinitues() -
				// tripDTO.getStage().getFromStation().getMinitues()) / 60;
				// int Minutes = (int)
				// (tripDTO.getStage().getToStation().getMinitues() -
				// tripDTO.getStage().getFromStation().getMinitues()) % 60;
				// tripIO.setTravelTime(Hours + " : " + Minutes);
				// tripIO.setCloseTime(tripDTO.getTripCloseTime().format("YYYY-MM-DD
				// hh:mm:ss"));
				// Bus
				BusIO busIO = new BusIO();
				busIO.setName(tripDTO.getBus().getName());
				busIO.setCode(tripDTO.getBus().getCode());
				busIO.setCategoryCode(tripDTO.getBus().getCategoryCode());
				busIO.setBusType(busService.getBusCategoryByCode(tripDTO.getBus().getCategoryCode()));
				busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
				busIO.setTotalSeatCount(tripDTO.getBus().getBusSeatLayoutDTO().getList().size());

				Map<String, Integer> availableMap = new HashMap<String, Integer>();
				Map<String, List<BusSeatLayoutDTO>> seatFareMap = new HashMap<String, List<BusSeatLayoutDTO>>();

				for (BusSeatLayoutDTO layoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {

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
				tripIO.setTripStageCode(tripDTO.getStage().getCode());
				tripIO.setTravelDate(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD"));
				StationIO fromStation = new StationIO();
				StationIO toStation = new StationIO();
				fromStation.setCode(tripDTO.getStage().getFromStation().getStation().getCode());
				fromStation.setName(tripDTO.getStage().getFromStation().getStation().getName());
				fromStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getStage().getTravelDate(), tripDTO.getStage().getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				toStation.setCode(tripDTO.getStage().getToStation().getStation().getCode());
				toStation.setName(tripDTO.getStage().getToStation().getStation().getName());
				toStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getStage().getTravelDate(), tripDTO.getStage().getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
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

				tripList.add(tripIO);
			}
		}
		catch (ServiceException e) {
			loggerapi.error("V3 inventory Index:" + operatorCode + " - " + username + " - " + travelDate + " #: " + tripList.size() + " e:" + e.getErrorCode().toString() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		catch (Exception e) {
			loggerapi.error("V3 inventory Index:" + operatorCode + " - " + username + " - " + "-" + travelDate + " #: " + tripList.size() + " e:" + e.getMessage() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		finally {
			releaseConcurrentRequests(apiToken);
		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/terms/{tripCode}/{fromStationCode}/{toStationCode}/{travelDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<CancellationTermIO> getCancellationTerms(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("tripCode") String tripCode, @PathVariable("fromStationCode") String fromStationCode, @PathVariable("toStationCode") String toStationCode, @PathVariable("travelDate") String travelDate) throws Exception {
		String data = "Cancellation Terms" + Text.HYPHEN + tripCode + Text.HYPHEN + travelDate + Text.HYPHEN + fromStationCode + Text.HYPHEN + toStationCode;
		checkConcurrentRequests(operatorCode, username, apiToken, data);
		CancellationTermIO cancellationTermIO = new CancellationTermIO();
		try {
			ValidateMandatory(operatorCode, username, apiToken);
			AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);

			if (!DateUtil.isValidDate(travelDate)) {
				throw new ServiceException(ErrorCode.INVALID_DATE, "Requried format is yyyy-MM-dd");
			}
			DateTime tripTravelDate = new DateTime(travelDate);
			if (DateUtil.getDayDifferent(DateUtil.NOW(), tripTravelDate) < 0) {
				throw new ServiceException(ErrorCode.TRIP_DATE_OVER);
			}
			if (DateUtil.getDayDifferent(DateUtil.NOW(), tripTravelDate) > authDTO.getNamespace().getProfile().getAdvanceBookingDays()) {
				throw new ServiceException(ErrorCode.MAX_ADVANCE_BOOKING_DAYS, authDTO.getNamespace().getProfile().getAdvanceBookingDays() + " days");
			}
			if (StringUtil.isNull(fromStationCode) || StringUtil.isNull(toStationCode) || fromStationCode.length() <= 7 || toStationCode.length() <= 7 || fromStationCode.equals(toStationCode)) {
				throw new ServiceException(ErrorCode.INVALID_STATION);
			}

			TripDTO tripStageDTO = new TripDTO();
			tripStageDTO.setCode(tripCode);
			SearchDTO searchDTO = new SearchDTO();

			searchDTO.setTravelDate(tripTravelDate);
			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setCode(fromStationCode);
			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setCode(toStationCode);
			searchDTO.setFromStation(fromStationDTO);
			searchDTO.setToStation(toStationDTO);
			tripStageDTO.setSearch(searchDTO);
			TripDTO tripDTO = busmapService.getSearchBusmapV3(authDTO, tripStageDTO);

			CancellationTermDTO cancellationTermDTO = termsService.getCancellationTermsByTripDTO(authDTO, authDTO.getUser(), tripDTO);
			if (cancellationTermDTO == null || cancellationTermDTO.getId() == 0) {
				throw new ServiceException();
			}
			Set<BigDecimal> hashsetList = new HashSet<BigDecimal>();
			// Group Wise Fare and Default Fare
			for (StageFareDTO fareDTO : tripDTO.getStage().getStageFare()) {
				if (fareDTO.getGroup().getId() != 0 && authDTO.getGroup().getId() == fareDTO.getGroup().getId()) {
					hashsetList.add(fareDTO.getFare());
				}
				else if (fareDTO.getGroup().getId() == 0) {
					hashsetList.add(fareDTO.getFare());
				}
			}
			DateTime travelDateTime = DateUtil.getDateTime(tripDTO.getAdditionalAttributes().get(Constants.CANCELLATION_DATETIME));
			cancellationTermDTO = cancelTicketService.getCancellationPolicyConvention(authDTO, authDTO.getUser(), cancellationTermDTO, null, travelDateTime, new ArrayList<>(hashsetList));

			cancellationTermIO.setCode(cancellationTermDTO.getCode());
			List<CancellationPolicyIO> policyIOs = new ArrayList<CancellationPolicyIO>();
			for (CancellationPolicyDTO policyDTO : cancellationTermDTO.getPolicyList()) {
				CancellationPolicyIO policyIO = new CancellationPolicyIO();
				policyIO.setFromValue(policyDTO.getFromValue());
				policyIO.setToValue(policyDTO.getToValue());
				policyIO.setDeductionAmount(policyDTO.getDeductionValue() != null ? policyDTO.getDeductionValue() : BigDecimal.ZERO);
				policyIO.setPercentageFlag(policyDTO.getPercentageFlag());
				policyIO.setPolicyPattern(policyDTO.getPolicyPattern() != null ? policyDTO.getPolicyPattern() : Text.EMPTY);

				policyIO.setTerm(policyDTO.getTerm());
				policyIO.setDeductionAmountTxt(policyDTO.getDeductionAmountTxt());
				policyIO.setRefundAmountTxt(policyDTO.getRefundAmountTxt());
				policyIO.setChargesTxt(policyDTO.getChargesTxt());
				policyIOs.add(policyIO);
			}
			cancellationTermIO.setPolicyList(policyIOs);
		}
		catch (ServiceException e) {
			loggerapi.error("V3 CancellationTerms:" + operatorCode + " - " + username + " - " + tripCode + "-" + fromStationCode + "-" + toStationCode + "-" + travelDate + e.getErrorCode().toString() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		catch (Exception e) {
			loggerapi.error("V3 CancellationTerms:" + operatorCode + " - " + username + " - " + tripCode + "-" + fromStationCode + "-" + toStationCode + "-" + travelDate + e.getMessage() + "--" + ConcurrentRequests.get(apiToken));
			throw e;
		}
		finally {
			releaseConcurrentRequests(apiToken);
		}
		return ResponseIO.success(cancellationTermIO);
	}

	private boolean ValidateMandatoryV3(String operatorCode, String username, String apiToken, String ticketCode, OrderTransferDetailsIO transferDetails) throws Exception {
		if (StringUtil.isNull(operatorCode)) {
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
		}
		if (StringUtil.isNull(username)) {
			throw new ServiceException(ErrorCode.USER_INVALID_USERNAME);
		}
		if (StringUtil.isNull(apiToken)) {
			throw new ServiceException(ErrorCode.INVALID_CREDENTIALS);
		}
		if (StringUtil.isNull(ticketCode)) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE, ticketCode);
		}
		if (StringUtil.isNull(transferDetails.getTripCode())) {
			throw new ServiceException(ErrorCode.TRIP_CODE);
		}
		if (StringUtil.isNull(transferDetails.getTravelDate())) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		if (transferDetails.getFromStation() == null || transferDetails.getToStation() == null || StringUtil.isNull(transferDetails.getFromStation().getCode()) || StringUtil.isNull(transferDetails.getToStation().getCode())) {
			throw new ServiceException(ErrorCode.INVALID_STATION);
		}
		if (transferDetails.getBoardingPoint() == null || StringUtil.isNull(transferDetails.getBoardingPoint().getCode())) {
			throw new ServiceException(ErrorCode.STATION_POINT);
		}
		return true;
	}

}
