package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.List;

public class ScheduleStationPointExceptionCacheDTO implements Serializable {
	private static final long serialVersionUID = 519679357785565031L;
	private String code;
	private int stationId;
	private String stationPointId;
	private String stationPointCode;
	private String scheduleCode;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private List<String> tripDates;
	private int releaseMinutes;
	private String boardingDroppingFlag;
	private String stationPointType;

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

	public String getStationPointId() {
		return stationPointId;
	}

	public void setStationPointId(String stationPointId) {
		this.stationPointId = stationPointId;
	}

	public String getStationPointCode() {
		return stationPointCode;
	}

	public void setStationPointCode(String stationPointCode) {
		this.stationPointCode = stationPointCode;
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

	public String getScheduleCode() {
		return scheduleCode;
	}

	public void setScheduleCode(String scheduleCode) {
		this.scheduleCode = scheduleCode;
	}

	public int getReleaseMinutes() {
		return releaseMinutes;
	}

	public void setReleaseMinutes(int releaseMinutes) {
		this.releaseMinutes = releaseMinutes;
	}

	public String getBoardingDroppingFlag() {
		return boardingDroppingFlag;
	}

	public void setBoardingDroppingFlag(String boardingDroppingFlag) {
		this.boardingDroppingFlag = boardingDroppingFlag;
	}

	public List<String> getTripDates() {
		return tripDates;
	}

	public void setTripDates(List<String> tripDates) {
		this.tripDates = tripDates;
	}

	public String getStationPointType() {
		return stationPointType;
	}

	public void setStationPointType(String stationPointType) {
		this.stationPointType = stationPointType;
	}
	
}