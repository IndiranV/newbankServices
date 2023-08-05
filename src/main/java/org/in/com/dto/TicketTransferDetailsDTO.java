package org.in.com.dto;

import lombok.Data;

@Data
public class TicketTransferDetailsDTO {
	private TicketDTO ticket;
	private TicketDTO transferTicket;
	private StationPointDTO boardingPoint;
	private StationPointDTO droppingPoint;
	private ScheduleTicketTransferTermsDTO scheduleTicketTransferTerms;
}
