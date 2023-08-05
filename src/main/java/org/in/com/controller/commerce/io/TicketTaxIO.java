package org.in.com.controller.commerce.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.controller.web.io.BaseIO;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketTaxIO extends BaseIO {
	private String gstin;
	private String tradeName;
	private BigDecimal cgstAmount;
	private BigDecimal sgstAmount;
	private BigDecimal ugstAmount;
	private BigDecimal igstAmount;
}
