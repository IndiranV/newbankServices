package org.in.com.cache.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ScheduleDiscountCacheDTO implements Serializable {
	private static final long serialVersionUID = 7022527162257878567L;
	private int id;
	private int activeFlag;
	private String code;
	private BigDecimal discountValue;
	private int percentageFlag;
	private int authenticationTypeId;
	private int deviceMediumId;
	private int femaleDiscountFlag;
	private String dateTypeCode;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleDiscountCacheDTO> overrideList = new ArrayList<ScheduleDiscountCacheDTO>();
	private int afterBookingMinutes;
	private int advanceBookingDays;
	private List<String> scheduleList;
	private List<String> groupList;
}