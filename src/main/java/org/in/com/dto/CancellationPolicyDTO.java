package org.in.com.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CancellationPolicyDTO {
	private int fromValue;
	private int toValue;
	private BigDecimal deductionValue;
	private String policyPattern;
	private int percentageFlag;
	private int policyId;

	// Display Version
	private String term;
	private String deductionAmountTxt;
	private String refundAmountTxt;
	private String chargesTxt;
}
