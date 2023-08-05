package org.in.com.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.PaymentGatewayCredentialsDTO;
import org.in.com.dto.PaymentGatewayPartnerDTO;
import org.in.com.dto.PaymentGatewayProviderDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class PaymentGatewayTransactionDAO {

	public void insert(AuthDTO authDTO, PaymentGatewayTransactionDTO gatewayTransaction) throws Exception {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			// TODO hard coded ticket_id
			insert(connection, authDTO, gatewayTransaction);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void insert(Connection connection, AuthDTO authDTO, PaymentGatewayTransactionDTO gatewayTransaction) throws Exception {
		int index = 1;
		try {
			// TODO hard coded ticket_id
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("insert into payment_gateway_transaction (code,namespace_id,user_id,payment_gateway_partner_id,payment_gateway_provider_id,payment_gateway_merchant_credentials_id,gateway_transaction_id,amount,order_code,service_charge,payment_gateway_transaction_type_id,device_medium,active_flag,updated_by,updated_at) values (?,?,?,?,?,?,?,?,?,?,?,?,1,?,NOW())");
			preparedStatement.setString(index++, gatewayTransaction.getCode());
			preparedStatement.setInt(index++, authDTO.getNamespace().getId());
			preparedStatement.setInt(index++, gatewayTransaction.getUser().getId());
			preparedStatement.setInt(index++, gatewayTransaction.getGatewayPartner().getId());
			preparedStatement.setInt(index++, gatewayTransaction.getGatewayProvider().getId());
			preparedStatement.setInt(index++, gatewayTransaction.getGatewayCredentials().getId());
			preparedStatement.setString(index++, StringUtil.isNotNull(gatewayTransaction.getGatewayTransactionCode()) ? gatewayTransaction.getGatewayTransactionCode() : "NA");
			preparedStatement.setBigDecimal(index++, gatewayTransaction.getAmount());
			preparedStatement.setString(index++, gatewayTransaction.getOrderCode());
			preparedStatement.setBigDecimal(index++, gatewayTransaction.getServiceCharge() != null ? gatewayTransaction.getServiceCharge() : new BigDecimal(0));
			preparedStatement.setInt(index++, gatewayTransaction.getTransactionType().getId());
			preparedStatement.setInt(index++, gatewayTransaction.getDeviceMedium().getId());
			preparedStatement.setInt(index++, authDTO.getUser().getId());
			preparedStatement.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void checkAndInsert(AuthDTO authDTO, PaymentGatewayTransactionDTO gatewayTransaction) {

		int index = 1;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement findStatement = connection.prepareStatement(" SELECT 1 FROM payment_gateway_transaction WHERE code = ? ANd namespace_id = ?");
			findStatement.setString(1, gatewayTransaction.getCode());
			findStatement.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet resultSet = findStatement.executeQuery();
			if (!resultSet.next()) {
				@Cleanup
				PreparedStatement preparedStatement = connection.prepareStatement("insert into payment_gateway_transaction (code,namespace_id,user_id,payment_gateway_partner_id,payment_gateway_provider_id,payment_gateway_merchant_credentials_id,gateway_transaction_id,amount,order_code,service_charge,payment_gateway_transaction_type_id,device_medium,active_flag,updated_by,updated_at) values (?,?,?,?,?,?,?,?,?,?,?,?,1,?,NOW())");
				preparedStatement.setString(index++, gatewayTransaction.getCode());
				preparedStatement.setInt(index++, authDTO.getNamespace().getId());
				preparedStatement.setInt(index++, gatewayTransaction.getUser().getId());
				preparedStatement.setInt(index++, gatewayTransaction.getGatewayPartner().getId());
				preparedStatement.setInt(index++, gatewayTransaction.getGatewayProvider().getId());
				preparedStatement.setInt(index++, gatewayTransaction.getGatewayCredentials().getId());
				preparedStatement.setString(index++, StringUtil.isNotNull(gatewayTransaction.getGatewayTransactionCode()) ? gatewayTransaction.getGatewayTransactionCode() : "NA");
				preparedStatement.setBigDecimal(index++, gatewayTransaction.getAmount());
				preparedStatement.setString(index++, gatewayTransaction.getOrderCode());
				preparedStatement.setBigDecimal(index++, gatewayTransaction.getServiceCharge() != null ? gatewayTransaction.getServiceCharge() : new BigDecimal(0));
				preparedStatement.setInt(index++, gatewayTransaction.getTransactionType().getId());
				preparedStatement.setInt(index++, gatewayTransaction.getDeviceMedium().getId());
				preparedStatement.setInt(index++, authDTO.getUser().getId());
				preparedStatement.executeUpdate();
			}
			else {
				throw new ServiceException(ErrorCode.TRANSACTION_ALREADY_SUCCESS);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean getTransactionStatus(AuthDTO authDTO, String referenceCode) {
		boolean transactionStatus = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT 1 from payment_gateway_transaction WHERE namespace_id = ? AND order_code = ? AND active_flag = 1");
			preparedStatement.setInt(1, authDTO.getNamespace().getId());
			preparedStatement.setString(2, referenceCode);
			@Cleanup
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				transactionStatus = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return transactionStatus;
	}

	public PaymentGatewayTransactionDTO getPaymentGatewayTransaction(AuthDTO authDTO, String orderCode, PaymentGatewayTransactionTypeEM transactionTypeEM) {
		PaymentGatewayTransactionDTO transactionDTO = new PaymentGatewayTransactionDTO();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT code,user_id,payment_gateway_partner_id,payment_gateway_provider_id,payment_gateway_merchant_credentials_id,gateway_transaction_id, amount,service_charge FROM payment_gateway_transaction WHERE namespace_id = ? AND order_code = ? AND payment_gateway_transaction_type_id = ?");
			preparedStatement.setInt(1, authDTO.getNamespace().getId());
			preparedStatement.setString(2, orderCode);
			preparedStatement.setInt(3, transactionTypeEM.getId());
			@Cleanup
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				transactionDTO.setAmount(resultSet.getBigDecimal("amount"));
				transactionDTO.setServiceCharge(resultSet.getBigDecimal("service_charge"));
				transactionDTO.setCode(resultSet.getString("code"));
				transactionDTO.setGatewayTransactionCode(resultSet.getString("gateway_transaction_id"));
				UserDTO userDTO = new UserDTO();
				userDTO.setId(resultSet.getInt("user_id"));
				transactionDTO.setUser(userDTO);
				PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
				gatewayProviderDTO.setId(resultSet.getInt("payment_gateway_provider_id"));
				PaymentGatewayCredentialsDTO gatewayCredentialsDTO = new PaymentGatewayCredentialsDTO();
				gatewayCredentialsDTO.setId(resultSet.getInt("payment_gateway_merchant_credentials_id"));
				PaymentGatewayPartnerDTO gatewayPartnerDTO = new PaymentGatewayPartnerDTO();
				gatewayPartnerDTO.setId(resultSet.getInt("payment_gateway_partner_id"));
				transactionDTO.setGatewayCredentials(gatewayCredentialsDTO);
				transactionDTO.setGatewayPartner(gatewayPartnerDTO);
				transactionDTO.setGatewayProvider(gatewayProviderDTO);
				transactionDTO.setTransactionType(transactionTypeEM);
			}
			else {
				transactionDTO.setAmount(BigDecimal.ZERO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return transactionDTO;
	}
	
	public List<PaymentGatewayTransactionDTO> getPaymentGatewayTransaction(AuthDTO authDTO, String orderCode) {
		List<PaymentGatewayTransactionDTO> list = new ArrayList<PaymentGatewayTransactionDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT code,user_id,payment_gateway_partner_id,payment_gateway_provider_id,payment_gateway_merchant_credentials_id,gateway_transaction_id, amount,service_charge,payment_gateway_transaction_type_id, device_medium FROM payment_gateway_transaction WHERE namespace_id = ? AND order_code = ? AND active_flag = 1");
			preparedStatement.setInt(1, authDTO.getNamespace().getId());
			preparedStatement.setString(2, orderCode);
			@Cleanup
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				PaymentGatewayTransactionDTO transactionDTO = new PaymentGatewayTransactionDTO();
				transactionDTO.setAmount(resultSet.getBigDecimal("amount"));
				transactionDTO.setServiceCharge(resultSet.getBigDecimal("service_charge"));
				transactionDTO.setCode(resultSet.getString("code"));
				transactionDTO.setGatewayTransactionCode(resultSet.getString("gateway_transaction_id"));
				UserDTO userDTO = new UserDTO();
				userDTO.setId(resultSet.getInt("user_id"));
				transactionDTO.setUser(userDTO);
				PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
				gatewayProviderDTO.setId(resultSet.getInt("payment_gateway_provider_id"));
				PaymentGatewayCredentialsDTO gatewayCredentialsDTO = new PaymentGatewayCredentialsDTO();
				gatewayCredentialsDTO.setId(resultSet.getInt("payment_gateway_merchant_credentials_id"));
				PaymentGatewayPartnerDTO gatewayPartnerDTO = new PaymentGatewayPartnerDTO();
				gatewayPartnerDTO.setId(resultSet.getInt("payment_gateway_partner_id"));
				transactionDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(resultSet.getInt("device_medium")));
				transactionDTO.setGatewayCredentials(gatewayCredentialsDTO);
				transactionDTO.setGatewayPartner(gatewayPartnerDTO);
				transactionDTO.setGatewayProvider(gatewayProviderDTO);
				transactionDTO.setTransactionType(PaymentGatewayTransactionTypeEM.getPaymentGatewayTransactionTypeDTO(resultSet.getInt("payment_gateway_transaction_type_id")));
				list.add(transactionDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void updateGatewayTransactionCode(AuthDTO authDTO, PaymentGatewayTransactionDTO gatewayTransactionDTO) throws Exception {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE payment_gateway_transaction set gateway_transaction_id = ?, updated_at = NOW() WHERE namespace_id = ? AND order_code = ?");
			preparedStatement.setString(1, gatewayTransactionDTO.getGatewayTransactionCode());
			preparedStatement.setInt(2, authDTO.getNamespace().getId());
			preparedStatement.setString(3, gatewayTransactionDTO.getCode());
			preparedStatement.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
