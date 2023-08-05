package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class NamespaceTaxIO extends BaseIO {
	private StateIO state;
	private BigDecimal cgstValue;
	private BigDecimal sgstValue;
	private BigDecimal ugstValue;
	private BigDecimal igstValue;
	private String tradeName;
	private String gstin;
	private String sacNumber;
	private BaseIO productType;

}
