package org.in.com.dao;

import java.sql.CallableStatement;
import java.sql.Connection;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;

public class BaseDAO {

	protected void addAuditLog(Connection connection, AuthDTO authDTO, String code, String tableName, String actionName, String logData) {

		try {
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_AUDIT_LOG(?,?,?,?,? ,?,?,?)}");
			callableStatement.setString(++pindex, code);
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, tableName);
			callableStatement.setString(++pindex, actionName);
			callableStatement.setString(++pindex, logData);
			callableStatement.setInt(++pindex, 1);
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void addAuditLog(AuthDTO authDTO, String code, String tableName, String actionName, String logData) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			addAuditLog(connection, authDTO, code, tableName, actionName, logData);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
