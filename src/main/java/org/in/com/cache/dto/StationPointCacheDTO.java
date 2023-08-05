package org.in.com.cache.dto;

import java.io.Serializable;

public class StationPointCacheDTO implements Serializable {

	private static final long serialVersionUID = 8035489126963000241L;
	private int id;
	private int activeFlag;
	private String code;
	private String name;
	private String latitude;
	private String longitude;
	private String address;
	private String landmark;
	private String number;
	private int stationId;
	private String mapUrl;
	private String amenitiesCodes;

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

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getLandmark() {
		return landmark;
	}

	public void setLandmark(String landmark) {
		this.landmark = landmark;
	}

	public String getNumber() {
		return number;
	}

	public void setNumber(String number) {
		this.number = number;
	}

	public int getStationId() {
		return stationId;
	}

	public void setStationId(int stationId) {
		this.stationId = stationId;
	}

	public String getMapUrl() {
		return mapUrl;
	}

	public void setMapUrl(String mapUrl) {
		this.mapUrl = mapUrl;
	}
	
	public String getAmenitiesCodes() {
		return amenitiesCodes;
	}

	public void setAmenitiesCodes(String amenitiesCodes) {
		this.amenitiesCodes = amenitiesCodes;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}
