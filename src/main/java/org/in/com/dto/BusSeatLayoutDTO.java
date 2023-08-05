package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatStatusEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusSeatLayoutDTO extends BaseDTO<BusSeatLayoutDTO> {
	private int rowPos;
	private int colPos;
	private int layer;
	private int sequence;
	private BigDecimal fare;
	private BigDecimal discountFare = BigDecimal.ZERO;
	private String ticketCode;
	private GroupDTO group;
	private UserDTO user;
	private OrganizationDTO organization;
	private SeatStatusEM seatStatus = SeatStatusEM.UN_KNOWN;
	private SeatGendarEM SeatGendar;
	private BusSeatTypeEM busSeatType;
	private String passengerName;
	private String remarks;
	private String boardingPointName;
	private String stationPoint;
	private StationDTO fromStation;
	private StationDTO toStation;
	private int passengerAge;
	private boolean phoneTicketFlag;
	private String contactNumber;
	private DateTime updatedAt;
	private DateTime releaseAt;
	private int orientation;
	
	public boolean isAllowBlockedSeatBooking() {
		if (seatStatus.getId() == SeatStatusEM.BLOCKED.getId() && passengerAge == 1) {
			return true;
		}
		return false;
	}
}
