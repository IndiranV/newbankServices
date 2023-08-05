package org.in.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import org.in.com.constants.Numeric;
import org.in.com.dto.AuthDTO;

import lombok.Cleanup;

public class TicketFailureLogDAO {

	public void insertFailureLog(AuthDTO authDTO, String erroCode, String message, String extras, String request) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO ticket_failure_log (namespace_id, user_id, error_code, event, extras, request, active_flag, updated_at) VALUES(?,?,?,?,? ,?,?,NOW())");
			ps.setInt(++pindex, authDTO.getNamespace().getId());
			ps.setInt(++pindex, authDTO.getUser().getId());
			ps.setString(++pindex, erroCode);
			ps.setString(++pindex, message);
			ps.setString(++pindex, extras);
			ps.setString(++pindex, request);
			ps.setInt(++pindex, Numeric.ONE_INT);
			ps.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
