package org.in.com.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLTransactionRollbackException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.slack.SlackService;
import org.in.com.aggregator.sms.SMSService;
import org.in.com.aggregator.whatsapp.WhatsappService;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.TicketCache;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ConnectDAO;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.CommissionDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.CommissionTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.EventNotificationEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.dto.enumeration.WalletAccessEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BlockSeatsService;
import org.in.com.service.CancelTicketService;
import org.in.com.service.CommissionService;
import org.in.com.service.ConfirmSeatsService;
import org.in.com.service.DiscountService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStationPointService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.TicketHelperService;
import org.in.com.service.TicketService;
import org.in.com.service.TicketTaxService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.service.UserTransactionService;
import org.in.com.service.UserWalletService;
import org.in.com.service.pg.PaymentRequestService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;
import net.sf.ehcache.Element;

@EnableAsync
@Service
public class ConfirmSeatsImpl extends CacheCentral implements ConfirmSeatsService {
	@Autowired
	PaymentRequestService paymentRequestService;
	@Autowired
	UserService userService;
	@Autowired
	UserTransactionService transactionService;
	@Autowired
	TicketService ticketService;
	@Autowired
	CommissionService commissionService;
	@Autowired
	BlockSeatsService blockSeatsService;
	@Autowired
	TripService tripService;
	@Autowired
	CancelTicketService cancelTicketService;
	@Autowired
	SMSService smsService;
	@Autowired
	EmailService emailService;
	@Autowired
	CancelTicketImpl cancelTicketImpl;
	@Autowired
	TicketHelperService ticketHelperService;
	@Autowired
	UserWalletService userWalletService;
	@Autowired
	TicketTaxService ticketTaxService;
	@Autowired
	SlackService slack;
	@Autowired
	DiscountService discountService;
	@Autowired
	ScheduleDynamicStageFareService dynamicFareService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	ScheduleStationPointService scheduleStationPointService;
	@Lazy
	@Autowired
	NotificationService notificationService;
	@Autowired
	WhatsappService whatsappService;

	public BookingDTO confirmBooking(AuthDTO authDTO, String bookingCode, String transactionMode, String mobileNumber, String emailId) {

		// Validate Fresh Transaction
		validateFreshTransaction(authDTO, bookingCode);

		BookingDTO bookingDTO = getBookingDTO(authDTO, bookingCode);
		BookingDTO finalBookingDTO = null;

		boolean paymentStatusFlag = Text.FALSE;
		boolean userBalanceStatusFlag = Text.FALSE;
		boolean isConfirmedTicketFlag = Text.FALSE;
		if (StringUtil.isNotNull(bookingDTO.getPaymentGatewayPartnerCode())) {
			PaymentGatewayTransactionDTO gatewayTransactionDTO = paymentRequestService.getPaymentGatewayTransactionAmount(authDTO, bookingDTO.getCode(), PaymentGatewayTransactionTypeEM.PAYMENT);
			if (gatewayTransactionDTO.getAmount().compareTo(BigDecimal.ZERO) == 0) {
				throw new ServiceException(ErrorCode.PAYMENT_GATEWAY_TRANSACTION_FAIL_NOT_COMPLETED);
			}
			else if (gatewayTransactionDTO.getAmount().compareTo(bookingDTO.getTransactionAmount().setScale(0, RoundingMode.HALF_UP)) == -1) {
				throw new ServiceException(ErrorCode.PAYMENT_GATEWAY_TRANSACTION_AMOUNT_TICKET_AMOUNT_MISMATCH);
			}
			else if (gatewayTransactionDTO.getAmount().compareTo(bookingDTO.getTransactionAmount().setScale(0, RoundingMode.HALF_UP)) >= 0) {
				paymentStatusFlag = true;
				bookingDTO.setTransactionMode(TransactionModeEM.PAYMENT_PAYMENT_GATEWAY);
			}
		}

		UserDTO userDTO = userService.getUser(authDTO, bookingDTO.getTicketUserDTO());
		TicketDTO firstTicket = bookingDTO.getTicketList().get(0);
		if (firstTicket.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
			userDTO = authDTO.getUser();
		}

		// get and check your balance
		CommissionDTO commissionDTO = null;
		if (!paymentStatusFlag && UserRoleEM.USER_ROLE.getId() == userDTO.getUserRole().getId()) {
			commissionDTO = commissionService.getCommission(authDTO, userDTO, CommissionTypeEM.TICKETS_BOOKING);

			if (userDTO.getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId()) {
				BigDecimal availableBalance = userService.getCurrentCreditBalace(authDTO, userDTO);
				if (commissionDTO != null) {
					availableBalance = availableBalance.add(commissionDTO.getCreditlimit());
				}
				if (availableBalance.compareTo(bookingDTO.getTransactionAmount()) == -1) {
					throw new ServiceException(ErrorCode.LOW_AVAILABLE_BALANCE);
				}
			}
			userBalanceStatusFlag = true;
			bookingDTO.setTransactionMode(StringUtil.isNotNull(transactionMode) ? TransactionModeEM.getTransactionModeEM(transactionMode) : TransactionModeEM.PAYMENT_PREPAID);
		}
		if (UserRoleEM.TABLET_POB_ROLE.getId() == authDTO.getUser().getUserRole().getId() || UserRoleEM.DRIVER.getId() == authDTO.getUser().getUserRole().getId()) {
			bookingDTO.setTransactionMode(StringUtil.isNotNull(transactionMode) ? TransactionModeEM.getTransactionModeEM(transactionMode) : TransactionModeEM.PAYMENT_PREPAID);
		}

		for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {

			// Validate ticket available if block time over
			if (DateTime.now(TimeZone.getDefault()).gteq(ticketDTO.getBlockingLiveTime()) && (ticketDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId() || (paymentStatusFlag && ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()))) {
				SearchDTO search = new SearchDTO();
				search.setFromStation(ticketDTO.getFromStation());
				search.setToStation(ticketDTO.getToStation());
				search.setTravelDate(ticketDTO.getTripDate());
				ticketDTO.getTripDTO().setSearch(search);
				blockSeatsService.lnstanceBlockSeats(authDTO, ticketDTO);
			}
			if (userBalanceStatusFlag || paymentStatusFlag || UserRoleEM.TABLET_POB_ROLE.getId() == authDTO.getUser().getUserRole().getId() || UserRoleEM.DRIVER.getId() == authDTO.getUser().getUserRole().getId()) {

				TicketDTO DBticketDTO = new TicketDTO();
				DBticketDTO.setCode(ticketDTO.getCode());
				DBticketDTO.setId(ticketDTO.getId());
				ticketService.getTicketStatus(authDTO, DBticketDTO);
				// Copy cache data
				DBticketDTO.setTripDTO(ticketDTO.getTripDTO());
				DBticketDTO.setTransactionMode(bookingDTO.getTransactionMode());
				DBticketDTO.setCancellationTerm(StringUtil.isNotNull(ticketDTO.getCancellationTerm().getCode()) && ticketDTO.getCancellationTerm().getPolicyList() != null ? ticketDTO.getCancellationTerm() : DBticketDTO.getCancellationTerm());
				DBticketDTO.setBoardingPoint(ticketDTO.getBoardingPoint());
				DBticketDTO.setDroppingPoint(ticketDTO.getDroppingPoint());
				DBticketDTO.setBlockingLiveTime(ticketDTO.getBlockingLiveTime());
				DBticketDTO.setRemarks(ticketDTO.getRemarks());

				// verify Mask mobile number with block
				if (StringUtil.isNotNull(mobileNumber) && !BitsUtil.isSimilarityMobileNumber(DBticketDTO.getPassengerMobile(), mobileNumber)) {
					throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER, "Both mobile number mismatch " + DBticketDTO.getPassengerMobile() + Text.HYPHEN + mobileNumber);
				}

				DBticketDTO.setPassengerMobile(StringUtil.isNull(mobileNumber, ticketDTO.getPassengerMobile()));
				DBticketDTO.setPassengerEmailId(StringUtil.isNull(emailId, ticketDTO.getPassengerEmailId()));
				try {
					if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() || DBticketDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId() || (paymentStatusFlag && DBticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId())) {
						if (firstTicket.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
							DBticketDTO.setTicketUser(userDTO);
						}
						// Validate Ticket Details
						validateTicketDetails(authDTO, DBticketDTO);
						// calculate User commissions
						BigDecimal creditCommissionAmount = new BigDecimal(0);
						BigDecimal creditExtraCommissionAmount = new BigDecimal(0);
						BigDecimal debitTdsTax = BigDecimal.ZERO;
						BigDecimal creditAddonsAmount = DBticketDTO.getAddonsValue();
						BigDecimal commissionServiceTaxAmount = BigDecimal.ZERO;

						// Commission
						if (commissionDTO != null && commissionDTO.getCommissionValue().compareTo(BigDecimal.ZERO) > 0) {
							if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
								creditCommissionAmount = DBticketDTO.getTotalSeatFare().subtract(creditAddonsAmount).multiply(commissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2);
							}
							else if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
								creditCommissionAmount = commissionDTO.getCommissionValue().multiply(new BigDecimal(DBticketDTO.getTicketDetails().size()));
							}
							// Extra Commission Processes
							CommissionDTO extraCommissionDTO = commissionService.getBookingExtraCommission(authDTO, userDTO, commissionDTO, DBticketDTO);
							if (extraCommissionDTO != null && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
								creditExtraCommissionAmount = DBticketDTO.getTotalSeatFare().subtract(creditAddonsAmount).multiply(extraCommissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2);
							}
							else if (extraCommissionDTO != null && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
								creditExtraCommissionAmount = extraCommissionDTO.getCommissionValue().multiply(new BigDecimal(DBticketDTO.getTicketDetails().size()));
							}
							// Override the Base commission if set in Extra
							// Commission
							if (extraCommissionDTO != null && extraCommissionDTO.getActiveFlag() == -1) {
								creditCommissionAmount = BigDecimal.ZERO;
							}
							creditCommissionAmount = creditCommissionAmount.add(creditExtraCommissionAmount);

							if (commissionDTO.getTdsTaxValue() != null && commissionDTO.getTdsTaxValue().compareTo(BigDecimal.ZERO) == 1) {
								debitTdsTax = creditCommissionAmount.multiply(commissionDTO.getTdsTaxValue()).divide(Numeric.ONE_HUNDRED, 2);
							}
							if (commissionDTO.getServiceTax() != null && commissionDTO.getServiceTax().compareTo(BigDecimal.ZERO) == 1) {
								commissionServiceTaxAmount = creditCommissionAmount.multiply(commissionDTO.getServiceTax()).divide(Numeric.ONE_HUNDRED, 2);
								creditCommissionAmount = creditCommissionAmount.add(commissionServiceTaxAmount);
							}
						}

						UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
						userTransactionDTO.setRefferenceCode(DBticketDTO.getCode());
						userTransactionDTO.setDebitAmount(DBticketDTO.getTotalFare().add(debitTdsTax).add(DBticketDTO.getDebitAddonsValue()));
						userTransactionDTO.setTdsTax(debitTdsTax);
						userTransactionDTO.setCommissionAmount(creditCommissionAmount);
						userTransactionDTO.setCreditAmount(creditCommissionAmount.add(creditAddonsAmount));
						userTransactionDTO.setTransactionAmount(userTransactionDTO.getCreditAmount().subtract(userTransactionDTO.getDebitAmount()));
						userTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
						userTransactionDTO.setTransactionMode(bookingDTO.getTransactionMode());
						DBticketDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
						DBticketDTO.setUserTransaction(userTransactionDTO);
						// Update ticket status
						DBticketDTO.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);

						// Update ticket transaction status
						for (TicketDetailsDTO ticketDetailsDTO : DBticketDTO.getTicketDetails()) {
							ticketDetailsDTO.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);
						}

						for (TicketAddonsDetailsDTO addonsDetailsDTO : DBticketDTO.getTicketAddonsDetails()) {
							addonsDetailsDTO.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);
						}
						// ticket transaction
						TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
						ticketTransactionDTO.setTransactionAmount(DBticketDTO.getTotalFare().add(debitTdsTax).subtract(creditAddonsAmount));
						ticketTransactionDTO.setAcBusTax(DBticketDTO.getAcBusTax());
						ticketTransactionDTO.setTdsTax(debitTdsTax);
						ticketTransactionDTO.setCommissionAmount(creditCommissionAmount);
						ticketTransactionDTO.setExtraCommissionAmount(creditExtraCommissionAmount);
						ticketTransactionDTO.setAddonsAmount(creditAddonsAmount);
						ticketTransactionDTO.setTransactionMode(bookingDTO.getTransactionMode());
						ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
						DBticketDTO.setTicketXaction(ticketTransactionDTO);

						// Apply wallet
						if (WalletAccessEM.getWalletAccessEM(authDTO.getNamespaceCode()) != null && DBticketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId() && DBticketDTO.isContainAddonWalletTransaction()) {
							userWalletService.processWalletTransaction(authDTO, ticketDTO, authDTO.getUserCustomer());
						}

						// update Ticket, and it's transaction
						SaveConfirmTicket(authDTO, DBticketDTO);

						// Update Coupon usage
						TicketAddonsDetailsDTO ticketAddonsDetails = DBticketDTO.getTicketCouponAddon(AddonsTypeEM.COUPON_DISCOUNT);
						if (ticketAddonsDetails != null) {
							discountService.updateDiscountCouponUsage(authDTO, ticketAddonsDetails);
						}

						// Clear Trip seat cache
						tripService.clearBookedBlockedSeatsCache(authDTO, DBticketDTO.getTripDTO());

						/** Apply Schedule Station Point Contact Address */
						applyScheduleStationPointContact(authDTO, DBticketDTO);

						// Cancellation datetime based on NS Settings
						DateTime travelDateTime = getCancellationDateTime(authDTO, DBticketDTO);

						// Notifications
						if (DeviceMediumEM.API_USER.getId() != ticketDTO.getDeviceMedium().getId() && DBticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
							cancelTicketImpl.getCancellationPolicyConvention(authDTO, ticketDTO.getTicketUser(), DBticketDTO.getCancellationTerm(), DBticketDTO.getFromStation().getState(), travelDateTime, DBticketDTO.getSeatFareUniqueList());
							// send notification
							notificationService.sendTicketBookingNotification(authDTO, DBticketDTO);
						}
						if (DBticketDTO.getTicketUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
							emailService.sendBookingEmail(authDTO, DBticketDTO);
						}
						// Send Ticket Event
						if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() && StringUtil.isContains(Constants.TICKET_EVENT, authDTO.getNamespaceCode())) {
							smsService.sendTicketEventSMS(authDTO, DBticketDTO, "New Ticket Booked");
						}
						// send Ticket Event To Customer
						notificationService.sendCustomerTicketEvent(authDTO, DBticketDTO, EventNotificationEM.TICKET_BOOKING);
						// Wallet Update
						if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() && DBticketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId() && WalletAccessEM.getWalletAccessEM(authDTO.getNamespaceCode()) != null) {
							userWalletService.addAfterTravelTransaction(authDTO, DBticketDTO, Text.ON_BOOK);
						}
						// Tax Invoice Mail
						if (DBticketDTO.getTax() != null && DBticketDTO.getTax().getId() != 0) {
							ticketTaxService.sendTaxInvoiceEmail(authDTO, DBticketDTO);
						}
						// Dynamic Pricing
						if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
							dynamicFareService.updateTicketStatus(authDTO, DBticketDTO);
						}
						notificationPushService.pushTicketBlockConfirmNotification(authDTO, DBticketDTO, NotificationSubscriptionTypeEM.TICKET_CONFIRM);

						if (finalBookingDTO == null) {
							finalBookingDTO = new BookingDTO();
							finalBookingDTO.setBookingDTO(bookingDTO);
						}
						finalBookingDTO.addTicketDTO(DBticketDTO);

					}
					else if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
						if (finalBookingDTO == null) {
							finalBookingDTO = new BookingDTO();
							finalBookingDTO.setBookingDTO(bookingDTO);
						}
						// cancellation date time
						getCancellationDateTime(authDTO, DBticketDTO);

						finalBookingDTO.addTicketDTO(DBticketDTO);
						isConfirmedTicketFlag = Text.TRUE;
					}
					else if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
						throw new ServiceException(ErrorCode.PHONE_BOOKED_TICKET);
					}
					else {
						throw new ServiceException();
					}
				}
				catch (ServiceException e) {
					if (e.getErrorCode() != null) {
						throw e;
					}
					else {
						throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
					}
				}
			}

		}
		// Release lock of 5 sec
		clearFreshTransaction(bookingCode);

		if (finalBookingDTO != null && !isConfirmedTicketFlag) {
			TicketCache ticketCache = new TicketCache();
			ticketCache.putBookingDTO(authDTO, finalBookingDTO);
			ticketHelperService.processTicketAfterTripTime(authDTO, finalBookingDTO);
		}
		else if (finalBookingDTO == null) {
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
		}
		return finalBookingDTO;
	}

	private void SaveConfirmTicket(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);

				ticketService.saveTicketTransaction(connection, authDTO, ticketDTO);

				ticketService.UpdateTicketStatus(connection, authDTO, ticketDTO);

				// update Trip seat Block
				tripService.updateTripSeatDetailsStatus(connection, authDTO, ticketDTO);

				if (ticketDTO.getTicketUser().getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId() && ticketDTO.getTicketXaction().getTransactionMode().getId() != TransactionModeEM.PAYMENT_PAYMENT_GATEWAY.getId()) {
					ticketDTO.getUserTransaction().setRefferenceId(ticketDTO.getTicketXaction().getId());
					transactionService.SaveUserTransaction(connection, authDTO, ticketDTO.getTicketUser(), ticketDTO.getUserTransaction());
				}
			}
			catch (ServiceException e) {
				connection.rollback();
				throw e;
			}
			catch (SQLTransactionRollbackException e) {
				slack.sendAlert(authDTO, ticketDTO.getCode() + " DL06 - Deadlock found when trying to get lock; try restarting transaction");

				e.printStackTrace();
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
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
		}
	}

	private DateTime getCancellationDateTime(AuthDTO authDTO, TicketDTO DBticketDTO) {
		DateTime travelDateTime = null;
		if (authDTO.getNamespace().getProfile().getCancellationTimeType().equals(Constants.STAGE)) {
			travelDateTime = DBticketDTO.getTripDateTime();
		}
		else {
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(DBticketDTO.getTripDTO().getCode());
			tripService.getTrip(authDTO, tripDTO);
			travelDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getTripMinutes());
		}
		DBticketDTO.getTripDTO().getAdditionalAttributes().put(Constants.CANCELLATION_DATETIME, DateUtil.convertDateTime(travelDateTime));

		return travelDateTime;
	}

	private void SaveConfirmPhoneTicket(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);

				ticketService.saveTicketTransaction(connection, authDTO, ticketDTO);
				ticketService.updateTicketStatusV2(connection, authDTO, ticketDTO);
				if (ticketDTO.getTicketUser().getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId()) {
					ticketDTO.getUserTransaction().setRefferenceId(ticketDTO.getTicketXaction().getId());
					transactionService.SaveUserTransaction(connection, authDTO, ticketDTO.getTicketUser(), ticketDTO.getUserTransaction());
				}
				// update Trip seat Block
				tripService.updateTripSeatDetailsWithExtrasV2(connection, authDTO, ticketDTO);
			}
			catch (SQLTransactionRollbackException e) {
				slack.sendAlert(authDTO, ticketDTO.getCode() + " DL05 - Deadlock found when trying to get lock; try restarting transaction");

				e.printStackTrace();
				connection.rollback();
				throw e;
			}
			catch (ServiceException e) {
				throw e;
			}
			catch (Exception e) {
				e.printStackTrace();
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
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);

		}
	}

	public TicketDTO confirmPhoneBooking(AuthDTO authDTO, TicketDTO ticketDTO) {

		// Validate ticket available if block time over
		TicketDTO DBticketDTO = new TicketDTO();
		DBticketDTO.setCode(ticketDTO.getCode());
		ticketService.getTicketStatus(authDTO, DBticketDTO);

		if (!ticketDTO.isOverideFlag() && authDTO.getUser().getId() != DBticketDTO.getTicketUser().getId()) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}

		if (ticketDTO.getTicketExtra() != null && !ticketDTO.getTicketExtra().getAdditionalAttributes().isEmpty()) {
			List<TicketDetailsDTO> detailsList = new ArrayList<>();
			for (TicketDetailsDTO ticketDetails : ticketDTO.getTicketDetails()) {
				BigDecimal newSeatFare = StringUtil.getBigDecimalValue(ticketDTO.getTicketExtra().getAdditionalAttributes().get(ticketDTO.getJourneyType().getCode() + "_" + ticketDetails.getSeatCode()));
				if (newSeatFare.compareTo(BigDecimal.ZERO) == 0) {
					continue;
				}
				for (Iterator<TicketDetailsDTO> iterator = DBticketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
					TicketDetailsDTO repoTicketDetails = iterator.next();
					if (ticketDetails.getSeatCode().equals(repoTicketDetails.getSeatCode())) {
						repoTicketDetails.setSeatFare(newSeatFare);
						detailsList.add(repoTicketDetails);
						iterator.remove();
						break;
					}
				}
			}
			if (!DBticketDTO.getTicketDetails().isEmpty()) {
				detailsList.addAll(DBticketDTO.getTicketDetails());
			}
			DBticketDTO.setTicketDetails(detailsList);
		}

		TripDTO tripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, DBticketDTO.getTripDTO());
		DBticketDTO.setTripDTO(tripDTO);

		TicketExtraDTO ticketExtraDTO = ticketService.getTicketExtra(authDTO, DBticketDTO);
		// Validate PBL Block Live Time
		if (ticketExtraDTO.getBlockReleaseMinutes() != 0 && BitsUtil.validateBlockReleaseTime(ticketExtraDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTimeV2(), DBticketDTO.getTicketAt())) {
			throw new ServiceException(ErrorCode.SELECTED_SEAT_BLOCK_TIME_OVER);
		}

		List<TicketDetailsDTO> cancelTicketDetailsList = new ArrayList<>();
		for (Iterator<TicketDetailsDTO> iterator = DBticketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
			TicketDetailsDTO ticketDetailsDTO = iterator.next();
			if (ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				iterator.remove();
				continue;
			}
			if (!ticketDTO.getSeatCodeList().contains(ticketDetailsDTO.getSeatCode())) {
				TicketDetailsDTO cancelTicketDetailsDTO = new TicketDetailsDTO();
				cancelTicketDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
				cancelTicketDetailsList.add(cancelTicketDetailsDTO);

				iterator.remove();
				continue;
			}
		}
		if (DBticketDTO.getTicketDetails().isEmpty()) {
			throw new ServiceException(ErrorCode.SEAT_NOT_AVAILABLE);
		}
		// Cancel remaining Tickets
		if (!cancelTicketDetailsList.isEmpty()) {
			TicketDTO cancelTicketDTO = new TicketDTO();
			cancelTicketDTO.setCode(ticketDTO.getCode());
			cancelTicketDTO.setTicketDetails(cancelTicketDetailsList);
			cancelTicketService.cancelPhoneBooking(authDTO, cancelTicketDTO);
		}

		UserDTO offlineUser = null;
		UserDTO ticketUser = DBticketDTO.getTicketUser();
		if (ticketDTO.getTicketForUser() != null) {
			offlineUser = userService.getUser(authDTO, ticketDTO.getTicketForUser());
		}

		boolean UserBalanceStatusFlag = false;
		// get Booking commission
		UserDTO userDTO = ticketDTO.isMyAccountFlag() ? authDTO.getUser() : offlineUser != null ? offlineUser : DBticketDTO.getTicketUser();
		DBticketDTO.setTicketUser(userDTO);

		CommissionDTO commissionDTO = null;
		CommissionDTO extraCommissionDTO = null;
		BigDecimal availableBalance = BigDecimal.ZERO;

		availableBalance = userService.getCurrentCreditBalace(authDTO, userDTO);
		commissionDTO = commissionService.getCommission(authDTO, userDTO, CommissionTypeEM.TICKETS_BOOKING);

		// get and check your balance
		if (commissionDTO != null) {
			availableBalance = availableBalance.add(commissionDTO.getCreditlimit());
		}

		if (userDTO.getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId() && availableBalance.compareTo(DBticketDTO.getTotalFare()) == -1) {
			throw new ServiceException(ErrorCode.LOW_AVAILABLE_BALANCE);
		}
		UserBalanceStatusFlag = true;
		DBticketDTO.setTransactionMode(ticketDTO.getTransactionMode() != null ? TransactionModeEM.getTransactionModeEM(ticketDTO.getTransactionMode().getCode()) : TransactionModeEM.PAYMENT_PREPAID);

		if (UserBalanceStatusFlag) {

			try {
				if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					DBticketDTO.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);

					// calculate User commissions
					BigDecimal debitAmount = DBticketDTO.getTotalFare();
					BigDecimal debitAcBusTax = DBticketDTO.getAcBusTax();
					BigDecimal debitTDSTax = new BigDecimal(0);
					BigDecimal creditCommissionAmount = new BigDecimal(0);
					BigDecimal creditExtraCommissionAmount = new BigDecimal(0);
					BigDecimal creditAddonsAmount = DBticketDTO.getAddonsValue();
					BigDecimal commissionServiceTaxAmount = BigDecimal.ZERO;

					if (commissionDTO != null && commissionDTO.getCommissionValue().doubleValue() > 0) {
						if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
							creditCommissionAmount = DBticketDTO.getTotalSeatFare().subtract(DBticketDTO.getAddonsValue()).multiply(commissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED);
						}
						else if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
							creditCommissionAmount = commissionDTO.getCommissionValue().multiply(new BigDecimal(ticketDTO.getTicketDetails().size()));
						}
						// Extra Commission Processes
						if (ticketDTO.isMyAccountFlag()) {
							extraCommissionDTO = commissionService.getBookingExtraCommission(authDTO, authDTO.getUser(), commissionDTO, DBticketDTO);
						}
						else {
							extraCommissionDTO = commissionService.getBookingExtraCommission(authDTO, DBticketDTO.getTicketUser(), commissionDTO, DBticketDTO);
						}
						if (extraCommissionDTO != null && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
							creditExtraCommissionAmount = DBticketDTO.getTotalSeatFare().subtract(DBticketDTO.getAddonsValue()).multiply(extraCommissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2);
						}
						else if (extraCommissionDTO != null && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
							creditExtraCommissionAmount = extraCommissionDTO.getCommissionValue().multiply(new BigDecimal(DBticketDTO.getTicketDetails().size()));
						}
						creditCommissionAmount = creditCommissionAmount.add(creditExtraCommissionAmount);

						if (commissionDTO.getTdsTaxValue() != null && commissionDTO.getTdsTaxValue().compareTo(BigDecimal.ZERO) == 1) {
							debitTDSTax = creditCommissionAmount.multiply(commissionDTO.getTdsTaxValue()).divide(Numeric.ONE_HUNDRED, 2);
						}
						if (commissionDTO.getServiceTax() != null && commissionDTO.getServiceTax().compareTo(BigDecimal.ZERO) == 1) {
							commissionServiceTaxAmount = creditCommissionAmount.multiply(commissionDTO.getServiceTax()).divide(Numeric.ONE_HUNDRED, 2);
							creditCommissionAmount = creditCommissionAmount.add(commissionServiceTaxAmount);
						}
					}

					calculateNetRevenueBookAmount(authDTO, creditCommissionAmount, commissionDTO, DBticketDTO);

					UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
					userTransactionDTO.setRefferenceCode(DBticketDTO.getCode());
					userTransactionDTO.setDebitAmount(debitAmount);
					userTransactionDTO.setCreditAmount(creditCommissionAmount.add(creditAddonsAmount));
					userTransactionDTO.setCommissionAmount(creditCommissionAmount);
					userTransactionDTO.setTdsTax(debitTDSTax);
					userTransactionDTO.setTransactionAmount(userTransactionDTO.getCreditAmount().subtract(userTransactionDTO.getDebitAmount()));
					userTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
					userTransactionDTO.setTransactionMode(DBticketDTO.getTransactionMode());
					DBticketDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
					DBticketDTO.setUserTransaction(userTransactionDTO);
					// Update ticket transaction status
					for (TicketDetailsDTO ticketDetailsDTO : DBticketDTO.getTicketDetails()) {
						ticketDetailsDTO.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);
						for (TicketAddonsDetailsDTO ticketAddonsDetailsDTO : DBticketDTO.getTicketAddonsDetails()) {
							if (ticketAddonsDetailsDTO.getSeatCode().equals(ticketDetailsDTO.getSeatCode())) {
								ticketAddonsDetailsDTO.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);
								break;
							}
						}
					}
					// ticket transaction
					TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
					ticketTransactionDTO.setTransactionAmount(DBticketDTO.getTotalFare().add(debitTDSTax).subtract(creditAddonsAmount));
					ticketTransactionDTO.setAcBusTax(debitAcBusTax);
					ticketTransactionDTO.setTdsTax(debitTDSTax);
					ticketTransactionDTO.setCommissionAmount(creditCommissionAmount);
					ticketTransactionDTO.setExtraCommissionAmount(creditExtraCommissionAmount);
					ticketTransactionDTO.setAddonsAmount(creditAddonsAmount);
					ticketTransactionDTO.setTransactionMode(DBticketDTO.getTransactionMode());
					ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
					DBticketDTO.setTicketXaction(ticketTransactionDTO);
					// capture confirmed user
					authDTO.getAdditionalAttribute().put(Text.PHONE_BOOK_CONFIRMED_BY, DBticketDTO.getTicketUser().getName());

					// update Ticket, and it's transaction
					SaveConfirmPhoneTicket(authDTO, DBticketDTO);

					/** Apply Schedule Station Point Contact Address */
					applyScheduleStationPointContact(authDTO, DBticketDTO);

					// Ticket Notifications
					notificationService.sendTicketBookingNotification(authDTO, DBticketDTO);

					tripService.clearBookedBlockedSeatsCache(authDTO, DBticketDTO.getTripDTO());

					// Send Ticket Event
					if (StringUtil.isContains(Constants.TICKET_EVENT, authDTO.getNamespaceCode())) {
						smsService.sendTicketEventSMS(authDTO, DBticketDTO, "Phone Block Ticket Booked");
					}
					// send Ticket Event To Customer
					notificationService.sendCustomerTicketEvent(authDTO, DBticketDTO, EventNotificationEM.TICKET_BOOKING);

					if (StringUtil.isNotNull(ticketDTO.getRemarks())) {
						DBticketDTO.setRemarks(ticketDTO.getRemarks());
						ticketService.updateTicketRemarks(authDTO, DBticketDTO);
					}

					if (offlineUser != null && offlineUser.getId() != 0) {
						DBticketDTO.setTicketForUser(ticketUser);
						ticketService.updateTicketForUser(authDTO, DBticketDTO);
					}

					// Dynamic Pricing Call back
					if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
						dynamicFareService.updateTicketStatus(authDTO, DBticketDTO);
					}
				}
				else if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
					ticketDTO.setTicketStatus(TicketStatusEM.PHONE_BOOKING_CANCELLED);
				}
				else if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
					throw new ServiceException(ErrorCode.SEAT_ALREADY_BLOOKED);
				}
				else if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					throw new ServiceException(ErrorCode.PHONE_BOOKED_TICKET);
				}
				else {
					throw new ServiceException();
				}

			}
			catch (ServiceException e) {
				if (e.getErrorCode() != null) {
					throw e;
				}
				else {
					throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
				}
			}

		}

		return DBticketDTO;

	}

	public TicketDTO autoConfirmPhoneBooking(AuthDTO authDTO, TicketDTO ticketDTO) {

		// Validate ticket available if block time over
		TicketDTO DBticketDTO = new TicketDTO();
		DBticketDTO.setCode(ticketDTO.getCode());
		ticketService.getTicketStatus(authDTO, DBticketDTO);
		TripDTO tripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, DBticketDTO.getTripDTO());
		DBticketDTO.setTripDTO(tripDTO);

		List<TicketDetailsDTO> cancelTicketDetailsList = new ArrayList<>();
		for (Iterator<TicketDetailsDTO> iterator = DBticketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
			TicketDetailsDTO ticketDetailsDTO = iterator.next();
			if (!ticketDTO.getSeatCodeList().contains(ticketDetailsDTO.getSeatCode())) {
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					TicketDetailsDTO cancelTicketDetailsDTO = new TicketDetailsDTO();
					cancelTicketDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
					cancelTicketDetailsList.add(cancelTicketDetailsDTO);
				}
				iterator.remove();
				continue;
			}
			ticketDetailsDTO.setTicketStatus(TicketStatusEM.PHONE_BOOKING_CANCELLED);
		}

		// Cancel remaining Tickets
		if (cancelTicketDetailsList != null && !cancelTicketDetailsList.isEmpty()) {
			TicketDTO cancelTicketDTO = new TicketDTO();
			cancelTicketDTO.setCode(ticketDTO.getCode());
			cancelTicketDTO.setTicketDetails(cancelTicketDetailsList);
			cancelTicketService.cancelPhoneBooking(authDTO, cancelTicketDTO);
		}

		boolean UserBalanceStatusFlag = false;
		// get Booking commission
		UserDTO userDTO = ticketDTO.isMyAccountFlag() ? authDTO.getUser() : DBticketDTO.getTicketUser();
		CommissionDTO commissionDTO = null;
		CommissionDTO extraCommissionDTO = null;
		BigDecimal availableBalance = BigDecimal.ZERO;

		availableBalance = userService.getCurrentCreditBalace(authDTO, userDTO);
		commissionDTO = commissionService.getCommission(authDTO, userDTO, CommissionTypeEM.TICKETS_BOOKING);

		// get and check your balance
		if (commissionDTO != null) {
			availableBalance = availableBalance.add(commissionDTO.getCreditlimit());
		}

		if (userDTO.getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId() && availableBalance.compareTo(DBticketDTO.getTotalFare()) == -1) {
			throw new ServiceException(ErrorCode.LOW_AVAILABLE_BALANCE);
		}
		UserBalanceStatusFlag = true;
		DBticketDTO.setTransactionMode(TransactionModeEM.PAYMENT_PREPAID);

		if (UserBalanceStatusFlag) {

			try {
				if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					DBticketDTO.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);

					// calculate User commissions
					BigDecimal debitAmount = DBticketDTO.getTotalFare();
					BigDecimal debitAcBusTax = DBticketDTO.getAcBusTax();
					BigDecimal debitTDSTax = new BigDecimal(0);
					BigDecimal creditCommissionAmount = new BigDecimal(0);
					BigDecimal creditExtraCommissionAmount = new BigDecimal(0);
					BigDecimal creditAddonsAmount = DBticketDTO.getAddonsValue();
					BigDecimal commissionServiceTaxAmount = BigDecimal.ZERO;

					if (commissionDTO != null && commissionDTO.getCommissionValue().doubleValue() > 0) {
						if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
							creditCommissionAmount = DBticketDTO.getTotalSeatFare().multiply(commissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED);
						}
						else if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
							creditCommissionAmount = commissionDTO.getCommissionValue();
						}
						// Extra Commission Processes
						if (ticketDTO.isMyAccountFlag()) {
							extraCommissionDTO = commissionService.getBookingExtraCommission(authDTO, authDTO.getUser(), commissionDTO, DBticketDTO);
						}
						else {
							extraCommissionDTO = commissionService.getBookingExtraCommission(authDTO, DBticketDTO.getTicketUser(), commissionDTO, DBticketDTO);
						}
						if (extraCommissionDTO != null && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
							creditExtraCommissionAmount = DBticketDTO.getTotalSeatFare().multiply(extraCommissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2);
						}
						else if (extraCommissionDTO != null && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
							creditExtraCommissionAmount = extraCommissionDTO.getCommissionValue().multiply(new BigDecimal(DBticketDTO.getTicketDetails().size()));
						}
						creditCommissionAmount = creditCommissionAmount.add(creditExtraCommissionAmount);

						if (commissionDTO.getTdsTaxValue() != null && commissionDTO.getTdsTaxValue().compareTo(BigDecimal.ZERO) == 1) {
							debitTDSTax = creditCommissionAmount.multiply(commissionDTO.getTdsTaxValue()).divide(Numeric.ONE_HUNDRED, 2);
						}
						if (commissionDTO.getServiceTax() != null && commissionDTO.getServiceTax().compareTo(BigDecimal.ZERO) == 1) {
							commissionServiceTaxAmount = creditCommissionAmount.multiply(commissionDTO.getServiceTax()).divide(Numeric.ONE_HUNDRED, 2);
							creditCommissionAmount = creditCommissionAmount.add(commissionServiceTaxAmount);
						}
					}
					UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
					userTransactionDTO.setRefferenceCode(DBticketDTO.getCode());
					userTransactionDTO.setDebitAmount(debitAmount);
					userTransactionDTO.setCreditAmount(creditCommissionAmount.add(creditAddonsAmount));
					userTransactionDTO.setCommissionAmount(creditCommissionAmount);
					userTransactionDTO.setTdsTax(debitTDSTax);
					userTransactionDTO.setTransactionAmount(userTransactionDTO.getCreditAmount().subtract(userTransactionDTO.getDebitAmount()));
					userTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
					userTransactionDTO.setTransactionMode(DBticketDTO.getTransactionMode());
					DBticketDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
					DBticketDTO.setUserTransaction(userTransactionDTO);
					// Update ticket transaction status
					for (TicketDetailsDTO ticketDetailsDTO : DBticketDTO.getTicketDetails()) {
						ticketDetailsDTO.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);
					}
					// ticket transaction
					TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
					ticketTransactionDTO.setTransactionAmount(DBticketDTO.getTotalFare().add(debitTDSTax).subtract(creditAddonsAmount));
					ticketTransactionDTO.setAcBusTax(debitAcBusTax);
					ticketTransactionDTO.setTdsTax(debitTDSTax);
					ticketTransactionDTO.setCommissionAmount(creditCommissionAmount);
					ticketTransactionDTO.setExtraCommissionAmount(creditExtraCommissionAmount);
					ticketTransactionDTO.setAddonsAmount(creditAddonsAmount);
					ticketTransactionDTO.setTransactionMode(DBticketDTO.getTransactionMode());
					ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
					DBticketDTO.setTicketXaction(ticketTransactionDTO);

					// update Ticket, and it's transaction
					SaveConfirmPhoneTicket(authDTO, DBticketDTO);

					/** Apply Schedule Station Point Contact Address */
					applyScheduleStationPointContact(authDTO, DBticketDTO);

					// Ticket Notifications
					notificationService.sendTicketBookingNotification(authDTO, DBticketDTO);
					/** send Ticket Event To Customer */
					notificationService.sendCustomerTicketEvent(authDTO, DBticketDTO, EventNotificationEM.TICKET_BOOKING);
					tripService.clearBookedBlockedSeatsCache(authDTO, DBticketDTO.getTripDTO());

					if (StringUtil.isNotNull(ticketDTO.getRemarks())) {
						DBticketDTO.setRemarks(ticketDTO.getRemarks());
						ticketService.updateTicketRemarks(authDTO, DBticketDTO);
					}
				}
				else if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
					ticketDTO.setTicketStatus(TicketStatusEM.PHONE_BOOKING_CANCELLED);
				}
				else if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
					throw new ServiceException(ErrorCode.SEAT_ALREADY_BLOOKED);
				}
				else if (DBticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					throw new ServiceException(ErrorCode.PHONE_BOOKED_TICKET);
				}
				else {
					throw new ServiceException();
				}

			}
			catch (ServiceException e) {
				if (e.getErrorCode() != null) {
					throw e;
				}
				else {
					throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
				}
			}

		}

		return DBticketDTO;
	}

	private void validateTicketDetails(AuthDTO authDTO, TicketDTO ticketDTO) {
		if (ticketDTO.getTicketDetails() == null || ticketDTO.getTicketDetails().isEmpty()) {
			throw new ServiceException(ErrorCode.SEAT_NOT_AVAILABLE);
		}
		boolean seatDouplicateEntry = ticketService.checkSeatDuplicateEntryV2(authDTO, ticketDTO);
		if (seatDouplicateEntry) {
			throw new ServiceException(ErrorCode.DOUBLE_ENTRY_VALIDATION_FAIL);
		}
	}

	private BookingDTO getBookingDTO(AuthDTO authDTO, String bookingCode) {
		BookingDTO bookingDTO = null;
		TicketCache ticketCache = new TicketCache();
		bookingDTO = ticketCache.getBookingDTO(authDTO, bookingCode);
		if (bookingDTO == null) {
			System.out.println("getBookingDTO" + bookingCode);
			TicketDTO ticketDTO = new TicketDTO();
			bookingDTO = new BookingDTO();
			bookingDTO.setNamespace(authDTO.getNamespace());
			ticketDTO.setCode(bookingCode);
			bookingDTO.setCode(bookingCode);
			ticketService.showTicket(authDTO, ticketDTO);

			// Trip related stage code
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(ticketDTO.getTripDTO().getCode());
			tripDTO = tripService.getTrip(authDTO, tripDTO);
			List<String> relatedStageCodes = tripService.getRelatedStageCodes(authDTO, tripDTO, ticketDTO.getFromStation(), ticketDTO.getToStation());

			ticketDTO.getTripDTO().setReleatedStageCodeList(relatedStageCodes);
			ticketDTO.getTripDTO().setTripDate(tripDTO.getTripDate());
			ticketDTO.setBlockingLiveTime(DateUtil.addMinituesToDate(ticketDTO.getTicketAt(), authDTO.getNamespace().getProfile().getSeatBlockTime()));

			bookingDTO.addTicketDTO(ticketDTO);
			if (StringUtil.isNotNull(ticketDTO.getRelatedTicketCode())) {
				TicketDTO returnTicketDTO = new TicketDTO();
				returnTicketDTO.setCode(ticketDTO.getRelatedTicketCode());
				ticketService.showTicket(authDTO, returnTicketDTO);

				// Trip related stage code
				TripDTO returnTripDTO = new TripDTO();
				returnTripDTO.setCode(returnTicketDTO.getTripDTO().getCode());
				returnTripDTO = tripService.getTrip(authDTO, returnTripDTO);
				List<String> returnRelatedStageCodes = tripService.getRelatedStageCodes(authDTO, returnTripDTO, returnTicketDTO.getFromStation(), returnTicketDTO.getToStation());

				returnTicketDTO.getTripDTO().setReleatedStageCodeList(returnRelatedStageCodes);
				returnTicketDTO.getTripDTO().setTripDate(returnTripDTO.getTripDate());
				returnTicketDTO.setBlockingLiveTime(DateUtil.addMinituesToDate(returnTicketDTO.getTicketAt(), authDTO.getNamespace().getProfile().getSeatBlockTime()));

				bookingDTO.addTicketDTO(returnTicketDTO);
			}
		}

		if (bookingDTO == null || bookingDTO.getTicketList().isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		return bookingDTO;
	}

	public static synchronized void validateFreshTransaction(AuthDTO authDTO, String bookingCode) {
		if (EhcacheManager.getFreshTransactionEhCache().get(bookingCode) == null) {
			Element element = new Element(bookingCode, bookingCode);
			EhcacheManager.getFreshTransactionEhCache().put(element);
		}
		else {
			System.out.println(authDTO.getNamespaceCode() + Text.HYPHEN + authDTO.getDeviceMedium().getCode() + Text.HYPHEN + authDTO.getAuthToken() + "TR01A - Parallel same transaction occurrence " + bookingCode);
			throw new ServiceException(ErrorCode.PARALLEL_SAME_TRANSACTION_OCCUR);
		}
	}

	private void clearFreshTransaction(String bookingCode) {
		EhcacheManager.getFreshTransactionEhCache().remove(bookingCode);
	}

	private void calculateNetRevenueBookAmount(AuthDTO authDTO, BigDecimal creditCommissionAmount, CommissionDTO commissionDTO, TicketDTO ticketDTO) {
		try {
			TripDAO tripDAO = new TripDAO();

			/** Convert Ticket Seat Extras */
			List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, ticketDTO);
			Map<String, TicketExtraDTO> repoTicketExtraMap = new HashMap<String, TicketExtraDTO>();
			for (TicketDetailsDTO detailsDTO : seatList) {
				repoTicketExtraMap.put(detailsDTO.getSeatCode(), detailsDTO.getTicketExtra());
			}

			BigDecimal seatCommissionAmount = BigDecimal.ZERO;
			if (creditCommissionAmount.compareTo(BigDecimal.ZERO) > 0) {
				seatCommissionAmount = creditCommissionAmount.divide(BigDecimal.valueOf(ticketDTO.getTicketDetails().size()), 2, RoundingMode.CEILING);
			}

			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				BigDecimal addonsAmount = ticketDTO.getAddonsValue(ticketDetailsDTO);
				BigDecimal netRevenueAmount = ticketDetailsDTO.getSeatFare().add(ticketDetailsDTO.getAcBusTax()).subtract(seatCommissionAmount).subtract(addonsAmount);

				TicketExtraDTO ticketExtraDTO = repoTicketExtraMap.get(ticketDetailsDTO.getSeatCode());
				if (ticketExtraDTO == null) {
					ticketExtraDTO = new TicketExtraDTO();
				}
				ticketExtraDTO.setNetAmount(netRevenueAmount.setScale(2, BigDecimal.ROUND_CEILING));
				ticketDetailsDTO.setTicketExtra(ticketExtraDTO);
			}
		}
		catch (Exception e) {
			System.out.println("Block NTRA Error:" + ticketDTO.getCode());
			e.printStackTrace();
		}
	}

	private void applyScheduleStationPointContact(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			Map<Integer, String> stationPointMap = new HashMap<Integer, String>();
			Map<Integer, String> stationPointAddressMap = new HashMap<Integer, String>();
			Map<Integer, String> stationMap = new HashMap<Integer, String>();

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(ticketDTO.getTripDTO().getCode());
			tripService.getTrip(authDTO, tripDTO);

			ScheduleDTO scheduleDTO = scheduleService.getSchedule(authDTO, tripDTO.getSchedule());

			List<ScheduleStationDTO> scheduleStationList = scheduleStationService.getScheduleStation(authDTO, scheduleDTO);
			for (ScheduleStationDTO scheduleStation : scheduleStationList) {
				stationMap.put(scheduleStation.getStation().getId(), scheduleStation.getMobileNumber());
			}

			List<ScheduleStationPointDTO> scheduleStationPointList = scheduleStationPointService.getScheduleStationPoint(authDTO, scheduleDTO);
			for (ScheduleStationPointDTO scheduleStationPoint : scheduleStationPointList) {
				stationPointMap.put(scheduleStationPoint.getStationPoint().getId(), scheduleStationPoint.getMobileNumber());
				stationPointAddressMap.put(scheduleStationPoint.getStationPoint().getId(), scheduleStationPoint.getAddress());
			}

			String fromStationMobile = stationMap.get(ticketDTO.getFromStation().getId());
			String toStationMobile = stationMap.get(ticketDTO.getToStation().getId());

			String boardingMobile = stationPointMap.get(ticketDTO.getBoardingPoint().getId());
			String droppingMobile = stationPointMap.get(ticketDTO.getDroppingPoint().getId());

			String boardingAddress = stationPointAddressMap.get(ticketDTO.getBoardingPoint().getId());
			String droppingAddress = stationPointAddressMap.get(ticketDTO.getDroppingPoint().getId());

			if (StringUtil.isNotNull(boardingAddress)) {
				ticketDTO.getBoardingPoint().setLandmark(Text.NA);
				ticketDTO.getBoardingPoint().setNumber(Text.NA);
				ticketDTO.getBoardingPoint().setAddress(boardingAddress);
			}
			if (StringUtil.isNotNull(droppingAddress)) {
				ticketDTO.getDroppingPoint().setLandmark(Text.NA);
				ticketDTO.getDroppingPoint().setNumber(Text.NA);
				ticketDTO.getDroppingPoint().setAddress(droppingAddress);
			}

			if (StringUtil.isNotNull(fromStationMobile) && StringUtil.isNotNull(boardingMobile)) {
				ticketDTO.getBoardingPoint().setNumber(boardingMobile + " / " + fromStationMobile);
			}
			else if (StringUtil.isNotNull(fromStationMobile) || StringUtil.isNotNull(boardingMobile)) {
				ticketDTO.getBoardingPoint().setNumber((StringUtil.isNotNull(boardingMobile) ? boardingMobile : ticketDTO.getBoardingPoint().getNumber()) + (StringUtil.isNotNull(fromStationMobile) ? " / " + fromStationMobile : ""));
			}

			if (StringUtil.isNotNull(toStationMobile) && StringUtil.isNotNull(droppingMobile)) {
				ticketDTO.getDroppingPoint().setNumber(droppingMobile + " / " + toStationMobile);
			}
			else if (StringUtil.isNotNull(toStationMobile) || StringUtil.isNotNull(droppingMobile)) {
				ticketDTO.getDroppingPoint().setNumber((StringUtil.isNotNull(droppingMobile) ? droppingMobile : ticketDTO.getDroppingPoint().getNumber()) + (StringUtil.isNotNull(toStationMobile) ? " / " + toStationMobile : ""));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}
}
