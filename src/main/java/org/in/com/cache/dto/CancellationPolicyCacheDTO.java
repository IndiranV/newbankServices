package org.in.com.cache.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class CancellationPolicyCacheDTO implements Serializable {
	private static final long serialVersionUID = -4167719642672853750L;
	private int fromValue;
	private int toValue;
	private BigDecimal deductionValue;
	private String policyPattern;
	private int percentageFlag;
	private int policyId;

	public int getFromValue() {
		return fromValue;
	}

	public void setFromValue(int fromValue) {
		this.fromValue = fromValue;
	}

	public int getToValue() {
		return toValue;
	}

	public void setToValue(int toValue) {
		this.toValue = toValue;
	}

	public BigDecimal getDeductionValue() {
		return deductionValue;
	}

	public void setDeductionValue(BigDecimal deductionValue) {
		this.deductionValue = deductionValue;
	}

	public String getPolicyPattern() {
		return policyPattern;
	}

	public void setPolicyPattern(String policyPattern) {
		this.policyPattern = policyPattern;
	}

	public int getPercentageFlag() {
		return percentageFlag;
	}

	public void setPercentageFlag(int percentageFlag) {
		this.percentageFlag = percentageFlag;
	}

	public int getPolicyId() {
		return policyId;
	}

	public void setPolicyId(int policyId) {
		this.policyId = policyId;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
