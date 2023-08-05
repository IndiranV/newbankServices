package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.aggregator.aws.S3Service;
import org.in.com.aggregator.cargo.CargoService;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusIO;
import org.in.com.controller.web.io.BusSeatLayoutIO;
import org.in.com.controller.web.io.BusSeatTypeIO;
import org.in.com.controller.web.io.BusVehicleIO;
import org.in.com.controller.web.io.EventTriggerIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.RoleIO;
import org.in.com.controller.web.io.ScheduleCategoryIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.StageFareIO;
import org.in.com.controller.web.io.StageIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.StationPointIO;
import org.in.com.controller.web.io.TripChartDetailsIO;
import org.in.com.controller.web.io.TripChartIO;
import org.in.com.controller.web.io.TripIO;
import org.in.com.controller.web.io.TripInfoIO;
import org.in.com.controller.web.io.TripScheduleControlIO;
import org.in.com.controller.web.io.TripStatusIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.controller.web.io.VehicleAttendantIO;
import org.in.com.controller.web.io.VehicleDriverIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.BusVehicleAttendantDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.EventTriggerDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripInfoDTO;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.NotificationBusContactEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusService;
import org.in.com.service.BusVehicleService;
import org.in.com.service.ScheduleTripService;
import org.in.com.service.ScheduleVisibilityService;
import org.in.com.service.SearchService;
import org.in.com.service.SectorService;
import org.in.com.service.TripService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/{authtoken}/trip")
public class TripController extends BaseController {
	@Autowired
	TripService tripService;
	@Autowired
	SearchService searchService;
	@Autowired
	BusService busService;
	@Autowired
	ScheduleVisibilityService visibilityService;
	@Autowired
	S3Service s3Service;
	@Autowired
	BusVehicleService vehicleService;
	@Autowired
	ScheduleTripService scheduleTripService;
	@Autowired
	SectorService sectorService;
	@Autowired
	CargoService cargoService;

	@RequestMapping(value = "/{tripCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> getUpdate(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, String tripStatusCode, String remarks) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);
			tripDTO.setTripStatus(TripStatusEM.getTripStatusEM(tripStatusCode));
			tripDTO.setRemarks(remarks);
			if (tripDTO.getTripStatus() != null) {
				tripService.UpdateTripStatus(authDTO, tripDTO);
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{tripCode}/tripstageseatdetails/cache/clear", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> clearBookedBlockedSeatsCache(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);
			tripService.clearBookedBlockedSeatsCache(authDTO, tripDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{tripCode}/status/{statusCode}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> UpdateTripStatus(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("statusCode") String statusCode, String remarks) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);
			tripDTO.setTripStatus(TripStatusEM.getTripStatusEM(statusCode));
			tripDTO.setRemarks(remarks);
			if (tripDTO.getTripStatus() == null || TripStatusEM.TRIP_YET_OPEN.getId() == tripDTO.getTripStatus().getId()) {
				throw new ServiceException(ErrorCode.INVALID_CODE);
			}
			tripService.UpdateTripStatus(authDTO, tripDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/schedule/{tripDate}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TripIO>> getActiveTripSchedule(@PathVariable("authtoken") String authtoken, @PathVariable("tripDate") String tripDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<TripIO> tripList = new ArrayList<>();
		if (authDTO != null) {
			if (!DateUtil.isValidDate(tripDate)) {
				throw new ServiceException(ErrorCode.INVALID_DATE);
			}

			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(new DateTime(tripDate));
			List<TripDTO> list = searchService.getAllTrips(authDTO, searchDTO);

			// Permission check
			List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
			Eventlist.add(MenuEventEM.REPORT_TRIP_CHART_RIGHTS_30);
			Eventlist.add(MenuEventEM.REPORT_TRIP_CHART_RIGHTS_3HR);
			List<TripDTO> finalList = new ArrayList<>();
			MenuEventDTO visibilityMenuEventDTO = getPrivilegeV2(authDTO, Eventlist);
			if (visibilityMenuEventDTO != null && visibilityMenuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
				DateTime now = DateTime.now(TimeZone.getDefault());
				for (TripDTO tripDTO : list) {
					ScheduleStationDTO firstStation = null;
					for (ScheduleStationDTO scheduleStationDTO : tripDTO.getSchedule().getStationList()) {
						if (firstStation == null || scheduleStationDTO.getStationSequence() < firstStation.getStationSequence()) {
							firstStation = scheduleStationDTO;
						}
					}
					int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(new DateTime(tripDate), firstStation.getMinitues()));
					if (Integer.parseInt(visibilityMenuEventDTO.getAttr1Value()) > minutiesDiff) {
						finalList.add(tripDTO);
						break;
					}
				}
			}
			else {
				finalList.addAll(list);
			}
			for (TripDTO tripDTO : finalList) {
				TripIO tripIO = new TripIO();
				ScheduleIO schedule = new ScheduleIO();
				schedule.setCode(tripDTO.getSchedule().getCode());
				schedule.setName(tripDTO.getSchedule().getName());
				schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
				schedule.setDisplayName(tripDTO.getSchedule().getDisplayName());
				if (tripDTO.getSchedule().getCategory() != null) {
					ScheduleCategoryIO categoryIO = new ScheduleCategoryIO();
					categoryIO.setCode(tripDTO.getSchedule().getCategory().getCode());
					categoryIO.setName(tripDTO.getSchedule().getCategory().getName());
					schedule.setCategory(categoryIO);
				}
				tripIO.setSchedule(schedule);

				// Bus
				BusIO busIO = new BusIO();
				busIO.setName(tripDTO.getBus().getName());
				busIO.setCategoryCode(tripDTO.getBus().getCategoryCode() == null ? "" : tripDTO.getBus().getCategoryCode());
				busIO.setBusType(BitsUtil.getBusCategoryUsingEM(tripDTO.getBus().getCategoryCode()));
				busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
				busIO.setTotalSeatCount(tripDTO.getBus().getReservableLayoutSeatCount());
				busIO.setCode(tripDTO.getBus().getCode());
				tripIO.setBus(busIO);
				tripIO.setBookedSeatCount(tripDTO.getBookedSeatCount());
				tripIO.setMultiStageBookedSeatCount(tripDTO.getMultiStageBookedSeatCount());
				tripIO.setTotalBookedAmount(tripDTO.getTotalBookedAmount());
				tripIO.setRevenueAmount(tripDTO.getRevenueAmount());
				tripIO.setRevenue(tripDTO.getRevenue());
				tripIO.setAdditionalAttributes(tripDTO.getAdditionalAttributes());
				tripIO.setRemarks(tripDTO.getRemarks());

				List<StageIO> stageList = new ArrayList<>();
				// Sorting
				Comparator<StageDTO> comp = new BeanComparator("stageSequence");
				Collections.sort(tripDTO.getStageList(), comp);
				Map<Integer, StageStationDTO> stationMap = new HashMap<>();

				Comparator<StationPointDTO> pointComp = new BeanComparator("minitues");

				// Stage
				for (StageDTO stageDTO : tripDTO.getStageList()) {
					stationMap.put(stageDTO.getFromStation().getStation().getId(), stageDTO.getFromStation());
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
					Collections.sort(stageDTO.getFromStation().getStationPoint(), pointComp);
					StationPointDTO fromPointDTO = stageDTO.getFromStation().getStationPoint().stream().findFirst().orElse(new StationPointDTO());
					StationPointIO fromPointIO = new StationPointIO();
					fromPointIO.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getFromStation().getMinitues() + fromPointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					fromPointIO.setCode(fromPointDTO.getCode());
					fromPointIO.setName(fromPointDTO.getName());
					fromStationPoint.add(fromPointIO);

					List<StationPointIO> toStationPoint = new ArrayList<>();
					Collections.sort(stageDTO.getToStation().getStationPoint(), pointComp.reversed());
					StationPointDTO toPointDTO = stageDTO.getToStation().getStationPoint().stream().findFirst().orElse(new StationPointDTO());
					StationPointIO pointIO = new StationPointIO();
					pointIO.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), stageDTO.getToStation().getMinitues() + toPointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					pointIO.setCode(toPointDTO.getCode());
					pointIO.setName(toPointDTO.getName());
					toStationPoint.add(pointIO);

					fromStation.setStationPoint(fromStationPoint);
					toStation.setStationPoint(toStationPoint);
					stageIO.setFromStation(fromStation);
					stageIO.setToStation(toStation);

					TripStatusIO stageStatus = new TripStatusIO();
					if (stageDTO.getStageStatus() != null) {
						stageStatus.setCode(stageDTO.getStageStatus().getCode());
						stageStatus.setName(stageDTO.getStageStatus().getName());
					}
					stageIO.setStageStatus(stageStatus);
					stageList.add(stageIO);
				}
				tripIO.setStageList(stageList);

				// Primary Trip Details
				StageIO stageIO = new StageIO();
				StationIO fromStation = new StationIO();
				StationIO toStation = new StationIO();
				fromStation.setCode(tripDTO.getStage().getFromStation().getStation().getCode());
				fromStation.setName(tripDTO.getStage().getFromStation().getStation().getName());
				fromStation.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), tripDTO.getStage().getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				toStation.setCode(tripDTO.getStage().getToStation().getStation().getCode());
				toStation.setName(tripDTO.getStage().getToStation().getStation().getName());
				stageIO.setStageSequence(tripDTO.getStage().getStageSequence());
				toStation.setDateTime(DateUtil.addMinituesToDate(searchDTO.getTravelDate(), tripDTO.getStage().getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				stageIO.setFromStation(fromStation);
				stageIO.setToStation(toStation);
				tripIO.setStage(stageIO);

				tripIO.setTripCode(tripDTO.getCode());
				// Trip Status
				TripStatusIO tripStatusIO = new TripStatusIO();
				if (tripDTO.getTripStatus() != null) {
					tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
					tripStatusIO.setName(tripDTO.getTripStatus().getName());
				}
				tripIO.setTripStatus(tripStatusIO);

				DateTime now = DateUtil.NOW();
				StageStationDTO firstStationDTO = tripDTO.getStage().getFromStation();
				List<TripScheduleControlIO> statusList = new ArrayList<TripScheduleControlIO>();
				for (ScheduleControlDTO controlDTO : tripDTO.getSchedule().getControlList()) {
					TripScheduleControlIO control = new TripScheduleControlIO();
					control.setTripStatus(getTripStatus(TripStatusEM.TRIP_NA));
					StageStationDTO respectiveStageStation = firstStationDTO;

					if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0) {
						GroupIO groupIO = new GroupIO();
						groupIO.setCode(controlDTO.getGroup().getCode());
						groupIO.setActiveFlag(controlDTO.getGroup().getActiveFlag());
						groupIO.setName(controlDTO.getGroup().getName());
						control.setGroup(groupIO);
					}
					if (controlDTO.getFromStation() != null && controlDTO.getFromStation().getId() != 0) {
						StationIO fromStationIO = new StationIO();
						fromStationIO.setCode(controlDTO.getFromStation().getCode());
						fromStationIO.setActiveFlag(controlDTO.getFromStation().getActiveFlag());
						fromStationIO.setName(controlDTO.getFromStation().getName());
						control.setFromStation(fromStationIO);
						StationIO toStationIO = new StationIO();
						toStationIO.setCode(controlDTO.getToStation().getCode());
						toStationIO.setActiveFlag(controlDTO.getToStation().getActiveFlag());
						toStationIO.setName(controlDTO.getToStation().getName());
						control.setToStation(toStationIO);
						respectiveStageStation = stationMap.get(controlDTO.getFromStation().getId());
					}
					if (respectiveStageStation == null) {
						continue;
					}
					if (controlDTO.getAllowBookingFlag() != 1) {
						control.setTripStatus(getTripStatus(TripStatusEM.TRIP_CLOSED));
						control.setCloseDate(DateUtil.addMinituesToDate(tripDTO.getTripDate(), respectiveStageStation.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), respectiveStageStation.getMinitues()));
					if (minutiesDiff >= controlDTO.getOpenMinitues()) {
						control.setTripStatus(getTripStatus(TripStatusEM.TRIP_YET_OPEN));
					}
					control.setOpenDate(DateUtil.minusMinituesToDate(DateUtil.addMinituesToDate(tripDTO.getTripDate(), respectiveStageStation.getMinitues()), Math.abs(controlDTO.getOpenMinitues())).format("YYYY-MM-DD hh:mm:ss"));

					if (controlDTO.getCloseMinitues() != -1 && minutiesDiff <= controlDTO.getCloseMinitues()) {
						control.setTripStatus(getTripStatus(TripStatusEM.TRIP_CLOSED));
						control.setCloseDate(DateUtil.addMinituesToDate(tripDTO.getTripDate(), respectiveStageStation.getMinitues() - controlDTO.getCloseMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					// Identify Close time
					if (controlDTO.getCloseMinitues() == -1 && respectiveStageStation.getStationPoint().size() > 0) {
						control.setTripStatus(getTripStatus(TripStatusEM.TRIP_OPEN));
						control.setCloseDate(DateUtil.addMinituesToDate(tripDTO.getTripDate(), respectiveStageStation.getMinitues() + respectiveStageStation.getStationPoint().get(respectiveStageStation.getStationPoint().size() - 1).getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					else if (controlDTO.getCloseMinitues() != -1 && minutiesDiff > controlDTO.getCloseMinitues()) {
						control.setTripStatus(getTripStatus(TripStatusEM.TRIP_OPEN));
						control.setCloseDate(DateUtil.addMinituesToDate(tripDTO.getTripDate(), respectiveStageStation.getMinitues() - controlDTO.getCloseMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					}
					if (controlDTO.getAllowBookingFlag() != 1) {
						control.setTripStatus(getTripStatus(TripStatusEM.TRIP_CLOSED));
					}
					statusList.add(control);
				}
				tripIO.setStatusList(statusList);
				List<EventTriggerIO> eventList = new ArrayList<EventTriggerIO>();
				for (EventTriggerDTO triggerDTO : tripDTO.getEventList()) {
					EventTriggerIO trigger = new EventTriggerIO();
					trigger.setCode(triggerDTO.getCode());
					trigger.setName(triggerDTO.getName());
					trigger.setEventTime(triggerDTO.getEventTime().format("YYYY-MM-DD hh:mm:ss"));
					BaseIO event = new BaseIO();
					event.setCode(triggerDTO.getTriggerType().getCode());
					event.setName(triggerDTO.getTriggerType().getName());
					trigger.setTriggerType(event);
					eventList.add(trigger);
				}
				tripIO.setEventList(eventList);
				// Breakeven Details
				tripIO.setBreakeven(tripDTO.getBreakeven());

				// Copy Trip informations
				if (tripDTO.getTripInfo() != null) {
					TripInfoIO tripInfo = new TripInfoIO();
					tripInfo.setDriverMobile(tripDTO.getTripInfo().getDriverMobile());
					tripInfo.setDriverName(tripDTO.getTripInfo().getDriverName());
					tripInfo.setDriverName2(tripDTO.getTripInfo().getDriverName2());
					tripInfo.setDriverMobile2(tripDTO.getTripInfo().getDriverMobile2());
					tripInfo.setAttenderName(tripDTO.getTripInfo().getAttenderName());
					tripInfo.setAttenderMobile(tripDTO.getTripInfo().getAttenderMobile());
					tripInfo.setCaptainName(tripDTO.getTripInfo().getCaptainName());
					tripInfo.setCaptainMobile(tripDTO.getTripInfo().getCaptainMobile());
					tripInfo.setRemarks(tripDTO.getTripInfo().getRemarks());
					tripInfo.setNotificationStatus(tripDTO.getTripInfo().getNotificationStatusCodes().split(","));
					tripInfo.setTripStartDateTime(tripDTO.getTripInfo().getTripStartDateTime() != null ? tripDTO.getTripInfo().getTripStartDateTime().format(Text.DATE_TIME_DATE4J) : null);
					tripInfo.setTripCloseDateTime(tripDTO.getTripInfo().getTripCloseDateTime() != null ? tripDTO.getTripInfo().getTripCloseDateTime().format(Text.DATE_TIME_DATE4J) : null);
					if (tripDTO.getTripInfo().getBusVehicle() != null) {
						BusVehicleIO busVehicleIO = new BusVehicleIO();
						busVehicleIO.setName(tripDTO.getTripInfo().getBusVehicle().getName());
						busVehicleIO.setCode(tripDTO.getTripInfo().getBusVehicle().getCode());
						busVehicleIO.setRegistrationDate(tripDTO.getTripInfo().getBusVehicle().getRegistrationDate());
						busVehicleIO.setRegistationNumber(tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
						busVehicleIO.setLicNumber(tripDTO.getTripInfo().getBusVehicle().getLicNumber());
						busVehicleIO.setGpsDeviceCode(tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode());
						BaseIO gpsDeviceVendor = new BaseIO();
						if (tripDTO.getTripInfo().getBusVehicle().getDeviceVendor() != null) {
							gpsDeviceVendor.setCode(tripDTO.getTripInfo().getBusVehicle().getDeviceVendor().getCode());
							gpsDeviceVendor.setName(tripDTO.getTripInfo().getBusVehicle().getDeviceVendor().getName());
						}
						busVehicleIO.setGpsDeviceVendor(gpsDeviceVendor);
						tripInfo.setBusVehicle(busVehicleIO);
					}

					if (tripDTO.getTripInfo().getPrimaryDriver() != null) {
						VehicleDriverIO primaryDriver = new VehicleDriverIO();
						primaryDriver.setCode(tripDTO.getTripInfo().getPrimaryDriver().getCode());
						primaryDriver.setName(tripDTO.getTripInfo().getPrimaryDriver().getName());
						tripInfo.setPrimaryDriver(primaryDriver);
					}
					if (tripDTO.getTripInfo().getSecondaryDriver() != null) {
						VehicleDriverIO secondaryDriver = new VehicleDriverIO();
						secondaryDriver.setCode(tripDTO.getTripInfo().getSecondaryDriver().getCode());
						secondaryDriver.setName(tripDTO.getTripInfo().getSecondaryDriver().getName());
						tripInfo.setSecondaryDriver(secondaryDriver);
					}
					if (tripDTO.getTripInfo().getAttendant() != null) {
						VehicleAttendantIO attendant = new VehicleAttendantIO();
						attendant.setCode(tripDTO.getTripInfo().getAttendant().getCode());
						attendant.setName(tripDTO.getTripInfo().getAttendant().getName());
						tripInfo.setAttendant(attendant);
					}
					if (tripDTO.getTripInfo().getCaptain() != null) {
						VehicleAttendantIO captain = new VehicleAttendantIO();
						captain.setCode(tripDTO.getTripInfo().getCaptain().getCode());
						captain.setName(tripDTO.getTripInfo().getCaptain().getName());
						tripInfo.setCaptain(captain);
					}
					tripIO.setTripInfo(tripInfo);
				}
				tripList.add(tripIO);
			}

			// Sorting Trips
			Collections.sort(tripList, new Comparator<TripIO>() {
				@Override
				public int compare(TripIO t1, TripIO t2) {
					return new CompareToBuilder().append(t1.getSchedule().getName(), t2.getSchedule().getName()).toComparison();
				}
			});

		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/list/{tripDate}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TripIO>> getTripsForTripchart(@PathVariable("authtoken") String authtoken, @PathVariable("tripDate") String tripDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<TripIO> tripList = new ArrayList<>();
		if (authDTO != null) {
			TripChartDTO tripChartDTO = new TripChartDTO();
			tripChartDTO.setTripDate(new DateTime(tripDate));
			tripService.getTripsForTripchart(authDTO, tripChartDTO);
			for (TripDTO tripDTO : tripChartDTO.getTripList()) {
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
				busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
				tripIO.setBus(busIO);
				// Trip Status
				TripStatusIO tripStatusIO = new TripStatusIO();
				tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
				tripStatusIO.setName(tripDTO.getTripStatus().getName());
				tripIO.setTripStatus(tripStatusIO);
				tripIO.setTripCode(tripDTO.getCode());
				tripList.add(tripIO);
			}
		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/chart/{tripCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TripChartIO> getTripPassengerDetails(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, String eventType) throws Exception {

		TripChartIO tripchart = new TripChartIO();

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);

		// Permission check
		List<MenuEventEM> eventlist = new ArrayList<MenuEventEM>();
		eventlist.add(MenuEventEM.REPORT_TRIP_CHART_RIGHTS_30);
		eventlist.add(MenuEventEM.REPORT_TRIP_CHART_RIGHTS_3HR);
		MenuEventDTO visibilityMenuEventDTO = getPrivilegeV2(authDTO, eventlist);
		if (visibilityMenuEventDTO != null && visibilityMenuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
			authDTO.getAdditionalAttribute().put(MenuEventEM.REPORT_TRIP_CHART_RIGHTS_ALL.getActionCode(), visibilityMenuEventDTO.getAttr1Value());
		}
		/* Boarding Point Trip Chart - show cancelled tickets permission check */
		if (StringUtil.isNotNull(eventType) && eventType.equals("BPTC")) {
			eventlist = new ArrayList<MenuEventEM>();
			eventlist.add(MenuEventEM.REPORT_TRIP_CHART_VIEW_CANCELLED_SEATS);
			MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventlist);
			if (menuEventDTO != null && menuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
				tripDTO.getAdditionalAttributes().put(Text.TRIP_CHART_VIEW_CANCEL_SEATS_FLAG, Numeric.ONE);
			}
		}

		TripChartDTO tripChartDTO = tripService.getTripChart(authDTO, tripDTO);
		Map<Integer, StationPointDTO> boardingStationPointMap = new HashMap<Integer, StationPointDTO>();
		Map<Integer, StationPointDTO> droppingStationPointMap = new HashMap<Integer, StationPointDTO>();

		TripIO tripIO = new TripIO();
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
		// schedule.setServiceTax(tripDTO.getSchedule().getAcBusTax());
		tripIO.setSchedule(schedule);
		List<BusSeatLayoutIO> seatLayoutList = new ArrayList<>();

		for (BusSeatLayoutDTO layoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			BusSeatLayoutIO layoutIO = new BusSeatLayoutIO();
			layoutIO.setCode(layoutDTO.getCode());
			layoutIO.setSeatName(layoutDTO.getName());
			layoutIO.setColPos(layoutDTO.getColPos());
			layoutIO.setRowPos(layoutDTO.getRowPos());
			layoutIO.setLayer(layoutDTO.getLayer());
			layoutIO.setSequence(layoutDTO.getSequence());
			layoutIO.setActiveFlag(layoutDTO.getActiveFlag());

			BusSeatTypeIO seatTypeIO = new BusSeatTypeIO();
			seatTypeIO.setCode(layoutDTO.getBusSeatType().getCode());
			seatTypeIO.setName(layoutDTO.getBusSeatType().getName());
			layoutIO.setBusSeatType(seatTypeIO);
			seatLayoutList.add(layoutIO);
		}

		// Sorting Bus Seat Layout
		Comparator<BusSeatLayoutIO> seatComp = new BeanComparator("sequence");
		Collections.sort(seatLayoutList, seatComp);

		// Bus
		BusIO busIO = new BusIO();
		busIO.setSeatLayoutList(seatLayoutList);
		busIO.setCode(tripDTO.getBus().getCode());
		busIO.setName(tripDTO.getBus().getName());
		busIO.setCategoryCode(tripDTO.getBus().getCategoryCode() == null ? "" : tripDTO.getBus().getCategoryCode());
		busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
		tripIO.setBus(busIO);

		List<StageIO> stageList = new ArrayList<>();
		// Sorting
		Comparator<StageDTO> stageComp = new BeanComparator("stageSequence");
		Collections.sort(tripDTO.getStageList(), stageComp);

		// Stage
		for (StageDTO stageDTO : tripDTO.getStageList()) {
			StageIO stageIO = new StageIO();
			StationIO fromStation = new StationIO();
			StationIO toStation = new StationIO();
			fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
			fromStation.setName(stageDTO.getFromStation().getStation().getName());
			fromStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
			toStation.setCode(stageDTO.getToStation().getStation().getCode());
			toStation.setName(stageDTO.getToStation().getStation().getName());
			stageIO.setStageSequence(stageDTO.getStageSequence());
			toStation.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getToStation().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
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
				if (pointDTO.getCreditDebitFlag().equals("Cr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
				pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
				pointIO.setCode(pointDTO.getCode());
				pointIO.setName(pointDTO.getName());
				pointIO.setLandmark(pointDTO.getLandmark());
				pointIO.setAddress(pointDTO.getAddress());
				pointIO.setNumber(pointDTO.getNumber());
				fromStationPoint.add(pointIO);
				boardingStationPointMap.put(pointDTO.getId(), pointDTO);
			}
			List<StationPointIO> toStationPoint = new ArrayList<>();
			for (StationPointDTO pointDTO : stageDTO.getToStation().getStationPoint()) {
				StationPointIO pointIO = new StationPointIO();
				if (pointDTO.getCreditDebitFlag().equals("Cr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				else if (pointDTO.getCreditDebitFlag().equals("Dr")) {
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getToStation().getMinitues() - pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}

				pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
				pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
				pointIO.setCode(pointDTO.getCode());
				pointIO.setName(pointDTO.getName());
				pointIO.setLandmark(pointDTO.getLandmark());
				pointIO.setAddress(pointDTO.getAddress());
				pointIO.setNumber(pointDTO.getNumber());
				toStationPoint.add(pointIO);
				droppingStationPointMap.put(pointDTO.getId(), pointDTO);
			}
			// Sorting
			Comparator<StationPointIO> pointcomp = new BeanComparator("dateTime");
			Collections.sort(fromStationPoint, pointcomp);
			Collections.sort(toStationPoint, pointcomp);

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
		tripchart.setTrip(tripIO);

		List<TripChartDetailsIO> ticketDetailsList = new ArrayList<TripChartDetailsIO>();
		for (TripChartDetailsDTO chartDTO : tripChartDTO.getTicketDetailsList()) {
			TripChartDetailsIO io = new TripChartDetailsIO();
			io.setSeatCode(chartDTO.getSeatCode());
			io.setSeatName(chartDTO.getSeatName());
			io.setTicketCode(chartDTO.getTicketCode());
			io.setBookedDate(chartDTO.getTicketAt().toString());
			io.setPassengerName(chartDTO.getPassengerName());
			io.setPassengerAge(chartDTO.getPassengerAge());
			io.setIdProof(chartDTO.getIdProof());
			io.setGender(chartDTO.getSeatGendar().getCode());
			io.setDeviceMedium(chartDTO.getDeviceMedium().getCode());
			io.setPassengerMobile(chartDTO.getPassengerMobile());
			io.setAlternateMobile(chartDTO.getAlternateMobile());
			io.setSeatFare(chartDTO.getSeatFare());
			io.setAcBusTax(chartDTO.getAcBusTax());
			io.setRemarks(StringUtil.isNotNull(chartDTO.getRemarks()) ? chartDTO.getRemarks().replaceAll("null", "").replaceAll("-", "") : "");
			StationIO fromStationIO = new StationIO();
			StationIO toStationIO = new StationIO();
			fromStationIO.setCode(chartDTO.getFromStation().getCode());
			fromStationIO.setName(chartDTO.getFromStation().getName());
			toStationIO.setCode(chartDTO.getToStation().getCode());
			toStationIO.setName(chartDTO.getToStation().getName());
			io.setFromStation(fromStationIO);
			io.setToStation(toStationIO);
			io.setTravelStatusCode(chartDTO.getTravelStatus().getCode());
			io.setTicketStatusCode(chartDTO.getTicketStatus().getCode());
			StationPointIO boardingStationPoint = new StationPointIO();
			StationPointIO droppingStationPoint = new StationPointIO();
			if (boardingStationPointMap.get(chartDTO.getBoardingPoint().getId()) != null) {
				StationPointDTO stationPointDTO = boardingStationPointMap.get(chartDTO.getBoardingPoint().getId());
				boardingStationPoint.setName(stationPointDTO.getName());
				boardingStationPoint.setCode(stationPointDTO.getCode());
				boardingStationPoint.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), chartDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				boardingStationPoint.setAddress(stationPointDTO.getAddress());
				boardingStationPoint.setLandmark(stationPointDTO.getLandmark());
				if (stationPointDTO.getActiveFlag() == 0) {
					boardingStationPoint.setName("Others (" + stationPointDTO.getName() + ")");
				}
			}
			else {
				boardingStationPoint.setName("Others");
				boardingStationPoint.setCode("Others");
				boardingStationPoint.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), chartDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
			}
			if (droppingStationPointMap.get(chartDTO.getDroppingPoint().getId()) != null) {
				StationPointDTO stationPointDTO = droppingStationPointMap.get(chartDTO.getDroppingPoint().getId());
				droppingStationPoint.setName(stationPointDTO.getName());
				droppingStationPoint.setCode(stationPointDTO.getCode());
				droppingStationPoint.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), chartDTO.getDroppingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				droppingStationPoint.setAddress(stationPointDTO.getAddress());
				droppingStationPoint.setLandmark(stationPointDTO.getLandmark());
			}
			else {
				droppingStationPoint.setName("Others");
				droppingStationPoint.setCode("Others");
				droppingStationPoint.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), chartDTO.getDroppingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
			}
			io.setBoardingPoint(boardingStationPoint);
			io.setDroppingPoint(droppingStationPoint);
			UserIO userIO = new UserIO();
			userIO.setCode(chartDTO.getUser().getCode());
			userIO.setUsername(chartDTO.getUser().getUsername());
			userIO.setName(chartDTO.getUser().getName());
			userIO.setLastname(chartDTO.getUser().getLastname());
			RoleIO role = new RoleIO();
			role.setCode(chartDTO.getUser().getUserRole().getCode());
			role.setName(chartDTO.getUser().getUserRole().getName());
			userIO.setRole(role);

			GroupIO groupIO = new GroupIO();
			groupIO.setName(chartDTO.getUser().getGroup().getName());
			groupIO.setCode(chartDTO.getUser().getGroup().getCode());
			userIO.setGroup(groupIO);
			io.setBookedBy(userIO);
			io.setBookedType(chartDTO.getBookingType());
			ticketDetailsList.add(io);
		}
		tripchart.setTicketDetailsList(ticketDetailsList);
		if (tripDTO.getTripInfo() != null) {
			tripchart.setDriverName(tripDTO.getTripInfo().getDriverName());
			tripchart.setDriverMobile(tripDTO.getTripInfo().getDriverMobile());
			tripchart.setDriverName2(tripDTO.getTripInfo().getDriverName2());
			tripchart.setDriverMobile2(tripDTO.getTripInfo().getDriverMobile2());
			tripchart.setAttenderName(tripDTO.getTripInfo().getAttenderName());
			tripchart.setAttenderMobile(tripDTO.getTripInfo().getAttenderMobile());
			tripchart.setCaptainName(tripDTO.getTripInfo().getCaptainName());
			tripchart.setCaptainMobile(tripDTO.getTripInfo().getCaptainMobile());
			tripchart.setRemarks(tripDTO.getTripInfo().getRemarks() == null ? Text.NA : tripDTO.getTripInfo().getRemarks());
			tripchart.setNotificationBusContactType(tripDTO.getTripInfo().getNotificationBusContactType().getCode());
			
			BusVehicleIO busVehicle = new BusVehicleIO();
			if (tripDTO.getTripInfo().getBusVehicle() != null) {
				busVehicle.setRegistationNumber(tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
				busVehicle.setCode(tripDTO.getTripInfo().getBusVehicle().getCode());
				busVehicle.setName(tripDTO.getTripInfo().getBusVehicle().getName());

				BaseIO gpsDeviceVendor = new BaseIO();
				gpsDeviceVendor.setCode(tripDTO.getTripInfo().getBusVehicle().getDeviceVendor() != null ? tripDTO.getTripInfo().getBusVehicle().getDeviceVendor().getCode() : "NA");
				gpsDeviceVendor.setName(tripDTO.getTripInfo().getBusVehicle().getDeviceVendor() != null ? tripDTO.getTripInfo().getBusVehicle().getDeviceVendor().getName() : "NA");
				busVehicle.setGpsDeviceVendor(gpsDeviceVendor);

				busVehicle.setGpsDeviceCode(tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode());
			}

			if (tripDTO.getTripInfo().getPrimaryDriver() != null) {
				VehicleDriverIO primaryDriver = new VehicleDriverIO();
				primaryDriver.setCode(tripDTO.getTripInfo().getPrimaryDriver().getCode());
				primaryDriver.setName(tripDTO.getTripInfo().getPrimaryDriver().getName());
				tripchart.setPrimaryDriver(primaryDriver);
			}
			if (tripDTO.getTripInfo().getSecondaryDriver() != null) {
				VehicleDriverIO secondaryDriver = new VehicleDriverIO();
				secondaryDriver.setCode(tripDTO.getTripInfo().getSecondaryDriver().getCode());
				secondaryDriver.setName(tripDTO.getTripInfo().getSecondaryDriver().getName());
				tripchart.setSecondaryDriver(secondaryDriver);
			}
			if (tripDTO.getTripInfo().getAttendant() != null) {
				VehicleAttendantIO attendant = new VehicleAttendantIO();
				attendant.setCode(tripDTO.getTripInfo().getAttendant().getCode());
				attendant.setName(tripDTO.getTripInfo().getAttendant().getName());
				tripchart.setAttendant(attendant);
			}
			if (tripDTO.getTripInfo().getCaptain() != null) {
				VehicleAttendantIO captain = new VehicleAttendantIO();
				captain.setCode(tripDTO.getTripInfo().getCaptain().getCode());
				captain.setName(tripDTO.getTripInfo().getCaptain().getName());
				tripchart.setCaptain(captain);
			}
			
			tripchart.setBusVehicle(busVehicle);
		}

		return ResponseIO.success(tripchart);
	}

	@RequestMapping(value = "/info/update/{tripCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateTripInfo(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, String driverName, String driverMobile, String remarks, String vehicleCode, String registrationNumber, String driverName2, String driverMobile2, String attenderName, String attenderMobile, String primaryDriverCode, String secondaryDriverCode, String attendantCode, String captainCode, String captainName, String captainMobile, String notificationBusContactType) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);
			TripInfoDTO infoDTO = new TripInfoDTO();
			infoDTO.setDriverName(StringUtil.isNull(driverName) ? Text.NA : driverName);
			infoDTO.setDriverMobile(StringUtil.isNull(driverMobile) ? Text.NA : driverMobile);
			infoDTO.setDriverName2(StringUtil.isNull(driverName2) ? Text.NA : driverName2);
			infoDTO.setDriverMobile2(StringUtil.isNull(driverMobile2) ? Text.NA : driverMobile2);
			infoDTO.setAttenderName(StringUtil.isNull(attenderName) ? Text.NA : attenderName);
			infoDTO.setAttenderMobile(StringUtil.isNull(attenderMobile) ? Text.NA : attenderMobile);
			infoDTO.setCaptainName(StringUtil.isNull(captainName) ? Text.NA : captainName);
			infoDTO.setCaptainMobile(StringUtil.isNull(captainMobile) ? Text.NA : captainMobile);
			
			BusVehicleDriverDTO primaryDriver = new BusVehicleDriverDTO();
			primaryDriver.setCode(primaryDriverCode);
			infoDTO.setPrimaryDriver(primaryDriver);

			BusVehicleDriverDTO secondaryDriver = new BusVehicleDriverDTO();
			secondaryDriver.setCode(secondaryDriverCode);
			infoDTO.setSecondaryDriver(secondaryDriver);

			BusVehicleAttendantDTO attendant = new BusVehicleAttendantDTO();
			attendant.setCode(attendantCode);
			infoDTO.setAttendant(attendant);
			
			BusVehicleAttendantDTO captain = new BusVehicleAttendantDTO();
			captain.setCode(captainCode);
			infoDTO.setCaptain(captain);
			
			infoDTO.setNotificationBusContact(NotificationBusContactEM.getTypeEM(notificationBusContactType));

			infoDTO.setRemarks(remarks);
			if (StringUtil.isNotNull(vehicleCode)) {
				BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
				busVehicleDTO.setCode(vehicleCode);
				busVehicleDTO.setRegistationNumber(registrationNumber);
				infoDTO.setBusVehicle(busVehicleDTO);
			}
			tripDTO.setTripInfo(infoDTO);
			tripService.UpdateTripContact(authDTO, tripDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/info/all", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TripChartIO>> getAutoCompleteTripInfoContact(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<TripChartIO> list = new ArrayList<TripChartIO>();
		if (authDTO != null) {

			TripInfoDTO infoDTO = tripService.getAllTripContact(authDTO);
			for (TripInfoDTO info : infoDTO.getList()) {
				TripChartIO tripChartIO = new TripChartIO();
				tripChartIO.setDriverName(info.getDriverName());
				tripChartIO.setDriverMobile(info.getDriverMobile());
				list.add(tripChartIO);
			}
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/driver", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<VehicleDriverIO>> getTripDrivers(@PathVariable("authtoken") String authtoken) throws Exception {
		List<VehicleDriverIO> vehicleDrivers = new ArrayList<VehicleDriverIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<BusVehicleDriverDTO> list = tripService.getTripDrivers(authDTO);
			for (BusVehicleDriverDTO driverDTO : list) {
				VehicleDriverIO vehicleDriver = new VehicleDriverIO();
				vehicleDriver.setCode(driverDTO.getCode());
				vehicleDriver.setName(driverDTO.getName());
				vehicleDriver.setBadgeNumber(driverDTO.getBadgeNumber());
				vehicleDriver.setEmployeeCode(driverDTO.getEmployeeCode());
				vehicleDriver.setMobileNumber(driverDTO.getMobileNumber());
				vehicleDriver.setActiveFlag(driverDTO.getActiveFlag());
				vehicleDrivers.add(vehicleDriver);
			}
		}
		return ResponseIO.success(vehicleDrivers);
	}

	@RequestMapping(value = "/attendant", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<VehicleAttendantIO>> getAllAttendant(@PathVariable("authtoken") String authtoken) throws Exception {
		List<VehicleAttendantIO> vehicleAttendantList = new ArrayList<VehicleAttendantIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<BusVehicleAttendantDTO> attendantDTOList = tripService.getTripAttenders(authDTO);

		for (BusVehicleAttendantDTO vehicleAttendantDTO : attendantDTOList) {
			VehicleAttendantIO vehicleAttendant = new VehicleAttendantIO();
			vehicleAttendant.setCode(vehicleAttendantDTO.getCode());
			vehicleAttendant.setName(vehicleAttendantDTO.getName());
			vehicleAttendant.setMobile(vehicleAttendantDTO.getMobile());
			vehicleAttendant.setActiveFlag(vehicleAttendantDTO.getActiveFlag());
			vehicleAttendantList.add(vehicleAttendant);
		}
		return ResponseIO.success(vehicleAttendantList);
	}

	@RequestMapping(value = "/info/{tripCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TripChartIO> getTripsForTripinfo(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TripChartIO tripChart = new TripChartIO();
		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		TripInfoDTO infoDTO = tripService.getTripInfo(authDTO, tripDTO);
		if (infoDTO != null) {
			TripIO tripIO = new TripIO();
			tripChart.setDriverMobile(infoDTO.getDriverMobile());
			tripChart.setDriverName(infoDTO.getDriverName());
			tripChart.setDriverName2(tripDTO.getTripInfo().getDriverName2());
			tripChart.setDriverMobile2(tripDTO.getTripInfo().getDriverMobile2());
			tripChart.setAttenderName(tripDTO.getTripInfo().getAttenderName());
			tripChart.setAttenderMobile(tripDTO.getTripInfo().getAttenderMobile());
			tripChart.setCaptainName(tripDTO.getTripInfo().getCaptainName());
			tripChart.setCaptainMobile(tripDTO.getTripInfo().getCaptainMobile());
			tripChart.setNotificationBusContactType(tripDTO.getTripInfo().getNotificationBusContactType().getCode());

			if (infoDTO.getPrimaryDriver() != null) {
				VehicleDriverIO primaryDriver = new VehicleDriverIO();
				primaryDriver.setCode(infoDTO.getPrimaryDriver().getCode());
				primaryDriver.setName(infoDTO.getPrimaryDriver().getName());
				tripChart.setPrimaryDriver(primaryDriver);
			}
			if (infoDTO.getSecondaryDriver() != null) {
				VehicleDriverIO secondaryDriver = new VehicleDriverIO();
				secondaryDriver.setCode(infoDTO.getSecondaryDriver().getCode());
				secondaryDriver.setName(infoDTO.getSecondaryDriver().getName());
				tripChart.setSecondaryDriver(secondaryDriver);
			}
			if (infoDTO.getAttendant() != null) {
				VehicleAttendantIO attendant = new VehicleAttendantIO();
				attendant.setCode(infoDTO.getAttendant().getCode());
				attendant.setName(infoDTO.getAttendant().getName());
				tripChart.setAttendant(attendant);
			}
			
			if (infoDTO.getCaptain() != null) {
				VehicleAttendantIO captain = new VehicleAttendantIO();
				captain.setCode(infoDTO.getCaptain().getCode());
				captain.setName(infoDTO.getCaptain().getName());
				tripChart.setCaptain(captain);
			}
			
			tripChart.setRemarks(infoDTO.getRemarks());
			tripChart.setTrip(tripIO);
			if (infoDTO.getBusVehicle() != null) {
				BusVehicleIO busVehicleIO = new BusVehicleIO();
				busVehicleIO.setName(infoDTO.getBusVehicle().getName());
				busVehicleIO.setCode(infoDTO.getBusVehicle().getCode());
				busVehicleIO.setRegistrationDate(infoDTO.getBusVehicle().getRegistrationDate());
				busVehicleIO.setRegistationNumber(infoDTO.getBusVehicle().getRegistationNumber());
				busVehicleIO.setLicNumber(infoDTO.getBusVehicle().getLicNumber());
				busVehicleIO.setGpsDeviceCode(infoDTO.getBusVehicle().getGpsDeviceCode());

				if (infoDTO.getBusVehicle().getDeviceVendor() != null) {
					BaseIO gpsVendor = new BaseIO();
					gpsVendor.setCode(infoDTO.getBusVehicle().getDeviceVendor().getCode());
					gpsVendor.setName(infoDTO.getBusVehicle().getDeviceVendor().getName());
					busVehicleIO.setGpsDeviceVendor(gpsVendor);
				}
				tripChart.setBusVehicle(busVehicleIO);
			}
		}
		return ResponseIO.success(tripChart);

	}

	@RequestMapping(value = "/all/info/{tripDate}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TripChartIO>> getTripsForTripinfoByTripDate(@PathVariable("authtoken") String authtoken, @PathVariable("tripDate") String tripDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<TripChartIO> tripList = new ArrayList<>();
		DateTime tripDateTime = new DateTime(tripDate);
		List<TripDTO> list = tripService.getTripInfoByTripDate(authDTO, tripDateTime);
		for (TripDTO tripDTO : list) {
			TripChartIO tripChart = new TripChartIO();
			TripIO tripIO = new TripIO();
			tripIO.setTripCode(tripDTO.getCode());
			tripChart.setDriverMobile(tripDTO.getTripInfo().getDriverMobile());
			tripChart.setDriverName(tripDTO.getTripInfo().getDriverName());
			tripChart.setDriverName2(tripDTO.getTripInfo().getDriverName2());
			tripChart.setDriverMobile2(tripDTO.getTripInfo().getDriverMobile2());
			tripChart.setAttenderName(tripDTO.getTripInfo().getAttenderName());
			tripChart.setAttenderMobile(tripDTO.getTripInfo().getAttenderMobile());
			tripChart.setCaptainName(tripDTO.getTripInfo().getCaptainName());
			tripChart.setCaptainMobile(tripDTO.getTripInfo().getCaptainMobile());
			tripChart.setNotificationBusContactType(tripDTO.getTripInfo().getNotificationBusContactType().getCode());
			
			if (tripDTO.getTripInfo().getPrimaryDriver() != null) {
				VehicleDriverIO primaryDriver = new VehicleDriverIO();
				primaryDriver.setCode(tripDTO.getTripInfo().getPrimaryDriver().getCode());
				primaryDriver.setName(tripDTO.getTripInfo().getPrimaryDriver().getName());
				tripChart.setPrimaryDriver(primaryDriver);
			}
			if (tripDTO.getTripInfo().getSecondaryDriver() != null) {
				VehicleDriverIO secondaryDriver = new VehicleDriverIO();
				secondaryDriver.setCode(tripDTO.getTripInfo().getSecondaryDriver().getCode());
				secondaryDriver.setName(tripDTO.getTripInfo().getSecondaryDriver().getName());
				tripChart.setSecondaryDriver(secondaryDriver);
			}
			if (tripDTO.getTripInfo().getAttendant() != null) {
				VehicleAttendantIO attendant = new VehicleAttendantIO();
				attendant.setCode(tripDTO.getTripInfo().getAttendant().getCode());
				attendant.setName(tripDTO.getTripInfo().getAttendant().getName());
				tripChart.setAttendant(attendant);
			}
			
			if (tripDTO.getTripInfo().getCaptain() != null) {
				VehicleAttendantIO captain = new VehicleAttendantIO();
				captain.setCode(tripDTO.getTripInfo().getCaptain().getCode());
				captain.setName(tripDTO.getTripInfo().getCaptain().getName());
				tripChart.setCaptain(captain);
			}

			tripChart.setRemarks(tripDTO.getTripInfo().getRemarks());
			tripChart.setTrip(tripIO);
			TripInfoDTO tripInfo = tripDTO.getTripInfo();
			if (tripInfo.getBusVehicle() != null) {
				BusVehicleDTO busVehicle = tripInfo.getBusVehicle();
				BusVehicleIO busVehicleIO = new BusVehicleIO();
				busVehicleIO.setName(busVehicle.getName());
				busVehicleIO.setCode(busVehicle.getCode());
				busVehicleIO.setRegistrationDate(busVehicle.getRegistrationDate());
				busVehicleIO.setRegistationNumber(busVehicle.getRegistationNumber());
				busVehicleIO.setLicNumber(busVehicle.getLicNumber());
				busVehicleIO.setGpsDeviceCode(busVehicle.getGpsDeviceCode());

				BaseIO gpsVendor = new BaseIO();
				gpsVendor.setCode(busVehicle.getDeviceVendor() != null ? busVehicle.getDeviceVendor().getCode() : null);
				gpsVendor.setName(busVehicle.getDeviceVendor() != null ? busVehicle.getDeviceVendor().getName() : null);
				busVehicleIO.setGpsDeviceVendor(gpsVendor);

				tripChart.setBusVehicle(busVehicleIO);
			}
			tripList.add(tripChart);
		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/list/{tripDate}/vehicle/{vehicleCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TripChartIO>> getTripsByTripDateVehicle(@PathVariable("authtoken") String authtoken, @PathVariable("tripDate") String tripDate, @PathVariable("vehicleCode") String vehicleCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<TripChartIO> tripList = new ArrayList<>();
		BusVehicleDTO vehicle = new BusVehicleDTO();
		vehicle.setCode(vehicleCode);
		DateTime tripDateTime = new DateTime(tripDate);
		try {
			List<TripDTO> list = scheduleTripService.getTripByTripDateAndVehicle(authDTO, tripDateTime, vehicle);
			for (TripDTO tripDTO : list) {
				TripChartIO tripChartIO = new TripChartIO();
				TripIO tripIO = new TripIO();

				// Stage
				StageDTO stageDTO = tripDTO.getStage();
				StageIO stageIO = new StageIO();
				stageIO.setCode(stageDTO.getCode());

				StationIO fromStation = new StationIO();
				fromStation.setName(stageDTO.getFromStation().getStation().getName());
				stageIO.setFromStation(fromStation);

				StationIO toStation = new StationIO();
				toStation.setName(stageDTO.getToStation().getStation().getName());
				stageIO.setToStation(toStation);

				stageIO.setStageSequence(stageDTO.getStageSequence());
				tripIO.setStage(stageIO);

				tripIO.setTripCode(tripDTO.getCode());
				tripIO.setTravelDate(tripDTO.getStage().getTravelDate().format("YYYY-MM-DD hh:mm:ss"));
				tripIO.setCloseTime(tripDTO.getTripInfo() != null ? tripDTO.getTripInfo().getTripCloseDateTime().format(Text.DATE_TIME_DATE4J) : null);

				tripChartIO.setTrip(tripIO);
				tripList.add(tripChartIO);
			}
		}
		catch (ServiceException e) {
			System.out.println("VehicleCode VE001: " + vehicleCode + "  - " + tripDate);
			throw e;
		}
		catch (Exception e) {
			System.out.println("VehicleCode VE002: " + vehicleCode + "  - " + tripDate);
			e.printStackTrace();
		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/schedule/{tripDate}/history", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<String> getActiveTripScheduleHistory(@PathVariable("authtoken") String authtoken, @PathVariable("tripDate") String tripDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		DateTime tripDateTime = new DateTime(tripDate);
		if (!tripDateTime.getEndOfDay().lteq(DateUtil.NOW())) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		// Permission check
		List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();

		Eventlist.clear();
		Eventlist.add(MenuEventEM.REPORT_TRIP_CHART_RIGHTS_30);
		Eventlist.add(MenuEventEM.REPORT_TRIP_CHART_RIGHTS_3HR);
		MenuEventDTO visibilityMenuEventDTO = getPrivilegeV2(authDTO, Eventlist);
		if (visibilityMenuEventDTO != null && visibilityMenuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		SectorDTO sector = sectorService.getActiveUserSectorSchedule(authDTO);
		JsonArray finalTripList = new JsonArray();
		JsonArray tripData = s3Service.getActiveTripList(authDTO, tripDateTime);

		if (sector != null && sector.getActiveFlag() == Numeric.ONE_INT) {
			List<ScheduleDTO> rightsSchedule = sector.getSchedule();
			for (ScheduleDTO rightsscheduleDTO : rightsSchedule) {
				for (Object object : tripData) {
					JsonObject trip = (JsonObject) object;
					if (trip.get("schedule").getAsJsonObject().get("code").getAsString().equals(rightsscheduleDTO.getCode())) {
						finalTripList.add(trip);
						break;
					}
				}
			}
		}
		else {
			finalTripList.addAll(tripData);
		}

		if (finalTripList.size() == 0) {
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		return ResponseIO.success(finalTripList.toString());

	}

	@RequestMapping(value = "/vehicle", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<BusVehicleIO>> getAllBusVehicle(@PathVariable("authtoken") String authtoken) throws Exception {
		List<BusVehicleIO> bvList = new ArrayList<BusVehicleIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<BusVehicleDTO> list = vehicleService.getActiveBusVehicles(authDTO);
			for (BusVehicleDTO bvDTO : list) {
				BusVehicleIO bvIo = new BusVehicleIO();
				bvIo.setCode(bvDTO.getCode());
				bvIo.setName(bvDTO.getName());

				BusIO busIO = new BusIO();
				busIO.setCode(bvDTO.getBus().getCode());
				busIO.setName(bvDTO.getBus().getName());
				busIO.setCategoryCode(bvDTO.getBus().getCategoryCode());
				bvIo.setBus(busIO);

				BaseIO vehicleType = new BaseIO();
				vehicleType.setCode(bvDTO.getVehicleType().getCode());
				vehicleType.setName(bvDTO.getVehicleType().getName());
				bvIo.setVehicleType(vehicleType);

				bvIo.setRegistrationDate(bvDTO.getRegistrationDate());
				bvIo.setRegistationNumber(bvDTO.getRegistationNumber());
				bvIo.setLicNumber(bvDTO.getLicNumber());
				bvIo.setGpsDeviceCode(bvDTO.getGpsDeviceCode());
				BaseIO deviceVendor = new BaseIO();
				deviceVendor.setCode(bvDTO.getDeviceVendor().getCode());
				deviceVendor.setName(bvDTO.getDeviceVendor().getName());
				bvIo.setGpsDeviceVendor(deviceVendor);
				bvIo.setMobileNumber(bvDTO.getMobileNumber());
				bvIo.setActiveFlag(bvDTO.getActiveFlag());
				bvList.add(bvIo);
			}
		}
		return ResponseIO.success(bvList);
	}

	@RequestMapping(value = "/vehicle/{tripDate}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<JSONArray> getActiveBusVehicle(@PathVariable("authtoken") String authtoken, @PathVariable("tripDate") String tripDate) throws Exception {
		JSONArray busVehicles = new JSONArray();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			DateTime tripDateTime = new DateTime(tripDate);
			busVehicles = tripService.getActiveTripBusVehicles(authDTO, tripDateTime);
		}
		return ResponseIO.success(busVehicles);
	}

	@RequestMapping(value = "/{tripCode}/check/anychange", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> getTripPassengerDetailsWithLatest(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, String version) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);
			DateTime versionDatetime = new DateTime(version);
			tripService.checkTripChartWithLatest(authDTO, tripDTO, versionDatetime);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{tripCode}/check/anychange/{syncTime}/v2", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> checkRecentTickets(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("syncTime") String syncTime) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);
			tripService.checkRecentTickets(authDTO, tripDTO, syncTime);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{tripCode}/vehicles/recommend", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<JSONArray> getPreferredVehicles(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		JSONArray busVehicles = new JSONArray();
		if (authDTO != null) {
			TripDTO trip = new TripDTO();
			trip.setCode(tripCode);
			busVehicles = tripService.getPreferredVehicles(authDTO, trip);
		}
		return ResponseIO.success(busVehicles);
	}

	private TripStatusIO getTripStatus(TripStatusEM statusEM) {
		TripStatusIO status = new TripStatusIO();
		status.setCode(statusEM.getCode());
		status.setName(statusEM.getName());
		return status;
	}

	@RequestMapping(value = "/{tripCode}/update/odometer", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateTripOdometer(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @RequestBody Map<String, String> additionalAttribute) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		additionalAttribute.put("tripCode", tripCode);

		if (additionalAttribute.isEmpty() || StringUtil.isNull(additionalAttribute.get("actionType"))) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL);
		}
		if ("START".equals(additionalAttribute.get("actionType")) && (StringUtil.isNull(additionalAttribute.get("startOdometer")) || Float.valueOf(additionalAttribute.get("startOdometer")) < 0)) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL, "Invalid Start Odometer");
		}
		else if ("END".equals(additionalAttribute.get("actionType")) && (StringUtil.isNull(additionalAttribute.get("endOdometer")) || Float.valueOf(additionalAttribute.get("endOdometer")) < 0)) {
			throw new ServiceException(ErrorCode.REQURIED_FIELD_SHOULD_NOT_NULL, "Invalid End Odometer");
		}

		tripService.updateTripExtras(authDTO, additionalAttribute);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{tripCode}/cargo/transit/vehicle/details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<JSONArray> getPreferredVehicles(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, String tripDate, String registrationNumber) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (!DateUtil.isValidDate(tripDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		if (StringUtil.isNull(registrationNumber)) {
			throw new ServiceException(ErrorCode.INVALID_VEHICLE_CODE);
		}
		JSONArray transitDetails = cargoService.getCargoTransitDetails(authDTO, tripDate, registrationNumber);
		return ResponseIO.success(transitDetails);
	}
	
	@RequestMapping(value = "/data/count/details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<Map<String, Map<String, String>>> getTripDataCount(@PathVariable("authtoken") String authtoken, String fromDate, String toDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (!DateUtil.isValidDate(fromDate) || !DateUtil.isValidDate(toDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}
		Map<String, Map<String, String>> resultMap = tripService.getTripDataCountDetails(authDTO, fromDate, toDate);
		return ResponseIO.success(resultMap);
	}
}
