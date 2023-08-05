package org.in.com.controller.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusIO;
import org.in.com.controller.web.io.BusSeatLayoutIO;
import org.in.com.controller.web.io.BusSeatTypeFareIO;
import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.OrganizationIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.RouteIO;
import org.in.com.controller.web.io.ScheduleCategoryIO;
import org.in.com.controller.web.io.ScheduleFareAutoOverrideIO;
import org.in.com.controller.web.io.ScheduleFareTemplateIO;
import org.in.com.controller.web.io.ScheduleIO;
import org.in.com.controller.web.io.ScheduleSeatQuotaIO;
import org.in.com.controller.web.io.ScheduleSeatVisibilityIO;
import org.in.com.controller.web.io.StageFareIO;
import org.in.com.controller.web.io.StageIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.TripIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleFareTemplateDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.ScheduleTripStageFareDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.ScheduleSeatQuotaService;
import org.in.com.service.ScheduleTripFareService;
import org.in.com.service.ScheduleTripStageFareService;
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

import com.google.gson.Gson;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@RequestMapping("/{authtoken}/trip")
public class ScheduleTripQuotaFareController extends BaseController {

	@Autowired
	ScheduleSeatQuotaService seatQuotaService;
	@Autowired
	ScheduleTripFareService fareService;
	@Autowired
	ScheduleTripStageFareService tripStageFareService;

	@RequestMapping(value = "/quota/{tripCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ScheduleSeatQuotaIO>> getTripQuota(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		List<ScheduleSeatQuotaIO> quotaList = new ArrayList<ScheduleSeatQuotaIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);

			List<ScheduleSeatVisibilityDTO> list = seatQuotaService.getTripSeatQuotaDetails(authDTO, tripDTO);
			for (ScheduleSeatVisibilityDTO visibilityDTO : list) {
				for (BusSeatLayoutDTO layoutDTO : visibilityDTO.getBus().getBusSeatLayoutDTO().getList()) {
					ScheduleSeatQuotaIO quotaIO = new ScheduleSeatQuotaIO();
					quotaIO.setReleaseMinutes(visibilityDTO.getReleaseMinutes());
					quotaIO.setVisibilityType(visibilityDTO.getVisibilityType());
					quotaIO.setRefferenceType(visibilityDTO.getRefferenceType());
					quotaIO.setRemarks(visibilityDTO.getRemarks());

					List<GroupIO> groupList = new ArrayList<GroupIO>();
					if (visibilityDTO.getGroupList() != null) {
						for (GroupDTO groupDTO : visibilityDTO.getGroupList()) {
							GroupIO groupIO = new GroupIO();
							groupIO.setCode(groupDTO.getCode());
							groupIO.setName(groupDTO.getName());
							groupIO.setLevel(groupDTO.getLevel());
							quotaIO.setGroup(groupIO);
							groupList.add(groupIO);
						}
					}
					quotaIO.setGroupList(groupList);

					List<UserIO> userList = new ArrayList<UserIO>();
					if (visibilityDTO.getUserList() != null) {
						for (UserDTO userDTO : visibilityDTO.getUserList()) {
							UserIO userIO = new UserIO();
							userIO.setCode(userDTO.getCode());
							userIO.setName(userDTO.getName());
							quotaIO.setUser(userIO);
							userList.add(userIO);
						}
					}
					quotaIO.setUserList(userList);

					List<RouteIO> routeList = new ArrayList<RouteIO>();
					if (visibilityDTO.getRouteList() != null) {
						for (RouteDTO routeDTO : visibilityDTO.getRouteList()) {
							RouteIO route = new RouteIO();

							StationIO fromStation = new StationIO();
							fromStation.setCode(routeDTO.getFromStation().getCode());
							fromStation.setName(routeDTO.getFromStation().getName());
							route.setFromStation(fromStation);

							StationIO toStation = new StationIO();
							toStation.setCode(routeDTO.getToStation().getCode());
							toStation.setName(routeDTO.getToStation().getName());
							route.setToStation(toStation);

							quotaIO.setFromStation(fromStation);
							quotaIO.setToStation(toStation);

							routeList.add(route);
						}

						List<UserIO> routeUsers = new ArrayList<UserIO>();
						if (visibilityDTO.getRouteUsers() != null) {
							for (UserDTO userDTO2 : visibilityDTO.getRouteUsers()) {
								UserIO userIO2 = new UserIO();
								userIO2.setCode(userDTO2.getCode());
								userIO2.setName(userDTO2.getName());
								routeUsers.add(userIO2);
							}
						}
						quotaIO.setRouteUsers(routeUsers);
					}
					quotaIO.setRouteList(routeList);

					List<OrganizationIO> organizationList = new ArrayList<OrganizationIO>();
					if (visibilityDTO.getOrganizations() != null) {
						for (OrganizationDTO organizationDTO : visibilityDTO.getOrganizations()) {
							OrganizationIO organizationIO = new OrganizationIO();
							organizationIO.setCode(organizationDTO.getCode());
							organizationIO.setName(organizationDTO.getName());
							organizationIO.setShortCode(organizationDTO.getShortCode());

							StationIO orgStationIO = new StationIO();
							if (organizationDTO.getStation() != null) {
								orgStationIO.setCode(organizationDTO.getStation().getCode());
								orgStationIO.setName(organizationDTO.getStation().getName());
							}
							organizationIO.setStation(orgStationIO);
							organizationList.add(organizationIO);
						}
					}
					quotaIO.setOrganizations(organizationList);

					quotaIO.setSeatCode(layoutDTO.getCode());
					quotaIO.setSeatName(layoutDTO.getName());
					quotaList.add(quotaIO);
				}
			}

		}
		return ResponseIO.success(quotaList);
	}

	@RequestMapping(value = "/quota/{tripCode}/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ScheduleSeatVisibilityIO> addTripQuota(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @RequestBody ScheduleSeatVisibilityIO seatVisibility) throws Exception {

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			ScheduleSeatVisibilityDTO visibilityDTO = new ScheduleSeatVisibilityDTO();
			List<BusSeatLayoutDTO> layoutList = new ArrayList<>();
			if (seatVisibility.getBusSeatLayout() != null) {
				for (BusSeatLayoutIO layoutIO : seatVisibility.getBusSeatLayout()) {
					BusSeatLayoutDTO layoutDTO = new BusSeatLayoutDTO();
					layoutDTO.setCode(layoutIO.getCode());
					layoutDTO.setName(layoutIO.getSeatName());
					layoutList.add(layoutDTO);
				}
			}
			visibilityDTO.setRefferenceType(seatVisibility.getRoleType());
			visibilityDTO.setReleaseMinutes(seatVisibility.getReleaseMinutes());
			visibilityDTO.setRemarks(seatVisibility.getRemarks());
			visibilityDTO.setVisibilityType(seatVisibility.getVisibilityType());

			// Stage based
			List<RouteDTO> routeList = new ArrayList<RouteDTO>();
			if (seatVisibility.getFromStation() != null && seatVisibility.getToStation() != null && StringUtil.isNotNull(seatVisibility.getFromStation().getCode()) && StringUtil.isNotNull(seatVisibility.getToStation().getCode())) {
				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setCode(seatVisibility.getFromStation().getCode());

				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setCode(seatVisibility.getToStation().getCode());

				RouteDTO existRoute = new RouteDTO();
				existRoute.setFromStation(fromStationDTO);
				existRoute.setToStation(toStationDTO);
				routeList.add(existRoute);
			}

			// Group
			List<GroupDTO> groupList = new ArrayList<GroupDTO>();
			if ("GR".equals(seatVisibility.getRoleType()) && seatVisibility.getGroup() != null) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setCode(seatVisibility.getGroup().getCode());
				groupList.add(groupDTO);
			}
			if (seatVisibility.getRoleType().equals("GR") && seatVisibility.getGroupList() != null) {
				for (GroupIO group : seatVisibility.getGroupList()) {
					if (StringUtil.isNull(group.getCode())) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(group.getCode());
					groupList.add(groupDTO);
				}
			}

			// User
			List<UserDTO> userList = new ArrayList<UserDTO>();
			if ("UR".equals(seatVisibility.getRoleType()) && seatVisibility.getUser() != null) {
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(seatVisibility.getUser().getCode());
				userList.add(userDTO);
			}
			if (seatVisibility.getRoleType().equals("UR") && seatVisibility.getUserList() != null) {
				for (UserIO user : seatVisibility.getUserList()) {
					if (StringUtil.isNull(user.getCode())) {
						continue;
					}
					UserDTO userDTO = new UserDTO();
					userDTO.setCode(user.getCode());
					userList.add(userDTO);
				}
			}

			visibilityDTO.setGroupList(groupList);
			visibilityDTO.setUserList(userList);

			if (seatVisibility.getRoleType().equals("SG") && seatVisibility.getRouteList() != null) {
				for (RouteIO route : seatVisibility.getRouteList()) {
					if (route.getFromStation() == null || route.getToStation() == null || StringUtil.isNull(seatVisibility.getFromStation().getCode()) || StringUtil.isNull(seatVisibility.getToStation().getCode())) {
						continue;
					}
					RouteDTO routeDTO = new RouteDTO();
					StationDTO fromStationDTO = new StationDTO();
					fromStationDTO.setCode(route.getFromStation().getCode());
					routeDTO.setFromStation(fromStationDTO);

					StationDTO toStationDTO = new StationDTO();
					toStationDTO.setCode(route.getToStation().getCode());
					routeDTO.setToStation(toStationDTO);

					routeList.add(routeDTO);
				}
			}
			visibilityDTO.setRouteList(routeList);

			List<UserDTO> routeUsers = new ArrayList<UserDTO>();
			if (seatVisibility.getRouteUsers() != null) {
				for (UserIO userIO : seatVisibility.getRouteUsers()) {
					if (StringUtil.isNull(userIO.getCode())) {
						continue;
					}
					UserDTO routeUser = new UserDTO();
					routeUser.setCode(userIO.getCode());
					routeUsers.add(routeUser);
				}
			}
			visibilityDTO.setRouteUsers(routeUsers);

			List<OrganizationDTO> organizationList = new ArrayList<OrganizationDTO>();
			if (seatVisibility.getRoleType().equals("BR") && seatVisibility.getOrganizations() != null) {
				for (OrganizationIO organizationIO : seatVisibility.getOrganizations()) {
					if (StringUtil.isNull(organizationIO.getCode())) {
						continue;
					}
					OrganizationDTO organizationDTO = new OrganizationDTO();
					organizationDTO.setCode(organizationIO.getCode());
					organizationList.add(organizationDTO);
				}
			}
			visibilityDTO.setOrganizations(organizationList);

			BusDTO busDTO = new BusDTO();
			BusSeatLayoutDTO busSeatLayoutDTO = new BusSeatLayoutDTO();
			busSeatLayoutDTO.setList(layoutList);
			busDTO.setBusSeatLayoutDTO(busSeatLayoutDTO);
			visibilityDTO.setBus(busDTO);

			visibilityDTO.setActiveFlag(seatVisibility.getActiveFlag());

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);

			// Validate Edit Permission
			checkVisibilityEditPermission(authDTO);

			seatQuotaService.addTripSeatQuotaDetails(authDTO, tripDTO, visibilityDTO);

			seatVisibility.setCode(visibilityDTO.getCode());
			seatVisibility.setActiveFlag(visibilityDTO.getActiveFlag());

		}
		return ResponseIO.success(seatVisibility);

	}

	private void checkVisibilityEditPermission(AuthDTO authDTO) {
		// Permission check
		List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
		eventList.add(MenuEventEM.SEAT_VISIBILITY_EDIT_RIGHTS_USER);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);

		Map<String, String> additionalAttribute = new HashMap<>();
		additionalAttribute.put(Text.SEAT_VISIBILITY_EDIT_RIGHTS, menuEventDTO != null ? String.valueOf(menuEventDTO.getEnabledFlag()) : Numeric.ZERO);
		authDTO.setAdditionalAttribute(additionalAttribute);
	}

	@RequestMapping(value = "/fare/{tripCode}", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<StageIO>> getTripFare(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		List<StageIO> fareList = new ArrayList<StageIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		List<StageDTO> stageList = fareService.getScheduleTripFare(authDTO, tripDTO);
		// Sorting Stage
		Collections.sort(stageList, new Comparator<StageDTO>() {
			@Override
			public int compare(StageDTO t1, StageDTO t2) {
				return new CompareToBuilder().append(t1.getFromStation().getMinitues(), t2.getFromStation().getMinitues()).append(t2.getToStation().getMinitues(), t1.getToStation().getMinitues()).toComparison();
			}
		});
		// custom Sorting in specific order
		final List<String> order = Arrays.asList("PB", "ST", "SS", "SL", "LSL", "SLSL", "USL", "SUSL");

		for (StageDTO stageDTO : stageList) {
			StageIO stageIO = new StageIO();
			StationIO fromStation = new StationIO();
			StationIO toStation = new StationIO();
			fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
			fromStation.setName(stageDTO.getFromStation().getStation().getName());
			toStation.setCode(stageDTO.getToStation().getStation().getCode());
			toStation.setName(stageDTO.getToStation().getStation().getName());
			stageIO.setStageSequence(stageDTO.getStageSequence());
			List<StageFareIO> stageFareList = new ArrayList<>();
			// custom Sorting
			Collections.sort(stageDTO.getStageFare(), new Comparator<StageFareDTO>() {
				@Override
				public int compare(StageFareDTO t1, StageFareDTO t2) {
					return new CompareToBuilder().append(order.indexOf(t1.getBusSeatType().getCode()), order.indexOf(t2.getBusSeatType().getCode())).toComparison();
				}
			});
			for (StageFareDTO fareDTO : stageDTO.getStageFare()) {
				StageFareIO stageFareIO = new StageFareIO();
				stageFareIO.setFare(fareDTO.getFare());
				stageFareIO.setMinFare(fareDTO.getMinFare());
				stageFareIO.setMaxFare(fareDTO.getMaxFare());
				stageFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
				stageFareIO.setSeatName(fareDTO.getBusSeatType().getName());
				if (fareDTO.getGroup() != null) {
					stageFareIO.setGroupName(fareDTO.getGroup().getName());
				}
				stageFareList.add(stageFareIO);
			}
			stageIO.setStageFare(stageFareList);
			stageIO.setCode(stageDTO.getCode());
			stageIO.setFromStation(fromStation);
			stageIO.setToStation(toStation);
			stageIO.setDistance(stageDTO.getDistance());
			fareList.add(stageIO);

		}
		return ResponseIO.success(fareList);
	}

	@RequestMapping(value = "/{tripCode}/fare/schedule/stagefare/sync", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> syncTripFareToScheduleStageFare(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		fareService.syncTripFareToScheduleStageFare(authDTO, tripDTO);
		return ResponseIO.success();

	}

	@RequestMapping(value = "/schedule/occupancy", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ScheduleIO>> getScheduleOccupancy(@PathVariable("authtoken") String authtoken) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<ScheduleIO> scheduleList = new ArrayList<ScheduleIO>();

		List<ScheduleDTO> scheList = fareService.getScheduleOccupancy(authDTO);
		for (ScheduleDTO scheduleDTO : scheList) {
			ScheduleIO schedule = new ScheduleIO();
			schedule.setCode(scheduleDTO.getCode());
			schedule.setName(scheduleDTO.getName());
			schedule.setServiceNumber(scheduleDTO.getServiceNumber());
			schedule.setActiveFrom(scheduleDTO.getActiveFrom());
			schedule.setActiveTo(scheduleDTO.getActiveTo());
			schedule.setDisplayName(scheduleDTO.getDisplayName());
			schedule.setDayOfWeek(scheduleDTO.getDayOfWeek());

			BusIO busIO = new BusIO();
			busIO.setCode(scheduleDTO.getScheduleBus().getBus().getCode());
			busIO.setCategoryCode(scheduleDTO.getScheduleBus().getBus().getCategoryCode());
			busIO.setDisplayName(scheduleDTO.getScheduleBus().getBus().getDisplayName());
			busIO.setName(scheduleDTO.getScheduleBus().getBus().getName());
			busIO.setSeatCount(scheduleDTO.getScheduleBus().getBus().getReservableLayoutSeatCount());
			schedule.setBus(busIO);

			if (scheduleDTO.getCategory() != null) {
				ScheduleCategoryIO categoryIO = new ScheduleCategoryIO();
				categoryIO.setCode(scheduleDTO.getCategory().getCode());
				categoryIO.setName(scheduleDTO.getCategory().getName());
				schedule.setCategory(categoryIO);
			}
			List<TripIO> tripList = new ArrayList<TripIO>();
			for (TripDTO tripDTO : scheduleDTO.getTripList()) {
				if (tripDTO.getActiveFlag() != 1) {
					continue;
				}
				TripIO tripIO = new TripIO();
				tripIO.setTripCode(tripDTO.getCode());
				tripIO.setTravelDate(tripDTO.getTripDate().format(Text.DATE_DATE4J));

				tripIO.setBookedSeatCount(tripDTO.getBookedSeatCount());
				tripList.add(tripIO);
			}
			schedule.setTripList(tripList);
			scheduleList.add(schedule);
		}
		return ResponseIO.success(scheduleList);
	}

	@RequestMapping(value = "/fare/stage/schedule", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TripIO>> getScheduleTripFare(@PathVariable("authtoken") String authtoken, String scheduleCode) throws Exception {
		List<TripIO> tripList = new ArrayList<TripIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			if (StringUtil.isNull(scheduleCode)) {
				throw new ServiceException(ErrorCode.REQURIED_SCHEDULE_DATA);
			}

			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleCode);
			List<String> tripDateList = DateUtil.getDateList(DateUtil.NOW(), 40);

			DateTime fromDate = DateUtil.NOW().getStartOfDay();
			DateTime toDate = DateUtil.addDaysToDate(DateUtil.NOW(), 40).getEndOfDay();

			List<TripDTO> list = fareService.getScheduleTripFareV2(authDTO, scheduleDTO, fromDate, toDate, tripDateList, true);

			for (TripDTO tripDTO : list) {
				TripIO tripIO = new TripIO();
				tripIO.setTripCode(tripDTO.getCode());
				tripIO.setTravelDate(tripDTO.getTripDate().format(Text.DATE_DATE4J));

				List<StageIO> fareList = new ArrayList<StageIO>();

				List<StageDTO> stageList = tripDTO.getStageList();

				// Sorting Stage
				Collections.sort(stageList, new Comparator<StageDTO>() {
					@Override
					public int compare(StageDTO t1, StageDTO t2) {
						return new CompareToBuilder().append(t1.getFromStation().getMinitues(), t2.getFromStation().getMinitues()).append(t2.getToStation().getMinitues(), t1.getToStation().getMinitues()).toComparison();
					}
				});
				for (StageDTO stageDTO : stageList) {
					StageIO stageIO = new StageIO();
					StationIO fromStation = new StationIO();
					StationIO toStation = new StationIO();
					fromStation.setCode(stageDTO.getFromStation().getStation().getCode());
					fromStation.setName(stageDTO.getFromStation().getStation().getName());
					toStation.setCode(stageDTO.getToStation().getStation().getCode());
					toStation.setName(stageDTO.getToStation().getStation().getName());
					stageIO.setStageSequence(stageDTO.getStageSequence());
					List<StageFareIO> stageFareList = new ArrayList<>();
					for (StageFareDTO fareDTO : stageDTO.getStageFare()) {
						StageFareIO stageFareIO = new StageFareIO();
						stageFareIO.setFare(fareDTO.getFare());
						stageFareIO.setSeatType(fareDTO.getBusSeatType().getCode());
						stageFareIO.setSeatName(fareDTO.getBusSeatType().getName());
						if (fareDTO.getGroup() != null) {
							stageFareIO.setGroupName(fareDTO.getGroup().getName());
						}
						stageFareList.add(stageFareIO);
					}
					stageIO.setStageFare(stageFareList);
					stageIO.setCode(stageDTO.getCode());
					stageIO.setFromStation(fromStation);
					stageIO.setToStation(toStation);
					fareList.add(stageIO);
				}
				tripIO.setStageList(fareList);
				tripIO.setBookedSeatCount(tripDTO.getBookedSeatCount());
				tripIO.setAvailableSeatCount(tripDTO.getBus().getReservableLayoutSeatCount() - tripDTO.getBookedSeatCount());
				tripList.add(tripIO);
			}
		}
		return ResponseIO.success(tripList);
	}

	@RequestMapping(value = "/fare/{tripCode}/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> addTripFare(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @RequestBody List<ScheduleFareAutoOverrideIO> fareList) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		List<ScheduleTripStageFareDTO> quickFares = new ArrayList<ScheduleTripStageFareDTO>();
		try {
			for (ScheduleFareAutoOverrideIO fareAutoOverride : fareList) {
				ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();

				if (fareAutoOverride.getFromStation() == null || StringUtil.isNull(fareAutoOverride.getFromStation().getCode()) || fareAutoOverride.getToStation() == null || StringUtil.isNull(fareAutoOverride.getToStation().getCode())) {
					throw new ServiceException(ErrorCode.ROUTE_NOT_FOUND);
				}
				if (fareAutoOverride.getBusSeatTypeFare() == null) {
					throw new ServiceException(ErrorCode.NOT_NULL_DATA_FOR_PERSITS, "Fare shouldn't be null");
				}
				RouteDTO routeDTO = new RouteDTO();

				StationDTO FromstationDTO = new StationDTO();
				FromstationDTO.setCode(fareAutoOverride.getFromStation().getCode().trim());
				routeDTO.setFromStation(FromstationDTO);

				StationDTO TostationDTO = new StationDTO();
				TostationDTO.setCode(fareAutoOverride.getToStation().getCode().trim());
				routeDTO.setToStation(TostationDTO);

				List<StageFareDTO> busSeatTypeFare = new ArrayList<StageFareDTO>();
				for (BusSeatTypeFareIO fare : fareAutoOverride.getBusSeatTypeFare()) {
					StageFareDTO seatTypeFare = new StageFareDTO();
					seatTypeFare.setFare(fare.getFare().setScale(0, BigDecimal.ROUND_DOWN));
					seatTypeFare.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(fare.getSeatType()));
					busSeatTypeFare.add(seatTypeFare);
				}
				routeDTO.setStageFare(busSeatTypeFare);
				quickFareOverrideDTO.setRoute(routeDTO);
				quickFareOverrideDTO.setActiveFlag(fareAutoOverride.getActiveFlag());

				quickFares.add(quickFareOverrideDTO);
			}

			fareService.addScheduleTripFare(authDTO, tripDTO, quickFares);
		}
		catch (ServiceException e) {
			Gson gson = new Gson();
			System.out.println("ER051 tripcode-" + e.getErrorCode() + " -- " + tripCode + " --- " + gson.toJson(quickFares) + " --- " + gson.toJson(fareList));
			throw e;
		}
		catch (Exception e) {
			Gson gson = new Gson();
			System.out.println("ER052 tripcode-" + tripCode + " -- " + gson.toJson(quickFares) + " --- " + gson.toJson(fareList));
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/fare/remove", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> addTripFare(@PathVariable("authtoken") String authtoken, @RequestBody List<ScheduleFareAutoOverrideIO> fareList) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		List<ScheduleTripStageFareDTO> quickFares = new ArrayList<ScheduleTripStageFareDTO>();
		try {
			for (ScheduleFareAutoOverrideIO scheduleFareAutoOverrideIO : fareList) {
				ScheduleTripStageFareDTO quickFareOverrideDTO = new ScheduleTripStageFareDTO();
				quickFareOverrideDTO.setCode(scheduleFareAutoOverrideIO.getCode());

				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(scheduleFareAutoOverrideIO.getSchedule().getCode());
				scheduleDTO.setTripDate(DateUtil.getDateTime(scheduleFareAutoOverrideIO.getActiveFrom()));
				quickFareOverrideDTO.setSchedule(scheduleDTO);

				quickFares.add(quickFareOverrideDTO);
			}

			tripStageFareService.updateQuickFareV2(authDTO, quickFares);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{tripCode}/fare/template/apply", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<Map<String, String>> applyTripFareTemplate(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @RequestBody ScheduleFareTemplateIO scheduleFareTemplate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);

		List<DateTime> tripDates = new ArrayList<>();
		if (scheduleFareTemplate.getTripDates() != null && !scheduleFareTemplate.getTripDates().isEmpty()) {
			for (String tripDate : scheduleFareTemplate.getTripDates()) {
				DateTime tripdate = DateUtil.getDateTime(tripDate);
				if (tripdate != null) {
					tripDates.add(tripdate);
				}
			}
		}
		else {
			if (StringUtil.isNull(scheduleFareTemplate.getFromDate()) || StringUtil.isNull(scheduleFareTemplate.getToDate()) || StringUtil.isNull(scheduleFareTemplate.getDayOfWeek())) {
				throw new ServiceException(ErrorCode.INVALID_DATE_RANGE);
			}
			tripDates = DateUtil.getDateListV3(DateUtil.getDateTime(scheduleFareTemplate.getFromDate()), DateUtil.getDateTime(scheduleFareTemplate.getToDate()), scheduleFareTemplate.getDayOfWeek());
		}

		if (tripDates.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_DATE);
		}

		ScheduleFareTemplateDTO fareTemplate = new ScheduleFareTemplateDTO();
		fareTemplate.setTripDates(tripDates);

		List<RouteDTO> routeList = new ArrayList<>();
		for (RouteIO routeIO : scheduleFareTemplate.getStageFare()) {
			RouteDTO routeDTO = new RouteDTO();
			StationDTO fromStation = new StationDTO();
			fromStation.setCode(routeIO.getFromStation().getCode());
			routeDTO.setFromStation(fromStation);

			StationDTO toStation = new StationDTO();
			toStation.setCode(routeIO.getToStation().getCode());
			routeDTO.setToStation(toStation);

			List<StageFareDTO> stageFareList = new ArrayList<StageFareDTO>();
			for (StageFareIO stageFareIO : routeIO.getStageFare()) {
				StageFareDTO stageFareDTO = new StageFareDTO();
				stageFareDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(stageFareIO.getSeatType()));
				stageFareDTO.setFare(stageFareIO.getFare());
				stageFareList.add(stageFareDTO);
			}
			routeDTO.setStageFare(stageFareList);
			routeList.add(routeDTO);
		}
		fareTemplate.setStageFare(routeList);

		Map<String, String> response = fareService.applyScheduleTripFareTemplate(authDTO, tripDTO, fareTemplate);
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/stage/fare", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<ScheduleFareAutoOverrideIO>> getScheduleTripStageFare(@PathVariable("authtoken") String authtoken, String scheduleCode, String fromDate, String toDate, String tripCode) throws Exception {
		List<ScheduleFareAutoOverrideIO> tripStageFareList = new ArrayList<ScheduleFareAutoOverrideIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if ((!DateUtil.isValidDate(fromDate) || !DateUtil.isValidDate(toDate)) && StringUtil.isNull(tripCode)) {
			throw new ServiceException(ErrorCode.INVALID_DATE_RANGE);
		}
		else if ((StringUtil.isNull(fromDate) || StringUtil.isNull(toDate)) && StringUtil.isNull(tripCode)) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}

		ScheduleDTO scheduleDTO = null;
		if (StringUtil.isNotNull(scheduleCode)) {
			scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleCode);
		}
		List<ScheduleTripStageFareDTO> fareList = tripStageFareService.getScheduleTripStageFareV2(authDTO, scheduleDTO, fromDate, toDate, tripCode);
		for (ScheduleTripStageFareDTO tripStageFareDTO : fareList) {
			ScheduleFareAutoOverrideIO fareAutoOverride = new ScheduleFareAutoOverrideIO();
			fareAutoOverride.setCode(tripStageFareDTO.getCode());
			fareAutoOverride.setActiveFrom(tripStageFareDTO.getTripDate());
			fareAutoOverride.setActiveFlag(tripStageFareDTO.getActiveFlag());

			ScheduleIO schedule = new ScheduleIO();
			schedule.setCode(tripStageFareDTO.getSchedule().getCode());
			schedule.setName(tripStageFareDTO.getSchedule().getName());
			schedule.setServiceNumber(tripStageFareDTO.getSchedule().getServiceNumber());
			fareAutoOverride.setSchedule(schedule);

			StationIO fromStationIO = new StationIO();
			fromStationIO.setCode(tripStageFareDTO.getRoute().getFromStation().getCode());
			fromStationIO.setName(tripStageFareDTO.getRoute().getFromStation().getName());
			fareAutoOverride.setFromStation(fromStationIO);

			StationIO toStationDTO = new StationIO();
			toStationDTO.setCode(tripStageFareDTO.getRoute().getToStation().getCode());
			toStationDTO.setName(tripStageFareDTO.getRoute().getToStation().getName());
			fareAutoOverride.setToStation(toStationDTO);

			List<BusSeatTypeFareIO> busSeatTypeFare = new ArrayList<BusSeatTypeFareIO>();
			for (StageFareDTO fare : tripStageFareDTO.getRoute().getStageFare()) {
				BusSeatTypeFareIO seatTypeFare = new BusSeatTypeFareIO();
				seatTypeFare.setFare(fare.getFare().setScale(0, BigDecimal.ROUND_DOWN));
				seatTypeFare.setSeatType(fare.getBusSeatType().getCode());
				busSeatTypeFare.add(seatTypeFare);
			}
			fareAutoOverride.setBusSeatTypeFare(busSeatTypeFare);

			UserIO updatedBy = new UserIO();
			if (tripStageFareDTO.getAudit().getUser() != null) {
				updatedBy.setCode(tripStageFareDTO.getAudit().getUser().getCode());
				updatedBy.setName(tripStageFareDTO.getAudit().getUser().getName());
			}
			fareAutoOverride.setUser(updatedBy);

			fareAutoOverride.setUpdateAt(tripStageFareDTO.getAudit().getUpdatedAt());
			tripStageFareList.add(fareAutoOverride);
		}
		return ResponseIO.success(tripStageFareList);
	}

	@RequestMapping(value = "/schedule/{scheduleCode}/stage/fare", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONArray> getScheduleTripStageFares(@PathVariable("authtoken") String authtoken, @PathVariable("scheduleCode") String scheduleCode, String fromDate, String toDate) throws Exception {
		JSONArray tripStageFares = new JSONArray();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setCode(scheduleCode);

		List<ScheduleTripStageFareDTO> fareList = tripStageFareService.getScheduleTripStageFares(authDTO, scheduleDTO, fromDate, toDate);

		for (ScheduleTripStageFareDTO tripStageFareDTO : fareList) {
			JSONObject stagefareJson = new JSONObject();
			stagefareJson.put("code", tripStageFareDTO.getCode());
			stagefareJson.put("tripDate", tripStageFareDTO.getTripDate());
			stagefareJson.put("activeFlag", tripStageFareDTO.getActiveFlag());

			JSONObject schedule = new JSONObject();
			schedule.put("code", scheduleDTO.getCode());
			schedule.put("name", scheduleDTO.getName());
			schedule.put("serviceNumber", scheduleDTO.getServiceNumber());
			stagefareJson.put("schedule", schedule);

			JSONObject fromStation = new JSONObject();
			fromStation.put("code", tripStageFareDTO.getRoute().getFromStation().getCode());
			fromStation.put("name", tripStageFareDTO.getRoute().getFromStation().getName());
			stagefareJson.put("fromStation", fromStation);

			JSONObject toStation = new JSONObject();
			toStation.put("code", tripStageFareDTO.getRoute().getToStation().getCode());
			toStation.put("name", tripStageFareDTO.getRoute().getToStation().getName());
			stagefareJson.put("toStation", toStation);

			JSONArray busSeatTypeFare = new JSONArray();
			for (StageFareDTO fare : tripStageFareDTO.getRoute().getStageFare()) {
				JSONObject seatTypeFare = new JSONObject();
				seatTypeFare.put("fare", fare.getFare().setScale(0, BigDecimal.ROUND_DOWN));
				seatTypeFare.put("seatType", fare.getBusSeatType().getCode());
				busSeatTypeFare.add(seatTypeFare);
			}
			stagefareJson.put("busSeatTypeFare", busSeatTypeFare);

			JSONObject updatedBy = new JSONObject();
			if (tripStageFareDTO.getAudit().getUser() != null) {
				updatedBy.put("code", tripStageFareDTO.getAudit().getUser().getCode());
				updatedBy.put("name", tripStageFareDTO.getAudit().getUser().getName());
			}
			stagefareJson.put("user", updatedBy);

			stagefareJson.put("updatedAt", tripStageFareDTO.getAudit().getUpdatedAt());
			tripStageFares.add(stagefareJson);
		}
		return ResponseIO.success(tripStageFares);
	}
	
	@RequestMapping(value = "/stage/fare/history", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONArray> getScheduleTripStageFareHistory(@PathVariable("authtoken") String authtoken, String scheduleCode, String fromDate, String toDate, String tripCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if ((!DateUtil.isValidDate(fromDate) || !DateUtil.isValidDate(toDate)) && StringUtil.isNull(tripCode)) {
			throw new ServiceException(ErrorCode.INVALID_DATE_RANGE);
		}
		else if ((StringUtil.isNull(fromDate) || StringUtil.isNull(toDate)) && StringUtil.isNull(tripCode)) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}

		ScheduleDTO scheduleDTO = null;
		if (StringUtil.isNotNull(scheduleCode)) {
			scheduleDTO = new ScheduleDTO();
			scheduleDTO.setCode(scheduleCode);
		}
		JSONArray jsonArrayData = tripStageFareService.getScheduleTripStageFareHistory(authDTO, scheduleDTO, fromDate, toDate, tripCode);
		return ResponseIO.success(jsonArrayData);
	}

	@RequestMapping(value = "/schedule/occupancy/analytics/report", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONArray> getScheduleOccupancyAnalytics(@PathVariable("authtoken") String authtoken, String fromDate, String toDate) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (StringUtil.isNull(fromDate) || StringUtil.isNull(toDate) || !DateUtil.isValidDateV2(fromDate) || !DateUtil.isValidDateV2(toDate)) {
			throw new ServiceException(ErrorCode.INVALID_DATE_RANGE);
		}
		JSONArray scheduleOccupancy = fareService.getScheduleOccupancyAnalytics(authDTO, DateUtil.getDateTime(fromDate), DateUtil.getDateTime(toDate));
		return ResponseIO.success(scheduleOccupancy);
	}
}
