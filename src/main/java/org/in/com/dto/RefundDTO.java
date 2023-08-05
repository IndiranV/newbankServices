package org.in.com.dto;

import java.math.BigDecimal;

import lombok.Data;

import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.exception.ErrorCode;

@Data
public class RefundDTO {

	private String transactionCode;
	private String orderTransactionCode;
	private String orderCode;
	private String gatewayTransactionCode;
	private BigDecimal amount;
	private PaymentGatewayCredentialsDTO gatewayCredentials;
	private PaymentGatewayStatusEM status;
	// identify the type of transaction(Payment/Refund)
	private String responseRecevied;
	private final PaymentGatewayTransactionTypeEM transactionType = PaymentGatewayTransactionTypeEM.REFUND;
	private ErrorCode errorCode;
	private OrderTypeEM orderType;

}
