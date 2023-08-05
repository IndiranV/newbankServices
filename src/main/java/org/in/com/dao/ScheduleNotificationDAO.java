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
import org.in.com.dto.ScheduleNotificationDTO;
import org.in.com.exception.ServiceException;

public class ScheduleNotificationDAO {
	public List<ScheduleNotificationDTO> getAllNotifications(ScheduleDTO scheduleDTO) {
		List<ScheduleNotificationDTO> list = new ArrayList<ScheduleNotificationDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT schn.code, sch.code, sch.name, schn.mobile_number, schn.minutes, schn.active_flag FROM schedule_notification schn, schedule sch WHERE schn.schedule_id = sch.id AND schn.active_flag = 1 AND sch.code = ?");
			selectPS.setString(1, scheduleDTO.getCode());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				scheduleDTO.setCode(selectRS.getString("sch.code"));
				scheduleDTO.setName(selectRS.getString("sch.name"));
				ScheduleNotificationDTO scheduleNotificationDTO = new ScheduleNotificationDTO();
				scheduleNotificationDTO.setCode(selectRS.getString("schn.code"));
				scheduleNotificationDTO.setMobileNumber(selectRS.getString("schn.mobile_number"));
				scheduleNotificationDTO.setMinutes(selectRS.getInt("schn.minutes"));
				scheduleNotificationDTO.setActiveFlag(selectRS.getInt("schn.active_flag"));
				scheduleNotificationDTO.setSchedule(scheduleDTO);
				list.add(scheduleNotificationDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public ScheduleNotificationDTO getScheduleNotificationUID(AuthDTO authDTO, ScheduleNotificationDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call EZEE_SP_SCHEDULE_NOTIFICATION_IUD(?,?,?,?,? ,?,?,?,?)}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, dto.getSchedule() != null ? dto.getSchedule().getCode() : null);
			callableStatement.setString(++pindex, dto.getMobileNumber());
			callableStatement.setInt(++pindex, dto.getMinutes());
			callableStatement.setInt(++pindex, dto.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				dto.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return dto;
	}
}
