package org.in.com.cache.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ScheduleSeatFareCacheDTO implements Serializable {
	private static final long serialVersionUID = -1249200169394186407L;
	private String code;
	private int busId;
	private String seatCodeList;
	private BigDecimal seatFare;
	private String fareTypeCode;
	private String fareOverrideTypeCode;
	private List<Integer> groupList;
	private List<String> routeList;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private List<ScheduleSeatFareCacheDTO> overrideList = new ArrayList<ScheduleSeatFareCacheDTO>();

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

	public BigDecimal getSeatFare() {
		return seatFare;
	}

	public void setSeatFare(BigDecimal seatFare) {
		this.seatFare = seatFare;
	}

	public String getFareTypeCode() {
		return fareTypeCode;
	}

	public void setFareTypeCode(String fareTypeCode) {
		this.fareTypeCode = fareTypeCode;
	}

	public String getFareOverrideTypeCode() {
		return fareOverrideTypeCode;
	}

	public void setFareOverrideTypeCode(String fareOverrideTypeCode) {
		this.fareOverrideTypeCode = fareOverrideTypeCode;
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

	public List<ScheduleSeatFareCacheDTO> getOverrideList() {
		return overrideList;
	}

	public void setOverrideList(List<ScheduleSeatFareCacheDTO> overrideList) {
		this.overrideList = overrideList;
	}

	public List<Integer> getGroupList() {
		return groupList;
	}

	public void setGroupList(List<Integer> groupList) {
		this.groupList = groupList;
	}

	public List<String> getRouteList() {
		return routeList;
	}

	public void setRouteList(List<String> routeList) {
		this.routeList = routeList;
	}

}