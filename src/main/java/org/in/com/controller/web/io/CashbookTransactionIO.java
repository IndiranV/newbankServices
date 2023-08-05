package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CashbookTransactionIO extends BaseIO {
	private String transactionDate;
	private BaseIO cashbookCategory;
	private String referenceCode;
	private CashbookTypeIO cashbookType;
	private String transactionType;
	private BigDecimal amount;
	private UserIO user;
	private CashbookVendorIO cashbookVendor;
	private TransactionModeIO transactionMode;
	private BaseIO acknowledgeStatus;
	private List<ImageDetailsIO> images;
	private String remarks;
	private int paymentStatusFlag;

}
