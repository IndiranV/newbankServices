package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.PaymentReceiptDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.PaymentAcknowledgeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.joda.time.DateTime;

public interface PaymentTransactionService {

	public void rechargeTransaction(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO);

	public PaymentTransactionDTO rechargeGatewayTransaction(AuthDTO authDTO, String orderCode);

	public PaymentTransactionDTO getPaymentTransaction(AuthDTO authDTO, PaymentTransactionDTO transactionDTO);

	public PaymentTransactionDTO getPaymentTransactionByTicket(AuthDTO authDTO, TicketDTO ticketDTO);

	public PaymentTransactionDTO getPaymentTransactionHistory(AuthDTO authDTO, UserDTO userDTO, PaymentTransactionDTO transactionDTO, DateTime fromDate, DateTime toDate);

	public void acknowledgeTransaction(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO);

	public void getUnAcknowledgeTransaction(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO);
	
	public List<PaymentTransactionDTO> verifyTicketTransaction(AuthDTO authDTO, List<PaymentTransactionDTO> transactionList);
	
	public List<PaymentTransactionDTO> updateTicketTransaction(AuthDTO authDTO, List<PaymentTransactionDTO> transactionList);
	
	public void sendPaymentNotification(AuthDTO authDTO, PaymentTransactionDTO paymentTransactionDTO, PaymentTransactionDTO repoPaymentTransactionDTO);

	public List<PaymentReceiptDTO> getPaymentReceipts(AuthDTO authDTO, UserDTO user, String fromDate, String toDate, PaymentAcknowledgeEM paymentAcknowledge, UserRoleEM userRole);

	public void savePaymentReceipt(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO);

	public void updatePaymentReceipt(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO);

	public void getPaymentReceipt(AuthDTO authDTO, PaymentReceiptDTO paymentReceiptDTO);
	
	public void acknowledgePaymentVoucherTransaction(AuthDTO authDTO, PaymentTransactionDTO userPayment);
	
	public void updatePaymentReceiptImageDetails(AuthDTO authDTO, String referenceCode, String imageDetailsIds);

}
