package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleDiscountIO extends BaseIO {
	private BigDecimal discountValue;
	private int percentageFlag;
	private BaseIO authenticationType;
	private BaseIO deviceMedium;
	private ScheduleIO schedule;
	private List<ScheduleIO> scheduleList;
	private List<GroupIO> groupList;
	private String dateType;
	private String activeFrom;
	private String activeTo;
	private int activeFromMinutes;
	private int activeToMinutes;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleDiscountIO> overrideList = new ArrayList<ScheduleDiscountIO>();
	private int afterBookingMinutes;
	private int advanceBookingDays;
	private int femaleDiscountFlag;

}