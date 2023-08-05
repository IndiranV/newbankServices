package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.commerce.io.SeatStatusIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusSeatLayoutIO extends BaseIO {

	private BusSeatTypeIO busSeatType;
	private SeatStatusIO seatStatus;
	private SeatGendarStatusIO seatGendarStatus;
	private int rowPos;
	private int colPos;
	private int layer;
	private int sequence;
	private String seatName;
	private double seatFare;
	private double serviceTax;

	private String passengerName;
	private int passengerAge;
	private String contactNumber;
	private String ticketCode;
	private UserIO user;
	private GroupIO group;
	private int orientation;
}
