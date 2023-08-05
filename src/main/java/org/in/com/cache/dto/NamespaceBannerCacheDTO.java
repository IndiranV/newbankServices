package org.in.com.cache.dto;

import java.util.ArrayList;
import java.util.List;

public class NamespaceBannerCacheDTO {
	private String code;
	private String name;
	private List<Integer> groupId;
	private String displayModel;
	private List<Integer> deviceMediumId;
	private String fromDate;
	private String toDate;
	private String dayOfWeek;
	private String color;
	private String udpatedAt;
	private int activeFlag;
	private List<NamespaceBannerDetailsCacheDTO> bannerDetailsList = new ArrayList<>();

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

	public List<Integer> getGroupId() {
		return groupId;
	}

	public void setGroupId(List<Integer> groupId) {
		this.groupId = groupId;
	}

	public String getDisplayModel() {
		return displayModel;
	}

	public void setDisplayModel(String displayModel) {
		this.displayModel = displayModel;
	}

	public List<Integer> getDeviceMediumId() {
		return deviceMediumId;
	}

	public void setDeviceMediumId(List<Integer> deviceMediumId) {
		this.deviceMediumId = deviceMediumId;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public String getDayOfWeek() {
		return dayOfWeek;
	}

	public void setDayOfWeek(String dayOfWeek) {
		this.dayOfWeek = dayOfWeek;
	}
	
	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}
	
	public String getUdpatedAt() {
		return udpatedAt;
	}

	public void setUdpatedAt(String udpatedAt) {
		this.udpatedAt = udpatedAt;
	}

	public List<NamespaceBannerDetailsCacheDTO> getBannerDetailsList() {
		return bannerDetailsList;
	}

	public void setBannerDetailsList(List<NamespaceBannerDetailsCacheDTO> bannerDetailsList) {
		this.bannerDetailsList = bannerDetailsList;
	}

}
