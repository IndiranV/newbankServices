package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleDynamicStageFareIO extends BaseIO {
	private ScheduleIO schedule;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleDynamicStageFareIO> overrideList;
	private List<ScheduleDynamicStageFareDetailsIO> stageFare;
	private int status;
	private BaseIO dynamicPriceProvider;
}