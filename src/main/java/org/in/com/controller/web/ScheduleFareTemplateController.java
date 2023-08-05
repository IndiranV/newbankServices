package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.AuditIO;
import org.in.com.controller.web.io.BusIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.RouteIO;
import org.in.com.controller.web.io.ScheduleFareTemplateIO;
import org.in.com.controller.web.io.StageFareIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleFareTemplateDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.service.AuthService;
import org.in.com.service.ScheduleFareTemplateService;
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
@RequestMapping("{authtoken}/schedule/fare/template")
public class ScheduleFareTemplateController extends BaseController {
	@Autowired
	AuthService authService;
	@Autowired
	ScheduleFareTemplateService scheduleFareTemplateService;

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ScheduleFareTemplateIO> updateScheduleFareTemplate(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleFareTemplateIO scheduleFareTemplate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ScheduleFareTemplateIO scheduleFareTemplateIO = new ScheduleFareTemplateIO();
		ScheduleFareTemplateDTO scheduleFareTemplateDTO = new ScheduleFareTemplateDTO();
		scheduleFareTemplateDTO.setCode(scheduleFareTemplate.getCode());
		scheduleFareTemplateDTO.setName(scheduleFareTemplate.getName());

		BusDTO bus = new BusDTO();
		bus.setCode(scheduleFareTemplate.getBus().getCode());
		scheduleFareTemplateDTO.setBus(bus);

		List<RouteDTO> routeList = new ArrayList<RouteDTO>();
		if (scheduleFareTemplate.getStageFare() != null) {
			for (RouteIO route : scheduleFareTemplate.getStageFare()) {
				if (route.getFromStation() == null || StringUtil.isNull(route.getFromStation().getCode()) || route.getToStation() == null || StringUtil.isNull(route.getToStation().getCode())) {
					continue;
				}
				RouteDTO routeDTO = new RouteDTO();
				StationDTO fromStation = new StationDTO();
				fromStation.setCode(route.getFromStation().getCode());
				routeDTO.setFromStation(fromStation);
				StationDTO toStation = new StationDTO();
				toStation.setCode(route.getToStation().getCode());
				routeDTO.setToStation(toStation);

				List<StageFareDTO> stageFareList = new ArrayList<StageFareDTO>();
				if (route.getStageFare() == null) {
					continue;
				}
				for (StageFareIO stageFare : route.getStageFare()) {
					StageFareDTO stageFareDTO = new StageFareDTO();
					stageFareDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(stageFare.getSeatType()));
					stageFareDTO.setFare(stageFare.getFare());
					stageFareList.add(stageFareDTO);
				}

				routeDTO.setStageFare(stageFareList);
				routeList.add(routeDTO);
			}
		}
		scheduleFareTemplateDTO.setStageFare(routeList);
		scheduleFareTemplateDTO.setActiveFlag(scheduleFareTemplate.getActiveFlag());
		scheduleFareTemplateService.Update(authDTO, scheduleFareTemplateDTO);
		scheduleFareTemplateIO.setCode(scheduleFareTemplateDTO.getCode());
		return ResponseIO.success(scheduleFareTemplateIO);
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ScheduleFareTemplateIO>> getAllScheduleFareTemplate(@PathVariable("authtoken") String authtoken) throws Exception {
		List<ScheduleFareTemplateIO> scheduleFareTemplateList = new ArrayList<ScheduleFareTemplateIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<ScheduleFareTemplateDTO> list = scheduleFareTemplateService.getAll(authDTO);

		for (ScheduleFareTemplateDTO scheduleFareTemplateDTO : list) {
			ScheduleFareTemplateIO scheduleFareTemplateIO = new ScheduleFareTemplateIO();
			scheduleFareTemplateIO.setCode(scheduleFareTemplateDTO.getCode());
			scheduleFareTemplateIO.setName(scheduleFareTemplateDTO.getName());

			BusIO bus = new BusIO();
			bus.setCode(scheduleFareTemplateDTO.getBus().getCode());
			bus.setName(scheduleFareTemplateDTO.getBus().getName());
			scheduleFareTemplateIO.setBus(bus);

			List<RouteIO> stageList = new ArrayList<RouteIO>();
			for (RouteDTO routeDTO : scheduleFareTemplateDTO.getStageFare()) {
				RouteIO routeIO = new RouteIO();

				StationIO fromStation = new StationIO();
				fromStation.setCode(routeDTO.getFromStation().getCode());
				fromStation.setName(routeDTO.getFromStation().getName());
				routeIO.setFromStation(fromStation);

				StationIO toStation = new StationIO();
				toStation.setCode(routeDTO.getToStation().getCode());
				toStation.setName(routeDTO.getToStation().getName());
				routeIO.setToStation(toStation);

				List<StageFareIO> stageFareList = new ArrayList<StageFareIO>();
				for (StageFareDTO stageFareDTO : routeDTO.getStageFare()) {
					StageFareIO stageFareIO = new StageFareIO();
					stageFareIO.setSeatType(stageFareDTO.getBusSeatType().getCode());
					stageFareIO.setFare(stageFareDTO.getFare());
					stageFareList.add(stageFareIO);
				}
				routeIO.setStageFare(stageFareList);
				stageList.add(routeIO);
			}
			scheduleFareTemplateIO.setStageFare(stageList);

			UserIO userIO = new UserIO();
			userIO.setCode(scheduleFareTemplateDTO.getAudit().getUser().getCode());
			userIO.setName(scheduleFareTemplateDTO.getAudit().getUser().getName());

			AuditIO auditIO = new AuditIO();
			auditIO.setUser(userIO);
			auditIO.setUpdatedAt(scheduleFareTemplateDTO.getAudit().getUpdatedAt());
			scheduleFareTemplateIO.setAudit(auditIO);

			scheduleFareTemplateIO.setActiveFlag(scheduleFareTemplateDTO.getActiveFlag());
			scheduleFareTemplateList.add(scheduleFareTemplateIO);
		}

		return ResponseIO.success(scheduleFareTemplateList);
	}

	@RequestMapping(value = "/trip/{tripCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ScheduleFareTemplateIO>> getScheduleStageFareTemplate(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		List<ScheduleFareTemplateIO> scheduleFareTemplateList = new ArrayList<ScheduleFareTemplateIO>();
		List<ScheduleFareTemplateDTO> list = scheduleFareTemplateService.getTripStageTemplate(authDTO, tripDTO);

		for (ScheduleFareTemplateDTO scheduleFareTemplateDTO : list) {
			ScheduleFareTemplateIO scheduleFareTemplateIO = new ScheduleFareTemplateIO();
			scheduleFareTemplateIO.setCode(scheduleFareTemplateDTO.getCode());
			scheduleFareTemplateIO.setName(scheduleFareTemplateDTO.getName());

			BusIO bus = new BusIO();
			bus.setCode(scheduleFareTemplateDTO.getBus().getCode());
			bus.setName(scheduleFareTemplateDTO.getBus().getName());
			scheduleFareTemplateIO.setBus(bus);

			List<RouteIO> stageList = new ArrayList<RouteIO>();
			for (RouteDTO routeDTO : scheduleFareTemplateDTO.getStageFare()) {
				RouteIO routeIO = new RouteIO();

				StationIO fromStation = new StationIO();
				fromStation.setCode(routeDTO.getFromStation().getCode());
				fromStation.setName(routeDTO.getFromStation().getName());
				routeIO.setFromStation(fromStation);

				StationIO toStation = new StationIO();
				toStation.setCode(routeDTO.getToStation().getCode());
				toStation.setName(routeDTO.getToStation().getName());
				routeIO.setToStation(toStation);

				List<StageFareIO> stageFareList = new ArrayList<StageFareIO>();
				for (StageFareDTO stageFareDTO : routeDTO.getStageFare()) {
					StageFareIO stageFareIO = new StageFareIO();
					stageFareIO.setSeatType(stageFareDTO.getBusSeatType().getCode());
					stageFareIO.setFare(stageFareDTO.getFare());
					stageFareList.add(stageFareIO);
				}
				routeIO.setStageFare(stageFareList);
				stageList.add(routeIO);
			}
			scheduleFareTemplateIO.setStageFare(stageList);

			scheduleFareTemplateIO.setActiveFlag(scheduleFareTemplateDTO.getActiveFlag());
			scheduleFareTemplateList.add(scheduleFareTemplateIO);
		}

		return ResponseIO.success(scheduleFareTemplateList);
	}
}
