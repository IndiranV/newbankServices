package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

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
	private List<PaymentTransactionIO> partialPaymentList;
}
