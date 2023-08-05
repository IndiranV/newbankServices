package org.in.com.cache.dto;

import java.math.BigDecimal;

public class TripSeatQuotaCacheDTO {
	private String tripCode;
	private int userId;
	private int fromStationId;
	private int toStationId;
	private int relaseMinutes;
	private String remarks;
	private String seatCode;
	private BigDecimal seatFare;
	private BigDecimal acBusTax;
	private String seatName;
	private String seatGendar;
	private int activeFlag;
	private int id;
	private String updatedAt;
	private int updatedUserId;

	public String getTripCode() {
		return tripCode;
	}

	public void setTripCode(String tripCode) {
		this.tripCode = tripCode;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getFromStationId() {
		return fromStationId;
	}

	public void setFromStationId(int fromStationId) {
		this.fromStationId = fromStationId;
	}

	public int getToStationId() {
		return toStationId;
	}

	public void setToStationId(int toStationId) {
		this.toStationId = toStationId;
	}

	public int getRelaseMinutes() {
		return relaseMinutes;
	}

	public void setRelaseMinutes(int relaseMinutes) {
		this.relaseMinutes = relaseMinutes;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getSeatCode() {
		return seatCode;
	}

	public void setSeatCode(String seatCode) {
		this.seatCode = seatCode;
	}

	public String getSeatName() {
		return seatName;
	}

	public void setSeatName(String seatName) {
		this.seatName = seatName;
	}

	public int getActiveFlag() {
		return activeFlag;
	}

	public void setActiveFlag(int activeFlag) {
		this.activeFlag = activeFlag;
	}

	public String getSeatGendar() {
		return seatGendar;
	}

	public void setSeatGendar(String seatGendar) {
		this.seatGendar = seatGendar;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public int getUpdatedUserId() {
		return updatedUserId;
	}

	public void setUpdatedUserId(int updatedUserId) {
		this.updatedUserId = updatedUserId;
	}

	public BigDecimal getSeatFare() {
		return seatFare;
	}

	public void setSeatFare(BigDecimal seatFare) {
		this.seatFare = seatFare;
	}

	public BigDecimal getAcBusTax() {
		return acBusTax;
	}

	public void setAcBusTax(BigDecimal acBusTax) {
		this.acBusTax = acBusTax;
	}

}
