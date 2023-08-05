package org.in.com.controller.commerce;

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
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang.WordUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.commerce.io.AddonsDiscountIO;
import org.in.com.controller.commerce.io.BusIO;
import org.in.com.controller.commerce.io.BusSeatLayoutIO;
import org.in.com.controller.commerce.io.BusSeatTypeIO;
import org.in.com.controller.commerce.io.OrderDetailsIO;
import org.in.com.controller.commerce.io.OrderDetailsV3IO;
import org.in.com.controller.commerce.io.OrderIO;
import org.in.com.controller.commerce.io.OrderV3IO;
import org.in.com.controller.commerce.io.OrganizationIO;
import org.in.com.controller.commerce.io.PaymentModeIO;
import org.in.com.controller.commerce.io.ResponseIO;
import org.in.com.controller.commerce.io.RouteIO;
import org.in.com.controller.commerce.io.ScheduleDiscountIO;
import org.in.com.controller.commerce.io.ScheduleIO;
import org.in.com.controller.commerce.io.ScheduleTicketTransferTermsIO;
import org.in.com.controller.commerce.io.SeatGendarStatusIO;
import org.in.com.controller.commerce.io.SeatStatusIO;
import org.in.com.controller.commerce.io.StageFareIO;
import org.in.com.controller.commerce.io.StageIO;
import org.in.com.controller.commerce.io.StateIO;
import org.in.com.controller.commerce.io.StationIO;
import org.in.com.controller.commerce.io.StationPointIO;
import org.in.com.controller.commerce.io.TicketDetailsIO;
import org.in.com.controller.commerce.io.TicketIO;
import org.in.com.controller.commerce.io.TicketStatusIO;
import org.in.com.controller.commerce.io.TravelStopsIO;
import org.in.com.controller.commerce.io.TripIO;
import org.in.com.controller.commerce.io.TripStatusIO;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.AmenitiesIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.CancellationPolicyIO;
import org.in.com.controller.web.io.CancellationTermIO;
import org.in.com.controller.web.io.DiscountCouponIO;
import org.in.com.controller.web.io.DiscountCriteriaIO;
import org.in.com.controller.web.io.GalleryImageIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.NamespaceBannerDetailsIO;
import org.in.com.controller.web.io.NamespaceBannerIO;
import org.in.com.controller.web.io.NamespaceTaxIO;
import org.in.com.controller.web.io.PaymentGatewayPartnerIO;
import org.in.com.controller.web.io.RoleIO;
import org.in.com.controller.web.io.DiscountSpecialCriteriaIO;
import org.in.com.controller.web.io.TermIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AddonsDiscountOfflineDTO;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.CancellationPolicyDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.DiscountCriteriaDTO;
import org.in.com.dto.GalleryImageDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.NamespaceBannerDTO;
import org.in.com.dto.NamespaceBannerDetailsDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.PaymentGatewayPartnerDTO;
import org.in.com.dto.PaymentGatewayScheduleDTO;
import org.in.com.dto.PaymentModeDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDiscountDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.DiscountSpecialCriteriaDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TermDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TravelStopsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.StationPointAmenitiesEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.dto.enumeration.TripActivitiesEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.dto.enumeration.WalletAccessEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AddonsDiscountOfflineService;
import org.in.com.service.BusService;
import org.in.com.service.BusmapService;
import org.in.com.service.CancelTicketService;
import org.in.com.service.CancellationTermsService;
import org.in.com.service.DiscountService;
import org.in.com.service.GalleryImageService;
import org.in.com.service.NamespaceBannerService;
import org.in.com.service.OrganizationService;
import org.in.com.service.PaymentMerchantGatewayScheduleService;
import org.in.com.service.ScheduleFareOverrideService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleTicketTransferTermsService;
import org.in.com.service.SearchService;
import org.in.com.service.DiscountSpecialCriteriaService;
import org.in.com.service.StationService;
import org.in.com.service.TermsService;
import org.in.com.service.TicketService;
import org.in.com.service.TransactionOTPService;
import org.in.com.service.TravelStopsService;
import org.in.com.service.TripService;
import org.in.com.service.UserCustomerService;
import org.in.com.service.UserWalletService;
import org.in.com.utils.BitsEnDecrypt;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.EmailUtil;
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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/{authtoken}/commerce")
public class CommerceController extends BaseController {
	@Autowired
	CancellationTermsService cancellationTermsService;
	@Autowired
	SearchService searchService;
	@Autowired
	BusmapService busmapService;
	@Autowired
	StationService stationService;
	@Autowired
	PaymentMerchantGatewayScheduleService gatewayScheduleService;
	@Autowired
	BusService busService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	OrganizationService organizationService;
	@Autowired
	TicketService ticketService;
	@Autowired
	TermsService termsService;
	@Autowired
	ScheduleFareOverrideService fareOverrideService;
	@Autowired
	DiscountService discountService;
	@Autowired
	GalleryImageService imageService;
	@Autowired
	TripService tripService;
	@Autowired
	TransactionOTPService otpService;
	@Autowired
	CancelTicketService cancelTicketService;
	@Autowired
	TravelStopsService travelStopsService;
	@Autowired
	AddonsDiscountOfflineService discountOfflineService;
	@Autowired
	ScheduleTicketTransferTermsService scheduleTicketTransferTermsService;
	@Autowired
	UserWalletService walletService;
	@Autowired
	NamespaceBannerService bannerService;
	@Autowired
	DiscountSpecialCriteriaService discountSpecialCriteriaService;
	@Autowired
	UserCustomerService userCustomerService;

	private static final Logger logger = LoggerFactory.getLogger(CommerceController.class);

	@RequestMapping(value = "/station", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationIO>> getStations(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<StationIO> IOlist = new ArrayList<StationIO>();
		if (authDTO != null) {
			List<StationDTO> list = stationService.getCommerceStation(authDTO);
			for (StationDTO stationDTO : list) {
				StationIO stationIO = new StationIO();
				stationIO.setCode(stationDTO.getCode());
				stationIO.setName(stationDTO.getName());
				IOlist.add(stationIO);
			}
		}
		return ResponseIO.success(IOlist);
	}

	@RequestMapping(value = "/route", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, List<String>>> getRoutes(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		Map<String, List<String>> MapList = null;
		if (authDTO != null) {
			MapList = stationService.getCommerceRoutes(authDTO);

		}
		return ResponseIO.success(MapList);
	}

	@RequestMapping(value = "/route/v2", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, String>>> getRoutesV2(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<Map<String, String>> listMap = new ArrayList<Map<String, String>>();
		if (authDTO != null) {
			Map<String, List<String>> routeList = stationService.getCommerceRoutes(authDTO);
			if (routeList != null) {
				for (Map.Entry<String, List<String>> entry : routeList.entrySet()) {
					for (String toStation : entry.getValue()) {
						Map<String, String> routeMap = new HashMap<String, String>();
						routeMap.put(entry.getKey(), toStation);
						listMap.add(routeMap);
					}
				}
			}
		}
		return ResponseIO.success(listMap);
	}

	@RequestMapping(value = "/route/v3", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, Object>> getRoutesV3(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		Map<String, Object> topRouteList = stationService.getTopRoutes(authDTO);

		return ResponseIO.success(topRouteList);
	}

	@RequestMapping(value = "/schedule/{tripDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripIO>> getActiveTripSchedule(@PathVariable("authtoken") String authtoken, @PathVariable("tripDate") String tripDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<TripIO> tripList = new ArrayList<>();
		if (authDTO != null) {
			if (!DateUtil.isValidDate(tripDate)) {
				throw new ServiceException(ErrorCode.INVALID_DATE);
			}

			// Sector Permission
			checkSectorPermission(authDTO);

			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(new DateTime(tripDate));
			List<TripDTO> list = searchService.getAllTrips(authDTO, searchDTO);
			for (TripDTO tripDTO : list) {
				TripIO tripIO = new TripIO();
				ScheduleIO schedule = new ScheduleIO();
				schedule.setCode(tripDTO.getSchedule().getCode());
				schedule.setName(tripDTO.getSchedule().getName());
				schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
				schedule.setActiveFrom(tripDTO.getSchedule().getActiveFrom());
				schedule.setActiveTo(tripDTO.getSchedule().getActiveTo());
				schedule.setDisplayName(tripDTO.getSchedule().getDisplayName());
				schedule.setPnrStartCode(tripDTO.getSchedule().getPnrStartCode());
				// schedule.setMobileTicketFlag(tripDTO.getSchedule().getMobileTicketFlag());
				schedule.setDayOfWeek(tripDTO.getSchedule().getDayOfWeek());
				// schedule.setBoardingReportingMinitues(tripDTO.getSchedule().getBoardingReportingMinitues());
				// schedule.setServiceTax(tripDTO.getSchedule().getAcBusTax());
				tripIO.setSchedule(schedule);

				// Bus
				BusIO busIO = new BusIO();
				busIO.setName(tripDTO.getBus().getName());
				busIO.setCategoryCode(tripDTO.getBus().getCategoryCode() == null ? "" : tripDTO.getBus().getCategoryCode());
				busIO.setBusType(busService.getBusCategoryByCode(tripDTO.getBus().getCategoryCode()));
				busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
				busIO.setTotalSeatCount(tripDTO.getBus().getReservableLayoutSeatCount());
				tripIO.setBus(busIO);
				tripIO.setBookedSeatCount(tripDTO.getBookedSeatCount());

				List<StageIO> stageList = new ArrayList<>();
				// Sorting
				Comparator<StageDTO> comp = new BeanComparator("stageSequence");
				Collections.sort(tripDTO.getStageList(), comp);

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
					List<StageFareIO> stageFareList = new ArrayList<>();
					for (StageFareDTO fareDTO : stageDTO.getStageFare()) {
						StageFareIO stageFareIO = new StageFareIO();
						stageFareIO.setFare(fareDTO.getFare());
						stageFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
						stageFareIO.setSeatName(fareDTO.getBusSeatType().getName());
						if (fareDTO.getGroup() != null) {
							stageFareIO.setGroupName(fareDTO.getGroup().getName());
						}
						stageFareList.add(stageFareIO);
					}
					stageIO.setStageFare(stageFareList);
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
		return ResponseIO.success(tripList);
	}

	private void checkSectorPermission(AuthDTO authDTO) {
		// Permission check
		List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
		eventList.add(MenuEventEM.SECTOR);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);

		Map<String, String> additionalAttribute = new HashMap<>();
		additionalAttribute.put(Text.SECTOR, menuEventDTO != null ? String.valueOf(menuEventDTO.getEnabledFlag()) : Numeric.ZERO);
		authDTO.setAdditionalAttribute(additionalAttribute);
	}

	@RequestMapping(value = "/search/{fromCode}/{toCode}/{tripDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripIO>> getSearchResult(@PathVariable("authtoken") String authtoken, @PathVariable("fromCode") String fromCode, @PathVariable("toCode") String toCode, @PathVariable("tripDate") String tripDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<TripIO> tripList = new ArrayList<>();
		if (authDTO != null) {
			logger.info("Searching the result for the stations from : " + fromCode + " to " + toCode + " for the travel date: " + tripDate);
			if (!DateUtil.isValidDate(tripDate)) {
				throw new ServiceException(ErrorCode.INVALID_DATE);
			}
			DateTime tripTravelDate = new DateTime(tripDate);
			if (DateUtil.getDayDifferent(tripTravelDate, DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSearchPastDayCount()) {
				throw new ServiceException(ErrorCode.TRIP_DATE_OVER);
			}
			if (DateUtil.getDayDifferent(DateUtil.NOW(), tripTravelDate) > authDTO.getNamespace().getProfile().getAdvanceBookingDays()) {
				throw new ServiceException(ErrorCode.MAX_ADVANCE_BOOKING_DAYS, authDTO.getNamespace().getProfile().getAdvanceBookingDays() + " days");
			}
			if (StringUtil.isNull(fromCode) || StringUtil.isNull(toCode) || fromCode.length() <= 7 || toCode.length() <= 7 || (fromCode.equals(toCode) && !toCode.equals(Constants.OPEN_SEARCH_KEY))) {
				throw new ServiceException(ErrorCode.INVALID_STATION);
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
				schedule.setCode(tripDTO.getSchedule().getCode());
				schedule.setName(tripDTO.getSchedule().getName());
				schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
				schedule.setDisplayName(tripDTO.getSchedule().getDisplayName());
				// schedule.setServiceTax(tripDTO.getSchedule().getAcBusTax());
				tripIO.setSchedule(schedule);

				// Trip Status
				TripStatusIO tripStatusIO = new TripStatusIO();
				tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
				tripStatusIO.setName(tripDTO.getTripStatus().getName());
				tripIO.setTripStatus(tripStatusIO);

				int Hours = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) / 60;
				int Minutes = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) % 60;
				tripIO.setTravelTime(String.format("%02d", Hours) + " : " + String.format("%02d", Minutes));
				tripIO.setAdditionalAttributes(tripDTO.getAdditionalAttributes());

				ScheduleTicketTransferTermsIO ticketTransferTerms = new ScheduleTicketTransferTermsIO();
				ScheduleTicketTransferTermsDTO ticketTransferTermsDTO = tripDTO.getSchedule().getTicketTransferTerms();
				ticketTransferTerms.setChargeAmount(ticketTransferTermsDTO != null ? ticketTransferTermsDTO.getChargeAmount() : BigDecimal.ZERO);
				ticketTransferTerms.setAllowedTill(ticketTransferTermsDTO != null ? ticketTransferTermsDTO.getDateTime().format(Text.DATE_TIME_DATE4J) : null);
				ticketTransferTerms.setTransferable(ticketTransferTermsDTO != null ? Numeric.ONE_INT : Numeric.ZERO_INT);
				ticketTransferTerms.setChargeType(ticketTransferTermsDTO != null ? ticketTransferTermsDTO.getChargeType().getCode() : Text.NA);
				tripIO.setTicketTransferTerms(ticketTransferTerms);

				// Bus
				BusIO busIO = new BusIO();
				busIO.setCode(tripDTO.getBus().getCode());
				busIO.setName(tripDTO.getBus().getName());
				busIO.setBusType(BitsUtil.getBusCategoryUsingEM(tripDTO.getBus().getCategoryCode()));
				busIO.setCategoryCode(tripDTO.getBus().getCategoryCode());
				busIO.setTotalSeatCount(tripDTO.getBus().getSeatCount());
				List<AmenitiesIO> amenities = new ArrayList<AmenitiesIO>();
				for (AmenitiesDTO amenitiesDTO : tripDTO.getAmenities()) {
					AmenitiesIO amenitiesIO = new AmenitiesIO();
					amenitiesIO.setCode(amenitiesDTO.getCode());
					amenitiesIO.setName(amenitiesDTO.getName());
					amenitiesIO.setActiveFlag(amenitiesDTO.getActiveFlag());
					amenities.add(amenitiesIO);
				}
				tripIO.setAmenities(amenities);

				List<BaseIO> activities = new ArrayList<BaseIO>();
				for (TripActivitiesEM activitiesEM : tripDTO.getActivities()) {
					BaseIO activitiesIO = new BaseIO();
					activitiesIO.setCode(activitiesEM.getCode());
					activitiesIO.setName(activitiesEM.getName());
					activities.add(activitiesIO);
				}
				tripIO.setActivities(activities);

				// List<BusSeatLayoutIO> seatLayoutList = new ArrayList<>();
				Map<String, Integer> availableMap = new HashMap<String, Integer>();
				Map<String, List<BusSeatLayoutDTO>> seatFareMap = new HashMap<String, List<BusSeatLayoutDTO>>();
				int blockedSeatCount = 0;
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
					layoutIO.setActiveFlag(layoutDTO.getActiveFlag());
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
					else if (layoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_OTHER || layoutDTO.getSeatStatus() == SeatStatusEM.BLOCKED) {
						blockedSeatCount++;
					}
				}

				tripIO.setBlockedSeatCount(blockedSeatCount);
				tripIO.setBus(busIO);
				tripIO.setTripCode(tripDTO.getCode());
				tripIO.setTravelDate(tripDate);
				// tripIO.setCloseTime(tripDTO.getTripCloseTime().format("YYYY-MM-DD
				// hh:mm:ss"));
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
					int availableSeatCount = 0;
					for (StageFareDTO fareDTO : tripDTO.getStage().getStageFare()) {
						StageFareIO stageFareIO = new StageFareIO();
						stageFareIO.setFare(fareDTO.getFare());
						stageFareIO.setDiscountFare(fareDTO.getDiscountFare());
						stageFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
						stageFareIO.setSeatName(fareDTO.getBusSeatType().getName());
						if (fareDTO.getGroup() != null) {
							stageFareIO.setGroupName(fareDTO.getGroup().getName());
						}
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
								seatFareIO.setDiscountFare(fareDTO.getDiscountFare());
								if (tripDTO.getSchedule().getScheduleDiscount() != null && tripDTO.getSchedule().getScheduleDiscount().getFemaleDiscountFlag() != 1) {
									seatFareIO.setDiscountFare(BitsUtil.getDiscountFare(tripDTO.getSchedule().getScheduleDiscount(), fare));
								}
								seatFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
								seatFareIO.setSeatName(fareDTO.getBusSeatType().getName());
								seatFareIO.setAvailableSeatCount(seatFareCount.get(fare));
								stageFareIO.setAvailableSeatCount(stageFareIO.getAvailableSeatCount() - seatFareCount.get(fare));
								stageFareList.add(seatFareIO);
							}
						}
						// remove multiple fare if avl is 0
						if (stageFareIO.getAvailableSeatCount() == 0 && !stageFareList.isEmpty()) {
							continue;
						}
						stageFareList.add(stageFareIO);
						availableSeatCount = stageFareIO.getAvailableSeatCount() + availableSeatCount;
					}
					// Sorting
					Comparator<StageFareIO> comp = new BeanComparator("fare");
					Collections.sort(stageFareList, comp);

					tripIO.setStageFare(stageFareList);
					tripIO.setAvailableSeatCount(availableSeatCount);
					tripIO.setTravelStopCount(tripDTO.getTravelStopCount());

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
						pointIO.setFare(pointDTO.getFare());

						List<BaseIO> amenitiesList = new ArrayList<>();
						for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(pointDTO.getAmenities())) {
							BaseIO amenitiesIO = new BaseIO();
							amenitiesIO.setCode(amenitiesEM.getCode());
							amenitiesIO.setName(amenitiesEM.getName());
							amenitiesList.add(amenitiesIO);
						}
						pointIO.setAmenities(amenitiesList);
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
						pointIO.setFare(pointDTO.getFare());

						List<BaseIO> amenitiesList = new ArrayList<>();
						for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(pointDTO.getAmenities())) {
							BaseIO amenitiesIO = new BaseIO();
							amenitiesIO.setCode(amenitiesEM.getCode());
							amenitiesIO.setName(amenitiesEM.getName());
							amenitiesList.add(amenitiesIO);
						}
						pointIO.setAmenities(amenitiesList);
						toStationPoint.add(pointIO);
					}
					// Sorting
					Comparator<StationPointIO> timeSort = new BeanComparator("dateTime");
					Collections.sort(fromStationPoint, timeSort);
					Collections.sort(toStationPoint, timeSort);

					fromStation.setStationPoint(fromStationPoint);
					toStation.setStationPoint(toStationPoint);

					tripIO.setFromStation(fromStation);
					tripIO.setToStation(toStation);
				}
				// Sorting
				Comparator<ScheduleStationDTO> comp = new BeanComparator("stationSequence");
				Collections.sort(tripDTO.getStationList(), comp);

				List<StationIO> viaStation = new ArrayList<StationIO>();
				for (ScheduleStationDTO stationDTO : tripDTO.getStationList()) {
					if (stationDTO.getActiveFlag() == 1) {
						StationIO station = new StationIO();
						station.setName(stationDTO.getStation().getName());
						station.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stationDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						viaStation.add(station);
					}
				}

				tripIO.setViaStations(viaStation);
				tripList.add(tripIO);
			}
			// Sorting Trips
			Collections.sort(tripList, new Comparator<TripIO>() {
				@Override
				public int compare(TripIO t1, TripIO t2) {
					return new CompareToBuilder().append(t2.getTripStatus().getCode(), t1.getTripStatus().getCode()).append(t2.getAvailableSeatCount() > 0 ? 1 : 0, t1.getAvailableSeatCount() > 0 ? 1 : 0).append(t1.getFromStation().getDateTime(), t2.getFromStation().getDateTime()).toComparison();
				}
			});
		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/busmap/{tripCode}/{fromStationCode}/{toStationCode}/{travelDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TripIO> getBusmapV3(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("fromStationCode") String fromStationCode, @PathVariable("toStationCode") String toStationCode, @PathVariable("travelDate") String travelDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TripIO tripIO = new TripIO();
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
		logger.info("Getting the trip busmap for the stageCode: " + tripCode + " - " + travelDate + " - " + fromStationCode + " - " + toStationCode);

		boolean isAllowToBookBlockedSeat = false;
		// Permission check
		if (authDTO.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
			List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
			eventList.add(MenuEventEM.ALLOW_BLOCKED_SEAT_BOOKING);
			MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);
			isAllowToBookBlockedSeat = menuEventDTO.getEnabledFlag() == Numeric.ONE_INT;

			authDTO.getAdditionalAttribute().put(Text.ALLOW_BLOCKED_SEAT_BOOKING_FLAG, String.valueOf(menuEventDTO.getEnabledFlag()));
		}
		TripDTO tripDTO = busmapService.getSearchBusmapV3(authDTO, tripStageDTO);

		ScheduleIO schedule = new ScheduleIO();
		schedule.setCode(tripDTO.getSchedule().getCode());
		schedule.setName(tripDTO.getSchedule().getName());
		schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
		// schedule.setActiveFrom(tripDTO.getSchedule().getActiveFrom());
		// schedule.setActiveTo(tripDTO.getSchedule().getActiveTo());
		schedule.setDisplayName(tripDTO.getSchedule().getDisplayName());
		// schedule.setPnrStartCode(tripDTO.getSchedule().getPnrStartCode());
		// schedule.setMobileTicketFlag(tripDTO.getSchedule().getMobileTicketFlag());
		// schedule.setDayOfWeek(tripDTO.getSchedule().getDayOfWeek());
		// schedule.setBoardingReportingMinitues(tripDTO.getSchedule().getBoardingReportingMinitues());
		schedule.setServiceTax(tripDTO.getSchedule().getTax().getServiceTax());
		if (tripDTO.getSchedule().getTax() != null && tripDTO.getSchedule().getTax().getId() != 0) {
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

		// Trip Status
		TripStatusIO tripStatusIO = new TripStatusIO();
		tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
		tripStatusIO.setName(tripDTO.getTripStatus().getName());
		tripIO.setTripStatus(tripStatusIO);

		int Hours = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) / 60;
		int Minutes = (int) (tripDTO.getStage().getToStation().getMinitues() - tripDTO.getStage().getFromStation().getMinitues()) % 60;
		tripIO.setTravelTime(String.format("%02d", Hours) + " : " + String.format("%02d", Minutes));
		// Bus
		BusIO busIO = new BusIO();
		busIO.setCode(tripDTO.getBus().getCode());
		busIO.setName(tripDTO.getBus().getName());
		busIO.setBusType(BitsUtil.getBusCategoryUsingEM(tripDTO.getBus().getCategoryCode()));
		busIO.setCategoryCode(tripDTO.getBus().getCategoryCode() == null ? "" : tripDTO.getBus().getCategoryCode());
		busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
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
			layoutIO.setLayer(layoutDTO.getLayer());
			layoutIO.setOrientation(layoutDTO.getOrientation());
			layoutIO.setPassengerAge(layoutDTO.getPassengerAge());
			layoutIO.setContactNumber(layoutDTO.getContactNumber());
			layoutIO.setPassengerName(layoutDTO.getPassengerName());
			layoutIO.setBoardingPointName(layoutDTO.getBoardingPointName());
			layoutIO.setStationPoint(BitsUtil.getStationPointJSON(layoutDTO.getStationPoint()));
			layoutIO.setRemarks(layoutDTO.getRemarks());
			layoutIO.setUpdatedAt(DateUtil.convertDateTime(layoutDTO.getUpdatedAt()));
			layoutIO.setReleaseAt(DateUtil.convertDateTime(layoutDTO.getReleaseAt()));

			if (layoutDTO.getFromStation() != null && layoutDTO.getToStation() != null) {
				layoutIO.setRoute(layoutDTO.getFromStation().getName() + " - " + layoutDTO.getToStation().getName());
				layoutIO.setRoutes(getRoutes(layoutDTO.getFromStation(), layoutDTO.getToStation()));
			}
			BusSeatTypeIO seatTypeIO = new BusSeatTypeIO();
			seatTypeIO.setCode(layoutDTO.getBusSeatType().getCode());
			seatTypeIO.setName(layoutDTO.getBusSeatType().getName());
			layoutIO.setBusSeatType(seatTypeIO);

			layoutIO.setTicketCode(layoutDTO.getTicketCode());

			SeatGendarStatusIO seatGendarIO = new SeatGendarStatusIO();
			if (layoutDTO.getSeatGendar() != null) {
				seatGendarIO.setCode(layoutDTO.getSeatGendar().getCode());
				seatGendarIO.setName(layoutDTO.getSeatGendar().getName());
			}
			else if (layoutDTO.getSeatStatus() != null) {
				seatGendarIO.setCode(layoutDTO.getSeatStatus().getCode());
				seatGendarIO.setName(layoutDTO.getSeatStatus().getDescription());
			}
			layoutIO.setSeatGendarStatus(seatGendarIO);
			// User specifications
			UserIO userIO = new UserIO();
			GroupIO groupIO = new GroupIO();
			OrganizationIO organizationIO = new OrganizationIO();
			if (layoutDTO.getUser() != null && layoutDTO.getUser().getId() != 0 && StringUtil.isNotNull(layoutDTO.getUser().getCode())) {
				userIO.setCode(layoutDTO.getUser().getCode());
				userIO.setUsername(layoutDTO.getUser().getUsername());
				userIO.setEmail(layoutDTO.getUser().getEmail());
				userIO.setMobile(layoutDTO.getUser().getMobile());
				if (layoutDTO.getUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
					userIO.setName("Online");
				}
				else if (layoutDTO.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId() || layoutDTO.getUser().getUserRole().getId() == UserRoleEM.TABLET_POB_ROLE.getId() || layoutDTO.getUser().getUserRole().getId() == UserRoleEM.DRIVER.getId()) {
					userIO.setName(layoutDTO.getUser().getName());
				}
				RoleIO roleIO = new RoleIO();
				roleIO.setCode(layoutDTO.getUser().getUserRole().getCode());
				roleIO.setName(layoutDTO.getUser().getUserRole().getName());
				userIO.setRole(roleIO);
			}
			if (layoutDTO.getGroup() != null) {
				groupIO.setCode(layoutDTO.getGroup().getCode());
				groupIO.setName(layoutDTO.getGroup().getName());
				groupIO.setDecription(layoutDTO.getGroup().getDecription());
				groupIO.setLevel(layoutDTO.getGroup().getLevel());
			}
			else if (layoutDTO.getUser() != null && layoutDTO.getUser().getGroup() != null) {
				groupIO.setCode(layoutDTO.getUser().getGroup().getCode());
				groupIO.setName(layoutDTO.getUser().getGroup().getName());
				groupIO.setLevel(layoutDTO.getUser().getGroup().getLevel());
				groupIO.setDecription(layoutDTO.getUser().getGroup().getDecription());
			}
			else if (layoutDTO.getOrganization() != null && StringUtil.isNotNull(layoutDTO.getOrganization().getCode())) {
				organizationIO.setCode(layoutDTO.getOrganization().getCode());
				organizationIO.setName(layoutDTO.getOrganization().getName());
			}

			layoutIO.setUser(userIO);
			layoutIO.setGroup(groupIO);
			layoutIO.setOrganization(organizationIO);

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

			StageFareDTO stageFareDTO = fareMap.get(authDTO.getUser().getGroup().getId() + layoutDTO.getBusSeatType().getCode()) != null ? fareMap.get(authDTO.getUser().getGroup().getId() + layoutDTO.getBusSeatType().getCode()) : fareMap.get(layoutDTO.getBusSeatType().getCode()) != null ? fareMap.get(layoutDTO.getBusSeatType().getCode()) : null;
			if (stageFareDTO != null) {
				// Stage Fare and Schedule Seat Fare included
				layoutIO.setSeatFare(layoutDTO.getFare() != null ? layoutDTO.getFare() : stageFareDTO.getFare());
				layoutIO.setDiscountFare(stageFareDTO.getDiscountFare());
				if (layoutDTO.getDiscountFare().compareTo(BigDecimal.ZERO) > 0) {
					layoutIO.setDiscountFare(layoutDTO.getDiscountFare());
				}
			}
			else {
				layoutIO.setSeatFare(BigDecimal.ZERO);
				layoutIO.setDiscountFare(BigDecimal.ZERO);
			}

			if (tripDTO.getSchedule().getTax().getServiceTax().doubleValue() != 0 && layoutIO.getSeatFare().doubleValue() != 0) {
				layoutIO.setServiceTax(layoutIO.getSeatFare().divide(new BigDecimal(100), 2, RoundingMode.CEILING).multiply(tripDTO.getSchedule().getTax().getServiceTax()));
			}
			else {
				layoutIO.setServiceTax(BigDecimal.ZERO);
			}
			// Set seat status Blocked id Fare amount is Zero
			if (layoutIO.getSeatFare().doubleValue() <= 0 && (layoutDTO.getSeatStatus().getId() != SeatStatusEM.BOOKED.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.PHONE_BLOCKED.getId())) {
				seatStatusIO.setCode(SeatStatusEM.BLOCKED.getCode());
				seatStatusIO.setName(SeatStatusEM.BLOCKED.getDescription());
				layoutIO.setSeatStatus(seatStatusIO);
				layoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
			}
			layoutIO.setActiveFlag(layoutDTO.getActiveFlag());
			if (layoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_YOU || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_ALL || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_MALE || layoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_FEMALE) {
				availableMap.put(layoutDTO.getBusSeatType().getCode(), availableMap.get(layoutDTO.getBusSeatType().getCode()) == null ? 0 : availableMap.get(layoutDTO.getBusSeatType().getCode()) + 1);
			}
			if (layoutDTO.getSeatStatus().getId() == SeatStatusEM.BOOKED.getId() || (SeatStatusEM.BLOCKED.getId() == layoutDTO.getSeatStatus().getId() && (!isAllowToBookBlockedSeat || !layoutDTO.isAllowBlockedSeatBooking())) || layoutDTO.getSeatStatus().getId() == SeatStatusEM.PHONE_BLOCKED.getId()) {
				layoutIO.setSeatFare(layoutDTO.getFare());
				layoutIO.setServiceTax(BigDecimal.ZERO);
				layoutIO.setDiscountFare(BigDecimal.ZERO);
			}
			seatLayoutList.add(layoutIO);
		}
		busIO.setSeatLayoutList(seatLayoutList);
		tripIO.setBus(busIO);

		tripIO.setSyncTime(StringUtil.isNotNull(tripDTO.getSyncTime()) ? tripDTO.getSyncTime() : Text.NA);
		tripIO.setTripCode(tripDTO.getCode());
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
				stageFareIO.setDiscountFare(fareDTO.getDiscountFare());
				stageFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
				stageFareIO.setSeatName(fareDTO.getBusSeatType().getName());
				if (fareDTO.getGroup() != null) {
					stageFareIO.setGroupName(fareDTO.getGroup().getName());
				}
				if (availableMap.get(fareDTO.getBusSeatType().getCode()) != null) {
					stageFareIO.setAvailableSeatCount(availableMap.get(fareDTO.getBusSeatType().getCode()));
				}
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
				pointIO.setFare(pointDTO.getFare());

				List<BaseIO> amenitiesList = new ArrayList<>();
				for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(pointDTO.getAmenities())) {
					BaseIO amenitiesIO = new BaseIO();
					amenitiesIO.setCode(amenitiesEM.getCode());
					amenitiesIO.setName(amenitiesEM.getName());
					amenitiesList.add(amenitiesIO);
				}
				pointIO.setAmenities(amenitiesList);
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
				pointIO.setFare(pointDTO.getFare());

				List<BaseIO> amenitiesList = new ArrayList<>();
				for (StationPointAmenitiesEM amenitiesEM : StationPointAmenitiesEM.getStationPointAmenitiesFromCodes(pointDTO.getAmenities())) {
					BaseIO amenitiesIO = new BaseIO();
					amenitiesIO.setCode(amenitiesEM.getCode());
					amenitiesIO.setName(amenitiesEM.getName());
					amenitiesList.add(amenitiesIO);
				}
				pointIO.setAmenities(amenitiesList);
				toStationPoint.add(pointIO);
			}

			List<ScheduleDiscountIO> discountList = new ArrayList<>();
			if (tripDTO.getSchedule().getScheduleDiscount() != null && tripDTO.getSchedule().getScheduleDiscount().getList() != null) {
				for (ScheduleDiscountDTO discountDTO : tripDTO.getSchedule().getScheduleDiscount().getList()) {
					ScheduleDiscountIO discountIO = new ScheduleDiscountIO();
					discountIO.setCode(discountDTO.getCode());
					discountIO.setName(discountDTO.getName());
					discountIO.setDiscountValue(discountDTO.getDiscountValue());
					discountIO.setPercentageFlag(discountDTO.getPercentageFlag());
					discountIO.setFemaleDiscountFlag(discountDTO.getFemaleDiscountFlag());
					discountIO.setAdvanceBookingDays(discountDTO.getAdvanceBookingDays());
					discountIO.setActiveFlag(Numeric.ONE_INT);
					discountList.add(discountIO);
				}
			}
			tripIO.setDiscountList(discountList);

			// Sorting
			Comparator<StationPointIO> comp = new BeanComparator("dateTime");
			Collections.sort(fromStationPoint, comp);
			Collections.sort(toStationPoint, comp);
			fromStation.setStationPoint(fromStationPoint);
			toStation.setStationPoint(toStationPoint);

			tripIO.setFromStation(fromStation);
			tripIO.setToStation(toStation);
		}
		return ResponseIO.success(tripIO);
	}

	@RequestMapping(value = "/trip/{tripCode}/booked/ticket", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, Object>> getTripBooketTickets(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		Map<String, Object> tickets = new HashMap<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		List<TicketDTO> bookedTicketList = busmapService.getBookedBlockedTickets(authDTO, tripDTO);

		tickets.put("syncTime", tripDTO.getSyncTime());

		JSONArray ticketList = new JSONArray();
		for (TicketDTO ticketDTO : bookedTicketList) {
			JSONObject ticket = new JSONObject();
			ticket.put("pmn", ticketDTO.getPassengerMobile());
			ticket.put("c", ticketDTO.getCode());
			ticket.put("tat", DateUtil.convertDateTime(ticketDTO.getTicketAt()));
			ticket.put("tut", DateUtil.convertDateTime(ticketDTO.getUpdatedAt()));
			ticket.put("trt", ticketDTO.getTicketExtra() != null ? DateUtil.convertDateTime(ticketDTO.getTicketExtra().getReleaseAt()) : Text.EMPTY);
			ticket.put("jt", ticketDTO.getTicketExtra() != null ? ticketDTO.getTicketExtra().getTicketEditDetails() + ticketDTO.getTicketExtra().getTicketAfterTripTime() : "0000000");
			ticket.put("otc", ticketDTO.getTicketExtra() != null ? ticketDTO.getTicketExtra().getOfflineTicketCode() : Text.NA);
			ticket.put("ttat", ticketDTO.getScheduleTicketTransferTerms() != null && ticketDTO.getScheduleTicketTransferTerms().getDateTime() != null ? DateUtil.convertDateTime(ticketDTO.getScheduleTicketTransferTerms().getDateTime()) : Text.NA);

			JSONObject user = new JSONObject();
			user.put("c", ticketDTO.getTicketUser().getCode());
			user.put("n", ticketDTO.getTicketUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId() ? "Online" : ticketDTO.getTicketUser().getName());

			JSONObject group = new JSONObject();
			group.put("c", ticketDTO.getTicketUser().getGroup().getCode());
			group.put("n", ticketDTO.getTicketUser().getGroup().getName());
			group.put("l", ticketDTO.getTicketUser().getGroup().getLevel());
			user.put("gr", group);
			ticket.put("ur", user);

			JSONObject fromStation = new JSONObject();
			fromStation.put("c", ticketDTO.getFromStation().getCode());
			fromStation.put("n", ticketDTO.getFromStation().getName());

			JSONObject toStation = new JSONObject();
			toStation.put("c", ticketDTO.getToStation().getCode());
			toStation.put("n", ticketDTO.getToStation().getName());

			if (ticketDTO.getBoardingPoint() != null && ticketDTO.getDroppingPoint() != null) {
				JSONObject boardingPoint = new JSONObject();
				boardingPoint.put("c", ticketDTO.getBoardingPoint().getCode());
				boardingPoint.put("n", ticketDTO.getBoardingPoint().getName());
				boardingPoint.put("d", DateUtil.convertDateTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getBoardingPoint().getMinitues())));
				fromStation.put("sp", boardingPoint);

				JSONObject droppingPoint = new JSONObject();
				droppingPoint.put("c", ticketDTO.getDroppingPoint().getCode());
				droppingPoint.put("n", ticketDTO.getDroppingPoint().getName());
				droppingPoint.put("d", DateUtil.convertDateTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getDroppingPoint().getMinitues())));
				toStation.put("sp", droppingPoint);
			}
			ticket.put("fs", fromStation);
			ticket.put("ts", toStation);

			JSONArray ticketDetailsList = new JSONArray();
			for (TicketDetailsDTO dto : ticketDTO.getTicketDetails()) {
				JSONObject tickDetails = new JSONObject();
				tickDetails.put("sn", dto.getSeatName());
				tickDetails.put("sc", dto.getSeatCode());

				JSONObject ticketStatus = new JSONObject();
				ticketStatus.put("c", dto.getTicketStatus().getCode());
				ticketStatus.put("n", dto.getTicketStatus().getDescription());
				tickDetails.put("ss", ticketStatus);

				tickDetails.put("pn", dto.getPassengerName());
				tickDetails.put("pa", dto.getPassengerAge());
				tickDetails.put("sg", dto.getSeatGendar().getCode());
				tickDetails.put("sf", dto.getSeatFare());
				tickDetails.put("jt", dto.getTicketExtra() != null ? dto.getTicketExtra().getTicketEditDetails() : "000000");
				tickDetails.put("tls", dto.getTicketExtra() != null && dto.getTicketExtra().getTravelStatus() != null ? dto.getTicketExtra().getTravelStatus().getCode() : TravelStatusEM.YET_BOARD);
				ticketDetailsList.add(tickDetails);
			}
			ticket.put("tck", ticketDetailsList);
			ticketList.add(ticket);
		}
		tickets.put("tickets", ticketList);
		return ResponseIO.success(tickets);
	}

	@RequestMapping(value = "/payment/gateway/options", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<PaymentModeIO>> getActivePaymentGateway(@PathVariable("authtoken") String authtoken) throws Exception {
		List<PaymentModeIO> paymentModeList = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<PaymentGatewayScheduleDTO> list = gatewayScheduleService.getActiveSchedulePaymentGateway(authDTO, OrderTypeEM.TICKET);
		if (!list.isEmpty()) {
			Map<String, List<PaymentGatewayPartnerIO>> mapList = new HashMap<>();
			Map<String, PaymentGatewayPartnerDTO> partnerMAP = new HashMap<>();
			Map<String, PaymentModeDTO> modeMAP = new HashMap<String, PaymentModeDTO>();

			for (PaymentGatewayScheduleDTO scheduleDTO : list) {
				scheduleDTO.getGatewayPartner().getPaymentMode().setActiveFlag(scheduleDTO.getPrecedence());
				if (mapList.get(scheduleDTO.getGatewayPartner().getPaymentMode().getCode()) == null) {
					mapList.put(scheduleDTO.getGatewayPartner().getPaymentMode().getCode(), new ArrayList<PaymentGatewayPartnerIO>());
				}
				PaymentGatewayPartnerIO partnerIO = new PaymentGatewayPartnerIO();
				partnerIO.setCode(scheduleDTO.getGatewayPartner().getCode());
				partnerIO.setName(scheduleDTO.getGatewayPartner().getName());
				partnerIO.setOfferNotes(scheduleDTO.getGatewayPartner().getOfferNotes());
				partnerIO.setOfferTerms(scheduleDTO.getGatewayPartner().getOfferTerms());
				partnerIO.setServiceCharge(scheduleDTO.getServiceCharge());
				partnerIO.setActiveFlag(scheduleDTO.getPrecedence());

				List<PaymentGatewayPartnerIO> partnerList = mapList.get(scheduleDTO.getGatewayPartner().getPaymentMode().getCode());
				partnerList.add(partnerIO);
				mapList.put(scheduleDTO.getGatewayPartner().getPaymentMode().getCode(), partnerList);
				partnerMAP.put(scheduleDTO.getGatewayPartner().getPaymentMode().getCode(), scheduleDTO.getGatewayPartner());
				modeMAP.put(scheduleDTO.getGatewayPartner().getPaymentMode().getCode(), scheduleDTO.getGatewayPartner().getPaymentMode());
			}
			List<PaymentModeDTO> modelist = new ArrayList<PaymentModeDTO>(modeMAP.values());
			// Sorting
			Collections.sort(modelist, new Comparator<PaymentModeDTO>() {
				@Override
				public int compare(PaymentModeDTO t1, PaymentModeDTO t2) {
					return new CompareToBuilder().append(t1.getActiveFlag(), t2.getActiveFlag()).toComparison();
				}
			});
			// Sorting
			Comparator<PaymentGatewayPartnerIO> comp = new BeanComparator("activeFlag");
			for (PaymentModeDTO paymentMode : modelist) {
				PaymentModeIO modeIO = new PaymentModeIO();
				modeIO.setCode(partnerMAP.get(paymentMode.getCode()).getPaymentMode().getCode());
				modeIO.setName(partnerMAP.get(paymentMode.getCode()).getPaymentMode().getName());
				List<PaymentGatewayPartnerIO> partnerList = mapList.get(paymentMode.getCode());
				Collections.sort(partnerList, comp);
				modeIO.setPaymentGatewayPartner(partnerList);
				paymentModeList.add(modeIO);
			}
		}
		return ResponseIO.success(paymentModeList);
	}

	@RequestMapping(value = "/addons/discount/{couponCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AddonsDiscountIO> getAddonsDiscount(@PathVariable("authtoken") String authtoken, @PathVariable("couponCode") String couponCode, @RequestBody OrderIO orderIO) throws Exception {
		AddonsDiscountIO discount = new AddonsDiscountIO();
		if (StringUtil.isNull(couponCode)) {
			throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
		}
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ValidateBlockingDetails(authDTO, orderIO);
		BookingDTO bookingDTO = new BookingDTO();
		for (OrderDetailsIO orderDetails : orderIO.getOrderDetails()) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setPassengerMobile(orderIO.getMobileNumber());
			ticketDTO.setPassengerEmailId(orderIO.getEmailId());
			StageDTO stageDTO = new StageDTO();
			stageDTO.setCode(orderDetails.getTripStageCode());
			TripDTO tripDTO = new TripDTO();
			tripDTO.setStage(stageDTO);
			ticketDTO.setTripDTO(tripDTO);
			ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(orderDetails.getJourneyType()));
			ticketDTO.setDeviceMedium(authDTO.getDeviceMedium());

			List<TicketDetailsDTO> passengerDetails = new ArrayList<TicketDetailsDTO>();
			for (TicketDetailsIO passDetails : orderDetails.getTicketDetails()) {
				TicketDetailsDTO tdDTO = new TicketDetailsDTO();
				tdDTO.setSeatCode(passDetails.getSeatCode());
				passengerDetails.add(tdDTO);
			}
			ticketDTO.setTicketDetails(passengerDetails);

			bookingDTO.addTicketDTO(ticketDTO);
		}
		// if round trip, common booking Code for both ticket
		if (bookingDTO.getTicketList().size() > 1) {
			bookingDTO.setRoundTripFlag(true);
		}
		bookingDTO.setCouponCode(couponCode);
		bookingDTO.setCode(orderIO.getCode());
		bookingDTO.setAdditionalAttributes(orderIO.getAdditionalAttributes() != null ? orderIO.getAdditionalAttributes() : new HashMap<String, String>());

		DiscountCriteriaDTO discountCriteriaDTO = discountService.validateCouponCode(authDTO, bookingDTO);
		if (discountCriteriaDTO != null) {
			discount.setValue(discountCriteriaDTO.getValue());
			if (!discountCriteriaDTO.isPercentageFlag()) {
				discount.setValue(discountCriteriaDTO.getValue() * bookingDTO.getTicketSeatCount());
			}
			if (!discountCriteriaDTO.isPercentageFlag() && discountCriteriaDTO.getMaxDiscountAmount() != 0 && discountCriteriaDTO.getMaxDiscountAmount() <= discount.getValue()) {
				discount.setValue(discountCriteriaDTO.getMaxDiscountAmount());
			}
			discount.setMaxValue(discountCriteriaDTO.getMaxDiscountAmount());
			discount.setPercentageFlag(discountCriteriaDTO.isPercentageFlag());
			discount.setMessage(discountCriteriaDTO.getDiscountCoupon().getActiveDesription());
		}
		else {
			throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
		}
		return ResponseIO.success(discount);
	}

	@RequestMapping(value = "/addonsV3/discount/{couponCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AddonsDiscountIO> getAddonsDiscountV3(@PathVariable("authtoken") String authtoken, @PathVariable("couponCode") String couponCode, @RequestBody OrderV3IO orderIO) throws Exception {
		AddonsDiscountIO discount = new AddonsDiscountIO();
		if (StringUtil.isNull(couponCode)) {
			throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
		}
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ValidateBlockingDetailsV3(authDTO, orderIO);
		BookingDTO bookingDTO = new BookingDTO();
		for (OrderDetailsV3IO orderDetails : orderIO.getOrderDetails()) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setPassengerMobile(orderIO.getMobileNumber());
			ticketDTO.setPassengerEmailId(orderIO.getEmailId());

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(orderDetails.getTripCode());
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
			ticketDTO.setTripDTO(tripDTO);
			ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(orderDetails.getJourneyType()));
			ticketDTO.setDeviceMedium(authDTO.getDeviceMedium());

			List<TicketDetailsDTO> passengerDetails = new ArrayList<TicketDetailsDTO>();
			for (TicketDetailsIO passDetails : orderDetails.getTicketDetails()) {
				TicketDetailsDTO tdDTO = new TicketDetailsDTO();
				tdDTO.setSeatCode(passDetails.getSeatCode());
				tdDTO.setSeatGendar(StringUtil.isNotNull(passDetails.getPassengerGendar()) ? SeatGendarEM.getSeatGendarEM(passDetails.getPassengerGendar()) : null);
				tdDTO.setPassengerAge(passDetails.getPassengerAge());
				passengerDetails.add(tdDTO);
			}
			ticketDTO.setTicketDetails(passengerDetails);

			bookingDTO.addTicketDTO(ticketDTO);
		}
		// if round trip, common booking Code for both ticket
		if (bookingDTO.getTicketList().size() > 1) {
			bookingDTO.setRoundTripFlag(true);
		}
		bookingDTO.setCouponCode(couponCode);
		bookingDTO.setCode(orderIO.getCode());
		bookingDTO.setAdditionalAttributes(orderIO.getAdditionalAttributes() != null ? orderIO.getAdditionalAttributes() : new HashMap<String, String>());

		DiscountCriteriaDTO discountCriteriaDTO = discountService.validateCouponCodeV3(authDTO, bookingDTO);
		if (discountCriteriaDTO != null) {
			discount.setMaxValue(discountCriteriaDTO.getMaxDiscountAmount());
			discount.setValue(discountCriteriaDTO.getValue());
			discount.setPercentageFlag(discountCriteriaDTO.isPercentageFlag());
			discount.setMessage(discountCriteriaDTO.getDiscountCoupon().getActiveDesription());
		}
		else {
			throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
		}
		return ResponseIO.success(discount);
	}

	@RequestMapping(value = "/wallet/redeem", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, Object>> getWalletCashCouponDiscount(@PathVariable("authtoken") String authtoken, @RequestBody OrderV3IO orderIO) throws Exception {
		Map<String, Object> order = new HashMap<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (WalletAccessEM.getWalletAccessEM(authDTO.getNamespaceCode()) == null) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		if (authDTO != null && authDTO.getUserCustomer() != null && authDTO.getUserCustomer().getId() != 0) {
			ValidateBlockingDetailsV3(authDTO, orderIO);
			BookingDTO bookingDTO = new BookingDTO();
			for (OrderDetailsV3IO orderDetails : orderIO.getOrderDetails()) {
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setPassengerMobile(orderIO.getMobileNumber());
				ticketDTO.setPassengerEmailId(orderIO.getEmailId());

				TripDTO tripDTO = new TripDTO();
				tripDTO.setCode(orderDetails.getTripCode());

				SearchDTO searchDTO = new SearchDTO();
				DateTime tripTravelDate = new DateTime(orderDetails.getTravelDate());
				searchDTO.setTravelDate(tripTravelDate);

				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setCode(orderDetails.getFromStation().getCode());
				searchDTO.setFromStation(fromStationDTO);

				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setCode(orderDetails.getToStation().getCode());
				searchDTO.setToStation(toStationDTO);

				tripDTO.setSearch(searchDTO);

				ticketDTO.setTripDTO(tripDTO);
				ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(orderDetails.getJourneyType()));
				ticketDTO.setDeviceMedium(authDTO.getDeviceMedium());

				List<TicketDetailsDTO> passengerDetails = new ArrayList<TicketDetailsDTO>();
				for (TicketDetailsIO passDetails : orderDetails.getTicketDetails()) {
					TicketDetailsDTO tdDTO = new TicketDetailsDTO();
					tdDTO.setSeatCode(passDetails.getSeatCode());
					passengerDetails.add(tdDTO);
				}
				ticketDTO.setTicketDetails(passengerDetails);

				bookingDTO.addTicketDTO(ticketDTO);
			}
			// if round trip, common booking Code for both ticket
			if (bookingDTO.getTicketList().size() > 1) {
				bookingDTO.setRoundTripFlag(true);
			}
			bookingDTO.setAdditionalAttributes(orderIO.getAdditionalAttributes() != null ? orderIO.getAdditionalAttributes() : new HashMap<String, String>());

			order = walletService.validateWalletCoupon(authDTO, bookingDTO);
		}
		return ResponseIO.success(order);
	}

	@RequestMapping(value = "/addonsV3/discount/offline", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<AddonsDiscountIO> getAddonsDiscountOffline(@PathVariable("authtoken") String authtoken, @RequestBody OrderV3IO orderIO) throws Exception {
		AddonsDiscountIO discount = new AddonsDiscountIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ValidateBlockingDetailsV4(authDTO, orderIO);
		BookingDTO bookingDTO = new BookingDTO();
		for (OrderDetailsV3IO orderDetails : orderIO.getOrderDetails()) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setPassengerMobile(orderIO.getMobileNumber());
			ticketDTO.setPassengerEmailId(orderIO.getEmailId());

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(orderDetails.getTripCode());
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
			ticketDTO.setTripDTO(tripDTO);
			ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(orderDetails.getJourneyType()));
			ticketDTO.setDeviceMedium(authDTO.getDeviceMedium());

			List<TicketDetailsDTO> passengerDetails = new ArrayList<TicketDetailsDTO>();
			for (TicketDetailsIO passDetails : orderDetails.getTicketDetails()) {
				TicketDetailsDTO tdDTO = new TicketDetailsDTO();
				tdDTO.setSeatCode(passDetails.getSeatCode());
				tdDTO.setSeatFare(passDetails.getSeatFare());
				passengerDetails.add(tdDTO);
			}
			ticketDTO.setTicketDetails(passengerDetails);

			bookingDTO.addTicketDTO(ticketDTO);
		}
		// if round trip, common booking Code for both ticket
		if (bookingDTO.getTicketList().size() > 1) {
			bookingDTO.setRoundTripFlag(true);
		}
		AddonsDiscountOfflineDTO discountOfflineDTO = discountOfflineService.getAvailableDiscountOffline(authDTO, bookingDTO);
		if (discountOfflineDTO != null) {
			discount.setCode(discountOfflineDTO.getCode());
			discount.setMaxValue(discountOfflineDTO.getMaxDiscountAmount().doubleValue());
			discount.setValue(discountOfflineDTO.getValue().doubleValue());
			discount.setPercentageFlag(discountOfflineDTO.isPercentageFlag());
			discount.setName(discountOfflineDTO.getName());
		}
		else {
			throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
		}
		return ResponseIO.success(discount);
	}

	@RequestMapping(value = "/addons/discount", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<DiscountCriteriaIO>> getAllAddonsDiscount(@PathVariable("authtoken") String authtoken) throws Exception {
		List<DiscountCriteriaIO> discount = new ArrayList<DiscountCriteriaIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<DiscountCriteriaDTO> list = discountService.getAllAvailableDiscountOfferPage(authDTO);
		for (DiscountCriteriaDTO criteriaDTO : list) {
			DiscountCriteriaIO discountio = new DiscountCriteriaIO();
			discountio.setCode(criteriaDTO.getCode());
			DiscountCouponIO discountCouponIO = new DiscountCouponIO();
			discountCouponIO.setCode(criteriaDTO.getDiscountCoupon().getCode());
			discountCouponIO.setCoupon(criteriaDTO.getDiscountCoupon().getCoupon());
			discountio.setDiscountCoupon(discountCouponIO);

			List<GroupIO> groupList = new ArrayList<GroupIO>();
			for (GroupDTO groupDTO : criteriaDTO.getGroupList()) {
				GroupIO groupIO = new GroupIO();
				groupIO.setCode(groupDTO.getCode());
				groupIO.setName(groupDTO.getName());
				groupIO.setLevel(groupDTO.getLevel());
				groupList.add(groupIO);
			}
			discountio.setUserGroup(groupList);
			discountio.setActiveFrom(criteriaDTO.getActiveFrom());
			discountio.setActiveTo(criteriaDTO.getActiveTo());
			discountio.setDayOfWeek(criteriaDTO.getDayOfWeek());
			discountio.setValue(criteriaDTO.getValue());
			discountio.setPercentageFlag(criteriaDTO.isPercentageFlag());
			discountio.setTravelDateFlag(criteriaDTO.isTravelDateFlag());
			discountio.setShowOfferPageFlag(criteriaDTO.isShowOfferPageFlag());
			discountio.setMaxUsageLimitPerUser(criteriaDTO.getMaxUsageLimitPerUser());
			discountio.setDeviceMedium(criteriaDTO.getDeviceMediumCodes());
			discountio.setBeforeBookingMinitues(criteriaDTO.getBeforeBookingMinitues());
			discountio.setAfterBookingMinitues(criteriaDTO.getAfterBookingMinitues());
			discountio.setMinTicketFare(criteriaDTO.getMinTicketFare());
			discountio.setMaxDiscountAmount(criteriaDTO.getMaxDiscountAmount());
			discountio.setMinSeatCount(criteriaDTO.getMinSeatCount());
			discountio.setAge(criteriaDTO.getAge());
			discountio.setMobileNumber(criteriaDTO.getMobileNumber());
			discountio.setServiceTiming(criteriaDTO.getServiceTiming());
			discountio.setScheduleCode(criteriaDTO.getScheduleCode());
			discountio.setRouteCode(criteriaDTO.getRouteCode());
			discountio.setActiveFlag(criteriaDTO.getActiveFlag());
			discount.add(discountio);
		}
		return ResponseIO.success(discount);
	}

	@RequestMapping(value = "/terms/{tripCode}/{fromStationCode}/{toStationCode}/{travelDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<CancellationTermIO> getCancellationTermByTripCode(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("fromStationCode") String fromStationCode, @PathVariable("toStationCode") String toStationCode, @PathVariable("travelDate") String travelDate) throws Exception {
		CancellationTermIO cancellationTermIO = new CancellationTermIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
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

		CancellationTermDTO cancellationTermDTO = cancellationTermsService.getCancellationTermsByTripDTO(authDTO, authDTO.getUser(), tripDTO);
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

		cancellationTermIO.setName(cancellationTermDTO.getName());
		cancellationTermIO.setCode(cancellationTermDTO.getCode());
		cancellationTermIO.setActiveFlag(cancellationTermDTO.getActiveFlag());
		List<CancellationPolicyIO> policyIOs = new ArrayList<CancellationPolicyIO>();
		for (CancellationPolicyDTO policyDTO : cancellationTermDTO.getPolicyList()) {
			CancellationPolicyIO policyIO = new CancellationPolicyIO();
			policyIO.setFromValue(policyDTO.getFromValue());
			policyIO.setToValue(policyDTO.getToValue());
			policyIO.setDeductionAmount(policyDTO.getDeductionValue());
			policyIO.setPercentageFlag(policyDTO.getPercentageFlag());
			policyIO.setPolicyPattern(policyDTO.getPolicyPattern());

			policyIO.setTerm(policyDTO.getTerm());
			policyIO.setDeductionAmountTxt(policyDTO.getDeductionAmountTxt());
			policyIO.setRefundAmountTxt(policyDTO.getRefundAmountTxt());
			policyIO.setChargesTxt(policyDTO.getChargesTxt());
			policyIOs.add(policyIO);
		}
		cancellationTermIO.setPolicyList(policyIOs);
		return ResponseIO.success(cancellationTermIO);
	}

	@RequestMapping(value = "/terms", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TermIO>> getTermsAndConditions(@PathVariable("authtoken") String authtoken) throws Exception {
		List<TermIO> termIOs = new ArrayList<TermIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		String tagValue = "general-terms";
		List<TermDTO> list = (List<TermDTO>) termsService.getTermsAndConditions(authDTO, tagValue);
		for (TermDTO dto : list) {
			TermIO termIO = new TermIO();
			termIO.setName(dto.getName());
			termIO.setCode(dto.getCode());
			termIO.setActiveFlag(dto.getActiveFlag());
			termIO.setSequence(dto.getSequenceId());
			List<String> tagList = new ArrayList<String>();
			if (dto.getTagList() != null && !dto.getTagList().isEmpty()) {
				for (String tag : dto.getTagList()) {
					tagList.add(tag);
				}
			}
			termIO.setTagList(tagList);
			termIOs.add(termIO);
		}
		return ResponseIO.success(termIOs);
	}

	@RequestMapping(value = "/schedule", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ScheduleIO>> getAllSchedule(@PathVariable("authtoken") String authtoken) throws Exception {
		List<ScheduleIO> PrerequisitesList = new ArrayList<ScheduleIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<ScheduleDTO> list = scheduleService.getActive(authDTO, DateUtil.NOW());
		for (ScheduleDTO scheduleDTO : list) {
			ScheduleIO scheduleio = new ScheduleIO();
			scheduleio.setCode(scheduleDTO.getCode());
			scheduleio.setName(scheduleDTO.getName());
			scheduleio.setServiceNumber(scheduleDTO.getServiceNumber());
			// scheduleio.setActiveFrom(scheduleDTO.getActiveFrom());
			// scheduleio.setActiveTo(scheduleDTO.getActiveTo());
			scheduleio.setDisplayName(scheduleDTO.getDisplayName());
			// scheduleio.setPnrStartCode(scheduleDTO.getPnrStartCode());
			// scheduleio.setMobileTicketFlag(scheduleDTO.getMobileTicketFlag());
			// scheduleio.setDayOfWeek(scheduleDTO.getDayOfWeek());
			// scheduleio.setBoardingReportingMinitues(scheduleDTO.getBoardingReportingMinitues());

			BusIO busIO = new BusIO();
			busIO.setCategoryCode(scheduleDTO.getScheduleBus().getBus().getCategoryCode());
			busIO.setDisplayName(scheduleDTO.getScheduleBus().getBus().getDisplayName());
			busIO.setName(scheduleDTO.getScheduleBus().getBus().getName());
			busIO.setTotalSeatCount(scheduleDTO.getScheduleBus().getBus().getReservableLayoutSeatCount());
			scheduleio.setBus(busIO);

			List<StageIO> stageList = new ArrayList<>();
			// Stage
			Map<String, Integer> sequenceMap = new HashMap<>();
			if (scheduleDTO.getStageList() != null) {
				for (StageDTO stageDTO : scheduleDTO.getStageList()) {
					if (sequenceMap.get("MIN") == null || sequenceMap.get("MIN") >= stageDTO.getFromStation().getStationSequence()) {
						sequenceMap.put("MIN", stageDTO.getFromStation().getStationSequence());
					}
					if (sequenceMap.get("MAX") == null || sequenceMap.get("MAX") <= stageDTO.getToStation().getStationSequence()) {
						sequenceMap.put("MAX", stageDTO.getToStation().getStationSequence());
					}
				}
			}
			if (scheduleDTO.getStageList() != null) {
				for (StageDTO stageDTO : scheduleDTO.getStageList()) {
					StageIO stageIO = new StageIO();
					org.in.com.controller.commerce.io.StationIO fromStation = new org.in.com.controller.commerce.io.StationIO();
					org.in.com.controller.commerce.io.StationIO toStation = new org.in.com.controller.commerce.io.StationIO();
					fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
					fromStation.setName(stageDTO.getFromStation().getStation().getName());
					fromStation.setDateTime("" + stageDTO.getFromStation().getMinitues());
					toStation.setCode(stageDTO.getToStation().getStation().getCode());
					toStation.setName(stageDTO.getToStation().getStation().getName());
					toStation.setDateTime("" + stageDTO.getToStation().getMinitues());
					List<StageFareIO> stageFareList = new ArrayList<>();
					for (StageFareDTO fareDTO : stageDTO.getStageFare()) {
						StageFareIO stageFareIO = new StageFareIO();
						stageFareIO.setFare(fareDTO.getFare());
						stageFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
						stageFareIO.setSeatName(fareDTO.getBusSeatType().getName());
						if (fareDTO.getGroup() != null) {
							stageFareIO.setGroupName(fareDTO.getGroup().getName());
						}
						stageFareList.add(stageFareIO);
					}
					stageIO.setStageFare(stageFareList);
					List<org.in.com.controller.commerce.io.StationPointIO> fromStationPoint = new ArrayList<>();
					for (StationPointDTO pointDTO : stageDTO.getFromStation().getStationPoint()) {
						org.in.com.controller.commerce.io.StationPointIO pointIO = new org.in.com.controller.commerce.io.StationPointIO();
						pointIO.setDateTime("" + (stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()));
						pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
						pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
						pointIO.setCode(pointDTO.getCode());
						pointIO.setName(pointDTO.getName());
						pointIO.setLandmark(pointDTO.getLandmark());
						pointIO.setAddress(pointDTO.getAddress());
						pointIO.setNumber(pointDTO.getNumber());
						fromStationPoint.add(pointIO);
					}
					List<org.in.com.controller.commerce.io.StationPointIO> toStationPoint = new ArrayList<>();
					for (StationPointDTO pointDTO : stageDTO.getToStation().getStationPoint()) {
						org.in.com.controller.commerce.io.StationPointIO pointIO = new org.in.com.controller.commerce.io.StationPointIO();
						pointIO.setDateTime("" + (stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()));
						pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
						pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
						pointIO.setCode(pointDTO.getCode());
						pointIO.setName(pointDTO.getName());
						pointIO.setLandmark(pointDTO.getLandmark());
						pointIO.setAddress(pointDTO.getAddress());
						pointIO.setNumber(pointDTO.getNumber());
						toStationPoint.add(pointIO);
					}
					if (sequenceMap.get("MIN") == stageDTO.getFromStation().getStationSequence() && sequenceMap.get("MAX") == stageDTO.getToStation().getStationSequence()) {
						stageIO.setStageSequence(1);
					}
					fromStation.setStationPoint(fromStationPoint);
					toStation.setStationPoint(toStationPoint);
					stageIO.setFromStation(fromStation);
					stageIO.setToStation(toStation);
					stageList.add(stageIO);

				}
				scheduleio.setStageList(stageList);
			}
			PrerequisitesList.add(scheduleio);
		}
		return ResponseIO.success(PrerequisitesList);

	}

	@RequestMapping(value = "/schedule/{tripCode}/stage", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StageIO>> getStageByStageCode(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		List<StageIO> list = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		tripDTO = tripService.getTripDTO(authDTO, tripDTO);

		List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, tripDTO);
		// Sorting
		Comparator<StageStationDTO> comp = new BeanComparator("stationSequence");
		Collections.sort(stageList, comp);

		for (StageStationDTO stageStationDTO : stageList) {
			StageIO stageIO = new StageIO();
			StationIO fromStation = new StationIO();
			fromStation.setCode(stageStationDTO.getStation().getCode());
			fromStation.setName(stageStationDTO.getStation().getName());
			stageIO.setStageSequence(stageStationDTO.getStationSequence());
			fromStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageStationDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));

			List<StationPointIO> fromStationPoint = new ArrayList<>();
			for (StationPointDTO pointDTO : stageStationDTO.getStationPoint()) {
				StationPointIO pointIO = new StationPointIO();
				if (pointDTO.getCreditDebitFlag().equals("Cr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageStationDTO.getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageStationDTO.getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
				pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
				pointIO.setCode(pointDTO.getCode());
				pointIO.setName(pointDTO.getName());
				pointIO.setLandmark(pointDTO.getLandmark());
				pointIO.setAddress(pointDTO.getAddress());
				pointIO.setNumber(pointDTO.getNumber());
				fromStationPoint.add(pointIO);
			}
			fromStation.setStationPoint(fromStationPoint.stream().sorted((s1, s2) -> s1.getDateTime().compareTo(s2.getDateTime())).collect(Collectors.toList()));
			stageIO.setFromStation(fromStation);
			list.add(stageIO);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/station/all", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StationIO>> getAllStation(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		Map<Integer, StationIO> stationMap = new HashMap<Integer, StationIO>();
		List<StationPointDTO> pointDTOList = stationService.getCommerceALLStationAndPoint(authDTO);
		for (StationPointDTO pointDTO : pointDTOList) {
			if (stationMap.get(pointDTO.getStation().getId()) == null) {
				StationIO station = new StationIO();
				StationPointIO point = new StationPointIO();
				point.setCode(pointDTO.getCode());
				point.setName(pointDTO.getName());
				point.setAddress(pointDTO.getAddress());
				point.setLandmark(pointDTO.getLandmark());
				point.setNumber(pointDTO.getNumber());
				point.setLatitude(pointDTO.getLatitude());
				point.setLongitude(pointDTO.getLongitude());

				station.setCode(pointDTO.getStation().getCode());
				station.setName(pointDTO.getStation().getName());

				StateIO state = new StateIO();
				state.setCode(pointDTO.getStation().getState().getCode());
				state.setName(pointDTO.getStation().getState().getName());

				station.setState(state);
				List<StationPointIO> pointList = new ArrayList<StationPointIO>();
				pointList.add(point);
				station.setStationPoint(pointList);

				stationMap.put(pointDTO.getStation().getId(), station);
			}
			else {
				StationIO station = stationMap.get(pointDTO.getStation().getId());
				StationPointIO point = new StationPointIO();
				point.setCode(pointDTO.getCode());
				point.setName(pointDTO.getName());
				point.setAddress(pointDTO.getAddress());
				point.setLandmark(pointDTO.getLandmark());
				point.setNumber(pointDTO.getNumber());
				point.setLatitude(pointDTO.getLatitude());
				point.setLongitude(pointDTO.getLongitude());
				station.getStationPoint().add(point);

				stationMap.put(pointDTO.getStation().getId(), station);
			}
		}
		List<StationIO> list = new ArrayList<StationIO>(stationMap.values());
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/organization", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<OrganizationIO>> getOrganization(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "1") int activeFlag) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<OrganizationIO> organizationlist = new ArrayList<OrganizationIO>();
		List<OrganizationDTO> list = (List<OrganizationDTO>) organizationService.getAll(authDTO);
		for (OrganizationDTO organizationDTO : list) {
			if (activeFlag != -1 && activeFlag != organizationDTO.getActiveFlag()) {
				continue;
			}
			OrganizationIO organizaionio = new OrganizationIO();
			organizaionio.setCode(organizationDTO.getCode());
			organizaionio.setName(organizationDTO.getName());
			organizaionio.setShortCode(organizationDTO.getShortCode());
			organizaionio.setAddress1(organizationDTO.getAddress1());
			organizaionio.setAddress2(organizationDTO.getAddress2());
			organizaionio.setContact(organizationDTO.getContact());
			organizaionio.setPincode(organizationDTO.getPincode());
			organizaionio.setLatitude(organizationDTO.getLatitude());
			organizaionio.setLongitude(organizationDTO.getLongitude());
			StationIO stationIO = new StationIO();
			StateIO stateIO = new StateIO();
			stateIO.setCode(organizationDTO.getStation().getState().getCode());
			stateIO.setName(organizationDTO.getStation().getState().getName());
			stationIO.setState(stateIO);
			stationIO.setName(organizationDTO.getStation().getName());
			stationIO.setCode(organizationDTO.getStation().getCode());
			organizaionio.setStation(stationIO);
			organizationlist.add(organizaionio);
		}
		return ResponseIO.success(organizationlist);
	}

	@RequestMapping(value = "/passengerdetails", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TicketIO> getPassengerDetails(@PathVariable("authtoken") String authtoken, String mobileNumber, @RequestParam(value = "seatCount", required = false, defaultValue = "1") int seatCount) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TicketIO ticketIO = new TicketIO();
		if (StringUtils.isNotBlank(mobileNumber) && Constants.MOBILE_NUMBER_PATTERN.matcher(mobileNumber).matches() == Boolean.FALSE) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		if (seatCount == 0) {
			seatCount = 1;
		}
		TicketDTO ticketDTO = ticketService.getAutoPassengerDetails(authDTO, mobileNumber, seatCount);
		if (ticketDTO != null) {
			ticketIO.setPassegerEmailId(ticketDTO.getPassengerEmailId());
			List<TicketDetailsIO> ticketDetails = new ArrayList<TicketDetailsIO>();
			if (ticketDTO.getTicketDetails() != null && !ticketDTO.getTicketDetails().isEmpty()) {
				for (TicketDetailsDTO detailsDTO : ticketDTO.getTicketDetails()) {
					TicketDetailsIO detailsIO = new TicketDetailsIO();
					detailsIO.setPassengerName(detailsDTO.getPassengerName());
					detailsIO.setPassengerAge(detailsDTO.getPassengerAge());
					detailsIO.setPassengerGendar(detailsDTO.getSeatGendar().getCode());
					ticketDetails.add(detailsIO);
				}
			}
			ticketIO.setTicketDetails(ticketDetails);
		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/gallery/{tripCode}/image", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<GalleryImageIO>> getScheduleGalleryImage(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		List<GalleryImageIO> list = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		List<GalleryImageDTO> imageList = imageService.getScheduleGalleryImage(authDTO, tripDTO);
		for (GalleryImageDTO galleryImageDTO : imageList) {
			GalleryImageIO image = new GalleryImageIO();
			image.setCode(galleryImageDTO.getCode());
			image.setName(galleryImageDTO.getName());
			image.setImageURL(galleryImageDTO.getImageURL());
			list.add(image);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/OTP/generate", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> generateTransactionOTP(@PathVariable("authtoken") String authtoken, String ticketCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BaseIO baseIO = new BaseIO();
		if (StringUtil.isNull(ticketCode)) {
			throw new ServiceException(ErrorCode.INVALID_TRANSACTION_ID);
		}
		if (authDTO.getUser().getUserRole().getId() != UserRoleEM.CUST_ROLE.getId()) {
			throw new ServiceException(ErrorCode.INVALID_AUTH_TYPE);
		}

		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		ticketService.getTicketStatus(authDTO, ticketDTO);

		if (ticketDTO.getTicketUser().getUserRole().getId() != UserRoleEM.CUST_ROLE.getId()) {
			throw new ServiceException(ErrorCode.INVALID_AUTH_TYPE);
		}

		if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
			otpService.generateOTP(authDTO, ticketCode, ticketDTO.getPassengerMobile(), true);
			baseIO.setCode(ticketDTO.getPassengerMobile());
		}
		else {
			throw new ServiceException(ErrorCode.SEAT_ALREADY_CANCELLED);
		}
		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/otp/generate/user", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> generateUserTransactionOTP(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BaseIO baseIO = new BaseIO();
		if (authDTO.getUser().getUserRole().getId() != UserRoleEM.USER_ROLE.getId()) {
			throw new ServiceException(ErrorCode.INVALID_AUTH_TYPE);
		}
		if (StringUtil.isNull(authDTO.getUser().getMobile()) || !StringUtil.isValidMobileNumber(authDTO.getUser().getMobile())) {
			throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
		}
		otpService.generateOTP(authDTO, authDTO.getUser().getCode(), authDTO.getUser().getMobile(), true);
		baseIO.setCode(authDTO.getUser().getMobile());

		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/otp/{otpNumber}/check/user", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> validateTransactionOTP(@PathVariable("authtoken") String authtoken, @PathVariable("otpNumber") int otpNumber) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BaseIO baseIO = new BaseIO();
		if (authDTO.getUser().getUserRole().getId() != UserRoleEM.USER_ROLE.getId()) {
			throw new ServiceException(ErrorCode.INVALID_AUTH_TYPE);
		}

		if (!otpService.checkOTP(authDTO, authDTO.getUser().getCode(), authDTO.getUser().getMobile(), otpNumber)) {
			throw new ServiceException(ErrorCode.INVAILD_TRANSACTION_OTP);
		}

		return ResponseIO.success(baseIO);
	}

	@RequestMapping(value = "/travel/stops/{tripCode}/{fromStationCode}/{toStationCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TravelStopsIO>> getTravelStops(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("fromStationCode") String fromStationCode, @PathVariable("toStationCode") String toStationCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		SearchDTO searchDTO = new SearchDTO();
		StationDTO fromStationDTO = new StationDTO();
		fromStationDTO.setCode(fromStationCode);
		StationDTO toStationDTO = new StationDTO();
		toStationDTO.setCode(toStationCode);
		searchDTO.setFromStation(fromStationDTO);
		searchDTO.setToStation(toStationDTO);

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		tripDTO.setSearch(searchDTO);

		List<TravelStopsDTO> travelStopsList = travelStopsService.getScheduleTripStop(authDTO, tripDTO);

		List<TravelStopsIO> stopsList = new ArrayList<TravelStopsIO>();
		for (TravelStopsDTO dto : travelStopsList) {
			TravelStopsIO stops = new TravelStopsIO();
			stops.setCode(dto.getCode());
			stops.setName(dto.getName());
			StationIO stationIO = new StationIO();
			stationIO.setCode(dto.getStation().getCode());
			stationIO.setName(dto.getStation().getName());
			stops.setStations(stationIO);
			stops.setAmenities(dto.getAmenities());
			stops.setRestRoom(dto.getRestRoom());
			stops.setTravelStopTime(dto.getTravelStopTime().format("YYYY-MM-DD hh:mm:ss"));
			stops.setMinutes(dto.getMinutes());
			stops.setLandmark(dto.getLandmark());
			stops.setLatitude(dto.getLatitude());
			stops.setLongitude(dto.getLongitude());
			stops.setRemarks(dto.getRemarks());
			stops.setActiveFlag(dto.getActiveFlag());
			stopsList.add(stops);
		}
		return ResponseIO.success(stopsList);
	}

	@RequestMapping(value = "/ticket/{ticketCode}/transfer/terms", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ScheduleTicketTransferTermsIO> getRescheduleTermsForTicket(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		ScheduleTicketTransferTermsIO ticketTransferTerms = new ScheduleTicketTransferTermsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);

		ScheduleTicketTransferTermsDTO ticketTransferTermsDTO = scheduleTicketTransferTermsService.getScheduleTicketTransferTermsByTicket(authDTO, ticketDTO);
		if (ticketTransferTermsDTO == null || StringUtil.isNull(ticketTransferTermsDTO.getCode())) {
			throw new ServiceException("Schedule Ticket Transfer Terms Empty!");
		}
		ticketTransferTerms.setChargeAmount(ticketTransferTermsDTO.getChargeAmount());
		ticketTransferTerms.setTransferable(Numeric.ONE_INT);
		ticketTransferTerms.setChargeType(ticketTransferTermsDTO.getChargeType().getCode());
		return ResponseIO.success(ticketTransferTerms);
	}

	@RequestMapping(value = "/validate/email", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> validateEmail(@PathVariable("authtoken") String authtoken, String email) throws Exception {
		if (!StringUtil.isValidEmailId(email)) {
			throw new ServiceException(ErrorCode.INVALID_EMAIL_ID);
		}
		if (!EmailUtil.isValid(email)) {
			throw new ServiceException(ErrorCode.INVALID_EMAIL_DOMAIN);
		}
		return ResponseIO.success();
	}

	private boolean ValidateBlockingDetails(AuthDTO authDTO, OrderIO orderIO) throws Exception {
		if (orderIO == null || orderIO.getOrderDetails() == null) {
			throw new ServiceException(ErrorCode.INVALID_ORDER_DETAILS);
		}
		for (OrderDetailsIO orderDetails : orderIO.getOrderDetails()) {
			if (StringUtil.isNull(orderDetails.getTripStageCode())) {
				throw new ServiceException(ErrorCode.TRIP_STATGE_CODE);
			}
			if (StringUtil.isNull(orderIO.getEmailId()) || !StringUtil.isValidEmailId(orderIO.getEmailId())) {
				throw new ServiceException(ErrorCode.INVALID_EMAIL_ID);
			}
			if (StringUtil.isNull(orderIO.getMobileNumber()) || !StringUtil.isValidMobileNumber(orderIO.getMobileNumber())) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			if (orderDetails.getTicketDetails() == null || orderDetails.getTicketDetails().isEmpty()) {
				throw new ServiceException(ErrorCode.SEAT_NOT_AVAILABLE);
			}
			if (orderDetails.getTicketDetails().size() > authDTO.getNamespace().getProfile().getMaxSeatPerTransaction()) {
				throw new ServiceException(ErrorCode.MAX_SEAT_PER_TRANSACTION);
			}
			for (TicketDetailsIO seatInfo : orderDetails.getTicketDetails()) {
				if (StringUtil.isNull(seatInfo.getSeatCode())) {
					throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
				}
			}
		}
		return true;
	}

	private boolean ValidateBlockingDetailsV3(AuthDTO authDTO, OrderV3IO orderIO) throws Exception {
		if (orderIO == null || orderIO.getOrderDetails() == null) {
			throw new ServiceException(ErrorCode.INVALID_ORDER_DETAILS);
		}
		for (OrderDetailsV3IO orderDetails : orderIO.getOrderDetails()) {
			if (StringUtil.isNull(orderDetails.getTripCode())) {
				throw new ServiceException(ErrorCode.TRIP_STATGE_CODE);
			}
			if (StringUtil.isNull(orderDetails.getTravelDate())) {
				throw new ServiceException(ErrorCode.INVALID_DATE);
			}
			if (orderDetails.getFromStation() == null || orderDetails.getToStation() == null || StringUtil.isNull(orderDetails.getFromStation().getCode()) || StringUtil.isNull(orderDetails.getToStation().getCode())) {
				throw new ServiceException(ErrorCode.INVALID_STATION);
			}
			if (StringUtil.isNull(orderIO.getEmailId()) || !StringUtil.isValidEmailId(orderIO.getEmailId())) {
				throw new ServiceException(ErrorCode.INVALID_EMAIL_ID);
			}
			if (StringUtil.isNull(orderIO.getMobileNumber()) || !StringUtil.isValidMobileNumber(orderIO.getMobileNumber())) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			if (Character.toString(orderIO.getMobileNumber().charAt(0)).equals(Numeric.ZERO)) {
				orderIO.setMobileNumber(orderIO.getMobileNumber().substring(1));
			}
			if (orderDetails.getBoardingPoint() == null || StringUtil.isNull(orderDetails.getBoardingPoint().getCode())) {
				throw new ServiceException(ErrorCode.STATION_POINT);
			}
			if (orderDetails.getDroppingPoint() == null || StringUtil.isNull(orderDetails.getDroppingPoint().getCode())) {
				throw new ServiceException(ErrorCode.STATION_POINT);
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
				if (StringUtil.isNull(seatInfo.getPassengerName())) {
					throw new ServiceException(ErrorCode.INVALID_PASSENGER_NAME);
				}
				if (seatInfo.getPassengerAge() < 3 || seatInfo.getPassengerAge() > 100) {
					throw new ServiceException(ErrorCode.INVALID_PASSENGER_AGE);
				}
				if (StringUtil.isNull(seatInfo.getPassengerGendar())) {
					throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
				}
				if (uniqueList.contains(seatInfo.getSeatCode().trim())) {
					throw new ServiceException(ErrorCode.DUPLICATE_SEAT_CODE);
				}
				uniqueList.add(seatInfo.getSeatCode().trim());

				seatInfo.setPassengerName(StringUtil.substring(StringUtil.removeUnknownSymbol(seatInfo.getPassengerName()), 59));
				seatInfo.setPassengerName(WordUtils.capitalize(seatInfo.getPassengerName()));
			}
		}
		return true;
	}

	private boolean ValidateBlockingDetailsV4(AuthDTO authDTO, OrderV3IO orderIO) throws Exception {
		if (orderIO == null || orderIO.getOrderDetails() == null) {
			throw new ServiceException(ErrorCode.INVALID_ORDER_DETAILS);
		}
		for (OrderDetailsV3IO orderDetails : orderIO.getOrderDetails()) {
			if (StringUtil.isNull(orderDetails.getTripCode())) {
				throw new ServiceException(ErrorCode.TRIP_STATGE_CODE);
			}
			if (StringUtil.isNull(orderDetails.getTravelDate())) {
				throw new ServiceException(ErrorCode.INVALID_DATE);
			}
			if (orderDetails.getFromStation() == null || orderDetails.getToStation() == null || StringUtil.isNull(orderDetails.getFromStation().getCode()) || StringUtil.isNull(orderDetails.getToStation().getCode())) {
				throw new ServiceException(ErrorCode.INVALID_STATION);
			}
			if (StringUtil.isNull(orderIO.getEmailId()) || !StringUtil.isValidEmailId(orderIO.getEmailId())) {
				throw new ServiceException(ErrorCode.INVALID_EMAIL_ID);
			}
			if (StringUtil.isNull(orderIO.getMobileNumber()) || !StringUtil.isValidMobileNumber(orderIO.getMobileNumber())) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			if (Character.toString(orderIO.getMobileNumber().charAt(0)).equals(Numeric.ZERO)) {
				orderIO.setMobileNumber(orderIO.getMobileNumber().substring(1));
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
				if (StringUtil.isNull(seatInfo.getSeatFare())) {
					throw new ServiceException(ErrorCode.INVALID_SEAT_FARE);
				}
				if (uniqueList.contains(seatInfo.getSeatCode().trim())) {
					throw new ServiceException(ErrorCode.DUPLICATE_SEAT_CODE);
				}
				uniqueList.add(seatInfo.getSeatCode().trim());
			}
		}
		return true;
	}

	private RouteIO getRoutes(StationDTO fromStationDTO, StationDTO toStationDTO) {
		RouteIO route = new RouteIO();
		BaseIO fromStation = new BaseIO();
		fromStation.setCode(fromStationDTO.getCode());
		fromStation.setName(fromStationDTO.getName());
		BaseIO toStation = new BaseIO();
		toStation.setCode(toStationDTO.getCode());
		toStation.setName(toStationDTO.getName());
		route.setFromStation(fromStation);
		route.setToStation(toStation);
		return route;
	}

	@RequestMapping(value = "/util/decrypt", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONObject> validateLinkPay(@PathVariable("authtoken") String authtoken, @RequestParam String linkpayUrl) throws Exception {
		authService.getAuthDTO(authtoken);
		String linkpayDetails = BitsEnDecrypt.getDecoder(linkpayUrl);
		JSONObject linkpayJSON = JSONObject.fromObject(linkpayDetails);
		return ResponseIO.success(linkpayJSON);
	}

	@RequestMapping(value = "/banner", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<NamespaceBannerIO>> getActiveBanner(@PathVariable("authtoken") String authtoken) {
		List<NamespaceBannerIO> bannerIOList = new ArrayList<NamespaceBannerIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<NamespaceBannerDTO> bannerList = bannerService.getActiveBanner(authDTO);
		for (NamespaceBannerDTO bannerDTO : bannerList) {
			NamespaceBannerIO bannerIO = new NamespaceBannerIO();
			bannerIO.setCode(bannerDTO.getCode());
			bannerIO.setName(bannerDTO.getName());
			bannerIO.setDisplayModel(bannerDTO.getDisplayModel());
			bannerIO.setUpdatedAt(DateUtil.convertDateTime(bannerDTO.getUpdatedAt()));
			bannerIO.setColor(bannerDTO.getColor());
			List<NamespaceBannerDetailsIO> bannerDetailsList = new ArrayList<NamespaceBannerDetailsIO>();
			for (NamespaceBannerDetailsDTO bannerDetails : bannerDTO.getBannerDetails()) {
				NamespaceBannerDetailsIO bannerDetailsIO = new NamespaceBannerDetailsIO();
				bannerDetailsIO.setCode(bannerDetails.getCode());
				bannerDetailsIO.setUrl(bannerDetails.getUrl());
				bannerDetailsIO.setRedirectUrl(bannerDetails.getRedirectUrl());
				bannerDetailsIO.setAlternateText(bannerDetails.getAlternateText());
				bannerDetailsIO.setSequence(bannerDetails.getSequence());
				BaseIO messageType = new BaseIO();
				if (bannerDetails.getMediaType() != null) {
					messageType.setCode(bannerDetails.getMediaType().getCode());
					messageType.setName(bannerDetails.getMediaType().getName());
				}
				bannerDetailsIO.setMessageType(messageType);
				bannerDetailsIO.setActiveFlag(bannerDetails.getActiveFlag());
				bannerDetailsList.add(bannerDetailsIO);
			}
			bannerIO.setBannerDetails(bannerDetailsList);
			bannerIO.setActiveFlag(bannerDTO.getActiveFlag());
			bannerIOList.add(bannerIO);
		}
		return ResponseIO.success(bannerIOList);
	}

	@RequestMapping(value = "/termsV2", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TermIO>> getTermsAndConditionsV2(@PathVariable("authtoken") String authtoken, @RequestParam(required = false) String[] tag, @RequestParam(required = false) String scheduleCode) throws Exception {
		List<TermIO> termIOs = new ArrayList<TermIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TermDTO termDTO = new TermDTO();
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		if (tag == null && scheduleCode == null) {
			throw new ServiceException(ErrorCode.MANDATORY_PARAMETERS_MISSING);
		}
		if (tag != null) {
			List<String> tagList = new ArrayList<String>();
			for (String str : tag) {
				tagList.add(str);
			}
			termDTO.setTagList(tagList);
		}
		scheduleDTO.setCode(scheduleCode);
		List<TermDTO> list = (List<TermDTO>) termsService.getTermsAndConditions(authDTO, termDTO, scheduleDTO);
		for (TermDTO dto : list) {
			TermIO termIO = new TermIO();
			termIO.setName(dto.getName());
			termIO.setCode(dto.getCode());
			termIO.setActiveFlag(dto.getActiveFlag());
			termIO.setSequence(dto.getSequenceId());
			List<String> tagList = new ArrayList<String>();
			if (dto.getTagList() != null && !dto.getTagList().isEmpty()) {
				for (String tag1 : dto.getTagList()) {
					tagList.add(tag1);
				}
			}
			termIO.setTagList(tagList);
			termIOs.add(termIO);
		}
		return ResponseIO.success(termIOs);
	}

	@RequestMapping(value = "/discount/special", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<DiscountSpecialCriteriaIO>> getSpecialDiscountCriteria(@PathVariable("authtoken") String authtoken, @RequestParam(required = true) String scheduleCode) {
		List<DiscountSpecialCriteriaIO> specialDiscountList = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleDTO schedule = new ScheduleDTO();
		schedule.setCode(scheduleCode);
		List<DiscountSpecialCriteriaDTO> list = discountSpecialCriteriaService.getSpecialDiscountCriteria(authDTO, schedule);
		for (DiscountSpecialCriteriaDTO dto : list) {
			DiscountSpecialCriteriaIO specialDiscount = new DiscountSpecialCriteriaIO();
			specialDiscount.setCode(dto.getCode());
			specialDiscount.setMaxAmount(dto.getMaxAmount());
			specialDiscount.setPercentageFlag(dto.isPercentageFlag());
			specialDiscount.setActiveFlag(dto.getActiveFlag());
			specialDiscountList.add(specialDiscount);
		}
		return ResponseIO.success(specialDiscountList);
	}

	@RequestMapping(value = "/account/ticket/history", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TicketIO>> getTicketByUser(@PathVariable("authtoken") String authtoken) {
		AuthDTO auth = authService.getAuthDTO(authtoken);
		List<TicketIO> tickets = new ArrayList<>();
		List<TicketDTO> list = ticketService.getRecentTicketUserCustomer(auth);

		for (TicketDTO ticketDTO : list) {
			TicketIO ticketIO = new TicketIO();
			ticketIO.setTicketCode(ticketDTO.getCode());
			ticketIO.setTravelDate(ticketDTO.getTripDate().toString());
			ticketIO.setTripCode(ticketDTO.getTripDTO().getCode());
			ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());
			ticketIO.setPassegerEmailId(ticketDTO.getPassengerEmailId());
			ticketIO.setJourneyType(ticketDTO.getJourneyType().getCode());
			ticketIO.setTransactionDate(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));

			StationIO fromStationIO = new StationIO();
			StationIO toStationIO = new StationIO();

			fromStationIO.setCode(ticketDTO.getFromStation().getCode());
			fromStationIO.setName(ticketDTO.getFromStation().getName());

			StationPointIO fromStationPoint = new StationPointIO();
			fromStationPoint.setDateTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
			fromStationPoint.setName(ticketDTO.getBoardingPoint().getName());

			fromStationIO.setStationPoints(fromStationPoint);
			ticketIO.setFromStation(fromStationIO);

			StationPointIO toStationPoint = new StationPointIO();
			toStationPoint.setDateTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getDroppingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
			toStationPoint.setName(ticketDTO.getDroppingPoint().getName());
			toStationIO.setStationPoints(toStationPoint);

			toStationIO.setCode(ticketDTO.getToStation().getCode());
			toStationIO.setName(ticketDTO.getToStation().getName());
			ticketIO.setToStation(toStationIO);

			BusIO bus = new BusIO();
			bus.setCategoryCode(ticketDTO.getTripDTO().getBus().getCategoryCode());
			ticketIO.setBus(bus);

			TicketStatusIO ticketStatus = new TicketStatusIO();
			ticketStatus.setCode(ticketDTO.getTicketStatus().getCode());
			ticketIO.setTicketStatus(ticketStatus);

			if (ticketDTO.getTicketUser() != null) {
				UserIO userIO = new UserIO();
				userIO.setName(ticketDTO.getTicketUser().getName());
				userIO.setCode(ticketDTO.getTicketUser().getCode());
				ticketIO.setUser(userIO);
			}

			List<TicketDetailsIO> ticketDetailList = new ArrayList<>();
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				TicketDetailsIO ticketDetails = new TicketDetailsIO();
				ticketDetails.setSeatName(ticketDetailsDTO.getSeatName());
				ticketDetails.setSeatFare(ticketDetailsDTO.getSeatFare());
				ticketDetails.setServiceTax(ticketDetailsDTO.getAcBusTax());
				ticketDetailList.add(ticketDetails);
			}
			ticketIO.setTicketDetails(ticketDetailList);
			tickets.add(ticketIO);

			// Sorting
			Comparator<TicketIO> comparator = new BeanComparator("travelDate");
			Collections.sort(tickets, comparator);
		}

		return ResponseIO.success(tickets);
	}
}
