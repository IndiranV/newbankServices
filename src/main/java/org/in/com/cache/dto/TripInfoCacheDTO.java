package org.in.com.cache.dto;

import java.io.Serializable;

public class TripInfoCacheDTO implements Serializable {
	private static final long serialVersionUID = 1L;

	private int busVehicleId;
	private String driverName;
	private String driverMobile;
	private String notificationStatus;
	private String tripStartDateTime;
	private String tripCloseDateTime;
	private String driverName2;
	private String driverMobile2;
	private String attenderName;
	private String attenderMobile;
	private String captainName;
	private String captainMobile;
	private int primaryDriverId;
	private int secondaryDriverId;
	private int attendantId;
	private int captainId;
	/** StartOdometer|DateTime|EndOdometer|DateTime */
	private String extras;
	private String remarks;

	public int getBusVehicleId() {
		return busVehicleId;
	}

	public void setBusVehicleId(int busVehicleId) {
		this.busVehicleId = busVehicleId;
	}

	public String getDriverName() {
		return driverName;
	}

	public void setDriverName(String driverName) {
		this.driverName = driverName;
	}

	public String getDriverMobile() {
		return driverMobile;
	}

	public void setDriverMobile(String driverMobile) {
		this.driverMobile = driverMobile;
	}

	public String getNotificationStatus() {
		return notificationStatus;
	}

	public void setNotificationStatus(String notificationStatus) {
		this.notificationStatus = notificationStatus;
	}

	public String getTripStartDateTime() {
		return tripStartDateTime;
	}

	public void setTripStartDateTime(String tripStartDateTime) {
		this.tripStartDateTime = tripStartDateTime;
	}

	public String getTripCloseDateTime() {
		return tripCloseDateTime;
	}

	public void setTripCloseDateTime(String tripCloseDateTime) {
		this.tripCloseDateTime = tripCloseDateTime;
	}

	public String getDriverName2() {
		return driverName2;
	}

	public void setDriverName2(String driverName2) {
		this.driverName2 = driverName2;
	}

	public String getDriverMobile2() {
		return driverMobile2;
	}

	public void setDriverMobile2(String driverMobile2) {
		this.driverMobile2 = driverMobile2;
	}

	public String getAttenderName() {
		return attenderName;
	}

	public void setAttenderName(String attenderName) {
		this.attenderName = attenderName;
	}

	public String getAttenderMobile() {
		return attenderMobile;
	}

	public void setAttenderMobile(String attenderMobile) {
		this.attenderMobile = attenderMobile;
	}
	
	public String getCaptainName() {
		return captainName;
	}
	
	public void setCaptainName(String captainName) {
		this.captainName = captainName;
	}

	public String getCaptainMobile() {
		return captainMobile;
	}

	public void setCaptainMobile(String captainMobile) {
		this.captainMobile = captainMobile;
	}

	public int getPrimaryDriverId() {
		return primaryDriverId;
	}

	public void setPrimaryDriverId(int primaryDriverId) {
		this.primaryDriverId = primaryDriverId;
	}

	public int getSecondaryDriverId() {
		return secondaryDriverId;
	}

	public void setSecondaryDriverId(int secondaryDriverId) {
		this.secondaryDriverId = secondaryDriverId;
	}

	public int getAttendantId() {
		return attendantId;
	}

	public void setAttendantId(int attendantId) {
		this.attendantId = attendantId;
	}

	public int getCaptainId() {
		return captainId;
	}

	public void setCaptainId(int captainId) {
		this.captainId = captainId;
	}

	public String getExtras() {
		return extras;
	}

	public void setExtras(String extras) {
		this.extras = extras;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

}
