package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.List;

public class TravelStopsCacheDTO implements Serializable {
	private static final long serialVersionUID = -6366692402438252647L;
	private String code;
	private String name;
	private String landmark;
	private String latitude;
	private String longitude;
	private int travelMinutes;
	private int stationId;
	private List<String> amenities;
	private int minutes;
	private String restRoom;
	private String remarks;

	public String getLandmark() {
		return landmark;
	}

	public void setLandmark(String landmark) {
		this.landmark = landmark;
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

	public int getTravelMinutes() {
		return travelMinutes;
	}

	public void setTravelMinutes(int travelMinutes) {
		this.travelMinutes = travelMinutes;
	}

	public int getStationId() {
		return stationId;
	}

	public void setStationId(int stationId) {
		this.stationId = stationId;
	}

	public List<String> getAmenities() {
		return amenities;
	}

	public void setAmenities(List<String> amenities) {
		this.amenities = amenities;
	}

	public int getMinutes() {
		return minutes;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
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

	public String getRestRoom() {
		return restRoom;
	}

	public void setRestRoom(String restRoom) {
		this.restRoom = restRoom;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

}
