package org.in.com.controller.api_v2.io;

import java.math.BigDecimal;

import lombok.Data;

import org.in.com.controller.web.io.GroupIO;
import org.in.com.controller.web.io.UserIO;

@Data
public class BusSeatLayoutIO {
	private String code;
	private String name;
	private BusSeatTypeIO busSeatType;
	private SeatGendarStatusIO seatGendar;
	private SeatStatusIO seatStatus;
	private int rowPos;
	private int colPos;
	private int layer;
	private String seatName;
	private BigDecimal seatFare;
	private BigDecimal serviceTax;

	private String passengerName;
	private int passengerAge;
	private String contactNumber;
	private String ticketCode;
	private UserIO user;
	private GroupIO group;

}
