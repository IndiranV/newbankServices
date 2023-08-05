package org.in.com.cache.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class TicketDetailsCacheDTO implements Serializable {
	private static final long serialVersionUID = 1L;
	private int ticketStatusId;
	private int passengerAge;
	private String seatName;
	private String seatCode;
	private String seatType;
	private String passengerName;
	private String seatGendarCode;
	private BigDecimal seatFare;
	private BigDecimal acBusTax;

	public int getTicketStatusId() {
		return ticketStatusId;
	}

	public void setTicketStatusId(int ticketStatusId) {
		this.ticketStatusId = ticketStatusId;
	}

	public int getPassengerAge() {
		return passengerAge;
	}

	public void setPassengerAge(int passengerAge) {
		this.passengerAge = passengerAge;
	}

	public String getSeatName() {
		return seatName;
	}

	public void setSeatName(String seatName) {
		this.seatName = seatName;
	}

	public String getSeatCode() {
		return seatCode;
	}

	public void setSeatCode(String seatCode) {
		this.seatCode = seatCode;
	}

	public String getSeatType() {
		return seatType;
	}

	public void setSeatType(String seatType) {
		this.seatType = seatType;
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
