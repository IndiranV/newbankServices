package org.in.com.controller.api_v3.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CommissionIO {
	private BigDecimal value;
	private String commissionType;
}
