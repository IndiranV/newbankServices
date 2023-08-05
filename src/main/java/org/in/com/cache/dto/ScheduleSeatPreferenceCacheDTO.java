package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScheduleSeatPreferenceCacheDTO implements Serializable {
	private static final long serialVersionUID = 8887048555978993335L;
	private String code;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private int busId;
	private String seatCodeList;
	private String SeatGendarCode;
	private List<String> groupCodes;
	private List<ScheduleSeatPreferenceCacheDTO> overrideList = new ArrayList<ScheduleSeatPreferenceCacheDTO>();

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

	public String getSeatGendarCode() {
		return SeatGendarCode;
	}

	public void setSeatGendarCode(String seatGendarCode) {
		SeatGendarCode = seatGendarCode;
	}

	public List<String> getGroupCodes() {
		return groupCodes;
	}

	public void setGroupCodes(List<String> groupCodes) {
		this.groupCodes = groupCodes;
	}

	public List<ScheduleSeatPreferenceCacheDTO> getOverrideList() {
		return overrideList;
	}

	public void setOverrideList(List<ScheduleSeatPreferenceCacheDTO> overrideListDTO) {
		this.overrideList = overrideListDTO;
	}

}