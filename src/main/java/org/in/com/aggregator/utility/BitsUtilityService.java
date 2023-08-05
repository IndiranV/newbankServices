package org.in.com.aggregator.utility;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.TicketDTO;

public interface BitsUtilityService {

	public void updateTripCancel(AuthDTO authDTO, NamespaceDTO namespace, TicketDTO ticketDTO);

	public void updateTicketTransfer(AuthDTO authDTO, NamespaceDTO namespace, TicketDTO ticketDTO);

	public void pushInventoryChangesEvent(AuthDTO authDTO, List<StageDTO> stages);
}
