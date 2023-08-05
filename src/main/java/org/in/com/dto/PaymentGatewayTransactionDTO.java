package org.in.com.dto;

import java.math.BigDecimal;

import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;

import lombok.Data;

@Data
public class PaymentGatewayTransactionDTO {

	private String code;
	private BigDecimal amount;
	private BigDecimal serviceCharge;
	private PaymentGatewayStatusEM status;
	private UserDTO user;
	private DeviceMediumEM deviceMedium;
	// identify the type of transaction(Payment/Refund)
	private PaymentGatewayTransactionTypeEM transactionType;
	private PaymentGatewayPartnerDTO gatewayPartner;
	private PaymentGatewayCredentialsDTO gatewayCredentials;
	private PaymentGatewayProviderDTO gatewayProvider;
	private String gatewayTransactionCode;
	private String orderCode;
}
