package org.in.com.controller.api_v3.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class NamespaceTaxIO {
	private BigDecimal cgstValue;
	private BigDecimal sgstValue;
	private BigDecimal ugstValue;
	private BigDecimal igstValue;
	private String tradeName;
	private String gstin;
}
