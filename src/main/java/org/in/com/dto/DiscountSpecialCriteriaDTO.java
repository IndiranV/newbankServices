package org.in.com.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiscountSpecialCriteriaDTO extends BaseDTO<DiscountSpecialCriteriaDTO> {
	private List<GroupDTO> userGroups;
	private List<ScheduleDTO> schedules;
	private boolean percentageFlag;
	private BigDecimal maxAmount;
}
