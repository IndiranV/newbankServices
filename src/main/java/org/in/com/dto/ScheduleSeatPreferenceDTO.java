package org.in.com.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Text;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.utils.StringUtil;

@Data
@EqualsAndHashCode(callSuper = true)
public class ScheduleSeatPreferenceDTO extends BaseDTO<ScheduleSeatPreferenceDTO> {
	private ScheduleDTO schedule;
	private BusDTO bus;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private SeatGendarEM gendar;
	private List<GroupDTO> groupList;
	private String lookupCode;
	private List<ScheduleSeatPreferenceDTO> overrideList = new ArrayList<ScheduleSeatPreferenceDTO>();
	private AuditDTO audit;
	
	public String getGroups() {
		String groupCodes = Text.EMPTY;
		if (groupList != null) {
			StringBuilder group = new StringBuilder();
			for (GroupDTO groupDTO : groupList) {
				if (StringUtil.isNotNull(groupDTO.getCode())) {
					group.append(groupDTO.getCode());
					group.append(Text.COMMA);
				}
			}
			groupCodes = group.toString();
		}
		if (StringUtil.isNull(groupCodes)) {
			groupCodes = Text.NA;
		}
		return groupCodes;
	}
}