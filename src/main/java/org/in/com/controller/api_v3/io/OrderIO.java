package org.in.com.controller.api_v3.io;

import java.math.BigDecimal;
import java.util.Map;

import com.google.gson.Gson;

import lombok.Data;

@Data
public class OrderIO {
	private String code;
	private OrderDetailsIO orderDetails;
	private String emailId;
	private String mobileNumber;
	private TripStatusIO tripStatus;
	private String blockingLiveTime;
	private String agentTicketNumber;
	private boolean phoneBookingFlag;
	private String transactionCode;
	private String gatewayCode;
	private BigDecimal transactionAmount;
	private String firstName;
	private String lastname;

	// Validation Status
	private BigDecimal currentBalance;
	private BigDecimal creditLimit;

	// GSTIN, Trade Name
	private Map<String, String> additionalAttributes;

	public String toJSON() {
		Gson gson = new Gson();
		if (this != null) {
			gson.toJson(this);
		}
		return gson.toJson(this);
	}
}
