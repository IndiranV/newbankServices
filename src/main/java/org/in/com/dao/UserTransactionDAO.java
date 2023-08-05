package org.in.com.dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Cleanup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.PaymentVoucherDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.PaymentAcknowledgeEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.joda.time.DateTime;

public class UserTransactionDAO {

	public static synchronized void SaveUserTransaction(Connection connection, AuthDTO authDTO, UserDTO userDTO, UserTransactionDTO userTransactionDTO) {
		try {
			int pindex = 0;
			@Cleanup
			CallableStatement termSt = connection.prepareCall("{call EZEE_SP_USER_TRANSACTION_IUD(  ?,?,?,?,?, ?,?,?,?,?, ?,?,?,?,?)}");

			termSt.setInt(++pindex, authDTO.getNamespace().getId());
			termSt.setInt(++pindex, userDTO.getId());
			termSt.setBigDecimal(++pindex, userTransactionDTO.getTransactionAmount() != null ? userTransactionDTO.getTransactionAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
			termSt.setBigDecimal(++pindex, userTransactionDTO.getCommissionAmount() != null ? userTransactionDTO.getCommissionAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
			termSt.setBigDecimal(++pindex, userTransactionDTO.getCreditAmount() != null ? userTransactionDTO.getCreditAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
			termSt.setBigDecimal(++pindex, userTransactionDTO.getDebitAmount() != null ? userTransactionDTO.getDebitAmount().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
			termSt.setBigDecimal(++pindex, userTransactionDTO.getTdsTax() != null ? userTransactionDTO.getTdsTax().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO);
			termSt.setInt(++pindex, userTransactionDTO.getTransactionType().getId());
			termSt.setInt(++pindex, userTransactionDTO.getTransactionMode().getId());
			termSt.setInt(++pindex, userTransactionDTO.getRefferenceId());
			termSt.setString(++pindex, userTransactionDTO.getRefferenceCode());
			termSt.setInt(++pindex, authDTO.getUser().getId());
			termSt.setInt(++pindex, 0);
			termSt.registerOutParameter(++pindex, Types.DECIMAL);
			termSt.registerOutParameter(++pindex, Types.BIGINT);
			termSt.execute();
			BigDecimal currentBalance = termSt.getBigDecimal("pdlCurrentBalance");
			if (currentBalance == null) {
				throw new ServiceException(ErrorCode.TRANSACTION_FAIL_BALANCE_ISSUES);
			}
			authDTO.setCurrnetBalance(currentBalance);
			userTransactionDTO.setId(termSt.getInt("pitUserTransactionId"));
		}
		catch (ServiceException e) {
			System.out.println(authDTO.getNamespaceCode() + "  " + userTransactionDTO.getRefferenceCode() + "  " + userTransactionDTO.getRefferenceId());
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			System.out.println(authDTO.getNamespaceCode() + "  " + userTransactionDTO.getRefferenceCode() + "  " + userTransactionDTO.getRefferenceId());
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}

	public UserTransactionDTO getTransactionHistory(AuthDTO authDTO, UserDTO userDTO, DateTime fromDate, DateTime toDate) {
		UserTransactionDTO transactionDTOList = new UserTransactionDTO();
		List<UserTransactionDTO> list = new ArrayList<UserTransactionDTO>();
		// Till next day 00:00:00
		toDate = toDate.plusDays(1);

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = null;
			if (StringUtil.isNotNull(userDTO.getCode())) {
				selectPS = connection.prepareStatement("SELECT first_name,code,user_group_id,user_id,reference_id,transaction_type_id,transaction_amount,commission_amount,credit_amount,debit_amount,closing_balance,created_by,transaction_mode_id,tds_tax,reference_code,created_at FROM user_transaction tras,user usr WHERE usr.namespace_id = ? AND tras.namespace_id = usr.namespace_id AND tras.user_id = usr.id AND usr.code = ? AND created_at > ? AND created_at < ?");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setString(2, userDTO.getCode());
				selectPS.setString(3, fromDate.toString(DateUtil.JODA_DATE_FORMATE));
				selectPS.setString(4, toDate.toString(DateUtil.JODA_DATE_FORMATE));
			}
			else if (userDTO.getId() != 0) {
				selectPS = connection.prepareStatement("SELECT first_name,code,user_group_id,user_id,transaction_type_id,transaction_amount,commission_amount,credit_amount,debit_amount,closing_balance,created_by,transaction_mode_id,tds_tax,reference_code,created_at FROM user_transaction tras,user usr WHERE usr.namespace_id = ? AND tras.namespace_id = usr.namespace_id AND tras.user_id = usr.id AND usr.id = ? AND created_at > ? AND created_at < ?");
				selectPS.setInt(1, authDTO.getNamespace().getId());
				selectPS.setInt(2, userDTO.getId());
				selectPS.setString(3, fromDate.toString(DateUtil.JODA_DATE_FORMATE));
				selectPS.setString(4, toDate.toString(DateUtil.JODA_DATE_FORMATE));
			}
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserTransactionDTO transactionDTO = new UserTransactionDTO();
				transactionDTO.setCreditAmount(selectRS.getBigDecimal("credit_amount"));
				transactionDTO.setDebitAmount(selectRS.getBigDecimal("debit_amount"));
				transactionDTO.setTdsTax(selectRS.getBigDecimal("tds_tax"));
				transactionDTO.setTransactionAmount(selectRS.getBigDecimal("transaction_amount"));
				transactionDTO.setCommissionAmount(selectRS.getBigDecimal("commission_amount"));
				transactionDTO.setClosingBalanceAmount(selectRS.getBigDecimal("closing_balance"));
				transactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(selectRS.getInt("transaction_mode_id")));
				transactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(selectRS.getInt("transaction_type_id")));
				transactionDTO.setTransactionDate(selectRS.getString("created_at"));
				transactionDTO.setRefferenceCode(selectRS.getString("reference_code"));
				userDTO = new UserDTO();
				userDTO.setId(selectRS.getInt("user_id"));
				userDTO.setCode(selectRS.getString("code"));
				userDTO.setName(selectRS.getString("first_name"));

				GroupDTO groupDTO = new GroupDTO();
				groupDTO.setId(selectRS.getInt("user_group_id"));
				userDTO.setGroup(groupDTO);

				transactionDTO.setUser(userDTO);
				list.add(transactionDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		transactionDTOList.setList(list);
		return transactionDTOList;
	}

	public List<PaymentVoucherDTO> getPaymentVoucherUnPaid(AuthDTO authDTO, String userCode, DateTime fromDate, DateTime toDate, Boolean travelDateFlag, String scheduleCode, String organizationIds) {
		List<PaymentVoucherDTO> list = new ArrayList<PaymentVoucherDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_RPT_USER_PAYMENT_UN_PAID_VOUCHER( ?,?,?,?,?, ?,?)}");
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, fromDate.toString(DateUtil.JODA_DATE_FORMATE));
			callableStatement.setString(++pindex, toDate.toString(DateUtil.JODA_DATE_FORMATE));
			callableStatement.setInt(++pindex, travelDateFlag ? 1 : 0);
			callableStatement.setString(++pindex, userCode);
			callableStatement.setString(++pindex, scheduleCode);
			callableStatement.setString(++pindex, organizationIds);
			@Cleanup
			ResultSet resultSet = callableStatement.executeQuery();
			while (resultSet.next()) {
				PaymentVoucherDTO voucherDTO = new PaymentVoucherDTO();
				voucherDTO.setTransactionDate(resultSet.getString("transaction_date"));
				voucherDTO.setTravelDate(resultSet.getString("travel_date"));
				voucherDTO.setTransactionCode(resultSet.getString("transaction_code"));
				voucherDTO.setTicketCode(resultSet.getString("ticket_code"));
				voucherDTO.setScheduleNames(resultSet.getString("schedule_name"));
				voucherDTO.setTripCode(resultSet.getString("trip_code"));
				voucherDTO.setSeatNames(resultSet.getString("seat_name"));
				voucherDTO.setTicketAmount(resultSet.getBigDecimal("ticket_amount"));
				voucherDTO.setServiceTax(resultSet.getBigDecimal("ac_bus_tax"));
				voucherDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(resultSet.getInt("transaction_type_id")));
				StationDTO fromStation = new StationDTO();
				StationDTO toStation = new StationDTO();
				fromStation.setName(resultSet.getString("from_station_name"));
				toStation.setName(resultSet.getString("to_station_name"));
				voucherDTO.setFromStation(fromStation);
				voucherDTO.setToStation(toStation);
				voucherDTO.setCommissionAmount(resultSet.getBigDecimal("commission_amount"));
				voucherDTO.setAddonsAmount(resultSet.getBigDecimal("addons_amount"));
				voucherDTO.setRevokeCancelCommissionAmount(resultSet.getBigDecimal("revoke_commission_amount"));
				voucherDTO.setCancellationChargeAmount(resultSet.getBigDecimal("cancellation_charges"));
				voucherDTO.setSeatCount(resultSet.getInt("seat_count"));
				voucherDTO.setCancellationChargeCommissionAmount(resultSet.getBigDecimal("cancel_commission"));
				voucherDTO.setRefundAmount(resultSet.getBigDecimal("refund_amount"));
				voucherDTO.setNetAmount(resultSet.getBigDecimal("net_amount"));
				GroupDTO groupDTO = new GroupDTO();
				UserDTO userDTO = new UserDTO();
				userDTO.setCode(resultSet.getString("user_code"));
				userDTO.setName(resultSet.getString("user_first_name"));
				groupDTO.setName(resultSet.getString("user_group_name"));
				userDTO.setGroup(groupDTO);
				voucherDTO.setUser(userDTO);
				list.add(voucherDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void generatePaymentVoucher(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("insert into payment_transaction (code,namespace_id,user_id,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,user_transaction_id,commission_amount,ac_bus_tax,tds_tax,lookup_id,remarks,active_flag,updated_by,updated_at) values (?,?,?,?,? ,?,?,?,NOW(),?,?, NULL,?,?,?,?,?,1,? ,NOW())", PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(++pindex, paymentTransactionDTO.getCode());
			preparedStatement.setInt(++pindex, authDTO.getNamespace().getId());
			preparedStatement.setInt(++pindex, paymentTransactionDTO.getUser().getId());
			preparedStatement.setInt(++pindex, paymentTransactionDTO.getTransactionMode().getId());
			preparedStatement.setInt(++pindex, paymentTransactionDTO.getTransactionType().getId());
			preparedStatement.setBigDecimal(++pindex, paymentTransactionDTO.getTransactionAmount());
			preparedStatement.setString(++pindex, paymentTransactionDTO.getTransactionType().getCreditDebitFlag());
			preparedStatement.setString(++pindex, paymentTransactionDTO.getAmountReceivedDate());
			preparedStatement.setInt(++pindex, paymentTransactionDTO.getPaymentHandledByUser().getId());
			preparedStatement.setInt(++pindex, paymentTransactionDTO.getPaymentAcknowledge().getId());
			preparedStatement.setBigDecimal(++pindex, paymentTransactionDTO.getCommissionAmount());
			preparedStatement.setBigDecimal(++pindex, paymentTransactionDTO.getAcBusTax());
			preparedStatement.setBigDecimal(++pindex, paymentTransactionDTO.getTdsTax());
			preparedStatement.setInt(++pindex, paymentTransactionDTO.getLookupId());
			preparedStatement.setString(++pindex, paymentTransactionDTO.getRemarks());
			preparedStatement.setInt(++pindex, authDTO.getUser().getId());
			preparedStatement.execute();
			@Cleanup
			ResultSet rs = preparedStatement.getGeneratedKeys();
			if (rs.next()) {
				paymentTransactionDTO.setId(rs.getInt(1));
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

	public PaymentTransactionDTO generatePaymentVoucher(Connection connection, AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		try {
			int pindex = 0;
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("insert into payment_transaction (code,namespace_id,user_id,transaction_mode_id,transaction_type_id,transaction_amount,credit_debit_flag,amount_received_date,transaction_at,payment_handle_by,payment_acknowledge_status_id,user_transaction_id,commission_amount,ac_bus_tax,tds_tax,lookup_id,remarks,active_flag,updated_by,updated_at) values (?,?,?,?,? ,?,?,?,NOW(),?,?, NULL,?,?,?,?,?,1,? ,NOW())", PreparedStatement.RETURN_GENERATED_KEYS);
			preparedStatement.setString(++pindex, paymentTransactionDTO.getCode());
			preparedStatement.setInt(++pindex, authDTO.getNamespace().getId());
			preparedStatement.setInt(++pindex, paymentTransactionDTO.getUser().getId());
			preparedStatement.setInt(++pindex, paymentTransactionDTO.getTransactionMode().getId());
			preparedStatement.setInt(++pindex, paymentTransactionDTO.getTransactionType().getId());
			preparedStatement.setBigDecimal(++pindex, paymentTransactionDTO.getTransactionAmount());
			preparedStatement.setString(++pindex, paymentTransactionDTO.getTransactionType().getCreditDebitFlag());
			preparedStatement.setString(++pindex, paymentTransactionDTO.getAmountReceivedDate());
			preparedStatement.setInt(++pindex, paymentTransactionDTO.getPaymentHandledByUser().getId());
			preparedStatement.setInt(++pindex, PaymentAcknowledgeEM.PAYMENT_INITIATED.getId());
			preparedStatement.setBigDecimal(++pindex, paymentTransactionDTO.getCommissionAmount());
			preparedStatement.setBigDecimal(++pindex, paymentTransactionDTO.getAcBusTax());
			preparedStatement.setBigDecimal(++pindex, paymentTransactionDTO.getTdsTax());
			preparedStatement.setInt(++pindex, paymentTransactionDTO.getLookupId());
			preparedStatement.setString(++pindex, paymentTransactionDTO.getRemarks());
			preparedStatement.setInt(++pindex, authDTO.getUser().getId());
			preparedStatement.execute();
			@Cleanup
			ResultSet rs = preparedStatement.getGeneratedKeys();
			if (rs.next()) {
				paymentTransactionDTO.setId(rs.getInt(1));
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_GENERATE_VOUCHER);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return paymentTransactionDTO;
	}

	public List<PaymentVoucherDTO> getGeneratedPaymentVoucherDetails(AuthDTO authDTO, String paymentCode) {
		List<PaymentVoucherDTO> list = new ArrayList<PaymentVoucherDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			int pindex = 0;
			@Cleanup
			CallableStatement callableStatement = connection.prepareCall("{call  EZEE_SP_RPT_USER_PAYMENT_VIEW_VOUCHER( ?,?)}");
			callableStatement.setInt(++pindex, authDTO.getNamespace().getId());
			callableStatement.setString(++pindex, paymentCode);
			@Cleanup
			ResultSet resultSet = callableStatement.executeQuery();
			while (resultSet.next()) {
				PaymentVoucherDTO voucherDTO = new PaymentVoucherDTO();
				voucherDTO.setTransactionDate(resultSet.getString("transaction_date"));
				voucherDTO.setTravelDate(resultSet.getString("travel_date"));
				voucherDTO.setTransactionCode(resultSet.getString("transaction_code"));
				voucherDTO.setTicketCode(resultSet.getString("ticket_code"));
				voucherDTO.setScheduleNames(resultSet.getString("schedule_name"));
				voucherDTO.setTripCode(resultSet.getString("trip_code"));
				voucherDTO.setSeatNames(resultSet.getString("seat_name"));
				voucherDTO.setTicketAmount(resultSet.getBigDecimal("ticket_amount"));
				voucherDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(resultSet.getInt("transaction_type_id")));
				StationDTO fromStation = new StationDTO();
				StationDTO toStation = new StationDTO();
				fromStation.setName(resultSet.getString("from_station_name"));
				toStation.setName(resultSet.getString("to_station_name"));
				voucherDTO.setFromStation(fromStation);
				voucherDTO.setToStation(toStation);
				voucherDTO.setCommissionAmount(resultSet.getBigDecimal("commission_amount"));
				voucherDTO.setRevokeCancelCommissionAmount(resultSet.getBigDecimal("revoke_commission_amount"));
				voucherDTO.setCancellationChargeAmount(resultSet.getBigDecimal("cancellation_charges"));
				voucherDTO.setAddonsAmount(resultSet.getBigDecimal("addons_amount"));
				voucherDTO.setRefundAmount(resultSet.getBigDecimal("refund_amount"));
				voucherDTO.setSeatCount(resultSet.getInt("seat_count"));
				voucherDTO.setCancellationChargeCommissionAmount(resultSet.getBigDecimal("cancel_commission"));
				voucherDTO.setRefundAmount(resultSet.getBigDecimal("refund_amount"));
				voucherDTO.setNetAmount(resultSet.getBigDecimal("net_amount"));
				UserDTO user = new UserDTO();
				user.setCode(resultSet.getString("user_code"));
				voucherDTO.setUser(user);
				list.add(voucherDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public Map<Integer, List<UserTransactionDTO>> getUserTransaction(AuthDTO authDTO, DateTime fromDate, DateTime toDate) {
		Map<Integer, List<UserTransactionDTO>> transactionMap = new HashMap<Integer, List<UserTransactionDTO>>();

		toDate = toDate.plusDays(1);
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT tra.id, user_id, usr.code, first_name, transaction_type_id, transaction_amount, commission_amount, credit_amount, debit_amount, closing_balance, transaction_mode_id, tds_tax, reference_code, created_at FROM user_transaction tra, user usr WHERE tra.namespace_id = ? AND usr.namespace_id = tra.namespace_id AND tra.user_id = usr.id AND created_at > ? AND created_at < ?");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setString(2, fromDate.toString(DateUtil.JODA_DATE_FORMATE));
			selectPS.setString(3, toDate.toString(DateUtil.JODA_DATE_FORMATE));
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserTransactionDTO transactionDTO = new UserTransactionDTO();
				transactionDTO.setId(selectRS.getInt("tra.id"));
				transactionDTO.setCreditAmount(selectRS.getBigDecimal("credit_amount"));
				transactionDTO.setDebitAmount(selectRS.getBigDecimal("debit_amount"));
				transactionDTO.setTdsTax(selectRS.getBigDecimal("tds_tax"));
				transactionDTO.setTransactionAmount(selectRS.getBigDecimal("transaction_amount"));
				transactionDTO.setCommissionAmount(selectRS.getBigDecimal("commission_amount"));
				transactionDTO.setClosingBalanceAmount(selectRS.getBigDecimal("closing_balance"));
				transactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(selectRS.getInt("transaction_mode_id")));
				transactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(selectRS.getInt("transaction_type_id")));
				transactionDTO.setTransactionDate(selectRS.getString("created_at"));
				transactionDTO.setRefferenceCode(selectRS.getString("reference_code"));

				UserDTO userDTO = new UserDTO();
				userDTO.setId(selectRS.getInt("user_id"));
				userDTO.setCode(selectRS.getString("usr.code"));
				userDTO.setName(selectRS.getString("first_name"));
				transactionDTO.setUser(userDTO);

				if (transactionMap.get(userDTO.getId()) != null) {
					List<UserTransactionDTO> transactionList = transactionMap.get(userDTO.getId());
					transactionList.add(transactionDTO);
					transactionMap.put(userDTO.getId(), transactionList);
				}
				else {
					List<UserTransactionDTO> list = new ArrayList<UserTransactionDTO>();
					list.add(transactionDTO);
					transactionMap.put(userDTO.getId(), list);
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return transactionMap;
	}

	public List<UserTransactionDTO> getUserTransactions(AuthDTO authDTO, UserTransactionDTO userTransactionDTO) {
		List<UserTransactionDTO> transactions = new ArrayList<UserTransactionDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT id, user_id, transaction_type_id, transaction_amount, commission_amount, credit_amount, debit_amount, closing_balance, transaction_mode_id, tds_tax, reference_code, created_at FROM user_transaction WHERE namespace_id = ? AND id >= ? AND user_id = ? AND active_flag = 1");
			selectPS.setInt(1, authDTO.getNamespace().getId());
			selectPS.setInt(2, userTransactionDTO.getId());
			selectPS.setInt(3, userTransactionDTO.getUser().getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			while (selectRS.next()) {
				UserTransactionDTO transactionDTO = new UserTransactionDTO();
				transactionDTO.setId(selectRS.getInt("id"));
				transactionDTO.setCreditAmount(selectRS.getBigDecimal("credit_amount"));
				transactionDTO.setDebitAmount(selectRS.getBigDecimal("debit_amount"));
				transactionDTO.setTdsTax(selectRS.getBigDecimal("tds_tax"));
				transactionDTO.setTransactionAmount(selectRS.getBigDecimal("transaction_amount"));
				transactionDTO.setCommissionAmount(selectRS.getBigDecimal("commission_amount"));
				transactionDTO.setClosingBalanceAmount(selectRS.getBigDecimal("closing_balance"));
				transactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(selectRS.getInt("transaction_mode_id")));
				transactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(selectRS.getInt("transaction_type_id")));
				transactionDTO.setTransactionDate(selectRS.getString("created_at"));
				transactionDTO.setRefferenceCode(selectRS.getString("reference_code"));

				UserDTO userDTO = new UserDTO();
				userDTO.setId(selectRS.getInt("user_id"));
				transactionDTO.setUser(userDTO);

				transactions.add(transactionDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return transactions;
	}

	public void updateClosingBalance(AuthDTO authDTO, List<UserTransactionDTO> userTransactions) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("UPDATE user_transaction SET closing_balance = ?, created_by = ? WHERE namespace_id = ? AND id = ?");
			PreparedStatement ps = connection.prepareStatement("UPDATE user_balance SET current_balance = ?, user_transaction_id = ? WHERE namespace_id = ? AND user_id = ?");

			for (UserTransactionDTO userTransactionDTO : userTransactions) {
				selectPS.setBigDecimal(1, userTransactionDTO.getClosingBalanceAmount());
				selectPS.setInt(2, authDTO.getUser().getId());
				selectPS.setInt(3, authDTO.getNamespace().getId());
				selectPS.setInt(4, userTransactionDTO.getId());

				ps.setBigDecimal(1, userTransactionDTO.getClosingBalanceAmount());
				ps.setInt(2, userTransactionDTO.getId());
				ps.setInt(3, authDTO.getNamespace().getId());
				ps.setInt(4, userTransactionDTO.getUser().getId());

				selectPS.addBatch();
				ps.addBatch();
			}

			selectPS.executeBatch();
			ps.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void getUserTransaction(AuthDTO authDTO, UserTransactionDTO userTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT user_id,reference_id,transaction_type_id,transaction_amount,commission_amount,credit_amount,debit_amount,closing_balance,created_by,transaction_mode_id,tds_tax,reference_code,created_at FROM user_transaction WHERE id = ? AND namespace_id = ? AND active_flag = 1");
			selectPS.setInt(1, userTransactionDTO.getId());
			selectPS.setInt(2, authDTO.getNamespace().getId());

			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				userTransactionDTO.setCreditAmount(selectRS.getBigDecimal("credit_amount"));
				userTransactionDTO.setDebitAmount(selectRS.getBigDecimal("debit_amount"));
				userTransactionDTO.setTdsTax(selectRS.getBigDecimal("tds_tax"));
				userTransactionDTO.setTransactionAmount(selectRS.getBigDecimal("transaction_amount"));
				userTransactionDTO.setCommissionAmount(selectRS.getBigDecimal("commission_amount"));
				userTransactionDTO.setClosingBalanceAmount(selectRS.getBigDecimal("closing_balance"));
				userTransactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(selectRS.getInt("transaction_mode_id")));
				userTransactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(selectRS.getInt("transaction_type_id")));
				userTransactionDTO.setTransactionDate(selectRS.getString("created_at"));
				userTransactionDTO.setRefferenceCode(selectRS.getString("reference_code"));

				UserDTO userDTO = new UserDTO();
				userDTO.setId(selectRS.getInt("user_id"));
				userTransactionDTO.setUser(userDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
