package org.in.com.dto;

import java.math.BigDecimal;
import java.util.List;

import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;

import hirondelle.date4j.DateTime;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TripChartDetailsDTO extends BaseDTO<TripChartDetailsDTO> {
	private int travelMinutes;
	private int reportingMinutes;
	private String passengerMobile;
	private String alternateMobile;
	private String serviceNumber;
	private String remarks;
	private String ticketCode;
	private String tripDate;
	private String seatName;
	private String seatCode;
	private String seatType;
	private String passengerName;
	private int passengerAge;
	private TicketStatusEM ticketStatus;
	private SeatGendarEM seatGendar;
	private TravelStatusEM travelStatus;
	private DeviceMediumEM deviceMedium;
	private BigDecimal seatFare;
	private BigDecimal acBusTax;
	private String seatInfo;
	private UserDTO user;
	private String ticketAt;
	private StationDTO fromStation;
	private StationPointDTO boardingPoint;
	private StationPointDTO droppingPoint;
	private StationDTO toStation;
	private String tripStageCode;
	private String tripCode;
	private String bookingType;
	private String idProof;
	private List<TicketAddonsDetailsDTO> ticketAddonsDetailsList;
	private DateTime ticketUpdatedAt;
	private DateTime ticketSeatUpdatedAt;

}
