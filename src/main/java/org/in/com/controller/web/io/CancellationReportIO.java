package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CancellationReportIO {
	private org.in.com.controller.commerce.io.ScheduleIO schedule;
	private String ticketCode;
	private String bookedAt;
	private UserIO bookedBy;
	private String cancelledAt;
	private String seatNbrs;
	private Integer noOfSeats;
	private UserIO cancelledBy;
	private BigDecimal ticketAmnt;
	private BigDecimal cancellationCharge;
	private BigDecimal refundAmnt;
	private BigDecimal agentCommission;
	private BigDecimal balanceAmnt;
	
}
