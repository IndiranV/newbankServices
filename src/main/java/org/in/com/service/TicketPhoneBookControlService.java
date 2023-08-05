package org.in.com.service;

import hirondelle.date4j.DateTime;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.TicketPhoneBookCancelControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketPhoneBookControlDTO;

public interface TicketPhoneBookControlService {

	public TicketPhoneBookControlDTO getActiveTimeControl(AuthDTO authDTO, DateTime tripDate);

	public TicketPhoneBookControlDTO getActiveLimitControl(AuthDTO authDTO, ScheduleDTO scheduleDTO, TicketDTO ticketDTO, DateTime tripDate);

	public TicketPhoneBookControlDTO updateBookLimitControlIUD(AuthDTO authDTO, TicketPhoneBookControlDTO controlDTO);

	public List<TicketPhoneBookControlDTO> getBookLimitsControl(AuthDTO authDTO);

	public TicketPhoneBookControlDTO updatePhoneBookTimeControlIUD(AuthDTO authDTO, TicketPhoneBookControlDTO controlDTO);

	public List<TicketPhoneBookControlDTO> getPhoneBookTimeControl(AuthDTO authDTO);
	
	public List<TicketPhoneBookCancelControlDTO> getPhoneBookCancelControl(AuthDTO authDTO);
	
	public TicketPhoneBookCancelControlDTO updatePhoneBookCancelControl(AuthDTO authDTO, TicketPhoneBookCancelControlDTO phoneBookCancel);
	
	public TicketPhoneBookCancelControlDTO getActivePhoneBookCancelControl(AuthDTO authDTO, ScheduleDTO scheduleDTO, TicketDTO ticketDTO); 

	public CancellationTermDTO getCancellationPolicyConvention(AuthDTO authDTO, TicketPhoneBookCancelControlDTO phoneBookCancelControl, TicketDTO ticketDTO);
}
