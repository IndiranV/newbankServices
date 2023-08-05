package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TicketDetailsIO {
	private String seatName;
	private String seatCode;
	private String seatType;
	private String passengerName;
	private String passengerGendar;
	private String contactNumber;
	private int passengerAge;
	private BigDecimal seatFare;
	private BigDecimal serviceTax;
	private BigDecimal refundAmount;
	private SeatStatusIO seatStatus;
	private BigDecimal cancellationCharges;
	private BigDecimal cancellationChargeGSTAmount;
	private BaseIO travelStatus;
}
