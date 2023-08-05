package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;

public interface BusmapService {

	public TripDTO getSearchBusmapV3(AuthDTO authDTO, TripDTO tripDTO);

	public TripDTO getBusmapforTrip(AuthDTO authDTO, TripDTO tripDTO);

	public List<TicketDTO> getBookedBlockedTickets(AuthDTO authDTO, TripDTO tripDTO);

}
