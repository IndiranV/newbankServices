package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleSeatFareIO extends BaseIO {
	private ScheduleIO schedule;
	private GroupIO group;
	private BusIO bus;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private StationIO fromStation;
	private StationIO toStation;
	private BigDecimal seatFare;
	private String fareType;
	private String fareOverrideType;
	private String lookupCode;
	private List<BusSeatLayoutIO> busSeatLayout;
	private List<ScheduleSeatFareIO> overrideList;
	private List<RouteIO> routeList;
	private List<GroupIO> groupList;
}