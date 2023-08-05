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

import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleVirtualSeatBlockDTO;

public class ScheduleVirtualSeatBlockDAO {
	public void updateScheduleVirtualSeatBlock(AuthDTO authDTO, ScheduleVirtualSeatBlockDTO scheduleVirtualSeatBlock) {
		try {
			int pindex = 0;

			StringBuilder occuapancyblockPercentage = new StringBuilder();
			for (String range : scheduleVirtualSeatBlock.getOccuapancyblockPercentage()) {
				occuapancyblockPercentage.append(range);
				occuapancyblockPercentage.append(",");
			}

			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			CallableStatement statement = connection.prepareCall("CALL EZEE_SP_SCHEDULE_VIRTUAL_SEAT_BLOCK_IUD(?,?,?,?,?, ?,?,?,?,?, ?,?,?,?)");
			statement.setString(++pindex, scheduleVirtualSeatBlock.getCode());
			statement.setInt(++pindex, authDTO.getNamespace().getId());
			statement.setString(++pindex, scheduleVirtualSeatBlock.getSchedule().getCode());
			statement.setString(++pindex, scheduleVirtualSeatBlock.getGroup().getCode());
			statement.setString(++pindex, occuapancyblockPercentage.toString());
			statement.setString(++pindex, scheduleVirtualSeatBlock.getActiveFrom());
			statement.setString(++pindex, scheduleVirtualSeatBlock.getActiveTo());
			statement.setString(++pindex, scheduleVirtualSeatBlock.getDayOfWeek());
			statement.setInt(++pindex, scheduleVirtualSeatBlock.getRefreshMinutes());
			statement.setString(++pindex, scheduleVirtualSeatBlock.getLookupCode());
			statement.setInt(++pindex, scheduleVirtualSeatBlock.getActiveFlag());
			statement.setInt(++pindex, authDTO.getUser().getId());
			statement.setInt(++pindex, 0);
			statement.registerOutParameter(++pindex, Types.INTEGER);
			statement.execute();
			if (statement.getInt("pitRowCount") > 0) {
				scheduleVirtualSeatBlock.setCode(statement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public List<ScheduleVirtualSeatBlockDTO> getScheduleVirtualSeatBlock(AuthDTO authDTO) {
		List<ScheduleVirtualSeatBlockDTO> exceptionList = new ArrayList<ScheduleVirtualSeatBlockDTO>();
		Map<Integer, ScheduleVirtualSeatBlockDTO> scheduleVirtualSeatBlockMap = new HashMap<Integer, ScheduleVirtualSeatBlockDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, schedule_codes, user_group_codes, occuapancy_block_percent, active_from, active_to, day_of_week, refresh_minutes, lookup_id, active_flag FROM schedule_virtual_seat_block WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleVirtualSeatBlockDTO scheduleVirtualSeatBlock = new ScheduleVirtualSeatBlockDTO();
				scheduleVirtualSeatBlock.setId(selectRS.getInt("id"));
				scheduleVirtualSeatBlock.setCode(selectRS.getString("code"));

				ScheduleDTO schedule = new ScheduleDTO();
				schedule.setCode(selectRS.getString("schedule_codes"));
				scheduleVirtualSeatBlock.setSchedule(schedule);

				GroupDTO group = new GroupDTO();
				group.setCode(selectRS.getString("user_group_codes"));
				scheduleVirtualSeatBlock.setGroup(group);

				String occuapancyBlockPercent = selectRS.getString("occuapancy_block_percent");
				List<String> occuapancyblockPercentage = new ArrayList<String>();
				for (String range : occuapancyBlockPercent.split(",")) {
					occuapancyblockPercentage.add(range);
				}
				scheduleVirtualSeatBlock.setOccuapancyblockPercentage(occuapancyblockPercentage);

				scheduleVirtualSeatBlock.setActiveFrom(selectRS.getString("active_from"));
				scheduleVirtualSeatBlock.setActiveTo(selectRS.getString("active_to"));
				scheduleVirtualSeatBlock.setDayOfWeek(selectRS.getString("day_of_week"));
				scheduleVirtualSeatBlock.setRefreshMinutes(selectRS.getInt("refresh_minutes"));
				scheduleVirtualSeatBlock.setLookupCode(selectRS.getString("lookup_id"));
				scheduleVirtualSeatBlock.setActiveFlag(selectRS.getInt("active_flag"));

				// Exception
				if ("0".equals(scheduleVirtualSeatBlock.getLookupCode())) {
					scheduleVirtualSeatBlockMap.put(scheduleVirtualSeatBlock.getId(), scheduleVirtualSeatBlock);
				}
				else {
					exceptionList.add(scheduleVirtualSeatBlock);
				}
			}

			for (ScheduleVirtualSeatBlockDTO virtualSeatBlock : exceptionList) {
				if (scheduleVirtualSeatBlockMap.get(Integer.parseInt(virtualSeatBlock.getLookupCode())) != null) {
					ScheduleVirtualSeatBlockDTO scheduleVirtualSeatBlockDTO = scheduleVirtualSeatBlockMap.get(Integer.parseInt(virtualSeatBlock.getLookupCode()));
					scheduleVirtualSeatBlockDTO.getExceptionList().add(virtualSeatBlock);
					scheduleVirtualSeatBlockMap.put(scheduleVirtualSeatBlockDTO.getId(), scheduleVirtualSeatBlockDTO);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<ScheduleVirtualSeatBlockDTO>(scheduleVirtualSeatBlockMap.values());
	}
}
