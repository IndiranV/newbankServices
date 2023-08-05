package org.in.com.cache.dto;

import java.util.List;

import org.in.com.dto.StageDTO;

public class ScheduleEnrouteBookControlCacheDTO {
	private String code;
	private List<StageDTO> stageList;
	private int releaseMinutes;
	private String dayOfWeek;
	private int enRouteTypeId;
	private int activeFlag;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public List<StageDTO> getStageList() {
		return stageList;
	}

	public void setStageList(List<StageDTO> stageList) {
		this.stageList = stageList;
	}

	public int getReleaseMinutes() {
		return releaseMinutes;
	}

	public void setReleaseMinutes(int releaseMinutes) {
		this.releaseMinutes = releaseMinutes;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public int getEnRouteTypeId() {
		return enRouteTypeId;
	}

	public void setEnRouteTypeId(int enRouteTypeId) {
		this.enRouteTypeId = enRouteTypeId;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

}
