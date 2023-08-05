package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Cleanup;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.TicketPhoneBookCancelControlDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketPhoneBookControlDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.SlabCalenderModeEM;
import org.in.com.dto.enumeration.SlabCalenderTypeEM;
import org.in.com.dto.enumeration.SlabModeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

public class PhoneTicketControlDAO {

	public List<TicketPhoneBookControlDTO> getAll(AuthDTO authDTO) {
		List<TicketPhoneBookControlDTO> overrideList = new ArrayList<TicketPhoneBookControlDTO>();
		Map<Integer, TicketPhoneBookControlDTO> controlMap = new HashMap<Integer, TicketPhoneBookControlDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, active_from, active_to, day_of_week, user_group_id, allow_minutes, block_minutes, block_minutes_type, lookup_id, active_flag FROM ticket_phone_book_time_control WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				TicketPhoneBookControlDTO controlDTO = new TicketPhoneBookControlDTO();
				controlDTO.setId(selectRS.getInt("id"));
				controlDTO.setCode(selectRS.getString("code"));
				controlDTO.setActiveFlag(selectRS.getInt("active_flag"));
				controlDTO.setActiveFrom(selectRS.getString("active_from"));
				controlDTO.setActiveTo(selectRS.getString("active_to"));
				controlDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				controlDTO.setAllowMinutes(selectRS.getInt("allow_minutes"));
				controlDTO.setBlockMinutes(selectRS.getInt("block_minutes"));
				controlDTO.setBlockMinutesType(MinutesTypeEM.getMinutesTypeEM(selectRS.getInt("block_minutes_type")));
				controlDTO.setLookupCode(selectRS.getString("lookup_id"));

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("user_group_id"));
				controlDTO.setGroup(groupDTO);

				if (controlDTO.getLookupCode().equals("0")) {
					controlMap.put(controlDTO.getId(), controlDTO);
				}
				else {
					overrideList.add(controlDTO);
				}
			}
			for (TicketPhoneBookControlDTO overrideControlDTO : overrideList) {
				if (controlMap.get(Integer.parseInt(overrideControlDTO.getLookupCode())) != null) {
					TicketPhoneBookControlDTO dto = controlMap.get(Integer.parseInt(overrideControlDTO.getLookupCode()));
					dto.getOverrideList().add(overrideControlDTO);
					controlMap.put(dto.getId(), dto);
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<TicketPhoneBookControlDTO>(controlMap.values());

	}

	public TicketPhoneBookControlDTO getIUD(AuthDTO authDTO, TicketPhoneBookControlDTO controlDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call   EZEE_SP_TICKET_PHONE_BOOK_TIME_CONTROL_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,? )}");

			callableStatement.setString(++pindex, controlDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, controlDTO.getActiveFrom() != null ? controlDTO.getActiveFrom().trim() : null);
			callableStatement.setString(++pindex, controlDTO.getActiveTo() != null ? controlDTO.getActiveTo().trim() : null);
			callableStatement.setString(++pindex, controlDTO.getDayOfWeek());
			callableStatement.setInt(++pindex, controlDTO.getGroup().getId());
			callableStatement.setInt(++pindex, controlDTO.getAllowMinutes());
			callableStatement.setInt(++pindex, controlDTO.getBlockMinutes());
			callableStatement.setInt(++pindex, controlDTO.getBlockMinutesType() != null ? controlDTO.getBlockMinutesType().getId() : 0);
			callableStatement.setString(++pindex, controlDTO.getLookupCode());
			callableStatement.setInt(++pindex, controlDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				controlDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
				controlDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return controlDTO;
	}

	public TicketPhoneBookControlDTO updateBookLimitControlIUD(AuthDTO authDTO, TicketPhoneBookControlDTO controlDTO) {
		String reffernceCode = null;
		if (controlDTO.getRefferenceType() != null && controlDTO.getRefferenceType().equals("UR") && controlDTO.getUserDTO() != null) {
			reffernceCode = controlDTO.getUserDTO().getCode();
		}
		else if (controlDTO.getRefferenceType() != null && controlDTO.getRefferenceType().equals("GR") && controlDTO.getGroup() != null) {
			reffernceCode = controlDTO.getGroup().getCode();
		}
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_TICKET_BOOK_LIMIT_CONTROL_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,?, ?,?,?,?,? ,?,?)}");
			callableStatement.setString(++pindex, controlDTO.getCode());
			callableStatement.setString(++pindex, controlDTO.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, controlDTO.getRefferenceType());
			callableStatement.setString(++pindex, reffernceCode);
			callableStatement.setString(++pindex, controlDTO.getActiveFrom() != null ? controlDTO.getActiveFrom().trim() : null);
			callableStatement.setString(++pindex, controlDTO.getActiveTo() != null ? controlDTO.getActiveTo().trim() : null);
			callableStatement.setString(++pindex, controlDTO.getDayOfWeek());
			callableStatement.setString(++pindex, controlDTO.getDateType() != null ? controlDTO.getDateType().getCode() : null);
			callableStatement.setString(++pindex, controlDTO.getLookupCode());
			callableStatement.setInt(++pindex, controlDTO.getTicketStatusId());
			callableStatement.setString(++pindex, getSchedules(controlDTO.getScheduleList()));
			callableStatement.setString(++pindex, getRoutes(controlDTO.getRouteList()));
			callableStatement.setInt(++pindex, controlDTO.getSlabCalenderType() != null ? controlDTO.getSlabCalenderType().getId() : 0);
			callableStatement.setInt(++pindex, controlDTO.getSlabCalenderMode() != null ? controlDTO.getSlabCalenderMode().getId() : 0);
			callableStatement.setInt(++pindex, controlDTO.getSlabMode() != null ? controlDTO.getSlabMode().getId() : 0);
			callableStatement.setInt(++pindex, controlDTO.getMaxSlabValueLimit());
			callableStatement.setInt(++pindex, controlDTO.getRespectiveFlag());
			callableStatement.setInt(++pindex, controlDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				controlDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
				controlDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return controlDTO;
	}

	public List<TicketPhoneBookControlDTO> getBookLimitsControl(AuthDTO authDTO) {
		List<TicketPhoneBookControlDTO> overrideList = new ArrayList<TicketPhoneBookControlDTO>();
		Map<Integer, TicketPhoneBookControlDTO> controlMap = new HashMap<Integer, TicketPhoneBookControlDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, role_type, role_type_refference_id, from_date, to_date, day_of_week, date_type, lookup_id, ticket_status_id, schedule_id, route_id, slab_calendar_type_id, slab_calendar_mode_id, slab_mode_id, slab_max_value_limit, respective_flag, active_flag FROM ticket_book_limit_control WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				TicketPhoneBookControlDTO controlDTO = new TicketPhoneBookControlDTO();
				controlDTO.setId(selectRS.getInt("id"));
				controlDTO.setCode(selectRS.getString("code"));
				controlDTO.setName(selectRS.getString("name"));
				controlDTO.setRefferenceType(selectRS.getString("role_type"));

				int reffernceId = selectRS.getInt("role_type_refference_id");
				if (reffernceId != 0 && controlDTO.getRefferenceType().equals("GR")) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(reffernceId);
					controlDTO.setGroup(groupDTO);
				}
				else if (reffernceId != 0 && controlDTO.getRefferenceType().equals("UR")) {
					UserDTO userDTO = new UserDTO();
					userDTO.setId(reffernceId);
					controlDTO.setUserDTO(userDTO);
				}

				controlDTO.setActiveFrom(selectRS.getString("from_date"));
				controlDTO.setActiveTo(selectRS.getString("to_date"));
				controlDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				controlDTO.setDateType(DateTypeEM.getDateTypeEM(selectRS.getString("date_type")));
				controlDTO.setLookupCode(selectRS.getString("lookup_id"));
				controlDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(selectRS.getInt("ticket_status_id")));

				String ScheduleIds = selectRS.getString("schedule_id");
				List<ScheduleDTO> scheduleList = convertSchedule(ScheduleIds);
				controlDTO.setScheduleList(scheduleList);

				String routeIds = selectRS.getString("route_id");
				List<RouteDTO> rouetList = convertRoute(routeIds);
				controlDTO.setRouteList(rouetList);

				controlDTO.setSlabCalenderType(selectRS.getInt("slab_calendar_type_id") != 0 ? SlabCalenderTypeEM.getSlabCalenderTypeEM(selectRS.getInt("slab_calendar_type_id")) : null);
				controlDTO.setSlabCalenderMode(selectRS.getInt("slab_calendar_mode_id") != 0 ? SlabCalenderModeEM.getSlabCalenderModeEM(selectRS.getInt("slab_calendar_mode_id")) : null);
				controlDTO.setSlabMode(selectRS.getInt("slab_mode_id") != 0 ? SlabModeEM.getSlabModeEM(selectRS.getInt("slab_mode_id")) : null);
				controlDTO.setMaxSlabValueLimit(selectRS.getInt("slab_max_value_limit"));
				controlDTO.setRespectiveFlag(selectRS.getInt("respective_flag"));
				controlDTO.setActiveFlag(selectRS.getInt("active_flag"));

				if (controlDTO.getLookupCode().equals("0")) {
					controlMap.put(controlDTO.getId(), controlDTO);
				}
				else {
					overrideList.add(controlDTO);
				}
			}
			for (TicketPhoneBookControlDTO overrideControlDTO : overrideList) {
				if (controlMap.get(Integer.parseInt(overrideControlDTO.getLookupCode())) != null) {
					TicketPhoneBookControlDTO dto = controlMap.get(Integer.parseInt(overrideControlDTO.getLookupCode()));
					dto.getOverrideList().add(overrideControlDTO);
					controlMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<TicketPhoneBookControlDTO>(controlMap.values());

	}

	public TicketPhoneBookCancelControlDTO updatePhoneBookCancelControl(AuthDTO authDTO, TicketPhoneBookCancelControlDTO controlDTO) {
		String referenceIds = getReferecenIds(controlDTO);
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_TICKET_PHONE_BOOK_CANCEL_CONTROL_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,?, ?,?)}");
			callableStatement.setString(++pindex, controlDTO.getCode());
			callableStatement.setString(++pindex, controlDTO.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, controlDTO.getRefferenceType());
			callableStatement.setString(++pindex, referenceIds);
			callableStatement.setString(++pindex, controlDTO.getActiveFrom() != null ? DateUtil.convertDate(controlDTO.getActiveFrom()) : null);
			callableStatement.setString(++pindex, controlDTO.getActiveTo() != null ? DateUtil.convertDate(controlDTO.getActiveTo()) : null);
			callableStatement.setString(++pindex, controlDTO.getDayOfWeek());
			callableStatement.setString(++pindex, getSchedules(controlDTO.getScheduleList()));
			callableStatement.setString(++pindex, getRoutes(controlDTO.getRouteList()));
			callableStatement.setInt(++pindex, controlDTO.getTripStageFlag());
			callableStatement.setInt(++pindex, controlDTO.getPolicyMinute());
			callableStatement.setString(++pindex, controlDTO.getPolicyPattern());
			callableStatement.setInt(++pindex, controlDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				controlDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
				controlDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return controlDTO;
	}

	public List<TicketPhoneBookCancelControlDTO> getPhoneBookCancelControl(AuthDTO authDTO) {
		List<TicketPhoneBookCancelControlDTO> list = new ArrayList<TicketPhoneBookCancelControlDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, role_type, role_type_refference_id, from_date, to_date, day_of_week, schedule_id, route_id, trip_minute_flag, policy_minute, policy_pattern, active_flag FROM phone_book_cancel_control WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				TicketPhoneBookCancelControlDTO controlDTO = new TicketPhoneBookCancelControlDTO();
				controlDTO.setId(selectRS.getInt("id"));
				controlDTO.setCode(selectRS.getString("code"));
				controlDTO.setName(selectRS.getString("name"));
				controlDTO.setRefferenceType(selectRS.getString("role_type"));
				controlDTO.setTripStageFlag(selectRS.getInt("trip_minute_flag"));

				String refferenceId = selectRS.getString("role_type_refference_id");
				if (controlDTO.getRefferenceType().equals("GR")) {
					List<GroupDTO> groupList = convertGroup(refferenceId);
					controlDTO.setGroupList(groupList);
				}
				else if (controlDTO.getRefferenceType().equals("UR")) {
					List<UserDTO> userList = convertUser(refferenceId);
					controlDTO.setUserList(userList);
				}

				controlDTO.setActiveFrom(DateUtil.getDateTime(selectRS.getString("from_date")));
				controlDTO.setActiveTo(DateUtil.getDateTime(selectRS.getString("to_date")));
				controlDTO.setDayOfWeek(selectRS.getString("day_of_week"));

				String ScheduleIds = selectRS.getString("schedule_id");
				List<ScheduleDTO> scheduleList = convertSchedule(ScheduleIds);
				controlDTO.setScheduleList(scheduleList);

				String routeIds = selectRS.getString("route_id");
				List<RouteDTO> rouetList = convertRoute(routeIds);
				controlDTO.setRouteList(rouetList);

				controlDTO.setPolicyMinute(selectRS.getInt("policy_minute"));
				controlDTO.setPolicyPattern(selectRS.getString("policy_pattern"));
				controlDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(controlDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return list;

	}

	private String getReferecenIds(TicketPhoneBookCancelControlDTO phoneBookControlDTO) {
		StringBuilder referenceIds = new StringBuilder();
		if (phoneBookControlDTO.getRefferenceType() != null && phoneBookControlDTO.getRefferenceType().equals("UR") && phoneBookControlDTO.getUserList() != null) {
			for (UserDTO userDTO : phoneBookControlDTO.getUserList()) {
				if (userDTO.getId() == 0) {
					continue;
				}
				referenceIds.append(userDTO.getId());
				referenceIds.append(Text.COMMA);
			}
		}
		else if (phoneBookControlDTO.getRefferenceType() != null && phoneBookControlDTO.getRefferenceType().equals("GR") && phoneBookControlDTO.getGroupList() != null) {
			for (GroupDTO groupDTO : phoneBookControlDTO.getGroupList()) {
				if (groupDTO.getId() == 0) {
					continue;
				}
				referenceIds.append(groupDTO.getId());
				referenceIds.append(Text.COMMA);
			}
		}
		return StringUtil.isNotNull(referenceIds.toString()) ? referenceIds.toString() : Text.NA;
	}

	public List<UserDTO> convertUser(String referenceIds) {
		List<UserDTO> userList = new ArrayList<>();
		if (StringUtil.isNotNull(referenceIds)) {
			String[] referenceId = referenceIds.split(Text.COMMA);

			for (String userId : referenceId) {
				if (StringUtil.isNull(userId) || Numeric.ZERO.equals(userId)) {
					continue;
				}
				UserDTO user = new UserDTO();
				user.setId(Integer.valueOf(userId));
				userList.add(user);
			}
		}
		return userList;
	}

	public List<GroupDTO> convertGroup(String referenceIds) {
		List<GroupDTO> groupList = new ArrayList<GroupDTO>();
		if (StringUtil.isNotNull(referenceIds)) {
			String[] referenceId = referenceIds.split(Text.COMMA);

			for (String groupId : referenceId) {
				if (StringUtil.isNull(groupId) || Numeric.ZERO.equals(groupId)) {
					continue;
				}
				GroupDTO group = new GroupDTO();
				group.setId(Integer.valueOf(groupId));
				groupList.add(group);
			}
		}
		return groupList;
	}

	private String getSchedules(List<ScheduleDTO> scheduleDTOList) {
		StringBuilder scheduleIds = new StringBuilder();
		for (ScheduleDTO scheduleDTO : scheduleDTOList) {
			if (scheduleDTO.getId() == 0) {
				continue;
			}
			scheduleIds.append(scheduleDTO.getId());
			scheduleIds.append(Text.COMMA);
		}
		return StringUtil.isNotNull(scheduleIds.toString()) ? scheduleIds.toString() : Text.NA;
	}

	private List<ScheduleDTO> convertSchedule(String scheduleIds) {
		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		if (StringUtil.isNotNull(scheduleIds)) {
			String[] scheduleId = scheduleIds.split(Text.COMMA);

			for (String schedule : scheduleId) {
				if (StringUtil.isNull(schedule) || Numeric.ZERO.equals(schedule)) {
					continue;
				}

				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(StringUtil.getIntegerValue(schedule));
				scheduleList.add(scheduleDTO);
			}
		}
		return scheduleList;
	}

	private String getRoutes(List<RouteDTO> rouetDTOList) {
		StringBuilder routeIds = new StringBuilder();
		for (RouteDTO routeDTO : rouetDTOList) {
			if (routeDTO.getFromStation() == null || routeDTO.getFromStation().getId() == 0 || routeDTO.getToStation() == null || routeDTO.getToStation().getId() == 0) {
				continue;
			}

			routeIds.append(routeDTO.getFromStation().getId() + Text.UNDER_SCORE + routeDTO.getToStation().getId());
			routeIds.append(Text.COMMA);
		}
		return StringUtil.isNotNull(routeIds.toString()) ? routeIds.toString() : Text.NA;
	}

	private List<RouteDTO> convertRoute(String routeIds) {
		List<RouteDTO> rouetList = new ArrayList<RouteDTO>();
		if (StringUtil.isNotNull(routeIds)) {
			String[] routeId = routeIds.split(Text.COMMA);

			for (String route : routeId) {
				String fromStationId = route.split(Text.UNDER_SCORE)[0];
				String toStationId = route.split(Text.UNDER_SCORE)[1];
				if (StringUtil.isNull(fromStationId) || Numeric.ZERO.equals(fromStationId) || StringUtil.isNull(toStationId) || Numeric.ZERO.equals(toStationId)) {
					continue;
				}

				RouteDTO routeDTO = new RouteDTO();

				StationDTO fromStationDTO = new StationDTO();
				fromStationDTO.setId(StringUtil.getIntegerValue(fromStationId));
				routeDTO.setFromStation(fromStationDTO);

				StationDTO toStationDTO = new StationDTO();
				toStationDTO.setId(StringUtil.getIntegerValue(toStationId));
				routeDTO.setToStation(toStationDTO);

				rouetList.add(routeDTO);
			}
		}
		return rouetList;
	}
}
