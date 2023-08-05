package org.in.com.dto;

import org.in.com.dto.enumeration.TransactionModeEM;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CashbookTypeDTO extends BaseDTO<CashbookTypeDTO> {
	private TransactionModeEM transactionMode;
	private String transactionType;

}
