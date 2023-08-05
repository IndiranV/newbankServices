package org.in.com.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.in.com.cache.CacheCentral;
import org.in.com.constants.Text;
import org.in.com.dao.TicketDAO;
import org.in.com.dao.TicketTransactionDAO;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.DBQueryParamDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.ReportQueryDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatVisibilityDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.service.BusService;
import org.in.com.service.GroupService;
import org.in.com.service.NotificationService;
import org.in.com.service.OrganizationService;
import org.in.com.service.ReportQueryService;
import org.in.com.service.ScheduleSeatVisibilityService;
import org.in.com.service.ScheduleService;
import org.in.com.service.SearchService;
import org.in.com.service.SeatVisibilityReportService;
import org.in.com.service.StationPointService;
import org.in.com.service.StationService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class SeatVisibilityReportServiceImpl extends CacheCentral implements SeatVisibilityReportService {
	@Autowired
	SearchService searchService;
	@Autowired
	TripService tripService;
	@Autowired
	ScheduleSeatVisibilityService visibilityService;
	@Autowired
	ReportQueryService reportQueryService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	GroupService groupService;
	@Autowired
	BusService busService;
	@Autowired
	OrganizationService organizationService;
	@Autowired
	StationService stationService;
	@Autowired
	UserService userService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	StationPointService stationPointService;

	public void sendOverallOccupancySummarySMS(AuthDTO authDTO) {
		try {
			Map<String, Object> dataModel = Maps.newHashMap();
			dataModel.put("domainUrl", authDTO.getNamespace().getProfile().getDomainURL());
			dataModel.put("time", DateUtil.parseDateFormat(DateUtil.NOW().format("YYYY-MM-DD hh:mm:ss"), "yyyy-MM-dd hh:mm:ss", "MMM dd E hh:mm a"));

			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(DateUtil.NOW());
			// get all trips by trip date
			List<TripDTO> list = searchService.getAllTrips(authDTO, searchDTO);

			int totalAllocatedSeatCount = 0;
			int totalBlockedSeatCount = 0;
			for (TripDTO trip : list) {
				ScheduleDTO scheduleDTO = trip.getSchedule();

				// schedule seat visibility by trip
				List<ScheduleSeatVisibilityDTO> seatVisibilityList = visibilityService.getByScheduleId(authDTO, scheduleDTO);
				if (seatVisibilityList.isEmpty()) {
					continue;
				}

				for (Iterator<ScheduleSeatVisibilityDTO> itrvisibility = seatVisibilityList.iterator(); itrvisibility.hasNext();) {
					ScheduleSeatVisibilityDTO seatVisibility = itrvisibility.next();

					// remove expired visibility
					if (seatVisibility.getActiveFrom() != null && !searchDTO.getTravelDate().gteq(new DateTime(seatVisibility.getActiveFrom()))) {
						itrvisibility.remove();
						continue;
					}
					if (seatVisibility.getActiveTo() != null && !searchDTO.getTravelDate().lteq(new DateTime(seatVisibility.getActiveTo()))) {
						itrvisibility.remove();
						continue;
					}
					if (seatVisibility.getDayOfWeek() != null && seatVisibility.getDayOfWeek().length() != 7) {
						itrvisibility.remove();
						continue;
					}

					// convert visibility by seat code
					for (BusSeatLayoutDTO seatLayoutDTO : seatVisibility.getBus().getBusSeatLayoutDTO().getList()) {
						if (StringUtil.isNotNull(seatLayoutDTO.getCode()) && seatVisibility.getVisibilityType().equals("ACAT")) {
							totalAllocatedSeatCount = totalAllocatedSeatCount + 1;
						}
						else if (StringUtil.isNotNull(seatLayoutDTO.getCode()) && seatVisibility.getVisibilityType().equals("HIDE")) {
							totalBlockedSeatCount = totalBlockedSeatCount + 1;
						}
					}
				}
			}

			// Booking and Cancellation
			List<DBQueryParamDTO> paramList = new ArrayList<DBQueryParamDTO>();
			DBQueryParamDTO namespaceParam = new DBQueryParamDTO();
			namespaceParam.setParamName("namespaceId");
			namespaceParam.setValue(String.valueOf(authDTO.getNamespace().getId()));
			paramList.add(namespaceParam);

			DBQueryParamDTO fromDateParam = new DBQueryParamDTO();
			fromDateParam.setParamName("fromTripDate");
			fromDateParam.setValue(searchDTO.getTravelDate().format(Text.DATE_DATE4J));
			paramList.add(fromDateParam);

			DBQueryParamDTO toDateParam = new DBQueryParamDTO();
			toDateParam.setParamName("toTripDate");
			toDateParam.setValue(searchDTO.getTravelDate().format(Text.DATE_DATE4J));
			paramList.add(toDateParam);

			ReportQueryDTO reportQueryDTO = new ReportQueryDTO();
			reportQueryDTO.setQuery("CALL EZEE_SP_RPT_DASHBOARD_STATISTIC(:namespaceId, :fromTripDate, :toTripDate)");
			List<Map<String, ?>> listMapData = reportQueryService.getQueryResultsMap(authDTO, reportQueryDTO, paramList);

			Map<String, Map<String, String>> summaryMap = new HashMap<String, Map<String, String>>();
			for (Map<String, ?> map : listMapData) {
				if (summaryMap.get(String.valueOf(map.get("trip_code"))) != null) {
					Map<String, String> entityMap = summaryMap.get(String.valueOf(map.get("trip_code")));
					entityMap.put(String.valueOf(map.get("attribute_type")), String.valueOf(map.get("attribute_value")));
					summaryMap.put(String.valueOf(map.get("trip_code")), entityMap);
				}
				else {
					Map<String, String> entityMap = new HashMap<String, String>();
					entityMap.put(String.valueOf(map.get("attribute_type")), String.valueOf(map.get("attribute_value")));
					summaryMap.put(String.valueOf(map.get("trip_code")), entityMap);
				}
			}

			int totalTripCount = 0;
			int totalTripSeatCount = 0;
			int bookedCount = 0;
			int phoneBookedCount = 0;
			int cancelledCount = 0;
			BigDecimal bookedAmount = BigDecimal.ZERO;
			BigDecimal phoneBookedAmount = BigDecimal.ZERO;
			for (Entry<String, Map<String, String>> entry : summaryMap.entrySet()) {
				Map<String, String> salesInfo = entry.getValue();
				List<String> groupList = Arrays.asList(String.valueOf(salesInfo.get("GR_LIST")).split(Text.COMMA));
				for (String group : groupList) {
					int tripSeatCount = Integer.valueOf(String.valueOf(salesInfo.get("TRIP_ST_CNT")));
					group = group.replaceAll(Text.SINGLE_SPACE, Text.EMPTY);

					if (salesInfo.get("BO_COUNT_" + group) != null) {
						bookedCount = bookedCount + Integer.valueOf(String.valueOf(salesInfo.get("BO_COUNT_" + group)));
					}
					if (salesInfo.get("PH_COUNT_" + group) != null) {
						phoneBookedCount = phoneBookedCount + Integer.valueOf(String.valueOf(salesInfo.get("PH_COUNT_" + group)));
					}
					if (salesInfo.get("CA_COUNT_" + group) != null) {
						cancelledCount = cancelledCount + Integer.valueOf(String.valueOf(salesInfo.get("CA_COUNT_" + group)));
					}

					if (salesInfo.get("BO_AMT_" + group) != null) {
						BigDecimal seatAmount = new BigDecimal(String.valueOf(salesInfo.get("BO_AMT_" + group)));
						bookedAmount = bookedAmount.add(seatAmount);
					}
					if (salesInfo.get("PH_BO_AMT_" + group) != null) {
						BigDecimal subTotal = new BigDecimal(String.valueOf(salesInfo.get("PH_BO_AMT_" + group)));
						phoneBookedAmount = phoneBookedAmount.add(subTotal);
					}

					totalTripSeatCount = totalTripSeatCount + tripSeatCount;
					totalTripCount = totalTripCount + 1;
				}
			}

			double totalBookPercentage = (Float.valueOf(bookedCount + phoneBookedCount) * 100) / totalTripSeatCount;
			totalBookPercentage = Math.round(totalBookPercentage);
			dataModel.put("header", totalTripCount + " Trips, " + (bookedCount + phoneBookedCount) + "/" + totalTripSeatCount + Text.SINGLE_SPACE + totalBookPercentage + "%");

			List<String> contentList = new ArrayList<String>();
			if (bookedCount > 0) {
				String amount = Text.DOUBLE_QUOTE;
				double bookPercentage = (Float.valueOf(bookedCount) * 100) / (bookedCount + phoneBookedCount);
				bookPercentage = Math.round(bookPercentage * 100.0) / 100.0;
				if (bookedAmount.compareTo(new BigDecimal("100000")) != -1) {
					bookedAmount = bookedAmount.divide(new BigDecimal(100000)).setScale(2, RoundingMode.HALF_UP);
					amount = bookedAmount + Text.L_UPPER;
				}
				else if (bookedAmount.compareTo(new BigDecimal("1000")) != -1) {
					bookedAmount = bookedAmount.divide(new BigDecimal(1000)).setScale(2, RoundingMode.HALF_UP);
					amount = bookedAmount + Text.K_UPPER;
				}
				contentList.add(bookedCount + " BO " + bookPercentage + "%" + Text.SINGLE_SPACE + amount);
			}
			if (phoneBookedCount > 0) {
				String amount = Text.DOUBLE_QUOTE;
				double phoneBookPercentage = (Float.valueOf(phoneBookedCount) * 100) / (bookedCount + phoneBookedCount);
				phoneBookPercentage = Math.round(phoneBookPercentage * 100.0) / 100.0;
				if (phoneBookedAmount.compareTo(new BigDecimal("100000")) != -1) {
					phoneBookedAmount = phoneBookedAmount.divide(new BigDecimal(100000)).setScale(2, RoundingMode.HALF_UP);
					amount = phoneBookedAmount + Text.L_UPPER;
				}
				else if (phoneBookedAmount.compareTo(new BigDecimal("1000")) != -1) {
					phoneBookedAmount = phoneBookedAmount.divide(new BigDecimal(1000)).setScale(2, RoundingMode.HALF_UP);
					amount = phoneBookedAmount + Text.K_UPPER;
				}

				contentList.add(phoneBookedCount + " PB " + phoneBookPercentage + "% " + phoneBookedAmount + Text.SINGLE_SPACE + amount);
			}
			if (totalAllocatedSeatCount > 0) {
				double allocatePercentage = (Float.valueOf(totalAllocatedSeatCount) * 100) / (totalAllocatedSeatCount + totalBlockedSeatCount);
				allocatePercentage = Math.round(allocatePercentage * 100.0) / 100.0;

				contentList.add(totalAllocatedSeatCount + " AL " + allocatePercentage + "%");
			}
			if (totalBlockedSeatCount > 0) {
				double blockPercentage = (Float.valueOf(totalBlockedSeatCount) * 100) / (totalAllocatedSeatCount + totalBlockedSeatCount);
				blockPercentage = Math.round(blockPercentage * 100.0) / 100.0;

				contentList.add(totalBlockedSeatCount + " BL " + blockPercentage + "%");
			}

			// Vacant
			int vacantCount = totalTripSeatCount - (bookedCount + phoneBookedCount);
			double vacantPercentage = (vacantCount * 100) / totalTripSeatCount;
			vacantPercentage = Math.round(vacantPercentage * 100.0) / 100.0;
			vacantCount = Math.round(vacantCount * 100) / 100;

			contentList.add(vacantCount + " VC " + vacantPercentage + "%");

			dataModel.put("content", contentList);

			notificationService.sendOverallTripSummarySMS(authDTO, dataModel);
		}
		catch (Exception e) {
			System.out.println(authDTO.getNamespace().getCode() + " - " + e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	public List<Map<String, String>> getAllScheduleVisibility(AuthDTO authDTO, SearchDTO searchDTO) {
		List<Map<String, String>> result = new ArrayList<>();
		try {
			// get all trips by trip date
			List<TripDTO> list = searchService.getAllTrips(authDTO, searchDTO);

			for (TripDTO trip : list) {
				ScheduleDTO scheduleDTO = trip.getSchedule();

				// schedule seat visibility by trip
				List<ScheduleSeatVisibilityDTO> seatVisibilityList = visibilityService.getSeatVisibilities(authDTO, scheduleDTO);
				if (seatVisibilityList.isEmpty()) {
					continue;
				}

				for (ScheduleSeatVisibilityDTO seatVisibility : seatVisibilityList) {
					StringBuilder group = new StringBuilder();
					if (seatVisibility.getRefferenceType().equals("GR") && seatVisibility.getGroupList() != null) {
						for (GroupDTO groupDTO : seatVisibility.getGroupList()) {
							groupService.getGroup(authDTO, groupDTO);
							if (group.length() > 0) {
								group.append(Text.COMMA + Text.SINGLE_SPACE);
							}
							group.append(groupDTO.getName());
						}
					}

					StringBuilder user = new StringBuilder();
					if (seatVisibility.getRefferenceType().equals("UR") && seatVisibility.getUserList() != null) {
						for (UserDTO userDTO : seatVisibility.getUserList()) {
							getUserDTOById(authDTO, userDTO);
							if (user.length() > 0) {
								user.append(Text.COMMA + Text.SINGLE_SPACE);
							}
							user.append(userDTO.getName());
						}
					}

					StringBuilder route = new StringBuilder();
					StringBuilder routeUsers = new StringBuilder();
					if (seatVisibility.getRefferenceType().equals("SG") && seatVisibility.getRouteList() != null) {
						for (RouteDTO routeDTO : seatVisibility.getRouteList()) {
							routeDTO.setFromStation(getStationDTObyId(routeDTO.getFromStation()));
							routeDTO.setToStation(getStationDTObyId(routeDTO.getToStation()));
							if (route.length() > 0) {
								route.append(Text.COMMA + Text.SINGLE_SPACE);
							}
							route.append(routeDTO.getFromStation().getName()).append(Text.HYPHEN).append(routeDTO.getToStation().getName());
						}

						if (seatVisibility.getRouteUsers() != null) {
							for (UserDTO userDTO2 : seatVisibility.getRouteUsers()) {
								getUserDTO(authDTO, userDTO2);
								if (user.length() > 0) {
									user.append(", ");
								}
								routeUsers.append(userDTO2.getName());
							}
						}
					}

					StringBuilder organization = new StringBuilder();
					if (seatVisibility.getRefferenceType().equals("BR") && seatVisibility.getOrganizations() != null) {
						for (OrganizationDTO organizationDTO : seatVisibility.getOrganizations()) {
							OrganizationDTO organizationDTO2 = organizationService.getOrganization(authDTO, organizationDTO);
							if (organizationDTO2 == null) {
								continue;
							}
							if (organization.length() > 0) {
								organization.append(", ");
							}
							organization.append(organizationDTO2.getName());
						}
					}

					BusDTO busDTO = new BusDTO();
					StringBuilder seatName = new StringBuilder();
					if (seatVisibility.getBus() != null && seatVisibility.getBus().getId() != 0) {
						busDTO.setId(seatVisibility.getBus().getId());
						busDTO = busService.getBus(authDTO, busDTO);

						for (BusSeatLayoutDTO fitterdto : seatVisibility.getBus().getBusSeatLayoutDTO().getList()) {
							for (BusSeatLayoutDTO dto : busDTO.getBusSeatLayoutDTO().getList()) {
								if (dto.getCode().equals(fitterdto.getCode())) {
									if (seatName.length() > 0) {
										seatName.append(Text.COMMA + Text.SINGLE_SPACE);
									}
									seatName.append(dto.getName());
								}
							}
						}
					}

					Map<String, String> seatVisibilityMap = new HashMap<>();
					seatVisibilityMap.put("code", seatVisibility.getCode());
					seatVisibilityMap.put("seatNames", seatName.toString());
					seatVisibilityMap.put("userNames", user.toString());
					seatVisibilityMap.put("groupNames", group.toString());
					seatVisibilityMap.put("branchNames", organization.toString());
					seatVisibilityMap.put("routes", route.toString());
					seatVisibilityMap.put("routeUsers", routeUsers.toString());
					seatVisibilityMap.put("busType", busService.getBusCategoryByCode(busDTO.getCategoryCode()));
					seatVisibilityMap.put("tripCode", trip.getCode());
					seatVisibilityMap.put("serviceNumber", scheduleDTO.getServiceNumber());
					seatVisibilityMap.put("scheduleName", scheduleDTO.getName());
					seatVisibilityMap.put("visibilityType", seatVisibility.getVisibilityType());

					if ("EXPN".equals(seatVisibility.getVisibilityType())) {
						ScheduleSeatVisibilityDTO scheduleSeatVisibilityDTO = seatVisibility.getOverrideList().get(0);
						seatVisibilityMap.put("roleType", scheduleSeatVisibilityDTO.getRefferenceType());
						seatVisibilityMap.put("updatedBy", scheduleSeatVisibilityDTO.getUpdatedBy());
						seatVisibilityMap.put("updatedAt", DateUtil.convertDateTime(DateUtil.getDateTime(scheduleSeatVisibilityDTO.getUpdatedAt())));
						seatVisibilityMap.put("remarks", scheduleSeatVisibilityDTO.getRemarks());
					}
					else {
						seatVisibilityMap.put("roleType", seatVisibility.getRefferenceType());
						seatVisibilityMap.put("updatedBy", seatVisibility.getUpdatedBy());
						seatVisibilityMap.put("updatedAt", DateUtil.convertDateTime(DateUtil.getDateTime(seatVisibility.getUpdatedAt())));
						seatVisibilityMap.put("remarks", seatVisibility.getRemarks());
					}
					result.add(seatVisibilityMap);
				}
			}
		}
		catch (Exception e) {
			System.out.println(authDTO.getNamespace().getCode() + " - " + e.getMessage());
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public JSONObject getBranchSeatAllocationReport(AuthDTO authDTO, String tripDate, List<OrganizationDTO> organizationList, List<RouteDTO> routeList, List<ScheduleDTO> scheduleList, String userCodes) {
		List<UserDTO> userList = new ArrayList<>();
		List<UserDTO> orgUserList = new ArrayList<>();
		if (StringUtil.isNotNull(userCodes)) {
			for (String userCode : userCodes.split(Text.COMMA)) {
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(userCode);
				userDTO = userService.getUser(authDTO, userDTO);
				if (userDTO == null || userDTO.getId() == 0) {
					continue;
				}
				userList.add(userDTO);
			}
		}
		if (!organizationList.isEmpty()) {
			for (OrganizationDTO organizationDTO : organizationList) {
				organizationService.getOrganization(authDTO, organizationDTO);
				List<UserDTO> users = userService.get(authDTO, organizationDTO);
				orgUserList.addAll(users);
			}
		}
		for (RouteDTO routeDTO : routeList) {
			routeDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
			routeDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
		}
		for (ScheduleDTO scheduleDTO : scheduleList) {
			scheduleService.getSchedule(authDTO, scheduleDTO);
		}

		// get user booked tickets
		List<TicketDetailsDTO> ticketList = new ArrayList<>();
		if (DateUtil.getDayDifferent(DateUtil.getDateTime(tripDate).getStartOfDay(), DateUtil.NOW()) < 3) {
			TripDAO tripDAO = new TripDAO();
			ticketList = tripDAO.getBookedBlockedSeatsV2(authDTO, tripDate);
		}
		else {
			TicketDAO ticketDAO = new TicketDAO();
			List<TicketDTO> tickets = ticketDAO.getTicketsByTripDate(authDTO, tripDate);
			ticketList = getTicketDetails(authDTO, tickets);
		}

		Map<String, List<TicketDetailsDTO>> allocationMap = new HashMap<>();
		Map<String, List<TicketDetailsDTO>> ticketDetailsMap = new HashMap<>();
		Map<String, TicketDetailsDTO> seatWiseTicketMap = new HashMap<>();

		Map<Integer, List<TicketDetailsDTO>> scheduleWiseTicketMap = new HashMap<>();
		Map<Integer, List<ScheduleSeatVisibilityDTO>> scheduleSeatVisibilityMap = new HashMap<>();
		for (TicketDetailsDTO ticketDetailsDTO : ticketList) {
			boolean isRouteFound = routeList.stream().anyMatch(route -> route.getFromStation().getId() == ticketDetailsDTO.getFromStation().getId() && route.getToStation().getId() == ticketDetailsDTO.getToStation().getId());
			boolean isUserFound = userList.stream().anyMatch(user -> user.getId() == ticketDetailsDTO.getUser().getId());
			boolean isScheduleFound = scheduleList.stream().anyMatch(schedule -> schedule.getId() == ticketDetailsDTO.getScheduleId());
			// schedule filter validation
			if (!scheduleList.isEmpty() && !isScheduleFound) {
				continue;
			}
			// route, user filter validation
			if ((!routeList.isEmpty() && !isRouteFound) || (!userList.isEmpty() && !isUserFound)) {
				continue;
			}
			if (scheduleWiseTicketMap.isEmpty() || scheduleWiseTicketMap.get(ticketDetailsDTO.getScheduleId()) == null) {
				List<TicketDetailsDTO> ticketDetailsList = new ArrayList<>();
				ticketDetailsList.add(ticketDetailsDTO);
				scheduleWiseTicketMap.put(ticketDetailsDTO.getScheduleId(), ticketDetailsList);
			}
			else {
				List<TicketDetailsDTO> ticketDetailsList = scheduleWiseTicketMap.get(ticketDetailsDTO.getScheduleId());
				ticketDetailsList.add(ticketDetailsDTO);
				scheduleWiseTicketMap.put(ticketDetailsDTO.getScheduleId(), ticketDetailsList);
			}
			ticketDetailsDTO.setUser(userService.getUser(authDTO, ticketDetailsDTO.getUser()));
			if (ticketDetailsDTO.getUser() != null && ticketDetailsDTO.getUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
				if (organizationList.stream().anyMatch(org -> org.getId() == ticketDetailsDTO.getUser().getOrganization().getId())) {
					orgUserList.add(ticketDetailsDTO.getUser());
				}
			}
			boolean isUserExist = orgUserList.stream().anyMatch(user -> user.getId() == ticketDetailsDTO.getUser().getId());
			if (isUserExist) {
				String ticketDetailsKey = ticketDetailsDTO.getUser().getId() + Text.UNDER_SCORE + ticketDetailsDTO.getScheduleId() + Text.UNDER_SCORE + ticketDetailsDTO.getFromStation().getId() + Text.UNDER_SCORE + ticketDetailsDTO.getToStation().getId();
				if (ticketDetailsMap.isEmpty() || ticketDetailsMap.get(ticketDetailsKey) == null) {
					List<TicketDetailsDTO> ticketDetailsList = new ArrayList<>();
					ticketDetailsList.add(ticketDetailsDTO);
					ticketDetailsMap.put(ticketDetailsKey, ticketDetailsList);
				}
				else {
					List<TicketDetailsDTO> ticketDetailsList = ticketDetailsMap.get(ticketDetailsKey);
					ticketDetailsList.add(ticketDetailsDTO);
					ticketDetailsMap.put(ticketDetailsKey, ticketDetailsList);
				}
			}
			String scheduleSeatKey = ticketDetailsDTO.getSeatCode() + Text.UNDER_SCORE + ticketDetailsDTO.getScheduleId();
			seatWiseTicketMap.put(scheduleSeatKey, ticketDetailsDTO);
		}

		for (Entry<Integer, List<TicketDetailsDTO>> ticketDataMap : scheduleWiseTicketMap.entrySet()) {
			int scheduleId = ticketDataMap.getKey();

			ScheduleDTO scheduleDTO = new ScheduleDTO();
			scheduleDTO.setId(scheduleId);
			scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);
			scheduleDTO.setTripDate(DateUtil.getDateTime(tripDate));

			List<ScheduleSeatVisibilityDTO> seatVisibilityList = new ArrayList<>();
			if (scheduleSeatVisibilityMap.get(scheduleDTO.getId()) == null) {
				seatVisibilityList = visibilityService.getByScheduleId(authDTO, scheduleDTO);
				scheduleSeatVisibilityMap.put(scheduleDTO.getId(), seatVisibilityList);
			}
			else {
				seatVisibilityList.addAll(scheduleSeatVisibilityMap.get(scheduleDTO.getId()));
			}

			for (Iterator<ScheduleSeatVisibilityDTO> itrvisibility = seatVisibilityList.iterator(); itrvisibility.hasNext();) {
				ScheduleSeatVisibilityDTO seatVisibility = itrvisibility.next();
				if (!seatVisibility.getVisibilityType().equals("ACAT") || !seatVisibility.getRefferenceType().equals("BR")) {
					itrvisibility.remove();
					continue;
				}
				boolean isOrganizationFound = false;
				if (seatVisibility.getRefferenceType().equals("BR") && seatVisibility.getOrganizations() != null && !seatVisibility.getOrganizations().isEmpty()) {
					for (OrganizationDTO organizationDTO : organizationList) {
						isOrganizationFound = seatVisibility.getOrganizations().stream().anyMatch(organization -> organization.getId() == organizationDTO.getId());
						if (isOrganizationFound) {
							break;
						}
					}
				}
				if (!isOrganizationFound) {
					itrvisibility.remove();
					continue;
				}

				for (BusSeatLayoutDTO seatLayoutDTO : seatVisibility.getBus().getBusSeatLayoutDTO().getList()) {
					if (StringUtil.isNotNull(seatLayoutDTO.getCode())) {
						TicketDetailsDTO ticketDetailsDTO = seatWiseTicketMap.get(seatLayoutDTO.getCode() + Text.UNDER_SCORE + scheduleDTO.getId());
						if (ticketDetailsDTO != null) {
							if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
								continue;
							}
							if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId()) {
								continue;
							}
							// remove not travel status seat
							if (ticketDetailsDTO.getTicketExtra() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus().getId() == TravelStatusEM.NOT_TRAVELED.getId()) {
								continue;
							}

							ticketDetailsDTO.setUser(userService.getUser(authDTO, ticketDetailsDTO.getUser()));
							boolean isSameOrgUser = false;
							if (seatVisibility.getRefferenceType().equals("BR") && seatVisibility.getOrganizations() != null && (seatVisibility.getOrganizations().isEmpty() || (!seatVisibility.getOrganizations().isEmpty() && BitsUtil.isOrganizationExists(seatVisibility.getOrganizations(), ticketDetailsDTO.getUser().getOrganization()) != null))) {
								isSameOrgUser = true;
							}
							if (isSameOrgUser) {
								continue;
							}
							String referenceName = Text.NA;
							if (seatVisibility.getRefferenceType().equals("BR") && seatVisibility.getOrganizations() != null) {
								OrganizationDTO organization = Iterables.getFirst(seatVisibility.getOrganizations(), null);
								referenceName = organization != null ? organizationService.getOrganization(authDTO, organization).getName() : "All Branch";
							}

							ticketDetailsDTO.setCode(seatVisibility.getRefferenceType());
							ticketDetailsDTO.setName(referenceName);
							userService.getUser(authDTO, ticketDetailsDTO.getUser());

							String allocationMapKey = ticketDetailsDTO.getUser().getId() + Text.UNDER_SCORE + ticketDetailsDTO.getScheduleId() + Text.UNDER_SCORE + ticketDetailsDTO.getFromStation().getId() + Text.UNDER_SCORE + ticketDetailsDTO.getToStation().getId();
							if (allocationMap.isEmpty() || allocationMap.get(allocationMapKey) == null) {
								List<TicketDetailsDTO> ticketDetailList = new ArrayList<>();
								ticketDetailList.add(ticketDetailsDTO);
								allocationMap.put(allocationMapKey, ticketDetailList);
							}
							else {
								List<TicketDetailsDTO> ticketDetailList = allocationMap.get(allocationMapKey);
								ticketDetailList.add(ticketDetailsDTO);
								allocationMap.put(allocationMapKey, ticketDetailList);
							}
						}
					}
				}
			}
		}

		JSONObject json = new JSONObject();

		JSONArray bookingArray = new JSONArray();
		JSONArray seatAllocationArray = new JSONArray();

		Map<String, TicketDTO> ticketDataMap = new HashMap<>();
		TicketTransactionDAO ticketTransactionDAO = new TicketTransactionDAO();
		for (Map.Entry<String, List<TicketDetailsDTO>> ticketDetailsDataMap2 : ticketDetailsMap.entrySet()) {
			List<TicketDetailsDTO> ticketDetailsList = ticketDetailsDataMap2.getValue();

			UserDTO userDTO = null;
			StringBuilder seatNames = new StringBuilder();
			StationDTO fromStation = null;
			StationDTO toStation = null;
			ScheduleDTO schedule = null;

			BigDecimal ticketAmount = BigDecimal.ZERO;
			BigDecimal acBusTax = BigDecimal.ZERO;
			BigDecimal addonAmount = BigDecimal.ZERO;
			BigDecimal commissionAmount = BigDecimal.ZERO;

			for (TicketDetailsDTO ticketDetailsDTO : ticketDetailsList) {
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setCode(ticketDetailsDTO.getTicketCode());

				if (ticketDataMap.get(ticketDTO.getCode()) == null) {
					ticketDTO.setTicketXaction(new TicketTransactionDTO());
					ticketDTO.getTicketXaction().setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
					ticketTransactionDAO.getTicketTransactionByTicketCode(authDTO, ticketDTO);
					ticketDataMap.put(ticketDTO.getCode(), ticketDTO);
				}
				else {
					ticketDTO = ticketDataMap.get(ticketDTO.getCode());
				}

				if (userDTO == null) {
					userDTO = userService.getUser(authDTO, ticketDetailsDTO.getUser());
				}
				if (fromStation == null) {
					fromStation = stationService.getStation(ticketDetailsDTO.getFromStation());
				}
				if (toStation == null) {
					toStation = stationService.getStation(ticketDetailsDTO.getToStation());
				}
				if (schedule == null) {
					schedule = new ScheduleDTO();
					schedule.setId(ticketDetailsDTO.getScheduleId());
					schedule = scheduleService.getSchedule(authDTO, schedule);
				}

				if (seatNames.length() > 0) {
					seatNames.append(", ");
				}

				seatNames.append(ticketDetailsDTO.getSeatName());
				ticketAmount = ticketAmount.add(ticketDetailsDTO.getSeatFare());
				acBusTax = acBusTax.add(ticketDetailsDTO.getAcBusTax());

				if (ticketDTO.getTicketXaction() != null && ticketDTO.getTicketXaction().getTransSeatCount() != 0) {
					addonAmount = addonAmount.add(ticketDTO.getTicketXaction().getAddonsAmount().divide(new BigDecimal(ticketDTO.getTicketXaction().getTransSeatCount())));
					commissionAmount = commissionAmount.add(ticketDTO.getTicketXaction().getCommissionAmount().divide(new BigDecimal(ticketDTO.getTicketXaction().getTransSeatCount()), 2, RoundingMode.HALF_UP));
				}
			}

			JSONObject bookingJson = new JSONObject();
			bookingJson.put("user_name", userDTO.getName());
			bookingJson.put("seat", seatNames.toString());
			bookingJson.put("route", fromStation.getName() + " - " + toStation.getName());
			bookingJson.put("scheduleName", schedule != null ? schedule.getName() : Text.EMPTY);
			bookingJson.put("serviceNo", schedule != null ? StringUtil.isNull(schedule.getServiceNumber(), Text.EMPTY) : Text.EMPTY);
			bookingJson.put("ticket_amount", getActualSeatFare(ticketAmount, acBusTax, addonAmount));
			bookingJson.put("ac_bus_tax", acBusTax);
			bookingJson.put("addon_amount", addonAmount);
			bookingJson.put("commission_amount", commissionAmount);
			bookingArray.add(bookingJson);
		}

		for (Entry<String, List<TicketDetailsDTO>> allocationDataMap2 : allocationMap.entrySet()) {
			List<TicketDetailsDTO> ticketDetailsList = allocationDataMap2.getValue();

			String referenceName = null;
			String referenceType = null;
			StringBuilder seatNames = new StringBuilder();
			UserDTO userDTO = null;
			StationDTO fromStation = null;
			StationDTO toStation = null;
			ScheduleDTO schedule = null;
			BigDecimal ticketAmount = BigDecimal.ZERO;
			BigDecimal acBusTax = BigDecimal.ZERO;
			BigDecimal addonAmount = BigDecimal.ZERO;

			for (TicketDetailsDTO ticketDetailsDTO : ticketDetailsList) {
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setCode(ticketDetailsDTO.getTicketCode());

				if (ticketDataMap.get(ticketDTO.getCode()) == null) {
					ticketDTO.setTicketXaction(new TicketTransactionDTO());
					ticketDTO.getTicketXaction().setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
					ticketTransactionDAO.getTicketTransactionByTicketCode(authDTO, ticketDTO);
					ticketDataMap.put(ticketDTO.getCode(), ticketDTO);
				}
				else {
					ticketDTO = ticketDataMap.get(ticketDTO.getCode());
				}

				if (StringUtil.isNull(referenceName)) {
					referenceName = ticketDetailsDTO.getName();
				}
				if (StringUtil.isNull(referenceType)) {
					referenceType = ticketDetailsDTO.getCode();
				}
				if (userDTO == null) {
					userDTO = userService.getUser(authDTO, ticketDetailsDTO.getUser());
				}
				if (fromStation == null) {
					fromStation = stationService.getStation(ticketDetailsDTO.getFromStation());
				}
				if (toStation == null) {
					toStation = stationService.getStation(ticketDetailsDTO.getToStation());
				}
				if (schedule == null) {
					schedule = new ScheduleDTO();
					schedule.setId(ticketDetailsDTO.getScheduleId());
					schedule = scheduleService.getSchedule(authDTO, schedule);
				}
				if (seatNames.length() > 0) {
					seatNames.append(", ");
				}
				seatNames.append(ticketDetailsDTO.getSeatName());
				ticketAmount = ticketAmount.add(ticketDetailsDTO.getSeatFare());
				acBusTax = acBusTax.add(ticketDetailsDTO.getAcBusTax());

				if (ticketDTO.getTicketXaction() != null && ticketDTO.getTicketXaction().getTransSeatCount() != 0) {
					addonAmount = addonAmount.add(ticketDTO.getTicketXaction().getAddonsAmount().divide(new BigDecimal(ticketDTO.getTicketXaction().getTransSeatCount()), 2, RoundingMode.HALF_UP));
				}
			}

			JSONObject seatAllocationJson = new JSONObject();
			seatAllocationJson.put("reference_name", referenceName);
			seatAllocationJson.put("reference_type", referenceType);
			seatAllocationJson.put("seat", seatNames.toString());
			seatAllocationJson.put("route", fromStation.getName() + " - " + toStation.getName());
			seatAllocationJson.put("scheduleName", schedule != null ? schedule.getName() : Text.EMPTY);
			seatAllocationJson.put("serviceNo", schedule != null ? StringUtil.isNull(schedule.getServiceNumber(), Text.EMPTY) : Text.EMPTY);
			seatAllocationJson.put("booked_by", userDTO.getName());
			seatAllocationJson.put("ticket_amount", getActualSeatFare(ticketAmount, acBusTax, addonAmount));
			seatAllocationArray.add(seatAllocationJson);
		}

		json.put("bookings", bookingArray);
		json.put("allocations", seatAllocationArray);
		return json;
	}

	private BigDecimal getActualSeatFare(BigDecimal ticketAmount, BigDecimal acBusTax, BigDecimal addonAmount) {
		return ticketAmount.subtract(acBusTax).subtract(addonAmount);
	}

	private List<TicketDetailsDTO> getTicketDetails(AuthDTO authDTO, List<TicketDTO> tickets) {
		List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
		for (TicketDTO ticketDTO : tickets) {
			TicketDetailsDTO ticketDetailsDTO = ticketDTO.getTicketDetails().get(0);
			ticketDTO.setBoardingPoint(stationPointService.getStationPoint(authDTO, ticketDTO.getBoardingPoint()));
			ticketDTO.setDroppingPoint(stationPointService.getStationPoint(authDTO, ticketDTO.getDroppingPoint()));
			ticketDetailsDTO.setBoardingPointName(ticketDTO.getBoardingPoint().getName());
			ticketDetailsDTO.setStationPoint(BitsUtil.convertStationPoint(ticketDTO.getBoardingPoint(), ticketDTO.getDroppingPoint()));
			TicketExtraDTO ticketExtraDTO = new TicketExtraDTO();
			ticketExtraDTO.setTravelStatus(ticketDetailsDTO.getTravelStatus());
			ticketDetails.add(ticketDetailsDTO);
		}
		return ticketDetails;
	}
}
