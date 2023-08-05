package org.in.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;

import lombok.Cleanup;

public class TicketTransactionDAO extends BaseDAO {

	public void getTicketTransaction(AuthDTO authDTO, TicketDTO ticketDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id,code,user_id,transaction_mode_id,transaction_seat_count,transaction_amount,commission_amount,extra_commission_amount,ac_bus_tax,tds_tax,addons_amount,payment_transaction_id,updated_at FROM ticket_transaction WHERE namespace_id = ? AND active_flag = 1 AND ticket_id = ? AND transaction_type_id = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setInt(2, ticketDTO.getId());
			ps.setInt(3, ticketDTO.getTicketXaction().getTransactionType().getId());
			@Cleanup
			ResultSet resultSet = ps.executeQuery();
			if (resultSet.next()) {
				ticketDTO.getTicketXaction().setId(resultSet.getInt("id"));
				ticketDTO.getTicketXaction().setCode(resultSet.getString("code"));
				ticketDTO.getTicketXaction().setTransSeatCount(resultSet.getInt("transaction_seat_count"));
				ticketDTO.getTicketXaction().setTransactionAmount(resultSet.getBigDecimal("transaction_amount"));
				ticketDTO.getTicketXaction().setCommissionAmount(resultSet.getBigDecimal("commission_amount"));
				ticketDTO.getTicketXaction().setExtraCommissionAmount(resultSet.getBigDecimal("extra_commission_amount"));
				ticketDTO.getTicketXaction().setTdsTax(resultSet.getBigDecimal("tds_tax"));
				ticketDTO.getTicketXaction().setAcBusTax(resultSet.getBigDecimal("ac_bus_tax"));
				ticketDTO.getTicketXaction().setAddonsAmount(resultSet.getBigDecimal("addons_amount"));
				ticketDTO.getTicketXaction().setTransactionMode(TransactionModeEM.getTransactionModeEM(resultSet.getInt("transaction_mode_id")));

				PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
				paymentTransactionDTO.setId(resultSet.getInt("payment_transaction_id"));
				ticketDTO.getTicketXaction().setPaymentTrans(paymentTransactionDTO);

				UserDTO userDTO = new UserDTO();
				userDTO.setId(resultSet.getInt("user_id"));
				ticketDTO.getTicketXaction().setUserDTO(userDTO);
				ticketDTO.getTicketXaction().setUpdatedAt(DateUtil.getDateTime(resultSet.getString("updated_at")));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public TicketTransactionDTO getTicketTransactionDetails(AuthDTO authDTO, String transactionCode, UserDTO userDTO) {
		TicketTransactionDTO ticketTransactionDTO = null;

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id,code,user_id,transaction_seat_count,transaction_amount,commission_amount,extra_commission_amount,ac_bus_tax,tds_tax,transaction_mode_id,transaction_type_id ,payment_transaction_id FROM ticket_transaction WHERE namespace_id = ? AND active_flag = 1 AND code = ? AND user_id = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, transactionCode);
			ps.setInt(3, userDTO.getId());
			@Cleanup
			ResultSet resultSet = ps.executeQuery();
			if (resultSet.next()) {
				ticketTransactionDTO = new TicketTransactionDTO();
				ticketTransactionDTO.setId(resultSet.getInt("id"));
				ticketTransactionDTO.setCode(resultSet.getString("code"));
				UserDTO userdto = new UserDTO();
				userdto.setId(resultSet.getInt("user_id"));
				ticketTransactionDTO.setUserDTO(userdto);
				ticketTransactionDTO.setTransSeatCount(resultSet.getInt("transaction_seat_count"));
				ticketTransactionDTO.setTransactionAmount(resultSet.getBigDecimal("transaction_amount"));
				ticketTransactionDTO.setCommissionAmount(resultSet.getBigDecimal("commission_amount"));
				ticketTransactionDTO.setExtraCommissionAmount(resultSet.getBigDecimal("extra_commission_amount"));
				ticketTransactionDTO.setTdsTax(resultSet.getBigDecimal("tds_tax"));
				ticketTransactionDTO.setAcBusTax(resultSet.getBigDecimal("ac_bus_tax"));
				ticketTransactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(resultSet.getInt("transaction_mode_id")));
				ticketTransactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(resultSet.getInt("transaction_type_id")));
				PaymentTransactionDTO paymentTransDTO = new PaymentTransactionDTO();
				paymentTransDTO.setId(resultSet.getInt("payment_transaction_id"));
				ticketTransactionDTO.setPaymentTrans(paymentTransDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ticketTransactionDTO;
	}

	public void getTicketCancelTransactionDetails(AuthDTO authDTO, TicketTransactionDTO transactionDTO, UserDTO userDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT charge_amount,revoke_commission,cancel_commission,tds_tax,charge_tax_amount,refund_amount FROM ticket_cancel_transaction WHERE user_id = ? AND ticket_transaction_id=?");
			ps.setInt(1, userDTO.getId());
			ps.setInt(2, transactionDTO.getId());
			@Cleanup
			ResultSet resultSet = ps.executeQuery();
			if (resultSet.next()) {
				transactionDTO.setCancellationChargeAmount(resultSet.getBigDecimal("charge_amount"));
				transactionDTO.setCancellationCommissionAmount(resultSet.getBigDecimal("revoke_commission"));
				transactionDTO.setCancellationChargeCommissionAmount(resultSet.getBigDecimal("cancel_commission"));
				transactionDTO.setCancelTdsTax(resultSet.getBigDecimal("tds_tax"));
				transactionDTO.setCancellationChargeTax(resultSet.getBigDecimal("charge_tax_amount"));
				transactionDTO.setRefundAmount(resultSet.getBigDecimal("refund_amount"));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void updatePaymentTransactionId(Connection connection, AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		try {
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE ticket_transaction SET payment_transaction_id = ?  WHERE code = ? AND namespace_id = ? AND active_flag = 1 ");
			preparedStatement.setInt(1, paymentTransactionDTO.getId());
			preparedStatement.setString(2, paymentTransactionDTO.getTransactionCode());
			preparedStatement.setInt(3, authDTO.getNamespace().getId());
			preparedStatement.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<TicketTransactionDTO> getTicketTransactionFindPaymentId(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		List<TicketTransactionDTO> list = new ArrayList<TicketTransactionDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("SELECT code FROM ticket_transaction WHERE payment_transaction_id = ? AND namespace_id = ? AND active_flag = 1 ");
			preparedStatement.setInt(1, paymentTransactionDTO.getId());
			preparedStatement.setInt(2, authDTO.getNamespace().getId());
			@Cleanup
			ResultSet resultSet = preparedStatement.executeQuery();
			while (resultSet.next()) {
				TicketTransactionDTO transactionDTO = new TicketTransactionDTO();
				transactionDTO.setCode(resultSet.getString("code"));
				list.add(transactionDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return list;
	}

	public void insertTicketTransaction(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			int psCount = 0;
			ticketDTO.getTicketXaction().setCode(ticketDTO.getTicketXaction().getTransactionCode());
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("INSERT INTO ticket_transaction (code,namespace_id,user_id,ticket_id,transaction_type_id,transaction_seat_count,transaction_amount,commission_amount,extra_commission_amount,addons_amount,ac_bus_tax,tds_tax,transaction_mode_id,active_flag,updated_by,updated_at) VALUES (?,?,?,?,? ,?,?,?,?,?, ?,?,?,1,?, NOW())", PreparedStatement.RETURN_GENERATED_KEYS);
			ps.setString(++psCount, ticketDTO.getTicketXaction().getCode());
			ps.setInt(++psCount, authDTO.getNamespace().getId());
			ps.setInt(++psCount, ticketDTO.getTicketUser().getId());
			ps.setInt(++psCount, ticketDTO.getId());
			ps.setInt(++psCount, ticketDTO.getTicketXaction().getTransactionType().getId());
			ps.setInt(++psCount, ticketDTO.getTicketDetails().size());
			ps.setBigDecimal(++psCount, ticketDTO.getTicketXaction().getTransactionAmount());
			ps.setBigDecimal(++psCount, ticketDTO.getTicketXaction().getCommissionAmount());
			ps.setBigDecimal(++psCount, ticketDTO.getTicketXaction().getExtraCommissionAmount());
			ps.setBigDecimal(++psCount, ticketDTO.getTicketXaction().getAddonsAmount());
			ps.setBigDecimal(++psCount, ticketDTO.getTicketXaction().getAcBusTax());
			ps.setBigDecimal(++psCount, ticketDTO.getTicketXaction().getTdsTax());
			ps.setInt(++psCount, ticketDTO.getTicketXaction().getTransactionMode().getId());
			ps.setInt(++psCount, authDTO.getUser().getId());
			ps.executeUpdate();
			@Cleanup
			ResultSet rs = ps.getGeneratedKeys();
			if (rs.next()) {
				ticketDTO.getTicketXaction().setId(rs.getInt(1));
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
			}
		}
		catch (ServiceException e) {
			throw new ServiceException(e.getErrorCode());
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public void insertTicketCancelTransaction(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		int pindex = 0;
		try {
			@Cleanup
			PreparedStatement txactionPS = connection.prepareStatement("INSERT INTO ticket_cancel_transaction(user_id,ticket_id,ticket_transaction_id,charge_amount,revoke_commission,cancel_commission,tds_tax,charge_tax_amount,refund_amount,ticket_status_id,device_medium,refund_status,remarks,active_flag,updated_by,updated_at) VALUES(?,?,?,?,?, ?,?,?,?,?,? ,?,?,1,?,NOW())");
			txactionPS.setInt(++pindex, ticketDTO.getTicketUser().getId());
			txactionPS.setInt(++pindex, ticketDTO.getId());
			txactionPS.setInt(++pindex, ticketDTO.getTicketXaction().getId());
			txactionPS.setBigDecimal(++pindex, ticketDTO.getCancellationCharges());
			txactionPS.setBigDecimal(++pindex, ticketDTO.getTicketXaction().getCommissionAmount());
			txactionPS.setBigDecimal(++pindex, ticketDTO.getTicketXaction().getCancellationCommissionAmount());
			txactionPS.setBigDecimal(++pindex, ticketDTO.getTicketXaction().getCancelTdsTax());
			txactionPS.setBigDecimal(++pindex, ticketDTO.getTicketXaction().getCancellationChargeTax());
			txactionPS.setBigDecimal(++pindex, ticketDTO.getTicketXaction().getRefundAmount());
			txactionPS.setInt(++pindex, ticketDTO.getTicketStatus().getId());
			txactionPS.setInt(++pindex, authDTO.getDeviceMedium().getId());
			txactionPS.setInt(++pindex, ticketDTO.getTicketXaction().getRefundStatus().getId());
			txactionPS.setString(++pindex, ticketDTO.getTicketXaction().getRemarks());
			txactionPS.setInt(++pindex, authDTO.getUser().getId());
			txactionPS.execute();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}

	public void insertTicketCancellationDetails(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			PreparedStatement ticketDetailsPS = connection.prepareStatement("INSERT INTO ticket_cancel_detail(ticket_id,ticket_detail_id,ticket_status_id,ticket_transaction_id,cancellation_charges,refund_amount,active_flag,updated_by,updated_at) VALUES(?,?,?,?,?,?,1,?,NOW())");
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				ticketDetailsPS.setInt(1, ticketDTO.getId());
				ticketDetailsPS.setInt(2, ticketDetailsDTO.getId());
				ticketDetailsPS.setInt(3, ticketDTO.getTicketStatus().getId());
				ticketDetailsPS.setInt(4, ticketDTO.getTicketXaction().getId());
				ticketDetailsPS.setBigDecimal(5, ticketDetailsDTO.getCancellationCharges());
				ticketDetailsPS.setBigDecimal(6, ticketDetailsDTO.getRefundAmount());
				ticketDetailsPS.setInt(7, authDTO.getUser().getId());
				ticketDetailsPS.addBatch();
			}
			ticketDetailsPS.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}

	}

	public void rejectTripCancelTransaction(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			PreparedStatement ticketCancelPS = connection.prepareStatement("UPDATE ticket_cancel_transaction tct, ticket_transaction tt SET tct.active_flag = 2, tt.active_flag = 2 WHERE tct.ticket_id = ? AND tct.ticket_id = tt.ticket_id AND tct.ticket_status_id = 9 AND tct.ticket_transaction_id = tt.id AND tct.active_flag = 1 AND tt.active_flag = 1");
			ticketCancelPS.setInt(1, ticketDTO.getId());
			ticketCancelPS.executeUpdate();

			@Cleanup
			PreparedStatement ticketCancelDetailsPS = connection.prepareStatement("UPDATE ticket_cancel_detail SET active_flag = 2 WHERE ticket_id = ? AND ticket_status_id = 9 AND active_flag = 1");
			ticketCancelDetailsPS.setInt(1, ticketDTO.getId());
			ticketCancelDetailsPS.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
	}

	public void updateTripCancelTicketDetail(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			PreparedStatement ticketCancelDetailsPS = connection.prepareStatement("UPDATE ticket_cancel_detail SET ticket_status_id = ?, updated_at = NOW() WHERE ticket_id = ? AND ticket_status_id = 9 AND active_flag = 1");
			ticketCancelDetailsPS.setInt(1, ticketDTO.getTicketStatus().getId());
			ticketCancelDetailsPS.setInt(2, ticketDTO.getId());
			ticketCancelDetailsPS.executeUpdate();
			@Cleanup
			PreparedStatement ticketCancelTransactionPS = connection.prepareStatement("UPDATE ticket_cancel_transaction SET ticket_status_id = ?, updated_at = NOW() WHERE ticket_id = ? AND ticket_status_id = 9 AND active_flag = 1");
			ticketCancelTransactionPS.setInt(1, ticketDTO.getTicketStatus().getId());
			ticketCancelTransactionPS.setInt(2, ticketDTO.getId());
			ticketCancelTransactionPS.executeUpdate();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());

		}
	}

	public boolean isTripCancelInitiated(AuthDTO authDTO, TicketDTO ticketDTO) {
		boolean isTripCancelInitiated = false;
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement selectPS = connection.prepareStatement("SELECT 1 FROM ticket_cancel_transaction WHERE ticket_id = ? AND ticket_status_id = 9 AND active_flag = 1");
			selectPS.setInt(1, ticketDTO.getId());
			@Cleanup
			ResultSet selectRS = selectPS.executeQuery();
			if (selectRS.next()) {
				isTripCancelInitiated = true;
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return isTripCancelInitiated;
	}

	public TicketTransactionDTO getTicketTransaction(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO) {
		TicketTransactionDTO ticketTransactionDTO = null;

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id,code,user_id,transaction_seat_count,transaction_amount,commission_amount,extra_commission_amount,ac_bus_tax,tds_tax,addons_amount,transaction_mode_id,transaction_type_id ,payment_transaction_id FROM ticket_transaction WHERE namespace_id = ? AND active_flag = 1 AND code = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setString(2, paymentTransactionDTO.getCode());
			@Cleanup
			ResultSet resultSet = ps.executeQuery();
			if (resultSet.next()) {
				ticketTransactionDTO = new TicketTransactionDTO();
				ticketTransactionDTO.setId(resultSet.getInt("id"));
				ticketTransactionDTO.setCode(resultSet.getString("code"));
				UserDTO userdto = new UserDTO();
				userdto.setId(resultSet.getInt("user_id"));
				ticketTransactionDTO.setUserDTO(userdto);
				ticketTransactionDTO.setTransSeatCount(resultSet.getInt("transaction_seat_count"));
				ticketTransactionDTO.setTransactionAmount(resultSet.getBigDecimal("transaction_amount"));
				ticketTransactionDTO.setCommissionAmount(resultSet.getBigDecimal("commission_amount"));
				ticketTransactionDTO.setExtraCommissionAmount(resultSet.getBigDecimal("extra_commission_amount"));
				ticketTransactionDTO.setAddonsAmount(resultSet.getBigDecimal("addons_amount"));
				ticketTransactionDTO.setTdsTax(resultSet.getBigDecimal("tds_tax"));
				ticketTransactionDTO.setAcBusTax(resultSet.getBigDecimal("ac_bus_tax"));
				ticketTransactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(resultSet.getInt("transaction_mode_id")));
				ticketTransactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(resultSet.getInt("transaction_type_id")));
				PaymentTransactionDTO paymentTransDTO = new PaymentTransactionDTO();
				paymentTransDTO.setId(resultSet.getInt("payment_transaction_id"));
				ticketTransactionDTO.setPaymentTrans(paymentTransDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ticketTransactionDTO;
	}

	public void updatePaymentTransactionId(AuthDTO authDTO, List<PaymentTransactionDTO> paymentTransactionList) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement preparedStatement = connection.prepareStatement("UPDATE ticket_transaction SET payment_transaction_id = ?  WHERE code = ? AND namespace_id = ? AND active_flag = 1 ");
			for (PaymentTransactionDTO paymentTransactionDTO : paymentTransactionList) {
				preparedStatement.setInt(1, paymentTransactionDTO.getId());
				preparedStatement.setString(2, paymentTransactionDTO.getCode());
				preparedStatement.setInt(3, authDTO.getNamespace().getId());
				preparedStatement.addBatch();
			}
			preparedStatement.executeBatch();
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	public List<TicketTransactionDTO> getTicketTransactionV2(AuthDTO authDTO, TicketDTO ticketDTO) {
		List<TicketTransactionDTO> list = new ArrayList<TicketTransactionDTO>();
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ps = connection.prepareStatement("SELECT id,code,user_id,transaction_type_id,transaction_mode_id,transaction_seat_count,transaction_amount,commission_amount,extra_commission_amount,ac_bus_tax,tds_tax,addons_amount,payment_transaction_id FROM ticket_transaction WHERE namespace_id = ? AND active_flag = 1 AND ticket_id = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setInt(2, ticketDTO.getId());
			@Cleanup
			ResultSet resultSet = ps.executeQuery();
			while (resultSet.next()) {
				TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
				ticketTransactionDTO.setId(resultSet.getInt("id"));
				ticketTransactionDTO.setCode(resultSet.getString("code"));
				ticketTransactionDTO.setTransSeatCount(resultSet.getInt("transaction_seat_count"));
				ticketTransactionDTO.setTransactionAmount(resultSet.getBigDecimal("transaction_amount"));
				ticketTransactionDTO.setCommissionAmount(resultSet.getBigDecimal("commission_amount"));
				ticketTransactionDTO.setExtraCommissionAmount(resultSet.getBigDecimal("extra_commission_amount"));
				ticketTransactionDTO.setTdsTax(resultSet.getBigDecimal("tds_tax"));
				ticketTransactionDTO.setAcBusTax(resultSet.getBigDecimal("ac_bus_tax"));
				ticketTransactionDTO.setAddonsAmount(resultSet.getBigDecimal("addons_amount"));
				ticketTransactionDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(resultSet.getInt("transaction_type_id")));
				ticketTransactionDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(resultSet.getInt("transaction_mode_id")));

				PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
				paymentTransactionDTO.setId(resultSet.getInt("payment_transaction_id"));
				ticketTransactionDTO.setPaymentTrans(paymentTransactionDTO);

				UserDTO userDTO = new UserDTO();
				userDTO.setId(resultSet.getInt("user_id"));
				ticketTransactionDTO.setUserDTO(userDTO);
				list.add(ticketTransactionDTO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}
	
	public void getTicketTransactionByTicketCode(AuthDTO authDTO, TicketDTO ticketDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			@Cleanup
			PreparedStatement ticketPS = connection.prepareStatement("SELECT id FROM ticket WHERE namespace_id = ? AND code = ? AND active_flag = 1");
			ticketPS.setInt(1, authDTO.getNamespace().getId());
			ticketPS.setString(2, ticketDTO.getCode());
			@Cleanup
			ResultSet ticketRS = ticketPS.executeQuery();
			if (ticketRS.next()) {
				ticketDTO.setId(ticketRS.getInt("id"));
			}
			PreparedStatement ps = connection.prepareStatement("SELECT id,code,user_id,transaction_mode_id,transaction_seat_count,transaction_amount,commission_amount,extra_commission_amount,ac_bus_tax,tds_tax,addons_amount,payment_transaction_id,updated_at FROM ticket_transaction WHERE namespace_id = ? AND active_flag = 1 AND ticket_id = ? AND transaction_type_id = ?");
			ps.setInt(1, authDTO.getNamespace().getId());
			ps.setInt(2, ticketDTO.getId());
			ps.setInt(3, ticketDTO.getTicketXaction().getTransactionType().getId());
			@Cleanup
			ResultSet resultSet = ps.executeQuery();
			if (resultSet.next()) {
				ticketDTO.getTicketXaction().setId(resultSet.getInt("id"));
				ticketDTO.getTicketXaction().setCode(resultSet.getString("code"));
				ticketDTO.getTicketXaction().setTransSeatCount(resultSet.getInt("transaction_seat_count"));
				ticketDTO.getTicketXaction().setTransactionAmount(resultSet.getBigDecimal("transaction_amount"));
				ticketDTO.getTicketXaction().setCommissionAmount(resultSet.getBigDecimal("commission_amount"));
				ticketDTO.getTicketXaction().setExtraCommissionAmount(resultSet.getBigDecimal("extra_commission_amount"));
				ticketDTO.getTicketXaction().setTdsTax(resultSet.getBigDecimal("tds_tax"));
				ticketDTO.getTicketXaction().setAcBusTax(resultSet.getBigDecimal("ac_bus_tax"));
				ticketDTO.getTicketXaction().setAddonsAmount(resultSet.getBigDecimal("addons_amount"));
				ticketDTO.getTicketXaction().setTransactionMode(TransactionModeEM.getTransactionModeEM(resultSet.getInt("transaction_mode_id")));

				PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
				paymentTransactionDTO.setId(resultSet.getInt("payment_transaction_id"));
				ticketDTO.getTicketXaction().setPaymentTrans(paymentTransactionDTO);

				UserDTO userDTO = new UserDTO();
				userDTO.setId(resultSet.getInt("user_id"));
				ticketDTO.getTicketXaction().setUserDTO(userDTO);
				ticketDTO.getTicketXaction().setUpdatedAt(DateUtil.getDateTime(resultSet.getString("updated_at")));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
