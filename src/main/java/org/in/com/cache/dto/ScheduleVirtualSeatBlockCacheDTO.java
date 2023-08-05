package org.in.com.cache.dto;

import java.io.Serializable;

public class ScheduleVirtualSeatBlockCacheDTO implements Serializable {
	private static final long serialVersionUID = 519679357785565031L;
	private String code;
	private String scheduleCode;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int refreshMinutes;
	private String lookupCode;
	private int activeFlag;
	private String groupCode;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getActiveFrom() {
		return activeFrom;
	}

	public void setActiveFrom(String activeFrom) {
		this.activeFrom = activeFrom;
	}

	public String getActiveTo() {
		return activeTo;
	}

	public void setActiveTo(String activeTo) {
		this.activeTo = activeTo;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public String getScheduleCode() {
		return scheduleCode;
	}

	public void setScheduleCode(String scheduleCode) {
		this.scheduleCode = scheduleCode;
	}

	public int getRefreshMinutes() {
		return refreshMinutes;
	}

	public void setRefreshMinutes(int refreshMinutes) {
		this.refreshMinutes = refreshMinutes;
	}

	public String getLookupCode() {
		return lookupCode;
	}

	public void setLookupCode(String lookupCode) {
		this.lookupCode = lookupCode;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getGroupCode() {
		return groupCode;
	}

	public void setGroupCode(String groupCode) {
		this.groupCode = groupCode;
	}
}