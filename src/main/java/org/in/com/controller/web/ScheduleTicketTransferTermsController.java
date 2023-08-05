package org.in.com.controller.web;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.RouteIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.ScheduleTicketTransferTermsIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.service.ScheduleTicketTransferTermsService;
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
@RequestMapping("/{authtoken}/schedule/ticket/transfer/terms")
public class ScheduleTicketTransferTermsController extends BaseController {
	@Autowired
	ScheduleTicketTransferTermsService scheduleTicketTransferTermsService;

	@RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<ScheduleTicketTransferTermsIO>> getScheduleTicketTransferTerms(@PathVariable("authtoken") String authtoken, @RequestParam(required = false, defaultValue = "-1") int activeFlag) throws Exception {
		List<ScheduleTicketTransferTermsIO> scheduleTicketTransferTermsList = new ArrayList<ScheduleTicketTransferTermsIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<ScheduleTicketTransferTermsDTO> list = scheduleTicketTransferTermsService.getAllScheduleTicketTransferTerms(authDTO);
			for (ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO : list) {
				if (activeFlag != -1 && activeFlag != scheduleTicketTransferTermsDTO.getActiveFlag()) {
					continue;
				}
				ScheduleTicketTransferTermsIO scheduleTicketTransferTerms = new ScheduleTicketTransferTermsIO();
				scheduleTicketTransferTerms.setCode(scheduleTicketTransferTermsDTO.getCode());
				scheduleTicketTransferTerms.setMinutes(scheduleTicketTransferTermsDTO.getMinutes());
				scheduleTicketTransferTerms.setChargeAmount(scheduleTicketTransferTermsDTO.getChargeAmount());
				scheduleTicketTransferTerms.setDayOfWeek(scheduleTicketTransferTermsDTO.getDayOfWeek());
				scheduleTicketTransferTerms.setAllowBookedUser(scheduleTicketTransferTermsDTO.getAllowBookedUser());

				BaseIO chargeType = new BaseIO();
				chargeType.setCode(scheduleTicketTransferTermsDTO.getChargeType().getCode());
				chargeType.setName(scheduleTicketTransferTermsDTO.getChargeType().getName());
				scheduleTicketTransferTerms.setChargeType(chargeType);

				BaseIO minutesType = new BaseIO();
				minutesType.setCode(scheduleTicketTransferTermsDTO.getMinutesType().getCode());
				minutesType.setName(scheduleTicketTransferTermsDTO.getMinutesType().getName());
				scheduleTicketTransferTerms.setMinutesType(minutesType);

				List<ScheduleIO> scheduleList = new ArrayList<ScheduleIO>();
				if (scheduleTicketTransferTermsDTO.getScheduleList() != null) {
					for (ScheduleDTO scheduleDTO : scheduleTicketTransferTermsDTO.getScheduleList()) {
						ScheduleIO schedule = new ScheduleIO();
						schedule.setCode(scheduleDTO.getCode());
						schedule.setName(scheduleDTO.getName());
						scheduleList.add(schedule);
					}
				}
				scheduleTicketTransferTerms.setScheduleList(scheduleList);

				List<RouteIO> routeList = new ArrayList<>();
				if (scheduleTicketTransferTermsDTO.getRouteList() != null) {
					for (RouteDTO routeDTO : scheduleTicketTransferTermsDTO.getRouteList()) {
						RouteIO route = new RouteIO();
						StationIO fromStation = new StationIO();
						fromStation.setCode(routeDTO.getFromStation().getCode());
						fromStation.setName(routeDTO.getFromStation().getName());
						route.setFromStation(fromStation);

						StationIO toStation = new StationIO();
						toStation.setCode(routeDTO.getToStation().getCode());
						toStation.setName(routeDTO.getToStation().getName());
						route.setToStation(toStation);
						routeList.add(route);
					}
				}
				scheduleTicketTransferTerms.setRouteList(routeList);

				List<GroupIO> groupList = new ArrayList<GroupIO>();
				for (GroupDTO GroupDTO : scheduleTicketTransferTermsDTO.getGroupList()) {
					GroupIO group = new GroupIO();
					group.setCode(GroupDTO.getCode());
					group.setName(GroupDTO.getName());
					groupList.add(group);
				}
				scheduleTicketTransferTerms.setGroupList(groupList);

				List<GroupIO> bookedGroupList = new ArrayList<GroupIO>();
				for (GroupDTO GroupDTO : scheduleTicketTransferTermsDTO.getBookedUserGroups()) {
					GroupIO group = new GroupIO();
					group.setCode(GroupDTO.getCode());
					group.setName(GroupDTO.getName());
					bookedGroupList.add(group);
				}
				scheduleTicketTransferTerms.setBookedUserGroups(bookedGroupList);

				scheduleTicketTransferTerms.setActiveFrom(scheduleTicketTransferTermsDTO.getActiveFrom().format(Text.DATE_DATE4J));
				scheduleTicketTransferTerms.setActiveTo(scheduleTicketTransferTermsDTO.getActiveTo().format(Text.DATE_DATE4J));
				scheduleTicketTransferTerms.setActiveFlag(scheduleTicketTransferTermsDTO.getActiveFlag());

				List<ScheduleTicketTransferTermsIO> overrideList = new ArrayList<ScheduleTicketTransferTermsIO>();
				for (ScheduleTicketTransferTermsDTO transferTermsDTO : scheduleTicketTransferTermsDTO.getOverrideList()) {
					ScheduleTicketTransferTermsIO overrideTransferTerms = new ScheduleTicketTransferTermsIO();
					overrideTransferTerms.setCode(transferTermsDTO.getCode());
					overrideTransferTerms.setMinutes(transferTermsDTO.getMinutes());
					overrideTransferTerms.setChargeAmount(transferTermsDTO.getChargeAmount());
					overrideTransferTerms.setDayOfWeek(transferTermsDTO.getDayOfWeek());
					overrideTransferTerms.setAllowBookedUser(transferTermsDTO.getAllowBookedUser());

					BaseIO overrideMinutesType = new BaseIO();
					overrideMinutesType.setCode(transferTermsDTO.getMinutesType().getCode());
					overrideMinutesType.setName(transferTermsDTO.getMinutesType().getName());
					overrideTransferTerms.setMinutesType(overrideMinutesType);

					BaseIO overrideChargeType = new BaseIO();
					overrideChargeType.setCode(transferTermsDTO.getChargeType().getCode());
					overrideChargeType.setName(transferTermsDTO.getChargeType().getName());
					overrideTransferTerms.setChargeType(overrideChargeType);

					List<ScheduleIO> overrideScheduleList = new ArrayList<ScheduleIO>();
					for (ScheduleDTO scheduleDTO : transferTermsDTO.getScheduleList()) {
						ScheduleIO schedule = new ScheduleIO();
						schedule.setCode(scheduleDTO.getCode());
						schedule.setName(scheduleDTO.getName());
						overrideScheduleList.add(schedule);
					}
					overrideTransferTerms.setScheduleList(overrideScheduleList);

					List<GroupIO> overrideGroupList = new ArrayList<GroupIO>();
					for (GroupDTO GroupDTO : transferTermsDTO.getGroupList()) {
						GroupIO group = new GroupIO();
						group.setCode(GroupDTO.getCode());
						group.setName(GroupDTO.getName());
						overrideGroupList.add(group);
					}
					overrideTransferTerms.setGroupList(overrideGroupList);

					List<GroupIO> overridebookedGroupList = new ArrayList<GroupIO>();
					for (GroupDTO GroupDTO : transferTermsDTO.getBookedUserGroups()) {
						GroupIO group = new GroupIO();
						group.setCode(GroupDTO.getCode());
						group.setName(GroupDTO.getName());
						overridebookedGroupList.add(group);
					}
					overrideTransferTerms.setBookedUserGroups(overridebookedGroupList);

					overrideTransferTerms.setActiveFrom(transferTermsDTO.getActiveFrom().format(Text.DATE_DATE4J));
					overrideTransferTerms.setActiveTo(transferTermsDTO.getActiveTo().format(Text.DATE_DATE4J));
					overrideTransferTerms.setActiveFlag(transferTermsDTO.getActiveFlag());
					overrideList.add(overrideTransferTerms);
				}
				scheduleTicketTransferTerms.setOverrideList(overrideList);

				scheduleTicketTransferTermsList.add(scheduleTicketTransferTerms);
			}
		}
		return ResponseIO.success(scheduleTicketTransferTermsList);
	}

	@RequestMapping(value = "/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<ScheduleTicketTransferTermsIO> updateScheduleTicketTransferTerms(@PathVariable("authtoken") String authtoken, @RequestBody ScheduleTicketTransferTermsIO scheduleTicketTransferTerms) throws Exception {
		ScheduleTicketTransferTermsIO ticketTransferTerms = new ScheduleTicketTransferTermsIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO = new ScheduleTicketTransferTermsDTO();
			scheduleTicketTransferTermsDTO.setCode(scheduleTicketTransferTerms.getCode());
			scheduleTicketTransferTermsDTO.setMinutes(scheduleTicketTransferTerms.getMinutes());
			scheduleTicketTransferTermsDTO.setMinutesType(MinutesTypeEM.getMinutesTypeEM(scheduleTicketTransferTerms.getMinutesType().getCode()));
			scheduleTicketTransferTermsDTO.setChargeAmount(scheduleTicketTransferTerms.getChargeAmount());
			scheduleTicketTransferTermsDTO.setChargeType(FareTypeEM.getFareTypeEM(scheduleTicketTransferTerms.getChargeType().getCode()));
			scheduleTicketTransferTermsDTO.setDayOfWeek(scheduleTicketTransferTerms.getDayOfWeek());
			scheduleTicketTransferTermsDTO.setLookupCode(scheduleTicketTransferTerms.getLookupCode());
			scheduleTicketTransferTermsDTO.setAllowBookedUser(scheduleTicketTransferTerms.getAllowBookedUser());

			List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
			if (scheduleTicketTransferTerms.getScheduleList() != null) {
				for (ScheduleIO schedule : scheduleTicketTransferTerms.getScheduleList()) {
					if (StringUtil.isNull(schedule.getCode())) {
						continue;
					}
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setCode(schedule.getCode());
					scheduleList.add(scheduleDTO);
				}
			}
			scheduleTicketTransferTermsDTO.setScheduleList(scheduleList);

			List<RouteDTO> routeList = new ArrayList<RouteDTO>();
			if (scheduleTicketTransferTerms.getRouteList() != null) {
				for (RouteIO route : scheduleTicketTransferTerms.getRouteList()) {
					if (route.getFromStation() != null && StringUtil.isNotNull(route.getFromStation().getCode()) && route.getToStation() != null && StringUtil.isNotNull(route.getToStation().getCode())) {
						RouteDTO routeDTO = new RouteDTO();

						StationDTO fromStationDTO = new StationDTO();
						fromStationDTO.setCode(route.getFromStation().getCode().trim());
						StationDTO toStationDTO = new StationDTO();
						toStationDTO.setCode(route.getToStation().getCode().trim());

						routeDTO.setFromStation(fromStationDTO);
						routeDTO.setToStation(toStationDTO);
						routeList.add(routeDTO);
					}
				}
			}
			scheduleTicketTransferTermsDTO.setRouteList(routeList);

			List<GroupDTO> groupList = new ArrayList<GroupDTO>();
			if (scheduleTicketTransferTerms.getGroupList() != null) {
				for (GroupIO GroupIO : scheduleTicketTransferTerms.getGroupList()) {
					GroupDTO group = new GroupDTO();
					group.setCode(GroupIO.getCode());
					group.setName(GroupIO.getName());
					groupList.add(group);
				}
			}
			scheduleTicketTransferTermsDTO.setGroupList(groupList);

			List<GroupDTO> bookedGroupList = new ArrayList<GroupDTO>();
			if (scheduleTicketTransferTerms.getBookedUserGroups() != null) {
				for (GroupIO GroupIO : scheduleTicketTransferTerms.getBookedUserGroups()) {
					GroupDTO group = new GroupDTO();
					group.setCode(GroupIO.getCode());
					group.setName(GroupIO.getName());
					bookedGroupList.add(group);
				}
			}
			scheduleTicketTransferTermsDTO.setBookedUserGroups(bookedGroupList);

			scheduleTicketTransferTermsDTO.setActiveFrom(new DateTime(scheduleTicketTransferTerms.getActiveFrom()));
			scheduleTicketTransferTermsDTO.setActiveTo(new DateTime(scheduleTicketTransferTerms.getActiveTo()));
			scheduleTicketTransferTermsDTO.setActiveFlag(scheduleTicketTransferTerms.getActiveFlag());

			scheduleTicketTransferTermsService.updateScheduleTicketTransferTerms(authDTO, scheduleTicketTransferTermsDTO);
			ticketTransferTerms.setCode(scheduleTicketTransferTermsDTO.getCode());
			ticketTransferTerms.setActiveFlag(scheduleTicketTransferTermsDTO.getActiveFlag());
		}
		return ResponseIO.success(ticketTransferTerms);
	}
}
