package org.in.com.dao;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.OrderInitRequestDTO;
import org.in.com.dto.PaymentGatewayCredentialsDTO;
import org.in.com.dto.PaymentGatewayPartnerDTO;
import org.in.com.dto.PaymentGatewayProviderDTO;
import org.in.com.dto.PaymentPreTransactionDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

public class PaymentGatewayPreTransactionDAO extends BaseDAO {

	public PaymentPreTransactionDTO isTransactionExists(String transactionId, PaymentGatewayTransactionTypeEM transactionType) throws Exception {
		PaymentPreTransactionDTO preTransactionDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("select user_id,amount,service_charge,payment_gateway_partner_id,device_medium from payment_gateway_pre_transaction where code = ? and payment_gateway_transaction_type_id =?");
			preparedStatement.setString(1, transactionId);
			preparedStatement.setInt(2, transactionType.getId());
			@Cleanup
			ResultSet resultSet = preparedStatement.executeQuery();
			if (resultSet.next()) {
				preTransactionDTO = new PaymentPreTransactionDTO();
				preTransactionDTO.setAmount(resultSet.getBigDecimal("amount"));
				UserDTO userDTO = new UserDTO();
				userDTO.setId(resultSet.getInt("user_id"));
				preTransactionDTO.setUser(userDTO);
				preTransactionDTO.setServiceCharge(resultSet.getBigDecimal("service_charge"));
				PaymentGatewayPartnerDTO gatewayPartnerDTO = new PaymentGatewayPartnerDTO();
				preTransactionDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(resultSet.getInt("device_medium")));
				gatewayPartnerDTO.setId(resultSet.getInt("payment_gateway_partner_id"));
				preTransactionDTO.setGatewayPartner(gatewayPartnerDTO);
				preTransactionDTO.setCode(transactionId);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
		return preTransactionDTO;
	}

	public void insert(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDetails) throws Exception {
		int index = 1;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("insert into payment_gateway_pre_transaction (code,namespace_id,user_id,payment_gateway_partner_id,payment_gateway_provider_id,payment_gateway_merchant_credentials_id,order_code,amount,service_charge,payment_gateway_transaction_type_id,order_type_id,payment_gateway_status_id, device_medium, active_flag,updated_by,updated_at) values (?,?,?,?,?,?,?,?,?,?,?,?,?,1,?,NOW())");
			preparedStatement.setString(index++, preTransactionDetails.getCode());
			preparedStatement.setInt(index++, authDTO.getNamespace().getId());
			preparedStatement.setInt(index++, preTransactionDetails.getUser().getId());
			preparedStatement.setInt(index++, preTransactionDetails.getGatewayPartner().getId());
			preparedStatement.setInt(index++, preTransactionDetails.getGatewayProvider().getId());
			preparedStatement.setInt(index++, preTransactionDetails.getGatewayCredentials().getId());
			preparedStatement.setString(index++, preTransactionDetails.getOrderCode());
			preparedStatement.setBigDecimal(index++, preTransactionDetails.getAmount());
			preparedStatement.setBigDecimal(index++, preTransactionDetails.getServiceCharge());
			preparedStatement.setInt(index++, preTransactionDetails.getTransactionType().getId());
			preparedStatement.setInt(index++, preTransactionDetails.getOrderType().getId());
			preparedStatement.setInt(index++, preTransactionDetails.getStatus().getId());
			preparedStatement.setInt(index++, authDTO.getDeviceMedium().getId());
			preparedStatement.setInt(index++, authDTO.getUser().getId());
			preparedStatement.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void updateStatus(PaymentPreTransactionDTO preTransactionDTO) throws Exception {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			updateStatus(connection, preTransactionDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void updateStatus(Connection connection, PaymentPreTransactionDTO preTransactionDTO) throws Exception {
		try {
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("update payment_gateway_pre_transaction set payment_gateway_status_id =? ,failure_error_code=? ,updated_at= now() where code = ?");
			preparedStatement.setInt(1, preTransactionDTO.getStatus().getId());
			preparedStatement.setString(2, preTransactionDTO.getFailureErrorCode());
			preparedStatement.setString(3, preTransactionDTO.getCode());
			preparedStatement.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void updateStatusV2(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDTO) throws Exception {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE payment_gateway_pre_transaction SET payment_gateway_status_id = ?, failure_error_code = ?, updated_by = ?, updated_at = NOW() WHERE code = ?");
			preparedStatement.setInt(1, preTransactionDTO.getStatus().getId());
			preparedStatement.setString(2, preTransactionDTO.getFailureErrorCode());
			preparedStatement.setInt(3, authDTO.getUser().getId());
			preparedStatement.setString(4, preTransactionDTO.getCode());
			preparedStatement.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void updateGatewayTransactionCode(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDTO) throws Exception {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE payment_gateway_pre_transaction set gateway_transaction_id =?, updated_at= now() WHERE namespace_id = ? AND code = ?");
			preparedStatement.setString(1, preTransactionDTO.getGatewayTransactionCode());
			preparedStatement.setInt(2, authDTO.getNamespace().getId());
			preparedStatement.setString(3, preTransactionDTO.getCode());
			preparedStatement.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public void updatePaymentGatewayStatus(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDTO) throws Exception {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE payment_gateway_pre_transaction SET payment_gateway_status_id = ?, failure_error_code = ? WHERE namespace_id = ? AND code = ?");
			preparedStatement.setInt(1, preTransactionDTO.getStatus().getId());
			preparedStatement.setString(2, preTransactionDTO.getFailureErrorCode());
			preparedStatement.setInt(3, authDTO.getNamespace().getId());
			preparedStatement.setString(4, preTransactionDTO.getOrderCode());
			preparedStatement.executeUpdate();

			addAuditLog(connection, authDTO, preTransactionDTO.getOrderCode(), "payment_gateway_pre_transaction", "Ticket moved from failure to pending order", preTransactionDTO.getOrderCode() + "," + preTransactionDTO.getStatus().getCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public PaymentPreTransactionDTO CheckAndMakeTransaction(AuthDTO authDTO, OrderInitRequestDTO orderInitDetails) {
		PaymentPreTransactionDTO paymentPreTransactionDTO = null;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement checkTransactionStatusPS = connection.prepareStatement("SELECT 1  FROM payment_gateway_transaction WHERE namespace_id = ? AND code = ? AND payment_gateway_transaction_type_id = ? AND active_flag = 1");
			checkTransactionStatusPS.setInt(1, authDTO.getNamespace().getId());
			checkTransactionStatusPS.setString(2, orderInitDetails.getOrderCode());
			checkTransactionStatusPS.setInt(3, PaymentGatewayTransactionTypeEM.PAYMENT.getId());
			@Cleanup
			ResultSet checkRS = checkTransactionStatusPS.executeQuery();
			if (checkRS.next()) {
				throw new ServiceException(ErrorCode.TRANSACTION_ALREADY_SUCCESS);
			}
			@Cleanup
			PreparedStatement countPS = connection.prepareStatement("SELECT count(1) as transactionCount FROM payment_gateway_pre_transaction WHERE namespace_id = ? AND order_code = ? AND payment_gateway_status_id != ?");
			countPS.setInt(1, authDTO.getNamespace().getId());
			countPS.setString(2, orderInitDetails.getOrderCode());
			countPS.setInt(3, PaymentGatewayStatusEM.SUCCESS.getId());
			@Cleanup
			ResultSet countCheckRS = countPS.executeQuery();
			int transactionCount = 0;
			if (countCheckRS.next()) {
				transactionCount = countCheckRS.getInt("transactionCount");
			}
			if (transactionCount < 4) {
				paymentPreTransactionDTO = new PaymentPreTransactionDTO();
				paymentPreTransactionDTO.setOrderCode(orderInitDetails.getOrderCode());
				paymentPreTransactionDTO.setCode(orderInitDetails.getOrderCode() + (transactionCount > 0 ? StringUtil.getCharForNumber(transactionCount) : ""));
				if (orderInitDetails.getOrderType().getId() == TransactionTypeEM.TICKETS_BOOKING.getId() || orderInitDetails.getOrderType().getId() == TransactionTypeEM.RECHARGE.getId()) {
					paymentPreTransactionDTO.setTransactionType(PaymentGatewayTransactionTypeEM.PAYMENT);
				}
			}
			else {
				throw new ServiceException(ErrorCode.TRANSACTION_REACHED_MAX_RETRY);
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return paymentPreTransactionDTO;
	}

	public void getPreTransactionForTicket(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id, code,user_id,payment_gateway_partner_id,payment_gateway_provider_id,payment_gateway_merchant_credentials_id,order_type_id,amount,order_code,service_charge,payment_gateway_status_id,failure_error_code,device_medium,gateway_transaction_id FROM payment_gateway_pre_transaction WHERE namespace_id=? AND order_code=?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, preTransactionDTO.getOrderCode());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.last()) {
				preTransactionDTO.setId(rs.getInt("id"));
				preTransactionDTO.setCode(rs.getString("code"));
				preTransactionDTO.setOrderCode(rs.getString("order_code"));
				UserDTO userDTO = new UserDTO();
				userDTO.setId(rs.getInt("user_id"));
				preTransactionDTO.setUser(userDTO);
				PaymentGatewayPartnerDTO gatewayPartnerDTO = new PaymentGatewayPartnerDTO();
				PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
				PaymentGatewayCredentialsDTO gatewayCredentialsDTO = new PaymentGatewayCredentialsDTO();
				gatewayPartnerDTO.setId(rs.getInt("payment_gateway_partner_id"));
				gatewayProviderDTO.setId(rs.getInt("payment_gateway_provider_id"));
				gatewayCredentialsDTO.setId(rs.getInt("payment_gateway_merchant_credentials_id"));
				preTransactionDTO.setGatewayPartner(gatewayPartnerDTO);
				preTransactionDTO.setGatewayProvider(gatewayProviderDTO);
				preTransactionDTO.setGatewayCredentials(gatewayCredentialsDTO);
				preTransactionDTO.setOrderType(OrderTypeEM.getOrderTypeEM(rs.getInt("order_type_id")));
				preTransactionDTO.setAmount(rs.getBigDecimal("amount"));
				preTransactionDTO.setServiceCharge(rs.getBigDecimal("service_charge"));
				preTransactionDTO.setStatus(PaymentGatewayStatusEM.getStatusDTO(rs.getInt("payment_gateway_status_id")));
				preTransactionDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
				preTransactionDTO.setFailureErrorCode(rs.getString("failure_error_code"));
				preTransactionDTO.setGatewayTransactionCode(rs.getString("gateway_transaction_id"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void getPreTransactionForTicket(NamespaceDTO namespace, PaymentPreTransactionDTO preTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id, code, user_id, payment_gateway_partner_id,payment_gateway_provider_id,payment_gateway_merchant_credentials_id,order_type_id,amount,order_code,service_charge,gateway_transaction_id,failure_error_code,device_medium FROM payment_gateway_pre_transaction WHERE namespace_id=? AND order_code=?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ps.setInt(1, namespace.getId());
			ps.setString(2, preTransactionDTO.getOrderCode());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.last()) {
				preTransactionDTO.setId(rs.getInt("id"));
				preTransactionDTO.setCode(rs.getString("code"));
				preTransactionDTO.setOrderCode(rs.getString("order_code"));
				UserDTO userDTO = new UserDTO();
				userDTO.setId(rs.getInt("user_id"));
				preTransactionDTO.setUser(userDTO);
				PaymentGatewayPartnerDTO gatewayPartnerDTO = new PaymentGatewayPartnerDTO();
				PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
				PaymentGatewayCredentialsDTO gatewayCredentialsDTO = new PaymentGatewayCredentialsDTO();
				gatewayPartnerDTO.setId(rs.getInt("payment_gateway_partner_id"));
				gatewayProviderDTO.setId(rs.getInt("payment_gateway_provider_id"));
				gatewayCredentialsDTO.setId(rs.getInt("payment_gateway_merchant_credentials_id"));
				preTransactionDTO.setGatewayPartner(gatewayPartnerDTO);
				preTransactionDTO.setGatewayProvider(gatewayProviderDTO);
				preTransactionDTO.setGatewayCredentials(gatewayCredentialsDTO);
				preTransactionDTO.setOrderType(OrderTypeEM.getOrderTypeEM(rs.getInt("order_type_id")));
				preTransactionDTO.setAmount(rs.getBigDecimal("amount"));
				preTransactionDTO.setServiceCharge(rs.getBigDecimal("service_charge"));
				preTransactionDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
				preTransactionDTO.setGatewayTransactionCode(rs.getString("gateway_transaction_id"));
				preTransactionDTO.setFailureErrorCode(rs.getString("failure_error_code"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void getPreTransactionForTransaction(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT code,payment_gateway_partner_id,amount,order_code,order_type_id,service_charge,failure_error_code FROM payment_gateway_pre_transaction WHERE namespace_id=? AND order_code=?", ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, preTransactionDTO.getCode());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			if (rs.last()) {
				preTransactionDTO.setCode(rs.getString("code"));
				preTransactionDTO.setOrderCode(rs.getString("order_code"));
				PaymentGatewayPartnerDTO gatewayPartnerDTO = new PaymentGatewayPartnerDTO();
				gatewayPartnerDTO.setId(rs.getInt("payment_gateway_partner_id"));
				preTransactionDTO.setGatewayPartner(gatewayPartnerDTO);
				preTransactionDTO.setAmount(new BigDecimal(rs.getString("amount")));
				preTransactionDTO.setServiceCharge(new BigDecimal(rs.getString("service_charge")));
				preTransactionDTO.setFailureErrorCode(rs.getString("failure_error_code"));
				preTransactionDTO.setOrderType(OrderTypeEM.getOrderTypeEM(rs.getInt("order_type_id")));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<PaymentPreTransactionDTO> getPreTransactions(AuthDTO authDTO, DateTime fromDate, DateTime toDate) {
		List<PaymentPreTransactionDTO> transactions = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id, code, user_id, payment_gateway_partner_id, payment_gateway_provider_id, payment_gateway_merchant_credentials_id, order_type_id, amount, order_code, service_charge, failure_error_code, device_medium FROM payment_gateway_pre_transaction WHERE namespace_id = ? AND payment_gateway_status_id = 5 AND active_flag = 1 AND updated_at BETWEEN ? AND ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, DateUtil.convertDateTime(fromDate));
			ps.setString(3, DateUtil.convertDateTime(toDate));
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO();
				preTransactionDTO.setId(rs.getInt("id"));
				preTransactionDTO.setCode(rs.getString("code"));
				preTransactionDTO.setOrderCode(rs.getString("order_code"));

				UserDTO userDTO = new UserDTO();
				userDTO.setId(rs.getInt("user_id"));
				preTransactionDTO.setUser(userDTO);

				PaymentGatewayPartnerDTO gatewayPartnerDTO = new PaymentGatewayPartnerDTO();
				gatewayPartnerDTO.setId(rs.getInt("payment_gateway_partner_id"));
				preTransactionDTO.setGatewayPartner(gatewayPartnerDTO);

				PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
				gatewayProviderDTO.setId(rs.getInt("payment_gateway_provider_id"));
				preTransactionDTO.setGatewayProvider(gatewayProviderDTO);

				PaymentGatewayCredentialsDTO gatewayCredentialsDTO = new PaymentGatewayCredentialsDTO();
				gatewayCredentialsDTO.setId(rs.getInt("payment_gateway_merchant_credentials_id"));
				preTransactionDTO.setGatewayCredentials(gatewayCredentialsDTO);

				preTransactionDTO.setOrderType(OrderTypeEM.getOrderTypeEM(rs.getInt("order_type_id")));
				preTransactionDTO.setAmount(rs.getBigDecimal("amount"));
				preTransactionDTO.setServiceCharge(rs.getBigDecimal("service_charge"));
				preTransactionDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
				preTransactionDTO.setFailureErrorCode(rs.getString("failure_error_code"));
				transactions.add(preTransactionDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return transactions;
	}

	public void updateStatusV3(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDTO) throws Exception {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE payment_gateway_pre_transaction SET payment_gateway_status_id = ?, updated_at = NOW() WHERE namespace_id = ? AND code = ?");
			preparedStatement.setInt(1, preTransactionDTO.getStatus().getId());
			preparedStatement.setInt(3, authDTO.getNamespace().getId());
			preparedStatement.setString(4, preTransactionDTO.getCode());
			preparedStatement.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

	public List<PaymentPreTransactionDTO> getPreTransactions(AuthDTO authDTO, PaymentPreTransactionDTO transactionDTO) {
		List<PaymentPreTransactionDTO> transactions = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT pg.id, pg.code, pg.user_id, pg.payment_gateway_partner_id, pg.payment_gateway_provider_id, pg.payment_gateway_merchant_credentials_id, pg.amount, pg.order_code, pg.service_charge, pg.payment_gateway_transaction_type_id, pg.device_medium FROM payment_gateway_transaction pg WHERE pg.namespace_id = ? AND pg.order_code = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, transactionDTO.getOrderCode());
			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO();
				preTransactionDTO.setId(rs.getInt("pg.id"));
				preTransactionDTO.setCode(rs.getString("pg.code"));
				preTransactionDTO.setOrderCode(rs.getString("pg.order_code"));

				UserDTO userDTO = new UserDTO();
				userDTO.setId(rs.getInt("pg.user_id"));
				preTransactionDTO.setUser(userDTO);

				PaymentGatewayPartnerDTO gatewayPartnerDTO = new PaymentGatewayPartnerDTO();
				gatewayPartnerDTO.setId(rs.getInt("pg.payment_gateway_partner_id"));
				preTransactionDTO.setGatewayPartner(gatewayPartnerDTO);

				PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
				gatewayProviderDTO.setId(rs.getInt("pg.payment_gateway_provider_id"));
				preTransactionDTO.setGatewayProvider(gatewayProviderDTO);

				PaymentGatewayCredentialsDTO gatewayCredentialsDTO = new PaymentGatewayCredentialsDTO();
				gatewayCredentialsDTO.setId(rs.getInt("pg.payment_gateway_merchant_credentials_id"));
				preTransactionDTO.setGatewayCredentials(gatewayCredentialsDTO);

				preTransactionDTO.setAmount(rs.getBigDecimal("pg.amount"));
				preTransactionDTO.setServiceCharge(rs.getBigDecimal("pg.service_charge"));
				preTransactionDTO.setTransactionType(PaymentGatewayTransactionTypeEM.getPaymentGatewayTransactionTypeDTO(rs.getInt("pg.payment_gateway_transaction_type_id")));
				preTransactionDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("pg.device_medium")));
				transactions.add(preTransactionDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return transactions;
	}

	public List<PaymentPreTransactionDTO> getPaymentGatewayPreTransactions(AuthDTO authDTO, String fromDate, String toDate, boolean isSuperNamespaceFlag) {
		List<PaymentPreTransactionDTO> transactions = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement ps = null;
			if (isSuperNamespaceFlag) {
				ps = connection.prepareStatement("SELECT id, code, namespace_id, user_id, payment_gateway_partner_id, payment_gateway_provider_id, payment_gateway_merchant_credentials_id, order_type_id, amount, order_code, service_charge, failure_error_code, device_medium, payment_gateway_status_id, gateway_transaction_id, payment_gateway_transaction_type_id FROM payment_gateway_pre_transaction WHERE active_flag = 1 AND updated_at BETWEEN ? AND ?");
				ps.setString(1, fromDate);
				ps.setString(2, toDate);
			}
			else {
				ps = connection.prepareStatement("SELECT id, code, namespace_id, user_id, payment_gateway_partner_id, payment_gateway_provider_id, payment_gateway_merchant_credentials_id, order_type_id, amount, order_code, service_charge, failure_error_code, device_medium, payment_gateway_status_id, gateway_transaction_id, payment_gateway_transaction_type_id FROM payment_gateway_pre_transaction WHERE namespace_id = ? AND active_flag = 1 AND updated_at BETWEEN ? AND ?");
				ps.setInt(1, authDTO.getNamespace().getId());
				ps.setString(2, fromDate);
				ps.setString(3, toDate);
			}

			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO();
				preTransactionDTO.setId(rs.getInt("id"));
				preTransactionDTO.setCode(rs.getString("code"));
				preTransactionDTO.setOrderCode(rs.getString("order_code"));

				NamespaceDTO namespace = new NamespaceDTO();
				namespace.setId(rs.getInt("namespace_id"));
				preTransactionDTO.setNamespace(namespace);

				UserDTO userDTO = new UserDTO();
				userDTO.setId(rs.getInt("user_id"));
				preTransactionDTO.setUser(userDTO);

				PaymentGatewayPartnerDTO gatewayPartnerDTO = new PaymentGatewayPartnerDTO();
				gatewayPartnerDTO.setId(rs.getInt("payment_gateway_partner_id"));
				preTransactionDTO.setGatewayPartner(gatewayPartnerDTO);

				PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
				gatewayProviderDTO.setId(rs.getInt("payment_gateway_provider_id"));
				preTransactionDTO.setGatewayProvider(gatewayProviderDTO);

				PaymentGatewayCredentialsDTO gatewayCredentialsDTO = new PaymentGatewayCredentialsDTO();
				gatewayCredentialsDTO.setId(rs.getInt("payment_gateway_merchant_credentials_id"));
				preTransactionDTO.setGatewayCredentials(gatewayCredentialsDTO);

				preTransactionDTO.setOrderType(OrderTypeEM.getOrderTypeEM(rs.getInt("order_type_id")));
				preTransactionDTO.setAmount(rs.getBigDecimal("amount"));
				preTransactionDTO.setServiceCharge(rs.getBigDecimal("service_charge"));
				preTransactionDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
				preTransactionDTO.setFailureErrorCode(rs.getString("failure_error_code"));
				preTransactionDTO.setStatus(PaymentGatewayStatusEM.getStatusDTO(rs.getInt("payment_gateway_status_id")));
				preTransactionDTO.setGatewayTransactionCode(rs.getString("gateway_transaction_id"));
				preTransactionDTO.setTransactionType(PaymentGatewayTransactionTypeEM.getPaymentGatewayTransactionTypeDTO(rs.getInt("payment_gateway_transaction_type_id")));
				transactions.add(preTransactionDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return transactions;
	}

	public List<PaymentPreTransactionDTO> getPaymentGatewayRefundTransactions(AuthDTO authDTO, String fromDate, String toDate, boolean isSuperNamespaceFlag) {
		List<PaymentPreTransactionDTO> transactions = new ArrayList<>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();

			@Cleanup
			PreparedStatement ps = null;
			if (isSuperNamespaceFlag) {
				ps = connection.prepareStatement("SELECT namespace_id, order_code, amount, service_charge, payment_gateway_provider_id, device_medium FROM payment_gateway_transaction WHERE payment_gateway_transaction_type_id = 2 AND active_flag = 1 AND updated_at BETWEEN ? AND ?");
				ps.setString(1, fromDate);
				ps.setString(2, toDate);
			}
			else {
				ps = connection.prepareStatement("SELECT namespace_id, order_code, amount, service_charge, payment_gateway_provider_id, device_medium FROM payment_gateway_transaction WHERE namespace_id = ? AND payment_gateway_transaction_type_id = 2 AND active_flag = 1 AND updated_at BETWEEN ? AND ?");
				ps.setInt(1, authDTO.getNamespace().getId());
				ps.setString(2, fromDate);
				ps.setString(3, toDate);
			}

			@Cleanup
			ResultSet rs = ps.executeQuery();
			while (rs.next()) {
				PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO();
				preTransactionDTO.setOrderCode(rs.getString("order_code"));

				NamespaceDTO namespace = new NamespaceDTO();
				namespace.setId(rs.getInt("namespace_id"));
				preTransactionDTO.setNamespace(namespace);

				PaymentGatewayProviderDTO gatewayProviderDTO = new PaymentGatewayProviderDTO();
				gatewayProviderDTO.setId(rs.getInt("payment_gateway_provider_id"));
				preTransactionDTO.setGatewayProvider(gatewayProviderDTO);

				preTransactionDTO.setOrderType(OrderTypeEM.TICKET);
				preTransactionDTO.setAmount(rs.getBigDecimal("amount"));
				preTransactionDTO.setServiceCharge(rs.getBigDecimal("service_charge"));
				preTransactionDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(rs.getInt("device_medium")));
				preTransactionDTO.setStatus(PaymentGatewayStatusEM.SUCCESS);
				preTransactionDTO.setTransactionType(PaymentGatewayTransactionTypeEM.REFUND);
				transactions.add(preTransactionDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return transactions;
	}

}
