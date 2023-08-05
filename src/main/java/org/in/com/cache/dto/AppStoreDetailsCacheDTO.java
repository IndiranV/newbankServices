package org.in.com.cache.dto;

public class AppStoreDetailsCacheDTO {
	private String code;
	private String udid;
	private String gcmToken;
	private String os;
	private String model;
	private String brand;
	private String deviceMedium;
	private int activeFlag;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getUdid() {
		return udid;
	}

	public void setUdid(String udid) {
		this.udid = udid;
	}

	public String getGcmToken() {
		return gcmToken;
	}

	public void setGcmToken(String gcmToken) {
		this.gcmToken = gcmToken;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public String getModel() {
		return model;
	}

	public void setModel(String model) {
		this.model = model;
	}

	public String getBrand() {
		return brand;
	}

	public void setBrand(String brand) {
		this.brand = brand;
	}

	public String getDeviceMedium() {
		return deviceMedium;
	}

	public void setDeviceMedium(String deviceMedium) {
		this.deviceMedium = deviceMedium;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}
	
}
