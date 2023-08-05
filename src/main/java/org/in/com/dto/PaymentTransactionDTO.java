package org.in.com.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.PaymentAcknowledgeEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.utils.TokenGenerator;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentTransactionDTO extends BaseDTO<PaymentTransactionDTO> {
	private String transactionCode;
	private String transactionDate;
	private UserDTO user;
	private TransactionTypeEM transactionType;
	private TransactionModeEM transactionMode;
	private BigDecimal transactionAmount = BigDecimal.ZERO;
	private BigDecimal commissionAmount = BigDecimal.ZERO;
	private BigDecimal acBusTax = BigDecimal.ZERO;
	private BigDecimal tdsTax = BigDecimal.ZERO;
	private PaymentAcknowledgeEM paymentAcknowledge;
	// recharge
	private String amountReceivedDate;
	private UserDTO paymentHandledByUser;
	private String remarks;
	private UserTransactionDTO userTransaction;
	private String gatewayTransactionId;
	private int lookupId;
	private List<PaymentTransactionDTO> partialPaymentPaidList = new ArrayList<PaymentTransactionDTO>();

	public String getPaymentTransactionCode() {
		return TokenGenerator.generateCode("PX");
	}
}
