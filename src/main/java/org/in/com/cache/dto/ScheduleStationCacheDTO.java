package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScheduleStationCacheDTO implements Serializable {

	private static final long serialVersionUID = 2776735921085564048L;
	private String code;
	private int stationId;
	private int minitues;
	private int stationSequence;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String mobileNumber;
	private String lookupCode;
	private List<ScheduleStationCacheDTO> overrideList = new ArrayList<ScheduleStationCacheDTO>();

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getStationId() {
		return stationId;
	}

	public void setStationId(int stationId) {
		this.stationId = stationId;
	}

	public int getMinitues() {
		return minitues;
	}

	public void setMinitues(int minitues) {
		this.minitues = minitues;
	}

	public int getStationSequence() {
		return stationSequence;
	}

	public void setStationSequence(int stationSequence) {
		this.stationSequence = stationSequence;
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

	public List<ScheduleStationCacheDTO> getOverrideList() {
		return overrideList;
	}

	public void setOverrideList(List<ScheduleStationCacheDTO> overrideListDTO) {
		this.overrideList = overrideListDTO;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}