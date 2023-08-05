package org.in.com.dto;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class FareRuleDTO extends BaseDTO<FareRuleDTO> {
	private StateDTO state;
	private List<FareRuleDetailsDTO> fareRuleDetails;

}
