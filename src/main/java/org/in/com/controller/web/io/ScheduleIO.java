package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import org.in.com.controller.commerce.io.StageIO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleIO extends BaseIO {
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String serviceNumber;
	private String displayName;
	private String apiDisplayName;
	private String pnrStartCode;
	private BigDecimal serviceTax;
	private String lookupCode;
	private List<ScheduleIO> overrideList;

	private List<StageIO> stageList;
	private List<TripIO> tripList;
	private BusIO bus;
	private ScheduleCategoryIO category;
	private List<ScheduleTagIO> scheduleTagList;
	private float distance;
	private List<SectorIO> sectorList;

}