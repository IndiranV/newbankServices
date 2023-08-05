package org.in.com.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.dto.enumeration.TripActivitiesEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.utils.DateUtil;

import com.google.gson.Gson;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;
import net.sf.json.JSONObject;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripDTO extends BaseDTO<TripDTO> {
	private DateTime tripDate;
	private int tripMinutes;
	private DateTime tripStartTime;
	private DateTime tripCloseTime;
	private String syncTime;
	private ScheduleDTO schedule;
	private List<StageDTO> stageList;
	private int bookedSeatCount;
	private int travelStopCount;
	private BigDecimal totalBookedAmount = BigDecimal.ZERO;
	private String remarks;
	// Via stages
	private List<ScheduleStationDTO> stationList;
	private List<String> releatedStageCodeList;
	private StageDTO stage;
	private SearchDTO search;
	private BusDTO bus;
	private CancellationTermDTO cancellationTerm;
	private List<AmenitiesDTO> amenities;
	private List<TripActivitiesEM> activities = new ArrayList<TripActivitiesEM>();
	private TripStatusEM tripStatus;
	private List<TicketDetailsDTO> ticketDetailsList;
	private List<EventTriggerDTO> eventList;
	private Map<String, String> additionalAttributes = new HashMap<String, String>();
	private JSONObject breakeven;
	private JSONObject revenue;
	// tripchart
	private TripInfoDTO tripInfo;
	private GPSLocationDTO location;
	private int cancelledSeatCount;
	private int multiStageBookedSeatCount;
	private BigDecimal totalCancelledAmount = BigDecimal.ZERO;
	private BigDecimal revenueAmount = BigDecimal.ZERO;

	public DateTime getTripDateTime() {
		return DateUtil.addMinituesToDate(tripDate, stage.getFromStation().getMinitues());
	}

	public DateTime getTripDateTimeV2() {
		return DateUtil.addMinituesToDate(tripDate, tripMinutes);
	}

	public String toString() {
		return (getCode() + "  -" + (tripStatus != null ? tripStatus.getCode() : "") + "-" + (schedule != null ? schedule.toString() : ""));
	}

	public String getTripOriginTime() {
		return DateUtil.getMinutesToTime(getTripOriginMinutes());
	}

	// get origin minutes without exception
	public int getTripOriginMinutes() {
		int minutes = 0;
		ScheduleStationDTO stationDTO = null;
		if (stationList != null && !stationList.isEmpty()) {
			for (ScheduleStationDTO scheduleStationDTO : stationList) {
				if (stationDTO == null) {
					stationDTO = scheduleStationDTO;
				}
				if (scheduleStationDTO.getStationSequence() < stationDTO.getStationSequence()) {
					stationDTO = scheduleStationDTO;
				}
			}
		}
		minutes = stationDTO != null ? stationDTO.getMinitues() : 0;
		return minutes;
	}
	
	// get origin minutes with exception
	public int getTripOriginMinutesWithException() {
		int minutes = 0;
		ScheduleStationDTO stationDTO = null;
		if (stationList != null && !stationList.isEmpty()) {
			for (ScheduleStationDTO scheduleStationDTO : stationList) {
				if (scheduleStationDTO.getActiveFlag() != 1) {
					continue;
				}
				if (stationDTO == null) {
					stationDTO = scheduleStationDTO;
				}
				if (scheduleStationDTO.getStationSequence() < stationDTO.getStationSequence()) {
					stationDTO = scheduleStationDTO;
				}
			}
		}
		minutes = stationDTO != null ? stationDTO.getMinitues() : 0;
		return minutes;
	}

	public String toJSON() {
		Gson gson = new Gson();
		if (this != null) {
			gson.toJson(this);
		}
		return gson.toJson(this);
	}

}
