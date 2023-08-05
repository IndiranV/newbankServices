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
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StationDTO;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class ScheduleControlDAO {

	public List<ScheduleControlDTO> get(AuthDTO authDTO, ScheduleControlDTO controlDTO) {
		List<ScheduleControlDTO> overrideList = new ArrayList<ScheduleControlDTO>();
		Map<Integer, ScheduleControlDTO> scheduleMap = new HashMap<Integer, ScheduleControlDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT con.id,con.code,con.open_minutes,con.close_minutes,con.active_from,con.active_to,con.day_of_week,con.lookup_id,con.active_flag,con.user_group_id,sche.code,con.allow_booking_flag,con.from_station_id,con.to_station_id FROM schedule_control con,schedule sche WHERE con.schedule_id = sche.id AND con.namespace_id = ? AND sche.code = ? AND  con.active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, controlDTO.getSchedule().getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleControlDTO dto = new ScheduleControlDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setAllowBookingFlag(selectRS.getInt("con.allow_booking_flag"));

				dto.setLookupCode(selectRS.getString("con.lookup_id"));
				dto.setActiveFrom(selectRS.getString("active_from"));
				dto.setActiveTo(selectRS.getString("active_to"));
				dto.setDayOfWeek(selectRS.getString("day_of_week"));
				dto.setOpenMinitues(selectRS.getInt("con.open_minutes"));
				dto.setCloseMinitues(selectRS.getInt("con.close_minutes"));
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(selectRS.getString("sche.code"));
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("con.user_group_id"));
				dto.setGroup(groupDTO);
				dto.setSchedule(scheduleDTO);
				// Stage based booking open/close
				int fromStationId = selectRS.getInt("con.from_station_id");
				int toStationId = selectRS.getInt("con.to_station_id");
				if (fromStationId != 0 && toStationId != 0) {
					StationDTO fromStationDTO = new StationDTO();
					StationDTO toStationDTO = new StationDTO();
					fromStationDTO.setId(fromStationId);
					toStationDTO.setId(toStationId);
					dto.setFromStation(fromStationDTO);
					dto.setToStation(toStationDTO);
				}
				// OverRides
				if (dto.getLookupCode().equals("0")) {
					scheduleMap.put(dto.getId(), dto);
				}
				else {
					overrideList.add(dto);
				}
			}
			for (ScheduleControlDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleControlDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleControlDTO>(scheduleMap.values());
	}

	public ScheduleControlDTO getIUD(AuthDTO authDTO, ScheduleControlDTO ScheduleStageControlDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call   EZEE_SP_SCHEDULE_CONTROL_IUD(?,?,?,?,? ,?,?,?,?,? ,?,?,?,?,? ,?,?)}");
			for (ScheduleControlDTO stageDTO : ScheduleStageControlDTO.getList()) {
				pindex = 0;
				callableStatement.setString(++pindex, stageDTO.getCode());
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, stageDTO.getSchedule().getCode());
				callableStatement.setString(++pindex, stageDTO.getGroup() != null ? stageDTO.getGroup().getCode() : null);
				callableStatement.setInt(++pindex, stageDTO.getOpenMinitues());
				callableStatement.setInt(++pindex, stageDTO.getCloseMinitues());
				callableStatement.setString(++pindex, stageDTO.getActiveFrom() != null ? stageDTO.getActiveFrom().trim() : null);
				callableStatement.setString(++pindex, stageDTO.getActiveTo() != null ? stageDTO.getActiveTo().trim() : null);
				callableStatement.setString(++pindex, stageDTO.getDayOfWeek());
				callableStatement.setInt(++pindex, stageDTO.getAllowBookingFlag());
				callableStatement.setInt(++pindex, stageDTO.getFromStation() != null ? stageDTO.getFromStation().getId() : 0);
				callableStatement.setInt(++pindex, stageDTO.getToStation() != null ? stageDTO.getToStation().getId() : 0);
				callableStatement.setString(++pindex, stageDTO.getLookupCode());
				callableStatement.setInt(++pindex, stageDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				if (callableStatement.getInt("pitRowCount") > 0) {
					ScheduleStageControlDTO.setActiveFlag(callableStatement.getInt("pitRowCount"));
					ScheduleStageControlDTO.setCode(callableStatement.getString("pcrCode"));
				}
				callableStatement.clearParameters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return ScheduleStageControlDTO;
	}

	public List<ScheduleControlDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleControlDTO> overrideList = new ArrayList<ScheduleControlDTO>();
		Map<Integer, ScheduleControlDTO> scheduleMap = new HashMap<Integer, ScheduleControlDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement(" SELECT con.id,con.code,con.open_minutes,con.close_minutes,con.active_from,con.active_to,con.day_of_week,con.lookup_id,con.active_flag,con.user_group_id, con.allow_booking_flag, con.from_station_id, con.to_station_id FROM schedule_control con WHERE con.namespace_id = ? AND con.schedule_id = ? AND con.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, scheduleDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleControlDTO dto = new ScheduleControlDTO();
				dto.setId(selectRS.getInt("id"));
				dto.setCode(selectRS.getString("code"));
				dto.setActiveFlag(selectRS.getInt("active_flag"));
				dto.setAllowBookingFlag(selectRS.getInt("con.allow_booking_flag"));
				dto.setLookupCode(selectRS.getString("lookup_id"));
				dto.setActiveFrom(selectRS.getString("active_from"));
				dto.setActiveTo(selectRS.getString("active_to"));
				dto.setDayOfWeek(selectRS.getString("day_of_week"));
				dto.setOpenMinitues(selectRS.getInt("con.open_minutes"));
				dto.setCloseMinitues(selectRS.getInt("con.close_minutes"));
				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("con.user_group_id"));
				dto.setGroup(groupDTO);
				dto.setSchedule(scheduleDTO);

				// Stage based booking open/close
				int fromStationId = selectRS.getInt("con.from_station_id");
				int toStationId = selectRS.getInt("con.to_station_id");
				if (fromStationId != 0 && toStationId != 0) {
					StationDTO fromStationDTO = new StationDTO();
					StationDTO toStationDTO = new StationDTO();
					fromStationDTO.setId(fromStationId);
					toStationDTO.setId(toStationId);
					dto.setFromStation(fromStationDTO);
					dto.setToStation(toStationDTO);
				}
				if (StringUtil.isNull(dto.getActiveFrom()) && StringUtil.isNull(dto.getActiveTo())) {
					dto.setActiveFrom(scheduleDTO.getActiveFrom());
					dto.setActiveTo(scheduleDTO.getActiveTo());
				}
				if (StringUtil.isNull(dto.getDayOfWeek())) {
					dto.setDayOfWeek(scheduleDTO.getDayOfWeek());
				}
				// Overrides
				if (dto.getLookupCode().equals("0")) {
					scheduleMap.put(dto.getId(), dto);
				}
				else {
					overrideList.add(dto);
				}
			}
			for (ScheduleControlDTO overrideScheduleDTO : overrideList) {
				if (scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode())) != null) {
					ScheduleControlDTO dto = scheduleMap.get(Integer.parseInt(overrideScheduleDTO.getLookupCode()));
					dto.getOverrideList().add(overrideScheduleDTO);
					scheduleMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return new ArrayList<ScheduleControlDTO>(scheduleMap.values());
	}
}
