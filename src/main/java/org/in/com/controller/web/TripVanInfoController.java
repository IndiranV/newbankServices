package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusVehicleIO;
import org.in.com.controller.web.io.BusVehicleVanPickupIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.TripVanExceptionIO;
import org.in.com.controller.web.io.TripVanInfoIO;
import org.in.com.controller.web.io.VehicleDriverIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TripVanExceptionDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.service.AuthService;
import org.in.com.service.TripVanInfoService;
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

@Controller
@RequestMapping("/{authtoken}/trip/van/info")
public class TripVanInfoController {

	@Autowired
	TripVanInfoService tripVanInfoService;
	@Autowired
	AuthService authService;

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TripVanInfoIO> updateTripVanInfo(@PathVariable("authtoken") String authtoken, @RequestBody TripVanInfoIO tripVanInfo) {
		TripVanInfoIO tripVanInfoIO = new TripVanInfoIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TripVanInfoDTO tripVanInfoDTO = new TripVanInfoDTO();

		tripVanInfoDTO.setCode(tripVanInfo.getCode());
		tripVanInfoDTO.setTripDate(DateUtil.getDateTime(tripVanInfo.getTripDate()));
		tripVanInfoDTO.setMobileNumber(tripVanInfo.getMobileNumber());

		BusVehicleDTO vehicleDTO = new BusVehicleDTO();
		vehicleDTO.setCode(tripVanInfo.getVehicle().getCode());
		tripVanInfoDTO.setVehicle(vehicleDTO);

		BusVehicleDriverDTO driverDTO = new BusVehicleDriverDTO();
		driverDTO.setCode(tripVanInfo.getDriver().getCode());
		tripVanInfoDTO.setDriver(driverDTO);

		BusVehicleVanPickupDTO vanPickupDTO = new BusVehicleVanPickupDTO();
		vanPickupDTO.setCode(tripVanInfo.getVanPickup().getCode());
		tripVanInfoDTO.setVanPickup(vanPickupDTO);

		tripVanInfoDTO.setActiveFlag(tripVanInfo.getActiveFlag());
		tripVanInfoService.updateTripVanInfo(authDTO, tripVanInfoDTO);
		tripVanInfoIO.setCode(tripVanInfoDTO.getCode());
		tripVanInfoIO.setActiveFlag(tripVanInfoDTO.getActiveFlag());
		return ResponseIO.success(tripVanInfoIO);
	}

	@RequestMapping(value = "/{vanPickupCode}/{tripDate}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TripVanInfoIO> getTripVanInfo(@PathVariable("authtoken") String authtoken, @PathVariable("vanPickupCode") String vanPickupCode, @PathVariable("tripDate") String tripDate) {
		TripVanInfoIO tripVanInfoIO = new TripVanInfoIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		TripVanInfoDTO vanInfoDTO = new TripVanInfoDTO();
		vanInfoDTO.setTripDate(DateUtil.getDateTime(tripDate));

		BusVehicleVanPickupDTO vanPickupDTO = new BusVehicleVanPickupDTO();
		vanPickupDTO.setCode(vanPickupCode);
		vanInfoDTO.setVanPickup(vanPickupDTO);

		TripVanInfoDTO tripVanInfoDTO = tripVanInfoService.getTripVanInfo(authDTO, vanInfoDTO);
		tripVanInfoIO.setCode(tripVanInfoDTO.getCode());
		tripVanInfoIO.setMobileNumber(tripVanInfoDTO.getMobileNumber());

		if (StringUtil.isNotNull(tripVanInfoDTO.getCode())) {
			BusVehicleIO busVehicleIO = new BusVehicleIO();
			busVehicleIO.setCode(tripVanInfoDTO.getVehicle().getCode());
			busVehicleIO.setName(tripVanInfoDTO.getVehicle().getName());
			tripVanInfoIO.setVehicle(busVehicleIO);

			VehicleDriverIO vehicleDriverIO = new VehicleDriverIO();
			vehicleDriverIO.setCode(tripVanInfoDTO.getDriver().getCode());
			vehicleDriverIO.setName(tripVanInfoDTO.getDriver().getName());
			tripVanInfoIO.setDriver(vehicleDriverIO);

			BusVehicleVanPickupIO vanPickupIO = new BusVehicleVanPickupIO();
			vanPickupIO.setCode(tripVanInfoDTO.getVanPickup().getCode());
			vanPickupIO.setName(tripVanInfoDTO.getVanPickup().getName());
			tripVanInfoIO.setVanPickup(vanPickupIO);
		}
		if (tripVanInfoDTO.getNotificationType() != null) {
			BaseIO notificationStatus = new BaseIO();
			notificationStatus.setCode(tripVanInfoDTO.getNotificationType().getCode());
			notificationStatus.setName(tripVanInfoDTO.getNotificationType().getDescription());
			tripVanInfoIO.setNotificationStatus(notificationStatus);
		}

		tripVanInfoIO.setActiveFlag(tripVanInfoDTO.getActiveFlag());
		
		TripVanExceptionIO tripVanExceptionIO = new TripVanExceptionIO();
		if (tripVanInfoDTO.getTripVanException() != null) {
			tripVanExceptionIO.setCode(tripVanInfoDTO.getTripVanException().getCode());
			tripVanExceptionIO.setTripDate(DateUtil.convertDate(tripVanInfoDTO.getTripVanException().getTripDate()));
			
			BusVehicleVanPickupIO vanPickupExceptionIO = new BusVehicleVanPickupIO();
			vanPickupExceptionIO.setCode(tripVanInfoDTO.getTripVanException().getVanPickup().getCode());
			vanPickupExceptionIO.setName(tripVanInfoDTO.getTripVanException().getVanPickup().getName());
			
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
			tripVanExceptionIO.setVanPickup(vanPickupExceptionIO);
		}
		tripVanInfoIO.setTripVanException(tripVanExceptionIO);
		return ResponseIO.success(tripVanInfoIO);
	}
	
	@RequestMapping(value = "/exception/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TripVanExceptionIO> updateTripVanException(@PathVariable("authtoken") String authtoken, @RequestBody TripVanExceptionIO tripVanInfo) {
		TripVanExceptionIO tripVanExceptionIO = new TripVanExceptionIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TripVanExceptionDTO tripVanExceptionDTO = new TripVanExceptionDTO();

		tripVanExceptionDTO.setCode(tripVanInfo.getCode());
		tripVanExceptionDTO.setTripDate(DateUtil.getDateTime(tripVanInfo.getTripDate()));

		BusVehicleVanPickupDTO vanPickupDTO = new BusVehicleVanPickupDTO();
		vanPickupDTO.setCode(tripVanInfo.getVanPickup().getCode());
	
		List<ScheduleDTO> scheduleList = new ArrayList<>();
		if (tripVanInfo.getSchedules() != null) {
			for (ScheduleIO scheduleIO : tripVanInfo.getSchedules()) {
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(scheduleIO.getCode());
				scheduleList.add(scheduleDTO);
			}
		}
		tripVanExceptionDTO.setSchedules(scheduleList);
		tripVanExceptionDTO.setVanPickup(vanPickupDTO);

		tripVanExceptionDTO.setActiveFlag(tripVanInfo.getActiveFlag());
		tripVanInfoService.updateTripVanException(authDTO, tripVanExceptionDTO);
		tripVanExceptionIO.setCode(tripVanExceptionDTO.getCode());
		return ResponseIO.success(tripVanExceptionIO);
	}

}
