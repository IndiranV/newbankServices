package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TicketRefundIO {
	private String ticketCode;
	private String bookingCode;
	private String transactionCode;
	private StationIO fromStation;
	private StationIO toStation;
	private String travelDateTime;
	private String bookedAt;
	private String canncelledAt;
	private String passegerMobleNo;
	private String passegerEmailId;
	private BigDecimal totalRefundAmount;
	private int seatCount;
	private String remarks;

}
