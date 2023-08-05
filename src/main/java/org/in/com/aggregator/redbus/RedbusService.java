package org.in.com.aggregator.redbus;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;

public interface RedbusService {
	public void updateTripCancel(AuthDTO authDTO, TicketDTO ticketDTO, String flag);

}
