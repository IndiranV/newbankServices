package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.Cleanup;

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleBookGenderRestrictionDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class ScheduleBookGenderRestrictionDAO {

	public ScheduleBookGenderRestrictionDTO updateScheduleBookGenderRestriction(AuthDTO authDTO, ScheduleBookGenderRestrictionDTO scheduleBookGenderRestriction) {
		try {
			int pindex = 0;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_BOOK_GENDER_RESTRICTION_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?)}");
			callableStatement.setString(++pindex, scheduleBookGenderRestriction.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, scheduleBookGenderRestriction.getScheduleCodes());
			callableStatement.setString(++pindex, scheduleBookGenderRestriction.getGroupCodes());
			callableStatement.setString(++pindex, scheduleBookGenderRestriction.getDayOfWeek());
			callableStatement.setInt(++pindex, scheduleBookGenderRestriction.getReleaseMinutes());
			callableStatement.setInt(++pindex, scheduleBookGenderRestriction.getFemaleSeatCount());
			callableStatement.setInt(++pindex, scheduleBookGenderRestriction.getSeatTypeGroupModel());
			callableStatement.setInt(++pindex, scheduleBookGenderRestriction.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				scheduleBookGenderRestriction.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return scheduleBookGenderRestriction;
	}

	public List<ScheduleBookGenderRestrictionDTO> getScheduleBookGenderRestriction(AuthDTO authDTO) {
		List<ScheduleBookGenderRestrictionDTO> list = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, schedule_codes, group_codes, day_of_week, release_minutes, female_seat_count, seat_type_group_model, active_flag FROM schedule_book_gender_restriction WHERE namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleBookGenderRestrictionDTO scheduleBookGenderRestrictionDTO = new ScheduleBookGenderRestrictionDTO();
				scheduleBookGenderRestrictionDTO.setCode(selectRS.getString("code"));
				scheduleBookGenderRestrictionDTO.setScheduleList(convertScheduleList(selectRS.getString("schedule_codes")));
				scheduleBookGenderRestrictionDTO.setGroupList(convertGroupList(selectRS.getString("group_codes")));
				scheduleBookGenderRestrictionDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleBookGenderRestrictionDTO.setReleaseMinutes(selectRS.getInt("release_minutes"));
				scheduleBookGenderRestrictionDTO.setFemaleSeatCount(selectRS.getInt("female_seat_count"));
				scheduleBookGenderRestrictionDTO.setSeatTypeGroupModel(selectRS.getInt("seat_type_group_model"));
				scheduleBookGenderRestrictionDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(scheduleBookGenderRestrictionDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	private List<ScheduleDTO> convertScheduleList(String Schedules) {
		List<ScheduleDTO> scheduleList = new ArrayList<>();
		if (StringUtil.isNotNull(Schedules)) {
			List<String> scheduleCodes = Arrays.asList(Schedules.split(Text.COMMA));
			if (scheduleCodes != null) {
				for (String scheduleCode : scheduleCodes) {
					if (StringUtil.isNull(scheduleCode)) {
						continue;
					}
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setCode(scheduleCode);
					scheduleList.add(scheduleDTO);
				}
			}
		}
		return scheduleList;
	}

	private List<GroupDTO> convertGroupList(String groups) {
		List<GroupDTO> groupList = new ArrayList<>();
		if (StringUtil.isNotNull(groups)) {
			List<String> groupCodes = Arrays.asList(groups.split(Text.COMMA));
			if (groupCodes != null) {
				for (String groupCode : groupCodes) {
					if (StringUtil.isNull(groupCode)) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setCode(groupCode);
					groupList.add(groupDTO);
				}
			}
		}
		return groupList;
	}
}
