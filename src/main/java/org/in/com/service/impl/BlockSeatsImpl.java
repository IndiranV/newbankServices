package org.in.com.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.text.WordUtils;
import org.in.com.aggregator.backup.DRService;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.sms.SMSService;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.TicketCache;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AddonsDiscountOfflineDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.CommissionDTO;
import org.in.com.dto.DiscountCriteriaDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.OrderInitRequestDTO;
import org.in.com.dto.PaymentGatewayScheduleDTO;
import org.in.com.dto.ScheduleBookGenderRestrictionDTO;
import org.in.com.dto.ScheduleDiscountDTO;
import org.in.com.dto.ScheduleSeatAutoReleaseDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TicketPhoneBookControlDTO;
import org.in.com.dto.TicketTaxDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.BusCategoryTypeEM;
import org.in.com.dto.enumeration.CommissionTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.EventNotificationEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.NamespaceZoneEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PNRGenerateTypeEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.ReleaseModeEM;
import org.in.com.dto.enumeration.ReleaseTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatGenderRestrictionEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.SlabModeEM;
import org.in.com.dto.enumeration.StateEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.dto.enumeration.WalletAccessEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AddonsDiscountOfflineService;
import org.in.com.service.BlockSeatsHelperService;
import org.in.com.service.BlockSeatsService;
import org.in.com.service.BusmapService;
import org.in.com.service.CancellationTermsService;
import org.in.com.service.CommissionService;
import org.in.com.service.DiscountService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.PaymentMerchantGatewayScheduleService;
import org.in.com.service.ScheduleBookGenderRestrictionService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.ScheduleFareOverrideService;
import org.in.com.service.ScheduleTicketTransferTermsService;
import org.in.com.service.StateService;
import org.in.com.service.TicketHelperService;
import org.in.com.service.TicketPhoneBookControlService;
import org.in.com.service.TicketService;
import org.in.com.service.TicketTaxService;
import org.in.com.service.TransactionOTPService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.service.UserWalletService;
import org.in.com.service.UtilService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.GSTINValidator;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

@Service
public class BlockSeatsImpl extends CacheCentral implements BlockSeatsService {

	@Autowired
	CancellationTermsService cancellationTermsService;
	@Autowired
	UserService userService;
	@Autowired
	TicketService ticketService;
	@Autowired
	TripService tripService;
	@Autowired
	BusmapService busmapService;
	@Autowired
	CommissionService commissionService;
	@Autowired
	ScheduleFareOverrideService fareOverrideService;
	@Autowired
	DiscountService discountService;
	@Autowired
	DRService drService;
	@Autowired
	SMSService smsService;
	@Autowired
	EmailService emailService;
	@Autowired
	TicketPhoneBookControlService bookControlService;
	@Autowired
	UtilService utilService;
	@Autowired
	BlockSeatsHelperService blockSeatsHelperService;
	@Autowired
	AddonsDiscountOfflineService discountOfflineService;
	@Autowired
	TicketHelperService ticketHelperService;
	@Autowired
	TransactionOTPService transactionOTPService;
	@Autowired
	TicketTaxService ticketTaxService;
	@Autowired
	UserWalletService walletService;
	@Autowired
	ScheduleBookGenderRestrictionService genderRestrictionService;
	@Autowired
	ScheduleDynamicStageFareService dynamicFareService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	ScheduleTicketTransferTermsService scheduleTicketTransferTermsService;
	@Lazy
	@Autowired
	NotificationService notificationService;
	@Autowired
	PaymentMerchantGatewayScheduleService paymentMerchantGatewayScheduleService;
	@Autowired
	StateService stateService;

	private void recalculateServiceTaxAmount(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO, TripDTO tripDTO) {
		for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
			BigDecimal addonAmount = ticketDTO.getAddonsValue(ticketDetailsDTO);
			ticketDetailsDTO.setAcBusTax((ticketDetailsDTO.getSeatFare().subtract(addonAmount)).divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(tripDTO.getSchedule().getTax().getServiceTax()));
		}
	}

	private void calculateStationPointAdditionalAmount(AuthDTO authDTO, TicketDTO ticketDTO, TripDTO tripDTO, BigDecimal boardingDropAdditionalFare) {
		for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
			ticketDetailsDTO.setSeatFare(ticketDetailsDTO.getSeatFare().add(boardingDropAdditionalFare));
			ticketDetailsDTO.setAcBusTax(ticketDetailsDTO.getSeatFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(tripDTO.getSchedule().getTax().getServiceTax()));
		}
	}

	private void validatePhoneBookTicket(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO, TripDTO tripDTO) {
		if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
			// Apply Phone booking Terms in time
			TicketPhoneBookControlDTO bookTimeControlDTO = bookControlService.getActiveTimeControl(authDTO, tripDTO.getTripDate());
			DateTime now = DateUtil.NOW();
			DateTime tripDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripDTO.getStage().getFromStation().getMinitues());
			if (bookTimeControlDTO != null) {
				int bookMinutes = DateUtil.getMinutiesDifferent(now, tripDateTime);
				if (bookMinutes < 0 || bookMinutes > bookTimeControlDTO.getAllowMinutes()) {
					throw new ServiceException(ErrorCode.PHONE_BOOK_TICKET_NOT_ALLOW, "Phone booking not yet opened for this service");
				}
				if (bookTimeControlDTO.getBlockMinutesType().getId() == MinutesTypeEM.MINUTES.getId()) {
					int blockMinutes = DateUtil.getMinutiesDifferent(now, tripDateTime);

					if (blockMinutes < 0 || blockMinutes < bookTimeControlDTO.getBlockMinutes()) {
						throw new ServiceException(ErrorCode.PHONE_BOOK_TICKET_NOT_ALLOW, "Phone booking closed for this service");
					}
				}
				else if (bookTimeControlDTO.getBlockMinutesType().getId() == MinutesTypeEM.AM.getId()) {
					DateTime checkTime = DateUtil.addMinituesToDate(tripDateTime, bookTimeControlDTO.getBlockMinutes());
					Integer check = DateUtil.getMinutiesDifferent(now, checkTime);
					if (check >= 0) {
						throw new ServiceException(ErrorCode.PHONE_BOOK_TICKET_NOT_ALLOW, "Phone booking closed for this service");
					}
				}
				else if (bookTimeControlDTO.getBlockMinutesType().getId() == MinutesTypeEM.PM.getId()) {
					DateTime checkTime = DateUtil.addMinituesToDate(tripDateTime, 720 + bookTimeControlDTO.getBlockMinutes());
					Integer check = DateUtil.getMinutiesDifferent(now, checkTime);
					if (check < 0) {
						throw new ServiceException(ErrorCode.PHONE_BOOK_TICKET_NOT_ALLOW, "Phone booking closed for this service");
					}
				}
			}

			// Apply Seat auto release validation
			for (Iterator<ScheduleSeatAutoReleaseDTO> iterator = tripDTO.getSchedule().getSeatAutoReleaseList().iterator(); iterator.hasNext();) {
				ScheduleSeatAutoReleaseDTO seatAutoRelease = iterator.next();
				if (seatAutoRelease.getReleaseTypeEM().getId() != ReleaseTypeEM.RELEASE_PHONE.getId() && seatAutoRelease.getReleaseTypeEM().getId() != ReleaseTypeEM.CONFIRM_PHONE.getId()) {
					iterator.remove();
					continue;
				}
				else if (seatAutoRelease.getGroups() != null && !seatAutoRelease.getGroups().isEmpty() && BitsUtil.isGroupExists(seatAutoRelease.getGroups(), authDTO.getGroup()) == null) {
					iterator.remove();
					continue;
				}

				if (seatAutoRelease.getMinutesTypeEM().getId() == MinutesTypeEM.MINUTES.getId()) {
					int minutiesDiff = seatAutoRelease.getReleaseModeEM().getId() == ReleaseModeEM.RELEASE_SCHEDULE.getId() ? DateUtil.getMinutiesDifferent(now, tripDateTime) : DateUtil.getMinutiesDifferent(now, tripDateTime);
					if (minutiesDiff > seatAutoRelease.getReleaseMinutes()) {
						iterator.remove();
						continue;
					}
				}
				else if (seatAutoRelease.getMinutesTypeEM().getId() == MinutesTypeEM.AM.getId()) {
					int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), seatAutoRelease.getReleaseMinutes()));
					if (minutiesDiff > 0) {
						iterator.remove();
						continue;
					}
				}
				else if (seatAutoRelease.getMinutesTypeEM().getId() == MinutesTypeEM.PM.getId()) {
					int minutiesDiff = DateUtil.getMinutiesDifferent(now, DateUtil.addMinituesToDate(tripDTO.getTripDate(), 720 + seatAutoRelease.getReleaseMinutes()));
					if (minutiesDiff > 0) {
						iterator.remove();
						continue;
					}
				}
			}
		}
	}

	private void validateBookingLimit(AuthDTO authDTO, TicketDTO ticketDTO, TripDTO tripDTO) {
		// Apply booking Limit
		TicketPhoneBookControlDTO bookLimitControlDTO = bookControlService.getActiveLimitControl(authDTO, tripDTO.getSchedule(), ticketDTO, tripDTO.getTripDate());
		if (bookLimitControlDTO != null) {
			List<TripDTO> seatCountlist = tripService.getTripWiseBookedSeatCountV2(authDTO, authDTO.getUser(), bookLimitControlDTO.getActiveFromDate(), bookLimitControlDTO.getActiveToDate(), bookLimitControlDTO.getTicketStatus());
			int seatCount = 0, seatAmount = 0;
			for (TripDTO tripSeatCountDTO : seatCountlist) {
				if (tripSeatCountDTO.getTripDate().gteq(bookLimitControlDTO.getActiveFromDate()) && tripSeatCountDTO.getTripDate().lteq(bookLimitControlDTO.getActiveToDate())) {
					if (bookLimitControlDTO.getRespectiveFlag() == Numeric.ONE_INT && !tripSeatCountDTO.getCode().equals(tripDTO.getCode())) {
						continue;
					}

					seatCount += tripSeatCountDTO.getBookedSeatCount();
					seatAmount += tripSeatCountDTO.getId();
				}
			}

			TicketStatusEM ticketStatusEM = ticketDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() ? TicketStatusEM.CONFIRM_BOOKED_TICKETS : ticketDTO.getTicketStatus();

			if (bookLimitControlDTO.getSlabMode().getId() == SlabModeEM.COUNT.getId() && seatCount + ticketDTO.getTicketDetails().size() > bookLimitControlDTO.getMaxSlabValueLimit() && ticketStatusEM.getId() == bookLimitControlDTO.getTicketStatus().getId()) {
				throw new ServiceException(ErrorCode.BOOK_TICKET_NOT_ALLOW_LIMITS, "Your booking limit reached for this travel date, " + seatCount + " seats Booked");
			}
			else if (bookLimitControlDTO.getSlabMode().getId() == SlabModeEM.AMOUNT.getId() && seatAmount + ticketDTO.getTotalFare().intValue() > bookLimitControlDTO.getMaxSlabValueLimit() && ticketStatusEM.getId() == bookLimitControlDTO.getTicketStatus().getId()) {
				throw new ServiceException(ErrorCode.BOOK_TICKET_NOT_ALLOW_LIMITS, "Your booking limit reached for this travel date, Booked amount Rs." + seatAmount);
			}
		}
	}

	public BookingDTO blockSeatsV3(AuthDTO authDTO, BookingDTO bookingDTO) {
		// Respective Login user's only able to book ticket
		if (!authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}

		bookingDTO.setNamespace(authDTO.getNamespace());
		// OTP Validations on Transaction
		if (bookingDTO.getAggregate().get(MenuEventEM.BOOKING_ON_OTP.getOperationCode()) != null) {
			boolean isValid = transactionOTPService.validateOTP(authDTO, authDTO.getUser().getCode(), authDTO.getUser().getMobile(), StringUtil.getIntegerValue(bookingDTO.getAggregate().get(MenuEventEM.BOOKING_ON_OTP.getOperationCode())));
			if (!isValid) {
				throw new ServiceException(ErrorCode.INVAILD_TRANSACTION_OTP);
			}
		}

		PaymentGatewayScheduleDTO gatewayScheduleDTO = null;
		if (StringUtil.isNotNull(bookingDTO.getPaymentGatewayPartnerCode())) {
			gatewayScheduleDTO = getPaymentGatewaySchedule(authDTO, bookingDTO);
		}

		for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {

			TripDTO tripDTO = getTripDetailsV3(authDTO, bookingDTO, ticketDTO);

			// Validate trip has been, or booking after Trip Time
			validateTrip(authDTO, bookingDTO, tripDTO);

			// Validate phone booking control
			validatePhoneBookTicket(authDTO, bookingDTO, ticketDTO, tripDTO);

			checkFreshRequest(authDTO, tripDTO, ticketDTO);

			applyBookGenderRestriction(authDTO, ticketDTO, tripDTO);

			BigDecimal stationPointAdditionalFare = BigDecimal.ZERO;
			for (StationPointDTO stationPointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
				if (ticketDTO.getBoardingPoint().getCode().equals(stationPointDTO.getCode())) {
					ticketDTO.setBoardingPoint(stationPointDTO);
					ticketDTO.getBoardingPoint().setMinitues(tripDTO.getStage().getFromStation().getMinitues() + ticketDTO.getBoardingPoint().getMinitues());
					int reportingMin = ticketDTO.getBoardingPoint().getMinitues() - authDTO.getNamespace().getProfile().getBoardingReportingMinitues();
					ticketDTO.setTravelMinutes(tripDTO.getStage().getFromStation().getMinitues());
					ticketDTO.setReportingMinutes(reportingMin);
					stationPointAdditionalFare = stationPointAdditionalFare.add(stationPointDTO.getFare());
					break;
				}
			}
			if (StringUtil.isNotNull(ticketDTO.getDroppingPoint().getCode())) {
				for (StationPointDTO stationPointDTO : tripDTO.getStage().getToStation().getStationPoint()) {
					if (ticketDTO.getDroppingPoint().getCode().equals(stationPointDTO.getCode())) {
						ticketDTO.setDroppingPoint(stationPointDTO);
						ticketDTO.getDroppingPoint().setMinitues(tripDTO.getStage().getToStation().getMinitues() + ticketDTO.getDroppingPoint().getMinitues());
						stationPointAdditionalFare = stationPointAdditionalFare.add(stationPointDTO.getFare());
						break;
					}
				}
			}
			// Get last Station point
			else if (tripDTO.getStage().getToStation().getStationPoint() != null && !tripDTO.getStage().getToStation().getStationPoint().isEmpty()) {
				ticketDTO.setDroppingPoint(tripDTO.getStage().getToStation().getStationPoint().get(tripDTO.getStage().getToStation().getStationPoint().size() - 1));
				ticketDTO.getDroppingPoint().setMinitues(tripDTO.getStage().getToStation().getMinitues() + ticketDTO.getDroppingPoint().getMinitues());
				stationPointAdditionalFare = stationPointAdditionalFare.add(ticketDTO.getDroppingPoint().getFare());

			}
			if (ticketDTO.getBoardingPoint().getId() == 0 || ticketDTO.getDroppingPoint().getId() == 0) {
				throw new ServiceException(ErrorCode.STATION_POINT_CODE_INVALID);
			}

			// User DTO for Offline booking
			UserDTO userDTO = null;
			if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId() && StringUtil.isNotNull(bookingDTO.getOfflineUserCode())) {
				userDTO = new UserDTO();
				userDTO.setCode(bookingDTO.getOfflineUserCode());
				// get from cache
				getUserDTO(authDTO, userDTO);
			}
			if (userDTO == null) {
				userDTO = authDTO.getUser();
			}
			CommissionDTO commissionDTO = null;
			if (UserRoleEM.USER_ROLE.getId() == userDTO.getUserRole().getId()) {
				// Get Booking Commission
				commissionDTO = commissionService.getCommission(authDTO, userDTO, CommissionTypeEM.TICKETS_BOOKING);
			}
			// get and check your balance
			if (StringUtil.isNull(bookingDTO.getPaymentGatewayPartnerCode()) && userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
				if (userDTO.getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId()) {
					BigDecimal availableBalance = userService.getCurrentCreditBalace(authDTO, userDTO);
					authDTO.setCurrnetBalance(availableBalance);
					if (commissionDTO != null) {
						availableBalance = availableBalance.add(commissionDTO.getCreditlimit());
					}
					if (availableBalance.compareTo(ticketDTO.getTotalFare()) == -1) {
						bookingDTO.setPaymentGatewayProcessFlag(true);
					}
					// if (authDTO.getDeviceMedium().getId() ==
					// DeviceMediumEM.API_USER.getId() &&
					// availableBalance.compareTo(Numeric.API_USER_LOW_BALANCE)
					// == -1) {
					// emailService.sendLowBalanceEmail(authDTO,
					// availableBalance);
					// }

					BigDecimal transactionAmount = bookingDTO.getTransactionAmount().add((stationPointAdditionalFare.multiply(BigDecimal.valueOf(bookingDTO.getTicketSeatCount()))));
					if (authDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId() && availableBalance.compareTo(transactionAmount) == -1) {
						throw new ServiceException(ErrorCode.LOW_AVAILABLE_BALANCE);
					}
				}
				else if (commissionDTO == null || (userDTO.getPaymentType().getId() == PaymentTypeEM.PAYMENT_UNLIMITED.getId() && commissionDTO.getCreditlimit().compareTo(BigDecimal.ONE.negate()) != 0)) {
					throw new ServiceException(ErrorCode.PAYMENT_DECLINED, " Unlimited Payment Type credit limit should be -1");
				}
			}
			if (StringUtil.isNull(bookingDTO.getPaymentGatewayPartnerCode()) && authDTO.getUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
				throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
			}

			// Calculate Boarding & Drop Additional Fare
			if (stationPointAdditionalFare.compareTo(BigDecimal.ZERO) == Numeric.ONE_INT) {
				calculateStationPointAdditionalAmount(authDTO, ticketDTO, tripDTO, stationPointAdditionalFare);
			}
			// Coupon Discount Addons
			if (StringUtil.isNotNull(bookingDTO.getCouponCode())) {
				addonDiscountCoupon(authDTO, bookingDTO, ticketDTO, tripDTO);
			}
			// Wallet
			if (WalletAccessEM.getWalletAccessEM(authDTO.getNamespaceCode()) != null && (StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.WALLET_REDREEM)) || StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.WALLET_COUPON_CODE))) && ticketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId()) {
				addonWalletCouponRedeem(authDTO, bookingDTO, ticketDTO);
			}
			// Additional Charge Extra Luggage & Extra ticket (Child)
			if ((StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.EXTRA_TICKET)) || StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.LUGGAGE))) && ticketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId()) {
				addonAdditionalCharge(authDTO, bookingDTO, ticketDTO);
			}
			// Discount Manual Amount Addons
			if ((userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId() || userDTO.getUserRole().getId() == UserRoleEM.TABLET_POB_ROLE.getId() || userDTO.getUserRole().getId() == UserRoleEM.DRIVER.getId()) && StringUtil.isNotNull(bookingDTO.getManualDiscountAmount())) {
				addonDiscountAmount(authDTO, bookingDTO, ticketDTO, tripDTO);
			}
			// Go Green Charge
			if (bookingDTO.getGoGreenAmount().compareTo(BigDecimal.ZERO) != 0) {
				addonGoGreenCharge(authDTO, bookingDTO, ticketDTO);
			}
			/** Customer Id Proof */
			customerIdProof(authDTO, bookingDTO, ticketDTO);

			// Offline Discount Addons
			if (userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && StringUtil.isNotNull(bookingDTO.getOfflineDiscountCode())) {
				addonOfflineDiscountApply(authDTO, bookingDTO, ticketDTO, tripDTO);
			}
			// Re-calculate Service Tax - GST for Discount
			recalculateServiceTaxAmount(authDTO, bookingDTO, ticketDTO, tripDTO);

			/** Schedule Ticket Transfer Terms */
			ScheduleTicketTransferTermsDTO scheduleTicketTransferTerms = scheduleTicketTransferTermsService.getScheduleTicketTransferTermsBySchedule(authDTO, tripDTO.getSchedule(), tripDTO.getSearch().getFromStation(), tripDTO.getSearch().getToStation());
			ticketDTO.setScheduleTicketTransferTerms(scheduleTicketTransferTerms);

			if (userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && bookingDTO.isFreeServiceFlag() && authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId()) {
				freeServiceTicketAmount(authDTO, bookingDTO, ticketDTO, tripDTO);
			}
			// Agent Service Charge
			if (StringUtil.isNotNull(bookingDTO.getAgentServiceCharge())) {
				addonAgentServiceCharge(authDTO, bookingDTO, ticketDTO, tripDTO);
			}
			// Identify Ticket Extras, Note: related to Onward Ticket
			if (ticketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId()) {
				TicketExtraDTO extraDTO = processTicketExtraDetails(authDTO, bookingDTO, ticketDTO);
				ticketDTO.setTicketExtra(extraDTO);
			}
			// Get Cancellation for schedule/group/User
			CancellationTermDTO cancellationTermDTO = cancellationTermsService.getCancellationTermsByTripDTO(authDTO, userDTO, tripDTO);
			if (cancellationTermDTO == null) {
				throw new ServiceException(ErrorCode.CANCELLATION_TERMS_NOT_FOUND);
			}

			cancellationTermsService.getCancellationTermGroupIdByGroupKey(authDTO, cancellationTermDTO);
			ticketDTO.setCancellationTerm(cancellationTermDTO);
			ticketDTO.setTicketUser(userDTO);

			// Find forUserId
			UserDTO forUser = new UserDTO();
			if (authDTO.getUserCustomer() != null && authDTO.getUserCustomer().getId() != 0) {
				forUser.setId(authDTO.getUserCustomer().getId());
			}
			else if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId() && StringUtil.isNotNull(bookingDTO.getOfflineUserCode()) && userDTO.getId() != authDTO.getUser().getId()) {
				forUser = authDTO.getUser();
			}
			ticketDTO.setTicketForUser(forUser);

			// Binding from to stations and station points
			ticketDTO.setFromStation(tripDTO.getStage().getFromStation().getStation());
			ticketDTO.setToStation(tripDTO.getStage().getToStation().getStation());
			ticketDTO.setTripDate(tripDTO.getTripDate());
			ticketDTO.setTripDTO(tripDTO);

			// Validate booking limit
			validateBookingLimit(authDTO, ticketDTO, tripDTO);

			// ticketDTO.setTripDate(tripDTO.getTripDate());

			boolean seatDouplicateEntry = ticketService.checkSeatDuplicateEntryV2(authDTO, ticketDTO);
			if (seatDouplicateEntry) {
				throw new ServiceException(ErrorCode.DOUBLE_ENTRY_VALIDATION_FAIL);
			}
			if (StringUtil.isNotNull(ticketDTO.getCode())) {
				boolean status = ticketService.checkDuplicateTicketCodeEntry(authDTO, ticketDTO.getCode());
				if (status) {
					throw new ServiceException(ErrorCode.DOUPLICATE_ENTRY_VALIDATION_FAIL);
				}
			}
			// Generate ticket code
			if (StringUtil.isNull(ticketDTO.getCode()) && authDTO.getNamespace().getProfile().getPnrGenerateType().getId() == PNRGenerateTypeEM.ENCODE_ALPHANUMERIC.getId()) {
				String PNRstartCode = StringUtil.isNotNull(ticketDTO.getPnrStartCode()) ? StringUtil.removeSymbol(ticketDTO.getPnrStartCode()).length() == 3 ? ticketDTO.getPnrStartCode() : authDTO.getNamespace().getProfile().getPnrStartCode() : authDTO.getNamespace().getProfile().getPnrStartCode();
				if (bookingDTO.isRoundTripFlag()) {
					// R--Round Trip PNR, S-Supporting PNR
					PNRstartCode = PNRstartCode + (ticketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId() ? "R" : "S");
				}
				String ticketCode = ApplicationConfig.getServerZoneCode().equals(NamespaceZoneEM.PARVEEN_BITS.getCode()) ? TokenGenerator.generateCode(PNRstartCode, 15) : TokenGenerator.generateCode(PNRstartCode);
				ticketDTO.setCode(ticketCode);
			}
			else if (StringUtil.isNull(ticketDTO.getCode()) && authDTO.getNamespace().getProfile().getPnrGenerateType().getId() == PNRGenerateTypeEM.SEQUENCE_NUMBERIC.getId()) {
				String ticketCode = utilService.getGenerateSequenceNumber(authDTO);
				if (bookingDTO.isRoundTripFlag()) {
					// R--Round Trip PNR, S-Supporting PNR
					ticketCode = ticketCode.substring(0, authDTO.getNamespace().getProfile().getPnrStartCode().length()) + (ticketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId() ? "R" : "S") + ticketCode.substring(3, ticketCode.length());
				}
				ticketDTO.setCode(ticketCode);

				boolean status = ticketService.checkDuplicateTicketCodeEntry(authDTO, ticketDTO.getCode());
				if (status) {
					throw new ServiceException(ErrorCode.DOUPLICATE_ENTRY_VALIDATION_FAIL);
				}
			}
			// booking code and ticket code will be same if single trip
			if (StringUtil.isNull(bookingDTO.getCode())) {
				bookingDTO.setCode(ticketDTO.getCode());
			}
			// Remarks
			if (userDTO.getUserRole().getId() == UserRoleEM.TABLET_POB_ROLE.getId() || userDTO.getUserRole().getId() == UserRoleEM.DRIVER.getId()) {
				ticketDTO.setRemarks(StringUtil.composeRemarks(ticketDTO.getRemarks(), " BB:" + userDTO.getName(), 120));
			}

			// Apply Agent Commission
			BigDecimal creditCommissionAmount = BigDecimal.ZERO;
			if (userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && commissionDTO != null && commissionDTO.getCommissionValue().compareTo(BigDecimal.ZERO) > 0) {
				creditCommissionAmount = applyCommission(authDTO, commissionDTO, bookingDTO, ticketDTO, userDTO);
			}

			// Calculate Net Revenue Amount
			calculateNetRevenueBookAmount(authDTO, creditCommissionAmount, commissionDTO, ticketDTO);

			/** Calculate PG Service Charge */
			if (gatewayScheduleDTO != null && gatewayScheduleDTO.getServiceCharge().compareTo(BigDecimal.ZERO) > 0) {
				calculatePaymentGatewayCharge(bookingDTO, ticketDTO, gatewayScheduleDTO);
			}
			TicketTaxDTO ticketTax = null;
			if (ticketDTO.getTripDTO().getSchedule().getTax() != null && ticketDTO.getTripDTO().getSchedule().getTax().getId() != 0 && StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.GST_IN)) && StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.GST_TRADE_NAME))) {
				ticketTax = composeNamespaceTaxFromCustomerGstin(authDTO, bookingDTO, ticketDTO);
			}

			blockSeatsHelperService.blockSeats(authDTO, bookingDTO, tripDTO, ticketDTO);

			if (StringUtil.isNull(ticketDTO.getCode())) {
				throw new ServiceException(ErrorCode.UNABLE_TO_BLOCK_TICKET);
			}

			// Store Customer Ticket Tax Details
			storeCustomerTicketTaxDetails(authDTO, ticketTax, ticketDTO);

			// Upload to Backup Server
			if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				// drService.flushTicketDetails(authDTO, ticketDTO);
				ticketService.generateLinkPay(authDTO, ticketDTO);
				smsService.sendPhoneBooking(authDTO, ticketDTO);
				ticketHelperService.processTicketAfterTripTime(authDTO, bookingDTO);

				// Send Ticket Event
				if (StringUtil.isContains(Constants.TICKET_EVENT, authDTO.getNamespaceCode())) {
					smsService.sendTicketEventSMS(authDTO, ticketDTO, "New Phone Block Ticket");
				}
				// send Ticket Event To Customer
				notificationService.sendCustomerTicketEvent(authDTO, ticketDTO, EventNotificationEM.PHONE_TICKET_BOOKING);
				// Dynamic Fare
				if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
					dynamicFareService.updateTicketStatus(authDTO, ticketDTO);
				}
			}

			// push fcm notification
			if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.TICKET_BLOCK)) {
				notificationPushService.pushTicketBlockConfirmNotification(authDTO, ticketDTO, NotificationSubscriptionTypeEM.TICKET_BLOCK);
			}
		}
		if (bookingDTO.getId() != 0) {
			TicketCache ticketCache = new TicketCache();
			ticketCache.putBookingDTO(authDTO, bookingDTO);
			ticketCache.putTicketEvent(authDTO, bookingDTO);
		}
		// Round Trip booking lookup update
		if (bookingDTO.getTicketList().size() > 1) {
			ticketService.updateTicketLookup(authDTO, bookingDTO);
		}
		return bookingDTO;
	}

	private BigDecimal applyCommission(AuthDTO authDTO, CommissionDTO commissionDTO, BookingDTO bookingDTO, TicketDTO ticketDTO, UserDTO userDTO) {
		BigDecimal creditCommissionAmount = BigDecimal.ZERO;
		BigDecimal creditExtraCommissionAmount = BigDecimal.ZERO;

		if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
			creditCommissionAmount = ticketDTO.getTotalSeatFare().subtract(ticketDTO.getAddonsValue()).multiply(commissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2);
		}
		else if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
			creditCommissionAmount = commissionDTO.getCommissionValue().multiply(new BigDecimal(ticketDTO.getTicketDetails().size()));
		}

		// Extra Commission Processes
		CommissionDTO extraCommissionDTO = commissionService.getBookingExtraCommission(authDTO, userDTO, commissionDTO, ticketDTO);
		if (extraCommissionDTO != null && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
			creditExtraCommissionAmount = ticketDTO.getTotalSeatFare().subtract(ticketDTO.getAddonsValue()).multiply(extraCommissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2);
		}
		else if (extraCommissionDTO != null && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
			creditExtraCommissionAmount = extraCommissionDTO.getCommissionValue().multiply(new BigDecimal(ticketDTO.getTicketDetails().size()));
		}

		// Override the Base commission if set in Extra
		// Commission
		if (extraCommissionDTO != null && extraCommissionDTO.getActiveFlag() == -1) {
			creditCommissionAmount = BigDecimal.ZERO;
		}
		creditCommissionAmount = creditCommissionAmount.add(creditExtraCommissionAmount);

		if (StringUtil.isNotNull(bookingDTO.getPaymentGatewayPartnerCode()) && creditCommissionAmount.compareTo(BigDecimal.ZERO) > 0) {
			BigDecimal seatDiscountAmount = creditCommissionAmount.divide(new BigDecimal(bookingDTO.getTicketSeatCount()), 2, RoundingMode.CEILING);

			// Schedule Discount
			List<TicketAddonsDetailsDTO> discountList = new ArrayList<>();
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				TicketAddonsDetailsDTO discountDetailsDTO = new TicketAddonsDetailsDTO();
				discountDetailsDTO.setValue(seatDiscountAmount);
				discountDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
				discountDetailsDTO.setAddonsType(AddonsTypeEM.DISCOUNT_AMOUNT);
				discountDetailsDTO.setActiveFlag(1);
				discountDetailsDTO.setRefferenceId(bookingDTO.getTicketSeatCount());
				discountDetailsDTO.setRefferenceCode(Text.NA);
				discountList.add(discountDetailsDTO);
			}
			ticketDTO.getTicketAddonsDetails().addAll(discountList);
		}
		return creditCommissionAmount;
	}

	private void calculateNetRevenueBookAmount(AuthDTO authDTO, BigDecimal creditCommissionAmount, CommissionDTO commissionDTO, TicketDTO ticketDTO) {
		try {
			BigDecimal seatCommissionAmount = BigDecimal.ZERO;
			BigDecimal serviceTaxAmount = BigDecimal.ZERO;
			if (creditCommissionAmount.compareTo(BigDecimal.ZERO) > 0) {
				seatCommissionAmount = creditCommissionAmount.divide(BigDecimal.valueOf(ticketDTO.getTicketDetails().size()), 2, RoundingMode.CEILING);
				serviceTaxAmount = seatCommissionAmount.multiply(commissionDTO.getServiceTax().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING));
			}
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				BigDecimal addonsAmount = ticketDTO.getAddonsValue(ticketDetailsDTO);
				BigDecimal netRevenueAmount = ticketDetailsDTO.getSeatFare().add(ticketDetailsDTO.getAcBusTax()).subtract(seatCommissionAmount).subtract(serviceTaxAmount).subtract(addonsAmount);
				ticketDetailsDTO.setNetAmount(netRevenueAmount.setScale(2, BigDecimal.ROUND_CEILING));
			}
		}
		catch (Exception e) {
			System.out.println("Block NTRA Error:" + ticketDTO.getCode());
			e.printStackTrace();
		}
	}

	private void addonGoGreenCharge(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO) {
		TicketDetailsDTO ticketDetailsDTO = Iterables.getFirst(ticketDTO.getTicketDetails(), null);

		// Go Green
		if (ticketDetailsDTO != null) {
			List<TicketAddonsDetailsDTO> discountList = new ArrayList<>();
			TicketAddonsDetailsDTO goGreenDetailsDTO = new TicketAddonsDetailsDTO();
			goGreenDetailsDTO.setValue(bookingDTO.getGoGreenAmount());
			goGreenDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
			goGreenDetailsDTO.setAddonsType(AddonsTypeEM.GO_GREEN);
			goGreenDetailsDTO.setActiveFlag(1);
			goGreenDetailsDTO.setRefferenceId(bookingDTO.getTicketSeatCount());
			discountList.add(goGreenDetailsDTO);
			ticketDTO.getTicketAddonsDetails().addAll(discountList);
		}
	}

	private void customerIdProof(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO) {
		List<TicketAddonsDetailsDTO> discountList = new ArrayList<>();
		for (TicketDetailsDTO ticketDetails : ticketDTO.getTicketDetails()) {
			if (StringUtil.isNull(ticketDetails.getIdProof())) {
				continue;
			}

			TicketAddonsDetailsDTO addonsDetails = new TicketAddonsDetailsDTO();
			addonsDetails.setValue(BigDecimal.ZERO);
			addonsDetails.setSeatCode(ticketDetails.getSeatCode());
			addonsDetails.setAddonsType(AddonsTypeEM.CUSTOMER_ID_PROOF);
			addonsDetails.setActiveFlag(1);
			addonsDetails.setRefferenceId(0);
			addonsDetails.setRefferenceCode(ticketDetails.getIdProof());
			discountList.add(addonsDetails);
		}
		ticketDTO.getTicketAddonsDetails().addAll(discountList);
	}

	public BookingDTO processTicketPayment(AuthDTO authDTO, BookingDTO bookingDTO) {
		// Respective Login user's only able to book ticket
		if (!authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		if (StringUtil.isNull(bookingDTO.getPaymentGatewayPartnerCode())) {
			throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
		}

		bookingDTO.setNamespace(authDTO.getNamespace());

		for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);

			// Trip related stage code
			TripDTO tripDTO = tripService.getTrip(authDTO, ticketDTO.getTripDTO());
			List<String> relatedStageCodes = tripService.getRelatedStageCodes(authDTO, tripDTO, ticketDTO.getFromStation(), ticketDTO.getToStation());
			tripDTO.setReleatedStageCodeList(relatedStageCodes);

			ticketDTO.setTripDTO(tripDTO);
			ticketDTO.setBlockingLiveTime(DateUtil.addMinituesToDate(DateUtil.NOW(), DateUtil.getMinutiesDifferent(DateUtil.NOW(), ticketDTO.getTicketAt()) - authDTO.getNamespace().getProfile().getSeatBlockTime()));
			bookingDTO.setCode(ticketDTO.getBookingCode());

			boolean seatDouplicateEntry = ticketService.checkSeatDuplicateEntryV2(authDTO, ticketDTO);
			if (seatDouplicateEntry) {
				throw new ServiceException(ErrorCode.DOUBLE_ENTRY_VALIDATION_FAIL);
			}

			TicketCache ticketCache = new TicketCache();
			ticketCache.putBookingDTO(authDTO, bookingDTO);
		}
		return bookingDTO;
	}

	private void storeCustomerTicketTaxDetails(AuthDTO authDTO, TicketTaxDTO ticketTax, TicketDTO ticketDTO) {
		if (ticketTax != null && StringUtil.isNotNull(ticketTax.getGstin()) && StringUtil.isNotNull(ticketTax.getTradeName())) {
			ticketTaxService.addTicketTax(authDTO, ticketDTO, ticketTax);
		}
	}

	private void freeServiceTicketAmount(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO, TripDTO tripDTO) {
		// FS Ticket
		if (bookingDTO.isFreeServiceFlag()) {
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				ticketDetailsDTO.setSeatFare(BigDecimal.ZERO);
				ticketDetailsDTO.setAcBusTax(BigDecimal.ZERO);
			}
			ticketDTO.setRemarks(StringUtil.substring(ticketDTO.getRemarks(), 120) + "FS Ticket");
			ticketDTO.getTicketAddonsDetails().clear();
		}
	}

	private void addonDiscountAmount(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO, TripDTO tripDTO) {

		try {
			BigDecimal seatDiscountAmount = bookingDTO.getManualDiscountAmount().divide(new BigDecimal(bookingDTO.getTicketSeatCount()), 2, RoundingMode.CEILING);
			// Schedule Discount
			List<TicketAddonsDetailsDTO> discountList = new ArrayList<>();
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				TicketAddonsDetailsDTO discountDetailsDTO = new TicketAddonsDetailsDTO();
				discountDetailsDTO.setValue(seatDiscountAmount);
				discountDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
				discountDetailsDTO.setAddonsType(AddonsTypeEM.DISCOUNT_AMOUNT);
				discountDetailsDTO.setActiveFlag(1);
				discountDetailsDTO.setRefferenceId(bookingDTO.getTicketSeatCount());
				discountDetailsDTO.setRefferenceCode(Text.NA);
				discountList.add(discountDetailsDTO);
			}
			ticketDTO.setRemarks(StringUtil.substring(ticketDTO.getRemarks(), 90) + " Discount Rs." + bookingDTO.getManualDiscountAmount().toString() + " for " + bookingDTO.getTicketSeatCount() + " seats");
			ticketDTO.getTicketAddonsDetails().addAll(discountList);
		}
		catch (ServiceException e) {
			System.out.println("Transaction addonDiscountCoupon: " + e.getErrorCode().getCode());
		}
		catch (Exception e) {
			System.out.println("Transaction addonDiscountCoupon:");
			e.printStackTrace();
		}

	}

	private void addonOfflineDiscountApply(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO, TripDTO tripDTO) {
		try {
			AddonsDiscountOfflineDTO discountOfflineDTO = new AddonsDiscountOfflineDTO();
			discountOfflineDTO.setCode(bookingDTO.getOfflineDiscountCode());
			AddonsDiscountOfflineDTO discountOffline = discountOfflineService.getOfflineDiscount(authDTO, bookingDTO.getOfflineDiscountCode());
			if (discountOffline == null) {
				throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);
			}
			if (discountOffline.getMinSeatCount() != 0 && ticketDTO.getTicketDetails().size() < discountOffline.getMinSeatCount()) {
				discountOffline.setActiveFlag(0);
			}
			if (discountOffline.getMinTicketFare() != 0 && !(discountOffline.getMinTicketFare() <= ticketDTO.getTotalSeatFare().intValue() / ticketDTO.getTicketDetails().size())) {
				discountOffline.setActiveFlag(0);
			}
			if (discountOffline.getActiveFlag() != 1) {
				throw new ServiceException(ErrorCode.INVALID_DISCOUNT_CODE);

			}
			BigDecimal totalDiscountAmount = BigDecimal.ZERO;
			if (discountOffline.isPercentageFlag()) {
				totalDiscountAmount = ticketDTO.getTotalSeatFare().multiply(discountOffline.getValue()).divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING);
			}
			else {
				totalDiscountAmount = discountOffline.getValue().multiply(new BigDecimal(ticketDTO.getTicketDetails().size()));
			}
			if (discountOffline.getMaxDiscountAmount().compareTo(BigDecimal.ZERO) == 1 && totalDiscountAmount.compareTo(discountOffline.getMaxDiscountAmount()) == 1) {
				totalDiscountAmount = discountOffline.getMaxDiscountAmount();
			}
			BigDecimal seatDiscountAmount = totalDiscountAmount.divide(new BigDecimal(bookingDTO.getTicketSeatCount()), 2, RoundingMode.CEILING);
			// Schedule Discount
			List<TicketAddonsDetailsDTO> discountList = new ArrayList<>();
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				TicketAddonsDetailsDTO discountDetailsDTO = new TicketAddonsDetailsDTO();
				discountDetailsDTO.setValue(seatDiscountAmount);
				discountDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
				discountDetailsDTO.setAddonsType(AddonsTypeEM.OFFLINE_DISCOUNT);
				discountDetailsDTO.setActiveFlag(1);
				discountDetailsDTO.setRefferenceId(bookingDTO.getTicketSeatCount());
				discountDetailsDTO.setRefferenceCode(Text.NA);
				discountList.add(discountDetailsDTO);
			}
			ticketDTO.getTicketAddonsDetails().addAll(discountList);
		}
		catch (ServiceException e) {
			System.out.println("Transaction addon Offline Discount : " + e.getErrorCode().getCode());
		}
		catch (Exception e) {
			System.out.println("Transaction addon Offline Discount");
			e.printStackTrace();
		}

	}

	public static synchronized void checkFreshRequest(AuthDTO authDTO, TripDTO tripDTO, TicketDTO ticketDTO) {
		String key = tripDTO.getCode() + tripDTO.getStage().getCode() + ticketDTO.getSeatCodes();
		if (EhcacheManager.getFreshRequestEhCache().get(key) == null) {
			EhcacheManager.getFreshRequestEhCache().put(new Element(key, 1));
		}
		else {
			throw new ServiceException(ErrorCode.PARALLEL_SAME_TRANSACTION_OCCUR);
		}
	}

	private void addonDiscountCoupon(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO, TripDTO tripDTO) {
		try {
			String referenceCode = bookingDTO.getCouponCode();

			TicketDTO preTicket = null;
			if (Constants.PREVIOUS_PNR_COUPEN.equals(bookingDTO.getCouponCode())) {
				preTicket = discountService.validatePreviousTicketCoupen(authDTO, bookingDTO);
				referenceCode = preTicket.getCode();
			}

			Map<String, DiscountCriteriaDTO> seatGenderCriteria = discountService.applyCouponCode(authDTO, bookingDTO, ticketDTO, preTicket);

			// Coupon Discount
			List<TicketAddonsDetailsDTO> discountList = new ArrayList<>();
			if (!seatGenderCriteria.isEmpty()) {
				BigDecimal discountValue = BigDecimal.ZERO;
				BigDecimal maxDiscountValue = BigDecimal.ZERO;

				for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
					DiscountCriteriaDTO discountCriteriaDTO = seatGenderCriteria.get(ticketDetailsDTO.getSeatGendar().getCode());

					if (StringUtil.isNotNull(discountCriteriaDTO.getAge()) && !BitsUtil.validateAge(discountCriteriaDTO.getAge(), ticketDetailsDTO.getPassengerAge())) {
						continue;
					}

					discountValue = new BigDecimal(discountCriteriaDTO.getValue());
					maxDiscountValue = new BigDecimal(discountCriteriaDTO.getMaxDiscountAmount());

					if (discountCriteriaDTO.isPercentageFlag()) {
						TicketAddonsDetailsDTO discountDetailsDTO = new TicketAddonsDetailsDTO();
						discountDetailsDTO.setValue(ticketDetailsDTO.getSeatFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(discountValue));
						if (maxDiscountValue.compareTo(BigDecimal.ZERO) == 1 && discountDetailsDTO.getValue().compareTo(maxDiscountValue) == 1) {
							discountDetailsDTO.setValue(maxDiscountValue);
						}
						discountDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
						discountDetailsDTO.setAddonsType(AddonsTypeEM.COUPON_DISCOUNT);
						discountDetailsDTO.setActiveFlag(1);
						discountDetailsDTO.setRefferenceId(discountCriteriaDTO.getId());
						discountDetailsDTO.setRefferenceCode(referenceCode);
						discountList.add(discountDetailsDTO);
					}
					else if (!discountCriteriaDTO.isPercentageFlag()) {
						TicketAddonsDetailsDTO discountDetailsDTO = new TicketAddonsDetailsDTO();
						discountDetailsDTO.setValue(discountValue);
						discountDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
						discountDetailsDTO.setAddonsType(AddonsTypeEM.COUPON_DISCOUNT);
						discountDetailsDTO.setActiveFlag(1);
						discountDetailsDTO.setRefferenceId(discountCriteriaDTO.getId());
						discountDetailsDTO.setRefferenceCode(referenceCode);
						discountList.add(discountDetailsDTO);
					}
				}
				ticketDTO.getTicketAddonsDetails().addAll(discountList);
			}
		}
		catch (ServiceException e) {
			System.out.println("Transaction addonDiscountCoupon: " + e.getErrorCode().getCode() + " " + bookingDTO.getCouponCode());
		}
		catch (Exception e) {
			System.out.println("Transaction addonDiscountCoupon:");
			e.printStackTrace();
		}
	}

	private void addonWalletCouponRedeem(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO) {
		try {
			List<TicketAddonsDetailsDTO> discountList = new ArrayList<>();
			TicketDetailsDTO ticketDetailsDTO = ticketDTO.getTicketDetails().get(Numeric.ZERO_INT);

			if (StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.WALLET_REDREEM))) {
				Map<String, String> redeemDetails = walletService.getWalletRedeemDetails(authDTO, bookingDTO.getAdditionalAttributes().get(Text.WALLET_REDREEM));

				if (redeemDetails != null && StringUtil.isNotNull(redeemDetails.get("redeemAmount"))) {
					TicketAddonsDetailsDTO discountDetailsDTO = new TicketAddonsDetailsDTO();
					discountDetailsDTO.setValue(new BigDecimal(redeemDetails.get("redeemAmount")).setScale(0, RoundingMode.HALF_UP));
					discountDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
					discountDetailsDTO.setAddonsType(AddonsTypeEM.WALLET_REDEEM);
					discountDetailsDTO.setActiveFlag(1);
					discountDetailsDTO.setRefferenceId(Numeric.ZERO_INT);
					discountDetailsDTO.setRefferenceCode(bookingDTO.getAdditionalAttributes().get(Text.WALLET_REDREEM));
					discountList.add(discountDetailsDTO);
				}
			}
			if (StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.WALLET_COUPON_CODE))) {
				Map<String, String> redeemDetails = walletService.getWalletRedeemDetails(authDTO, bookingDTO.getAdditionalAttributes().get(Text.WALLET_COUPON_CODE));

				if (redeemDetails != null && StringUtil.isNotNull(redeemDetails.get("discountAmount"))) {
					TicketAddonsDetailsDTO discountDetailsDTO = new TicketAddonsDetailsDTO();
					discountDetailsDTO.setValue(new BigDecimal(redeemDetails.get("discountAmount")).setScale(0, RoundingMode.HALF_UP));
					discountDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
					discountDetailsDTO.setAddonsType(AddonsTypeEM.WALLET_COUPON);
					discountDetailsDTO.setActiveFlag(1);
					discountDetailsDTO.setRefferenceId(Numeric.ZERO_INT);
					discountDetailsDTO.setRefferenceCode(bookingDTO.getAdditionalAttributes().get(Text.WALLET_COUPON_CODE));
					discountList.add(discountDetailsDTO);
				}
			}

			if (!discountList.isEmpty()) {
				ticketDTO.getTicketAddonsDetails().addAll(discountList);
			}
		}
		catch (ServiceException e) {
			System.out.println("Transaction addonDiscountCouponV2: ERRTKBL001 :" + e.getErrorCode().getCode() + " " + bookingDTO.getCouponCode());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void addonAdditionalCharge(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO) {
		try {
			TicketDetailsDTO ticketDetailsDTO = ticketDTO.getTicketDetails().get(Numeric.ZERO_INT);
			if (StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.EXTRA_TICKET))) {
				TicketAddonsDetailsDTO extraTicketDetails = new TicketAddonsDetailsDTO();
				extraTicketDetails.setRefferenceCode(Text.EXTRA_TICKET);
				extraTicketDetails.setValue(new BigDecimal(bookingDTO.getAdditionalAttributes().get(Text.EXTRA_TICKET)).setScale(0, RoundingMode.HALF_UP));

				if (extraTicketDetails.getValue().compareTo(BigDecimal.ZERO) > 0) {
					extraTicketDetails.setSeatCode(ticketDetailsDTO.getSeatCode());
					extraTicketDetails.setAddonsType(AddonsTypeEM.ADDITIONAL_CHARGE);
					extraTicketDetails.setActiveFlag(1);
					extraTicketDetails.setRefferenceId(Numeric.ZERO_INT);
					ticketDTO.getTicketAddonsDetails().add(extraTicketDetails);
				}
			}
			if (StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.LUGGAGE))) {
				TicketAddonsDetailsDTO luggageDetails = new TicketAddonsDetailsDTO();
				luggageDetails.setRefferenceCode(Text.LUGGAGE);
				luggageDetails.setValue(new BigDecimal(bookingDTO.getAdditionalAttributes().get(Text.LUGGAGE)).setScale(0, RoundingMode.HALF_UP));

				if (luggageDetails.getValue().compareTo(BigDecimal.ZERO) > 0) {
					luggageDetails.setSeatCode(ticketDetailsDTO.getSeatCode());
					luggageDetails.setAddonsType(AddonsTypeEM.ADDITIONAL_CHARGE);
					luggageDetails.setActiveFlag(1);
					luggageDetails.setRefferenceId(Numeric.ZERO_INT);
					ticketDTO.getTicketAddonsDetails().add(luggageDetails);
				}
			}

		}
		catch (ServiceException e) {
			System.out.println("Transaction addonAdditionalCharge: ERRTKBL002 :" + e.getErrorCode().getCode() + " " + bookingDTO.getAdditionalAttributes());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Call from confirm seat if block is expired
	public void lnstanceBlockSeats(AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDTO tripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());

		List<BusSeatLayoutDTO> seatLayoutDTOList = tripDTO.getBus().getBusSeatLayoutDTO().getList();

		Map<String, BusSeatLayoutDTO> seatMap = new HashMap<String, BusSeatLayoutDTO>();
		Map<String, BusSeatLayoutDTO> allseatMap = new HashMap<String, BusSeatLayoutDTO>();
		for (BusSeatLayoutDTO seatLayoutDTO : seatLayoutDTOList) {
			if (seatLayoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_YOU || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_ALL || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_MALE || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_FEMALE) {
				seatMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
			}
			allseatMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
		}

		TicketDetailsDTO ticketDetailsDTO = ticketDTO.getTicketDetails().get(Numeric.ZERO_INT);
		boolean isLinkpayTicket = Text.FALSE;
		if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
			TicketExtraDTO ticketExtraDTO = ticketService.getTicketExtra(authDTO, ticketDTO);
			if (StringUtil.isNotNull(ticketExtraDTO.getLinkPay())) {
				isLinkpayTicket = Text.TRUE;
			}
		}

		List<TicketDetailsDTO> ticketDetails = ticketDTO.getTicketDetails();
		for (TicketDetailsDTO ticketDetailDTO : ticketDetails) {
			if (allseatMap.get(ticketDetailDTO.getSeatCode()) != null && allseatMap.get(ticketDetailDTO.getSeatCode()).getSeatStatus().getId() == SeatStatusEM.BOOKED.getId() && allseatMap.get(ticketDetailDTO.getSeatCode()).getTicketCode().equals(ticketDTO.getCode())) {
				throw new ServiceException(ErrorCode.SEAT_ALREADY_BLOOKED);
			}
			if (isLinkpayTicket && (StringUtil.isNull(allseatMap.get(ticketDetailDTO.getSeatCode()).getTicketCode()) || !allseatMap.get(ticketDetailDTO.getSeatCode()).getTicketCode().equals(ticketDTO.getCode()))) {
				throw new ServiceException(ErrorCode.SEAT_ALREADY_BLOOKED);
			}
			else if (!isLinkpayTicket && seatMap.get(ticketDetailDTO.getSeatCode()) == null) {
				StringBuilder message = new StringBuilder();
				BusSeatLayoutDTO seatLayout = allseatMap.get(ticketDetailDTO.getSeatCode());
				if (seatLayout != null) {
					message.append(seatLayout.getCode()).append(Text.VERTICAL_BAR).append(seatLayout.getSeatStatus().getCode()).append(Text.VERTICAL_BAR).append(seatLayout.getTicketCode()).append(Text.VERTICAL_BAR).append(DateUtil.convertDateTime(DateUtil.NOW()));
				}
				else {
					message.append("Invalid Seat Code!").append(Text.VERTICAL_BAR).append(ticketDetailDTO.getSeatCode());
				}
				throw new ServiceException(ErrorCode.SELECTED_SEAT_BLOCK_TIME_OVER, message.toString());
			}
		}
	}

	private void validateTrip(AuthDTO authDTO, BookingDTO bookingDTO, TripDTO tripDTO) {
		if (tripDTO == null) {
			throw new ServiceException(ErrorCode.TRIP_STATGE_CODE);
		}
		if (TripStatusEM.TRIP_OPEN.getId() != tripDTO.getTripStatus().getId() && !bookingDTO.isBookAfterTripTimeFlag()) {
			throw new ServiceException(ErrorCode.TRIP_CLOSED_NOT_ALLOW_BOOKING, "Trip Status Closed / Book After Trip Time");
		}
		if (TripStatusEM.TRIP_YET_OPEN.getId() == tripDTO.getTripStatus().getId()) {
			throw new ServiceException(ErrorCode.TRIP_YET_OPEN_NOT_ALLOW_BOOKING);
		}
		if (TripStatusEM.TRIP_CANCELLED.getId() == tripDTO.getTripStatus().getId()) {
			throw new ServiceException(ErrorCode.TRIP_CANCELLED);
		}
		if (tripDTO.getStage() == null) {
			throw new ServiceException(ErrorCode.TRIP_STATGE_CODE);
		}
		// if (tripDTO.getStage().getId() == 0) {
		// throw new ServiceException(ErrorCode.TRIP_CLOSED_NOT_ALLOW_BOOKING,
		// "Trip Stage Empty!");
		// }
		if (tripDTO.getStage().getFromStation() == null || tripDTO.getStage().getFromStation().getStationPoint() == null || tripDTO.getStage().getFromStation().getStationPoint().isEmpty()) {
			throw new ServiceException(ErrorCode.STATION_POINT);
		}
		if (tripDTO.getStage().getToStation() == null || tripDTO.getStage().getToStation().getStationPoint() == null || tripDTO.getStage().getToStation().getStationPoint().isEmpty()) {
			throw new ServiceException(ErrorCode.STATION_POINT);
		}
	}

	private TripDTO getTripDetailsV3(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO) {
		TripDTO returnTripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());

		// if
		// (StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.GST_IN))
		// && returnTripDTO.getSchedule().getTax() != null &&
		// returnTripDTO.getSchedule().getTax().getId() != 0) {
		// returnTripDTO.getSchedule().setTax(ticketTaxService.getTax(authDTO,
		// returnTripDTO, bookingDTO));
		// returnTripDTO.getSchedule().setAcBusTax(returnTripDTO.getSchedule().getTax().getServiceTax());
		// }

		// TODO To be removed
		// tripService.getStageId(authDTO, returnTripDTO.getStage());

		if (returnTripDTO == null || returnTripDTO.getBus() == null || returnTripDTO.getBus().getBusSeatLayoutDTO() == null) {
			System.out.println("Error:0098A0 " + ticketDTO.getTripDTO().getStage().getCode());
		}
		// Get seatFare
		Map<String, BusSeatLayoutDTO> fareMap = new HashMap<String, BusSeatLayoutDTO>();
		List<BusSeatLayoutDTO> seatLayoutDTOList = returnTripDTO.getBus().getBusSeatLayoutDTO().getList();

		Map<String, StageFareDTO> getFareMap = new HashMap<String, StageFareDTO>();
		// Group Wise Fare and Default Fare
		for (StageFareDTO fareDTO : returnTripDTO.getStage().getStageFare()) {
			if (fareDTO.getGroup().getId() != 0) {
				getFareMap.put(fareDTO.getGroup().getId() + fareDTO.getBusSeatType().getCode(), fareDTO);
			}
			else {
				getFareMap.put(fareDTO.getBusSeatType().getCode(), fareDTO);
			}
		}
		// Get Group Wise Fare and Default Fare
		for (BusSeatLayoutDTO seatLayoutDTO : seatLayoutDTOList) {
			if (seatLayoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_YOU || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_ALL || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_MALE || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_FEMALE || seatLayoutDTO.isAllowBlockedSeatBooking()) {
				if (getFareMap.get(authDTO.getGroup().getId() + seatLayoutDTO.getBusSeatType().getCode()) != null) {

					// Seat Fare
					if (seatLayoutDTO.getFare() == null) {
						seatLayoutDTO.setFare(getFareMap.get(authDTO.getGroup().getId() + seatLayoutDTO.getBusSeatType().getCode()).getFare());
					}
					fareMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
				}
				else if (getFareMap.get(seatLayoutDTO.getBusSeatType().getCode()) != null) {
					if (seatLayoutDTO.getFare() == null) {
						seatLayoutDTO.setFare(getFareMap.get(seatLayoutDTO.getBusSeatType().getCode()).getFare());
					}
					fareMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
				}
			}
		}

		/** Adjacent seat gender validation */
		Map<String, String> seatCodeMap = new HashMap<>();
		Map<String, BusSeatLayoutDTO> seatGenderMap = new HashMap<>();
		// skip gender validation is 1x1
		boolean isLayout1X1 = returnTripDTO.getSchedule().getScheduleBus().getBus().checkLayoutCategory(BusCategoryTypeEM.LAYOUT_1X1);
		List<BusSeatLayoutDTO> seatList = returnTripDTO.getSchedule().getScheduleBus().getBus().getBusSeatLayoutDTO().getList();
		for (BusSeatLayoutDTO dto : seatList) {
			seatCodeMap.put(dto.getCode(), dto.getLayer() + "_" + dto.getRowPos() + "_" + dto.getColPos());
			if (dto.getSeatStatus() != null) {
				seatGenderMap.put(dto.getLayer() + "_" + dto.getRowPos() + "_" + dto.getColPos(), dto);
			}
		}

		// Get seatFare
		SeatGenderRestrictionEM gendarRestriction = authDTO.getNamespace().getProfile().getSeatGendarRestriction();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
			BusSeatLayoutDTO seatLayoutDTO = fareMap.get(ticketDetailsDTO.getSeatCode());
			if (seatLayoutDTO == null) {
				throw new ServiceException(ErrorCode.SEAT_ALREADY_BLOCKED);
			}
			// to avoid seat repect
			fareMap.remove(ticketDetailsDTO.getSeatCode());
			if (ticketDetailsDTO.getSeatFare().compareTo(BigDecimal.ZERO) == 1 && seatLayoutDTO.getFare().setScale(0, RoundingMode.HALF_UP).compareTo(ticketDetailsDTO.getSeatFare().setScale(0, RoundingMode.HALF_UP)) != 0) {
				throw new ServiceException(ErrorCode.MISMATCH_IN_TRANSACTION_AMOUNT);
			}
			if (StringUtil.isNotNull(ticketDetailsDTO.getSeatName()) && !seatLayoutDTO.getName().equals(ticketDetailsDTO.getSeatName())) {
				throw new ServiceException(ErrorCode.MISMATCH_SEATCODENAME);
			}

			/** Apply Manual Seat Fare */
			BigDecimal seatFare = seatLayoutDTO.getFare();
			if (bookingDTO.getAdditionalAttributes() != null && StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(ticketDTO.getJourneyType().getCode() + "_" + seatLayoutDTO.getCode())) && StringUtil.isBigDecimal(bookingDTO.getAdditionalAttributes().get(ticketDTO.getJourneyType().getCode() + "_" + seatLayoutDTO.getCode()))) {
				seatFare = StringUtil.getBigDecimalValue(bookingDTO.getAdditionalAttributes().get(ticketDTO.getJourneyType().getCode() + "_" + seatLayoutDTO.getCode()));
				if (seatFare.compareTo(BigDecimal.ZERO) == 0) {
					seatFare = seatLayoutDTO.getFare();
				}
			}

			ticketDetailsDTO.setSeatFare(seatFare);
			ticketDetailsDTO.setSeatCode(seatLayoutDTO.getCode());
			ticketDetailsDTO.setSeatName(seatLayoutDTO.getName());
			ticketDetailsDTO.setAcBusTax(ticketDetailsDTO.getSeatFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(returnTripDTO.getSchedule().getTax().getServiceTax()));
			ticketDetailsDTO.setSeatType(seatLayoutDTO.getBusSeatType().getCode());
			ticketDetailsDTO.setActiveFlag(1);

			/** adjacent seat validation */

			String seatPos = seatCodeMap.get(ticketDetailsDTO.getSeatCode());
			if (seatPos != null) {
				int layer = Integer.parseInt(seatPos.split("_")[0]);
				int rowCount = Integer.parseInt(seatPos.split("_")[1]);
				int colCount = Integer.parseInt(seatPos.split("_")[2]);
				if (!isLayout1X1 && seatLayoutDTO.getOrientation() == 0) {
					// Female Seat validations
					if (ticketDetailsDTO.getSeatGendar().getId() == SeatGendarEM.FEMALE.getId() && (gendarRestriction.getId() != SeatGenderRestrictionEM.FAMALE_SUPERIOR.getId() && gendarRestriction.getId() != SeatGenderRestrictionEM.ANY_GENDER.getId())) {
						// Right Side seat
						if (seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null) {
							BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
							if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId()) && !layoutDTO.isAllowBlockedSeatBooking()) {
								throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
							}
						}
						// Left Side seat
						else if (seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
							BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
							if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId()) && !layoutDTO.isAllowBlockedSeatBooking()) {
								throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
							}
						}
					}
					// Male Seat validations
					else if (ticketDetailsDTO.getSeatGendar().getId() == SeatGendarEM.MALE.getId() && gendarRestriction.getId() != SeatGenderRestrictionEM.ANY_GENDER.getId()) {
						// Right Side seat
						if (seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null) {
							BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
							if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId()) && !layoutDTO.isAllowBlockedSeatBooking()) {
								throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
							}
						}
						// Left Side seat
						else if (seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
							BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
							if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId()) && !layoutDTO.isAllowBlockedSeatBooking()) {
								throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
							}
						}
					}
				}
				else if (!isLayout1X1 && seatLayoutDTO.getOrientation() == 1) {
					// Female Seat validations
					if (ticketDetailsDTO.getSeatGendar().getId() == SeatGendarEM.FEMALE.getId() && (gendarRestriction.getId() != SeatGenderRestrictionEM.FAMALE_SUPERIOR.getId() && gendarRestriction.getId() != SeatGenderRestrictionEM.ANY_GENDER.getId())) {
						// Right Side seat
						if (seatGenderMap.get(layer + "_" + rowCount + "_" + (colCount + 1)) != null) {
							BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + rowCount + "_" + (colCount + 1));
							if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId()) && !layoutDTO.isAllowBlockedSeatBooking()) {
								throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
							}
						}
						// Left Side seat
						else if (seatGenderMap.get(layer + "_" + rowCount + "_" + (colCount - 1)) != null) {
							BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + rowCount + "_" + (colCount - 1));
							if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId()) && !layoutDTO.isAllowBlockedSeatBooking()) {
								throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
							}
						}
					}
					// Male Seat validations
					else if (ticketDetailsDTO.getSeatGendar().getId() == SeatGendarEM.MALE.getId() && gendarRestriction.getId() != SeatGenderRestrictionEM.ANY_GENDER.getId()) {
						// Right Side seat
						if (seatGenderMap.get(layer + "_" + rowCount + "_" + (colCount + 1)) != null) {
							BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + rowCount + "_" + (colCount + 1));
							if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId()) && !layoutDTO.isAllowBlockedSeatBooking()) {
								throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
							}
						}
						// Left Side seat
						else if (seatGenderMap.get(layer + "_" + rowCount + "_" + (colCount - 1)) != null) {
							BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + rowCount + "_" + (colCount - 1));
							if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId()) && !layoutDTO.isAllowBlockedSeatBooking()) {
								throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
							}
						}
					}

				}
			}

			// Seat Preference
			if (seatLayoutDTO.getSeatGendar() != null && !seatLayoutDTO.isAllowBlockedSeatBooking()) {
				if (seatLayoutDTO.getSeatGendar().getId() != ticketDetailsDTO.getSeatGendar().getId()) {
					throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER_PREFERENCE);
				}
			}
			// Remove from Map, to avoid the duplicate seat Code from passenger
			// details
			fareMap.remove(ticketDetailsDTO.getSeatCode());
		}
		for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
			if (ticketDetailsDTO.getSeatFare().compareTo(BigDecimal.ZERO) != 1) {
				throw new ServiceException(ErrorCode.SELECTED_STEAT_FARE_ZERO);
			}
		}
		// Schedule Discount
		List<TicketAddonsDetailsDTO> discountList = new ArrayList<>();
		if (returnTripDTO.getSchedule().getScheduleDiscount() != null && returnTripDTO.getSchedule().getScheduleDiscount().getList() != null) {
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				for (ScheduleDiscountDTO scheduleDiscountDTO : returnTripDTO.getSchedule().getScheduleDiscount().getList()) {
					// Female Discount
					if (scheduleDiscountDTO.getFemaleDiscountFlag() == Numeric.ONE_INT && SeatGendarEM.FEMALE.getId() != ticketDetailsDTO.getSeatGendar().getId()) {
						continue;
					}

					TicketAddonsDetailsDTO discountDetailsDTO = new TicketAddonsDetailsDTO();
					if (scheduleDiscountDTO.getPercentageFlag() == 0) {
						discountDetailsDTO.setValue(scheduleDiscountDTO.getDiscountValue());
					}
					else if (scheduleDiscountDTO.getPercentageFlag() == 1) {
						discountDetailsDTO.setValue(ticketDetailsDTO.getSeatFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(scheduleDiscountDTO.getDiscountValue()));
					}
					discountDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
					discountDetailsDTO.setAddonsType(AddonsTypeEM.SCHEDULE_DISCOUNT);
					discountDetailsDTO.setActiveFlag(1);
					discountDetailsDTO.setRefferenceId(scheduleDiscountDTO.getId());
					discountDetailsDTO.setRefferenceCode(Text.NA);
					discountList.add(discountDetailsDTO);
				}
			}
		}
		ticketDTO.setTicketAddonsDetails(discountList);
		ticketDTO.setPnrStartCode(returnTripDTO.getSchedule().getPnrStartCode());
		ticketDTO.setServiceNo(returnTripDTO.getSchedule().getServiceNumber());
		return returnTripDTO;
	}

	private void applyBookGenderRestriction(AuthDTO authDTO, TicketDTO ticketDTO, TripDTO tripDTO) {
		// skip gender validation is 1x1
		boolean isLayout1X1 = tripDTO.getSchedule().getScheduleBus().getBus().checkLayoutCategory(BusCategoryTypeEM.LAYOUT_1X1);
		// Validate Seat Gender
		TicketDetailsDTO ticketDetails = Iterables.getFirst(ticketDTO.getTicketDetails(), null);

		if (!isLayout1X1 && ticketDTO.getTicketDetails().size() == 1 && SeatGendarEM.FEMALE.getId() == ticketDetails.getSeatGendar().getId()) {
			ScheduleBookGenderRestrictionDTO bookGenderRestrictionDTO = genderRestrictionService.getScheduleBookGenderRestrictionBySchedule(authDTO, tripDTO);
			if (bookGenderRestrictionDTO != null) {
				List<BusSeatLayoutDTO> seatList = tripDTO.getSchedule().getScheduleBus().getBus().getBusSeatLayoutDTO().getList();
				Map<String, BusSeatLayoutDTO> seatCodeMap = new HashMap<>();
				Map<String, BusSeatLayoutDTO> seatGenderMap = new HashMap<>();
				Map<String, BusSeatLayoutDTO> singleFemaleSeatMap = new HashMap<String, BusSeatLayoutDTO>();

				for (BusSeatLayoutDTO busSeatLayoutDTO : seatList) {
					seatCodeMap.put(busSeatLayoutDTO.getCode(), busSeatLayoutDTO);
					seatGenderMap.put(busSeatLayoutDTO.getLayer() + Text.UNDER_SCORE + busSeatLayoutDTO.getRowPos() + Text.UNDER_SCORE + busSeatLayoutDTO.getColPos(), busSeatLayoutDTO);
				}

				BusSeatLayoutDTO currentSeatLayout = seatCodeMap.get(ticketDetails.getSeatCode());
				BusSeatLayoutDTO rightSeatGenderLayout = seatGenderMap.get(currentSeatLayout.getLayer() + Text.UNDER_SCORE + (currentSeatLayout.getRowPos() + 1) + Text.UNDER_SCORE + currentSeatLayout.getColPos());
				BusSeatLayoutDTO leftSeatGenderLayout = seatGenderMap.get(currentSeatLayout.getLayer() + Text.UNDER_SCORE + (currentSeatLayout.getRowPos() - 1) + Text.UNDER_SCORE + currentSeatLayout.getColPos());
				if (rightSeatGenderLayout != null || leftSeatGenderLayout != null) {
					for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {
						if (ticketDetailsDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId()) {
							continue;
						}
						if (TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() != ticketDetailsDTO.getTicketStatus().getId() && TicketStatusEM.PHONE_BLOCKED_TICKET.getId() != ticketDetailsDTO.getTicketStatus().getId() && TicketStatusEM.TMP_BLOCKED_TICKET.getId() != ticketDetailsDTO.getTicketStatus().getId()) {
							continue;
						}
						BusSeatLayoutDTO seatPos = seatCodeMap.get(ticketDetailsDTO.getSeatCode());
						if (seatPos == null || seatPos.getOrientation() == 1) {
							continue;
						}
						if (bookGenderRestrictionDTO.getSeatTypeGroupModel() == 1 && (seatPos.getLayer() != currentSeatLayout.getLayer() || seatPos.getBusSeatType().getId() != currentSeatLayout.getBusSeatType().getId())) {
							continue;
						}

						int layer = seatPos.getLayer();
						int rowCount = seatPos.getRowPos();
						int colCount = seatPos.getColPos();
						BusSeatLayoutDTO seatGenderLayout1 = seatGenderMap.get(layer + Text.UNDER_SCORE + (rowCount + 1) + Text.UNDER_SCORE + colCount);
						BusSeatLayoutDTO seatGenderLayout2 = seatGenderMap.get(layer + Text.UNDER_SCORE + (rowCount - 1) + Text.UNDER_SCORE + colCount);

						if (seatGenderLayout1 != null && (seatGenderLayout1.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_FEMALE.getId() || (seatGenderLayout1.getSeatStatus().getId() == SeatStatusEM.TEMP_BLOCKED.getId() && seatGenderLayout1.getSeatGendar() != null && seatGenderLayout1.getSeatGendar().getId() == SeatGendarEM.FEMALE.getId())) && seatGenderLayout1.getBusSeatType().getId() == seatPos.getBusSeatType().getId()) {
							singleFemaleSeatMap.put(seatGenderLayout1.getCode(), seatGenderLayout1);
						}
						if (seatGenderLayout2 != null && (seatGenderLayout2.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_FEMALE.getId() || (seatGenderLayout2.getSeatStatus().getId() == SeatStatusEM.TEMP_BLOCKED.getId() && seatGenderLayout2.getSeatGendar() != null && seatGenderLayout2.getSeatGendar().getId() == SeatGendarEM.FEMALE.getId())) && seatGenderLayout2.getBusSeatType().getId() == seatPos.getBusSeatType().getId()) {
							singleFemaleSeatMap.put(seatGenderLayout2.getCode(), seatGenderLayout2);
						}
					}
				}
				if (!singleFemaleSeatMap.isEmpty() && singleFemaleSeatMap.size() >= bookGenderRestrictionDTO.getFemaleSeatCount()) {
					for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
						if (singleFemaleSeatMap.get(ticketDetailsDTO.getSeatCode()) == null) {
							throw new ServiceException(ErrorCode.PASSENGER_GENDER_RESTRICT);
						}
					}
				}
			}
		}
	}

	private void addonAgentServiceCharge(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO, TripDTO tripDTO) {
		TicketAddonsDetailsDTO discountDetailsDTO = new TicketAddonsDetailsDTO();
		discountDetailsDTO.setValue(bookingDTO.getAgentServiceCharge());
		discountDetailsDTO.setSeatCode(ticketDTO.getTicketDetails().get(0).getSeatCode());
		discountDetailsDTO.setAddonsType(AddonsTypeEM.AGENT_SERVICE_CHARGE);
		discountDetailsDTO.setRefferenceId(Numeric.ZERO_INT);
		discountDetailsDTO.setRefferenceCode(Text.NA);
		discountDetailsDTO.setActiveFlag(1);
		ticketDTO.getTicketAddonsDetails().add(discountDetailsDTO);
	}

	private TicketExtraDTO processTicketExtraDetails(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO) {
		TicketExtraDTO ticketExtra = new TicketExtraDTO();

		// All related to Onward Ticket
		int releaseMinutes = Numeric.ZERO_INT;
		if (StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.RELEASE_MINUTES_OVERRIDE))) {
			releaseMinutes = StringUtil.getIntegerValue(bookingDTO.getAdditionalAttributes().get(Text.RELEASE_MINUTES_OVERRIDE));
		}
		ticketExtra.setBlockReleaseMinutes(releaseMinutes);

		if (StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.ENABLE_LINKPAY)) && bookingDTO.getAdditionalAttributes().get(Text.ENABLE_LINKPAY).equals(Numeric.ONE)) {
			ticketExtra.setLinkPay(Text.TRUE_STRING);
		}
		ticketExtra.setOfflineTicketCode(StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.OFFLINE_TICKET)) ? bookingDTO.getAdditionalAttributes().get(Text.OFFLINE_TICKET) : Text.NA);

		if (ticketDTO.getScheduleTicketTransferTerms() != null) {
			DateTime bookedStageDateTime = DateUtil.addMinituesToDate(ticketDTO.getTripDTO().getTripDate(), ticketDTO.getTripDTO().getStage().getFromStation().getMinitues());
			ticketExtra.setTicketTransferMinutes(BitsUtil.getTicketTransferMinutes(authDTO, ticketDTO.getScheduleTicketTransferTerms(), bookedStageDateTime, ticketDTO.getTripDTO().getTripDateTimeV2()));
		}
		return ticketExtra;
	}

	private PaymentGatewayScheduleDTO getPaymentGatewaySchedule(AuthDTO authDTO, BookingDTO bookingDTO) {
		PaymentGatewayScheduleDTO gatewayScheduleDTO = null;
		try {
			OrderInitRequestDTO orderInitRequest = new OrderInitRequestDTO();
			orderInitRequest.setPartnerCode(bookingDTO.getPaymentGatewayPartnerCode());
			orderInitRequest.setOrderType(OrderTypeEM.TICKET);

			gatewayScheduleDTO = paymentMerchantGatewayScheduleService.getPaymentGatewayForNamespace(authDTO, orderInitRequest);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return gatewayScheduleDTO;
	}

	private void calculatePaymentGatewayCharge(BookingDTO bookingDTO, TicketDTO ticketDTO, PaymentGatewayScheduleDTO gatewayScheduleDTO) {
		BigDecimal totalTicketAmount = bookingDTO.getTransactionAmount().setScale(0, RoundingMode.HALF_UP);
		BigDecimal serviceCharge = totalTicketAmount.multiply(gatewayScheduleDTO.getServiceCharge()).divide(Numeric.ONE_HUNDRED);
		BigDecimal ticketServiceCharge = serviceCharge.divide(BigDecimal.valueOf(bookingDTO.getTicketList().size()));
		double seatServiceCharge = (Math.round(ticketServiceCharge.divide(BigDecimal.valueOf(ticketDTO.getTicketDetails().size())).doubleValue() * 100.0) / 100.0);

		List<TicketAddonsDetailsDTO> serviceChargeAddons = new ArrayList<>();
		for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
			TicketAddonsDetailsDTO discountDetailsDTO = new TicketAddonsDetailsDTO();
			discountDetailsDTO.setValue(BigDecimal.valueOf(seatServiceCharge));
			discountDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
			discountDetailsDTO.setAddonsType(AddonsTypeEM.PG_SERVICE_CHARGE);
			discountDetailsDTO.setRefferenceId(bookingDTO.getTicketSeatCount());
			discountDetailsDTO.setRefferenceCode(Text.NA);
			discountDetailsDTO.setActiveFlag(1);
			serviceChargeAddons.add(discountDetailsDTO);
		}
		ticketDTO.getTicketAddonsDetails().addAll(serviceChargeAddons);
	}

	private TicketTaxDTO composeNamespaceTaxFromCustomerGstin(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO) {
		TicketTaxDTO ticketTax = null;
		if (StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.GST_IN)) && StringUtil.isNotNull(bookingDTO.getAdditionalAttributes().get(Text.GST_TRADE_NAME))) {
			ticketTax = new TicketTaxDTO();
			ticketTax.setGstin(bookingDTO.getAdditionalAttributes().get(Text.GST_IN).toUpperCase());
			ticketTax.setTradeName(WordUtils.capitalize(StringUtil.substring(StringUtil.removeUnknownSymbol(bookingDTO.getAdditionalAttributes().get(Text.GST_TRADE_NAME)), 120)));
			ticketTax.setEmail(StringUtil.isNull(bookingDTO.getAdditionalAttributes().get(Text.GST_EMAIL), Text.NA));
		}

		if (ticketTax != null && StringUtil.isNotNull(ticketTax.getGstin())) {
			NamespaceTaxDTO namespaceTax = ticketDTO.getTripDTO().getSchedule().getTax();
			stateService.getState(namespaceTax.getState());
			StateEM stateEM = GSTINValidator.getState(ticketTax.getGstin().toUpperCase());
			if (stateEM != null && namespaceTax != null && namespaceTax.getId() != 0 && namespaceTax.getState() != null && StringUtil.isNotNull(namespaceTax.getState().getCode()) && !namespaceTax.getState().getCode().equals(stateEM.getCode())) {
				StateDTO stateDTO = new StateDTO();
				stateDTO.setCode(stateEM.getCode());
				List<NamespaceTaxDTO> taxList = getNamespaceTaxbyStateCode(authDTO, stateDTO);
				if (!taxList.isEmpty()) {
					ticketDTO.getTripDTO().getSchedule().setTax(Iterables.getFirst(taxList, null));
				}
			}
		}
		return ticketTax;
	}
}
