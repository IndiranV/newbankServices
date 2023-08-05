package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ExtraCommissionIO extends BaseIO {
	private BigDecimal commissionValue;
	private BaseIO commissionValueType;
	private String dateType;
	private List<BaseIO> group;
	private List<BaseIO> user;
	private String roleType;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleIO> schedule;
	private List<String> routeCode;
	private List<ExtraCommissionIO> overrideList = new ArrayList<ExtraCommissionIO>();
	private ExtraCommissionSlabIO commissionSlab;
	private BigDecimal maxCommissionLimit;
	private BigDecimal minTicketFare;
	private BigDecimal maxExtraCommissionAmount;
	private int minSeatCount;
	private int overrideCommissionFlag;

}
