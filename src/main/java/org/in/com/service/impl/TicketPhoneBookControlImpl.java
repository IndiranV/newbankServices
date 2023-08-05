package org.in.com.service.impl;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import net.sf.ehcache.Element;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.TicketPhoneBookControlCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.PhoneTicketControlDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CancellationPolicyDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.TicketPhoneBookCancelControlDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketPhoneBookControlDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.SlabCalenderModeEM;
import org.in.com.dto.enumeration.SlabCalenderTypeEM;
import org.in.com.dto.enumeration.SlabModeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.service.GroupService;
import org.in.com.service.StationService;
import org.in.com.service.TicketPhoneBookControlService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TicketPhoneBookControlImpl extends ScheduleCache implements TicketPhoneBookControlService {
	private final String CACHEKEY = "TCK_BOOK_LIMIT";

	@Autowired
	GroupService groupService;
	@Autowired
	StationService stationService;
	@Autowired
	UserService userService;
	@Autowired
	TripService tripService;

	public TicketPhoneBookControlDTO updatePhoneBookTimeControlIUD(AuthDTO authDTO, TicketPhoneBookControlDTO controlDTO) {
		if (StringUtil.isNotNull(controlDTO.getGroup().getCode())) {
			controlDTO.setGroup(groupService.getGroup(authDTO, controlDTO.getGroup()));
		}
		PhoneTicketControlDAO ticketControlDAO = new PhoneTicketControlDAO();
		return ticketControlDAO.getIUD(authDTO, controlDTO);
	}

	public List<TicketPhoneBookControlDTO> getPhoneBookTimeControl(AuthDTO authDTO) {
		PhoneTicketControlDAO ticketControlDAO = new PhoneTicketControlDAO();
		List<TicketPhoneBookControlDTO> list = ticketControlDAO.getAll(authDTO);
		for (TicketPhoneBookControlDTO controlDTO : list) {
			if (controlDTO.getGroup().getId() != 0) {
				controlDTO.setGroup(groupService.getGroup(authDTO, controlDTO.getGroup()));
			}
		}
		return list;

	}

	public TicketPhoneBookControlDTO getActiveTimeControl(AuthDTO authDTO, DateTime tripDate) {
		PhoneTicketControlDAO ticketControlDAO = new PhoneTicketControlDAO();
		List<TicketPhoneBookControlDTO> list = ticketControlDAO.getAll(authDTO);

		for (Iterator<TicketPhoneBookControlDTO> iterator = list.iterator(); iterator.hasNext();) {
			TicketPhoneBookControlDTO fareOverrideDTO = iterator.next();
			// common validations
			if (fareOverrideDTO.getActiveFrom() != null && !tripDate.gteq(new DateTime(fareOverrideDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getActiveTo() != null && !tripDate.lteq(new DateTime(fareOverrideDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getDayOfWeek() != null && fareOverrideDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getDayOfWeek() != null && fareOverrideDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			if (fareOverrideDTO.getGroup() != null && fareOverrideDTO.getGroup().getId() != 0 && fareOverrideDTO.getGroup().getId() != authDTO.getGroup().getId()) {
				iterator.remove();
				continue;
			}
			// Exceptions and Override
			for (Iterator<TicketPhoneBookControlDTO> overrideIterator = fareOverrideDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				TicketPhoneBookControlDTO overrideDTO = overrideIterator.next();
				if (!tripDate.gteq(new DateTime(overrideDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!tripDate.lteq(new DateTime(overrideDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideDTO.getDayOfWeek() != null && overrideDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideDTO.getDayOfWeek() != null && overrideDTO.getDayOfWeek().substring(tripDate.getWeekDay() - 1, tripDate.getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}

				// Apply Exceptions
				if (overrideDTO.getBlockMinutes() == -1 || overrideDTO.getAllowMinutes() == -1) {
					iterator.remove();
					break;
				}
				else {
					// Apply Override
					fareOverrideDTO.setAllowMinutes(overrideDTO.getAllowMinutes());
					fareOverrideDTO.setBlockMinutes(overrideDTO.getBlockMinutes());
					fareOverrideDTO.setBlockMinutesType(overrideDTO.getBlockMinutesType());
				}
			}
		}
		TicketPhoneBookControlDTO bookControlDTO = null;
		for (TicketPhoneBookControlDTO phoneBookControlDTO : list) {
			if (bookControlDTO == null) {
				bookControlDTO = phoneBookControlDTO;
			}
			if (phoneBookControlDTO.getUserDTO() != null && phoneBookControlDTO.getUserDTO().getId() != 0) {
				bookControlDTO = phoneBookControlDTO;
			}
		}

		return bookControlDTO;
	}

	public TicketPhoneBookControlDTO updateBookLimitControlIUD(AuthDTO authDTO, TicketPhoneBookControlDTO controlDTO) {
		if (controlDTO.getGroup() != null) {
			controlDTO.setGroup(groupService.getGroup(authDTO, controlDTO.getGroup()));
		}
		else if (controlDTO.getUserDTO() != null) {
			controlDTO.setUserDTO(getUserDTO(authDTO, controlDTO.getUserDTO()));
		}

		List<ScheduleDTO> scheduleList = new ArrayList<>();
		for (ScheduleDTO scheduleDTO : controlDTO.getScheduleList()) {
			getScheduleDTO(authDTO, scheduleDTO);
			if (scheduleDTO != null && scheduleDTO.getId() != 0 && scheduleDTO.getActiveFlag() == 1) {
				scheduleList.add(scheduleDTO);
			}
		}
		for (RouteDTO routeDTO : controlDTO.getRouteList()) {
			routeDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
			routeDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
		}
		PhoneTicketControlDAO ticketControlDAO = new PhoneTicketControlDAO();
		return ticketControlDAO.updateBookLimitControlIUD(authDTO, controlDTO);
	}

	public List<TicketPhoneBookControlDTO> getBookLimitsControl(AuthDTO authDTO) {
		PhoneTicketControlDAO ticketControlDAO = new PhoneTicketControlDAO();
		List<TicketPhoneBookControlDTO> list = ticketControlDAO.getBookLimitsControl(authDTO);
		for (TicketPhoneBookControlDTO controlDTO : list) {
			if (controlDTO.getGroup() != null && controlDTO.getGroup().getId() != 0) {
				controlDTO.setGroup(groupService.getGroup(authDTO, controlDTO.getGroup()));
			}
			else if (controlDTO.getUserDTO() != null && controlDTO.getUserDTO().getId() != 0) {
				controlDTO.setUserDTO(getUserDTOById(authDTO, controlDTO.getUserDTO()));
			}

			for (ScheduleDTO schedulDTO : controlDTO.getScheduleList()) {
				getScheduleDTObyId(authDTO, schedulDTO);
			}
			for (RouteDTO routeDTO : controlDTO.getRouteList()) {
				getStationDTObyId(routeDTO.getFromStation());
				getStationDTObyId(routeDTO.getToStation());
			}
		}
		return list;
	}

	public TicketPhoneBookControlDTO getActiveLimitControl(AuthDTO authDTO, ScheduleDTO scheduleDTO, TicketDTO ticketDTO, DateTime tripDate) {
		List<TicketPhoneBookControlDTO> ticketBookingLimits = null;
		TicketStatusEM ticketStatusEM = ticketDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() ? TicketStatusEM.CONFIRM_BOOKED_TICKETS : ticketDTO.getTicketStatus();
		
		String key = CACHEKEY + Text.UNDER_SCORE + authDTO.getNamespaceCode();
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<TicketPhoneBookControlCacheDTO> ticketBookControlCacheList = (List<TicketPhoneBookControlCacheDTO>) element.getObjectValue();
			ticketBookingLimits = bindTicketPhoneBookControlFromCache(ticketBookControlCacheList);
		}
		else {
			PhoneTicketControlDAO ticketControlDAO = new PhoneTicketControlDAO();
			ticketBookingLimits = ticketControlDAO.getBookLimitsControl(authDTO);
			bindTicketPhoneBookControlToCache(ticketBookingLimits);
		}

		for (Iterator<TicketPhoneBookControlDTO> iterator = ticketBookingLimits.iterator(); iterator.hasNext();) {
			TicketPhoneBookControlDTO bookControlDTO = iterator.next();
			// common validations
			DateTime txtrDate = bookControlDTO.getDateType().getId() == DateTypeEM.TRIP.getId() ? tripDate : DateUtil.NOW();
			if (bookControlDTO.getActiveFrom() != null && !txtrDate.gteq(new DateTime(bookControlDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (bookControlDTO.getActiveTo() != null && !txtrDate.lteq(new DateTime(bookControlDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (bookControlDTO.getDayOfWeek() != null && bookControlDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (bookControlDTO.getDayOfWeek() != null && bookControlDTO.getDayOfWeek().substring(txtrDate.getWeekDay() - 1, txtrDate.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && bookControlDTO.getGroup() != null && bookControlDTO.getGroup().getId() != 0 && bookControlDTO.getGroup().getId() != authDTO.getGroup().getId()) {
				iterator.remove();
				continue;
			}
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && bookControlDTO.getUserDTO() != null && bookControlDTO.getUserDTO().getId() != 0 && bookControlDTO.getUserDTO().getId() != authDTO.getUser().getId()) {
				iterator.remove();
				continue;
			}
			// Route List
			if (!bookControlDTO.getRouteList().isEmpty() && BitsUtil.isRouteExists(bookControlDTO.getRouteList(), ticketDTO.getFromStation(), ticketDTO.getToStation()) == null) {
				iterator.remove();
				continue;
			}
			// Schedule List
			if (!bookControlDTO.getScheduleList().isEmpty() && BitsUtil.isScheduleExists(bookControlDTO.getScheduleList(), scheduleDTO) == null) {
				iterator.remove();
				continue;
			}
			if (bookControlDTO.getTicketStatus().getId() != ticketStatusEM.getId()) {
				iterator.remove();
				continue;
			}
			// Exceptions and Override
			for (Iterator<TicketPhoneBookControlDTO> overrideIterator = bookControlDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				TicketPhoneBookControlDTO overrideDTO = overrideIterator.next();
				if (!txtrDate.gteq(new DateTime(overrideDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (!txtrDate.lteq(new DateTime(overrideDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideDTO.getDayOfWeek() != null && overrideDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideDTO.getDayOfWeek() != null && overrideDTO.getDayOfWeek().substring(txtrDate.getWeekDay() - 1, txtrDate.getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}
				if (overrideDTO.getTicketStatus().getId() != ticketStatusEM.getId()) {
					overrideIterator.remove();
					continue;
				}

				// Apply Exceptions
				if (overrideDTO.getMaxSlabValueLimit() == -1) {
					iterator.remove();
					break;
				}
				else {
					// Apply Override
					bookControlDTO.setSlabCalenderMode(overrideDTO.getSlabCalenderMode());
					bookControlDTO.setSlabCalenderType(overrideDTO.getSlabCalenderType());
					bookControlDTO.setSlabMode(overrideDTO.getSlabMode());
					bookControlDTO.setMaxSlabValueLimit(overrideDTO.getMaxSlabValueLimit());
				}
			}
		}

		// Sorting
		Collections.sort(ticketBookingLimits, new Comparator<TicketPhoneBookControlDTO>() {
			@Override
			public int compare(TicketPhoneBookControlDTO t1, TicketPhoneBookControlDTO t2) {
				return new CompareToBuilder().append(t2.getActiveFrom(), t1.getActiveFrom()).append(t2.getActiveTo(), t1.getActiveTo()).toComparison();
			}
		});

		// Identify specific recent control limit
		TicketPhoneBookControlDTO recentPhoneBookControlDTO = null;
		for (Iterator<TicketPhoneBookControlDTO> iterator = ticketBookingLimits.iterator(); iterator.hasNext();) {
			TicketPhoneBookControlDTO ticketPhoneBookControlDTO = iterator.next();
			if (recentPhoneBookControlDTO == null) {
				recentPhoneBookControlDTO = ticketPhoneBookControlDTO;
				continue;
			}
			if (DateUtil.getDayDifferent(new DateTime(ticketPhoneBookControlDTO.getActiveFrom()), new DateTime(ticketPhoneBookControlDTO.getActiveTo())) > DateUtil.getDayDifferent(new DateTime(recentPhoneBookControlDTO.getActiveFrom()), new DateTime(recentPhoneBookControlDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}

		}

		TicketPhoneBookControlDTO bookControlDTO = null;
		for (TicketPhoneBookControlDTO phoneBookControlDTO : ticketBookingLimits) {
			if (bookControlDTO == null) {
				bookControlDTO = getSlabDateRange(tripDate, phoneBookControlDTO);
			}
			if (phoneBookControlDTO.getUserDTO() != null && phoneBookControlDTO.getUserDTO().getId() != 0) {
				bookControlDTO = getSlabDateRange(tripDate, phoneBookControlDTO);
			}
		}
		return bookControlDTO;
	}

	// find slab wise date range
	private TicketPhoneBookControlDTO getSlabDateRange(DateTime tripDate, TicketPhoneBookControlDTO bookControlDTO) {
		DateTime fromDate = null, toDate = null;
		if (bookControlDTO.getSlabCalenderMode().getId() == SlabCalenderModeEM.FLEXI.getId()) {
			if (bookControlDTO.getSlabCalenderType().getId() == SlabCalenderTypeEM.DAY.getId()) {
				fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getStartOfDay();
				toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
			}
			else if (bookControlDTO.getSlabCalenderType().getId() == SlabCalenderTypeEM.WEEK.getId()) {
				fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 8).getStartOfDay();
				toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
			}
			else if (bookControlDTO.getSlabCalenderType().getId() == SlabCalenderTypeEM.MONTH.getId()) {
				fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 31).getStartOfDay();
				toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
			}
		}
		else if (bookControlDTO.getSlabCalenderMode().getId() == SlabCalenderModeEM.STRICT.getId()) {
			if (bookControlDTO.getSlabCalenderType().getId() == SlabCalenderTypeEM.DAY.getId()) {
				fromDate = tripDate.getStartOfDay();
				toDate = tripDate.getEndOfDay();
			}
			else if (bookControlDTO.getSlabCalenderType().getId() == SlabCalenderTypeEM.WEEK.getId()) {
				fromDate = DateUtil.getWeekStartDate(tripDate).getStartOfDay();
				toDate = DateUtil.getWeekEndDate(tripDate).getEndOfDay();
			}
			else if (bookControlDTO.getSlabCalenderType().getId() == SlabCalenderTypeEM.MONTH.getId()) {
				fromDate = tripDate.getStartOfMonth();
				toDate = fromDate.plusDays(tripDate.getNumDaysInMonth() - 1).getEndOfDay();
			}
		}
		bookControlDTO.setActiveFrom(fromDate.format("YYYY-MM-DD"));
		bookControlDTO.setActiveTo(toDate.format("YYYY-MM-DD"));
		return bookControlDTO;
	}

	private List<TicketPhoneBookControlDTO> bindTicketPhoneBookControlFromCache(List<TicketPhoneBookControlCacheDTO> phoneBookControlCacheList) {
		List<TicketPhoneBookControlDTO> ticketBookingLimts = new ArrayList<>();
		for (TicketPhoneBookControlCacheDTO bookControlCacheDTO : phoneBookControlCacheList) {
			TicketPhoneBookControlDTO phoneBookControlDTO = new TicketPhoneBookControlDTO();
			phoneBookControlDTO.setId(bookControlCacheDTO.getId());
			phoneBookControlDTO.setCode(bookControlCacheDTO.getCode());
			phoneBookControlDTO.setName(bookControlCacheDTO.getName());
			phoneBookControlDTO.setRefferenceType(bookControlCacheDTO.getRefferenceType());

			if (bookControlCacheDTO.getGroupId() != 0) {
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(bookControlCacheDTO.getGroupId());
				phoneBookControlDTO.setGroup(groupDTO);
			}
			else if (bookControlCacheDTO.getUserId() != 0) {
				UserDTO userDTO = new UserDTO();
				userDTO.setId(bookControlCacheDTO.getUserId());
				phoneBookControlDTO.setUserDTO(userDTO);
			}

			phoneBookControlDTO.setActiveFrom(bookControlCacheDTO.getActiveFrom());
			phoneBookControlDTO.setActiveTo(bookControlCacheDTO.getActiveTo());
			phoneBookControlDTO.setDayOfWeek(bookControlCacheDTO.getDayOfWeek());
			phoneBookControlDTO.setDateType(DateTypeEM.getDateTypeEM(bookControlCacheDTO.getDateType()));
			phoneBookControlDTO.setLookupCode(bookControlCacheDTO.getLookupCode());
			phoneBookControlDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(bookControlCacheDTO.getTicketStatusId()));

			List<ScheduleDTO> scheduleList = new ArrayList<>();
			for (int scheduleId : bookControlCacheDTO.getScheduleIds()) {
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(scheduleId);
				scheduleList.add(scheduleDTO);
			}
			phoneBookControlDTO.setScheduleList(scheduleList);

			List<RouteDTO> rouetList = new ArrayList<>();
			for (String route : bookControlCacheDTO.getRouteList()) {
				RouteDTO routeDTO = new RouteDTO();

				StationDTO fromStation = new StationDTO();
				fromStation.setId(Integer.valueOf(route.split(Text.UNDER_SCORE)[Numeric.ZERO_INT]));
				routeDTO.setFromStation(stationService.getStation(fromStation));
				routeDTO.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setId(Integer.valueOf(route.split(Text.UNDER_SCORE)[Numeric.ONE_INT]));
				routeDTO.setToStation(stationService.getStation(toStation));
				routeDTO.setToStation(toStation);

				rouetList.add(routeDTO);
			}
			phoneBookControlDTO.setRouteList(rouetList);

			phoneBookControlDTO.setSlabCalenderType(SlabCalenderTypeEM.getSlabCalenderTypeEM(bookControlCacheDTO.getSlabCalenderType()));
			phoneBookControlDTO.setSlabCalenderMode(SlabCalenderModeEM.getSlabCalenderModeEM(bookControlCacheDTO.getSlabCalenderMode()));
			phoneBookControlDTO.setSlabMode(SlabModeEM.getSlabModeEM(bookControlCacheDTO.getSlabMode()));
			phoneBookControlDTO.setMaxSlabValueLimit(bookControlCacheDTO.getMaxSlabValueLimit());
			phoneBookControlDTO.setActiveFlag(bookControlCacheDTO.getActiveFlag());

			List<TicketPhoneBookControlDTO> overrideTicketBookingLimts = new ArrayList<>();
			for (TicketPhoneBookControlCacheDTO overrideBookControlCache : bookControlCacheDTO.getOverride()) {
				TicketPhoneBookControlDTO overrideTicketBookControl = new TicketPhoneBookControlDTO();
				overrideTicketBookControl.setId(overrideBookControlCache.getId());
				overrideTicketBookControl.setCode(overrideBookControlCache.getCode());
				overrideTicketBookControl.setName(overrideBookControlCache.getName());
				overrideTicketBookControl.setRefferenceType(overrideBookControlCache.getRefferenceType());

				if (overrideBookControlCache.getGroupId() != 0) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(overrideBookControlCache.getGroupId());
					overrideTicketBookControl.setGroup(groupDTO);
				}
				if (overrideBookControlCache.getUserId() != 0) {
					UserDTO userDTO = new UserDTO();
					userDTO.setId(overrideBookControlCache.getUserId());
					overrideTicketBookControl.setUserDTO(userDTO);
				}

				overrideTicketBookControl.setActiveFrom(overrideBookControlCache.getActiveFrom());
				overrideTicketBookControl.setActiveTo(overrideBookControlCache.getActiveTo());
				overrideTicketBookControl.setDayOfWeek(overrideBookControlCache.getDayOfWeek());
				overrideTicketBookControl.setDateType(DateTypeEM.getDateTypeEM(overrideBookControlCache.getDateType()));
				overrideTicketBookControl.setLookupCode(overrideBookControlCache.getLookupCode());
				overrideTicketBookControl.setTicketStatus(TicketStatusEM.getTicketStatusEM(overrideBookControlCache.getTicketStatusId()));

				List<ScheduleDTO> overrideScheduleList = new ArrayList<>();
				for (int scheduleId : overrideBookControlCache.getScheduleIds()) {
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setId(scheduleId);
					overrideScheduleList.add(scheduleDTO);
				}
				overrideTicketBookControl.setScheduleList(overrideScheduleList);

				List<RouteDTO> overrideRouetList = new ArrayList<>();
				for (String route : overrideBookControlCache.getRouteList()) {
					RouteDTO routeDTO = new RouteDTO();

					StationDTO fromStation = new StationDTO();
					fromStation.setId(Integer.valueOf(route.split(Text.UNDER_SCORE)[Numeric.ZERO_INT]));
					routeDTO.setFromStation(stationService.getStation(fromStation));
					routeDTO.setFromStation(fromStation);

					StationDTO toStation = new StationDTO();
					toStation.setId(Integer.valueOf(route.split(Text.UNDER_SCORE)[Numeric.ONE_INT]));
					routeDTO.setToStation(stationService.getStation(toStation));
					routeDTO.setToStation(toStation);

					overrideRouetList.add(routeDTO);
				}
				overrideTicketBookControl.setRouteList(overrideRouetList);

				overrideTicketBookControl.setSlabCalenderType(SlabCalenderTypeEM.getSlabCalenderTypeEM(overrideBookControlCache.getSlabCalenderType()));
				overrideTicketBookControl.setSlabCalenderMode(SlabCalenderModeEM.getSlabCalenderModeEM(overrideBookControlCache.getSlabCalenderMode()));
				overrideTicketBookControl.setSlabMode(SlabModeEM.getSlabModeEM(overrideBookControlCache.getSlabMode()));
				overrideTicketBookControl.setMaxSlabValueLimit(overrideBookControlCache.getMaxSlabValueLimit());
				overrideTicketBookControl.setActiveFlag(overrideBookControlCache.getActiveFlag());
				overrideTicketBookingLimts.add(overrideTicketBookControl);
			}

			phoneBookControlDTO.setOverrideList(overrideTicketBookingLimts);
			ticketBookingLimts.add(phoneBookControlDTO);
		}
		return ticketBookingLimts;
	}

	private List<TicketPhoneBookControlCacheDTO> bindTicketPhoneBookControlToCache(List<TicketPhoneBookControlDTO> phoneBookControlList) {
		List<TicketPhoneBookControlCacheDTO> ticketBookingLimts = new ArrayList<>();
		for (TicketPhoneBookControlDTO ticketBookControlDTO : phoneBookControlList) {
			TicketPhoneBookControlCacheDTO ticketPhoneBookControlCache = new TicketPhoneBookControlCacheDTO();
			ticketPhoneBookControlCache.setId(ticketBookControlDTO.getId());
			ticketPhoneBookControlCache.setCode(ticketBookControlDTO.getCode());
			ticketPhoneBookControlCache.setName(ticketBookControlDTO.getName());
			ticketPhoneBookControlCache.setRefferenceType(ticketBookControlDTO.getRefferenceType());

			if (ticketBookControlDTO.getGroup() != null && ticketBookControlDTO.getGroup().getId() != 0) {
				ticketPhoneBookControlCache.setGroupId(ticketBookControlDTO.getGroup().getId());
			}
			else if (ticketBookControlDTO.getUserDTO() != null && ticketBookControlDTO.getUserDTO().getId() != 0) {
				ticketPhoneBookControlCache.setUserId(ticketBookControlDTO.getUserDTO().getId());
			}

			ticketPhoneBookControlCache.setActiveFrom(ticketBookControlDTO.getActiveFrom());
			ticketPhoneBookControlCache.setActiveTo(ticketBookControlDTO.getActiveTo());
			ticketPhoneBookControlCache.setDayOfWeek(ticketBookControlDTO.getDayOfWeek());
			ticketPhoneBookControlCache.setDateType(ticketBookControlDTO.getDateType().getId());
			ticketPhoneBookControlCache.setLookupCode(ticketBookControlDTO.getLookupCode());
			ticketPhoneBookControlCache.setTicketStatusId(ticketBookControlDTO.getTicketStatusId());

			List<Integer> scheduleList = new ArrayList<>();
			for (ScheduleDTO scheduleDTO : ticketBookControlDTO.getScheduleList()) {
				scheduleList.add(scheduleDTO.getId());
			}
			ticketPhoneBookControlCache.setScheduleIds(scheduleList);

			List<String> rouetList = new ArrayList<>();
			for (RouteDTO routeDTO : ticketBookControlDTO.getRouteList()) {
				String route = routeDTO.getFromStation().getId() + "_" + routeDTO.getToStation().getId();
				rouetList.add(route);
			}
			ticketPhoneBookControlCache.setRouteList(rouetList);

			ticketPhoneBookControlCache.setSlabCalenderType(ticketBookControlDTO.getSlabCalenderType().getCode());
			ticketPhoneBookControlCache.setSlabCalenderMode(ticketBookControlDTO.getSlabCalenderMode().getCode());
			ticketPhoneBookControlCache.setSlabMode(ticketBookControlDTO.getSlabMode().getCode());
			ticketPhoneBookControlCache.setMaxSlabValueLimit(ticketBookControlDTO.getMaxSlabValueLimit());
			ticketPhoneBookControlCache.setActiveFlag(ticketBookControlDTO.getActiveFlag());

			List<TicketPhoneBookControlCacheDTO> overrideTicketBookingCacheLimts = new ArrayList<>();
			for (TicketPhoneBookControlDTO overrideBookControlDTO : ticketBookControlDTO.getOverrideList()) {
				TicketPhoneBookControlCacheDTO overrideBookControlCache = new TicketPhoneBookControlCacheDTO();
				overrideBookControlCache.setId(overrideBookControlDTO.getId());
				overrideBookControlCache.setCode(overrideBookControlDTO.getCode());
				overrideBookControlCache.setName(overrideBookControlDTO.getName());
				overrideBookControlCache.setRefferenceType(overrideBookControlDTO.getRefferenceType());

				if (overrideBookControlDTO.getGroup() != null && overrideBookControlDTO.getGroup().getId() != 0) {
					overrideBookControlCache.setGroupId(overrideBookControlDTO.getGroup().getId());
				}
				else if (overrideBookControlDTO.getUserDTO() != null && overrideBookControlDTO.getUserDTO().getId() != 0) {
					overrideBookControlCache.setUserId(overrideBookControlDTO.getUserDTO().getId());
				}

				overrideBookControlCache.setActiveFrom(overrideBookControlDTO.getActiveFrom());
				overrideBookControlCache.setActiveTo(overrideBookControlDTO.getActiveTo());
				overrideBookControlCache.setDayOfWeek(overrideBookControlDTO.getDayOfWeek());
				overrideBookControlCache.setDateType(overrideBookControlDTO.getDateType().getId());
				overrideBookControlCache.setLookupCode(overrideBookControlDTO.getLookupCode());
				overrideBookControlCache.setTicketStatusId(overrideBookControlDTO.getTicketStatusId());

				List<Integer> overrideScheduleList = new ArrayList<>();
				for (ScheduleDTO scheduleDTO : overrideBookControlDTO.getScheduleList()) {
					overrideScheduleList.add(scheduleDTO.getId());
				}
				overrideBookControlCache.setScheduleIds(overrideScheduleList);

				List<String> overrideRouetList = new ArrayList<>();
				for (RouteDTO routeDTO : overrideBookControlDTO.getRouteList()) {
					String route = routeDTO.getFromStation().getId() + "_" + routeDTO.getToStation().getId();
					overrideRouetList.add(route);
				}
				overrideBookControlCache.setRouteList(overrideRouetList);

				overrideBookControlCache.setSlabCalenderType(overrideBookControlDTO.getSlabCalenderType().getCode());
				overrideBookControlCache.setSlabCalenderMode(overrideBookControlDTO.getSlabCalenderMode().getCode());
				overrideBookControlCache.setSlabMode(overrideBookControlDTO.getSlabMode().getCode());
				overrideBookControlCache.setMaxSlabValueLimit(overrideBookControlDTO.getMaxSlabValueLimit());
				overrideBookControlCache.setActiveFlag(overrideBookControlDTO.getActiveFlag());
				overrideTicketBookingCacheLimts.add(overrideBookControlCache);
			}
			ticketPhoneBookControlCache.setOverride(overrideTicketBookingCacheLimts);
			ticketBookingLimts.add(ticketPhoneBookControlCache);
		}
		return ticketBookingLimts;
	}

	@Override
	public List<TicketPhoneBookCancelControlDTO> getPhoneBookCancelControl(AuthDTO authDTO) {
		PhoneTicketControlDAO ticketControlDAO = new PhoneTicketControlDAO();
		List<TicketPhoneBookCancelControlDTO> phoneBookControlList = ticketControlDAO.getPhoneBookCancelControl(authDTO);
		for (TicketPhoneBookCancelControlDTO phoneBookCancel : phoneBookControlList) {
			if (phoneBookCancel.getRefferenceType().equals("GR") && phoneBookCancel.getGroupList() != null) {
				for (GroupDTO group : phoneBookCancel.getGroupList()) {
					group = groupService.getGroup(authDTO, group);
				}
			}
			else if (phoneBookCancel.getRefferenceType().equals("UR") && phoneBookCancel.getUserList() != null) {
				for (UserDTO user : phoneBookCancel.getUserList()) {
					user = userService.getUser(authDTO, user);
				}
			}
			for (ScheduleDTO scheduleDTO : phoneBookCancel.getScheduleList()) {
				getScheduleDTO(authDTO, scheduleDTO);
			}
			for (RouteDTO routeDTO : phoneBookCancel.getRouteList()) {
				routeDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
				routeDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
			}
		}
		return phoneBookControlList;
	}

	@Override
	public TicketPhoneBookCancelControlDTO updatePhoneBookCancelControl(AuthDTO authDTO, TicketPhoneBookCancelControlDTO phoneBookCancel) {
		PhoneTicketControlDAO ticketControlDAO = new PhoneTicketControlDAO();
		if (StringUtil.isNotNull(phoneBookCancel.getRefferenceType()) && phoneBookCancel.getRefferenceType().equals("GR") && phoneBookCancel.getGroupList() != null) {
			for (GroupDTO group : phoneBookCancel.getGroupList()) {
				group = groupService.getGroup(authDTO, group);
			}
		}
		else if (StringUtil.isNotNull(phoneBookCancel.getRefferenceType()) && phoneBookCancel.getRefferenceType().equals("UR") && phoneBookCancel.getUserList() != null) {
			for (UserDTO user : phoneBookCancel.getUserList()) {
				UserDTO repoDTO = userService.getUser(authDTO, user);
				user.setId(repoDTO.getId());
			}
		}
		for (ScheduleDTO scheduleDTO : phoneBookCancel.getScheduleList()) {
			getScheduleDTO(authDTO, scheduleDTO);
		}
		for (RouteDTO routeDTO : phoneBookCancel.getRouteList()) {
			routeDTO.setFromStation(stationService.getStation(routeDTO.getFromStation()));
			routeDTO.setToStation(stationService.getStation(routeDTO.getToStation()));
		}
		return ticketControlDAO.updatePhoneBookCancelControl(authDTO, phoneBookCancel);
	}

	@Override
	public TicketPhoneBookCancelControlDTO getActivePhoneBookCancelControl(AuthDTO authDTO, ScheduleDTO scheduleDTO, TicketDTO ticketDTO) {
		TicketPhoneBookCancelControlDTO phoneBookCancelControl = null;
		PhoneTicketControlDAO ticketControlDAO = new PhoneTicketControlDAO();
		List<TicketPhoneBookCancelControlDTO> phoneBookControlList = ticketControlDAO.getPhoneBookCancelControl(authDTO);
		for (Iterator<TicketPhoneBookCancelControlDTO> iterator = phoneBookControlList.iterator(); iterator.hasNext();) {
			TicketPhoneBookCancelControlDTO bookControlDTO = iterator.next();
			// common validations
			DateTime now = DateUtil.NOW();
			if (bookControlDTO.getActiveFrom() != null && !now.gteq(bookControlDTO.getActiveFrom())) {
				iterator.remove();
				continue;
			}
			if (bookControlDTO.getActiveTo() != null && !now.lteq(bookControlDTO.getActiveTo().getEndOfDay())) {
				iterator.remove();
				continue;
			}
			if (bookControlDTO.getDayOfWeek() != null && bookControlDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (bookControlDTO.getDayOfWeek() != null && bookControlDTO.getDayOfWeek().substring(now.getWeekDay() - 1, now.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && bookControlDTO.getRefferenceType().equals("GR") && !bookControlDTO.getGroupList().isEmpty() && BitsUtil.isGroupExists(bookControlDTO.getGroupList(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}
			else if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && bookControlDTO.getRefferenceType().equals("UR") && !bookControlDTO.getUserList().isEmpty() && BitsUtil.isUserExists(bookControlDTO.getUserList(), authDTO.getUser()) == null) {
				iterator.remove();
				continue;
			}
			// Route List
			if (!bookControlDTO.getRouteList().isEmpty() && BitsUtil.isRouteExists(bookControlDTO.getRouteList(), ticketDTO.getFromStation(), ticketDTO.getToStation()) == null) {
				iterator.remove();
				continue;
			}
			// Schedule List
			if (!bookControlDTO.getScheduleList().isEmpty() && BitsUtil.isScheduleExists(bookControlDTO.getScheduleList(), scheduleDTO) == null) {
				iterator.remove();
				continue;
			}
		}
		
		if (phoneBookControlList.size() >= 2) {
			// Sorting, find most recent close date
			Collections.sort(phoneBookControlList, new Comparator<TicketPhoneBookCancelControlDTO>() {
				public int compare(final TicketPhoneBookCancelControlDTO object1, final TicketPhoneBookCancelControlDTO object2) {
					return Integer.compare(DateUtil.getDayDifferent(object1.getActiveFrom(), object1.getActiveTo()), DateUtil.getDayDifferent(object2.getActiveFrom(), object2.getActiveTo()));
				}
			});
		}
		
		if (!phoneBookControlList.isEmpty()) {
			phoneBookCancelControl = phoneBookControlList.get(Numeric.ZERO_INT);
		}
		
		return phoneBookCancelControl;
	}

	@Override
	public CancellationTermDTO getCancellationPolicyConvention(AuthDTO authDTO, TicketPhoneBookCancelControlDTO phoneBookCancelControl, TicketDTO ticketDTO) {
		CancellationTermDTO cancellationterm = new CancellationTermDTO();
		List<CancellationPolicyDTO> cancellationTermsList = new ArrayList<CancellationPolicyDTO>();
		if (phoneBookCancelControl != null) {
			cancellationterm.setCode(phoneBookCancelControl.getCode());
			cancellationterm.setName(phoneBookCancelControl.getName());
			cancellationterm.setActiveFlag(phoneBookCancelControl.getActiveFlag());
			if (phoneBookCancelControl.getPolicyMinute() == -1) {
				List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, ticketDTO.getTripDTO());
				StageStationDTO stageStatin = BitsUtil.getDestinationStageStation(stageList);
				StationPointDTO lastStationpoint = BitsUtil.getDestinationStationPoint(stageStatin.getStationPoint());
				DateTime destinationStationDateTime = BitsUtil.getDestinationStationTime(stageList, ticketDTO.getTripDTO().getTripDate());
				
				CancellationPolicyDTO defaultPolicyDTO = new CancellationPolicyDTO();
				defaultPolicyDTO.setTerm("After " + DateUtil.addMinituesToDate(destinationStationDateTime, lastStationpoint.getMinitues()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
				defaultPolicyDTO.setDeductionAmountTxt("No Cancellation");
				defaultPolicyDTO.setRefundAmountTxt(" - ");
				defaultPolicyDTO.setChargesTxt("-");
				cancellationTermsList.add(defaultPolicyDTO);
				
				CancellationPolicyDTO policyDTO = new CancellationPolicyDTO();
				policyDTO.setTerm("Till " + DateUtil.addMinituesToDate(destinationStationDateTime, lastStationpoint.getMinitues()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
				policyDTO.setDeductionAmountTxt("Cancellation Allowed");
				policyDTO.setRefundAmountTxt(" - ");
				policyDTO.setChargesTxt("-");
				cancellationTermsList.add(policyDTO);
			}
			else if (phoneBookCancelControl.getPolicyPattern().equals("MIN")) {
				DateTime tripDateTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes());
				DateTime stageDateTime = ticketDTO.getBoardingPointDateTime();
				
				CancellationPolicyDTO defaultPolicyDTO = new CancellationPolicyDTO();
				defaultPolicyDTO.setDeductionAmountTxt("No Cancellation");
				defaultPolicyDTO.setRefundAmountTxt(" - ");
				defaultPolicyDTO.setChargesTxt("-");
				
				CancellationPolicyDTO tillPolicyDTO = new CancellationPolicyDTO();
				tillPolicyDTO.setDeductionAmountTxt("Cancellation Allowed");
				tillPolicyDTO.setRefundAmountTxt(" - ");
				tillPolicyDTO.setChargesTxt("-");
				
				if (phoneBookCancelControl.getTripStageFlag() == Numeric.ONE_INT) {
					defaultPolicyDTO.setTerm("After " + DateUtil.minusMinituesToDate(tripDateTime, phoneBookCancelControl.getPolicyMinute()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
					tillPolicyDTO.setTerm("Till " + DateUtil.minusMinituesToDate(tripDateTime, phoneBookCancelControl.getPolicyMinute()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
				}
				else {
					defaultPolicyDTO.setTerm("After " + DateUtil.minusMinituesToDate(stageDateTime, phoneBookCancelControl.getPolicyMinute()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
					tillPolicyDTO.setTerm("Till " + DateUtil.minusMinituesToDate(stageDateTime, phoneBookCancelControl.getPolicyMinute()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
				}
				cancellationTermsList.add(defaultPolicyDTO);
				cancellationTermsList.add(tillPolicyDTO);
			}
			else if (phoneBookCancelControl.getPolicyPattern().equals("AM") || phoneBookCancelControl.getPolicyPattern().equals("PM")) {
				DateTime tripDateTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes());
				DateTime stageDateTime = ticketDTO.getBoardingPointDateTime();
				
				CancellationPolicyDTO defaultPolicyDTO = new CancellationPolicyDTO();
				defaultPolicyDTO.setDeductionAmountTxt("No Cancellation");
				defaultPolicyDTO.setRefundAmountTxt(" - ");
				defaultPolicyDTO.setChargesTxt("-");
				
				CancellationPolicyDTO tillPolicyDTO = new CancellationPolicyDTO();
				tillPolicyDTO.setDeductionAmountTxt("Cancellation Allowed");
				tillPolicyDTO.setRefundAmountTxt(" - ");
				tillPolicyDTO.setChargesTxt("-");
				
				if (phoneBookCancelControl.getTripStageFlag() == Numeric.ONE_INT) {
					defaultPolicyDTO.setTerm("After " + tripDateTime.format("DD/MM/YYYY", Locale.forLanguageTag("en_IN")) + " " + phoneBookCancelControl.getPolicyMinute() + ":00 " + phoneBookCancelControl.getPolicyPattern());
					tillPolicyDTO.setTerm("Till " + tripDateTime.format("DD/MM/YYYY", Locale.forLanguageTag("en_IN")) + " " + phoneBookCancelControl.getPolicyMinute() + ":00 " + phoneBookCancelControl.getPolicyPattern());
				}
				else {
					defaultPolicyDTO.setTerm("After " + stageDateTime.format("DD/MM/YYYY", Locale.forLanguageTag("en_IN")) + " " + phoneBookCancelControl.getPolicyMinute() + ":00 " + phoneBookCancelControl.getPolicyPattern());
					tillPolicyDTO.setTerm("Till " + stageDateTime.format("DD/MM/YYYY", Locale.forLanguageTag("en_IN")) + " " + phoneBookCancelControl.getPolicyMinute() + ":00 " + phoneBookCancelControl.getPolicyPattern());
				}
				cancellationTermsList.add(defaultPolicyDTO);
				cancellationTermsList.add(tillPolicyDTO);
			}
		}
		cancellationterm.setPolicyList(cancellationTermsList);
		return cancellationterm;
		
		
	}
}
