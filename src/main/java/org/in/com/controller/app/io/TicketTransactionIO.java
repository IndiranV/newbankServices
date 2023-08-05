package org.in.com.controller.app.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketTransactionIO extends BaseIO {
	private TransactionTypeIO transactionType;
	private int transSeatCount;
	private TransactionModeIO transactionMode;
	private BigDecimal transactionAmount;
	private BigDecimal serviceTax;
	private BigDecimal acBusTax;
	private BigDecimal tdsTax;
	private BigDecimal commissionAmount;
	private BigDecimal extraCommissionAmount;
	private BigDecimal cancellationChargeAmount;
	private BigDecimal cancelTdsTax;
	private BigDecimal cancellationChargeTax;
	private BigDecimal refundAmount;
	private BigDecimal cancelCommissionAmount;
	private String remarks;
	private PaymentTransactionIO paymentTransaction;

}
