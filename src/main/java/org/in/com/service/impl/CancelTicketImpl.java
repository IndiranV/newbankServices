package org.in.com.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLTransactionRollbackException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import org.in.com.aggregator.backup.DRService;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.slack.SlackService;
import org.in.com.aggregator.sms.SMSService;
import org.in.com.cache.BusCache;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ConnectDAO;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CancellationPolicyDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.CommissionDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TicketPhoneBookCancelControlDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.CommissionTypeEM;
import org.in.com.dto.enumeration.DynamicPriceProviderEM;
import org.in.com.dto.enumeration.EventNotificationEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.RefundStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.CancelTicketHelperService;
import org.in.com.service.CancelTicketService;
import org.in.com.service.CancellationTermsService;
import org.in.com.service.CommissionService;
import org.in.com.service.NamespaceTaxService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleTicketTransferTermsService;
import org.in.com.service.StationService;
import org.in.com.service.TicketHelperService;
import org.in.com.service.TicketPhoneBookControlService;
import org.in.com.service.TicketService;
import org.in.com.service.TransactionOTPService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.service.UserTransactionService;
import org.in.com.service.pg.PaymentRefundService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;
import net.sf.ehcache.Element;

@Service
public class CancelTicketImpl extends CacheCentral implements CancelTicketService {
	@Autowired
	TicketService ticketService;
	@Autowired
	CancellationTermsService termsService;
	@Autowired
	UserTransactionService transactionService;
	@Autowired
	TripService tripService;
	@Autowired
	CommissionService commissionService;
	@Autowired
	SMSService smsService;
	@Autowired
	DRService drService;
	@Autowired
	PaymentRefundService refundService;
	@Autowired
	TransactionOTPService otpService;
	@Autowired
	EmailService emailService;
	@Autowired
	CancelTicketHelperService cancelTicketHelperService;
	@Autowired
	SlackService slack;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	StationService stationService;
	@Autowired
	NamespaceTaxService taxService;
	@Autowired
	ScheduleDynamicStageFareService dynamicFareService;
	@Autowired
	TicketPhoneBookControlService phoneBookCancelControlService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	ScheduleTicketTransferTermsService scheduleTicketTransferTermsService;
	@Lazy
	@Autowired
	NotificationService notificationService;
	@Autowired
	UserService userService;
	@Autowired
	AuthService authService;
	@Autowired
	ScheduleDynamicStageFareService dpService;
	@Lazy
	@Autowired
	TicketHelperService ticketHelperService;

	private static final Logger logger = LoggerFactory.getLogger(CancelTicketImpl.class);
	private static final Logger TRIP_CANCEL_LOGGER = LoggerFactory.getLogger("org.in.com.service.impl.CancelTicketImpl");

	public TicketDTO TicketIsCancel(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDTO DBTicketDTO = new TicketDTO();
		DBTicketDTO.setCode(ticketDTO.getCode());
		DBTicketDTO.setOverideFlag(ticketDTO.isOverideFlag());
		ticketService.getTicketStatus(authDTO, DBTicketDTO);
		if (DBTicketDTO.getId() != 0 && (DBTicketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || DBTicketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() || DBTicketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId())) {
			/** Check Same Trip Date Permission */
			if (authDTO.getAdditionalAttribute().containsKey(Text.ALLOW_BOOKING_CANCEL_SAME_DAY) && Numeric.ONE.equals(authDTO.getAdditionalAttribute().get(Text.ALLOW_BOOKING_CANCEL_SAME_DAY)) && DateUtil.NOW().compareTo(DBTicketDTO.getTripDate().getEndOfDay()) > 0) {
				throw new ServiceException(ErrorCode.CANCELLATION_NOT_ALLOWED, "Same trip date tickets only allowed to cancel");
			}
			// Validate Cancellation Allowed
			if ((DBTicketDTO.getJourneyType().getId() == JourneyTypeEM.POSTPONE.getId() || DBTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId()) && (!authDTO.getAdditionalAttribute().containsKey(Text.TRANSFER_BOOKING_CANCEL) || Numeric.ZERO.equals(authDTO.getAdditionalAttribute().get(Text.TRANSFER_BOOKING_CANCEL)))) {
				throw new ServiceException(ErrorCode.CANCELLATION_NOT_ALLOWED, "Permission is not enabled");
			}
			else if (DBTicketDTO.getJourneyType().getId() == JourneyTypeEM.ALL_TRIP.getId()) {
				throw new ServiceException(ErrorCode.CANCELLATION_NOT_ALLOWED);
			}
			// No Cancel after 6 PM
			DateTime currentDate = DateUtil.getDateTime(DateUtil.convertDate(DateUtil.NOW()));
			if (authDTO.getAdditionalAttribute().containsKey(Text.NO_CANCEL_AFTER_6PM) && Numeric.ONE.equals(authDTO.getAdditionalAttribute().get(Text.NO_CANCEL_AFTER_6PM)) && currentDate.compareTo(DBTicketDTO.getTripDate()) == 0 && currentDate.getHour() >= 18) {
				throw new ServiceException(ErrorCode.CANCELLATION_NOT_ALLOWED, "After 6PM");
			}

			DBTicketDTO.setTicketUser(getUserDTOById(authDTO, DBTicketDTO.getTicketUser()));
			// get Ticket Details
			TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
			ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
			DBTicketDTO.setTicketXaction(ticketTransactionDTO);
			ticketService.getTicketTransaction(authDTO, DBTicketDTO);

			// Cancellation datetime based on NS Settings
			DateTime travelDateTime = getCancellationDateTime(authDTO, DBTicketDTO);

			// get cancellation terms for the ticket
			CancellationTermDTO cancellationTermDTO = termsService.getCancellationTermsById(authDTO, DBTicketDTO.getCancellationTerm());
			cancellationTermDTO = getCancellationPolicyConvention(authDTO, ticketDTO.getTicketUser(), cancellationTermDTO, DBTicketDTO.getFromStation().getState(), travelDateTime, DBTicketDTO.getSeatFareUniqueList());
			DBTicketDTO.setCancellationTerm(cancellationTermDTO);

			if (DBTicketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				ScheduleDTO scheduleDTO = tripService.getTrip(authDTO, DBTicketDTO.getTripDTO()).getSchedule();
				TicketPhoneBookCancelControlDTO phoneBookControl = phoneBookCancelControlService.getActivePhoneBookCancelControl(authDTO, scheduleDTO, DBTicketDTO);
				DBTicketDTO.setCancellationTerm(phoneBookCancelControlService.getCancellationPolicyConvention(authDTO, phoneBookControl, DBTicketDTO));
			}
			if (DBTicketDTO.getCancellationTerm() == null || DBTicketDTO.getCancellationTerm().getPolicyList() == null || DBTicketDTO.getCancellationTerm().getPolicyList().isEmpty()) {
				throw new ServiceException(ErrorCode.CANCELLATION_TERMS_NOT_FOUND);
			}

			if ((DBTicketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || DBTicketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) && (DBTicketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId() || DBTicketDTO.getJourneyType().getId() == JourneyTypeEM.RETURN_TRIP.getId())) {
				DBTicketDTO.setScheduleTicketTransferTerms(scheduleTicketTransferTermsService.getScheduleTicketTransferTermsByTicket(authDTO, DBTicketDTO));
				if (DBTicketDTO.getScheduleTicketTransferTerms() != null && DBTicketDTO.getScheduleTicketTransferTerms().getId() != 0) {
					calculateScheduleTicketTransferDateTime(authDTO, DBTicketDTO);
				}
			}

			// Validate User Permissions
			if (authDTO.getUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
				if (DBTicketDTO.getTicketUser().getUserRole().getId() != UserRoleEM.CUST_ROLE.getId()) {
					throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_USER_FAIL);
				}
				else if (StringUtil.isNull(ticketDTO.getPassengerEmailId()) && StringUtil.isNull(ticketDTO.getPassengerMobile())) {
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
				}
				else if (StringUtil.isNotNull(ticketDTO.getPassengerMobile()) && !ticketDTO.getPassengerMobile().equalsIgnoreCase(DBTicketDTO.getPassengerMobile())) {
					throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_MOBILE_FAIL);
				}
				else if (StringUtil.isNotNull(ticketDTO.getPassengerEmailId()) && !ticketDTO.getPassengerEmailId().equalsIgnoreCase(DBTicketDTO.getPassengerEmailId())) {
					throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_EMAIL_FAIL);
				}
			}
			else if (authDTO.getAdditionalAttribute().containsKey(Text.BOOKING_CANCEL_ALL_GROUP) && Numeric.ONE.equals(authDTO.getAdditionalAttribute().get(Text.BOOKING_CANCEL_ALL_GROUP)) && authDTO.getUser().getGroup().getId() != DBTicketDTO.getTicketUser().getGroup().getId()) {
				throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_USER_FAIL, "Same group tickets only allowed to cancel");
			}
			else if (!ticketDTO.isOverideFlag() && authDTO.getUser().getId() != DBTicketDTO.getTicketUser().getId()) {
				throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_USER_FAIL);
			}
			// Calculate Cancellation Charge
			calculateCancellationCharges(authDTO, DBTicketDTO, cancellationTermDTO, new HashMap<String, String>());

			// Calculate GST On Cancellation Charge
			calculateTAXOnCancellationCharge(authDTO, DBTicketDTO);

			/** Instant Cancellation Till DateTime */
			DBTicketDTO.setInstantCancellationTill(BitsUtil.getInstantCancellationTill(authDTO, DBTicketDTO.getTicketXaction().getUpdatedAt()));

			BusCache busCache = new BusCache();
			DBTicketDTO.getTripDTO().setBus(busCache.getBusDTObyId(authDTO, DBTicketDTO.getTripDTO().getBus()));
		}
		else {
			if (DBTicketDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			else if (DBTicketDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() || DBTicketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
				throw new ServiceException(ErrorCode.NOT_CONFIRM_BOOKED_TICKET);
			}
			else if (DBTicketDTO.getTicketStatus().getId() == TicketStatusEM.TICKET_TRANSFERRED.getId()) {
				throw new ServiceException(ErrorCode.TRANSFERED_TICKET_NOT_ALLOW_CANCEL);
			}
		}
		return DBTicketDTO;
	}

	public void cancelPhoneBooking(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDTO DBTicketDTO = new TicketDTO();
		DBTicketDTO.setCode(ticketDTO.getCode());

		// get Ticket Details
		ticketService.getTicketStatus(authDTO, DBTicketDTO);

		int repTicketDetailsCount = DBTicketDTO.getBookedTicketDetails(TicketStatusEM.PHONE_BLOCKED_TICKET).size();
		// User Validations and check override flag
		if (authDTO.getUser().getId() != DBTicketDTO.getTicketUser().getId() && !ticketDTO.isOverideFlag()) {
			throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_USER_FAIL);
		}

		// cancel only selected seats
		if (DBTicketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
			for (Iterator<TicketDetailsDTO> iterator = DBTicketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
				TicketDetailsDTO ticketDetailsDTO = iterator.next();
				if (!ticketDTO.getSeatCodeList().contains(ticketDetailsDTO.getSeatCode())) {
					iterator.remove();
					continue;
				}
				if (ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					iterator.remove();
					continue;
				}
			}
		}
		else {
			throw new ServiceException(ErrorCode.NOT_PHONE_BOOKED_TICKET);
		}
		if (DBTicketDTO.getTicketDetails().isEmpty()) {
			throw new ServiceException(ErrorCode.SEAT_ALREADY_CANCELLED);
		}

		// Validate phone book cancel control
		if (!ticketDTO.isOverideFlag()) {
			applyPhoneBookCancelControl(authDTO, DBTicketDTO);
		}

		for (TicketDetailsDTO ticketDetailsDTO : DBTicketDTO.getTicketDetails()) {
			if (ticketDTO.isOverideFlag() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CANCELLATION_ALLOWED.getId()) {
				ticketDetailsDTO.setTicketStatus(TicketStatusEM.PHONE_BOOKING_CANCELLED);
			}
			else {
				throw new ServiceException(ErrorCode.CANCELLATION_TIME_OVER);
			}

			// Update ticket transaction status
			for (TicketAddonsDetailsDTO ticketAddonsDetailsDTO : DBTicketDTO.getTicketAddonsDetails()) {
				if (ticketAddonsDetailsDTO.getSeatCode().equals(ticketDetailsDTO.getSeatCode())) {
					ticketAddonsDetailsDTO.setTicketStatus(TicketStatusEM.PHONE_BOOKING_CANCELLED);
					break;
				}
			}
		}
		// check all seat has been cancelled in the ticket
		if (DBTicketDTO.getTicketDetails().size() == repTicketDetailsCount) {
			DBTicketDTO.setTicketStatus(TicketStatusEM.PHONE_BOOKING_CANCELLED);
		}
		SaveCancelPhoneBooking(authDTO, DBTicketDTO);

		// Send Cancel SMS
		smsService.sendPhoneBookingCancelSMS(authDTO, DBTicketDTO);

		notificationPushService.pushPhoneBookTicketCancelNotification(authDTO, DBTicketDTO);
		// Send Ticket Event
		if (StringUtil.isContains(Constants.TICKET_EVENT, authDTO.getNamespaceCode())) {
			smsService.sendTicketEventSMS(authDTO, DBTicketDTO, "Phone Block Cancelled");
		}
		// send Ticket Event To Customer
		notificationService.sendCustomerTicketEvent(authDTO, DBTicketDTO, EventNotificationEM.PHONE_TICKET_CANCEL);
		// Dynamic Pricing Call back
		if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
			// tripService.getTrip(authDTO, DBTicketDTO.getTripDTO());
			dynamicFareService.updateTicketStatus(authDTO, DBTicketDTO);
		}
		// Upload to Backup Server
		drService.flushTicketDetails(authDTO, DBTicketDTO);
		tripService.clearBookedBlockedSeatsCache(authDTO, DBTicketDTO.getTripDTO());
	}

	private void SaveCancelPhoneBooking(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);

				// update ticket table
				ticketService.UpdateTicketStatus(connection, authDTO, ticketDTO);

				// update Trip seat details table
				tripService.updateTripSeatDetailsStatus(connection, authDTO, ticketDTO);

			}
			catch (SQLTransactionRollbackException e) {
				slack.sendAlert(authDTO, ticketDTO.getCode() + " DL07-  Deadlock found when trying to get lock; try restarting transaction");

				e.printStackTrace();
				connection.rollback();
				throw e;
			}
			catch (ServiceException e) {
				connection.rollback();
				throw e;
			}
			catch (Exception e) {
				e.printStackTrace();
				connection.rollback();
				throw e;
			}
			finally {
				connection.commit();
				connection.setAutoCommit(true);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error("Error in saving the cancel phone ticket: " + e.getMessage());
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);

		}
	}

	public TicketDTO TicketConfirmCancel(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> additionalAttribute) {
		TicketDTO repositoryTicketDTO = new TicketDTO();
		repositoryTicketDTO.setCode(ticketDTO.getCode());
		repositoryTicketDTO.setOverideFlag(ticketDTO.isOverideFlag());
		repositoryTicketDTO.setCancellationOverideRefundAmount(ticketDTO.getCancellationOverideRefundAmount());
		repositoryTicketDTO.setCancellationOveridePercentageFlag(ticketDTO.isCancellationOveridePercentageFlag());
		repositoryTicketDTO.setCancellationOverideValue(ticketDTO.getCancellationOverideValue());
		// get Ticket Details
		ticketService.getTicketStatus(authDTO, repositoryTicketDTO);

		int totalBookedSeatCount = repositoryTicketDTO.getBookedTicketDetails(TicketStatusEM.CONFIRM_BOOKED_TICKETS).size();
		if (repositoryTicketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
			/** Check Same Trip Date Permission */
			if (authDTO.getAdditionalAttribute().containsKey(Text.ALLOW_BOOKING_CANCEL_SAME_DAY) && Numeric.ONE.equals(authDTO.getAdditionalAttribute().get(Text.ALLOW_BOOKING_CANCEL_SAME_DAY)) && DateUtil.NOW().compareTo(repositoryTicketDTO.getTripDate().getEndOfDay()) > 0) {
				throw new ServiceException(ErrorCode.CANCELLATION_NOT_ALLOWED, "Same trip date tickets only allowed to cancel");
			}
			// No Cancel after 6 PM
			DateTime currentDate = DateUtil.getDateTime(DateUtil.convertDate(DateUtil.NOW()));
			if (authDTO.getAdditionalAttribute().containsKey(Text.NO_CANCEL_AFTER_6PM) && Numeric.ONE.equals(authDTO.getAdditionalAttribute().get(Text.NO_CANCEL_AFTER_6PM)) && currentDate.compareTo(repositoryTicketDTO.getTripDate()) == 0 && DateUtil.NOW().getHour() >= 18) {
				throw new ServiceException(ErrorCode.CANCELLATION_NOT_ALLOWED, "After 6PM");
			}
			// Remove Un selected Seats
			for (Iterator<TicketDetailsDTO> iterator = repositoryTicketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
				TicketDetailsDTO ticketDetailsDTO = iterator.next();
				if (!ticketDTO.getSeatCodeList().contains(ticketDetailsDTO.getSeatCode())) {
					iterator.remove();
					continue;
				}
				if (ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
					iterator.remove();
					continue;
				}
			}

			if (ticketDTO.getTicketDetails().size() != repositoryTicketDTO.getTicketDetails().size()) {
				throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
			}
			if (repositoryTicketDTO.getTicketDetails().isEmpty()) {
				throw new ServiceException(ErrorCode.SEAT_ALREADY_CANCELLED);
			}

			// Validate Cancellation Allowed
			if ((repositoryTicketDTO.getJourneyType().getId() == JourneyTypeEM.POSTPONE.getId() || repositoryTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId()) && (!authDTO.getAdditionalAttribute().containsKey(Text.TRANSFER_BOOKING_CANCEL) || Numeric.ZERO.equals(authDTO.getAdditionalAttribute().get(Text.TRANSFER_BOOKING_CANCEL)))) {
				throw new ServiceException(ErrorCode.CANCELLATION_NOT_ALLOWED);
			}
			else if (repositoryTicketDTO.getJourneyType().getId() == JourneyTypeEM.ALL_TRIP.getId()) {
				throw new ServiceException(ErrorCode.CANCELLATION_NOT_ALLOWED);
			}

			// Remove respective Ticket Addons Details
			for (Iterator<TicketAddonsDetailsDTO> iterator = repositoryTicketDTO.getTicketAddonsDetails().iterator(); iterator.hasNext();) {
				TicketAddonsDetailsDTO addonsDetailsDTO = iterator.next();
				if (addonsDetailsDTO.getTicketDetailsId(repositoryTicketDTO.getTicketDetails()) == 0) {
					iterator.remove();
					continue;
				}
			}
			// get Ticket Transaction
			TicketTransactionDTO transactionDTO = new TicketTransactionDTO();
			transactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
			repositoryTicketDTO.setTicketXaction(transactionDTO);
			ticketService.getTicketTransaction(authDTO, repositoryTicketDTO);

			// get cancellation terms for the ticket
			CancellationTermDTO cancellationTermDTO = termsService.getCancellationTermsById(authDTO, repositoryTicketDTO.getCancellationTerm());

			ticketDTO.setCancellationTerm(cancellationTermDTO);
			// Validate User Permissions
			if (authDTO.getUser().getUserRole().getId() != UserRoleEM.USER_ROLE.getId()) {
				if (authDTO.getUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
					if (ticketDTO.getOtpNumber() != 0) {
						if (!otpService.validateOTP(authDTO, ticketDTO.getCode(), repositoryTicketDTO.getPassengerMobile(), ticketDTO.getOtpNumber())) {
							throw new ServiceException(ErrorCode.INVAILD_TRANSACTION_OTP);
						}
					}
					else if (StringUtil.isNull(ticketDTO.getPassengerEmailId()) || !ticketDTO.getPassengerEmailId().equalsIgnoreCase(repositoryTicketDTO.getPassengerEmailId()) || repositoryTicketDTO.getTicketUser().getUserRole().getId() != UserRoleEM.CUST_ROLE.getId()) {
						throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_EMAIL_FAIL);
					}
				}
				else if (authDTO.getAdditionalAttribute().containsKey(Text.BOOKING_CANCEL_ALL_GROUP) && Numeric.ONE.equals(authDTO.getAdditionalAttribute().get(Text.BOOKING_CANCEL_ALL_GROUP)) && authDTO.getUser().getGroup().getId() != repositoryTicketDTO.getTicketUser().getGroup().getId()) {
					throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_USER_FAIL, "Same group tickets only allowed to cancel");
				}
				else if (!ticketDTO.isOverideFlag() && authDTO.getUser().getId() != repositoryTicketDTO.getTicketUser().getId()) {
					throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_USER_FAIL);
				}
			}
			else if (authDTO.getAdditionalAttribute().containsKey(Text.BOOKING_CANCEL_ALL_GROUP) && Numeric.ONE.equals(authDTO.getAdditionalAttribute().get(Text.BOOKING_CANCEL_ALL_GROUP)) && authDTO.getUser().getGroup().getId() != repositoryTicketDTO.getTicketUser().getGroup().getId()) {
				throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_USER_FAIL, "Same group tickets only allowed to cancel");
			}
			else if (!ticketDTO.isOverideFlag() && authDTO.getUser().getId() != repositoryTicketDTO.getTicketUser().getId()) {
				throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_USER_FAIL);
			}
			// calculate cancellation charge
			if (ticketDTO.isOverideFlag() && ticketDTO.getCancellationOverideRefundAmount() != null && (!authDTO.getAdditionalAttribute().containsKey(Text.OVERRIDE_AS_PER_POLICY_FLAG) || authDTO.getAdditionalAttribute().get(Text.OVERRIDE_AS_PER_POLICY_FLAG).equals(Numeric.ZERO))) {
				for (TicketDetailsDTO ticketDetailsDTO : repositoryTicketDTO.getTicketDetails()) {
					BigDecimal deductionAmount = ticketDetailsDTO.getSeatFare().subtract((ticketDTO.getCancellationOverideRefundAmount().divide(new BigDecimal(repositoryTicketDTO.getTicketDetails().size()), 2, RoundingMode.CEILING)));
					ticketDetailsDTO.setRefundAmount(ticketDetailsDTO.getSeatFare().subtract(deductionAmount));
					ticketDetailsDTO.setCancellationCharges(deductionAmount);
				}
				authDTO.getAdditionalAttribute().put("OVERRIDE_REFUND", Numeric.ONE);
			}
			else {
				getCancellationDateTime(authDTO, repositoryTicketDTO);
				calculateCancellationCharges(authDTO, repositoryTicketDTO, cancellationTermDTO, additionalAttribute);
			}

			// Calculate GST On Cancellation Charge
			calculateTAXOnCancellationCharge(authDTO, repositoryTicketDTO);

			if (repositoryTicketDTO.getTicketStatus().getId() == TicketStatusEM.CANCELLATION_NOT_ALLOWED.getId()) {
				throw new ServiceException(ErrorCode.CANCELLATION_NOT_ALLOWED);
			}

			if (repositoryTicketDTO.getRefundAmount().setScale(0, BigDecimal.ROUND_DOWN).compareTo(repositoryTicketDTO.getTicketXaction().getTransactionAmount().setScale(0, BigDecimal.ROUND_DOWN)) == 1) {
				throw new ServiceException(ErrorCode.INVALID_REFUND_AMOUNT);
			}
			if (StringUtil.isNumeric(additionalAttribute.get("CCA")) && StringUtil.getIntegerValue(additionalAttribute.get("CCA")) != repositoryTicketDTO.getCancellationCharges().intValue()) {
				TRIP_CANCEL_LOGGER.info("Cancellation Charge Mismatched {} {} {} - CCA {} {}", authDTO.getNamespaceCode(), authDTO.getUserCode(), authDTO.getDeviceMedium().getCode(), additionalAttribute.get("CCA"), repositoryTicketDTO.getCancellationCharges());
				// throw new
				// ServiceException(ErrorCode.CANCELLATION_TERMS_NOT_FOUND,
				// "Cancellation Charge Mismatched!");
			}
			// Validate Fresh Transaction
			validateFreshTransaction(repositoryTicketDTO);

			repositoryTicketDTO.setFromStation(stationService.getStation(repositoryTicketDTO.getFromStation()));
			repositoryTicketDTO.setToStation(stationService.getStation(repositoryTicketDTO.getToStation()));

			// MyAccount Cancellation Flag
			if (ticketDTO.isMyAccountFlag()) {
				repositoryTicketDTO.setMyAccountFlag(ticketDTO.isMyAccountFlag());
				repositoryTicketDTO.setTicketUser(authDTO.getUser());
				authDTO.getAdditionalAttribute().put("CAN_MY_ACC", Numeric.ONE);
			}

			BigDecimal totalRefundAmount = BigDecimal.ZERO;
			BigDecimal cancellationChargeAmount = BigDecimal.ZERO;
			// calculate User commissions
			BigDecimal commissionOnTicketFare = BigDecimal.ZERO;
			BigDecimal commissionOnChargeAmount = BigDecimal.ZERO;
			BigDecimal debitCommissionAmount = BigDecimal.ZERO;
			BigDecimal debitExtraCommissionAmount = BigDecimal.ZERO;
			BigDecimal debitAddonsAmount = repositoryTicketDTO.getAddonsValue();
			BigDecimal creditTDSTaxAmount = BigDecimal.ZERO;
			BigDecimal cancelTDSTaxAmount = BigDecimal.ZERO;
			BigDecimal creditAcBusTax = BigDecimal.ZERO;
			BigDecimal bookingSeatCommissionAmount = BigDecimal.ZERO;
			BigDecimal bookingCommissionPercent = BigDecimal.ZERO;
			BigDecimal cancelCommissionServiceTax = BigDecimal.ZERO;
			BigDecimal cancelCommissionServiceTaxAmount = BigDecimal.ZERO;

			// Update Transactions
			for (TicketDetailsDTO ticketDetailsDTO : repositoryTicketDTO.getTicketDetails()) {
				cancellationChargeAmount = cancellationChargeAmount.add(ticketDetailsDTO.getCancellationCharges());
				totalRefundAmount = totalRefundAmount.add(ticketDetailsDTO.getRefundAmount());
				creditAcBusTax = creditAcBusTax.add(ticketDetailsDTO.getAcBusTax());
				ticketDetailsDTO.setTicketStatus(TicketStatusEM.CONFIRM_CANCELLED_TICKETS);
			}

			// Sum of All ticket Addons Details
			for (TicketAddonsDetailsDTO addonsDetailsDTO : repositoryTicketDTO.getTicketAddonsDetails()) {
				addonsDetailsDTO.setTicketStatus(TicketStatusEM.CONFIRM_CANCELLED_TICKETS);
			}
			List<CommissionDTO> commissionList = commissionService.getCommissionV2(authDTO, repositoryTicketDTO.getTicketUser());
			CommissionDTO tdsCommission = commissionService.getUserTaxDetails(authDTO, repositoryTicketDTO.getTicketUser());

			// Revoke Commission based on Namespace Setting
			if (repositoryTicketDTO.getTicketXaction().getTransactionAmount().compareTo(BigDecimal.ZERO) != 0 && repositoryTicketDTO.getTicketXaction().getCommissionAmount().compareTo(BigDecimal.ZERO) != 0 && authDTO.getNamespace().getProfile().isCancellationCommissionRevokeFlag()) {
				bookingCommissionPercent = repositoryTicketDTO.getTicketXaction().getCommissionAmount().multiply(Numeric.ONE_HUNDRED).divide(repositoryTicketDTO.getTicketXaction().getTransactionAmount().subtract(repositoryTicketDTO.getTicketXaction().getAcBusTax()).subtract(repositoryTicketDTO.getTicketXaction().getTdsTax()), 2);

				CommissionDTO serviceTaxDTO = BitsUtil.getCommission(commissionList, CommissionTypeEM.TICKETS_BOOKING);
				BigDecimal bookingCommissionAmount = BigDecimal.ZERO;
				BigDecimal serviceTax = BigDecimal.ZERO;
				if (serviceTaxDTO != null && serviceTaxDTO.getServiceTax().compareTo(BigDecimal.ZERO) != 0) {
					serviceTax = serviceTaxDTO.getServiceTax();
					/** Reference : StringUtil.commissionReverseCalculation */
					BigDecimal serviceTaxAmount = repositoryTicketDTO.getTicketXaction().getCommissionAmount().multiply(serviceTaxDTO.getServiceTax()).divide(Numeric.ONE_HUNDRED.add(serviceTaxDTO.getServiceTax()), 2);
					bookingCommissionAmount = serviceTaxAmount.multiply(Numeric.ONE_HUNDRED).divide(serviceTaxDTO.getServiceTax(), 2);
					bookingCommissionPercent = bookingCommissionAmount.multiply(Numeric.ONE_HUNDRED).divide(repositoryTicketDTO.getTicketXaction().getTransactionAmount().subtract(repositoryTicketDTO.getTicketXaction().getAcBusTax()).subtract(repositoryTicketDTO.getTicketXaction().getTdsTax()), 2, 2);
				}
				else {
					bookingCommissionAmount = repositoryTicketDTO.getTicketXaction().getCommissionAmount();
				}

				bookingSeatCommissionAmount = repositoryTicketDTO.getTotalSeatFare().subtract(debitAddonsAmount).divide(Numeric.ONE_HUNDRED, 2).multiply(bookingCommissionPercent);
				debitCommissionAmount = bookingSeatCommissionAmount.add(bookingSeatCommissionAmount.divide(Numeric.ONE_HUNDRED, 2).multiply(serviceTax));

				if (repositoryTicketDTO.getTicketXaction().getTdsTax().compareTo(BigDecimal.ZERO) != 0) {
					BigDecimal tdsTaxPercent = repositoryTicketDTO.getTicketXaction().getTdsTax().multiply(Numeric.ONE_HUNDRED).divide(bookingCommissionAmount, 2);
					creditTDSTaxAmount = (bookingSeatCommissionAmount.add(debitExtraCommissionAmount)).divide(Numeric.ONE_HUNDRED, 2).multiply(tdsTaxPercent);
				}
			}

			CommissionDTO cancellationChargeCommission = null;
			// Process Cancel Commission on Cancellation Charge
			if (UserRoleEM.USER_ROLE.getId() == repositoryTicketDTO.getTicketUser().getUserRole().getId()) {
				cancellationChargeCommission = BitsUtil.getCommission(commissionList, CommissionTypeEM.TICKETS_CANCEL_COMMISSION_ON_CHARGE);

				if (cancellationChargeCommission != null) {
					commissionOnChargeAmount = cancellationChargeAmount.multiply(cancellationChargeCommission.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2);
					cancelCommissionServiceTax = cancellationChargeCommission.getServiceTax();
				}

				// Process Cancel Commission on Ticket Fare
				CommissionDTO cancelCommissionTicketFareDTO = null;
				if (UserRoleEM.USER_ROLE.getId() == repositoryTicketDTO.getTicketUser().getUserRole().getId() && bookingSeatCommissionAmount.compareTo(BigDecimal.ZERO) != 0) {
					cancelCommissionTicketFareDTO = BitsUtil.getCommission(commissionList, CommissionTypeEM.TICKET_CANCEL_COMMISSION_ON_TICKETFARE);

					if (cancelCommissionTicketFareDTO != null) {
						BigDecimal commissionPercent = bookingCommissionPercent.multiply(cancelCommissionTicketFareDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED);
						commissionOnTicketFare = repositoryTicketDTO.getTotalSeatFare().subtract(debitAddonsAmount).multiply(commissionPercent).divide(Numeric.ONE_HUNDRED);
						cancelCommissionServiceTax = cancelCommissionServiceTax.add(cancelCommissionTicketFareDTO.getServiceTax());
					}
				}

				if (commissionOnChargeAmount.add(commissionOnTicketFare).compareTo(BigDecimal.ZERO) != 0 && tdsCommission.getTdsTaxValue().compareTo(BigDecimal.ZERO) != 0) {
					cancelTDSTaxAmount = commissionOnChargeAmount.add(commissionOnTicketFare).multiply(tdsCommission.getTdsTaxValue()).divide(Numeric.ONE_HUNDRED, 2);
				}
				cancelCommissionServiceTaxAmount = commissionOnTicketFare.add(commissionOnChargeAmount).divide(Numeric.ONE_HUNDRED, 2).multiply(cancelCommissionServiceTax);
			}
			UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
			userTransactionDTO.setRefferenceCode(ticketDTO.getCode());
			userTransactionDTO.setDebitAmount(cancellationChargeAmount.add(debitAddonsAmount).add(cancelTDSTaxAmount));
			userTransactionDTO.setCommissionAmount(debitCommissionAmount.add(debitExtraCommissionAmount).add(commissionOnChargeAmount).add(commissionOnTicketFare).add(cancelCommissionServiceTaxAmount));
			userTransactionDTO.setCreditAmount(repositoryTicketDTO.getTotalFare().add(creditTDSTaxAmount).add(commissionOnChargeAmount).add(commissionOnTicketFare).add(cancelCommissionServiceTaxAmount).subtract(debitCommissionAmount.add(debitExtraCommissionAmount)));

			userTransactionDTO.setTdsTax(cancelTDSTaxAmount);
			userTransactionDTO.setTransactionAmount(userTransactionDTO.getCreditAmount().subtract(userTransactionDTO.getDebitAmount()));
			userTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
			userTransactionDTO.setTransactionMode(repositoryTicketDTO.getTicketXaction().getTransactionMode());
			// repositoryTicketDTO.setTransactionMode(repositoryTicketDTO.getTicketXaction().getTransactionMode());
			repositoryTicketDTO.setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
			repositoryTicketDTO.setUserTransaction(userTransactionDTO);
			// Update ticket transaction status
			if (totalBookedSeatCount == repositoryTicketDTO.getTicketDetails().size()) {
				repositoryTicketDTO.setTicketStatus(TicketStatusEM.CONFIRM_CANCELLED_TICKETS);
			}

			// ticket transaction
			TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
			ticketTransactionDTO.setTransactionAmount(repositoryTicketDTO.getTotalFare().add(commissionOnChargeAmount).add(commissionOnTicketFare).add(cancelCommissionServiceTaxAmount).subtract(cancellationChargeAmount.add(debitCommissionAmount.add(debitExtraCommissionAmount)).subtract(debitAddonsAmount)));
			ticketTransactionDTO.setAcBusTax(creditAcBusTax);
			ticketTransactionDTO.setTdsTax(creditTDSTaxAmount);
			ticketTransactionDTO.setCancelTdsTax(cancelTDSTaxAmount);
			ticketTransactionDTO.setCancellationChargeTax(repositoryTicketDTO.getTotalCancellationChargeTaxAmount());
			ticketTransactionDTO.setExtraCommissionAmount(debitExtraCommissionAmount);
			ticketTransactionDTO.setCommissionAmount(debitCommissionAmount);
			ticketTransactionDTO.setAddonsAmount(debitAddonsAmount);
			ticketTransactionDTO.setCancellationCommissionAmount(commissionOnChargeAmount.add(commissionOnTicketFare).add(cancelCommissionServiceTaxAmount));
			ticketTransactionDTO.setTransactionMode(repositoryTicketDTO.getTicketXaction().getTransactionMode());
			ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
			ticketTransactionDTO.setRefundAmount(totalRefundAmount.add(creditAcBusTax).subtract((debitCommissionAmount.add(debitExtraCommissionAmount).add(debitAddonsAmount))));
			ticketTransactionDTO.setRemarks(StringUtil.substring(ticketDTO.getRemarks(), 120));

			// Refund status of the ticket
			ticketTransactionDTO.setRefundStatus(RefundStatusEM.REFUND_TO_ACCOUNT);
			if (repositoryTicketDTO.getTicketXaction().getTransactionMode().getId() == TransactionModeEM.PAYMENT_PAYMENT_GATEWAY.getId()) {
				ticketTransactionDTO.setRefundStatus(RefundStatusEM.INITIAL);
			}
			repositoryTicketDTO.setTicketXaction(ticketTransactionDTO);

			calculateNetRefundAmount(authDTO, repositoryTicketDTO, repositoryTicketDTO.getTicketXaction(), cancellationChargeCommission);

			// update Ticket, and it's transaction
			SaveConfirmCancelledTicket(authDTO, repositoryTicketDTO);

			// Ticket Notifications
			notificationService.sendTicketCancelNotification(authDTO, repositoryTicketDTO);

			// e mail Notification
			emailService.sendCancelEmailV2(authDTO, repositoryTicketDTO);

			notificationPushService.pushConfirmTicketCancelNotification(authDTO, repositoryTicketDTO);
			// Upload to Backup Server
			drService.flushTicketDetails(authDTO, repositoryTicketDTO);

			tripService.clearBookedBlockedSeatsCache(authDTO, repositoryTicketDTO.getTripDTO());

			// Send Ticket Event
			if (StringUtil.isContains(Constants.TICKET_EVENT, authDTO.getNamespaceCode()) || StringUtil.isContains(Constants.TICKET_EVENT_CANCEL, authDTO.getNamespaceCode())) {
				smsService.sendTicketEventSMS(authDTO, repositoryTicketDTO, "Ticket Cancelled");
			}
			// ticket after trip time cancel notify
			ticketHelperService.processTicketAfterTripTimeCancel(authDTO, repositoryTicketDTO);
			// send Ticket Event To Customer
			notificationService.sendCustomerTicketEvent(authDTO, repositoryTicketDTO, EventNotificationEM.TICKET_CANCEL);
			// do auto refund
			if (repositoryTicketDTO.getTicketUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
				doAutoRefundProcess(authDTO, repositoryTicketDTO);
			}
			// Push Ticket Event to API Callback
			authDTO.getAdditionalAttribute().put("activity_type", "ticket-cancel");
			ticketService.pushInventoryChangesEvent(authDTO, repositoryTicketDTO);

			// Dynamic Fare
			if (BitsUtil.getDynamicPriceProvider(authDTO.getNamespace().getProfile().getDynamicPriceProviders(), DynamicPriceProviderEM.NOT_AVAILABLE) == null) {
				tripService.getTrip(authDTO, repositoryTicketDTO.getTripDTO());
				dynamicFareService.updateTicketStatus(authDTO, repositoryTicketDTO);
			}
		}
		else {
			if (repositoryTicketDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			if (repositoryTicketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
				throw new ServiceException(ErrorCode.SEAT_ALREADY_CANCELLED);
			}
			else if (repositoryTicketDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId()) {
				throw new ServiceException(ErrorCode.NOT_CONFIRM_BOOKED_TICKET);
			}
			else {
				throw new ServiceException(ErrorCode.NOT_CONFIRM_BOOKED_TICKET);
			}
		}
		return repositoryTicketDTO;
	}

	private void calculateNetRefundAmount(AuthDTO authDTO, TicketDTO repositoryTicketDTO, TicketTransactionDTO ticketTransaction, CommissionDTO commissionDTO) {
		int seatCount = repositoryTicketDTO.getTicketDetails().size();
		BigDecimal cancelCommissionAmount = ticketTransaction.getCancellationCommissionAmount().divide(BigDecimal.valueOf(seatCount), 2);

		TripDAO tripDAO = new TripDAO();
		/** Convert Ticket Seat Extras */
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, repositoryTicketDTO);
		Map<String, TicketExtraDTO> repoTicketExtraMap = new HashMap<String, TicketExtraDTO>();
		for (TicketDetailsDTO detailsDTO : seatList) {
			repoTicketExtraMap.put(detailsDTO.getSeatCode(), detailsDTO.getTicketExtra());
		}

		for (TicketDetailsDTO ticketDetailsDTO : repositoryTicketDTO.getTicketDetails()) {
			BigDecimal netRefundAmount = BigDecimal.ZERO;
			if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
				netRefundAmount = ticketDetailsDTO.getCancellationCharges().subtract(cancelCommissionAmount);
			}
			TicketExtraDTO ticketExtraDTO = repoTicketExtraMap.get(ticketDetailsDTO.getSeatCode());
			if (ticketExtraDTO == null) {
				ticketExtraDTO = new TicketExtraDTO();
			}
			ticketExtraDTO.setNetAmount(netRefundAmount.setScale(2, BigDecimal.ROUND_CEILING));
			ticketDetailsDTO.setTicketExtra(ticketExtraDTO);
		}

	}

	private void calculateTAXOnCancellationCharge(AuthDTO authDTO, TicketDTO ticketDTO) {
		// Calculate GST On Cancellation Charge
		if (BitsUtil.isCalculateCancellationChargeTax(authDTO, ticketDTO.getTicketUser()) && authDTO.getNamespace().getProfile().isCancellationChargeTaxFlag() && ticketDTO.getCancellationCharges().compareTo(BigDecimal.ZERO) != 0) {
			NamespaceTaxDTO namespaceTax = taxService.getTaxbyState(authDTO, ticketDTO.getFromStation().getState());
			if (namespaceTax.getServiceTax().compareTo(BigDecimal.ZERO) != 0) {
				for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
					BigDecimal chargeAmountTax = ticketDetailsDTO.getCancellationCharges().multiply(namespaceTax.getServiceTax()).divide(Numeric.ONE_HUNDRED, 2);
					BigDecimal cancellationChargeAmount = ticketDetailsDTO.getCancellationCharges().add(chargeAmountTax);
					ticketDetailsDTO.setCancellationChargeTax(chargeAmountTax);
					ticketDetailsDTO.setCancellationCharges(cancellationChargeAmount);
					ticketDetailsDTO.setRefundAmount(ticketDetailsDTO.getRefundAmount().subtract(chargeAmountTax));
				}
			}
		}
	}

	private void doAutoRefundProcess(AuthDTO authDTO, TicketDTO repositoryTicketDTO) {
		RefundDTO refundDTO = new RefundDTO();
		refundDTO.setAmount(repositoryTicketDTO.getTicketXaction().getRefundAmount());
		refundDTO.setOrderCode(repositoryTicketDTO.getBookingCode());
		refundDTO.setOrderTransactionCode(repositoryTicketDTO.getTicketXaction().getCode());
		refundDTO.setOrderType(OrderTypeEM.TICKET);
		refundService.doRefund(authDTO, refundDTO);
	}

	private void SaveConfirmCancelledTicket(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);

				ticketService.UpdateTicketStatus(connection, authDTO, ticketDTO);

				ticketService.saveTicketTransaction(connection, authDTO, ticketDTO);

				if (ticketDTO.getTicketUser().getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId()) {
					ticketDTO.getUserTransaction().setRefferenceId(ticketDTO.getTicketXaction().getId());
					transactionService.SaveUserTransaction(connection, authDTO, ticketDTO.getTicketUser(), ticketDTO.getUserTransaction());
				}

				ticketService.saveTicketCancelTransaction(connection, authDTO, ticketDTO);

				ticketService.saveTicketCancellationDetails(connection, authDTO, ticketDTO);

				// update Trip seat Block
				tripService.updateTripSeatDetailsWithExtras(connection, authDTO, ticketDTO);

			}
			catch (SQLTransactionRollbackException e) {
				slack.sendAlert(authDTO, ticketDTO.getCode() + " DL08-  Deadlock found when trying to get lock; try restarting transaction");

				e.printStackTrace();
				connection.rollback();
				throw e;
			}
			catch (ServiceException e) {
				connection.rollback();
				throw e;
			}
			catch (Exception e) {
				e.printStackTrace();
				connection.rollback();
				throw e;
			}
			finally {
				connection.commit();
				connection.setAutoCommit(true);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error("Error in saving the cancelled ticket: " + e.getMessage());
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);

		}
	}

	private void calculateCancellationCharges(AuthDTO authDTO, TicketDTO ticketDTO, CancellationTermDTO cancellationTermDTO, Map<String, String> additionalAttribute) {
		try {
			List<CancellationPolicyDTO> policyList = cancellationTermDTO.getPolicyList();
			CancellationPolicyDTO policyDTO = null;
			DateTime dateTime = DateTime.now(TimeZone.getDefault());
			DateTime TravelDateTime = DateUtil.getDateTime(ticketDTO.getTripDTO().getAdditionalAttributes().get(Constants.CANCELLATION_DATETIME));

			int minutis = DateUtil.getMinutiesDifferent(dateTime, TravelDateTime);
			BigDecimal refundAmount = BigDecimal.ZERO;

			// Instant Cancellation Logic
			int instantCancellationMinitues = DateUtil.getMinutiesDifferent(ticketDTO.getTicketAt(), dateTime);
			if (BitsUtil.isAllowInstantCancellation(authDTO) && authDTO.getNamespace().getProfile().getInstantCancellationMinitues() != 0 && instantCancellationMinitues <= authDTO.getNamespace().getProfile().getInstantCancellationMinitues()) {
				for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
					if (TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() == ticketDetailsDTO.getTicketStatus().getId()) {
						ticketDetailsDTO.setRefundAmount(ticketDetailsDTO.getSeatFare());
						ticketDetailsDTO.setCancellationCharges(BigDecimal.ZERO);
						ticketDetailsDTO.setTicketStatus(TicketStatusEM.CANCELLATION_ALLOWED);
					}
				}
				return;
			}
			// Normal Cancellation flow
			if (minutis > 0) {
				for (CancellationPolicyDTO cancellationPolicyDTO : policyList) {
					if (StringUtil.isNull(cancellationPolicyDTO.getPolicyPattern())) {
						continue;
					}
					if (cancellationPolicyDTO.getPolicyPattern().equals("MIN")) {
						// With in Time Range
						if (cancellationPolicyDTO.getFromValue() <= minutis && minutis < cancellationPolicyDTO.getToValue()) {
							policyDTO = cancellationPolicyDTO;
							break;
						}
						// Any Time
						else if (cancellationPolicyDTO.getFromValue() != 0 && cancellationPolicyDTO.getToValue() == 0) {
							policyDTO = cancellationPolicyDTO;
							break;
						}
					}
					else if (cancellationPolicyDTO.getPolicyPattern().equals("AM") || cancellationPolicyDTO.getPolicyPattern().equals("PM")) {
						int fromValueMinutes = cancellationPolicyDTO.getFromValue();
						int toValueMinutes = cancellationPolicyDTO.getToValue();
						if (cancellationPolicyDTO.getPolicyPattern().equals("PM") && fromValueMinutes >= 0 && fromValueMinutes <= 11) {
							fromValueMinutes = fromValueMinutes + 12;
						}
						if (cancellationPolicyDTO.getPolicyPattern().equals("PM") && toValueMinutes >= 0 && toValueMinutes <= 11) {
							toValueMinutes = toValueMinutes + 12;
						}

						if (cancellationPolicyDTO.getFromValue() != 0 && cancellationPolicyDTO.getToValue() == 0) {
							DateTime cutoffTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), fromValueMinutes * 60);
							if (dateTime.lteq(cutoffTime)) {
								policyDTO = cancellationPolicyDTO;
								break;
							}
						}
						else if (cancellationPolicyDTO.getFromValue() == 0 && cancellationPolicyDTO.getToValue() != 0) {
							DateTime cutoffTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), toValueMinutes * 60);
							DateTime travelDateTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes());
							if (dateTime.gteq(cutoffTime) && dateTime.lteq(travelDateTime)) {
								policyDTO = cancellationPolicyDTO;
								break;
							}
						}
						else {
							DateTime cutoffFromTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), toValueMinutes * 60);
							DateTime cutoffToTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), fromValueMinutes * 60);
							if (dateTime.gteq(cutoffFromTime) && dateTime.lteq(cutoffToTime)) {
								policyDTO = cancellationPolicyDTO;
								break;
							}
						}
					}
				}
			}
			if (policyDTO == null && !ticketDTO.isOverideFlag()) {
				throw new ServiceException(ErrorCode.CANCELLATION_TERMS_NOT_FOUND);
			}
			if (policyDTO != null) {
				String policyCode = StringUtil.generateCancellationPolicyCode(cancellationTermDTO, policyDTO);
				if (StringUtil.isNotNull(additionalAttribute.get("CTPC")) && !policyCode.equals(additionalAttribute.get("CTPC"))) {
					TRIP_CANCEL_LOGGER.info("Cancellation Term Policy is Mismatched {} {} {} - CTPC {} {}", authDTO.getNamespaceCode(), authDTO.getUserCode(), authDTO.getDeviceMedium().getCode(), additionalAttribute.get("CTPC"), policyCode);
					// throw new
					// ServiceException(ErrorCode.CANCELLATION_TERMS_NOT_FOUND,
					// "Cancellation Term Policy is Mismatched!");
				}

				for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
					if ((policyDTO.getPolicyPattern().equals("MIN") || policyDTO.getPolicyPattern().equals("AM") || policyDTO.getPolicyPattern().equals("PM")) && policyDTO.getDeductionValue().compareTo(BigDecimal.ONE.negate()) != 0) {
						if (TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() == ticketDetailsDTO.getTicketStatus().getId()) {
							if (policyDTO.getPercentageFlag() == 1) {
								BigDecimal deductionAmount = ticketDetailsDTO.getSeatFare().multiply(policyDTO.getDeductionValue().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING));
								refundAmount = ticketDetailsDTO.getSeatFare().subtract(deductionAmount);
								ticketDetailsDTO.setRefundAmount(refundAmount.intValue() > 0 ? refundAmount : BigDecimal.ZERO);
								ticketDetailsDTO.setCancellationCharges(deductionAmount);
							}
							else if (policyDTO.getPercentageFlag() == 0) {
								BigDecimal deductionAmount = policyDTO.getDeductionValue();
								ticketDetailsDTO.setCancellationCharges(deductionAmount);
								refundAmount = ticketDetailsDTO.getSeatFare().subtract(deductionAmount);
								ticketDetailsDTO.setRefundAmount(refundAmount.intValue() > 0 ? refundAmount : BigDecimal.ZERO);
							}
							ticketDetailsDTO.setTicketStatus(TicketStatusEM.CANCELLATION_ALLOWED);
						}
						else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
							if (authDTO.getNamespace().getProfile().getPhoneBookingCancellationBlockMinutes() <= minutis) {
								ticketDetailsDTO.setTicketStatus(TicketStatusEM.CANCELLATION_ALLOWED);
							}
							else {
								ticketDetailsDTO.setTicketStatus(TicketStatusEM.CANCELLATION_ALLOWED);
							}
						}
					}
					else if (policyDTO.getDeductionValue().compareTo(BigDecimal.ONE.negate()) == 0) {
						ticketDTO.setTicketStatus(TicketStatusEM.CANCELLATION_NOT_ALLOWED);
						ticketDetailsDTO.setTicketStatus(TicketStatusEM.CANCELLATION_NOT_ALLOWED);
					}
					else {
						ticketDTO.setTicketStatus(TicketStatusEM.CANCELLATION_NOT_ALLOWED);
						ticketDetailsDTO.setTicketStatus(TicketStatusEM.CANCELLATION_NOT_ALLOWED);
					}
					// Free Service Cancellation,
					if (ticketDetailsDTO.getRefundAmount().compareTo(BigDecimal.ZERO) <= 0) {
						ticketDetailsDTO.setRefundAmount(BigDecimal.ZERO);
					}
				}
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
	}

	private void applyPhoneBookCancelControl(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			TicketPhoneBookCancelControlDTO phoneBookCancel = null;
			DateTime dateTime = DateTime.now(TimeZone.getDefault());
			DateTime tripDateTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes());
			int tripMinutes = DateUtil.getMinutiesDifferent(dateTime, tripDateTime);
			DateTime stageDateTime = ticketDTO.getBoardingPointDateTime();
			int stageMinutes = DateUtil.getMinutiesDifferent(dateTime, stageDateTime);

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(ticketDTO.getTripDTO().getCode());
			ScheduleDTO scheduleDTO = tripService.getTrip(authDTO, tripDTO).getSchedule();
			TicketPhoneBookCancelControlDTO cancellationPolicyDTO = phoneBookCancelControlService.getActivePhoneBookCancelControl(authDTO, scheduleDTO, ticketDTO);

			// Normal Cancellation flow
			int minute = 0;
			if (cancellationPolicyDTO != null) {
				if (cancellationPolicyDTO.getTripStageFlag() == Numeric.ONE_INT) {
					minute = tripMinutes;
				}
				else if (cancellationPolicyDTO.getTripStageFlag() == Numeric.ZERO_INT) {
					minute = stageMinutes;
				}
				if (cancellationPolicyDTO.getPolicyMinute() == -1) {
					List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, tripDTO);
					DateTime destinationStationDateTime = BitsUtil.getDestinationStationTime(stageList, tripDTO.getTripDate());
					int tripEndMinuties = DateUtil.getMinutiesDifferent(destinationStationDateTime, dateTime);
					if (cancellationPolicyDTO.getPolicyMinute() < tripEndMinuties) {
						phoneBookCancel = cancellationPolicyDTO;
					}
				}
				else if (cancellationPolicyDTO.getPolicyPattern().equals("MIN")) {
					// With in Time Range
					if (minute < cancellationPolicyDTO.getPolicyMinute()) {
						phoneBookCancel = cancellationPolicyDTO;
					}
				}
				else if (cancellationPolicyDTO.getPolicyPattern().equals("AM") || cancellationPolicyDTO.getPolicyPattern().equals("PM")) {
					int valueMinutes = cancellationPolicyDTO.getPolicyMinute();
					if (cancellationPolicyDTO.getPolicyPattern().equals("PM") && valueMinutes >= 0 && valueMinutes <= 11) {
						valueMinutes = valueMinutes + 12;
					}

					if (cancellationPolicyDTO.getPolicyMinute() != 0 && cancellationPolicyDTO.getTripStageFlag() == Numeric.ONE_INT) {
						DateTime cutoffTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), valueMinutes * 60);
						DateTime travelDateTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes());
						if (dateTime.gteq(cutoffTime) && dateTime.lteq(travelDateTime)) {
							phoneBookCancel = cancellationPolicyDTO;
						}
					}
					else if (cancellationPolicyDTO.getPolicyMinute() != 0 && cancellationPolicyDTO.getTripStageFlag() != Numeric.ONE_INT) {
						DateTime cutoffTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), valueMinutes * 60);
						DateTime boardingDateTime = ticketDTO.getBoardingPointDateTime();
						if (dateTime.gteq(cutoffTime) && dateTime.lteq(boardingDateTime)) {
							phoneBookCancel = cancellationPolicyDTO;
						}
					}
					else if (cancellationPolicyDTO.getPolicyMinute() == 0 && cancellationPolicyDTO.getTripStageFlag() == Numeric.ONE_INT) {
						DateTime cutoffTripTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes() + valueMinutes * 60);
						if (dateTime.gt(cutoffTripTime)) {
							phoneBookCancel = cancellationPolicyDTO;
						}
					}
					else if (cancellationPolicyDTO.getPolicyMinute() == 0 && cancellationPolicyDTO.getTripStageFlag() != Numeric.ONE_INT) {
						DateTime cutoffStageTime = DateUtil.addMinituesToDate(ticketDTO.getBoardingPointDateTime(), valueMinutes * 60);
						if (dateTime.gt(cutoffStageTime)) {
							phoneBookCancel = cancellationPolicyDTO;
						}
					}
				}
				if (phoneBookCancel != null) {
					throw new ServiceException(ErrorCode.CANCELLATION_TIME_OVER);
				}
			}

			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					ticketDetailsDTO.setTicketStatus(TicketStatusEM.CANCELLATION_ALLOWED);
				}
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

	}

	public DateTime getTripOriginStationDatetime(AuthDTO authDTO, TripDTO tripDTO) {
		TripDTO repoTripDTO = new TripDTO();
		repoTripDTO.setCode(tripDTO.getCode());
		repoTripDTO = tripService.getTrip(authDTO, repoTripDTO);
		DateTime travelDateTime = DateUtil.addMinituesToDate(repoTripDTO.getTripDate(), repoTripDTO.getTripMinutes());

		return travelDateTime;
	}

	public CancellationTermDTO getCancellationPolicyConvention(AuthDTO authDTO, UserDTO user, CancellationTermDTO cancellationTerm, StateDTO stateDTO, DateTime travelDateTime, List<BigDecimal> seatFareList) {
		List<CancellationPolicyDTO> cancellationTermsList = new ArrayList<CancellationPolicyDTO>();
		try {
			BigDecimal chargeServiceTax = BigDecimal.ZERO;
			BigDecimal chargeServiceTaxPercent = BigDecimal.ZERO;
			if (BitsUtil.isCalculateCancellationChargeTax(authDTO, user) && authDTO.getNamespace().getProfile().isCancellationChargeTaxFlag() && stateDTO != null) {
				NamespaceTaxDTO namespaceTax = taxService.getTaxbyState(authDTO, stateDTO);
				chargeServiceTaxPercent = namespaceTax.getServiceTax();
				chargeServiceTax = chargeServiceTaxPercent.divide(Numeric.ONE_HUNDRED, 2);
			}

			CancellationPolicyDTO defaultPolicyDTO = new CancellationPolicyDTO();
			defaultPolicyDTO.setTerm("After " + travelDateTime.format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
			defaultPolicyDTO.setDeductionAmountTxt("No Cancellation");
			defaultPolicyDTO.setRefundAmountTxt(" - ");
			defaultPolicyDTO.setChargesTxt("-");
			cancellationTermsList.add(defaultPolicyDTO);
			if (cancellationTerm.getPolicyList() != null && !cancellationTerm.getPolicyList().isEmpty()) {
				String fromPolicyPattern = "MIN";
				for (CancellationPolicyDTO policyDTO : cancellationTerm.getPolicyList()) {
					if (fromPolicyPattern.equals("MIN") && "MIN".equals(policyDTO.getPolicyPattern())) {
						if (policyDTO.getFromValue() != 0 && policyDTO.getToValue() == 0) {
							policyDTO.setTerm("Till " + DateUtil.minusMinituesToDate(travelDateTime, policyDTO.getFromValue()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
						}
						else if (policyDTO.getFromValue() == 0 && policyDTO.getToValue() != 0) {
							policyDTO.setTerm("From " + DateUtil.minusMinituesToDate(travelDateTime, policyDTO.getToValue()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")) + " to " + DateUtil.minusMinituesToDate(travelDateTime, policyDTO.getFromValue()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
						}
						else {
							policyDTO.setTerm("From " + DateUtil.minusMinituesToDate(travelDateTime, policyDTO.getToValue()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")) + " to " + DateUtil.minusMinituesToDate(travelDateTime, policyDTO.getFromValue()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
						}
						StringBuilder deductionAmountTxt = null;
						StringBuilder refundAmountTxt = null;
						boolean isNoCancellationTextExist = false;
						for (BigDecimal fare : seatFareList) {
							if (deductionAmountTxt == null) {
								deductionAmountTxt = new StringBuilder();
								refundAmountTxt = new StringBuilder();
							}
							else {
								deductionAmountTxt.append(!isNoCancellationTextExist ? "," : Text.EMPTY);
								refundAmountTxt.append(",");
							}

							if (policyDTO.getPercentageFlag() == 1 && policyDTO.getDeductionValue().toBigInteger().intValue() != -1) {
								BigDecimal deductionAmount = fare.multiply(policyDTO.getDeductionValue()).divide(Numeric.ONE_HUNDRED, 2);
								deductionAmount = deductionAmount.add(deductionAmount.multiply(chargeServiceTax)).setScale(2, RoundingMode.HALF_UP);

								deductionAmountTxt.append("Rs." + deductionAmount);
								refundAmountTxt.append("Rs." + (fare.subtract(deductionAmount).setScale(2, RoundingMode.HALF_UP)));
								policyDTO.setChargesTxt(policyDTO.getDeductionValue().toBigInteger() + " %");
								if (chargeServiceTaxPercent.compareTo(BigDecimal.ZERO) == 1) {
									policyDTO.setChargesTxt(policyDTO.getDeductionValue().toBigInteger() + " % + " + chargeServiceTaxPercent.toBigInteger() + " % Tax");
								}
							}
							else if (policyDTO.getPercentageFlag() == 0 && policyDTO.getDeductionValue().toBigInteger().intValue() != -1) {
								BigDecimal deductionAmount = policyDTO.getDeductionValue();
								deductionAmount = deductionAmount.add(deductionAmount.multiply(chargeServiceTax)).setScale(2, RoundingMode.HALF_UP);

								deductionAmountTxt.append("Rs. " + deductionAmount);
								refundAmountTxt.append("Rs. " + fare.subtract(deductionAmount).setScale(2, RoundingMode.HALF_UP));
								policyDTO.setChargesTxt("Rs. " + policyDTO.getDeductionValue().toBigInteger());
								if (chargeServiceTaxPercent.compareTo(BigDecimal.ZERO) == 1) {
									policyDTO.setChargesTxt(policyDTO.getDeductionValue().toBigInteger() + " % + " + chargeServiceTaxPercent.toBigInteger() + " % Tax");
								}
							}
							else if (policyDTO.getDeductionValue().toBigInteger().intValue() == -1) {
								refundAmountTxt.append(" - ");
								deductionAmountTxt.append(!isNoCancellationTextExist ? "No Cancellation" : Text.EMPTY);
								policyDTO.setChargesTxt(" - ");
								isNoCancellationTextExist = true;
							}
						}
						policyDTO.setRefundAmountTxt(refundAmountTxt.toString());
						policyDTO.setDeductionAmountTxt(deductionAmountTxt.toString());
						fromPolicyPattern = policyDTO.getPolicyPattern();
					}
					else if ((fromPolicyPattern.equals("AM") || fromPolicyPattern.equals("PM")) && "MIN".equals(policyDTO.getPolicyPattern())) {
						if (policyDTO.getFromValue() != 0 && policyDTO.getToValue() == 0) {
							policyDTO.setTerm("Till " + DateUtil.minusMinituesToDate(travelDateTime, policyDTO.getFromValue()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
						}
						else if (policyDTO.getFromValue() == 0 && policyDTO.getToValue() != 0) {
							policyDTO.setTerm("From " + DateUtil.minusMinituesToDate(travelDateTime, policyDTO.getToValue()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")) + " to " + DateUtil.addMinituesToDate(DateUtil.getDateFromDateTime(travelDateTime), policyDTO.getFromValue()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
						}
						else {
							policyDTO.setTerm("From " + DateUtil.minusMinituesToDate(travelDateTime, policyDTO.getToValue()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")) + " to " + DateUtil.addMinituesToDate(DateUtil.getDateFromDateTime(travelDateTime), policyDTO.getFromValue()).format("DD/MM/YYYY hh12:mm a", Locale.forLanguageTag("en_IN")));
						}
						StringBuilder deductionAmountTxt = null;
						StringBuilder refundAmountTxt = null;
						boolean isNoCancellationTextExist = false;
						for (BigDecimal fare : seatFareList) {
							if (deductionAmountTxt == null) {
								deductionAmountTxt = new StringBuilder();
								refundAmountTxt = new StringBuilder();
							}
							else {
								deductionAmountTxt.append(!isNoCancellationTextExist ? "," : Text.EMPTY);
								refundAmountTxt.append(",");
							}

							if (policyDTO.getPercentageFlag() == 1 && policyDTO.getDeductionValue().toBigInteger().intValue() != -1) {
								BigDecimal deductionAmount = fare.multiply(policyDTO.getDeductionValue()).divide(Numeric.ONE_HUNDRED, 2);
								deductionAmount = deductionAmount.add(deductionAmount.multiply(chargeServiceTax)).setScale(2, RoundingMode.HALF_UP);

								deductionAmountTxt.append("Rs." + deductionAmount);
								refundAmountTxt.append("Rs." + (fare.subtract(deductionAmount).setScale(2, RoundingMode.HALF_UP)));
								policyDTO.setChargesTxt(policyDTO.getDeductionValue().toBigInteger() + " %");
								if (chargeServiceTaxPercent.compareTo(BigDecimal.ZERO) == 1) {
									policyDTO.setChargesTxt(policyDTO.getDeductionValue().toBigInteger() + " % + " + chargeServiceTaxPercent.toBigInteger() + " % Tax");
								}
							}
							else if (policyDTO.getPercentageFlag() == 0 && policyDTO.getDeductionValue().toBigInteger().intValue() != -1) {
								BigDecimal deductionAmount = policyDTO.getDeductionValue();
								deductionAmount = deductionAmount.add(deductionAmount.multiply(chargeServiceTax)).setScale(2, RoundingMode.HALF_UP);

								deductionAmountTxt.append("Rs. " + deductionAmount);
								refundAmountTxt.append("Rs. " + fare.subtract(deductionAmount).setScale(2, RoundingMode.HALF_UP));
								policyDTO.setChargesTxt("Rs. " + policyDTO.getDeductionValue().toBigInteger());
								if (chargeServiceTaxPercent.compareTo(BigDecimal.ZERO) == 1) {
									policyDTO.setChargesTxt(policyDTO.getDeductionValue().toBigInteger() + " % + " + chargeServiceTaxPercent.toBigInteger() + " % Tax");
								}
							}
							else if (policyDTO.getDeductionValue().toBigInteger().intValue() == -1) {
								refundAmountTxt.append(" - ");
								deductionAmountTxt.append(!isNoCancellationTextExist ? "No Cancellation" : Text.EMPTY);
								policyDTO.setChargesTxt(" - ");
								isNoCancellationTextExist = true;
							}
						}
						policyDTO.setRefundAmountTxt(refundAmountTxt.toString());
						policyDTO.setDeductionAmountTxt(deductionAmountTxt.toString());
						fromPolicyPattern = policyDTO.getPolicyPattern();
					}
					else if ("AM".equals(policyDTO.getPolicyPattern()) || "PM".equals(policyDTO.getPolicyPattern())) {
						if (policyDTO.getFromValue() != 0 && policyDTO.getToValue() == 0) {
							policyDTO.setTerm("Till " + travelDateTime.format("DD/MM/YYYY", Locale.forLanguageTag("en_IN")) + " " + policyDTO.getFromValue() + ":00 " + policyDTO.getPolicyPattern());
						}
						else if (policyDTO.getFromValue() == 0 && policyDTO.getToValue() != 0) {
							policyDTO.setTerm("From " + policyDTO.getToValue() + ":00 " + policyDTO.getPolicyPattern() + " to " + travelDateTime.format("hh12:mm a", Locale.forLanguageTag("en_IN")));
						}
						else {
							policyDTO.setTerm("From " + policyDTO.getToValue() + ":00 " + policyDTO.getPolicyPattern() + " to " + policyDTO.getFromValue() + ":00 " + policyDTO.getPolicyPattern());
						}
						StringBuilder deductionAmountTxt = null;
						StringBuilder refundAmountTxt = null;
						boolean isNoCancellationTextExist = false;
						for (BigDecimal fare : seatFareList) {
							if (deductionAmountTxt == null) {
								deductionAmountTxt = new StringBuilder();
								refundAmountTxt = new StringBuilder();
							}
							else {
								deductionAmountTxt.append(!isNoCancellationTextExist ? "," : Text.EMPTY);
								refundAmountTxt.append(",");
							}

							if (policyDTO.getPercentageFlag() == 1 && policyDTO.getDeductionValue().toBigInteger().intValue() != -1) {
								BigDecimal deductionAmount = fare.multiply(policyDTO.getDeductionValue()).divide(Numeric.ONE_HUNDRED, 2);
								deductionAmount = deductionAmount.add(deductionAmount.multiply(chargeServiceTax)).setScale(2, RoundingMode.HALF_UP);

								deductionAmountTxt.append("Rs. " + deductionAmount);
								refundAmountTxt.append("Rs. " + (fare.subtract(deductionAmount).setScale(2, RoundingMode.HALF_UP)));
								policyDTO.setChargesTxt(policyDTO.getDeductionValue().setScale(2, RoundingMode.HALF_UP) + " %");
								if (chargeServiceTaxPercent.compareTo(BigDecimal.ZERO) == 1) {
									policyDTO.setChargesTxt(policyDTO.getDeductionValue().toBigInteger() + " % + " + chargeServiceTaxPercent.toBigInteger() + " % Tax");
								}
							}
							else if (policyDTO.getPercentageFlag() == 0 && policyDTO.getDeductionValue().toBigInteger().intValue() != -1) {
								BigDecimal deductionAmount = policyDTO.getDeductionValue();
								deductionAmount = deductionAmount.add(deductionAmount.multiply(chargeServiceTax)).setScale(2, RoundingMode.HALF_UP);

								deductionAmountTxt.append("Rs. " + deductionAmount);
								refundAmountTxt.append("Rs. " + fare.subtract(deductionAmount).setScale(2, RoundingMode.HALF_UP));
								policyDTO.setChargesTxt("Rs. " + policyDTO.getDeductionValue().toBigInteger());
								if (chargeServiceTaxPercent.compareTo(BigDecimal.ZERO) == 1) {
									policyDTO.setChargesTxt(policyDTO.getDeductionValue().toBigInteger() + " % + " + chargeServiceTaxPercent.toBigInteger() + " % Tax");
								}
							}
							else if (policyDTO.getDeductionValue().toBigInteger().intValue() == -1) {
								refundAmountTxt.append(" - ");
								deductionAmountTxt.append(!isNoCancellationTextExist ? "No Cancellation" : Text.EMPTY);
								policyDTO.setChargesTxt(" - ");
								isNoCancellationTextExist = true;
							}
						}
						policyDTO.setRefundAmountTxt(refundAmountTxt.toString());
						policyDTO.setDeductionAmountTxt(deductionAmountTxt.toString());
						fromPolicyPattern = policyDTO.getPolicyPattern();
					}
					else {
						continue;
					}
					cancellationTermsList.add(policyDTO);
				}
			}
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}

		cancellationTerm.setPolicyList(cancellationTermsList);
		return cancellationTerm;
	}

	@Override
	public void updateTripCancelAcknowledge(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDTO repositoryTicketDTO = new TicketDTO();
		repositoryTicketDTO.setCode(ticketDTO.getCode());
		TripDTO tripDTO = ticketDTO.getTripDTO();

		// get Ticket Details
		ticketService.getTicketStatus(authDTO, repositoryTicketDTO);

		repositoryTicketDTO.setFromStation(stationService.getStation(repositoryTicketDTO.getFromStation()));
		repositoryTicketDTO.setToStation(stationService.getStation(repositoryTicketDTO.getToStation()));

		if (tripDTO.getTripStatus() != null && tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_CANCELLED.getId()) {
			TRIP_CANCEL_LOGGER.info("Trip cancel updating for ticketCode: " + repositoryTicketDTO.getCode());
			// get Ticket Transaction
			TicketTransactionDTO transactionDTO = new TicketTransactionDTO();
			transactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
			repositoryTicketDTO.setTicketXaction(transactionDTO);
			ticketService.getTicketTransaction(authDTO, repositoryTicketDTO);

			// get Trip Details
			tripService.getTripDTO(authDTO, tripDTO);

			if (repositoryTicketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {

				// Remove Un selected Seats
				for (Iterator<TicketDetailsDTO> iterator = repositoryTicketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
					TicketDetailsDTO DBDetailsDTO = iterator.next();
					if (!ticketDTO.getSeatCodeList().contains(DBDetailsDTO.getSeatCode()) || DBDetailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
						iterator.remove();
						continue;
					}
				}

				if (repositoryTicketDTO.getTicketDetails().size() != ticketDTO.getTicketDetails().size() && repositoryTicketDTO.getTicketDetails().isEmpty()) {
					throw new ServiceException(ErrorCode.SEAT_ALREADY_CANCELLED);
				}

				repositoryTicketDTO.setTicketStatus(TicketStatusEM.TRIP_CANCELLED);
				repositoryTicketDTO.setTripDTO(tripDTO);

				BigDecimal totalRefundAmount = BigDecimal.ZERO;
				BigDecimal creditAcBusTax = BigDecimal.ZERO;
				// calculate User commissions
				BigDecimal debitCommissionAmount = BigDecimal.ZERO;
				BigDecimal debitExtraCommissionAmount = BigDecimal.ZERO;
				BigDecimal debitAddonsAmount = repositoryTicketDTO.getAddonsValue();
				BigDecimal creditTDSTaxAmount = BigDecimal.ZERO;
				BigDecimal bookingSeatCommissionAmount = BigDecimal.ZERO;
				BigDecimal bookingCommissionPercent = BigDecimal.ZERO;

				// calculate Trip cancellation charge
				for (TicketDetailsDTO ticketDetailsDTO : repositoryTicketDTO.getTicketDetails()) {
					ticketDetailsDTO.setTicketStatus(TicketStatusEM.TRIP_CANCELLED);
					ticketDetailsDTO.setRefundAmount(ticketDetailsDTO.getSeatFare());
					ticketDetailsDTO.setCancellationCharges(BigDecimal.ZERO);
					totalRefundAmount = totalRefundAmount.add(ticketDetailsDTO.getRefundAmount());
					creditAcBusTax = creditAcBusTax.add(ticketDetailsDTO.getAcBusTax());
				}

				// Revoke Commission based on Namespace Setting
				if (repositoryTicketDTO.getTicketXaction().getTransactionAmount().compareTo(BigDecimal.ZERO) == 1 && repositoryTicketDTO.getTicketXaction().getCommissionAmount().compareTo(BigDecimal.ZERO) != 0 && authDTO.getNamespace().getProfile().isCancellationCommissionRevokeFlag()) {
					bookingCommissionPercent = repositoryTicketDTO.getTicketXaction().getCommissionAmount().add(repositoryTicketDTO.getTicketXaction().getExtraCommissionAmount()).multiply(new BigDecimal(100)).divide(repositoryTicketDTO.getTicketXaction().getTransactionAmount().subtract(repositoryTicketDTO.getTicketXaction().getAcBusTax()), 2, RoundingMode.HALF_EVEN);

					CommissionDTO serviceTaxDTO = commissionService.getCommission(authDTO, repositoryTicketDTO.getTicketUser(), CommissionTypeEM.TICKETS_BOOKING);
					BigDecimal bookingCommissionAmount = BigDecimal.ZERO;
					if (serviceTaxDTO != null && serviceTaxDTO.getServiceTax().compareTo(BigDecimal.ZERO) != 0) {
						// Reference : StringUtil.commissionReverseCalculation
						BigDecimal serviceTaxAmount = repositoryTicketDTO.getTicketXaction().getCommissionAmount().multiply(serviceTaxDTO.getServiceTax()).divide(Numeric.ONE_HUNDRED.add(serviceTaxDTO.getServiceTax()), 2);
						bookingCommissionAmount = serviceTaxAmount.multiply(Numeric.ONE_HUNDRED).divide(serviceTaxDTO.getServiceTax(), 2);
						bookingCommissionPercent = bookingCommissionAmount.multiply(Numeric.ONE_HUNDRED).divide(repositoryTicketDTO.getTicketXaction().getTransactionAmount().subtract(repositoryTicketDTO.getTicketXaction().getAcBusTax()).subtract(repositoryTicketDTO.getTicketXaction().getTdsTax()), 2, 2);
					}
					else {
						bookingCommissionAmount = repositoryTicketDTO.getTicketXaction().getCommissionAmount();
					}

					bookingSeatCommissionAmount = repositoryTicketDTO.getTotalSeatFare().subtract(debitAddonsAmount).divide(Numeric.ONE_HUNDRED, 2).multiply(bookingCommissionPercent);
					debitCommissionAmount = bookingSeatCommissionAmount.add(bookingSeatCommissionAmount.divide(Numeric.ONE_HUNDRED, 2).multiply(serviceTaxDTO.getServiceTax()));

					if (repositoryTicketDTO.getTicketXaction().getTdsTax().compareTo(BigDecimal.ZERO) != 0) {
						BigDecimal tdsTaxPercent = repositoryTicketDTO.getTicketXaction().getTdsTax().multiply(Numeric.ONE_HUNDRED).divide(bookingCommissionAmount, 2);
						creditTDSTaxAmount = (bookingSeatCommissionAmount.add(debitExtraCommissionAmount)).divide(Numeric.ONE_HUNDRED, 2).multiply(tdsTaxPercent);
					}
				}

				UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
				userTransactionDTO.setRefferenceCode(ticketDTO.getCode());
				userTransactionDTO.setDebitAmount(debitAddonsAmount);
				userTransactionDTO.setCommissionAmount(debitCommissionAmount.add(debitExtraCommissionAmount));
				userTransactionDTO.setCreditAmount(repositoryTicketDTO.getTotalFare().subtract(debitCommissionAmount.add(debitExtraCommissionAmount)));

				userTransactionDTO.setTdsTax(creditTDSTaxAmount);
				userTransactionDTO.setTransactionAmount(userTransactionDTO.getCreditAmount().subtract(userTransactionDTO.getDebitAmount()));
				userTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
				userTransactionDTO.setTransactionMode(repositoryTicketDTO.getTicketXaction().getTransactionMode());

				repositoryTicketDTO.setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
				repositoryTicketDTO.setUserTransaction(userTransactionDTO);

				CommissionDTO commissionDTO = commissionService.getCommission(authDTO, repositoryTicketDTO.getTicketUser(), CommissionTypeEM.TICKETS_CANCEL_COMMISSION_ON_CHARGE);
				ticketDTO.setId(repositoryTicketDTO.getId());
				TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
				ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
				ticketDTO.setTicketXaction(ticketTransactionDTO);
				ticketService.getTicketTransaction(authDTO, ticketDTO);
				ticketService.getTicketCancelTransactionDetails(authDTO, ticketTransactionDTO, ticketTransactionDTO.getUserDTO());

				calculateNetRefundAmount(authDTO, repositoryTicketDTO, ticketDTO.getTicketXaction(), commissionDTO);

				// update Ticket, and it's transaction
				updateTripCancelAcknowledgeStatus(authDTO, repositoryTicketDTO);

				// do auto refund
				TRIP_CANCEL_LOGGER.info("Trip cancel validating ticket user role for ticketCode: {} - {}", repositoryTicketDTO.getCode(), repositoryTicketDTO.getTicketUser().getUserRole().getCode());
				if (repositoryTicketDTO.getTicketUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
					BigDecimal refundAmount = totalRefundAmount.add(creditAcBusTax).subtract((debitCommissionAmount.add(debitExtraCommissionAmount).add(debitAddonsAmount)));
					repositoryTicketDTO.getTicketXaction().setRefundAmount(refundAmount.setScale(0, RoundingMode.DOWN));
					TRIP_CANCEL_LOGGER.info("Trip cancel auto refund request for ticketCode: {} refundAmount: {}", repositoryTicketDTO.getCode(), refundAmount);
					doAutoRefundProcess(authDTO, repositoryTicketDTO);
				}
				tripService.clearBookedBlockedSeatsCache(authDTO, repositoryTicketDTO.getTripDTO());
				// Update API Ticket
				if (authDTO.getNamespace().getProfile().isAliasNamespaceFlag()) {
					List<NamespaceDTO> aliasNamespaceList = getAliasNamespaceList(authDTO, repositoryTicketDTO.getTicketUser());
					for (NamespaceDTO namespace : aliasNamespaceList) {
						cancelTicketHelperService.notifyTripCancel(authDTO, namespace, repositoryTicketDTO);
					}
				}
				else {
					cancelTicketHelperService.notifyTripCancel(authDTO, null, repositoryTicketDTO);
				}
			}
			else if (repositoryTicketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				cancelPhoneBooking(authDTO, ticketDTO);
				tripService.clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
			}
			else {
				if (repositoryTicketDTO.getId() == 0) {
					throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
				}
				if (repositoryTicketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
					throw new ServiceException(ErrorCode.SEAT_ALREADY_CANCELLED);
				}
				else if (repositoryTicketDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId()) {
					throw new ServiceException(ErrorCode.NOT_CONFIRM_BOOKED_TICKET);
				}
				else {
					throw new ServiceException(ErrorCode.NOT_CONFIRM_BOOKED_TICKET);
				}
			}
			emailService.sendTripCancellEmail(authDTO, repositoryTicketDTO);
			// Dynamic Pricing push
			if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
				dpService.updateTicketStatus(authDTO, repositoryTicketDTO);
			}
		}
		else if (tripDTO.getTripStatus() != null && tripDTO.getTripStatus().getId() == TripStatusEM.TRIP_NA.getId()) {
			TRIP_CANCEL_LOGGER.info("Trip cancel rejecting for ticketCode: " + repositoryTicketDTO.getCode());

			updateTripCancelReject(authDTO, repositoryTicketDTO);
		}
	}

	@Override
	public TicketDTO tripCancelInitiate(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, String> dataMap) {
		TicketDTO repositoryTicketDTO = new TicketDTO();
		repositoryTicketDTO.setCode(ticketDTO.getCode());
		TripDTO tripDTO = ticketDTO.getTripDTO();

		// get Trip Details
		tripService.getTripDTO(authDTO, tripDTO);

		// if (tripDTO.getTripStatus().getId() !=
		// TripStatusEM.TRIP_CANCELLED.getId()) {
		// throw new ServiceException(ErrorCode.TRIP_INVALID);
		// }

		// get Ticket Details
		ticketService.getTicketStatus(authDTO, repositoryTicketDTO);
		int totalBookedSeatCount = repositoryTicketDTO.getBookedTicketDetails(TicketStatusEM.CONFIRM_BOOKED_TICKETS).size();

		// Trip Validation
		if (!repositoryTicketDTO.getTripDTO().getCode().equals(tripDTO.getCode())) {
			throw new ServiceException(ErrorCode.TRIP_INVALID);
		}

		if (repositoryTicketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
			// Check Duplicate Trip Cancel Initiate
			boolean isTripCancelInitiated = ticketService.isTripCancelInitiated(authDTO, repositoryTicketDTO);
			if (isTripCancelInitiated) {
				throw new ServiceException(ErrorCode.TRIP_CANCELLED);
			}
			// Remove Un selected Seats
			for (Iterator<TicketDetailsDTO> iterator = repositoryTicketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
				TicketDetailsDTO DBDetailsDTO = iterator.next();
				if (!ticketDTO.getSeatCodeList().contains(DBDetailsDTO.getSeatCode()) || DBDetailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
					iterator.remove();
					continue;
				}
			}

			if (repositoryTicketDTO.getTicketDetails().isEmpty()) {
				throw new ServiceException(ErrorCode.SEAT_ALREADY_CANCELLED);
			}

			// Remove respective Ticket Addons Details
			for (Iterator<TicketAddonsDetailsDTO> iterator = repositoryTicketDTO.getTicketAddonsDetails().iterator(); iterator.hasNext();) {
				TicketAddonsDetailsDTO addonsDetailsDTO = iterator.next();
				if (addonsDetailsDTO.getTicketDetailsId(repositoryTicketDTO.getTicketDetails()) == 0) {
					iterator.remove();
					continue;
				}
			}

			repositoryTicketDTO.setTicketStatus(TicketStatusEM.TRIP_CANCEL_INITIATED);
			repositoryTicketDTO.setTripDTO(tripDTO);

			// calculate Trip cancellation charge
			for (TicketDetailsDTO ticketDetailsDTO : repositoryTicketDTO.getTicketDetails()) {
				ticketDetailsDTO.setTicketStatus(TicketStatusEM.TRIP_CANCEL_INITIATED);
				ticketDetailsDTO.setRefundAmount(ticketDetailsDTO.getSeatFare());
				ticketDetailsDTO.setCancellationCharges(BigDecimal.ZERO);
			}

			// Update Addons Details
			for (TicketAddonsDetailsDTO addonsDetailsDTO : repositoryTicketDTO.getTicketAddonsDetails()) {
				addonsDetailsDTO.setTicketStatus(TicketStatusEM.TRIP_CANCEL_INITIATED);
			}

			// Validate User Permissions
			if (authDTO.getUser().getUserRole().getId() != UserRoleEM.USER_ROLE.getId()) {
				throw new ServiceException(ErrorCode.CANCELLATION_VERIFICATION_USER_FAIL);
			}

			// // User Validations and check override flag
			// if (authDTO.getUser().getId() !=
			// repositoryTicketDTO.getTicketUser().getId() &&
			// !ticketDTO.isOverideFlag()) {
			// throw new
			// ServiceException(ErrorCode.CANCELLATION_VERIFICATION_USER_FAIL);
			// }

			// get Ticket Transaction
			TicketTransactionDTO transactionDTO = new TicketTransactionDTO();
			transactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
			repositoryTicketDTO.setTicketXaction(transactionDTO);
			ticketService.getTicketTransaction(authDTO, repositoryTicketDTO);

			repositoryTicketDTO.setFromStation(stationService.getStation(repositoryTicketDTO.getFromStation()));
			repositoryTicketDTO.setToStation(stationService.getStation(repositoryTicketDTO.getToStation()));

			BigDecimal totalRefundAmount = BigDecimal.ZERO;
			// calculate User commissions
			BigDecimal debitCommissionAmount = BigDecimal.ZERO;
			BigDecimal debitExtraCommissionAmount = BigDecimal.ZERO;
			BigDecimal debitAddonsAmount = repositoryTicketDTO.getAddonsValue();
			BigDecimal creditTDSTaxAmount = BigDecimal.ZERO;
			BigDecimal creditAcBusTax = BigDecimal.ZERO;

			// Update Transactions
			for (TicketDetailsDTO ticketDetailsDTO : repositoryTicketDTO.getTicketDetails()) {
				totalRefundAmount = totalRefundAmount.add(ticketDetailsDTO.getRefundAmount());
				creditAcBusTax = creditAcBusTax.add(ticketDetailsDTO.getAcBusTax());
			}

			// Update ticket status
			if (totalBookedSeatCount == repositoryTicketDTO.getTicketDetails().size()) {
				repositoryTicketDTO.setTicketStatus(TicketStatusEM.TRIP_CANCEL_INITIATED);
			}

			// Sum of All ticket Addons Details
			for (TicketAddonsDetailsDTO addonsDetailsDTO : repositoryTicketDTO.getTicketAddonsDetails()) {
				addonsDetailsDTO.setTicketStatus(TicketStatusEM.TRIP_CANCEL_INITIATED);
			}

			// Revoke Commission based on Namespace Setting
			if (repositoryTicketDTO.getTicketXaction().getCommissionAmount().compareTo(BigDecimal.ZERO) != 0 && authDTO.getNamespace().getProfile().isCancellationCommissionRevokeFlag()) {
				BigDecimal bookingCommissionPercent = repositoryTicketDTO.getTicketXaction().getCommissionAmount().multiply(Numeric.ONE_HUNDRED).divide(repositoryTicketDTO.getTicketXaction().getTransactionAmount().subtract(repositoryTicketDTO.getTicketXaction().getAcBusTax()).subtract(repositoryTicketDTO.getTicketXaction().getTdsTax()), 2);

				CommissionDTO serviceTaxDTO = commissionService.getCommission(authDTO, repositoryTicketDTO.getTicketUser(), CommissionTypeEM.TICKETS_BOOKING);
				BigDecimal bookingCommissionAmount = BigDecimal.ZERO;
				if (serviceTaxDTO != null && serviceTaxDTO.getServiceTax().compareTo(BigDecimal.ZERO) != 0) {
					/** Reference : StringUtil.commissionReverseCalculation */
					BigDecimal serviceTaxAmount = repositoryTicketDTO.getTicketXaction().getCommissionAmount().multiply(serviceTaxDTO.getServiceTax()).divide(Numeric.ONE_HUNDRED.add(serviceTaxDTO.getServiceTax()), 2);
					bookingCommissionAmount = serviceTaxAmount.multiply(Numeric.ONE_HUNDRED).divide(serviceTaxDTO.getServiceTax(), 2);
					bookingCommissionPercent = bookingCommissionAmount.multiply(Numeric.ONE_HUNDRED).divide(repositoryTicketDTO.getTicketXaction().getTransactionAmount().subtract(repositoryTicketDTO.getTicketXaction().getAcBusTax()).subtract(repositoryTicketDTO.getTicketXaction().getTdsTax()), 2, 2);
				}
				else {
					bookingCommissionAmount = repositoryTicketDTO.getTicketXaction().getCommissionAmount();
				}

				BigDecimal bookingSeatCommissionAmount = repositoryTicketDTO.getTotalSeatFare().subtract(debitAddonsAmount).divide(Numeric.ONE_HUNDRED, 2).multiply(bookingCommissionPercent);
				debitCommissionAmount = bookingSeatCommissionAmount.add(bookingSeatCommissionAmount.divide(Numeric.ONE_HUNDRED, 2).multiply(serviceTaxDTO.getServiceTax()));

				if (repositoryTicketDTO.getTicketXaction().getTdsTax().compareTo(BigDecimal.ZERO) != 0) {
					BigDecimal tdsTaxPercent = repositoryTicketDTO.getTicketXaction().getTdsTax().multiply(Numeric.ONE_HUNDRED).divide(bookingCommissionAmount, 2);
					creditTDSTaxAmount = (bookingSeatCommissionAmount.add(debitExtraCommissionAmount)).divide(Numeric.ONE_HUNDRED, 2).multiply(tdsTaxPercent);
				}
			}

			// ticket transaction
			TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
			ticketTransactionDTO.setTransactionAmount(repositoryTicketDTO.getTotalFare().subtract(debitCommissionAmount.add(debitExtraCommissionAmount)).subtract(debitAddonsAmount));
			ticketTransactionDTO.setAcBusTax(creditAcBusTax);
			ticketTransactionDTO.setTdsTax(creditTDSTaxAmount);
			ticketTransactionDTO.setExtraCommissionAmount(debitExtraCommissionAmount);
			ticketTransactionDTO.setCommissionAmount(debitCommissionAmount);
			ticketTransactionDTO.setAddonsAmount(debitAddonsAmount);
			ticketTransactionDTO.setCancellationCommissionAmount(BigDecimal.ZERO);
			ticketTransactionDTO.setTransactionMode(repositoryTicketDTO.getTicketXaction().getTransactionMode());
			ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
			ticketTransactionDTO.setRefundAmount(totalRefundAmount.add(creditAcBusTax).subtract((debitCommissionAmount.add(debitExtraCommissionAmount).add(debitAddonsAmount))));
			ticketTransactionDTO.setRemarks(StringUtil.substring(ticketDTO.getRemarks(), 120));

			// Refund status of the ticket
			ticketTransactionDTO.setRefundStatus(RefundStatusEM.REFUND_TO_ACCOUNT);
			if (repositoryTicketDTO.getTicketXaction().getTransactionMode().getId() == TransactionModeEM.PAYMENT_PAYMENT_GATEWAY.getId()) {
				ticketTransactionDTO.setRefundStatus(RefundStatusEM.INITIAL);
			}
			repositoryTicketDTO.setTicketXaction(ticketTransactionDTO);

			// update Ticket, and it's transaction
			updateTripCancelInitialize(authDTO, repositoryTicketDTO);

			// send trip cancel sms notification
			if (dataMap != null && dataMap.containsKey(Text.TRIP_CANCEL_SMS_SENT_FLAG) && StringUtil.getIntegerValue(dataMap.get(Text.TRIP_CANCEL_SMS_SENT_FLAG)) == 1) {
				notificationService.tripCancelNotification(authDTO, ticketDTO.getCode(), dataMap.get("REASON"), dataMap.get("SUPPORT_NUMBER"));
			}

		}
		else {
			if (repositoryTicketDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			if (repositoryTicketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
				throw new ServiceException(ErrorCode.SEAT_ALREADY_CANCELLED);
			}
			else if (repositoryTicketDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId()) {
				throw new ServiceException(ErrorCode.NOT_CONFIRM_BOOKED_TICKET);
			}
			else {
				throw new ServiceException(ErrorCode.NOT_CONFIRM_BOOKED_TICKET);
			}
		}
		return ticketDTO;
	}

	private void updateTripCancelInitialize(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);

				ticketService.saveTicketTransaction(connection, authDTO, ticketDTO);

				ticketService.saveTicketCancelTransaction(connection, authDTO, ticketDTO);

				ticketService.saveTicketCancellationDetails(connection, authDTO, ticketDTO);
				// Audit Log
				ticketService.insertTicketAuditLog(authDTO, ticketDTO, connection, "Trip Cancel Initiate");
			}
			catch (ServiceException e) {
				connection.rollback();
				throw e;
			}
			catch (Exception e) {
				e.printStackTrace();
				connection.rollback();
				throw e;
			}
			finally {
				connection.commit();
				connection.setAutoCommit(true);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error("Error in saving the cancel phone ticket: " + e.getMessage());
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
		}
	}

	private void updateTripCancelAcknowledgeStatus(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);

				// Update ticket table
				ticketService.UpdateTicketStatus(connection, authDTO, ticketDTO);
				// Ticket Cancel Detail
				ticketService.updateTripCancelTicketDetail(connection, authDTO, ticketDTO);

				if (ticketDTO.getTicketUser().getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId()) {
					ticketDTO.getUserTransaction().setRefferenceId(ticketDTO.getTicketXaction().getId());
					transactionService.SaveUserTransaction(connection, authDTO, ticketDTO.getTicketUser(), ticketDTO.getUserTransaction());
				}

				// Update Trip seat details table
				tripService.updateTripSeatDetailsWithExtras(connection, authDTO, ticketDTO);

			}
			catch (ServiceException e) {
				connection.rollback();
				throw e;
			}
			catch (Exception e) {
				e.printStackTrace();
				connection.rollback();
				throw e;
			}
			finally {
				connection.commit();
				connection.setAutoCommit(true);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error("Error in saving the cancel phone ticket: " + e.getMessage());
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
		}
	}

	private void updateTripCancelReject(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);

				ticketService.rejectTripCancelTransaction(connection, authDTO, ticketDTO);
				// Audit Log
				ticketDTO.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);
				ticketService.insertTicketAuditLog(authDTO, ticketDTO, connection, "Trip Cancel Rejected");
			}
			catch (ServiceException e) {
				connection.rollback();
				throw e;
			}
			catch (Exception e) {
				e.printStackTrace();
				connection.rollback();
				throw e;
			}
			finally {
				connection.commit();
				connection.setAutoCommit(true);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error("Error in saving the cancel phone ticket: " + e.getMessage());
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
		}
	}

	private DateTime getCancellationDateTime(AuthDTO authDTO, TicketDTO ticketDTO) {
		DateTime travelDateTime = null;
		if (authDTO.getNamespace().getProfile().getCancellationTimeType().equals(Constants.STAGE)) {
			travelDateTime = ticketDTO.getTripDateTime();
		}
		else {
			travelDateTime = getTripOriginStationDatetime(authDTO, ticketDTO.getTripDTO());
		}
		ticketDTO.getTripDTO().getAdditionalAttributes().put(Constants.CANCELLATION_DATETIME, DateUtil.convertDateTime(travelDateTime));

		return travelDateTime;
	}

	public static synchronized void validateFreshTransaction(TicketDTO ticketDTO) {
		if (EhcacheManager.getFreshTransactionEhCache().get(ticketDTO.getCode()) == null) {
			Element element = new Element(ticketDTO.getCode(), ticketDTO.getCode());
			EhcacheManager.getFreshTransactionEhCache().put(element);
		}
		else {
			throw new ServiceException(ErrorCode.PARALLEL_SAME_TRANSACTION_OCCUR);
		}
	}

	private void calculateScheduleTicketTransferDateTime(AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(ticketDTO.getTripDTO().getCode());
		tripDTO = tripService.getTrip(authDTO, tripDTO);
		ticketDTO.getScheduleTicketTransferTerms().setDateTime(BitsUtil.getTicketTransferTermsDateTime(authDTO, ticketDTO.getScheduleTicketTransferTerms(), ticketDTO.getTripDateTime(), tripDTO.getTripDateTimeV2()));
	}

	private List<NamespaceDTO> getAliasNamespaceList(AuthDTO authDTO, UserDTO ticketUser) {
		List<NamespaceDTO> aliasNamespaceList = new ArrayList<>();
		Map<String, String> aliasMap = Maps.newHashMap();
		List<UserDTO> userList = userService.getAllUserV2(authDTO, UserTagEM.API_USER_RB);
		for (UserDTO userDTO : userList) {
			if (userDTO.getAdditionalAttribute() == null || !userDTO.getAdditionalAttribute().containsKey(Constants.ALIAS_NAMESPACE) || ticketUser.getId() != userDTO.getId()) {
				continue;
			}
			String aliasNamespaceCode = userDTO.getAdditionalAttribute().get(Constants.ALIAS_NAMESPACE);
			aliasMap.put(aliasNamespaceCode, aliasNamespaceCode);
		}

		for (String aliasCode : aliasMap.values()) {
			NamespaceDTO namespace = authService.getAliasNamespace(aliasCode);
			aliasNamespaceList.add(namespace);
		}
		// if alias Namespace not found - use parent Namespace
		if (aliasNamespaceList.isEmpty()) {
			aliasNamespaceList.add(authDTO.getNamespace());
		}
		return aliasNamespaceList;
	}
}
