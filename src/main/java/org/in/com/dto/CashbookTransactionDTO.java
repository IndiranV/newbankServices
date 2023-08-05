package org.in.com.dto;

import java.math.BigDecimal;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.CashbookAckStatusEM;
import org.in.com.dto.enumeration.CashbookCategoryEM;
import org.in.com.dto.enumeration.TransactionModeEM;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CashbookTransactionDTO extends BaseDTO<CashbookTransactionDTO> {
	private DateTime transactionDate;
	private CashbookCategoryEM cashbookCategory;
	private String referenceCode;
	private CashbookTypeDTO cashbookType;
	private String transactionType;
	private BigDecimal amount;
	private UserDTO user;
	private CashbookVendorDTO cashbookVendor;
	private TransactionModeEM transactionMode;
	private CashbookAckStatusEM acknowledgeStatus;
	private List<ImageDetailsDTO> images;
	private String remarks;
	private int paymentStatusFlag;

	public String getImageIds() {
		StringBuilder imageIds = new StringBuilder();
		if (images != null && !images.isEmpty()) {
			for (ImageDetailsDTO imageDetailsDTO : images) {
				if (imageDetailsDTO.getId() != 0) {
					imageIds.append(imageDetailsDTO.getId()).append(Text.COMMA);
				}
			}
		}
		else {
			imageIds.append(Text.NA);
		}
		return imageIds.toString();
	}
}
