package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.List;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.CancellationTermIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.TicketPhoneBookCancelControlIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.RouteIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.TermIO;
import org.in.com.controller.web.io.TicketPhoneBookControlIO;
import org.in.com.controller.web.io.TicketStatusIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.TicketPhoneBookCancelControlDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TermDTO;
import org.in.com.dto.TicketPhoneBookControlDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.SlabCalenderModeEM;
import org.in.com.dto.enumeration.SlabCalenderTypeEM;
import org.in.com.dto.enumeration.SlabModeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.TermsService;
import org.in.com.service.TicketPhoneBookControlService;
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
@RequestMapping("/{authtoken}/terms")
public class TermsController extends BaseController {
	@Autowired
	TermsService termsService;
	@Autowired
	TicketPhoneBookControlService controlService;

	@RequestMapping(method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TermIO>> getTerms(@PathVariable("authtoken") String authtoken, @RequestParam(required = false) String[] tag, @RequestParam(required = false) String scheduleCode) throws Exception {
		List<TermIO> termIOs = new ArrayList<TermIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TermDTO termDTO = new TermDTO();
			ScheduleDTO scheduleDTO = new ScheduleDTO();
			if (tag == null && StringUtil.isNull(scheduleCode)) {
				throw new ServiceException(ErrorCode.MANDATORY_PARAMETERS_MISSING);
			}
			List<String> tagList = new ArrayList<String>();
			if (tag != null) {
				for (String str : tag) {
					tagList.add(str);
				}
				termDTO.setTagList(tagList);
			}
			scheduleDTO.setCode(scheduleCode);
			List<TermDTO> list = (List<TermDTO>) termsService.getTermsAndConditions(authDTO, termDTO, scheduleDTO);

			for (TermDTO dto : list) {
				TermIO termIO = new TermIO();
				termIO.setName(dto.getName());
				termIO.setCode(dto.getCode());
				termIO.setActiveFlag(dto.getActiveFlag());
				termIO.setSequence(dto.getSequenceId());
				List<String> tagList1 = new ArrayList<String>();
				if (dto.getTagList() != null && !dto.getTagList().isEmpty()) {
					for (String tag1 : dto.getTagList()) {
						tagList1.add(tag1);
					}
				}
				termIO.setTagList(tagList1);
				List<ScheduleIO> scheduleList = new ArrayList<ScheduleIO>();
				if (dto.getSchedule() != null && !dto.getSchedule().isEmpty()) {
					for (ScheduleDTO schedule : dto.getSchedule()) {
						ScheduleIO scheduleIO = new ScheduleIO();
						scheduleIO.setCode(schedule.getCode());
						scheduleList.add(scheduleIO);
					}
				}
				termIO.setSchedule(scheduleList);
				BaseIO transactionType = new BaseIO();
				transactionType.setCode(dto.getTransactionType().getCode());
				transactionType.setName(dto.getTransactionType().getName());
				termIO.setTransactionType(transactionType);
				termIOs.add(termIO);
			}
		}
		return ResponseIO.success(termIOs);

	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<CancellationTermIO> getCancellationTermsUID(@PathVariable("authtoken") String authtoken, @RequestBody TermIO termIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		CancellationTermIO termIO2 = new CancellationTermIO();
		if (authDTO != null) {
			TermDTO termDTO = new TermDTO();
			termDTO.setCode(termIO.getCode());
			termDTO.setName(termIO.getName());
			termDTO.setSequenceId(termIO.getSequence());
			List<String> tagList = new ArrayList<>();
			if (termIO.getTagList() != null && !termIO.getTagList().isEmpty()) {
				for (String tag : termIO.getTagList()) {
					tagList.add(tag);
				}
			}
			termDTO.setTagList(tagList);
			List<ScheduleDTO> scheduleList = new ArrayList<>();
			if (termIO.getSchedule() != null && !termIO.getSchedule().isEmpty()) {
				for (ScheduleIO schedule : termIO.getSchedule()) {
					ScheduleDTO dto = new ScheduleDTO();
					dto.setCode(schedule.getCode());
					scheduleList.add(dto);
				}
			}
			termDTO.setSchedule(scheduleList);
			termDTO.setActiveFlag(termIO.getActiveFlag());
			termDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(termIO.getTransactionType().getCode()));
			termsService.Update(authDTO, termDTO);
			termIO2.setCode(termDTO.getCode());
			termIO2.setActiveFlag(termDTO.getActiveFlag());
		}
		return ResponseIO.success(termIO2);
	}

	@RequestMapping(value = "/ticket/phone/book/time/control/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TicketPhoneBookControlIO> updatePhoneTicketTimeControl(@PathVariable("authtoken") String authtoken, @RequestBody TicketPhoneBookControlIO ticketControl) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TicketPhoneBookControlIO control = new TicketPhoneBookControlIO();
		if (authDTO != null) {
			TicketPhoneBookControlDTO ticketControlDTO = new TicketPhoneBookControlDTO();
			ticketControlDTO.setCode(ticketControl.getCode());
			ticketControlDTO.setActiveFrom(ticketControl.getActiveFrom());
			ticketControlDTO.setActiveTo(ticketControl.getActiveTo());
			ticketControlDTO.setDayOfWeek(ticketControl.getDayOfWeek());
			ticketControlDTO.setAllowMinutes(ticketControl.getAllowMinutes());
			ticketControlDTO.setBlockMinutes(ticketControl.getBlockMinutes());
			ticketControlDTO.setActiveFlag(ticketControl.getActiveFlag());
			ticketControlDTO.setBlockMinutesType(MinutesTypeEM.getMinutesTypeEM(ticketControl.getBlockMinutesType()));

			GroupDTO groupDTO = new GroupDTO();
			groupDTO.setCode(ticketControl.getGroup() != null ? ticketControl.getGroup().getCode() : null);
			ticketControlDTO.setGroup(groupDTO);
			ticketControlDTO.setLookupCode(ticketControl.getLookupCode());
			controlService.updatePhoneBookTimeControlIUD(authDTO, ticketControlDTO);

			control.setCode(ticketControlDTO.getCode());
			control.setActiveFlag(ticketControlDTO.getActiveFlag());
		}
		return ResponseIO.success(control);
	}

	@RequestMapping(value = "/ticket/phone/book/time/control", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TicketPhoneBookControlIO>> getPhoneTicketControl(@PathVariable("authtoken") String authtoken) throws Exception {
		List<TicketPhoneBookControlIO> controlIOList = new ArrayList<TicketPhoneBookControlIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<TicketPhoneBookControlDTO> list = controlService.getPhoneBookTimeControl(authDTO);
			for (TicketPhoneBookControlDTO controlDTO : list) {
				TicketPhoneBookControlIO ticketControl = new TicketPhoneBookControlIO();
				ticketControl.setCode(controlDTO.getCode());
				ticketControl.setActiveFrom(controlDTO.getActiveFrom());
				ticketControl.setActiveTo(controlDTO.getActiveTo());
				ticketControl.setActiveFlag(controlDTO.getActiveFlag());
				ticketControl.setAllowMinutes(controlDTO.getAllowMinutes());
				ticketControl.setBlockMinutes(controlDTO.getBlockMinutes());
				ticketControl.setBlockMinutesType(controlDTO.getBlockMinutesType().getCode());
				ticketControl.setDayOfWeek(controlDTO.getDayOfWeek());

				if (controlDTO.getGroup() != null) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(controlDTO.getGroup().getCode());
					groupIO.setName(controlDTO.getGroup().getName());
					ticketControl.setGroup(groupIO);
				}

				// override
				List<TicketPhoneBookControlIO> overrideIOList = new ArrayList<TicketPhoneBookControlIO>();

				for (TicketPhoneBookControlDTO lookupControlDTO : controlDTO.getOverrideList()) {
					TicketPhoneBookControlIO lookupStageIO = new TicketPhoneBookControlIO();
					lookupStageIO.setCode(lookupControlDTO.getCode());
					lookupStageIO.setActiveFrom(lookupControlDTO.getActiveFrom());
					lookupStageIO.setActiveTo(lookupControlDTO.getActiveTo());
					lookupStageIO.setActiveFlag(lookupControlDTO.getActiveFlag());
					lookupStageIO.setDayOfWeek(lookupControlDTO.getDayOfWeek());
					lookupStageIO.setBlockMinutes(lookupControlDTO.getBlockMinutes());
					lookupStageIO.setAllowMinutes(lookupControlDTO.getAllowMinutes());
					lookupStageIO.setBlockMinutesType(lookupControlDTO.getBlockMinutesType().getCode());
					overrideIOList.add(lookupStageIO);
				}
				ticketControl.setOverrideList(overrideIOList);

				controlIOList.add(ticketControl);
			}
		}
		return ResponseIO.success(controlIOList);
	}

	@RequestMapping(value = "/ticket/phone/book/limit/control/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TicketPhoneBookControlIO> updateTicketControlLimit(@PathVariable("authtoken") String authtoken, @RequestBody TicketPhoneBookControlIO ticketControl) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TicketPhoneBookControlIO control = new TicketPhoneBookControlIO();
		if (authDTO != null) {
			TicketPhoneBookControlDTO ticketControlDTO = new TicketPhoneBookControlDTO();
			ticketControlDTO.setCode(ticketControl.getCode());
			ticketControlDTO.setName(ticketControl.getName());

			if ("UR".equals(ticketControl.getRefferenceType()) && ticketControl.getUser() != null) {
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(ticketControl.getUser().getCode());
				ticketControlDTO.setUserDTO(userDTO);
			}
			else if ("GR".equals(ticketControl.getRefferenceType()) && ticketControl.getGroup() != null) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(ticketControl.getGroup().getCode());
				ticketControlDTO.setGroup(groupDTO);
			}

			List<ScheduleDTO> scheduleDTOList = new ArrayList<ScheduleDTO>();
			if (ticketControl.getScheduleList() != null) {
				for (ScheduleIO scheduleIO : ticketControl.getScheduleList()) {
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setCode(scheduleIO.getCode());
					scheduleDTOList.add(scheduleDTO);
				}
			}

			List<RouteDTO> routeDTOList = new ArrayList<RouteDTO>();
			if (ticketControl.getRouteList() != null) {
				for (RouteIO routeIO : ticketControl.getRouteList()) {
					RouteDTO routeDTO = new RouteDTO();

					StationDTO fromStationDTO = new StationDTO();
					fromStationDTO.setCode(routeIO.getFromStation().getCode());
					routeDTO.setFromStation(fromStationDTO);

					StationDTO toStationDTO = new StationDTO();
					toStationDTO.setCode(routeIO.getToStation().getCode());
					routeDTO.setToStation(toStationDTO);

					routeDTOList.add(routeDTO);
				}
			}

			ticketControlDTO.setScheduleList(scheduleDTOList);
			ticketControlDTO.setRouteList(routeDTOList);
			ticketControlDTO.setMaxSlabValueLimit(ticketControl.getMaxSlabValueLimit());
			ticketControlDTO.setRefferenceType(ticketControl.getRefferenceType());
			ticketControlDTO.setActiveFrom(ticketControl.getActiveFrom());
			ticketControlDTO.setActiveTo(ticketControl.getActiveTo());
			ticketControlDTO.setDayOfWeek(ticketControl.getDayOfWeek());
			ticketControlDTO.setDateType(DateTypeEM.getDateTypeEM(ticketControl.getDateType()));
			ticketControlDTO.setLookupCode(ticketControl.getLookupCode());
			ticketControlDTO.setTicketStatus(ticketControl.getTicketStatus() != null ? TicketStatusEM.getTicketStatusEM(ticketControl.getTicketStatus().getCode()) : null);
			ticketControlDTO.setSlabCalenderType(SlabCalenderTypeEM.getSlabCalenderTypeEM(ticketControl.getSlabCalenderType()));
			ticketControlDTO.setSlabCalenderMode(SlabCalenderModeEM.getSlabCalenderModeEM(ticketControl.getSlabCalenderMode()));
			ticketControlDTO.setSlabMode(SlabModeEM.getSlabModeEM(ticketControl.getSlabMode()));
			ticketControlDTO.setRespectiveFlag(ticketControl.getRespectiveFlag());
			ticketControlDTO.setActiveFlag(ticketControl.getActiveFlag());

			controlService.updateBookLimitControlIUD(authDTO, ticketControlDTO);

			control.setCode(ticketControlDTO.getCode());
			control.setActiveFlag(ticketControlDTO.getActiveFlag());
		}
		return ResponseIO.success(control);
	}

	@RequestMapping(value = "/ticket/phone/book/limit/control", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TicketPhoneBookControlIO>> getTicketControlLimit(@PathVariable("authtoken") String authtoken) throws Exception {
		List<TicketPhoneBookControlIO> controlIOList = new ArrayList<TicketPhoneBookControlIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<TicketPhoneBookControlDTO> list = controlService.getBookLimitsControl(authDTO);
			for (TicketPhoneBookControlDTO controlDTO : list) {
				TicketPhoneBookControlIO ticketControl = new TicketPhoneBookControlIO();
				ticketControl.setCode(controlDTO.getCode());
				ticketControl.setName(controlDTO.getName());

				if (controlDTO.getGroup() != null) {
					GroupIO groupIO = new GroupIO();
					groupIO.setCode(controlDTO.getGroup().getCode());
					groupIO.setName(controlDTO.getGroup().getName());
					ticketControl.setGroup(groupIO);
				}
				else if (controlDTO.getUserDTO() != null) {
					UserIO userIO = new UserIO();
					userIO.setCode(controlDTO.getUserDTO().getCode());
					userIO.setName(controlDTO.getUserDTO().getName());
					ticketControl.setUser(userIO);
				}

				List<ScheduleIO> scheduleList = new ArrayList<ScheduleIO>();
				for (ScheduleDTO scheduleDTO : controlDTO.getScheduleList()) {
					ScheduleIO scheduleIO = new ScheduleIO();
					scheduleIO.setCode(scheduleDTO.getCode());
					scheduleIO.setName(scheduleDTO.getName());
					scheduleList.add(scheduleIO);
				}

				List<RouteIO> routeList = new ArrayList<RouteIO>();
				for (RouteDTO routeDTO : controlDTO.getRouteList()) {
					RouteIO routeIO = new RouteIO();

					BaseIO fromStationIO = new BaseIO();
					fromStationIO.setCode(routeDTO.getFromStation().getCode());
					fromStationIO.setName(routeDTO.getFromStation().getName());
					routeIO.setFromStation(fromStationIO);

					BaseIO toStationIO = new BaseIO();
					toStationIO.setCode(routeDTO.getToStation().getCode());
					toStationIO.setName(routeDTO.getToStation().getName());
					routeIO.setToStation(toStationIO);

					routeList.add(routeIO);
				}

				TicketStatusIO ticketStatus = new TicketStatusIO();
				if (controlDTO.getTicketStatus() != null) {
					ticketStatus.setCode(controlDTO.getTicketStatus().getCode());
					ticketStatus.setName(controlDTO.getTicketStatus().getDescription());
				}
				ticketControl.setTicketStatus(ticketStatus);

				ticketControl.setScheduleList(scheduleList);
				ticketControl.setRouteList(routeList);
				ticketControl.setActiveFrom(controlDTO.getActiveFrom());
				ticketControl.setRefferenceType(controlDTO.getRefferenceType());
				ticketControl.setActiveTo(controlDTO.getActiveTo());
				ticketControl.setDayOfWeek(controlDTO.getDayOfWeek());
				ticketControl.setDateType(controlDTO.getDateType().getCode());
				ticketControl.setSlabCalenderType(controlDTO.getSlabCalenderType() != null ? controlDTO.getSlabCalenderType().getCode() : null);
				ticketControl.setSlabCalenderMode(controlDTO.getSlabCalenderMode() != null ? controlDTO.getSlabCalenderMode().getCode() : null);
				ticketControl.setSlabMode(controlDTO.getSlabMode() != null ? controlDTO.getSlabMode().getCode() : null);
				ticketControl.setMaxSlabValueLimit(controlDTO.getMaxSlabValueLimit());
				ticketControl.setRespectiveFlag(controlDTO.getRespectiveFlag());
				ticketControl.setActiveFlag(controlDTO.getActiveFlag());

				// override
				List<TicketPhoneBookControlIO> overrideIOList = new ArrayList<TicketPhoneBookControlIO>();

				for (TicketPhoneBookControlDTO lookupControlDTO : controlDTO.getOverrideList()) {
					TicketPhoneBookControlIO lookupStageIO = new TicketPhoneBookControlIO();
					lookupStageIO.setCode(lookupControlDTO.getCode());
					lookupStageIO.setName(lookupControlDTO.getName());
					lookupStageIO.setActiveFrom(lookupControlDTO.getActiveFrom());
					lookupStageIO.setActiveTo(lookupControlDTO.getActiveTo());
					lookupStageIO.setDayOfWeek(lookupControlDTO.getDayOfWeek());

					List<ScheduleIO> scheduleOverrideList = new ArrayList<ScheduleIO>();
					for (ScheduleDTO scheduleDTO : lookupControlDTO.getScheduleList()) {
						ScheduleIO scheduleIO = new ScheduleIO();
						scheduleIO.setCode(scheduleDTO.getCode());
						scheduleIO.setName(scheduleDTO.getName());
						scheduleOverrideList.add(scheduleIO);
					}

					List<RouteIO> routeOverrideList = new ArrayList<RouteIO>();
					for (RouteDTO routeDTO : lookupControlDTO.getRouteList()) {
						RouteIO routeIO = new RouteIO();

						BaseIO fromStationIO = new BaseIO();
						fromStationIO.setCode(routeDTO.getFromStation().getCode());
						fromStationIO.setName(routeDTO.getFromStation().getName());
						routeIO.setFromStation(fromStationIO);

						BaseIO toStationIO = new BaseIO();
						toStationIO.setCode(routeDTO.getToStation().getCode());
						toStationIO.setName(routeDTO.getToStation().getName());
						routeIO.setToStation(toStationIO);

						routeOverrideList.add(routeIO);
					}
					lookupStageIO.setScheduleList(scheduleOverrideList);
					lookupStageIO.setRouteList(routeOverrideList);

					TicketStatusIO overrideTicketStatus = new TicketStatusIO();
					if (lookupControlDTO.getTicketStatus() != null) {
						overrideTicketStatus.setCode(lookupControlDTO.getTicketStatus().getCode());
						overrideTicketStatus.setName(lookupControlDTO.getTicketStatus().getDescription());
					}
					lookupStageIO.setTicketStatus(overrideTicketStatus);

					lookupStageIO.setDateType(lookupControlDTO.getDateType().getCode());
					lookupStageIO.setSlabCalenderType(lookupControlDTO.getSlabCalenderType() != null ? controlDTO.getSlabCalenderType().getCode() : null);
					lookupStageIO.setSlabCalenderMode(lookupControlDTO.getSlabCalenderMode() != null ? controlDTO.getSlabCalenderMode().getCode() : null);
					lookupStageIO.setSlabMode(lookupControlDTO.getSlabMode() != null ? controlDTO.getSlabMode().getCode() : null);
					lookupStageIO.setRefferenceType(lookupControlDTO.getRefferenceType());
					lookupStageIO.setMaxSlabValueLimit(lookupControlDTO.getMaxSlabValueLimit());
					lookupStageIO.setActiveFlag(lookupControlDTO.getActiveFlag());
					overrideIOList.add(lookupStageIO);
				}
				ticketControl.setOverrideList(overrideIOList);

				controlIOList.add(ticketControl);
			}
		}
		return ResponseIO.success(controlIOList);
	}

	@RequestMapping(value = "/phone/book/cancel/control", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TicketPhoneBookCancelControlIO>> getPhoneBookCancelControl(@PathVariable("authtoken") String authtoken) throws Exception {
		List<TicketPhoneBookCancelControlIO> phoneBookControlList = new ArrayList<TicketPhoneBookCancelControlIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<TicketPhoneBookCancelControlDTO> list = controlService.getPhoneBookCancelControl(authDTO);
		for (TicketPhoneBookCancelControlDTO phoneBookControlDTO : list) {
			TicketPhoneBookCancelControlIO phoneBookControlIO = new TicketPhoneBookCancelControlIO();
			phoneBookControlIO.setCode(phoneBookControlDTO.getCode());
			phoneBookControlIO.setName(phoneBookControlDTO.getName());
			phoneBookControlIO.setActiveFrom(DateUtil.convertDate(phoneBookControlDTO.getActiveFrom()));
			phoneBookControlIO.setActiveTo(DateUtil.convertDate(phoneBookControlDTO.getActiveTo()));
			phoneBookControlIO.setDayOfWeek(phoneBookControlDTO.getDayOfWeek());
			phoneBookControlIO.setRefferenceType(phoneBookControlDTO.getRefferenceType());
			phoneBookControlIO.setTripMinuteFlag(phoneBookControlDTO.getTripStageFlag());
			phoneBookControlIO.setPolicyMinute(phoneBookControlDTO.getPolicyMinute());
			phoneBookControlIO.setPolicyPattern(phoneBookControlDTO.getPolicyPattern());

			if (phoneBookControlDTO.getGroupList() != null) {
				List<GroupIO> groups = new ArrayList<GroupIO>();
				for (GroupDTO groupDTO : phoneBookControlDTO.getGroupList()) {
					GroupIO group = new GroupIO();
					group.setCode(groupDTO.getCode());
					group.setName(groupDTO.getName());
					groups.add(group);
				}
				phoneBookControlIO.setGroupList(groups);
			}
			else if (phoneBookControlDTO.getUserList() != null) {
				List<UserIO> users = new ArrayList<UserIO>();
				for (UserDTO userDTO : phoneBookControlDTO.getUserList()) {
					UserIO user = new UserIO();
					user.setCode(userDTO.getCode());
					user.setName(userDTO.getName());
					users.add(user);
				}
				phoneBookControlIO.setUserList(users);
			}

			List<ScheduleIO> scheduleList = new ArrayList<ScheduleIO>();
			for (ScheduleDTO scheduleDTO : phoneBookControlDTO.getScheduleList()) {
				ScheduleIO scheduleIO = new ScheduleIO();
				scheduleIO.setCode(scheduleDTO.getCode());
				scheduleIO.setName(scheduleDTO.getName());
				scheduleList.add(scheduleIO);
			}

			List<RouteIO> routeList = new ArrayList<RouteIO>();
			for (RouteDTO routeDTO : phoneBookControlDTO.getRouteList()) {
				RouteIO routeIO = new RouteIO();

				BaseIO fromStationIO = new BaseIO();
				fromStationIO.setCode(routeDTO.getFromStation().getCode());
				fromStationIO.setName(routeDTO.getFromStation().getName());
				routeIO.setFromStation(fromStationIO);

				BaseIO toStationIO = new BaseIO();
				toStationIO.setCode(routeDTO.getToStation().getCode());
				toStationIO.setName(routeDTO.getToStation().getName());
				routeIO.setToStation(toStationIO);

				routeList.add(routeIO);
			}
			phoneBookControlIO.setScheduleList(scheduleList);
			phoneBookControlIO.setRouteList(routeList);
			phoneBookControlIO.setActiveFlag(phoneBookControlDTO.getActiveFlag());
			phoneBookControlList.add(phoneBookControlIO);
		}
		return ResponseIO.success(phoneBookControlList);
	}

	@RequestMapping(value = "/phone/book/cancel/control/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TicketPhoneBookCancelControlIO> updatePhoneBookCancelControl(@PathVariable("authtoken") String authtoken, @RequestBody TicketPhoneBookCancelControlIO phoneBookControlIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TicketPhoneBookCancelControlDTO phoneBookControlDTO = new TicketPhoneBookCancelControlDTO();
		phoneBookControlDTO.setCode(phoneBookControlIO.getCode());
		phoneBookControlDTO.setName(phoneBookControlIO.getName());
		phoneBookControlDTO.setActiveFrom(DateUtil.getDateTime(phoneBookControlIO.getActiveFrom()));
		phoneBookControlDTO.setActiveTo(DateUtil.getDateTime(phoneBookControlIO.getActiveTo()));
		phoneBookControlDTO.setDayOfWeek(phoneBookControlIO.getDayOfWeek());
		phoneBookControlDTO.setRefferenceType(phoneBookControlIO.getRefferenceType());
		phoneBookControlDTO.setTripStageFlag(phoneBookControlIO.getTripMinuteFlag());
		phoneBookControlDTO.setPolicyMinute(phoneBookControlIO.getPolicyMinute());
		phoneBookControlDTO.setPolicyPattern(phoneBookControlIO.getPolicyPattern());

		if ("UR".equals(phoneBookControlDTO.getRefferenceType()) && phoneBookControlIO.getUserList() != null) {
			List<UserDTO> userList = new ArrayList<UserDTO>();
			for (UserIO userIO : phoneBookControlIO.getUserList()) {
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(userIO.getCode());
				userList.add(userDTO);
			}
			phoneBookControlDTO.setUserList(userList);

		}
		else if ("GR".equals(phoneBookControlDTO.getRefferenceType()) && phoneBookControlIO.getGroupList() != null) {
			List<GroupDTO> groupList = new ArrayList<GroupDTO>();
			for (GroupIO groupIO : phoneBookControlIO.getGroupList()) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(groupIO.getCode());
				groupList.add(groupDTO);
			}
			phoneBookControlDTO.setGroupList(groupList);
		}

		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		if (phoneBookControlIO.getScheduleList() != null) {
			for (ScheduleIO scheduleIO : phoneBookControlIO.getScheduleList()) {
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(scheduleIO.getCode());
				scheduleList.add(scheduleDTO);
			}
		}

		List<RouteDTO> routeList = new ArrayList<RouteDTO>();
		if (phoneBookControlIO.getRouteList() != null) {
			for (RouteIO routeIO : phoneBookControlIO.getRouteList()) {
				RouteDTO routeDTO = new RouteDTO();

				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setCode(routeIO.getFromStation().getCode());
				routeDTO.setFromStation(fromStationDTO);

				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setCode(routeIO.getToStation().getCode());
				routeDTO.setToStation(toStationDTO);

				routeList.add(routeDTO);
			}
		}

		phoneBookControlDTO.setScheduleList(scheduleList);
		phoneBookControlDTO.setRouteList(routeList);
		phoneBookControlDTO.setActiveFlag(phoneBookControlIO.getActiveFlag());
		controlService.updatePhoneBookCancelControl(authDTO, phoneBookControlDTO);
		TicketPhoneBookCancelControlIO phoneBookCancelControl = new TicketPhoneBookCancelControlIO();
		phoneBookCancelControl.setCode(phoneBookControlDTO.getCode());
		phoneBookCancelControl.setActiveFlag(phoneBookControlDTO.getActiveFlag());
		return ResponseIO.success(phoneBookCancelControl);
	}
}
