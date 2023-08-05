package org.in.com.service.helper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.cache.BusCache;
import org.in.com.cache.CancellationTermsCache;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.ScheduleSeatAutoReleaseDTO;
import org.in.com.dto.ScheduleSeatFareDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.ScheduleTimeOverrideDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripSeatQuotaDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.BusCategoryTypeEM;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.FareOverrideTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.OverrideTypeEM;
import org.in.com.dto.enumeration.ReleaseTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.service.BusService;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleBusOverrideService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleStationPointService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.StationPointService;
import org.in.com.service.StationService;
import org.in.com.service.TripHelperService;
import org.in.com.service.UserService;
import org.in.com.service.impl.ScheduleBusOverrideServiceImpl;
import org.in.com.service.impl.ScheduleTimeOverrideImpl;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import hirondelle.date4j.DateTime;

@Service
public class TripHelperServiceImpl implements TripHelperService {

	@Autowired
	BusService busService;
	@Autowired
	StationService stationService;
	@Autowired
	StationPointService stationPointService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	GroupService groupService;
	@Autowired
	UserService userService;
	@Autowired
	StationPointService pointService;
	@Autowired
	ScheduleStationPointService scheduleStationPointService;
	@Autowired
	ScheduleStageService stageService;
	@Autowired
	ScheduleBusService scheduleBusService;
	@Autowired
	ScheduleStationService scheduleStationService;

	public List<TripDTO> ConvertScheduleToTrip(AuthDTO authDTO, SearchDTO searchDTO, List<ScheduleDTO> scheduleList) {
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
					stationMap.put(stationDTO.getStation().getId(), stageStationDTO);
				}
				for (Iterator<ScheduleStationPointDTO> iterator = scheduleDTO.getStationPointList().iterator(); iterator.hasNext();) {
					ScheduleStationPointDTO pointDTO = iterator.next();
					if (stationMap.get(pointDTO.getStation().getId()) != null) {
						StageStationDTO stageStationDTO = stationMap.get(pointDTO.getStation().getId());
						StationPointDTO stationPointDTO = new StationPointDTO();
						stationPointDTO.setId(pointDTO.getStationPoint().getId());
						// Copy station Point from cache
						pointService.getStationPoint(authDTO, stationPointDTO);
						if (!stationPointDTO.isActive()) {
							iterator.remove();
							continue;
						}
						stationPointDTO.setCreditDebitFlag(pointDTO.getCreditDebitFlag());
						stationPointDTO.setMinitues(pointDTO.getMinitues());
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
						if (scheduleDTO.getScheduleDiscount() != null) {
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
						// After 10days, service open at start day midnight
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
				int tripFromStationMinitues = tripDTO.getStage().getFromStation().getMinitues() > 1440 ? tripDTO.getStage().getFromStation().getMinitues() - 1440 : tripDTO.getStage().getFromStation().getMinitues();
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
									userService.getUser(authDTO, routeuUerDTO);
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
								if (visibilityDTO.getRouteUsers() != null && ((visibilityRouteUserDTO != null && visibilityRouteUserDTO.getId() != 0 && !visibilityDTO.getRouteUsers().isEmpty()) || (visibilityDTO.getRouteUsers().isEmpty()))) {
									seatLayoutDTO.setSeatStatus(SeatStatusEM.BLOCKED);
									routeDTO = Iterables.getFirst(visibilityDTO.getRouteList(), null);
									seatLayoutDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
									seatLayoutDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
								}
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
						}
					}
				}
				// Cancellation Terms
				CancellationTermsCache termsCache = new CancellationTermsCache();
				CancellationTermDTO cancellationTermDTO = termsCache.getCancellationTermDTOById(authDTO, scheduleDTO.getCancellationTerm());
				tripDTO.setCancellationTerm(cancellationTermDTO);

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
				tripDTO.setTripMinutes(tripDTO.getTripOriginMinutes());
				tripList.add(tripDTO);
			}
		}
		catch (Exception e) {
			System.out.println("Exception occurred when BLF3BI3499 converting the schedule to trip " + authDTO.getNamespaceCode() + " " + userDTO.getUsername());
			e.printStackTrace();
		}
		return getUniqueTripList(tripList);
	}

	protected void applyBookedBlockedSeat(AuthDTO authDTO, TripDTO tripDTO) {
		Map<String, List<TicketDetailsDTO>> statusMAP = new HashMap<String, List<TicketDetailsDTO>>();
		if (tripDTO != null && tripDTO.getTicketDetailsList() != null && !tripDTO.getTicketDetailsList().isEmpty()) {
			for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
					continue;
				}
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId()) {
					continue;
				}
				// Validate PBL Block Live Time
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && BitsUtil.validateBlockReleaseTime(ticketDetailsDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTime(), ticketDetailsDTO.getUpdatedAt())) {
					continue;
				}

				if (tripDTO.getReleatedStageCodeList().contains(ticketDetailsDTO.getTripStageCode())) {
					if (ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() && ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
						List<TicketDetailsDTO> list = null;
						if (statusMAP.get(ticketDetailsDTO.getSeatCode()) == null) {
							list = new ArrayList<TicketDetailsDTO>();
						}
						else {
							list = statusMAP.get(ticketDetailsDTO.getSeatCode());
						}
						list.add(ticketDetailsDTO);
						statusMAP.put(ticketDetailsDTO.getSeatCode(), list);
					}
				}
			}
		}
		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			List<TicketDetailsDTO> list = statusMAP.get(seatLayoutDTO.getCode());
			if (list != null && !list.isEmpty()) {
				for (TicketDetailsDTO ticketDetailsDTO : list) {
					if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.BOOKED);
					}
					else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.TEMP_BLOCKED);
						seatLayoutDTO.setUpdatedAt(ticketDetailsDTO.getUpdatedAt());
					}
					else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.PHONE_BLOCKED);
					}
					else if (seatLayoutDTO.getSeatStatus() == null) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
					}

					// Copy ticket details
					if ((SeatStatusEM.BOOKED.getId() == seatLayoutDTO.getSeatStatus().getId() || SeatStatusEM.BLOCKED.getId() == seatLayoutDTO.getSeatStatus().getId() || SeatStatusEM.TEMP_BLOCKED.getId() == seatLayoutDTO.getSeatStatus().getId() || SeatStatusEM.PHONE_BLOCKED.getId() == seatLayoutDTO.getSeatStatus().getId()) && StringUtil.isNull(seatLayoutDTO.getTicketCode())) {
						seatLayoutDTO.setSeatGendar(ticketDetailsDTO.getSeatGendar());
						seatLayoutDTO.setPassengerAge(ticketDetailsDTO.getPassengerAge());
						seatLayoutDTO.setPassengerName(ticketDetailsDTO.getPassengerName());
						seatLayoutDTO.setContactNumber(ticketDetailsDTO.getContactNumber());
						seatLayoutDTO.setTicketCode(ticketDetailsDTO.getTicketCode());
						seatLayoutDTO.setBoardingPointName(ticketDetailsDTO.getBoardingPointName());
						seatLayoutDTO.setStationPoint(ticketDetailsDTO.getStationPoint());
						seatLayoutDTO.setUser(userService.getUser(authDTO, ticketDetailsDTO.getUser()));
						seatLayoutDTO.setGroup(groupService.getGroup(authDTO, seatLayoutDTO.getUser().getGroup()));
						seatLayoutDTO.setFromStation(stationService.getStation(ticketDetailsDTO.getFromStation()));
						seatLayoutDTO.setToStation(stationService.getStation(ticketDetailsDTO.getToStation()));
						seatLayoutDTO.setUpdatedAt(ticketDetailsDTO.getUpdatedAt());
						if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId()) {
							seatLayoutDTO.setFare(ticketDetailsDTO.getSeatFare());
						}
					}
				}
			}
			else {
				try {
					if (seatLayoutDTO.getUser() != null && seatLayoutDTO.getUser().getId() != 0) {
						seatLayoutDTO.setUser(userService.getUser(authDTO, seatLayoutDTO.getUser()));
					}
					if (seatLayoutDTO.getGroup() != null && seatLayoutDTO.getGroup().getId() != 0) {
						seatLayoutDTO.setGroup(groupService.getGroup(authDTO, seatLayoutDTO.getGroup()));
					}
				}
				catch (Exception e) {
					System.out.println("High profile Error ERR100:" + tripDTO.getCode() + "-" + seatLayoutDTO.getUser().getId());
					e.printStackTrace();
				}
			}
		}
	}

	protected void applyTripSeatQuota(AuthDTO authDTO, TripDTO tripDTO, List<TripSeatQuotaDTO> tripSeatQuatoList) {
		try {
			Map<String, List<TripSeatQuotaDTO>> quotaMAP = new HashMap<String, List<TripSeatQuotaDTO>>();
			if (tripDTO != null && tripSeatQuatoList != null && !tripSeatQuatoList.isEmpty()) {
				for (TripSeatQuotaDTO seatQuotaDTO : tripSeatQuatoList) {
					if ((seatQuotaDTO.getFromStation().getId() == 0 && seatQuotaDTO.getToStation().getId() == 0) || tripDTO.getReleatedStageCodeList().contains(getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), tripDTO, seatQuotaDTO.getFromStation(), seatQuotaDTO.getToStation()))) {
						List<TripSeatQuotaDTO> list = null;
						if (quotaMAP.get(seatQuotaDTO.getSeatDetails().getSeatCode()) == null) {
							list = new ArrayList<TripSeatQuotaDTO>();
						}
						else {
							list = quotaMAP.get(seatQuotaDTO.getSeatDetails().getSeatCode());
						}
						list.add(seatQuotaDTO);
						quotaMAP.put(seatQuotaDTO.getSeatDetails().getSeatCode(), list);
					}
				}
			}
			for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
				List<TripSeatQuotaDTO> list = quotaMAP.get(seatLayoutDTO.getCode());
				if (list != null && !list.isEmpty()) {
					for (TripSeatQuotaDTO seatQuotaDTO : list) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.QUOTA_SEAT);

						// Copy ticket details
						seatLayoutDTO.setSeatGendar(seatQuotaDTO.getSeatDetails().getSeatGendar());
						seatLayoutDTO.setUser(userService.getUser(authDTO, seatQuotaDTO.getUser()));
						seatLayoutDTO.setFromStation(stationService.getStation(seatQuotaDTO.getFromStation()));
						seatLayoutDTO.setToStation(stationService.getStation(seatQuotaDTO.getToStation()));
						seatLayoutDTO.setUpdatedAt(new DateTime(seatQuotaDTO.getUpdatedAt()));
						seatLayoutDTO.setPassengerName(seatQuotaDTO.getUpdatedBy().getName());

						if (seatLayoutDTO.getUser() != null && seatLayoutDTO.getUser().getId() != 0) {
							seatLayoutDTO.setUser(userService.getUser(authDTO, seatLayoutDTO.getUser()));
						}
						if (seatLayoutDTO.getGroup() != null && seatLayoutDTO.getGroup().getId() != 0) {
							seatLayoutDTO.setGroup(groupService.getGroup(authDTO, seatLayoutDTO.getGroup()));
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getTripDetails(AuthDTO authDTO, TripDTO tripDTO) {
		try {
			DateTime tripDate = tripDTO.getTripDate();
			tripDTO.setSchedule(scheduleService.getSchedule(authDTO, tripDTO.getSchedule()));
			tripDTO.getSchedule().setTripDate(tripDate);
			// Schedule Stage
			List<ScheduleStageDTO> stageList = stageService.getByScheduleTripDate(authDTO, tripDTO.getSchedule(), tripDate);

			// Schedule Station
			List<ScheduleStationDTO> stationList = scheduleStationService.getByScheduleTripDate(authDTO, tripDTO.getSchedule(), tripDate);

			// Schedule Station Point
			List<ScheduleStationPointDTO> stationPointList = scheduleStationPointService.getByScheduleTripDate(authDTO, tripDTO.getSchedule(), tripDate);

			// Schedule Bus
			ScheduleBusDTO scheduleBusDTO = scheduleBusService.getByScheduleId(authDTO, tripDTO.getSchedule());

			ScheduleBusOverrideService busOverrideService = new ScheduleBusOverrideServiceImpl();
			BusDTO busDTO = busOverrideService.applyScheduleBusOverride(authDTO, tripDTO.getSchedule(), scheduleBusDTO.getBus());
			scheduleBusDTO.setBus(busDTO);

			ScheduleTimeOverrideImpl timeOverride = new ScheduleTimeOverrideImpl();
			List<ScheduleTimeOverrideDTO> timeOverridelist = timeOverride.getByScheduleId(authDTO, tripDTO.getSchedule());

			// Copy to Trip
			tripDTO.setBus(busService.getBus(authDTO, scheduleBusDTO.getBus()));
			tripDTO.setAmenities(scheduleBusDTO.getAmentiesList());

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
				for (ScheduleStationDTO stationDTO : stationList) {
					if (reactionStationDTO != null && stationDTO.getStationSequence() > reactionStationDTO.getStationSequence()) {
						stationDTO.setMinitues(getStationTimeOverride(overrideDTO, stationDTO.getMinitues()));
					}
				}
			}

			tripDTO.setStationList(stationList);

			// Identify the last stage if stage fare exists
			Map<String, Integer> stageMap = new HashMap<>();
			for (ScheduleStageDTO stageDTO : stageList) {
				stageMap.put(stageDTO.getFromStation().getId() + "_" + stageDTO.getToStation().getId(), stageDTO.getId());
			}

			ScheduleStationDTO firstStation = null;
			ScheduleStationDTO lastStation = null;

			// Identify First station
			for (ScheduleStationDTO scheduleStationDTO : stationList) {
				if (scheduleStationDTO.getActiveFlag() == -1) {
					continue;
				}
				if (firstStation == null || scheduleStationDTO.getStationSequence() < firstStation.getStationSequence()) {
					firstStation = scheduleStationDTO;
				}
			}

			Map<Integer, StageStationDTO> stationMap = new HashMap<>();
			for (Iterator<ScheduleStationDTO> iterator = stationList.iterator(); iterator.hasNext();) {
				ScheduleStationDTO scheduleStationDTO = iterator.next();
				if (scheduleStationDTO.getActiveFlag() == -1) {
					continue;
				}
				StageStationDTO stageStationDTO = new StageStationDTO();
				stageStationDTO.setMinitues(scheduleStationDTO.getMinitues());
				stageStationDTO.setStationSequence(scheduleStationDTO.getStationSequence());
				stageStationDTO.setStation(scheduleStationDTO.getStation());
				stationMap.put(scheduleStationDTO.getStation().getId(), stageStationDTO);

				// Identify last station
				if (stageMap.get(firstStation.getStation().getId() + "_" + stageStationDTO.getStation().getId()) != null && (lastStation == null || scheduleStationDTO.getStationSequence() > lastStation.getStationSequence())) {
					lastStation = scheduleStationDTO;
				}
			}
			for (Iterator<ScheduleStationPointDTO> iterator = stationPointList.iterator(); iterator.hasNext();) {
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
					stationPointDTO.setActiveFlag(pointDTO.getActiveFlag());
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

					stageStationDTO.getStationPoint().add(stationPointDTO);
					stationMap.put(stageStationDTO.getStation().getId(), stageStationDTO);
				}
				else {
					iterator.remove();
					continue;
				}
			}
			Map<String, StageDTO> fareMap = new HashMap<>();
			for (Iterator<ScheduleStageDTO> iterator = stageList.iterator(); iterator.hasNext();) {
				ScheduleStageDTO scheduleStageDTO = iterator.next();
				StageDTO stageDTO = new StageDTO();
				if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
					stageDTO.setFromStation(stationMap.get(scheduleStageDTO.getFromStation().getId()));
					stageDTO.setToStation(stationMap.get(scheduleStageDTO.getToStation().getId()));

					// Allow added stage only
					if (lastStation.getStationSequence() < stationMap.get(scheduleStageDTO.getToStation().getId()).getStationSequence()) {
						continue;
					}
					stageDTO.setStageSequence(Integer.parseInt(stationMap.get(scheduleStageDTO.getFromStation().getId()).getStationSequence() + "" + (lastStation.getStationSequence() - stationMap.get(scheduleStageDTO.getToStation().getId()).getStationSequence())));
					stageDTO.getFromStation().setStation(stationService.getStation(scheduleStageDTO.getFromStation()));
					stageDTO.getToStation().setStation(stationService.getStation(scheduleStageDTO.getToStation()));
					stageDTO.getFromStation().setMinitues(stageDTO.getFromStation().getMinitues());
					stageDTO.getToStation().setMinitues(stageDTO.getToStation().getMinitues());
					StageFareDTO stageFareDTO = new StageFareDTO();
					stageFareDTO.setFare(new BigDecimal(scheduleStageDTO.getFare()));
					stageFareDTO.setBusSeatType(scheduleStageDTO.getBusSeatType());
					if (scheduleStageDTO.getGroup() != null) {
						stageFareDTO.setGroup(groupService.getGroup(authDTO, scheduleStageDTO.getGroup()));
					}
					if (fareMap.get(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId()) == null) {
						List<StageFareDTO> fareList = new ArrayList<>();
						stageDTO.setStageFare(fareList);
						fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
					}
					List<StageFareDTO> fareList = (fareMap.get(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId())).getStageFare();
					fareList.add(stageFareDTO);
					stageDTO.setStageFare(fareList);
					fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
				}
				else {
					iterator.remove();
					continue;
				}
			}
			List<StageDTO> stageDTOList = new ArrayList<>();
			Set<String> mapList = fareMap.keySet();
			for (String mapKey : mapList) {
				stageDTOList.add(fareMap.get(mapKey));
			}

			tripDTO.setStageList(stageDTOList);

		}
		catch (Exception e) {
			System.out.println("ER-TRIP01 - " + authDTO.getNamespaceCode() + Text.HYPHEN + tripDTO.getCode() + Text.HYPHEN + tripDTO.getTripDate() + Text.HYPHEN + tripDTO.getSchedule().getCode());
			e.printStackTrace();
		}
	}

	private String getGeneratedTripStageCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, TripDTO tripDTO, ScheduleStageDTO scheduleStageDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(tripDTO.getTripDate()) + "D" + scheduleStageDTO.getFromStation().getId() + "T" + scheduleStageDTO.getToStation().getId();
	}

	private String getGeneratedTripStageCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, TripDTO tripDTO, StationDTO fromStationDTO, StationDTO toStationDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(tripDTO.getTripDate()) + "D" + fromStationDTO.getId() + "T" + toStationDTO.getId();
	}

	private String getGeneratedTripCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, TripDTO tripDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(tripDTO.getTripDate()) + "D";
	}

	public String getGeneratedTripCodeV2(AuthDTO authDTO, ScheduleDTO scheduleDTO, TripDTO tripDTO) {
		return getGeneratedTripCode(authDTO, scheduleDTO, tripDTO);
	}

	protected String getGeneratedTripStageCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, SearchDTO searchDTO, StageDTO stageDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(searchDTO.getTravelDate()) + "D" + stageDTO.getFromStation().getStation().getId() + "T" + stageDTO.getToStation().getStation().getId();
	}

	protected String getGeneratedTripCode(AuthDTO authDTO, ScheduleDTO scheduleDTO, SearchDTO searchDTO, StageDTO stageDTO) {
		return authDTO.getNamespace().getId() + "N" + scheduleDTO.getId() + "S" + DateUtil.getCompressDate(searchDTO.getTravelDate()) + "D";
	}

	protected List<TripDTO> getUniqueTripList(List<TripDTO> nonUniqueAccountList) {
		Map<String, TripDTO> uniqueAccountsMapList = new HashMap<String, TripDTO>();
		if (nonUniqueAccountList != null && !nonUniqueAccountList.isEmpty()) {
			for (TripDTO nprDto : nonUniqueAccountList) {
				uniqueAccountsMapList.put(nprDto.getCode(), nprDto);
			}
		}
		return new ArrayList<TripDTO>(uniqueAccountsMapList.values());
	}

	private int getStationTimeOverride(ScheduleTimeOverrideDTO timeOverrideDTO, int stationMinitues) {
		int finalStationMinitues = stationMinitues;
		if (timeOverrideDTO.getOverrideType().getId() == OverrideTypeEM.DECREASE_VALUE.getId()) {
			finalStationMinitues = stationMinitues - timeOverrideDTO.getOverrideMinutes();
		}
		else if (timeOverrideDTO.getOverrideType().getId() == OverrideTypeEM.INCREASE_VALUE.getId()) {
			finalStationMinitues = stationMinitues + timeOverrideDTO.getOverrideMinutes();
		}
		return finalStationMinitues;
	}

	private BigDecimal applyFareAutoOverride(StageDTO stageDTO, double fare, List<ScheduleFareAutoOverrideDTO> fareOverrideDTOList, DateTime tripDate, BusSeatTypeEM busSeatTypeEM) {
		BigDecimal stageFare = new BigDecimal(fare);

		// Identify and remove the generic fare
		if (fareOverrideDTOList != null) {
			boolean groupSpecificFoundFlag = false;
			boolean seatTypeSpecificFoundFlag = false;
			boolean routeSpecificFoundFlag = false;
			for (ScheduleFareAutoOverrideDTO autoOverrideDTO : fareOverrideDTOList) {
				if (!groupSpecificFoundFlag) {
					groupSpecificFoundFlag = !autoOverrideDTO.getGroupList().isEmpty() ? true : false;
				}
				if (!seatTypeSpecificFoundFlag) {
					BusSeatTypeEM busSeatType = BitsUtil.existBusSeatType(autoOverrideDTO.getBusSeatType(), busSeatTypeEM);
					seatTypeSpecificFoundFlag = busSeatType != null ? true : false;
				}
				if (!routeSpecificFoundFlag) {
					routeSpecificFoundFlag = !autoOverrideDTO.getRouteList().isEmpty() ? true : false;
				}
			}
			for (Iterator<ScheduleFareAutoOverrideDTO> iterator = fareOverrideDTOList.iterator(); iterator.hasNext();) {
				ScheduleFareAutoOverrideDTO fareOverrideDTO = iterator.next();
				if (fareOverrideDTO.getGroupList().isEmpty() && groupSpecificFoundFlag) {
					iterator.remove();
					continue;
				}
				BusSeatTypeEM busSeatType = BitsUtil.existBusSeatType(fareOverrideDTO.getBusSeatType(), busSeatTypeEM);
				if (seatTypeSpecificFoundFlag && busSeatType == null) {
					iterator.remove();
					continue;
				}
				if (routeSpecificFoundFlag && fareOverrideDTO.getRouteList().isEmpty()) {
					iterator.remove();
					continue;
				}
			}
			// Sorting Trips
			Collections.sort(fareOverrideDTOList, new Comparator<ScheduleFareAutoOverrideDTO>() {
				@Override
				public int compare(ScheduleFareAutoOverrideDTO t1, ScheduleFareAutoOverrideDTO t2) {
					return new CompareToBuilder().append(t2.getActiveFrom(), t1.getActiveFrom()).append(t2.getActiveTo(), t1.getActiveTo()).toComparison();
				}
			});
			// Identify specific recent fare
			ScheduleFareAutoOverrideDTO recentScheduleFareAutoDTO = null;
			for (Iterator<ScheduleFareAutoOverrideDTO> iterator = fareOverrideDTOList.iterator(); iterator.hasNext();) {
				ScheduleFareAutoOverrideDTO fareOverrideDTO = iterator.next();
				if (recentScheduleFareAutoDTO == null) {
					recentScheduleFareAutoDTO = fareOverrideDTO;
					continue;
				}
				if (DateUtil.getDayDifferent(new DateTime(fareOverrideDTO.getActiveFrom()), new DateTime(fareOverrideDTO.getActiveTo())) > DateUtil.getDayDifferent(new DateTime(recentScheduleFareAutoDTO.getActiveFrom()), new DateTime(recentScheduleFareAutoDTO.getActiveTo()))) {
					iterator.remove();
					continue;
				}

			}
		}
		// Schedule Fare auto override
		if (fareOverrideDTOList != null && !fareOverrideDTOList.isEmpty()) {
			for (

			ScheduleFareAutoOverrideDTO fareAutoOverrideDTO : fareOverrideDTOList) {
				if (fareAutoOverrideDTO.getOverrideMinutes() != 0 && (DateUtil.getMinutiesDifferent(DateUtil.NOW(), DateUtil.addMinituesToDate(tripDate, stageDTO.getFromStation().getMinitues())) >= fareAutoOverrideDTO.getOverrideMinutes())) {
					continue;
				}
				if (fareAutoOverrideDTO.getRouteList().isEmpty() || BitsUtil.isRouteExists(fareAutoOverrideDTO.getRouteList(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation()) != null) {
					stageFare = fareAutoOverrideDTO.getFare();
				}
			}
		}
		return stageFare;
	}

	private BigDecimal calculateSeatFare(ScheduleSeatFareDTO seatFareDTO, BigDecimal seatFare) {
		if (seatFareDTO.getFareOverrideType().getId() == FareOverrideTypeEM.FINAL_FARE.getId()) {
			seatFare = seatFareDTO.getSeatFare();
		}
		else if (seatFareDTO.getFareOverrideType().getId() == FareOverrideTypeEM.DECREASE_FARE.getId()) {
			if (seatFareDTO.getFareType().getId() == FareTypeEM.FLAT.getId()) {
				seatFare = seatFare.subtract(seatFareDTO.getSeatFare());
			}
			else if (seatFareDTO.getFareType().getId() == FareTypeEM.PERCENTAGE.getId()) {
				seatFare = seatFare.subtract(seatFare.multiply(seatFareDTO.getSeatFare()).divide(Numeric.ONE_HUNDRED, 2));
			}
		}
		else if (seatFareDTO.getFareOverrideType().getId() == FareOverrideTypeEM.INCREASE_FARE.getId()) {
			if (seatFareDTO.getFareType().getId() == FareTypeEM.FLAT.getId()) {
				seatFare = seatFare.add(seatFareDTO.getSeatFare());
			}
			else if (seatFareDTO.getFareType().getId() == FareTypeEM.PERCENTAGE.getId()) {
				seatFare = seatFare.add(seatFare.multiply(seatFareDTO.getSeatFare()).divide(Numeric.ONE_HUNDRED, 2));
			}
		}
		return seatFare;
	}

	private List<ScheduleFareAutoOverrideDTO> getFareAutoOverrideList(AuthDTO authDTO, ScheduleDTO scheduleDTO, StageDTO stageDTO, BusSeatTypeEM busSeatType) {
		List<ScheduleFareAutoOverrideDTO> list = new ArrayList<ScheduleFareAutoOverrideDTO>();
		for (ScheduleFareAutoOverrideDTO fareAutoOverrideDTO : scheduleDTO.getFareAutoOverrideList()) {
			if (fareAutoOverrideDTO.getOverrideMinutes() != 0 && (DateUtil.getMinutiesDifferent(DateUtil.NOW(), DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), stageDTO.getFromStation().getMinitues())) >= fareAutoOverrideDTO.getOverrideMinutes())) {
				continue;
			}
			BusSeatTypeEM busSeatTypeEM = BitsUtil.existBusSeatType(fareAutoOverrideDTO.getBusSeatType(), busSeatType);
			if (busSeatTypeEM == null) {
				continue;
			}
			if (!fareAutoOverrideDTO.getRouteList().isEmpty() && BitsUtil.isRouteExists(fareAutoOverrideDTO.getRouteList(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation()) == null) {
				continue;
			}
			list.add(fareAutoOverrideDTO);
		}
		return list;
	}

	private Integer isSameGenderAllStage(List<SeatGendarEM> genderList) {
		Integer finalGender = null;
		for (SeatGendarEM gender : genderList) {
			finalGender = finalGender == null ? gender.getId() : gender.getId() == finalGender ? finalGender : SeatStatusEM.BLOCKED.getId();
		}
		return finalGender;
	}

	protected void applyMultiStageGendarValidations(AuthDTO authDTO, TripDTO tripDTO, List<TripSeatQuotaDTO> tripSeatQuatoList) {
		Map<String, List<TicketDetailsDTO>> statusMAP = new HashMap<String, List<TicketDetailsDTO>>();
		Map<String, List<SeatGendarEM>> seatGenderMAP = new HashMap<String, List<SeatGendarEM>>();
		// skip gender validation is 1x1
		boolean isLayout1X1 = tripDTO.getBus().checkLayoutCategory(BusCategoryTypeEM.LAYOUT_1X1);

		if (tripDTO != null && tripDTO.getTicketDetailsList() != null && !tripDTO.getTicketDetailsList().isEmpty()) {
			for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
					continue;
				}
				// Validate PBL Block Live Time
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && BitsUtil.validateBlockReleaseTime(ticketDetailsDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTime(), ticketDetailsDTO.getUpdatedAt())) {
					continue;
				}
				if (tripDTO.getReleatedStageCodeList().contains(ticketDetailsDTO.getTripStageCode())) {
					if (ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() && ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
						List<TicketDetailsDTO> list = null;
						if (statusMAP.get(ticketDetailsDTO.getSeatCode()) == null) {
							list = new ArrayList<TicketDetailsDTO>();
						}
						else {
							list = statusMAP.get(ticketDetailsDTO.getSeatCode());
						}
						list.add(ticketDetailsDTO);
						statusMAP.put(ticketDetailsDTO.getSeatCode(), list);
					}
				}
			}
		}
		if (tripSeatQuatoList != null && !tripSeatQuatoList.isEmpty()) {
			for (TripSeatQuotaDTO seatQuotaDTO : tripSeatQuatoList) {
				if ((seatQuotaDTO.getFromStation().getId() == 0 && seatQuotaDTO.getToStation().getId() == 0) || tripDTO.getReleatedStageCodeList().contains(getGeneratedTripStageCode(authDTO, tripDTO.getSchedule(), tripDTO, seatQuotaDTO.getFromStation(), seatQuotaDTO.getToStation()))) {
					List<TicketDetailsDTO> list = null;
					if (statusMAP.get(seatQuotaDTO.getSeatDetails().getSeatCode()) == null) {
						list = new ArrayList<TicketDetailsDTO>();
					}
					else {
						list = statusMAP.get(seatQuotaDTO.getSeatDetails().getSeatCode());
					}
					TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
					ticketDetailsDTO.setSeatCode(seatQuotaDTO.getSeatDetails().getSeatCode());
					ticketDetailsDTO.setTicketStatus(TicketStatusEM.TRIP_SEAT_QUOTA);
					ticketDetailsDTO.setSeatGendar(seatQuotaDTO.getSeatDetails().getSeatGendar());
					list.add(ticketDetailsDTO);
					statusMAP.put(seatQuotaDTO.getSeatDetails().getSeatCode(), list);
				}
			}
		}
		// Seat Gender preferences apply for Next seats
		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			if (seatLayoutDTO.getSeatGendar() != null && (seatLayoutDTO.getSeatGendar().getId() == SeatGendarEM.MALE.getId() || seatLayoutDTO.getSeatGendar().getId() == SeatGendarEM.FEMALE.getId())) {
				List<TicketDetailsDTO> list = null;
				if (statusMAP.get(seatLayoutDTO.getCode()) == null) {
					list = new ArrayList<TicketDetailsDTO>();
				}
				else {
					list = statusMAP.get(seatLayoutDTO.getCode());
				}
				TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
				ticketDetailsDTO.setSeatCode(seatLayoutDTO.getCode());
				ticketDetailsDTO.setTicketStatus(TicketStatusEM.TRIP_SEAT_QUOTA);
				ticketDetailsDTO.setSeatGendar(seatLayoutDTO.getSeatGendar());
				list.add(ticketDetailsDTO);
				statusMAP.put(seatLayoutDTO.getCode(), list);
			}
		}
		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			List<TicketDetailsDTO> list = statusMAP.get(seatLayoutDTO.getCode());
			if (list != null && !list.isEmpty()) {
				for (TicketDetailsDTO ticketDetailsDTO : list) {
					String seatPos = seatLayoutDTO.getLayer() + "_" + seatLayoutDTO.getColPos() + "_" + seatLayoutDTO.getRowPos();

					// all Stage wise seat Gender
					if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TRIP_SEAT_QUOTA.getId()) {
						if (seatGenderMAP.get(seatPos) == null) {
							List<SeatGendarEM> genderList = new ArrayList<SeatGendarEM>();
							genderList.add(ticketDetailsDTO.getSeatGendar());
							seatGenderMAP.put(seatPos, genderList);
						}
						else {
							List<SeatGendarEM> genderList = seatGenderMAP.get(seatPos);
							genderList.add(ticketDetailsDTO.getSeatGendar());
							seatGenderMAP.put(seatPos, genderList);
						}
					}
				}
			}
		}
		// Gender Validations
		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			Integer colCount = seatLayoutDTO.getColPos();
			Integer rowCount = seatLayoutDTO.getRowPos();
			Integer orientation = seatLayoutDTO.getOrientation();
			Integer layer = seatLayoutDTO.getLayer();
			if (!isLayout1X1 && orientation == 0 && seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_ALL.getId()) {
				if (seatGenderMAP.get(layer + "_" + colCount + "_" + (rowCount + 1)) != null) {
					Integer seatGender = isSameGenderAllStage(seatGenderMAP.get(layer + "_" + colCount + "_" + (rowCount + 1)));
					seatLayoutDTO.setSeatStatus(seatGender == SeatStatusEM.AVAILABLE_MALE.getId() ? SeatStatusEM.AVAILABLE_MALE : seatGender == SeatStatusEM.AVAILABLE_FEMALE.getId() ? SeatStatusEM.AVAILABLE_FEMALE : seatGender == SeatStatusEM.BLOCKED.getId() ? SeatStatusEM.BLOCKED : SeatStatusEM.AVAILABLE_ALL);
					continue;
				}
				else if (seatGenderMAP.get(layer + "_" + colCount + "_" + (rowCount - 1)) != null) {
					Integer seatGender = isSameGenderAllStage(seatGenderMAP.get(layer + "_" + colCount + "_" + (rowCount - 1)));
					seatLayoutDTO.setSeatStatus(seatGender == SeatStatusEM.AVAILABLE_MALE.getId() ? SeatStatusEM.AVAILABLE_MALE : seatGender == SeatStatusEM.AVAILABLE_FEMALE.getId() ? SeatStatusEM.AVAILABLE_FEMALE : seatGender == SeatStatusEM.BLOCKED.getId() ? SeatStatusEM.BLOCKED : SeatStatusEM.AVAILABLE_ALL);
				}
			}
			if (!isLayout1X1 && orientation == 1 && seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_ALL.getId()) {
				if (seatGenderMAP.get(layer + "_" + (colCount + 1) + "_" + rowCount) != null) {
					Integer seatGender = isSameGenderAllStage(seatGenderMAP.get(layer + "_" + (colCount + 1) + "_" + rowCount));
					seatLayoutDTO.setSeatStatus(seatGender == SeatStatusEM.AVAILABLE_MALE.getId() ? SeatStatusEM.AVAILABLE_MALE : seatGender == SeatStatusEM.AVAILABLE_FEMALE.getId() ? SeatStatusEM.AVAILABLE_FEMALE : seatGender == SeatStatusEM.BLOCKED.getId() ? SeatStatusEM.BLOCKED : SeatStatusEM.AVAILABLE_ALL);
					continue;
				}
				else if (seatGenderMAP.get(layer + "_" + (colCount - 1) + "_" + rowCount) != null) {
					Integer seatGender = isSameGenderAllStage(seatGenderMAP.get(layer + "_" + (colCount - 1) + "_" + rowCount));
					seatLayoutDTO.setSeatStatus(seatGender == SeatStatusEM.AVAILABLE_MALE.getId() ? SeatStatusEM.AVAILABLE_MALE : seatGender == SeatStatusEM.AVAILABLE_FEMALE.getId() ? SeatStatusEM.AVAILABLE_FEMALE : seatGender == SeatStatusEM.BLOCKED.getId() ? SeatStatusEM.BLOCKED : SeatStatusEM.AVAILABLE_ALL);
				}
			}
		}
	}

}
