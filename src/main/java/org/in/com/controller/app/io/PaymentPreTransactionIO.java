package org.in.com.controller.app.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentPreTransactionIO {
	private String orderCode;
	private BigDecimal amount;
	private BigDecimal serviceCharge;
	private String type;
	private String status;
	private String failureErrorCode;
	private int gatewayPartnerId;
	// identify the type of transaction(recharges/booking/or any)
	private TransactionTypeIO transactionTypeIO;
	private String receivedDate;

}
