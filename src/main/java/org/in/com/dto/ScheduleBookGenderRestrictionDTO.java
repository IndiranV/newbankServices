package org.in.com.dto;

import java.util.List;

import org.in.com.constants.Text;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class ScheduleBookGenderRestrictionDTO extends BaseDTO<ScheduleBookGenderRestrictionDTO> {
	private String dayOfWeek;
	private int releaseMinutes;
	private int femaleSeatCount;
	/** 1.SEAT_TYPE-Individual 2.ALL-Together  */
	private int seatTypeGroupModel;
	private List<ScheduleDTO> scheduleList;
	private List<GroupDTO> groupList;

	public String getScheduleCodes() {
		String schedule = Text.EMPTY;
		if (scheduleList != null) {
			StringBuilder schedules = new StringBuilder();
			for (ScheduleDTO scheduleDTO : scheduleList) {
				if (StringUtil.isNull(scheduleDTO.getCode())) {
					continue;
				}
				schedules.append(scheduleDTO.getCode());
				schedules.append(Text.COMMA);
			}
			schedule = schedules.toString();
		}
		if (StringUtil.isNull(schedule)) {
			schedule = Text.NA;
		}
		return schedule;
	}

	public String getGroupCodes() {
		String group = Text.EMPTY;
		if (groupList != null) {
			StringBuilder groups = new StringBuilder();
			for (GroupDTO groupDTO : groupList) {
				if (StringUtil.isNull(groupDTO.getCode())) {
					continue;
				}
				groups.append(groupDTO.getCode());
				groups.append(Text.COMMA);
			}
			group = groups.toString();
		}
		if (StringUtil.isNull(group)) {
			group = Text.NA;
		}
		return group;
	}
}
