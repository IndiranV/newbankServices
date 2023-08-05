package org.in.com.controller.app.io;

import java.math.BigDecimal;

import org.in.com.controller.web.io.BaseIO;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
	private int orientation;
	private String seatName;
	private BigDecimal seatFare;
	private BigDecimal discountFare;
	private BigDecimal serviceTax;

}
