package org.in.com.cache.dto;

import java.util.List;

public class ScheduleBookGenderRestrictionCacheDTO {
	private String code;
	private String dayOfWeek;
	private int releaseMinutes;
	private int femaleSeatCount;
	private int seatTypeGroupModel;
	private List<String> scheduleList;
	private List<String> groupList;
	private int activeFlag;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public int getReleaseMinutes() {
		return releaseMinutes;
	}

	public void setReleaseMinutes(int releaseMinutes) {
		this.releaseMinutes = releaseMinutes;
	}

	public int getFemaleSeatCount() {
		return femaleSeatCount;
	}

	public void setFemaleSeatCount(int femaleSeatCount) {
		this.femaleSeatCount = femaleSeatCount;
	}

	public List<String> getScheduleList() {
		return scheduleList;
	}

	public void setScheduleList(List<String> scheduleList) {
		this.scheduleList = scheduleList;
	}

	public List<String> getGroupList() {
		return groupList;
	}

	public void setGroupList(List<String> groupList) {
		this.groupList = groupList;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

	public int getSeatTypeGroupModel() {
		return seatTypeGroupModel;
	}

	public void setSeatTypeGroupModel(int seatTypeGroupModel) {
		this.seatTypeGroupModel = seatTypeGroupModel;
	}
}
