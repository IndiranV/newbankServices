package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CancellationTermIO extends BaseIO {
	private List<CancellationPolicyIO> policyList;
}
