package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.List;

public class ScheduleBusCacheDTO implements Serializable {
	private static final long serialVersionUID = 6141851311975678571L;
	private int id;
	private int activeFlag;
	private String code;
	private int busId;
	private int taxId;
	private int breakevenId;
	private float distance;
	private List<String> amentiesList;

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

	public int getBusId() {
		return busId;
	}

	public void setBusId(int busId) {
		this.busId = busId;
	}

	public List<String> getAmentiesList() {
		return amentiesList;
	}

	public void setAmentiesList(List<String> amentiesList) {
		this.amentiesList = amentiesList;
	}
	
	public int getTaxId() {
		return taxId;
	}

	public void setTaxId(int taxId) {
		this.taxId = taxId;
	}

	public int getBreakevenId() {
		return breakevenId;
	}

	public void setBreakevenId(int breakevenId) {
		this.breakevenId = breakevenId;
	}

	public float getDistance() {
		return distance;
	}

	public void setDistance(float distance) {
		this.distance = distance;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

}