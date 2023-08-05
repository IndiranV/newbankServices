package org.in.com.controller.web.io;

import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ScheduleBookGenderRestrictionIO extends BaseIO {
	private String dayOfWeek;
	private int releaseMinutes;
	private int femaleSeatCount;
	/** 1.SEAT_TYPE-Individual 2.ALL-Together  */
	private String seatTypeGroupModel;
	private List<ScheduleIO> scheduleList;
	private List<GroupIO> groupList;
}
