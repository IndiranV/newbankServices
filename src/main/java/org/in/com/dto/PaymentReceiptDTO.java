package org.in.com.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.PaymentAcknowledgeEM;
import org.in.com.dto.enumeration.PaymentReceiptTypeEM;
import org.in.com.dto.enumeration.TransactionModeEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class PaymentReceiptDTO extends BaseDTO<PaymentReceiptDTO> {
	private String remarks;
	private String transactionDate;
	private String amountReceivedDate;
	private String updatedAt;
	private BigDecimal transactionAmount = BigDecimal.ZERO;
	private BigDecimal balanceAmount = BigDecimal.ZERO;
	private BigDecimal openingBalance = BigDecimal.ZERO;
	private BigDecimal closingBalance = BigDecimal.ZERO;
	private TransactionModeEM transactionMode;
	private PaymentAcknowledgeEM paymentAcknowledge;
	private PaymentReceiptTypeEM paymentReceiptType;
	// private TransactionTypeEM transactionType;
	private UserDTO user;
	private UserDTO updatedBy;
	private List<PaymentTransactionDTO> paymentTransactions = new ArrayList<>();
	private List<AuditDTO> auditLog;
	private List<ImageDetailsDTO> imageDetails;

	public String getPaymentTransactionIds() {
		Map<Integer, PaymentTransactionDTO> paymentMap = new HashMap<>();
		StringBuilder paymentTransactionIds = new StringBuilder();
		for (PaymentTransactionDTO paymentTransactionDTO : paymentTransactions) {
			if (paymentMap.get(paymentTransactionDTO.getId()) != null) {
				continue;
			}
			if (paymentTransactionDTO.getId() != 0) {
				paymentTransactionIds.append(paymentTransactionDTO.getId());
				paymentTransactionIds.append(Text.COMMA);
				paymentMap.put(paymentTransactionDTO.getId(), paymentTransactionDTO);
			}
		}
		return paymentTransactionIds.toString();
	}
}
