package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.TicketDTO;

public interface CancelTicketHelperService {
	public void notifyTripCancel(AuthDTO authDTO, NamespaceDTO namespace, TicketDTO ticketDTO);

	public void notifyTicketTransfer(AuthDTO authDTO, NamespaceDTO namespace, TicketDTO ticketDTO);
	
}
