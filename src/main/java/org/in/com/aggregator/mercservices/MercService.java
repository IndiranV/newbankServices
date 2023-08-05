package org.in.com.aggregator.mercservices;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.MenuDTO;
import org.in.com.dto.NamespaceProfileDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDynamicStageFareDetailsDTO;
import org.in.com.dto.ScheduleTripStageFareDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketHistoryDTO;
import org.in.com.dto.TripDTO;

import net.sf.json.JSONArray;

public interface MercService {
	public void indexTicketHistory(AuthDTO authDTO, TicketDTO Ticket, TicketHistoryDTO history);

	public List<TicketHistoryDTO> searchTicketHistory(AuthDTO authDTO, TicketDTO ticket);

	public void indexFareHistory(AuthDTO auth, ScheduleDTO schedule, TripDTO trip, List<ScheduleDynamicStageFareDetailsDTO> dynamicStageFareRepoList);

	public void indexFareHistory(AuthDTO authDTO, ScheduleDTO schedule, TripDTO tripDTO, ScheduleTripStageFareDTO quickFareOverride);

	public void indexMenuPrivilegeAuditHistory(AuthDTO authDTO, MenuDTO menuDTO, String action);

	public void indexNamespaceProfileHistory(AuthDTO authDTO, NamespaceProfileDTO profileDTO, JSONArray namespaceProfileHistory);

}
