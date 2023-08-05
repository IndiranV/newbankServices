package org.in.com.cache.dto;

import java.io.Serializable;

public class TicketAfterTripTimeCacheDTO implements Serializable {
	private static final long serialVersionUID = -4411746694124678068L;
	private String code;
	private String travelDate;
	private String tripDate;
	private int travelMinutes;
	private int tripMinutes;
	private String busCode;
	private String ticketAt;
	private String namespaceCode;

	private String passengerMobile;
	private String serviceNo;

	private String deviceMediumCode;
	private String remarks;
	private String tripCode;
	private int scheduleId;

	private int ticketUserId;
	private int fromStationId;
	private int toStationId;
	private String ticketStatusCode;

	private String seatName;
	private String passengerName;
	private String seatGendarCode;

	private String boardingPointCode;
	private int boardingPointMinitues;

	public String getTicketAt() {
		return ticketAt;
	}

	public void setTicketAt(String ticketAt) {
		this.ticketAt = ticketAt;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getTravelDate() {
		return travelDate;
	}

	public void setTravelDate(String travelDate) {
		this.travelDate = travelDate;
	}

	public int getTravelMinutes() {
		return travelMinutes;
	}

	public void setTravelMinutes(int travelMinutes) {
		this.travelMinutes = travelMinutes;
	}

	public String getBusCode() {
		return busCode;
	}

	public void setBusCode(String busCode) {
		this.busCode = busCode;
	}

	public String getPassengerMobile() {
		return passengerMobile;
	}

	public void setPassengerMobile(String passengerMobile) {
		this.passengerMobile = passengerMobile;
	}

	public String getServiceNo() {
		return serviceNo;
	}

	public void setServiceNo(String serviceNo) {
		this.serviceNo = serviceNo;
	}

	public String getDeviceMediumCode() {
		return deviceMediumCode;
	}

	public void setDeviceMediumCode(String deviceMediumCode) {
		this.deviceMediumCode = deviceMediumCode;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public String getTripCode() {
		return tripCode;
	}

	public void setTripCode(String tripCode) {
		this.tripCode = tripCode;
	}

	public int getScheduleId() {
		return scheduleId;
	}

	public void setScheduleId(int scheduleId) {
		this.scheduleId = scheduleId;
	}

	public int getTicketUserId() {
		return ticketUserId;
	}

	public void setTicketUserId(int ticketUserId) {
		this.ticketUserId = ticketUserId;
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

	public String getTicketStatusCode() {
		return ticketStatusCode;
	}

	public void setTicketStatusCode(String ticketStatusCode) {
		this.ticketStatusCode = ticketStatusCode;
	}

	public String getTripDate() {
		return tripDate;
	}

	public void setTripDate(String tripDate) {
		this.tripDate = tripDate;
	}

	public String getSeatName() {
		return seatName;
	}

	public void setSeatName(String seatName) {
		this.seatName = seatName;
	}

	public String getPassengerName() {
		return passengerName;
	}

	public void setPassengerName(String passengerName) {
		this.passengerName = passengerName;
	}

	public String getSeatGendarCode() {
		return seatGendarCode;
	}

	public void setSeatGendarCode(String seatGendarCode) {
		this.seatGendarCode = seatGendarCode;
	}

	public String getNamespaceCode() {
		return namespaceCode;
	}

	public void setNamespaceCode(String namespaceCode) {
		this.namespaceCode = namespaceCode;
	}

	public String getBoardingPointCode() {
		return boardingPointCode;
	}

	public void setBoardingPointCode(String boardingPointCode) {
		this.boardingPointCode = boardingPointCode;
	}

	public int getBoardingPointMinitues() {
		return boardingPointMinitues;
	}

	public void setBoardingPointMinitues(int boardingPointMinitues) {
		this.boardingPointMinitues = boardingPointMinitues;
	}

	public int getTripMinutes() {
		return tripMinutes;
	}

	public void setTripMinutes(int tripMinutes) {
		this.tripMinutes = tripMinutes;
	}

}
