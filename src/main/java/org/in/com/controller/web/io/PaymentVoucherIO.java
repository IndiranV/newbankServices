package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class PaymentVoucherIO {
	private String ticketCode;
	private UserIO user;
	private StationIO fromStation;
	private StationIO toStation;
	private String transactionCode;
	private String transactionDate;
	private String travelDate;
	private String seatNames;
	private String scheduleNames;
	private String tripCode;
	private int seatCount;
	private BigDecimal transactionAmount;
	private BigDecimal ticketAmount;
	private BigDecimal serviceTax;
	private BigDecimal commissionAmount;
	private BigDecimal addonsAmount;
	private BigDecimal extraCommissionAmount;

	private BigDecimal refundAmount;
	private BigDecimal cancellationChargeAmount;
	private BigDecimal cancellationChargeCommissionAmount;
	private BigDecimal revokeCancelCommissionAmount;
	private BigDecimal netAmount;

	private TransactionModeIO transactionMode;
	private TransactionTypeIO transactionType;
}
