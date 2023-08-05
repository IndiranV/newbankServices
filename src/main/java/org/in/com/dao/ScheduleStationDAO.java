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

import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.exception.ServiceException;

public class ScheduleStationDAO {

	public List<ScheduleStationDTO> get(AuthDTO authDTO, ScheduleStationDTO scheduleStageDTO) {
		List<ScheduleStationDTO> overrideList = new ArrayList<ScheduleStationDTO>();
		Map<Integer, ScheduleStationDTO> scheduleMap = new HashMap<Integer, ScheduleStationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT stage.id, stage.code,stage.active_from,stage.active_to,stage.station_id,stage.minitues,stage.day_of_week,stage.station_sequence, stage.mobile_number, stage.lookup_id,stage.active_flag FROM schedule sche, schedule_station stage WHERE sche.id = stage.schedule_id AND stage.namespace_id = ? AND sche.code = ? AND stage.active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, scheduleStageDTO.getSchedule().getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleStationDTO dto = new ScheduleStationDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setLookupCode(selectRS.getString("lookup_id"));
				dto.setActiveFrom(selectRS.getString("active_from"));
				dto.setActiveTo(selectRS.getString("active_to"));
				dto.setDayOfWeek(selectRS.getString("day_of_week"));
				StationDTO station = new StationDTO();
				station.setId(selectRS.getInt("station_id"));
				dto.setStation(station);
				dto.setSchedule(scheduleStageDTO.getSchedule());
				dto.setMinitues(selectRS.getInt("minitues"));
				dto.setMobileNumber(selectRS.getString("mobile_number"));
				dto.setStationSequence(selectRS.getInt("station_sequence"));
				if (dto.getLookupCode().equals("0")) {
					scheduleMap.put(dto.getId(), dto);
				}
				else {
					overrideList.add(dto);
				}
			}
			for (ScheduleStationDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleStationDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return new ArrayList<ScheduleStationDTO>(scheduleMap.values());
	}

	public ScheduleStationDTO getIUD(AuthDTO authDTO, ScheduleStationDTO scheduleStationDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call   EZEE_SP_SCHEDULE_STATION_IUD(?,?,?,?,? ,?,?,?,?,?, ?,?,?,?,?)}");
			for (ScheduleStationDTO stageDTO : scheduleStationDTO.getList()) {
				pindex = 0;
				callableStatement.setString(++pindex, stageDTO.getCode());
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, stageDTO.getSchedule() != null ? stageDTO.getSchedule().getCode() : null);
				callableStatement.setInt(++pindex, stageDTO.getStation() != null ? stageDTO.getStation().getId() : null);
				callableStatement.setString(++pindex, stageDTO.getActiveFrom() != null ? stageDTO.getActiveFrom().trim() : null);
				callableStatement.setString(++pindex, stageDTO.getActiveTo() != null ? stageDTO.getActiveTo().trim() : null);
				callableStatement.setString(++pindex, stageDTO.getDayOfWeek());
				callableStatement.setInt(++pindex, stageDTO.getStationSequence());
				callableStatement.setInt(++pindex, stageDTO.getMinitues());
				callableStatement.setString(++pindex, stageDTO.getMobileNumber());
				callableStatement.setString(++pindex, stageDTO.getLookupCode());
				callableStatement.setInt(++pindex, stageDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				if (callableStatement.getInt("pitRowCount") > 0) {
					scheduleStationDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
					scheduleStationDTO.setCode(callableStatement.getString("pcrCode"));
				}
				callableStatement.clearParameters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return scheduleStationDTO;
	}

	// get all trips
	public List<ScheduleStationDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleStationDTO> overrideList = new ArrayList<ScheduleStationDTO>();
		Map<Integer, ScheduleStationDTO> scheduleMap = new HashMap<Integer, ScheduleStationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT stage.id, stage.code,stage.active_from,stage.active_to,stage.station_id,stage.minitues,stage.day_of_week,stage.station_sequence, stage.mobile_number, stage.lookup_id,stage.active_flag FROM  schedule_station stage WHERE stage.namespace_id = ? AND stage.schedule_id = ? AND stage.active_flag  = 1 ORDER BY station_sequence ASC");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleStationDTO dto = new ScheduleStationDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setLookupCode(selectRS.getString("lookup_id"));
				dto.setActiveFrom(selectRS.getString("active_from"));
				dto.setActiveTo(selectRS.getString("active_to"));
				dto.setDayOfWeek(selectRS.getString("day_of_week"));
				StationDTO station = new StationDTO();
				station.setId(selectRS.getInt("station_id"));
				dto.setStation(station);
				dto.setSchedule(scheduleDTO);
				dto.setMinitues(selectRS.getInt("minitues"));
				dto.setMobileNumber(selectRS.getString("mobile_number"));
				dto.setStationSequence(selectRS.getInt("station_sequence"));

				// if (StringUtil.isNull(dto.getActiveFrom())) {
				// dto.setActiveFrom(scheduleDTO.getActiveFrom());
				// dto.setActiveTo(scheduleDTO.getActiveTo());
				// }
				// if (StringUtil.isNull(dto.getDayOfWeek())) {
				// dto.setDayOfWeek(scheduleDTO.getDayOfWeek());
				// }

				if (dto.getLookupCode().equals("0")) {
					scheduleMap.put(dto.getId(), dto);
				}
				else {
					overrideList.add(dto);
				}
			}
			for (ScheduleStationDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleStationDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleStationDTO>(scheduleMap.values());
	}

	public boolean CheckStationUsed(AuthDTO authDTO, StationDTO dto) {
		boolean status = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT sche.code ,sche.active_flag FROM schedule_station stage, schedule sche WHERE sche.namespace_id = stage.namespace_id AND stage.namespace_id = ? AND sche.id = stage.schedule_id AND stage.station_id = ? AND stage.lookup_id = 0 AND sche.active_flag = 1 AND stage.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, dto.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				status = true;
			}
		}

		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return status;
	}

	public boolean isStationUsed(AuthDTO authDTO, ScheduleDTO schedule, StationDTO station) {
		boolean stationUsed = Text.FALSE;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT 1 FROM ticket WHERE namespace_id = ? AND schedule_id = ? AND (from_station_id = ? OR to_station_id = ?) AND ticket_status_id IN (1, 5) AND trip_date >= DATE(NOW()) AND active_flag = 1 LIMIT 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, schedule.getId());
			selectPS.setInt(3, station.getId());
			selectPS.setInt(4, station.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				stationUsed = Text.TRUE;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return stationUsed;
	}

}
