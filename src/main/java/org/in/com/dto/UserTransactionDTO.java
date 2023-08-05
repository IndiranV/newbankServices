package org.in.com.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserTransactionDTO extends BaseDTO<UserTransactionDTO> {
	private int refferenceId;
	private String refferenceCode;
	private String transactionDate;
	private UserDTO user;
	private TransactionTypeEM transactionType;
	private TransactionModeEM transactionMode;
	private BigDecimal transactionAmount;
	private BigDecimal commissionAmount;
	private BigDecimal tdsTax = BigDecimal.ZERO;
	private BigDecimal creditAmount;
	private BigDecimal debitAmount;
	private BigDecimal closingBalanceAmount;

}
