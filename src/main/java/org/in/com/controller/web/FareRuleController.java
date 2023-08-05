package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.json.JSONArray;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.FareRuleDetailsIO;
import org.in.com.controller.web.io.FareRuleIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.StageFareIO;
import org.in.com.controller.web.io.StageIO;
import org.in.com.controller.web.io.StateIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.FareRuleDTO;
import org.in.com.dto.FareRuleDetailsDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.FareRuleService;
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
@RequestMapping("/{authtoken}/fare/rules")
public class FareRuleController extends BaseController {
	public static Map<String, Integer> concurrentRequests = new ConcurrentHashMap<String, Integer>();

	@Autowired
	FareRuleService fareRuleService;

	@RequestMapping(value = "/{fareRuleCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<FareRuleIO> getFareRule(@PathVariable("authtoken") String authtoken, @PathVariable("fareRuleCode") String fareRuleCode) throws Exception {
		FareRuleIO fareRuleIO = new FareRuleIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			FareRuleDTO fareRuleDTO = new FareRuleDTO();
			fareRuleDTO.setCode(fareRuleCode);
			fareRuleService.getFareRule(authDTO, fareRuleDTO);
			fareRuleIO.setCode(fareRuleDTO.getCode());
			fareRuleIO.setName(fareRuleDTO.getName());

			StateIO statIO = new StateIO();
			if (fareRuleDTO.getState() != null) {
				statIO.setCode(fareRuleDTO.getState().getCode());
				statIO.setName(fareRuleDTO.getState().getName());
			}
			fareRuleIO.setState(statIO);
			fareRuleIO.setActiveFlag(fareRuleDTO.getActiveFlag());
		}
		return ResponseIO.success(fareRuleIO);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<FareRuleIO> updateFareRule(@PathVariable("authtoken") String authtoken, @RequestBody FareRuleIO fareRuleIO) throws Exception {
		FareRuleIO fareRule = new FareRuleIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (fareRuleIO.getState() == null || StringUtil.isNull(fareRuleIO.getState().getCode())) {
			throw new ServiceException(ErrorCode.INVALID_STATE);
		}
		FareRuleDTO fareRuleDTO = new FareRuleDTO();
		fareRuleDTO.setCode(fareRuleIO.getCode());
		fareRuleDTO.setName(fareRuleIO.getName());

		StateDTO state = new StateDTO();
		state.setCode(fareRuleIO.getState().getCode());
		fareRuleDTO.setState(state);

		fareRuleDTO.setActiveFlag(fareRuleIO.getActiveFlag());
		fareRuleService.Update(authDTO, fareRuleDTO);
		fareRule.setCode(fareRuleDTO.getCode());
		fareRule.setActiveFlag(fareRuleDTO.getActiveFlag());
		return ResponseIO.success(fareRule);
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<FareRuleIO>> getAllFareRule(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<FareRuleIO> fareRuleList = new ArrayList<FareRuleIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<FareRuleDTO> fareRuleDTOList = fareRuleService.getAll(authDTO);
			for (FareRuleDTO fareRuleDTO : fareRuleDTOList) {
				if (activeFlag != -1 && activeFlag != fareRuleDTO.getActiveFlag()) {
					continue;
				}
				FareRuleIO fareRuleIO = new FareRuleIO();
				fareRuleIO.setCode(fareRuleDTO.getCode());
				fareRuleIO.setName(fareRuleDTO.getName());

				StateIO stateIO = new StateIO();
				stateIO.setCode(fareRuleDTO.getState().getCode());
				stateIO.setName(fareRuleDTO.getState().getName());

				fareRuleIO.setState(stateIO);
				fareRuleIO.setActiveFlag(fareRuleDTO.getActiveFlag());
				fareRuleList.add(fareRuleIO);
			}
		}
		return ResponseIO.success(fareRuleList);
	}

	@RequestMapping(value = "/details/route", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<FareRuleIO> getFareRuleDetails(@PathVariable("authtoken") String authtoken, @RequestParam(value = "fromStationCode", required = false, defaultValue = "NA") String fromStationCode, @RequestParam(value = "toStationCode", required = false, defaultValue = "NA") String toStationCode) throws Exception {
		FareRuleIO fareRule = new FareRuleIO();
		List<FareRuleDetailsIO> fareRuleDetailsList = new ArrayList<FareRuleDetailsIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		StationDTO fromStationDTO = new StationDTO();
		fromStationDTO.setCode(fromStationCode);

		StationDTO toStationDTO = new StationDTO();
		toStationDTO.setCode(toStationCode);

		FareRuleDTO fareRuleDTO = fareRuleService.getFareRuleDetails(authDTO, fromStationDTO, toStationDTO);
		fareRule.setCode(fareRuleDTO.getCode());

		for (FareRuleDetailsDTO fareRuleDetailsDTO : fareRuleDTO.getFareRuleDetails()) {
			FareRuleDetailsIO fareRuleDetails = new FareRuleDetailsIO();

			StationIO fromStation = new StationIO();
			fromStation.setCode(fareRuleDetailsDTO.getFromStation().getCode());
			fromStation.setName(fareRuleDetailsDTO.getFromStation().getName());
			fareRuleDetails.setFromStation(fromStation);

			StationIO toStation = new StationIO();
			toStation.setCode(fareRuleDetailsDTO.getToStation().getCode());
			toStation.setName(fareRuleDetailsDTO.getToStation().getName());
			fareRuleDetails.setToStation(toStation);

			fareRuleDetails.setDistance(fareRuleDetailsDTO.getDistance());

			fareRuleDetails.setNonAcSeaterMaxFare(fareRuleDetailsDTO.getNonAcSeaterMaxFare());
			fareRuleDetails.setNonAcSeaterMinFare(fareRuleDetailsDTO.getNonAcSeaterMinFare());

			fareRuleDetails.setAcSeaterMinFare(fareRuleDetailsDTO.getAcSeaterMinFare());
			fareRuleDetails.setAcSeaterMaxFare(fareRuleDetailsDTO.getAcSeaterMaxFare());

			fareRuleDetails.setMultiAxleSeaterMinFare(fareRuleDetailsDTO.getMultiAxleSeaterMinFare());
			fareRuleDetails.setMultiAxleSeaterMaxFare(fareRuleDetailsDTO.getMultiAxleSeaterMaxFare());

			fareRuleDetails.setNonAcSleeperLowerMinFare(fareRuleDetailsDTO.getNonAcSleeperLowerMinFare());
			fareRuleDetails.setNonAcSleeperLowerMaxFare(fareRuleDetailsDTO.getNonAcSleeperLowerMaxFare());

			fareRuleDetails.setNonAcSleeperUpperMinFare(fareRuleDetailsDTO.getNonAcSleeperUpperMinFare());
			fareRuleDetails.setNonAcSleeperUpperMaxFare(fareRuleDetailsDTO.getNonAcSleeperUpperMaxFare());

			fareRuleDetails.setAcSleeperLowerMinFare(fareRuleDetailsDTO.getAcSleeperLowerMinFare());
			fareRuleDetails.setAcSleeperLowerMaxFare(fareRuleDetailsDTO.getAcSleeperLowerMaxFare());

			fareRuleDetails.setAcSleeperUpperMinFare(fareRuleDetailsDTO.getAcSleeperUpperMinFare());
			fareRuleDetails.setAcSleeperUpperMaxFare(fareRuleDetailsDTO.getAcSleeperUpperMaxFare());

			fareRuleDetails.setBrandedAcSleeperMinFare(fareRuleDetailsDTO.getBrandedAcSleeperMinFare());
			fareRuleDetails.setBrandedAcSleeperMaxFare(fareRuleDetailsDTO.getBrandedAcSleeperMaxFare());

			fareRuleDetails.setSingleAxleAcSeaterMinFare(fareRuleDetailsDTO.getSingleAxleAcSeaterMinFare());
			fareRuleDetails.setSingleAxleAcSeaterMaxFare(fareRuleDetailsDTO.getSingleAxleAcSeaterMaxFare());

			fareRuleDetails.setMultiAxleAcSleeperMinFare(fareRuleDetailsDTO.getMultiAxleAcSleeperMinFare());
			fareRuleDetails.setMultiAxleAcSleeperMaxFare(fareRuleDetailsDTO.getMultiAxleAcSleeperMaxFare());

			fareRuleDetails.setActiveFlag(fareRuleDetailsDTO.getActiveFlag());
			fareRuleDetailsList.add(fareRuleDetails);
		}
		fareRule.setFareRuleDetails(fareRuleDetailsList);
		return ResponseIO.success(fareRule);
	}

	@RequestMapping(value = "/{farerulecode}/details/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateFareRule(@PathVariable("authtoken") String authtoken, @PathVariable("farerulecode") String farerulecode, @RequestBody List<FareRuleDetailsIO> fareRuleDetailsIOList) throws Exception {
		List<FareRuleDetailsDTO> fareRuleDetailsList = new ArrayList<FareRuleDetailsDTO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		FareRuleDTO fareRuleDTO = new FareRuleDTO();
		fareRuleDTO.setCode(farerulecode);

		for (FareRuleDetailsIO fareRuleDetailsIO : fareRuleDetailsIOList) {
			FareRuleDetailsDTO fareRuleDtailsDTO = new FareRuleDetailsDTO();

			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setCode(fareRuleDetailsIO.getFromStation().getCode());
			fareRuleDtailsDTO.setFromStation(fromStationDTO);

			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setCode(fareRuleDetailsIO.getToStation().getCode());
			fareRuleDtailsDTO.setToStation(toStationDTO);

			fareRuleDtailsDTO.setDistance(fareRuleDetailsIO.getDistance());

			fareRuleDtailsDTO.setNonAcSeaterMinFare(fareRuleDetailsIO.getNonAcSeaterMinFare());
			fareRuleDtailsDTO.setNonAcSeaterMaxFare(fareRuleDetailsIO.getNonAcSeaterMaxFare());

			fareRuleDtailsDTO.setAcSeaterMinFare(fareRuleDetailsIO.getAcSeaterMinFare());
			fareRuleDtailsDTO.setAcSeaterMaxFare(fareRuleDetailsIO.getAcSeaterMaxFare());

			fareRuleDtailsDTO.setMultiAxleSeaterMinFare(fareRuleDetailsIO.getMultiAxleSeaterMinFare());
			fareRuleDtailsDTO.setMultiAxleSeaterMaxFare(fareRuleDetailsIO.getMultiAxleSeaterMaxFare());

			fareRuleDtailsDTO.setNonAcSleeperLowerMinFare(fareRuleDetailsIO.getNonAcSleeperLowerMinFare());
			fareRuleDtailsDTO.setNonAcSleeperLowerMaxFare(fareRuleDetailsIO.getNonAcSleeperLowerMaxFare());

			fareRuleDtailsDTO.setNonAcSleeperUpperMinFare(fareRuleDetailsIO.getNonAcSleeperUpperMinFare());
			fareRuleDtailsDTO.setNonAcSleeperUpperMaxFare(fareRuleDetailsIO.getNonAcSleeperUpperMaxFare());

			fareRuleDtailsDTO.setAcSleeperLowerMinFare(fareRuleDetailsIO.getAcSleeperLowerMinFare());
			fareRuleDtailsDTO.setAcSleeperLowerMaxFare(fareRuleDetailsIO.getAcSleeperLowerMaxFare());

			fareRuleDtailsDTO.setAcSleeperUpperMinFare(fareRuleDetailsIO.getAcSleeperUpperMinFare());
			fareRuleDtailsDTO.setAcSleeperUpperMaxFare(fareRuleDetailsIO.getAcSleeperUpperMaxFare());

			fareRuleDtailsDTO.setBrandedAcSleeperMinFare(fareRuleDetailsIO.getBrandedAcSleeperMinFare());
			fareRuleDtailsDTO.setBrandedAcSleeperMaxFare(fareRuleDetailsIO.getBrandedAcSleeperMaxFare());

			fareRuleDtailsDTO.setSingleAxleAcSeaterMinFare(fareRuleDetailsIO.getSingleAxleAcSeaterMinFare());
			fareRuleDtailsDTO.setSingleAxleAcSeaterMaxFare(fareRuleDetailsIO.getSingleAxleAcSeaterMaxFare());

			fareRuleDtailsDTO.setMultiAxleAcSleeperMinFare(fareRuleDetailsIO.getMultiAxleAcSleeperMinFare());
			fareRuleDtailsDTO.setMultiAxleAcSleeperMaxFare(fareRuleDetailsIO.getMultiAxleAcSleeperMaxFare());

			fareRuleDtailsDTO.setActiveFlag(fareRuleDetailsIO.getActiveFlag());
			fareRuleDetailsList.add(fareRuleDtailsDTO);
		}
		fareRuleDTO.setFareRuleDetails(fareRuleDetailsList);

		fareRuleService.updateFareRuleDetails(authDTO, fareRuleDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/details/{farerulecode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<FareRuleIO> getFareRuleDetailsByFareRule(@PathVariable("authtoken") String authtoken, @PathVariable("farerulecode") String farerulecode, @RequestParam(value = "fromStationCode", required = false, defaultValue = "NA") String fromStationCode, @RequestParam(value = "toStationCode", required = false, defaultValue = "NA") String toStationCode) throws Exception {
		FareRuleIO fareRule = new FareRuleIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		FareRuleDTO fareRuleDTO = new FareRuleDTO();
		fareRuleDTO.setCode(farerulecode);

		StationDTO fromStationDTO = new StationDTO();
		fromStationDTO.setCode(fromStationCode);

		StationDTO toStationDTO = new StationDTO();
		toStationDTO.setCode(toStationCode);

		fareRuleDTO = fareRuleService.getFareRuleDetailsByFareRule(authDTO, fareRuleDTO, fromStationDTO, toStationDTO);

		fareRule.setCode(fareRuleDTO.getCode());
		fareRule.setName(fareRuleDTO.getName());

		List<FareRuleDetailsIO> fareRuleDetailsList = new ArrayList<FareRuleDetailsIO>();
		for (FareRuleDetailsDTO fareRuleDetailsDTO : fareRuleDTO.getFareRuleDetails()) {
			FareRuleDetailsIO fareRuleDetails = new FareRuleDetailsIO();

			StationIO fromStation = new StationIO();
			fromStation.setCode(fareRuleDetailsDTO.getFromStation().getCode());
			fromStation.setName(fareRuleDetailsDTO.getFromStation().getName());
			fareRuleDetails.setFromStation(fromStation);

			StationIO toStation = new StationIO();
			toStation.setCode(fareRuleDetailsDTO.getToStation().getCode());
			toStation.setName(fareRuleDetailsDTO.getToStation().getName());
			fareRuleDetails.setToStation(toStation);

			fareRuleDetails.setDistance(fareRuleDetailsDTO.getDistance());

			fareRuleDetails.setNonAcSeaterMaxFare(fareRuleDetailsDTO.getNonAcSeaterMaxFare());
			fareRuleDetails.setNonAcSeaterMinFare(fareRuleDetailsDTO.getNonAcSeaterMinFare());

			fareRuleDetails.setAcSeaterMinFare(fareRuleDetailsDTO.getAcSeaterMinFare());
			fareRuleDetails.setAcSeaterMaxFare(fareRuleDetailsDTO.getAcSeaterMaxFare());

			fareRuleDetails.setMultiAxleSeaterMinFare(fareRuleDetailsDTO.getMultiAxleSeaterMinFare());
			fareRuleDetails.setMultiAxleSeaterMaxFare(fareRuleDetailsDTO.getMultiAxleSeaterMaxFare());

			fareRuleDetails.setNonAcSleeperLowerMinFare(fareRuleDetailsDTO.getNonAcSleeperLowerMinFare());
			fareRuleDetails.setNonAcSleeperLowerMaxFare(fareRuleDetailsDTO.getNonAcSleeperLowerMaxFare());

			fareRuleDetails.setNonAcSleeperUpperMinFare(fareRuleDetailsDTO.getNonAcSleeperUpperMinFare());
			fareRuleDetails.setNonAcSleeperUpperMaxFare(fareRuleDetailsDTO.getNonAcSleeperUpperMaxFare());

			fareRuleDetails.setAcSleeperLowerMinFare(fareRuleDetailsDTO.getAcSleeperLowerMinFare());
			fareRuleDetails.setAcSleeperLowerMaxFare(fareRuleDetailsDTO.getAcSleeperLowerMaxFare());

			fareRuleDetails.setAcSleeperUpperMinFare(fareRuleDetailsDTO.getAcSleeperUpperMinFare());
			fareRuleDetails.setAcSleeperUpperMaxFare(fareRuleDetailsDTO.getAcSleeperUpperMaxFare());

			fareRuleDetails.setBrandedAcSleeperMinFare(fareRuleDetailsDTO.getBrandedAcSleeperMinFare());
			fareRuleDetails.setBrandedAcSleeperMaxFare(fareRuleDetailsDTO.getBrandedAcSleeperMaxFare());

			fareRuleDetails.setSingleAxleAcSeaterMinFare(fareRuleDetailsDTO.getSingleAxleAcSeaterMinFare());
			fareRuleDetails.setSingleAxleAcSeaterMaxFare(fareRuleDetailsDTO.getSingleAxleAcSeaterMaxFare());

			fareRuleDetails.setMultiAxleAcSleeperMinFare(fareRuleDetailsDTO.getMultiAxleAcSleeperMinFare());
			fareRuleDetails.setMultiAxleAcSleeperMaxFare(fareRuleDetailsDTO.getMultiAxleAcSleeperMaxFare());

			fareRuleDetails.setActiveFlag(fareRuleDetailsDTO.getActiveFlag());
			fareRuleDetails.setUpdatedAt(fareRuleDetailsDTO.getUpdatedAt());
			fareRuleDetailsList.add(fareRuleDetails);
		}
		fareRule.setFareRuleDetails(fareRuleDetailsList);

		return ResponseIO.success(fareRule);
	}

	@RequestMapping(value = "/route/{fromStationCode}/{toStationCode}/{busCode}/v2", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<StageIO> getFareRuleDetailsByRoute(@PathVariable("authtoken") String authtoken, @PathVariable("fromStationCode") String fromStationCode, @PathVariable("toStationCode") String toStationCode, @PathVariable("busCode") String busCode) throws Exception {
		StageIO stageIO = new StageIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		StationDTO fromStationDTO = new StationDTO();
		fromStationDTO.setCode(fromStationCode);

		StationDTO toStationDTO = new StationDTO();
		toStationDTO.setCode(toStationCode);

		BusDTO busDTO = new BusDTO();
		busDTO.setCode(busCode);

		StageDTO stageDTO = fareRuleService.getFareRuleByRoute(authDTO, authDTO.getNamespace().getProfile().getFareRule(), fromStationDTO, toStationDTO, busDTO);
		stageIO.setDistance(stageDTO.getDistance());
		List<StageFareIO> stageFareList = new ArrayList<StageFareIO>();
		for (StageFareDTO stageFareDTO : stageDTO.getStageFare()) {
			StageFareIO stageFare = new StageFareIO();
			stageFare.setSeatType(stageFareDTO.getBusSeatType().getCode());
			stageFare.setSeatName(stageFareDTO.getBusSeatType().getName());
			stageFare.setMinFare(stageFareDTO.getMinFare());
			stageFare.setMaxFare(stageFareDTO.getMaxFare());
			stageFareList.add(stageFare);
		}
		stageIO.setStageFare(stageFareList);

		return ResponseIO.success(stageIO);
	}

	@RequestMapping(value = "/details/schedule/{scheduleCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONArray> getFareRuleDetails(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCode(scheduleCode);

		JSONArray stages = fareRuleService.getFareRuleDetailsBySchedule(authDTO, scheduleDTO);
		return ResponseIO.success(stages);
	}

	@RequestMapping(value = "/details/schedule/{scheduleCode}/validation", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONArray> getLowFareStages(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCode(scheduleCode);

		JSONArray stages = fareRuleService.getLowFareStages(authDTO, scheduleDTO);
		return ResponseIO.success(stages);
	}

	@RequestMapping(value = "/details/schedule/{scheduleCode}/validation/apply", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> applyLowFareStages(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCode(scheduleCode);

		fareRuleService.applyFareRulesInStages(authDTO, scheduleDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{farerulecode}/details/zonesync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<FareRuleDetailsIO>> getZoneSyncFareRuleDetails(@PathVariable("authtoken") String authtoken, @PathVariable("farerulecode") String farerulecode, String syncDate) throws Exception {
		List<FareRuleDetailsIO> fareRuleDetailsList = new ArrayList<FareRuleDetailsIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		FareRuleDTO fareRuleDTO = new FareRuleDTO();
		fareRuleDTO.setCode(farerulecode);

		List<FareRuleDetailsDTO> list = fareRuleService.getZoneSyncFareRuleDetails(authDTO, fareRuleDTO, syncDate);

		for (FareRuleDetailsDTO fareRuleDetailsDTO : list) {
			FareRuleDetailsIO fareRuleDetails = new FareRuleDetailsIO();

			StationIO fromStation = new StationIO();
			fromStation.setCode(fareRuleDetailsDTO.getFromStation().getCode());
			fromStation.setName(fareRuleDetailsDTO.getFromStation().getName());
			fareRuleDetails.setFromStation(fromStation);

			StationIO toStation = new StationIO();
			toStation.setCode(fareRuleDetailsDTO.getToStation().getCode());
			toStation.setName(fareRuleDetailsDTO.getToStation().getName());
			fareRuleDetails.setToStation(toStation);

			fareRuleDetails.setDistance(fareRuleDetailsDTO.getDistance());

			fareRuleDetails.setNonAcSeaterMaxFare(fareRuleDetailsDTO.getNonAcSeaterMaxFare());
			fareRuleDetails.setNonAcSeaterMinFare(fareRuleDetailsDTO.getNonAcSeaterMinFare());

			fareRuleDetails.setAcSeaterMinFare(fareRuleDetailsDTO.getAcSeaterMinFare());
			fareRuleDetails.setAcSeaterMaxFare(fareRuleDetailsDTO.getAcSeaterMaxFare());

			fareRuleDetails.setMultiAxleSeaterMinFare(fareRuleDetailsDTO.getMultiAxleSeaterMinFare());
			fareRuleDetails.setMultiAxleSeaterMaxFare(fareRuleDetailsDTO.getMultiAxleSeaterMaxFare());

			fareRuleDetails.setNonAcSleeperLowerMinFare(fareRuleDetailsDTO.getNonAcSleeperLowerMinFare());
			fareRuleDetails.setNonAcSleeperLowerMaxFare(fareRuleDetailsDTO.getNonAcSleeperLowerMaxFare());

			fareRuleDetails.setNonAcSleeperUpperMinFare(fareRuleDetailsDTO.getNonAcSleeperUpperMinFare());
			fareRuleDetails.setNonAcSleeperUpperMaxFare(fareRuleDetailsDTO.getNonAcSleeperUpperMaxFare());

			fareRuleDetails.setAcSleeperLowerMinFare(fareRuleDetailsDTO.getAcSleeperLowerMinFare());
			fareRuleDetails.setAcSleeperLowerMaxFare(fareRuleDetailsDTO.getAcSleeperLowerMaxFare());

			fareRuleDetails.setAcSleeperUpperMinFare(fareRuleDetailsDTO.getAcSleeperUpperMinFare());
			fareRuleDetails.setAcSleeperUpperMaxFare(fareRuleDetailsDTO.getAcSleeperUpperMaxFare());

			fareRuleDetails.setBrandedAcSleeperMinFare(fareRuleDetailsDTO.getBrandedAcSleeperMinFare());
			fareRuleDetails.setBrandedAcSleeperMaxFare(fareRuleDetailsDTO.getBrandedAcSleeperMaxFare());

			fareRuleDetails.setSingleAxleAcSeaterMinFare(fareRuleDetailsDTO.getSingleAxleAcSeaterMinFare());
			fareRuleDetails.setSingleAxleAcSeaterMaxFare(fareRuleDetailsDTO.getSingleAxleAcSeaterMaxFare());

			fareRuleDetails.setMultiAxleAcSleeperMinFare(fareRuleDetailsDTO.getMultiAxleAcSleeperMinFare());
			fareRuleDetails.setMultiAxleAcSleeperMaxFare(fareRuleDetailsDTO.getMultiAxleAcSleeperMaxFare());

			fareRuleDetails.setActiveFlag(fareRuleDetailsDTO.getActiveFlag());
			fareRuleDetailsList.add(fareRuleDetails);
		}

		return ResponseIO.success(fareRuleDetailsList);
	}

	@RequestMapping(value = "/{farerulecode}/sync", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> syncFareRuleDetails(@PathVariable("authtoken") String authtoken, @PathVariable("farerulecode") String farerulecode) throws Exception {
		String jobName = "fare_rule_api_sync";
		try {
			checkConcurrentRequests(jobName, farerulecode);

			AuthDTO authDTO = authService.getAuthDTO(authtoken);
			if (!farerulecode.equals("FR3A85294M") && !farerulecode.equals("FR188B392G")) {
				throw new ServiceException(ErrorCode.INVALID_CODE);
			}
			FareRuleDTO fareRuleDTO = new FareRuleDTO();
			fareRuleDTO.setCode(farerulecode);

			fareRuleService.syncVertexFareRule(authDTO, fareRuleDTO, null);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {
			releaseConcurrentRequests(jobName);
		}
		return ResponseIO.success();
	}

	public static synchronized boolean checkConcurrentRequests(String jobName, String data) {
		if (concurrentRequests.get(jobName) != null && concurrentRequests.get(jobName) >= 1) {
			System.out.println(DateUtil.NOW() + " CRONJOB02 - " + jobName + " - reached Max Concurrent Request - " + data);
			throw new ServiceException(ErrorCode.REACHED_MAX_CONCURRENT_REQUESTS);
		}
		if (concurrentRequests.get(jobName) != null) {
			concurrentRequests.put(jobName, concurrentRequests.get(jobName) + 1);
		}
		else {
			concurrentRequests.put(jobName, 1);
		}
		return true;
	}

	public static synchronized boolean releaseConcurrentRequests(String jobName) {
		if (concurrentRequests.get(jobName) != null) {
			if (concurrentRequests.get(jobName) > 0) {
				concurrentRequests.put(jobName, concurrentRequests.get(jobName) - 1);
			}
		}
		return true;
	}
}
