package org.in.com.controller.api_v3.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class TicketDetailsIO {
	private String seatName;
	private String seatCode;
	private String seatType;
	private String passengerName;
	private String passengerGendar;

	private int passengerAge;
	private BigDecimal seatFare;
	private BigDecimal serviceTax;
	private BigDecimal discountAmount = BigDecimal.ZERO;

	private BigDecimal refundAmount;
	private SeatStatusIO seatStatus;
	// private BigDecimal cancellationChargeTax;
	private BigDecimal cancellationCharges;

}
