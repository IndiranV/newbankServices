package org.in.com.aggregator.backup;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;

public interface DRService {
	public void flushTicketDetails(AuthDTO authDTO,TicketDTO ticketDTO);

}
