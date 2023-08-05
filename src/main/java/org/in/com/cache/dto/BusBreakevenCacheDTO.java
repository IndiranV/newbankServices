package org.in.com.cache.dto;

public class BusBreakevenCacheDTO {
	private int id;
	private String code;
	private String name;
	private int busId;
	private String breakevenDetails;
	private int activeFlag;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
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

	public int getBusId() {
		return busId;
	}

	public void setBusId(int busId) {
		this.busId = busId;
	}

	public String getBreakevenDetails() {
		return breakevenDetails;
	}

	public void setBreakevenDetails(String breakevenDetails) {
		this.breakevenDetails = breakevenDetails;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

}
