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
import org.in.com.dto.PaymentGatewayProviderDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;

public class PaymentGatewayProviderDAO {

	public List<PaymentGatewayProviderDTO> getAllPgProvider() {
		List<PaymentGatewayProviderDTO> list = new ArrayList<PaymentGatewayProviderDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code,name,service_name,active_flag FROM payment_gateway_provider where active_flag < 2");
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				PaymentGatewayProviderDTO pgDTO = new PaymentGatewayProviderDTO();
				pgDTO.setCode(selectRS.getString("code"));
				pgDTO.setName(selectRS.getString("name"));
				pgDTO.setServiceName(selectRS.getString("service_name"));
				pgDTO.setActiveFlag(selectRS.getInt("active_flag"));
				list.add(pgDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public PaymentGatewayProviderDTO getPgProviderUpdate(AuthDTO authDTO, PaymentGatewayProviderDTO pgProviderDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{CALL  EZEE_SP_PAYMENT_GATEWAY_PROVIDER_IUD( ?,?,?,?,? ,?,?)}");
			callableStatement.setString(++pindex, pgProviderDTO.getCode());
			callableStatement.setString(++pindex, pgProviderDTO.getName());
			callableStatement.setString(++pindex, pgProviderDTO.getServiceName());
			callableStatement.setInt(++pindex, pgProviderDTO.getActiveFlag());
			callableStatement.setInt(++pindex, authDTO.getUser().getId());
			callableStatement.setInt(++pindex, 0);
			callableStatement.registerOutParameter(++pindex, Types.INTEGER);
			callableStatement.execute();
			if (callableStatement.getInt("pitRowCount") > 0) {
				pgProviderDTO.setCode(callableStatement.getString("pcrCode"));
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return null;
	}

	public PaymentGatewayProviderDTO getPGDetails(int id) {
		PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT code, name, service_name, active_flag FROM payment_gateway_provider WHERE id = ? AND active_flag < 2");
			selectPS.setInt(1, id);
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				gatewayProviderDTO.setCode(selectRS.getString("code"));
				gatewayProviderDTO.setName(selectRS.getString("name"));
				gatewayProviderDTO.setServiceName(selectRS.getString("service_name"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
		return gatewayProviderDTO;
	}
}
