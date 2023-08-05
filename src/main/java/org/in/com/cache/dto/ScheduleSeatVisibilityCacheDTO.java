package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScheduleSeatVisibilityCacheDTO implements Serializable {
	private static final long serialVersionUID = 5731572808212365696L;
	private int id;
	private int activeFlag;
	private String code;
	private int busId;
	private String seatCodeList;
	private int releaseMinutes;
	private String refferenceType;
	private List<String> refferenceList;
	private List<String> routeUsers;
	private String visibilityType;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String remarks;
	private String updatedBy;
	private String updatedAt;
	private List<ScheduleSeatVisibilityCacheDTO> overrideList = new ArrayList<ScheduleSeatVisibilityCacheDTO>();

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getBusId() {
		return busId;
	}

	public void setBusId(int busId) {
		this.busId = busId;
	}

	public String getSeatCodeList() {
		return seatCodeList;
	}

	public void setSeatCodeList(String seatCodeList) {
		this.seatCodeList = seatCodeList;
	}

	public String getRefferenceType() {
		return refferenceType;
	}

	public void setRefferenceType(String refferenceType) {
		this.refferenceType = refferenceType;
	}

	public String getVisibilityType() {
		return visibilityType;
	}

	public void setVisibilityType(String visibilityType) {
		this.visibilityType = visibilityType;
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

	public List<ScheduleSeatVisibilityCacheDTO> getOverrideList() {
		return overrideList;
	}

	public void setOverrideList(List<ScheduleSeatVisibilityCacheDTO> overrideListDTO) {
		this.overrideList = overrideListDTO;
	}

	public int getReleaseMinutes() {
		return releaseMinutes;
	}

	public void setReleaseMinutes(int releaseMinutes) {
		this.releaseMinutes = releaseMinutes;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(String updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public List<String> getRefferenceList() {
		return refferenceList;
	}

	public void setRefferenceList(List<String> refferenceList) {
		this.refferenceList = refferenceList;
	}

	public List<String> getRouteUsers() {
		return routeUsers;
	}

	public void setRouteUsers(List<String> routeUsers) {
		this.routeUsers = routeUsers;
	}
	
}