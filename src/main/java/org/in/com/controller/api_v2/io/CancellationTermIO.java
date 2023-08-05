package org.in.com.controller.api_v2.io;

import java.util.List;

import lombok.Data;

@Data
public class CancellationTermIO {
	private List<CancellationPolicyIO> policyList;

}
