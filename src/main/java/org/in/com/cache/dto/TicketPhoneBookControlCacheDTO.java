package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TicketPhoneBookControlCacheDTO implements Serializable {
	private static final long serialVersionUID = 8818851325259302973L;

	private int id;
	private int activeFlag;
	private int groupId;
	private int userId;
	private int dateType;
	private int ticketStatusId;
	private int maxSlabValueLimit;

	private String code;
	private String name;
	private String refferenceType;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private String slabCalenderMode;
	private String slabCalenderType;
	private String slabMode;

	private List<Integer> scheduleIds;
	private List<String> routeList;
	private List<TicketPhoneBookControlCacheDTO> override = new ArrayList<>();

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

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getDateType() {
		return dateType;
	}

	public void setDateType(int dateType) {
		this.dateType = dateType;
	}

	public int getTicketStatusId() {
		return ticketStatusId;
	}

	public void setTicketStatusId(int ticketStatusId) {
		this.ticketStatusId = ticketStatusId;
	}

	public int getMaxSlabValueLimit() {
		return maxSlabValueLimit;
	}

	public void setMaxSlabValueLimit(int maxSlabValueLimit) {
		this.maxSlabValueLimit = maxSlabValueLimit;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getRefferenceType() {
		return refferenceType;
	}

	public void setRefferenceType(String refferenceType) {
		this.refferenceType = refferenceType;
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

	public String getLookupCode() {
		return lookupCode;
	}

	public void setLookupCode(String lookupCode) {
		this.lookupCode = lookupCode;
	}

	public String getSlabCalenderMode() {
		return slabCalenderMode;
	}

	public void setSlabCalenderMode(String slabCalenderMode) {
		this.slabCalenderMode = slabCalenderMode;
	}

	public String getSlabCalenderType() {
		return slabCalenderType;
	}

	public void setSlabCalenderType(String slabCalenderType) {
		this.slabCalenderType = slabCalenderType;
	}

	public String getSlabMode() {
		return slabMode;
	}

	public void setSlabMode(String slabMode) {
		this.slabMode = slabMode;
	}

	public List<Integer> getScheduleIds() {
		return scheduleIds;
	}

	public void setScheduleIds(List<Integer> scheduleIds) {
		this.scheduleIds = scheduleIds;
	}

	public List<String> getRouteList() {
		return routeList;
	}

	public void setRouteList(List<String> routeList) {
		this.routeList = routeList;
	}

	public List<TicketPhoneBookControlCacheDTO> getOverride() {
		return override;
	}

	public void setOverride(List<TicketPhoneBookControlCacheDTO> override) {
		this.override = override;
	}
}