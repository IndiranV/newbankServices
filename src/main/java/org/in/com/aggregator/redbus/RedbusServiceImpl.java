package org.in.com.aggregator.redbus;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.TicketDTO;
import org.springframework.stereotype.Service;

@Service
public class RedbusServiceImpl implements RedbusService {
	@Override
	public void updateTripCancel(AuthDTO authDTO, TicketDTO ticketDTO, String flag) {
		RedbusCommunicator communicator = new RedbusCommunicator();
		communicator.updateTripCancel(authDTO, ticketDTO, flag);
	}
}
