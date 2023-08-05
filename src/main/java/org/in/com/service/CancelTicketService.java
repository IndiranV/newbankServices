package org.in.com.service;

import hirondelle.date4j.DateTime;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.UserDTO;

public interface CancelTicketService {
	public TicketDTO TicketIsCancel(AuthDTO authDTO, TicketDTO ticketDTO);

	public void cancelPhoneBooking(AuthDTO authDTO, TicketDTO ticketDTO);

	public TicketDTO TicketConfirmCancel(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> additionalAttribute);

	public CancellationTermDTO getCancellationPolicyConvention(AuthDTO authDTO, UserDTO user, CancellationTermDTO cancellationTermDTO, StateDTO stateDTO, DateTime travelDateTime, List<BigDecimal> seatFareList);

	public TicketDTO tripCancelInitiate(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> dataMap);

	public void updateTripCancelAcknowledge(AuthDTO authDTO, TicketDTO ticketDTO);

}
