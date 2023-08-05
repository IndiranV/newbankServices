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
import org.in.com.dto.AuditEventDTO;
import org.in.com.dto.enumeration.AuditEventTypeEM;
import org.in.com.exception.ServiceException;

public class AuditEventDAO {
	public AuditEventDTO getNamespaceEventUpdate(AuthDTO authDTO, AuditEventDTO namespaceEvent) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL EZEE_SP_NAMESPACE_EVENT_ALERT_IUD(?,?,?,?,?, ?,?,?,?)}");
			callableStatement.setString(++pindex, namespaceEvent.getCode());
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setInt(++pindex, namespaceEvent.getNamespaceEventType().getId());
			callableStatement.setString(++pindex, namespaceEvent.getMobileNumber());
			callableStatement.setString(++pindex, namespaceEvent.getEmailId());
			callableStatement.setInt(++pindex, namespaceEvent.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				namespaceEvent.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return namespaceEvent;
	}

	public List<AuditEventDTO> getAll(AuthDTO authDTO) {
		List<AuditEventDTO> list = new ArrayList<AuditEventDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, event_type_id, mobile_number, email_id, active_flag FROM namespace_event_alert WHERE namespace_id = ? AND active_flag < 2");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				AuditEventDTO namespaceEvent = new AuditEventDTO();
				namespaceEvent.setCode(selectRS.getString("code"));
				namespaceEvent.setNamespaceEventType(AuditEventTypeEM.getNamespaceEventTypeEM(selectRS.getInt("event_type_id")));
				namespaceEvent.setMobileNumber(selectRS.getString("mobile_number"));
				namespaceEvent.setEmailId(selectRS.getString("email_id"));
				namespaceEvent.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(namespaceEvent);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public AuditEventDTO getNamespaceEvent(AuthDTO authDTO, AuditEventDTO namespaceEvent) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, event_type_id, mobile_number, email_id, active_flag FROM namespace_event_alert WHERE namespace_id = ? AND event_type_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, namespaceEvent.getNamespaceEventType().getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				namespaceEvent.setCode(selectRS.getString("code"));
				namespaceEvent.setNamespaceEventType(AuditEventTypeEM.getNamespaceEventTypeEM(selectRS.getInt("event_type_id")));
				namespaceEvent.setMobileNumber(selectRS.getString("mobile_number"));
				namespaceEvent.setEmailId(selectRS.getString("email_id"));
				namespaceEvent.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return namespaceEvent;
	}
}
