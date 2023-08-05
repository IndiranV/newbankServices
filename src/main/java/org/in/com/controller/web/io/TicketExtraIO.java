package org.in.com.controller.web.io;

import lombok.Data;

@Data
public class TicketExtraIO {
	private int sequenceNumber;
	private int blockReleaseMinutes;
	private int phoneBookPaymentStatus;
	private String linkPay;
	private String releaseAt;
	private String offlineTicketCode;
}
