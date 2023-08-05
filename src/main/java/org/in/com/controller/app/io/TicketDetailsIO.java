package org.in.com.controller.app.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.UserIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketDetailsIO extends BaseIO {
	private String seatName;
	private String seatCode;
	private String seatType;
	private String passengerName;
	private String passengerGender;
	private String contactNumber;
	private int passengerAge;
	private BigDecimal seatFare;
	private BigDecimal serviceTax;
	private BigDecimal refundAmount;
	private UserIO bookedBy;
	private SeatStatusIO seatStatus;
	private BigDecimal cancellationCharges;
	//Cancellation charge GST
	private BigDecimal cancellationChargeTax;
	private BaseIO travelStatus;

}
