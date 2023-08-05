package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;

public interface GpsService {
	public TripDTO getTicketVechileLocation(AuthDTO authDTO, TicketDTO ticketDTO);

	public TripDTO getTripVechileLocation(AuthDTO authDTO, TripDTO tripDTO);

	public TripDTO getTripStageVechileLocation(AuthDTO authDTO, TripDTO tripDTO);

}
