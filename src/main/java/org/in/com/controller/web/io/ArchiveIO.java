package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;

import org.in.com.dto.enumeration.DeviceMediumEM;

@Data
public class ArchiveIO {
	// Booking and Cancellation
	private UserIO user;
	private BigDecimal revokeCommissionAmount;
	private BigDecimal cancellationChargeAmount;
	private BigDecimal refundAmount;
	private BigDecimal transactionAmount;
	private int bookedSeatCount;
	private int cancelledSeatCount;
	private int seatCount;
	private BigDecimal commissionAmount;
	private BigDecimal acBusTax;
	private int phoneSeatCancelledCount;
	private int phoneBlockedSeatCount;

	// Trip
	private int tripCount;
	private TripStatusIO tripStatus;

	private DeviceMediumEM deviceMedium;
	private TicketStatusIO ticketStatus;
	private GroupIO group;
}
