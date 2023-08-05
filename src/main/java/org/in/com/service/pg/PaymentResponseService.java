package org.in.com.service.pg;

import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TransactionEnquiryDTO;

public interface PaymentResponseService {

	TransactionEnquiryDTO handlePayment(String transactionCode, Map<String, String> fields);

	public AuthDTO validateInternalOrderStatus(String transactionCode);

	public TransactionEnquiryDTO internalTransactionEnquiry(String transactionCode, Map<String, String> fields);

}
