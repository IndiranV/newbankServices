package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;
import org.springframework.stereotype.Component;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;

@Component
public class ScheduleTicketTransferTermsDAO {
	public ScheduleTicketTransferTermsDTO updateScheduleTicketTransferTerms(AuthDTO authDTO, ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_TICKET_TRANSFER_TERMS_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?, ?,?,?)}");
			callableStatement.setString(++pindex, scheduleTicketTransferTermsDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, scheduleTicketTransferTermsDTO.getMinutes());
			callableStatement.setInt(++pindex, scheduleTicketTransferTermsDTO.getMinutesType().getId());
			callableStatement.setInt(++pindex, scheduleTicketTransferTermsDTO.getAllowBookedUser());
			callableStatement.setString(++pindex, scheduleTicketTransferTermsDTO.getScheduleCodes());
			callableStatement.setString(++pindex, scheduleTicketTransferTermsDTO.getGroupCodes());
			callableStatement.setString(++pindex, scheduleTicketTransferTermsDTO.getBookedGroupCodes());
			callableStatement.setBigDecimal(++pindex, scheduleTicketTransferTermsDTO.getChargeAmount());
			callableStatement.setInt(++pindex, scheduleTicketTransferTermsDTO.getChargeType().getId());
			callableStatement.setString(++pindex, scheduleTicketTransferTermsDTO.getActiveFrom().format(Text.DATE_DATE4J));
			callableStatement.setString(++pindex, scheduleTicketTransferTermsDTO.getActiveTo().format(Text.DATE_DATE4J));
			callableStatement.setString(++pindex, scheduleTicketTransferTermsDTO.getDayOfWeek());
			callableStatement.setString(++pindex, scheduleTicketTransferTermsDTO.getLookupCode());
			callableStatement.setInt(++pindex, scheduleTicketTransferTermsDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				scheduleTicketTransferTermsDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return scheduleTicketTransferTermsDTO;
	}

	public List<ScheduleTicketTransferTermsDTO> getScheduleTicketTransferTerms(AuthDTO authDTO) {
		List<ScheduleTicketTransferTermsDTO> overrideList = new ArrayList<ScheduleTicketTransferTermsDTO>();
		Map<Integer, ScheduleTicketTransferTermsDTO> scheduleTermsMap = new HashMap<Integer, ScheduleTicketTransferTermsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, minutes, minute_type_id, schedule_codes, group_codes, transfer_charge_amount, transfer_charge_type, active_from, active_to, day_of_week, allow_booked_user_flag, booked_group_codes, lookup_id, active_flag FROM schedule_ticket_transfer_terms WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO = new ScheduleTicketTransferTermsDTO();
				scheduleTicketTransferTermsDTO.setId(selectRS.getInt("id"));
				scheduleTicketTransferTermsDTO.setCode(selectRS.getString("code"));
				scheduleTicketTransferTermsDTO.setMinutes(selectRS.getInt("minutes"));
				scheduleTicketTransferTermsDTO.setMinutesType(MinutesTypeEM.getMinutesTypeEM(selectRS.getInt("minute_type_id")));

				// Schedule & Route
				String scheduleCodes = selectRS.getString("schedule_codes");

				List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
				List<RouteDTO> routeList = new ArrayList<>();
				if (StringUtil.isNotNull(scheduleCodes)) {
					scheduleList = getScheduleList(scheduleCodes);
					routeList = converRouteList(scheduleCodes);
				}
				scheduleTicketTransferTermsDTO.setScheduleList(scheduleList);
				scheduleTicketTransferTermsDTO.setRouteList(routeList);

				// Group
				String groupCodes = selectRS.getString("group_codes");
				List<GroupDTO> groupList = new ArrayList<GroupDTO>();
				if (StringUtil.isNotNull(groupCodes)) {
					groupList = getGroupList(groupCodes);
				}
				scheduleTicketTransferTermsDTO.setGroupList(groupList);

				scheduleTicketTransferTermsDTO.setChargeAmount(selectRS.getBigDecimal("transfer_charge_amount"));
				scheduleTicketTransferTermsDTO.setChargeType(FareTypeEM.getFareTypeEM(selectRS.getInt("transfer_charge_type")));
				scheduleTicketTransferTermsDTO.setActiveFrom(new DateTime(selectRS.getString("active_from")));
				scheduleTicketTransferTermsDTO.setActiveTo(new DateTime(selectRS.getString("active_to")));
				scheduleTicketTransferTermsDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleTicketTransferTermsDTO.setLookupCode(selectRS.getString("lookup_id"));
				scheduleTicketTransferTermsDTO.setActiveFlag(selectRS.getInt("active_flag"));
				scheduleTicketTransferTermsDTO.setAllowBookedUser(selectRS.getInt("allow_booked_user_flag"));

				String bookedGroupCodes = selectRS.getString("booked_group_codes");
				List<GroupDTO> bookedGroupList = new ArrayList<GroupDTO>();
				if (StringUtil.isNotNull(bookedGroupCodes)) {
					bookedGroupList = getGroupList(bookedGroupCodes);
				}
				scheduleTicketTransferTermsDTO.setBookedUserGroups(bookedGroupList);

				if (scheduleTicketTransferTermsDTO.getLookupCode().equals("0")) {
					scheduleTermsMap.put(scheduleTicketTransferTermsDTO.getId(), scheduleTicketTransferTermsDTO);
				}
				else {
					overrideList.add(scheduleTicketTransferTermsDTO);
				}
			}
			for (ScheduleTicketTransferTermsDTO overrideScheduleDTO : overrideList) {
				if (scheduleTermsMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleTicketTransferTermsDTO dto = scheduleTermsMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleTermsMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleTicketTransferTermsDTO>(scheduleTermsMap.values());
	}

	public ScheduleTicketTransferTermsDTO getScheduleTicketTransferTermsById(AuthDTO authDTO, ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO) {
		ScheduleTicketTransferTermsDTO ticketTransferTermsDTO = new ScheduleTicketTransferTermsDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, minutes, minute_type_id, schedule_codes, group_codes, transfer_charge_amount, transfer_charge_type, active_from, active_to, day_of_week, allow_booked_user_flag, booked_group_codes, lookup_id, active_flag FROM schedule_ticket_transfer_terms WHERE namespace_id = ? AND id = ? AND active_flag <= 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleTicketTransferTermsDTO.getId());

			@Cleanup
			PreparedStatement lookupPS = connection.prepareStatement("SELECT id, code, minutes, minute_type_id, schedule_codes, group_codes, transfer_charge_amount, transfer_charge_type, active_from, active_to, day_of_week, allow_booked_user_flag, booked_group_codes, lookup_id, active_flag FROM schedule_ticket_transfer_terms WHERE namespace_id = ? AND lookup_id = ? AND active_flag <= 2");
			lookupPS.setInt(1, authDTO.getNamespace().getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				ticketTransferTermsDTO.setId(selectRS.getInt("id"));
				ticketTransferTermsDTO.setCode(selectRS.getString("code"));
				ticketTransferTermsDTO.setMinutes(selectRS.getInt("minutes"));
				ticketTransferTermsDTO.setMinutesType(MinutesTypeEM.getMinutesTypeEM(selectRS.getInt("minute_type_id")));

				// Schedule & Route
				String scheduleCodes = selectRS.getString("schedule_codes");

				List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
				List<RouteDTO> routeList = new ArrayList<>();
				if (StringUtil.isNotNull(scheduleCodes)) {
					scheduleList = getScheduleList(scheduleCodes);
					routeList = converRouteList(scheduleCodes);
				}
				ticketTransferTermsDTO.setScheduleList(scheduleList);
				ticketTransferTermsDTO.setRouteList(routeList);

				// Group
				String groupCodes = selectRS.getString("group_codes");
				List<GroupDTO> groupList = new ArrayList<GroupDTO>();
				if (StringUtil.isNotNull(groupCodes)) {
					groupList = getGroupList(groupCodes);
				}
				ticketTransferTermsDTO.setGroupList(groupList);

				ticketTransferTermsDTO.setChargeAmount(selectRS.getBigDecimal("transfer_charge_amount"));
				ticketTransferTermsDTO.setChargeType(FareTypeEM.getFareTypeEM(selectRS.getInt("transfer_charge_type")));
				ticketTransferTermsDTO.setActiveFrom(new DateTime(selectRS.getString("active_from")));
				ticketTransferTermsDTO.setActiveTo(new DateTime(selectRS.getString("active_to")));
				ticketTransferTermsDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				ticketTransferTermsDTO.setLookupCode(selectRS.getString("lookup_id"));
				ticketTransferTermsDTO.setActiveFlag(selectRS.getInt("active_flag"));
				ticketTransferTermsDTO.setAllowBookedUser(selectRS.getInt("allow_booked_user_flag"));

				String bookedGroupCodes = selectRS.getString("booked_group_codes");
				List<GroupDTO> bookedGroupList = new ArrayList<GroupDTO>();
				if (StringUtil.isNotNull(bookedGroupCodes)) {
					bookedGroupList = getGroupList(bookedGroupCodes);
				}
				ticketTransferTermsDTO.setBookedUserGroups(bookedGroupList);

				lookupPS.setInt(1, authDTO.getNamespace().getId());
				lookupPS.setInt(2, ticketTransferTermsDTO.getId());

				@Cleanup
				ResultSet lookupRS = lookupPS.executeQuery();

				List<ScheduleTicketTransferTermsDTO> overrideList = new ArrayList<ScheduleTicketTransferTermsDTO>();
				while (lookupRS.next()) {
					ScheduleTicketTransferTermsDTO overrideScheduleTicketTransferTermsDTO = new ScheduleTicketTransferTermsDTO();
					overrideScheduleTicketTransferTermsDTO.setId(lookupRS.getInt("id"));
					overrideScheduleTicketTransferTermsDTO.setCode(lookupRS.getString("code"));
					overrideScheduleTicketTransferTermsDTO.setMinutes(lookupRS.getInt("minutes"));
					overrideScheduleTicketTransferTermsDTO.setMinutesType(MinutesTypeEM.getMinutesTypeEM(lookupRS.getInt("minute_type_id")));

					// Schedule & Route
					String overrideScheduleCodes = lookupRS.getString("schedule_codes");

					List<ScheduleDTO> overrideScheduleList = new ArrayList<ScheduleDTO>();
					List<RouteDTO> overrideRouteList = new ArrayList<>();
					if (StringUtil.isNotNull(overrideScheduleCodes)) {
						overrideScheduleList = getScheduleList(overrideScheduleCodes);
						overrideRouteList = converRouteList(overrideScheduleCodes);
					}
					overrideScheduleTicketTransferTermsDTO.setScheduleList(overrideScheduleList);
					overrideScheduleTicketTransferTermsDTO.setRouteList(overrideRouteList);

					// Group
					String overrideGroupCodes = lookupRS.getString("group_codes");
					List<GroupDTO> overrideGroupList = new ArrayList<GroupDTO>();
					if (StringUtil.isNotNull(overrideGroupCodes)) {
						overrideGroupList = getGroupList(overrideGroupCodes);
					}
					overrideScheduleTicketTransferTermsDTO.setGroupList(overrideGroupList);
					
					overrideScheduleTicketTransferTermsDTO.setAllowBookedUser(lookupRS.getInt("allow_booked_user_flag"));

					String overrideBookedGroupCodes = lookupRS.getString("booked_group_codes");
					List<GroupDTO> overrideBookedGroupList = new ArrayList<GroupDTO>();
					if (StringUtil.isNotNull(overrideBookedGroupCodes)) {
						overrideBookedGroupList = getGroupList(overrideBookedGroupCodes);
					}
					overrideScheduleTicketTransferTermsDTO.setBookedUserGroups(overrideBookedGroupList);

					overrideScheduleTicketTransferTermsDTO.setChargeAmount(lookupRS.getBigDecimal("transfer_charge_amount"));
					overrideScheduleTicketTransferTermsDTO.setChargeType(FareTypeEM.getFareTypeEM(lookupRS.getInt("transfer_charge_type")));
					overrideScheduleTicketTransferTermsDTO.setActiveFrom(new DateTime(lookupRS.getString("active_from")));
					overrideScheduleTicketTransferTermsDTO.setActiveTo(new DateTime(lookupRS.getString("active_to")));
					overrideScheduleTicketTransferTermsDTO.setDayOfWeek(lookupRS.getString("day_of_week"));
					overrideScheduleTicketTransferTermsDTO.setLookupCode(lookupRS.getString("lookup_id"));
					overrideScheduleTicketTransferTermsDTO.setActiveFlag(lookupRS.getInt("active_flag"));
					overrideList.add(overrideScheduleTicketTransferTermsDTO);
				}
				ticketTransferTermsDTO.setOverrideList(overrideList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return ticketTransferTermsDTO;
	}

	public List<ScheduleTicketTransferTermsDTO> getAllScheduleTicketTransferTerms(AuthDTO authDTO) {
		List<ScheduleTicketTransferTermsDTO> overrideList = new ArrayList<ScheduleTicketTransferTermsDTO>();
		Map<Integer, ScheduleTicketTransferTermsDTO> scheduleTermsMap = new HashMap<Integer, ScheduleTicketTransferTermsDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, minutes, minute_type_id, schedule_codes, group_codes, transfer_charge_amount, transfer_charge_type, active_from, active_to, day_of_week, allow_booked_user_flag, booked_group_codes, lookup_id, active_flag FROM schedule_ticket_transfer_terms WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO = new ScheduleTicketTransferTermsDTO();
				scheduleTicketTransferTermsDTO.setId(selectRS.getInt("id"));
				scheduleTicketTransferTermsDTO.setCode(selectRS.getString("code"));
				scheduleTicketTransferTermsDTO.setMinutes(selectRS.getInt("minutes"));
				scheduleTicketTransferTermsDTO.setMinutesType(MinutesTypeEM.getMinutesTypeEM(selectRS.getInt("minute_type_id")));
				// Schedule & Route
				String scheduleCodes = selectRS.getString("schedule_codes");

				List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
				List<RouteDTO> routeList = new ArrayList<>();
				if (StringUtil.isNotNull(scheduleCodes)) {
					scheduleList = getScheduleList(scheduleCodes);
					routeList = converRouteList(scheduleCodes);
				}
				scheduleTicketTransferTermsDTO.setScheduleList(scheduleList);
				scheduleTicketTransferTermsDTO.setRouteList(routeList);

				// Group
				String groupCodes = selectRS.getString("group_codes");
				List<GroupDTO> groupList = new ArrayList<GroupDTO>();
				if (StringUtil.isNotNull(groupCodes)) {
					groupList = getGroupList(groupCodes);
				}
				scheduleTicketTransferTermsDTO.setGroupList(groupList);

				scheduleTicketTransferTermsDTO.setChargeAmount(selectRS.getBigDecimal("transfer_charge_amount"));
				scheduleTicketTransferTermsDTO.setChargeType(FareTypeEM.getFareTypeEM(selectRS.getInt("transfer_charge_type")));
				scheduleTicketTransferTermsDTO.setActiveFrom(new DateTime(selectRS.getString("active_from")));
				scheduleTicketTransferTermsDTO.setActiveTo(new DateTime(selectRS.getString("active_to")));
				scheduleTicketTransferTermsDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleTicketTransferTermsDTO.setLookupCode(selectRS.getString("lookup_id"));
				scheduleTicketTransferTermsDTO.setActiveFlag(selectRS.getInt("active_flag"));
				scheduleTicketTransferTermsDTO.setAllowBookedUser(selectRS.getInt("allow_booked_user_flag"));

				String bookedGroupCodes = selectRS.getString("booked_group_codes");
				List<GroupDTO> bookedGroupList = new ArrayList<GroupDTO>();
				if (StringUtil.isNotNull(bookedGroupCodes)) {
					bookedGroupList = getGroupList(bookedGroupCodes);
				}
				scheduleTicketTransferTermsDTO.setBookedUserGroups(bookedGroupList);
				
				if (scheduleTicketTransferTermsDTO.getLookupCode().equals("0")) {
					scheduleTermsMap.put(scheduleTicketTransferTermsDTO.getId(), scheduleTicketTransferTermsDTO);
				}
				else {
					overrideList.add(scheduleTicketTransferTermsDTO);
				}
			}
			for (ScheduleTicketTransferTermsDTO overrideScheduleDTO : overrideList) {
				if (scheduleTermsMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleTicketTransferTermsDTO dto = scheduleTermsMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleTermsMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleTicketTransferTermsDTO>(scheduleTermsMap.values());
	}

	private List<ScheduleDTO> getScheduleList(String scheduleCodes) {
		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		List<String> scheduleCodeList = Arrays.asList(scheduleCodes.split(Text.COMMA));
		for (String scheduleCode : scheduleCodeList) {
			if (StringUtil.isNull(scheduleCode) || scheduleCode.split("_").length > Numeric.ONE_INT) {
				continue;
			}
			ScheduleDTO schedule = new ScheduleDTO();
			schedule.setCode(scheduleCode.trim());
			scheduleList.add(schedule);
		}
		return scheduleList;
	}

	private List<GroupDTO> getGroupList(String groupCodes) {
		List<GroupDTO> groupList = new ArrayList<GroupDTO>();
		if (StringUtil.isNotNull(groupCodes)) {
			List<String> groupCodeList = Arrays.asList(groupCodes.split(Text.COMMA));
			for (String groupCode : groupCodeList) {
				if (StringUtil.isNull(groupCode)) {
					continue;
				}
				GroupDTO group = new GroupDTO();
				group.setCode(groupCode.trim());
				groupList.add(group);
			}
		}
		return groupList;
	}

	private List<RouteDTO> converRouteList(String routes) {
		List<RouteDTO> routeList = new ArrayList<RouteDTO>();
		if (StringUtil.isNotNull(routes)) {
			for (String stationIds : routes.split(",")) {
				if (StringUtil.isNull(stationIds) || stationIds.split("_").length <= Numeric.ONE_INT) {
					continue;
				}
				int fromStationId = Integer.valueOf(stationIds.split("_")[0]);
				int toStationId = Integer.valueOf(stationIds.split("_")[1]);
				if (fromStationId == 0 || toStationId == 0) {
					continue;
				}
				RouteDTO routeDTO = new RouteDTO();
				StationDTO fromStation = new StationDTO();
				fromStation.setId(fromStationId);
				routeDTO.setFromStation(fromStation);

				StationDTO toStation = new StationDTO();
				toStation.setId(toStationId);
				routeDTO.setToStation(toStation);
				routeList.add(routeDTO);
			}
		}
		return routeList;
	}
}
