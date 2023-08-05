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
import org.in.com.dto.PaymentModeDTO;
import org.in.com.exception.ServiceException;

public class PaymentModeDAO {

	public List<PaymentModeDTO> getAllPaymentMode() {
		List<PaymentModeDTO> list = new ArrayList<PaymentModeDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code,name,active_flag FROM payment_gateway_mode where active_flag < 2");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				PaymentModeDTO pgDTO = new PaymentModeDTO();
				pgDTO.setCode(selectRS.getString("code"));
				pgDTO.setName(selectRS.getString("name"));
				pgDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(pgDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public PaymentModeDTO getPaymentModeUpdate(AuthDTO authDTO, PaymentModeDTO dto) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_PAYMENT_GATEWAY_MODE_IUD(?,?,?,?,?,?)}");
			callableStatement.setString(++pindex, dto.getCode());
			callableStatement.setString(++pindex, dto.getName());
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
			throw new ServiceException(e.getMessage());
		}
		return null;

	}

}
