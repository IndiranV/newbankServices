package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusVehicleIO;
import org.in.com.controller.web.io.OrganizationIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.SectorIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.service.SectorService;
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
@RequestMapping("/{authtoken}/sector")
public class SectorController extends BaseController {

	@Autowired
	SectorService sectorService;

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<SectorIO> updateSector(@PathVariable("authtoken") String authtoken, @RequestBody SectorIO sector) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		SectorIO sectorio = new SectorIO();
		if (authDTO != null) {
			SectorDTO sectorDTO = new SectorDTO();
			sectorDTO.setCode(sector.getCode());
			sectorDTO.setName(sector.getName());
			sectorDTO.setActiveFlag(sector.getActiveFlag());

			SectorDTO repoSector = sectorService.Update(authDTO, sectorDTO);
			if (repoSector != null) {
				sectorio.setCode(repoSector.getCode());
				sectorio.setActiveFlag(repoSector.getActiveFlag());
			}
		}
		return ResponseIO.success(sectorio);
	}

	@RequestMapping(value = "/{sectorCode}/schedule/{scheduleCode}/{actionCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<SectorIO> updateSectorSchedule(@PathVariable("authtoken") String authtoken, @PathVariable("sectorCode") String sectorCode, @PathVariable("scheduleCode") String scheduleCode, @PathVariable("actionCode") String actionCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		SectorIO sector = new SectorIO();
		if (authDTO != null) {
			SectorDTO sectorDTO = new SectorDTO();
			sectorDTO.setCode(sectorCode);

			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleCode);

			sectorDTO.setActiveFlag("ADD".equals(actionCode) ? Numeric.ONE_INT : Numeric.ZERO_INT);

			sectorService.updateSectorSchedule(authDTO, sectorDTO, scheduleDTO);
			sector.setCode(sectorDTO.getCode());
			sector.setActiveFlag(sectorDTO.getActiveFlag());
		}
		return ResponseIO.success(sector);
	}

	@RequestMapping(value = "/{sectorCode}/vehicle/{vehicleCode}/{actionCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<SectorIO> updateSectorVehicle(@PathVariable("authtoken") String authtoken, @PathVariable("sectorCode") String sectorCode, @PathVariable("vehicleCode") String vehicleCode, @PathVariable("actionCode") String actionCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		SectorIO sector = new SectorIO();
		if (authDTO != null) {
			SectorDTO sectorDTO = new SectorDTO();
			sectorDTO.setCode(sectorCode);

			BusVehicleDTO vehicleDTO = new BusVehicleDTO();
			vehicleDTO.setCode(vehicleCode);

			sectorDTO.setActiveFlag("ADD".equals(actionCode) ? Numeric.ONE_INT : Numeric.ZERO_INT);

			sectorService.updateSectorVehicle(authDTO, sectorDTO, vehicleDTO);
			sector.setCode(sectorDTO.getCode());
			sector.setActiveFlag(sectorDTO.getActiveFlag());
		}
		return ResponseIO.success(sector);
	}

	@RequestMapping(value = "/{sectorCode}/station/{stationCode}/{actionCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<SectorIO> updateSectorStation(@PathVariable("authtoken") String authtoken, @PathVariable("sectorCode") String sectorCode, @PathVariable("stationCode") String stationCode, @PathVariable("actionCode") String actionCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		SectorIO sector = new SectorIO();
		if (authDTO != null) {
			SectorDTO sectorDTO = new SectorDTO();
			sectorDTO.setCode(sectorCode);

			StationDTO stationDTO = new StationDTO();
			stationDTO.setCode(stationCode);

			sectorDTO.setActiveFlag("ADD".equals(actionCode) ? Numeric.ONE_INT : Numeric.ZERO_INT);

			sectorService.updateSectorStation(authDTO, sectorDTO, stationDTO);
			sector.setCode(sectorDTO.getCode());
			sector.setActiveFlag(sectorDTO.getActiveFlag());
		}
		return ResponseIO.success(sector);
	}

	@RequestMapping(value = "/{sectorCode}/organization/{organizationCode}/{actionCode}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<SectorIO> updateSectorOrganization(@PathVariable("authtoken") String authtoken, @PathVariable("sectorCode") String sectorCode, @PathVariable("organizationCode") String organizationCode, @PathVariable("actionCode") String actionCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		SectorIO sector = new SectorIO();
		if (authDTO != null) {
			SectorDTO sectorDTO = new SectorDTO();
			sectorDTO.setCode(sectorCode);

			OrganizationDTO organizationDTO = new OrganizationDTO();
			organizationDTO.setCode(organizationCode);

			sectorDTO.setActiveFlag("ADD".equals(actionCode) ? Numeric.ONE_INT : Numeric.ZERO_INT);

			sectorService.updateSectorOrganization(authDTO, sectorDTO, organizationDTO);
			sector.setCode(sectorDTO.getCode());
			sector.setActiveFlag(sectorDTO.getActiveFlag());
		}
		return ResponseIO.success(sector);
	}

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<SectorIO>> getAllSector(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<SectorIO> sectorIOList = new ArrayList<SectorIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<SectorDTO> sectorDTOlist = sectorService.getAll(authDTO);
			for (SectorDTO sectorDTO : sectorDTOlist) {
				if (activeFlag != -1 && activeFlag != sectorDTO.getActiveFlag()) {
					continue;
				}
				SectorIO sectorIO = new SectorIO();
				sectorIO.setCode(sectorDTO.getCode());
				sectorIO.setName(sectorDTO.getName());

				List<ScheduleIO> scheduleList = new ArrayList<>();
				for (ScheduleDTO scheduleDTO : sectorDTO.getSchedule()) {
					ScheduleIO scheduleIO = new ScheduleIO();
					scheduleIO.setCode(scheduleDTO.getCode());
					scheduleIO.setName(scheduleDTO.getName());
					scheduleIO.setServiceNumber(scheduleDTO.getServiceNumber());
					scheduleList.add(scheduleIO);
				}
				sectorIO.setSchedules(scheduleList);

				List<BusVehicleIO> vehicleList = new ArrayList<>();
				for (BusVehicleDTO vehicleDTO : sectorDTO.getVehicle()) {
					BusVehicleIO vehicleIO = new BusVehicleIO();
					vehicleIO.setCode(vehicleDTO.getCode());
					vehicleIO.setName(vehicleDTO.getName());
					vehicleIO.setRegistationNumber(vehicleDTO.getRegistationNumber());
					vehicleList.add(vehicleIO);
				}
				sectorIO.setVehicles(vehicleList);

				List<StationIO> stationList = new ArrayList<>();
				for (StationDTO stationDTO : sectorDTO.getStation()) {
					StationIO stationIO = new StationIO();
					stationIO.setCode(stationDTO.getCode());
					stationIO.setName(stationDTO.getName());
					stationList.add(stationIO);
				}
				sectorIO.setStations(stationList);

				List<OrganizationIO> organozationList = new ArrayList<>();
				for (OrganizationDTO organizationDTO : sectorDTO.getOrganization()) {
					OrganizationIO organizationIO = new OrganizationIO();
					organizationIO.setCode(organizationDTO.getCode());
					organizationIO.setName(organizationDTO.getName());
					organozationList.add(organizationIO);
				}
				sectorIO.setOrganizations(organozationList);

				sectorIO.setActiveFlag(sectorDTO.getActiveFlag());
				sectorIOList.add(sectorIO);
			}

		}
		return ResponseIO.success(sectorIOList);
	}

	@RequestMapping(value = "/{sectorCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<SectorIO> getSector(@PathVariable("authtoken") String authtoken, @PathVariable("sectorCode") String sectorCode) throws Exception {
		SectorIO sectorIO = new SectorIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			SectorDTO sectorDTO = new SectorDTO();
			sectorDTO.setCode(sectorCode);
			sectorDTO = sectorService.getSector(authDTO, sectorDTO);
			sectorIO.setCode(sectorDTO.getCode());
			sectorIO.setName(sectorDTO.getName());

			List<ScheduleIO> scheduleList = new ArrayList<>();
			List<BusVehicleIO> vehicleList = new ArrayList<>();
			List<StationIO> stationList = new ArrayList<>();
			List<OrganizationIO> organozationList = new ArrayList<>();

			if (sectorDTO.getSchedule() != null && sectorDTO.getVehicle() != null && sectorDTO.getStation() != null && sectorDTO.getOrganization() != null) {
				for (ScheduleDTO scheduleDTO : sectorDTO.getSchedule()) {
					ScheduleIO scheduleIO = new ScheduleIO();
					scheduleIO.setCode(scheduleDTO.getCode());
					scheduleIO.setName(scheduleDTO.getName());
					scheduleIO.setServiceNumber(scheduleDTO.getServiceNumber());
					scheduleList.add(scheduleIO);
				}
				for (BusVehicleDTO vehicleDTO : sectorDTO.getVehicle()) {
					BusVehicleIO vehicleIO = new BusVehicleIO();
					vehicleIO.setCode(vehicleDTO.getCode());
					vehicleIO.setName(vehicleDTO.getName());
					vehicleIO.setRegistationNumber(vehicleDTO.getRegistationNumber());
					vehicleList.add(vehicleIO);
				}
				for (StationDTO stationDTO : sectorDTO.getStation()) {
					StationIO stationIO = new StationIO();
					stationIO.setCode(stationDTO.getCode());
					stationIO.setName(stationDTO.getName());
					stationList.add(stationIO);
				}
				for (OrganizationDTO organizationDTO : sectorDTO.getOrganization()) {
					OrganizationIO organizationIO = new OrganizationIO();
					organizationIO.setCode(organizationDTO.getCode());
					organizationIO.setName(organizationDTO.getName());
					organozationList.add(organizationIO);
				}
			}

			sectorIO.setSchedules(scheduleList);
			sectorIO.setVehicles(vehicleList);
			sectorIO.setStations(stationList);
			sectorIO.setOrganizations(organozationList);
			sectorIO.setActiveFlag(sectorDTO.getActiveFlag());

		}
		return ResponseIO.success(sectorIO);
	}

	@RequestMapping(value = "/{sectorCode}/user/{userCode}/{action}", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> updateSectorUser(@PathVariable("authtoken") String authtoken, @PathVariable("sectorCode") String sectorCode, @PathVariable("userCode") String userCode, @PathVariable("action") String action) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		SectorDTO sectorDTO = new SectorDTO();
		sectorDTO.setCode(sectorCode);

		UserDTO userDTO = new UserDTO();
		userDTO.setCode(userCode);

		if (action.equals("ADD")) {
			sectorDTO.setActiveFlag(1);
		}
		else {
			sectorDTO.setActiveFlag(0);
		}

		sectorService.updateSectorUser(authDTO, sectorDTO, userDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/user/{userCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<SectorIO>> getSectorUser(@PathVariable("authtoken") String authtoken, @PathVariable("userCode") String userCode) throws Exception {
		List<SectorIO> sectorList = new ArrayList<SectorIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			UserDTO userDTO = new UserDTO();
			userDTO.setCode(userCode);
			List<SectorDTO> list = sectorService.getSectorUser(authDTO, userDTO);
			for (SectorDTO sectorDTO : list) {
				SectorIO sectorIO = new SectorIO();
				sectorIO.setCode(sectorDTO.getCode());
				sectorIO.setName(sectorDTO.getName());

				List<ScheduleIO> scheduleList = new ArrayList<>();
				for (ScheduleDTO scheduleDTO : sectorDTO.getSchedule()) {
					ScheduleIO scheduleIO = new ScheduleIO();
					scheduleIO.setCode(scheduleDTO.getCode());
					scheduleIO.setName(scheduleDTO.getName());
					scheduleIO.setServiceNumber(scheduleDTO.getServiceNumber());
					scheduleList.add(scheduleIO);
				}
				sectorIO.setSchedules(scheduleList);

				List<BusVehicleIO> vehicleList = new ArrayList<>();
				for (BusVehicleDTO vehicleDTO : sectorDTO.getVehicle()) {
					BusVehicleIO vehicleIO = new BusVehicleIO();
					vehicleIO.setCode(vehicleDTO.getCode());
					vehicleIO.setName(vehicleDTO.getName());
					vehicleIO.setRegistationNumber(vehicleDTO.getRegistationNumber());
					vehicleList.add(vehicleIO);
				}
				sectorIO.setVehicles(vehicleList);

				List<StationIO> stationList = new ArrayList<>();
				for (StationDTO stationDTO : sectorDTO.getStation()) {
					StationIO stationIO = new StationIO();
					stationIO.setCode(stationDTO.getCode());
					stationIO.setName(stationDTO.getName());
					stationList.add(stationIO);
				}
				sectorIO.setStations(stationList);

				List<OrganizationIO> organozationList = new ArrayList<>();
				for (OrganizationDTO organizationDTO : sectorDTO.getOrganization()) {
					OrganizationIO organizationIO = new OrganizationIO();
					organizationIO.setCode(organizationDTO.getCode());
					organizationIO.setName(organizationDTO.getName());
					organozationList.add(organizationIO);
				}
				sectorIO.setOrganizations(organozationList);

				sectorIO.setActiveFlag(sectorDTO.getActiveFlag());
				sectorList.add(sectorIO);
			}
		}
		return ResponseIO.success(sectorList);
	}

	@RequestMapping(value = "/{sectorCode}/user", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<UserIO>> getSectorUsers(@PathVariable("authtoken") String authtoken, @PathVariable("sectorCode") String sectorCode) throws Exception {
		List<UserIO> userList = new ArrayList<UserIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			SectorDTO sectorDTO = new SectorDTO();
			sectorDTO.setCode(sectorCode);
			List<UserDTO> list = sectorService.getSectorUsers(authDTO, sectorDTO);
			for (UserDTO userDTO : list) {
				UserIO userIO = new UserIO();
				userIO.setCode(userDTO.getCode());
				userIO.setName(userDTO.getName());
				userIO.setActiveFlag(userDTO.getActiveFlag());
				userList.add(userIO);
			}
		}
		return ResponseIO.success(userList);
	}
}
