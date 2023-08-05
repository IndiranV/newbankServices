package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.ScheduleEnrouteBookControlIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.StageIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleEnrouteBookControlDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.EnRouteTypeEM;
import org.in.com.service.ScheduleEnrouteBookControlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/{authtoken}/schedule")
public class ScheduleEnrouteBookControlController extends BaseController {
	@Autowired
	ScheduleEnrouteBookControlService scheduleEnrouteBookControlService;

	@RequestMapping(value = "/book/control/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleEnrouteBookControlIO> updateScheduleEnrouteBookControl(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleEnrouteBookControlIO scheduleEnrouteBookControl) throws Exception {
		ScheduleEnrouteBookControlIO scheduleEnrouteBookControlIO = new ScheduleEnrouteBookControlIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleEnrouteBookControlDTO scheduleEnrouteBookControlDTO = new ScheduleEnrouteBookControlDTO();
			scheduleEnrouteBookControlDTO.setCode(scheduleEnrouteBookControl.getCode());
			scheduleEnrouteBookControlDTO.setDayOfWeek(scheduleEnrouteBookControl.getDayOfWeek());
			scheduleEnrouteBookControlDTO.setEnRouteType(EnRouteTypeEM.getEnRouteTypeEM(scheduleEnrouteBookControl.getEnRouteType().getCode()));
			scheduleEnrouteBookControlDTO.setReleaseMinutes(scheduleEnrouteBookControl.getReleaseMinutes());
			scheduleEnrouteBookControlDTO.setActiveFlag(scheduleEnrouteBookControl.getActiveFlag());

			ScheduleDTO schedule = new ScheduleDTO();
			schedule.setCode(scheduleEnrouteBookControl.getSchedule().getCode());
			scheduleEnrouteBookControlDTO.setSchedule(schedule);

			List<StageDTO> stageList = new ArrayList<StageDTO>();
			for (StageIO stage : scheduleEnrouteBookControl.getStageList()) {
				StageDTO stageDTO = new StageDTO();

				StageStationDTO fromStageStationDTO = new StageStationDTO();
				StationDTO fromStation = new StationDTO();
				fromStation.setCode(stage.getFromStation().getCode());
				fromStageStationDTO.setStation(fromStation);
				stageDTO.setFromStation(fromStageStationDTO);

				StageStationDTO toStageStationDTO = new StageStationDTO();
				StationDTO toStation = new StationDTO();
				toStation.setCode(stage.getToStation().getCode());
				toStageStationDTO.setStation(toStation);
				stageDTO.setToStation(toStageStationDTO);

				stageList.add(stageDTO);
			}
			scheduleEnrouteBookControlDTO.setStageList(stageList);

			scheduleEnrouteBookControlService.Update(authDTO, scheduleEnrouteBookControlDTO);
			scheduleEnrouteBookControlIO.setCode(scheduleEnrouteBookControlDTO.getCode());
			scheduleEnrouteBookControlIO.setActiveFlag(scheduleEnrouteBookControlDTO.getActiveFlag());
		}
		return ResponseIO.success(scheduleEnrouteBookControlIO);
	}

	@RequestMapping(value = "/{scheduleCode}/book/control", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleEnrouteBookControlIO>> getAllScheduleEnrouteBookControl(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode) throws Exception {
		List<ScheduleEnrouteBookControlIO> scheduleEnrouteBookControlList = new ArrayList<ScheduleEnrouteBookControlIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleCode);

			List<ScheduleEnrouteBookControlDTO> list = scheduleEnrouteBookControlService.getAll(authDTO, scheduleDTO);
			for (ScheduleEnrouteBookControlDTO scheduleEnrouteBookControlDTO : list) {
				ScheduleEnrouteBookControlIO scheduleEnrouteBookControlIO = new ScheduleEnrouteBookControlIO();
				scheduleEnrouteBookControlIO.setCode(scheduleEnrouteBookControlDTO.getCode());
				scheduleEnrouteBookControlIO.setDayOfWeek(scheduleEnrouteBookControlDTO.getDayOfWeek());

				ScheduleIO schedule = new ScheduleIO();
				schedule.setCode(scheduleDTO.getCode());
				schedule.setName(scheduleDTO.getName());
				schedule.setServiceNumber(scheduleDTO.getServiceNumber());
				scheduleEnrouteBookControlIO.setSchedule(schedule);

				BaseIO enRouteType = new BaseIO();
				enRouteType.setCode(scheduleEnrouteBookControlDTO.getEnRouteType().getCode());
				enRouteType.setName(scheduleEnrouteBookControlDTO.getEnRouteType().getName());
				scheduleEnrouteBookControlIO.setEnRouteType(enRouteType);

				scheduleEnrouteBookControlIO.setReleaseMinutes(scheduleEnrouteBookControlDTO.getReleaseMinutes());
				scheduleEnrouteBookControlIO.setActiveFlag(scheduleEnrouteBookControlDTO.getActiveFlag());

				List<StageIO> stageList = new ArrayList<StageIO>();
				for (StageDTO stageDTO : scheduleEnrouteBookControlDTO.getStageList()) {
					StageIO stage = new StageIO();
					if (stageDTO.getFromStation() != null && stageDTO.getToStation() != null) {
						StationDTO fromStationDTO = stageDTO.getFromStation().getStation();
						StationDTO toStationDTO = stageDTO.getToStation().getStation();

						StationIO fromStation = new StationIO();
						fromStation.setCode(fromStationDTO.getCode());
						fromStation.setName(fromStationDTO.getName());
						stage.setFromStation(fromStation);

						StationIO toStation = new StationIO();
						toStation.setCode(toStationDTO.getCode());
						toStation.setName(toStationDTO.getName());
						stage.setToStation(toStation);
					}
					stageList.add(stage);
				}
				scheduleEnrouteBookControlIO.setStageList(stageList);
				scheduleEnrouteBookControlList.add(scheduleEnrouteBookControlIO);
			}
		}
		return ResponseIO.success(scheduleEnrouteBookControlList);
	}

	@RequestMapping(value = "/book/control/{enrouteCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleEnrouteBookControlIO> getScheduleEnrouteBookControl(@PathVariable("authtoken") String authtoken, @PathVariable("enrouteCode") String enrouteCode) throws Exception {
		ScheduleEnrouteBookControlIO scheduleEnrouteBookControl = new ScheduleEnrouteBookControlIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleEnrouteBookControlDTO scheduleEnrouteBookControlDTO = new ScheduleEnrouteBookControlDTO();
			scheduleEnrouteBookControlDTO.setCode(enrouteCode);

			scheduleEnrouteBookControlService.getScheduleEnrouteBookControl(authDTO, scheduleEnrouteBookControlDTO);
			scheduleEnrouteBookControl.setCode(scheduleEnrouteBookControlDTO.getCode());
			scheduleEnrouteBookControl.setDayOfWeek(scheduleEnrouteBookControlDTO.getDayOfWeek());

			ScheduleIO schedule = new ScheduleIO();
			schedule.setCode(scheduleEnrouteBookControlDTO.getSchedule().getCode());
			schedule.setName(scheduleEnrouteBookControlDTO.getSchedule().getName());
			schedule.setServiceNumber(scheduleEnrouteBookControlDTO.getSchedule().getServiceNumber());
			scheduleEnrouteBookControl.setSchedule(schedule);

			BaseIO enRouteType = new BaseIO();
			enRouteType.setCode(scheduleEnrouteBookControlDTO.getEnRouteType().getCode());
			enRouteType.setName(scheduleEnrouteBookControlDTO.getEnRouteType().getName());
			scheduleEnrouteBookControl.setEnRouteType(enRouteType);

			scheduleEnrouteBookControl.setReleaseMinutes(scheduleEnrouteBookControlDTO.getReleaseMinutes());
			scheduleEnrouteBookControl.setActiveFlag(scheduleEnrouteBookControlDTO.getActiveFlag());

			List<StageIO> stageList = new ArrayList<StageIO>();
			for (StageDTO stageDTO : scheduleEnrouteBookControlDTO.getStageList()) {
				StageIO stage = new StageIO();
				if (stageDTO.getFromStation() != null && stageDTO.getToStation() != null) {
					StationDTO fromStationDTO = stageDTO.getFromStation().getStation();
					StationDTO toStationDTO = stageDTO.getToStation().getStation();

					StationIO fromStation = new StationIO();
					fromStation.setCode(fromStationDTO.getCode());
					fromStation.setName(fromStationDTO.getName());
					stage.setFromStation(fromStation);

					StationIO toStation = new StationIO();
					toStation.setCode(toStationDTO.getCode());
					toStation.setName(toStationDTO.getName());
					stage.setToStation(toStation);
				}
				stageList.add(stage);
			}
			scheduleEnrouteBookControl.setStageList(stageList);
		}
		return ResponseIO.success(scheduleEnrouteBookControl);
	}
}
