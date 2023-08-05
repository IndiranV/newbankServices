package org.in.com.controller.api_v3.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BusSeatLayoutIO {
	private String code;
	private String name;
	private BusSeatTypeIO busSeatType;
	private SeatGendarStatusIO seatGendar;
	private SeatStatusIO seatStatus;
	private int rowPos;
	private int colPos;
	private int seatPos;
	private int layer;
	private String seatName;
	private BigDecimal seatFare;
	private BigDecimal serviceTax;
	private BigDecimal discountFare;
	private int orientation;

}
