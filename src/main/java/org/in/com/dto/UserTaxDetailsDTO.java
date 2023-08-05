package org.in.com.dto;

import java.math.BigDecimal;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserTaxDetailsDTO extends BaseDTO<UserTaxDetailsDTO> {
	private DateTime fromDate;
	private BigDecimal tdsTaxValue;
	private String panCardCode;
	private UserDTO user;
}
