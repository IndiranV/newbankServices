package org.in.com.dto;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.ProductTypeEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class NamespaceTaxDTO extends BaseDTO<NamespaceTaxDTO> {
	private StateDTO state;
	private BigDecimal cgstValue = BigDecimal.ZERO;
	private BigDecimal sgstValue = BigDecimal.ZERO;
	private BigDecimal ugstValue = BigDecimal.ZERO;
	private BigDecimal igstValue = BigDecimal.ZERO;
	private String sacNumber;
	private String gstin;
	private String tradeName;
	private ProductTypeEM productType;

	public BigDecimal getServiceTax() {
		return cgstValue.add(sgstValue).add(ugstValue).add(igstValue);
	}
}
