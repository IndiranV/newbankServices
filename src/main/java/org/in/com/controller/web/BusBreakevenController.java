package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.controller.web.io.BusBreakevenSettingsIO;
import org.in.com.controller.web.io.BusIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusBreakevenSettingsDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StateDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusBreakevenService;
import org.in.com.service.StateService;
import org.in.com.service.TripService;
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

import hirondelle.date4j.DateTime;

@Controller
@RequestMapping("/{authtoken}/bus/breakeven")
public class BusBreakevenController extends BaseController {
	@Autowired
	StateService state;
	@Autowired
	BusBreakevenService breakevenService;
	@Autowired
	TripService tripService;

	@RequestMapping(value = "/settings/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BusBreakevenSettingsIO> updateBusBreakevenSettings(@PathVariable("authtoken") String authtoken, @RequestBody BusBreakevenSettingsIO breakevenSettings) throws Exception {
		BusBreakevenSettingsIO breakevenSettingsIO = new BusBreakevenSettingsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BusBreakevenSettingsDTO breakevenSettingsDTO = new BusBreakevenSettingsDTO();
		breakevenSettingsDTO.setCode(breakevenSettings.getCode());
		breakevenSettingsDTO.setName(breakevenSettings.getName());

		BusDTO bus = new BusDTO();
		bus.setCode(breakevenSettings.getBus() != null ? breakevenSettings.getBus().getCode() : null);
		breakevenSettingsDTO.setBus(bus);

		breakevenSettingsDTO.setBreakevenDetails(breakevenSettings.getBreakevenDetails());
		breakevenSettingsDTO.setActiveFlag(breakevenSettings.getActiveFlag());
		breakevenService.updateBreakevenSettings(authDTO, breakevenSettingsDTO);
		breakevenSettingsIO.setCode(breakevenSettingsDTO.getCode());
		breakevenSettingsIO.setActiveFlag(breakevenSettingsDTO.getActiveFlag());
		return ResponseIO.success(breakevenSettingsIO);
	}

	@RequestMapping(value = "/settings/details/{settingsCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BusBreakevenSettingsIO> getBusBreakevenSettingsDetails(@PathVariable("authtoken") String authtoken, @PathVariable("settingsCode") String settingsCode) throws Exception {
		BusBreakevenSettingsIO breakevenSettingsIO = new BusBreakevenSettingsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BusBreakevenSettingsDTO breakevenSettingsDTO = new BusBreakevenSettingsDTO();
		breakevenSettingsDTO.setCode(settingsCode);
		breakevenService.getBreakevenSettings(authDTO, breakevenSettingsDTO);
		breakevenSettingsIO.setCode(breakevenSettingsDTO.getCode());
		breakevenSettingsIO.setName(breakevenSettingsDTO.getName());

		BusIO bus = new BusIO();
		if (breakevenSettingsDTO.getBus() != null) {
			bus.setCode(breakevenSettingsDTO.getBus().getCode());
			bus.setName(breakevenSettingsDTO.getBus().getName());
			bus.setCategoryCode(breakevenSettingsDTO.getBus().getCategoryCode());
			bus.setSeatCount(breakevenSettingsDTO.getBus().getSeatLayoutCount());
		}
		breakevenSettingsIO.setBus(bus);

		breakevenSettingsIO.setBreakevenDetails(breakevenSettingsDTO.getBreakevenDetails());
		breakevenSettingsIO.setActiveFlag(breakevenSettingsDTO.getActiveFlag());
		return ResponseIO.success(breakevenSettingsIO);
	}

	@RequestMapping(value = "/settings/{settingsCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BusBreakevenSettingsIO> getBusBreakevenSettings(@PathVariable("authtoken") String authtoken, @PathVariable("settingsCode") String settingsCode) throws Exception {
		BusBreakevenSettingsIO breakevenSettingsIO = new BusBreakevenSettingsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		BusBreakevenSettingsDTO breakevenSettingsDTO = new BusBreakevenSettingsDTO();
		breakevenSettingsDTO.setCode(settingsCode);
		breakevenService.getBreakevenSettings(authDTO, breakevenSettingsDTO);
		breakevenSettingsIO.setCode(breakevenSettingsDTO.getCode());
		breakevenSettingsIO.setName(breakevenSettingsDTO.getName());
		breakevenSettingsIO.setActiveFlag(breakevenSettingsDTO.getActiveFlag());
		return ResponseIO.success(breakevenSettingsIO);
	}

	@RequestMapping(value = "/settings", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<BusBreakevenSettingsIO>> getAllBusBreakevenSettings(@PathVariable("authtoken") String authtoken) throws Exception {
		List<BusBreakevenSettingsIO> list = new ArrayList<BusBreakevenSettingsIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<BusBreakevenSettingsDTO> breakevenSettings = breakevenService.getAllBreakevenSettings(authDTO);
		for (BusBreakevenSettingsDTO breakevenSettingsDTO : breakevenSettings) {
			BusBreakevenSettingsIO breakevenSettingsIO = new BusBreakevenSettingsIO();
			breakevenSettingsIO.setCode(breakevenSettingsDTO.getCode());
			breakevenSettingsIO.setName(breakevenSettingsDTO.getName());

			BusIO bus = new BusIO();
			if (breakevenSettingsDTO.getBus() != null) {
				bus.setCode(breakevenSettingsDTO.getBus().getCode());
				bus.setName(breakevenSettingsDTO.getBus().getName());
				bus.setCategoryCode(breakevenSettingsDTO.getBus().getCategoryCode());
				bus.setSeatCount(breakevenSettingsDTO.getBus().getSeatLayoutCount());
			}
			breakevenSettingsIO.setBus(bus);

			breakevenSettingsIO.setBreakevenDetails(breakevenSettingsDTO.getBreakevenDetails());
			breakevenSettingsIO.setActiveFlag(breakevenSettingsDTO.getActiveFlag());
			list.add(breakevenSettingsIO);
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/{fromStationCode}/{toStationCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, String>> getDistace(@PathVariable("authtoken") String authtoken, @PathVariable("fromStationCode") String fromStationCode, @PathVariable("toStationCode") String toStationCode) throws Exception {
		authService.getAuthDTO(authtoken);
		Map<String, String> map = new HashMap<String, String>();
		map.put("distance", "600");
		return ResponseIO.success(map);
	}

	@RequestMapping(value = "/fuel/price/{fuelDate}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, String>> getAllStageFuelPrice(@PathVariable("authtoken") String authtoken, @PathVariable("fuelDate") String fuelDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		DateTime fuel = DateUtil.getDateTime(fuelDate);
		Map<String, String> map = breakevenService.getAllStageFuelPrice(authDTO, fuel);
		List<StateDTO> statelist = state.getAll();
		for (StateDTO stateDTO : statelist) {
			map.put(stateDTO.getName() + "-" + stateDTO.getCode(), map.get(stateDTO.getCode()));
			map.remove(stateDTO.getCode());
		}
		return ResponseIO.success(map);
	}

	@RequestMapping(value = "/breakeven/expense", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<Map<String, Object>>> getBreakevenDetails(@PathVariable("authtoken") String authtoken, String fromDate, String toDate, String scheduleCode, String vehicleCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (StringUtil.isNull(fromDate) || StringUtil.isNull(toDate) || !DateUtil.isValidDateV2(fromDate) || !DateUtil.isValidDateV2(toDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE_RANGE);
		}
		DateTime fromDateTime = DateUtil.getDateTime(fromDate);
		DateTime toDateTime = DateUtil.getDateTime(toDate);

		ScheduleDTO schedule = null;
		if (StringUtil.isNotNull(scheduleCode)) {
			schedule = new ScheduleDTO();
			schedule.setCode(scheduleCode);
		}

		BusVehicleDTO vehicle = null;
		if (StringUtil.isNotNull(vehicleCode)) {
			vehicle = new BusVehicleDTO();
			vehicle.setCode(vehicleCode);
		}
		List<Map<String, Object>> breakevenList = tripService.getBreakevenDetails(authDTO, fromDateTime, toDateTime, schedule, vehicle);
		return ResponseIO.success(breakevenList);
	}

}
