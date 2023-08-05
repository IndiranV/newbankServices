package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class AddonsDiscountOfflineIO extends BaseIO {
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private BigDecimal value;
	private boolean percentageFlag;
	private boolean travelDateFlag;
	private BigDecimal maxDiscountAmount;
	private int minSeatCount;
	private int minTicketFare;
	private List<String> groupCode;
	private List<String> scheduleCode;
	private List<String> routeCode;

}
