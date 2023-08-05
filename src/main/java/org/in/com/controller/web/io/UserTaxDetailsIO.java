package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class UserTaxDetailsIO extends BaseIO {
	private String fromDate;
	private BigDecimal tdsTaxValue;
	private String panCardCode;
	private UserIO user;
}
