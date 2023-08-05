package org.in.com.service.pg;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderInitRequestDTO;
import org.in.com.dto.OrderInitStatusDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;

public interface PaymentRequestService {

	public OrderInitStatusDTO handlePgService(AuthDTO authDTO, OrderInitRequestDTO paymentRequest);

	public boolean CheckPaymentStatus(AuthDTO authDTO, String orderCode);

	public PaymentGatewayTransactionDTO getPaymentGatewayTransactionAmount(AuthDTO authDTO, String orderCode, PaymentGatewayTransactionTypeEM transactionTypeEM);
	
	public List<PaymentGatewayTransactionDTO> getPaymentGatewayTransaction(AuthDTO authDTO, String orderCode);
}
