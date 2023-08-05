package org.in.com.cache.dto;

import java.io.Serializable;
import java.util.List;

public class TicketCacheDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private String code;
	private String bookingCode;
	private String travelDate;
	private String tripDate;
	private int tripMinutes;
	private int travelMinutes;
	private int reportingMinutes;
	private String blockingLiveTime;
	private String busCode;
	private String ticketAt;

	private String passengerMobile;
	private String passengerEmailId;
	private String serviceNo;
	private String alternateMobile;

	private String journeyType;
	private String deviceMediumCode;
	private String remarks;
	private String tripCode;
	private String tripStageCode;
	private String relatedTicketCode;

	private int ticketUserId;
	private int ticketForUserId;
	private int transactionTypeId;
	private int transactionModeId;
	private int fromStationId;
	private int toStationId;
	private int cancellationTermId;

	private ScheduleStationPointCacheDTO boardingPointCacheDTO;
	private ScheduleStationPointCacheDTO droppingPointCacheDTO;
	private String ticketStatusCode;
	private String releatedStageCode;
	private List<TicketDetailsCacheDTO> ticketDetailsCache;
	private List<TicketAddonsDetailsCacheDTO> ticketAddonsDetailsCache;

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

	public String getBookingCode() {
		return bookingCode;
	}

	public void setBookingCode(String bookingCode) {
		this.bookingCode = bookingCode;
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

	public int getReportingMinutes() {
		return reportingMinutes;
	}

	public void setReportingMinutes(int reportingMinutes) {
		this.reportingMinutes = reportingMinutes;
	}

	public String getBlockingLiveTime() {
		return blockingLiveTime;
	}

	public void setBlockingLiveTime(String blockingLiveTime) {
		this.blockingLiveTime = blockingLiveTime;
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

	public String getPassengerEmailId() {
		return passengerEmailId;
	}

	public void setPassengerEmailId(String passengerEmailId) {
		this.passengerEmailId = passengerEmailId;
	}

	public String getServiceNo() {
		return serviceNo;
	}

	public void setServiceNo(String serviceNo) {
		this.serviceNo = serviceNo;
	}
	
	public String getAlternateMobile() {
		return alternateMobile;
	}

	public void setAlternateMobile(String alternateMobile) {
		this.alternateMobile = alternateMobile;
	}

	public String getJourneyType() {
		return journeyType;
	}

	public void setJourneyType(String journeyType) {
		this.journeyType = journeyType;
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

	public String getTripStageCode() {
		return tripStageCode;
	}

	public void setTripStageCode(String tripStageCode) {
		this.tripStageCode = tripStageCode;
	}

	public String getRelatedTicketCode() {
		return relatedTicketCode;
	}

	public void setRelatedTicketCode(String relatedTicketCode) {
		this.relatedTicketCode = relatedTicketCode;
	}

	public int getTicketUserId() {
		return ticketUserId;
	}

	public void setTicketUserId(int ticketUserId) {
		this.ticketUserId = ticketUserId;
	}

	public int getTicketForUserId() {
		return ticketForUserId;
	}

	public void setTicketForUserId(int ticketForUserId) {
		this.ticketForUserId = ticketForUserId;
	}

	public int getTransactionTypeId() {
		return transactionTypeId;
	}

	public void setTransactionTypeId(int transactionTypeId) {
		this.transactionTypeId = transactionTypeId;
	}

	public int getTransactionModeId() {
		return transactionModeId;
	}

	public void setTransactionModeId(int transactionModeId) {
		this.transactionModeId = transactionModeId;
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

	public int getCancellationTermId() {
		return cancellationTermId;
	}

	public void setCancellationTermId(int cancellationTermId) {
		this.cancellationTermId = cancellationTermId;
	}

	public ScheduleStationPointCacheDTO getBoardingPointCacheDTO() {
		return boardingPointCacheDTO;
	}

	public void setBoardingPointCacheDTO(ScheduleStationPointCacheDTO boardingPointCacheDTO) {
		this.boardingPointCacheDTO = boardingPointCacheDTO;
	}

	public ScheduleStationPointCacheDTO getDroppingPointCacheDTO() {
		return droppingPointCacheDTO;
	}

	public void setDroppingPointCacheDTO(ScheduleStationPointCacheDTO droppingPointCacheDTO) {
		this.droppingPointCacheDTO = droppingPointCacheDTO;
	}

	public String getTicketStatusCode() {
		return ticketStatusCode;
	}

	public void setTicketStatusCode(String ticketStatusCode) {
		this.ticketStatusCode = ticketStatusCode;
	}

	public String getReleatedStageCode() {
		return releatedStageCode;
	}

	public void setReleatedStageCode(String releatedStageCode) {
		this.releatedStageCode = releatedStageCode;
	}

	public List<TicketDetailsCacheDTO> getTicketDetailsCache() {
		return ticketDetailsCache;
	}

	public void setTicketDetailsCache(List<TicketDetailsCacheDTO> ticketDetailsCache) {
		this.ticketDetailsCache = ticketDetailsCache;
	}

	public List<TicketAddonsDetailsCacheDTO> getTicketAddonsDetailsCache() {
		return ticketAddonsDetailsCache;
	}

	public void setTicketAddonsDetailsCache(List<TicketAddonsDetailsCacheDTO> ticketAddonsDetailsCache) {
		this.ticketAddonsDetailsCache = ticketAddonsDetailsCache;
	}

	public String getTripDate() {
		return tripDate;
	}

	public void setTripDate(String tripDate) {
		this.tripDate = tripDate;
	}

	public int getTripMinutes() {
		return tripMinutes;
	}

	public void setTripMinutes(int tripMinutes) {
		this.tripMinutes = tripMinutes;
	}

}
