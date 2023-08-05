package org.in.com.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Cleanup;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuditDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ImageDetailsDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.PaymentReceiptDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.PaymentAcknowledgeEM;
import org.in.com.dto.enumeration.PaymentReceiptTypeEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.joda.time.DateTime;

public class PaymentTransactionDAO extends BaseDAO {
	BigDecimal oneCrore = new BigDecimal("10000000");

	public void SaveRechargeTransaction(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		try {
			if (oneCrore.compareTo(paymentTransactionDTO.getTransactionAmount()) <= 0) {
				throw new ServiceException(ErrorCode.TRANSACTION_AMOUNT_EXCEED);
			}
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			saveRechargeTransaction(connection, authDTO, paymentTransactionDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void saveRechargeTransaction(Connection connection, AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		try {
			if (oneCrore.compareTo(paymentTransactionDTO.getTransactionAmount()) <= 0) {
				throw new ServiceException(ErrorCode.TRANSACTION_AMOUNT_EXCEED);
			}
			String tranactionCode = paymentTransactionDTO.getCode() != null ? paymentTransactionDTO.getCode() : paymentTransactionDTO.getPaymentTransactionCode();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("INSERT INTO payment_transaction(code,namespace_id,user_id,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,user_transaction_id,commission_amount,ac_bus_tax,tds_tax, remarks,active_flag,updated_by,updated_at) VALUES(?,?,?,?,? ,?,?,?,NOW(),? ,?,?,?,?,?, ?,1,? ,NOW())", PreparedStatement.RETURN_GENERATED_KEYS);
			selectPS.setString(1, tranactionCode);
			selectPS.setInt(2, authDTO.getNamespace().getId());
			selectPS.setInt(3, paymentTransactionDTO.getUser().getId());
			selectPS.setInt(4, paymentTransactionDTO.getTransactionMode().getId());
			selectPS.setInt(5, paymentTransactionDTO.getTransactionType().getId());
			selectPS.setBigDecimal(6, paymentTransactionDTO.getTransactionAmount());
			selectPS.setString(7, paymentTransactionDTO.getTransactionType().getCreditDebitFlag());
			selectPS.setString(8, paymentTransactionDTO.getAmountReceivedDate());
			selectPS.setInt(9, paymentTransactionDTO.getPaymentHandledByUser().getId());
			selectPS.setInt(10, PaymentAcknowledgeEM.PAYMENT_INITIATED.getId());
			selectPS.setString(11, null);
			selectPS.setBigDecimal(12, paymentTransactionDTO.getCommissionAmount());
			selectPS.setBigDecimal(13, paymentTransactionDTO.getAcBusTax());
			selectPS.setBigDecimal(14, paymentTransactionDTO.getTdsTax());
			selectPS.setString(15, paymentTransactionDTO.getRemarks());
			selectPS.setInt(16, authDTO.getUser().getId());
			selectPS.execute();
			@Cleanup
			ResultSet rs = selectPS.getGeneratedKeys();
			if (rs.next()) {
				paymentTransactionDTO.setId(rs.getInt(1));
				paymentTransactionDTO.setCode(tranactionCode);
			}
			else {
				throw new Exception();
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public PaymentTransactionDTO getPaymentTransactionHistory(AuthDTO authDTO, UserDTO userDTO, PaymentTransactionDTO transactionDTO, DateTime fromDate, DateTime toDate) {
		List<PaymentTransactionDTO> list = new ArrayList<PaymentTransactionDTO>();
		try {
			UserDAO userDAO = new UserDAO();
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (StringUtil.isNotNull(userDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT pay.code,usr.code,usr.first_name,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id, commission_amount, ac_bus_tax, tds_tax, remarks FROM payment_transaction pay,user usr WHERE pay.namespace_id = ? AND usr.namespace_id = ? AND usr.id = pay.user_id AND usr.code = ? AND transaction_type_id = ? AND pay.transaction_at BETWEEN ? AND ? AND pay.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, authDTO.getNamespace().getId());
				selectPS.setString(3, userDTO.getCode());
				selectPS.setInt(4, transactionDTO.getTransactionType().getId());
				selectPS.setString(5, fromDate.toString(DateUtil.JODA_DATE_TIME_FORMATE));
				selectPS.setString(6, toDate.plusDays(1).minusSeconds(1).toString(DateUtil.JODA_DATE_TIME_FORMATE));
			}
			else if (userDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT pay.code,usr.code,usr.first_name,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,commission_amount, ac_bus_tax, tds_tax, remarks FROM payment_transaction pay,user usr WHERE pay.namespace_id = ? AND usr.namespace_id = ? AND usr.id = pay.user_id AND usr.id = ? AND transaction_type_id = ? AND pay.transaction_at BETWEEN ? AND ? AND pay.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, authDTO.getNamespace().getId());
				selectPS.setInt(3, userDTO.getId());
				selectPS.setInt(4, transactionDTO.getTransactionType().getId());
				selectPS.setString(5, fromDate.toString(DateUtil.JODA_DATE_TIME_FORMATE));
				selectPS.setString(6, toDate.plusDays(1).minusSeconds(1).toString(DateUtil.JODA_DATE_TIME_FORMATE));
			}
			@Cleanup
			ResultSet rs = selectPS.executeQuery();
			while (rs.next()) {
				PaymentTransactionDTO dto = new PaymentTransactionDTO();
				dto.setCode(rs.getString("pay.code"));
				dto.setTransactionMode(TransactionModeEM.getTransactionModeEM(rs.getInt("transaction_mode_id")));
				dto.setTransactionType(TransactionTypeEM.getTransactionTypeEM(rs.getInt("transaction_type_id")));
				dto.setTransactionAmount(rs.getBigDecimal("transaction_amount"));
				dto.setCommissionAmount(rs.getBigDecimal("commission_amount"));
				dto.setAcBusTax(rs.getBigDecimal("ac_bus_tax"));
				dto.setTdsTax(rs.getBigDecimal("tds_tax"));
				dto.setAmountReceivedDate(rs.getString("amount_received_date"));
				dto.setTransactionDate(rs.getString("transaction_at"));
				dto.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(rs.getInt("payment_acknowledge_status_id")));
				dto.setRemarks(rs.getString("remarks"));
				UserDTO paymentHandledUserDTO = userDAO.getUsersDTO(connection, authDTO.getNamespace().getId(), rs.getInt("payment_handle_by"));
				UserDTO user = new UserDTO();
				user.setCode(rs.getString("usr.code"));
				user.setName(rs.getString("usr.first_name"));
				dto.setUser(user);
				dto.setPaymentHandledByUser(paymentHandledUserDTO);
				list.add(dto);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		transactionDTO.setList(list);
		return transactionDTO;
	}

	public PaymentTransactionDTO getPaymentTransaction(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id,code,user_id,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,user_transaction_id,commission_amount,ac_bus_tax,tds_tax,lookup_id,remarks,active_flag,updated_by,updated_at FROM payment_transaction WHERE namespace_id = ? and code = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, paymentTransactionDTO.getCode());
			@Cleanup
			ResultSet rs = selectPS.executeQuery();
			if (rs.next()) {
				paymentTransactionDTO.setId(rs.getInt("id"));
				paymentTransactionDTO.setCode(rs.getString("code"));
				UserDTO userDTO = new UserDTO();
				UserDTO paymentHandledByuserDTO = new UserDTO();
				userDTO.setId(rs.getInt("user_id"));
				paymentHandledByuserDTO.setId(rs.getInt("payment_handle_by"));
				paymentTransactionDTO.setUser(userDTO);

				UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
				userTransactionDTO.setId(rs.getInt("user_transaction_id"));
				paymentTransactionDTO.setUserTransaction(userTransactionDTO);

				paymentTransactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(rs.getInt("transaction_mode_id")));
				paymentTransactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(rs.getInt("transaction_type_id")));
				paymentTransactionDTO.setTransactionAmount(rs.getBigDecimal("transaction_amount"));
				paymentTransactionDTO.setAmountReceivedDate(rs.getString("amount_received_date"));
				paymentTransactionDTO.setCommissionAmount(rs.getBigDecimal("commission_amount"));
				paymentTransactionDTO.setAcBusTax(rs.getBigDecimal("ac_bus_tax"));
				paymentTransactionDTO.setTdsTax(rs.getBigDecimal("tds_tax"));
				paymentTransactionDTO.setLookupId(rs.getInt("lookup_id"));
				paymentTransactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(rs.getInt("payment_acknowledge_status_id")));
				paymentTransactionDTO.setPaymentHandledByUser(paymentHandledByuserDTO);
				paymentTransactionDTO.setTransactionDate(rs.getString("transaction_at"));
				paymentTransactionDTO.setRemarks(rs.getString("remarks"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return paymentTransactionDTO;
	}

	public PaymentTransactionDTO getPaymentTransactionV2(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (paymentTransactionDTO.getId() != Numeric.ZERO_INT) {
				selectPS = connection.prepareStatement("SELECT id,code,user_id,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,user_transaction_id,commission_amount,ac_bus_tax,tds_tax,lookup_id,remarks,active_flag,updated_by,updated_at FROM payment_transaction WHERE namespace_id = ? and id = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, paymentTransactionDTO.getId());
			}
			else if (StringUtil.isNotNull(paymentTransactionDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT id,code,user_id,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,user_transaction_id,commission_amount,ac_bus_tax,tds_tax,lookup_id,remarks,active_flag,updated_by,updated_at FROM payment_transaction WHERE namespace_id = ? and code = ? AND active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, paymentTransactionDTO.getCode());
			}

			if (selectPS != null) {
				@Cleanup
				ResultSet rs = selectPS.executeQuery();

				@Cleanup
				PreparedStatement selectPartialPS = null;
				@Cleanup
				ResultSet partialRS = null;

				if (rs.next()) {
					paymentTransactionDTO.setId(rs.getInt("id"));
					paymentTransactionDTO.setCode(rs.getString("code"));
					UserDTO userDTO = new UserDTO();
					UserDTO paymentHandledByuserDTO = new UserDTO();
					userDTO.setId(rs.getInt("user_id"));
					paymentHandledByuserDTO.setId(rs.getInt("payment_handle_by"));
					paymentTransactionDTO.setUser(userDTO);
					paymentTransactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(rs.getInt("transaction_mode_id")));
					paymentTransactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(rs.getInt("transaction_type_id")));
					paymentTransactionDTO.setTransactionAmount(rs.getBigDecimal("transaction_amount"));
					paymentTransactionDTO.setAmountReceivedDate(rs.getString("amount_received_date"));
					paymentTransactionDTO.setCommissionAmount(rs.getBigDecimal("commission_amount"));
					paymentTransactionDTO.setAcBusTax(rs.getBigDecimal("ac_bus_tax"));
					paymentTransactionDTO.setTdsTax(rs.getBigDecimal("tds_tax"));
					paymentTransactionDTO.setLookupId(rs.getInt("lookup_id"));
					paymentTransactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(rs.getInt("payment_acknowledge_status_id")));
					paymentTransactionDTO.setPaymentHandledByUser(paymentHandledByuserDTO);
					paymentTransactionDTO.setTransactionDate(rs.getString("transaction_at"));
					paymentTransactionDTO.setRemarks(rs.getString("remarks"));

					selectPartialPS = connection.prepareStatement("SELECT id,code,user_id,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,user_transaction_id,commission_amount,ac_bus_tax,tds_tax,lookup_id,remarks,active_flag,updated_by,updated_at FROM payment_transaction WHERE namespace_id = ? AND lookup_id = ? AND active_flag = 1");
					selectPartialPS.setInt(1, authDTO.getNamespace().getId());
					selectPartialPS.setInt(2, paymentTransactionDTO.getId());
					partialRS = selectPartialPS.executeQuery();
					List<PaymentTransactionDTO> paymentTransactionList = new ArrayList<PaymentTransactionDTO>();

					while (partialRS.next()) {
						PaymentTransactionDTO paymentTransaction = new PaymentTransactionDTO();
						paymentTransaction.setId(partialRS.getInt("id"));
						paymentTransaction.setCode(partialRS.getString("code"));

						UserDTO userPartialDTO = new UserDTO();
						userPartialDTO.setId(partialRS.getInt("user_id"));
						paymentTransaction.setUser(userPartialDTO);

						UserDTO paymentHandledByuser = new UserDTO();
						paymentHandledByuser.setId(partialRS.getInt("payment_handle_by"));
						paymentTransaction.setPaymentHandledByUser(paymentHandledByuser);

						paymentTransaction.setTransactionMode(TransactionModeEM.getTransactionModeEM(partialRS.getInt("transaction_mode_id")));
						paymentTransaction.setTransactionType(TransactionTypeEM.getTransactionTypeEM(partialRS.getInt("transaction_type_id")));
						paymentTransaction.setTransactionAmount(partialRS.getBigDecimal("transaction_amount"));
						paymentTransaction.setAmountReceivedDate(partialRS.getString("amount_received_date"));
						paymentTransaction.setCommissionAmount(partialRS.getBigDecimal("commission_amount"));
						paymentTransaction.setAcBusTax(partialRS.getBigDecimal("ac_bus_tax"));
						paymentTransaction.setTdsTax(partialRS.getBigDecimal("tds_tax"));
						paymentTransaction.setLookupId(partialRS.getInt("lookup_id"));
						paymentTransaction.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(partialRS.getInt("payment_acknowledge_status_id")));
						paymentTransaction.setTransactionDate(partialRS.getString("transaction_at"));
						paymentTransaction.setRemarks(partialRS.getString("remarks"));
						paymentTransactionList.add(paymentTransaction);
					}
					paymentTransactionDTO.setPartialPaymentPaidList(paymentTransactionList);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return paymentTransactionDTO;
	}

	public void getALLUnAcknowledgedPaymentTransaction(AuthDTO authDTO, PaymentTransactionDTO transactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			List<PaymentTransactionDTO> list = new ArrayList<PaymentTransactionDTO>();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id,code,user_id,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,user_transaction_id,remarks,active_flag,updated_by,updated_at FROM payment_transaction WHERE namespace_id = ? AND payment_acknowledge_status_id = ? AND lookup_id = 0 AND active_flag = 1 ");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, transactionDTO.getPaymentAcknowledge().getId());
			@Cleanup
			ResultSet rs = selectPS.executeQuery();

			@Cleanup
			PreparedStatement selectPartialPS = null;
			@Cleanup
			ResultSet partialRS = null;
			while (rs.next()) {
				PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
				paymentTransactionDTO.setId(rs.getInt("id"));
				paymentTransactionDTO.setCode(rs.getString("code"));
				UserDTO userDTO = new UserDTO();
				UserDTO paymentHandledByuserDTO = new UserDTO();
				userDTO.setId(rs.getInt("user_id"));
				paymentHandledByuserDTO.setId(rs.getInt("payment_handle_by"));
				paymentTransactionDTO.setUser(userDTO);
				paymentTransactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(rs.getInt("transaction_mode_id")));
				paymentTransactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(rs.getInt("transaction_type_id")));
				paymentTransactionDTO.setTransactionAmount(rs.getBigDecimal("transaction_amount"));
				paymentTransactionDTO.setAmountReceivedDate(rs.getString("amount_received_date"));
				paymentTransactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(rs.getInt("payment_acknowledge_status_id")));
				paymentTransactionDTO.setPaymentHandledByUser(paymentHandledByuserDTO);
				paymentTransactionDTO.setTransactionDate(rs.getString("transaction_at"));
				paymentTransactionDTO.setRemarks(rs.getString("remarks"));

				selectPartialPS = connection.prepareStatement("SELECT id,code,user_id,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,user_transaction_id,remarks,active_flag,updated_by,updated_at FROM payment_transaction WHERE namespace_id = ? AND lookup_id = ? AND active_flag = 1");
				selectPartialPS.setInt(1, authDTO.getNamespace().getId());
				selectPartialPS.setInt(2, paymentTransactionDTO.getId());
				partialRS = selectPartialPS.executeQuery();

				List<PaymentTransactionDTO> paymentTransactionList = new ArrayList<PaymentTransactionDTO>();
				while (partialRS.next()) {
					PaymentTransactionDTO paymentTransaction = new PaymentTransactionDTO();
					paymentTransaction.setId(partialRS.getInt("id"));
					paymentTransaction.setCode(partialRS.getString("code"));

					UserDTO userPartialDTO = new UserDTO();
					userPartialDTO.setId(partialRS.getInt("user_id"));
					paymentTransaction.setUser(userPartialDTO);

					UserDTO paymentHandledByuser = new UserDTO();
					paymentHandledByuser.setId(partialRS.getInt("payment_handle_by"));
					paymentTransaction.setPaymentHandledByUser(paymentHandledByuser);

					paymentTransaction.setTransactionMode(TransactionModeEM.getTransactionModeEM(partialRS.getInt("transaction_mode_id")));
					paymentTransaction.setTransactionType(TransactionTypeEM.getTransactionTypeEM(partialRS.getInt("transaction_type_id")));
					paymentTransaction.setTransactionAmount(partialRS.getBigDecimal("transaction_amount"));
					paymentTransaction.setAmountReceivedDate(partialRS.getString("amount_received_date"));
					paymentTransaction.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(partialRS.getInt("payment_acknowledge_status_id")));
					paymentTransaction.setTransactionDate(partialRS.getString("transaction_at"));
					paymentTransaction.setRemarks(partialRS.getString("remarks"));
					paymentTransactionList.add(paymentTransaction);
				}
				paymentTransactionDTO.setPartialPaymentPaidList(paymentTransactionList);

				list.add(paymentTransactionDTO);
			}
			transactionDTO.setList(list);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void getAcknowledgedPaymentTransactionUpdate(AuthDTO authDTO, PaymentTransactionDTO transactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			getAcknowledgedPaymentTransactionUpdate(connection, authDTO, transactionDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void getAcknowledgedPaymentTransactionUpdate(Connection connection, AuthDTO authDTO, PaymentTransactionDTO transactionDTO) {
		try {
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE  payment_transaction  SET payment_acknowledge_status_id = ?, user_transaction_id = ?, updated_by = ?, remarks = ?, updated_at  = NOW()  WHERE namespace_id = ? AND id = ? AND active_flag = 1 ");
			selectPS.setInt(1, transactionDTO.getPaymentAcknowledge().getId());
			selectPS.setInt(2, transactionDTO.getUserTransaction().getId());
			selectPS.setInt(3, authDTO.getUser().getId());
			selectPS.setString(4, transactionDTO.getRemarks());
			selectPS.setInt(5, authDTO.getNamespace().getId());
			selectPS.setInt(6, transactionDTO.getId());
			selectPS.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<PaymentTransactionDTO> getPartialPaymentTransaction(AuthDTO authDTO, PaymentTransactionDTO paymentTransaction) {
		List<PaymentTransactionDTO> partialPaymentTransactionList = new ArrayList<PaymentTransactionDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id,code,user_id,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,user_transaction_id,remarks,active_flag,updated_by,updated_at FROM payment_transaction WHERE namespace_id = ? AND lookup_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, paymentTransaction.getId());
			@Cleanup
			ResultSet rs = selectPS.executeQuery();
			while (rs.next()) {
				PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
				paymentTransactionDTO.setId(rs.getInt("id"));
				paymentTransactionDTO.setCode(rs.getString("code"));

				UserDTO userDTO = new UserDTO();
				userDTO.setId(rs.getInt("user_id"));
				paymentTransactionDTO.setUser(userDTO);

				UserDTO paymentHandledByuserDTO = new UserDTO();
				paymentHandledByuserDTO.setId(rs.getInt("payment_handle_by"));
				paymentTransactionDTO.setPaymentHandledByUser(paymentHandledByuserDTO);

				paymentTransactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(rs.getInt("transaction_mode_id")));
				paymentTransactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(rs.getInt("transaction_type_id")));
				paymentTransactionDTO.setTransactionAmount(rs.getBigDecimal("transaction_amount"));
				paymentTransactionDTO.setAmountReceivedDate(rs.getString("amount_received_date"));
				paymentTransactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(rs.getInt("payment_acknowledge_status_id")));
				paymentTransactionDTO.setTransactionDate(rs.getString("transaction_at"));
				paymentTransactionDTO.setRemarks(rs.getString("remarks"));
				partialPaymentTransactionList.add(paymentTransactionDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return partialPaymentTransactionList;
	}
	
	public void getPaymentTransactionsWithPartialPayment(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id,code,user_id,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,user_transaction_id,remarks,active_flag,updated_by,updated_at FROM payment_transaction WHERE namespace_id = ? AND id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, paymentTransactionDTO.getId());
			@Cleanup
			ResultSet rs = selectPS.executeQuery();
			
			@Cleanup
			PreparedStatement partialPS = null;
			@Cleanup
			ResultSet partialRS = null;
			if (rs.next()) {
				paymentTransactionDTO.setId(rs.getInt("id"));
				paymentTransactionDTO.setCode(rs.getString("code"));

				UserDTO userDTO = new UserDTO();
				userDTO.setId(rs.getInt("user_id"));
				paymentTransactionDTO.setUser(userDTO);

				UserDTO paymentHandledByuserDTO = new UserDTO();
				paymentHandledByuserDTO.setId(rs.getInt("payment_handle_by"));
				paymentTransactionDTO.setPaymentHandledByUser(paymentHandledByuserDTO);

				paymentTransactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(rs.getInt("transaction_mode_id")));
				paymentTransactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(rs.getInt("transaction_type_id")));
				paymentTransactionDTO.setTransactionAmount(rs.getBigDecimal("transaction_amount"));
				paymentTransactionDTO.setAmountReceivedDate(rs.getString("amount_received_date"));
				paymentTransactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(rs.getInt("payment_acknowledge_status_id")));
				paymentTransactionDTO.setTransactionDate(rs.getString("transaction_at"));
				paymentTransactionDTO.setRemarks(rs.getString("remarks"));
				
				partialPS = connection.prepareStatement("SELECT id,code,user_id,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,user_transaction_id,remarks,active_flag,updated_by,updated_at FROM payment_transaction WHERE namespace_id = ? AND lookup_id = ? AND active_flag = 1");
				partialPS.setInt(1, authDTO.getNamespace().getId());
				partialPS.setInt(2, paymentTransactionDTO.getId());
				partialRS = partialPS.executeQuery();

				List<PaymentTransactionDTO> partialPaymentTransactionList = new ArrayList<PaymentTransactionDTO>();
				while (partialRS.next()) {
					PaymentTransactionDTO partialPaymentTransaction = new PaymentTransactionDTO();
					partialPaymentTransaction.setId(partialRS.getInt("id"));
					partialPaymentTransaction.setCode(partialRS.getString("code"));
					
					UserDTO user = new UserDTO();
					user.setId(partialRS.getInt("user_id"));
					partialPaymentTransaction.setUser(user);

					UserDTO paymentHandledByuser = new UserDTO();
					paymentHandledByuser.setId(partialRS.getInt("payment_handle_by"));
					partialPaymentTransaction.setPaymentHandledByUser(paymentHandledByuser);

					partialPaymentTransaction.setTransactionMode(TransactionModeEM.getTransactionModeEM(partialRS.getInt("transaction_mode_id")));
					partialPaymentTransaction.setTransactionType(TransactionTypeEM.getTransactionTypeEM(partialRS.getInt("transaction_type_id")));
					partialPaymentTransaction.setTransactionAmount(partialRS.getBigDecimal("transaction_amount"));
					partialPaymentTransaction.setAmountReceivedDate(partialRS.getString("amount_received_date"));
					partialPaymentTransaction.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(partialRS.getInt("payment_acknowledge_status_id")));
					partialPaymentTransaction.setTransactionDate(partialRS.getString("transaction_at"));
					partialPaymentTransaction.setRemarks(partialRS.getString("remarks"));
					partialPaymentTransactionList.add(partialPaymentTransaction);
				}
				paymentTransactionDTO.setPartialPaymentPaidList(partialPaymentTransactionList);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}
	
	public List<PaymentTransactionDTO> getALLUnAcknowledgedPaymentTransaction(AuthDTO authDTO, OrganizationDTO organizationDTO, UserDTO user) {
		List<PaymentTransactionDTO> partialPaidList = new ArrayList<PaymentTransactionDTO>();
		Map<Integer, PaymentTransactionDTO> paymentMap = new HashMap<Integer, PaymentTransactionDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (organizationDTO != null && organizationDTO.getId() != Numeric.ZERO_INT) {
				selectPS = connection.prepareStatement("SELECT cpt.id, cpt.code, usr.id AS user_id, cpt.transaction_mode_id, cpt.transaction_type_id, cpt.transaction_amount, cpt.amount_received_date, cpt.transaction_at, cpt.payment_handle_by, cpt.payment_acknowledge_status_id, cpt.lookup_id, cpt.remarks, cpt.active_flag, cpt.updated_by, cpt.updated_at FROM payment_transaction cpt, user usr WHERE cpt.namespace_id = ? AND cpt.payment_acknowledge_status_id IN (1, 4, 5) AND usr.namespace_id = ? AND usr.id = cpt.user_id AND usr.organization_id = ? AND usr.active_flag = 1 AND cpt.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, authDTO.getNamespace().getId());
				selectPS.setInt(3, organizationDTO.getId());
			}
			else if (user != null && user.getId() != Numeric.ZERO_INT) {
				selectPS = connection.prepareStatement("SELECT cpt.id, cpt.code, usr.id AS user_id, cpt.transaction_mode_id, cpt.transaction_type_id, cpt.transaction_amount, cpt.amount_received_date, cpt.transaction_at, cpt.payment_handle_by, cpt.payment_acknowledge_status_id, cpt.lookup_id, cpt.remarks, cpt.active_flag, cpt.updated_by, cpt.updated_at FROM payment_transaction cpt, user usr WHERE cpt.namespace_id = ? AND cpt.payment_acknowledge_status_id IN (1, 4, 5) AND usr.namespace_id = ? AND usr.id = cpt.user_id AND usr.id = ? AND usr.active_flag = 1 AND cpt.active_flag = 1");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, authDTO.getNamespace().getId());
				selectPS.setInt(3, user.getId());
			}
			else {
				selectPS = connection.prepareStatement("SELECT id,code,user_id,transaction_mode_id,transaction_type_id,transaction_amount,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,lookup_id,remarks,active_flag,updated_by,updated_at FROM payment_transaction WHERE namespace_id = ? AND payment_acknowledge_status_id IN (1, 4, 5) AND active_flag = 1 ");
				selectPS.setInt(1, authDTO.getNamespace().getId());
			}
			@Cleanup
			ResultSet rs = selectPS.executeQuery();
			while (rs.next()) {
				PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
				paymentTransactionDTO.setId(rs.getInt("id"));
				paymentTransactionDTO.setCode(rs.getString("code"));
				UserDTO userDTO = new UserDTO();
				UserDTO paymentHandledByuserDTO = new UserDTO();
				userDTO.setId(rs.getInt("user_id"));
				paymentHandledByuserDTO.setId(rs.getInt("payment_handle_by"));
				paymentTransactionDTO.setUser(userDTO);
				paymentTransactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(rs.getInt("transaction_mode_id")));
				paymentTransactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(rs.getInt("transaction_type_id")));
				paymentTransactionDTO.setTransactionAmount(rs.getBigDecimal("transaction_amount"));
				paymentTransactionDTO.setAmountReceivedDate(rs.getString("amount_received_date"));
				paymentTransactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(rs.getInt("payment_acknowledge_status_id")));
				paymentTransactionDTO.setPaymentHandledByUser(paymentHandledByuserDTO);
				paymentTransactionDTO.setTransactionDate(rs.getString("transaction_at"));
				paymentTransactionDTO.setRemarks(rs.getString("remarks"));
				paymentTransactionDTO.setLookupId(rs.getInt("lookup_id"));

				if (paymentTransactionDTO.getLookupId() == 0) {
					paymentMap.put(paymentTransactionDTO.getId(), paymentTransactionDTO);
				}
				else {
					partialPaidList.add(paymentTransactionDTO);
				}
			}

			for (PaymentTransactionDTO overrideScheduleDTO : partialPaidList) {
				if (paymentMap.get(overrideScheduleDTO.getLookupId()) != null) {
					PaymentTransactionDTO dto = paymentMap.get(overrideScheduleDTO.getLookupId());
					dto.getPartialPaymentPaidList().add(overrideScheduleDTO);
					paymentMap.put(dto.getId(), dto);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return new ArrayList<PaymentTransactionDTO>(paymentMap.values());
	}
	
	public void generatePaymentReceipt(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO payment_receipt (code,namespace_id,user_id,amount,balance_amount,transaction_mode_id,amount_received_date,transaction_at,payment_transaction_ids,payment_acknowledge_status_id,payment_receipt_type_id,remarks,active_flag,updated_by,updated_at) values (?,?,?,?,? ,?,?,NOW(),?,?, ?,?,1,?,NOW())", PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(++pindex, paymentReceiptDTO.getCode());
			preparedStatement.setInt(++pindex, authDTO.getNamespace().getId());
			preparedStatement.setInt(++pindex, paymentReceiptDTO.getUser().getId());
			preparedStatement.setBigDecimal(++pindex, paymentReceiptDTO.getTransactionAmount());
			preparedStatement.setBigDecimal(++pindex, paymentReceiptDTO.getBalanceAmount());
			preparedStatement.setInt(++pindex, paymentReceiptDTO.getTransactionMode().getId());
			preparedStatement.setString(++pindex, paymentReceiptDTO.getAmountReceivedDate());
			preparedStatement.setString(++pindex, paymentReceiptDTO.getPaymentTransactionIds());
			preparedStatement.setInt(++pindex, paymentReceiptDTO.getPaymentAcknowledge().getId());
			preparedStatement.setInt(++pindex, paymentReceiptDTO.getPaymentReceiptType().getId());
			preparedStatement.setString(++pindex, getRemarks(authDTO, paymentReceiptDTO, "New Payment Receipt Created"));
			preparedStatement.setInt(++pindex, authDTO.getUser().getId());
			preparedStatement.execute();
			@Cleanup
			ResultSet rs = preparedStatement.getGeneratedKeys();
			if (rs.next()) {
				paymentReceiptDTO.setId(rs.getInt(1));
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_GENERATE_VOUCHER);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	private String getRemarks(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO, String event) {
		JSONObject remarks = new JSONObject();
		JSONArray audits = new JSONArray();

		JSONObject audit = new JSONObject();
		audit.put("e", event);
		audit.put("u", authDTO.getUser().getId());
		audit.put("t", DateUtil.convertDateTime(DateUtil.NOW()));
		audits.add(audit);

		remarks.put("r", paymentReceiptDTO.getRemarks());
		remarks.put("a", audits);

		return remarks.toString();
	}

	public void updatePaymentReceipt(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE payment_receipt SET balance_amount = ?, payment_transaction_ids = ?, remarks = ? WHERE id = ? AND namespace_id = ? AND active_flag = 1");
			preparedStatement.setBigDecimal(++pindex, paymentReceiptDTO.getBalanceAmount());
			preparedStatement.setString(++pindex, paymentReceiptDTO.getPaymentTransactionIds());
			preparedStatement.setString(++pindex, convertRemarks(authDTO, paymentReceiptDTO, "Balance Amount Updated"));
			preparedStatement.setInt(++pindex, paymentReceiptDTO.getId());
			preparedStatement.setInt(++pindex, authDTO.getNamespace().getId());
			preparedStatement.executeUpdate();
		}
		catch (Exception e) {
			System.out.println("PTER01: " + paymentReceiptDTO.getId() + " code:" + paymentReceiptDTO.getCode() + " pIds:" + paymentReceiptDTO.getPaymentTransactionIds());
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	private String convertRemarks(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO, String event) {
		String remarks = Text.EMPTY;
		if (StringUtil.isValidJSON(paymentReceiptDTO.getRemarks())) {
			JSONObject remarksJSON = JSONObject.fromObject(paymentReceiptDTO.getRemarks());

			JSONObject audit = new JSONObject();
			audit.put("e", event);
			audit.put("u", authDTO.getUser().getId());
			audit.put("t", DateUtil.convertDateTime(DateUtil.NOW()));
			remarksJSON.getJSONArray("a").add(audit);

			remarks = remarksJSON.toString();
		}
		else {
			remarks = getRemarks(authDTO, paymentReceiptDTO, event);
		}

		return remarks;
	}

	public void updatePaymentReceiptV2(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE payment_receipt SET payment_acknowledge_status_id = ?, remarks = ?, updated_by = ?, updated_at = NOW() WHERE code = ? AND namespace_id = ? AND active_flag = 1");
			preparedStatement.setInt(++pindex, paymentReceiptDTO.getPaymentAcknowledge().getId());
			preparedStatement.setString(++pindex, paymentReceiptDTO.getRemarks());
			preparedStatement.setInt(++pindex, authDTO.getUser().getId());
			preparedStatement.setString(++pindex, paymentReceiptDTO.getCode());
			preparedStatement.setInt(++pindex, authDTO.getNamespace().getId());
			preparedStatement.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<PaymentReceiptDTO> getPaymentReceipts(AuthDTO authDTO, UserDTO userDTO, String fromDate, String toDate, PaymentAcknowledgeEM paymentAcknowledge) {
		List<PaymentReceiptDTO> paymentReceipts = new ArrayList<PaymentReceiptDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;

			String paymentAcknowledgeStatus = Text.EMPTY;
			if (paymentAcknowledge != null) {
				paymentAcknowledgeStatus = " AND payment_acknowledge_status_id = " + paymentAcknowledge.getId();
			}

			if (userDTO != null && userDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT code, user_id, amount, balance_amount, transaction_mode_id, amount_received_date, transaction_at, payment_transaction_ids, payment_acknowledge_status_id, payment_receipt_type_id, image_details_id, remarks, active_flag, updated_by, updated_at FROM payment_receipt WHERE namespace_id = ? AND user_id = ?" + paymentAcknowledgeStatus + " AND active_flag = 1 AND transaction_at BETWEEN ? AND ?");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, userDTO.getId());
				selectPS.setString(3, fromDate);
				selectPS.setString(4, toDate);
			}
			else {
				selectPS = connection.prepareStatement("SELECT code, user_id, amount, balance_amount, transaction_mode_id, amount_received_date, transaction_at, payment_transaction_ids, payment_acknowledge_status_id, payment_receipt_type_id, image_details_id, remarks, active_flag, updated_by, updated_at FROM payment_receipt WHERE namespace_id = ?" + paymentAcknowledgeStatus + " AND active_flag = 1 AND transaction_at BETWEEN ? AND ?");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, fromDate);
				selectPS.setString(3, toDate);
			}

			@Cleanup
			ResultSet rs = selectPS.executeQuery();
			while (rs.next()) {
				PaymentReceiptDTO paymentReceiptDTO = new PaymentReceiptDTO();
				paymentReceiptDTO.setCode(rs.getString("code"));
				paymentReceiptDTO.setTransactionAmount(rs.getBigDecimal("amount"));
				paymentReceiptDTO.setBalanceAmount(rs.getBigDecimal("balance_amount"));
				paymentReceiptDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(rs.getInt("transaction_mode_id")));
				paymentReceiptDTO.setAmountReceivedDate(rs.getString("amount_received_date"));
				paymentReceiptDTO.setTransactionDate(rs.getString("transaction_at"));
				paymentReceiptDTO.setUpdatedAt(rs.getString("updated_at"));
				paymentReceiptDTO.setPaymentTransactions(convertPaymentTransactions(rs.getString("payment_transaction_ids")));
				paymentReceiptDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(rs.getInt("payment_acknowledge_status_id")));
				paymentReceiptDTO.setPaymentReceiptType(PaymentReceiptTypeEM.getPaymentReceiptType(rs.getInt("payment_receipt_type_id")));
				convertRemarks(paymentReceiptDTO, rs.getString("remarks"));
				paymentReceiptDTO.setActiveFlag(rs.getInt("active_flag"));

				UserDTO user = new UserDTO();
				user.setId(rs.getInt("user_id"));
				paymentReceiptDTO.setUser(user);
				
				List<ImageDetailsDTO> imageDetailsList = convertImageDetails(rs.getString("image_details_id"));
				paymentReceiptDTO.setImageDetails(imageDetailsList);

				UserDTO updatedBy = new UserDTO();
				updatedBy.setId(rs.getInt("updated_by"));
				paymentReceiptDTO.setUpdatedBy(updatedBy);

				paymentReceipts.add(paymentReceiptDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return paymentReceipts;
	}

	private void convertRemarks(PaymentReceiptDTO paymentReceiptDTO, String remarks) {
		if (StringUtil.isNotNull(remarks)) {
			if (StringUtil.isValidJSON(remarks)) {
				JSONObject remarksJSON = JSONObject.fromObject(remarks);
				paymentReceiptDTO.setRemarks(remarksJSON.getString("r"));

				JSONArray audits = remarksJSON.getJSONArray("a");

				List<AuditDTO> auditLog = new ArrayList<>();
				for (Object obj : audits) {
					JSONObject remarksObj = (JSONObject) obj;

					AuditDTO auditDTO = new AuditDTO();
					auditDTO.setEvent(remarksObj.getString("e"));
					auditDTO.setUpdatedAt(remarksObj.getString("t"));

					UserDTO userDTO = new UserDTO();
					userDTO.setId(remarksObj.getInt("u"));
					auditDTO.setUser(userDTO);
					auditLog.add(auditDTO);
				}

				paymentReceiptDTO.setAuditLog(auditLog);
			}
			else {
				paymentReceiptDTO.setRemarks(remarks);
			}
		}
	}

	public List<PaymentReceiptDTO> getBalancePaymentReceipt(AuthDTO authDTO, UserDTO userDTO) {
		List<PaymentReceiptDTO> paymentReceipts = new ArrayList<PaymentReceiptDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, user_id, amount, balance_amount, transaction_mode_id, amount_received_date, transaction_at, payment_transaction_ids, payment_receipt_type_id, payment_acknowledge_status_id, remarks, active_flag, updated_by, updated_at FROM payment_receipt WHERE namespace_id = ? AND user_id = ? AND payment_acknowledge_status_id = 2 AND balance_amount > 0.0 AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userDTO.getId());

			@Cleanup
			ResultSet rs = selectPS.executeQuery();
			while (rs.next()) {
				PaymentReceiptDTO paymentReceiptDTO = new PaymentReceiptDTO();
				paymentReceiptDTO.setId(rs.getInt("id"));
				paymentReceiptDTO.setCode(rs.getString("code"));
				paymentReceiptDTO.setTransactionAmount(rs.getBigDecimal("amount"));
				paymentReceiptDTO.setBalanceAmount(rs.getBigDecimal("balance_amount"));
				paymentReceiptDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(rs.getInt("transaction_mode_id")));
				paymentReceiptDTO.setAmountReceivedDate(rs.getString("amount_received_date"));
				paymentReceiptDTO.setTransactionDate(rs.getString("transaction_at"));
				paymentReceiptDTO.setUpdatedAt(rs.getString("updated_at"));
				paymentReceiptDTO.setPaymentTransactions(convertPaymentTransactions(rs.getString("payment_transaction_ids")));
				paymentReceiptDTO.setPaymentReceiptType(PaymentReceiptTypeEM.getPaymentReceiptType(rs.getInt("payment_receipt_type_id")));
				paymentReceiptDTO.setRemarks(rs.getString("remarks"));
				paymentReceiptDTO.setActiveFlag(rs.getInt("active_flag"));

				UserDTO user = new UserDTO();
				user.setId(rs.getInt("user_id"));
				paymentReceiptDTO.setUser(user);

				UserDTO updatedBy = new UserDTO();
				updatedBy.setId(rs.getInt("updated_by"));
				paymentReceiptDTO.setUpdatedBy(updatedBy);

				paymentReceipts.add(paymentReceiptDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return paymentReceipts;
	}

	public void getPaymentReceipt(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, code, user_id, amount, balance_amount, transaction_mode_id, amount_received_date, transaction_at, payment_transaction_ids, payment_acknowledge_status_id, payment_receipt_type_id, image_details_id, remarks, active_flag, updated_by, updated_at FROM payment_receipt WHERE namespace_id = ? AND code = ?  AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, paymentReceiptDTO.getCode());

			@Cleanup
			ResultSet rs = selectPS.executeQuery();
			if (rs.next()) {
				paymentReceiptDTO.setId(rs.getInt("id"));
				paymentReceiptDTO.setCode(rs.getString("code"));
				paymentReceiptDTO.setTransactionAmount(rs.getBigDecimal("amount"));
				paymentReceiptDTO.setBalanceAmount(rs.getBigDecimal("balance_amount"));
				paymentReceiptDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(rs.getInt("transaction_mode_id")));
				paymentReceiptDTO.setAmountReceivedDate(rs.getString("amount_received_date"));
				paymentReceiptDTO.setTransactionDate(rs.getString("transaction_at"));
				paymentReceiptDTO.setUpdatedAt(rs.getString("updated_at"));
				paymentReceiptDTO.setPaymentTransactions(convertPaymentTransactions(rs.getString("payment_transaction_ids")));
				paymentReceiptDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.getPaymentAcknowledgeDTO(rs.getInt("payment_acknowledge_status_id")));
				paymentReceiptDTO.setPaymentReceiptType(PaymentReceiptTypeEM.getPaymentReceiptType(rs.getInt("payment_receipt_type_id")));

				List<ImageDetailsDTO> imageDetailsList = convertImageDetails(rs.getString("image_details_id"));
				paymentReceiptDTO.setImageDetails(imageDetailsList);
				
				paymentReceiptDTO.setRemarks(rs.getString("remarks"));
				paymentReceiptDTO.setActiveFlag(rs.getInt("active_flag"));

				UserDTO user = new UserDTO();
				user.setId(rs.getInt("user_id"));
				paymentReceiptDTO.setUser(user);

				UserDTO updatedBy = new UserDTO();
				updatedBy.setId(rs.getInt("updated_by"));
				paymentReceiptDTO.setUpdatedBy(updatedBy);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}
	
	public void updatePaymentReceiptImageDetails(AuthDTO authDTO, String referenceCode, String imageDetailsIds) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE payment_receipt SET image_details_id = ? WHERE code = ? AND namespace_id = ? AND active_flag = 1");
			preparedStatement.setString(++pindex, imageDetailsIds);
			preparedStatement.setString(++pindex, referenceCode);
			preparedStatement.setInt(++pindex, authDTO.getNamespace().getId());
			preparedStatement.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	private List<PaymentTransactionDTO> convertPaymentTransactions(String paymentTransactionIds) {
		List<PaymentTransactionDTO> paymentTransactions = new ArrayList<>();
		if (StringUtil.isNotNull(paymentTransactionIds)) {
			List<String> paymentTransactionList = Arrays.asList(paymentTransactionIds.split(Text.COMMA));
			for (String paymentTransactionId : paymentTransactionList) {
				if (StringUtil.isNull(paymentTransactionId) || Numeric.ZERO.equals(paymentTransactionId)) {
					continue;
				}
				PaymentTransactionDTO paymentTransaction = new PaymentTransactionDTO();
				paymentTransaction.setId(Integer.valueOf(paymentTransactionId));
				paymentTransactions.add(paymentTransaction);
			}
		}
		return paymentTransactions;
	}
	
	private List<ImageDetailsDTO> convertImageDetails(String imageDetailsIds) {
		List<ImageDetailsDTO> imageDetailsList = new ArrayList<ImageDetailsDTO>();
		if (StringUtil.isNotNull(imageDetailsIds)) {
			List<String> imageIds = Arrays.asList(imageDetailsIds.split(Text.COMMA));
			for (String imageId : imageIds) {
				if (imageId.equals(Numeric.ZERO)) {
					continue;
				}
				ImageDetailsDTO imegDetailsDTO = new ImageDetailsDTO();
				imegDetailsDTO.setId(Integer.valueOf(imageId));
				imageDetailsList.add(imegDetailsDTO);
			}
		}
		return imageDetailsList;
	}
}
