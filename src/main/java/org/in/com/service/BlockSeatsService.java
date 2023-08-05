package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.TicketDTO;

public interface BlockSeatsService {

	public BookingDTO blockSeatsV3(AuthDTO authDTO, BookingDTO bookingDTO);

	public void lnstanceBlockSeats(AuthDTO authDTO, TicketDTO ticketDTO);

	public BookingDTO processTicketPayment(AuthDTO authDTO, BookingDTO bookingDTO);

}
