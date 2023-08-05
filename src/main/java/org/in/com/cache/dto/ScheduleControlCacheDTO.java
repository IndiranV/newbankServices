package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScheduleControlCacheDTO implements Serializable {
	private static final long serialVersionUID = -1227884671199859849L;
	private int groupId;
	private int openMinitues;
	private int closeMinitues;
	private String code;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int allowBookingFlag;
	private int fromStationId;
	private int toStationId;
	private List<ScheduleControlCacheDTO> overrideListControlCacheDTO = new ArrayList<ScheduleControlCacheDTO>();

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getOpenMinitues() {
		return openMinitues;
	}

	public void setOpenMinitues(int openMinitues) {
		this.openMinitues = openMinitues;
	}

	public int getCloseMinitues() {
		return closeMinitues;
	}

	public void setCloseMinitues(int closeMinitues) {
		this.closeMinitues = closeMinitues;
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

	public int getAllowBookingFlag() {
		return allowBookingFlag;
	}

	public void setAllowBookingFlag(int allowBookingFlag) {
		this.allowBookingFlag = allowBookingFlag;
	}

	public int getFromStationId() {
		return fromStationId;
	}

	public void setFromStationId(int fromStationId) {
		this.fromStationId = fromStationId;
	}

	public int getToStationId() {
		return toStationId;
	}

	public void setToStationId(int toStationId) {
		this.toStationId = toStationId;
	}

	public List<ScheduleControlCacheDTO> getOverrideListControlCacheDTO() {
		return overrideListControlCacheDTO;
	}

	public void setOverrideListControlCacheDTO(List<ScheduleControlCacheDTO> overrideListControlCacheDTO) {
		this.overrideListControlCacheDTO = overrideListControlCacheDTO;
	}

}