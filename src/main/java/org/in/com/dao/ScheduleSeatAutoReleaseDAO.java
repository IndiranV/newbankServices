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
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatAutoReleaseDTO;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.ReleaseModeEM;
import org.in.com.dto.enumeration.ReleaseTypeEM;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import lombok.Cleanup;

public class ScheduleSeatAutoReleaseDAO {

	public List<ScheduleSeatAutoReleaseDTO> getAllScheduleSeatAutoRelease(AuthDTO authDTO) {
		List<ScheduleSeatAutoReleaseDTO> overrideList = new ArrayList<ScheduleSeatAutoReleaseDTO>();
		Map<Integer, ScheduleSeatAutoReleaseDTO> scheduleMap = new HashMap<Integer, ScheduleSeatAutoReleaseDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, schedule_id, group_id, active_from, active_to, day_of_week, release_minutes, minutes_type, release_mode, release_type, lookup_id, active_flag FROM schedule_seat_auto_release WHERE namespace_id = ? AND active_flag = 1 AND active_to >= ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, DateUtil.convertDate(DateUtil.minusDaysToDate(DateUtil.NOW(), 30)));

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleSeatAutoReleaseDTO dto = new ScheduleSeatAutoReleaseDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setLookupCode(selectRS.getString("lookup_id"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setActiveFrom(selectRS.getString("active_from"));
				dto.setActiveTo(selectRS.getString("active_to"));
				dto.setDayOfWeek(selectRS.getString("day_of_week"));
				dto.setReleaseMinutes(selectRS.getInt("release_minutes"));
				dto.setMinutesTypeEM(MinutesTypeEM.getMinutesTypeEM(selectRS.getInt("minutes_type")));
				dto.setReleaseModeEM(ReleaseModeEM.getReleaseModeEM(selectRS.getInt("release_mode")));
				dto.setReleaseTypeEM(ReleaseTypeEM.getReleaseTypeEM(selectRS.getInt("release_type")));

				String scheduleIds = selectRS.getString("schedule_id");
				dto.setSchedules(convertSchedules(scheduleIds));

				String groupIds = selectRS.getString("group_id");
				dto.setGroups(convertGroups(groupIds));

				if (dto.getLookupCode().equals("0")) {
					scheduleMap.put(dto.getId(), dto);
				}
				else {
					overrideList.add(dto);
				}
			}
			for (ScheduleSeatAutoReleaseDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleSeatAutoReleaseDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<ScheduleSeatAutoReleaseDTO>(scheduleMap.values());
	}

	public ScheduleSeatAutoReleaseDTO getIUD(AuthDTO authDTO, ScheduleSeatAutoReleaseDTO releaseDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_SEAT_AUTO_RELEASE_IUD(?,?,?,?,? ,?,?,?,?,?  ,?,?,?,?,? ,?)}");

			callableStatement.setString(++pindex, releaseDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, releaseDTO.getScheduleIds());
			callableStatement.setString(++pindex, releaseDTO.getGroupIds());
			callableStatement.setString(++pindex, releaseDTO.getActiveFrom());
			callableStatement.setString(++pindex, releaseDTO.getActiveTo());
			callableStatement.setString(++pindex, releaseDTO.getDayOfWeek());
			callableStatement.setInt(++pindex, releaseDTO.getReleaseMinutes());
			callableStatement.setInt(++pindex, releaseDTO.getMinutesTypeEM() != null ? releaseDTO.getMinutesTypeEM().getId() : 0);
			callableStatement.setInt(++pindex, releaseDTO.getReleaseModeEM() != null ? releaseDTO.getReleaseModeEM().getId() : 0);
			callableStatement.setInt(++pindex, releaseDTO.getReleaseTypeEM() != null ? releaseDTO.getReleaseTypeEM().getId() : 0);
			callableStatement.setString(++pindex, releaseDTO.getLookupCode());
			callableStatement.setInt(++pindex, releaseDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);

			callableStatement.registerOutParameter(++pindex, Types.INTEGER);

			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				releaseDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
				releaseDTO.setCode(callableStatement.getString("pcrCode"));
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return releaseDTO;
	}

	private List<GroupDTO> convertGroups(String groups) {
		List<GroupDTO> groupList = new ArrayList<>();
		if (StringUtil.isNotNull(groups)) {
			List<String> groupIds = Arrays.asList(groups.split(Text.COMMA));
			if (groupIds != null) {
				for (String groupId : groupIds) {
					if (StringUtil.isNull(groupId) || groupId.equals(Numeric.ZERO)) {
						continue;
					}
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(Integer.valueOf(groupId));
					groupList.add(groupDTO);
				}
			}
		}
		return groupList;
	}

	private List<ScheduleDTO> convertSchedules(String schedules) {
		List<ScheduleDTO> scheduleList = new ArrayList<>();
		if (StringUtil.isNotNull(schedules)) {
			List<String> scheduleIds = Arrays.asList(schedules.split(Text.COMMA));
			if (scheduleIds != null) {
				for (String scheduleId : scheduleIds) {
					if (StringUtil.isNull(scheduleId) || scheduleId.equals(Numeric.ZERO)) {
						continue;
					}
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setId(Integer.valueOf(scheduleId));
					scheduleList.add(scheduleDTO);
				}
			}
		}
		return scheduleList;
	}

}
