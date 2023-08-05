package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DiscountSpecialCriteriaIO extends BaseIO {
	private List<GroupIO> userGroups;
	private List<ScheduleIO> schedules;
	private boolean percentageFlag;
	private BigDecimal maxAmount;
}
