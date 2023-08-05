package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ScheduleStageCacheDTO implements Serializable {
	private static final long serialVersionUID = 8920830136164752895L;

	private int id;
	private int activeFlag;
//	private String code;
	private int fromStationId;
	private int toStationId;
	private String busSeatTypeCode;
	private int groupId;
	private int scheduleId;
	private double fare;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private List<ScheduleStageCacheDTO> overrideList = new ArrayList<ScheduleStageCacheDTO>();

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

//	public String getCode() {
//		return code;
//	}
//
//	public void setCode(String code) {
//		this.code = code;
//	}

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

	public String getBusSeatTypeCode() {
		return busSeatTypeCode;
	}

	public void setBusSeatTypeCode(String busSeatTypeCode) {
		this.busSeatTypeCode = busSeatTypeCode;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public double getFare() {
		return fare;
	}

	public void setFare(double fare) {
		this.fare = fare;
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

	public List<ScheduleStageCacheDTO> getOverrideList() {
		return overrideList;
	}

	public void setOverrideList(List<ScheduleStageCacheDTO> overrideListDTO) {
		this.overrideList = overrideListDTO;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public int getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(int scheduleId) {
		this.scheduleId = scheduleId;
	}

}