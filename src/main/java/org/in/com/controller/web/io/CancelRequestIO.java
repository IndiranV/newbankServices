package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.commerce.io.TicketDetailsIO;
import org.in.com.controller.web.io.BaseIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class CancelRequestIO extends BaseIO {
	private String bookingCode;
	private BigDecimal cancellationCharge;
	private BigDecimal refund;
	private BigDecimal totalFare;
	private BigDecimal discountAmount;
	private List<TicketDetailsIO> ticketDetails;
}
