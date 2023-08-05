package org.in.com.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CancellationTermDTO extends BaseDTO<CancellationTermDTO> {
	private String policyGroupKey;
	private int policyGroupId;
	private List<CancellationPolicyDTO> policyList;

}
