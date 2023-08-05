package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.AuditIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusIO;
import org.in.com.controller.web.io.BusVehicleIO;
import org.in.com.controller.web.io.BusVehicleVanPickupIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.RoleIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.StationPointIO;
import org.in.com.controller.web.io.TripChartDetailsIO;
import org.in.com.controller.web.io.TripPickupVanChartIO;
import org.in.com.controller.web.io.TripStatusIO;
import org.in.com.controller.web.io.TripVanExceptionIO;
import org.in.com.controller.web.io.TripVanInfoIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.controller.web.io.VehicleDriverIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TripChartDetailsDTO;
import org.in.com.dto.TripPickupVanChartDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.service.AuthService;
import org.in.com.service.BusVehicleVanPickupService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/bus/vehicle")
public class BusVehicleVanPickupController extends BaseController {
	@Autowired
	AuthService authService;
	@Autowired
	BusVehicleVanPickupService busVehicleVanService;

	@RequestMapping(value = "/pickup/van/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BusVehicleVanPickupIO> updateBusVehicleVanPickup(@PathVariable("authtoken") String authtoken, @RequestBody BusVehicleVanPickupIO vanRoute) throws Exception {
		BusVehicleVanPickupIO vanRouteIO = new BusVehicleVanPickupIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			BusVehicleVanPickupDTO vanRouteDTO = new BusVehicleVanPickupDTO();
			vanRouteDTO.setCode(vanRoute.getCode());
			vanRouteDTO.setName(vanRoute.getName());

			StationDTO station = new StationDTO();
			station.setCode(vanRoute.getStation().getCode());
			vanRouteDTO.setStation(station);

			vanRouteDTO.setTripStatus(vanRoute.getTripStatus() != null ? TripStatusEM.getTripStatusEM(vanRoute.getTripStatus().getCode()) : TripStatusEM.TRIP_OPEN);
			vanRouteDTO.setSeatCount(vanRoute.getSeatCount());
			vanRouteDTO.setActiveFlag(vanRoute.getActiveFlag());
			busVehicleVanService.updateBusVehicleVanPickup(authDTO, vanRouteDTO);

			vanRouteIO.setCode(vanRouteDTO.getCode());
			vanRouteIO.setName(vanRouteDTO.getName());
			vanRouteIO.setActiveFlag(vanRouteDTO.getActiveFlag());
		}
		return ResponseIO.success(vanRouteIO);
	}

	@RequestMapping(value = "/pickup/van/{code}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BusVehicleVanPickupIO> getBusVehicleVanPickup(@PathVariable("authtoken") String authtoken, @PathVariable("code") String code) throws Exception {
		BusVehicleVanPickupIO vanRoute = new BusVehicleVanPickupIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			BusVehicleVanPickupDTO vanRouteDTO = new BusVehicleVanPickupDTO();
			vanRouteDTO.setCode(code);

			busVehicleVanService.getBusVehicleVanPickup(authDTO, vanRouteDTO);

			vanRoute.setCode(vanRouteDTO.getCode());
			vanRoute.setName(vanRouteDTO.getName());

			StationIO station = new StationIO();
			station.setCode(vanRouteDTO.getStation().getCode());
			vanRoute.setStation(station);

			TripStatusIO tripStatus = new TripStatusIO();
			tripStatus.setCode(vanRouteDTO.getTripStatus().getCode());
			tripStatus.setName(vanRouteDTO.getTripStatus().getName());
			vanRoute.setTripStatus(tripStatus);

			vanRoute.setSeatCount(vanRouteDTO.getSeatCount());
			vanRoute.setActiveFlag(vanRouteDTO.getActiveFlag());
		}
		return ResponseIO.success(vanRoute);
	}

	@RequestMapping(value = "/pickup/van", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BusVehicleVanPickupIO>> getAllBusVehicleVanPickup(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<BusVehicleVanPickupIO> vanPickup = new ArrayList<BusVehicleVanPickupIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<BusVehicleVanPickupDTO> list = busVehicleVanService.getAllBusVehicleVanPickup(authDTO);
			for (BusVehicleVanPickupDTO vanRouteDTO : list) {
				if (activeFlag != -1 && activeFlag != vanRouteDTO.getActiveFlag()) {
					continue;
				}
				BusVehicleVanPickupIO vanRoute = new BusVehicleVanPickupIO();
				vanRoute.setCode(vanRouteDTO.getCode());
				vanRoute.setName(vanRouteDTO.getName());

				StationIO station = new StationIO();
				station.setCode(vanRouteDTO.getStation().getCode());
				vanRoute.setStation(station);

				TripStatusIO tripStatus = new TripStatusIO();
				tripStatus.setCode(vanRouteDTO.getTripStatus().getCode());
				tripStatus.setName(vanRouteDTO.getTripStatus().getName());
				vanRoute.setTripStatus(tripStatus);

				vanRoute.setSeatCount(vanRouteDTO.getSeatCount());

				vanRoute.setActiveFlag(vanRouteDTO.getActiveFlag());
				vanPickup.add(vanRoute);
			}
		}
		return ResponseIO.success(vanPickup);
	}

	@RequestMapping(value = "/pickup/van/station/{code}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BusVehicleVanPickupIO>> getStationBusVehicleVanPickup(@PathVariable("authtoken") String authtoken, @PathVariable("code") String code) throws Exception {
		List<BusVehicleVanPickupIO> vanPickup = new ArrayList<BusVehicleVanPickupIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			StationDTO stationDTO = new StationDTO();
			stationDTO.setCode(code);
			List<BusVehicleVanPickupDTO> list = busVehicleVanService.getByStationId(authDTO, stationDTO);
			for (BusVehicleVanPickupDTO vanRouteDTO : list) {
				BusVehicleVanPickupIO vanRoute = new BusVehicleVanPickupIO();
				vanRoute.setCode(vanRouteDTO.getCode());
				vanRoute.setName(vanRouteDTO.getName());
				vanRoute.setActiveFlag(vanRouteDTO.getActiveFlag());
				vanPickup.add(vanRoute);
			}
		}
		return ResponseIO.success(vanPickup);
	}

	@RequestMapping(value = "/pickup/van/active/trip/{tripDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BusVehicleVanPickupIO>> getActiveVanPickupTrips(@PathVariable("authtoken") String authtoken, @PathVariable("tripDate") String tripDate) throws Exception {
		List<BusVehicleVanPickupIO> trips = new ArrayList<BusVehicleVanPickupIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		SearchDTO searchDTO = new SearchDTO();
		searchDTO.setTravelDate(DateUtil.getDateTime(tripDate));

		List<TripVanInfoDTO> vanPickupTrips = busVehicleVanService.getActiveVanPickupTrips(authDTO, searchDTO);

		for (TripVanInfoDTO tripVanInfoDTO : vanPickupTrips) {
			BusVehicleVanPickupDTO vanRouteDTO = tripVanInfoDTO.getVanPickup();

			BusVehicleVanPickupIO vanRoute = new BusVehicleVanPickupIO();
			vanRoute.setCode(vanRouteDTO.getCode());
			vanRoute.setName(vanRouteDTO.getName());

			TripStatusIO tripStatus = new TripStatusIO();
			tripStatus.setCode(vanRouteDTO.getTripStatus().getCode());
			tripStatus.setName(vanRouteDTO.getTripStatus().getName());
			vanRoute.setTripStatus(tripStatus);

			vanRoute.setSeatCount(vanRouteDTO.getSeatCount());

			StationIO station = new StationIO();
			station.setCode(vanRouteDTO.getStation().getCode());
			station.setName(vanRouteDTO.getStation().getName());

			List<StationPointIO> stationPoints = new ArrayList<>();
			if (vanRouteDTO.getStation().getStationPoints() != null) {
				for (StationPointDTO pointDTO : vanRouteDTO.getStation().getStationPoints()) {
					StationPointIO pointIO = new StationPointIO();
					pointIO.setCode(pointDTO.getCode());
					pointIO.setName(pointDTO.getName());
					pointIO.setLandmark(pointDTO.getLandmark());
					pointIO.setAddress(pointDTO.getAddress());
					pointIO.setNumber(pointDTO.getNumber());
					pointIO.setLatitude(pointDTO.getLatitude() == null ? "" : pointDTO.getLatitude());
					pointIO.setLongitude(pointDTO.getLongitude() == null ? "" : pointDTO.getLongitude());
					pointIO.setSeatCount(pointDTO.getSeatCount());
					stationPoints.add(pointIO);
				}
			}
			station.setStationPoint(stationPoints);
			vanRoute.setStation(station);

			vanRoute.setActiveFlag(vanRouteDTO.getActiveFlag());

			TripVanInfoIO tripVanInfoIO = new TripVanInfoIO();
			tripVanInfoIO.setCode(tripVanInfoDTO.getCode());
			tripVanInfoIO.setMobileNumber(tripVanInfoDTO.getMobileNumber());

			if (StringUtil.isNotNull(tripVanInfoDTO.getCode())) {
				BusVehicleIO busVehicleIO = new BusVehicleIO();
				busVehicleIO.setCode(tripVanInfoDTO.getVehicle().getCode());
				busVehicleIO.setName(tripVanInfoDTO.getVehicle().getName());
				busVehicleIO.setRegistationNumber(tripVanInfoDTO.getVehicle().getRegistationNumber());

				BusIO busIO = new BusIO();
				busIO.setName(tripVanInfoDTO.getVehicle().getBus().getName());
				busIO.setCategoryCode(tripVanInfoDTO.getVehicle().getBus().getCategoryCode() == null ? "" : tripVanInfoDTO.getVehicle().getBus().getCategoryCode());
				busIO.setBusType(BitsUtil.getBusCategoryUsingEM(tripVanInfoDTO.getVehicle().getBus().getCategoryCode()));
				busIO.setCode(tripVanInfoDTO.getVehicle().getBus().getCode());
				busVehicleIO.setBus(busIO);
				tripVanInfoIO.setVehicle(busVehicleIO);

				VehicleDriverIO vehicleDriverIO = new VehicleDriverIO();
				vehicleDriverIO.setCode(tripVanInfoDTO.getDriver().getCode());
				vehicleDriverIO.setName(tripVanInfoDTO.getDriver().getName());
				tripVanInfoIO.setDriver(vehicleDriverIO);

				BusVehicleVanPickupIO vanPickupIO = new BusVehicleVanPickupIO();
				vanPickupIO.setCode(tripVanInfoDTO.getVanPickup().getCode());
				vanPickupIO.setName(tripVanInfoDTO.getVanPickup().getName());
				tripVanInfoIO.setVanPickup(vanPickupIO);

				if (tripVanInfoDTO.getNotificationType() != null) {
					BaseIO notificationStatus = new BaseIO();
					notificationStatus.setCode(tripVanInfoDTO.getNotificationType().getCode());
					notificationStatus.setName(tripVanInfoDTO.getNotificationType().getDescription());
					tripVanInfoIO.setNotificationStatus(notificationStatus);
				}

				tripVanInfoIO.setActiveFlag(tripVanInfoDTO.getActiveFlag());
			}
			
			TripVanExceptionIO tripVanExceptionIO = new TripVanExceptionIO();
			if (tripVanInfoDTO.getTripVanException() != null) {
				tripVanExceptionIO.setCode(tripVanInfoDTO.getTripVanException().getCode());
				tripVanExceptionIO.setTripDate(DateUtil.convertDate(tripVanInfoDTO.getTripVanException().getTripDate()));
				
				BusVehicleVanPickupIO vanPickupExceptionIO = new BusVehicleVanPickupIO();
				vanPickupExceptionIO.setCode(tripVanInfoDTO.getTripVanException().getVanPickup().getCode());
				vanPickupExceptionIO.setName(tripVanInfoDTO.getTripVanException().getVanPickup().getName());
				tripVanExceptionIO.setVanPickup(vanPickupExceptionIO);
				
				List<ScheduleIO> exceptionSchedules = new ArrayList<>();
				for (ScheduleDTO exceptionScheduleDTO : tripVanInfoDTO.getTripVanException().getSchedules()) {
					ScheduleIO exceptionScheduleIO = new ScheduleIO();
					exceptionScheduleIO.setCode(exceptionScheduleDTO.getCode());
					exceptionScheduleIO.setName(exceptionScheduleDTO.getName());
					exceptionScheduleIO.setServiceNumber(exceptionScheduleDTO.getServiceNumber());
					exceptionScheduleIO.setDisplayName(exceptionScheduleDTO.getDisplayName());
					exceptionSchedules.add(exceptionScheduleIO);
				}
				tripVanExceptionIO.setSchedules(exceptionSchedules);
				
				AuditIO audit = new AuditIO();
				if (tripVanInfoDTO.getTripVanException().getAudit() != null && tripVanInfoDTO.getTripVanException().getAudit().getUser() != null) {
					UserIO updatedBy = new UserIO();
					updatedBy.setCode(tripVanInfoDTO.getTripVanException().getAudit().getUser().getCode());
					updatedBy.setName(tripVanInfoDTO.getTripVanException().getAudit().getUser().getName());
					audit.setUser(updatedBy);
					audit.setUpdatedAt(tripVanInfoDTO.getTripVanException().getAudit().getUpdatedAt());
				}
				tripVanExceptionIO.setAudit(audit);
			}
			tripVanInfoIO.setTripVanException(tripVanExceptionIO);
			vanRoute.setTripVanInfo(tripVanInfoIO);

			List<ScheduleIO> schedules = new ArrayList<>();
			for (ScheduleDTO scheduleDTO : vanRouteDTO.getSchedules()) {
				ScheduleIO schedule = new ScheduleIO();
				schedule.setCode(scheduleDTO.getCode());
				schedule.setName(scheduleDTO.getName());
				schedule.setServiceNumber(scheduleDTO.getServiceNumber());
				schedule.setDisplayName(scheduleDTO.getDisplayName());
				schedules.add(schedule);
			}
			vanRoute.setSchedules(schedules);

			trips.add(vanRoute);
		}
		return ResponseIO.success(trips);
	}

	@RequestMapping(value = "/pickup/van/{vanPickupCode}/trip/{tripDate}/chart", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TripPickupVanChartIO> getActiveVanPickupTripChart(@PathVariable("authtoken") String authtoken, @PathVariable("vanPickupCode") String vanPickupCode, @PathVariable("tripDate") String tripDate) throws Exception {
		TripPickupVanChartIO tripPickupVanChart = new TripPickupVanChartIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		SearchDTO searchDTO = new SearchDTO();
		searchDTO.setTravelDate(DateUtil.getDateTime(tripDate));

		BusVehicleVanPickupDTO busVehicleVanPickup = new BusVehicleVanPickupDTO();
		busVehicleVanPickup.setCode(vanPickupCode);

		TripPickupVanChartDTO tripPickupVanChartDTO = busVehicleVanService.getActiveVanPickupTripChart(authDTO, searchDTO, busVehicleVanPickup);

		List<TripChartDetailsIO> ticketDetailsList = new ArrayList<TripChartDetailsIO>();
		for (TripChartDetailsDTO tripChartDetailsDTO : tripPickupVanChartDTO.getTicketDetails()) {
			TripChartDetailsIO tripChartDetailsIO = new TripChartDetailsIO();
			tripChartDetailsIO.setSeatCode(tripChartDetailsDTO.getSeatCode());
			tripChartDetailsIO.setSeatName(tripChartDetailsDTO.getSeatName());
			tripChartDetailsIO.setTicketCode(tripChartDetailsDTO.getTicketCode());
			tripChartDetailsIO.setBookedDate(tripChartDetailsDTO.getTicketAt().toString());
			tripChartDetailsIO.setPassengerName(tripChartDetailsDTO.getPassengerName());
			tripChartDetailsIO.setPassengerAge(tripChartDetailsDTO.getPassengerAge());
			tripChartDetailsIO.setIdProof(tripChartDetailsDTO.getIdProof());
			tripChartDetailsIO.setGender(tripChartDetailsDTO.getSeatGendar().getCode());
			tripChartDetailsIO.setDeviceMedium(tripChartDetailsDTO.getDeviceMedium().getCode());
			tripChartDetailsIO.setPassengerMobile(tripChartDetailsDTO.getPassengerMobile());
			tripChartDetailsIO.setAlternateMobile(tripChartDetailsDTO.getAlternateMobile());
			tripChartDetailsIO.setSeatFare(tripChartDetailsDTO.getSeatFare());
			tripChartDetailsIO.setAcBusTax(tripChartDetailsDTO.getAcBusTax());
			tripChartDetailsIO.setRemarks(StringUtil.isNotNull(tripChartDetailsDTO.getRemarks()) ? tripChartDetailsDTO.getRemarks().replaceAll("null", "").replaceAll("-", "") : "");

			StationIO fromStationIO = new StationIO();
			fromStationIO.setCode(tripChartDetailsDTO.getFromStation().getCode());
			fromStationIO.setName(tripChartDetailsDTO.getFromStation().getName());
			tripChartDetailsIO.setFromStation(fromStationIO);

			StationIO toStationIO = new StationIO();
			toStationIO.setCode(tripChartDetailsDTO.getToStation().getCode());
			toStationIO.setName(tripChartDetailsDTO.getToStation().getName());
			tripChartDetailsIO.setToStation(toStationIO);

			tripChartDetailsIO.setTravelStatusCode(tripChartDetailsDTO.getTravelStatus().getCode());
			tripChartDetailsIO.setTicketStatusCode(tripChartDetailsDTO.getTicketStatus().getCode());

			StationPointDTO boardingStationPointDTO = tripChartDetailsDTO.getBoardingPoint();
			StationPointIO boardingStationPoint = new StationPointIO();
			boardingStationPoint.setName(boardingStationPointDTO.getName());
			boardingStationPoint.setCode(boardingStationPointDTO.getCode());
			boardingStationPoint.setAddress(boardingStationPointDTO.getAddress());
			boardingStationPoint.setLandmark(boardingStationPointDTO.getLandmark());
			boardingStationPoint.setDateTime(DateUtil.convertDateTime(DateUtil.addMinituesToDate(DateUtil.getDateTime(tripChartDetailsDTO.getTripDate()), boardingStationPointDTO.getMinitues())));
			tripChartDetailsIO.setBoardingPoint(boardingStationPoint);

			StationPointDTO droppingStationPointDTO = tripChartDetailsDTO.getDroppingPoint();
			StationPointIO droppingStationPoint = new StationPointIO();
			droppingStationPoint.setName(droppingStationPointDTO.getName());
			droppingStationPoint.setCode(droppingStationPointDTO.getCode());
			droppingStationPoint.setAddress(droppingStationPointDTO.getAddress());
			droppingStationPoint.setLandmark(droppingStationPointDTO.getLandmark());
			droppingStationPoint.setDateTime(DateUtil.convertDateTime(DateUtil.addMinituesToDate(DateUtil.getDateTime(tripChartDetailsDTO.getTripDate()), droppingStationPointDTO.getMinitues())));
			tripChartDetailsIO.setDroppingPoint(droppingStationPoint);

			UserIO userIO = new UserIO();
			userIO.setCode(tripChartDetailsDTO.getUser().getCode());
			userIO.setUsername(tripChartDetailsDTO.getUser().getUsername());
			userIO.setName(tripChartDetailsDTO.getUser().getName());
			userIO.setLastname(tripChartDetailsDTO.getUser().getLastname());
			RoleIO role = new RoleIO();
			role.setCode(tripChartDetailsDTO.getUser().getUserRole().getCode());
			role.setName(tripChartDetailsDTO.getUser().getUserRole().getName());
			userIO.setRole(role);

			GroupIO groupIO = new GroupIO();
			groupIO.setName(tripChartDetailsDTO.getUser().getGroup().getName());
			groupIO.setCode(tripChartDetailsDTO.getUser().getGroup().getCode());
			userIO.setGroup(groupIO);
			tripChartDetailsIO.setBookedBy(userIO);
			tripChartDetailsIO.setBookedType(tripChartDetailsDTO.getBookingType());
			ticketDetailsList.add(tripChartDetailsIO);
		}
		tripPickupVanChart.setTicketDetails(ticketDetailsList);

		TripVanInfoDTO tripVanInfoDTO = tripPickupVanChartDTO.getTripVanInfo();
		TripVanInfoIO tripVanInfoIO = new TripVanInfoIO();
		if (tripVanInfoDTO != null) {
			tripVanInfoIO.setCode(tripVanInfoDTO.getCode());
			tripVanInfoIO.setMobileNumber(tripVanInfoDTO.getMobileNumber());

			if (StringUtil.isNotNull(tripVanInfoDTO.getCode())) {
				BusVehicleIO busVehicleIO = new BusVehicleIO();
				busVehicleIO.setCode(tripVanInfoDTO.getVehicle().getCode());
				busVehicleIO.setName(tripVanInfoDTO.getVehicle().getName());
				busVehicleIO.setRegistationNumber(tripVanInfoDTO.getVehicle().getRegistationNumber());

				BusIO busIO = new BusIO();
				busIO.setName(tripVanInfoDTO.getVehicle().getBus().getName());
				busIO.setCategoryCode(tripVanInfoDTO.getVehicle().getBus().getCategoryCode() == null ? "" : tripVanInfoDTO.getVehicle().getBus().getCategoryCode());
				busIO.setBusType(BitsUtil.getBusCategoryUsingEM(tripVanInfoDTO.getVehicle().getBus().getCategoryCode()));
				busIO.setCode(tripVanInfoDTO.getVehicle().getBus().getCode());
				busVehicleIO.setBus(busIO);
				tripVanInfoIO.setVehicle(busVehicleIO);

				VehicleDriverIO vehicleDriverIO = new VehicleDriverIO();
				vehicleDriverIO.setCode(tripVanInfoDTO.getDriver().getCode());
				vehicleDriverIO.setName(tripVanInfoDTO.getDriver().getName());
				tripVanInfoIO.setDriver(vehicleDriverIO);

				BusVehicleVanPickupIO vanPickupIO = new BusVehicleVanPickupIO();
				vanPickupIO.setCode(tripVanInfoDTO.getVanPickup().getCode());
				vanPickupIO.setName(tripVanInfoDTO.getVanPickup().getName());

				List<ScheduleIO> schedules = new ArrayList<>();
				for (ScheduleDTO scheduleDTO : tripVanInfoDTO.getVanPickup().getSchedules()) {
					ScheduleIO schedule = new ScheduleIO();
					schedule.setCode(scheduleDTO.getCode());
					schedule.setName(scheduleDTO.getName());
					schedule.setServiceNumber(scheduleDTO.getServiceNumber());
					schedule.setDisplayName(scheduleDTO.getDisplayName());
					schedules.add(schedule);
				}
				vanPickupIO.setSchedules(schedules);

				tripVanInfoIO.setVanPickup(vanPickupIO);

				if (tripVanInfoDTO.getNotificationType() != null) {
					BaseIO notificationStatus = new BaseIO();
					notificationStatus.setCode(tripVanInfoDTO.getNotificationType().getCode());
					notificationStatus.setName(tripVanInfoDTO.getNotificationType().getDescription());
					tripVanInfoIO.setNotificationStatus(notificationStatus);
				}

				tripVanInfoIO.setActiveFlag(tripVanInfoDTO.getActiveFlag());
			}
			tripPickupVanChart.setTripVanInfo(tripVanInfoIO);
		}
		return ResponseIO.success(tripPickupVanChart);
	}
}
