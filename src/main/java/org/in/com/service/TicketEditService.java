package org.in.com.service;

import java.util.List;
import java.util.Map;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketMigrationDTO;
import org.in.com.dto.TripDTO;

import net.sf.json.JSONArray;

public interface TicketEditService {
	public boolean editBoardingPoint(AuthDTO authDTO, TicketDTO ticketDTO, String event, int notificationFlag);

	public boolean editDroppingPoint(AuthDTO authDTO, TicketDTO ticketDTO, String event, int notificationFlag);

	public boolean editPassengerDetails(AuthDTO authDTO, TicketDTO ticketDTO, String event, int notificationFlag);

	public boolean editChangeSeat(AuthDTO authDTO, TicketDTO ticketDTO, BusSeatLayoutDTO oldSeatLayoutDTO, BusSeatLayoutDTO newSeatLayoutDTO, int notificationFlag);

	public boolean editMobileNumber(AuthDTO authDTO, TicketDTO ticketDTO, String event, int notificationFlag);

	public boolean editEmailId(AuthDTO authDTO, TicketDTO ticketDTO, String event);

	public boolean editAlternateMobileNumber(AuthDTO authDTO, TicketDTO ticketDTO, String event, int notificationFlag);

	public boolean editRemarks(AuthDTO authDTO, TicketDTO ticketDTO, String event, int notificationFlag);

	public boolean editChangeSeatV2(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, BusSeatLayoutDTO> seatLayoutMap, int notificationFlag);

	public void editTicketExtra(AuthDTO authDTO, TicketDTO ticketDTO);

	public boolean editCustomerIdProof(AuthDTO authDTO, TicketDTO ticketDTO, TicketAddonsDetailsDTO ticketAddonsDetails, String event);

	public void addCustomerIdProof(AuthDTO authDTO, TicketDTO ticketDTO, TicketAddonsDetailsDTO ticketAddonsDetails, String event);

	public void updateSeatDetails(AuthDTO authDTO, int notificationFlag, TicketMigrationDTO ticketMigrationDTO);

	public void validateMigrateTicket(AuthDTO authDTO, TicketMigrationDTO ticketMigrationDTO);

	public JSONArray validateTicketAutoMigration(AuthDTO authDTO, TripDTO trip, List<String> ticketList);

}
