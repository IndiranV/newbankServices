package org.in.com.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.aggregator.utility.BitsUtilityService;
import org.in.com.cache.BusCache;
import org.in.com.cache.CancellationTermsCache;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.TripCache;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.StationDAO;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleCancellationTermDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDiscountDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.ScheduleSeatAutoReleaseDTO;
import org.in.com.dto.ScheduleSeatFareDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.ScheduleTimeOverrideDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TravelStopsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripSeatQuotaDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.BusCategoryTypeEM;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.ReleaseTypeEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TripActivitiesEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.BusBreakevenService;
import org.in.com.service.NamespaceTaxService;
import org.in.com.service.ScheduleBusOverrideService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleCancellationTermService;
import org.in.com.service.ScheduleDiscountService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.ScheduleEnrouteBookControlService;
import org.in.com.service.ScheduleFareOverrideService;
import org.in.com.service.ScheduleSeatAutoReleaseService;
import org.in.com.service.ScheduleSeatFareService;
import org.in.com.service.ScheduleSeatVisibilityService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleStationPointService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.ScheduleTicketTransferTermsService;
import org.in.com.service.ScheduleTimeOverrideService;
import org.in.com.service.ScheduleTripService;
import org.in.com.service.SearchService;
import org.in.com.service.SectorService;
import org.in.com.service.StationPointService;
import org.in.com.service.StationService;
import org.in.com.service.TravelStopsService;
import org.in.com.service.TripSeatQuotaService;
import org.in.com.service.TripService;
import org.in.com.service.TripServiceV2;
import org.in.com.service.UserService;
import org.in.com.service.helper.HelperUtil;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.gson.Gson;

import hirondelle.date4j.DateTime;
import lombok.Data;
import net.sf.json.JSONObject;

@Service
public class SearchImpl extends HelperUtil implements SearchService {
	public static String DEBUG_SCHEDULE_CODE = "";
	@Autowired
	TripService tripService;
	@Autowired
	TripServiceV2 tripServiceV2;
	@Autowired
	ScheduleSeatVisibilityService visibilityService;
	@Autowired
	ScheduleSeatFareService seatFareService;
	@Autowired
	ScheduleFareOverrideService fareOverrideService;
	@Autowired
	ScheduleTimeOverrideService timeOverrideService;
	@Autowired
	ScheduleSeatAutoReleaseService autoReleaseService;
	@Autowired
	ScheduleTripService scheduleTripService;
	@Autowired
	ScheduleDiscountService discountService;
	@Autowired
	ScheduleCancellationTermService cancellationTermService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	ScheduleStationPointService scheduleStationPointService;
	@Autowired
	StationPointService stationPointService;
	@Autowired
	NamespaceTaxService taxService;
	@Autowired
	ScheduleStageService stageService;
	@Autowired
	ScheduleDynamicStageFareService dynamicStageFareService;
	@Autowired
	ScheduleTicketTransferTermsService scheduleTicketTransferTermsService;
	@Autowired
	TripSeatQuotaService quotaService;
	@Autowired
	ScheduleEnrouteBookControlService enrouteBookControlService;
	@Autowired
	ScheduleBusService busService;
	@Autowired
	ScheduleBusOverrideService busOverrideService;
	@Autowired
	TravelStopsService travelStopsService;
	@Autowired
	StationService stationService;
	@Autowired
	SectorService sectorService;
	@Autowired
	BusBreakevenService breakevenService;
	@Autowired
	BitsUtilityService utilityService;
	@Autowired
	AuthService authService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	UserService userService;

	private static final Logger logger = LoggerFactory.getLogger(SearchImpl.class);

	public List<TripDTO> getSearch(AuthDTO authDTO, SearchDTO searchDTO) {
		List<TripDTO> trips = new ArrayList<>();
		try {
			/** Validate open search */
			boolean isOpenSearch = isOpenSearch(authDTO, searchDTO);

			List<TripDTO> availableTrips = null;
			if (isOpenSearch) {
				List<TripDTO> activeScheduleTrips = getNamespaceSearchResultV2(authDTO, searchDTO);
				availableTrips = validateAndConvertTrips(authDTO, activeScheduleTrips);
			}
			else {
				if (searchDTO.getFromStation().getId() == 0 || searchDTO.getToStation().getId() == 0) {
					throw new ServiceException(ErrorCode.INVALID_STATION);
				}

				List<SearchDTO> searchResults = getRelatedSearch(authDTO, searchDTO);
				availableTrips = new ArrayList<>();
				List<String> scheduleCodes = new ArrayList<>();
				for (SearchDTO search : searchResults) {
					List<ScheduleDTO> scheduleList = getNamespaceSearchResult(authDTO, search, scheduleCodes);
					List<TripDTO> tripList = convertScheduleToTrip(authDTO, search, scheduleList, scheduleCodes);
					availableTrips.addAll(tripList);
				}
			}

			if (authDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId()) {
				tripServiceV2.saveTrip(authDTO, availableTrips);
			}
			else {
				tripService.saveTrip(authDTO, availableTrips);
			}
			if (authDTO.getNamespace().getProfile().isAliasNamespaceFlag() && authDTO.isSectorEnabled()) {
				applySector(authDTO, availableTrips);
			}
			trips.addAll(availableTrips);

			for (TripDTO tripDTO : trips) {
				if (tripDTO.getTripStatus() == null) {
					tripDTO.setTripStatus(tripDTO.getStage().getStageStatus());
				}
				tripService.getBookedBlockedSeats(authDTO, tripDTO);
				applyBookedBlockedSeat(authDTO, tripDTO);

				if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId()) {
					List<TripSeatQuotaDTO> tripSeatQuatoList = quotaService.getAllTripSeatQuota(authDTO, tripDTO);
					applyTripSeatQuota(authDTO, tripDTO, tripSeatQuatoList);
				}

				enrouteBookControlService.applyScheduleEnrouteBookControl(authDTO, tripDTO);

				List<TravelStopsDTO> stopsList = travelStopsService.getScheduleStopV2(authDTO, tripDTO.getSchedule(), tripDTO.getSearch());
				tripDTO.setTravelStopCount(stopsList.size());
				tripDTO.getAdditionalAttributes().put("tripStatusName", BitsUtil.getTripStatusBasedOnStageTime(tripDTO));
			}
		}
		catch (Exception e) {
			trips.clear();
			System.out.println(authDTO.getAuthToken() + " - " + authDTO.getDeviceMedium() + " - " + authDTO.getApiToken() + " - " + searchDTO.getTravelDate() + " - " + searchDTO.getFromStation().getCode() + " - " + searchDTO.getToStation().getCode());
			logger.error("Exception occurred while searching the result " + e.getMessage());
			e.printStackTrace();
		}

		return trips;
	}

	private boolean isOpenSearch(AuthDTO authDTO, SearchDTO searchDTO) {
		boolean isOpenSearch = false;
		if (searchDTO.getFromStation().getCode().equals(Constants.OPEN_SEARCH_KEY) || searchDTO.getToStation().getCode().equals(Constants.OPEN_SEARCH_KEY)) {
			isOpenSearch = true;
		}
		if (!searchDTO.getFromStation().getCode().equals(Constants.OPEN_SEARCH_KEY)) {
			searchDTO.setFromStation(stationService.getStation(searchDTO.getFromStation()));
			searchDTO.getFromStation().setList(stationService.getRelatedStation(authDTO, searchDTO.getFromStation()));
		}
		if (!searchDTO.getToStation().getCode().equals(Constants.OPEN_SEARCH_KEY)) {
			searchDTO.setToStation(stationService.getStation(searchDTO.getToStation()));
			searchDTO.getToStation().setList(stationService.getRelatedStation(authDTO, searchDTO.getToStation()));
		}
		return isOpenSearch;
	}

	private List<SearchDTO> getRelatedSearch(AuthDTO authDTO, SearchDTO search) {
		List<SearchDTO> searchResults = new ArrayList<>();

		for (StationDTO stationDTO : search.getFromStation().getList()) {
			if (stationDTO.getId() == 0) {
				continue;
			}
			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(search.getTravelDate());
			searchDTO.setFromStation(stationDTO);
			searchDTO.setToStation(search.getToStation());
			searchResults.add(searchDTO);

			for (StationDTO toStationDTO : search.getToStation().getList()) {
				if (toStationDTO.getId() == 0) {
					continue;
				}
				SearchDTO relatedSearchDTO = new SearchDTO();
				relatedSearchDTO.setTravelDate(search.getTravelDate());
				relatedSearchDTO.setFromStation(stationDTO);
				relatedSearchDTO.setToStation(search.getToStation());
				relatedSearchDTO.setToStation(toStationDTO);
				searchResults.add(relatedSearchDTO);
			}
		}

		SearchDTO searchDTO = new SearchDTO();
		searchDTO.setTravelDate(search.getTravelDate());
		searchDTO.setFromStation(search.getFromStation());
		searchDTO.setToStation(search.getToStation());
		searchResults.add(searchDTO);

		for (StationDTO toStationDTO : search.getToStation().getList()) {
			if (toStationDTO.getId() == 0) {
				continue;
			}
			SearchDTO relatedSearchDTO = new SearchDTO();
			relatedSearchDTO.setTravelDate(search.getTravelDate());
			relatedSearchDTO.setFromStation(search.getFromStation());
			relatedSearchDTO.setToStation(toStationDTO);
			searchResults.add(relatedSearchDTO);
		}
		return searchResults;
	}

	public List<TripDTO> getAllTrips(AuthDTO authDTO, SearchDTO searchDTO) {
		SectorDTO sector = sectorService.getActiveSectorScheduleStation(authDTO);

		// validate before date limit
		int reportingDays = authDTO.getNamespace().getProfile().getReportingDays();
		if (reportingDays != 0 && !DateUtil.isValidBeforeDate(searchDTO.getTravelDate(), reportingDays)) {
			throw new ServiceException(ErrorCode.INVALID_DATE, "Trip date limit should be " + reportingDays + " days before from current date");
		}

		List<TripDTO> list = scheduleTripService.getAllTripDetails(authDTO, sector, searchDTO);

		if (authDTO.getNamespace().getProfile().isAliasNamespaceFlag() && authDTO.isSectorEnabled() && authDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId()) {
			applySector(authDTO, list);
		}

		BusCache busCache = new BusCache();
		for (TripDTO tripDTO : list) {
			tripDTO.setCode(getGeneratedTripCode(authDTO, tripDTO.getSchedule(), searchDTO));

			for (StageDTO stageDTO : tripDTO.getStageList()) {
				stageDTO.setCode(getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), searchDTO, stageDTO));
			}
			tripDTO.getStage().setCode(getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), searchDTO, tripDTO.getStage()));
			// tripDTO.setTripStatus(TripStatusEM.TRIP_OPEN);
			tripDTO.setBus(busCache.getBusDTObyId(authDTO, tripDTO.getBus()));
		}
		List<TripDTO> activeList = tripService.saveTrip(authDTO, list);
		for (TripDTO tripDTO : activeList) {
			if (tripDTO.getTripStatus() == null) {
				tripDTO.setTripStatus(tripDTO.getStage().getStageStatus());
			}
			TripSeatVisibilityDTO tripSeatVisibility = processSeatVisibility(authDTO, tripDTO, searchDTO);

			tripService.getBookedBlockedSeats(authDTO, tripDTO);

			Map<String, String> uniqueSeatMap = new HashMap<>();
			Map<String, String> phoneBlockSeatMap = new HashMap<>();
			Map<String, String> seatSubMap = new HashMap<>();
			Map<String, String> multiStageBookedSeatMap = new HashMap<>();
			Map<String, TripRevenueDTO> userGroupRevenue = new HashMap<>();
			Map<String, String> uniqueCancelSeatMap = new HashMap<>();
			BigDecimal bookedAmount = BigDecimal.ZERO;
			BigDecimal revenueAmount = BigDecimal.ZERO;
			BigDecimal cancelledAmount = BigDecimal.ZERO;
			for (TicketDetailsDTO detailsDTO : tripDTO.getTicketDetailsList()) {

				// Validate PBL Block Live Time
				if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && BitsUtil.validateBlockReleaseTime(detailsDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTime(), detailsDTO.getUpdatedAt())) {
					detailsDTO.setTicketStatus(TicketStatusEM.PHONE_BOOKING_CANCELLED);
				}
				if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					revenueAmount = revenueAmount.add(detailsDTO.getNetRevenueAmount());
					bookedAmount = bookedAmount.add(detailsDTO.getSeatFare());
					uniqueSeatMap.put(detailsDTO.getSeatCode() + detailsDTO.getTicketCode(), detailsDTO.getSeatCode());
					if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
						phoneBlockSeatMap.put(detailsDTO.getSeatCode() + detailsDTO.getTicketCode(), detailsDTO.getSeatCode());
					}

					// multi stage booked seat count
					if (!seatSubMap.isEmpty() && seatSubMap.get(detailsDTO.getSeatCode()) != null) {
						multiStageBookedSeatMap.put(detailsDTO.getSeatCode() + detailsDTO.getTicketCode(), detailsDTO.getSeatCode());
					}
					else {
						seatSubMap.put(detailsDTO.getSeatCode(), detailsDTO.getSeatCode());
					}
				}
				else if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
					revenueAmount = revenueAmount.add(detailsDTO.getNetRevenueAmount());
					cancelledAmount = cancelledAmount.add(detailsDTO.getSeatFare());
					uniqueCancelSeatMap.put(detailsDTO.getSeatCode() + detailsDTO.getTicketCode(), detailsDTO.getSeatCode());
				}
				else if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
					cancelledAmount = cancelledAmount.add(detailsDTO.getSeatFare());
					uniqueCancelSeatMap.put(detailsDTO.getSeatCode() + detailsDTO.getTicketCode(), detailsDTO.getSeatCode());
				}

				if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() || detailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
					processUserGroupRevenue(authDTO, detailsDTO, userGroupRevenue);
				}
			}
			tripDTO.setBookedSeatCount(uniqueSeatMap.size());
			tripDTO.setMultiStageBookedSeatCount(multiStageBookedSeatMap.size());
			tripDTO.setTotalBookedAmount(bookedAmount);
			tripDTO.setCancelledSeatCount(uniqueCancelSeatMap.size());
			tripDTO.setTotalCancelledAmount(cancelledAmount);
			tripDTO.setRevenueAmount(revenueAmount.setScale(2, RoundingMode.HALF_UP));
			tripDTO.getAdditionalAttributes().put("allocatedSeatCount", String.valueOf(tripSeatVisibility.getAllocateCount()));
			tripDTO.getAdditionalAttributes().put("blockedSeatCount", String.valueOf(tripSeatVisibility.getBlockCount()));
			tripDTO.getAdditionalAttributes().put("bookedSeatCount", String.valueOf(uniqueSeatMap.size()));
			tripDTO.getAdditionalAttributes().put("phoneBlockSeatCount", String.valueOf(phoneBlockSeatMap.size()));
			tripDTO.getAdditionalAttributes().put("multiStageBookedSeatCount", String.valueOf(multiStageBookedSeatMap.size()));
			tripDTO.getAdditionalAttributes().put("cancelledSeatCount", String.valueOf(uniqueCancelSeatMap.size()));
			tripDTO.getAdditionalAttributes().put("bookedAmount", String.valueOf(bookedAmount));
			tripDTO.getAdditionalAttributes().put("cancelledAmount", String.valueOf(cancelledAmount));
			tripDTO.getAdditionalAttributes().put("revenueAmount", String.valueOf(bookedAmount));

			Gson gson = new Gson();
			String groupRevenue = gson.toJson(userGroupRevenue);
			tripDTO.setRevenue(JSONObject.fromObject(groupRevenue));

			/** Apply Break even */
			if (tripDTO.getSchedule().getScheduleBus().getBreakevenSettings() != null && tripDTO.getSchedule().getScheduleBus().getBreakevenSettings().getId() != Numeric.ZERO_INT) {
				breakevenService.processTripBreakeven(authDTO, tripDTO.getSchedule().getScheduleBus(), tripDTO, tripDTO.getStage().getFromStation().getStation());
			}
			tripService.getTripInfo(authDTO, tripDTO);

			// Dynamic Pricing Stage Fare
			if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
				for (StageDTO stageDTO : tripDTO.getStageList()) {
					ScheduleDynamicStageFareDetailsDTO dynamicPricingStage = dynamicStageFareService.getScheduleDynamicStageFare(authDTO, tripDTO.getSchedule(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation());
					if (dynamicPricingStage == null) {
						continue;
					}
					ScheduleDynamicStageFareDetailsDTO dynamicStageTripFareDetails = dynamicStageFareService.getDynamicPricingTripStageFareDetails(authDTO, tripDTO.getSchedule(), dynamicPricingStage);
					if (dynamicStageTripFareDetails == null) {
						tripDTO.getAdditionalAttributes().put(TripActivitiesEM.DYNAMIC_PRICING.getCode(), "NODPF");
						continue;
					}
					// Dynamic Seat Fare
					Map<String, BusSeatLayoutDTO> seatMap = new HashMap<String, BusSeatLayoutDTO>();
					for (BusSeatLayoutDTO seatLayoutDTO : dynamicStageTripFareDetails.getSeatFare()) {
						seatMap.put(seatLayoutDTO.getName(), seatLayoutDTO);
					}

					Map<String, BigDecimal> seatTypeFare = new HashMap<String, BigDecimal>();
					for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
						if (seatMap.get(seatLayoutDTO.getName()) != null && (seatTypeFare.get(seatLayoutDTO.getBusSeatType().getCode()) == null || seatTypeFare.get(seatLayoutDTO.getBusSeatType().getCode()).compareTo(seatMap.get(seatLayoutDTO.getName()).getFare()) <= 0)) {
							seatTypeFare.put(seatLayoutDTO.getBusSeatType().getCode(), seatMap.get(seatLayoutDTO.getName()).getFare());
						}
					}
					for (StageFareDTO stageFare : stageDTO.getStageFare()) {
						if (seatTypeFare.get(stageFare.getBusSeatType().getCode()) != null) {
							stageFare.setFare(seatTypeFare.get(stageFare.getBusSeatType().getCode()));
						}
					}
					tripDTO.getAdditionalAttributes().put(TripActivitiesEM.DYNAMIC_PRICING.getCode(), "DPFARE");
				}
			}
			tripDTO.getAdditionalAttributes().putAll(tripDTO.getSchedule().getAdditionalAttributes());
			tripDTO.getAdditionalAttributes().put("tripStatusName", BitsUtil.getTripStatusBasedOnStageTime(tripDTO));
		}

		/** Put Trip data count into Eh Cache */
		if (!activeList.isEmpty()) {
			putTripDataCountCache(authDTO, searchDTO.getTravelDate(), activeList);
		}

		return activeList;
	}

	private TripSeatVisibilityDTO processSeatVisibility(AuthDTO authDTO, TripDTO tripDTO, SearchDTO searchDTO) {
		// Prepare seat Visibility start
		DateTime now = DateUtil.NOW();
		UserDTO userDTO = authDTO.getUser();
		Map<String, List<ScheduleSeatVisibilityDTO>> allocatedMap = new HashMap<>();
		Map<String, List<ScheduleSeatVisibilityDTO>> stageSeatMap = new HashMap<>();

		// Seat Allocation and Deallocations
		List<ScheduleSeatVisibilityDTO> seatVisibilityList = visibilityService.getByScheduleId(authDTO, tripDTO.getSchedule());
		// Sorting
		Collections.sort(seatVisibilityList, new Comparator<ScheduleSeatVisibilityDTO>() {
			public int compare(ScheduleSeatVisibilityDTO previousSeatVisibility, ScheduleSeatVisibilityDTO visibilityDTO) {
				if (SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode().equals(previousSeatVisibility.getVisibilityType()) && "HIDE".equals(visibilityDTO.getVisibilityType()))
					return -1;
				return 1;
			}
		});
		for (ScheduleSeatVisibilityDTO visibilityDTO : seatVisibilityList) {
			// Seat Auto Release
			DateTime trTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getTripOriginMinutes());
			int check = DateUtil.getMinutiesDifferent(now, trTime);
			if (visibilityDTO.getReleaseMinutes() > 0 && check < visibilityDTO.getReleaseMinutes()) {
				continue;
			}
			// Stage based
			if (visibilityDTO.getRefferenceType().equals("SG") && visibilityDTO.getRouteList() != null) {
				for (BusSeatLayoutDTO seatLayoutDTO : visibilityDTO.getBus().getBusSeatLayoutDTO().getList()) {
					if (stageSeatMap.get(seatLayoutDTO.getCode()) == null) {
						List<ScheduleSeatVisibilityDTO> list = new ArrayList<ScheduleSeatVisibilityDTO>();
						list.add(visibilityDTO);
						stageSeatMap.put(seatLayoutDTO.getCode(), list);
					}
					else {
						List<ScheduleSeatVisibilityDTO> list = stageSeatMap.get(seatLayoutDTO.getCode());
						list.add(visibilityDTO);
						stageSeatMap.put(seatLayoutDTO.getCode(), list);
					}
				}
			}
			else if (visibilityDTO.getRefferenceType().equals("GR") || visibilityDTO.getRefferenceType().equals("UR") || visibilityDTO.getRefferenceType().equals("BR")) {
				for (BusSeatLayoutDTO seatLayoutDTO : visibilityDTO.getBus().getBusSeatLayoutDTO().getList()) {
					if (allocatedMap.get(seatLayoutDTO.getCode()) == null) {
						List<ScheduleSeatVisibilityDTO> list = new ArrayList<ScheduleSeatVisibilityDTO>();
						list.add(visibilityDTO);
						allocatedMap.put(seatLayoutDTO.getCode(), list);
					}
					else {
						List<ScheduleSeatVisibilityDTO> list = allocatedMap.get(seatLayoutDTO.getCode());
						list.add(visibilityDTO);
						allocatedMap.put(seatLayoutDTO.getCode(), list);
					}
				}
			}
		}

		TripSeatVisibilityDTO tripSeatVisibilityDTO = new TripSeatVisibilityDTO();
		for (StageDTO stageDTO : tripDTO.getStageList()) {
			if (stageDTO.getFromStation().getStation().getId() != tripDTO.getStage().getFromStation().getStation().getId() || stageDTO.getToStation().getStation().getId() != tripDTO.getStage().getToStation().getStation().getId()) {
				continue;
			}

			BusDTO busDTO = processStageSeatlayout(authDTO, userDTO, tripDTO, stageDTO, searchDTO, allocatedMap, stageSeatMap);
			for (BusSeatLayoutDTO seatLayoutDTO : busDTO.getBusSeatLayoutDTO().getList()) {
				if (SeatStatusEM.ALLOCATED_YOU.getId() == seatLayoutDTO.getSeatStatus().getId()) {
					tripSeatVisibilityDTO.setAllocateCount(tripSeatVisibilityDTO.getAllocateCount() + 1);
				}
				else if (SeatStatusEM.BLOCKED.getId() == seatLayoutDTO.getSeatStatus().getId() || SeatStatusEM.ALLOCATED_OTHER.getId() == seatLayoutDTO.getSeatStatus().getId() || SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId() == seatLayoutDTO.getSeatStatus().getId()) {
					tripSeatVisibilityDTO.setBlockCount(tripSeatVisibilityDTO.getBlockCount() + 1);
				}
			}
			break;
		}
		return tripSeatVisibilityDTO;
	}

	private BusDTO processStageSeatlayout(AuthDTO authDTO, UserDTO userDTO, TripDTO tripDTO, StageDTO stageDTO, SearchDTO searchDTO, Map<String, List<ScheduleSeatVisibilityDTO>> allocatedMap, Map<String, List<ScheduleSeatVisibilityDTO>> stageSeatMap) {
		List<BusSeatLayoutDTO> seatList = new ArrayList<BusSeatLayoutDTO>();
		BusDTO busDTO = new BusDTO();
		busDTO.setCode(tripDTO.getBus().getCode());

		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);

			if (allocatedMap.get(seatLayoutDTO.getCode()) != null) {
				List<ScheduleSeatVisibilityDTO> visibilityDTOList = allocatedMap.get(seatLayoutDTO.getCode());
				for (ScheduleSeatVisibilityDTO visibilityDTO : visibilityDTOList) {
					UserDTO visibilityUserDTO = BitsUtil.isUserExists(visibilityDTO.getUserList(), userDTO);
					GroupDTO visibilityGroupDTO = BitsUtil.isGroupExists(visibilityDTO.getGroupList(), userDTO.getGroup());
					OrganizationDTO visibilityOrganizationDTO = BitsUtil.isOrganizationExists(visibilityDTO.getOrganizations(), userDTO.getOrganization());

					if (visibilityDTO.getVisibilityType().equals("HIDE")) {
						if (visibilityDTO.getUserList() != null && !visibilityDTO.getUserList().isEmpty() && visibilityUserDTO != null && visibilityUserDTO.getId() != 0) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
						}
						else if (visibilityDTO.getUserList() != null && visibilityDTO.getUserList().isEmpty()) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
						}
						else if (visibilityDTO.getGroupList() != null && visibilityDTO.getGroupList().isEmpty()) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
						}
						else if (visibilityDTO.getGroupList() != null && visibilityGroupDTO != null && visibilityGroupDTO.getId() != 0) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
						}
						else if (visibilityDTO.getOrganizations() != null && !visibilityDTO.getOrganizations().isEmpty() && visibilityOrganizationDTO != null && visibilityOrganizationDTO.getId() != 0) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
						}
						else if (visibilityDTO.getOrganizations() != null && visibilityDTO.getOrganizations().isEmpty()) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
						}
					}
					else if (visibilityDTO.getVisibilityType().equals("ACAT")) {
						if (visibilityDTO.getGroupList() != null && !visibilityDTO.getGroupList().isEmpty()) {
							if (visibilityGroupDTO != null && visibilityGroupDTO.getId() != 0) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
							}
							else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
							}
						}
						else if (visibilityDTO.getUserList() != null && !visibilityDTO.getUserList().isEmpty()) {
							if (visibilityUserDTO != null && visibilityUserDTO.getId() != 0) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
							}
							else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
							}
						}
						else if (visibilityDTO.getOrganizations() != null && !visibilityDTO.getOrganizations().isEmpty()) {
							if (visibilityOrganizationDTO != null && visibilityOrganizationDTO.getId() != 0) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
							}
							else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
							}
						}
					}
					else if (visibilityDTO.getVisibilityType().equals(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode())) {
						if (visibilityDTO.getUserList() != null && !visibilityDTO.getUserList().isEmpty() && visibilityUserDTO != null && visibilityUserDTO.getId() != 0) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
						}
						else if (visibilityDTO.getUserList() != null && visibilityDTO.getUserList().isEmpty()) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
						}
						else if (visibilityDTO.getGroupList() != null && visibilityDTO.getGroupList().isEmpty()) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
						}
						else if (visibilityDTO.getGroupList() != null && visibilityGroupDTO != null && visibilityGroupDTO.getId() != 0) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
						}
					}
				}
			}

			Map<Integer, StageStationDTO> stationMap = new HashMap<>();
			for (ScheduleStationDTO stationDTO : tripDTO.getStationList()) {
				StageStationDTO stageStationDTO = new StageStationDTO();
				stageStationDTO.setMinitues(stationDTO.getMinitues());
				stageStationDTO.setStationSequence(stationDTO.getStationSequence());
				stageStationDTO.setStation(stationDTO.getStation());
				stageStationDTO.setMobileNumber(stationDTO.getMobileNumber());
				stationMap.put(stationDTO.getStation().getId(), stageStationDTO);
			}

			int fromStationSquence = searchDTO.getFromStation() != null ? stationMap.get(searchDTO.getFromStation().getId()).getStationSequence() : 0;
			int toStationSquence = searchDTO.getToStation() != null ? stationMap.get(searchDTO.getToStation().getId()).getStationSequence() : 0;

			// Stage Seat Visibility
			if (stageSeatMap.get(seatLayoutDTO.getCode()) != null) {
				List<ScheduleSeatVisibilityDTO> visibilityDTOList = stageSeatMap.get(seatLayoutDTO.getCode());
				for (ScheduleSeatVisibilityDTO visibilityDTO : visibilityDTOList) {
					RouteDTO routeDTO = BitsUtil.isRouteExists(visibilityDTO.getRouteList(), tripDTO.getStage().getFromStation().getStation(), tripDTO.getStage().getToStation().getStation());
					if (visibilityDTO.getRouteUsers() != null) {
						for (UserDTO routeuUerDTO : visibilityDTO.getRouteUsers()) {
							getUserDTO(authDTO, routeuUerDTO);
						}
					}
					UserDTO visibilityRouteUserDTO = BitsUtil.isUserExists(visibilityDTO.getRouteUsers(), userDTO);

					boolean isRouteFound = false;
					if (fromStationSquence != 0 && toStationSquence != 0 && routeDTO == null && visibilityDTO.getRouteList() != null && !visibilityDTO.getRouteList().isEmpty()) {
						for (RouteDTO visiblityRouteDTO : visibilityDTO.getRouteList()) {
							StageStationDTO visibilityFromStation = stationMap.get(visiblityRouteDTO.getFromStation().getId());
							StageStationDTO visibilityToStation = stationMap.get(visiblityRouteDTO.getToStation().getId());
							if (fromStationSquence <= visibilityFromStation.getStationSequence() && toStationSquence <= visibilityFromStation.getStationSequence()) {
								isRouteFound = true;
							}
							else if (fromStationSquence >= visibilityToStation.getStationSequence() && toStationSquence >= visibilityToStation.getStationSequence()) {
								isRouteFound = true;
							}
						}
					}

					if (visibilityDTO.getVisibilityType().equals("ACAT") && visibilityDTO.getRouteList() != null && !visibilityDTO.getRouteList().isEmpty() && routeDTO == null && !isRouteFound && visibilityDTO.getRouteUsers() != null && !visibilityDTO.getRouteUsers().isEmpty() && (visibilityRouteUserDTO == null || visibilityRouteUserDTO.getId() == 0)) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
						seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
						seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
						routeDTO = Iterables.getFirst(visibilityDTO.getRouteList(), null);
						seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
						seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
					}
					else if (visibilityDTO.getVisibilityType().equals("ACAT") && visibilityDTO.getRouteList() != null && !visibilityDTO.getRouteList().isEmpty() && routeDTO != null) {
						if (visibilityDTO.getRouteUsers() != null && !visibilityDTO.getRouteUsers().isEmpty() && (visibilityRouteUserDTO == null || visibilityRouteUserDTO.getId() == 0)) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
							seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
							seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
							routeDTO = Iterables.getFirst(visibilityDTO.getRouteList(), null);
							seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
							seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
						}
					}
					else if (visibilityDTO.getVisibilityType().equals("HIDE") && visibilityDTO.getRouteList() != null && routeDTO != null) {
						if (visibilityDTO.getRouteUsers() != null && ((visibilityRouteUserDTO != null && visibilityRouteUserDTO.getId() != 0 && !visibilityDTO.getRouteUsers().isEmpty()) || (visibilityDTO.getRouteUsers().isEmpty()))) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
							seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
							seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
						}
					}
					else if (visibilityDTO.getVisibilityType().equals("HIDE") && visibilityDTO.getRouteList() != null && visibilityDTO.getRouteList().isEmpty()) {
						if (visibilityDTO.getRouteUsers() != null && ((visibilityRouteUserDTO != null && visibilityRouteUserDTO.getId() != 0 && !visibilityDTO.getRouteUsers().isEmpty()) || (visibilityDTO.getRouteUsers().isEmpty()))) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
						}
					}
					else if (visibilityDTO.getVisibilityType().equals("HIDE") && visibilityDTO.getRouteList() != null && routeDTO == null && !isRouteFound && visibilityDTO.getRouteUsers() != null && ((visibilityRouteUserDTO != null && visibilityRouteUserDTO.getId() != 0 && !visibilityDTO.getRouteUsers().isEmpty()) || (visibilityDTO.getRouteUsers().isEmpty()))) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
					}
					else if (visibilityDTO.getVisibilityType().equals(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode()) && visibilityDTO.getRouteList() != null && visibilityDTO.getRouteList().isEmpty()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
					}
					else if (visibilityDTO.getVisibilityType().equals(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode()) && visibilityDTO.getRouteList() != null && routeDTO != null) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
						seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
						seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
					}
				}
			}
			seatList.add(seatLayoutDTO);
		}
		BusSeatLayoutDTO seatLayout = new BusSeatLayoutDTO();
		seatLayout.setList(seatList);
		busDTO.setBusSeatLayoutDTO(seatLayout);
		return busDTO;
	}

	private void processUserGroupRevenue(AuthDTO authDTO, TicketDetailsDTO ticketDetails, Map<String, TripRevenueDTO> userGroupRevenue) {
		UserDTO user = getUserDTOById(authDTO, ticketDetails.getUser());
		GroupDTO group = getGroupDTOById(authDTO, user.getGroup());

		String key = group.getCode() + Text.UNDER_SCORE + group.getName();
		if (userGroupRevenue.isEmpty() || userGroupRevenue.get(key) == null) {
			TripRevenueDTO tripRevenueDTO = new TripRevenueDTO();
			tripRevenueDTO.setRevenueAmount(ticketDetails.getNetRevenueAmount());
			if (ticketDetails.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetails.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				tripRevenueDTO.setAcBusTax(ticketDetails.getAcBusTax());
			}
			tripRevenueDTO.setSeatCount(1);
			userGroupRevenue.put(key, tripRevenueDTO);
		}
		else if (userGroupRevenue.get(key) != null) {
			TripRevenueDTO tripRevenueDTO = userGroupRevenue.get(key);
			tripRevenueDTO.setRevenueAmount(ticketDetails.getNetRevenueAmount().add(tripRevenueDTO.getRevenueAmount()));
			if (ticketDetails.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetails.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				tripRevenueDTO.setAcBusTax(ticketDetails.getAcBusTax().add(tripRevenueDTO.getAcBusTax()));
			}
			tripRevenueDTO.setSeatCount(tripRevenueDTO.getSeatCount() + 1);
			userGroupRevenue.put(key, tripRevenueDTO);
		}
	}

	public List<TripDTO> getScheduleTripList(AuthDTO authDTO, ScheduleDTO schedule, List<DateTime> tripDateList) {
		List<TripDTO> list = scheduleTripService.getScheduleTripList(authDTO, schedule, tripDateList);
		for (TripDTO tripDTO : list) {
			tripDTO.setCode(BitsUtil.getGeneratedTripCode(authDTO, tripDTO.getSchedule(), tripDTO));
		}
		List<TripDTO> activeList = tripService.saveTrip(authDTO, list);
		return activeList;
	}

	private List<ScheduleDTO> getNamespaceSearchResult(AuthDTO authDTO, SearchDTO searchDTO, List<String> scheduleCodes) {
		// get Available Stage for Given Route
		List<ScheduleDTO> finalScheduleList = new ArrayList<>();
		logger.info("Searching the result for station from " + searchDTO.getFromStation().getName() + " to station: " + searchDTO.getToStation().getName());
		ScheduleCache scheduleCache = new ScheduleCache();
		UserDTO userDTO = authDTO.getUser();

		List<ScheduleDTO> scheduleList = stageService.getScheduleSearchStage(authDTO, searchDTO.getFromStation(), searchDTO.getToStation());

		try {
			// Schedule Validations
			for (Iterator<ScheduleDTO> itrSchedule = scheduleList.iterator(); itrSchedule.hasNext();) {
				ScheduleDTO scheduleDTO = itrSchedule.next();
				ScheduleDTO schedule = scheduleCache.getScheduleDTObyId(authDTO, scheduleDTO);

				if (scheduleCodes.contains(schedule.getCode())) {
					itrSchedule.remove();
					continue;
				}
				// validate active flag
				if (schedule.getActiveFlag() != 1) {
					itrSchedule.remove();
					continue;
				}
				if (!scheduleDTO.getPreRequrities().equals("000000")) {
					itrSchedule.remove();
					continue;
				}
				// copy schedule data
				scheduleDTO.setId(schedule.getId());
				scheduleDTO.setCode(schedule.getCode());
				scheduleDTO.setActiveFrom(schedule.getActiveFrom());
				scheduleDTO.setActiveTo(schedule.getActiveTo());
				scheduleDTO.setPreRequrities(schedule.getPreRequrities());
				scheduleDTO.setDayOfWeek(schedule.getDayOfWeek());
				scheduleDTO.setOverrideList(schedule.getOverrideList());
				scheduleDTO.setName(schedule.getName());
				scheduleDTO.setDisplayName(schedule.getDisplayName());
				scheduleDTO.setApiDisplayName(StringUtil.isNotNull(schedule.getApiDisplayName()) ? schedule.getApiDisplayName() : "NA");

				// Station
				List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, scheduleDTO);
				if (stationList == null || stationList.isEmpty()) {
					itrSchedule.remove();
					continue;
				}
				// Identify Trip Date
				for (ScheduleStationDTO scheduleStationDTO : stationList) {
					if (!scheduleDTO.getScheduleStageList().isEmpty() && scheduleDTO.getScheduleStageList().get(0).getFromStation().getId() == scheduleStationDTO.getStation().getId()) {
						if (scheduleStationDTO.getMinitues() < 1440) {
							scheduleDTO.setTripDate(searchDTO.getTravelDate());
						}
						else if (scheduleStationDTO.getMinitues() >= 1440 && scheduleStationDTO.getMinitues() <= 2880) {
							scheduleDTO.setTripDate(searchDTO.getTravelDate().minusDays(1));
						}
						else if (scheduleStationDTO.getMinitues() >= 2880 && scheduleStationDTO.getMinitues() <= 4320) {
							scheduleDTO.setTripDate(searchDTO.getTravelDate().minusDays(2));
						}
						else if (scheduleStationDTO.getMinitues() >= 4320 && scheduleStationDTO.getMinitues() <= 5760) {
							scheduleDTO.setTripDate(searchDTO.getTravelDate().minusDays(3));
						}
						else if (scheduleStationDTO.getMinitues() >= 5760 && scheduleStationDTO.getMinitues() <= 7200) {
							scheduleDTO.setTripDate(searchDTO.getTravelDate().minusDays(4));
						}
					}
				}
				if (scheduleDTO.getTripDate() == null) {
					itrSchedule.remove();
					continue;
				}
				// Validate all stations
				Map<Integer, ScheduleStationDTO> stationMap = new HashMap<Integer, ScheduleStationDTO>();
				for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
					ScheduleStationDTO stationDTO = iterator.next();
					int overrideRecentDays = 0;
					// Exception and override
					for (Iterator<ScheduleStationDTO> OverrideIterator = stationDTO.getOverrideList().iterator(); OverrideIterator.hasNext();) {
						ScheduleStationDTO overrideStationDTO = OverrideIterator.next();
						// common validations
						if (StringUtil.isNotNull(overrideStationDTO.getActiveFrom()) && !scheduleDTO.getTripDate().gteq(new DateTime(overrideStationDTO.getActiveFrom()))) {
							OverrideIterator.remove();
							continue;
						}
						if (StringUtil.isNotNull(overrideStationDTO.getActiveTo()) && !scheduleDTO.getTripDate().lteq(new DateTime(overrideStationDTO.getActiveTo()))) {
							OverrideIterator.remove();
							continue;
						}
						if (overrideStationDTO.getDayOfWeek() != null && overrideStationDTO.getDayOfWeek().length() != 7) {
							OverrideIterator.remove();
							continue;
						}
						if (overrideStationDTO.getDayOfWeek() != null && overrideStationDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
							OverrideIterator.remove();
							continue;
						}
						// Remove if Exceptions
						if (overrideStationDTO.getMinitues() == -1) {
							stationDTO.setActiveFlag(-1);
							// iterator.remove();
							break;
						}
						// Override, time should follow in same day
						if (stationDTO.getMinitues() < 1440 && overrideStationDTO.getMinitues() >= 1440) {
							OverrideIterator.remove();
							continue;
						} // Second day
						else if (stationDTO.getMinitues() < 2880 && overrideStationDTO.getMinitues() >= 2880) {
							OverrideIterator.remove();
							continue;
						} // Third day
						else if (stationDTO.getMinitues() < 4320 && overrideStationDTO.getMinitues() >= 4320) {
							OverrideIterator.remove();
							continue;
						} // Fourth day
						else if (stationDTO.getMinitues() < 5760 && overrideStationDTO.getMinitues() >= 5760) {
							OverrideIterator.remove();
							continue;
						} // Fifth day
						else if (stationDTO.getMinitues() < 7200 && overrideStationDTO.getMinitues() >= 7200) {
							OverrideIterator.remove();
							continue;
						}
						if (overrideRecentDays == 0 || DateUtil.getDayDifferent(new DateTime(overrideStationDTO.getActiveFrom()), new DateTime(overrideStationDTO.getActiveTo())) <= overrideRecentDays) {
							stationDTO.setMinitues(overrideStationDTO.getMinitues());
							overrideRecentDays = DateUtil.getDayDifferent(new DateTime(overrideStationDTO.getActiveFrom()), new DateTime(overrideStationDTO.getActiveTo())) + 1;
						}
					}
					stationDTO.setStation(stationService.getStation(stationDTO.getStation()));
					stationMap.put(stationDTO.getStation().getId(), stationDTO);
				}
				if (stationList.isEmpty()) {
					itrSchedule.remove();
					continue;
				}
				DateTime tripDate = scheduleDTO.getTripDate();
				// common Schedule validations
				DateTime scheduleFromDate = new DateTime(scheduleDTO.getActiveFrom());
				DateTime scheduleEndDate = new DateTime(scheduleDTO.getActiveTo());

				if (!tripDate.gteq(scheduleFromDate)) {
					itrSchedule.remove();
					continue;
				}
				if (!tripDate.lteq(scheduleEndDate)) {
					itrSchedule.remove();
					continue;
				}
				if (scheduleDTO.getDayOfWeek() == null || scheduleDTO.getDayOfWeek().length() != 7) {
					itrSchedule.remove();
					continue;
				}
				if (scheduleDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
					itrSchedule.remove();
					continue;
				}

				// check for any exception has been added
				List<ScheduleDTO> scheduleOverrideList = scheduleDTO.getOverrideList();
				for (Iterator<ScheduleDTO> itrlookupSchedule = scheduleOverrideList.iterator(); itrlookupSchedule.hasNext();) {
					ScheduleDTO lookupscheduleDTO = itrlookupSchedule.next();
					// common validations
					if (!tripDate.gteq(new DateTime(lookupscheduleDTO.getActiveFrom()))) {
						itrlookupSchedule.remove();
						continue;
					}
					if (!tripDate.lteq(new DateTime(lookupscheduleDTO.getActiveTo()))) {
						itrlookupSchedule.remove();
						continue;
					}
					if (lookupscheduleDTO.getDayOfWeek() == null || lookupscheduleDTO.getDayOfWeek().length() != 7) {
						itrlookupSchedule.remove();
						continue;
					}
					if (lookupscheduleDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
						itrlookupSchedule.remove();
						continue;
					}
				}
				if (!scheduleOverrideList.isEmpty()) {
					itrSchedule.remove();
					continue;
				}

				// Validate all Booking Control
				List<ScheduleControlDTO> controlList = scheduleCache.getScheduleControlDTO(authDTO, scheduleDTO);
				boolean groupLevelFound = false;
				boolean stageLevelFound = false;
				for (Iterator<ScheduleControlDTO> itrControlDTO = controlList.iterator(); itrControlDTO.hasNext();) {
					ScheduleControlDTO controlDTO = itrControlDTO.next();
					// common validations
					if (controlDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(controlDTO.getActiveFrom()))) {
						itrControlDTO.remove();
						continue;
					}
					if (controlDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(controlDTO.getActiveTo()))) {
						itrControlDTO.remove();
						continue;
					}
					if (controlDTO.getDayOfWeek() != null && controlDTO.getDayOfWeek().length() != 7) {
						itrControlDTO.remove();
						continue;
					}
					if (controlDTO.getDayOfWeek() != null && controlDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
						itrControlDTO.remove();
						continue;
					}
					// alternate days
					if (controlDTO.getDayOfWeek().equals("ALRNATE") && !DateUtil.isFallonAlternateDays(new DateTime(controlDTO.getActiveFrom()), tripDate)) {
						itrControlDTO.remove();
						continue;
					}
					// Check for group level or should be default
					if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0 && controlDTO.getGroup().getId() != userDTO.getGroup().getId()) {
						itrControlDTO.remove();
						continue;
					}
					// Check for Stage based booking control
					if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0 && controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0 && (controlDTO.getFromStation().getId() != searchDTO.getFromStation().getId() || controlDTO.getToStation().getId() != searchDTO.getToStation().getId())) {
						itrControlDTO.remove();
						continue;
					}
					if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0 && controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0 && controlDTO.getFromStation().getId() == searchDTO.getFromStation().getId() && controlDTO.getToStation().getId() == searchDTO.getToStation().getId()) {
						stageLevelFound = true;
					}
					if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0 && controlDTO.getGroup().getId() == userDTO.getGroup().getId()) {
						groupLevelFound = true;
					}
					// Override and Exceptions
					for (Iterator<ScheduleControlDTO> overrideItrControlDTO = controlDTO.getOverrideList().iterator(); overrideItrControlDTO.hasNext();) {
						ScheduleControlDTO overrideControlDTO = overrideItrControlDTO.next();
						// common validations
						if (overrideControlDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(overrideControlDTO.getActiveFrom()))) {
							overrideItrControlDTO.remove();
							continue;
						}
						if (overrideControlDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(overrideControlDTO.getActiveTo()))) {
							overrideItrControlDTO.remove();
							continue;
						}
						if (overrideControlDTO.getDayOfWeek() != null && overrideControlDTO.getDayOfWeek().length() != 7) {
							overrideItrControlDTO.remove();
							continue;
						}
						if (overrideControlDTO.getDayOfWeek() != null && overrideControlDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
							overrideItrControlDTO.remove();
							continue;
						}
						// exceptions/Override for Group Level
						if (overrideControlDTO.getGroup() != null && overrideControlDTO.getGroup().getId() != 0 && overrideControlDTO.getGroup().getId() != userDTO.getGroup().getId()) {
							overrideItrControlDTO.remove();
							continue;
						}
						// Remove if Exceptions
						if (overrideControlDTO.getOpenMinitues() == -1) {
							itrControlDTO.remove();
							break;
						}
						// Apply Override
						controlDTO.setOpenMinitues(overrideControlDTO.getOpenMinitues());
						controlDTO.setCloseMinitues(overrideControlDTO.getCloseMinitues());
						controlDTO.setAllowBookingFlag(overrideControlDTO.getAllowBookingFlag());
					}
				}
				// Group level validation and check exception
				if (controlList.size() > 1 && groupLevelFound) {
					// Check for group based schedule Control
					// remove default control, if group level found
					for (Iterator<ScheduleControlDTO> iterator = controlList.iterator(); iterator.hasNext();) {
						ScheduleControlDTO controlDTO = iterator.next();
						if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != userDTO.getGroup().getId()) {
							iterator.remove();
							continue;
						}
						if (controlDTO.getGroup() == null || controlDTO.getGroup().getId() == 0) {
							iterator.remove();
							continue;
						}
					}
				}
				if (controlList.size() > 1 && stageLevelFound) {
					// remove default control, if Stage level found
					for (Iterator<ScheduleControlDTO> iterator = controlList.iterator(); iterator.hasNext();) {
						ScheduleControlDTO controlDTO = iterator.next();
						if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0 && controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0 && (controlDTO.getFromStation().getId() != searchDTO.getFromStation().getId() || controlDTO.getToStation().getId() != searchDTO.getToStation().getId())) {
							iterator.remove();
							continue;
						}
						if (controlDTO.getFromStation() == null || controlDTO.getFromStation().getId() == 0 || controlDTO.getToStation() == null || controlDTO.getToStation().getId() == 0) {
							iterator.remove();
							continue;
						}
					}
				}
				if (controlList.isEmpty()) {
					itrSchedule.remove();
					continue;
				}

				// Stage validations
				if (scheduleDTO.getScheduleStageList() == null || scheduleDTO.getScheduleStageList().isEmpty()) {
					itrSchedule.remove();
					continue;
				}

				// Identify group level fare
				boolean stageFareFoundGroupLevel = false;
				for (Iterator<ScheduleStageDTO> iterator = scheduleDTO.getScheduleStageList().iterator(); iterator.hasNext();) {
					ScheduleStageDTO scheduleStageDTO = iterator.next();
					if (stationMap.get(scheduleStageDTO.getFromStation().getId()) == null || stationMap.get(scheduleStageDTO.getToStation().getId()) == null) {
						iterator.remove();
						continue;
					}
					// Remove stage if station Exception
					if (stationMap.get(scheduleStageDTO.getFromStation().getId()).getActiveFlag() == -1 || stationMap.get(scheduleStageDTO.getToStation().getId()).getActiveFlag() == -1) {
						iterator.remove();
						continue;
					}
					if (scheduleStageDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(scheduleStageDTO.getActiveFrom()))) {
						iterator.remove();
						continue;
					}
					if (scheduleStageDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(scheduleStageDTO.getActiveTo()))) {
						iterator.remove();
						continue;
					}
					if (scheduleStageDTO.getDayOfWeek() != null && scheduleStageDTO.getDayOfWeek().length() != 7) {
						iterator.remove();
						continue;
					}
					if (scheduleStageDTO.getDayOfWeek() != null && scheduleStageDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
						iterator.remove();
						continue;
					}
					// Check for group level or should be default
					if (scheduleStageDTO.getGroup() != null && scheduleStageDTO.getGroup().getId() != 0 && scheduleStageDTO.getGroup().getId() != userDTO.getGroup().getId()) {
						iterator.remove();
						continue;
					}
					if (scheduleStageDTO.getGroup() != null && scheduleStageDTO.getGroup().getId() != 0 && scheduleStageDTO.getGroup().getId() == userDTO.getGroup().getId()) {
						stageFareFoundGroupLevel = true;
					}
					// Exceptions and Override
					for (Iterator<ScheduleStageDTO> overrideIterator = scheduleStageDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
						ScheduleStageDTO OverrideScheduleStageDTO = overrideIterator.next();
						if (!tripDate.gteq(new DateTime(OverrideScheduleStageDTO.getActiveFrom()))) {
							overrideIterator.remove();
							continue;
						}
						if (!tripDate.lteq(new DateTime(OverrideScheduleStageDTO.getActiveTo()))) {
							overrideIterator.remove();
							continue;
						}
						if (OverrideScheduleStageDTO.getDayOfWeek().length() != 7) {
							overrideIterator.remove();
							continue;
						}
						if (OverrideScheduleStageDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
							overrideIterator.remove();
							continue;
						}
						// If exceptions
						if (OverrideScheduleStageDTO.getFare() == -1) {
							iterator.remove();
							break;
						}
						// exceptions/Override for Group Level
						if (OverrideScheduleStageDTO.getGroup().getId() != 0 && OverrideScheduleStageDTO.getGroup().getId() != userDTO.getGroup().getId()) {
							overrideIterator.remove();
							continue;
						}
						// Apply Override
						if (OverrideScheduleStageDTO.getBusSeatType().getId() == scheduleStageDTO.getBusSeatType().getId()) {
							scheduleStageDTO.setFare(OverrideScheduleStageDTO.getFare());
						}
					}
					// Identify and set specific fare
					if (scheduleStageDTO.getOverrideList().size() >= 2) {
						ScheduleStageDTO recentScheduleStageDTO = null;
						for (ScheduleStageDTO stageDTO : scheduleStageDTO.getOverrideList()) {
							if (recentScheduleStageDTO == null) {
								recentScheduleStageDTO = stageDTO;
							}
							if (DateUtil.getDayDifferent(new DateTime(stageDTO.getActiveFrom()), new DateTime(stageDTO.getActiveTo())) <= DateUtil.getDayDifferent(new DateTime(recentScheduleStageDTO.getActiveFrom()), new DateTime(recentScheduleStageDTO.getActiveTo()))) {
								recentScheduleStageDTO = stageDTO;
							}
						}
						scheduleStageDTO.setFare(recentScheduleStageDTO.getFare());
					}
				}
				if (stageFareFoundGroupLevel) {
					for (Iterator<ScheduleStageDTO> iterator = scheduleDTO.getScheduleStageList().iterator(); iterator.hasNext();) {
						ScheduleStageDTO stageDTO = iterator.next();
						if (stageDTO.getGroup() != null && stageDTO.getGroup().getId() != userDTO.getGroup().getId()) {
							iterator.remove();
							continue;
						}
					}
				}

				// Schedule Station Point
				List<ScheduleStationPointDTO> stationPointList = scheduleStationPointService.getActiveScheduleStationPointList(authDTO, scheduleDTO, searchDTO, stationMap);

				if (stationPointList.isEmpty()) {
					itrSchedule.remove();
					continue;
				}
				// Sorting
				Comparator<ScheduleStationPointDTO> comp = new BeanComparator("minitues");
				Collections.sort(stationPointList, comp);
				// Bus Type and BusMap
				ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);

				// Validate all bus
				if (scheduleBusDTO == null) {
					itrSchedule.remove();
					continue;
				}
				BusCache busCache = new BusCache();
				scheduleBusDTO.setBus(busCache.getBusDTObyId(authDTO, scheduleBusDTO.getBus()));
				if (scheduleBusDTO.getBus() == null || StringUtil.isNull(scheduleBusDTO.getBus().getCode())) {
					itrSchedule.remove();
					continue;
				}

				/** Copy Schedule Bus Tax Into Schedule */
				scheduleDTO.setTax(scheduleBusDTO.getTax());

				// Apply Bus Override
				BusDTO busOverrideDTO = busOverrideService.applyScheduleBusOverride(authDTO, scheduleDTO, scheduleBusDTO.getBus());
				if (busOverrideDTO != null) {
					scheduleBusDTO.setBus(busOverrideDTO);

					if (StringUtil.isNotNull(scheduleBusDTO.getBus().getCategoryCode())) {
						List<String> categoryCodes = Arrays.asList(scheduleBusDTO.getBus().getCategoryCode().split("\\|"));
						for (String categoryCode : categoryCodes) {
							if (StringUtil.isNotNull(categoryCode) && categoryCode.equals(BusCategoryTypeEM.CLIMATE_CONTROL_NON_AC.getCode())) {
								scheduleDTO.getTax().setId(Numeric.ZERO_INT);
								scheduleDTO.getTax().setCode(null);
								break;
							}
						}
					}
				}
				// schedule Cancellation Terms
				ScheduleCancellationTermDTO cancellationTermDTO = cancellationTermService.getByScheduleId(authDTO, scheduleDTO);

				if (cancellationTermDTO == null || cancellationTermDTO.getCancellationTerm() == null) {
					itrSchedule.remove();
					continue;
				}
				// Seat Allocation and Deallocations
				List<ScheduleSeatVisibilityDTO> seatVisibilityList = visibilityService.getByScheduleId(authDTO, scheduleDTO);

				List<ScheduleSeatAutoReleaseDTO> seatAutoReleaseList = autoReleaseService.getByScheduleId(authDTO, scheduleDTO);

				List<ScheduleFareAutoOverrideDTO> autoFareOverridelist = fareOverrideService.getByScheduleId(authDTO, scheduleDTO, searchDTO.getFromStation(), searchDTO.getToStation());

				List<ScheduleTimeOverrideDTO> timeOverridelist = timeOverrideService.getByScheduleId(authDTO, scheduleDTO);

				List<ScheduleSeatFareDTO> seatFarelist = seatFareService.getByScheduleId(authDTO, scheduleDTO, searchDTO.getFromStation(), searchDTO.getToStation());

				// Schedule Discount
				ScheduleDiscountDTO scheduleDiscountDTO = discountService.getByScheduleId(authDTO, scheduleDTO);

				// Dynamic Stage Fare
				if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
					ScheduleDynamicStageFareDetailsDTO dynamicStageFare = dynamicStageFareService.getScheduleDynamicStageFare(authDTO, schedule, searchDTO.getFromStation(), searchDTO.getToStation());
					if (dynamicStageFare != null) {
						ScheduleDynamicStageFareDetailsDTO dynamicStageTripFareDetails = dynamicStageFareService.getDynamicPricingTripStageFareDetails(authDTO, schedule, dynamicStageFare);
						dynamicStageFare.setSeatFare(dynamicStageTripFareDetails != null ? dynamicStageTripFareDetails.getSeatFare() : null);
						scheduleDTO.setDynamicStageFare(dynamicStageFare);
					}
				}
				// Schedule Ticket Transfer Terms
				if ((authDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId() && BitsUtil.isTagExists(authDTO.getUser().getUserTags(), UserTagEM.API_USER_RB)) || authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId()) {
					ScheduleTicketTransferTermsDTO scheduleTicketTransferTerms = scheduleTicketTransferTermsService.getScheduleTicketTransferTermsBySchedule(authDTO, schedule, searchDTO.getFromStation(), searchDTO.getToStation());
					scheduleDTO.setTicketTransferTerms(scheduleTicketTransferTerms);
				}

				// collect other stages list to check blocked/booked seat status
				List<ScheduleStageDTO> otherScheduleStageList = scheduleCache.getScheduleStageDTO(authDTO, scheduleDTO);

				/** Apply Schedule Tax */
				/** Apply GST Exception */
				if (scheduleDTO.getTax() != null && BitsUtil.isTagExists(authDTO.getUser().getUserTags(), Constants.GST_EXCEPTION_TAG) || authDTO.getNamespace().getProfile().isGstExceptionGroup(authDTO.getUser().getGroup())) {
					scheduleDTO.getTax().setCgstValue(BigDecimal.ZERO);
					scheduleDTO.getTax().setSgstValue(BigDecimal.ZERO);
					scheduleDTO.getTax().setUgstValue(BigDecimal.ZERO);
					scheduleDTO.getTax().setIgstValue(BigDecimal.ZERO);
					scheduleDTO.getTax().setId(Numeric.ZERO_INT);
					scheduleDTO.getTax().setGstin(Text.NA);
					scheduleDTO.getTax().setTradeName(Text.NA);
				}
				else if (scheduleDTO.getTax() != null && scheduleDTO.getTax().getId() != 0) {
					scheduleDTO.setTax(taxService.getTaxbyStateV2(authDTO, scheduleDTO.getTax(), searchDTO.getFromStation().getState()));
				}

				// add to schedule
				scheduleDTO.setScheduleBus(scheduleBusDTO);
				scheduleDTO.setSeatVisibilityList(seatVisibilityList);
				scheduleDTO.setSeatAutoReleaseList(seatAutoReleaseList);
				scheduleDTO.setStationList(stationList);
				scheduleDTO.setStationPointList(stationPointList);
				scheduleDTO.setControlList(controlList);
				scheduleDTO.setOtherSscheduleStageList(otherScheduleStageList);
				scheduleDTO.setFareAutoOverrideList(autoFareOverridelist);
				scheduleDTO.setTimeOverrideList(timeOverridelist);
				scheduleDTO.setSeatFareList(seatFarelist);
				scheduleDTO.setScheduleDiscount(scheduleDiscountDTO);
				scheduleDTO.setCancellationTerm(cancellationTermDTO.getCancellationTerm());
				finalScheduleList.add(scheduleDTO);
			}
		}
		catch (Exception e) {
			System.out.println("ERRR10" + "-" + authDTO.getNamespaceCode() + ":" + searchDTO.getFromStation().getCode() + ":" + searchDTO.getToStation().getCode() + ":" + searchDTO.getTravelDate());
			e.printStackTrace();
		}

		return finalScheduleList;
	}

	protected List<TripDTO> convertScheduleToTrip(AuthDTO authDTO, SearchDTO searchDTO, List<ScheduleDTO> scheduleList, List<String> scheduleCodes) {
		List<TripDTO> tripList = new ArrayList<>();
		UserDTO userDTO = authDTO.getUser();
		try {
			DateTime now = DateUtil.NOW();
			for (Iterator<ScheduleDTO> scheIterator = scheduleList.iterator(); scheIterator.hasNext();) {
				ScheduleDTO scheduleDTO = scheIterator.next();

				// Schedule Stage
				if (scheduleDTO.getScheduleStageList().isEmpty()) {
					scheIterator.remove();
					continue;
				}

				// Schedule Station
				if (scheduleDTO.getStationList().isEmpty()) {
					scheIterator.remove();
					continue;
				}

				// Schedule Station Point
				if (scheduleDTO.getStationPointList().isEmpty()) {
					scheIterator.remove();
					continue;
				}

				// Booking Control
				if (scheduleDTO.getControlList().isEmpty()) {
					scheIterator.remove();
					continue;
				}

				// Schedule Bus
				if (scheduleDTO.getScheduleBus() == null) {
					scheIterator.remove();
					continue;
				}
				// Schedule Cancellation Terms
				if (scheduleDTO.getCancellationTerm() == null) {
					scheIterator.remove();
					continue;
				}
				// Copy to Trip
				TripDTO tripDTO = new TripDTO();
				tripDTO.setTripDate(scheduleDTO.getTripDate());
				tripDTO.setCode(getGeneratedTripCode(authDTO, scheduleDTO, tripDTO));
				tripDTO.setSchedule(scheduleDTO);
				tripDTO.setBus(scheduleDTO.getScheduleBus().getBus());
				tripDTO.setAmenities(scheduleDTO.getScheduleBus().getAmentiesList());
				tripDTO.setStationList(scheduleDTO.getStationList());
				tripDTO.getAdditionalAttributes().putAll(scheduleDTO.getAdditionalAttributes());

				// Station time override
				for (ScheduleTimeOverrideDTO overrideDTO : scheduleDTO.getTimeOverrideList()) {
					ScheduleStationDTO reactionStationDTO = null;
					for (ScheduleStationDTO stationDTO : scheduleDTO.getStationList()) {
						if (overrideDTO.getStation().getId() == stationDTO.getStation().getId()) {
							stationDTO.setMinitues(getStationTimeOverride(overrideDTO, stationDTO.getMinitues()));

							if (overrideDTO.isReactionFlag()) {
								reactionStationDTO = stationDTO;
							}
						}
					}
					for (ScheduleStationDTO stationDTO : scheduleDTO.getStationList()) {
						if (reactionStationDTO != null && stationDTO.getStationSequence() > reactionStationDTO.getStationSequence()) {
							stationDTO.setMinitues(getStationTimeOverride(overrideDTO, stationDTO.getMinitues()));
						}
					}
				}
				Map<Integer, StageStationDTO> stationMap = new HashMap<>();
				for (Iterator<ScheduleStationDTO> iterator = scheduleDTO.getStationList().iterator(); iterator.hasNext();) {
					ScheduleStationDTO stationDTO = iterator.next();
					StageStationDTO stageStationDTO = new StageStationDTO();
					stageStationDTO.setMinitues(stationDTO.getMinitues());
					stageStationDTO.setStationSequence(stationDTO.getStationSequence());
					stageStationDTO.setStation(stationDTO.getStation());
					stageStationDTO.setMobileNumber(stationDTO.getMobileNumber());
					stationMap.put(stationDTO.getStation().getId(), stageStationDTO);
				}
				for (Iterator<ScheduleStationPointDTO> iterator = scheduleDTO.getStationPointList().iterator(); iterator.hasNext();) {
					ScheduleStationPointDTO pointDTO = iterator.next();
					if (stationMap.get(pointDTO.getStation().getId()) != null) {
						StageStationDTO stageStationDTO = stationMap.get(pointDTO.getStation().getId());
						StationPointDTO stationPointDTO = new StationPointDTO();
						stationPointDTO.setId(pointDTO.getStationPoint().getId());
						// Copy station Point from cache
						stationPointService.getStationPoint(authDTO, stationPointDTO);
						if (!stationPointDTO.isActive()) {
							iterator.remove();
							continue;
						}
						stationPointDTO.setCreditDebitFlag(pointDTO.getCreditDebitFlag());
						stationPointDTO.setMinitues(pointDTO.getMinitues());
						stationPointDTO.setFare(pointDTO.getFare());
						if (StringUtil.isNotNull(pointDTO.getAddress())) {
							stationPointDTO.setLandmark(Text.NA);
							stationPointDTO.setNumber(Text.NA);
							stationPointDTO.setAddress(pointDTO.getAddress());
						}
						if (pointDTO.getAmenities() != null && !pointDTO.getAmenities().isEmpty()) {
							stationPointDTO.setAmenities(pointDTO.getAmenities());
						}
						if (StringUtil.isNotNull(pointDTO.getMobileNumber())) {
							stationPointDTO.setNumber(pointDTO.getMobileNumber());
						}
						if (StringUtil.isNotNull(stageStationDTO.getMobileNumber()) && StringUtil.isNotNull(stationPointDTO.getNumber())) {
							stationPointDTO.setNumber(stageStationDTO.getMobileNumber() + " / " + stationPointDTO.getNumber());
						}
						else if (StringUtil.isNotNull(stageStationDTO.getMobileNumber()) && StringUtil.isNull(stationPointDTO.getNumber())) {
							stationPointDTO.setNumber(stageStationDTO.getMobileNumber());
						}
						if (pointDTO.getBusVehicleVanPickup() != null && pointDTO.getBusVehicleVanPickup().getId() != 0) {
							stationPointDTO.setName(stationPointDTO.getName() + " (Van Pickup)");
						}
						stageStationDTO.getStationPoint().add(stationPointDTO);
						stationMap.put(stageStationDTO.getStation().getId(), stageStationDTO);
					}
					else {
						iterator.remove();
						continue;
					}
				}
				// Identify Stage and fare with trip
				// bus-type(using-bus-override)
				List<ScheduleStageDTO> scheduleStageDTOList = new ArrayList<ScheduleStageDTO>();
				Map<String, BusSeatTypeEM> bustype = scheduleDTO.getScheduleBus().getBus().getUniqueReservableBusType();
				Map<String, BusSeatTypeEM> stageFareBustype = scheduleDTO.getUniqueStageBusType();
				for (BusSeatTypeEM seatTypeEM : new ArrayList<BusSeatTypeEM>(bustype.values())) {
					for (ScheduleStageDTO scheduleStageDTO : scheduleDTO.getScheduleStageList()) {
						if (seatTypeEM.getCode().equals(scheduleStageDTO.getBusSeatType().getCode())) {
							scheduleStageDTOList.add(scheduleStageDTO);
						}
						else if (stageFareBustype.get(seatTypeEM.getCode()) == null && stageFareBustype.get(seatTypeEM.getCode()) == null) {
							ScheduleStageDTO scheduleStage = scheduleStageDTO.clone();
							scheduleStage.setBusSeatType(seatTypeEM);
							stageFareBustype.put(seatTypeEM.getCode(), seatTypeEM);
							scheduleStageDTOList.add(scheduleStage);
						}
					}
				}

				Map<String, StageDTO> fareMap = new HashMap<>();
				for (Iterator<ScheduleStageDTO> iterator = scheduleStageDTOList.iterator(); iterator.hasNext();) {
					ScheduleStageDTO scheduleStageDTO = iterator.next();
					StageDTO stageDTO = new StageDTO();
					if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
						stageDTO.setFromStation(stationMap.get(scheduleStageDTO.getFromStation().getId()));
						stageDTO.setToStation(stationMap.get(scheduleStageDTO.getToStation().getId()));
						stageDTO.getFromStation().setStation(stationService.getStation(scheduleStageDTO.getFromStation()));
						stageDTO.getToStation().setStation(stationService.getStation(scheduleStageDTO.getToStation()));
						StageFareDTO stageFareDTO = new StageFareDTO();
						stageFareDTO.setFare(new BigDecimal(scheduleStageDTO.getFare()));
						stageFareDTO.setBusSeatType(scheduleStageDTO.getBusSeatType());
						if (scheduleStageDTO.getGroup() != null) {
							stageFareDTO.setGroup(scheduleStageDTO.getGroup());
						}
						if (fareMap.get(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId()) == null) {
							List<StageFareDTO> fareList = new ArrayList<>();
							stageDTO.setStageFare(fareList);
							fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
						}
						List<StageFareDTO> fareList = (fareMap.get(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId())).getStageFare();

						// Schedule Fare auto override
						if (scheduleDTO.getFareAutoOverrideList() != null && !scheduleDTO.getFareAutoOverrideList().isEmpty()) {
							List<ScheduleFareAutoOverrideDTO> overridelist = getFareAutoOverrideList(authDTO, scheduleDTO, stageDTO, scheduleStageDTO.getBusSeatType());
							stageFareDTO.setFare(applyFareAutoOverride(stageDTO, scheduleStageDTO.getFare(), overridelist, scheduleDTO.getTripDate(), scheduleStageDTO.getBusSeatType()));
						}

						// Apply Schedule based Discount if Any
						if (scheduleDTO.getScheduleDiscount() != null && scheduleDTO.getScheduleDiscount().getFemaleDiscountFlag() != 1) {
							BigDecimal discountFare = BigDecimal.ZERO;
							if (scheduleDTO.getScheduleDiscount().getPercentageFlag() == 0) {
								discountFare = scheduleDTO.getScheduleDiscount().getDiscountValue();
							}
							else if (scheduleDTO.getScheduleDiscount().getPercentageFlag() == 1) {
								discountFare = stageFareDTO.getFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(scheduleDTO.getScheduleDiscount().getDiscountValue());
							}
							stageFareDTO.setDiscountFare(discountFare.setScale(0, RoundingMode.HALF_UP));
						}
						stageFareDTO.setFare(stageFareDTO.getFare().setScale(2, RoundingMode.HALF_UP));
						fareList.add(stageFareDTO);
						stageDTO.setStageFare(fareList);
						stageDTO.setCode(getGeneratedTripStageCode(authDTO, scheduleDTO, tripDTO, scheduleStageDTO));
						fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);

					}
					else {
						iterator.remove();
						continue;
					}
					if (stageDTO.getFromStation().getStationPoint().isEmpty() || stageDTO.getToStation().getStationPoint().isEmpty()) {
						iterator.remove();
						continue;
					}
				}

				// Schedule Seat Fare
				if (scheduleDTO.getSeatFareList() != null && !scheduleDTO.getSeatFareList().isEmpty()) {
					Map<String, BusSeatLayoutDTO> seatMap = new HashMap<String, BusSeatLayoutDTO>();
					for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
						seatMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
					}
					for (ScheduleSeatFareDTO scheduleSeatFareDTO : scheduleDTO.getSeatFareList()) {
						for (BusSeatLayoutDTO seatLayoutDTO : scheduleSeatFareDTO.getBus().getBusSeatLayoutDTO().getList()) {
							if (seatMap.get(seatLayoutDTO.getCode()) != null) {
								seatMap.get(seatLayoutDTO.getCode()).setFare(calculateSeatFare(scheduleSeatFareDTO, fareMap.entrySet().iterator().next().getValue().getSeatFare(seatMap.get(seatLayoutDTO.getCode()).getBusSeatType())));
							}
						}
					}

				}

				// Apply Dynamic Price at Seat level
				if (scheduleDTO.getDynamicStageFare() != null && scheduleDTO.getDynamicStageFare().getSeatFare() != null && !scheduleDTO.getDynamicStageFare().getSeatFare().isEmpty()) {
					Map<String, BusSeatLayoutDTO> seatMap = new HashMap<String, BusSeatLayoutDTO>();
					for (BusSeatLayoutDTO seatLayoutDTO : scheduleDTO.getDynamicStageFare().getSeatFare()) {
						seatMap.put(seatLayoutDTO.getName(), seatLayoutDTO);
					}
					for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
						if (seatMap.get(seatLayoutDTO.getName()) != null) {
							seatLayoutDTO.setFare(seatMap.get(seatLayoutDTO.getName()).getFare());
						}
						else {
							seatLayoutDTO.setFare(BigDecimal.ZERO);
						}
						/** Apply Schedule Discount */
						if (scheduleDTO.getScheduleDiscount() != null && scheduleDTO.getScheduleDiscount().getFemaleDiscountFlag() != 1) {
							BigDecimal discountFare = BigDecimal.ZERO;
							if (scheduleDTO.getScheduleDiscount().getPercentageFlag() == 0) {
								discountFare = scheduleDTO.getScheduleDiscount().getDiscountValue();
							}
							else if (scheduleDTO.getScheduleDiscount().getPercentageFlag() == 1) {
								discountFare = seatLayoutDTO.getFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(scheduleDTO.getScheduleDiscount().getDiscountValue());
							}
							seatLayoutDTO.setDiscountFare(discountFare.setScale(0, RoundingMode.HALF_UP));
						}
					}
				}
				// DP Fare status expose
				if (scheduleDTO.getDynamicStageFare() != null && scheduleDTO.getDynamicStageFare().getSeatFare() != null && !scheduleDTO.getDynamicStageFare().getSeatFare().isEmpty()) {
					tripDTO.getAdditionalAttributes().put(TripActivitiesEM.DYNAMIC_PRICING.getCode(), "DPFARE");
				}
				else if (scheduleDTO.getDynamicStageFare() != null && (scheduleDTO.getDynamicStageFare().getSeatFare() == null || scheduleDTO.getDynamicStageFare().getSeatFare().isEmpty())) {
					tripDTO.getAdditionalAttributes().put(TripActivitiesEM.DYNAMIC_PRICING.getCode(), "NODPF");
				}

				// Validate Min and Max fare of namespace TODO:

				// other stage Filter, identify other stages
				for (Iterator<ScheduleStageDTO> itrStageDTO = scheduleDTO.getOtherSscheduleStageList().iterator(); itrStageDTO.hasNext();) {
					ScheduleStageDTO scheduleStageDTO = itrStageDTO.next();
					if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
						scheduleStageDTO.setFromStationSequence(stationMap.get(scheduleStageDTO.getFromStation().getId()).getStationSequence());
						scheduleStageDTO.setToStationSequence(stationMap.get(scheduleStageDTO.getToStation().getId()).getStationSequence());
					}
					else {
						itrStageDTO.remove();
					}
				}
				// If Fare not available, may be due to station exceptions
				if (fareMap.isEmpty()) {
					scheIterator.remove();
					continue;
				}
				Collection<StageDTO> dtStageDTOsos = fareMap.values();
				for (StageDTO stageDTO : dtStageDTOsos) {
					// Advance Booking Validations
					for (Iterator<ScheduleControlDTO> itrControlDTO = scheduleDTO.getControlList().iterator(); itrControlDTO.hasNext();) {
						ScheduleControlDTO controlDTO = itrControlDTO.next();
						// Identify Trip origin station or Stage wise Open Close
						int tripStageOriginStationOpenMinutes = stageDTO.getFromStation().getMinitues();
						int tripStageOriginStationCloseMinutes = stageDTO.getFromStation().getMinitues();
						if (controlDTO.getFromStation() == null && controlDTO.getToStation() == null) {
							tripStageOriginStationOpenMinutes = tripDTO.getTripOriginMinutes();
						}
						if (controlDTO.getAllowBookingFlag() != 1) {
							itrControlDTO.remove();
							stageDTO.setStageStatus(TripStatusEM.TRIP_CLOSED);
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationCloseMinutes));
							continue;
						}

						if (controlDTO.getOpenMinitues() > 14400) {
							tripStageOriginStationOpenMinutes = 0;
						}

						int minutiesOpenDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationOpenMinutes));
						int minutiesCloseDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationCloseMinutes));
						if (minutiesOpenDiff >= controlDTO.getOpenMinitues()) {
							stageDTO.setStageStatus(TripStatusEM.TRIP_YET_OPEN);
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationOpenMinutes - controlDTO.getCloseMinitues()));
							itrControlDTO.remove();
							continue;
						}
						if (controlDTO.getCloseMinitues() != -1 && minutiesCloseDiff <= controlDTO.getCloseMinitues()) {
							itrControlDTO.remove();
							stageDTO.setStageStatus(TripStatusEM.TRIP_CLOSED);
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationOpenMinutes - controlDTO.getCloseMinitues()));
							continue;
						}
						// Identify Close time
						if (controlDTO.getCloseMinitues() == -1 && stageDTO.getFromStation().getStationPoint().size() >= 1) {
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), stageDTO.getFromStation().getMinitues() + stageDTO.getFromStation().getStationPoint().get(stageDTO.getFromStation().getStationPoint().size() - 1).getMinitues()));
						}
						else {
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), stageDTO.getFromStation().getMinitues() - controlDTO.getCloseMinitues()));
						}
						if (minutiesOpenDiff <= controlDTO.getOpenMinitues()) {
							stageDTO.setStageStatus(TripStatusEM.TRIP_OPEN);
							itrControlDTO.remove();
							continue;
						}
					}
					if (stageDTO.getStageStatus() == null) {
						stageDTO.setStageStatus(TripStatusEM.TRIP_CLOSED);
					}
					tripDTO.setStage(stageDTO);
				}
				// If next day travel date
				int tripFromStationMinitues = tripDTO.getStage().getFromStation().getMinitues();
				if (tripDTO.getStage().getFromStation().getMinitues() > 1440 && tripDTO.getStage().getFromStation().getMinitues() <= 2880) {
					tripFromStationMinitues = tripDTO.getStage().getFromStation().getMinitues() - 1440;
				}
				else if (tripDTO.getStage().getFromStation().getMinitues() > 2880 && tripDTO.getStage().getFromStation().getMinitues() <= 4320) {
					tripFromStationMinitues = tripDTO.getStage().getFromStation().getMinitues() - 2880;
				}
				else if (tripDTO.getStage().getFromStation().getMinitues() > 4320 && tripDTO.getStage().getFromStation().getMinitues() <= 5760) {
					tripFromStationMinitues = tripDTO.getStage().getFromStation().getMinitues() - 4320;
				}
				else if (tripDTO.getStage().getFromStation().getMinitues() > 5760 && tripDTO.getStage().getFromStation().getMinitues() <= 7200) {
					tripFromStationMinitues = tripDTO.getStage().getFromStation().getMinitues() - 5760;
				}
				// check current time with boarding point time
				if (DateUtil.addMinituesToDate(searchDTO.getTravelDate(), tripFromStationMinitues).lteq(now)) {
					int stationPointFound = 0;
					for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
						if (DateUtil.addMinituesToDate(searchDTO.getTravelDate(), tripFromStationMinitues + pointDTO.getMinitues()).gteq(now)) {
							stationPointFound = 1;
						}
					}
					if (stationPointFound != 1 && tripDTO.getStage().getStageStatus().getId() == TripStatusEM.TRIP_OPEN.getId()) {
						tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_CLOSED);
					}
				}

				// Identify co-releated schedules stage,
				int fromStationSquence = stationMap.get(tripDTO.getStage().getFromStation().getStation().getId()).getStationSequence();
				int toStationSquence = stationMap.get(tripDTO.getStage().getToStation().getStation().getId()).getStationSequence();
				List<String> releatedStageCodeList = new ArrayList<>();
				for (Iterator<ScheduleStageDTO> itrStageDTO = scheduleDTO.getOtherSscheduleStageList().iterator(); itrStageDTO.hasNext();) {
					ScheduleStageDTO scheduleStageDTO = itrStageDTO.next();
					if (scheduleStageDTO.getToStationSequence() <= fromStationSquence) {
						itrStageDTO.remove();
						continue;
					}
					if (scheduleStageDTO.getFromStationSequence() >= toStationSquence) {
						itrStageDTO.remove();
						continue;
					}
					releatedStageCodeList.add(getGeneratedTripStageCode(authDTO, scheduleDTO, tripDTO, scheduleStageDTO));

				}
				tripDTO.setReleatedStageCodeList(releatedStageCodeList);

				// Validate seat Visibility
				Map<String, List<ScheduleSeatVisibilityDTO>> allocatedMap = new HashMap<>();
				Map<String, List<ScheduleSeatVisibilityDTO>> stageSeatMap = new HashMap<>();

				// Sorting
				Collections.sort(scheduleDTO.getSeatVisibilityList(), new Comparator<ScheduleSeatVisibilityDTO>() {
					public int compare(ScheduleSeatVisibilityDTO previousSeatVisibility, ScheduleSeatVisibilityDTO visibilityDTO) {
						if (SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode().equals(previousSeatVisibility.getVisibilityType()) && "HIDE".equals(visibilityDTO.getVisibilityType()))
							return -1;
						return 1;
					}
				});

				for (Iterator<ScheduleSeatVisibilityDTO> itrSeatVisibility = scheduleDTO.getSeatVisibilityList().iterator(); itrSeatVisibility.hasNext();) {
					ScheduleSeatVisibilityDTO visibilityDTO = itrSeatVisibility.next();

					// Seat Auto Release
					DateTime trTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getTripOriginMinutes());
					Integer check = DateUtil.getMinutiesDifferent(now, trTime);
					if (visibilityDTO.getReleaseMinutes() > 0 && check < visibilityDTO.getReleaseMinutes()) {
						continue;
					}
					// Stage based
					if (visibilityDTO.getRefferenceType().equals("SG") && visibilityDTO.getRouteList() != null) {
						for (Iterator<BusSeatLayoutDTO> itrlayout = visibilityDTO.getBus().getBusSeatLayoutDTO().getList().iterator(); itrlayout.hasNext();) {
							BusSeatLayoutDTO seatLayoutDTO = itrlayout.next();
							if (stageSeatMap.get(seatLayoutDTO.getCode()) == null) {
								List<ScheduleSeatVisibilityDTO> list = new ArrayList<ScheduleSeatVisibilityDTO>();
								list.add(visibilityDTO);
								stageSeatMap.put(seatLayoutDTO.getCode(), list);
							}
							else {
								List<ScheduleSeatVisibilityDTO> list = stageSeatMap.get(seatLayoutDTO.getCode());
								list.add(visibilityDTO);
								stageSeatMap.put(seatLayoutDTO.getCode(), list);
							}
						}
					}
					else if (visibilityDTO.getRefferenceType().equals("GR") || visibilityDTO.getRefferenceType().equals("UR") || visibilityDTO.getRefferenceType().equals("BR")) {

						// User/Group based
						for (Iterator<BusSeatLayoutDTO> itrlayout = visibilityDTO.getBus().getBusSeatLayoutDTO().getList().iterator(); itrlayout.hasNext();) {
							BusSeatLayoutDTO seatLayoutDTO = itrlayout.next();
							if (allocatedMap.get(seatLayoutDTO.getCode()) == null) {
								List<ScheduleSeatVisibilityDTO> list = new ArrayList<ScheduleSeatVisibilityDTO>();
								list.add(visibilityDTO);
								allocatedMap.put(seatLayoutDTO.getCode(), list);
							}
							else {
								List<ScheduleSeatVisibilityDTO> list = allocatedMap.get(seatLayoutDTO.getCode());
								list.add(visibilityDTO);
								allocatedMap.put(seatLayoutDTO.getCode(), list);
							}
						}
					}
				}
				// Auto Release validations
				ScheduleSeatAutoReleaseDTO scheduleSeatAutoRelease = null;
				Collections.sort(scheduleDTO.getSeatAutoReleaseList(), new Comparator<ScheduleSeatAutoReleaseDTO>() {
					@Override
					public int compare(ScheduleSeatAutoReleaseDTO t1, ScheduleSeatAutoReleaseDTO t2) {
						return new CompareToBuilder().append(t2.getActiveFrom(), t1.getActiveFrom()).append(t2.getActiveTo(), t1.getActiveTo()).toComparison();
					}
				});

				for (Iterator<ScheduleSeatAutoReleaseDTO> itrAutoRelease = scheduleDTO.getSeatAutoReleaseList().iterator(); itrAutoRelease.hasNext();) {
					ScheduleSeatAutoReleaseDTO autoRelaseDTO = itrAutoRelease.next();
					if (autoRelaseDTO.getReleaseTypeEM().getId() != ReleaseTypeEM.RELEASE_ACAT.getId() && autoRelaseDTO.getReleaseTypeEM().getId() != ReleaseTypeEM.RELEASE_HIDE.getId()) {
						itrAutoRelease.remove();
						continue;
					}

					if (MinutesTypeEM.MINUTES.getId() == autoRelaseDTO.getMinutesTypeEM().getId()) {
						DateTime trTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues());
						Integer check = DateUtil.getMinutiesDifferent(now, trTime);
						if (check > autoRelaseDTO.getReleaseMinutes()) {
							itrAutoRelease.remove();
							break;
						}
					}
					// AM
					else if (MinutesTypeEM.AM.getId() == autoRelaseDTO.getMinutesTypeEM().getId()) {
						DateTime checkTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), autoRelaseDTO.getReleaseMinutes());
						Integer check = DateUtil.getMinutiesDifferent(now, checkTime);
						if (check > 0) {
							itrAutoRelease.remove();
							break;
						}
					}
					// PM
					else if (MinutesTypeEM.PM.getId() == autoRelaseDTO.getMinutesTypeEM().getId()) {
						DateTime checkTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), 720 + autoRelaseDTO.getReleaseMinutes());
						Integer check = DateUtil.getMinutiesDifferent(now, checkTime);
						if (check > 0) {
							itrAutoRelease.remove();
							break;
						}
					}
					if (scheduleSeatAutoRelease == null) {
						scheduleSeatAutoRelease = autoRelaseDTO;
						continue;
					}
					if (DateUtil.getDayDifferent(DateUtil.getDateTime(autoRelaseDTO.getActiveFrom()), DateUtil.getDateTime(autoRelaseDTO.getActiveTo())) > DateUtil.getDayDifferent(DateUtil.getDateTime(scheduleSeatAutoRelease.getActiveFrom()), DateUtil.getDateTime(scheduleSeatAutoRelease.getActiveTo()))) {
						itrAutoRelease.remove();
						continue;
					}
				}

				// Apply Seat Visibility
				for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
					seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);

					if (allocatedMap.get(seatLayoutDTO.getCode()) != null) {
						List<ScheduleSeatVisibilityDTO> visibilityDTOList = allocatedMap.get(seatLayoutDTO.getCode());
						for (ScheduleSeatVisibilityDTO visibilityDTO : visibilityDTOList) {
							UserDTO visibilityUserDTO = BitsUtil.isUserExists(visibilityDTO.getUserList(), userDTO);
							GroupDTO visibilityGroupDTO = BitsUtil.isGroupExists(visibilityDTO.getGroupList(), userDTO.getGroup());
							OrganizationDTO visibilityOrganizationDTO = BitsUtil.isOrganizationExists(visibilityDTO.getOrganizations(), userDTO.getOrganization());

							if (visibilityDTO.getVisibilityType().equals("HIDE")) {
								if (visibilityDTO.getUserList() != null && !visibilityDTO.getUserList().isEmpty() && visibilityUserDTO != null && visibilityUserDTO.getId() != 0) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
									seatLayoutDTO.setUser(visibilityUserDTO);
								}
								else if (visibilityDTO.getUserList() != null && visibilityDTO.getUserList().isEmpty()) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								}
								else if (visibilityDTO.getGroupList() != null && visibilityDTO.getGroupList().isEmpty()) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								}
								else if (visibilityDTO.getGroupList() != null && visibilityGroupDTO != null && visibilityGroupDTO.getId() != 0) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
									seatLayoutDTO.setGroup(visibilityGroupDTO);
								}
								else if (visibilityDTO.getOrganizations() != null && !visibilityDTO.getOrganizations().isEmpty() && visibilityOrganizationDTO != null && visibilityOrganizationDTO.getId() != 0) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
									seatLayoutDTO.setOrganization(visibilityOrganizationDTO);
								}
								else if (visibilityDTO.getOrganizations() != null && visibilityDTO.getOrganizations().isEmpty()) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								}
							}
							else if (visibilityDTO.getVisibilityType().equals("ACAT")) {
								if (visibilityDTO.getGroupList() != null && !visibilityDTO.getGroupList().isEmpty()) {
									if (visibilityGroupDTO != null && visibilityGroupDTO.getId() != 0) {
										seatLayoutDTO.setGroup(visibilityGroupDTO);
										seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
									}
									else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
										seatLayoutDTO.setGroup(Iterables.getFirst(visibilityDTO.getGroupList(), null));
										seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
									}
								}
								else if (visibilityDTO.getUserList() != null && !visibilityDTO.getUserList().isEmpty()) {
									if (visibilityUserDTO != null && visibilityUserDTO.getId() != 0) {
										seatLayoutDTO.setUser(visibilityUserDTO);
										seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
									}
									else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
										seatLayoutDTO.setUser(Iterables.getFirst(visibilityDTO.getUserList(), null));
										seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
									}
								}
								else if (visibilityDTO.getOrganizations() != null && !visibilityDTO.getOrganizations().isEmpty()) {
									if (visibilityOrganizationDTO != null && visibilityOrganizationDTO.getId() != 0) {
										seatLayoutDTO.setOrganization(visibilityOrganizationDTO);
										seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
									}
									else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
										seatLayoutDTO.setOrganization(Iterables.getFirst(visibilityDTO.getOrganizations(), null));
										seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
									}
								}
							}
							else if (visibilityDTO.getVisibilityType().equals(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode())) {
								if (visibilityDTO.getUserList() != null && !visibilityDTO.getUserList().isEmpty() && visibilityUserDTO != null && visibilityUserDTO.getId() != 0) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
									seatLayoutDTO.setUser(visibilityUserDTO);
								}
								else if (visibilityDTO.getUserList() != null && visibilityDTO.getUserList().isEmpty()) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
								}
								else if (visibilityDTO.getGroupList() != null && visibilityDTO.getGroupList().isEmpty()) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
								}
								else if (visibilityDTO.getGroupList() != null && visibilityGroupDTO != null && visibilityGroupDTO.getId() != 0) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
									seatLayoutDTO.setGroup(visibilityGroupDTO);
								}
							}
							// Apply Auto Release
							for (ScheduleSeatAutoReleaseDTO seatAutoRelease : scheduleDTO.getSeatAutoReleaseList()) {
								GroupDTO visibilityGroup = BitsUtil.isGroupExists(visibilityDTO.getGroupList(), seatAutoRelease.getGroups());
								if (visibilityDTO.getGroupList() != null && seatAutoRelease.getGroups() != null && !seatAutoRelease.getGroups().isEmpty() && visibilityGroup == null) {
									continue;
								}
								if (visibilityDTO.getVisibilityType().equals(seatAutoRelease.getReleaseTypeEM().getCode())) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
								}
							}
						}
					}
					// Stage Seat Visibility
					if (stageSeatMap.get(seatLayoutDTO.getCode()) != null) {
						List<ScheduleSeatVisibilityDTO> visibilityDTOList = stageSeatMap.get(seatLayoutDTO.getCode());
						for (ScheduleSeatVisibilityDTO visibilityDTO : visibilityDTOList) {
							RouteDTO routeDTO = BitsUtil.isRouteExists(visibilityDTO.getRouteList(), tripDTO.getStage().getFromStation().getStation(), tripDTO.getStage().getToStation().getStation());
							if (visibilityDTO.getRouteUsers() != null) {
								for (UserDTO routeuUerDTO : visibilityDTO.getRouteUsers()) {
									getUserDTO(authDTO, routeuUerDTO);
								}
							}
							UserDTO visibilityRouteUserDTO = BitsUtil.isUserExists(visibilityDTO.getRouteUsers(), userDTO);

							boolean isRouteFound = false;
							if (routeDTO == null && visibilityDTO.getRouteList() != null && !visibilityDTO.getRouteList().isEmpty()) {
								for (RouteDTO visiblityRouteDTO : visibilityDTO.getRouteList()) {
									StageStationDTO visibilityFromStation = stationMap.get(visiblityRouteDTO.getFromStation().getId());
									StageStationDTO visibilityToStation = stationMap.get(visiblityRouteDTO.getToStation().getId());
									if (fromStationSquence <= visibilityFromStation.getStationSequence() && toStationSquence <= visibilityFromStation.getStationSequence()) {
										isRouteFound = true;
									}
									else if (fromStationSquence >= visibilityToStation.getStationSequence() && toStationSquence >= visibilityToStation.getStationSequence()) {
										isRouteFound = true;
									}
								}
							}

							if (visibilityDTO.getVisibilityType().equals("ACAT") && visibilityDTO.getRouteList() != null && !visibilityDTO.getRouteList().isEmpty() && routeDTO == null && !isRouteFound && visibilityDTO.getRouteUsers() != null && !visibilityDTO.getRouteUsers().isEmpty() && (visibilityRouteUserDTO == null || visibilityRouteUserDTO.getId() == 0)) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								routeDTO = Iterables.getFirst(visibilityDTO.getRouteList(), null);
								seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
								seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
							}
							else if (visibilityDTO.getVisibilityType().equals("ACAT") && visibilityDTO.getRouteList() != null && !visibilityDTO.getRouteList().isEmpty() && routeDTO != null) {
								if (visibilityDTO.getRouteUsers() != null && !visibilityDTO.getRouteUsers().isEmpty() && (visibilityRouteUserDTO == null || visibilityRouteUserDTO.getId() == 0)) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
									routeDTO = Iterables.getFirst(visibilityDTO.getRouteList(), null);
									seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
									seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
								}
							}
							else if (visibilityDTO.getVisibilityType().equals("HIDE") && visibilityDTO.getRouteList() != null && routeDTO != null) {
								if (visibilityDTO.getRouteUsers() != null && ((visibilityRouteUserDTO != null && visibilityRouteUserDTO.getId() != 0 && !visibilityDTO.getRouteUsers().isEmpty()) || (visibilityDTO.getRouteUsers().isEmpty()))) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
									seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
									seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
								}
							}
							else if (visibilityDTO.getVisibilityType().equals("HIDE") && visibilityDTO.getRouteList() != null && visibilityDTO.getRouteList().isEmpty()) {
								if (visibilityDTO.getRouteUsers() != null && ((visibilityRouteUserDTO != null && visibilityRouteUserDTO.getId() != 0 && !visibilityDTO.getRouteUsers().isEmpty()) || (visibilityDTO.getRouteUsers().isEmpty()))) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								}
							}
							else if (visibilityDTO.getVisibilityType().equals("HIDE") && visibilityDTO.getRouteList() != null && routeDTO == null && !isRouteFound && visibilityDTO.getRouteUsers() != null && ((visibilityRouteUserDTO != null && visibilityRouteUserDTO.getId() != 0 && !visibilityDTO.getRouteUsers().isEmpty()) || (visibilityDTO.getRouteUsers().isEmpty()))) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
							}
							else if (visibilityDTO.getVisibilityType().equals(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode()) && visibilityDTO.getRouteList() != null && visibilityDTO.getRouteList().isEmpty()) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
							}
							else if (visibilityDTO.getVisibilityType().equals(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode()) && visibilityDTO.getRouteList() != null && routeDTO != null) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
								seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
								seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
							}
						}
					}
				}
				int firstStageStationMinutes = tripDTO.getTripOriginMinutes();
				// Cancellation Terms
				CancellationTermsCache termsCache = new CancellationTermsCache();
				CancellationTermDTO cancellationTermDTO = termsCache.getCancellationTermDTOById(authDTO, scheduleDTO.getCancellationTerm());
				tripDTO.setCancellationTerm(cancellationTermDTO);

				// Cancellation datetime based on NS Settings
				DateTime travelDateTime = null;
				if (authDTO.getNamespace().getProfile().getCancellationTimeType().equals(Constants.STAGE)) {
					travelDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues());
				}
				else {
					// TODO need to handle with station exception
					firstStageStationMinutes = tripDTO.getTripOriginMinutes();
					travelDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), firstStageStationMinutes);
				}
				tripDTO.getAdditionalAttributes().put(Constants.CANCELLATION_DATETIME, DateUtil.convertDateTime(travelDateTime));

				BusCache busCache = new BusCache();
				for (AmenitiesDTO amenitiesDTO : tripDTO.getAmenities()) {
					AmenitiesDTO dto = busCache.getAmenitiesDTO(amenitiesDTO.getCode());
					if (dto != null) {
						amenitiesDTO.setName(dto.getName());
						amenitiesDTO.setCode(dto.getCode());
						amenitiesDTO.setActiveFlag(dto.getActiveFlag());
					}
					else {
						System.out.println("ERRAMAN01 " + amenitiesDTO.getCode() + " " + tripDTO.getCode());
					}
				}
				tripDTO.setTripMinutes(firstStageStationMinutes);

				/** Apply Ticket Transfer Terms */
				if (tripDTO.getSchedule().getTicketTransferTerms() != null) {
					DateTime transferDateTime = BitsUtil.getTicketTransferTermsDateTime(authDTO, tripDTO.getSchedule().getTicketTransferTerms(), DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues()), BitsUtil.getOriginScheduleStationTime(tripDTO.getStationList(), tripDTO.getTripDate()));
					tripDTO.getSchedule().getTicketTransferTerms().setDateTime(transferDateTime);
				}

				/** Changed like open search - Set search into trip */
				tripDTO.setSearch(searchDTO);
				scheduleCodes.add(scheduleDTO.getCode());
				tripList.add(tripDTO);
			}
		}
		catch (Exception e) {
			System.out.println("Exception occurred when BLF3BI3499 converting the schedule to trip " + authDTO.getNamespaceCode() + " " + userDTO.getUsername());
			e.printStackTrace();
		}
		return getUniqueTripList(tripList);
	}

	private List<TripDTO> getNamespaceSearchResultV2(AuthDTO authDTO, SearchDTO search) {
		// get Available Stage for Given Route
		List<TripDTO> tripList = new ArrayList<>();
		logger.info("Open searching the result for station from " + search.getFromStation().getName() + " to station: " + search.getToStation().getName());
		ScheduleCache scheduleCache = new ScheduleCache();
		UserDTO userDTO = authDTO.getUser();

		List<ScheduleDTO> scheduleList = stageService.getScheduleSearchStage(authDTO, search.getFromStation(), search.getToStation());

		try {
			// Schedule Validations
			for (Iterator<ScheduleDTO> itrSchedule = scheduleList.iterator(); itrSchedule.hasNext();) {
				ScheduleDTO scheduleDTO = itrSchedule.next();
				ScheduleDTO schedule = scheduleCache.getScheduleDTObyId(authDTO, scheduleDTO);

				// validate active flag
				if (schedule.getActiveFlag() != 1) {
					itrSchedule.remove();
					continue;
				}
				if (!scheduleDTO.getPreRequrities().equals("000000")) {
					itrSchedule.remove();
					continue;
				}
				// copy schedule data
				scheduleDTO.setId(schedule.getId());
				scheduleDTO.setCode(schedule.getCode());
				scheduleDTO.setActiveFrom(schedule.getActiveFrom());
				scheduleDTO.setActiveTo(schedule.getActiveTo());
				scheduleDTO.setPreRequrities(schedule.getPreRequrities());
				scheduleDTO.setDayOfWeek(schedule.getDayOfWeek());
				scheduleDTO.setOverrideList(schedule.getOverrideList());
				scheduleDTO.setName(schedule.getName());
				scheduleDTO.setDisplayName(schedule.getDisplayName());
				scheduleDTO.setApiDisplayName(StringUtil.isNotNull(schedule.getApiDisplayName()) ? schedule.getApiDisplayName() : "NA");
				scheduleDTO.setTripDate(search.getTravelDate());

				// Station
				List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, scheduleDTO);
				if (stationList == null || stationList.isEmpty()) {
					itrSchedule.remove();
					continue;
				}
				// Validate all stations
				Map<Integer, ScheduleStationDTO> stationMap = new HashMap<Integer, ScheduleStationDTO>();
				for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
					ScheduleStationDTO stationDTO = iterator.next();
					int overrideRecentDays = 0;
					// Exception and override
					for (Iterator<ScheduleStationDTO> OverrideIterator = stationDTO.getOverrideList().iterator(); OverrideIterator.hasNext();) {
						ScheduleStationDTO overrideStationDTO = OverrideIterator.next();
						// common validations
						if (StringUtil.isNotNull(overrideStationDTO.getActiveFrom()) && !scheduleDTO.getTripDate().gteq(new DateTime(overrideStationDTO.getActiveFrom()))) {
							OverrideIterator.remove();
							continue;
						}
						if (StringUtil.isNotNull(overrideStationDTO.getActiveTo()) && !scheduleDTO.getTripDate().lteq(new DateTime(overrideStationDTO.getActiveTo()))) {
							OverrideIterator.remove();
							continue;
						}
						if (overrideStationDTO.getDayOfWeek() != null && overrideStationDTO.getDayOfWeek().length() != 7) {
							OverrideIterator.remove();
							continue;
						}
						if (overrideStationDTO.getDayOfWeek() != null && overrideStationDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
							OverrideIterator.remove();
							continue;
						}
						// Remove if Exceptions
						if (overrideStationDTO.getMinitues() == -1) {
							stationDTO.setActiveFlag(-1);
							// iterator.remove();
							break;
						}
						// Override, time should follow in same day
						if (stationDTO.getMinitues() < 1440 && overrideStationDTO.getMinitues() >= 1440) {
							OverrideIterator.remove();
							continue;
						} // Second day
						else if (stationDTO.getMinitues() < 2880 && overrideStationDTO.getMinitues() >= 2880) {
							OverrideIterator.remove();
							continue;
						} // Third day
						else if (stationDTO.getMinitues() < 4320 && overrideStationDTO.getMinitues() >= 4320) {
							OverrideIterator.remove();
							continue;
						} // Fourth day
						else if (stationDTO.getMinitues() < 5760 && overrideStationDTO.getMinitues() >= 5760) {
							OverrideIterator.remove();
							continue;
						} // Fifth day
						else if (stationDTO.getMinitues() < 7200 && overrideStationDTO.getMinitues() >= 7200) {
							OverrideIterator.remove();
							continue;
						}
						if (overrideRecentDays == 0 || DateUtil.getDayDifferent(new DateTime(overrideStationDTO.getActiveFrom()), new DateTime(overrideStationDTO.getActiveTo())) <= overrideRecentDays) {
							stationDTO.setMinitues(overrideStationDTO.getMinitues());
							overrideRecentDays = DateUtil.getDayDifferent(new DateTime(overrideStationDTO.getActiveFrom()), new DateTime(overrideStationDTO.getActiveTo())) + 1;
						}
					}
					stationDTO.setStation(stationService.getStation(stationDTO.getStation()));
					stationMap.put(stationDTO.getStation().getId(), stationDTO);
				}
				if (stationList.isEmpty()) {
					itrSchedule.remove();
					continue;
				}

				/** Validate origin & destination station */
				SearchDTO searchDTO = new SearchDTO();
				searchDTO.setFromStation(search.getFromStation());
				searchDTO.setToStation(search.getToStation());
				searchDTO.setTravelDate(search.getTravelDate());

				if (searchDTO.getFromStation().getId() == 0) {
					ScheduleStationDTO originScheduleStation = BitsUtil.getOriginStation(stationList);
					searchDTO.setFromStation(originScheduleStation.getStation());
				}
				if (searchDTO.getToStation().getId() == 0) {
					ScheduleStationDTO destinationScheduleStation = BitsUtil.getDestinationStation(stationList);
					searchDTO.setToStation(destinationScheduleStation.getStation());
				}

				DateTime tripDate = scheduleDTO.getTripDate();
				// common Schedule validations
				DateTime scheduleFromDate = new DateTime(scheduleDTO.getActiveFrom());
				DateTime scheduleEndDate = new DateTime(scheduleDTO.getActiveTo());

				if (!tripDate.gteq(scheduleFromDate)) {
					itrSchedule.remove();
					continue;
				}
				if (!tripDate.lteq(scheduleEndDate)) {
					itrSchedule.remove();
					continue;
				}
				if (scheduleDTO.getDayOfWeek() == null || scheduleDTO.getDayOfWeek().length() != 7) {
					itrSchedule.remove();
					continue;
				}
				if (scheduleDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
					itrSchedule.remove();
					continue;
				}

				// check for any exception has been added
				List<ScheduleDTO> scheduleOverrideList = scheduleDTO.getOverrideList();
				for (Iterator<ScheduleDTO> itrlookupSchedule = scheduleOverrideList.iterator(); itrlookupSchedule.hasNext();) {
					ScheduleDTO lookupscheduleDTO = itrlookupSchedule.next();
					// common validations
					if (!tripDate.gteq(new DateTime(lookupscheduleDTO.getActiveFrom()))) {
						itrlookupSchedule.remove();
						continue;
					}
					if (!tripDate.lteq(new DateTime(lookupscheduleDTO.getActiveTo()))) {
						itrlookupSchedule.remove();
						continue;
					}
					if (lookupscheduleDTO.getDayOfWeek() == null || lookupscheduleDTO.getDayOfWeek().length() != 7) {
						itrlookupSchedule.remove();
						continue;
					}
					if (lookupscheduleDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
						itrlookupSchedule.remove();
						continue;
					}
				}
				if (!scheduleOverrideList.isEmpty()) {
					itrSchedule.remove();
					continue;
				}

				// Validate all Booking Control
				List<ScheduleControlDTO> controlList = scheduleCache.getScheduleControlDTO(authDTO, scheduleDTO);
				boolean groupLevelFound = false;
				boolean stageLevelFound = false;
				for (Iterator<ScheduleControlDTO> itrControlDTO = controlList.iterator(); itrControlDTO.hasNext();) {
					ScheduleControlDTO controlDTO = itrControlDTO.next();
					// common validations
					if (controlDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(controlDTO.getActiveFrom()))) {
						itrControlDTO.remove();
						continue;
					}
					if (controlDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(controlDTO.getActiveTo()))) {
						itrControlDTO.remove();
						continue;
					}
					if (controlDTO.getDayOfWeek() != null && controlDTO.getDayOfWeek().length() != 7) {
						itrControlDTO.remove();
						continue;
					}
					if (controlDTO.getDayOfWeek() != null && controlDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
						itrControlDTO.remove();
						continue;
					}
					// alternate days
					if (controlDTO.getDayOfWeek().equals("ALRNATE") && !DateUtil.isFallonAlternateDays(new DateTime(controlDTO.getActiveFrom()), tripDate)) {
						itrControlDTO.remove();
						continue;
					}
					// Check for group level or should be default
					if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0 && controlDTO.getGroup().getId() != userDTO.getGroup().getId()) {
						itrControlDTO.remove();
						continue;
					}
					// Check for Stage based booking control
					if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0 && controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0 && (controlDTO.getFromStation().getId() != searchDTO.getFromStation().getId() || controlDTO.getToStation().getId() != searchDTO.getToStation().getId())) {
						itrControlDTO.remove();
						continue;
					}
					if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0 && controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0 && controlDTO.getFromStation().getId() == searchDTO.getFromStation().getId() && controlDTO.getToStation().getId() == searchDTO.getToStation().getId()) {
						stageLevelFound = true;
					}
					if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0 && controlDTO.getGroup().getId() == userDTO.getGroup().getId()) {
						groupLevelFound = true;
					}
					// Override and Exceptions
					for (Iterator<ScheduleControlDTO> overrideItrControlDTO = controlDTO.getOverrideList().iterator(); overrideItrControlDTO.hasNext();) {
						ScheduleControlDTO overrideControlDTO = overrideItrControlDTO.next();
						// common validations
						if (overrideControlDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(overrideControlDTO.getActiveFrom()))) {
							overrideItrControlDTO.remove();
							continue;
						}
						if (overrideControlDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(overrideControlDTO.getActiveTo()))) {
							overrideItrControlDTO.remove();
							continue;
						}
						if (overrideControlDTO.getDayOfWeek() != null && overrideControlDTO.getDayOfWeek().length() != 7) {
							overrideItrControlDTO.remove();
							continue;
						}
						if (overrideControlDTO.getDayOfWeek() != null && overrideControlDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
							overrideItrControlDTO.remove();
							continue;
						}
						// exceptions/Override for Group Level
						if (overrideControlDTO.getGroup() != null && overrideControlDTO.getGroup().getId() != 0 && overrideControlDTO.getGroup().getId() != userDTO.getGroup().getId()) {
							overrideItrControlDTO.remove();
							continue;
						}
						// Remove if Exceptions
						if (overrideControlDTO.getOpenMinitues() == -1) {
							itrControlDTO.remove();
							break;
						}
						// Apply Override
						controlDTO.setOpenMinitues(overrideControlDTO.getOpenMinitues());
						controlDTO.setCloseMinitues(overrideControlDTO.getCloseMinitues());
						controlDTO.setAllowBookingFlag(overrideControlDTO.getAllowBookingFlag());
					}
				}
				// Group level validation and check exception
				if (controlList.size() > 1 && groupLevelFound) {
					// Check for group based schedule Control
					// remove default control, if group level found
					for (Iterator<ScheduleControlDTO> iterator = controlList.iterator(); iterator.hasNext();) {
						ScheduleControlDTO controlDTO = iterator.next();
						if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != userDTO.getGroup().getId()) {
							iterator.remove();
							continue;
						}
						if (controlDTO.getGroup() == null || controlDTO.getGroup().getId() == 0) {
							iterator.remove();
							continue;
						}
					}
				}
				if (controlList.size() > 1 && stageLevelFound) {
					// remove default control, if Stage level found
					for (Iterator<ScheduleControlDTO> iterator = controlList.iterator(); iterator.hasNext();) {
						ScheduleControlDTO controlDTO = iterator.next();
						if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0 && controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0 && (controlDTO.getFromStation().getId() != searchDTO.getFromStation().getId() || controlDTO.getToStation().getId() != searchDTO.getToStation().getId())) {
							iterator.remove();
							continue;
						}
						if (controlDTO.getFromStation() == null || controlDTO.getFromStation().getId() == 0 || controlDTO.getToStation() == null || controlDTO.getToStation().getId() == 0) {
							iterator.remove();
							continue;
						}
					}
				}
				if (controlList.isEmpty()) {
					itrSchedule.remove();
					continue;
				}

				// Stage validations
				if (scheduleDTO.getScheduleStageList() == null || scheduleDTO.getScheduleStageList().isEmpty()) {
					itrSchedule.remove();
					continue;
				}

				// Identify group level fare
				boolean stageFareFoundGroupLevel = false;
				for (Iterator<ScheduleStageDTO> iterator = scheduleDTO.getScheduleStageList().iterator(); iterator.hasNext();) {
					ScheduleStageDTO scheduleStageDTO = iterator.next();
					/**
					 * Additional Validation - From and To Station Validation
					 */
					if (scheduleStageDTO.getFromStation().getId() != searchDTO.getFromStation().getId() || scheduleStageDTO.getToStation().getId() != searchDTO.getToStation().getId()) {
						iterator.remove();
						continue;
					}
					if (stationMap.get(scheduleStageDTO.getFromStation().getId()) == null || stationMap.get(scheduleStageDTO.getToStation().getId()) == null) {
						iterator.remove();
						continue;
					}
					// Remove stage if station Exception
					if (stationMap.get(scheduleStageDTO.getFromStation().getId()).getActiveFlag() == -1 || stationMap.get(scheduleStageDTO.getToStation().getId()).getActiveFlag() == -1) {
						iterator.remove();
						continue;
					}
					if (scheduleStageDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(scheduleStageDTO.getActiveFrom()))) {
						iterator.remove();
						continue;
					}
					if (scheduleStageDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(scheduleStageDTO.getActiveTo()))) {
						iterator.remove();
						continue;
					}
					if (scheduleStageDTO.getDayOfWeek() != null && scheduleStageDTO.getDayOfWeek().length() != 7) {
						iterator.remove();
						continue;
					}
					if (scheduleStageDTO.getDayOfWeek() != null && scheduleStageDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
						iterator.remove();
						continue;
					}
					// Check for group level or should be default
					if (scheduleStageDTO.getGroup() != null && scheduleStageDTO.getGroup().getId() != 0 && scheduleStageDTO.getGroup().getId() != userDTO.getGroup().getId()) {
						iterator.remove();
						continue;
					}
					if (scheduleStageDTO.getGroup() != null && scheduleStageDTO.getGroup().getId() != 0 && scheduleStageDTO.getGroup().getId() == userDTO.getGroup().getId()) {
						stageFareFoundGroupLevel = true;
					}
					// Exceptions and Override
					for (Iterator<ScheduleStageDTO> overrideIterator = scheduleStageDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
						ScheduleStageDTO OverrideScheduleStageDTO = overrideIterator.next();
						if (!tripDate.gteq(new DateTime(OverrideScheduleStageDTO.getActiveFrom()))) {
							overrideIterator.remove();
							continue;
						}
						if (!tripDate.lteq(new DateTime(OverrideScheduleStageDTO.getActiveTo()))) {
							overrideIterator.remove();
							continue;
						}
						if (OverrideScheduleStageDTO.getDayOfWeek().length() != 7) {
							overrideIterator.remove();
							continue;
						}
						if (OverrideScheduleStageDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
							overrideIterator.remove();
							continue;
						}
						// If exceptions
						if (OverrideScheduleStageDTO.getFare() == -1) {
							iterator.remove();
							break;
						}
						// exceptions/Override for Group Level
						if (OverrideScheduleStageDTO.getGroup().getId() != 0 && OverrideScheduleStageDTO.getGroup().getId() != userDTO.getGroup().getId()) {
							overrideIterator.remove();
							continue;
						}
						// Apply Override
						if (OverrideScheduleStageDTO.getBusSeatType().getId() == scheduleStageDTO.getBusSeatType().getId()) {
							scheduleStageDTO.setFare(OverrideScheduleStageDTO.getFare());
						}
					}
					// Identify and set specific fare
					if (scheduleStageDTO.getOverrideList().size() >= 2) {
						ScheduleStageDTO recentScheduleStageDTO = null;
						for (ScheduleStageDTO stageDTO : scheduleStageDTO.getOverrideList()) {
							if (recentScheduleStageDTO == null) {
								recentScheduleStageDTO = stageDTO;
							}
							if (DateUtil.getDayDifferent(new DateTime(stageDTO.getActiveFrom()), new DateTime(stageDTO.getActiveTo())) <= DateUtil.getDayDifferent(new DateTime(recentScheduleStageDTO.getActiveFrom()), new DateTime(recentScheduleStageDTO.getActiveTo()))) {
								recentScheduleStageDTO = stageDTO;
							}
						}
						scheduleStageDTO.setFare(recentScheduleStageDTO.getFare());
					}
				}
				if (stageFareFoundGroupLevel) {
					for (Iterator<ScheduleStageDTO> iterator = scheduleDTO.getScheduleStageList().iterator(); iterator.hasNext();) {
						ScheduleStageDTO stageDTO = iterator.next();
						if (stageDTO.getGroup() != null && stageDTO.getGroup().getId() != userDTO.getGroup().getId()) {
							iterator.remove();
							continue;
						}
					}
				}

				// Schedule Station Point
				List<ScheduleStationPointDTO> stationPointList = scheduleStationPointService.getActiveScheduleStationPointList(authDTO, scheduleDTO, searchDTO, stationMap);

				if (stationPointList.isEmpty()) {
					itrSchedule.remove();
					continue;
				}
				// Sorting
				Comparator<ScheduleStationPointDTO> comp = new BeanComparator("minitues");
				Collections.sort(stationPointList, comp);
				// Bus Type and BusMap
				ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);

				// Validate all bus
				if (scheduleBusDTO == null) {
					itrSchedule.remove();
					continue;
				}
				BusCache busCache = new BusCache();
				scheduleBusDTO.setBus(busCache.getBusDTObyId(authDTO, scheduleBusDTO.getBus()));
				if (scheduleBusDTO.getBus() == null || StringUtil.isNull(scheduleBusDTO.getBus().getCode())) {
					itrSchedule.remove();
					continue;
				}

				/** Copy Schedule Bus Tax Into Schedule */
				scheduleDTO.setTax(scheduleBusDTO.getTax());

				// Apply Bus Override
				BusDTO busOverrideDTO = busOverrideService.applyScheduleBusOverride(authDTO, scheduleDTO, scheduleBusDTO.getBus());
				if (busOverrideDTO != null) {
					scheduleBusDTO.setBus(busOverrideDTO);

					if (StringUtil.isNotNull(scheduleBusDTO.getBus().getCategoryCode())) {
						List<String> categoryCodes = Arrays.asList(scheduleBusDTO.getBus().getCategoryCode().split("\\|"));
						for (String categoryCode : categoryCodes) {
							if (StringUtil.isNotNull(categoryCode) && categoryCode.equals(BusCategoryTypeEM.CLIMATE_CONTROL_NON_AC.getCode())) {
								scheduleDTO.getTax().setId(Numeric.ZERO_INT);
								scheduleDTO.getTax().setCode(null);
								break;
							}
						}
					}
				}
				// schedule Cancellation Terms
				ScheduleCancellationTermDTO cancellationTermDTO = cancellationTermService.getByScheduleId(authDTO, scheduleDTO);

				if (cancellationTermDTO == null || cancellationTermDTO.getCancellationTerm() == null) {
					itrSchedule.remove();
					continue;
				}
				// Seat Allocation and Deallocations
				List<ScheduleSeatVisibilityDTO> seatVisibilityList = visibilityService.getByScheduleId(authDTO, scheduleDTO);

				List<ScheduleSeatAutoReleaseDTO> seatAutoReleaseList = autoReleaseService.getByScheduleId(authDTO, scheduleDTO);

				List<ScheduleFareAutoOverrideDTO> autoFareOverridelist = fareOverrideService.getByScheduleId(authDTO, scheduleDTO, searchDTO.getFromStation(), searchDTO.getToStation());

				List<ScheduleTimeOverrideDTO> timeOverridelist = timeOverrideService.getByScheduleId(authDTO, scheduleDTO);

				List<ScheduleSeatFareDTO> seatFarelist = seatFareService.getByScheduleId(authDTO, scheduleDTO, searchDTO.getFromStation(), searchDTO.getToStation());

				// Schedule Discount
				ScheduleDiscountDTO scheduleDiscountDTO = discountService.getByScheduleId(authDTO, scheduleDTO);

				// Dynamic Stage Fare
				if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
					ScheduleDynamicStageFareDetailsDTO dynamicStageFare = dynamicStageFareService.getScheduleDynamicStageFare(authDTO, schedule, searchDTO.getFromStation(), searchDTO.getToStation());
					if (dynamicStageFare != null) {
						ScheduleDynamicStageFareDetailsDTO dynamicStageTripFareDetails = dynamicStageFareService.getDynamicPricingTripStageFareDetails(authDTO, schedule, dynamicStageFare);
						dynamicStageFare.setSeatFare(dynamicStageTripFareDetails != null ? dynamicStageTripFareDetails.getSeatFare() : null);
						scheduleDTO.setDynamicStageFare(dynamicStageFare);
					}
				}
				// Schedule Ticket Transfer Terms
				if ((authDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId() && BitsUtil.isTagExists(authDTO.getUser().getUserTags(), UserTagEM.API_USER_RB)) || authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId()) {
					ScheduleTicketTransferTermsDTO scheduleTicketTransferTerms = scheduleTicketTransferTermsService.getScheduleTicketTransferTermsBySchedule(authDTO, schedule, searchDTO.getFromStation(), searchDTO.getToStation());
					scheduleDTO.setTicketTransferTerms(scheduleTicketTransferTerms);
				}

				// collect other stages list to check blocked/booked seat status
				List<ScheduleStageDTO> otherScheduleStageList = scheduleCache.getScheduleStageDTO(authDTO, scheduleDTO);

				/** Apply Schedule Tax - If GST Exception groups not found */
				if (scheduleDTO.getTax() != null && scheduleDTO.getTax().getId() != 0 && !authDTO.getNamespace().getProfile().isGstExceptionGroup(authDTO.getUser().getGroup())) {
					scheduleDTO.setTax(taxService.getTaxbyStateV2(authDTO, scheduleDTO.getTax(), searchDTO.getFromStation().getState()));
				}

				// add to schedule
				scheduleDTO.setScheduleBus(scheduleBusDTO);
				scheduleDTO.setSeatVisibilityList(seatVisibilityList);
				scheduleDTO.setSeatAutoReleaseList(seatAutoReleaseList);
				scheduleDTO.setStationList(stationList);
				scheduleDTO.setStationPointList(stationPointList);
				scheduleDTO.setControlList(controlList);
				scheduleDTO.setOtherSscheduleStageList(otherScheduleStageList);
				scheduleDTO.setFareAutoOverrideList(autoFareOverridelist);
				scheduleDTO.setTimeOverrideList(timeOverridelist);
				scheduleDTO.setSeatFareList(seatFarelist);
				scheduleDTO.setScheduleDiscount(scheduleDiscountDTO);
				scheduleDTO.setCancellationTerm(cancellationTermDTO.getCancellationTerm());

				TripDTO tripDTO = new TripDTO();
				tripDTO.setSearch(searchDTO);
				tripDTO.setSchedule(scheduleDTO);
				tripList.add(tripDTO);
			}
		}
		catch (Exception e) {
			System.out.println("ERRR10" + "-" + authDTO.getNamespaceCode() + ":" + search.getFromStation().getCode() + ":" + search.getToStation().getCode() + ":" + search.getTravelDate());
			e.printStackTrace();
		}

		return tripList;
	}

	private List<TripDTO> validateAndConvertTrips(AuthDTO authDTO, List<TripDTO> trips) {
		List<TripDTO> tripList = new ArrayList<>();
		UserDTO userDTO = authDTO.getUser();
		try {
			DateTime now = DateUtil.NOW();
			for (Iterator<TripDTO> scheIterator = trips.iterator(); scheIterator.hasNext();) {
				TripDTO trip = scheIterator.next();

				ScheduleDTO scheduleDTO = trip.getSchedule();
				SearchDTO searchDTO = trip.getSearch();

				// Schedule Stage
				if (scheduleDTO.getScheduleStageList().isEmpty()) {
					scheIterator.remove();
					continue;
				}

				// Schedule Station
				if (scheduleDTO.getStationList().isEmpty()) {
					scheIterator.remove();
					continue;
				}

				// Schedule Station Point
				if (scheduleDTO.getStationPointList().isEmpty()) {
					scheIterator.remove();
					continue;
				}

				// Booking Control
				if (scheduleDTO.getControlList().isEmpty()) {
					scheIterator.remove();
					continue;
				}

				// Schedule Bus
				if (scheduleDTO.getScheduleBus() == null) {
					scheIterator.remove();
					continue;
				}
				// Schedule Cancellation Terms
				if (scheduleDTO.getCancellationTerm() == null) {
					scheIterator.remove();
					continue;
				}
				// Copy to Trip
				TripDTO tripDTO = new TripDTO();
				tripDTO.setTripDate(scheduleDTO.getTripDate());
				tripDTO.setCode(getGeneratedTripCode(authDTO, scheduleDTO, tripDTO));
				tripDTO.setSchedule(scheduleDTO);
				tripDTO.setBus(scheduleDTO.getScheduleBus().getBus());
				tripDTO.setAmenities(scheduleDTO.getScheduleBus().getAmentiesList());
				tripDTO.setStationList(scheduleDTO.getStationList());
				tripDTO.setSearch(searchDTO);
				tripDTO.getAdditionalAttributes().putAll(scheduleDTO.getAdditionalAttributes());

				// Station time override
				for (ScheduleTimeOverrideDTO overrideDTO : scheduleDTO.getTimeOverrideList()) {
					ScheduleStationDTO reactionStationDTO = null;
					for (ScheduleStationDTO stationDTO : scheduleDTO.getStationList()) {
						if (overrideDTO.getStation().getId() == stationDTO.getStation().getId()) {
							stationDTO.setMinitues(getStationTimeOverride(overrideDTO, stationDTO.getMinitues()));

							if (overrideDTO.isReactionFlag()) {
								reactionStationDTO = stationDTO;
							}
						}
					}
					for (ScheduleStationDTO stationDTO : scheduleDTO.getStationList()) {
						if (reactionStationDTO != null && stationDTO.getStationSequence() > reactionStationDTO.getStationSequence()) {
							stationDTO.setMinitues(getStationTimeOverride(overrideDTO, stationDTO.getMinitues()));
						}
					}
				}
				Map<Integer, StageStationDTO> stationMap = new HashMap<>();
				for (Iterator<ScheduleStationDTO> iterator = scheduleDTO.getStationList().iterator(); iterator.hasNext();) {
					ScheduleStationDTO stationDTO = iterator.next();
					StageStationDTO stageStationDTO = new StageStationDTO();
					stageStationDTO.setMinitues(stationDTO.getMinitues());
					stageStationDTO.setStationSequence(stationDTO.getStationSequence());
					stageStationDTO.setStation(stationDTO.getStation());
					stageStationDTO.setMobileNumber(stationDTO.getMobileNumber());
					stationMap.put(stationDTO.getStation().getId(), stageStationDTO);
				}
				for (Iterator<ScheduleStationPointDTO> iterator = scheduleDTO.getStationPointList().iterator(); iterator.hasNext();) {
					ScheduleStationPointDTO pointDTO = iterator.next();
					if (stationMap.get(pointDTO.getStation().getId()) != null) {
						StageStationDTO stageStationDTO = stationMap.get(pointDTO.getStation().getId());
						StationPointDTO stationPointDTO = new StationPointDTO();
						stationPointDTO.setId(pointDTO.getStationPoint().getId());
						// Copy station Point from cache
						stationPointService.getStationPoint(authDTO, stationPointDTO);
						if (!stationPointDTO.isActive()) {
							iterator.remove();
							continue;
						}
						stationPointDTO.setCreditDebitFlag(pointDTO.getCreditDebitFlag());
						stationPointDTO.setMinitues(pointDTO.getMinitues());
						stationPointDTO.setFare(pointDTO.getFare());
						if (StringUtil.isNotNull(pointDTO.getMobileNumber())) {
							stationPointDTO.setNumber(pointDTO.getMobileNumber());
						}
						if (StringUtil.isNotNull(stageStationDTO.getMobileNumber())) {
							stationPointDTO.setNumber(stageStationDTO.getMobileNumber() + " / " + stationPointDTO.getNumber());
						}
						if (StringUtil.isNotNull(pointDTO.getAddress())) {
							stationPointDTO.setAddress(pointDTO.getAddress());
						}
						if (pointDTO.getBusVehicleVanPickup() != null && pointDTO.getBusVehicleVanPickup().getId() != 0) {
							stationPointDTO.setName(stationPointDTO.getName() + " (Van Pickup)");
						}
						stageStationDTO.getStationPoint().add(stationPointDTO);
						stationMap.put(stageStationDTO.getStation().getId(), stageStationDTO);
					}
					else {
						iterator.remove();
						continue;
					}
				}
				// Identify Stage and fare with trip
				// bus-type(using-bus-override)
				List<ScheduleStageDTO> scheduleStageDTOList = new ArrayList<ScheduleStageDTO>();
				Map<String, BusSeatTypeEM> bustype = scheduleDTO.getScheduleBus().getBus().getUniqueReservableBusType();
				Map<String, BusSeatTypeEM> stageFareBustype = scheduleDTO.getUniqueStageBusType();
				for (BusSeatTypeEM seatTypeEM : new ArrayList<BusSeatTypeEM>(bustype.values())) {
					for (ScheduleStageDTO scheduleStageDTO : scheduleDTO.getScheduleStageList()) {
						if (seatTypeEM.getCode().equals(scheduleStageDTO.getBusSeatType().getCode())) {
							scheduleStageDTOList.add(scheduleStageDTO);
						}
						else if (stageFareBustype.get(seatTypeEM.getCode()) == null && stageFareBustype.get(seatTypeEM.getCode()) == null) {
							ScheduleStageDTO scheduleStage = scheduleStageDTO.clone();
							scheduleStage.setBusSeatType(seatTypeEM);
							stageFareBustype.put(seatTypeEM.getCode(), seatTypeEM);
							scheduleStageDTOList.add(scheduleStage);
						}
					}
				}

				Map<String, StageDTO> fareMap = new HashMap<>();
				for (Iterator<ScheduleStageDTO> iterator = scheduleStageDTOList.iterator(); iterator.hasNext();) {
					ScheduleStageDTO scheduleStageDTO = iterator.next();
					StageDTO stageDTO = new StageDTO();
					if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
						stageDTO.setFromStation(stationMap.get(scheduleStageDTO.getFromStation().getId()));
						stageDTO.setToStation(stationMap.get(scheduleStageDTO.getToStation().getId()));
						stageDTO.getFromStation().setStation(stationService.getStation(scheduleStageDTO.getFromStation()));
						stageDTO.getToStation().setStation(stationService.getStation(scheduleStageDTO.getToStation()));
						StageFareDTO stageFareDTO = new StageFareDTO();
						stageFareDTO.setFare(new BigDecimal(scheduleStageDTO.getFare()));
						stageFareDTO.setBusSeatType(scheduleStageDTO.getBusSeatType());
						if (scheduleStageDTO.getGroup() != null) {
							stageFareDTO.setGroup(scheduleStageDTO.getGroup());
						}
						if (fareMap.get(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId()) == null) {
							List<StageFareDTO> fareList = new ArrayList<>();
							stageDTO.setStageFare(fareList);
							fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
						}
						List<StageFareDTO> fareList = (fareMap.get(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId())).getStageFare();

						// Schedule Fare auto override
						if (scheduleDTO.getFareAutoOverrideList() != null && !scheduleDTO.getFareAutoOverrideList().isEmpty()) {
							List<ScheduleFareAutoOverrideDTO> overridelist = getFareAutoOverrideList(authDTO, scheduleDTO, stageDTO, scheduleStageDTO.getBusSeatType());
							stageFareDTO.setFare(applyFareAutoOverride(stageDTO, scheduleStageDTO.getFare(), overridelist, scheduleDTO.getTripDate(), scheduleStageDTO.getBusSeatType()));
						}

						// Apply Schedule based Discount if Any
						if (scheduleDTO.getScheduleDiscount() != null && scheduleDTO.getScheduleDiscount().getFemaleDiscountFlag() != 1) {
							BigDecimal discountFare = BigDecimal.ZERO;
							if (scheduleDTO.getScheduleDiscount().getPercentageFlag() == 0) {
								discountFare = scheduleDTO.getScheduleDiscount().getDiscountValue();
							}
							else if (scheduleDTO.getScheduleDiscount().getPercentageFlag() == 1) {
								discountFare = stageFareDTO.getFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(scheduleDTO.getScheduleDiscount().getDiscountValue());
							}
							stageFareDTO.setDiscountFare(discountFare.setScale(0, RoundingMode.HALF_UP));
						}
						fareList.add(stageFareDTO);
						stageDTO.setStageFare(fareList);
						stageDTO.setCode(getGeneratedTripStageCode(authDTO, scheduleDTO, tripDTO, scheduleStageDTO));
						fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);

					}
					else {
						iterator.remove();
						continue;
					}
					if (stageDTO.getFromStation().getStationPoint().isEmpty() || stageDTO.getToStation().getStationPoint().isEmpty()) {
						iterator.remove();
						continue;
					}
				}

				// Schedule Seat Fare
				if (scheduleDTO.getSeatFareList() != null && !scheduleDTO.getSeatFareList().isEmpty()) {
					Map<String, BusSeatLayoutDTO> seatMap = new HashMap<String, BusSeatLayoutDTO>();
					for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
						seatMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
					}
					for (ScheduleSeatFareDTO scheduleSeatFareDTO : scheduleDTO.getSeatFareList()) {
						for (BusSeatLayoutDTO seatLayoutDTO : scheduleSeatFareDTO.getBus().getBusSeatLayoutDTO().getList()) {
							if (seatMap.get(seatLayoutDTO.getCode()) != null) {
								seatMap.get(seatLayoutDTO.getCode()).setFare(calculateSeatFare(scheduleSeatFareDTO, fareMap.entrySet().iterator().next().getValue().getSeatFare(seatMap.get(seatLayoutDTO.getCode()).getBusSeatType())));
							}
						}
					}

				}

				// Dynamic Seat Fare
				if (scheduleDTO.getDynamicStageFare() != null && scheduleDTO.getDynamicStageFare().getSeatFare() != null && !scheduleDTO.getDynamicStageFare().getSeatFare().isEmpty()) {
					Map<String, BusSeatLayoutDTO> seatMap = new HashMap<String, BusSeatLayoutDTO>();
					for (BusSeatLayoutDTO seatLayoutDTO : scheduleDTO.getDynamicStageFare().getSeatFare()) {
						seatMap.put(seatLayoutDTO.getName(), seatLayoutDTO);
					}
					for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
						if (seatMap.get(seatLayoutDTO.getName()) != null) {
							seatLayoutDTO.setFare(seatMap.get(seatLayoutDTO.getName()).getFare());
						}
						else {
							seatLayoutDTO.setFare(BigDecimal.ZERO);
						}
					}
				}
				// DP Fare status expose
				if (scheduleDTO.getDynamicStageFare() != null && scheduleDTO.getDynamicStageFare().getSeatFare() != null && !scheduleDTO.getDynamicStageFare().getSeatFare().isEmpty()) {
					tripDTO.getAdditionalAttributes().put(TripActivitiesEM.DYNAMIC_PRICING.getCode(), "DPFARE");
				}
				else if (scheduleDTO.getDynamicStageFare() != null && (scheduleDTO.getDynamicStageFare().getSeatFare() == null || scheduleDTO.getDynamicStageFare().getSeatFare().isEmpty())) {
					tripDTO.getAdditionalAttributes().put(TripActivitiesEM.DYNAMIC_PRICING.getCode(), "NODPF");
				}

				// Validate Min and Max fare of namespace TODO:

				// other stage Filter, identify other stages
				for (Iterator<ScheduleStageDTO> itrStageDTO = scheduleDTO.getOtherSscheduleStageList().iterator(); itrStageDTO.hasNext();) {
					ScheduleStageDTO scheduleStageDTO = itrStageDTO.next();
					if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
						scheduleStageDTO.setFromStationSequence(stationMap.get(scheduleStageDTO.getFromStation().getId()).getStationSequence());
						scheduleStageDTO.setToStationSequence(stationMap.get(scheduleStageDTO.getToStation().getId()).getStationSequence());
					}
					else {
						itrStageDTO.remove();
					}
				}
				// If Fare not available, may be due to station exceptions
				if (fareMap.isEmpty()) {
					scheIterator.remove();
					continue;
				}
				Collection<StageDTO> dtStageDTOsos = fareMap.values();
				for (StageDTO stageDTO : dtStageDTOsos) {
					// Advance Booking Validations
					for (Iterator<ScheduleControlDTO> itrControlDTO = scheduleDTO.getControlList().iterator(); itrControlDTO.hasNext();) {
						ScheduleControlDTO controlDTO = itrControlDTO.next();
						// Identify Trip origin station or Stage wise Open Close
						int tripStageOriginStationOpenMinutes = stageDTO.getFromStation().getMinitues();
						int tripStageOriginStationCloseMinutes = stageDTO.getFromStation().getMinitues();
						if (controlDTO.getFromStation() == null && controlDTO.getToStation() == null) {
							tripStageOriginStationOpenMinutes = tripDTO.getTripOriginMinutes();
						}
						if (controlDTO.getAllowBookingFlag() != 1) {
							itrControlDTO.remove();
							stageDTO.setStageStatus(TripStatusEM.TRIP_CLOSED);
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationCloseMinutes));
							continue;
						}

						if (controlDTO.getOpenMinitues() > 14400) {
							tripStageOriginStationOpenMinutes = 0;
						}

						int minutiesOpenDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationOpenMinutes));
						int minutiesCloseDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationCloseMinutes));
						if (minutiesOpenDiff >= controlDTO.getOpenMinitues()) {
							stageDTO.setStageStatus(TripStatusEM.TRIP_YET_OPEN);
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationOpenMinutes - controlDTO.getCloseMinitues()));
							itrControlDTO.remove();
							continue;
						}
						if (controlDTO.getCloseMinitues() != -1 && minutiesCloseDiff <= controlDTO.getCloseMinitues()) {
							itrControlDTO.remove();
							stageDTO.setStageStatus(TripStatusEM.TRIP_CLOSED);
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationOpenMinutes - controlDTO.getCloseMinitues()));
							continue;
						}
						// Identify Close time
						if (controlDTO.getCloseMinitues() == -1 && stageDTO.getFromStation().getStationPoint().size() >= 1) {
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), stageDTO.getFromStation().getMinitues() + stageDTO.getFromStation().getStationPoint().get(stageDTO.getFromStation().getStationPoint().size() - 1).getMinitues()));
						}
						else {
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), stageDTO.getFromStation().getMinitues() - controlDTO.getCloseMinitues()));
						}
						if (minutiesOpenDiff <= controlDTO.getOpenMinitues()) {
							stageDTO.setStageStatus(TripStatusEM.TRIP_OPEN);
							itrControlDTO.remove();
							continue;
						}
					}
					if (stageDTO.getStageStatus() == null) {
						stageDTO.setStageStatus(TripStatusEM.TRIP_CLOSED);
					}
					tripDTO.setStage(stageDTO);
				}
				// If next day travel date
				int tripFromStationMinitues = tripDTO.getStage().getFromStation().getMinitues();
				if (tripDTO.getStage().getFromStation().getMinitues() > 1440 && tripDTO.getStage().getFromStation().getMinitues() <= 2880) {
					tripFromStationMinitues = tripDTO.getStage().getFromStation().getMinitues() - 1440;
				}
				else if (tripDTO.getStage().getFromStation().getMinitues() > 2880 && tripDTO.getStage().getFromStation().getMinitues() <= 4320) {
					tripFromStationMinitues = tripDTO.getStage().getFromStation().getMinitues() - 2880;
				}
				else if (tripDTO.getStage().getFromStation().getMinitues() > 4320 && tripDTO.getStage().getFromStation().getMinitues() <= 5760) {
					tripFromStationMinitues = tripDTO.getStage().getFromStation().getMinitues() - 4320;
				}
				else if (tripDTO.getStage().getFromStation().getMinitues() > 5760 && tripDTO.getStage().getFromStation().getMinitues() <= 7200) {
					tripFromStationMinitues = tripDTO.getStage().getFromStation().getMinitues() - 5760;
				}
				// check current time with boarding point time
				if (DateUtil.addMinituesToDate(searchDTO.getTravelDate(), tripFromStationMinitues).lteq(now)) {
					int stationPointFound = 0;
					for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
						if (DateUtil.addMinituesToDate(searchDTO.getTravelDate(), tripFromStationMinitues + pointDTO.getMinitues()).gteq(now)) {
							stationPointFound = 1;
						}
					}
					if (stationPointFound != 1 && tripDTO.getStage().getStageStatus().getId() == TripStatusEM.TRIP_OPEN.getId()) {
						tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_CLOSED);
					}
				}

				// Identify co-releated schedules stage,
				int fromStationSquence = stationMap.get(tripDTO.getStage().getFromStation().getStation().getId()).getStationSequence();
				int toStationSquence = stationMap.get(tripDTO.getStage().getToStation().getStation().getId()).getStationSequence();
				List<String> releatedStageCodeList = new ArrayList<>();
				for (Iterator<ScheduleStageDTO> itrStageDTO = scheduleDTO.getOtherSscheduleStageList().iterator(); itrStageDTO.hasNext();) {
					ScheduleStageDTO scheduleStageDTO = itrStageDTO.next();
					if (scheduleStageDTO.getToStationSequence() <= fromStationSquence) {
						itrStageDTO.remove();
						continue;
					}
					if (scheduleStageDTO.getFromStationSequence() >= toStationSquence) {
						itrStageDTO.remove();
						continue;
					}
					releatedStageCodeList.add(getGeneratedTripStageCode(authDTO, scheduleDTO, tripDTO, scheduleStageDTO));

				}
				tripDTO.setReleatedStageCodeList(releatedStageCodeList);

				// Validate seat Visibility
				Map<String, List<ScheduleSeatVisibilityDTO>> allocatedMap = new HashMap<>();
				Map<String, List<ScheduleSeatVisibilityDTO>> stageSeatMap = new HashMap<>();

				// Sorting
				Collections.sort(scheduleDTO.getSeatVisibilityList(), new Comparator<ScheduleSeatVisibilityDTO>() {
					public int compare(ScheduleSeatVisibilityDTO previousSeatVisibility, ScheduleSeatVisibilityDTO visibilityDTO) {
						if (SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode().equals(previousSeatVisibility.getVisibilityType()) && "HIDE".equals(visibilityDTO.getVisibilityType()))
							return -1;
						return 1;
					}
				});

				for (Iterator<ScheduleSeatVisibilityDTO> itrSeatVisibility = scheduleDTO.getSeatVisibilityList().iterator(); itrSeatVisibility.hasNext();) {
					ScheduleSeatVisibilityDTO visibilityDTO = itrSeatVisibility.next();

					// Seat Auto Release
					DateTime trTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getTripOriginMinutes());
					Integer check = DateUtil.getMinutiesDifferent(now, trTime);
					if (visibilityDTO.getReleaseMinutes() > 0 && check < visibilityDTO.getReleaseMinutes()) {
						continue;
					}
					// Stage based
					if (visibilityDTO.getRefferenceType().equals("SG") && visibilityDTO.getRouteList() != null) {
						for (Iterator<BusSeatLayoutDTO> itrlayout = visibilityDTO.getBus().getBusSeatLayoutDTO().getList().iterator(); itrlayout.hasNext();) {
							BusSeatLayoutDTO seatLayoutDTO = itrlayout.next();
							if (stageSeatMap.get(seatLayoutDTO.getCode()) == null) {
								List<ScheduleSeatVisibilityDTO> list = new ArrayList<ScheduleSeatVisibilityDTO>();
								list.add(visibilityDTO);
								stageSeatMap.put(seatLayoutDTO.getCode(), list);
							}
							else {
								List<ScheduleSeatVisibilityDTO> list = stageSeatMap.get(seatLayoutDTO.getCode());
								list.add(visibilityDTO);
								stageSeatMap.put(seatLayoutDTO.getCode(), list);
							}
						}
					}
					else if (visibilityDTO.getRefferenceType().equals("GR") || visibilityDTO.getRefferenceType().equals("UR") || visibilityDTO.getRefferenceType().equals("BR")) {

						// User/Group based
						for (Iterator<BusSeatLayoutDTO> itrlayout = visibilityDTO.getBus().getBusSeatLayoutDTO().getList().iterator(); itrlayout.hasNext();) {
							BusSeatLayoutDTO seatLayoutDTO = itrlayout.next();
							if (allocatedMap.get(seatLayoutDTO.getCode()) == null) {
								List<ScheduleSeatVisibilityDTO> list = new ArrayList<ScheduleSeatVisibilityDTO>();
								list.add(visibilityDTO);
								allocatedMap.put(seatLayoutDTO.getCode(), list);
							}
							else {
								List<ScheduleSeatVisibilityDTO> list = allocatedMap.get(seatLayoutDTO.getCode());
								list.add(visibilityDTO);
								allocatedMap.put(seatLayoutDTO.getCode(), list);
							}
						}
					}
				}
				// Auto Release validations
				ScheduleSeatAutoReleaseDTO scheduleSeatAutoRelease = null;
				Collections.sort(scheduleDTO.getSeatAutoReleaseList(), new Comparator<ScheduleSeatAutoReleaseDTO>() {
					@Override
					public int compare(ScheduleSeatAutoReleaseDTO t1, ScheduleSeatAutoReleaseDTO t2) {
						return new CompareToBuilder().append(t2.getActiveFrom(), t1.getActiveFrom()).append(t2.getActiveTo(), t1.getActiveTo()).toComparison();
					}
				});

				for (Iterator<ScheduleSeatAutoReleaseDTO> itrAutoRelease = scheduleDTO.getSeatAutoReleaseList().iterator(); itrAutoRelease.hasNext();) {
					ScheduleSeatAutoReleaseDTO autoRelaseDTO = itrAutoRelease.next();
					if (autoRelaseDTO.getReleaseTypeEM().getId() != ReleaseTypeEM.RELEASE_ACAT.getId() && autoRelaseDTO.getReleaseTypeEM().getId() != ReleaseTypeEM.RELEASE_HIDE.getId()) {
						itrAutoRelease.remove();
						continue;
					}

					if (MinutesTypeEM.MINUTES.getId() == autoRelaseDTO.getMinutesTypeEM().getId()) {
						DateTime trTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues());
						Integer check = DateUtil.getMinutiesDifferent(now, trTime);
						if (check > autoRelaseDTO.getReleaseMinutes()) {
							itrAutoRelease.remove();
							break;
						}
					}
					// AM
					else if (MinutesTypeEM.AM.getId() == autoRelaseDTO.getMinutesTypeEM().getId()) {
						DateTime checkTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), autoRelaseDTO.getReleaseMinutes());
						Integer check = DateUtil.getMinutiesDifferent(now, checkTime);
						if (check > 0) {
							itrAutoRelease.remove();
							break;
						}
					}
					// PM
					else if (MinutesTypeEM.PM.getId() == autoRelaseDTO.getMinutesTypeEM().getId()) {
						DateTime checkTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), 720 + autoRelaseDTO.getReleaseMinutes());
						Integer check = DateUtil.getMinutiesDifferent(now, checkTime);
						if (check > 0) {
							itrAutoRelease.remove();
							break;
						}
					}
					if (scheduleSeatAutoRelease == null) {
						scheduleSeatAutoRelease = autoRelaseDTO;
						continue;
					}
					if (DateUtil.getDayDifferent(DateUtil.getDateTime(autoRelaseDTO.getActiveFrom()), DateUtil.getDateTime(autoRelaseDTO.getActiveTo())) > DateUtil.getDayDifferent(DateUtil.getDateTime(scheduleSeatAutoRelease.getActiveFrom()), DateUtil.getDateTime(scheduleSeatAutoRelease.getActiveTo()))) {
						itrAutoRelease.remove();
						continue;
					}
				}

				// Apply Seat Visibility
				for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
					seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);

					if (allocatedMap.get(seatLayoutDTO.getCode()) != null) {
						List<ScheduleSeatVisibilityDTO> visibilityDTOList = allocatedMap.get(seatLayoutDTO.getCode());
						for (ScheduleSeatVisibilityDTO visibilityDTO : visibilityDTOList) {
							UserDTO visibilityUserDTO = BitsUtil.isUserExists(visibilityDTO.getUserList(), userDTO);
							GroupDTO visibilityGroupDTO = BitsUtil.isGroupExists(visibilityDTO.getGroupList(), userDTO.getGroup());
							OrganizationDTO visibilityOrganizationDTO = BitsUtil.isOrganizationExists(visibilityDTO.getOrganizations(), userDTO.getOrganization());

							if (visibilityDTO.getVisibilityType().equals("HIDE")) {
								if (visibilityDTO.getUserList() != null && !visibilityDTO.getUserList().isEmpty() && visibilityUserDTO != null && visibilityUserDTO.getId() != 0) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
									seatLayoutDTO.setUser(visibilityUserDTO);
								}
								else if (visibilityDTO.getUserList() != null && visibilityDTO.getUserList().isEmpty()) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								}
								else if (visibilityDTO.getGroupList() != null && visibilityDTO.getGroupList().isEmpty()) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								}
								else if (visibilityDTO.getGroupList() != null && visibilityGroupDTO != null && visibilityGroupDTO.getId() != 0) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
									seatLayoutDTO.setGroup(visibilityGroupDTO);
								}
								else if (visibilityDTO.getOrganizations() != null && !visibilityDTO.getOrganizations().isEmpty() && visibilityOrganizationDTO != null && visibilityOrganizationDTO.getId() != 0) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
									seatLayoutDTO.setOrganization(visibilityOrganizationDTO);
								}
								else if (visibilityDTO.getOrganizations() != null && visibilityDTO.getOrganizations().isEmpty()) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								}
							}
							else if (visibilityDTO.getVisibilityType().equals("ACAT")) {
								if (visibilityDTO.getGroupList() != null && !visibilityDTO.getGroupList().isEmpty()) {
									if (visibilityGroupDTO != null && visibilityGroupDTO.getId() != 0) {
										seatLayoutDTO.setGroup(visibilityGroupDTO);
										seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
									}
									else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
										seatLayoutDTO.setGroup(Iterables.getFirst(visibilityDTO.getGroupList(), null));
										seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
									}
								}
								else if (visibilityDTO.getUserList() != null && !visibilityDTO.getUserList().isEmpty()) {
									if (visibilityUserDTO != null && visibilityUserDTO.getId() != 0) {
										seatLayoutDTO.setUser(visibilityUserDTO);
										seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
									}
									else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
										seatLayoutDTO.setUser(Iterables.getFirst(visibilityDTO.getUserList(), null));
										seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
									}
								}
								else if (visibilityDTO.getOrganizations() != null && !visibilityDTO.getOrganizations().isEmpty()) {
									if (visibilityOrganizationDTO != null && visibilityOrganizationDTO.getId() != 0) {
										seatLayoutDTO.setOrganization(visibilityOrganizationDTO);
										seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
									}
									else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
										seatLayoutDTO.setOrganization(Iterables.getFirst(visibilityDTO.getOrganizations(), null));
										seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
									}
								}
							}
							else if (visibilityDTO.getVisibilityType().equals(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode())) {
								if (visibilityDTO.getUserList() != null && !visibilityDTO.getUserList().isEmpty() && visibilityUserDTO != null && visibilityUserDTO.getId() != 0) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
									seatLayoutDTO.setUser(visibilityUserDTO);
								}
								else if (visibilityDTO.getUserList() != null && visibilityDTO.getUserList().isEmpty()) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
								}
								else if (visibilityDTO.getGroupList() != null && visibilityDTO.getGroupList().isEmpty()) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
								}
								else if (visibilityDTO.getGroupList() != null && visibilityGroupDTO != null && visibilityGroupDTO.getId() != 0) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
									seatLayoutDTO.setGroup(visibilityGroupDTO);
								}
							}
							// Apply Auto Release
							for (ScheduleSeatAutoReleaseDTO seatAutoRelease : scheduleDTO.getSeatAutoReleaseList()) {
								GroupDTO visibilityGroup = BitsUtil.isGroupExists(visibilityDTO.getGroupList(), seatAutoRelease.getGroups());
								if (visibilityDTO.getGroupList() != null && seatAutoRelease.getGroups() != null && !seatAutoRelease.getGroups().isEmpty() && visibilityGroup == null) {
									continue;
								}
								if (visibilityDTO.getVisibilityType().equals(seatAutoRelease.getReleaseTypeEM().getCode())) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
								}
							}
						}
					}
					// Stage Seat Visibility
					if (stageSeatMap.get(seatLayoutDTO.getCode()) != null) {
						List<ScheduleSeatVisibilityDTO> visibilityDTOList = stageSeatMap.get(seatLayoutDTO.getCode());
						for (ScheduleSeatVisibilityDTO visibilityDTO : visibilityDTOList) {
							RouteDTO routeDTO = BitsUtil.isRouteExists(visibilityDTO.getRouteList(), tripDTO.getStage().getFromStation().getStation(), tripDTO.getStage().getToStation().getStation());
							if (visibilityDTO.getRouteUsers() != null) {
								for (UserDTO routeuUerDTO : visibilityDTO.getRouteUsers()) {
									getUserDTO(authDTO, routeuUerDTO);
								}
							}
							UserDTO visibilityRouteUserDTO = BitsUtil.isUserExists(visibilityDTO.getRouteUsers(), userDTO);

							boolean isRouteFound = false;
							if (routeDTO == null && visibilityDTO.getRouteList() != null && !visibilityDTO.getRouteList().isEmpty()) {
								for (RouteDTO visiblityRouteDTO : visibilityDTO.getRouteList()) {
									StageStationDTO visibilityFromStation = stationMap.get(visiblityRouteDTO.getFromStation().getId());
									StageStationDTO visibilityToStation = stationMap.get(visiblityRouteDTO.getToStation().getId());
									if (fromStationSquence <= visibilityFromStation.getStationSequence() && toStationSquence <= visibilityFromStation.getStationSequence()) {
										isRouteFound = true;
									}
									else if (fromStationSquence >= visibilityToStation.getStationSequence() && toStationSquence >= visibilityToStation.getStationSequence()) {
										isRouteFound = true;
									}
								}
							}

							if (visibilityDTO.getVisibilityType().equals("ACAT") && visibilityDTO.getRouteList() != null && !visibilityDTO.getRouteList().isEmpty() && routeDTO == null && !isRouteFound && visibilityDTO.getRouteUsers() != null && !visibilityDTO.getRouteUsers().isEmpty() && (visibilityRouteUserDTO == null || visibilityRouteUserDTO.getId() == 0)) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								routeDTO = Iterables.getFirst(visibilityDTO.getRouteList(), null);
								seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
								seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
							}
							else if (visibilityDTO.getVisibilityType().equals("ACAT") && visibilityDTO.getRouteList() != null && !visibilityDTO.getRouteList().isEmpty() && routeDTO != null) {
								if (visibilityDTO.getRouteUsers() != null && !visibilityDTO.getRouteUsers().isEmpty() && (visibilityRouteUserDTO == null || visibilityRouteUserDTO.getId() == 0)) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
									routeDTO = Iterables.getFirst(visibilityDTO.getRouteList(), null);
									seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
									seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
								}
							}
							else if (visibilityDTO.getVisibilityType().equals("HIDE") && visibilityDTO.getRouteList() != null && routeDTO != null) {
								if (visibilityDTO.getRouteUsers() != null && ((visibilityRouteUserDTO != null && visibilityRouteUserDTO.getId() != 0 && !visibilityDTO.getRouteUsers().isEmpty()) || (visibilityDTO.getRouteUsers().isEmpty()))) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
									seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
									seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
								}
							}
							else if (visibilityDTO.getVisibilityType().equals("HIDE") && visibilityDTO.getRouteList() != null && visibilityDTO.getRouteList().isEmpty()) {
								if (visibilityDTO.getRouteUsers() != null && ((visibilityRouteUserDTO != null && visibilityRouteUserDTO.getId() != 0 && !visibilityDTO.getRouteUsers().isEmpty()) || (visibilityDTO.getRouteUsers().isEmpty()))) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								}
							}
							else if (visibilityDTO.getVisibilityType().equals("HIDE") && visibilityDTO.getRouteList() != null && routeDTO == null && !isRouteFound && visibilityDTO.getRouteUsers() != null && ((visibilityRouteUserDTO != null && visibilityRouteUserDTO.getId() != 0 && !visibilityDTO.getRouteUsers().isEmpty()) || (visibilityDTO.getRouteUsers().isEmpty()))) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
							}
							else if (visibilityDTO.getVisibilityType().equals(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode()) && visibilityDTO.getRouteList() != null && visibilityDTO.getRouteList().isEmpty()) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
							}
							else if (visibilityDTO.getVisibilityType().equals(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode()) && visibilityDTO.getRouteList() != null && routeDTO != null) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.SOCIAL_DISTANCE_BLOCK);
								seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
								seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
							}
						}
					}
				}
				int firstStageStationMinutes = tripDTO.getTripOriginMinutes();
				// Cancellation Terms
				CancellationTermsCache termsCache = new CancellationTermsCache();
				CancellationTermDTO cancellationTermDTO = termsCache.getCancellationTermDTOById(authDTO, scheduleDTO.getCancellationTerm());
				tripDTO.setCancellationTerm(cancellationTermDTO);

				// Cancellation datetime based on NS Settings
				DateTime travelDateTime = null;
				if (authDTO.getNamespace().getProfile().getCancellationTimeType().equals(Constants.STAGE)) {
					travelDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues());
				}
				else {
					// TODO need to handle with station exception
					travelDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), firstStageStationMinutes);
				}
				tripDTO.getAdditionalAttributes().put(Constants.CANCELLATION_DATETIME, DateUtil.convertDateTime(travelDateTime));

				BusCache busCache = new BusCache();
				for (AmenitiesDTO amenitiesDTO : tripDTO.getAmenities()) {
					AmenitiesDTO dto = busCache.getAmenitiesDTO(amenitiesDTO.getCode());
					if (dto != null) {
						amenitiesDTO.setName(dto.getName());
						amenitiesDTO.setCode(dto.getCode());
						amenitiesDTO.setActiveFlag(dto.getActiveFlag());
					}
					else {
						System.out.println("ERRAMAN02 " + amenitiesDTO.getCode() + " " + tripDTO.getCode());
					}
				}
				tripDTO.setTripMinutes(firstStageStationMinutes);

				/** Apply Ticket Transfer Terms */
				if (tripDTO.getSchedule().getTicketTransferTerms() != null) {
					DateTime transferDateTime = BitsUtil.getTicketTransferTermsDateTime(authDTO, tripDTO.getSchedule().getTicketTransferTerms(), DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues()), BitsUtil.getOriginScheduleStationTime(tripDTO.getStationList(), tripDTO.getTripDate()));
					tripDTO.getSchedule().getTicketTransferTerms().setDateTime(transferDateTime);
				}
				tripList.add(tripDTO);
			}
		}
		catch (Exception e) {
			System.out.println("Exception occurred when BLF3BI3500 converting the schedule to trip " + authDTO.getNamespaceCode() + " " + userDTO.getUsername());
			e.printStackTrace();
		}
		return getUniqueTripList(tripList);
	}

	public void pushTripsDetails(AuthDTO authDT2O, List<NamespaceDTO> namespaces, DateTime startDate, int days) {

		List<DateTime> dateTimes = DateUtil.getDateListV2(startDate.getStartOfDay(), days);

		for (DateTime dateTime : dateTimes) {

			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(dateTime);

			for (NamespaceDTO namespace : namespaces) {
				try {
					Map<String, StageDTO> stageMap = new HashMap<>();
					List<StageDTO> stages = new ArrayList<>();
					AuthDTO auth = authService.getGuestAuthendtication(namespace.getCode(), DeviceMediumEM.WEB_USER);

					// RB OTA updated
					if (auth.getNamespace().getProfile().getOtaPartnerCode().isEmpty() || auth.getNamespace().getProfile().getOtaPartnerCode().get(UserTagEM.API_USER_RB.getCode()) == null) {
						continue;
					}
					// Advance Booking date Validation
					if (!DateUtil.isValidFutureDate(searchDTO.getTravelDate(), auth.getNamespace().getProfile().getAdvanceBookingDays())) {
						continue;
					}
					/** Get Active Trips */
					List<TripDTO> list = getAllTripsV2(auth, searchDTO);
					System.out.println("Get-Active_Trips:" + list.size());
					/** Unique stages */
					for (TripDTO tripDTO : list) {
						if (tripDTO.getStage().getStageStatus().getId() != TripStatusEM.TRIP_OPEN.getId()) {
							continue;
						}
						for (StageDTO stageDTO : tripDTO.getStageList()) {
							String key = namespace.getCode() + Text.UNDER_SCORE + stageDTO.getFromStation().getStation().getId() + Text.UNDER_SCORE + stageDTO.getToStation().getStation().getId() + Text.UNDER_SCORE + DateUtil.convertDate(dateTime);

							if (stageMap.isEmpty() || stageMap.get(key) == null) {
								stageDTO.getFromStation().getStationPoint().clear();
								stageDTO.getToStation().getStationPoint().clear();
								stageDTO.getStageFare().clear();
								stageDTO.setCode(namespace.getCode());
								stageDTO.setTravelDate(dateTime);
								stageMap.put(key, stageDTO);
							}
						}
					}

					stages.addAll(stageMap.values());
					stageMap.clear();
					/** Push Service and Reset */
					if (!stages.isEmpty()) {
						auth.getAdditionalAttribute().put("activity_type", "daily-job-" + startDate + "+" + days + " days-" + dateTime.format(Text.DATE_DATE4J));
						auth.getAdditionalAttribute().put("activity", "daily-job");
						pushInventoryChangesEvents(auth, stages);
					}
					stages.clear();
				}
				catch (ServiceException e) {
					System.out.println("pushTripsDetails-" + e.getErrorCode().getCode() + namespace.getCode());
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void disableInActiveRoutes(AuthDTO authDsTO, List<NamespaceDTO> namespaces) {
		StationDAO stationDAO = new StationDAO();

		for (NamespaceDTO namespace : namespaces) {
			SearchDTO searchDTO = new SearchDTO();
			Map<String, StageDTO> stageMap = new HashMap<>();
			List<StageDTO> stages = new ArrayList<>();

			AuthDTO auth = authService.getGuestAuthendtication(namespace.getCode(), DeviceMediumEM.WEB_USER);

			Map<String, RouteDTO> routeMap = new HashMap<>();
			List<RouteDTO> routelist = stationDAO.getNamespaceRoutes(auth);
			for (RouteDTO routeDTO : routelist) {
				if (routeDTO.getActiveFlag() != 1) {
					continue;
				}
				routeMap.put(routeDTO.getFromStation().getId() + Text.UNDER_SCORE + routeDTO.getToStation().getId(), routeDTO);
			}
			List<DateTime> dateTimes = DateUtil.getDateListV2(DateUtil.NOW().minusDays(10).getStartOfDay(), auth.getNamespace().getProfile().getAdvanceBookingDays());

			for (DateTime dateTime : dateTimes) {
				searchDTO.setTravelDate(dateTime);
				try {
					/** Get Active Trips */
					List<TripDTO> list = getAllTripsV2(auth, searchDTO);

					/** Unique stages */
					for (TripDTO tripDTO : list) {
						if (tripDTO.getStage().getStageStatus().getId() != TripStatusEM.TRIP_OPEN.getId()) {
							continue;
						}
						for (StageDTO stageDTO : tripDTO.getStageList()) {
							String key = namespace.getCode() + Text.UNDER_SCORE + stageDTO.getFromStation().getStation().getId() + Text.UNDER_SCORE + stageDTO.getToStation().getStation().getId() + Text.UNDER_SCORE + DateUtil.convertDate(dateTime);

							if (stageMap.isEmpty() || stageMap.get(key) == null) {
								stageDTO.getFromStation().getStationPoint().clear();
								stageDTO.getToStation().getStationPoint().clear();
								stageDTO.getStageFare().clear();
								stageDTO.setCode(namespace.getCode());
								stageDTO.setTravelDate(dateTime);

								stages.add(stageDTO);
								stageMap.put(key, stageDTO);
							}
						}
					}
				}
				catch (ServiceException e) {
				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}

			/** Push Service and Reset */
			for (StageDTO stageDTO : stages) {
				if (routeMap.get(stageDTO.getFromStation().getStation().getId() + Text.UNDER_SCORE + stageDTO.getToStation().getStation().getId()) != null) {
					routeMap.remove(stageDTO.getFromStation().getStation().getId() + Text.UNDER_SCORE + stageDTO.getToStation().getStation().getId());
				}
			}

			if (!routeMap.isEmpty()) {
				stationService.updateRouteStatus(auth, new ArrayList<>(routeMap.values()), 0);
			}

			/** wait for next request */
			try {
				TimeUnit.MINUTES.sleep(2);
			}
			catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	@Async
	public void pushInventoryChangesEvent(AuthDTO authDTO, TripDTO tripDTO) {
		try {
			tripService.getTrip(authDTO, tripDTO);
			scheduleTripService.getTripStageDetails(authDTO, tripDTO);

			Map<String, StageDTO> stageMap = new HashMap<>();
			List<StageDTO> stages = new ArrayList<>();
			for (StageDTO stageDTO : tripDTO.getStageList()) {
				String stageKey = authDTO.getNamespaceCode() + Text.UNDER_SCORE + stageDTO.getFromStation().getStation().getId() + Text.UNDER_SCORE + stageDTO.getToStation().getStation().getId() + Text.UNDER_SCORE + DateUtil.convertDate(tripDTO.getTripDate());

				if (stageMap.isEmpty() || stageMap.get(stageKey) == null) {
					stageDTO.getFromStation().getStationPoint().clear();
					stageDTO.getToStation().getStationPoint().clear();
					stageDTO.setCode(authDTO.getNamespaceCode());
					stageDTO.setTravelDate(tripDTO.getTripDate());
					stageMap.put(stageKey, stageDTO);
				}

			}
			stages.addAll(new ArrayList<>(stageMap.values()));
			/** Push Service */
			pushInventoryChangesEvents(authDTO, stages);
		}
		catch (ServiceException e) {
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Async
	public void pushInventoryChangesEvent(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		try {
			scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);

			List<ScheduleStageDTO> scheduleStages = stageService.get(authDTO, scheduleDTO);

			int days = DateUtil.getDayDifferent(DateUtil.getDateTime(scheduleDTO.getActiveFrom()), DateUtil.getDateTime(scheduleDTO.getActiveTo()));
			if (days >= 10) {
				days = 10;
			}

			List<DateTime> dateTimes = DateUtil.getDateListV2(DateUtil.getDateTime(scheduleDTO.getActiveFrom()), days);

			List<StageDTO> stages = new ArrayList<>();
			Map<String, StageDTO> stageMap = new HashMap<>();
			for (DateTime dateTime : dateTimes) {
				if (scheduleDTO.getDayOfWeek().substring(dateTime.getWeekDay() - 1, dateTime.getWeekDay()).equals("0")) {
					continue;
				}
				for (ScheduleStageDTO scheduleStageDTO : scheduleStages) {
					String stageKey = authDTO.getNamespaceCode() + Text.UNDER_SCORE + scheduleStageDTO.getFromStation().getId() + Text.UNDER_SCORE + scheduleStageDTO.getToStation().getId() + Text.UNDER_SCORE + DateUtil.convertDate(dateTime);

					if (stageMap.isEmpty() || stageMap.get(stageKey) == null) {
						StageDTO stageDTO = new StageDTO();
						stageDTO.setCode(authDTO.getNamespaceCode());
						stageDTO.setTravelDate(dateTime);

						StageStationDTO fromStation = new StageStationDTO();
						fromStation.setStation(scheduleStageDTO.getFromStation());
						stageDTO.setFromStation(fromStation);

						StageStationDTO toStation = new StageStationDTO();
						toStation.setStation(scheduleStageDTO.getToStation());
						stageDTO.setToStation(toStation);
						stageMap.put(stageKey, stageDTO);
					}
				}
			}
			stages.addAll(new ArrayList<>(stageMap.values()));

			/** Push Service */
			if (!stages.isEmpty()) {
				pushInventoryChangesEvents(authDTO, stages);
			}
		}
		catch (ServiceException e) {
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private List<TripDTO> getAllTripsV2(AuthDTO authDTO, SearchDTO searchDTO) {
		SectorDTO sector = sectorService.getActiveSectorScheduleStation(authDTO);
		List<TripDTO> list = scheduleTripService.getAllTripDetails(authDTO, sector, searchDTO);
		BusCache busCache = new BusCache();
		for (TripDTO tripDTO : list) {
			tripDTO.setCode(getGeneratedTripCode(authDTO, tripDTO.getSchedule(), searchDTO));

			for (StageDTO stageDTO : tripDTO.getStageList()) {
				stageDTO.setCode(getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), searchDTO, stageDTO));
			}
			tripDTO.setBus(busCache.getBusDTObyId(authDTO, tripDTO.getBus()));
		}
		List<TripDTO> activeList = tripService.saveTrip(authDTO, list);
		return activeList;
	}

	private void applySector(AuthDTO authDTO, List<TripDTO> tripList) {
		UserTagEM[] allowedUserTags = { UserTagEM.API_USER_EZ, UserTagEM.API_USER_RB };
		if (tripList != null && BitsUtil.isTagExists(authDTO.getUser().getUserTags(), allowedUserTags)) {
			SectorDTO sector = sectorService.getUserActiveSector(authDTO, authDTO.getUser());
			for (Iterator<TripDTO> tripItr = tripList.iterator(); tripItr.hasNext();) {
				TripDTO trip = tripItr.next();
				if (sector.getActiveFlag() == 1 && BitsUtil.isScheduleExists(sector.getSchedule(), trip.getSchedule()) == null) {
					tripItr.remove();
					continue;
				}
			}
		}
	}

	private List<StageDTO> getAliasNamespaceStages(AuthDTO authDTO, List<StageDTO> stages) {
		List<StageDTO> aliasStages = new ArrayList<>();
		if (stages != null) {
			List<NamespaceDTO> aliasNamespaceList = getAliasNamespaceList(authDTO);
			for (NamespaceDTO namespace : aliasNamespaceList) {
				if (StringUtil.isNull(namespace.getAliasCode())) {
					continue;
				}
				for (StageDTO stage : stages) {
					StageDTO stageDTO = new StageDTO();
					stageDTO.setCode(namespace.getAliasCode());
					stageDTO.setTravelDate(stage.getTravelDate());
					stageDTO.setFromStation(stage.getFromStation());
					stageDTO.setToStation(stage.getToStation());
					aliasStages.add(stageDTO);
				}
			}
		}
		return aliasStages;
	}

	private List<NamespaceDTO> getAliasNamespaceList(AuthDTO authDTO) {
		List<NamespaceDTO> aliasNamespaceList = new ArrayList<>();
		Map<String, String> aliasMap = Maps.newHashMap();
		List<UserDTO> userList = userService.getAllUserV2(authDTO, UserTagEM.API_USER_RB);
		for (UserDTO userDTO : userList) {
			if (userDTO.getAdditionalAttribute() == null || !userDTO.getAdditionalAttribute().containsKey(Constants.ALIAS_NAMESPACE)) {
				continue;
			}
			String aliasNamespaceCode = userDTO.getAdditionalAttribute().get(Constants.ALIAS_NAMESPACE);
			aliasMap.put(aliasNamespaceCode, aliasNamespaceCode);
		}
		for (String aliasCode : aliasMap.values()) {
			NamespaceDTO namespace = authService.getAliasNamespace(aliasCode);
			aliasNamespaceList.add(namespace);
		}
		return aliasNamespaceList;
	}

	private void pushInventoryChangesEvents(AuthDTO authDTO, List<StageDTO> stages) {
		try {
			/* add alias namespace stages */
			if (authDTO.getNamespace().getProfile().isAliasNamespaceFlag() && !stages.isEmpty()) {
				List<StageDTO> aliasStages = getAliasNamespaceStages(authDTO, stages);
				if (!aliasStages.isEmpty()) {
					stages.addAll(aliasStages);
				}
			}

			Iterable<List<StageDTO>> batchStages = Iterables.partition(stages, 100);
			for (List<StageDTO> stageList : batchStages) {
				utilityService.pushInventoryChangesEvent(authDTO, stageList);
				TimeUnit.SECONDS.sleep(1);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Data
	private class TripRevenueDTO {
		private BigDecimal revenueAmount = BigDecimal.ZERO;
		private BigDecimal acBusTax = BigDecimal.ZERO;
		private int seatCount;
	}

	@Data
	private class TripSeatVisibilityDTO {
		private int blockCount;
		private int allocateCount;
	}

	/** Cron Job */
	public void updateTripDataCount(AuthDTO authDTO, int days) {
		List<DateTime> totalDates = DateUtil.getDateListV2(DateUtil.NOW(), days);
		Map<String, Map<String, String>> repoDataMap = tripService.getTripDataCountCache(authDTO);
		if (repoDataMap == null) {
			repoDataMap = new HashMap<>();
		}
		for (DateTime tripDate : totalDates) {
			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(tripDate);
			List<TripDTO> tripList = getAllTrips(authDTO, searchDTO);

			int totalSeatCount = 0;
			int bookedSeatCount = 0;
			int tripOpenCount = 0;
			int tripCancelCount = 0;
			int tripYetOpenCount = 0;
			int tripClosedCount = 0;

			for (TripDTO tripDTO : tripList) {
				totalSeatCount = totalSeatCount + tripDTO.getBus().getReservableLayoutSeatCount();
				bookedSeatCount = bookedSeatCount + tripDTO.getBookedSeatCount();

				tripOpenCount = tripOpenCount + (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_OPEN.getId() ? 1 : 0);
				tripCancelCount = tripCancelCount + (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_CANCELLED.getId() ? 1 : 0);
				tripYetOpenCount = tripYetOpenCount + (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_YET_OPEN.getId() ? 1 : 0);
				tripClosedCount = tripClosedCount + (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_CLOSED.getId() ? 1 : 0);
			}
			int vacantSeatCount = Math.abs(totalSeatCount - bookedSeatCount);

			Map<String, String> dataMap = new HashMap<>();
			dataMap.put("totalTripCount", String.valueOf(tripList.size()));
			dataMap.put("openTripCount", String.valueOf(tripOpenCount));
			dataMap.put("tripCancelCount", String.valueOf(tripCancelCount));
			dataMap.put("tripYetOpenCount", String.valueOf(tripYetOpenCount));
			dataMap.put("tripClosedCount", String.valueOf(tripClosedCount));
			dataMap.put("totalSeatCount", String.valueOf(totalSeatCount));
			dataMap.put("bookedSeatCount", String.valueOf(bookedSeatCount));
			dataMap.put("vacantSeatCount", String.valueOf(vacantSeatCount));
			repoDataMap.put(DateUtil.convertDate(tripDate), dataMap);
		}

		// put trip data into redis cache
		if (!repoDataMap.isEmpty()) {
			tripService.putTripDataCountCache(authDTO, repoDataMap);
		}
	}

	/** Put Trip data count into Eh Cache */
	@Async
	private void putTripDataCountCache(AuthDTO authDTO, DateTime tripDate, List<TripDTO> tripList) {

		int totalSeatCount = 0;
		int bookedSeatCount = 0;
		int tripOpenCount = 0;
		int tripCancelCount = 0;
		int tripYetOpenCount = 0;
		int tripClosedCount = 0;

		for (TripDTO tripDTO : tripList) {
			totalSeatCount = totalSeatCount + tripDTO.getBus().getReservableLayoutSeatCount();
			bookedSeatCount = bookedSeatCount + tripDTO.getBookedSeatCount();

			tripOpenCount = tripOpenCount + (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_OPEN.getId() ? 1 : 0);
			tripCancelCount = tripCancelCount + (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_CANCELLED.getId() ? 1 : 0);
			tripYetOpenCount = tripYetOpenCount + (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_YET_OPEN.getId() ? 1 : 0);
			tripClosedCount = tripClosedCount + (tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_CLOSED.getId() ? 1 : 0);
		}

		int vacantSeatCount = Math.abs(totalSeatCount - bookedSeatCount);

		Map<String, String> tripDataMap = new HashMap<>();
		tripDataMap.put("totalTripCount", String.valueOf(tripList.size()));
		tripDataMap.put("openTripCount", String.valueOf(tripOpenCount));
		tripDataMap.put("tripCancelCount", String.valueOf(tripCancelCount));
		tripDataMap.put("tripYetOpenCount", String.valueOf(tripYetOpenCount));
		tripDataMap.put("tripClosedCount", String.valueOf(tripClosedCount));
		tripDataMap.put("totalSeatCount", String.valueOf(totalSeatCount));
		tripDataMap.put("bookedSeatCount", String.valueOf(bookedSeatCount));
		tripDataMap.put("vacantSeatCount", String.valueOf(vacantSeatCount));

		TripCache tripCache = new TripCache();
		tripCache.putTripDataCountEhCache(authDTO, DateUtil.convertDate(tripDate), tripDataMap);
	}
}