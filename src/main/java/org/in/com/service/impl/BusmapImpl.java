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

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.cache.BusCache;
import org.in.com.cache.ScheduleCache;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDiscountDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.ScheduleSeatAutoReleaseDTO;
import org.in.com.dto.ScheduleSeatFareDTO;
import org.in.com.dto.ScheduleSeatPreferenceDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.ScheduleTimeOverrideDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripSeatQuotaDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.BusCategoryTypeEM;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.ReleaseTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusmapService;
import org.in.com.service.GroupService;
import org.in.com.service.NamespaceTaxService;
import org.in.com.service.ScheduleBusOverrideService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleDiscountService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.ScheduleEnrouteBookControlService;
import org.in.com.service.ScheduleFareOverrideService;
import org.in.com.service.ScheduleSeatAutoReleaseService;
import org.in.com.service.ScheduleSeatFareService;
import org.in.com.service.ScheduleSeatPreferenceService;
import org.in.com.service.ScheduleSeatVisibilityService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleStationPointService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.ScheduleTimeOverrideService;
import org.in.com.service.SectorService;
import org.in.com.service.StationPointService;
import org.in.com.service.StationService;
import org.in.com.service.TripSeatQuotaService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
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
public class BusmapImpl extends HelperUtil implements BusmapService {
	@Autowired
	TripService tripService;
	@Autowired
	ScheduleSeatVisibilityService visibilityService;
	@Autowired
	ScheduleSeatPreferenceService seatPreferenceService;
	@Autowired
	ScheduleSeatFareService seatFareService;
	@Autowired
	ScheduleFareOverrideService fareOverrideService;
	@Autowired
	ScheduleSeatAutoReleaseService autoReleaseService;
	@Autowired
	ScheduleTimeOverrideService timeOverrideService;
	@Autowired
	ScheduleStageService stageService;
	@Autowired
	ScheduleDiscountService discountService;
	@Autowired
	NamespaceTaxService taxService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	ScheduleStationPointService scheduleStationPointService;
	@Autowired
	ScheduleDynamicStageFareService dynamicStageFareService;
	@Autowired
	TripSeatQuotaService quotaService;
	@Autowired
	ScheduleBusService busService;
	@Autowired
	ScheduleBusOverrideService busOverrideService;
	@Autowired
	ScheduleEnrouteBookControlService enrouteBookControlService;
	@Autowired
	GroupService groupService;
	@Autowired
	UserService userService;
	@Autowired
	StationService stationService;
	@Autowired
	StationPointService stationPointService;
	@Autowired
	SectorService sectorService;

	private static final Logger logger = LoggerFactory.getLogger(BusmapImpl.class);

	public TripDTO getSearchBusmapV3(AuthDTO authDTO, TripDTO tripStageDTO) {
		TripDTO tripDTO = tripService.getTripStageDetails(authDTO, tripStageDTO);
		if (tripDTO == null || tripDTO.getSchedule() == null || tripDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.TRIP_STATGE_CODE);
		}
		if ((tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_CLOSED.getId() || tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_YET_OPEN.getId()) && authDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId()) {
			throw new ServiceException(ErrorCode.TRIP_CLOSED_NOT_ALLOW_BOOKING);
		}
		TripDTO returnTripDTO = getBusmapforTrip(authDTO, tripDTO);

		if (returnTripDTO == null || returnTripDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.TRIP_STAGE_CLOSED_NOT_ALLOW_BOOKING);
		}
		// Get booked and phone blocked seat status
		tripService.getBookedBlockedSeats(authDTO, returnTripDTO);
		applyBookedBlockedSeat(authDTO, returnTripDTO);

		// Get Quota seat Details
		List<TripSeatQuotaDTO> tripSeatQuatoList = quotaService.getAllTripSeatQuota(authDTO, tripDTO);
		applyTripSeatQuota(authDTO, returnTripDTO, tripSeatQuatoList);

		enrouteBookControlService.applyScheduleEnrouteBookControl(authDTO, tripDTO);

		// Apply Gendar Validation
		applyMultiStageGendarValidations(authDTO, returnTripDTO, tripSeatQuatoList);

		if (authDTO.getNamespace().getProfile().isAliasNamespaceFlag() && authDTO.isSectorEnabled() && authDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId()) {
			applySector(authDTO, returnTripDTO);
		}
		return returnTripDTO;
	}

	private void applySector(AuthDTO authDTO, TripDTO tripDTO) {
		if (tripDTO != null && BitsUtil.isTagExists(authDTO.getUser().getUserTags(), UserTagEM.API_USER_RB)) {
			SectorDTO sector = sectorService.getUserActiveSector(authDTO, authDTO.getUser());
			if (sector.getActiveFlag() == 1 && BitsUtil.isScheduleExists(sector.getSchedule(), tripDTO.getSchedule()) == null) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}
		}
	}

	public TripDTO getBusmapforTrip(AuthDTO authDTO, TripDTO tripDTO) {
		TripDTO returnTripDTO = null;
		try {
			// update station from Cache
			tripDTO.getStage().getFromStation().setStation(stationService.getStation(tripDTO.getStage().getFromStation().getStation()));
			tripDTO.getStage().getToStation().setStation(stationService.getStation(tripDTO.getStage().getToStation().getStation()));

			logger.info("Getting bus map for thr trip from : " + tripDTO.getStage().getFromStation().getStation().getName() + " to station: " + tripDTO.getStage().getToStation().getStation().getName());

			ScheduleDTO scheduleDTO = getScheduleMap(authDTO, tripDTO);
			if (scheduleDTO != null) {
				returnTripDTO = ConvertScheduleToTrip(authDTO, tripDTO, scheduleDTO);
			}
		}
		catch (ServiceException e) {
			logger.info("Exception while getting the bus map for trip" + e.getErrorCode() + " " + tripDTO.getCode());
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception while getting the bus map for trip" + e.getMessage());
		}
		return returnTripDTO;
	}

	private ScheduleDTO getScheduleMap(AuthDTO authDTO, TripDTO tripDTO) {
		ScheduleCache cache = new ScheduleCache();
		UserDTO userDTO = authDTO.getUser();
		// get Available Stage for Given Route
		ScheduleDTO scheduleDTO = tripDTO.getSchedule();
		StationDTO fromStation = tripDTO.getStage().getFromStation().getStation();
		StationDTO toStation = tripDTO.getStage().getToStation().getStation();

		// Schedule Validations and status active flag
		ScheduleDTO schedule = cache.getScheduleDTObyId(authDTO, scheduleDTO);
		if (schedule == null || schedule.getId() == 0 || schedule.getActiveFlag() != 1) {
			return null;
		}
		scheduleDTO.setTripDate(tripDTO.getTripDate());

		List<ScheduleStageDTO> stageList = stageService.getByScheduleId(authDTO, tripDTO.getSchedule(), fromStation, toStation);
		if (stageList.isEmpty()) {
			throw new ServiceException(ErrorCode.TRIP_STAGE_CLOSED_NOT_ALLOW_BOOKING);
		}
		// Identify group level fare
		boolean stageFareFoundGroupLevel = false;
		for (Iterator<ScheduleStageDTO> iterator = stageList.iterator(); iterator.hasNext();) {
			ScheduleStageDTO scheduleStageDTO = iterator.next();

			// Stage Date Time validations
			if (scheduleStageDTO.getActiveFrom() != null && tripDTO.getTripDate().lt(new DateTime(scheduleStageDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (scheduleStageDTO.getActiveTo() != null && !tripDTO.getTripDate().lteq(new DateTime(scheduleStageDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (scheduleStageDTO.getDayOfWeek() != null && scheduleStageDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (scheduleStageDTO.getDayOfWeek() != null && scheduleStageDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}

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
				if (!tripDTO.getTripDate().gteq(new DateTime(OverrideScheduleStageDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!tripDTO.getTripDate().lteq(new DateTime(OverrideScheduleStageDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (OverrideScheduleStageDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (OverrideScheduleStageDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
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

		for (Iterator<ScheduleStageDTO> itrStage = stageList.iterator(); itrStage.hasNext();) {
			ScheduleStageDTO scheduleStageDTO = itrStage.next();
			if (scheduleStageDTO.getFare() == 0) {
				itrStage.remove();
				continue;
			}
			// Remove stage fare if group level found
			if (stageFareFoundGroupLevel && scheduleStageDTO.getGroup() != null && scheduleStageDTO.getGroup().getId() != userDTO.getGroup().getId()) {
				itrStage.remove();
				continue;
			}
			scheduleStageDTO.setFromStation(stationService.getStation(scheduleStageDTO.getFromStation()));
			scheduleStageDTO.setToStation(stationService.getStation(scheduleStageDTO.getToStation()));

			// Group the Stage by schedule Id and get corresponding Group fare
			if (scheduleDTO.getScheduleStageList() == null) {
				List<ScheduleStageDTO> scheduleStageDTOList = new ArrayList<>();
				if (scheduleStageDTO.getGroup() == null || scheduleStageDTO.getGroup().getId() == 0) {
					scheduleStageDTOList.add(scheduleStageDTO);
					scheduleDTO.setScheduleStageList(scheduleStageDTOList);
				}
				else if (scheduleStageDTO.getGroup().getId() == userDTO.getGroup().getId()) {
					scheduleStageDTOList.add(scheduleStageDTO);
					scheduleDTO.setScheduleStageList(scheduleStageDTOList);
				}
			}
			else {
				if (scheduleStageDTO.getGroup() == null || scheduleStageDTO.getGroup().getId() == 0) {
					scheduleDTO.getScheduleStageList().add(scheduleStageDTO);
				}
				else if (scheduleStageDTO.getGroup().getId() == userDTO.getGroup().getId()) {
					scheduleDTO.getScheduleStageList().add(scheduleStageDTO);
				}
			}
		}

		// copy schedule data
		scheduleDTO.setId(schedule.getId());
		scheduleDTO.setCode(schedule.getCode());
		scheduleDTO.setActiveFrom(schedule.getActiveFrom());
		scheduleDTO.setActiveTo(schedule.getActiveTo());
		scheduleDTO.setPreRequrities(schedule.getPreRequrities());
		scheduleDTO.setDayOfWeek(schedule.getDayOfWeek());
		scheduleDTO.setOverrideList(schedule.getOverrideList());
		scheduleDTO.setPnrStartCode(schedule.getPnrStartCode());

		scheduleDTO.setName(schedule.getName());
		scheduleDTO.setDisplayName(schedule.getDisplayName());
		scheduleDTO.setTripDate(schedule.getTripDate());
		// common validations
		DateTime scheduleFromDate = new DateTime(scheduleDTO.getActiveFrom());
		DateTime scheduleEndDate = new DateTime(scheduleDTO.getActiveTo());
		if (!scheduleDTO.getPreRequrities().equals("000000")) {
			return null;
		}
		if (!tripDTO.getTripDate().gteq(scheduleFromDate)) {
			return null;
		}
		if (!tripDTO.getTripDate().lteq(scheduleEndDate)) {
			return null;
		}
		if (scheduleDTO.getDayOfWeek() == null || scheduleDTO.getDayOfWeek().length() != 7) {
			return null;
		}
		if (scheduleDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
			return null;
		}

		// check for any exception has been added
		List<ScheduleDTO> scheduleOverRideList = scheduleDTO.getOverrideList();
		for (Iterator<ScheduleDTO> itrlookupSchedule = scheduleOverRideList.iterator(); itrlookupSchedule.hasNext();) {
			ScheduleDTO lookupscheduleDTO = itrlookupSchedule.next();
			// common validations
			if (!tripDTO.getTripDate().gteq(new DateTime(lookupscheduleDTO.getActiveFrom()))) {
				itrlookupSchedule.remove();
				continue;
			}
			if (!tripDTO.getTripDate().lteq(new DateTime(lookupscheduleDTO.getActiveTo()))) {
				itrlookupSchedule.remove();
				continue;
			}
			if (lookupscheduleDTO.getDayOfWeek() == null || lookupscheduleDTO.getDayOfWeek().length() != 7) {
				itrlookupSchedule.remove();
				continue;
			}
			if (lookupscheduleDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
				itrlookupSchedule.remove();
				continue;
			}
		}
		if (!scheduleOverRideList.isEmpty()) {
			return null;
		}
		// Validate all Booking Control
		boolean groupLevelFound = false;
		boolean stageLevelFound = false;
		List<ScheduleControlDTO> controlList = cache.getScheduleControlDTO(authDTO, scheduleDTO);
		for (Iterator<ScheduleControlDTO> itrControlDTO = controlList.iterator(); itrControlDTO.hasNext();) {
			ScheduleControlDTO controlDTO = itrControlDTO.next();
			// common validations
			if (controlDTO.getActiveFrom() != null && !tripDTO.getTripDate().gteq(new DateTime(controlDTO.getActiveFrom()))) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getActiveTo() != null && !tripDTO.getTripDate().lteq(new DateTime(controlDTO.getActiveTo()))) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getDayOfWeek() != null && controlDTO.getDayOfWeek().length() != 7) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getDayOfWeek() != null && controlDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
				itrControlDTO.remove();
				continue;
			}
			// alternate days
			if (controlDTO.getDayOfWeek().equals("ALRNATE") && !DateUtil.isFallonAlternateDays(new DateTime(controlDTO.getActiveFrom()), tripDTO.getTripDate())) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0 && controlDTO.getGroup().getId() != userDTO.getGroup().getId()) {
				itrControlDTO.remove();
				continue;
			}
			// Check for Stage based booking control
			if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0 && controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0 && (controlDTO.getFromStation().getId() != fromStation.getId() || controlDTO.getToStation().getId() != toStation.getId())) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0 && controlDTO.getGroup().getId() == userDTO.getGroup().getId()) {
				groupLevelFound = true;
			}
			if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0 && controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0 && controlDTO.getFromStation().getId() == fromStation.getId() && controlDTO.getToStation().getId() == toStation.getId()) {
				stageLevelFound = true;
			}
			// Override and Exceptions
			for (Iterator<ScheduleControlDTO> overrideItrControlDTO = controlDTO.getOverrideList().iterator(); overrideItrControlDTO.hasNext();) {
				ScheduleControlDTO overrideControlDTO = overrideItrControlDTO.next();
				// common validations
				if (overrideControlDTO.getActiveFrom() != null && !tripDTO.getTripDate().gteq(new DateTime(overrideControlDTO.getActiveFrom()))) {
					overrideItrControlDTO.remove();
					continue;
				}
				if (overrideControlDTO.getActiveTo() != null && !tripDTO.getTripDate().lteq(new DateTime(overrideControlDTO.getActiveTo()))) {
					overrideItrControlDTO.remove();
					continue;
				}
				if (overrideControlDTO.getDayOfWeek() != null && overrideControlDTO.getDayOfWeek().length() != 7) {
					overrideItrControlDTO.remove();
					continue;
				}
				if (overrideControlDTO.getDayOfWeek() != null && overrideControlDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
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
				if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0 && controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0 && (controlDTO.getFromStation().getId() != fromStation.getId() || controlDTO.getToStation().getId() != toStation.getId())) {
					iterator.remove();
					continue;
				}
				if (controlDTO.getFromStation() == null || controlDTO.getFromStation().getId() == 0 || controlDTO.getToStation() == null || controlDTO.getToStation().getId() == 0) {
					iterator.remove();
					continue;
				}
			}
		}
		// Station
		List<ScheduleStationDTO> stationList = scheduleStationService.getScheduleStation(authDTO, scheduleDTO);
		// Validate all stations
		Map<Integer, ScheduleStationDTO> stationMap = new HashMap<Integer, ScheduleStationDTO>();
		for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
			ScheduleStationDTO stationDTO = iterator.next();
			// Remove if Exceptions
			if (stationDTO.getMinitues() < 0) {
				stationDTO.setActiveFlag(-1);
				// iterator.remove();
				break;
			}
			int overrideRecentDays = 0;
			// Exception and override
			for (Iterator<ScheduleStationDTO> overrideIterator = stationDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleStationDTO overrideStationDTO = overrideIterator.next();
				// common validations
				if (StringUtil.isNotNull(overrideStationDTO.getActiveFrom()) && !tripDTO.getTripDate().gteq(new DateTime(overrideStationDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (StringUtil.isNotNull(overrideStationDTO.getActiveTo()) && !tripDTO.getTripDate().lteq(new DateTime(overrideStationDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (StringUtil.isNotNull(overrideStationDTO.getDayOfWeek()) && overrideStationDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (StringUtil.isNotNull(overrideStationDTO.getDayOfWeek()) && overrideStationDTO.getDayOfWeek().substring(tripDTO.getTripDate().getWeekDay() - 1, tripDTO.getTripDate().getWeekDay()).equals("0")) {
					overrideIterator.remove();
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
					overrideIterator.remove();
					continue;
				}
				else if (stationDTO.getMinitues() < 2880 && overrideStationDTO.getMinitues() >= 2880) {
					overrideIterator.remove();
					continue;
				}
				else if (stationDTO.getMinitues() < 4320 && overrideStationDTO.getMinitues() >= 4320) {
					overrideIterator.remove();
					continue;
				}
				else if (stationDTO.getMinitues() < 5760 && overrideStationDTO.getMinitues() >= 5760) {
					overrideIterator.remove();
					continue;
				}
				else if (stationDTO.getMinitues() < 7200 && overrideStationDTO.getMinitues() >= 7200) {
					overrideIterator.remove();
					continue;
				}
				if (overrideRecentDays == 0 || DateUtil.getDayDifferent(new DateTime(overrideStationDTO.getActiveFrom()), new DateTime(overrideStationDTO.getActiveTo())) <= overrideRecentDays) {
					stationDTO.setMinitues(overrideStationDTO.getMinitues());
					overrideRecentDays = DateUtil.getDayDifferent(new DateTime(overrideStationDTO.getActiveFrom()), new DateTime(overrideStationDTO.getActiveTo())) + 1;
				}
				// stationDTO.setMinitues(overrideStationDTO.getMinitues());
			}
			// stationDTO.setStation(getStationDTO(stationDTO.getStation()));
			stationMap.put(stationDTO.getStation().getId(), stationDTO);
		}
		if (scheduleDTO.getScheduleStageList() == null) {
			System.out.println("ERROR Busmap " + tripDTO.getCode() + " - " + tripDTO.getStage().getCode());
		}
		for (Iterator<ScheduleStageDTO> iterator = scheduleDTO.getScheduleStageList().iterator(); iterator.hasNext();) {
			ScheduleStageDTO stageDTO = iterator.next();
			if (stationMap.get(stageDTO.getFromStation().getId()) == null || stationMap.get(stageDTO.getToStation().getId()) == null) {
				iterator.remove();
				continue;
			}
			// Remove stage if station Exception
			if (stationMap.get(stageDTO.getFromStation().getId()).getActiveFlag() == -1 || stationMap.get(stageDTO.getToStation().getId()).getActiveFlag() == -1) {
				iterator.remove();
				continue;
			}
		}
		// Schedule Station Point
		List<ScheduleStationPointDTO> stationPointList = scheduleStationPointService.getActiveScheduleStationPointList(authDTO, scheduleDTO, tripDTO.getSearch(), stationMap);

		if (stationPointList.isEmpty()) {
			return null;
		}
		// Bus Type and BusMap
		ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);

		if (scheduleBusDTO == null) {
			return null;
		}
		// Validate all bus
		BusCache busCache = new BusCache();
		scheduleBusDTO.setBus(busCache.getBusDTObyId(authDTO, scheduleBusDTO.getBus()));
		if (scheduleBusDTO.getBus() == null || StringUtil.isNull(scheduleBusDTO.getBus().getCode())) {
			return null;
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
						// scheduleDTO.setAcBusTax(BigDecimal.ZERO);
						break;
					}
				}
			}
		}
		// Seat Allocation and Deallocations
		List<ScheduleSeatVisibilityDTO> seatVisibilityList = visibilityService.getByScheduleId(authDTO, scheduleDTO);

		// Seat Preference
		List<ScheduleSeatPreferenceDTO> seatPreferenceList = seatPreferenceService.getByScheduleId(authDTO, scheduleDTO);

		// Seat Auto Release
		List<ScheduleSeatAutoReleaseDTO> seatAutoReleaseList = autoReleaseService.getByScheduleId(authDTO, scheduleDTO);

		// Seat Fare
		List<ScheduleSeatFareDTO> seatFarelist = seatFareService.getByScheduleId(authDTO, scheduleDTO, fromStation, toStation);

		// Fare Override
		List<ScheduleFareAutoOverrideDTO> autoFareOverridelist = fareOverrideService.getByScheduleId(authDTO, scheduleDTO, fromStation, toStation);

		// Time Override
		List<ScheduleTimeOverrideDTO> timeOverridelist = timeOverrideService.getByScheduleId(authDTO, scheduleDTO);

		// collect other stages list to check blocked/booked seat status
		List<ScheduleStageDTO> otherScheduleStageList = cache.getScheduleStageDTO(authDTO, scheduleDTO);

		// Schedule Discount
		ScheduleDiscountDTO scheduleDiscountDTO = discountService.getByScheduleId(authDTO, scheduleDTO);

		// Dynamic Stage Fare
		if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
			ScheduleDynamicStageFareDetailsDTO dynamicStageFare = dynamicStageFareService.getScheduleDynamicStageFare(authDTO, schedule, fromStation, toStation);
			if (dynamicStageFare != null) {
				ScheduleDynamicStageFareDetailsDTO dynamicStageTripFareDetails = dynamicStageFareService.getDynamicPricingTripStageFareDetails(authDTO, tripDTO.getSchedule(), dynamicStageFare);
				dynamicStageFare.setSeatFare(dynamicStageTripFareDetails != null ? dynamicStageTripFareDetails.getSeatFare() : null);
				scheduleDTO.setDynamicStageFare(dynamicStageFare);
			}
		}
		/** Apply Schedule Tax */
		/**
		 * Apply GST Exception to OTA, Validate User Tag, GST Exception groups
		 */
		if (scheduleDTO.getTax() != null && (BitsUtil.isTagExists(authDTO.getUser().getUserTags(), Constants.GST_EXCEPTION_TAG) || authDTO.getNamespace().getProfile().isGstExceptionGroup(authDTO.getUser().getGroup()))) {
			scheduleDTO.getTax().setCgstValue(BigDecimal.ZERO);
			scheduleDTO.getTax().setSgstValue(BigDecimal.ZERO);
			scheduleDTO.getTax().setUgstValue(BigDecimal.ZERO);
			scheduleDTO.getTax().setIgstValue(BigDecimal.ZERO);
			scheduleDTO.getTax().setGstin(Text.NA);
			scheduleDTO.getTax().setId(Numeric.ZERO_INT);
			scheduleDTO.getTax().setTradeName(Text.NA);
		}
		else if (schedule.getTax() != null && schedule.getTax().getId() != 0) {
			scheduleDTO.setTax(taxService.getTaxbyStateV2(authDTO, schedule.getTax(), fromStation.getState()));
		}

		// add to schedule
		scheduleDTO.setScheduleBus(scheduleBusDTO);
		scheduleDTO.setSeatVisibilityList(seatVisibilityList);
		scheduleDTO.setSeatAutoReleaseList(seatAutoReleaseList);
		scheduleDTO.setSeatPreferenceList(seatPreferenceList);
		scheduleDTO.setStationList(stationList);
		scheduleDTO.setStationPointList(stationPointList);
		scheduleDTO.setControlList(controlList);
		scheduleDTO.setTimeOverrideList(timeOverridelist);
		scheduleDTO.setOtherSscheduleStageList(otherScheduleStageList);
		scheduleDTO.setScheduleDiscount(scheduleDiscountDTO);
		scheduleDTO.setFareAutoOverrideList(autoFareOverridelist);
		scheduleDTO.setSeatFareList(seatFarelist);
		return scheduleDTO;
	}

	private TripDTO ConvertScheduleToTrip(AuthDTO authDTO, TripDTO tripDTO, ScheduleDTO scheduleDTO) {
		TripStatusEM tripStatus = tripDTO.getTripStatus();
		UserDTO userDTO = authDTO.getUser();
		try {
			DateTime now = DateUtil.NOW();

			// Schedule Stage
			if (scheduleDTO.getScheduleStageList().isEmpty()) {
				return null;
			}

			// Schedule Station
			if (scheduleDTO.getStationList().isEmpty()) {
				return null;
			}

			// Schedule Station Point
			if (scheduleDTO.getStationPointList().isEmpty()) {
				return null;
			}

			// Booking Control
			if (scheduleDTO.getControlList().isEmpty()) {
				return null;
			}

			// Schedule Bus
			if (scheduleDTO.getScheduleBus() == null || scheduleDTO.getScheduleBus().getBus() == null) {
				return null;
			}

			// Copy to Trip
			tripDTO.setCode(getGeneratedTripCode(authDTO, tripDTO.getSchedule(), tripDTO));
			tripDTO.setSchedule(scheduleDTO);
			tripDTO.setBus(scheduleDTO.getScheduleBus().getBus());
			tripDTO.setAmenities(scheduleDTO.getScheduleBus().getAmentiesList());
			tripDTO.setStationList(scheduleDTO.getStationList());

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
					// Copy from point from Cache
					stationPointDTO.setId(pointDTO.getStationPoint().getId());
					stationPointService.getStationPoint(authDTO, stationPointDTO);
					if (!stationPointDTO.isActive()) {
						iterator.remove();
						continue;
					}
					stationPointDTO.setCreditDebitFlag(pointDTO.getCreditDebitFlag());
					stationPointDTO.setMinitues(pointDTO.getMinitues());
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
					stationPointDTO.setFare(pointDTO.getFare());
					stageStationDTO.getStationPoint().add(stationPointDTO);
					stationMap.put(stageStationDTO.getStation().getId(), stageStationDTO);
				}
				else {
					iterator.remove();
					continue;
				}
			}
			// Identify Stage and fare with trip bus-type(using-bus-override)
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
					stageDTO.setCode(getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), tripDTO, tripDTO.getStage()));

					// Fare based on travel Date, handled next day travel date
					if (stageDTO.getCode().equals(tripDTO.getStage().getCode())) {
						stageDTO.setId(tripDTO.getStage().getId());
						stageDTO.setTravelDate(tripDTO.getStage().getTravelDate());
						fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
					}
					else if (stageDTO.getFromStation().getMinitues() <= 1440 && tripDTO.getTripDate().format("YYYY-MM-DD").equals(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD"))) {
						stageDTO.setTravelDate(tripDTO.getTripDate());
						fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
					}
					else if (stageDTO.getFromStation().getMinitues() > 1440 && stageDTO.getFromStation().getMinitues() <= 2880 && tripDTO.getTripDate().plusDays(1).format("YYYY-MM-DD").equals(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD"))) {
						stageDTO.setTravelDate(tripDTO.getTripDate().plusDays(1));
						fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
					}
					else if (stageDTO.getFromStation().getMinitues() > 2880 && stageDTO.getFromStation().getMinitues() <= 4320 && tripDTO.getTripDate().plusDays(2).format("YYYY-MM-DD").equals(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD"))) {
						stageDTO.setTravelDate(tripDTO.getTripDate().plusDays(2));
						fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
					}
					else if (stageDTO.getFromStation().getMinitues() > 4320 && stageDTO.getFromStation().getMinitues() <= 5760 && tripDTO.getTripDate().plusDays(3).format("YYYY-MM-DD").equals(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD"))) {
						stageDTO.setTravelDate(tripDTO.getTripDate().plusDays(3));
						fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
					}
					else if (stageDTO.getFromStation().getMinitues() > 5760 && stageDTO.getFromStation().getMinitues() <= 7200 && tripDTO.getTripDate().plusDays(4).format("YYYY-MM-DD").equals(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD"))) {
						stageDTO.setTravelDate(tripDTO.getTripDate().plusDays(4));
						fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
					}
				}
				else {
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
							seatMap.get(seatLayoutDTO.getCode()).setFare(calculateSeatFare(scheduleSeatFareDTO, fareMap.get(tripDTO.getStage().getFromStation().getStation().getId() + "_" + tripDTO.getStage().getToStation().getStation().getId()).getSeatFare(seatMap.get(seatLayoutDTO.getCode()).getBusSeatType())));
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
				return null;
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
					// After 10days, service open at start day midnight
					if (controlDTO.getOpenMinitues() > 14400) {
						tripStageOriginStationOpenMinutes = 0;
					}
					if (controlDTO.getAllowBookingFlag() != 1) {
						itrControlDTO.remove();
						tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
						continue;
					}
					int minutiesOpenDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationOpenMinutes));
					int minutiesCloseDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationCloseMinutes));
					if (minutiesOpenDiff >= controlDTO.getOpenMinitues()) {
						tripDTO.setTripStatus(TripStatusEM.TRIP_YET_OPEN);
						itrControlDTO.remove();
						continue;
					}
					if (controlDTO.getCloseMinitues() != -1 && minutiesCloseDiff <= controlDTO.getCloseMinitues()) {
						itrControlDTO.remove();
						tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
						continue;
					} // Add to list if Schedule open
					if (minutiesOpenDiff <= controlDTO.getOpenMinitues()) {
						tripDTO.setTripStatus(TripStatusEM.TRIP_OPEN);
						itrControlDTO.remove();
						continue;
					}
				}
				if (tripDTO.getTripStatus() == null) {
					tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
				}
				// Busmap Next day Travel Date Validation
				if (stageDTO.getFromStation().getMinitues() < 1440 && !tripDTO.getTripDate().format("YYYY-MM-DD").equals(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD"))) {
					tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
				}
				else if (stageDTO.getFromStation().getMinitues() >= 1440 && stageDTO.getFromStation().getMinitues() < 2880 && !tripDTO.getTripDate().plusDays(1).format("YYYY-MM-DD").equals(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD"))) {
					tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
				}
				else if (stageDTO.getFromStation().getMinitues() >= 2880 && stageDTO.getFromStation().getMinitues() < 4320 && !tripDTO.getTripDate().plusDays(2).format("YYYY-MM-DD").equals(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD"))) {
					tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
				}
				else if (stageDTO.getFromStation().getMinitues() >= 4320 && stageDTO.getFromStation().getMinitues() < 5760 && !tripDTO.getTripDate().plusDays(3).format("YYYY-MM-DD").equals(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD"))) {
					tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
				}
				else if (stageDTO.getFromStation().getMinitues() >= 5760 && stageDTO.getFromStation().getMinitues() < 7200 && !tripDTO.getTripDate().plusDays(4).format("YYYY-MM-DD").equals(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD"))) {
					tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
				}
				else if (stageDTO.getFromStation().getMinitues() >= 7200 && !tripDTO.getTripDate().plusDays(5).format("YYYY-MM-DD").equals(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD"))) {
					tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
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
			// check current time with boarding point time,remove it..Even
			// boarding point start early to station Time
			if (DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripFromStationMinitues - 180).lteq(now)) {

				// Permission check
				boolean bookAfterTripTimeFlag = false;
				if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId()) {
					List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
					Eventlist.add(MenuEventEM.BOOKING_AFTER_TRIP_TIME);
					BaseImpl baseImpl = new BaseImpl();
					MenuEventDTO menuEventDTO = baseImpl.getPrivilegeV2(authDTO, Eventlist);
					bookAfterTripTimeFlag = menuEventDTO.getEnabledFlag() == Numeric.ONE_INT ? true : false;
				}
				if (UserRoleEM.TABLET_POB_ROLE.getId() == authDTO.getUser().getUserRole().getId() || UserRoleEM.DRIVER.getId() == authDTO.getUser().getUserRole().getId()) {
					bookAfterTripTimeFlag = true;
				}
				for (Iterator<StationPointDTO> itrpointDTO = tripDTO.getStage().getFromStation().getStationPoint().iterator(); itrpointDTO.hasNext();) {
					StationPointDTO pointDTO = itrpointDTO.next();
					if (!bookAfterTripTimeFlag && DateUtil.addMinituesToDate(tripDTO.getSearch().getTravelDate(), tripFromStationMinitues + pointDTO.getMinitues()).lteq(now)) {
						itrpointDTO.remove();
						continue;
					}
				}
				if (tripDTO.getStage().getFromStation().getStationPoint().isEmpty() && tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_OPEN.getId()) {
					tripDTO.setTripStatus(TripStatusEM.TRIP_CLOSED);
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

				String stageCode = getGeneratedTripStageCode(authDTO, scheduleDTO, tripDTO, scheduleStageDTO);
				if (!releatedStageCodeList.contains(stageCode)) {
					releatedStageCodeList.add(stageCode);
				}
			}
			tripDTO.setReleatedStageCodeList(releatedStageCodeList);

			// Prepare seat Visibility
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
			for (ScheduleSeatVisibilityDTO visibilityDTO : scheduleDTO.getSeatVisibilityList()) {
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

			// Prepare Auto Release validations
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

			boolean isAllowBlockedSeatBooking = authDTO.getAdditionalAttribute().containsKey(Text.ALLOW_BLOCKED_SEAT_BOOKING_FLAG) ? authDTO.getAdditionalAttribute().get(Text.ALLOW_BLOCKED_SEAT_BOOKING_FLAG).equals(Numeric.ONE) : false;
			// Apply Seat visibility
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
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
								seatLayoutDTO.setUser(visibilityUserDTO);
								seatLayoutDTO.setSeatGendar(SeatGendarEM.MALE);
							}
							else if (visibilityDTO.getUserList() != null && visibilityDTO.getUserList().isEmpty()) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
								seatLayoutDTO.setSeatGendar(SeatGendarEM.MALE);
								// allow blocked seats booking - identify flag
								if (isAllowBlockedSeatBooking) {
									seatLayoutDTO.setPassengerAge(1);
									seatLayoutDTO.setSeatGendar(SeatGendarEM.ALL);
								}
							}
							else if (visibilityDTO.getGroupList() != null && visibilityDTO.getGroupList().isEmpty()) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
								seatLayoutDTO.setSeatGendar(SeatGendarEM.MALE);
								// allow blocked seats booking - identify flag
								if (isAllowBlockedSeatBooking) {
									seatLayoutDTO.setPassengerAge(1);
									seatLayoutDTO.setSeatGendar(SeatGendarEM.ALL);
								}
							}
							else if (visibilityDTO.getGroupList() != null && visibilityGroupDTO != null && visibilityGroupDTO.getId() != 0) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
								seatLayoutDTO.setGroup(visibilityGroupDTO);
								seatLayoutDTO.setSeatGendar(SeatGendarEM.MALE);
							}
							else if (visibilityDTO.getOrganizations() != null && !visibilityDTO.getOrganizations().isEmpty() && visibilityOrganizationDTO != null && visibilityOrganizationDTO.getId() != 0) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
								seatLayoutDTO.setOrganization(getOrganizationDTObyId(authDTO, visibilityOrganizationDTO));
								seatLayoutDTO.setSeatGendar(SeatGendarEM.MALE);
							}
							else if (visibilityDTO.getOrganizations() != null && visibilityDTO.getOrganizations().isEmpty()) {
								seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
								seatLayoutDTO.setSeatGendar(SeatGendarEM.MALE);
								// allow blocked seats booking - identify flag
								if (isAllowBlockedSeatBooking) {
									seatLayoutDTO.setPassengerAge(1);
									seatLayoutDTO.setSeatGendar(SeatGendarEM.ALL);
								}
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
									seatLayoutDTO.setOrganization(getOrganizationDTObyId(authDTO, visibilityOrganizationDTO));
									seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
									seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
									seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
									seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
								}
								else if (seatLayoutDTO.getSeatStatus() == null || seatLayoutDTO.getSeatStatus().getId() != SeatStatusEM.ALLOCATED_YOU.getId()) {
									OrganizationDTO organizationDTO = Iterables.getFirst(visibilityDTO.getOrganizations(), null);
									seatLayoutDTO.setOrganization(organizationDTO != null ? getOrganizationDTObyId(authDTO, organizationDTO) : null);
									seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
									seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
									seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
									seatLayoutDTO.setUpdatedAt(new DateTime(visibilityDTO.getUpdatedAt()));
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
								seatLayoutDTO.setPassengerName(Text.EMPTY);
								seatLayoutDTO.setUpdatedAt(null);
								seatLayoutDTO.setRemarks(Text.EMPTY);
								seatLayoutDTO.setSeatGendar(SeatGendarEM.ALL);
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
							seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
							seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
							routeDTO = Iterables.getFirst(visibilityDTO.getRouteList(), null);
							seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
							seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
						}
						else if (visibilityDTO.getVisibilityType().equals("ACAT") && visibilityDTO.getRouteList() != null && !visibilityDTO.getRouteList().isEmpty() && routeDTO != null) {
							if (visibilityDTO.getRouteUsers() != null && !visibilityDTO.getRouteUsers().isEmpty() && (visibilityRouteUserDTO == null || visibilityRouteUserDTO.getId() == 0)) {
								seatLayoutDTO.setUser(Iterables.getFirst(visibilityDTO.getRouteUsers(), null));
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_OTHER);
								seatLayoutDTO.setPassengerName(visibilityDTO.getUpdatedBy());
								seatLayoutDTO.setRemarks(visibilityDTO.getRemarks());
								routeDTO = Iterables.getFirst(visibilityDTO.getRouteList(), null);
								seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
								seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
							}
							else if (visibilityDTO.getRouteUsers() != null && !visibilityDTO.getRouteUsers().isEmpty() && visibilityRouteUserDTO != null && visibilityRouteUserDTO.getId() != 0) {
								seatLayoutDTO.setUser(visibilityRouteUserDTO);
								seatLayoutDTO.setSeatStatus(SeatStatusEM.ALLOCATED_YOU);
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
								// allow blocked seats booking - identify flag
								if (isAllowBlockedSeatBooking) {
									seatLayoutDTO.setPassengerAge(1);
									seatLayoutDTO.setSeatGendar(SeatGendarEM.ALL);
								}
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

			// preference seat
			Map<String, ScheduleSeatPreferenceDTO> preferenceMap = new HashMap<>();
			if (scheduleDTO.getSeatPreferenceList() != null) {
				for (Iterator<ScheduleSeatPreferenceDTO> itrSeatPreference = scheduleDTO.getSeatPreferenceList().iterator(); itrSeatPreference.hasNext();) {
					ScheduleSeatPreferenceDTO preferenceDTO = itrSeatPreference.next();
					for (Iterator<BusSeatLayoutDTO> itrlayout = preferenceDTO.getBus().getBusSeatLayoutDTO().getList().iterator(); itrlayout.hasNext();) {
						BusSeatLayoutDTO seatLayoutDTO = itrlayout.next();
						preferenceMap.put(seatLayoutDTO.getCode(), preferenceDTO);
					}
				}
				for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
					if (preferenceMap.get(seatLayoutDTO.getCode()) != null) {
						ScheduleSeatPreferenceDTO seatPreferenceDTO = preferenceMap.get(seatLayoutDTO.getCode());
						seatLayoutDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(seatPreferenceDTO.getGendar().getCode()));
					}
				}
			}
			// Cancellation datetime based on NS Settings
			DateTime travelDateTime = null;
			if (authDTO.getNamespace().getProfile().getCancellationTimeType().equals(Constants.STAGE)) {
				travelDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues());
			}
			else {
				// TODO need to handle with station exception
				int firstStageStationMinutes = tripDTO.getTripOriginMinutes();
				travelDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), firstStageStationMinutes);
			}
			tripDTO.getAdditionalAttributes().put(Constants.CANCELLATION_DATETIME, DateUtil.convertDateTime(travelDateTime));

		}
		catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception occurred while converting the schedule to trip" + e.getMessage());
		}
		// Copy DB Trip Status
		if (tripStatus != null && (tripStatus.getId() == TripStatusEM.TRIP_CLOSED.getId() || tripStatus.getId() == TripStatusEM.TRIP_CANCELLED.getId())) {
			tripDTO.setTripStatus(tripStatus);
		}
		return tripDTO;
	}

	protected boolean existStageInRouteList(List<RouteDTO> routeList, StationDTO fromStationDTO, StationDTO toStationDTO) {
		boolean status = false;
		// Route List
		for (RouteDTO routeDTO : routeList) {
			if (routeDTO.getFromStation().getId() == fromStationDTO.getId() && routeDTO.getToStation().getId() == toStationDTO.getId()) {
				status = true;
				break;
			}
		}
		return status;
	}

	protected GroupDTO existGroupInGroupList(List<GroupDTO> groupList, GroupDTO groupDTO) {
		GroupDTO existingGroup = null;
		// Route List
		for (GroupDTO group : groupList) {
			if (group.getId() != 0 && groupDTO.getId() != 0 && group.getId() == groupDTO.getId()) {
				existingGroup = group;
				break;
			}
		}
		return existingGroup;
	}

	@Override
	public List<TicketDTO> getBookedBlockedTickets(AuthDTO authDTO, TripDTO tripDTO) {
		Map<String, TicketDTO> ticketMap = new HashMap<>();
		tripService.getTrip(authDTO, tripDTO);
		tripService.getBookedBlockedSeats(authDTO, tripDTO);

		List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, tripDTO);

		StageStationDTO firstStageStationDTO = null;
		Map<String, Integer> stationPointMap = new HashMap<>();
		Map<Integer, Integer> stationMinutesMap = new HashMap<>();
		for (StageStationDTO stageStation : stageList) {
			if (firstStageStationDTO == null) {
				firstStageStationDTO = stageStation;
			}
			if (stageStation.getStationSequence() < firstStageStationDTO.getStationSequence()) {
				firstStageStationDTO = stageStation;
			}

			if (stageStation.getStationPoint() != null && !stageStation.getStationPoint().isEmpty()) {
				for (StationPointDTO stationPointDTO : stageStation.getStationPoint()) {
					stationPointMap.put(stationPointDTO.getCode(), stageStation.getMinitues() + stationPointDTO.getMinitues());
				}
			}
			stationMinutesMap.put(stageStation.getStation().getId(), stageStation.getMinitues());
		}
		DateTime originStationDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), firstStageStationDTO.getMinitues());
		DateTime originDateTime = BitsUtil.getOriginStationPointDateTime(stageList, tripDTO.getTripDate());
		int tripMinuties = 0;

		if (tripDTO.getTicketDetailsList() != null) {
			for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
					continue;
				}
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId()) {
					continue;
				}
				// remove not travel status seat
				if (ticketDetailsDTO.getTicketExtra() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus().getId() == TravelStatusEM.NOT_TRAVELED.getId()) {
					continue;
				}
				// Validate PBL Block Live Time
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && BitsUtil.validateBlockReleaseTime(ticketDetailsDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTimeV2(), ticketDetailsDTO.getUpdatedAt())) {
					continue;
				}

				if (StringUtil.isNotNull(ticketDetailsDTO.getTicketCode()) && ticketMap.get(ticketDetailsDTO.getTicketCode()) == null) {
					TicketDTO ticket = new TicketDTO();
					ticket.setPassengerMobile(ticketDetailsDTO.getContactNumber());
					ticket.setCode(ticketDetailsDTO.getTicketCode());
					ticket.setTicketUser(userService.getUser(authDTO, ticketDetailsDTO.getUser()));
					ticket.setFromStation(stationService.getStation(ticketDetailsDTO.getFromStation()));
					ticket.setToStation(stationService.getStation(ticketDetailsDTO.getToStation()));
					ticket.setUpdatedAt(ticketDetailsDTO.getUpdatedAt());
					ticket.setTicketAt(ticketDetailsDTO.getTicketAt());
					ticket.setTripDate(tripDTO.getTripDate());
					ticket.getTicketUser().setGroup(groupService.getGroup(authDTO, ticket.getTicketUser().getGroup()));
					if (ticketDetailsDTO.getTicketExtra().getTicketTransferMinutes() > 0) {
						ticket.setScheduleTicketTransferTerms(new ScheduleTicketTransferTermsDTO());
						ticket.getScheduleTicketTransferTerms().setMinutes(ticketDetailsDTO.getTicketExtra().getTicketTransferMinutes());

						DateTime dateTime = null;
						if (authDTO.getNamespace().getProfile().getCancellationTimeType().equals(Constants.STAGE) && stationMinutesMap.get(ticket.getFromStation().getId()) != null) {
							dateTime = DateUtil.minusMinituesToDate(tripDTO.getTripDate(), stationMinutesMap.get(ticket.getFromStation().getId()));
						}
						else {
							dateTime = DateUtil.minusMinituesToDate(originStationDateTime, ticket.getScheduleTicketTransferTerms().getMinutes());
						}
						ticket.getScheduleTicketTransferTerms().setDateTime(dateTime);
					}

					TicketExtraDTO ticketExtraDTO = new TicketExtraDTO();
					if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
						ticketExtraDTO.setReleaseAt(BitsUtil.getBlockReleaseDateTime(ticketDetailsDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTimeV2(), ticketDetailsDTO.getUpdatedAt()));
					}
					tripMinuties = DateUtil.getMinutiesDifferent(originDateTime, ticket.getTicketAt());
					if (tripMinuties > -30) {
						ticketExtraDTO.setTicketAfterTripTime(Numeric.ONE_INT);
					}

					ticket.setTicketExtra(ticketExtraDTO);

					if (StringUtil.isNotNull(ticketDetailsDTO.getStationPoint())) {
						String[] stationPoints = ticketDetailsDTO.getStationPoint().split(":-:");

						StationPointDTO boardingPoint = new StationPointDTO();
						boardingPoint.setCode(stationPoints[Numeric.ZERO_INT]);
						boardingPoint.setName(stationPoints[Numeric.ONE_INT]);
						boardingPoint.setMinitues(stationPointMap.get(boardingPoint.getCode()) != null ? stationPointMap.get(boardingPoint.getCode()) : Numeric.ZERO_INT);
						ticket.setBoardingPoint(boardingPoint);

						StationPointDTO droppingPoint = new StationPointDTO();
						droppingPoint.setCode(stationPoints[Numeric.TWO_INT]);
						droppingPoint.setName(stationPoints[Numeric.THREE_INT]);
						droppingPoint.setMinitues(stationPointMap.get(droppingPoint.getCode()) != null ? stationPointMap.get(droppingPoint.getCode()) : Numeric.ZERO_INT);
						ticket.setDroppingPoint(droppingPoint);
					}

					List<TicketDetailsDTO> ticketDetails = new ArrayList<>();
					ticketDetails.add(ticketDetailsDTO);
					ticket.setTicketDetails(ticketDetails);
					ticketMap.put(ticketDetailsDTO.getTicketCode(), ticket);
				}
				else if (StringUtil.isNotNull(ticketDetailsDTO.getTicketCode()) && ticketMap.get(ticketDetailsDTO.getTicketCode()) != null) {
					TicketDTO ticket = ticketMap.get(ticketDetailsDTO.getTicketCode());
					ticket.getTicketDetails().add(ticketDetailsDTO);
					ticketMap.put(ticketDetailsDTO.getTicketCode(), ticket);
				}
			}
		}

		// Get Quota seat Details
		List<TripSeatQuotaDTO> tripSeatQuatoList = quotaService.getAllTripSeatQuota(authDTO, tripDTO);
		if (tripSeatQuatoList != null) {
			for (TripSeatQuotaDTO seatQuotaDTO : tripSeatQuatoList) {
				TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
				ticketDetailsDTO.setSeatCode(seatQuotaDTO.getSeatDetails().getSeatCode());
				if (seatQuotaDTO.getSeatDetails().getSeatFare().compareTo(BigDecimal.ZERO) > 0) {
					ticketDetailsDTO.setSeatFare(seatQuotaDTO.getSeatDetails().getSeatFare());
					ticketDetailsDTO.setAcBusTax(seatQuotaDTO.getSeatDetails().getAcBusTax());
				}
				ticketDetailsDTO.setTicketStatus(TicketStatusEM.TRIP_SEAT_QUOTA);
				ticketDetailsDTO.setSeatGendar(seatQuotaDTO.getSeatDetails().getSeatGendar());

				if (ticketMap.get(seatQuotaDTO.getUser().getCode()) == null) {
					TicketDTO ticket = new TicketDTO();
					ticket.setTicketUser(seatQuotaDTO.getUser());
					ticket.setFromStation(seatQuotaDTO.getFromStation());
					ticket.setToStation(seatQuotaDTO.getToStation());
					ticket.setUpdatedAt(new DateTime(seatQuotaDTO.getUpdatedAt()));
					ticket.setTicketAt(ticket.getUpdatedAt());

					List<TicketDetailsDTO> ticketDetails = new ArrayList<>();
					ticketDetails.add(ticketDetailsDTO);
					ticket.setTicketDetails(ticketDetails);
					ticketMap.put(seatQuotaDTO.getUser().getCode(), ticket);
				}
				else if (ticketMap.get(seatQuotaDTO.getUser().getCode()) != null) {
					TicketDTO ticket = ticketMap.get(seatQuotaDTO.getUser().getCode());
					ticket.getTicketDetails().add(ticketDetailsDTO);
					ticketMap.put(seatQuotaDTO.getUser().getCode(), ticket);
				}
			}
		}
		return new ArrayList<TicketDTO>(ticketMap.values());
	}

	private String getGeneratedTripStageCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, TripDTO tripDTO, StageDTO stageDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(tripDTO.getTripDate()) + "D" + stageDTO.getFromStation().getStation().getId() + "T" + stageDTO.getToStation().getStation().getId();
	}
}
