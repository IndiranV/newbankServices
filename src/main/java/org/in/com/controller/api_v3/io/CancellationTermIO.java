package org.in.com.controller.api_v3.io;

import java.util.List;

import lombok.Data;

@Data
public class CancellationTermIO {
	private String code;
	private String datetime;
	private String instantCancellationTill;
	private String instantCancellationMinutes;
	private List<CancellationPolicyIO> policyList;

}
