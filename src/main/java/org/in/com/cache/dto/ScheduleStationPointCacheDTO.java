package org.in.com.cache.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ScheduleStationPointCacheDTO implements Serializable {
	private static final long serialVersionUID = 519679357785565031L;
	private String code;
	private int stationId;
	private int stationPointId;
	private int minitues;
	private String creditDebitFlag;
	private String activeFrom;
	private String activeTo;
	private String dayOfWeek;
	private String boardingDroppingFlag;
	private int vanRouteId;
	private BigDecimal fare;
	private String mobileNumber;
	private String amenitiesCodes;
	private String address;

	private List<ScheduleStationPointCacheDTO> overrideList = new ArrayList<ScheduleStationPointCacheDTO>();

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

	public int getStationPointId() {
		return stationPointId;
	}

	public void setStationPointId(int stationPointId) {
		this.stationPointId = stationPointId;
	}

	public int getMinitues() {
		return minitues;
	}

	public void setMinitues(int minitues) {
		this.minitues = minitues;
	}

	public String getCreditDebitFlag() {
		return creditDebitFlag;
	}

	public void setCreditDebitFlag(String creditDebitFlag) {
		this.creditDebitFlag = creditDebitFlag;
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

	public List<ScheduleStationPointCacheDTO> getOverrideList() {
		return overrideList;
	}

	public void setOverrideList(List<ScheduleStationPointCacheDTO> overrideListDTO) {
		this.overrideList = overrideListDTO;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	public String getBoardingDroppingFlag() {
		return boardingDroppingFlag;
	}

	public void setBoardingDroppingFlag(String boardingDroppingFlag) {
		this.boardingDroppingFlag = boardingDroppingFlag;
	}

	public int getVanRouteId() {
		return vanRouteId;
	}

	public void setVanRouteId(int vanRouteId) {
		this.vanRouteId = vanRouteId;
	}

	public BigDecimal getFare() {
		return fare;
	}

	public void setFare(BigDecimal fare) {
		this.fare = fare;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getAmenitiesCodes() {
		return amenitiesCodes;
	}

	public void setAmenitiesCodes(String amenitiesCodes) {
		this.amenitiesCodes = amenitiesCodes;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

}