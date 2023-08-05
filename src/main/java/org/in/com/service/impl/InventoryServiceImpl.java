package org.in.com.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.in.com.aggregator.utility.BitsUtilityService;
import org.in.com.constants.Numeric;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleEnrouteBookControlDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.ScheduleSeatFareDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.ScheduleTimeOverrideDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripSeatQuotaDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.service.AuthService;
import org.in.com.service.BusService;
import org.in.com.service.InventoryService;
import org.in.com.service.NamespaceTaxService;
import org.in.com.service.ScheduleBusOverrideService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleCancellationTermService;
import org.in.com.service.ScheduleControlService;
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
import org.in.com.service.ScheduleTripStageFareService;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import hirondelle.date4j.DateTime;
import lombok.Data;

@Service
public class InventoryServiceImpl extends HelperUtil implements InventoryService {
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
	ScheduleControlService controlService;
	@Autowired
	SectorService sectorService;
	@Autowired
	BusService bus;
	@Autowired
	BitsUtilityService utilityService;
	@Autowired
	AuthService authService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	UserService userService;
	@Autowired
	ScheduleTripStageFareService tripStageFareService;
	private static final Logger logger = LoggerFactory.getLogger(InventoryServiceImpl.class);

	public List<TripDTO> getScheduleTripStageList(AuthDTO authDTO, DateTime tripDate, List<String> scheduleCodes) {

		SectorDTO sector = sectorService.getActiveSectorScheduleStation(authDTO);
		SearchDTO searchDTO = new SearchDTO();
		searchDTO.setTravelDate(tripDate);
		List<TripDTO> tripStageList = new ArrayList<TripDTO>();
		DateTime now = DateUtil.NOW();
		List<TripDTO> list = scheduleTripService.getStageTripList(authDTO, sector, searchDTO);
		try {
			for (TripDTO tripDTO : list) {

				ScheduleDTO scheduleDTO = tripDTO.getSchedule();
				tripDTO.setCode(getGeneratedTripCode(authDTO, tripDTO.getSchedule(), tripDTO));
				int previousDays = DateUtil.getDayDifferent(tripDTO.getTripDate(), tripDate);

				// Schedule Station
				List<ScheduleStationDTO> stationList = scheduleStationService.getByScheduleTripDate(authDTO, scheduleDTO, scheduleDTO.getTripDate());

				Map<Integer, ScheduleStationDTO> fromStationMap = new HashMap<>();
				for (ScheduleStationDTO scheStation : scheduleDTO.getStationList()) {
					fromStationMap.put(scheStation.getStation().getId(), scheStation);
				}

				// Schedule Stage
				List<ScheduleStageDTO> stageList = stageService.getByScheduleTripDate(authDTO, scheduleDTO, scheduleDTO.getTripDate());

				for (Iterator<ScheduleStageDTO> iterator = stageList.iterator(); iterator.hasNext();) {
					ScheduleStageDTO scheduleStageDTO = iterator.next();
					if (fromStationMap.get(scheduleStageDTO.getFromStation().getId()) == null) {
						iterator.remove();
						continue;
					}
				}
				if (stageList.isEmpty()) {
					continue;
				}
				scheduleDTO.setScheduleStageList(stageList);
				List<ScheduleTimeOverrideDTO> timeOverridelist = timeOverrideService.getByScheduleId(authDTO, scheduleDTO);
				scheduleDTO.setTimeOverrideList(timeOverridelist);

				// Station time override
				for (ScheduleTimeOverrideDTO overrideDTO : timeOverridelist) {
					ScheduleStationDTO reactionStationDTO = null;
					for (ScheduleStationDTO stationDTO : stationList) {
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
				Map<Integer, StageStationDTO> stationStageMap = new HashMap<>();
				Map<Integer, ScheduleStationDTO> stationMap = new HashMap<Integer, ScheduleStationDTO>();
				for (ScheduleStationDTO stationDTO : stationList) {
					StageStationDTO stageStationDTO = new StageStationDTO();
					stageStationDTO.setMinitues(stationDTO.getMinitues());
					if (previousDays == 1) {
						stageStationDTO.setMinitues(stationDTO.getMinitues() - 1440);
					}
					else if (previousDays == 2) {
						stageStationDTO.setMinitues(stationDTO.getMinitues() - 2880);
					}
					stageStationDTO.setStationSequence(stationDTO.getStationSequence());
					stageStationDTO.setStation(stationDTO.getStation());
					stageStationDTO.setMobileNumber(stationDTO.getMobileNumber());
					stationStageMap.put(stationDTO.getStation().getId(), stageStationDTO);
					stationMap.put(stationDTO.getStation().getId(), stationDTO);
				}
				// required for trip save
				tripDTO.setStationList(stationList);
				int firstStageStationMinutes = tripDTO.getTripOriginMinutes();

				// Schedule Station Point
				List<ScheduleStationPointDTO> scheduleStationPointList = scheduleStationPointService.getScheduleStationPoint(authDTO, scheduleDTO);
				if (scheduleStationPointList.isEmpty()) {
					continue;
				}
				for (ScheduleStationPointDTO pointDTO : scheduleStationPointList) {
					if (stationMap.get(pointDTO.getStation().getId()) != null) {
						StageStationDTO stageStationDTO = stationStageMap.get(pointDTO.getStation().getId());
						StationPointDTO stationPointDTO = new StationPointDTO();
						stationPointDTO.setCreditDebitFlag(pointDTO.getCreditDebitFlag());
						stationPointDTO.setMinitues(pointDTO.getMinitues());
						stationPointDTO.setFare(pointDTO.getFare());
						stageStationDTO.getStationPoint().add(stationPointDTO);
						stationStageMap.put(stageStationDTO.getStation().getId(), stageStationDTO);
					}
				}
				// Booking Control
				List<ScheduleControlDTO> controlList = controlService.getAllGroupTripScheduleControl(authDTO, scheduleDTO, tripDate);
				if (controlList.isEmpty()) {
					continue;
				}

				// Schedule Bus
				ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);
				if (scheduleBusDTO == null || scheduleBusDTO.getBus() == null) {
					continue;
				}

				// Apply Bus Override
				BusDTO busOverrideDTO = busOverrideService.applyScheduleBusOverride(authDTO, scheduleDTO, scheduleBusDTO.getBus());
				scheduleBusDTO.setBus(busOverrideDTO);
				tripDTO.setBus(busOverrideDTO);

				List<ScheduleEnrouteBookControlDTO> scheduleEnrouteBookControlList = enrouteBookControlService.getScheduleEnrouteBookControl(authDTO, scheduleDTO);

				// Dynamic Pricing Stage Fare
				Map<String, ScheduleDynamicStageFareDetailsDTO> dpfareMap = new HashMap<String, ScheduleDynamicStageFareDetailsDTO>();
				if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
					ScheduleDynamicStageFareDetailsDTO dynamicPricingStage = dynamicStageFareService.getScheduleDynamicStageFare(authDTO, tripDTO.getSchedule(), null, null);
					if (dynamicPricingStage != null) {
						dpfareMap = dynamicStageFareService.getDynamicPricingTripStageFareDetailsV3(authDTO, tripDTO.getSchedule(), dynamicPricingStage);
					}
				}
				// Schedule Trip Stage Fare
				List<ScheduleFareAutoOverrideDTO> tripStageFareList = tripStageFareService.getTripStageActiveFare(authDTO, tripDTO.getSchedule());
				List<ScheduleFareAutoOverrideDTO> fareOverridelist = fareOverrideService.getTripScheduleDateRangeActiveFare(authDTO, tripDTO.getSchedule(), tripDTO.getTripDate(), tripDTO.getTripDate());

				List<ScheduleSeatFareDTO> seatFareOveralllist = seatFareService.getActiveScheduleSeatFare(authDTO, scheduleDTO);

				// Seat Allocation and Deallocations
				List<ScheduleSeatVisibilityDTO> seatVisibilityList = visibilityService.getByScheduleId(authDTO, tripDTO.getSchedule());

				// Identify Stage and fare with trip
				// bus-type(using-bus-override)
				List<ScheduleStageDTO> scheduleStageDTOList = new ArrayList<ScheduleStageDTO>();
				Map<String, BusSeatTypeEM> bustype = scheduleBusDTO.getBus().getUniqueReservableBusType();
				Map<String, BusSeatTypeEM> stageFareBustype = scheduleDTO.getUniqueStageBusType();
				for (BusSeatTypeEM seatTypeEM : new ArrayList<BusSeatTypeEM>(bustype.values())) {
					if (stageFareBustype.get(seatTypeEM.getCode()) != null) {
						for (ScheduleStageDTO scheduleStageDTO : stageList) {
							if (scheduleStageDTO.getBusSeatType().getCode().equals(seatTypeEM.getCode()) && stageFareBustype.get(seatTypeEM.getCode() + scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId()) == null) {
								ScheduleStageDTO scheduleStage = scheduleStageDTO.clone();
								scheduleStage.setBusSeatType(seatTypeEM);
								stageFareBustype.put(seatTypeEM.getCode() + scheduleStage.getFromStation().getId() + "_" + scheduleStage.getToStation().getId(), seatTypeEM);
								scheduleStageDTOList.add(scheduleStage);
							}
						}
					}
					else {
						for (ScheduleStageDTO scheduleStageDTO : stageList) {
							if (!scheduleStageDTO.getBusSeatType().getCode().equals(seatTypeEM.getCode()) && stageFareBustype.get(seatTypeEM.getCode() + scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId()) == null) {
								ScheduleStageDTO scheduleStage = scheduleStageDTO.clone();
								scheduleStage.setBusSeatType(seatTypeEM);
								stageFareBustype.put(seatTypeEM.getCode() + scheduleStage.getFromStation().getId() + "_" + scheduleStage.getToStation().getId(), seatTypeEM);
								scheduleStageDTOList.add(scheduleStage);
							}
						}
					}
				}

				// consider Stage of trip
				Map<String, StageDTO> fareMap = new HashMap<>();
				for (Iterator<ScheduleStageDTO> iterator = scheduleStageDTOList.iterator(); iterator.hasNext();) {
					ScheduleStageDTO scheduleStageDTO = iterator.next();
					StageDTO stageDTO = new StageDTO();
					if (stationStageMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationStageMap.get(scheduleStageDTO.getToStation().getId()) != null) {
						stageDTO.setFromStation(stationStageMap.get(scheduleStageDTO.getFromStation().getId()));
						stageDTO.setToStation(stationStageMap.get(scheduleStageDTO.getToStation().getId()));
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

						// Group
						Map<Integer, GroupDTO> groupMap = new HashMap<>();
						groupMap.put(scheduleStageDTO.getGroup().getId(), scheduleStageDTO.getGroup());

						// check DP fare else process Auto override and quick
						// fare
						ScheduleDynamicStageFareDetailsDTO dynamicStageTripFareDetails = dpfareMap.get(scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId());
						if (dynamicStageTripFareDetails == null) {
							List<ScheduleFareAutoOverrideDTO> tripFareOverridelist = tripStageFareService.processScheduleTripStageFare(authDTO, tripDTO.getSchedule(), tripStageFareList, scheduleStageDTO.getFromStation(), scheduleStageDTO.getToStation());

							if (tripFareOverridelist.isEmpty()) {
								List<ScheduleFareAutoOverrideDTO> autoOverridelist = fareOverrideService.processTripScheduleActiveFare(authDTO, tripDTO.getSchedule(), fareOverridelist, scheduleStageDTO.getFromStation(), scheduleStageDTO.getToStation(), groupMap, scheduleStageDTO.getBusSeatType());
								tripFareOverridelist.addAll(autoOverridelist);
							}

							// Schedule Fare auto override
							if (tripFareOverridelist != null && !tripFareOverridelist.isEmpty()) {
								scheduleDTO.setFareAutoOverrideList(tripFareOverridelist);
								List<ScheduleFareAutoOverrideDTO> overridelist = getFareAutoOverrideList(authDTO, scheduleDTO, stageDTO, scheduleStageDTO.getBusSeatType());
								stageFareDTO.setFare(applyFareAutoOverride(stageDTO, scheduleStageDTO.getFare(), overridelist, scheduleDTO.getTripDate(), scheduleStageDTO.getBusSeatType()));
							}
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
				}

				// Booked/quota
				List<TicketDetailsDTO> ticketDetails = tripService.getBookedBlockedSeats(authDTO, tripDTO);
				List<TripSeatQuotaDTO> tripSeatQuatoList = quotaService.getAllTripSeatQuotaV2(authDTO, tripDTO);

				List<StageDTO> stageFareList = new ArrayList<StageDTO>(fareMap.values());

				for (StageDTO stage : stageFareList) {

					// Identify co-releated schedules stage,
					int fromStationSquence = stationStageMap.get(stage.getFromStation().getStation().getId()).getStationSequence();
					int toStationSquence = stationStageMap.get(stage.getToStation().getStation().getId()).getStationSequence();
					List<String> releatedStageCodeList = new ArrayList<>();
					for (ScheduleStageDTO scheduleStageDTO : tripDTO.getSchedule().getScheduleStageList()) {
						scheduleStageDTO.setFromStationSequence(stationStageMap.get(stage.getFromStation().getStation().getId()).getStationSequence());
						scheduleStageDTO.setToStationSequence(stationStageMap.get(stage.getToStation().getStation().getId()).getStationSequence());
						if (scheduleStageDTO.getToStationSequence() <= fromStationSquence) {
							continue;
						}
						if (scheduleStageDTO.getFromStationSequence() >= toStationSquence) {
							continue;
						}
						releatedStageCodeList.add(getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), tripDTO, scheduleStageDTO));
					}
					tripDTO.setReleatedStageCodeList(releatedStageCodeList);
					stage.setTravelDate(tripDate);

					TripDTO trip = new TripDTO();
					trip.setCode(tripDTO.getCode());
					trip.setSchedule(tripDTO.getSchedule());
					trip.setStage(stage);
					trip.setTripStatus(stage.getStageStatus());
					trip.setTripDate(tripDTO.getTripDate());
					trip.setStageList(stageFareList);
					trip.setStationList(stationList);
					trip.setActiveFlag(Numeric.ONE_INT);
					trip.setReleatedStageCodeList(releatedStageCodeList);
					trip.setTripMinutes(firstStageStationMinutes);

					BusDTO busDTO = new BusDTO();
					busDTO.setCode(tripDTO.getBus().getCode());
					trip.setBus(bus.getBus(authDTO, busDTO));

					TripSeatVisibilityDTO tripSeatVisibility = processSeatVisibility(authDTO, trip, seatVisibilityList);

					applySearchBookedBlockedSeat(authDTO, trip, ticketDetails);

					applyTripSeatQuota(authDTO, trip, tripSeatQuatoList);

					enrouteBookControlService.applyScheduleEnrouteBookControl(authDTO, trip, scheduleEnrouteBookControlList, ticketDetails, tripSeatQuatoList);

					// Schedule Seat Fare
					List<ScheduleSeatFareDTO> stageSeatFare = seatFareService.processScheduleSeatFare(authDTO, scheduleDTO, stage.getFromStation().getStation(), stage.getToStation().getStation(), seatFareOveralllist);
					if (stageSeatFare != null && !stageSeatFare.isEmpty()) {
						Map<String, BusSeatLayoutDTO> seatFareMap = new HashMap<String, BusSeatLayoutDTO>();
						for (BusSeatLayoutDTO seatLayoutDTO : trip.getBus().getBusSeatLayoutDTO().getList()) {
							seatFareMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
						}
						for (ScheduleSeatFareDTO scheduleSeatFareDTO : stageSeatFare) {
							for (BusSeatLayoutDTO seatLayoutDTO : scheduleSeatFareDTO.getBus().getBusSeatLayoutDTO().getList()) {
								if (seatFareMap.get(seatLayoutDTO.getCode()) != null) {
									seatFareMap.get(seatLayoutDTO.getCode()).setFare(calculateSeatFare(scheduleSeatFareDTO, fareMap.get(stage.getFromStation().getStation().getId() + "_" + stage.getToStation().getStation().getId()).getSeatFare(seatFareMap.get(seatLayoutDTO.getCode()).getBusSeatType())));
								}
							}
						}
					}

					// Dynamic Pricing Stage Fare
					ScheduleDynamicStageFareDetailsDTO dynamicStageTripFareDetails = dpfareMap.get(stage.getFromStation().getStation().getId() + "_" + stage.getToStation().getStation().getId());
					if (dynamicStageTripFareDetails != null) {
						Map<String, BusSeatLayoutDTO> seatMap = new HashMap<String, BusSeatLayoutDTO>();
						for (BusSeatLayoutDTO seatLayoutDTO : dynamicStageTripFareDetails.getSeatFare()) {
							seatMap.put(seatLayoutDTO.getName(), seatLayoutDTO);
						}

						Map<String, BigDecimal> seatTypeFare = new HashMap<String, BigDecimal>();
						for (BusSeatLayoutDTO seatLayoutDTO : trip.getBus().getBusSeatLayoutDTO().getList()) {
							if (seatMap.get(seatLayoutDTO.getName()) != null && (seatTypeFare.get(seatLayoutDTO.getBusSeatType().getCode()) == null || seatTypeFare.get(seatLayoutDTO.getBusSeatType().getCode()).compareTo(seatMap.get(seatLayoutDTO.getName()).getFare()) <= 0)) {
								seatTypeFare.put(seatLayoutDTO.getBusSeatType().getCode(), seatMap.get(seatLayoutDTO.getName()).getFare());
							}
						}
						for (StageFareDTO stageFare : stage.getStageFare()) {
							if (seatTypeFare.get(stageFare.getBusSeatType().getCode()) != null) {
								stageFare.setFare(seatTypeFare.get(stageFare.getBusSeatType().getCode()));
							}
						}
					}
					// Trip Status
					// Advance Booking Validations
					for (ScheduleControlDTO controlDTO : controlList) {
						// Identify Trip origin station or Stage wise Open Close
						int tripStageOriginStationOpenMinutes = stage.getFromStation().getMinitues();
						int tripStageOriginStationCloseMinutes = stage.getFromStation().getMinitues();
						if (controlDTO.getFromStation() == null && controlDTO.getToStation() == null) {
							tripStageOriginStationOpenMinutes = tripDTO.getTripOriginMinutes();
						}
						if (controlDTO.getAllowBookingFlag() != 1) {
							stage.setStageStatus(TripStatusEM.TRIP_CLOSED);
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationCloseMinutes));
							continue;
						}

						if (controlDTO.getOpenMinitues() > 14400) {
							tripStageOriginStationOpenMinutes = 0;
						}

						int minutiesOpenDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationOpenMinutes));
						int minutiesCloseDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationCloseMinutes));
						if (minutiesOpenDiff >= controlDTO.getOpenMinitues()) {
							stage.setStageStatus(TripStatusEM.TRIP_YET_OPEN);
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationOpenMinutes - controlDTO.getCloseMinitues()));
							continue;
						}
						if (controlDTO.getCloseMinitues() != -1 && minutiesCloseDiff <= controlDTO.getCloseMinitues()) {
							stage.setStageStatus(TripStatusEM.TRIP_CLOSED);
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationOpenMinutes - controlDTO.getCloseMinitues()));
							continue;
						}
						// Identify Close time
						if (controlDTO.getCloseMinitues() == -1 && stage.getFromStation().getStationPoint().size() >= 1) {
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), stage.getFromStation().getMinitues() + stage.getFromStation().getStationPoint().get(stage.getFromStation().getStationPoint().size() - 1).getMinitues()));
						}
						else {
							tripDTO.setTripCloseTime(DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), stage.getFromStation().getMinitues() - controlDTO.getCloseMinitues()));
						}
						if (minutiesOpenDiff <= controlDTO.getOpenMinitues()) {
							stage.setStageStatus(TripStatusEM.TRIP_OPEN);
							continue;
						}
					}
					if (stage.getStageStatus() == null) {
						stage.setStageStatus(TripStatusEM.TRIP_CLOSED);
					}
					trip.setStage(stage);
					trip.setTripStatus(stage.getStageStatus());
					// add to final list
					tripStageList.add(trip);
				}
			}
			tripService.saveTrip(authDTO, tripStageList);
		}
		catch (Exception e) {
			e.printStackTrace();
			// TODO: handle exception
		}
		return tripStageList;
	}

	private TripSeatVisibilityDTO processSeatVisibility(AuthDTO authDTO, TripDTO tripDTO, List<ScheduleSeatVisibilityDTO> seatVisibilityList) {

		// Prepare seat Visibility start
		DateTime now = DateUtil.NOW();
		UserDTO userDTO = authDTO.getUser();
		Map<String, List<ScheduleSeatVisibilityDTO>> allocatedMap = new HashMap<>();
		Map<String, List<ScheduleSeatVisibilityDTO>> stageSeatMap = new HashMap<>();

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

			BusDTO busDTO = processStageSeatlayout(authDTO, userDTO, tripDTO, stageDTO, allocatedMap, stageSeatMap);
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

	private BusDTO processStageSeatlayout(AuthDTO authDTO, UserDTO userDTO, TripDTO tripDTO, StageDTO stageDTO, Map<String, List<ScheduleSeatVisibilityDTO>> allocatedMap, Map<String, List<ScheduleSeatVisibilityDTO>> stageSeatMap) {
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

			int fromStationSquence = stageDTO.getFromStation() != null ? stationMap.get(stageDTO.getFromStation().getStation().getId()).getStationSequence() : 0;
			int toStationSquence = stageDTO.getToStation() != null ? stationMap.get(stageDTO.getToStation().getStation().getId()).getStationSequence() : 0;

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

	public List<TripDTO> getScheduleTripList(AuthDTO authDTO, ScheduleDTO schedule, List<DateTime> tripDateList) {
		List<TripDTO> list = scheduleTripService.getScheduleTripList(authDTO, schedule, tripDateList);
		for (TripDTO tripDTO : list) {
			tripDTO.setCode(BitsUtil.getGeneratedTripCode(authDTO, tripDTO.getSchedule(), tripDTO));
		}
		List<TripDTO> activeList = tripService.saveTrip(authDTO, list);
		return activeList;
	}

	@Data
	private class TripSeatVisibilityDTO {
		private int blockCount;
		private int allocateCount;
	}
}