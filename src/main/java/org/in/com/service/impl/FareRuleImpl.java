package org.in.com.service.impl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.lang.ArrayUtils;
import org.in.com.aggregator.bits.BitsService;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.FareRuleDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatTypeFareDTO;
import org.in.com.dto.FareRuleDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleTripStageFareDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusService;
import org.in.com.service.FareRuleService;
import org.in.com.service.ScheduleBusOverrideService;
import org.in.com.service.ScheduleBusService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStageService;
import org.in.com.service.ScheduleTripStageFareService;
import org.in.com.service.StateService;
import org.in.com.service.StationService;
import org.in.com.service.TripService;
import org.in.com.service.helper.TripHelperServiceImpl;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StreamUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class FareRuleImpl extends BaseImpl implements FareRuleService {
	@Autowired
	ScheduleStageService scheduleStageService;
	@Autowired
	StateService stateService;
	@Autowired
	StationService stationService;
	@Autowired
	BusService busService;
	@Autowired
	BitsService bitsService;
	@Autowired
	ScheduleService scheduleService;
	@Lazy
	@Autowired
	ScheduleBusService scheduleBusService;
	@Lazy
	@Autowired
	ScheduleBusOverrideService busOverrideService;
	@Autowired
	ScheduleTripStageFareService scheduleTripStageFareService;
	@Autowired
	TripService tripService;

	@Override
	public List<FareRuleDTO> get(AuthDTO authDTO, FareRuleDTO fareRuleDTO) {
		return null;
	}

	@Override
	public List<FareRuleDTO> getAll(AuthDTO authDTO) {
		FareRuleDAO fareRuleDAO = new FareRuleDAO();
		List<FareRuleDTO> fareRuleList = fareRuleDAO.getAllFareRule(authDTO);
		for (FareRuleDTO fareRule : fareRuleList) {
			fareRule.setState(stateService.getState(fareRule.getState()));
		}
		return fareRuleList;
	}

	@Override
	public FareRuleDTO Update(AuthDTO authDTO, FareRuleDTO fareRuleDTO) {
		FareRuleDAO fareRuleDAO = new FareRuleDAO();
		fareRuleDTO.setState(stateService.getState(fareRuleDTO.getState()));
		fareRuleDAO.updateFareRule(authDTO, fareRuleDTO);
		return fareRuleDTO;
	}

	@Override
	public FareRuleDTO getFareRule(AuthDTO authDTO, FareRuleDTO fareRuleDTO) {
		FareRuleDAO fareRuleDAO = new FareRuleDAO();
		fareRuleDAO.getFareRule(authDTO, fareRuleDTO);

		StateDTO stateDTO = stateService.getState(fareRuleDTO.getState());
		fareRuleDTO.setState(stateDTO);
		return fareRuleDTO;
	}

	@Override
	public void updateFareRuleDetails(AuthDTO authDTO, FareRuleDTO fareRule) {
		if (!ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		FareRuleDAO fareRuleDAO = new FareRuleDAO();
		for (FareRuleDetailsDTO fareRuleDetailsDTO : fareRule.getFareRuleDetails()) {
			// get from station
			fareRuleDetailsDTO.setFromStation(stationService.getStation(fareRuleDetailsDTO.getFromStation()));
			// get to station
			fareRuleDetailsDTO.setToStation(stationService.getStation(fareRuleDetailsDTO.getToStation()));
		}
		fareRuleDAO.updateFareRuleDetails(authDTO, fareRule);
	}

	@Override
	public FareRuleDTO getFareRuleDetails(AuthDTO authDTO, StationDTO fromStationDTO, StationDTO toStationDTO) {
		FareRuleDTO fareRule = new FareRuleDTO();
		List<FareRuleDTO> fareRuleList = authDTO.getNamespace().getProfile().getFareRule();
		if (fareRuleList.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE, " FareRule Not Assigned");
		}
		if (StringUtil.isNotNull(fromStationDTO.getCode())) {
			fromStationDTO = stationService.getStation(fromStationDTO);
		}
		if (StringUtil.isNotNull(toStationDTO.getCode())) {
			toStationDTO = stationService.getStation(toStationDTO);
		}
		List<FareRuleDetailsDTO> fareRuleDetailsList = getFareRuleDetailsV1(authDTO, fareRuleList, fromStationDTO, toStationDTO);

		fareRule.setFareRuleDetails(fareRuleDetailsList);
		return fareRule;
	}

	@Override
	public JSONArray getFareRuleDetailsBySchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		JSONArray stages = new JSONArray();

		/** Fare rule should be enabled */
		List<FareRuleDTO> fareRuleList = authDTO.getNamespace().getProfile().getFareRule();
		if (!fareRuleList.isEmpty()) {
			/** Schedule Details */
			scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);
			scheduleDTO.setTripDate(DateUtil.getDateTime(DateUtil.convertDate(DateUtil.NOW())));

			/** Schedule Bus */
			ScheduleBusDTO scheduleBusDTO = scheduleBusService.getByScheduleId(authDTO, scheduleDTO);

			/** Schedule Bus Overrides - Only current & future date overrides */
			List<ScheduleBusOverrideDTO> scheduleBusOverrides = busOverrideService.getBusOverrideByScheduleV2(authDTO, scheduleDTO);

			/** Combine schedule bus and it's overrides */
			ScheduleBusOverrideDTO scheduleBusOverrideDTO = new ScheduleBusOverrideDTO();
			scheduleBusOverrideDTO.setBus(scheduleBusDTO.getBus());
			scheduleBusOverrides.add(scheduleBusOverrideDTO);

			/** Schedule Stages */
			List<ScheduleStageDTO> scheduleStages = scheduleStageService.get(authDTO, scheduleDTO);
			Map<String, ScheduleStageDTO> stagesMap = new HashMap<>();
			for (ScheduleStageDTO stage : scheduleStages) {
				stagesMap.put(stage.getFromStation().getCode() + "_" + stage.getToStation().getCode(), stage);
			}

			scheduleStages = new ArrayList<>(stagesMap.values());

			for (ScheduleStageDTO scheduleStageDTO : scheduleStages) {
				StationDTO fromStationDTO = stationService.getStation(scheduleStageDTO.getFromStation());
				StationDTO toStationDTO = stationService.getStation(scheduleStageDTO.getToStation());

				/** Fare rule details based on route */
				FareRuleDetailsDTO fareRuleDetailsDTO = getFareRuleDetailsV2(authDTO, fareRuleList, fromStationDTO, toStationDTO);

				JSONArray busArray = new JSONArray();
				for (ScheduleBusOverrideDTO scheduleBusOverride : scheduleBusOverrides) {
					BusDTO busDTO = scheduleBusOverride.getBus();
					/** Bus */
					busDTO = busService.getBus(authDTO, busDTO);

					/** Convert minimum & maximum fare */
					JSONArray stageFares = convertFareRuleDetails(authDTO, fareRuleDetailsDTO, busDTO);

					JSONObject bus = new JSONObject();
					bus.put("code", busDTO.getCode());
					bus.put("name", busDTO.getName());
					bus.put("activeFrom", scheduleBusOverride.getActiveFrom());
					bus.put("activeTo", scheduleBusOverride.getActiveTo());
					bus.put("stageFares", stageFares);
					busArray.add(bus);
				}

				JSONObject stage = new JSONObject();

				JSONObject fromStation = new JSONObject();
				fromStation.put("code", fromStationDTO.getCode());
				fromStation.put("name", fromStationDTO.getName());
				stage.put("fromStation", fromStation);

				JSONObject toStation = new JSONObject();
				toStation.put("code", toStationDTO.getCode());
				toStation.put("name", toStationDTO.getName());
				stage.put("toStation", toStation);

				stage.put("distance", fareRuleDetailsDTO.getDistance());
				stage.put("buses", busArray);
				stages.add(stage);
			}
		}
		return stages;
	}

	@Override
	public JSONArray getLowFareStages(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		JSONArray stages = new JSONArray();
		try {
			/** Fare rule should be enabled */
			List<FareRuleDTO> fareRuleList = authDTO.getNamespace().getProfile().getFareRule();

			if (!fareRuleList.isEmpty()) {
				/** Schedule Details */
				scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);

				/** Schedule Stages */
				List<ScheduleStageDTO> scheduleStages = scheduleStageService.getScheduleStageV2(authDTO, scheduleDTO);

				/** Schedule Bus */
				ScheduleBusDTO scheduleBusDTO = scheduleBusService.getByScheduleId(authDTO, scheduleDTO);
				scheduleBusDTO.setBus(busService.getBus(authDTO, scheduleBusDTO.getBus()));

				Map<String, BusSeatTypeEM> scheduleBustype = scheduleBusDTO.getBus().getUniqueReservableBusType();
				Map<String, ScheduleStageDTO> activeStagesMap = new HashMap<>();
				for (ScheduleStageDTO scheduleStage : scheduleStages) {
					scheduleStage.setFromStation(stationService.getStation(scheduleStage.getFromStation()));
					scheduleStage.setToStation(stationService.getStation(scheduleStage.getToStation()));
					activeStagesMap.put(scheduleStage.getFromStation().getId() + "-" + scheduleStage.getToStation().getId(), scheduleStage);

					JSONObject stage = new JSONObject();
					JSONObject fromStation = new JSONObject();
					fromStation.put("code", scheduleStage.getFromStation().getCode());
					fromStation.put("name", scheduleStage.getFromStation().getName());

					JSONObject toStation = new JSONObject();
					toStation.put("code", scheduleStage.getToStation().getCode());
					toStation.put("name", scheduleStage.getToStation().getName());

					stage.put("fromStation", fromStation);
					stage.put("toStation", toStation);

					JSONArray buses = new JSONArray();
					JSONObject bus = new JSONObject();
					bus.put("busType", busService.getBusCategoryByCode(scheduleBusDTO.getBus().getCategoryCode()));
					bus.put("type", "ROUTE");
					bus.put("code", scheduleBusDTO.getBus().getCode());
					bus.put("name", scheduleBusDTO.getBus().getName());

					/** Fare rule details based on route */
					FareRuleDetailsDTO fareRuleDetailsDTO = getFareRuleDetailsV2(authDTO, fareRuleList, scheduleStage.getFromStation(), scheduleStage.getToStation());
					if (!fareRuleDetailsDTO.isValid()) {
						continue;
					}

					JSONArray stageFares = new JSONArray();
					if (scheduleStage.getBusSeatTypeFare() != null && !scheduleStage.getBusSeatTypeFare().isEmpty()) {
						for (BusSeatTypeFareDTO busSeatTypeFareDTO : scheduleStage.getBusSeatTypeFare()) {
							if (scheduleBustype.get(busSeatTypeFareDTO.getBusSeatType().getCode()) == null) {
								continue;
							}
							StageFareDTO stageFareDTO = new StageFareDTO();
							stageFareDTO.setBusSeatType(busSeatTypeFareDTO.getBusSeatType());
							stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, scheduleBusDTO.getBus(), fareRuleDetailsDTO);

							if (stageFareDTO.getMinFare().compareTo(busSeatTypeFareDTO.getFare()) > 0) {
								JSONObject data = new JSONObject();
								data.put("seatTypeCode", busSeatTypeFareDTO.getBusSeatType().getCode());
								data.put("seatTypeName", busSeatTypeFareDTO.getBusSeatType().getName());
								data.put("minFare", stageFareDTO.getMinFare());
								data.put("fare", busSeatTypeFareDTO.getFare());
								stageFares.add(data);
							}
						}
					}
					else if (scheduleStage.getBusSeatType() != null && scheduleStage.getFare() > 0 && scheduleBustype.get(scheduleStage.getBusSeatType().getCode()) != null) {
						StageFareDTO stageFareDTO = new StageFareDTO();
						stageFareDTO.setBusSeatType(scheduleStage.getBusSeatType());
						stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, scheduleBusDTO.getBus(), fareRuleDetailsDTO);

						if (stageFareDTO.getMinFare().compareTo(BigDecimal.valueOf(scheduleStage.getFare())) > 0) {
							scheduleStage.setFare(stageFareDTO.getMinFare().doubleValue());
							JSONObject data = new JSONObject();
							data.put("seatTypeCode", scheduleStage.getBusSeatType().getCode());
							data.put("seatTypeName", scheduleStage.getBusSeatType().getName());
							data.put("minFare", stageFareDTO.getMinFare());
							data.put("fare", scheduleStage.getFare());
							stageFares.add(data);
						}
					}

					if (!stageFares.isEmpty()) {
						bus.put("stageFares", stageFares);
						buses.add(bus);
						stage.put("buses", buses);
						stages.add(stage);
					}
				}

				/** Upcoming Schedule Bus Overrides */
				scheduleDTO.setTripDate(DateUtil.getDateTime(DateUtil.convertDate(DateUtil.NOW())));
				List<ScheduleBusOverrideDTO> scheduleBusOverrides = busOverrideService.getUpcomingBusOverrides(authDTO, scheduleDTO);

				Map<String, String> scheduleBusTripDates = new HashMap<>();
				for (ScheduleBusOverrideDTO scheduleBusOverride : scheduleBusOverrides) {
					List<DateTime> activeDates = scheduleBusOverride.getTripDateTimes();
					List<DateTime> tripDates = new ArrayList<>();
					for (DateTime tripDate : activeDates) {
						if (tripDate.getStartOfDay().compareTo(DateUtil.NOW().getStartOfDay()) >= 0) {
							tripDates.add(tripDate);
						}
					}
					if (tripDates.size() > 30) {
						tripDates = StreamUtil.limitDateTime(tripDates.stream(), 30);
					}

					for (DateTime tripDate : tripDates) {
						scheduleBusTripDates.put(DateUtil.convertDate(tripDate), DateUtil.convertDate(tripDate));
					}
				}

				ScheduleBusOverrideDTO scheduleBusOverrideDTO = new ScheduleBusOverrideDTO();
				scheduleBusOverrideDTO.setActiveFrom(DateUtil.convertDate(DateUtil.NOW()));
				scheduleBusOverrideDTO.setActiveTo(DateUtil.convertDate(DateUtil.addDaysToDate(DateUtil.NOW(), 30)));
				scheduleBusOverrideDTO.setDayOfWeek("1111111");
				scheduleBusOverrideDTO.setBus(scheduleBusDTO.getBus());
				scheduleBusOverrideDTO.setActiveFlag(-1);
				scheduleBusOverrides.add(scheduleBusOverrideDTO);

				TripHelperServiceImpl tripHelperServiceImpl = new TripHelperServiceImpl();

				for (ScheduleBusOverrideDTO scheduleBusOverride : scheduleBusOverrides) {
					scheduleBusOverride.setBus(busService.getBus(authDTO, scheduleBusOverride.getBus()));

					Map<String, BusSeatTypeEM> bustype = scheduleBusOverride.getBus().getUniqueReservableBusType();
					List<DateTime> tripDates = scheduleBusOverride.getTripDateTimes();
					if (tripDates.size() > 30) {
						tripDates = StreamUtil.limitDateTime(tripDates.stream(), 30);
					}

					for (DateTime tripDate : tripDates) {
						TripDTO tripDTO = new TripDTO();
						tripDTO.setTripDate(tripDate);
						scheduleDTO.setTripDate(tripDate);

						if (DateUtil.NOW().getStartOfDay().compareTo(tripDate.getStartOfDay()) > 0) {
							continue;
						}
						if (scheduleBusOverride.getActiveFlag() == -1 && StringUtil.isNotNull(scheduleBusTripDates.get(DateUtil.convertDate(tripDate)))) {
							continue;
						}

						/** Quick fares */
						List<ScheduleTripStageFareDTO> scheduleTripStageFares = scheduleTripStageFareService.getScheduleTripStageFare(authDTO, scheduleDTO);
						if (scheduleTripStageFares.isEmpty()) {
							ScheduleTripStageFareDTO saveTripFare = new ScheduleTripStageFareDTO();
							saveTripFare.setCode(tripHelperServiceImpl.getGeneratedTripCodeV2(authDTO, scheduleDTO, tripDTO));
							saveTripFare.setSchedule(scheduleDTO);
							saveTripFare.setActiveFlag(1);

							List<ScheduleTripStageFareDTO> saveTripStageFares = new ArrayList<>();
							for (ScheduleStageDTO scheduleStage : scheduleStages) {
								/** Fare rule details based on route */
								FareRuleDetailsDTO fareRuleDetailsDTO = getFareRuleDetailsV2(authDTO, fareRuleList, scheduleStage.getFromStation(), scheduleStage.getToStation());
								if (!fareRuleDetailsDTO.isValid()) {
									continue;
								}

								ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
								quickFareOverrideDTO.setCode(tripHelperServiceImpl.getGeneratedTripCodeV2(authDTO, scheduleDTO, tripDTO));
								quickFareOverrideDTO.setSchedule(scheduleDTO);
								quickFareOverrideDTO.setActiveFlag(1);

								RouteDTO routeDTO = new RouteDTO();
								routeDTO.setFromStation(scheduleStage.getFromStation());
								routeDTO.setToStation(scheduleStage.getToStation());

								boolean isFareChanged = false;
								List<StageFareDTO> busSeatTypeFare = new ArrayList<StageFareDTO>();
								if (scheduleStage.getBusSeatTypeFare() != null && !scheduleStage.getBusSeatTypeFare().isEmpty()) {
									for (BusSeatTypeEM busSeatTypeEM : new ArrayList<>(bustype.values())) {
										StageFareDTO stageFareDTO = new StageFareDTO();
										stageFareDTO.setBusSeatType(busSeatTypeEM);
										stageFareDTO.setFare(BigDecimal.ZERO);
										stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, scheduleBusOverride.getBus(), fareRuleDetailsDTO);

										/**
										 * Apply route fare, if fare with in a
										 * range
										 */
										for (BusSeatTypeFareDTO busSeatTypeFareDTO : scheduleStage.getBusSeatTypeFare()) {
											if (busSeatTypeFareDTO.getBusSeatType().getId() == busSeatTypeEM.getId()) {
												if (busSeatTypeFareDTO.getFare().compareTo(stageFareDTO.getMinFare()) >= 0) {
													stageFareDTO.setFare(busSeatTypeFareDTO.getFare());
													break;
												}
											}
										}

										if (stageFareDTO.getFare().compareTo(stageFareDTO.getMinFare()) >= 0) {
											isFareChanged = false;
										}
										busSeatTypeFare.add(stageFareDTO);
									}
									routeDTO.setStageFare(busSeatTypeFare);
								}
								else if (scheduleStage.getBusSeatType() != null && scheduleStage.getFare() > 0) {
									// TODO
								}
								quickFareOverrideDTO.setRoute(routeDTO);

								/** Save quick fare */
								if (isFareChanged) {
									saveTripStageFares.add(quickFareOverrideDTO);
								}
							}

							if (!saveTripStageFares.isEmpty()) {
								composeJSON(saveTripStageFares, stages, scheduleBusDTO, scheduleBusOverride, tripDate);
							}
						}
						else {
							ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
							quickFareOverrideDTO.setSchedule(scheduleDTO);
							quickFareOverrideDTO.setActiveFlag(1);

							List<ScheduleTripStageFareDTO> saveTripStageFares = new ArrayList<>();
							for (ScheduleTripStageFareDTO scheduleTripStageFareDTO : scheduleTripStageFares) {
								if (activeStagesMap.get(scheduleTripStageFareDTO.getRoute().getFromStation().getId() + "-" + scheduleTripStageFareDTO.getRoute().getToStation().getId()) == null) {
									continue;
								}
								boolean isFareChanged = false;
								quickFareOverrideDTO.setCode(scheduleTripStageFareDTO.getCode());
								/** Fare rule details based on route */
								FareRuleDetailsDTO fareRuleDetailsDTO = getFareRuleDetailsV2(authDTO, fareRuleList, scheduleTripStageFareDTO.getRoute().getFromStation(), scheduleTripStageFareDTO.getRoute().getToStation());
								if (!fareRuleDetailsDTO.isValid()) {
									continue;
								}

								List<StageFareDTO> busSeatTypeFare = new ArrayList<StageFareDTO>();
								for (BusSeatTypeEM busSeatTypeEM : new ArrayList<>(bustype.values())) {
									StageFareDTO stageFareDTO = new StageFareDTO();
									stageFareDTO.setBusSeatType(busSeatTypeEM);
									stageFareDTO.setFare(BigDecimal.ZERO);
									stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, scheduleBusOverride.getBus(), fareRuleDetailsDTO);

									for (StageFareDTO stageFare : scheduleTripStageFareDTO.getRoute().getStageFare()) {
										if (stageFare.getBusSeatType().getId() == busSeatTypeEM.getId()) {
											stageFareDTO.setFare(stageFare.getFare());
											break;
										}
									}
									if (stageFareDTO.getMinFare().compareTo(stageFareDTO.getFare()) > 0) {
										isFareChanged = true;
										busSeatTypeFare.add(stageFareDTO);
									}
								}
								if (isFareChanged) {
									scheduleTripStageFareDTO.getRoute().setStageFare(busSeatTypeFare);
									saveTripStageFares.add(scheduleTripStageFareDTO);
								}
							}
							if (!saveTripStageFares.isEmpty()) {
								composeJSON(saveTripStageFares, stages, scheduleBusDTO, scheduleBusOverride, tripDate);
							}
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return stages;
	}

	private void composeJSON(List<ScheduleTripStageFareDTO> saveTripStageFares, JSONArray stages, ScheduleBusDTO scheduleBusDTO, ScheduleBusOverrideDTO scheduleBusOverride, DateTime tripDate) {
		for (ScheduleTripStageFareDTO scheduleTripStageFareDTO : saveTripStageFares) {
			scheduleTripStageFareDTO.getRoute().setFromStation(stationService.getStation(scheduleTripStageFareDTO.getRoute().getFromStation()));
			scheduleTripStageFareDTO.getRoute().setToStation(stationService.getStation(scheduleTripStageFareDTO.getRoute().getToStation()));

			boolean isExist = false;
			for (Object stageObj : stages) {
				JSONObject stageJSON = (JSONObject) stageObj;

				if (stageJSON.getJSONObject("fromStation").get("code").equals(scheduleTripStageFareDTO.getRoute().getFromStation().getCode()) && stageJSON.getJSONObject("toStation").get("code").equals(scheduleTripStageFareDTO.getRoute().getToStation().getCode())) {
					isExist = true;
				}
			}

			if (isExist) {
				for (Object stageObj : stages) {
					JSONObject stageJSON = (JSONObject) stageObj;
					if (!stageJSON.getJSONObject("fromStation").get("code").equals(scheduleTripStageFareDTO.getRoute().getFromStation().getCode()) || !stageJSON.getJSONObject("toStation").get("code").equals(scheduleTripStageFareDTO.getRoute().getToStation().getCode())) {
						continue;
					}
					JSONArray buses = stageJSON.getJSONArray("buses");

					JSONObject bus = new JSONObject();
					bus.put("busType", busService.getBusCategoryByCode(scheduleBusDTO.getBus().getCategoryCode()));
					bus.put("type", "BUS_TYPE_OVERRIDE");
					bus.put("code", scheduleBusDTO.getBus().getCode());
					bus.put("name", scheduleBusDTO.getBus().getName());
					bus.put("overrideBusType", busService.getBusCategoryByCode(scheduleBusOverride.getBus().getCategoryCode()));
					bus.put("fromDate", DateUtil.convertDate(tripDate));

					JSONArray stageFares = new JSONArray();
					for (StageFareDTO stageFareDTO : scheduleTripStageFareDTO.getRoute().getStageFare()) {
						if (stageFareDTO.getMinFare().compareTo(stageFareDTO.getFare()) == 0) {
							continue;
						}
						JSONObject data = new JSONObject();
						data.put("seatTypeCode", stageFareDTO.getBusSeatType().getCode());
						data.put("seatTypeName", stageFareDTO.getBusSeatType().getName());
						data.put("minFare", stageFareDTO.getMinFare());
						data.put("fare", stageFareDTO.getFare());
						stageFares.add(data);
					}

					if (!stageFares.isEmpty()) {
						bus.put("stageFares", stageFares);
						buses.add(bus);
						stageJSON.put("buses", buses);
					}
				}
			}
			else {
				JSONObject stage = new JSONObject();
				JSONObject fromStation = new JSONObject();
				fromStation.put("code", scheduleTripStageFareDTO.getRoute().getFromStation().getCode());
				fromStation.put("name", scheduleTripStageFareDTO.getRoute().getFromStation().getName());

				JSONObject toStation = new JSONObject();
				toStation.put("code", scheduleTripStageFareDTO.getRoute().getToStation().getCode());
				toStation.put("name", scheduleTripStageFareDTO.getRoute().getToStation().getName());

				stage.put("fromStation", fromStation);
				stage.put("toStation", toStation);

				JSONArray buses = new JSONArray();
				JSONObject bus = new JSONObject();
				bus.put("busType", busService.getBusCategoryByCode(scheduleBusDTO.getBus().getCategoryCode()));
				bus.put("type", "BUS_TYPE_OVERRIDE");
				bus.put("code", scheduleBusDTO.getBus().getCode());
				bus.put("name", scheduleBusDTO.getBus().getName());
				bus.put("overrideBusType", busService.getBusCategoryByCode(scheduleBusOverride.getBus().getCategoryCode()));
				bus.put("fromDate", DateUtil.convertDate(tripDate));

				JSONArray stageFares = new JSONArray();
				for (StageFareDTO stageFareDTO : scheduleTripStageFareDTO.getRoute().getStageFare()) {
					if (stageFareDTO.getMinFare().compareTo(stageFareDTO.getFare()) == 0) {
						continue;
					}
					JSONObject data = new JSONObject();
					data.put("seatTypeCode", stageFareDTO.getBusSeatType().getCode());
					data.put("seatTypeName", stageFareDTO.getBusSeatType().getName());
					data.put("minFare", stageFareDTO.getMinFare());
					data.put("fare", stageFareDTO.getFare());
					stageFares.add(data);
				}

				if (!stageFares.isEmpty()) {
					bus.put("stageFares", stageFares);
					buses.add(bus);
					stage.put("buses", buses);
					stages.add(stage);
				}
			}
		}
	}

	public void applyFareRulesInStages(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		try {
			/** Fare rule should be enabled */
			List<FareRuleDTO> fareRuleList = authDTO.getNamespace().getProfile().getFareRule();

			if (!fareRuleList.isEmpty()) {
				/** Schedule Details */
				scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);

				/** Schedule Stages */
				List<ScheduleStageDTO> scheduleStages = scheduleStageService.getScheduleStageV2(authDTO, scheduleDTO);

				/** Schedule Bus */
				ScheduleBusDTO scheduleBusDTO = scheduleBusService.getByScheduleId(authDTO, scheduleDTO);
				scheduleBusDTO.setBus(busService.getBus(authDTO, scheduleBusDTO.getBus()));

				Map<String, BusSeatTypeEM> scheduleBustype = scheduleBusDTO.getBus().getUniqueReservableBusType();
				Map<String, ScheduleStageDTO> activeStagesMap = new HashMap<>();
				for (ScheduleStageDTO scheduleStage : scheduleStages) {
					scheduleStage.setFromStation(stationService.getStation(scheduleStage.getFromStation()));
					scheduleStage.setToStation(stationService.getStation(scheduleStage.getToStation()));
					activeStagesMap.put(scheduleStage.getFromStation().getId() + "-" + scheduleStage.getToStation().getId(), scheduleStage);

					/** Fare rule details based on route */
					FareRuleDetailsDTO fareRuleDetailsDTO = getFareRuleDetailsV2(authDTO, fareRuleList, scheduleStage.getFromStation(), scheduleStage.getToStation());
					if (!fareRuleDetailsDTO.isValid()) {
						continue;
					}

					if (scheduleStage.getBusSeatTypeFare() != null && !scheduleStage.getBusSeatTypeFare().isEmpty()) {
						for (BusSeatTypeFareDTO busSeatTypeFareDTO : scheduleStage.getBusSeatTypeFare()) {
							if (scheduleBustype.get(busSeatTypeFareDTO.getBusSeatType().getCode()) == null) {
								continue;
							}
							StageFareDTO stageFareDTO = new StageFareDTO();
							stageFareDTO.setBusSeatType(busSeatTypeFareDTO.getBusSeatType());
							stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, scheduleBusDTO.getBus(), fareRuleDetailsDTO);

							if (stageFareDTO.getMinFare().compareTo(busSeatTypeFareDTO.getFare()) > 0) {
								busSeatTypeFareDTO.setFare(stageFareDTO.getMinFare());
								scheduleStage.setActiveFlag(-1);
							}
						}
					}
					else if (scheduleStage.getBusSeatType() != null && scheduleStage.getFare() > 0 && scheduleBustype.get(scheduleStage.getBusSeatType().getCode()) != null) {
						StageFareDTO stageFareDTO = new StageFareDTO();
						stageFareDTO.setBusSeatType(scheduleStage.getBusSeatType());
						stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, scheduleBusDTO.getBus(), fareRuleDetailsDTO);

						if (stageFareDTO.getMinFare().compareTo(BigDecimal.valueOf(scheduleStage.getFare())) > 0) {
							scheduleStage.setFare(stageFareDTO.getMinFare().doubleValue());
							scheduleStage.setActiveFlag(-1);
						}
					}
				}

				/** Save changed stages */
				List<ScheduleStageDTO> scheduleStageList = new ArrayList<ScheduleStageDTO>();
				for (ScheduleStageDTO scheduleStage : scheduleStages) {
					if (scheduleStage.getActiveFlag() != -1) {
						continue;
					}
					scheduleStage.setActiveFlag(1);
					scheduleStageList.add(scheduleStage);
				}
				checkAndUpdateStages(authDTO, scheduleStageList);

				/** Upcoming Schedule Bus Overrides */
				scheduleDTO.setTripDate(DateUtil.getDateTime(DateUtil.convertDate(DateUtil.NOW())));
				List<ScheduleBusOverrideDTO> scheduleBusOverrides = busOverrideService.getUpcomingBusOverrides(authDTO, scheduleDTO);

				Map<String, String> scheduleBusTripDates = new HashMap<>();
				for (ScheduleBusOverrideDTO scheduleBusOverride : scheduleBusOverrides) {
					List<DateTime> activeDates = scheduleBusOverride.getTripDateTimes();
					List<DateTime> tripDates = new ArrayList<>();
					for (DateTime tripDate : activeDates) {
						if (tripDate.getStartOfDay().compareTo(DateUtil.NOW().getStartOfDay()) >= 0) {
							tripDates.add(tripDate);
						}
					}
					if (tripDates.size() > 30) {
						tripDates = StreamUtil.limitDateTime(tripDates.stream(), 30);
					}

					for (DateTime tripDate : tripDates) {
						scheduleBusTripDates.put(DateUtil.convertDate(tripDate), DateUtil.convertDate(tripDate));
					}
				}

				ScheduleBusOverrideDTO scheduleBusOverrideDTO = new ScheduleBusOverrideDTO();
				scheduleBusOverrideDTO.setActiveFrom(DateUtil.convertDate(DateUtil.NOW()));
				scheduleBusOverrideDTO.setActiveTo(DateUtil.convertDate(DateUtil.addDaysToDate(DateUtil.NOW(), 30)));
				scheduleBusOverrideDTO.setDayOfWeek("1111111");
				scheduleBusOverrideDTO.setBus(scheduleBusDTO.getBus());
				scheduleBusOverrideDTO.setActiveFlag(-1);
				scheduleBusOverrides.add(scheduleBusOverrideDTO);

				TripHelperServiceImpl tripHelperServiceImpl = new TripHelperServiceImpl();

				for (ScheduleBusOverrideDTO scheduleBusOverride : scheduleBusOverrides) {
					scheduleBusOverride.setBus(busService.getBus(authDTO, scheduleBusOverride.getBus()));

					Map<String, BusSeatTypeEM> bustype = scheduleBusOverride.getBus().getUniqueReservableBusType();
					List<DateTime> tripDates = scheduleBusOverride.getTripDateTimes();
					if (tripDates.size() > 30) {
						tripDates = StreamUtil.limitDateTime(tripDates.stream(), 30);
					}

					for (DateTime tripDate : tripDates) {
						TripDTO tripDTO = new TripDTO();
						tripDTO.setTripDate(tripDate);
						scheduleDTO.setTripDate(tripDate);
						if (DateUtil.NOW().getStartOfDay().compareTo(tripDate.getStartOfDay()) > 0) {
							continue;
						}
						if (scheduleBusOverride.getActiveFlag() == -1 && StringUtil.isNotNull(scheduleBusTripDates.get(DateUtil.convertDate(tripDate)))) {
							continue;
						}

						/** Quick fares */
						List<ScheduleTripStageFareDTO> scheduleTripStageFares = scheduleTripStageFareService.getScheduleTripStageFare(authDTO, scheduleDTO);
						if (scheduleTripStageFares.isEmpty()) {
							ScheduleTripStageFareDTO saveTripFare = new ScheduleTripStageFareDTO();
							saveTripFare.setCode(tripHelperServiceImpl.getGeneratedTripCodeV2(authDTO, scheduleDTO, tripDTO));
							saveTripFare.setSchedule(scheduleDTO);
							saveTripFare.setActiveFlag(1);

							List<ScheduleTripStageFareDTO> saveTripStageFares = new ArrayList<>();
							for (ScheduleStageDTO scheduleStage : scheduleStages) {
								/** Fare rule details based on route */
								FareRuleDetailsDTO fareRuleDetailsDTO = getFareRuleDetailsV2(authDTO, fareRuleList, scheduleStage.getFromStation(), scheduleStage.getToStation());
								if (!fareRuleDetailsDTO.isValid()) {
									continue;
								}

								ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
								quickFareOverrideDTO.setCode(tripHelperServiceImpl.getGeneratedTripCodeV2(authDTO, scheduleDTO, tripDTO));
								quickFareOverrideDTO.setSchedule(scheduleDTO);
								quickFareOverrideDTO.setActiveFlag(1);

								RouteDTO routeDTO = new RouteDTO();
								routeDTO.setFromStation(scheduleStage.getFromStation());
								routeDTO.setToStation(scheduleStage.getToStation());

								boolean isFareChanged = false;
								List<StageFareDTO> busSeatTypeFare = new ArrayList<StageFareDTO>();
								if (scheduleStage.getBusSeatTypeFare() != null && !scheduleStage.getBusSeatTypeFare().isEmpty()) {
									for (BusSeatTypeEM busSeatTypeEM : new ArrayList<>(bustype.values())) {
										StageFareDTO stageFareDTO = new StageFareDTO();
										stageFareDTO.setBusSeatType(busSeatTypeEM);
										stageFareDTO.setFare(BigDecimal.ZERO);
										stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, scheduleBusOverride.getBus(), fareRuleDetailsDTO);

										/**
										 * Apply route fare, if fare with in a
										 * range
										 */
										for (BusSeatTypeFareDTO busSeatTypeFareDTO : scheduleStage.getBusSeatTypeFare()) {
											if (busSeatTypeFareDTO.getBusSeatType().getId() == busSeatTypeEM.getId()) {
												if (busSeatTypeFareDTO.getFare().compareTo(stageFareDTO.getMinFare()) >= 0) {
													stageFareDTO.setFare(busSeatTypeFareDTO.getFare());
													break;
												}
											}
										}

										if (stageFareDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
											stageFareDTO.setFare(stageFareDTO.getMinFare());
											isFareChanged = true;
										}
										busSeatTypeFare.add(stageFareDTO);
									}
									routeDTO.setStageFare(busSeatTypeFare);
								}
								else if (scheduleStage.getBusSeatType() != null && scheduleStage.getFare() > 0) {
									// TODO
								}
								quickFareOverrideDTO.setRoute(routeDTO);

								/** Save quick fare */
								if (isFareChanged) {
									saveTripStageFares.add(quickFareOverrideDTO);
								}
							}

							if (!saveTripStageFares.isEmpty()) {
								saveTripFare.setFareDetails(convertQuickFareDetailsToString(saveTripStageFares));
								scheduleTripStageFareService.updateQuickFare(authDTO, saveTripFare);
							}
						}
						else {
							ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
							quickFareOverrideDTO.setSchedule(scheduleDTO);
							quickFareOverrideDTO.setActiveFlag(1);

							List<ScheduleTripStageFareDTO> saveTripStageFares = new ArrayList<>();
							for (ScheduleTripStageFareDTO scheduleTripStageFareDTO : scheduleTripStageFares) {
								if (activeStagesMap.get(scheduleTripStageFareDTO.getRoute().getFromStation().getId() + "-" + scheduleTripStageFareDTO.getRoute().getToStation().getId()) == null) {
									continue;
								}
								boolean isFareChanged = false;
								quickFareOverrideDTO.setCode(scheduleTripStageFareDTO.getCode());
								/** Fare rule details based on route */
								FareRuleDetailsDTO fareRuleDetailsDTO = getFareRuleDetailsV2(authDTO, fareRuleList, scheduleTripStageFareDTO.getRoute().getFromStation(), scheduleTripStageFareDTO.getRoute().getToStation());
								if (!fareRuleDetailsDTO.isValid()) {
									continue;
								}

								List<StageFareDTO> busSeatTypeFare = new ArrayList<StageFareDTO>();
								for (BusSeatTypeEM busSeatTypeEM : new ArrayList<>(bustype.values())) {
									StageFareDTO stageFareDTO = new StageFareDTO();
									stageFareDTO.setBusSeatType(busSeatTypeEM);
									stageFareDTO.setFare(BigDecimal.ZERO);
									stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, scheduleBusOverride.getBus(), fareRuleDetailsDTO);

									for (StageFareDTO stageFare : scheduleTripStageFareDTO.getRoute().getStageFare()) {
										if (stageFare.getBusSeatType().getId() == busSeatTypeEM.getId()) {
											stageFareDTO.setFare(stageFare.getFare());
											break;
										}
									}
									if (stageFareDTO.getMinFare().compareTo(stageFareDTO.getFare()) > 0) {
										stageFareDTO.setFare(stageFareDTO.getMinFare());
										isFareChanged = true;
									}
									busSeatTypeFare.add(stageFareDTO);
								}
								if (isFareChanged) {
									scheduleTripStageFareDTO.getRoute().setStageFare(busSeatTypeFare);
									saveTripStageFares.add(scheduleTripStageFareDTO);
								}
							}

							/** Save quick fare */
							quickFareOverrideDTO.setFareDetails(convertQuickFareDetailsToString(scheduleTripStageFares));
							scheduleTripStageFareService.updateQuickFare(authDTO, quickFareOverrideDTO);
						}
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void applyChangeOfScheduleBusInStages(AuthDTO authDTO, List<FareRuleDTO> fareRuleList, ScheduleDTO scheduleDTO) {
		try {
			/** Schedule Details */
			scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);

			/** Schedule Stages */
			List<ScheduleStageDTO> scheduleStages = scheduleStageService.getScheduleStageV2(authDTO, scheduleDTO);

			/** Schedule Bus */
			ScheduleBusDTO scheduleBusDTO = scheduleBusService.getByScheduleId(authDTO, scheduleDTO);
			scheduleBusDTO.setBus(busService.getBus(authDTO, scheduleBusDTO.getBus()));

			Map<String, BusSeatTypeEM> bustype = scheduleBusDTO.getBus().getUniqueReservableBusType();

			for (ScheduleStageDTO scheduleStage : scheduleStages) {
				scheduleStage.setFromStation(stationService.getStation(scheduleStage.getFromStation()));
				scheduleStage.setToStation(stationService.getStation(scheduleStage.getToStation()));

				/** Fare rule details based on route */
				FareRuleDetailsDTO fareRuleDetailsDTO = getFareRuleDetailsV2(authDTO, fareRuleList, scheduleStage.getFromStation(), scheduleStage.getToStation());
				if (!fareRuleDetailsDTO.isValid()) {
					continue;
				}

				if (scheduleStage.getBusSeatTypeFare() != null && !scheduleStage.getBusSeatTypeFare().isEmpty()) {
					List<BusSeatTypeFareDTO> busSeatTypeFares = new ArrayList<BusSeatTypeFareDTO>();
					for (BusSeatTypeFareDTO busSeatTypeFareDTO : scheduleStage.getBusSeatTypeFare()) {
						if (bustype.get(busSeatTypeFareDTO.getBusSeatType().getCode()) == null) {
							continue;
						}
						StageFareDTO stageFareDTO = new StageFareDTO();
						stageFareDTO.setBusSeatType(busSeatTypeFareDTO.getBusSeatType());
						stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, scheduleBusDTO.getBus(), fareRuleDetailsDTO);

						if (stageFareDTO.getMinFare().compareTo(busSeatTypeFareDTO.getFare()) > 0) {
							busSeatTypeFareDTO.setFare(stageFareDTO.getMinFare());
							busSeatTypeFares.add(busSeatTypeFareDTO);
							bustype.remove(busSeatTypeFareDTO.getBusSeatType().getCode());
						}
						else if (busSeatTypeFareDTO.getFare().compareTo(stageFareDTO.getMinFare()) >= 0) {
							busSeatTypeFares.add(busSeatTypeFareDTO);
							bustype.remove(busSeatTypeFareDTO.getBusSeatType().getCode());
						}
					}

					if (!bustype.isEmpty()) {
						for (BusSeatTypeEM busSeatTypeEM : new ArrayList<>(bustype.values())) {
							StageFareDTO stageFareDTO = new StageFareDTO();
							stageFareDTO.setBusSeatType(busSeatTypeEM);
							stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, scheduleBusDTO.getBus(), fareRuleDetailsDTO);

							BusSeatTypeFareDTO busSeatTypeFareDTO = new BusSeatTypeFareDTO();
							busSeatTypeFareDTO.setBusSeatType(busSeatTypeEM);
							busSeatTypeFareDTO.setFare(stageFareDTO.getMinFare());
							busSeatTypeFares.add(busSeatTypeFareDTO);
						}
					}

					scheduleStage.setBusSeatTypeFare(busSeatTypeFares);
				}
			}

			/** Save changed stages */
			checkAndUpdateStages(authDTO, scheduleStages);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void checkAndUpdateStages(AuthDTO authDTO, List<ScheduleStageDTO> scheduleStages) {
		for (Iterator<ScheduleStageDTO> iterator = scheduleStages.iterator(); iterator.hasNext();) {
			ScheduleStageDTO scheduleStage = iterator.next();

			boolean isValidFare = false;
			for (BusSeatTypeFareDTO busSeatTypeFareDTO : scheduleStage.getBusSeatTypeFare()) {
				if (busSeatTypeFareDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
					isValidFare = true;
					break;
				}
			}
			if (isValidFare || (scheduleStage.getBusSeatTypeFare().isEmpty() && scheduleStage.getFare() == 0)) {
				iterator.remove();
			}
		}

		/** Save changed stages */
		if (!scheduleStages.isEmpty()) {
			ScheduleStageDTO scheduleStageDTO = new ScheduleStageDTO();
			scheduleStageDTO.setList(scheduleStages);
			scheduleStageService.Update(authDTO, scheduleStageDTO);
		}
	}

	public void applyScheduleBusOverrideInQuickFare(AuthDTO authDTO, List<FareRuleDTO> fareRuleList, ScheduleBusOverrideDTO scheduleBusOverride) {
		try {
			/** Schedule Details */
			ScheduleDTO scheduleDTO = scheduleService.getSchedule(authDTO, scheduleBusOverride.getSchedule());
			scheduleBusOverride.setBus(busService.getBus(authDTO, scheduleBusOverride.getBus()));
			Map<String, BusSeatTypeEM> bustype = scheduleBusOverride.getBus().getUniqueReservableBusType();

			/** Schedule Stages */
			List<ScheduleStageDTO> scheduleStages = scheduleStageService.getScheduleStageV2(authDTO, scheduleDTO);

			/** Quick fares */
			List<DateTime> tripDates = scheduleBusOverride.getTripDateTimes();
			if (tripDates.size() > 30) {
				tripDates = StreamUtil.limitDateTime(tripDates.stream(), 30);
			}

			TripHelperServiceImpl tripHelperServiceImpl = new TripHelperServiceImpl();
			for (DateTime tripDate : tripDates) {
				TripDTO tripDTO = new TripDTO();
				tripDTO.setTripDate(tripDate);
				scheduleDTO.setTripDate(tripDate);
				if (DateUtil.NOW().getStartOfDay().compareTo(tripDate.getStartOfDay()) > 0) {
					continue;
				}

				/** Quick fares */
				List<ScheduleTripStageFareDTO> scheduleTripStageFares = scheduleTripStageFareService.getScheduleTripStageFare(authDTO, scheduleDTO);

				if (scheduleTripStageFares.isEmpty()) {
					ScheduleTripStageFareDTO saveTripFare = new ScheduleTripStageFareDTO();
					saveTripFare.setCode(tripHelperServiceImpl.getGeneratedTripCodeV2(authDTO, scheduleDTO, tripDTO));
					saveTripFare.setSchedule(scheduleDTO);
					saveTripFare.setActiveFlag(1);

					List<ScheduleTripStageFareDTO> saveTripStageFares = new ArrayList<>();
					for (ScheduleStageDTO scheduleStage : scheduleStages) {
						/** Fare rule details based on route */
						FareRuleDetailsDTO fareRuleDetailsDTO = getFareRuleDetailsV2(authDTO, fareRuleList, scheduleStage.getFromStation(), scheduleStage.getToStation());
						if (!fareRuleDetailsDTO.isValid()) {
							continue;
						}

						ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
						quickFareOverrideDTO.setCode(tripHelperServiceImpl.getGeneratedTripCodeV2(authDTO, scheduleDTO, tripDTO));
						quickFareOverrideDTO.setSchedule(scheduleDTO);
						quickFareOverrideDTO.setActiveFlag(1);

						RouteDTO routeDTO = new RouteDTO();
						routeDTO.setFromStation(scheduleStage.getFromStation());
						routeDTO.setToStation(scheduleStage.getToStation());

						boolean isFareChanged = false;
						List<StageFareDTO> busSeatTypeFare = new ArrayList<StageFareDTO>();
						if (scheduleStage.getBusSeatTypeFare() != null && !scheduleStage.getBusSeatTypeFare().isEmpty()) {
							for (BusSeatTypeEM busSeatTypeEM : new ArrayList<>(bustype.values())) {
								StageFareDTO stageFareDTO = new StageFareDTO();
								stageFareDTO.setBusSeatType(busSeatTypeEM);
								stageFareDTO.setFare(BigDecimal.ZERO);
								stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, scheduleBusOverride.getBus(), fareRuleDetailsDTO);

								/**
								 * Apply route fare, if fare with in a
								 * range
								 */
								for (BusSeatTypeFareDTO busSeatTypeFareDTO : scheduleStage.getBusSeatTypeFare()) {
									if (busSeatTypeFareDTO.getBusSeatType().getId() == busSeatTypeEM.getId()) {
										if (busSeatTypeFareDTO.getFare().compareTo(stageFareDTO.getMinFare()) >= 0) {
											stageFareDTO.setFare(busSeatTypeFareDTO.getFare());
											break;
										}
									}
								}

								if (stageFareDTO.getFare().compareTo(BigDecimal.ZERO) == 0) {
									stageFareDTO.setFare(stageFareDTO.getMinFare());
									isFareChanged = true;
								}
								busSeatTypeFare.add(stageFareDTO);
							}
							routeDTO.setStageFare(busSeatTypeFare);
						}
						else if (scheduleStage.getBusSeatType() != null && scheduleStage.getFare() > 0) {
							// TODO
						}
						quickFareOverrideDTO.setRoute(routeDTO);

						/** Save quick fare */
						if (isFareChanged) {
							saveTripStageFares.add(quickFareOverrideDTO);
						}
					}

					if (!saveTripStageFares.isEmpty()) {
						saveTripFare.setFareDetails(convertQuickFareDetailsToString(saveTripStageFares));
						scheduleTripStageFareService.updateQuickFare(authDTO, saveTripFare);
					}
				}
				else {
					ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
					quickFareOverrideDTO.setSchedule(scheduleDTO);
					quickFareOverrideDTO.setActiveFlag(1);

					List<ScheduleTripStageFareDTO> saveTripStageFares = new ArrayList<>();
					for (ScheduleTripStageFareDTO scheduleTripStageFareDTO : scheduleTripStageFares) {
						boolean isFareChanged = false;
						quickFareOverrideDTO.setCode(scheduleTripStageFareDTO.getCode());
						/** Fare rule details based on route */
						FareRuleDetailsDTO fareRuleDetailsDTO = getFareRuleDetailsV2(authDTO, fareRuleList, scheduleTripStageFareDTO.getRoute().getFromStation(), scheduleTripStageFareDTO.getRoute().getToStation());
						if (!fareRuleDetailsDTO.isValid()) {
							continue;
						}

						List<StageFareDTO> busSeatTypeFare = new ArrayList<StageFareDTO>();
						for (BusSeatTypeEM busSeatTypeEM : new ArrayList<>(bustype.values())) {
							StageFareDTO stageFareDTO = new StageFareDTO();
							stageFareDTO.setBusSeatType(busSeatTypeEM);
							stageFareDTO.setFare(BigDecimal.ZERO);
							stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, scheduleBusOverride.getBus(), fareRuleDetailsDTO);

							BigDecimal routeFare = BigDecimal.ZERO;
							for (ScheduleStageDTO scheduleStage : scheduleStages) {
								if (scheduleTripStageFareDTO.getRoute().getFromStation().getId() == scheduleStage.getFromStation().getId() && scheduleTripStageFareDTO.getRoute().getToStation().getId() == scheduleStage.getToStation().getId()) {
									for (BusSeatTypeFareDTO busSeatTypeFareDTO : scheduleStage.getBusSeatTypeFare()) {
										if (busSeatTypeFareDTO.getBusSeatType().getId() == busSeatTypeEM.getId() && busSeatTypeFareDTO.getFare().compareTo(stageFareDTO.getMinFare()) >= 0) {
											routeFare = busSeatTypeFareDTO.getFare();
											break;
										}
									}
									break;
								}
							}

							for (StageFareDTO stageFare : scheduleTripStageFareDTO.getRoute().getStageFare()) {
								if (stageFare.getBusSeatType().getId() == busSeatTypeEM.getId()) {
									stageFareDTO.setFare(stageFare.getFare());
									break;
								}
							}
							if (stageFareDTO.getFare().compareTo(stageFareDTO.getMinFare()) < 0) {
								stageFareDTO.setFare(stageFareDTO.getMinFare());
								isFareChanged = true;
								if (routeFare.compareTo(stageFareDTO.getMinFare()) > 0) {
									stageFareDTO.setFare(routeFare);
								}
							}
							busSeatTypeFare.add(stageFareDTO);
						}
						if (isFareChanged) {
							scheduleTripStageFareDTO.getRoute().setStageFare(busSeatTypeFare);
							saveTripStageFares.add(scheduleTripStageFareDTO);
						}
					}

					/** Save quick fare */
					if (!saveTripStageFares.isEmpty()) {
						quickFareOverrideDTO.setFareDetails(convertQuickFareDetailsToString(scheduleTripStageFares));
						scheduleTripStageFareService.updateQuickFare(authDTO, quickFareOverrideDTO);
					}
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private JSONArray convertFareRuleDetails(AuthDTO authDTO, FareRuleDetailsDTO fareRuleDetailsDTO, BusDTO busDTO) {
		JSONArray stageFares = new JSONArray();
		Map<String, BusSeatTypeEM> seatTypeEM = busDTO.getUniqueBusType();
		for (BusSeatTypeEM busSeatType : new ArrayList<BusSeatTypeEM>(seatTypeEM.values())) {
			JSONObject stageFare = new JSONObject();

			JSONObject busSeatType1 = new JSONObject();
			busSeatType1.put("code", busSeatType.getCode());
			busSeatType1.put("name", busSeatType.getName());
			stageFare.put("busSeatType", busSeatType1);

			StageFareDTO stageFareDTO = new StageFareDTO();
			stageFareDTO.setBusSeatType(busSeatType);
			stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, busDTO, fareRuleDetailsDTO);

			stageFare.put("minFare", stageFareDTO.getMinFare());
			stageFare.put("maxFare", stageFareDTO.getMaxFare());

			stageFares.add(stageFare);
		}
		return stageFares;
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
	public FareRuleDTO getFareRuleDetailsByFareRule(AuthDTO authDTO, FareRuleDTO fareRule, StationDTO fromStation, StationDTO toStation) {
		FareRuleDAO fareRuleDAO = new FareRuleDAO();

		if (StringUtil.isNotNull(fromStation.getCode())) {
			fromStation = stationService.getStation(fromStation);
		}
		if (StringUtil.isNotNull(toStation.getCode())) {
			toStation = stationService.getStation(toStation);
		}
		FareRuleDTO fareRuleDTO = fareRuleDAO.getFareRuleDetailsByFareRule(authDTO, fareRule, fromStation, toStation);
		for (FareRuleDetailsDTO fareRuleDetailsDTO : fareRuleDTO.getFareRuleDetails()) {
			fareRuleDetailsDTO.setFromStation(stationService.getStation(fareRuleDetailsDTO.getFromStation()));
			fareRuleDetailsDTO.setToStation(stationService.getStation(fareRuleDetailsDTO.getToStation()));
		}
		return fareRuleDTO;
	}

	@Override
	public FareRuleDetailsDTO getStageFareRuleDetails(AuthDTO authDTO, List<FareRuleDTO> fareRuleList, StationDTO fromStation, StationDTO toStation) {
		if (fromStation.getId() == 0) {
			fromStation = stationService.getStation(fromStation);
		}
		if (toStation.getId() == 0) {
			toStation = stationService.getStation(toStation);
		}
		return getFareRuleDetailsV2(authDTO, fareRuleList, fromStation, toStation);
	}

	@Override
	public FareRuleDetailsDTO getFareRuleDetails(AuthDTO authDTO, List<FareRuleDTO> fareRuleList, StationDTO fromStationDTO, StationDTO toStationDTO) {
		return getFareRuleDetailsV2(authDTO, fareRuleList, fromStationDTO, toStationDTO);
	}

	@Override
	public StageDTO getFareRuleByRoute(AuthDTO authDTO, List<FareRuleDTO> fareRuleList, StationDTO fromStationDTO, StationDTO toStationDTO, BusDTO busDTO) {
		StageDTO stageDTO = new StageDTO();
		List<StageFareDTO> stageFareList = new ArrayList<StageFareDTO>();

		fromStationDTO = stationService.getStation(fromStationDTO);
		toStationDTO = stationService.getStation(toStationDTO);

		FareRuleDetailsDTO fareRuleDetailsDTO = getFareRuleDetailsV2(authDTO, fareRuleList, fromStationDTO, toStationDTO);
		busDTO = busService.getBus(authDTO, busDTO);

		if (fareRuleDetailsDTO.getId() != Numeric.ZERO_INT && busDTO.getId() != Numeric.ZERO_INT) {
			stageDTO.setDistance(fareRuleDetailsDTO.getDistance());
			Map<String, BusSeatTypeEM> seatTypeEM = busDTO.getUniqueBusType();
			for (BusSeatTypeEM busSeatType : new ArrayList<BusSeatTypeEM>(seatTypeEM.values())) {
				StageFareDTO stageFareDTO = new StageFareDTO();
				stageFareDTO.setBusSeatType(busSeatType);
				stageFareDTO = BitsUtil.applyFareRule(authDTO, stageFareDTO, busDTO, fareRuleDetailsDTO);
				stageFareList.add(stageFareDTO);
			}
		}
		stageDTO.setStageFare(stageFareList);

		return stageDTO;
	}

	@Override
	public void syncVertexFareRule(AuthDTO authDTO, FareRuleDTO fareRule, DateTime lastSyncDate) {
		List<FareRuleDetailsDTO> fareRuleDetails = bitsService.syncVertexFareRule(fareRule.getCode());

		if (!fareRuleDetails.isEmpty()) {
			FareRuleDAO fareRuleDAO = new FareRuleDAO();
			fareRuleDAO.getFareRule(authDTO, fareRule);
			List<FareRuleDetailsDTO> fareDetails = new ArrayList<>();
			for (FareRuleDetailsDTO fareRuleDetailsDTO : fareRuleDetails) {
				try {
					fareRuleDetailsDTO.setFromStation(stationService.getStation(fareRuleDetailsDTO.getFromStation()));
					fareRuleDetailsDTO.setToStation(stationService.getStation(fareRuleDetailsDTO.getToStation()));
				}
				catch (Exception e) {
					continue;
				}
				fareDetails.add(fareRuleDetailsDTO);
			}
			Iterable<List<FareRuleDetailsDTO>> batchFareRules = Iterables.partition(fareDetails, 100);
			authDTO.getAdditionalAttribute().put(Text.FARE_RULE_SYNC_FLAG, Numeric.ONE);
			for (List<FareRuleDetailsDTO> list : batchFareRules) {
				fareRule.setFareRuleDetails(list);
				fareRuleDAO.updateFareRuleDetails(authDTO, fareRule);
			}

		}
	}

	@Override
	public List<FareRuleDetailsDTO> getZoneSyncFareRuleDetails(AuthDTO authDTO, FareRuleDTO fareRule, String syncDate) {
		FareRuleDAO fareRuleDAO = new FareRuleDAO();
		fareRule = getFareRule(authDTO, fareRule);
		List<FareRuleDetailsDTO> fareRuleDetails = fareRuleDAO.getZoneSyncFareRuleDetails(authDTO, fareRule, syncDate);
		for (FareRuleDetailsDTO fareRuleDetailsDTO : fareRuleDetails) {
			// from station
			fareRuleDetailsDTO.setFromStation(stationService.getStation(fareRuleDetailsDTO.getFromStation()));
			// to station
			fareRuleDetailsDTO.setToStation(stationService.getStation(fareRuleDetailsDTO.getToStation()));
		}
		return fareRuleDetails;
	}

	private List<FareRuleDetailsDTO> getFareRuleDetailsV1(AuthDTO authDTO, List<FareRuleDTO> fareRuleList, StationDTO fromStation, StationDTO toStation) {
		List<FareRuleDetailsDTO> finalList = new ArrayList<>();
		Map<String, List<FareRuleDetailsDTO>> fareRuleMap = new HashMap<>();
		FareRuleDAO fareRuleDAO = new FareRuleDAO();
		for (FareRuleDTO fareRule : fareRuleList) {
			/** Fare rule details based on route */
			List<FareRuleDetailsDTO> list = fareRuleDAO.getFareRuleDetails(authDTO, fareRule, fromStation, toStation);
			if (!list.isEmpty()) {
				for (FareRuleDetailsDTO fareRuleDetails : list) {
					String key = fareRuleDetails.getFromStation().getId() + Text.UNDER_SCORE + fareRuleDetails.getToStation().getId();
					if (fareRuleMap.get(key) != null) {
						List<FareRuleDetailsDTO> existList = fareRuleMap.get(key);
						existList.add(fareRuleDetails);
						fareRuleMap.put(key, existList);
					}
					else {
						List<FareRuleDetailsDTO> detailsList = new ArrayList<>();
						detailsList.add(fareRuleDetails);
						fareRuleMap.put(key, detailsList);
					}
				}
			}
		}
		for (Entry<String, List<FareRuleDetailsDTO>> dataMap : fareRuleMap.entrySet()) {
			FareRuleDetailsDTO fareRuleDetails = null;
			if (dataMap.getValue().size() == 1) {
				fareRuleDetails = dataMap.getValue().stream().findFirst().orElse(null);
			}
			else if (dataMap.getValue().size() > 1) {
				fareRuleDetails = getMinFareFareRuelDetails(fareRuleDetails, dataMap.getValue().stream().collect(Collectors.toList()));
			}
			if (fareRuleDetails != null) {
				fareRuleDetails.setFromStation(stationService.getStation(fareRuleDetails.getFromStation()));
				fareRuleDetails.setToStation(stationService.getStation(fareRuleDetails.getToStation()));
				finalList.add(fareRuleDetails);
			}
		}
		return finalList;
	}

	private FareRuleDetailsDTO getFareRuleDetailsV2(AuthDTO authDTO, List<FareRuleDTO> fareRuleList, StationDTO fromStation, StationDTO toStation) {
		Map<Integer, FareRuleDetailsDTO> fareRuleMap = new HashMap<>();
		FareRuleDAO fareRuleDAO = new FareRuleDAO();
		for (FareRuleDTO fareRule : fareRuleList) {
			/** Fare rule details based on route */
			FareRuleDetailsDTO fareRuleDetailsDTO = fareRuleDAO.getFareRuleDetailsV2(authDTO, fareRule, fromStation, toStation);
			if (fareRuleDetailsDTO.getId() != 0) {
				fareRuleMap.put(fareRule.getId(), fareRuleDetailsDTO);
			}
		}

		FareRuleDetailsDTO fareRuleDetails = null;
		if (fareRuleMap.size() == 1) {
			fareRuleDetails = fareRuleMap.values().stream().findFirst().orElse(null);
		}
		else if (fareRuleMap.size() > 1) {
			fareRuleDetails = getMinFareFareRuelDetails(fareRuleDetails, fareRuleMap.values().stream().collect(Collectors.toList()));
		}
		if (fareRuleDetails == null) {
			fareRuleDetails = new FareRuleDetailsDTO();
		}
		return fareRuleDetails;
	}

	private FareRuleDetailsDTO getMinFareFareRuelDetails(FareRuleDetailsDTO fareRuleDetails, List<FareRuleDetailsDTO> list) {
		for (FareRuleDetailsDTO repoFareRuleDetails : list) {
			if (fareRuleDetails == null) {
				fareRuleDetails = repoFareRuleDetails;
			}
			else {
				if (fareRuleDetails.getNonAcSeaterMinFare().compareTo(repoFareRuleDetails.getNonAcSeaterMinFare()) > 0) {
					fareRuleDetails.setNonAcSeaterMinFare(repoFareRuleDetails.getNonAcSeaterMinFare());
				}
				if (fareRuleDetails.getNonAcSeaterMaxFare().compareTo(repoFareRuleDetails.getNonAcSeaterMaxFare()) > 0) {
					fareRuleDetails.setNonAcSeaterMaxFare(repoFareRuleDetails.getNonAcSeaterMaxFare());
				}
				if (fareRuleDetails.getAcSeaterMinFare().compareTo(repoFareRuleDetails.getAcSeaterMinFare()) > 0) {
					fareRuleDetails.setAcSeaterMinFare(repoFareRuleDetails.getAcSeaterMinFare());
				}
				if (fareRuleDetails.getAcSeaterMaxFare().compareTo(repoFareRuleDetails.getAcSeaterMaxFare()) > 0) {
					fareRuleDetails.setAcSeaterMaxFare(repoFareRuleDetails.getAcSeaterMaxFare());
				}
				if (fareRuleDetails.getMultiAxleSeaterMinFare().compareTo(repoFareRuleDetails.getMultiAxleSeaterMinFare()) > 0) {
					fareRuleDetails.setMultiAxleSeaterMinFare(repoFareRuleDetails.getMultiAxleSeaterMinFare());
				}
				if (fareRuleDetails.getMultiAxleSeaterMaxFare().compareTo(repoFareRuleDetails.getMultiAxleSeaterMaxFare()) > 0) {
					fareRuleDetails.setMultiAxleSeaterMaxFare(repoFareRuleDetails.getMultiAxleSeaterMaxFare());
				}
				if (fareRuleDetails.getNonAcSleeperLowerMinFare().compareTo(repoFareRuleDetails.getNonAcSleeperLowerMinFare()) > 0) {
					fareRuleDetails.setNonAcSleeperLowerMinFare(repoFareRuleDetails.getNonAcSleeperLowerMinFare());
				}
				if (fareRuleDetails.getNonAcSleeperLowerMaxFare().compareTo(repoFareRuleDetails.getNonAcSleeperLowerMaxFare()) > 0) {
					fareRuleDetails.setNonAcSleeperLowerMaxFare(repoFareRuleDetails.getNonAcSleeperLowerMaxFare());
				}
				if (fareRuleDetails.getNonAcSleeperUpperMinFare().compareTo(repoFareRuleDetails.getNonAcSleeperUpperMinFare()) > 0) {
					fareRuleDetails.setNonAcSleeperUpperMinFare(repoFareRuleDetails.getNonAcSleeperUpperMinFare());
				}
				if (fareRuleDetails.getNonAcSleeperUpperMaxFare().compareTo(repoFareRuleDetails.getNonAcSleeperUpperMaxFare()) > 0) {
					fareRuleDetails.setNonAcSleeperUpperMaxFare(repoFareRuleDetails.getNonAcSleeperUpperMaxFare());
				}
				if (fareRuleDetails.getAcSleeperLowerMinFare().compareTo(repoFareRuleDetails.getAcSleeperLowerMinFare()) > 0) {
					fareRuleDetails.setAcSleeperLowerMinFare(repoFareRuleDetails.getAcSleeperLowerMinFare());
				}
				if (fareRuleDetails.getAcSleeperLowerMaxFare().compareTo(repoFareRuleDetails.getAcSleeperLowerMaxFare()) > 0) {
					fareRuleDetails.setAcSleeperLowerMaxFare(repoFareRuleDetails.getAcSleeperLowerMaxFare());
				}
				if (fareRuleDetails.getAcSleeperUpperMinFare().compareTo(repoFareRuleDetails.getAcSleeperUpperMinFare()) > 0) {
					fareRuleDetails.setAcSleeperUpperMinFare(repoFareRuleDetails.getAcSleeperUpperMinFare());
				}
				if (fareRuleDetails.getAcSleeperUpperMaxFare().compareTo(repoFareRuleDetails.getAcSleeperUpperMaxFare()) > 0) {
					fareRuleDetails.setAcSleeperUpperMaxFare(repoFareRuleDetails.getAcSleeperUpperMaxFare());
				}
				if (fareRuleDetails.getBrandedAcSleeperMinFare().compareTo(repoFareRuleDetails.getBrandedAcSleeperMinFare()) > 0) {
					fareRuleDetails.setBrandedAcSleeperMinFare(repoFareRuleDetails.getBrandedAcSleeperMinFare());
				}
				if (fareRuleDetails.getBrandedAcSleeperMaxFare().compareTo(repoFareRuleDetails.getBrandedAcSleeperMaxFare()) > 0) {
					fareRuleDetails.setBrandedAcSleeperMaxFare(repoFareRuleDetails.getBrandedAcSleeperMaxFare());
				}
				if (fareRuleDetails.getSingleAxleAcSeaterMinFare().compareTo(repoFareRuleDetails.getSingleAxleAcSeaterMinFare()) > 0) {
					fareRuleDetails.setSingleAxleAcSeaterMinFare(repoFareRuleDetails.getSingleAxleAcSeaterMinFare());
				}
				if (fareRuleDetails.getSingleAxleAcSeaterMaxFare().compareTo(repoFareRuleDetails.getSingleAxleAcSeaterMaxFare()) > 0) {
					fareRuleDetails.setSingleAxleAcSeaterMaxFare(repoFareRuleDetails.getSingleAxleAcSeaterMaxFare());
				}
				if (fareRuleDetails.getMultiAxleAcSleeperMinFare().compareTo(repoFareRuleDetails.getMultiAxleAcSleeperMinFare()) > 0) {
					fareRuleDetails.setMultiAxleAcSleeperMinFare(repoFareRuleDetails.getMultiAxleAcSleeperMinFare());
				}
				if (fareRuleDetails.getMultiAxleAcSleeperMaxFare().compareTo(repoFareRuleDetails.getMultiAxleAcSleeperMaxFare()) > 0) {
					fareRuleDetails.setMultiAxleAcSleeperMaxFare(repoFareRuleDetails.getMultiAxleAcSleeperMaxFare());
				}
			}
		}
		return fareRuleDetails;
	}
}
