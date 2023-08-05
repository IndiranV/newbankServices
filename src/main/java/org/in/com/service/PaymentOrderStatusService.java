package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.PaymentPreTransactionDTO;
import org.in.com.dto.TicketDTO;

public interface PaymentOrderStatusService {
	public Map<String, String> getOrderStatus(AuthDTO authDTO, String orderCode, String namespaceCode);
	
	public List<PaymentGatewayTransactionDTO> getPaymentGatewayTransactions(AuthDTO authDTO, TicketDTO ticketDTO);

	public Map<String, String> verifyOrderStatus(AuthDTO authDTO, TicketDTO ticketDTO);
	
	public Map<String, String> updateTransactionStatus(AuthDTO authDTO, TicketDTO ticketDTO);

	public void paymentRefund(AuthDTO authDTO, PaymentPreTransactionDTO preTransaction);
}
