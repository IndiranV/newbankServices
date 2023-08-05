package org.in.com.service;

import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketTransferDTO;
import org.in.com.dto.TripDTO;

import net.sf.json.JSONArray;

public interface TicketTransferService {
	public TicketDTO transferTicket(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO transferDTO, Map<String, Boolean> additionalAttribute);

	public ScheduleTicketTransferTermsDTO isTransferTicket(AuthDTO authDTO, TicketDTO ticketDTO);

	public BookingDTO blockTicket(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO transferDTO, String paymentGatewayPartnerCode);

	public TicketDTO transferTicketConfirm(AuthDTO authDTO, TicketDTO transferDTO);

	public void isTransferTicket(AuthDTO authDTO, TicketTransferDTO ticketTransferDTO);

	public void transferTicket(AuthDTO authDTO, TicketTransferDTO ticketTransferDTO);

	public JSONArray validateTicketAutoTransfer(AuthDTO authDTO, TripDTO trip, TripDTO transferTrip);
}
