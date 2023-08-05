package org.in.com.controller.app.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

import org.in.com.controller.web.io.NamespaceTaxIO;
import org.in.com.controller.web.io.ScheduleCategoryIO;

@Data
public class ScheduleIO {
	private String code;
	private String name;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
 	private String serviceNumber;
	private String displayName;
	private String pnrStartCode;
	private BigDecimal serviceTax;
	private NamespaceTaxIO tax;

	private BusIO bus;
	private List<StageIO> stageList;
	
	private ScheduleCategoryIO categoryIO;
	private List<TravelStopsIO> travelStops;
}