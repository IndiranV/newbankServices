package org.in.com.controller.busbuddy.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentTransactionIO extends BaseIO {
	private String transactionDate;
	private BaseIO transactionType;
	private BaseIO transactionMode;
	private BaseIO acknowledgeStatus;
	private BigDecimal amount;
	private BaseIO user;

	// recharge
	private String amountReceivedDate;
	private BaseIO paymentHandledBy;
	private String remarks;
	private String gatewayTransactionId;
	// Payment Voucher
	private String transactionCodes;
	private List<PaymentTransactionIO> partialPaymentList;
}
