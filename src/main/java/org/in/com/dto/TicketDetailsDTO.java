package org.in.com.dto;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.math.RoundingMode;

import lombok.Data;
import lombok.EqualsAndHashCode;

import org.in.com.constants.Numeric;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;

@Data
@EqualsAndHashCode(callSuper = true)
public class TicketDetailsDTO extends BaseDTO<TicketDetailsDTO> {
	private int ticketId;
	private String ticketCode;
	private String tripStageCode;
	private String boardingPointName;
	private String stationPoint;
	private int scheduleId;
	private String seatName;
	private String seatCode;
	private String seatType;
	private TicketStatusEM ticketStatus;
	private String passengerName;
	private String contactNumber;
	private String idProof;
	private int passengerAge;
	private SeatGendarEM seatGendar;
	private StationDTO fromStation;
	private StationDTO toStation;
	private BigDecimal seatFare = BigDecimal.ZERO;
	private BigDecimal acBusTax = BigDecimal.ZERO;
	private BigDecimal cancellationChargeTax = BigDecimal.ZERO;
	private BigDecimal refundAmount = BigDecimal.ZERO;
	private BigDecimal cancellationCharges = BigDecimal.ZERO;
	private BigDecimal netAmount = BigDecimal.ZERO;
	private UserDTO user;
	private DateTime updatedAt;
	private DateTime ticketAt;
	private TravelStatusEM travelStatus;
	private TicketExtraDTO ticketExtra;

	public int getBlockReleaseMinutes() {
		return ticketExtra != null ? ticketExtra.getBlockReleaseMinutes() : Numeric.ZERO_INT;
	}

	public BigDecimal getNetRevenueAmount() {
		BigDecimal netRevenueAmount = BigDecimal.ZERO;
		if (netAmount.compareTo(BigDecimal.ZERO) == 1 && (ticketStatus.getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketStatus.getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId())) {
			netRevenueAmount = netAmount.subtract(acBusTax).setScale(2, RoundingMode.CEILING);
		}
		else if (ticketStatus.getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
			netRevenueAmount = netAmount;
		}
		return netRevenueAmount;
	}

	public String getSeatTypeName() {
		return seatType.equals(BusSeatTypeEM.SEATER.getCode()) || seatType.equals(BusSeatTypeEM.SEMI_SLEEPER.getCode()) || seatType.equals(BusSeatTypeEM.PUSH_BACK.getCode()) || seatType.equals(BusSeatTypeEM.SINGLE_SEATER.getCode()) || seatType.equals(BusSeatTypeEM.SINGLE_SEMI_SLEEPER.getCode()) ? "ST" : "SL";
	}
}
