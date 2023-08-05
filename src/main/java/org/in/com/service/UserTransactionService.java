package org.in.com.service;

import java.sql.Connection;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.PaymentVoucherDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTransactionDTO;
import org.joda.time.DateTime;

public interface UserTransactionService {

	public UserTransactionDTO getTransactionHistory(AuthDTO authDTO, UserDTO userDTO, DateTime fromDate, DateTime toDate);

	public List<PaymentVoucherDTO> getPaymentVoucherUnPaid(AuthDTO authDTO, String userCode, DateTime fromDate, DateTime toDate, Boolean useTravelDate, String schedule);

	public List<PaymentVoucherDTO> getGeneratedPaymentVoucherDetails(AuthDTO authDTO, String paymentCode);

	public PaymentTransactionDTO generatePaymentVoucher(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO, String ticketTransactionCode);

	public void SaveUserTransaction(Connection connection, AuthDTO authDTO, UserDTO userDTO, UserTransactionDTO userTransactionDTO);

	public void SaveUserTransaction(AuthDTO authDTO, UserDTO userDTO, UserTransactionDTO userTransactionDTO);

	public void updateUserBalance(AuthDTO authDTO, List<String> paymentCodes);

	public void creditUserBoardingCommission(AuthDTO authDTO, SearchDTO searchDTO);

	public PaymentTransactionDTO generatePaymentVoucherV2(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO);

	public List<UserTransactionDTO> validateUserBalanceMismatch(AuthDTO auth, DateTime fromDate, DateTime toDate);

	public void updateUserBalanceMismatch(AuthDTO authDTO, UserTransactionDTO userTransaction);
	
	public void getUserTransaction(AuthDTO authDTO, UserTransactionDTO userTransactionDTO);

}
