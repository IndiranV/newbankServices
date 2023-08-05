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
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleTimeOverrideDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.OverrideTypeEM;
import org.in.com.exception.ServiceException;

import hirondelle.date4j.DateTime;

public class ScheduleTimeOverrideDAO {

	public List<ScheduleTimeOverrideDTO> get(AuthDTO authDTO, ScheduleTimeOverrideDTO timeOverrideDTO) {
		List<ScheduleTimeOverrideDTO> overrideList = new ArrayList<ScheduleTimeOverrideDTO>();
		Map<Integer, ScheduleTimeOverrideDTO> scheduleMap = new HashMap<Integer, ScheduleTimeOverrideDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT seat.id,seat.code, seat.active_from,seat.active_to,seat.day_of_week,seat.station_id, seat.override_minutes, seat.override_type_id,seat.reaction_flag,seat.lookup_id,seat.active_flag,sche.code,sche.name, seat.updated_by, seat.updated_at FROM schedule_time_override seat,schedule sche WHERE sche.id = seat.schedule_id AND seat.namespace_id = ? AND sche.namespace_id = ? AND sche.code = ? AND seat.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setString(3, timeOverrideDTO.getSchedule().getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleTimeOverrideDTO overrideDTO = new ScheduleTimeOverrideDTO();
				overrideDTO.setCode(selectRS.getString("code"));
				overrideDTO.setId(selectRS.getInt("id"));
				overrideDTO.setActiveFlag(selectRS.getInt("active_flag"));
				overrideDTO.setLookupCode(selectRS.getString("lookup_id"));
				overrideDTO.setActiveFrom(selectRS.getString("active_from"));
				overrideDTO.setActiveTo(selectRS.getString("active_to"));
				overrideDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				overrideDTO.setOverrideMinutes(selectRS.getInt("override_minutes"));
				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(selectRS.getInt("station_id"));
				overrideDTO.setStation(stationDTO);

				overrideDTO.setReactionFlag(selectRS.getBoolean("reaction_flag"));
			
				UserDTO updatedUser = new UserDTO();
				updatedUser.setId(selectRS.getInt("updated_by"));
				overrideDTO.setUpdatedUser(updatedUser);

				overrideDTO.setUpdatedAt(new DateTime(selectRS.getString("updated_at")));
				overrideDTO.setOverrideType(OverrideTypeEM.getOverrideTypeEM(selectRS.getInt("override_type_id")));

				if (overrideDTO.getLookupCode().equals("0")) {
					scheduleMap.put(overrideDTO.getId(), overrideDTO);
				}
				else {
					overrideList.add(overrideDTO);
				}
			}
			for (ScheduleTimeOverrideDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleTimeOverrideDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleTimeOverrideDTO>(scheduleMap.values());

	}

	public ScheduleTimeOverrideDTO getIUD(AuthDTO authDTO, ScheduleTimeOverrideDTO timeOverrideDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call   EZEE_SP_SCHEDULE_TIME_OVERRIDE_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,? )}");

			callableStatement.setString(++pindex, timeOverrideDTO.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, timeOverrideDTO.getSchedule().getCode());
			callableStatement.setString(++pindex, timeOverrideDTO.getActiveFrom() != null ? timeOverrideDTO.getActiveFrom().trim() : null);
			callableStatement.setString(++pindex, timeOverrideDTO.getActiveTo() != null ? timeOverrideDTO.getActiveTo().trim() : null);
			callableStatement.setString(++pindex, timeOverrideDTO.getDayOfWeek());
			callableStatement.setInt(++pindex, timeOverrideDTO.getOverrideMinutes());
			callableStatement.setInt(++pindex, timeOverrideDTO.getStation() != null ? timeOverrideDTO.getStation().getId() : 0);
			callableStatement.setInt(++pindex, timeOverrideDTO.getOverrideType() != null ? timeOverrideDTO.getOverrideType().getId() : 0);
			callableStatement.setBoolean(++pindex, timeOverrideDTO.isReactionFlag());
			callableStatement.setString(++pindex, timeOverrideDTO.getLookupCode());
			callableStatement.setInt(++pindex, timeOverrideDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				timeOverrideDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
				timeOverrideDTO.setCode(callableStatement.getString("pcrCode"));
			}
			callableStatement.clearParameters();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return timeOverrideDTO;
	}

	public List<ScheduleTimeOverrideDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleTimeOverrideDTO> overrideList = new ArrayList<ScheduleTimeOverrideDTO>();
		Map<Integer, ScheduleTimeOverrideDTO> scheduleMap = new HashMap<Integer, ScheduleTimeOverrideDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id,code,active_from,active_to,day_of_week,override_minutes,station_id,override_type_id,reaction_flag,lookup_id, active_flag FROM schedule_time_override WHERE namespace_id = ? AND active_flag  = 1 AND schedule_id = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleTimeOverrideDTO overrideDTO = new ScheduleTimeOverrideDTO();
				overrideDTO.setId(selectRS.getInt("id"));
				overrideDTO.setCode(selectRS.getString("code"));
				overrideDTO.setActiveFlag(selectRS.getInt("active_flag"));
				overrideDTO.setLookupCode(selectRS.getString("lookup_id"));
				overrideDTO.setActiveFrom(selectRS.getString("active_from"));
				overrideDTO.setActiveTo(selectRS.getString("active_to"));
				overrideDTO.setDayOfWeek(selectRS.getString("day_of_week"));
				overrideDTO.setOverrideMinutes(selectRS.getInt("override_minutes"));
				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(selectRS.getInt("station_id"));
				overrideDTO.setStation(stationDTO);

				overrideDTO.setReactionFlag(selectRS.getBoolean("reaction_flag"));
				overrideDTO.setOverrideType(OverrideTypeEM.getOverrideTypeEM(selectRS.getInt("override_type_id")));

				if (overrideDTO.getLookupCode().equals("0")) {
					scheduleMap.put(overrideDTO.getId(), overrideDTO);
				}
				else {
					overrideList.add(overrideDTO);
				}
			}
			for (ScheduleTimeOverrideDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleTimeOverrideDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleTimeOverrideDTO>(scheduleMap.values());

	}
}
