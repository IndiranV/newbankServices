package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class BusSeatTypeFareIO {
	private BigDecimal fare;
	private String seatType;
}