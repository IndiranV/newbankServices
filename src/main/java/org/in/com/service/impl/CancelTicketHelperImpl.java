package org.in.com.service.impl;

import org.in.com.aggregator.utility.BitsUtilityService;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.service.CancelTicketHelperService;
import org.in.com.utils.BitsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class CancelTicketHelperImpl implements CancelTicketHelperService {
	@Autowired
	BitsUtilityService bitsUtilityService;

	@Override
	@Async
	public void notifyTripCancel(AuthDTO authDTO, NamespaceDTO namespace, TicketDTO ticketDTO) {
		if (BitsUtil.isTagExists(ticketDTO.getTicketUser().getUserTags(), UserTagEM.API_USER_RB) || BitsUtil.isTagExists(ticketDTO.getTicketUser().getUserTags(), UserTagEM.API_USER_EZ) || BitsUtil.isTagExists(ticketDTO.getTicketUser().getUserTags(), UserTagEM.API_USER_PT) || BitsUtil.isTagExists(ticketDTO.getTicketUser().getUserTags(), UserTagEM.API_USER_AB) || BitsUtil.isTagExists(ticketDTO.getTicketUser().getUserTags(), UserTagEM.OTA_USER)) {
			bitsUtilityService.updateTripCancel(authDTO, namespace, ticketDTO);
		}
	}

	@Override
	public void notifyTicketTransfer(AuthDTO authDTO, NamespaceDTO namespace, TicketDTO ticketDTO) {
		// Red bus
		if (BitsUtil.isTagExists(ticketDTO.getTicketUser().getUserTags(), UserTagEM.API_USER_RB)) {
			bitsUtilityService.updateTicketTransfer(authDTO, namespace, ticketDTO);
		}
	}
}
