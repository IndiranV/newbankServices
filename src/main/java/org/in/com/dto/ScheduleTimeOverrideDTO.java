package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import org.in.com.dto.enumeration.OverrideTypeEM;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleTimeOverrideDTO extends BaseDTO<ScheduleTimeOverrideDTO> {
	private ScheduleDTO schedule;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int overrideMinutes;
	private StationDTO station;
	private OverrideTypeEM overrideType;
	private boolean reactionFlag;
	private String lookupCode;
	private List<ScheduleTimeOverrideDTO> overrideList = new ArrayList<ScheduleTimeOverrideDTO>();
	private UserDTO updatedUser;
	private DateTime updatedAt;
}