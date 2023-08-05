package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDTO;

public interface ScheduleTicketTransferTermsService {
	public ScheduleTicketTransferTermsDTO updateScheduleTicketTransferTerms(AuthDTO authDTO, ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO);

	public List<ScheduleTicketTransferTermsDTO> getAllScheduleTicketTransferTerms(AuthDTO authDTO);

	public ScheduleTicketTransferTermsDTO getScheduleTicketTransferTermsBySchedule(AuthDTO authDTO, ScheduleDTO schedule, StationDTO fromStation, StationDTO toStation);

	public ScheduleTicketTransferTermsDTO getScheduleTicketTransferTermsByTicket(AuthDTO authDTO, TicketDTO ticketDTO);
}
