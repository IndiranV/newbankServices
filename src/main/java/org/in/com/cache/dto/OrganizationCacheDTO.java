package org.in.com.cache.dto;

import java.io.Serializable;

public class OrganizationCacheDTO implements Serializable {
	private static final long serialVersionUID = -4365121145815670126L;
	private int id;
	private int activeFlag;
	private String code;
	private String name;
	private String address1;
	private String address2;
	private String contact;
	private String stationCode;
	private String pincode;
	private String latLon;
	private String shortCode;
	private int workingMinutes;

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

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getContact() {
		return contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getStationCode() {
		return stationCode;
	}

	public void setStationCode(String stationCode) {
		this.stationCode = stationCode;
	}

	public String getPincode() {
		return pincode;
	}

	public void setPincode(String pincode) {
		this.pincode = pincode;
	}

	public String getLatLon() {
		return latLon;
	}

	public void setLatLon(String latLon) {
		this.latLon = latLon;
	}

	public String getShortCode() {
		return shortCode;
	}

	public void setShortCode(String shortCode) {
		this.shortCode = shortCode;
	}

	public int getWorkingMinutes() {
		return workingMinutes;
	}

	public void setWorkingMinutes(int minutes) {
		this.workingMinutes = minutes;
	}

}
