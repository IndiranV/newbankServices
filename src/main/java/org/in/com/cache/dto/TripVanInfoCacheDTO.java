package org.in.com.cache.dto;

import java.util.List;

public class TripVanInfoCacheDTO {
	private String code;
	private String mobileNumber;
	private String tripDate;
	private String notificationTypeCode;
	private int vehicleId;
	private int driverId;
	private int vanPickupId;
	private List<String> scheduleList;
	private int updatedBy;
	private String updatedAt;
	private TripVanInfoCacheDTO tripVanExceptionCache;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getMobileNumber() {
		return mobileNumber;
	}

	public void setMobileNumber(String mobileNumber) {
		this.mobileNumber = mobileNumber;
	}

	public String getTripDate() {
		return tripDate;
	}

	public void setTripDate(String tripDate) {
		this.tripDate = tripDate;
	}

	public String getNotificationTypeCode() {
		return notificationTypeCode;
	}

	public void setNotificationTypeCode(String notificationTypeCode) {
		this.notificationTypeCode = notificationTypeCode;
	}

	public int getVehicleId() {
		return vehicleId;
	}

	public void setVehicleId(int vehicleId) {
		this.vehicleId = vehicleId;
	}

	public int getDriverId() {
		return driverId;
	}

	public void setDriverId(int driverId) {
		this.driverId = driverId;
	}

	public int getVanPickupId() {
		return vanPickupId;
	}

	public void setVanPickupId(int vanPickupId) {
		this.vanPickupId = vanPickupId;
	}

	public List<String> getScheduleList() {
		return scheduleList;
	}

	public void setScheduleList(List<String> scheduleList) {
		this.scheduleList = scheduleList;
	}

	public int getUpdatedBy() {
		return updatedBy;
	}

	public void setUpdatedBy(int updatedBy) {
		this.updatedBy = updatedBy;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public TripVanInfoCacheDTO getTripVanExceptionCache() {
		return tripVanExceptionCache;
	}

	public void setTripVanExceptionCache(TripVanInfoCacheDTO tripVanExceptionCache) {
		this.tripVanExceptionCache = tripVanExceptionCache;
	}

}
