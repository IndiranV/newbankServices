package org.in.com.controller.web.io;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CashbookTypeIO extends BaseIO {
	private BaseIO transactionMode;
	private String transactionType;
}
