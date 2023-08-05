package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class ScheduleTimeOverrideCacheDTO implements Serializable {
	private static final long serialVersionUID = -7458483031543258737L;
	private int id;
	private int activeFlag;
	private String code;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int overrideMinutes;
	private int stationId;
	private String overrideTypeCode;
	private boolean reactionFlag;
	private List<ScheduleTimeOverrideCacheDTO> overrideList = new ArrayList<ScheduleTimeOverrideCacheDTO>();
}