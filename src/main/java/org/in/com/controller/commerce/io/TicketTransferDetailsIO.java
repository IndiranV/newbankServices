package org.in.com.controller.commerce.io;

import java.util.List;

import lombok.Data;

@Data
public class TicketTransferDetailsIO {
	private String ticketCode;
	private List<String> seatCode;
	private List<String> transferSeatCode;
	private ScheduleTicketTransferTermsIO scheduleTicketTransferTerms;
}
