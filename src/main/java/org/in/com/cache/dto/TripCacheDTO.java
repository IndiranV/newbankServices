package org.in.com.cache.dto;

import java.io.Serializable;

public class TripCacheDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private String tripCode;
	private int id;
	private int busId;
	private int tripStatusId;
	private int scheduleId;
	private String tripDate;
	private int tripMinutes;
	private String syncTime;
	private String remarks;

	public int getTripMinutes() {
		return tripMinutes;
	}

	public void setTripMinutes(int tripMinutes) {
		this.tripMinutes = tripMinutes;
	}

	public int getBusId() {
		return busId;
	}

	public void setBusId(int busId) {
		this.busId = busId;
	}

	public int getTripStatusId() {
		return tripStatusId;
	}

	public void setTripStatusId(int tripStatusId) {
		this.tripStatusId = tripStatusId;
	}

	public int getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(int scheduleId) {
		this.scheduleId = scheduleId;
	}

	public String getTripDate() {
		return tripDate;
	}

	public void setTripDate(String tripDate) {
		this.tripDate = tripDate;
	}

	public String getTripCode() {
		return tripCode;
	}

	public void setTripCode(String tripCode) {
		this.tripCode = tripCode;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSyncTime() {
		return syncTime;
	}

	public void setSyncTime(String syncTime) {
		this.syncTime = syncTime;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

}
