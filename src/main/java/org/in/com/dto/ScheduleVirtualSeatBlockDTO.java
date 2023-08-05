package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleVirtualSeatBlockDTO extends BaseDTO<ScheduleVirtualSeatBlockDTO> {
	private ScheduleDTO schedule;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleVirtualSeatBlockDTO> exceptionList = new ArrayList<ScheduleVirtualSeatBlockDTO>();
	private List<ScheduleDTO> scheduleList;
	private int refreshMinutes;
	private List<GroupDTO> userGroupList;
	private GroupDTO group;
	private List<String> occuapancyblockPercentage;
}