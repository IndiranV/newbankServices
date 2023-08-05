package org.in.com.controller.commerce.io;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import lombok.Data;

import org.in.com.controller.web.io.PaymentGatewayPartnerIO;

import com.google.gson.Gson;

@Data
public class OrderIO {
	private String code;
	private List<OrderDetailsIO> orderDetails;
	private String emailId;
	private String mobileNumber;
	private TripStatusIO tripStatus;
	private String blockingLiveTime;
	private String remarks;
	private String offerDiscountCode;
	private String offlineUserCode;
	private BigDecimal discountAmount;
	private BigDecimal agentServiceCharge;
	private boolean phoneBookingFlag;
	private List<PaymentModeIO> paymentMode;
	private Map<String, String> additionalAttributes;

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

	// Validation Status
	private BigDecimal currentBalance;
	private BigDecimal creditLimit;
	private boolean paymentGatewayProcessFlag;

	public String toJSON() {
		Gson gson = new Gson();
		if (this != null) {
			gson.toJson(this);
		}
		return gson.toJson(this);
	}
}
