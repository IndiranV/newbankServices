package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class StageFareIO {
	private BigDecimal fare;
	private BigDecimal minFare;
	private BigDecimal maxFare;
	private String seatType;
	private String seatName;
	private String groupName;
	private int availableSeatCount;
}