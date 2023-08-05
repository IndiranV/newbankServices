package org.in.com.controller.app.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.AcknowledgeStatusIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.UserIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentTransactionIO extends BaseIO {
	private String transactionDate;
	private TransactionTypeIO transactionType;
	private TransactionModeIO transactionMode;
	private AcknowledgeStatusIO acknowledgeStatus;
	private BigDecimal transactionAmount;
	private UserIO user;

	// recharge
	private String amountReceivedDate;
	private UserIO paymentHandledBy;
	private String remarks;
	private String gatewayTransactionId;
	// Payment Voucher
	private String transactionCodes;
}
