package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ScheduleSeatAutoReleaseCacheDTO implements Serializable {
	private static final long serialVersionUID = -2077030309760322132L;
	private int id;
	private int activeFlag;
	private String code;
	private List<Integer> groupId;
	private List<Integer> scheduleId;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int releaseMinutes;
	private String minutesTypeCode;
	private String releaseModeCode;
	private String releaseTypeCode;

	private List<ScheduleSeatAutoReleaseCacheDTO> overrideList = new ArrayList<ScheduleSeatAutoReleaseCacheDTO>();;

}