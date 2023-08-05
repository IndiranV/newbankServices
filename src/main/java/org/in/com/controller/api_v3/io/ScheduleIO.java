package org.in.com.controller.api_v3.io;

import lombok.Data;

@Data
public class ScheduleIO {
	private String name;
	private String code;
	private String serviceNumber;
//	private BigDecimal serviceTax;
	private NamespaceTaxIO tax;
}