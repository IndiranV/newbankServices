package org.in.com.controller.api_v2.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class StageFareIO {
	private BigDecimal fare;
	private String seatType;
	private String seatName;
	private String groupName;
	private int availableSeatCount;
}