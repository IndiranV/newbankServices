package org.in.com.controller.web.io;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class CancellationPolicyIO {
	private int fromValue;
	private int toValue;
	private BigDecimal deductionAmount;
	private String policyPattern;
	private int percentageFlag;
	private double refundAmount;

	// Display Version
	private String term;
	private String deductionAmountTxt;
	private String refundAmountTxt;
	private String chargesTxt;
}
