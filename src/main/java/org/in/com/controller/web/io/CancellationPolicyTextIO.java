package org.in.com.controller.web.io;

import lombok.Data;

@Data
public class CancellationPolicyTextIO {
	private String fromValue;
	private String toValue;
	private double deductionAmount;
	private double refundAmount;
	private String charges;
}
