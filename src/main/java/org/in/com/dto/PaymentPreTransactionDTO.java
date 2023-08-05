package org.in.com.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentPreTransactionDTO extends BaseDTO<PaymentPreTransactionDTO> {
	private String orderCode;
	private BigDecimal amount;
	private BigDecimal serviceCharge;
	private String failureErrorCode;
	private UserDTO user;
	private DeviceMediumEM deviceMedium;
	private PaymentGatewayPartnerDTO gatewayPartner;
	private PaymentGatewayCredentialsDTO gatewayCredentials;
	private PaymentGatewayProviderDTO gatewayProvider;
	private PaymentGatewayStatusEM status;
	// identify the type of transaction(Payment/Refund)
	private PaymentGatewayTransactionTypeEM transactionType;
	private OrderTypeEM orderType;
	private String receivedDate;
	private String gatewayTransactionCode;
	private NamespaceDTO namespace;

}
