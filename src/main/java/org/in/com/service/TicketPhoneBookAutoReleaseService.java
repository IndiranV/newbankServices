package org.in.com.service;

import hirondelle.date4j.DateTime;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;

public interface TicketPhoneBookAutoReleaseService {

	public List<TicketDTO> releaseTicket(AuthDTO authDTO, TripDTO tripDTO);

	public List<TicketDTO> confirmAndReleasePhoneBlockTicket(AuthDTO authDTO, DateTime tripDate);

}
