package org.in.com.controller.api_v3.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CancellationPolicyIO {
	private String code;
	private int fromValue;
	private int toValue;
	private BigDecimal deductionAmount;
	private String policyPattern;
	private int percentageFlag;
	
	// Display Version
	private String term;
	private String deductionAmountTxt;
	private String refundAmountTxt;
	private String chargesTxt;
}
