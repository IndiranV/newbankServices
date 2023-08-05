package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.TicketDTO;

public interface ConfirmSeatsService {
	public TicketDTO confirmPhoneBooking(AuthDTO authDTO, TicketDTO ticketDTO);

	public BookingDTO confirmBooking(AuthDTO authDTO, String bookingCode, String transactionMode, String mobileNumber, String emailId);
}
