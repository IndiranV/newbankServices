package org.in.com.controller.api_v2.io;

import lombok.Data;

@Data
public class ScheduleIO {
	private String code;
	private String name;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int mobileTicketFlag;
	private String serviceNumber;
	private String displayName;
	private String pnrStartCode;
	private int boardingReportingMinitues;
//	private BigDecimal serviceTax;
}