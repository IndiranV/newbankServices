package org.in.com.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.in.com.dto.OrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.exception.DAOException;
import org.in.com.exception.ErrorCode;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Cleanup;

public class PaymentGatewayLoggingDAO {

	Logger logger = LoggerFactory.getLogger(PaymentGatewayLoggingDAO.class);

	public void pgresponselogging(OrderDTO order) {

		try {
			StringBuilder log = new StringBuilder();
			log.append("Request sent=").append(order.getAmount()).append(",").append(order.getTransactionCode()).append("||Response Rcvd=").append(order.getResponseRecevied());

			if (log == null || StringUtil.isNull(order.getTransactionCode())) {
				logger.error("Null for logdata - " + log + " -code -" + order.getTransactionCode());
				throw new DAOException(ErrorCode.NOT_NULL_DATA_FOR_PERSITS);
			}
			insertLog(order.getTransactionCode(), log);
		}
		catch (Exception e) {
			logger.error(order.getTransactionCode(), e);
		}
	}

	public void pgresponselogging(TransactionEnquiryDTO transactionEnquiry) {

		try {
			// TODO Create a service for logging and reuse this in payment
			// request service and here
			StringBuilder logging = new StringBuilder();
			logging.append("Request sent=").append(transactionEnquiry.getAmount()).append(",").append(transactionEnquiry.getTransactionCode()).append("||Response Rcvd=").append(transactionEnquiry.getResponseRecevied()).append("||Request map data").append(transactionEnquiry.getResponseRecevied() != null ? transactionEnquiry.getResponseRecevied().toString() : "null");

			if (logging == null || transactionEnquiry.getTransactionCode() == null) {
				logger.error("Null for logdata - " + logging + " -code -" + transactionEnquiry.getTransactionCode());
				throw new DAOException(ErrorCode.NOT_NULL_DATA_FOR_PERSITS);
			}
			insertLog(transactionEnquiry.getTransactionCode(), logging);
		}
		catch (Exception e) {
			logger.error(transactionEnquiry.getTransactionCode(), e);
		}
	}

	public void pgRefundLogging(RefundDTO refundDTO) {

		try {
			// TODO Create a service for logging and reuse this in payment
			// request service and here
			StringBuilder logging = new StringBuilder();
			logging.append("Request sent=").append(refundDTO.getAmount()).append(",").append(refundDTO.getTransactionCode()).append("||Response Rcvd=").append(refundDTO.getResponseRecevied()).append("||Request map data").append(refundDTO.getResponseRecevied() != null ? refundDTO.getResponseRecevied().toString() : "null");

			if (logging == null || refundDTO.getTransactionCode() == null) {
				logger.error("Null for logdata - " + logging + " -code -" + refundDTO.getTransactionCode());
				throw new DAOException(ErrorCode.NOT_NULL_DATA_FOR_PERSITS);
			}
			insertLog(refundDTO.getTransactionCode(), logging);
		}
		catch (Exception e) {
			logger.error(refundDTO.getTransactionCode(), e);
		}
	}

	private void insertLog(String transactionId, StringBuilder log) throws SQLException {

		@Cleanup
		Connection connection = ConnectDAO.getConnection();
		@Cleanup
		PreparedStatement insertStatement = connection.prepareStatement("insert into payment_gateway_logging (code,log_data,created_at) values(?,?,NOW())");
		insertStatement.setString(1, transactionId);
		insertStatement.setString(2, log.toString());
		insertStatement.executeUpdate();
	}

}
