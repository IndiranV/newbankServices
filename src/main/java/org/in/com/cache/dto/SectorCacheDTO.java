package org.in.com.cache.dto;

import java.util.List;

public class SectorCacheDTO {

	private int id;
	private int activeFlag;
	private String code;
	private String name;
	private List<Integer> schedules;
	private List<Integer> vehicles;
	private List<Integer> stations;
	private List<Integer> organizations;

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

	public List<Integer> getSchedules() {
		return schedules;
	}

	public void setSchedules(List<Integer> schedules) {
		this.schedules = schedules;
	}

	public List<Integer> getVehicles() {
		return vehicles;
	}

	public void setVehicles(List<Integer> vehicles) {
		this.vehicles = vehicles;
	}

	public List<Integer> getStations() {
		return stations;
	}

	public void setStations(List<Integer> stations) {
		this.stations = stations;
	}

	public List<Integer> getOrganizations() {
		return organizations;
	}

	public void setOrganizations(List<Integer> organizations) {
		this.organizations = organizations;
	}

}
