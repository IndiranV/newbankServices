package org.in.com.cache.dto;

import java.util.ArrayList;
import java.util.List;

public class ScheduleBusOverrideCacheDTO {
	private String code;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int scheduleId;
	private int busId;
	private int taxId;
	private String categoryCode;
	private String lookupCode;
	private List<String> tripDates;

	private List<ScheduleBusOverrideCacheDTO> overrideList = new ArrayList<ScheduleBusOverrideCacheDTO>();

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

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}

	public void setActiveTo(String activeTo) {
		this.activeTo = activeTo;
	}

	public int getBusId() {
		return busId;
	}

	public void setBusId(int busId) {
		this.busId = busId;
	}
	
	public int getTaxId() {
		return taxId;
	}

	public void setTaxId(int taxId) {
		this.taxId = taxId;
	}

	public int getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(int scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getCategoryCode() {
		return categoryCode;
	}

	public void setCategoryCode(String categoryCode) {
		this.categoryCode = categoryCode;
	}

	public String getLookupCode() {
		return lookupCode;
	}

	public void setLookupCode(String lookupCode) {
		this.lookupCode = lookupCode;
	}

	public List<String> getTripDates() {
		return tripDates;
	}

	public void setTripDates(List<String> tripDates) {
		this.tripDates = tripDates;
	}
	
	public List<ScheduleBusOverrideCacheDTO> getOverrideList() {
		return overrideList;
	}

	public void setOverrideList(List<ScheduleBusOverrideCacheDTO> overrideList) {
		this.overrideList = overrideList;
	}

}
