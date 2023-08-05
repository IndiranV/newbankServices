package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.ReleaseModeEM;
import org.in.com.dto.enumeration.ReleaseTypeEM;
import org.in.com.utils.StringUtil;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleSeatAutoReleaseDTO extends BaseDTO<ScheduleSeatAutoReleaseDTO> {
	private List<ScheduleDTO> schedules;
	private List<GroupDTO> groups;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int releaseMinutes;
	private String lookupCode;
	private MinutesTypeEM minutesTypeEM;
	private ReleaseModeEM releaseModeEM;
	private ReleaseTypeEM releaseTypeEM;

	private List<ScheduleSeatAutoReleaseDTO> overrideList = new ArrayList<ScheduleSeatAutoReleaseDTO>();

	public String getScheduleIds() {
		StringBuilder scheduleCodes = new StringBuilder();
		if (schedules != null) {
			for (ScheduleDTO scheduleDTO : schedules) {
				if (scheduleCodes.length() > 0) {
					scheduleCodes.append(Text.COMMA);
				}
				if (scheduleDTO.getId() != 0) {
					scheduleCodes.append(scheduleDTO.getId());
				}
			}
		}
		if (StringUtil.isNull(scheduleCodes.toString())) {
			scheduleCodes.append(Text.NA);
		}
		return scheduleCodes.toString();
	}

	public String getGroupIds() {
		StringBuilder groupCodes = new StringBuilder();
		if (groups != null) {
			for (GroupDTO groupDTO : groups) {
				if (groupCodes.length() > 0) {
					groupCodes.append(Text.COMMA);
				}
				if (groupDTO.getId() != 0) {
					groupCodes.append(groupDTO.getId());
				}
			}
		}
		if (StringUtil.isNull(groupCodes.toString())) {
			groupCodes.append(Text.NA);
		}
		return groupCodes.toString();
	}

}