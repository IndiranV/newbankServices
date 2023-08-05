package org.in.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketTaxDTO;
import org.in.com.exception.ServiceException;

public class TicketTaxDAO extends BaseDAO {

	public void addTicketTax(AuthDTO authDTO, TicketDTO ticket, TicketTaxDTO ticketTax) {
		try {
			int psCount = 0;
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO ticket_tax (namespace_id, ticket_id, gstin, trade_name, email, active_flag, updated_by, updated_at) VALUES(?,?,?,?,?, 1,?,NOW())");
			ps.setInt(++psCount, authDTO.getNamespace().getId());
			ps.setInt(++psCount, ticket.getId());
			ps.setString(++psCount, ticketTax.getGstin());
			ps.setString(++psCount, ticketTax.getTradeName());
			ps.setString(++psCount, ticketTax.getEmail());
			ps.setInt(++psCount, authDTO.getUser().getId());
			ps.executeUpdate();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	public TicketTaxDTO getTicketTax(AuthDTO authDTO, TicketDTO ticket) {
		TicketTaxDTO ticketTax = new TicketTaxDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, gstin, trade_name, email, active_flag FROM ticket_tax WHERE namespace_id = ? AND ticket_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, ticket.getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				ticketTax.setId(selectRS.getInt("id"));
				ticketTax.setGstin(selectRS.getString("gstin"));
				ticketTax.setTradeName(selectRS.getString("trade_name"));
				ticketTax.setEmail(selectRS.getString("email"));
				ticketTax.setActiveFlag(selectRS.getInt("active_flag"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return ticketTax;
	}
}
