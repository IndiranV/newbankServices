package org.in.com.controller.api.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BusSeatLayoutIO {
	private String code;
	private String name;
	private int activeFlag;
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
	private int sequence;

}
