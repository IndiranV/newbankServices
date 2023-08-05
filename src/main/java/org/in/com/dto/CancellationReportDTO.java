package org.in.com.dto;

import java.math.BigDecimal;

import lombok.Data;
import hirondelle.date4j.DateTime;

@Data
public class CancellationReportDTO {

	private ScheduleDTO	schedule;
	private String		ticketCode;
	private DateTime	bookedAt;
	private DateTime	travelAt;
	private UserDTO		bookedBy;
	private DateTime	cancelledAt;
	private String		seatNbrs;
	private Integer		noOfSeats;
	private UserDTO		cancelledBy;
	private BigDecimal		ticketAmnt;
	private BigDecimal		cancellationCharge;
	private BigDecimal		refundAmnt;
	private BigDecimal		agentCommission;
	private BigDecimal		balanceAmnt;
}
