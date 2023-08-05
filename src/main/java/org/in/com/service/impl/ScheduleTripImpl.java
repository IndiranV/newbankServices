package org.in.com.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ScheduleDAO;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.EventTriggerDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleCategoryDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.ScheduleSeatAutoReleaseDTO;
import org.in.com.dto.ScheduleSeatFareDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.ScheduleTimeOverrideDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.EventTriggerTypeEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.ReleaseModeEM;
import org.in.com.dto.enumeration.ReleaseTypeEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusVehicleDriverService;
import org.in.com.service.BusVehicleService;
import org.in.com.service.FareRuleService;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleBusOverrideService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleCategoryService;
import org.in.com.service.ScheduleControlService;
import org.in.com.service.ScheduleFareOverrideService;
import org.in.com.service.ScheduleSeatAutoReleaseService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleStationPointService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.ScheduleTimeOverrideService;
import org.in.com.service.ScheduleTripFareService;
import org.in.com.service.ScheduleTripService;
import org.in.com.service.StationPointService;
import org.in.com.service.StationService;
import org.in.com.service.TripService;
import org.in.com.service.helper.HelperUtil;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

@Service
public class ScheduleTripImpl extends HelperUtil implements ScheduleTripService {

	@Autowired
	ScheduleService scheduleService;
	@Autowired
	ScheduleTimeOverrideService timeOverrideService;
	@Autowired
	TripService tripService;
	@Autowired
	ScheduleSeatAutoReleaseService autoReleaseService;
	@Autowired
	ScheduleControlService controlService;
	@Autowired
	ScheduleBusService busService;
	@Autowired
	ScheduleStationPointService scheduleStationPointService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	StationService stationService;
	@Autowired
	StationPointService stationPointService;
	@Autowired
	ScheduleStageService stageService;
	@Autowired
	ScheduleFareOverrideService fareOverrideService;
	@Autowired
	ScheduleCategoryService categoryService;
	@Autowired
	ScheduleBusOverrideService busOverrideService;
	@Autowired
	GroupService groupService;
	@Autowired
	BusVehicleService vehicleService;
	@Lazy
	@Autowired
	ScheduleTripFareService scheduleTripFareService;
	@Autowired
	FareRuleService fareRuleService;
	@Autowired
	BusVehicleDriverService driverService;

	public List<TripDTO> getAllTripDetails(AuthDTO authDTO, SectorDTO sector, SearchDTO searchDTO) {
		List<TripDTO> tripList = new ArrayList<>();
		try {
			DateTime tripDate = searchDTO.getTravelDate();
			List<ScheduleDTO> scheduleList = getScheduleByTripDate(authDTO, tripDate);
			Map<Integer, ScheduleCategoryDTO> categoryMap = categoryService.getCategoryMap(authDTO);

			for (Iterator<ScheduleDTO> scheIterator = scheduleList.iterator(); scheIterator.hasNext();) {
				ScheduleDTO scheduleDTO = scheIterator.next();
				try {
					// Schedule Stage
					List<ScheduleStageDTO> stageList = stageService.getByScheduleTripDate(authDTO, scheduleDTO, tripDate);
					if (stageList.isEmpty()) {
						scheIterator.remove();
						continue;
					}
					scheduleDTO.setTripDate(tripDate);

					// Schedule Station
					List<ScheduleStationDTO> stationList = scheduleStationService.getByScheduleTripDate(authDTO, scheduleDTO, tripDate);
					if (stationList.isEmpty()) {
						scheIterator.remove();
						continue;
					}

					// Apply Sector schedule, Station filter
					if (sector.getActiveFlag() == Numeric.ONE_INT && BitsUtil.isScheduleExists(sector.getSchedule(), scheduleDTO) == null && BitsUtil.isStationExists(sector.getStation(), stationList) == null) {
						scheIterator.remove();
						continue;
					}

					// Schedule Station Point
					List<ScheduleStationPointDTO> stationPointList = scheduleStationPointService.getByScheduleTripDate(authDTO, scheduleDTO, tripDate);
					if (stationPointList.isEmpty()) {
						scheIterator.remove();
						continue;
					}

					// Booking Control
					List<ScheduleControlDTO> controlList = controlService.getAllGroupTripScheduleControl(authDTO, scheduleDTO, tripDate);
					if (controlList.isEmpty()) {
						scheIterator.remove();
						continue;
					}
					// Schedule Bus
					ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);
					if (scheduleBusDTO == null || scheduleBusDTO.getBus() == null) {
						scheIterator.remove();
						continue;
					}

					// Apply Bus Override
					BusDTO busOverrideDTO = busOverrideService.applyScheduleBusOverride(authDTO, scheduleDTO, scheduleBusDTO.getBus());
					if (busOverrideDTO != null) {
						scheduleBusDTO.setBus(busOverrideDTO);
					}
					scheduleDTO.setTax(scheduleBusDTO.getTax());
					// Schedule Fare Auto Override
					List<ScheduleFareAutoOverrideDTO> autoFareOverridelist = fareOverrideService.getByScheduleTripDate(authDTO, scheduleDTO, tripDate);

					List<ScheduleSeatAutoReleaseDTO> seatAutoReleaseList = autoReleaseService.getByScheduleId(authDTO, scheduleDTO);
					List<ScheduleTimeOverrideDTO> timeAutoOverrideList = timeOverrideService.getByScheduleId(authDTO, scheduleDTO);
					List<ScheduleControlDTO> allControlList = controlService.getAllGroupTripScheduleControl(authDTO, scheduleDTO, tripDate);

					scheduleDTO.setSeatAutoReleaseList(seatAutoReleaseList);
					scheduleDTO.setTimeOverrideList(timeAutoOverrideList);
					scheduleDTO.setControlList(allControlList);

					if (scheduleDTO.getCategory() != null && scheduleDTO.getCategory().getId() != 0 && categoryMap.get(scheduleDTO.getCategory().getId()) != null) {
						scheduleDTO.setCategory(categoryMap.get(scheduleDTO.getCategory().getId()));
					}

					// Copy to Trip
					TripDTO tripDTO = new TripDTO();
					scheduleDTO.setScheduleBus(scheduleBusDTO);
					tripDTO.setSchedule(scheduleDTO);
					tripDTO.setBus(scheduleBusDTO.getBus());
					tripDTO.setAmenities(scheduleBusDTO.getAmentiesList());
					tripDTO.setTripDate(tripDate);
					ScheduleStationDTO firstStation = null;
					ScheduleStationDTO lastStation = null;
					Map<Integer, StageStationDTO> stationMap = new HashMap<>();

					// Station time override
					for (ScheduleTimeOverrideDTO overrideDTO : timeAutoOverrideList) {
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
					// Identify the last stage if stage fare exists
					Map<String, Integer> stageMap = new HashMap<>();
					for (ScheduleStageDTO stageDTO : stageList) {
						stageMap.put(stageDTO.getFromStation().getId() + "_" + stageDTO.getToStation().getId(), stageDTO.getId());
					}
					// Identify First station
					for (ScheduleStationDTO scheduleStationDTO : stationList) {
						if (scheduleStationDTO.getActiveFlag() == -1) {
							continue;
						}
						if (firstStation == null || scheduleStationDTO.getStationSequence() < firstStation.getStationSequence()) {
							firstStation = scheduleStationDTO;
						}
					}
					for (ScheduleStationDTO scheduleStationDTO : stationList) {
						if (scheduleStationDTO.getActiveFlag() == -1) {
							continue;
						}
						StageStationDTO stageStationDTO = new StageStationDTO();
						stageStationDTO.setMinitues(scheduleStationDTO.getMinitues());
						stageStationDTO.setStationSequence(scheduleStationDTO.getStationSequence());
						stageStationDTO.setStation(scheduleStationDTO.getStation());
						stationMap.put(scheduleStationDTO.getStation().getId(), stageStationDTO);
						if (stageMap.get(firstStation.getStation().getId() + "_" + stageStationDTO.getStation().getId()) != null && (lastStation == null || scheduleStationDTO.getStationSequence() > lastStation.getStationSequence())) {
							lastStation = scheduleStationDTO;
						}
					}
					if (firstStation == null || lastStation == null) {
						scheIterator.remove();
						continue;
					}
					scheduleDTO.setStationList(stationList);
					tripDTO.setStationList(stationList);
					for (Iterator<ScheduleStationPointDTO> iterator = stationPointList.iterator(); iterator.hasNext();) {
						ScheduleStationPointDTO pointDTO = iterator.next();
						if (pointDTO.getActiveFlag() == 1 && stationMap.get(pointDTO.getStation().getId()) != null) {
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
							stationPointDTO.setBusVehicleVanPickup(pointDTO.getBusVehicleVanPickup());
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
					/*
					 * Hot Fix
					 * Should not check null, stage seat type with bus layout
					 * seat type, bz specific seat type add
					 * only for intermediate stages and missed end * route, so
					 * null check blocked to set default value of end route
					 */
					List<ScheduleStageDTO> scheduleStageDTOList = new ArrayList<ScheduleStageDTO>();
					Map<String, BusSeatTypeEM> bustype = scheduleBusDTO.getBus().getUniqueReservableBusType();
					Map<String, BusSeatTypeEM> stageFareBustype = scheduleDTO.getUniqueStageBusType(stageList);
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
					Map<String, StageDTO> fareMap = new HashMap<>();
					for (Iterator<ScheduleStageDTO> iterator = scheduleStageDTOList.iterator(); iterator.hasNext();) {
						ScheduleStageDTO scheduleStageDTO = iterator.next();
						StageDTO stageDTO = new StageDTO();
						if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationMap.get(scheduleStageDTO.getToStation().getId()) != null && !stationMap.get(scheduleStageDTO.getFromStation().getId()).getStationPoint().isEmpty()) {
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
							stageFareDTO.setFare(getStageFareWithOverride(stageDTO, scheduleStageDTO.getFare(), autoFareOverridelist, tripDate, scheduleStageDTO.getBusSeatType()));
							stageFareDTO.setBusSeatType(scheduleStageDTO.getBusSeatType());
							stageDTO.setStageStatus(TripStatusEM.TRIP_CLOSED);
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
							// set first and last station as main stage of trip
							if (stageDTO.getFromStation().getStation().getId() == firstStation.getStation().getId() && stageDTO.getToStation().getStation().getId() == lastStation.getStation().getId()) {
								tripDTO.setStage(stageDTO);
							}
						}
						else {
							iterator.remove();
							continue;
						}
					}
					scheduleDTO.setScheduleStageList(scheduleStageDTOList);
					if (tripDTO.getStage() == null && !fareMap.isEmpty()) {
						tripDTO.setStage(fareMap.get(fareMap.keySet().toArray()[0]));
					}
					else if (tripDTO.getStage() == null && fareMap.isEmpty()) {
						System.out.println("Trip Stage Fare Not Found " + scheduleDTO.getCode() + " - " + tripDTO.getCode());
					}
					// Booking Control
					if (controlList.size() > 1) {
						// remove default control, if Stage level found
						for (Iterator<ScheduleControlDTO> iterator = controlList.iterator(); iterator.hasNext();) {
							ScheduleControlDTO controlDTO = iterator.next();
							if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0 && controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0 && (controlDTO.getFromStation().getId() != firstStation.getStation().getId() || controlDTO.getToStation().getId() != lastStation.getStation().getId())) {
								iterator.remove();
								continue;
							}
						}
					}
					DateTime now = DateTime.now(TimeZone.getDefault());
					StageStationDTO stageFirstStation = stationMap.get(firstStation.getStation().getId());
					Collections.sort(stageFirstStation.getStationPoint(), new Comparator<StationPointDTO>() {
						@Override
						public int compare(StationPointDTO t1, StationPointDTO t2) {
							return new CompareToBuilder().append(t1.getMinitues(), t2.getMinitues()).toComparison();
						}
					});

					// Advance Booking Identification to Trip status
					for (ScheduleControlDTO controlDTO : controlList) {
						if (tripDTO.getStage() == null) {
							continue;
						}
						// Check for group level or should be default
						if (controlDTO.getAllowBookingFlag() != 1) {
							tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_CLOSED);
							continue;
						}
						int tripStageOriginStationOpenMinutes = tripDTO.getStage().getFromStation().getMinitues();
						int tripStageOriginStationCloseMinutes = tripDTO.getStage().getFromStation().getMinitues();
						if (controlDTO.getFromStation() == null && controlDTO.getToStation() == null) {
							tripStageOriginStationOpenMinutes = tripDTO.getTripOriginMinutes();
						}
						// open on mid night of Advance booking > 10days
						if (controlDTO.getOpenMinitues() > 14400) {
							tripStageOriginStationOpenMinutes = 0;
						}
						int minutiesOpenDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationOpenMinutes));
						int minutiesCloseDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(scheduleDTO.getTripDate(), tripStageOriginStationCloseMinutes));
						if (minutiesOpenDiff >= controlDTO.getOpenMinitues()) {
							tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_YET_OPEN);
							continue;
						}
						if (controlDTO.getCloseMinitues() != -1 && minutiesCloseDiff <= controlDTO.getCloseMinitues()) {
							tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_CLOSED);
							continue;
						}
						else if (controlDTO.getCloseMinitues() != -1 && minutiesCloseDiff >= controlDTO.getCloseMinitues()) {
							tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_OPEN);
							continue;
						}

						int minutiesDiffLastPoint = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDate, firstStation.getMinitues() + Iterables.getLast(stageFirstStation.getStationPoint()).getMinitues()));
						if (controlDTO.getCloseMinitues() == -1 && minutiesDiffLastPoint < 0) {
							tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_CLOSED);
							continue;
						}
						else if (controlDTO.getCloseMinitues() == -1 && minutiesDiffLastPoint >= 0) {
							tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_OPEN);
							continue;
						}
					}
					List<StageDTO> stageDTOList = new ArrayList<>();
					Set<String> mapList = fareMap.keySet();
					for (String mapKey : mapList) {
						stageDTOList.add(fareMap.get(mapKey));
					}
					if (stageDTOList.isEmpty()) {
						scheIterator.remove();
						continue;
					}
					for (StageDTO stageDTO : stageDTOList) {
						StageStationDTO stageStation = stationMap.get(stageDTO.getFromStation().getStation().getId());
						Collections.sort(stageStation.getStationPoint(), new Comparator<StationPointDTO>() {
							@Override
							public int compare(StationPointDTO t1, StationPointDTO t2) {
								return new CompareToBuilder().append(t1.getMinitues(), t2.getMinitues()).toComparison();
							}
						});
						if (stageDTO.getFromStation().getStation().getId() == firstStation.getStation().getId() && stageDTO.getToStation().getStation().getId() == lastStation.getStation().getId()) {
							stageDTO.setStageStatus(tripDTO.getStage().getStageStatus());
							continue;
						}

						// Advance Booking Identification to stage status
						for (ScheduleControlDTO controlDTO : controlList) {
							// Check for group level or should be default
							if (controlDTO.getAllowBookingFlag() != 1) {
								stageDTO.setStageStatus(TripStatusEM.TRIP_CLOSED);
								continue;
							}
							int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDate, stageDTO.getFromStation().getMinitues()));
							if (minutiesDiff >= controlDTO.getOpenMinitues()) {
								stageDTO.setStageStatus(TripStatusEM.TRIP_YET_OPEN);
								continue;
							}
							if (controlDTO.getCloseMinitues() != -1 && minutiesDiff <= controlDTO.getCloseMinitues()) {
								stageDTO.setStageStatus(TripStatusEM.TRIP_CLOSED);
								continue;
							}
							else if (controlDTO.getCloseMinitues() != -1 && minutiesDiff >= controlDTO.getCloseMinitues()) {
								tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_OPEN);
								continue;
							}
							int minutiesDiffLastPoint = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDate, stageDTO.getFromStation().getMinitues() + Iterables.getLast(stageStation.getStationPoint()).getMinitues()));
							if (controlDTO.getCloseMinitues() == -1 && minutiesDiffLastPoint < 0) {
								tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_CLOSED);
								continue;
							}
							else if (controlDTO.getCloseMinitues() == -1 && minutiesDiffLastPoint >= 0) {
								tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_OPEN);
								continue;
							}
						}
					}
					tripDTO.setStageList(stageDTOList);
					tripDTO.setTripMinutes(firstStation.getMinitues());
					// Schedule Phone book seat auto release
					List<EventTriggerDTO> eventList = new ArrayList<>();
					for (ScheduleSeatAutoReleaseDTO autoReleaseDTO : seatAutoReleaseList) {
						if (autoReleaseDTO.getReleaseTypeEM().getId() != ReleaseTypeEM.RELEASE_PHONE.getId() && autoReleaseDTO.getReleaseTypeEM().getId() != ReleaseTypeEM.CONFIRM_PHONE.getId()) {
							continue;
						}
						if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId()) {
							if (autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId()) {
								EventTriggerDTO triggerDTO = new EventTriggerDTO();
								triggerDTO.setEventTime(DateUtil.addMinituesToDate(tripDate, firstStation.getMinitues() + 1 - autoReleaseDTO.getReleaseMinutes()));
								triggerDTO.setName(ReleaseModeEM.RELEASE_SCHEDULE.getName());
								triggerDTO.setTriggerType(autoReleaseDTO.getReleaseTypeEM().getId() == ReleaseTypeEM.RELEASE_PHONE.getId() ? EventTriggerTypeEM.TICKET_PHONE_BOOK_AUTO_RELEASE : EventTriggerTypeEM.TICKET_PHONE_BOOK_AUTO_CONFIRM);
								triggerDTO.setCode(autoReleaseDTO.getCode());
								eventList.add(triggerDTO);
							}
							else if (autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
								for (StageDTO stageDTO : stageDTOList) {
									EventTriggerDTO triggerDTO = new EventTriggerDTO();
									triggerDTO.setEventTime(DateUtil.addMinituesToDate(tripDate, stageDTO.getFromStation().getMinitues() + 1 - autoReleaseDTO.getReleaseMinutes()));
									triggerDTO.setName(ReleaseModeEM.RELEASE_SCHEDULE.getName());
									triggerDTO.setTriggerType(autoReleaseDTO.getReleaseTypeEM().getId() == ReleaseTypeEM.RELEASE_PHONE.getId() ? EventTriggerTypeEM.TICKET_PHONE_BOOK_AUTO_RELEASE : EventTriggerTypeEM.TICKET_PHONE_BOOK_AUTO_CONFIRM);
									triggerDTO.setCode(autoReleaseDTO.getCode());
									eventList.add(triggerDTO);
								}
							}
						}
						else if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) {
							EventTriggerDTO triggerDTO = new EventTriggerDTO();
							triggerDTO.setEventTime(DateUtil.addMinituesToDate(tripDate, (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId()) ? autoReleaseDTO.getReleaseMinutes() : autoReleaseDTO.getReleaseMinutes() + 720));
							triggerDTO.setName(ReleaseModeEM.RELEASE_SCHEDULE.getName());
							triggerDTO.setTriggerType(EventTriggerTypeEM.TICKET_PHONE_BOOK_AUTO_RELEASE);
							triggerDTO.setCode(autoReleaseDTO.getCode());
							eventList.add(triggerDTO);
						}
					}
					tripDTO.setEventList(eventList);

					tripList.add(tripDTO);
				}
				catch (Exception e) {
					System.out.println("error in All Trip " + authDTO.getNamespaceCode() + " - " + scheduleDTO.getCode());
					e.printStackTrace();
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tripList;
	}

	// Particular schedule to trip list, used in apply fare Template
	public List<TripDTO> getScheduleTripList(AuthDTO authDTO, ScheduleDTO schedule, List<DateTime> tripDateList) {
		List<TripDTO> tripList = new ArrayList<>();
		try {
			for (DateTime tripDate : tripDateList) {
				try {
					ScheduleDTO scheduleTrip = new ScheduleDTO();
					scheduleTrip.setTripDate(tripDate);
					scheduleTrip.setCode(schedule.getCode());
					scheduleTrip.setId(schedule.getId());
					scheduleTrip.setActiveFrom(schedule.getActiveFrom());
					scheduleTrip.setActiveTo(schedule.getActiveTo());
					scheduleTrip.setDayOfWeek(schedule.getDayOfWeek());

					// Schedule Stage
					List<ScheduleStageDTO> stageList = stageService.getByScheduleTripDate(authDTO, scheduleTrip, tripDate);
					if (stageList.isEmpty()) {
						continue;
					}

					// Schedule Station
					List<ScheduleStationDTO> stationList = scheduleStationService.getByScheduleTripDate(authDTO, scheduleTrip, tripDate);
					if (stationList.isEmpty()) {
						continue;
					}

					// Schedule Station Point
					List<ScheduleStationPointDTO> stationPointList = scheduleStationPointService.getByScheduleTripDate(authDTO, scheduleTrip, tripDate);
					if (stationPointList.isEmpty()) {
						continue;
					}

					// Booking Control
					List<ScheduleControlDTO> controlList = controlService.getAllGroupTripScheduleControl(authDTO, scheduleTrip, tripDate);
					if (controlList.isEmpty()) {
						continue;
					}

					// Schedule Bus
					ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleTrip);
					if (scheduleBusDTO == null || scheduleBusDTO.getBus() == null) {
						continue;
					}

					// Apply Bus Override
					BusDTO busOverrideDTO = busOverrideService.applyScheduleBusOverride(authDTO, scheduleTrip, scheduleBusDTO.getBus());
					scheduleBusDTO.setBus(busOverrideDTO);

					scheduleTrip.setTax(scheduleBusDTO.getTax());
					scheduleTrip.setScheduleBus(scheduleBusDTO);

					// Copy to Trip
					TripDTO tripDTO = new TripDTO();
					tripDTO.setBus(scheduleBusDTO.getBus());
					tripDTO.setAmenities(scheduleBusDTO.getAmentiesList());
					tripDTO.setTripDate(tripDate);
					ScheduleStationDTO firstStation = null;
					ScheduleStationDTO lastStation = null;
					Map<Integer, StageStationDTO> stationMap = new HashMap<>();

					// Identify the last stage if stage fare exists
					Map<String, Integer> stageMap = new HashMap<>();
					for (ScheduleStageDTO stageDTO : stageList) {
						stageMap.put(stageDTO.getFromStation().getId() + "_" + stageDTO.getToStation().getId(), stageDTO.getId());
					}
					// Identify First station
					for (ScheduleStationDTO scheduleStationDTO : stationList) {
						if (scheduleStationDTO.getActiveFlag() == -1) {
							continue;
						}
						if (firstStation == null || scheduleStationDTO.getStationSequence() < firstStation.getStationSequence()) {
							firstStation = scheduleStationDTO;
						}
					}
					for (ScheduleStationDTO scheduleStationDTO : stationList) {
						if (scheduleStationDTO.getActiveFlag() == -1) {
							continue;
						}
						StageStationDTO stageStationDTO = new StageStationDTO();
						stageStationDTO.setMinitues(scheduleStationDTO.getMinitues());
						stageStationDTO.setStationSequence(scheduleStationDTO.getStationSequence());
						stageStationDTO.setStation(scheduleStationDTO.getStation());
						stationMap.put(scheduleStationDTO.getStation().getId(), stageStationDTO);
						if (stageMap.get(firstStation.getStation().getId() + "_" + stageStationDTO.getStation().getId()) != null && (lastStation == null || scheduleStationDTO.getStationSequence() > lastStation.getStationSequence())) {
							lastStation = scheduleStationDTO;
						}
					}
					if (firstStation == null || lastStation == null) {
						continue;
					}
					scheduleTrip.setStationList(stationList);
					// Identify Stage and fare with trip
					// bus-type(using-bus-override)
					List<ScheduleStageDTO> scheduleStageDTOList = new ArrayList<ScheduleStageDTO>();
					Map<String, BusSeatTypeEM> bustype = scheduleBusDTO.getBus().getUniqueReservableBusType();
					Map<String, BusSeatTypeEM> stageFareBustype = scheduleTrip.getUniqueStageBusType(stageList);
					for (BusSeatTypeEM seatTypeEM : new ArrayList<BusSeatTypeEM>(bustype.values())) {
						for (ScheduleStageDTO scheduleStageDTO : stageList) {
							if (seatTypeEM.getCode().equals(scheduleStageDTO.getBusSeatType().getCode())) {
								scheduleStageDTOList.add(scheduleStageDTO);
							}
							else if (stageFareBustype.get(seatTypeEM.getCode()) == null && stageFareBustype.get(seatTypeEM.getCode() + scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId()) == null) {
								ScheduleStageDTO scheduleStage = scheduleStageDTO.clone();
								scheduleStage.setBusSeatType(seatTypeEM);
								stageFareBustype.put(seatTypeEM.getCode() + scheduleStage.getFromStation().getId() + "_" + scheduleStage.getToStation().getId(), seatTypeEM);
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
							// Allow added stage only
							if (lastStation.getStationSequence() < stationMap.get(scheduleStageDTO.getToStation().getId()).getStationSequence()) {
								continue;
							}
							stageDTO.setStageSequence(Integer.parseInt(stationMap.get(scheduleStageDTO.getFromStation().getId()).getStationSequence() + "" + (lastStation.getStationSequence() - stationMap.get(scheduleStageDTO.getToStation().getId()).getStationSequence())));
							stageDTO.getFromStation().setStation(stationService.getStation(scheduleStageDTO.getFromStation()));
							stageDTO.getToStation().setStation(stationService.getStation(scheduleStageDTO.getToStation()));
							stageDTO.getFromStation().setMinitues(stageDTO.getFromStation().getMinitues());
							stageDTO.getToStation().setMinitues(stageDTO.getToStation().getMinitues());
							stageDTO.setStageStatus(TripStatusEM.TRIP_CLOSED);
							if (fareMap.get(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId()) == null) {
								List<StageFareDTO> fareList = new ArrayList<>();
								stageDTO.setStageFare(fareList);
								fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
							}
							List<StageFareDTO> fareList = (fareMap.get(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId())).getStageFare();
							stageDTO.setStageFare(fareList);

							fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
							// set first and last station as main stage of trip
							if (stageDTO.getFromStation().getStation().getId() == firstStation.getStation().getId() && stageDTO.getToStation().getStation().getId() == lastStation.getStation().getId()) {
								tripDTO.setStage(stageDTO);
							}
						}
						else {
							iterator.remove();
							continue;
						}
					}
					scheduleTrip.setScheduleStageList(scheduleStageDTOList);
					if (tripDTO.getStage() == null && !fareMap.isEmpty()) {
						tripDTO.setStage(fareMap.get(fareMap.keySet().toArray()[0]));
					}
					else if (tripDTO.getStage() == null && fareMap.isEmpty()) {
						System.out.println("Trip Stage Fare Not Found " + scheduleTrip.getCode() + " - " + tripDTO.getCode());
					}
					// Booking Control
					if (controlList.size() > 1) {
						// remove default control, if Stage level found
						for (Iterator<ScheduleControlDTO> iterator = controlList.iterator(); iterator.hasNext();) {
							ScheduleControlDTO controlDTO = iterator.next();
							if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0 && controlDTO.getToStation() != null && controlDTO.getToStation().getId() != 0 && (controlDTO.getFromStation().getId() != firstStation.getStation().getId() || controlDTO.getToStation().getId() != lastStation.getStation().getId())) {
								iterator.remove();
								continue;
							}
						}
					}
					DateTime now = DateTime.now(TimeZone.getDefault());
					StageStationDTO stageFirstStation = stationMap.get(firstStation.getStation().getId());
					Collections.sort(stageFirstStation.getStationPoint(), new Comparator<StationPointDTO>() {
						@Override
						public int compare(StationPointDTO t1, StationPointDTO t2) {
							return new CompareToBuilder().append(t1.getMinitues(), t2.getMinitues()).toComparison();
						}
					});

					// Advance Booking Identification to Trip status
					for (ScheduleControlDTO controlDTO : controlList) {
						// Check for group level or should be default
						if (controlDTO.getAllowBookingFlag() != 1) {
							tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_CLOSED);
							continue;
						}
						int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDate, firstStation.getMinitues()));
						if (minutiesDiff >= controlDTO.getOpenMinitues()) {
							tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_YET_OPEN);
							continue;
						}
						if (controlDTO.getCloseMinitues() != -1 && minutiesDiff <= controlDTO.getCloseMinitues()) {
							tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_CLOSED);
							continue;
						}
						else if (controlDTO.getCloseMinitues() != -1 && minutiesDiff >= controlDTO.getCloseMinitues()) {
							tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_OPEN);
							continue;
						}

						int minutiesDiffLastPoint = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDate, firstStation.getMinitues()));
						if (controlDTO.getCloseMinitues() == -1 && minutiesDiffLastPoint < 0) {
							tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_CLOSED);
							continue;
						}
						else if (controlDTO.getCloseMinitues() == -1 && minutiesDiffLastPoint >= 0) {
							tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_OPEN);
							continue;
						}
					}
					List<StageDTO> stageDTOList = new ArrayList<>();
					Set<String> mapList = fareMap.keySet();
					for (String mapKey : mapList) {
						stageDTOList.add(fareMap.get(mapKey));
					}
					if (stageDTOList.isEmpty()) {
						continue;
					}
					for (StageDTO stageDTO : stageDTOList) {
						StageStationDTO stageStation = stationMap.get(stageDTO.getFromStation().getStation().getId());
						Collections.sort(stageStation.getStationPoint(), new Comparator<StationPointDTO>() {
							@Override
							public int compare(StationPointDTO t1, StationPointDTO t2) {
								return new CompareToBuilder().append(t1.getMinitues(), t2.getMinitues()).toComparison();
							}
						});
						// Advance Booking Identification to stage status
						for (ScheduleControlDTO controlDTO : controlList) {
							// Check for group level or should be default
							if (controlDTO.getAllowBookingFlag() != 1) {
								stageDTO.setStageStatus(TripStatusEM.TRIP_CLOSED);
								continue;
							}
							int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDate, stageDTO.getFromStation().getMinitues()));
							if (minutiesDiff >= controlDTO.getOpenMinitues()) {
								stageDTO.setStageStatus(TripStatusEM.TRIP_YET_OPEN);
								continue;
							}
							if (controlDTO.getCloseMinitues() != -1 && minutiesDiff <= controlDTO.getCloseMinitues()) {
								stageDTO.setStageStatus(TripStatusEM.TRIP_CLOSED);
								continue;
							}
							else if (controlDTO.getCloseMinitues() != -1 && minutiesDiff >= controlDTO.getCloseMinitues()) {
								tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_OPEN);
								continue;
							}
							int minutiesDiffLastPoint = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDate, stageDTO.getFromStation().getMinitues()));
							if (controlDTO.getCloseMinitues() == -1 && minutiesDiffLastPoint < 0) {
								tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_CLOSED);
								continue;
							}
							else if (controlDTO.getCloseMinitues() == -1 && minutiesDiffLastPoint >= 0) {
								tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_OPEN);
								continue;
							}
						}
					}
					tripDTO.setStageList(stageDTOList);
					tripDTO.setTripMinutes(firstStation.getMinitues());
					tripDTO.setSchedule(scheduleTrip);

					tripList.add(tripDTO);
				}
				catch (Exception e) {
					System.out.println("error in All schedule Trip " + authDTO.getNamespaceCode() + " - " + schedule.getCode());
					e.printStackTrace();
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tripList;
	}

	// Travel date based schedule to trip list
	public List<TripDTO> getStageTripList(AuthDTO authDTO, SectorDTO sector, SearchDTO searchDTO) {
		List<TripDTO> tripScheduleList = new ArrayList<>();
		try {
			List<DateTime> tripDateList = new ArrayList<>();
			tripDateList.add(searchDTO.getTravelDate());
			tripDateList.add(searchDTO.getTravelDate().minusDays(1));
			tripDateList.add(searchDTO.getTravelDate().minusDays(2));
			for (DateTime tripDate : tripDateList) {
				int previousDays = DateUtil.getDayDifferent(searchDTO.getTravelDate(), tripDate);
				try {
					List<String> scheduleCodeList = getActiveScheduleByTripDate(authDTO, tripDate);

					for (String scheduleCode : scheduleCodeList) {
						ScheduleDTO scheduleTrip = new ScheduleDTO();
						scheduleTrip.setCode(scheduleCode);
						scheduleTrip = scheduleService.getSchedule(authDTO, scheduleTrip);

						Map<Integer, Integer> stageMap = new HashMap<>();
						// Schedule Stage
						List<ScheduleStageDTO> stageList = stageService.getByScheduleTripDate(authDTO, scheduleTrip, tripDate);
						if (stageList.isEmpty()) {
							continue;
						}
						for (ScheduleStageDTO stage : stageList) {
							stageMap.put(stage.getFromStation().getId(), stage.getId());
						}

						// Schedule Station
						List<ScheduleStationDTO> stationList = scheduleStationService.getByScheduleTripDate(authDTO, scheduleTrip, tripDate);

						// Identify Trip Date
						for (Iterator<ScheduleStationDTO> stationIterator = stationList.iterator(); stationIterator.hasNext();) {
							ScheduleStationDTO scheduleStationDTO = stationIterator.next();
							if (stageMap.get(scheduleStationDTO.getStation().getId()) == null) {
								stationIterator.remove();
								continue;
							}
							if (scheduleStationDTO.getMinitues() > 1440 && previousDays == 0) {
								stationIterator.remove();
								continue;
							}
							else if (scheduleStationDTO.getMinitues() < 1440 && previousDays == -1) {
								stationIterator.remove();
								continue;
							}
							else if (scheduleStationDTO.getMinitues() < 2880 && previousDays == -2) {
								stationIterator.remove();
								continue;
							}
						}
						if (stationList.isEmpty()) {
							continue;
						}

						scheduleTrip.setTripDate(tripDate);
						scheduleTrip.setStationList(stationList);
						TripDTO trip = new TripDTO();
						trip.setSchedule(scheduleTrip);
						trip.setTripDate(tripDate);
						tripScheduleList.add(trip);
					}
				}
				catch (Exception e) {
					System.out.println("error in All schedule Trip " + authDTO.getNamespaceCode());
					e.printStackTrace();
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return tripScheduleList;
	}

	private List<String> getActiveScheduleByTripDate(AuthDTO authDTO, DateTime tripDate) {
		String CACHEKEY = "ACTIVE_SCHE_" + authDTO.getNamespaceCode() + Text.UNDER_SCORE + tripDate.format(Text.DATE_DATE4J);
		List<String> scheduleCodes = null;
		Element element = EhcacheManager.getActiveScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			List<String> cachelist = (List<String>) element.getObjectValue();
			scheduleCodes = cachelist.stream().collect(Collectors.toList());
		}
		else {
			List<ScheduleDTO> cachelist = getScheduleByTripDate(authDTO, tripDate);
			scheduleCodes = cachelist.stream().map((ScheduleDTO schedule) -> schedule.getCode()).collect(Collectors.toList());
			element = new Element(CACHEKEY, scheduleCodes);
			EhcacheManager.getActiveScheduleEhCache().put(element);
		}
		return scheduleCodes;
	}

	private List<ScheduleDTO> getScheduleByTripDate(AuthDTO authDTO, DateTime tripDate) {
		List<ScheduleDTO> scheduleList = null;
		try {
			ScheduleDAO scheduleDAO = new ScheduleDAO();
			scheduleList = scheduleDAO.getAll(authDTO);
			for (Iterator<ScheduleDTO> itrSchedule = scheduleList.iterator(); itrSchedule.hasNext();) {
				ScheduleDTO scheduleDTO = itrSchedule.next();
				DateTime scheduleFromDate = new DateTime(scheduleDTO.getActiveFrom());
				DateTime scheduleEndDate = new DateTime(scheduleDTO.getActiveTo());
				if (!scheduleDTO.getPreRequrities().equals("000000")) {
					itrSchedule.remove();
					continue;
				}
				if (!tripDate.lteq(scheduleEndDate)) {
					itrSchedule.remove();
					continue;
				}
				if (tripDate.lt(scheduleFromDate)) {
					itrSchedule.remove();
					continue;
				}
				if (scheduleDTO.getDayOfWeek().length() != 7) {
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
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return scheduleList;
	}

	private BigDecimal getStageFareWithOverride(StageDTO stageDTO, double fare, List<ScheduleFareAutoOverrideDTO> fareOverrideDTOList, DateTime tripDate, BusSeatTypeEM busSeatTypeEM) {
		BigDecimal stageFare = new BigDecimal(fare);

		// Identify and remove the generic fare
		List<ScheduleFareAutoOverrideDTO> fareOverrideList = new ArrayList<>();
		if (fareOverrideDTOList != null) {
			for (ScheduleFareAutoOverrideDTO fareOverrideDTO : fareOverrideDTOList) {
				// Route List
				if (!fareOverrideDTO.getRouteList().isEmpty() && !existStageInRouteList(fareOverrideDTO.getRouteList(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation())) {
					continue;
				}
				BusSeatTypeEM busSeatType = BitsUtil.existBusSeatType(fareOverrideDTO.getBusSeatType(), busSeatTypeEM);
				if (busSeatType == null) {
					continue;
				}

				// Exceptions and Override
				for (ScheduleFareAutoOverrideDTO fareAutoOverrideExceptionDTO : fareOverrideDTO.getOverrideList()) {

					// Apply Exceptions
					if (fareAutoOverrideExceptionDTO.getFare().intValue() == -1) {
						if (fareAutoOverrideExceptionDTO.getRouteList().isEmpty() || existStageInRouteList(fareAutoOverrideExceptionDTO.getRouteList(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation())) {
							break;
						}
					}
					else {
						if (!fareAutoOverrideExceptionDTO.getRouteList().isEmpty() && !existStageInRouteList(fareAutoOverrideExceptionDTO.getRouteList(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation())) {
							continue;
						}
						// Apply Override
						fareOverrideDTO.setFare(fareAutoOverrideExceptionDTO.getFare());
						fareOverrideDTO.setOverrideMinutes(fareAutoOverrideExceptionDTO.getOverrideMinutes());
						fareOverrideDTO.setRouteList(fareAutoOverrideExceptionDTO.getRouteList());
						fareOverrideDTO.setGroupList(fareAutoOverrideExceptionDTO.getGroupList());
						fareOverrideDTO.setActiveFrom(fareAutoOverrideExceptionDTO.getActiveFrom());
						fareOverrideDTO.setActiveTo(fareAutoOverrideExceptionDTO.getActiveTo());
					}
				}
				fareOverrideList.add(fareOverrideDTO);
			}

			boolean groupSpecificFoundFlag = false;
			boolean seatTypeSpecificFoundFlag = false;
			boolean routeSpecificFoundFlag = false;
			for (ScheduleFareAutoOverrideDTO autoOverrideDTO : fareOverrideList) {
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

			for (Iterator<ScheduleFareAutoOverrideDTO> iterator = fareOverrideList.iterator(); iterator.hasNext();) {
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
			Collections.sort(fareOverrideList, new Comparator<ScheduleFareAutoOverrideDTO>() {
				@Override
				public int compare(ScheduleFareAutoOverrideDTO t1, ScheduleFareAutoOverrideDTO t2) {
					return new CompareToBuilder().append(t2.getActiveFrom(), t1.getActiveFrom()).append(t2.getActiveTo(), t1.getActiveTo()).toComparison();
				}
			});
			// Identify specific recent fare
			ScheduleFareAutoOverrideDTO recentScheduleFareAutoDTO = null;
			for (Iterator<ScheduleFareAutoOverrideDTO> iterator = fareOverrideList.iterator(); iterator.hasNext();) {
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
		if (fareOverrideList != null && !fareOverrideList.isEmpty()) {
			for (ScheduleFareAutoOverrideDTO fareAutoOverrideDTO : fareOverrideList) {
				if (fareAutoOverrideDTO.getOverrideMinutes() != 0 && (DateUtil.getMinutiesDifferent(DateUtil.NOW(), DateUtil.addMinituesToDate(tripDate, stageDTO.getFromStation().getMinitues())) >= fareAutoOverrideDTO.getOverrideMinutes())) {
					continue;
				}
				if (fareAutoOverrideDTO.getRouteList().isEmpty() || existStageInRouteList(fareAutoOverrideDTO.getRouteList(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation())) {
					stageFare = fareAutoOverrideDTO.getFare();
				}
			}
		}
		return stageFare;
	}

	@Override
	public TripDTO getTripDetails(AuthDTO authDTO, TripDTO tripDTO) {
		tripService.getTrip(authDTO, tripDTO);
		if (tripDTO.getSchedule() == null || tripDTO.getSchedule().getId() == 0) {

		}
		DateTime tripDate = tripDTO.getTripDate();
		ScheduleCache scheduleCache = new ScheduleCache();
		ScheduleDTO scheduleDTO = scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());

		// Schedule Stage
		List<ScheduleStageDTO> stageList = stageService.getByScheduleTripDate(authDTO, scheduleDTO, tripDate);
		scheduleDTO.setTripDate(tripDate);

		// Schedule Station
		List<ScheduleStationDTO> stationList = scheduleStationService.getByScheduleTripDate(authDTO, scheduleDTO, tripDate);

		// Schedule Station Point
		List<ScheduleStationPointDTO> stationPointList = scheduleStationPointService.getByScheduleTripDate(authDTO, scheduleDTO, tripDate);

		// Booking Control
		List<ScheduleControlDTO> controlList = controlService.getByScheduleTripDate(authDTO, scheduleDTO, tripDate);

		// Schedule Bus
		ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);

		// Schedule Fare Auto Override
		List<ScheduleFareAutoOverrideDTO> autoFareOverridelist = fareOverrideService.getByScheduleTripDate(authDTO, scheduleDTO, tripDate);

		List<ScheduleSeatAutoReleaseDTO> seatAutoReleaseList = autoReleaseService.getByScheduleTripDate(authDTO, scheduleDTO, tripDate);
		scheduleDTO.setSeatAutoReleaseList(seatAutoReleaseList);

		// Copy to Trip
		tripDTO.setSchedule(scheduleDTO);
		tripDTO.setBus(scheduleBusDTO.getBus());
		tripDTO.setAmenities(scheduleBusDTO.getAmentiesList());
		ScheduleStationDTO firstStation = null;
		ScheduleStationDTO lastStation = null;
		Map<Integer, StageStationDTO> stationMap = new HashMap<>();

		// Station time override
		ScheduleTimeOverrideImpl timeOverride = new ScheduleTimeOverrideImpl();
		List<ScheduleTimeOverrideDTO> timeOverridelist = timeOverride.getByScheduleId(authDTO, tripDTO.getSchedule());

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
		// Identify the last stage if stage fare exists
		Map<String, Integer> stageMap = new HashMap<>();
		for (ScheduleStageDTO stageDTO : stageList) {
			stageMap.put(stageDTO.getFromStation().getId() + "_" + stageDTO.getToStation().getId(), stageDTO.getId());
		}
		// Identify First station
		for (ScheduleStationDTO scheduleStationDTO : stationList) {
			if (scheduleStationDTO.getActiveFlag() == -1) {
				continue;
			}
			if (firstStation == null || scheduleStationDTO.getStationSequence() < firstStation.getStationSequence()) {
				firstStation = scheduleStationDTO;
			}
		}
		for (ScheduleStationDTO scheduleStationDTO : stationList) {
			if (scheduleStationDTO.getActiveFlag() == -1) {
				continue;
			}
			StageStationDTO stageStationDTO = new StageStationDTO();
			stageStationDTO.setMinitues(scheduleStationDTO.getMinitues());
			stageStationDTO.setStationSequence(scheduleStationDTO.getStationSequence());
			stageStationDTO.setStation(scheduleStationDTO.getStation());
			stationMap.put(scheduleStationDTO.getStation().getId(), stageStationDTO);
			if (stageMap.get(firstStation.getStation().getId() + "_" + stageStationDTO.getStation().getId()) != null && (lastStation == null || scheduleStationDTO.getStationSequence() > lastStation.getStationSequence())) {
				lastStation = scheduleStationDTO;
			}
		}
		scheduleDTO.setStationList(stationList);
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
				stageDTO.setStageSequence(Integer.parseInt(stationMap.get(scheduleStageDTO.getFromStation().getId()).getStationSequence() + "" + (lastStation.getStationSequence() - stationMap.get(scheduleStageDTO.getToStation().getId()).getStationSequence())));
				stageDTO.getFromStation().setStation(stationService.getStation(scheduleStageDTO.getFromStation()));
				stageDTO.getToStation().setStation(stationService.getStation(scheduleStageDTO.getToStation()));
				stageDTO.getFromStation().setMinitues(stageDTO.getFromStation().getMinitues());
				stageDTO.getToStation().setMinitues(stageDTO.getToStation().getMinitues());
				StageFareDTO stageFareDTO = new StageFareDTO();
				stageFareDTO.setFare(getStageFareWithOverride(stageDTO, scheduleStageDTO.getFare(), autoFareOverridelist, tripDate, scheduleStageDTO.getBusSeatType()));
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
			// Set First Stage
			if (stageDTO.getFromStation().getStation().getId() == firstStation.getStation().getId() && stageDTO.getToStation().getStation().getId() == lastStation.getStation().getId()) {
				tripDTO.setStage(stageDTO);
			}
		}
		tripDTO.getStage().setTravelDate(DateUtil.addMinituesToDate(tripDate, firstStation.getMinitues()));

		List<StageStationDTO> stageStationList = new ArrayList<StageStationDTO>(stationMap.values());
		DateTime originDateTime = BitsUtil.getOriginStationPointDateTime(stageStationList, tripDTO.getTripDate());
		tripDTO.setTripStartTime(originDateTime);
		DateTime destinationStationDateTime = BitsUtil.getDestinationStationTime(stageStationList, tripDTO.getTripDate());
		tripDTO.setTripCloseTime(destinationStationDateTime);

		DateTime now = DateTime.now(TimeZone.getDefault());
		// Advance Booking Validations
		for (Iterator<ScheduleControlDTO> itrControlDTO = controlList.iterator(); itrControlDTO.hasNext();) {
			ScheduleControlDTO controlDTO = itrControlDTO.next();
			// Check for group level or should be default
			if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0) {
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getAllowBookingFlag() != 1) {
				itrControlDTO.remove();
				tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_CLOSED);
				continue;
			}
			int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDate, firstStation.getMinitues()));
			if (minutiesDiff >= controlDTO.getOpenMinitues()) {
				tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_YET_OPEN);
				itrControlDTO.remove();
				continue;
			}
			if (controlDTO.getCloseMinitues() != -1 && minutiesDiff <= controlDTO.getCloseMinitues()) {
				itrControlDTO.remove();
				tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_CLOSED);
				continue;
			}

		}
		if (tripDTO.getStage().getStageStatus() == null) {
			tripDTO.getStage().setStageStatus(TripStatusEM.TRIP_OPEN);
		}
		List<StageDTO> stageDTOList = new ArrayList<>();
		Set<String> mapList = fareMap.keySet();
		for (String mapKey : mapList) {
			stageDTOList.add(fareMap.get(mapKey));
		}
		tripDTO.setStageList(stageDTOList);

		// Schedule Phone book seat auto release
		List<EventTriggerDTO> eventList = new ArrayList<>();
		for (ScheduleSeatAutoReleaseDTO autoReleaseDTO : seatAutoReleaseList) {
			if (autoReleaseDTO.getReleaseTypeEM().getId() == ReleaseTypeEM.RELEASE_PHONE.getId()) {
				if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId()) {
					if (autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId()) {
						EventTriggerDTO triggerDTO = new EventTriggerDTO();
						triggerDTO.setEventTime(DateUtil.addMinituesToDate(tripDate, firstStation.getMinitues() + 1 - autoReleaseDTO.getReleaseMinutes()));
						triggerDTO.setName(ReleaseModeEM.RELEASE_SCHEDULE.getName());
						triggerDTO.setTriggerType(EventTriggerTypeEM.TICKET_PHONE_BOOK_AUTO_RELEASE);
						triggerDTO.setCode(tripDTO.getCode());
						eventList.add(triggerDTO);
					}
					else if (autoReleaseDTO.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_STAGE.getId()) {
						for (StageDTO stageDTO : stageDTOList) {
							EventTriggerDTO triggerDTO = new EventTriggerDTO();
							triggerDTO.setEventTime(DateUtil.addMinituesToDate(tripDate, stageDTO.getFromStation().getMinitues() + 1 - autoReleaseDTO.getReleaseMinutes()));
							triggerDTO.setName(ReleaseModeEM.RELEASE_SCHEDULE.getName());
							triggerDTO.setTriggerType(EventTriggerTypeEM.TICKET_PHONE_BOOK_AUTO_RELEASE);
							triggerDTO.setCode(tripDTO.getCode());
							eventList.add(triggerDTO);
						}
					}
				}
				else if (autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId() || autoReleaseDTO.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) {
					EventTriggerDTO triggerDTO = new EventTriggerDTO();
					triggerDTO.setEventTime(DateUtil.addMinituesToDate(tripDate, autoReleaseDTO.getReleaseMinutes()));
					triggerDTO.setName(ReleaseModeEM.RELEASE_SCHEDULE.getName());
					triggerDTO.setTriggerType(EventTriggerTypeEM.TICKET_PHONE_BOOK_AUTO_RELEASE);
					triggerDTO.setCode(tripDTO.getCode());
					eventList.add(triggerDTO);
				}
			}
		}
		tripDTO.setEventList(eventList);

		return tripDTO;
	}

	public TripDTO getTripStageDetails(AuthDTO authDTO, TripDTO tripDTO) {
		tripService.getTrip(authDTO, tripDTO);
		DateTime tripDate = tripDTO.getTripDate();
		ScheduleCache scheduleCache = new ScheduleCache();
		ScheduleDTO scheduleDTO = scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());

		// Schedule Stage
		List<ScheduleStageDTO> stageList = stageService.getByScheduleTripDate(authDTO, scheduleDTO, tripDate);
		scheduleDTO.setTripDate(tripDate);

		// Schedule Station
		List<ScheduleStationDTO> stationList = scheduleStationService.getByScheduleTripDate(authDTO, scheduleDTO, tripDate);

		// Schedule Bus
		ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, scheduleDTO);

		// Apply Bus Override
		BusDTO busOverrideDTO = busOverrideService.applyScheduleBusOverride(authDTO, scheduleDTO, scheduleBusDTO.getBus());
		if (busOverrideDTO != null) {
			scheduleBusDTO.setBus(busOverrideDTO);
		}

		// Copy to Trip
		tripDTO.setSchedule(scheduleDTO);
		tripDTO.setBus(scheduleBusDTO.getBus());
		ScheduleStationDTO firstStation = null;
		ScheduleStationDTO lastStation = null;
		Map<Integer, StageStationDTO> stationMap = new HashMap<>();

		// Identify the last stage if stage fare exists
		Map<String, Integer> stageMap = new HashMap<>();
		for (ScheduleStageDTO stageDTO : stageList) {
			stageMap.put(stageDTO.getFromStation().getId() + "_" + stageDTO.getToStation().getId(), stageDTO.getId());
		}
		// Identify First station
		for (ScheduleStationDTO scheduleStationDTO : stationList) {
			if (scheduleStationDTO.getActiveFlag() == -1) {
				continue;
			}
			if (firstStation == null || scheduleStationDTO.getStationSequence() < firstStation.getStationSequence()) {
				firstStation = scheduleStationDTO;
			}
		}
		for (ScheduleStationDTO scheduleStationDTO : stationList) {
			if (scheduleStationDTO.getActiveFlag() == -1) {
				continue;
			}
			StageStationDTO stageStationDTO = new StageStationDTO();
			stageStationDTO.setMinitues(scheduleStationDTO.getMinitues());
			stageStationDTO.setStationSequence(scheduleStationDTO.getStationSequence());
			stageStationDTO.setStation(scheduleStationDTO.getStation());
			stationMap.put(scheduleStationDTO.getStation().getId(), stageStationDTO);
			if (stageMap.get(firstStation.getStation().getId() + "_" + stageStationDTO.getStation().getId()) != null && (lastStation == null || scheduleStationDTO.getStationSequence() > lastStation.getStationSequence())) {
				lastStation = scheduleStationDTO;
			}
		}
		scheduleDTO.setStationList(stationList);
		List<StageDTO> finalStageList = new ArrayList<>();
		for (ScheduleStageDTO scheduleStageDTO : stageList) {
			StageDTO stageDTO = new StageDTO();
			if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
				stageDTO.setFromStation(stationMap.get(scheduleStageDTO.getFromStation().getId()));
				stageDTO.setToStation(stationMap.get(scheduleStageDTO.getToStation().getId()));
				stageDTO.setStageSequence(Integer.parseInt(stationMap.get(scheduleStageDTO.getFromStation().getId()).getStationSequence() + Text.EMPTY + Math.abs((lastStation.getStationSequence() - stationMap.get(scheduleStageDTO.getToStation().getId()).getStationSequence()))));
				stageDTO.getFromStation().setStation(stationService.getStation(scheduleStageDTO.getFromStation()));
				stageDTO.getToStation().setStation(stationService.getStation(scheduleStageDTO.getToStation()));
				stageDTO.getFromStation().setMinitues(stageDTO.getFromStation().getMinitues());
				stageDTO.getToStation().setMinitues(stageDTO.getToStation().getMinitues());
				finalStageList.add(stageDTO);
			}
			else {
				continue;
			}
			// Set First Stage
			if (stageDTO.getFromStation().getStation().getId() == firstStation.getStation().getId() && stageDTO.getToStation().getStation().getId() == lastStation.getStation().getId()) {
				tripDTO.setStage(stageDTO);
			}
		}
		tripDTO.getStage().setTravelDate(DateUtil.addMinituesToDate(tripDate, firstStation.getMinitues()));

		tripDTO.setStageList(finalStageList);

		return tripDTO;
	}

	public List<TripDTO> getTripByTripDateAndVehicle(AuthDTO authDTO, DateTime tripDate, BusVehicleDTO vehicle) {
		TripDAO tripDAO = new TripDAO();
		List<TripDTO> list = tripDAO.getTripByTripDateAndVehicle(authDTO, tripDate, vehicle);
		for (TripDTO tripDTO : list) {
			tripDTO = getTripDetails(authDTO, tripDTO);

			if (tripDTO.getId() == 0) {
				continue;
			}
			if (tripDTO.getTripInfo().getBusVehicle() != null && tripDTO.getTripInfo().getBusVehicle().getId() != 0) {
				tripDTO.getTripInfo().setBusVehicle(vehicleService.getBusVehicles(authDTO, tripDTO.getTripInfo().getBusVehicle()));
			}
			if (tripDTO.getTripInfo().getPrimaryDriver() != null && tripDTO.getTripInfo().getPrimaryDriver().getId() != 0) {
				tripDTO.getTripInfo().setPrimaryDriver(getVehicleDriverDTOById(authDTO, tripDTO.getTripInfo().getPrimaryDriver()));
			}
			if (tripDTO.getTripInfo().getSecondaryDriver() != null && tripDTO.getTripInfo().getSecondaryDriver().getId() != 0) {
				tripDTO.getTripInfo().setSecondaryDriver(getVehicleDriverDTOById(authDTO, tripDTO.getTripInfo().getSecondaryDriver()));
			}
			if (tripDTO.getTripInfo().getAttendant() != null && tripDTO.getTripInfo().getAttendant().getId() != 0) {
				tripDTO.getTripInfo().setAttendant(getVehicleAttendantDTOById(authDTO, tripDTO.getTripInfo().getAttendant()));
			}
		}
		return list;
	}

	@Override
	public List<TripDTO> getTripByTripDateAndDriver(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriver, DateTime tripDate) {
		driverService.get(authDTO, busVehicleDriver);
		if (busVehicleDriver == null || busVehicleDriver.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_CODE);
		}

		List<DateTime> tripDates = new ArrayList<>();
		tripDates.add(DateUtil.minusDaysToDate(tripDate, 1));
		tripDates.add(tripDate);

		DateTime now = DateUtil.NOW();
		TripDAO tripDAO = new TripDAO();
		List<TripDTO> trips = new ArrayList<>();
		for (DateTime tripDateTime : tripDates) {
			List<TripDTO> tripList = tripDAO.getTripByTripDateAndDriver(authDTO, busVehicleDriver, tripDateTime);
			for (TripDTO tripDTO : tripList) {
				tripDTO = getTripDetails(authDTO, tripDTO);
				// Allow till 3hours
				if (tripDTO.getId() == 0 || DateUtil.plusMinituesToDate(tripDTO.getTripInfo().getTripCloseDateTime(), Constants.BUSBUDDY_DRIVER_TRIP_CLOSE_MINUTES).lt(now)) {
					tripDTO.setActiveFlag(Numeric.ZERO_INT);
					continue;
				}
				// Open before 3hours
				if (DateUtil.minusMinituesToDate(tripDTO.getTripInfo().getTripStartDateTime(), Constants.BUSBUDDY_DRIVER_TRIP_OPEN_MINUTES).gt(now)) {
					tripDTO.setActiveFlag(Numeric.ZERO_INT);
					continue;
				}
				if (tripDTO.getTripInfo().getBusVehicle() != null && tripDTO.getTripInfo().getBusVehicle().getId() != 0) {
					tripDTO.getTripInfo().setBusVehicle(vehicleService.getBusVehicles(authDTO, tripDTO.getTripInfo().getBusVehicle()));
				}
				if (tripDTO.getTripInfo().getPrimaryDriver() != null && tripDTO.getTripInfo().getPrimaryDriver().getId() != 0) {
					tripDTO.getTripInfo().setPrimaryDriver(getVehicleDriverDTOById(authDTO, tripDTO.getTripInfo().getPrimaryDriver()));
				}
				if (tripDTO.getTripInfo().getSecondaryDriver() != null && tripDTO.getTripInfo().getSecondaryDriver().getId() != 0) {
					tripDTO.getTripInfo().setSecondaryDriver(getVehicleDriverDTOById(authDTO, tripDTO.getTripInfo().getSecondaryDriver()));
				}
				if (tripDTO.getTripInfo().getAttendant() != null && tripDTO.getTripInfo().getAttendant().getId() != 0) {
					tripDTO.getTripInfo().setAttendant(getVehicleAttendantDTOById(authDTO, tripDTO.getTripInfo().getAttendant()));
				}
			}
			trips.addAll(tripList);
		}
		return trips;
	}

	public void validateSeatFareWithFareRule(AuthDTO authDTO, ScheduleSeatFareDTO scheduleSeatFareDTO) {
		DateTime fromDateTime = DateUtil.getDateTime(scheduleSeatFareDTO.getActiveFrom()).getStartOfDay();
		DateTime toDateTime = DateUtil.getDateTime(scheduleSeatFareDTO.getActiveTo()).getEndOfDay();
		List<String> tripDates = DateUtil.getDateListToString(fromDateTime, toDateTime, scheduleSeatFareDTO.getDayOfWeek());

		List<TripDTO> trips = scheduleTripFareService.getScheduleTripFareV2(authDTO, scheduleSeatFareDTO.getSchedule(), fromDateTime, toDateTime, tripDates, false);

		for (TripDTO tripDTO : trips) {
			Map<String, BusSeatLayoutDTO> seatMap = new HashMap<String, BusSeatLayoutDTO>();
			for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
				seatMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
			}
			for (StageDTO stageDTO : tripDTO.getStageList()) {
				if (!scheduleSeatFareDTO.getRoutes().isEmpty() && !existStageInRouteList(scheduleSeatFareDTO.getRoutes(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation())) {
					continue;
				}

				for (StageFareDTO stageFareDTO : stageDTO.getStageFare()) {
					for (BusSeatLayoutDTO busSeatLayoutDTO : scheduleSeatFareDTO.getBus().getBusSeatLayoutDTO().getList()) {
						BusSeatLayoutDTO layoutDTO = seatMap.get(busSeatLayoutDTO.getCode());
						if (layoutDTO != null && layoutDTO.getBusSeatType().getId() == stageFareDTO.getBusSeatType().getId()) {
							BigDecimal seatFare = calculateSeatFare(scheduleSeatFareDTO, stageFareDTO.getFare());

							FareRuleDetailsDTO fareRuleDetails = fareRuleService.getFareRuleDetails(authDTO, authDTO.getNamespace().getProfile().getFareRule(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation());
							if (fareRuleDetails.getId() == Numeric.ZERO_INT) {
								continue;
							}
							stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, tripDTO.getBus(), fareRuleDetails);
							if (fareRuleDetails.getId() != 0 && (stageFareDTO.getMinFare().intValue() != 0 && seatFare.intValue() < stageFareDTO.getMinFare().intValue()) || (stageFareDTO.getMaxFare().intValue() != 0 && seatFare.intValue() > stageFareDTO.getMaxFare().intValue())) {
								throw new ServiceException(ErrorCode.ROUTE_FARE_OUT_OF_RANGE, stageDTO.getFromStation().getStation().getName() + " - " + stageDTO.getToStation().getStation().getName() + " Fare excepted: (" + stageFareDTO.getMinFare() + " - " + stageFareDTO.getMaxFare() + ") but requested is: " + seatFare);
							}
						}
					}
				}
			}
		}
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
}
