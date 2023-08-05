package org.in.com.service.impl;

import java.sql.Connection;
import java.sql.SQLTransactionRollbackException;
import java.util.TimeZone;

import org.in.com.aggregator.slack.SlackService;
import org.in.com.constants.Text;
import org.in.com.dao.ConnectDAO;
import org.in.com.dao.TicketDAO;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BlockSeatsHelperService;
import org.in.com.service.TripService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;

@Service
public class BlockSeatsHelperImpl implements BlockSeatsHelperService {
	@Autowired
	TripService cacheService;
	@Autowired
	SlackService slack;

	public void blockSeats(AuthDTO authDTO, BookingDTO bookingDTO, TripDTO tripDTO, TicketDTO ticketDTO) {
		try {
			if (StringUtil.isNull(ticketDTO.getCode())) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			// default Temp blocked, if not phone booking
			if (ticketDTO.getTicketStatus() == null) {
				ticketDTO.setTicketStatus(TicketStatusEM.TMP_BLOCKED_TICKET);
			}

			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				ticketDetailsDTO.setTicketStatus(ticketDTO.getTicketStatus());
			}

			// change ticket addons status
			if (ticketDTO.getTicketAddonsDetails() != null && !ticketDTO.getTicketAddonsDetails().isEmpty()) {
				for (TicketAddonsDetailsDTO discountDetailsDTO : ticketDTO.getTicketAddonsDetails()) {
					discountDetailsDTO.setTicketStatus(ticketDTO.getTicketStatus());
				}
			}

			DateTime now = DateTime.now(TimeZone.getDefault());
			ticketDTO.setBlockingLiveTime(DateUtil.addMinituesToDate(now, authDTO.getNamespace().getProfile().getSeatBlockTime()));
			ticketDTO.setTicketAt(now);

			// Save Passenger and it's Details
			saveTicket(authDTO, bookingDTO, ticketDTO);

		}
		catch (ServiceException e) {
			if (e.getErrorCode() != null) {
				throw new ServiceException(e.getErrorCode());
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_BLOCK_TICKET);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UNABLE_TO_BLOCK_TICKET);
		}
		finally {
			// Clear Trip seat cache
			cacheService.clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
		}

	}

	private void saveTicket(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO) throws Exception {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);
				TicketDAO dao = new TicketDAO();

				// Insert Ticket table
				dao.insertTicket(authDTO, bookingDTO, ticketDTO, connection, Text.EMPTY);

				// Insert Ticket Details table
				dao.insertTicketDetails(authDTO, ticketDTO, connection);

				// Insert Ticket Addons Details table
				dao.insertTicketAddonsDetails(authDTO, ticketDTO, connection);

				// Insert Ticket Extras
				dao.insertTicketExtras(authDTO, ticketDTO, connection);

				if (ticketDTO.getId() == 0) {
					throw new ServiceException(ErrorCode.UNABLE_TO_BLOCK_TICKET);
				}
				TripDAO tripDAO = new TripDAO();
				tripDAO.SaveBookedBlockedSeats(authDTO, ticketDTO, connection);

			}
			catch (SQLTransactionRollbackException e) {
				slack.sendAlert(authDTO, ticketDTO.getCode() + " DL09 - Deadlock found when trying to get lock; try restarting transaction");

				e.printStackTrace();
				connection.rollback();
				throw e;
			}
			catch (Exception e) {
				connection.rollback();
				throw e;
			}
			finally {
				connection.commit();
				connection.setAutoCommit(true);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
	}

}
