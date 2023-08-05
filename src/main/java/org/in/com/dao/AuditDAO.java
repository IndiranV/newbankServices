package org.in.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.exception.ServiceException;

public class AuditDAO extends BaseDAO {
	public void addTicketJsonLog(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			int psCount = 0;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO audit_ticket_json_log (namespace_id, ticket_code, user_id, ticket_status_id, json_log, active_flag,updated_by,updated_at) VALUES (?,?,?,?,?,1,?,NOW())");
			ps.setInt(++psCount, authDTO.getNamespace().getId());
			ps.setString(++psCount, ticketDTO.getCode());
			ps.setInt(++psCount, authDTO.getUser().getId());
			ps.setInt(++psCount, ticketDTO.getTicketStatus().getId());
			ps.setString(++psCount, ticketDTO.toJSON());
			ps.setInt(++psCount, authDTO.getUser().getId());
			ps.executeUpdate();
		}
		catch (Exception e) {
			System.out.println(ticketDTO.toJSON());
			e.printStackTrace();
		}
	}

	public void insertTicketAudit(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			int psCount = 0;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO audit_ticket_log (namespace_id, ticket_code, user_id, ticket_status_id, event,device_medium, active_flag,updated_by,updated_at) VALUES (?,?,?,?,?,?,1,?,NOW())");
			ps.setInt(++psCount, authDTO.getNamespace().getId());
			ps.setString(++psCount, ticketDTO.getCode());
			ps.setInt(++psCount, authDTO.getUser().getId());
			ps.setInt(++psCount, ticketDTO.getTicketStatus().getId());
			ps.setString(++psCount, ticketDTO.getTicketEvent());
			ps.setInt(++psCount, authDTO.getDeviceMedium().getId());
			ps.setInt(++psCount, authDTO.getUser().getId());
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}
}
