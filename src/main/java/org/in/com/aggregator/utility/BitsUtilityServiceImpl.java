package org.in.com.aggregator.utility;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.service.GroupService;
import org.in.com.service.StateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BitsUtilityServiceImpl implements BitsUtilityService {

	@Autowired
	GroupService groupService;
	@Autowired
	StateService stateService;

	public void updateTripCancel(AuthDTO authDTO, NamespaceDTO namespace, TicketDTO ticketDTO) {
		GroupDTO groupDTO = groupService.getGroup(authDTO, ticketDTO.getTicketUser().getGroup());
		ticketDTO.getTicketUser().setGroup(groupDTO);
		
		if (authDTO.getNamespace().getProfile().getState() != null && authDTO.getNamespace().getProfile().getState().getId() != 0) {
			StateDTO state = authDTO.getNamespace().getProfile().getState();
			state = stateService.getState(state);
			authDTO.getNamespace().getProfile().setState(state);
		}
		
		BitsUtilityCommunicator communicator = new BitsUtilityCommunicator();
		communicator.updateTripCancel(authDTO, namespace, ticketDTO);
	}

	public void updateTicketTransfer(AuthDTO authDTO, NamespaceDTO namespace, TicketDTO ticketDTO) {
		GroupDTO groupDTO = groupService.getGroup(authDTO, ticketDTO.getTicketUser().getGroup());
		ticketDTO.getTicketUser().setGroup(groupDTO);
		
		if (authDTO.getNamespace().getProfile().getState() != null && authDTO.getNamespace().getProfile().getState().getId() != 0) {
			StateDTO state = authDTO.getNamespace().getProfile().getState();
			state = stateService.getState(state);
			authDTO.getNamespace().getProfile().setState(state);
		}
		
		BitsUtilityCommunicator communicator = new BitsUtilityCommunicator();
		communicator.updateTicketTransfer(authDTO, namespace, ticketDTO);
	}

	@Override
	public void pushInventoryChangesEvent(AuthDTO authDTO, List<StageDTO> stages) {
		try {
			BitsUtilityCommunicator communicator = new BitsUtilityCommunicator();
			communicator.pushInventoryChangesEvent(authDTO, stages);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
