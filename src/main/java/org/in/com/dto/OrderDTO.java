package org.in.com.dto;

import java.math.BigDecimal;
import java.util.Map;

import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;

import lombok.Data;

@Data
public class OrderDTO {

	private String transactionCode;
	private String gatewayTransactionCode;
	private BigDecimal amount;
	private String gatewayId;
	private String gatewayPartnerCode;
	private String gatewayPaymentCode;
	private PaymentGatewayProviderDTO gatewayProvider;
	private PaymentGatewayCredentialsDTO gatewayCredentials;
	private PaymentGatewayTransactionTypeEM transactionTypeDTO;
	private String status;
	private String returnUrl;
	private String responseUrl;
	private String authToken;
	private String orderCode;
	private OrderTypeEM orderType;
	
	private String firstName;
	private String lastName;
	private String mobile;
	private String email;
	private String address1;
	private String address2;
	private String city = "Chennai";
	private String state = "Tamil Nadu";
	private String zipCode = "600107";
	private String udf1;
	private String udf2;
	private String udf3;
	private String udf4;
	private String udf5;

	// TODO : Separate this code into another DTO
	private String method = "post";
	private String gatewayUrl;
	private Map<String, String> gatewayFormParam;
	private String responseRecevied;
}
