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

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.aggregator.mercservices.MercService;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.ScheduleCache;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ScheduleFareAutoOverrideDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.BusSeatTypeFareDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleFareAutoOverrideDTO;
import org.in.com.dto.ScheduleFareTemplateDTO;
import org.in.com.dto.ScheduleSeatFareDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleTripStageFareDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.FareOverrideTypeEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.FareRuleService;
import org.in.com.service.GroupService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.ScheduleBusOverrideService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.ScheduleFareOverrideService;
import org.in.com.service.ScheduleFareTemplateService;
import org.in.com.service.ScheduleSeatFareService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.ScheduleTripFareService;
import org.in.com.service.ScheduleTripStageFareService;
import org.in.com.service.SearchService;
import org.in.com.service.StationService;
import org.in.com.service.TripService;
import org.in.com.service.helper.HelperUtil;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class ScheduleTripFareImpl extends CacheCentral implements ScheduleTripFareService {
	@Autowired
	TripService tripService;
	@Autowired
	ScheduleStageService stageService;
	@Autowired
	ScheduleFareOverrideService fareOverrideService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	ScheduleBusOverrideService busOverrideService;
	@Autowired
	ScheduleBusService busService;
	@Autowired
	StationService stationRouteService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	GroupService groupService;
	@Autowired
	StationService stationService;
	@Autowired
	FareRuleService fareRuleService;
	@Autowired
	ScheduleFareTemplateService fareTemplateService;
	@Autowired
	SearchService searchService;
	@Autowired
	ScheduleTripStageFareService tripStageFareService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	ScheduleSeatFareService scheduleSeatFareService;
	@Autowired
	ScheduleDynamicStageFareService dynamicStageFareService;
	@Autowired
	MercService mercService;
	private static final Logger logger = LoggerFactory.getLogger("org.in.com.service.impl.ScheduleTripFareImpl");

	public List<StageDTO> getScheduleTripFare(AuthDTO authDTO, TripDTO tripDTO) {
		List<StageDTO> stageDTOList = new ArrayList<>();
		try {
			tripService.getTripDTO(authDTO, tripDTO);
			ScheduleCache scheduleCache = new ScheduleCache();
			scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());

			tripDTO.getSchedule().setTripDate(tripDTO.getTripDate());
			DateTime tripDate = tripDTO.getTripDate();
			// Schedule Stage
			List<ScheduleStageDTO> stageList = stageService.getByScheduleTripDate(authDTO, tripDTO.getSchedule(), tripDate);
			// Schedule Station
			List<ScheduleStationDTO> stationList = scheduleStationService.getByScheduleTripDate(authDTO, tripDTO.getSchedule(), tripDate);
			Map<Integer, StageStationDTO> stationMap = new HashMap<>();
			ScheduleStationDTO firstStation = null;
			ScheduleStationDTO lastStation = null;
			for (ScheduleStationDTO scheduleStationDTO : stationList) {
				if (scheduleStationDTO.getActiveFlag() == -1) {
					continue;
				}
				StageStationDTO stageStationDTO = new StageStationDTO();
				stageStationDTO.setStationSequence(scheduleStationDTO.getStationSequence());
				stageStationDTO.setStation(scheduleStationDTO.getStation());
				stageStationDTO.setMinitues(scheduleStationDTO.getMinitues());
				stationMap.put(scheduleStationDTO.getStation().getId(), stageStationDTO);
				//
				if (firstStation == null || scheduleStationDTO.getStationSequence() < firstStation.getStationSequence()) {
					firstStation = scheduleStationDTO;
				}
				if (lastStation == null || scheduleStationDTO.getStationSequence() > lastStation.getStationSequence()) {
					lastStation = scheduleStationDTO;
				}
			}
			// Bus Type and BusMap
			ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, tripDTO.getSchedule());
			BusDTO busOverrideDTO = busOverrideService.applyScheduleBusOverride(authDTO, tripDTO.getSchedule(), scheduleBusDTO.getBus());
			// if (busOverrideDTO.getId() != tripDTO.getBus().getId()) {
			// throw new ServiceException(ErrorCode.BUSMAP_MISSED_MATCHED);
			// }
			// Identify Stage and fare with trip bus-type(using-bus-override)

			Map<String, List<BusSeatTypeEM>> stageBustypes = new HashMap<>();
			/** Get route wise unique bus seat type */
			for (ScheduleStageDTO scheduleStageDTO : stageList) {
				if (stageBustypes.get(scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId()) == null) {
					List<BusSeatTypeEM> busSeatTypes = new ArrayList<>();
					busSeatTypes.add(scheduleStageDTO.getBusSeatType());
					stageBustypes.put(scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId(), busSeatTypes);
				}
				else if (stageBustypes.get(scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId()) != null) {
					List<BusSeatTypeEM> busSeatTypes = stageBustypes.get(scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId());
					busSeatTypes.add(scheduleStageDTO.getBusSeatType());
					stageBustypes.put(scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId(), busSeatTypes);
				}
			}

			List<ScheduleStageDTO> scheduleStageDTOList = new ArrayList<ScheduleStageDTO>();
			Map<String, BusSeatTypeEM> bustype = busOverrideDTO.getUniqueReservableBusType();
			Map<String, BusSeatTypeEM> stageFareBustype = tripDTO.getSchedule().getUniqueStageBusType(stageList);

			for (BusSeatTypeEM seatTypeEM : new ArrayList<BusSeatTypeEM>(bustype.values())) {
				for (ScheduleStageDTO scheduleStageDTO : stageList) {
					if (seatTypeEM.getCode().equals(scheduleStageDTO.getBusSeatType().getCode())) {
						scheduleStageDTOList.add(scheduleStageDTO);
					}
					else if (BitsUtil.existBusSeatType(stageBustypes.get(scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId()), seatTypeEM) == null) {
						ScheduleStageDTO scheduleStage = scheduleStageDTO.clone();
						scheduleStage.setBusSeatType(seatTypeEM);
						stageFareBustype.put(seatTypeEM.getCode() + scheduleStage.getFromStation().getId() + "_" + scheduleStage.getToStation().getId(), seatTypeEM);
						scheduleStageDTOList.add(scheduleStage);

						List<BusSeatTypeEM> busSeatTypes = stageBustypes.get(scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId());
						busSeatTypes.add(seatTypeEM);
						stageBustypes.put(scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId(), busSeatTypes);
					}
					else if (stageFareBustype.get(seatTypeEM.getCode()) == null && stageFareBustype.get(seatTypeEM.getCode() + scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId()) == null) {
						ScheduleStageDTO scheduleStage = scheduleStageDTO.clone();
						scheduleStage.setBusSeatType(seatTypeEM);
						stageFareBustype.put(seatTypeEM.getCode() + scheduleStage.getFromStation().getId() + "_" + scheduleStage.getToStation().getId(), seatTypeEM);
						scheduleStageDTOList.add(scheduleStage);
					}
				}
			}
			// Schedule Trip Stage Fare
			List<ScheduleFareAutoOverrideDTO> tripStageFareList = tripStageFareService.getTripStageActiveFare(authDTO, tripDTO.getSchedule());
			List<ScheduleFareAutoOverrideDTO> fareOverridelist = fareOverrideService.getTripScheduleDateRangeActiveFare(authDTO, tripDTO.getSchedule(), tripDTO.getTripDate(), tripDTO.getTripDate());
			// Schedule Fare Auto Override
			Map<String, StageDTO> fareMap = new HashMap<>();
			for (ScheduleStageDTO scheduleStageDTO : scheduleStageDTOList) {
				if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
					// Group
					Map<Integer, GroupDTO> groupMap = new HashMap<>();
					groupMap.put(scheduleStageDTO.getGroup().getId(), scheduleStageDTO.getGroup());

					List<BusSeatTypeEM> busSeatTypeList = new ArrayList<>();
					busSeatTypeList.add(scheduleStageDTO.getBusSeatType());

					List<ScheduleFareAutoOverrideDTO> tripFareOverridelist = tripStageFareService.processScheduleTripStageFare(authDTO, tripDTO.getSchedule(), tripStageFareList, scheduleStageDTO.getFromStation(), scheduleStageDTO.getToStation());

					if (tripFareOverridelist.isEmpty()) {
						List<ScheduleFareAutoOverrideDTO> autoOverridelist = fareOverrideService.processTripScheduleActiveFare(authDTO, tripDTO.getSchedule(), fareOverridelist, scheduleStageDTO.getFromStation(), scheduleStageDTO.getToStation(), groupMap, scheduleStageDTO.getBusSeatType());
						tripFareOverridelist.addAll(autoOverridelist);
					}
					StageDTO stageDTO = new StageDTO();
					stageDTO.setFromStation(stationMap.get(scheduleStageDTO.getFromStation().getId()));
					stageDTO.setToStation(stationMap.get(scheduleStageDTO.getToStation().getId()));
					stageDTO.setStageSequence(Integer.parseInt(stationMap.get(scheduleStageDTO.getFromStation().getId()).getStationSequence() + "" + (lastStation.getStationSequence() - stationMap.get(scheduleStageDTO.getToStation().getId()).getStationSequence())));
					stageDTO.getFromStation().setStation(getStationDTObyId(scheduleStageDTO.getFromStation()));
					stageDTO.getToStation().setStation(getStationDTObyId(scheduleStageDTO.getToStation()));
					stageDTO.getFromStation().setMinitues(stationMap.get(scheduleStageDTO.getFromStation().getId()).getMinitues());
					stageDTO.getToStation().setMinitues(stationMap.get(scheduleStageDTO.getToStation().getId()).getMinitues());
					StageFareDTO stageFareDTO = new StageFareDTO();
					stageFareDTO.setFare(getStageFareWithOverride(stageDTO, scheduleStageDTO.getFare(), tripFareOverridelist, tripDate, scheduleStageDTO.getBusSeatType()));
					stageFareDTO.setBusSeatType(scheduleStageDTO.getBusSeatType());
					if (fareMap.get(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId()) == null) {
						List<StageFareDTO> fareList = new ArrayList<>();
						stageDTO.setStageFare(fareList);
						fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
					}

					// Apply Fare Rule
					if (authDTO.getNamespace().getProfile().getFareRule() != null && !authDTO.getNamespace().getProfile().getFareRule().isEmpty()) {
						FareRuleDetailsDTO fareRuleDetailsDTO = fareRuleService.getFareRuleDetails(authDTO, authDTO.getNamespace().getProfile().getFareRule(), stageDTO.getFromStation().getStation(), stageDTO.getToStation().getStation());
						if (fareRuleDetailsDTO.getId() != Numeric.ZERO_INT) {
							stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, busOverrideDTO, fareRuleDetailsDTO);
							stageDTO.setDistance(fareRuleDetailsDTO.getDistance());
						}
					}

					List<StageFareDTO> fareList = (fareMap.get(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId())).getStageFare();
					fareList.add(stageFareDTO);

					stageDTO.setStageFare(fareList);
					fareMap.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId(), stageDTO);
				}
			}
			Set<String> mapList = fareMap.keySet();
			for (String mapKey : mapList) {
				stageDTOList.add(fareMap.get(mapKey));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return stageDTOList;

	}

	public void syncTripFareToScheduleStageFare(AuthDTO authDTO, TripDTO tripDTO) {
		try {
			List<StageDTO> stages = getScheduleTripFare(authDTO, tripDTO);

			Map<String, BusSeatTypeEM> tripBusSeatTypes = new HashMap<>();
			Map<String, StageFareDTO> routeBusSeatTypeFare = new HashMap<>();
			for (StageDTO stageDTO : stages) {
				for (StageFareDTO fareDTO : stageDTO.getStageFare()) {
					tripBusSeatTypes.put(fareDTO.getBusSeatType().getCode(), fareDTO.getBusSeatType());

					routeBusSeatTypeFare.put(stageDTO.getFromStation().getStation().getId() + "_" + stageDTO.getToStation().getStation().getId() + "_" + fareDTO.getBusSeatType().getCode(), fareDTO);
				}
			}

			List<ScheduleStageDTO> scheduleStages = stageService.getScheduleStageV2(authDTO, tripDTO.getSchedule());
			ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, tripDTO.getSchedule());

			Map<String, BusSeatTypeEM> scheduleStageBusSeatTypes = scheduleBusDTO.getBus().getUniqueBusType();

			for (BusSeatTypeEM busSeatTypeEM : tripBusSeatTypes.values()) {
				if (scheduleStageBusSeatTypes.get(busSeatTypeEM.getCode()) == null) {
					throw new ServiceException(ErrorCode.BUSMAP_MISSED_MATCHED);
				}
			}

			List<ScheduleStageDTO> scheduleStageFares = new ArrayList<>();
			for (ScheduleStageDTO scheduleStageDTO : scheduleStages) {
				boolean isFareChanged = false;
				for (BusSeatTypeFareDTO seatTypeFare : scheduleStageDTO.getBusSeatTypeFare()) {
					StageFareDTO stageFareDTO = routeBusSeatTypeFare.get(scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId() + "_" + seatTypeFare.getBusSeatType().getCode());
					if (stageFareDTO != null && stageFareDTO.getFare().compareTo(seatTypeFare.getFare()) != 0) {
						seatTypeFare.setFare(stageFareDTO.getFare());
						isFareChanged = true;
					}
				}
				if (scheduleStageDTO.getBusSeatTypeFare().isEmpty()) {
					StageFareDTO stageFareDTO = routeBusSeatTypeFare.get(scheduleStageDTO.getFromStation().getId() + "_" + scheduleStageDTO.getToStation().getId() + "_" + scheduleStageDTO.getBusSeatType().getCode());
					if (stageFareDTO != null && stageFareDTO.getFare().compareTo(BigDecimal.valueOf(scheduleStageDTO.getFare())) != 0) {
						scheduleStageDTO.setFare(stageFareDTO.getFare().doubleValue());
						isFareChanged = true;
					}
				}
				if (isFareChanged) {
					scheduleStageFares.add(scheduleStageDTO);
				}
			}

			if (!scheduleStageFares.isEmpty()) {
				ScheduleStageDTO scheduleStage = new ScheduleStageDTO();
				scheduleStage.setList(scheduleStageFares);
				stageService.Update(authDTO, scheduleStage);
			}
		}
		catch (ServiceException e) {
			e.printStackTrace();
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	@Override
	public void addScheduleTripFare(AuthDTO authDTO, TripDTO tripDTO, List<ScheduleTripStageFareDTO> fareList) {
		try {
			tripService.getTrip(authDTO, tripDTO);
			logger.info("************************ Quick Fare {} ************************", tripDTO.getCode());
			logger.info("Trip fare update {}, {} {}", authDTO.getNamespaceCode(), authDTO.getUser().getUsername(), tripDTO.getCode());
			scheduleService.getSchedule(authDTO, tripDTO.getSchedule());

			tripDTO.getSchedule().setTripDate(tripDTO.getTripDate());

			List<ScheduleTripStageFareDTO> existingQuickFarelist = tripStageFareService.getScheduleTripStageFare(authDTO, tripDTO.getSchedule());

			StringBuilder errors = new StringBuilder();
			for (ScheduleTripStageFareDTO fareAutoOverrideDTO : fareList) {
				fareAutoOverrideDTO.getRoute().setFromStation(stationService.getStation(fareAutoOverrideDTO.getRoute().getFromStation()));
				fareAutoOverrideDTO.getRoute().setToStation(stationService.getStation(fareAutoOverrideDTO.getRoute().getToStation()));

				// Route Minimum & Maximum Fare Validation
				validateRouteFare(authDTO, tripDTO, fareAutoOverrideDTO.getRoute(), fareAutoOverrideDTO, errors);
			}
			if (StringUtil.isNotNull(errors.toString())) {
				throw new ServiceException(ErrorCode.ROUTE_FARE_OUT_OF_RANGE, errors.toString());
			}

			// save fare history
			updateQuickeFareHistory(authDTO, tripDTO, existingQuickFarelist, fareList);
			
			for (ScheduleTripStageFareDTO quickFareOverrideDTO : existingQuickFarelist) {
				boolean isExist = Text.FALSE;
				for (ScheduleTripStageFareDTO fareAutoOverrideDTO : fareList) {
					RouteDTO routeDTO = quickFareOverrideDTO.getRoute();
					RouteDTO route2 = fareAutoOverrideDTO.getRoute();
					if (routeDTO.getFromStation().getId() == route2.getFromStation().getId() && routeDTO.getToStation().getId() == route2.getToStation().getId()) {
						isExist = Text.TRUE;
						break;
					}
				}

				if (!isExist) {
					fareList.add(quickFareOverrideDTO);
				}
			}

			ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
			quickFareOverrideDTO.setCode(tripDTO.getCode());
			quickFareOverrideDTO.setSchedule(tripDTO.getSchedule());
			quickFareOverrideDTO.setActiveFlag(1);
			quickFareOverrideDTO.setFareDetails(convertQuickFareDetailsToString(fareList));
			quickFareOverrideDTO.setList(fareList);

			logger.info("Fare Details {} {}", tripDTO.getCode(), quickFareOverrideDTO.getFareDetails());

			tripStageFareService.updateQuickFare(authDTO, quickFareOverrideDTO);

			if (authDTO.getNamespace().getProfile().getOtaPartnerCode().get(UserTagEM.API_USER_RB.getCode()) != null && DateUtil.getDayDifferent(DateUtil.NOW(), tripDTO.getTripDate()) <= 15) {
				authDTO.getAdditionalAttribute().put("activity_type", "fare-change");
				searchService.pushInventoryChangesEvent(authDTO, tripDTO);
			}
			mercService.indexFareHistory(authDTO, tripDTO.getSchedule(), tripDTO, quickFareOverrideDTO);
			logger.info("-------------{}-------------- Quick Fare Added Successfully ---------------------------", tripDTO.getCode());
		}
		catch (ServiceException e) {
			logger.info("Exception while add trip fare " + e.getErrorCode() + (e.getData() != null ? " - " + e.getData().toString() : "") + " " + tripDTO.getCode());
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			logger.info("Exception while add trip fare " + e.getMessage());
		}
		finally {
			ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
			quickFareOverrideDTO.setCode(tripDTO.getCode());
			quickFareOverrideDTO.setSchedule(tripDTO.getSchedule());
			quickFareOverrideDTO.setActiveFlag(1);
			quickFareOverrideDTO.setList(fareList);
			notificationPushService.pushFareChangeNotification(authDTO, tripDTO, fareList);
		}
	}

	/*
	 * Apply Fare to Trip quick fare, consider bus type override, alternate date
	 */
	public Map<String, String> applyScheduleTripFareTemplate(AuthDTO authDTO, TripDTO tripDTO, ScheduleFareTemplateDTO fareTemplate) {
		Map<String, String> tripStatus = new HashMap<String, String>();
		List<DateTime> tripDateList = fareTemplate.getTripDates();

		tripService.getTrip(authDTO, tripDTO);
		ScheduleDTO schedule = scheduleService.getSchedule(authDTO, tripDTO.getSchedule());
		schedule.setTripDate(tripDTO.getTripDate());
		BusDTO tripbus = tripDTO.getBus();

		for (RouteDTO routeDTO : fareTemplate.getStageFare()) {
			routeDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
			routeDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
		}

		// fareTemplate = fareTemplateService.getScheduleFareTemplate(authDTO,
		// fareTemplate);
		Map<String, RouteDTO> routeFareTemplate = fareTemplate.getRouteFare();
		List<TripDTO> tripList = searchService.getScheduleTripList(authDTO, schedule, tripDateList);

		for (DateTime tripDate : tripDateList) {
			TripDTO trip = new TripDTO();
			trip.setTripDate(tripDate);
			try {
				trip.setCode(BitsUtil.getGeneratedTripCode(authDTO, schedule, trip));
				TripDTO scheduleTrip = tripList.stream().filter(tripfilter -> trip.getCode().equals(tripfilter.getCode())).findFirst().orElse(null);
				if (scheduleTrip == null) {
					tripStatus.put(tripDate.format("YYYY-MM-DD"), Text.FAIL);
					continue;
				}

				if (tripbus.getId() != scheduleTrip.getSchedule().getScheduleBus().getBus().getId()) {
					tripStatus.put(tripDate.format("YYYY-MM-DD"), Text.FAIL);
					continue;
				}

				List<ScheduleTripStageFareDTO> quickFares = new ArrayList<ScheduleTripStageFareDTO>();
				// Identify Stage and fare with trip
				// bus-type(using-bus-override)
				for (StageDTO stage : scheduleTrip.getStageList()) {
					RouteDTO routeTemplate = routeFareTemplate.get(stage.getFromStation().getStation().getId() + Text.UNDER_SCORE + stage.getToStation().getStation().getId());
					if (routeTemplate == null) {
						continue;
					}
					ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();

					RouteDTO route = new RouteDTO();
					route.setFromStation(stationService.getStation(routeTemplate.getFromStation()));
					route.setToStation(stationService.getStation(routeTemplate.getToStation()));

					List<StageFareDTO> stageSeatTypeFare = new ArrayList<StageFareDTO>();
					for (StageFareDTO stageFare : routeTemplate.getStageFare()) {
						if (stageFare.getFare().compareTo(BigDecimal.ZERO) <= 0) {
							continue;
						}
						stageFare.setFare(stageFare.getFare().setScale(0, BigDecimal.ROUND_DOWN));
						stageSeatTypeFare.add(stageFare);
					}
					route.setStageFare(stageSeatTypeFare);
					quickFareOverrideDTO.setRoute(route);

					quickFareOverrideDTO.setActiveFlag(Numeric.ONE_INT);
					quickFares.add(quickFareOverrideDTO);
				}
				if (quickFares.isEmpty()) {
					tripStatus.put(tripDate.format("YYYY-MM-DD"), Text.FAIL);
					continue;
				}
				addScheduleTripFare(authDTO, trip, quickFares);
				tripStatus.put(tripDate.format("YYYY-MM-DD"), Text.SUCCESS);
			}
			catch (ServiceException e) {
				tripStatus.put(trip.getTripDate().format("YYYY-MM-DD"), Text.FAIL);
			}
			catch (Exception e) {
				tripStatus.put(trip.getTripDate().format("YYYY-MM-DD"), Text.FAIL);
				e.printStackTrace();
			}
		}
		return tripStatus;

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

	private boolean existStageInRouteList(List<RouteDTO> routeList, StationDTO fromStationDTO, StationDTO toStationDTO) {
		boolean status = false;
		// Route List
		for (RouteDTO routeDTO : routeList) {
			if (!status && fromStationDTO != null && routeDTO.getFromStation() != null && routeDTO.getFromStation().getId() != 0 && routeDTO.getFromStation().getId() == fromStationDTO.getId() && toStationDTO != null && routeDTO.getToStation() != null && routeDTO.getToStation().getId() != 0 && routeDTO.getToStation().getId() == toStationDTO.getId()) {
				status = true;
			}
		}
		return status;
	}

	@Override
	public List<TripDTO> getScheduleTripFareV2(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime fromDate, DateTime toDate, List<String> tripDateList, boolean includeTicketBookings) {
		List<TripDTO> tripList = new ArrayList<TripDTO>();

		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.getScheduleDTO(authDTO, scheduleDTO);

		List<ScheduleFareAutoOverrideDTO> fareOverridelist = fareOverrideService.getTripScheduleDateRangeActiveFare(authDTO, scheduleDTO, fromDate, toDate);
		for (String tripDate : tripDateList) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setTripDate(new DateTime(tripDate));
			tripDTO.setCode(BitsUtil.getGeneratedTripCode(authDTO, scheduleDTO, tripDTO));
			scheduleDTO.setTripDate(tripDTO.getTripDate());
			tripDTO.setSchedule(scheduleDTO);

			List<StageDTO> stageDTOList = getTripStageFare(authDTO, tripDTO, fareOverridelist);
			tripDTO.setStageList(stageDTOList);

			if (includeTicketBookings) {
				tripService.getBookedBlockedSeats(authDTO, tripDTO);

				// Booked Seat Count
				int bookedSeatCount = 0;
				if (tripDTO != null && tripDTO.getTicketDetailsList() != null && !tripDTO.getTicketDetailsList().isEmpty()) {
					for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {
						if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
							continue;
						}
						if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId()) {
							continue;
						}
						if (ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() && ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
							bookedSeatCount++;
						}
					}
				}
				tripDTO.setBookedSeatCount(bookedSeatCount);
			}
			tripList.add(tripDTO);
		}
		return tripList;
	}

	private List<StageDTO> getTripStageFare(AuthDTO authDTO, TripDTO tripDTO, List<ScheduleFareAutoOverrideDTO> scheduleFareOverridelist) {
		List<StageDTO> stageDTOList = new ArrayList<>();
		try {
			tripDTO.getSchedule().setTripDate(tripDTO.getTripDate());
			DateTime tripDate = tripDTO.getTripDate();
			// Schedule Stage
			List<ScheduleStageDTO> stageList = stageService.getByScheduleTripDate(authDTO, tripDTO.getSchedule(), tripDate);
			// Schedule Station
			List<ScheduleStationDTO> stationList = scheduleStationService.getByScheduleTripDate(authDTO, tripDTO.getSchedule(), tripDate);
			Map<Integer, StageStationDTO> stationMap = new HashMap<>();
			ScheduleStationDTO firstStation = null;
			ScheduleStationDTO lastStation = null;
			for (ScheduleStationDTO scheduleStationDTO : stationList) {
				if (scheduleStationDTO.getActiveFlag() == -1) {
					continue;
				}
				StageStationDTO stageStationDTO = new StageStationDTO();
				stageStationDTO.setStationSequence(scheduleStationDTO.getStationSequence());
				stageStationDTO.setStation(scheduleStationDTO.getStation());
				stageStationDTO.setMinitues(scheduleStationDTO.getMinitues());
				stationMap.put(scheduleStationDTO.getStation().getId(), stageStationDTO);
				//
				if (firstStation == null || scheduleStationDTO.getStationSequence() < firstStation.getStationSequence()) {
					firstStation = scheduleStationDTO;
				}
				if (lastStation == null || scheduleStationDTO.getStationSequence() > lastStation.getStationSequence()) {
					lastStation = scheduleStationDTO;
				}
			}
			// Bus Type and BusMap
			ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, tripDTO.getSchedule());
			BusDTO busOverrideDTO = busOverrideService.applyScheduleBusOverride(authDTO, tripDTO.getSchedule(), scheduleBusDTO.getBus());
			tripDTO.setBus(busOverrideDTO);

			Map<String, BusSeatTypeEM> busTypeMap = new HashMap<String, BusSeatTypeEM>();
			for (BusSeatLayoutDTO layoutDTO : busOverrideDTO.getBusSeatLayoutDTO().getList()) {
				if (layoutDTO.getBusSeatType().isReservation()) {
					busTypeMap.put(layoutDTO.getName(), layoutDTO.getBusSeatType());
				}
			}

			// Identify Stage and fare with trip bus-type(using-bus-override)
			List<ScheduleStageDTO> scheduleStageDTOList = new ArrayList<ScheduleStageDTO>();
			Map<String, BusSeatTypeEM> bustype = busOverrideDTO.getUniqueReservableBusType();
			Map<String, BusSeatTypeEM> stageFareBustype = tripDTO.getSchedule().getUniqueStageBusType(stageList);
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
			// Schedule Trip Stage Fare
			List<ScheduleFareAutoOverrideDTO> tripStageFareList = tripStageFareService.getTripStageActiveFare(authDTO, tripDTO.getSchedule());
			// Schedule Fare Auto Override
			Map<String, StageDTO> fareMap = new HashMap<>();
			for (ScheduleStageDTO scheduleStageDTO : scheduleStageDTOList) {
				if (stationMap.get(scheduleStageDTO.getFromStation().getId()) != null && stationMap.get(scheduleStageDTO.getToStation().getId()) != null) {
					// Group
					Map<Integer, GroupDTO> groupMap = new HashMap<>();
					groupMap.put(scheduleStageDTO.getGroup().getId(), scheduleStageDTO.getGroup());

					List<ScheduleFareAutoOverrideDTO> tripFareOverridelist = tripStageFareService.processScheduleTripStageFare(authDTO, tripDTO.getSchedule(), tripStageFareList, scheduleStageDTO.getFromStation(), scheduleStageDTO.getToStation());

					if (tripFareOverridelist.isEmpty()) {
						List<ScheduleFareAutoOverrideDTO> autoOverridelist = fareOverrideService.processTripScheduleActiveFare(authDTO, tripDTO.getSchedule(), scheduleFareOverridelist, scheduleStageDTO.getFromStation(), scheduleStageDTO.getToStation(), groupMap, scheduleStageDTO.getBusSeatType());
						tripFareOverridelist.addAll(autoOverridelist);
					}
					StageDTO stageDTO = new StageDTO();
					stageDTO.setFromStation(stationMap.get(scheduleStageDTO.getFromStation().getId()));
					stageDTO.setToStation(stationMap.get(scheduleStageDTO.getToStation().getId()));
					stageDTO.setStageSequence(Integer.parseInt(stationMap.get(scheduleStageDTO.getFromStation().getId()).getStationSequence() + "" + (lastStation.getStationSequence() - stationMap.get(scheduleStageDTO.getToStation().getId()).getStationSequence())));
					stageDTO.getFromStation().setStation(getStationDTObyId(scheduleStageDTO.getFromStation()));
					stageDTO.getToStation().setStation(getStationDTObyId(scheduleStageDTO.getToStation()));
					stageDTO.getFromStation().setMinitues(stationMap.get(scheduleStageDTO.getFromStation().getId()).getMinitues());
					stageDTO.getToStation().setMinitues(stationMap.get(scheduleStageDTO.getToStation().getId()).getMinitues());
					StageFareDTO stageFareDTO = new StageFareDTO();
					stageFareDTO.setFare(getStageFareWithOverride(stageDTO, scheduleStageDTO.getFare(), tripFareOverridelist, tripDate, scheduleStageDTO.getBusSeatType()));
					stageFareDTO.setBusSeatType(scheduleStageDTO.getBusSeatType());

					if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
						applyDynamicStageFare(authDTO, tripDTO, scheduleStageDTO, busTypeMap, stageFareDTO);
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
			}
			Set<String> mapList = fareMap.keySet();
			for (String mapKey : mapList) {
				stageDTOList.add(fareMap.get(mapKey));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return stageDTOList;

	}

	private void applyDynamicStageFare(AuthDTO authDTO, TripDTO tripDTO, ScheduleStageDTO scheduleStageDTO, Map<String, BusSeatTypeEM> busTypeMap, StageFareDTO stageFareDTO) {
		ScheduleDynamicStageFareDetailsDTO dynamicStageFare = dynamicStageFareService.getScheduleDynamicStageFare(authDTO, tripDTO.getSchedule(), scheduleStageDTO.getFromStation(), scheduleStageDTO.getToStation());
		if (dynamicStageFare != null) {
			ScheduleDynamicStageFareDetailsDTO dynamicStageTripFareDetails = dynamicStageFareService.getDynamicPricingTripStageFareDetails(authDTO, tripDTO.getSchedule(), dynamicStageFare);
			dynamicStageFare.setSeatFare(dynamicStageTripFareDetails != null ? dynamicStageTripFareDetails.getSeatFare() : null);
			if (dynamicStageFare.getSeatFare() != null && !dynamicStageFare.getSeatFare().isEmpty()) {
				for (BusSeatLayoutDTO seatLayoutDTO : dynamicStageFare.getSeatFare()) {
					BusSeatTypeEM busSeatTypeEM = busTypeMap.get(seatLayoutDTO.getName());
					if (busSeatTypeEM != null && busSeatTypeEM.getId() == scheduleStageDTO.getBusSeatType().getId()) {
						stageFareDTO.setFare(seatLayoutDTO.getFare());
						break;
					}
				}
			}
		}

	}

	private void validateRouteFare(AuthDTO authDTO, TripDTO tripDTO, RouteDTO route, ScheduleTripStageFareDTO fareAutoOverrideDTO, StringBuilder errors) {
		String error = Text.EMPTY;
		RouteDTO routeDTO = stationRouteService.getRouteDTO(authDTO, route.getFromStation(), route.getToStation());
		if (routeDTO != null) {
			List<ScheduleSeatFareDTO> seatFares = scheduleSeatFareService.getByScheduleId(authDTO, tripDTO.getSchedule(), fareAutoOverrideDTO.getRoute().getFromStation(), fareAutoOverrideDTO.getRoute().getToStation());

			// Schedule Seat Fare
			Map<String, BusSeatLayoutDTO> seatMap = new HashMap<String, BusSeatLayoutDTO>();
			if (seatFares != null && !seatFares.isEmpty()) {
				// Bus Type and BusMap
				ScheduleBusDTO scheduleBusDTO = busService.getByScheduleId(authDTO, tripDTO.getSchedule());
				BusDTO busOverrideDTO = busOverrideService.applyScheduleBusOverride(authDTO, tripDTO.getSchedule(), scheduleBusDTO.getBus());
				tripDTO.setBus(busOverrideDTO);

				for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
					seatMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
				}
			}

			for (StageFareDTO busSeatTypeFare : fareAutoOverrideDTO.getRoute().getStageFare()) {
				if (seatFares != null && !seatFares.isEmpty()) {
					for (ScheduleSeatFareDTO scheduleSeatFareDTO : seatFares) {
						for (BusSeatLayoutDTO seatLayoutDTO : scheduleSeatFareDTO.getBus().getBusSeatLayoutDTO().getList()) {
							if (seatMap.get(seatLayoutDTO.getCode()) != null) {
								int fare = calculateSeatFare(scheduleSeatFareDTO, busSeatTypeFare.getFare()).intValue();
								if (fare < routeDTO.getMinFare() || fare > routeDTO.getMaxFare()) {
									error = route.getFromStation().getName() + " to " + route.getToStation().getName() + ", Expected fare is " + routeDTO.getMinFare() + " - " + routeDTO.getMaxFare() + ". But given " + busSeatTypeFare.getFare();
									System.out.println("ERVRF01 - " + error);
									errors.append(error);
									errors.append(Text.VERTICAL_BAR);
								}
							}
						}
					}
				}
				else if (busSeatTypeFare.getFare().intValue() < routeDTO.getMinFare() || busSeatTypeFare.getFare().intValue() > routeDTO.getMaxFare()) {
					error = route.getFromStation().getName() + " to " + route.getToStation().getName() + ", Expected fare is " + routeDTO.getMinFare() + " - " + routeDTO.getMaxFare() + ". But given " + busSeatTypeFare.getFare();
					System.out.println("ERVRF01 - " + error);
					errors.append(error);
					errors.append(Text.VERTICAL_BAR);
				}
			}
		}
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

	@Override
	public List<ScheduleDTO> getScheduleOccupancy(AuthDTO authDTO) {
		List<DateTime> tripDateList = DateUtil.getDateListV2(DateUtil.NOW().getStartOfDay().minusDays(4), authDTO.getNamespace().getProfile().getAdvanceBookingDays());
		List<ScheduleDTO> scheduleList = scheduleService.getActive(authDTO, DateUtil.NOW().minusDays(4));
		HelperUtil helperUtil = new HelperUtil();
		DateTime maxDate = DateUtil.NOW().plusDays(15);
		for (ScheduleDTO schedule : scheduleList) {
			DateTime fromDate = DateUtil.getDateTime(schedule.getActiveFrom());
			DateTime toDate = DateUtil.getDateTime(schedule.getActiveTo());
			List<TripDTO> tripList = new ArrayList<TripDTO>();
			for (DateTime tripDate : tripDateList) {
				TripDTO tripDTO = new TripDTO();
				tripDTO.setTripDate(tripDate);
				tripDTO.setActiveFlag(Numeric.ONE_INT);
				int bookedSeatCount = -1;
				if (DateUtil.isDateTimeWithinRange(fromDate, toDate, tripDate)) {
					tripDTO.setCode(helperUtil.getGeneratedTripCodeV2(authDTO, schedule, tripDTO));
					List<TicketDetailsDTO> list = tripService.getBookedBlockedSeats(authDTO, tripDTO);

					bookedSeatCount = 0;
					if (list != null && !list.isEmpty()) {
						for (TicketDetailsDTO ticketDetailsDTO : list) {
							if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
								continue;
							}
							if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId()) {
								continue;
							}
							if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
								continue;
							}
							if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
								bookedSeatCount++;
							}
						}
					}
					if (bookedSeatCount > 0 && tripDate.gt(maxDate)) {
						maxDate = tripDate;
					}
				}
				tripDTO.setBookedSeatCount(bookedSeatCount);
				tripList.add(tripDTO);
			}
			schedule.setTripList(tripList);
		}
		// Based on Advance Booking max Date, default 15days
		for (ScheduleDTO schedule : scheduleList) {
			for (TripDTO tripDTO : schedule.getTripList()) {
				if (tripDTO.getTripDate().gt(maxDate) && tripDTO.getBookedSeatCount() <= 0) {
					tripDTO.setActiveFlag(Numeric.ZERO_INT);
				}
			}
		}
		return scheduleList;
	}

	private String convertQuickFareDetailsToString(List<ScheduleTripStageFareDTO> quickFareOverrides) {
		StringBuilder fareDetails = new StringBuilder();
		for (Iterator<ScheduleTripStageFareDTO> iterator = quickFareOverrides.iterator(); iterator.hasNext();) {
			ScheduleTripStageFareDTO quickFareOverrideDTO = iterator.next();
			RouteDTO routeDTO = quickFareOverrideDTO.getRoute();
			fareDetails.append(routeDTO.getFromStation().getId()).append("_").append(routeDTO.getToStation().getId()).append("-");

			for (Iterator<StageFareDTO> busSeatTypeFareIterator = routeDTO.getStageFare().iterator(); busSeatTypeFareIterator.hasNext();) {
				StageFareDTO busSeatTypeFare = busSeatTypeFareIterator.next();

				fareDetails.append(busSeatTypeFare.getBusSeatType().getCode()).append(":").append(busSeatTypeFare.getFare());
				if (busSeatTypeFareIterator.hasNext()) {
					fareDetails.append(",");
				}
			}
			if (iterator.hasNext()) {
				fareDetails.append("|");
			}
		}
		return fareDetails.toString();
	}

	@Override
	public JSONArray getScheduleOccupancyAnalytics(AuthDTO authDTO, DateTime fromDate, DateTime toDate) {
		List<DateTime> tripDateList = DateUtil.getDateListV3(fromDate.getStartOfDay(), toDate.getEndOfDay(), "1111111");

		List<ScheduleDTO> scheduleList = scheduleService.getActive(authDTO, fromDate);

		List<String> stationCodes = new ArrayList<>();
		for (ScheduleDTO schedule : scheduleList) {
			ScheduleStationDTO fromScheduleStationDTO = BitsUtil.getOriginStation(schedule.getStationList());
			if (stationCodes.isEmpty() || !stationCodes.contains(fromScheduleStationDTO.getStation().getCode())) {
				stationCodes.add(fromScheduleStationDTO.getStation().getCode());
			}
		}

		HelperUtil helperUtil = new HelperUtil();

		Map<String, JSONObject> scheduleOccupancyAnalyticsMap = new HashMap<>();
		for (ScheduleDTO schedule : scheduleList) {
			DateTime activeFrom = DateUtil.getDateTime(schedule.getActiveFrom());
			DateTime activeTo = DateUtil.getDateTime(schedule.getActiveTo());

			ScheduleStationDTO fromScheduleStationDTO = BitsUtil.getOriginStation(schedule.getStationList());
			ScheduleStationDTO toScheduleStationDTO = BitsUtil.getDestinationStation(schedule.getStationList());

			for (DateTime tripDate : tripDateList) {
				TripDTO tripDTO = new TripDTO();
				tripDTO.setTripDate(tripDate);
				tripDTO.setActiveFlag(Numeric.ONE_INT);

				int fromStationBookedCount = 0;
				int toStationBookedCount = 0;

				if (DateUtil.isDateTimeWithinRange(activeFrom, activeTo, tripDate)) {
					tripDTO.setCode(helperUtil.getGeneratedTripCodeV2(authDTO, schedule, tripDTO));
					List<TicketDetailsDTO> list = tripService.getBookedBlockedSeats(authDTO, tripDTO);

					if (list != null && !list.isEmpty()) {
						for (TicketDetailsDTO ticketDetailsDTO : list) {
							if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
								continue;
							}
							if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId()) {
								continue;
							}
							if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
								continue;
							}
							if (fromScheduleStationDTO.getStation().getId() != ticketDetailsDTO.getFromStation().getId() && toScheduleStationDTO.getStation().getId() != ticketDetailsDTO.getToStation().getId()) {
								continue;
							}
							if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
								if (fromScheduleStationDTO.getStation().getId() == ticketDetailsDTO.getFromStation().getId() && stationCodes.contains(fromScheduleStationDTO.getStation().getCode())) {
									fromStationBookedCount = fromStationBookedCount + 1;
								}
								if (toScheduleStationDTO.getStation().getId() == ticketDetailsDTO.getToStation().getId() && stationCodes.contains(toScheduleStationDTO.getStation().getCode())) {
									toStationBookedCount = toStationBookedCount + 1;
								}
							}
						}
					}
				}

				if (fromStationBookedCount == 0 && toStationBookedCount == 0) {
					continue;
				}

				String fromKey = fromScheduleStationDTO.getStation().getCode() + Text.UNDER_SCORE + DateUtil.convertDate(tripDate);
				String toKey = toScheduleStationDTO.getStation().getCode() + Text.UNDER_SCORE + DateUtil.convertDate(tripDate);

				if (scheduleOccupancyAnalyticsMap.get(fromKey) == null && fromStationBookedCount != 0) {
					JSONObject scheduleOccupancyAnalyticsJSON = new JSONObject();
					scheduleOccupancyAnalyticsJSON.put("stationCode", fromScheduleStationDTO.getStation().getCode());
					scheduleOccupancyAnalyticsJSON.put("stationName", fromScheduleStationDTO.getStation().getName());
					scheduleOccupancyAnalyticsJSON.put("tripDate", DateUtil.convertDate(tripDTO.getTripDate()));
					scheduleOccupancyAnalyticsJSON.put("fromSeatCount", schedule.getScheduleBus().getBus().getReservableLayoutSeatCount());
					scheduleOccupancyAnalyticsJSON.put("fromBookedSeatCount", fromStationBookedCount);
					scheduleOccupancyAnalyticsJSON.put("toSeatCount", 0);
					scheduleOccupancyAnalyticsJSON.put("toBookedSeatCount", 0);
					scheduleOccupancyAnalyticsMap.put(fromKey, scheduleOccupancyAnalyticsJSON);
				}
				else if (scheduleOccupancyAnalyticsMap.get(toKey) == null && toStationBookedCount != 0) {
					JSONObject scheduleOccupancyAnalyticsJSON = new JSONObject();
					scheduleOccupancyAnalyticsJSON.put("stationCode", toScheduleStationDTO.getStation().getCode());
					scheduleOccupancyAnalyticsJSON.put("stationName", toScheduleStationDTO.getStation().getName());
					scheduleOccupancyAnalyticsJSON.put("tripDate", DateUtil.convertDate(tripDTO.getTripDate()));
					scheduleOccupancyAnalyticsJSON.put("fromSeatCount", 0);
					scheduleOccupancyAnalyticsJSON.put("fromBookedSeatCount", 0);
					scheduleOccupancyAnalyticsJSON.put("toSeatCount", schedule.getScheduleBus().getBus().getReservableLayoutSeatCount());
					scheduleOccupancyAnalyticsJSON.put("toBookedSeatCount", toStationBookedCount);
					scheduleOccupancyAnalyticsMap.put(toKey, scheduleOccupancyAnalyticsJSON);
				}
				else if (scheduleOccupancyAnalyticsMap.get(fromKey) != null) {
					JSONObject scheduleOccupancyAnalyticsJSON = scheduleOccupancyAnalyticsMap.get(fromKey);
					scheduleOccupancyAnalyticsJSON.put("fromSeatCount", schedule.getScheduleBus().getBus().getReservableLayoutSeatCount() + Integer.valueOf(String.valueOf(scheduleOccupancyAnalyticsJSON.get("fromSeatCount"))));
					scheduleOccupancyAnalyticsJSON.put("fromBookedSeatCount", fromStationBookedCount + Integer.valueOf(String.valueOf(scheduleOccupancyAnalyticsJSON.get("fromBookedSeatCount"))));
					scheduleOccupancyAnalyticsMap.put(fromKey, scheduleOccupancyAnalyticsJSON);
				}
				else if (scheduleOccupancyAnalyticsMap.get(toKey) != null) {
					JSONObject scheduleOccupancyAnalyticsJSON = scheduleOccupancyAnalyticsMap.get(toKey);
					scheduleOccupancyAnalyticsJSON.put("toSeatCount", schedule.getScheduleBus().getBus().getReservableLayoutSeatCount() + Integer.valueOf(String.valueOf(scheduleOccupancyAnalyticsJSON.get("toSeatCount"))));
					scheduleOccupancyAnalyticsJSON.put("toBookedSeatCount", toStationBookedCount + Integer.valueOf(String.valueOf(scheduleOccupancyAnalyticsJSON.get("toBookedSeatCount"))));
					scheduleOccupancyAnalyticsMap.put(toKey, scheduleOccupancyAnalyticsJSON);
				}
			}
		}

		return JSONArray.fromObject(scheduleOccupancyAnalyticsMap.values());
	}
	
	private void updateQuickeFareHistory(AuthDTO authDTO, TripDTO tripDTO, List<ScheduleTripStageFareDTO> repoStageFareList, List<ScheduleTripStageFareDTO> stageFareList) {
		try {
			List<ScheduleTripStageFareDTO> stageFareLogList = new ArrayList<>();
			Map<String, Map<String, BigDecimal>> stageFareDataMap = new HashMap<>();
			for (ScheduleTripStageFareDTO repoScheduleStagefareDTO : repoStageFareList) {
				String routeKey = repoScheduleStagefareDTO.getRoute().getFromStation().getId() + "_" + repoScheduleStagefareDTO.getRoute().getToStation().getId();
				Map<String, BigDecimal> fareMap = new HashMap<>();
				for (StageFareDTO repoStageFareDTO : repoScheduleStagefareDTO.getRoute().getStageFare()) {
					fareMap.put(repoStageFareDTO.getBusSeatType().getCode(), repoStageFareDTO.getFare());
				}
				stageFareDataMap.put(routeKey, fareMap);
			}
			
			for (ScheduleTripStageFareDTO scheduleStageFareDTO : stageFareList) {
				String routeKey = scheduleStageFareDTO.getRoute().getFromStation().getId() + "_" + scheduleStageFareDTO.getRoute().getToStation().getId();
				
				StringBuilder addFareLogDetails = new StringBuilder();
				if (stageFareDataMap.get(routeKey) != null) {
					Map<String, BigDecimal> fareMap = stageFareDataMap.get(routeKey);
					for (StageFareDTO stageFareDTO : scheduleStageFareDTO.getRoute().getStageFare()) {
						BigDecimal stageFare = fareMap.get(stageFareDTO.getBusSeatType().getCode());
						if (addFareLogDetails.length() > 0) {
							addFareLogDetails.append(Text.COMMA);
						}
						if (stageFare != null && stageFare.compareTo(stageFareDTO.getFare()) != 0) {
							addFareLogDetails.append(stageFareDTO.getBusSeatType().getCode()).append(":");
							addFareLogDetails.append(stageFare).append("_").append(stageFareDTO.getFare());
						}
						else if (stageFare == null) {
							addFareLogDetails.append(stageFareDTO.getBusSeatType().getCode()).append(":");
							addFareLogDetails.append(stageFareDTO.getFare());
						}
					}
					
					ScheduleTripStageFareDTO stageFareLogDTO = new ScheduleTripStageFareDTO();
					stageFareLogDTO.setName("Edited");
					stageFareLogDTO.setTripDate(DateUtil.convertDate(tripDTO.getTripDate()));
					stageFareLogDTO.setCode(tripDTO.getCode());
					stageFareLogDTO.setSchedule(tripDTO.getSchedule());
					
					RouteDTO route = new RouteDTO();
					route.setFromStation(scheduleStageFareDTO.getRoute().getFromStation());
					route.setToStation(scheduleStageFareDTO.getRoute().getToStation());
					stageFareLogDTO.setRoute(route);
					stageFareLogDTO.setFareDetails(addFareLogDetails.toString());
					stageFareLogList.add(stageFareLogDTO);
				}
				else {
					StringBuilder fareLogDetails = new StringBuilder();
					
					ScheduleTripStageFareDTO stageFareLogDTO = new ScheduleTripStageFareDTO();
					stageFareLogDTO.setName("Added");
					stageFareLogDTO.setTripDate(DateUtil.convertDate(tripDTO.getTripDate()));
					stageFareLogDTO.setCode(tripDTO.getCode());
					stageFareLogDTO.setSchedule(tripDTO.getSchedule());
					
					RouteDTO route = new RouteDTO();
					route.setFromStation(scheduleStageFareDTO.getRoute().getFromStation());
					route.setToStation(scheduleStageFareDTO.getRoute().getToStation());
					stageFareLogDTO.setRoute(route);
					
					for (StageFareDTO stageFareDTO : scheduleStageFareDTO.getRoute().getStageFare()) {
						if (fareLogDetails.length() > 0) {
							fareLogDetails.append(Text.COMMA);
						} 
						fareLogDetails.append(stageFareDTO.getBusSeatType().getCode()).append(":");
						fareLogDetails.append(stageFareDTO.getFare());
					}
					stageFareLogDTO.setFareDetails(fareLogDetails.toString());
					stageFareLogList.add(stageFareLogDTO);
				}
			}
			
			if (!stageFareLogList.isEmpty()) {
				ScheduleFareAutoOverrideDAO dao = new ScheduleFareAutoOverrideDAO();
				dao.saveScheduleTripStageFareLog(authDTO, stageFareLogList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
