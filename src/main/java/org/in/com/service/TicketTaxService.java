package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketTaxDTO;
import org.in.com.dto.TripDTO;

public interface TicketTaxService {

	public TicketTaxDTO getTicketTax(AuthDTO authDTO, TicketDTO ticket);

	public void addTicketTax(AuthDTO authDTO, TicketDTO ticket, TicketTaxDTO ticketTax);

	public void updateTicketTax(AuthDTO authDTO, TicketDTO ticket, TicketTaxDTO ticketTaxDTO);

	public void sendTaxInvoiceEmail(AuthDTO authDTO, TicketDTO ticketDTO);

	public NamespaceTaxDTO getTax(AuthDTO authDTO, TripDTO tripDTO, BookingDTO bookingDTO);

	public NamespaceTaxDTO getTaxByGstin(AuthDTO authDTO, String gstin);
}
