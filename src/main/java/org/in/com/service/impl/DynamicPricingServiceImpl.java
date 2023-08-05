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

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.cache.BusCache;
import org.in.com.constants.Numeric;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.service.BusBreakevenService;
import org.in.com.service.DynamicPricingService;
import org.in.com.service.NamespaceTaxService;
import org.in.com.service.ScheduleBusOverrideService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleCancellationTermService;
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
import org.in.com.service.ScheduleVisibilityService;
import org.in.com.service.SectorService;
import org.in.com.service.StationPointService;
import org.in.com.service.StationService;
import org.in.com.service.TravelStopsService;
import org.in.com.service.TripSeatQuotaService;
import org.in.com.service.TripService;
import org.in.com.service.helper.HelperUtil;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import hirondelle.date4j.DateTime;

@Service
public class DynamicPricingServiceImpl extends HelperUtil implements DynamicPricingService {
	public static String DEBUG_SCHEDULE_CODE = "";
	@Autowired
	TripService tripService;
	@Autowired
	ScheduleSeatVisibilityService seatVisibilityService;
	@Autowired
	ScheduleVisibilityService visibilityService;
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
	ScheduleService scheduleService;
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
	private static final Logger logger = LoggerFactory.getLogger(DynamicPricingServiceImpl.class);

	public List<TripDTO> getDateWiseDPTripList(AuthDTO authDTO, List<DateTime> tripDateList, String filterType, String[] scheduleCode) {
		SectorDTO sector = sectorService.getActiveSectorScheduleStation(authDTO);

		// Add Visibility Schedule to filter
		List<ScheduleDTO> accessableSchedules = visibilityService.getUserActiveSchedule(authDTO);

		// Add Schedules to sector concept to filter it
		if (StringUtil.isNotNull(scheduleCode) && scheduleCode.length != 0) {
			sector.setActiveFlag(Numeric.ONE_INT);
			sector.getSchedule().clear();
			sector.getStation().clear();
			for (String code : scheduleCode) {
				if (BitsUtil.isScheduleExists(accessableSchedules, code) != null) {
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setCode(code);
					sector.getSchedule().add(scheduleService.getSchedule(authDTO, scheduleDTO));
				}
			}
		}
		else {
			sector.getSchedule().clear();
			sector.getStation().clear();
			sector.setActiveFlag(Numeric.ONE_INT);
			sector.getSchedule().addAll(accessableSchedules);
		}

		// Iterate per date wise
		List<TripDTO> activeAllTrips = new ArrayList<TripDTO>();
		for (DateTime activeTripDate : tripDateList) {
			if (DateUtil.getDayDifferent(DateUtil.NOW(), activeTripDate) > authDTO.getNamespace().getProfile().getAdvanceBookingDays()) {
				continue;
			}

			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(activeTripDate);

			List<TripDTO> tripList = scheduleTripService.getAllTripDetails(authDTO, sector, searchDTO);
			BusCache busCache = new BusCache();
			for (TripDTO tripDTO : tripList) {
				tripDTO.setCode(getGeneratedTripCode(authDTO, tripDTO.getSchedule(), searchDTO));

				for (StageDTO stageDTO : tripDTO.getStageList()) {
					stageDTO.setCode(getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), searchDTO, stageDTO));
				}
				tripDTO.getStage().setCode(getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), searchDTO, tripDTO.getStage()));
				// tripDTO.setTripStatus(TripStatusEM.TRIP_OPEN);
				tripDTO.setBus(busCache.getBusDTObyId(authDTO, tripDTO.getBus()));
			}
			List<TripDTO> activeList = tripService.saveTrip(authDTO, tripList);
			for (Iterator<TripDTO> tripIterator = activeList.iterator(); tripIterator.hasNext();) {
				TripDTO tripDTO = tripIterator.next();
				try {
					if (tripDTO.getTripStatus() == null) {
						tripDTO.setTripStatus(tripDTO.getStage().getStageStatus());
					}
					// Prepare seat Visibility start
					DateTime now = DateUtil.NOW();
					UserDTO userDTO = authDTO.getUser();
					Map<String, List<ScheduleSeatVisibilityDTO>> allocatedMap = new HashMap<>();
					Map<String, List<ScheduleSeatVisibilityDTO>> stageSeatMap = new HashMap<>();
					// Seat Allocation and Deallocations
					List<ScheduleSeatVisibilityDTO> seatVisibilityList = seatVisibilityService.getByScheduleId(authDTO, tripDTO.getSchedule());
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
					// Sorting
					Comparator<StageDTO> comp = new BeanComparator("stageSequence");
					Collections.sort(tripDTO.getStageList(), comp);
					Map<Integer, ScheduleStationDTO> stationMap = new HashMap<>();
					for (ScheduleStationDTO stationDTO : tripDTO.getSchedule().getStationList()) {
						stationMap.put(stationDTO.getStation().getId(), stationDTO);
					}
					// reuse the cache list of DP
					List<ScheduleDynamicStageFareDetailsDTO> scheduleDynamicStageFareList = new ArrayList<ScheduleDynamicStageFareDetailsDTO>();

					for (Iterator<StageDTO> stageIterator = tripDTO.getStageList().iterator(); stageIterator.hasNext();) {
						StageDTO stageDTO = stageIterator.next();
						if (filterType.equals("ROUTE") && (stageDTO.getFromStation().getStation().getId() != tripDTO.getStage().getFromStation().getStation().getId() || stageDTO.getToStation().getStation().getId() != tripDTO.getStage().getToStation().getStation().getId())) {
							stageDTO.setActiveFlag(-1);
							stageIterator.remove();
							continue;
						}
						tripDTO.setBus(busCache.getBusDTObyId(authDTO, tripDTO.getBus()));

						BusDTO busDTO = processStageSeatlayout(authDTO, userDTO, tripDTO, stageDTO, searchDTO, allocatedMap, stageSeatMap);
						// -------------
						// Identify co-releated schedules stage,
						int fromStationSquence = stationMap.get(stageDTO.getFromStation().getStation().getId()).getStationSequence();
						int toStationSquence = stationMap.get(stageDTO.getToStation().getStation().getId()).getStationSequence();
						List<String> releatedStageCodeList = new ArrayList<>();
						for (ScheduleStageDTO scheduleStageDTO : tripDTO.getSchedule().getScheduleStageList()) {
							if (stationMap.get(scheduleStageDTO.getToStation().getId()).getStationSequence() <= fromStationSquence) {
								continue;
							}
							if (stationMap.get(scheduleStageDTO.getFromStation().getId()).getStationSequence() >= toStationSquence) {
								continue;
							}
							releatedStageCodeList.add(getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), tripDTO, scheduleStageDTO));
						}
						tripDTO.setReleatedStageCodeList(releatedStageCodeList);

						tripService.getBookedBlockedSeats(authDTO, tripDTO);
						applyBookedBlockedSeat(authDTO, tripDTO);

						enrouteBookControlService.applyScheduleEnrouteBookControl(authDTO, tripDTO);

						// Apply Gender Validation
						applyMultiStageGendarValidations(authDTO, tripDTO, null);

						// Dynamic Stage Fare
						ScheduleDynamicStageFareDetailsDTO dynamicPricingStage = dynamicStageFareService.getScheduleDynamicStageFare(authDTO, tripDTO.getSchedule(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation());
						if (dynamicPricingStage != null) {
							ScheduleDynamicStageFareDetailsDTO dynamicStageTripFareDetails = dynamicStageFareService.getDynamicPricingTripStageFareDetailsV2(authDTO, tripDTO.getSchedule(), dynamicPricingStage, scheduleDynamicStageFareList);
							// Dynamic Seat Fare
							Map<String, BusSeatLayoutDTO> seatMap = new HashMap<String, BusSeatLayoutDTO>();
							if (dynamicStageTripFareDetails != null) {
								for (BusSeatLayoutDTO seatLayoutDTO : dynamicStageTripFareDetails.getSeatFare()) {
									seatMap.put(seatLayoutDTO.getName(), seatLayoutDTO);
								}
							}
							for (BusSeatLayoutDTO seatLayoutDTO : busDTO.getBusSeatLayoutDTO().getList()) {
								if (seatMap.get(seatLayoutDTO.getName()) != null) {
									seatLayoutDTO.setFare(seatMap.get(seatLayoutDTO.getName()).getFare());
								}
								else {
									seatLayoutDTO.setFare(dynamicPricingStage.getMinFare());
								}
							}
						}
						else {
							List<StageFareDTO> stageFare = stageDTO.getStageFare();
							Map<String, BigDecimal> fareMap = new HashMap<>();
							for (StageFareDTO fare : stageFare) {
								fareMap.put(fare.getBusSeatType().getCode(), fare.getFare());
							}
							for (BusSeatLayoutDTO seatLayoutDTO : busDTO.getBusSeatLayoutDTO().getList()) {
								if (fareMap.get(seatLayoutDTO.getBusSeatType().getCode()) != null) {
									seatLayoutDTO.setFare(fareMap.get(seatLayoutDTO.getBusSeatType().getCode()));
								}
								else {
									seatLayoutDTO.setFare(BigDecimal.ZERO);
								}
							}
						}
						stageDTO.setBus(busDTO);
					}
					if (tripDTO.getStageList().isEmpty()) {
						tripIterator.remove();
						continue;
					}
				}
				catch (Exception e) {
					System.out.println("DPAllschedule Error: " + tripDTO.getSchedule().getCode() + tripDTO.getCode());
					e.printStackTrace();
					tripIterator.remove();
					continue;
				}
			}
			activeAllTrips.addAll(activeList);
		}
		return activeAllTrips;
	}

	public List<TripDTO> getBookedBlockedTickets(AuthDTO authDTO, List<String> tripCodeList, DateTime syncTime) {
		List<TripDTO> tripList = new ArrayList<TripDTO>();
		for (String tripCode : tripCodeList) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);

			tripService.getTrip(authDTO, tripDTO);
			tripService.getBookedBlockedSeats(authDTO, tripDTO);
			List<TicketDetailsDTO> ticketDetailsList = new ArrayList<TicketDetailsDTO>();
			scheduleService.getSchedule(authDTO, tripDTO.getSchedule());
			tripDTO.getSchedule().setTripDate(tripDTO.getTripDate());
			// Apply Bus Override
			ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, tripDTO.getSchedule());
			BusDTO busOverrideDTO = busOverrideService.applyScheduleBusOverride(authDTO, tripDTO.getSchedule(), scheduleBusDTO.getBus());
			if (busOverrideDTO != null) {
				scheduleBusDTO.setBus(busOverrideDTO);
			}
			tripDTO.getSchedule().setTax(scheduleBusDTO.getTax());

			for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
					continue;
				}
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId()) {
					continue;
				}
				// Validate PBL Block Live Time
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && BitsUtil.validateBlockReleaseTime(ticketDetailsDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTimeV2(), ticketDetailsDTO.getUpdatedAt())) {
					continue;
				}
				// Validate Sync time
				if (DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), syncTime) > 0) {
					continue;
				}
				stationService.getStation(ticketDetailsDTO.getFromStation());
				stationService.getStation(ticketDetailsDTO.getToStation());
				ticketDetailsDTO.setSeatFare(ticketDetailsDTO.getSeatFare().multiply(Numeric.ONE_HUNDRED.divide(Numeric.ONE_HUNDRED.add(tripDTO.getSchedule().getTax().getServiceTax()), 6, RoundingMode.HALF_UP)));
				ticketDetailsDTO.setAcBusTax(ticketDetailsDTO.getSeatFare().divide(Numeric.ONE_HUNDRED).multiply(tripDTO.getSchedule().getTax().getServiceTax()));
				ticketDetailsList.add(ticketDetailsDTO);
			}
			tripDTO.setTicketDetailsList(ticketDetailsList);
			tripList.add(tripDTO);
		}
		return tripList;
	}

	private BusDTO processStageSeatlayout(AuthDTO authDTO, UserDTO userDTO, TripDTO tripDTO, StageDTO stageDTO, SearchDTO searchDTO, Map<String, List<ScheduleSeatVisibilityDTO>> allocatedMap, Map<String, List<ScheduleSeatVisibilityDTO>> stageSeatMap) {
		// Prepare seat Visibility start
		// Apply Seat visibility
		List<BusSeatLayoutDTO> seatList = new ArrayList<BusSeatLayoutDTO>();
		BusDTO busDTO = new BusDTO();
		busDTO.setCode(tripDTO.getBus().getCode());
		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
			seatLayoutDTO.setFare(BigDecimal.ZERO);
			if (allocatedMap.get(seatLayoutDTO.getCode()) != null) {
				List<ScheduleSeatVisibilityDTO> visibilityDTOList = allocatedMap.get(seatLayoutDTO.getCode());
				for (ScheduleSeatVisibilityDTO visibilityDTO : visibilityDTOList) {
					UserDTO visibilityUserDTO = BitsUtil.isUserExists(visibilityDTO.getUserList(), userDTO);
					GroupDTO visibilityGroupDTO = BitsUtil.isGroupExists(visibilityDTO.getGroupList(), userDTO.getGroup());
					OrganizationDTO visibilityOrganizationDTO = BitsUtil.isOrganizationExists(visibilityDTO.getOrganizations(), userDTO.getOrganization());

					if (visibilityDTO.getVisibilityType().equals("HIDE")) {
						if (visibilityDTO.getUserList() != null && !visibilityDTO.getUserList().isEmpty() && visibilityUserDTO != null && visibilityUserDTO.getId() != 0) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
							seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
							seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
							seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
							seatLayoutDTO.setUser(visibilityUserDTO);
						}
						else if (visibilityDTO.getUserList() != null && visibilityDTO.getUserList().isEmpty()) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
							seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
							seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
							seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
						}
						else if (visibilityDTO.getGroupList() != null && visibilityDTO.getGroupList().isEmpty()) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
							seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
							seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
							seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
						}
						else if (visibilityDTO.getGroupList() != null && visibilityGroupDTO != null && visibilityGroupDTO.getId() != 0) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
							seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
							seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
							seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
							seatLayoutDTO.setGroup(visibilityGroupDTO);
						}
						else if (visibilityDTO.getOrganizations() != null && !visibilityDTO.getOrganizations().isEmpty() && visibilityOrganizationDTO != null && visibilityOrganizationDTO.getId() != 0) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
							seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
							seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
							seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
							seatLayoutDTO.setSeatGendar(SeatGendarEM.MALE);
						}
						else if (visibilityDTO.getOrganizations() != null && visibilityDTO.getOrganizations().isEmpty()) {
							seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
							seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
							seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
							seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
							seatLayoutDTO.setSeatGendar(SeatGendarEM.MALE);
						}
					}
					else if (visibilityDTO.getVisibilityType().equals("ACAT")) {
						if (visibilityDTO.getGroupList() != null && !visibilityDTO.getGroupList().isEmpty()) {
							if (visibilityGroupDTO != null && visibilityGroupDTO.getId() != 0) {
								seatLayoutDTO.setGroup(visibilityGroupDTO);
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
							}
							else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
								seatLayoutDTO.setGroup(Iterables.getFirst(visibilityDTO.getGroupList(), null));
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
							}
						}
						else if (visibilityDTO.getUserList() != null && !visibilityDTO.getUserList().isEmpty()) {
							if (visibilityUserDTO != null && visibilityUserDTO.getId() != 0) {
								seatLayoutDTO.setUser(visibilityUserDTO);
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
							}
							else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
								seatLayoutDTO.setUser(Iterables.getFirst(visibilityDTO.getUserList(), null));
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
							}
						}
						else if (visibilityDTO.getOrganizations() != null && !visibilityDTO.getOrganizations().isEmpty()) {
							if (visibilityOrganizationDTO != null && visibilityOrganizationDTO.getId() != 0) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
							}
							else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
								seatLayoutDTO.setSeatGendar(SeatGendarEM.MALE);
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

			ScheduleStationDTO fromStationDTO = BitsUtil.getOriginStation(tripDTO.getStationList());
			ScheduleStationDTO toStationDTO = BitsUtil.getDestinationStation(tripDTO.getStationList());
			int fromStationSquence = fromStationDTO != null ? fromStationDTO.getStationSequence() : 0;
			int toStationSquence = toStationDTO != null ? toStationDTO.getStationSequence() : 0;
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

}
