package org.in.com.controller.web.io;

import java.math.BigDecimal;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Text;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleFareAutoOverrideIO extends BaseIO {
	private ScheduleIO schedule;
	private GroupIO group;
	private List<GroupIO> groupList;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private StationIO fromStation;
	private StationIO toStation;
	private int overrideMinutes;
	private BaseIO fareOverrideMode;
	private BigDecimal fare;
	private String busSeatTypeCode;
	private String lookupCode;
	private String tag = Text.NA;
	private List<String> busSeatTypeCodes;
	private List<BusSeatTypeFareIO> busSeatTypeFare;
	private List<ScheduleFareAutoOverrideIO> overrideList;
	private UserIO user;
	private String updateAt;
	private List<RouteIO> routeList;
}