package org.in.com.controller.api_v3.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ScheduleTicketTransferTermsIO {
	private BigDecimal chargeAmount;
	private String allowedTill;
	private int transferable;
	private String chargeType; 
}