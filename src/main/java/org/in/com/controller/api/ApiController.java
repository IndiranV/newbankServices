package org.in.com.controller.api;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.BeanComparator;
import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.constants.Numeric;
import org.in.com.controller.api.io.BusIO;
import org.in.com.controller.api.io.BusSeatLayoutIO;
import org.in.com.controller.api.io.ResponseIO;
import org.in.com.controller.api.io.ScheduleIO;
import org.in.com.controller.api.io.StageFareIO;
import org.in.com.controller.api.io.StageIO;
import org.in.com.controller.api.io.StationIO;
import org.in.com.controller.api.io.StationPointIO;
import org.in.com.controller.api.io.TripChartDetailsIO;
import org.in.com.controller.api.io.TripChartIO;
import org.in.com.controller.api.io.TripIO;
import org.in.com.controller.api.io.TripInfoIO;
import org.in.com.controller.api.io.TripStatusIO;
import org.in.com.controller.api_v3.io.OperatorIO;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.api.io.BusVehicleIO;
import org.in.com.controller.web.io.RoleIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TripChartDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.BusService;
import org.in.com.service.ScheduleVisibilityService;
import org.in.com.service.SearchService;
import org.in.com.service.TripService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/json/{operatorCode}/{username}/{apiToken}")
public class ApiController extends BaseController {
	public static Map<String, Integer> ConcurrentRequests = new ConcurrentHashMap<String, Integer>();

	@Autowired
	AuthService authService;
	@Autowired
	TripService tripService;
	@Autowired
	SearchService searchService;
	@Autowired
	BusService busService;
	@Autowired
	ScheduleVisibilityService visibilityService;

	@RequestMapping(value = "/schedule/{tripDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripIO>> getActiveTripSchedule(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("tripDate") String tripDate) throws Exception {
		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
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
			Eventlist.add(MenuEventEM.REPORT_TRIP_CHART_RIGHTS_ALL);
			MenuEventDTO MinsMenuEventDTO = getPrivilegeV2(authDTO, Eventlist);

			List<TripDTO> rightFinalList = new ArrayList<>();
			if (MinsMenuEventDTO != null && MinsMenuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
				List<ScheduleDTO> rightsSchedule = visibilityService.getUserActiveSchedule(authDTO);
				for (ScheduleDTO rightsscheduleDTO : rightsSchedule) {
					for (Iterator<TripDTO> scheIterator = list.iterator(); scheIterator.hasNext();) {
						TripDTO tripDTO = scheIterator.next();
						if (tripDTO.getSchedule().getId() == rightsscheduleDTO.getId()) {
							rightFinalList.add(tripDTO);
							break;
						}
					}
				}
			}
			else {
				rightFinalList.addAll(list);
			}
			Eventlist.clear();
			Eventlist.add(MenuEventEM.REPORT_TRIP_CHART_RIGHTS_30);
			List<TripDTO> finalList = new ArrayList<>();
			MenuEventDTO visibilityMenuEventDTO = getPrivilegeV2(authDTO, Eventlist);
			if (visibilityMenuEventDTO != null && visibilityMenuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
				DateTime now = DateTime.now(TimeZone.getDefault());
				for (TripDTO tripDTO : rightFinalList) {
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
				finalList.addAll(rightFinalList);
			}
			for (TripDTO tripDTO : finalList) {
				TripIO tripIO = new TripIO();
				ScheduleIO schedule = new ScheduleIO();
				schedule.setCode(tripDTO.getSchedule().getCode());
				schedule.setName(tripDTO.getSchedule().getName());
				schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
				schedule.setActiveFrom(tripDTO.getSchedule().getActiveFrom());
				schedule.setActiveTo(tripDTO.getSchedule().getActiveTo());
				schedule.setDisplayName(tripDTO.getSchedule().getDisplayName());
				schedule.setPnrStartCode(tripDTO.getSchedule().getPnrStartCode());
//				schedule.setMobileTicketFlag(tripDTO.getSchedule().getMobileTicketFlag());
				schedule.setDayOfWeek(tripDTO.getSchedule().getDayOfWeek());
//				schedule.setBoardingReportingMinitues(tripDTO.getSchedule().getBoardingReportingMinitues());
//				schedule.setServiceTax(tripDTO.getSchedule().getAcBusTax());
				tripIO.setSchedule(schedule);

				// Bus
				BusIO busIO = new BusIO();
				busIO.setName(tripDTO.getBus().getName());
				busIO.setCategoryCode(tripDTO.getBus().getCategoryCode() == null ? "" : tripDTO.getBus().getCategoryCode());
				busIO.setBusType(busService.getBusCategoryByCode(tripDTO.getBus().getCategoryCode()));
				busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
				busIO.setTotalSeatCount(tripDTO.getBus().getReservableLayoutSeatCount());
				busIO.setCode(tripDTO.getBus().getCode());
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
				if (tripDTO.getTripStatus() != null) {
					tripStatusIO.setCode(tripDTO.getTripStatus().getCode());
					tripStatusIO.setName(tripDTO.getTripStatus().getName());
				}
				tripIO.setTripStatus(tripStatusIO);
				// Copy Trip informations
				if (tripDTO.getTripInfo() != null) {
					TripInfoIO tripInfo = new TripInfoIO();
					tripInfo.setDriverMobile(tripDTO.getTripInfo().getDriverMobile());
					tripInfo.setDriverName(tripDTO.getTripInfo().getDriverName());
					tripInfo.setRemarks(tripDTO.getTripInfo().getRemarks());
					tripInfo.setNotificationStatus(tripDTO.getTripInfo().getNotificationStatusCodes().split(","));
					if (tripDTO.getTripInfo().getBusVehicle() != null) {
						BusVehicleIO busVehicleIO = new BusVehicleIO();
						busVehicleIO.setName(tripDTO.getTripInfo().getBusVehicle().getName());
						busVehicleIO.setCode(tripDTO.getTripInfo().getBusVehicle().getCode());
						busVehicleIO.setRegistrationDate(tripDTO.getTripInfo().getBusVehicle().getRegistrationDate());
						busVehicleIO.setRegistationNumber(tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
						busVehicleIO.setLicNumber(tripDTO.getTripInfo().getBusVehicle().getLicNumber());
						busVehicleIO.setGpsDeviceCode(tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode());
						tripInfo.setBusVehicle(busVehicleIO);
					}
					tripIO.setTripInfo(tripInfo);
				}
				OperatorIO operatorIO = new OperatorIO();
				operatorIO.setCode(authDTO.getNamespace().getCode());
				operatorIO.setName(authDTO.getNamespace().getName());
				tripIO.setOperator(operatorIO);

				tripList.add(tripIO);
			} // Sorting Trips
			Collections.sort(tripList, new Comparator<TripIO>() {
				@Override
				public int compare(TripIO t1, TripIO t2) {
					return new CompareToBuilder().append(t1.getSchedule().getName(), t2.getSchedule().getName()).toComparison();
				}
			});

		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/schedule/trip/{tripCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TripChartIO> getTripPassengerDetails(@PathVariable("operatorCode") String operatorCode, @PathVariable("username") String username, @PathVariable("apiToken") String apiToken, @PathVariable("tripCode") String tripCode) throws Exception {

		TripChartIO tripchartIO = new TripChartIO();
		TripChartDTO tripChartDTO = new TripChartDTO();

		AuthDTO authDTO = authService.APIAuthendtication(operatorCode, username, apiToken);
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);

			tripChartDTO = tripService.getTripChart(authDTO, tripDTO);
			Map<Integer, StationPointDTO> boardingStationPointMap = new HashMap<Integer, StationPointDTO>();
			Map<Integer, StationPointDTO> droppingStationPointMap = new HashMap<Integer, StationPointDTO>();

			TripIO tripIO = new TripIO();
			ScheduleIO schedule = new ScheduleIO();
			schedule.setCode(tripDTO.getSchedule().getCode());
			schedule.setName(tripDTO.getSchedule().getName());
			schedule.setServiceNumber(tripDTO.getSchedule().getServiceNumber());
			schedule.setActiveFrom(tripDTO.getSchedule().getActiveFrom());
			schedule.setActiveTo(tripDTO.getSchedule().getActiveTo());
			schedule.setDisplayName(tripDTO.getSchedule().getDisplayName());
			schedule.setPnrStartCode(tripDTO.getSchedule().getPnrStartCode());
//			schedule.setMobileTicketFlag(tripDTO.getSchedule().getMobileTicketFlag());
			schedule.setDayOfWeek(tripDTO.getSchedule().getDayOfWeek());
//			schedule.setBoardingReportingMinitues(tripDTO.getSchedule().getBoardingReportingMinitues());
//			schedule.setServiceTax(tripDTO.getSchedule().getAcBusTax());
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
				seatLayoutList.add(layoutIO);
			}

			// Sorting Bus Seat Layout
			Comparator<BusSeatLayoutIO> comp = new BeanComparator("sequence");
			Collections.sort(seatLayoutList, comp);

			// Bus
			BusIO busIO = new BusIO();
			busIO.setSeatLayoutList(seatLayoutList);
			busIO.setName(tripDTO.getBus().getName());
			busIO.setCategoryCode(tripDTO.getBus().getCategoryCode() == null ? "" : tripDTO.getBus().getCategoryCode());
			busIO.setDisplayName(tripDTO.getBus().getDisplayName() == null ? "" : tripDTO.getBus().getDisplayName());
			tripIO.setBus(busIO);

			List<StageIO> stageList = new ArrayList<>();
			// Sorting
			Comparator<StageDTO> comp1 = new BeanComparator("stageSequence");
			Collections.sort(tripDTO.getStageList(), comp1);

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
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getFromStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
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
					pointIO.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), stageDTO.getToStation().getMinitues() + pointDTO.getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
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
			tripchartIO.setTrip(tripIO);
			OperatorIO operatorIO = new OperatorIO();
			operatorIO.setCode(authDTO.getNamespace().getCode());
			operatorIO.setName(authDTO.getNamespace().getName());
			tripIO.setOperator(operatorIO);
			
			List<TripChartDetailsIO> ticketDetailsList = new ArrayList<TripChartDetailsIO>();
			for (TripChartDetailsDTO chartDTO : tripChartDTO.getTicketDetailsList()) {
				TripChartDetailsIO io = new TripChartDetailsIO();
				io.setSeatName(chartDTO.getSeatName());
				io.setTicketCode(chartDTO.getTicketCode());
				io.setBookedDate(chartDTO.getTicketAt().toString());
				io.setPassengerName(chartDTO.getPassengerName());
				io.setPassengerAge(chartDTO.getPassengerAge());
				io.setGender(chartDTO.getSeatGendar().getCode());
				io.setPassengerMobile(chartDTO.getPassengerMobile());
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
				StationPointIO boardingStationPoint = new StationPointIO();
				StationPointIO droppingStationPoint = new StationPointIO();
				if (boardingStationPointMap.get(chartDTO.getBoardingPoint().getId()) != null) {
					StationPointDTO stationPointDTO = boardingStationPointMap.get(chartDTO.getBoardingPoint().getId());
					boardingStationPoint.setName(stationPointDTO.getName());
					boardingStationPoint.setCode(stationPointDTO.getCode());
					boardingStationPoint.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), chartDTO.getTravelMinutes() + chartDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					boardingStationPoint.setAddress(stationPointDTO.getAddress());
					boardingStationPoint.setLandmark(stationPointDTO.getLandmark());
				}
				else {
					boardingStationPoint.setName("Others");
					boardingStationPoint.setCode("Others");
					boardingStationPoint.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), chartDTO.getTravelMinutes() + chartDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				if (droppingStationPointMap.get(chartDTO.getDroppingPoint().getId()) != null) {
					StationPointDTO stationPointDTO = droppingStationPointMap.get(chartDTO.getDroppingPoint().getId());
					droppingStationPoint.setName(stationPointDTO.getName());
					droppingStationPoint.setCode(stationPointDTO.getCode());
					droppingStationPoint.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), chartDTO.getTravelMinutes() + chartDTO.getDroppingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					droppingStationPoint.setAddress(stationPointDTO.getAddress());
					droppingStationPoint.setLandmark(stationPointDTO.getLandmark());
				}
				else {
					droppingStationPoint.setName("Others");
					droppingStationPoint.setCode("Others");
					droppingStationPoint.setDateTime(DateUtil.addMinituesToDate(tripDTO.getTripDate(), chartDTO.getTravelMinutes() + chartDTO.getDroppingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
				}
				io.setBoardingPoint(boardingStationPoint);
				io.setDroppingPoint(droppingStationPoint);
				UserIO userIO = new UserIO();
				userIO.setUsername(chartDTO.getUser().getUsername());
				userIO.setName(chartDTO.getUser().getName());
				userIO.setLastname(chartDTO.getUser().getLastname());
				RoleIO role = new RoleIO();
				role.setCode(chartDTO.getUser().getUserRole().getCode());
				role.setName(chartDTO.getUser().getUserRole().getName());
				userIO.setRole(role);

				io.setBookedBy(userIO);
				io.setBookedType(chartDTO.getBookingType());
				ticketDetailsList.add(io);
			}
			tripchartIO.setTicketDetailsList(ticketDetailsList);
			if (tripDTO.getTripInfo() != null) {
				tripchartIO.setDriverName(tripDTO.getTripInfo().getDriverName() == null ? "NA" : tripDTO.getTripInfo().getDriverName());
				tripchartIO.setDriverPhoneNumber(tripDTO.getTripInfo().getDriverMobile() == null ? "NA" : tripDTO.getTripInfo().getDriverMobile());
				tripchartIO.setRemarks(tripDTO.getTripInfo().getRemarks() == null ? "NA" : tripDTO.getTripInfo().getRemarks());
			}

		return ResponseIO.success(tripchartIO);
	}
}
