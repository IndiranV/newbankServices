package org.in.com.controller.app.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class StageFareIO {
	private BigDecimal fare;
	private BigDecimal discountFare;
	private String seatType;
	private String seatName;
	private String groupName;
	private int availableSeatCount;
}