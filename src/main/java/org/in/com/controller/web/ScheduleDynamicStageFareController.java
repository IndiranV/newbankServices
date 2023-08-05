package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusSeatLayoutIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.ScheduleDynamicStageFareDetailsIO;
import org.in.com.controller.web.io.ScheduleDynamicStageFareIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.DynamicPriceProviderEM;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/{authtoken}/schedule/dynamic")
public class ScheduleDynamicStageFareController extends BaseController {
	@Autowired
	ScheduleDynamicStageFareService dynamicStageFareService;

	@RequestMapping(value = "/stagefare/{schedulecode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleDynamicStageFareIO>> getScheduleSeatFare(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleDynamicStageFareIO> seatFareList = new ArrayList<ScheduleDynamicStageFareIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDynamicStageFareDTO scheduleSeatFare = new ScheduleDynamicStageFareDTO();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(schedulecode);
			scheduleSeatFare.setSchedule(scheduleDTO);
			List<ScheduleDynamicStageFareDTO> list = dynamicStageFareService.getScheduleStageFare(authDTO, scheduleDTO);
			for (ScheduleDynamicStageFareDTO seatFareDTO : list) {
				ScheduleDynamicStageFareIO stageFare = new ScheduleDynamicStageFareIO();

				ScheduleIO schedule = new ScheduleIO();
				schedule.setCode(scheduleDTO.getCode());
				stageFare.setSchedule(schedule);

				BaseIO dynamicPriceProvider = new BaseIO();
				dynamicPriceProvider.setCode(seatFareDTO.getDynamicPriceProvider().getCode());
				dynamicPriceProvider.setName(seatFareDTO.getDynamicPriceProvider().getName());
				stageFare.setDynamicPriceProvider(dynamicPriceProvider);

				stageFare.setCode(seatFareDTO.getCode());
				stageFare.setActiveFrom(seatFareDTO.getActiveFrom());
				stageFare.setActiveTo(seatFareDTO.getActiveTo());
				stageFare.setDayOfWeek(seatFareDTO.getDayOfWeek());
				stageFare.setStatus(seatFareDTO.getStatus());
				stageFare.setActiveFlag(seatFareDTO.getActiveFlag());

				List<ScheduleDynamicStageFareDetailsIO> fareDetails = new ArrayList<ScheduleDynamicStageFareDetailsIO>();
				for (ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails : seatFareDTO.getStageFare()) {
					ScheduleDynamicStageFareDetailsIO scheduleDynamicStageFareDetails = new ScheduleDynamicStageFareDetailsIO();
					scheduleDynamicStageFareDetails.setCode(dynamicStageFareDetails.getCode());
					scheduleDynamicStageFareDetails.setMinFare(dynamicStageFareDetails.getMinFare());
					scheduleDynamicStageFareDetails.setMaxFare(dynamicStageFareDetails.getMaxFare());
					scheduleDynamicStageFareDetails.setActiveFlag(dynamicStageFareDetails.getActiveFlag());

					StationIO fromStation = new StationIO();
					fromStation.setCode(dynamicStageFareDetails.getFromStation() != null ? dynamicStageFareDetails.getFromStation().getCode() : null);
					fromStation.setName(dynamicStageFareDetails.getFromStation() != null ? dynamicStageFareDetails.getFromStation().getName() : null);
					scheduleDynamicStageFareDetails.setFromStation(fromStation);

					StationIO toStation = new StationIO();
					toStation.setCode(dynamicStageFareDetails.getToStation() != null ? dynamicStageFareDetails.getToStation().getCode() : null);
					toStation.setName(dynamicStageFareDetails.getToStation() != null ? dynamicStageFareDetails.getToStation().getName() : null);
					scheduleDynamicStageFareDetails.setToStation(toStation);

					fareDetails.add(scheduleDynamicStageFareDetails);
				}
				stageFare.setStageFare(fareDetails);

				// Override
				if (!seatFareDTO.getOverrideList().isEmpty()) {
					List<ScheduleDynamicStageFareIO> overrideAutoReleaseIOlist = new ArrayList<ScheduleDynamicStageFareIO>();
					for (ScheduleDynamicStageFareDTO overrideStageFare : seatFareDTO.getOverrideList()) {
						ScheduleDynamicStageFareIO overrideScheduleStageFare = new ScheduleDynamicStageFareIO();
						overrideScheduleStageFare.setCode(overrideStageFare.getCode());
						overrideScheduleStageFare.setDayOfWeek(overrideStageFare.getDayOfWeek());
						overrideScheduleStageFare.setActiveFrom(overrideStageFare.getActiveFrom());
						overrideScheduleStageFare.setActiveTo(overrideStageFare.getActiveTo());
						overrideScheduleStageFare.setActiveFlag(overrideStageFare.getActiveFlag());
						overrideScheduleStageFare.setStatus(overrideStageFare.getStatus());

						List<ScheduleDynamicStageFareDetailsIO> overrideFareDetails = new ArrayList<ScheduleDynamicStageFareDetailsIO>();
						for (ScheduleDynamicStageFareDetailsDTO overrideStageFareDetails : overrideStageFare.getStageFare()) {
							ScheduleDynamicStageFareDetailsIO overrideDynamicStageFareDetails = new ScheduleDynamicStageFareDetailsIO();
							overrideDynamicStageFareDetails.setCode(overrideStageFareDetails.getCode());
							overrideDynamicStageFareDetails.setMinFare(overrideStageFareDetails.getMinFare());
							overrideDynamicStageFareDetails.setMaxFare(overrideStageFareDetails.getMaxFare());
							overrideDynamicStageFareDetails.setActiveFlag(overrideStageFareDetails.getActiveFlag());

							StationIO fromStation = new StationIO();
							fromStation.setCode(overrideStageFareDetails.getFromStation() != null ? overrideStageFareDetails.getFromStation().getCode() : null);
							fromStation.setName(overrideStageFareDetails.getFromStation() != null ? overrideStageFareDetails.getFromStation().getName() : null);
							overrideDynamicStageFareDetails.setFromStation(fromStation);

							StationIO toStation = new StationIO();
							toStation.setCode(overrideStageFareDetails.getToStation() != null ? overrideStageFareDetails.getToStation().getCode() : null);
							toStation.setName(overrideStageFareDetails.getToStation() != null ? overrideStageFareDetails.getToStation().getName() : null);
							overrideDynamicStageFareDetails.setToStation(toStation);

							overrideFareDetails.add(overrideDynamicStageFareDetails);
						}
						overrideScheduleStageFare.setStageFare(overrideFareDetails);

						overrideAutoReleaseIOlist.add(overrideScheduleStageFare);
					}
					stageFare.setOverrideList(overrideAutoReleaseIOlist);
				}
				seatFareList.add(stageFare);
			}
		}
		return ResponseIO.success(seatFareList);
	}

	@RequestMapping(value = "/stage/fare/refresh", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> getScheduleSeatFareCron(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleDynamicStageFareIO dynamicStageFare) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDynamicStageFareDTO scheduleSeatFare = new ScheduleDynamicStageFareDTO();
			scheduleSeatFare.setCode(dynamicStageFare.getCode());
			scheduleSeatFare.setActiveFrom(dynamicStageFare.getActiveFrom());
			scheduleSeatFare.setActiveTo(dynamicStageFare.getActiveTo());
			scheduleSeatFare.setDayOfWeek(dynamicStageFare.getDayOfWeek());
			scheduleSeatFare.setDynamicPriceProvider(DynamicPriceProviderEM.getDynamicPriceProviderEM(dynamicStageFare.getDynamicPriceProvider().getCode()));

			if (dynamicStageFare.getSchedule() != null) {
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(dynamicStageFare.getSchedule().getCode());
				scheduleSeatFare.setSchedule(scheduleDTO);
			}

			List<ScheduleDynamicStageFareDetailsDTO> fareDetailsList = new ArrayList<ScheduleDynamicStageFareDetailsDTO>();
			for (ScheduleDynamicStageFareDetailsIO dynamicStageFareDetails : dynamicStageFare.getStageFare()) {
				ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetailsDTO = new ScheduleDynamicStageFareDetailsDTO();
				dynamicStageFareDetailsDTO.setMinFare(dynamicStageFareDetails.getMinFare());
				dynamicStageFareDetailsDTO.setMaxFare(dynamicStageFareDetails.getMaxFare());
				dynamicStageFareDetailsDTO.setActiveFlag(dynamicStageFareDetails.getActiveFlag());

				StationDTO fromStation = new StationDTO();
				fromStation.setCode(dynamicStageFareDetails.getFromStation() != null ? dynamicStageFareDetails.getFromStation().getCode() : null);
				dynamicStageFareDetailsDTO.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setCode(dynamicStageFareDetails.getToStation() != null ? dynamicStageFareDetails.getToStation().getCode() : null);
				dynamicStageFareDetailsDTO.setToStation(toStation);

				fareDetailsList.add(dynamicStageFareDetailsDTO);
			}
			scheduleSeatFare.setStageFare(fareDetailsList);
			scheduleSeatFare.setActiveFlag(dynamicStageFare.getActiveFlag());
			if (BitsUtil.getDynamicPriceProvider(authDTO.getNamespace().getProfile().getDynamicPriceProviders(), DynamicPriceProviderEM.REDBUS) != null && scheduleSeatFare.getDynamicPriceProvider().getId() == DynamicPriceProviderEM.REDBUS.getId()) {
				dynamicStageFareService.dynamicFareProcess(authDTO, scheduleSeatFare, scheduleSeatFare);
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/stagefare/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleDynamicStageFareIO> updateScheduleSeatFare(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleDynamicStageFareIO seatFare) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleDynamicStageFareIO scheduleSeatFare = new ScheduleDynamicStageFareIO();
		ScheduleDynamicStageFareDTO seatFareDTO = new ScheduleDynamicStageFareDTO();
		if (seatFare.getSchedule() != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(seatFare.getSchedule().getCode());
			seatFareDTO.setSchedule(scheduleDTO);
		}
		seatFareDTO.setCode(seatFare.getCode());
		seatFareDTO.setActiveFrom(seatFare.getActiveFrom());
		seatFareDTO.setActiveTo(seatFare.getActiveTo());
		seatFareDTO.setDayOfWeek(seatFare.getDayOfWeek());
		seatFareDTO.setLookupCode(seatFare.getLookupCode());
		seatFareDTO.setActiveFlag(seatFare.getActiveFlag());
		seatFareDTO.setDynamicPriceProvider(DynamicPriceProviderEM.getDynamicPriceProviderEM(seatFare.getDynamicPriceProvider().getCode()));

		List<ScheduleDynamicStageFareDetailsDTO> fareDetailsList = new ArrayList<ScheduleDynamicStageFareDetailsDTO>();
		for (ScheduleDynamicStageFareDetailsIO dynamicStageFareDetails : seatFare.getStageFare()) {
			ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetailsDTO = new ScheduleDynamicStageFareDetailsDTO();
			dynamicStageFareDetailsDTO.setMinFare(dynamicStageFareDetails.getMinFare());
			dynamicStageFareDetailsDTO.setMaxFare(dynamicStageFareDetails.getMaxFare());
			dynamicStageFareDetailsDTO.setActiveFlag(dynamicStageFareDetails.getActiveFlag());

			StationDTO fromStation = new StationDTO();
			fromStation.setCode(dynamicStageFareDetails.getFromStation() != null ? dynamicStageFareDetails.getFromStation().getCode() : null);
			dynamicStageFareDetailsDTO.setFromStation(fromStation);

			StationDTO toStation = new StationDTO();
			toStation.setCode(dynamicStageFareDetails.getToStation() != null ? dynamicStageFareDetails.getToStation().getCode() : null);
			dynamicStageFareDetailsDTO.setToStation(toStation);

			fareDetailsList.add(dynamicStageFareDetailsDTO);
		}
		seatFareDTO.setStageFare(fareDetailsList);

		seatFareDTO = dynamicStageFareService.updateScheduleDynamicStageFareDetails(authDTO, seatFareDTO);
		scheduleSeatFare.setCode(seatFareDTO.getCode());
		scheduleSeatFare.setActiveFlag(1);
		return ResponseIO.success(scheduleSeatFare);
	}

	@RequestMapping(value = "/trip/exception/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleDynamicStageFareIO> addScheduleDynamicPriceException(@PathVariable("authtoken") String authtoken, @RequestParam(value = "scheduleCode", required = true) String scheduleCode, @RequestParam(value = "tripDate", required = true) String tripDate, @RequestParam(value = "tripCode", required = true) String tripCode, @RequestParam(value = "status", required = true) int status) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleDynamicStageFareIO scheduleSeatFare = new ScheduleDynamicStageFareIO();

		ScheduleDynamicStageFareDTO seatFareDTO = new ScheduleDynamicStageFareDTO();
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCode(scheduleCode);
		seatFareDTO.setSchedule(scheduleDTO);
		seatFareDTO.setActiveFrom(tripDate);
		seatFareDTO.setActiveTo(tripDate);
		seatFareDTO.setDayOfWeek("1111111");
		seatFareDTO.setStatus(status);
		seatFareDTO.setActiveFlag(Numeric.ONE_INT);

		TripDTO trip = new TripDTO();
		trip.setCode(tripCode);
		dynamicStageFareService.addScheduleDynamicPriceException(authDTO, seatFareDTO, trip);
		return ResponseIO.success(scheduleSeatFare);
	}

	@RequestMapping(value = "/stagefare/{schedulecode}/details", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleDynamicStageFareDetailsIO>> getScheduleDynamicStageTripFareDetails(@PathVariable("authtoken") String authtoken, @PathVariable("schedulecode") String schedulecode) throws Exception {
		List<ScheduleDynamicStageFareDetailsIO> fareDetails = new ArrayList<ScheduleDynamicStageFareDetailsIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleDynamicStageFareDTO scheduleSeatFare = new ScheduleDynamicStageFareDTO();
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCode(schedulecode);
		scheduleSeatFare.setSchedule(scheduleDTO);

		List<ScheduleDynamicStageFareDetailsDTO> scheduleDynamicStageFareDetails = dynamicStageFareService.getScheduleDynamicStageTripFareDetails(authDTO, scheduleDTO);

		for (ScheduleDynamicStageFareDetailsDTO dynamicStageFareDetails : scheduleDynamicStageFareDetails) {
			ScheduleDynamicStageFareDetailsIO scheduleDynamicStageFareDetailsIO = new ScheduleDynamicStageFareDetailsIO();
			scheduleDynamicStageFareDetailsIO.setActiveFlag(dynamicStageFareDetails.getActiveFlag());

			StationIO fromStation = new StationIO();
			fromStation.setCode(dynamicStageFareDetails.getFromStation() != null ? dynamicStageFareDetails.getFromStation().getCode() : null);
			fromStation.setName(dynamicStageFareDetails.getFromStation() != null ? dynamicStageFareDetails.getFromStation().getName() : null);
			scheduleDynamicStageFareDetailsIO.setFromStation(fromStation);

			StationIO toStation = new StationIO();
			toStation.setCode(dynamicStageFareDetails.getToStation() != null ? dynamicStageFareDetails.getToStation().getCode() : null);
			toStation.setName(dynamicStageFareDetails.getToStation() != null ? dynamicStageFareDetails.getToStation().getName() : null);
			scheduleDynamicStageFareDetailsIO.setToStation(toStation);

			scheduleDynamicStageFareDetailsIO.setTripDate(dynamicStageFareDetails.getTripDate().format("DD-MM-YYYY"));
			List<BusSeatLayoutIO> seats = new ArrayList<>();
			for (BusSeatLayoutDTO busSeatLayoutDTO : dynamicStageFareDetails.getSeatFare()) {
				BusSeatLayoutIO seat = new BusSeatLayoutIO();
				seat.setName(busSeatLayoutDTO.getName());
				seat.setSeatFare(busSeatLayoutDTO.getFare().doubleValue());
				seats.add(seat);
			}
			scheduleDynamicStageFareDetailsIO.setSeatFare(seats);

			fareDetails.add(scheduleDynamicStageFareDetailsIO);
		}
		return ResponseIO.success(fareDetails);
	}

	@RequestMapping(value = "/raw/fare/{scheduleCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<JSONObject> getScheduleSeatTripRawFare(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode, String tripDate) throws Exception {
		ScheduleDTO schedule = new ScheduleDTO();
		schedule.setCode(scheduleCode);
		schedule.setTripDate(DateUtil.getDateTime(tripDate));
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		JSONObject respose = dynamicStageFareService.getScheduleStageTripDPRawFare(authDTO, schedule);
		return ResponseIO.success(respose);
	}
}
