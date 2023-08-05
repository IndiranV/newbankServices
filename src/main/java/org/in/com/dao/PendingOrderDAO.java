package org.in.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.PendingOrderDTO;
import org.in.com.exception.ServiceException;

public class PendingOrderDAO {

	public PendingOrderDTO update(AuthDTO authDTO, PendingOrderDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("UPDATE payment_gateway_pre_transaction SET active_flag = 0 WHERE order_code = ?");
			ps.setString(1, dto.getOrderCode());
			ps.executeUpdate();
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return dto;
	}

}
