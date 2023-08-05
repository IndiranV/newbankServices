package org.in.com.service;

import java.sql.Connection;
import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TicketRefundDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.TicketStatusEM;

import hirondelle.date4j.DateTime;

public interface TicketService {
	public TicketDTO showTicket(AuthDTO authDTO, TicketDTO ticketDTO);

	public TicketDTO notifyTicket(AuthDTO authDTO, TicketDTO ticketDTO);

	public void notifyTicketV2(AuthDTO authDTO, TicketDTO ticketDTO, String emailType);

	public TicketDTO getTicketStatus(AuthDTO authDTO, TicketDTO ticketDTO);

	public void updateTicketLookup(AuthDTO authDTO, BookingDTO bookingDTO);

	public List<TicketDTO> getTicketStatusByBookingCode(AuthDTO authDTO, String bookingCode);

	public void getTicketTransaction(AuthDTO authDTO, TicketDTO ticketDTO);

	public List<TicketDTO> findTicket(AuthDTO authDTO, TicketDTO ticketDTO);

	public List<TicketDTO> findTicketbyMobileCouponHistory(AuthDTO authDTO, String passengerMobile, String coupon);

	public TicketDTO getAutoPassengerDetails(AuthDTO authDTO, String mobileNumber, int seatCount);

	public List<TicketDTO> getPhoneBookingTickets(AuthDTO authDTO, DateTime fromDate, DateTime toDate);

	public boolean checkSeatDuplicateEntry(AuthDTO authDTO, TicketDTO ticketDTO);

	public boolean checkSeatDuplicateEntryV2(AuthDTO authDTO, TicketDTO ticketDTO);

	public boolean checkDuplicateTicketCodeEntry(AuthDTO authDTO, String ticketCode);

	public void saveTicketTransaction(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO);

	public void UpdateTicketStatus(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO);

	public void updateTicketStatusV2(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO);

	public void UpdateTicketDetailsStatus(Connection connection, AuthDTO authDTO, TicketDTO ticketDTOl, String auditEvent);

	public void saveTicketCancelTransaction(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO);

	public void saveTicketCancellationDetails(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO);

	public TicketRefundDTO getRefundTicket(AuthDTO authDTO, TicketRefundDTO ticketRefundDTO);

	public void updateRefundTicket(AuthDTO authDTO, TicketRefundDTO ticketRefundDTO);

	public void updateTicketRemarks(AuthDTO authDTO, TicketDTO ticketDTO);

	public void updatePhoneBookTicketPaymentStatus(AuthDTO authDTO, TicketDTO ticketDTO);

	public void getTicketTripDetails(AuthDTO authDTO, TicketDTO ticketDTO);

	public void updateTravelStatus(AuthDTO authDTO, TicketDTO ticketDTO);

	public List<TicketDTO> getTicketsForFeedback(AuthDTO authDTO, TicketDTO ticket);

	public void rejectTripCancelTransaction(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO);

	public void insertTicketAuditLog(AuthDTO authDTO, TicketDTO ticketDTO, Connection connection, String actionEvent);

	public void updateTripCancelTicketDetail(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO);

	public boolean isTripCancelInitiated(AuthDTO authDTO, TicketDTO ticketDTO);

	public void releaseTentativeBlockTicket(AuthDTO authDTO, TicketDTO ticketDTO);

	public void applyMobileNumberMasking(AuthDTO authDTO, TicketDTO ticketDTO);

	public TicketAddonsDetailsDTO checkTicketUsed(AuthDTO authDTO, TicketDTO ticketDTO);

	public void generateLinkPay(AuthDTO authDTO, TicketDTO ticketDTO);

	public TicketExtraDTO generateLinkPayV2(AuthDTO authDTO, TicketDTO ticketDTO);

	public void pushInventoryChangesEvent(AuthDTO authDTO, TicketDTO ticketDTO);

	public TicketExtraDTO getTicketExtra(AuthDTO authDTO, TicketDTO ticket);

	public void addAfterTravelCoupon(AuthDTO authDTO, List<String> ticketList);

	public List<TicketTransactionDTO> getTicketTransactionV2(AuthDTO authDTO, TicketDTO ticketDTO);

	public boolean isServiceFirstTicket(AuthDTO authDTO, TicketDTO ticketDTO);

	public void updateTicketForUser(AuthDTO authDTO, TicketDTO ticketDTO);

	public void findTicketByLookupId(AuthDTO authDTO, TicketDTO ticketDTO);

	public List<TicketDTO> findTicketByLookupIdV2(AuthDTO authDTO, TicketDTO ticketDTO);

	public void getTicketCancelTransactionDetails(AuthDTO authDTO, TicketTransactionDTO transactionDTO, UserDTO userDTO);

	public void migrateTicketNetRevenue(AuthDTO authDTO, DateTime fromDate);

	public List<TripDTO> getTripsForPhoneBlockForceRelease(AuthDTO authDTO, DateTime travelDate);

	public void updateTopRoute(AuthDTO authDTO);

	public void saveTicketAddons(AuthDTO authDTO, TicketDTO ticketDTO);

	public List<TicketAddonsDetailsDTO> getTicketAddonsDetails(AuthDTO authDTO, TicketDTO ticketDTO);

	public void updateTicketStatusToBlock(AuthDTO authDTO, List<GroupDTO> groups, List<TicketStatusEM> ticketStatusList, int numberOfDays);

	public List<TicketDTO> getRecentTicketUserCustomer(AuthDTO auth);

}
