package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TicketDTO;

public interface TicketHelperService {
	public void processTicketAfterTripTime(AuthDTO authDTO, BookingDTO bookingDTO);

	public List<TicketDTO> getAllTicketAfterTripTime(AuthDTO authDTO, List<ScheduleDTO> scheduleList);
	
	public void processTicketAfterTripTimeCancel(AuthDTO authDTO, TicketDTO ticketDTO);

	public void acknowledgeTicketAfterTripTime(AuthDTO authDTO, String ticketCode);

	public List<TicketDTO> getInProcegressNotBoardedTicket(AuthDTO authDTO);

}
