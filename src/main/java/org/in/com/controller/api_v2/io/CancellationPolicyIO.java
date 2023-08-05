package org.in.com.controller.api_v2.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CancellationPolicyIO {
	private int fromValue;
	private int toValue;
	private BigDecimal deductionAmount;
	private String policyPattern;
	private int percentageFlag;
}
