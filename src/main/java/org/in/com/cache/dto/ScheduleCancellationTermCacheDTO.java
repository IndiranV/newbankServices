package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScheduleCancellationTermCacheDTO implements Serializable {
	private static final long serialVersionUID = -7366715323665347184L;
	private String code;
	private int CancellationTermId;
	private int groupId;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String lookupCode;
	private List<ScheduleCancellationTermCacheDTO> overrideList = new ArrayList<ScheduleCancellationTermCacheDTO>();

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public int getCancellationTermId() {
		return CancellationTermId;
	}

	public void setCancellationTermId(int cancellationTermId) {
		CancellationTermId = cancellationTermId;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
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

	public List<ScheduleCancellationTermCacheDTO> getOverrideList() {
		return overrideList;
	}

	public void setOverrideList(List<ScheduleCancellationTermCacheDTO> overrideListDTO) {
		this.overrideList = overrideListDTO;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}