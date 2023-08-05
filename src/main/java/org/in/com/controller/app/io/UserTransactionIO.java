package org.in.com.controller.app.io;

import java.math.BigDecimal;

import lombok.Data;

import org.in.com.controller.web.io.UserIO;

@Data
public class UserTransactionIO {
	private String refferenceCode;
	private String transactionDate;
	private TransactionTypeIO transactionType;
	private TransactionModeIO transactionMode;
	private BigDecimal transactionAmount;
	private BigDecimal tdsTax;
	private BigDecimal creditAmount;
	private BigDecimal debitAmount;
	private BigDecimal closingBalance;
	private UserIO user;
}
