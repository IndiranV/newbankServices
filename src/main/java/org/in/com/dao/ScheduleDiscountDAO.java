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

import lombok.Cleanup;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDiscountDTO;
import org.in.com.dto.enumeration.AuthenticationTypeEM;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class ScheduleDiscountDAO {

	public List<ScheduleDiscountDTO> getScheduleDiscount(AuthDTO authDTO) {
		List<ScheduleDiscountDTO> overrideList = new ArrayList<ScheduleDiscountDTO>();
		Map<Integer, ScheduleDiscountDTO> scheduleMap = new HashMap<Integer, ScheduleDiscountDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, schedule_codes, discount_value, group_codes, percentage_flag, active_from, active_to, after_booking_minitues, day_of_week, date_type, device_medium_id, authentication_type_id, female_discount_flag, advance_booking_days, lookup_id, active_flag FROM schedule_discount WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDiscountDTO scheduleDiscountDTO = new ScheduleDiscountDTO();
				scheduleDiscountDTO.setId(selectRS.getInt("id"));
				scheduleDiscountDTO.setCode(selectRS.getString("code"));
				scheduleDiscountDTO.setName(selectRS.getString("name"));
				scheduleDiscountDTO.setActiveFlag(selectRS.getInt("active_flag"));
				scheduleDiscountDTO.setLookupCode(selectRS.getString("lookup_id"));
				scheduleDiscountDTO.setActiveFrom(selectRS.getString("active_from"));
				scheduleDiscountDTO.setActiveTo(selectRS.getString("active_to"));
				scheduleDiscountDTO.setAfterBookingMinutes(selectRS.getInt("after_booking_minitues"));
				scheduleDiscountDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleDiscountDTO.setDateType(DateTypeEM.getDateTypeEM(selectRS.getString("date_type")));
				scheduleDiscountDTO.setDiscountValue(selectRS.getBigDecimal("discount_value"));
				scheduleDiscountDTO.setPercentageFlag(selectRS.getInt("percentage_flag"));
				scheduleDiscountDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(selectRS.getInt("device_medium_id")));
				scheduleDiscountDTO.setAuthenticationType(AuthenticationTypeEM.getAuthenticationTypeEM(selectRS.getInt("authentication_type_id")));
				scheduleDiscountDTO.setFemaleDiscountFlag(selectRS.getInt("female_discount_flag"));
				scheduleDiscountDTO.setAdvanceBookingDays(selectRS.getInt("advance_booking_days"));

				String scheduleCodes = selectRS.getString("schedule_codes");

				List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
				if (StringUtil.isNotNull(scheduleCodes)) {
					scheduleList = getScheduleList(scheduleCodes);
				}
				scheduleDiscountDTO.setScheduleList(scheduleList);

				// Group
				String groupCodes = selectRS.getString("group_codes");
				List<GroupDTO> groupList = new ArrayList<GroupDTO>();
				if (StringUtil.isNotNull(groupCodes)) {
					groupList = getGroupList(groupCodes);
				}
				scheduleDiscountDTO.setGroupList(groupList);

				if (scheduleDiscountDTO.getLookupCode().equals("0")) {
					scheduleMap.put(scheduleDiscountDTO.getId(), scheduleDiscountDTO);
				}
				else {
					overrideList.add(scheduleDiscountDTO);
				}
			}
			for (ScheduleDiscountDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleDiscountDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleDiscountDTO>(scheduleMap.values());
	}

	public boolean getIUD(AuthDTO authDTO, ScheduleDiscountDTO scheduleDiscountDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_DISCOUNT_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,?, ?)}");
			callableStatement.setString(++pindex, scheduleDiscountDTO.getCode());
			callableStatement.setString(++pindex, scheduleDiscountDTO.getName());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, scheduleDiscountDTO.getSchedules());
			callableStatement.setBigDecimal(++pindex, scheduleDiscountDTO.getDiscountValue());
			callableStatement.setInt(++pindex, scheduleDiscountDTO.getPercentageFlag());
			callableStatement.setInt(++pindex, scheduleDiscountDTO.getAuthenticationType().getId());
			callableStatement.setInt(++pindex, scheduleDiscountDTO.getFemaleDiscountFlag());
			callableStatement.setInt(++pindex, scheduleDiscountDTO.getAdvanceBookingDays());
			callableStatement.setString(++pindex, scheduleDiscountDTO.getGroups());
			callableStatement.setString(++pindex, scheduleDiscountDTO.getActiveFrom());
			callableStatement.setString(++pindex, scheduleDiscountDTO.getActiveTo());
			callableStatement.setInt(++pindex, scheduleDiscountDTO.getAfterBookingMinutes());
			callableStatement.setString(++pindex, scheduleDiscountDTO.getDayOfWeek());
			callableStatement.setString(++pindex, scheduleDiscountDTO.getDateType() != null ? scheduleDiscountDTO.getDateType().getCode() : null);
			callableStatement.setInt(++pindex, scheduleDiscountDTO.getDeviceMedium().getId());
			callableStatement.setString(++pindex, scheduleDiscountDTO.getLookupCode());
			callableStatement.setInt(++pindex, scheduleDiscountDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				scheduleDiscountDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
				scheduleDiscountDTO.setCode(callableStatement.getString("pcrCode"));
			}
			callableStatement.clearParameters();
			scheduleDiscountDTO.setScheduleList(scheduleDiscountDTO.getScheduleList());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return true;
	}

	private List<ScheduleDTO> getScheduleList(String scheduleCodes) {
		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		List<String> scheduleCodeList = Arrays.asList(scheduleCodes.split(Text.COMMA));
		for (String scheduleCode : scheduleCodeList) {
			if (StringUtil.isNull(scheduleCode)) {
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
}
