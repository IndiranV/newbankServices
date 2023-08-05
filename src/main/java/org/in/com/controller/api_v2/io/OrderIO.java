package org.in.com.controller.api_v2.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderIO {
	private String code;
	private OrderDetailsIO orderDetails;
	private String emailId;
	private String mobileNumber;
	private TripStatusIO tripStatus;
	private String blockingLiveTime;
	private String referenceTicketNumber;
	private boolean phoneBookingFlag;
	private String transactionCode;
	private String gatewayCode;
	private BigDecimal transactionAmount;
	private String firstName;
	private String lastname;

	// Validation Status
	private BigDecimal currentBalance;
	private BigDecimal creditLimit;

	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append(mobileNumber).append(" - ").append(emailId).append(" - ");
		if (orderDetails != null) {
			string.append(orderDetails.toString());
		}
		return string.toString();
	}
}
