package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Data;

@Data
public class RechargeOrderIO {
	private String code;
	private String emailId;
	private String mobileNumber;

	// Payment Process POJO
	private PaymentGatewayPartnerIO gatewayPartner;
	private String paymentRequestUrl;
	private String transactionCode;
	private String gatewayCode;
	private BigDecimal transactionAmount;
	private Map<String, String> gatewayInputDetails;
	private String firstName;
	private String lastname;
	private String responseUrl;



}
