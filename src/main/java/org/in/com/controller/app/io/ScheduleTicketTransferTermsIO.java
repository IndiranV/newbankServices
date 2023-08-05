package org.in.com.controller.app.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ScheduleTicketTransferTermsIO {
	private BigDecimal chargeAmount;
	private String chargeType;
	private String allowedTill;
	private int transferable;
}