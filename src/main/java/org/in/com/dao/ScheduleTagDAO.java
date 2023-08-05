package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleTagDTO;
import org.in.com.exception.ServiceException;

import lombok.Cleanup;

public class ScheduleTagDAO {

	public List<ScheduleTagDTO> getAll(AuthDTO authDTO) {
		List<ScheduleTagDTO> list = new ArrayList<ScheduleTagDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, name, active_flag FROM schedule_tag WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleTagDTO scheduleTagDTO = new ScheduleTagDTO();
				scheduleTagDTO.setId(selectRS.getInt("id"));
				scheduleTagDTO.setCode(selectRS.getString("code"));
				scheduleTagDTO.setName(selectRS.getString("name"));
				scheduleTagDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(scheduleTagDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public ScheduleTagDTO getScheduleTagUpdate(AuthDTO authDTO, ScheduleTagDTO scheduleTag) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_SCHEDULE_TAG_IUD(?,?,?,?,? ,?,?)}");
			callableStatement.setString(++pindex, scheduleTag.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, scheduleTag.getName());
			callableStatement.setInt(++pindex, scheduleTag.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				scheduleTag.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return scheduleTag;
	}

	public ScheduleTagDTO getScheduleTag(AuthDTO authDTO, ScheduleTagDTO scheduleTagDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (scheduleTagDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT id, code, name, active_flag FROM schedule_tag WHERE namespace_id = ? AND id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, scheduleTagDTO.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT id, code, name, active_flag FROM schedule_tag WHERE namespace_id = ? AND code = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, scheduleTagDTO.getCode());
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				scheduleTagDTO.setId(selectRS.getInt("id"));
				scheduleTagDTO.setCode(selectRS.getString("code"));
				scheduleTagDTO.setName(selectRS.getString("name"));
				scheduleTagDTO.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return scheduleTagDTO;
	}
}
