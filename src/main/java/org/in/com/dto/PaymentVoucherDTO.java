package org.in.com.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentVoucherDTO extends BaseDTO<PaymentVoucherDTO> {
	private String ticketCode;
	private StationDTO fromStation;
	private StationDTO toStation;
	private String transactionCode;
	private String transactionDate;
	private String travelDate;
	private String seatNames;
	private String scheduleNames;
	private String tripCode;
	private int seatCount;
	private UserDTO user;
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

	private TransactionTypeEM transactionType;
	private TransactionModeEM transactionMode;

}
