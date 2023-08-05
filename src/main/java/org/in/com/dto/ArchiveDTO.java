package org.in.com.dto;

import java.math.BigDecimal;

import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TripStatusEM;

import lombok.Data;

@Data
public class ArchiveDTO {
	// Booking and Cancellation
	private UserDTO user;
	private BigDecimal revokeCommissionAmount;
	private BigDecimal cancellationChargeAmount;
	private BigDecimal refundAmount;
	private BigDecimal transactionAmount;
	private int bookedSeatCount;
	private int cancelledSeatCount;
	private BigDecimal commissionAmount;
	private BigDecimal acBusTax;
	private int phoneSeatCancelledCount;
	private int phoneBlockedSeatCount;

	// Trip
	private int tripCount;
	private int seatCount;
	private TripStatusEM tripStatus;

	private DeviceMediumEM deviceMedium;
	private TicketStatusEM ticketStatus;
	private GroupDTO group;
}
