package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.UserDTO;
import org.in.com.exception.ServiceException;

public class ScheduleVisibilityDAO {

	public List<UserDTO> get(AuthDTO authDTO, String scheduleCode) {
		List<UserDTO> userList = new ArrayList<UserDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT usr.code,usr.first_name FROM schedule sche,schedule_visibility visi,user usr WHERE sche.namespace_id = ? AND sche.code = ? AND sche.id =visi.schedule_id AND usr.id = visi.user_id AND visi.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, scheduleCode);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(selectRS.getString("usr.code"));
				userDTO.setName(selectRS.getString("usr.first_name"));
				userList.add(userDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return userList;
	}

	public boolean getIUD(AuthDTO authDTO, String scheduleCode, List<UserDTO> userList) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE schedule_visibility,schedule  SET schedule_visibility.active_flag = 0 WHERE schedule.namespace_id = ? AND schedule.id = schedule_visibility.schedule_id AND schedule.code = ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, scheduleCode);
			selectPS.executeUpdate();
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call   EZEE_SP_SCHEDULE_VISIBILITY_IUD(?,?,?,? ,?,?)}");
			for (UserDTO dto : userList) {
				pindex = 0;
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, scheduleCode);
				callableStatement.setString(++pindex, dto.getCode());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.registerOutParameter(++pindex, Types.INTEGER);
				callableStatement.execute();
				callableStatement.clearParameters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return true;
	}

	public List<ScheduleDTO> getUserActiveSchedule(AuthDTO authDTO) {
		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT sche.id, sche.code FROM  schedule sche, schedule_visibility visi WHERE sche.id = visi.schedule_id AND sche.namespace_id = ? AND sche.active_flag = 1 AND visi.user_id = ? AND  visi.active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, authDTO.getUser().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(selectRS.getInt("id"));
				scheduleDTO.setCode(selectRS.getString("code"));
				scheduleList.add(scheduleDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
		return scheduleList;
	}
}
