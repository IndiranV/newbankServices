package org.in.com.aggregator.payment;

import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.TransactionEnquiryDTO;
import org.in.com.exception.PaymentResponseException;

public interface PGInterface {
	// Init Payment Request
	public void packPaymentRequest(AuthDTO authDTO, OrderDTO order) throws Exception;

	public void internalVerfication(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception;

	// Check Order Status using URL response
	public void transactionVerify(AuthDTO authDTO, TransactionEnquiryDTO enquiryStatus) throws PaymentResponseException, Exception;

	// Refund Order
	public void refund(AuthDTO authDTO, RefundDTO refund) throws Exception;

	// Check Order Status using M2M
	public Map<String, String> verifyPaymentOrder(AuthDTO authDTO, OrderDTO orderDTO);

}
