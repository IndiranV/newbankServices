package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleAuditLogDTO;
import org.in.com.exception.ServiceException;

import lombok.Cleanup;

public class ScheduleAuditDAO {

	public void addScheduleAudit(AuthDTO authDTO, List<ScheduleAuditLogDTO> scheduleLogDTOList) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_AUDIT_SCHEDULE_LOG(?,?,?,?,? ,?,?,?,?,?)}");
			for (ScheduleAuditLogDTO scheduleLogDTO : scheduleLogDTOList) {
				int pindex = 0;
				callableStatement.setString(++pindex, scheduleLogDTO.getCode());
				callableStatement.setString(++pindex, scheduleLogDTO.getScheduleCode());
				callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
				callableStatement.setString(++pindex, scheduleLogDTO.getTableName());
				callableStatement.setString(++pindex, scheduleLogDTO.getEvent());
				callableStatement.setInt(++pindex, scheduleLogDTO.getEventType().getId());
				callableStatement.setString(++pindex, scheduleLogDTO.getLog());
				callableStatement.setInt(++pindex, scheduleLogDTO.getActiveFlag());
				callableStatement.setInt(++pindex, authDTO.getUser().getId());
				callableStatement.setInt(++pindex, 0);
				callableStatement.execute();
				callableStatement.clearParameters();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}
}
