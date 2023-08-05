package org.in.com.dto;

import java.math.BigDecimal;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.enumeration.RefundStatusEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketTransactionDTO extends BaseDTO<TicketTransactionDTO> {

	/*
	 * private String transactionDate;
	 * private TransactionTypeDTO transactionTypeDTO;
	 * private TransactionModeDTO transactionModeDTO;
	 * private BigDecimal transactionAmount;
	 * private TicketDTO ticketDTO;
	 * private UserTransactionDTO userTransactionDTO;
	 */

	private UserDTO userDTO;
	private TransactionTypeEM transactionType;
	private int transSeatCount;
	private TransactionModeEM transactionMode;
	private BigDecimal transactionAmount;
	private BigDecimal commissionAmount;
	private BigDecimal extraCommissionAmount;
	private BigDecimal addonsAmount;
	private BigDecimal acBusTax;
	private BigDecimal tdsTax = BigDecimal.ZERO;
	private BigDecimal cancelTdsTax = BigDecimal.ZERO;
	private BigDecimal cancellationChargeTax = BigDecimal.ZERO;
	private PaymentTransactionDTO paymentTrans;

	private BigDecimal cancellationCommissionAmount;
	private BigDecimal cancellationChargeAmount;
	private BigDecimal cancellationChargeCommissionAmount;
	private BigDecimal refundAmount;
	private RefundStatusEM refundStatus;
	private DateTime updatedAt;

	// Archive
	private BigDecimal revokeCommissionAmount;
	private int phoneSeatCancelledCount;
	private int phoneBlockedSeatCount;
	private String remarks;

	public String getTransactionCode() {
		return TokenGenerator.generateCode("TX");
	}

	public String getVoucherGenerateStatus() {
		return paymentTrans != null && (StringUtil.isNotNull(paymentTrans.getCode()) || paymentTrans.getId() != Numeric.ZERO_INT) ? Text.SUCCESS : Text.NA;
	}
}
