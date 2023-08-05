package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.AcknowledgeStatusIO;
import org.in.com.controller.web.io.UserIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentReceiptIO extends BaseIO {
	private String remarks;
	private String transactionDate;
	private String amountReceivedDate;
	private String updatedAt;
	private BigDecimal transactionAmount = BigDecimal.ZERO;
	private BigDecimal balanceAmount = BigDecimal.ZERO;
	private BigDecimal openingBalance = BigDecimal.ZERO;
	private BigDecimal closingBalance = BigDecimal.ZERO;
	private BaseIO transactionMode;
	private AcknowledgeStatusIO paymentAcknowledgeStatus;
	private BaseIO paymentReceiptType;
	// private TransactionTypeEM transactionType;
	private int image;
	private UserIO user;
	private UserIO updatedBy;
	private List<PaymentTransactionIO> paymentTransactions;
	private List<AuditIO> auditLog;
}
