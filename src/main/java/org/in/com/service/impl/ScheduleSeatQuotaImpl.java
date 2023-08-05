package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.cache.BusCache;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.ScheduleCache;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.GroupService;
import org.in.com.service.OrganizationService;
import org.in.com.service.ScheduleSeatQuotaService;
import org.in.com.service.ScheduleSeatVisibilityService;
import org.in.com.service.ScheduleService;
import org.in.com.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ScheduleSeatQuotaImpl extends CacheCentral implements ScheduleSeatQuotaService {
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	ScheduleSeatVisibilityService seatVisibilityService;
	@Autowired
	TripService tripService;
	@Autowired
	GroupService groupService;
	@Autowired
	OrganizationService organizationService;

	public List<ScheduleSeatVisibilityDTO> getTripSeatQuotaDetails(AuthDTO authDTO, TripDTO tripDTO) {
		tripService.getTripDTO(authDTO, tripDTO);
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());
		tripDTO.getSchedule().setTripDate(tripDTO.getTripDate());

		Map<Integer, Map<String, BusSeatLayoutDTO>> busmap = new HashMap<Integer, Map<String, BusSeatLayoutDTO>>();
		List<ScheduleSeatVisibilityDTO> list = seatVisibilityService.getByScheduleId(authDTO, tripDTO.getSchedule());
		for (ScheduleSeatVisibilityDTO visibilityDTO : list) {
			if (busmap.get(visibilityDTO.getBus().getId()) == null) {
				BusCache busCache = new BusCache();
				BusDTO busDTO = new BusDTO();
				busDTO.setId(visibilityDTO.getBus().getId());
				busmap.put(visibilityDTO.getBus().getId(), busCache.getBusDTObyId(authDTO, busDTO).getBusSeatLayoutMapFromList());
			}
			for (BusSeatLayoutDTO layoutDTO : visibilityDTO.getBus().getBusSeatLayoutDTO().getList()) {
				if (busmap.get(visibilityDTO.getBus().getId()).get(layoutDTO.getCode()) != null) {
					layoutDTO.setName(busmap.get(visibilityDTO.getBus().getId()).get(layoutDTO.getCode()).getName());
				}
			}

			if (visibilityDTO.getRefferenceType().equals("GR") && visibilityDTO.getGroupList() != null) {
				for (GroupDTO groupDTO : visibilityDTO.getGroupList()) {
					groupService.getGroup(authDTO, groupDTO);
				}
			}
			if (visibilityDTO.getRefferenceType().equals("UR") && visibilityDTO.getUserList() != null) {
				for (UserDTO userDTO : visibilityDTO.getUserList()) {
					getUserDTOById(authDTO, userDTO);
				}
			}
			if (visibilityDTO.getRefferenceType().equals("SG") && visibilityDTO.getRouteList() != null) {
				for (RouteDTO routeDTO : visibilityDTO.getRouteList()) {
					routeDTO.setFromStation(getStationDTObyId(routeDTO.getFromStation()));
					routeDTO.setToStation(getStationDTObyId(routeDTO.getToStation()));
				}
				for (UserDTO userDTO : visibilityDTO.getRouteUsers()) {
                    getUserDTOById(authDTO, userDTO);
                }
			}
			if (visibilityDTO.getRefferenceType().equals("BR") && visibilityDTO.getOrganizations() != null) {
                for (OrganizationDTO organizationDTO : visibilityDTO.getOrganizations()) {
                    organizationService.getOrganization(authDTO, organizationDTO);
                    organizationDTO.setStation(getStationDTO(organizationDTO.getStation()));
                }
            }
		}

		return list;
	}

	public ScheduleSeatVisibilityDTO addTripSeatQuotaDetails(AuthDTO authDTO, TripDTO tripDTO, ScheduleSeatVisibilityDTO seatVisibilityDTO) {
		tripService.getTripDTO(authDTO, tripDTO);
		BusCache busCache = new BusCache();
		tripDTO.setBus(busCache.getBusDTObyId(authDTO, tripDTO.getBus()));

		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());
		tripDTO.getSchedule().setTripDate(tripDTO.getTripDate());

		Map<String, BusSeatLayoutDTO> seatMap = seatVisibilityDTO.getBus().getBusSeatLayoutMapFromList();

		List<ScheduleSeatVisibilityDTO> list = seatVisibilityService.getByScheduleId(authDTO, tripDTO.getSchedule());
		List<ScheduleSeatVisibilityDTO> overrideList = new ArrayList<ScheduleSeatVisibilityDTO>();
		for (ScheduleSeatVisibilityDTO visibilityDTO : list) {
			if ("SDBL".equals(visibilityDTO.getVisibilityType())) {
				continue;
			}
			for (BusSeatLayoutDTO seatLayoutDTO : visibilityDTO.getBus().getBusSeatLayoutDTO().getList()) {
				if (seatMap.get(seatLayoutDTO.getCode()) != null) {
					ScheduleSeatVisibilityDTO orverrideVisibilityDTO = new ScheduleSeatVisibilityDTO();
					orverrideVisibilityDTO.setActiveFrom(tripDTO.getTripDate().format("YYYY-MM-DD"));
					orverrideVisibilityDTO.setActiveTo(tripDTO.getTripDate().format("YYYY-MM-DD"));
					orverrideVisibilityDTO.setDayOfWeek("1111111");
					orverrideVisibilityDTO.setLookupCode(visibilityDTO.getCode());
					orverrideVisibilityDTO.setBus(seatVisibilityDTO.getBus());
					orverrideVisibilityDTO.getBus().setCode(tripDTO.getBus().getCode());
					orverrideVisibilityDTO.setRemarks(seatVisibilityDTO.getRemarks());
					orverrideVisibilityDTO.setRefferenceType(seatVisibilityDTO.getRefferenceType());
					orverrideVisibilityDTO.setVisibilityType(visibilityDTO.getVisibilityType());
					orverrideVisibilityDTO.setSchedule(tripDTO.getSchedule());
					orverrideVisibilityDTO.setUserList(new ArrayList<UserDTO>());
					orverrideVisibilityDTO.setGroupList(new ArrayList<GroupDTO>());
					orverrideVisibilityDTO.setRouteList(new ArrayList<RouteDTO>());
					orverrideVisibilityDTO.setRouteUsers(seatVisibilityDTO.getRouteUsers());
					orverrideVisibilityDTO.setOrganizations(new ArrayList<OrganizationDTO>());
					orverrideVisibilityDTO.setActiveFlag(seatVisibilityDTO.getActiveFlag());
					overrideList.add(orverrideVisibilityDTO);
					break;
				}
			}
		}
		if (!overrideList.isEmpty()) {
			ScheduleSeatVisibilityDTO orverrideVisibilityDTO = new ScheduleSeatVisibilityDTO();
			orverrideVisibilityDTO.setList(overrideList);
			// Save Override List
			seatVisibilityService.Update(authDTO, orverrideVisibilityDTO);
		}
		Map<String, BusSeatLayoutDTO> map = tripDTO.getBus().getBusSeatLayoutMapFromList();
		if (seatVisibilityDTO.getVisibilityType().equals("ACAT") || seatVisibilityDTO.getVisibilityType().equals("HIDE")) {
			for (BusSeatLayoutDTO layoutDTO : seatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList()) {
				if (map.get(layoutDTO.getCode()) == null) {
					throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
				}
			}

			List<ScheduleSeatVisibilityDTO> addList = new ArrayList<ScheduleSeatVisibilityDTO>();
			seatVisibilityDTO.setActiveFrom(tripDTO.getTripDate().format("YYYY-MM-DD"));
			seatVisibilityDTO.setActiveTo(tripDTO.getTripDate().format("YYYY-MM-DD"));
			seatVisibilityDTO.setDayOfWeek("1111111");
			seatVisibilityDTO.setSchedule(tripDTO.getSchedule());
			seatVisibilityDTO.getBus().setCode(tripDTO.getBus().getCode());
			addList.add(seatVisibilityDTO);
			seatVisibilityDTO.setList(addList);
			seatVisibilityService.Update(authDTO, seatVisibilityDTO);

			saveTripHistory(authDTO, tripDTO, seatVisibilityDTO);
		}
		return seatVisibilityDTO;
	}

	private void saveTripHistory(AuthDTO authDTO, TripDTO tripDTO, ScheduleSeatVisibilityDTO seatVisibility) {
		StringBuilder activityLog = new StringBuilder();
		activityLog.append("Seat " + seatVisibility.getVisibilityType() + " to ");

		if (seatVisibility.getUserList() != null && !seatVisibility.getUserList().isEmpty()) {
			for (UserDTO user : seatVisibility.getUserList()) {
				UserDTO userDTO = getUserDTO(authDTO, user);
				activityLog.append(userDTO.getName());
				activityLog.append(Text.COMMA);
			}
			activityLog.append(Text.SINGLE_SPACE);
		}
		if (seatVisibility.getGroupList() != null && !seatVisibility.getGroupList().isEmpty()) {
			for (GroupDTO group : seatVisibility.getGroupList()) {
				GroupDTO groupDTO = groupService.getGroup(authDTO, group);
				activityLog.append(groupDTO.getName());
				activityLog.append(Text.COMMA);
			}
			activityLog.append(Text.SINGLE_SPACE);
		}
		if (seatVisibility.getRouteList() != null && !seatVisibility.getRouteList().isEmpty()) {
			activityLog.append("Route ");
			for (RouteDTO routeDTO : seatVisibility.getRouteList()) {
				StationDTO fromstationDTO = getStationDTO(routeDTO.getFromStation());
				StationDTO tostationDTO = getStationDTO(routeDTO.getToStation());
				activityLog.append(fromstationDTO.getName() + "-" + tostationDTO.getName());
				activityLog.append(Text.COMMA);
			}
			activityLog.append(Text.SINGLE_SPACE);
			
			if (seatVisibility.getRouteUsers() != null && !seatVisibility.getRouteUsers().isEmpty()) {
				activityLog.append("Route User ");
            	for (UserDTO user : seatVisibility.getRouteUsers()) {
                    UserDTO userDTO = getUserDTO(authDTO, user);
                    activityLog.append(userDTO.getName());
                    activityLog.append(",");
                }
	        }
		}
		if (seatVisibility.getOrganizations() != null && !seatVisibility.getOrganizations().isEmpty()) {
			for (OrganizationDTO organizationDTO : seatVisibility.getOrganizations()) {
                OrganizationDTO organization = organizationService.getOrganization(authDTO, organizationDTO);
                if (organization == null) {
                    continue;
                }
                activityLog.append(organization.getName());
                activityLog.append(",");
			 }
			 activityLog.append(" ");
	    }
		activityLog.append("by " + authDTO.getUser().getName() + " ");

		tripService.SaveTripHistory(authDTO, tripDTO, "Trip Seat Visibility Quota Update", activityLog.toString());
	}
}
