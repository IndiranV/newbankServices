package org.in.com.controller.api_v2.io;


import java.math.BigDecimal;

import lombok.Data;

@Data
 public class TicketTransactionIO   {
	private TransactionTypeIO transactionType;
	private int transSeatCount;
	private TransactionModeIO transactionMode;
	private BigDecimal transactionAmount;
	private BigDecimal serviceTax;
	private BigDecimal userCommissionAmount;

}
