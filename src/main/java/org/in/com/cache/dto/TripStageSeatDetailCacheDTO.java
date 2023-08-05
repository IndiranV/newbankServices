package org.in.com.cache.dto;

import java.io.Serializable;

public class TripStageSeatDetailCacheDTO implements Serializable {
	private static final long serialVersionUID = -5664405329405348337L;
	private String tripCode;
	private String tripStageCode;
	private String seatCode;
	private String seatName;
	private String ticketCode;
	private int fromStationId;
	private int toStationId;
	private int passengerAge;
	private int userId;
	private String seatGendarCode;
	private String ticketStatusCode;
	private String updatedAt;
	private String ticketAt;

	private String seatFare;
	private String boardingPointName;
	private String stationPoint;
	private String passengerName;
	private String contactNumber;
	private int blockReleaseMinutes;
	private String netAmount;
	private String ticketEditDetails;
	private String acBusTax;
	private int ticketTransferMinutes;
	private int travelStatus;

	public String getTripCode() {
		return tripCode;
	}

	public void setTripCode(String tripCode) {
		this.tripCode = tripCode;
	}

	public String getTripStageCode() {
		return tripStageCode;
	}

	public void setTripStageCode(String tripStageCode) {
		this.tripStageCode = tripStageCode;
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

	public String getTicketCode() {
		return ticketCode;
	}

	public void setTicketCode(String ticketCode) {
		this.ticketCode = ticketCode;
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

	public int getPassengerAge() {
		return passengerAge;
	}

	public void setPassengerAge(int passengerAge) {
		this.passengerAge = passengerAge;
	}

	public String getSeatGendarCode() {
		return seatGendarCode;
	}

	public void setSeatGendarCode(String seatGendarCode) {
		this.seatGendarCode = seatGendarCode;
	}

	public String getTicketStatusCode() {
		return ticketStatusCode;
	}

	public void setTicketStatusCode(String ticketStatusCode) {
		this.ticketStatusCode = ticketStatusCode;
	}

	public String getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(String updatedAt) {
		this.updatedAt = updatedAt;
	}

	public String getBoardingPointName() {
		return boardingPointName;
	}

	public void setBoardingPointName(String boardingPointName) {
		this.boardingPointName = boardingPointName;
	}

	public String getPassengerName() {
		return passengerName;
	}

	public void setPassengerName(String passengerName) {
		this.passengerName = passengerName;
	}

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getSeatFare() {
		return seatFare;
	}

	public void setSeatFare(String seatFare) {
		this.seatFare = seatFare;
	}

	public String getStationPoint() {
		return stationPoint;
	}

	public void setStationPoint(String stationPoint) {
		this.stationPoint = stationPoint;
	}

	public int getBlockReleaseMinutes() {
		return blockReleaseMinutes;
	}

	public void setBlockReleaseMinutes(int blockReleaseMinutes) {
		this.blockReleaseMinutes = blockReleaseMinutes;
	}

	public String getTicketAt() {
		return ticketAt;
	}

	public void setTicketAt(String ticketAt) {
		this.ticketAt = ticketAt;
	}

	public String getNetAmount() {
		return netAmount;
	}

	public void setNetAmount(String netAmount) {
		this.netAmount = netAmount;
	}

	public String getTicketEditDetails() {
		return ticketEditDetails;
	}

	public void setTicketEditDetails(String ticketEditDetails) {
		this.ticketEditDetails = ticketEditDetails;
	}

	public String getAcBusTax() {
		return acBusTax;
	}

	public void setAcBusTax(String acBusTax) {
		this.acBusTax = acBusTax;
	}

	public int getTicketTransferMinutes() {
		return ticketTransferMinutes;
	}

	public void setTicketTransferMinutes(int ticketTransferMinutes) {
		this.ticketTransferMinutes = ticketTransferMinutes;
	}

	public int getTravelStatus() {
		return travelStatus;
	}

	public void setTravelStatus(int travelStatus) {
		this.travelStatus = travelStatus;
	}

}
