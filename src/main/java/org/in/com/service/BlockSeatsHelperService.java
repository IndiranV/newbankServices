package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;

public interface BlockSeatsHelperService {
	public void blockSeats(AuthDTO authDTO, BookingDTO bookingDTO, TripDTO tripDTO, TicketDTO ticketDTO);

}
