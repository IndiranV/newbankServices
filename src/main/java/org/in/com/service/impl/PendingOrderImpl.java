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
import java.util.stream.Collectors;

import org.in.com.aggregator.backup.DRService;
import org.in.com.aggregator.bits.BitsService;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.payment.PGInterface;
import org.in.com.aggregator.payment.impl.GatewayFactory;
import org.in.com.aggregator.slack.SlackService;
import org.in.com.aggregator.sms.SMSService;
import org.in.com.cache.TicketCache;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ConnectDAO;
import org.in.com.dao.PaymentGatewayPreTransactionDAO;
import org.in.com.dao.PaymentGatewayProviderDAO;
import org.in.com.dao.PaymentGatewayTransactionDAO;
import org.in.com.dao.PaymentMerchantGatewayCredentialsDAO;
import org.in.com.dao.PaymentTransactionDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.OrderDTO;
import org.in.com.dto.PaymentGatewayCredentialsDTO;
import org.in.com.dto.PaymentGatewayProviderDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.PaymentPreTransactionDTO;
import org.in.com.dto.PaymentTransactionDTO;
import org.in.com.dto.PendingOrderDTO;
import org.in.com.dto.RefundDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserCustomerDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.EventNotificationEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PaymentAcknowledgeEM;
import org.in.com.dto.enumeration.PaymentGatewayStatusEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.dto.enumeration.PaymentOrderEM;
import org.in.com.dto.enumeration.RefundStatusEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.dto.enumeration.WalletAccessEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.PaymentRequestException;
import org.in.com.exception.PaymentResponseException;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusmapService;
import org.in.com.service.DiscountService;
import org.in.com.service.NotificationService;
import org.in.com.service.PaymentOrderStatusService;
import org.in.com.service.PendingOrderService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStationPointService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.SearchService;
import org.in.com.service.TicketService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.service.UserTransactionService;
import org.in.com.service.UserWalletService;
import org.in.com.service.pg.PaymentRefundService;
import org.in.com.service.pg.PaymentRequestService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import lombok.Cleanup;

@Service
public class PendingOrderImpl extends TicketCache implements PendingOrderService {

	@Autowired
	TripService tripService;
	@Autowired
	EmailService emailService;
	@Autowired
	SMSService smsService;
	@Autowired
	TicketService ticketService;
	@Autowired
	SearchService searchService;
	@Autowired
	BusmapService busmapService;
	@Autowired
	PaymentRequestService paymentRequestService;
	@Autowired
	DRService drService;
	@Autowired
	PaymentOrderStatusService paymentOrderStatusService;
	@Autowired
	UserWalletService walletService;
	@Autowired
	SlackService slack;
	@Autowired
	DiscountService discountService;
	@Autowired
	UserTransactionService userTransactionService;
	@Autowired
	PaymentRefundService refundService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	BitsService bitsService;
	@Autowired
	UserService userService;
	@Autowired
	ScheduleDynamicStageFareService dynamicFareService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	ScheduleStationPointService scheduleStationPointService;

	@Override
	public List<PendingOrderDTO> get(AuthDTO authDTO, PendingOrderDTO dto) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<PendingOrderDTO> getAll(AuthDTO authDTO) {
		return null;
	}

	@Override
	public PendingOrderDTO cancelPendingOrder(AuthDTO authDTO, PendingOrderDTO dto) {
		try {
			PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();
			PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO();
			preTransactionDTO.setOrderCode(dto.getOrderCode());
			if (dto.getOrderCode().equals(dto.getCode())) {
				paymentPreTransactionDAO.getPreTransactionForTicket(authDTO, preTransactionDTO);
			}
			else {
				paymentPreTransactionDAO.getPreTransactionForTransaction(authDTO, preTransactionDTO);
			}
			preTransactionDTO.setStatus(PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED);
			preTransactionDTO.setFailureErrorCode("CAN By Admin");
			// Update payment gateway pre transaction
			paymentPreTransactionDAO.updateStatusV2(authDTO, preTransactionDTO);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
		}
		return dto;
	}

	@Override
	public TicketDTO confirmPendingOrder(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDTO DBTicketDTO = new TicketDTO();

		try {
			DBTicketDTO.setCode(ticketDTO.getCode());
			ticketService.getTicketStatus(authDTO, DBTicketDTO);
			DBTicketDTO.setRemarks(ticketDTO.getRemarks());

			if (DBTicketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
				throw new ServiceException(ErrorCode.ALREADY_CONFIRM_BOOKED_TICKET);
			}
			if (DBTicketDTO.getTicketDetails().size() != ticketDTO.getTicketDetails().size()) {
				throw new ServiceException(ErrorCode.PENDING_ORDER_CONFIRMATION_SEAT_COUNT_MISMATCH);
			}

			// Get Payment pre-transction details
			PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();
			PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO();
			preTransactionDTO.setOrderCode(DBTicketDTO.getBookingCode());
			paymentPreTransactionDAO.getPreTransactionForTicket(authDTO, preTransactionDTO);
			preTransactionDTO.setUser(userService.getUserDTO(authDTO, preTransactionDTO.getUser()));

			// M2M Verifications
			Map<String, String> responseMap = getPaymentStatus(authDTO, preTransactionDTO);
			if (responseMap == null || StringUtil.isNull(responseMap.get("paymentOrderStatus")) || !PaymentOrderEM.SUCCESS.getCode().equals(responseMap.get("paymentOrderStatus"))) {
				throw new PaymentRequestException(ErrorCode.PAYMENT_DECLINED);
			}

			Map<String, String> newSeatValidate = new HashMap<>();
			for (TicketDetailsDTO DBdetailsDTO : DBTicketDTO.getTicketDetails()) {
				for (TicketDetailsDTO detailsDTO : ticketDTO.getTicketDetails()) {
					if (DBdetailsDTO.getSeatCode().equals(detailsDTO.getSeatCode().split("-")[0])) {
						if (newSeatValidate.get(detailsDTO.getSeatCode().split("-")[1]) != null) {
							throw new ServiceException(ErrorCode.DUPLICATE_SEAT_CODE);
						}
						newSeatValidate.put(detailsDTO.getSeatCode().split("-")[1], detailsDTO.getSeatCode().split("-")[1]);
						DBdetailsDTO.setSeatCode(detailsDTO.getSeatCode().split("-")[1]);
						DBdetailsDTO.setActiveFlag(3);
						break;
					}
				}
				// check already ticket status
				if (DBdetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
					throw new ServiceException(ErrorCode.ALREADY_CONFIRM_BOOKED_TICKET);
				}
			}
			// Get Bus map
			TripDTO tripDTO = busmapService.getSearchBusmapV3(authDTO, DBTicketDTO.getTripDTO());

			Map<String, TicketDetailsDTO> statusMAP = new HashMap<String, TicketDetailsDTO>();
			for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {
				if (tripDTO.getReleatedStageCodeList().contains(ticketDetailsDTO.getTripStageCode())) {
					statusMAP.put(ticketDetailsDTO.getSeatCode(), ticketDetailsDTO);
				}
			}
			// Validate seat status from bus map
			for (TicketDetailsDTO DBdetailsDTO : DBTicketDTO.getTicketDetails()) {
				TicketDetailsDTO ticketDetailsDTO = statusMAP.get(DBdetailsDTO.getCode());
				if (DBdetailsDTO.getActiveFlag() != 3) {
					throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
				}
				DBdetailsDTO.setActiveFlag(1);
				if (ticketDetailsDTO != null) {
					if (ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
						throw new ServiceException(ErrorCode.DOUBLE_ENTRY_VALIDATION_FAIL);
					}
				}
			}
			// Copy Boarding Point name
			for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
				if (pointDTO.getId() == DBTicketDTO.getBoardingPoint().getId()) {
					DBTicketDTO.getBoardingPoint().setName(pointDTO.getName());
					break;
				}
			}

			// Get Seat Name from bus map
			for (TicketDetailsDTO DBdetailsDTO : DBTicketDTO.getTicketDetails()) {
				for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
					if (DBdetailsDTO.getSeatCode().equals(seatLayoutDTO.getCode())) {
						if (seatLayoutDTO.getSeatStatus() != null && seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId()) {
							throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
						}
						DBdetailsDTO.setSeatName(seatLayoutDTO.getName());
						break;
					}
				}
			}
			DBTicketDTO.setTripDTO(tripDTO);
			// Check whether the seat already booked.
			boolean seatDouplicateEntry = ticketService.checkSeatDuplicateEntryV2(authDTO, DBTicketDTO);
			if (seatDouplicateEntry) {
				throw new ServiceException(ErrorCode.DOUBLE_ENTRY_VALIDATION_FAIL);
			}

			TicketExtraDTO ticketExtraDTO = ticketService.getTicketExtra(authDTO, DBTicketDTO);
			if (StringUtil.isNotNull(ticketExtraDTO.getLinkPay())) {
				DBTicketDTO.setTicketUser(preTransactionDTO.getUser());
			}

			// calculate ticket transaction details
			TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
			ticketTransactionDTO.setTransactionMode(TransactionModeEM.PAYMENT_PAYMENT_GATEWAY);
			ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
			ticketTransactionDTO.setTransactionAmount(DBTicketDTO.getTotalFare());
			ticketTransactionDTO.setAddonsAmount(DBTicketDTO.getAddonsValue());
			ticketTransactionDTO.setAcBusTax(DBTicketDTO.getAcBusTax());
			ticketTransactionDTO.setTdsTax(BigDecimal.ZERO);
			ticketTransactionDTO.setCommissionAmount(BigDecimal.ZERO);
			ticketTransactionDTO.setExtraCommissionAmount(BigDecimal.ZERO);
			DBTicketDTO.setTicketXaction(ticketTransactionDTO);
			// Binding new seat code in ticket details
			for (TicketDetailsDTO dto : DBTicketDTO.getTicketDetails()) {
				dto.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);
			}
			DBTicketDTO.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);

			TicketDTO repoTicketDTO = null;
			if (DBTicketDTO.getLookupId() != 0 && (DBTicketDTO.getJourneyType().getId() == JourneyTypeEM.POSTPONE.getId() || DBTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId())) {
				repoTicketDTO = validateTicketTransfer(authDTO, ticketDTO, DBTicketDTO);
				ticketExtraDTO = ticketService.getTicketExtra(authDTO, repoTicketDTO);
				if (StringUtil.isNotNull(ticketExtraDTO.getLinkPay())) {
					repoTicketDTO.setTicketUser(preTransactionDTO.getUser());
					DBTicketDTO.setTicketUser(preTransactionDTO.getUser());
				}
			}
			// validate trip stage seat details
			Map<String, List<TicketDetailsDTO>> seatDetailsMap = validateTripStageSeats(authDTO, DBTicketDTO, ticketDTO);
			// update details
			saveConfirmOrder(authDTO, DBTicketDTO, repoTicketDTO, seatDetailsMap);

			// Update Coupon usage
			TicketAddonsDetailsDTO ticketAddonsDetails = DBTicketDTO.getTicketCouponAddon(AddonsTypeEM.COUPON_DISCOUNT);
			if (ticketAddonsDetails != null) {
				discountService.updateDiscountCouponUsage(authDTO, ticketAddonsDetails);
			}

			preTransactionDTO.setFailureErrorCode("Handle by Admin");
			preTransactionDTO.setStatus(PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS);

			// Check if Round Trip Booking
			List<TicketDTO> ticketList = ticketService.getTicketStatusByBookingCode(authDTO, DBTicketDTO.getBookingCode());
			for (Iterator<TicketDTO> iterator = ticketList.iterator(); iterator.hasNext();) {
				TicketDTO dto = iterator.next();
				if (dto.getTicketStatus().getId() != TicketStatusEM.TMP_BLOCKED_TICKET.getId()) {
					iterator.remove();
				}
			}
			if (ticketList.isEmpty()) {
				// Update payment gateway pre transaction
				paymentPreTransactionDAO.updateStatusV2(authDTO, preTransactionDTO);
			}
			if (!paymentRequestService.CheckPaymentStatus(authDTO, preTransactionDTO.getCode())) {
				// mapping pre transaction values to gateway transaction
				PaymentGatewayTransactionDTO transactionEntity = new PaymentGatewayTransactionDTO();
				transactionEntity.setCode(preTransactionDTO.getCode());
				transactionEntity.setUser(preTransactionDTO.getUser());
				transactionEntity.setDeviceMedium(preTransactionDTO.getDeviceMedium());
				transactionEntity.setGatewayPartner(preTransactionDTO.getGatewayPartner());
				transactionEntity.setGatewayProvider(preTransactionDTO.getGatewayProvider());
				transactionEntity.setGatewayCredentials(preTransactionDTO.getGatewayCredentials());
				transactionEntity.setAmount(preTransactionDTO.getAmount());
				transactionEntity.setTransactionType(PaymentGatewayTransactionTypeEM.PAYMENT);
				transactionEntity.setOrderCode(preTransactionDTO.getOrderCode());
				transactionEntity.setStatus(PaymentGatewayStatusEM.SUCCESS);
				PaymentGatewayTransactionDAO gatewayTransaction = new PaymentGatewayTransactionDAO();
				gatewayTransaction.insert(authDTO, transactionEntity);
			}

			/** Apply Schedule Station Point Contact Address */
			applyScheduleStationPointContact(authDTO, DBTicketDTO);

			// Ticket Notifications
			notificationService.sendTicketBookingNotification(authDTO, DBTicketDTO);
			/** send Ticket Event To Customer */
			notificationService.sendCustomerTicketEvent(authDTO, DBTicketDTO, EventNotificationEM.TICKET_BOOKING);
			emailService.sendBookingEmail(authDTO, DBTicketDTO);
			checkStatus(authDTO, DBTicketDTO, preTransactionDTO);

			// Upload to Backup Server
			drService.flushTicketDetails(authDTO, DBTicketDTO);

			// Apply Wallet
			if (WalletAccessEM.getWalletAccessEM(authDTO.getNamespaceCode()) != null && DBTicketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId() && DBTicketDTO.isContainAddonWalletTransaction() && DBTicketDTO.getTicketForUser() != null && DBTicketDTO.getTicketForUser().getId() != 0) {
				UserCustomerDTO userCustomerDTO = new UserCustomerDTO();
				userCustomerDTO.setId(DBTicketDTO.getTicketForUser().getId());

				walletService.processWalletTransaction(authDTO, DBTicketDTO, userCustomerDTO);
			}
			// Dynamic Pricing Call back
			if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
				tripService.getTrip(authDTO, DBTicketDTO.getTripDTO());
				dynamicFareService.updateTicketStatus(authDTO, DBTicketDTO);
			}
		}
		catch (ServiceException e) {
			e.printStackTrace();
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return DBTicketDTO;
	}

	private TicketDTO validateTicketTransfer(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO DBTicketDTO) {
		TicketDTO repoTicketDTO = new TicketDTO();
		repoTicketDTO.setId(DBTicketDTO.getLookupId());
		ticketService.getTicketStatus(authDTO, repoTicketDTO);

		if (repoTicketDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() && repoTicketDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
			throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED);
		}

		List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
		for (Iterator<TicketDetailsDTO> iterator = repoTicketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
			TicketDetailsDTO detailsDTO = iterator.next();

			if (detailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() && detailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				iterator.remove();
				continue;
			}
			seatDetailsList.add(detailsDTO);
		}

		if (seatDetailsList.size() != ticketDTO.getTicketDetails().size()) {
			throw new ServiceException(ErrorCode.SELECTED_STEAT_NOT_FOR_RESERVATION);
		}
		if (seatDetailsList.size() != DBTicketDTO.getTicketDetails().size()) {
			throw new ServiceException(ErrorCode.SELECTED_STEAT_NOT_FOR_RESERVATION);
		}

		// get Ticket Transaction
		TicketTransactionDTO transactionDTO = new TicketTransactionDTO();
		transactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
		repoTicketDTO.setTicketXaction(transactionDTO);
		ticketService.getTicketTransaction(authDTO, repoTicketDTO);

		BigDecimal totalTransferFare = getTicketFareWithAddons(DBTicketDTO);
		BigDecimal totalTicketFare = repoTicketDTO.getTicketFareWithAddons();
		BigDecimal revenueAmount = BigDecimal.ZERO;

		if (totalTicketFare.compareTo(totalTransferFare) == 1) {
			revenueAmount = totalTicketFare.subtract(totalTransferFare);
		}

		// Change old Ticket status
		BigDecimal cancellationCharges = revenueAmount.divide(StringUtil.getBigDecimalValue(String.valueOf(repoTicketDTO.getTicketDetails().size())));
		for (TicketDetailsDTO ticketDetailsDTO : repoTicketDTO.getTicketDetails()) {
			ticketDetailsDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
			ticketDetailsDTO.setCancellationCharges(cancellationCharges);
		}
		repoTicketDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
		for (TicketAddonsDetailsDTO addonsDetailsDTO : repoTicketDTO.getTicketAddonsDetails()) {
			addonsDetailsDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
		}

		repoTicketDTO.getTicketXaction().setCancellationCommissionAmount(BigDecimal.ZERO);
		repoTicketDTO.getTicketXaction().setRefundAmount((repoTicketDTO.getTotalSeatFare().add(repoTicketDTO.getTicketXaction().getAcBusTax())).subtract(repoTicketDTO.getTicketXaction().getCommissionAmount().add(repoTicketDTO.getTicketXaction().getExtraCommissionAmount()).add(repoTicketDTO.getAddonsValue())));
		repoTicketDTO.getTicketXaction().setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
		repoTicketDTO.getTicketXaction().setRefundStatus(RefundStatusEM.PROCESSED_BY_BANK);

		return repoTicketDTO;
	}

	private BigDecimal getTicketFareWithAddons(TicketDTO transferTicket) {
		BigDecimal totalFare = BigDecimal.ZERO;
		for (TicketDetailsDTO ticketDetailsDTO : transferTicket.getTicketDetails()) {
			totalFare = totalFare.add(ticketDetailsDTO.getSeatFare().add(ticketDetailsDTO.getAcBusTax()));
		}
		if (transferTicket.getTicketAddonsDetails() != null && !transferTicket.getTicketAddonsDetails().isEmpty()) {
			for (TicketAddonsDetailsDTO addonsDetailsDTO : transferTicket.getTicketAddonsDetails()) {
				if (!addonsDetailsDTO.getAddonsType().isRefundable() || addonsDetailsDTO.getAddonsType().getId() == AddonsTypeEM.TICKET_TRANSFER_CHARGE.getId()) {
					continue;
				}
				if ("Cr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
					totalFare = totalFare.subtract(addonsDetailsDTO.getValue());
				}
				else if ("Dr".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
					totalFare = totalFare.add(addonsDetailsDTO.getValue());
				}
				else if ("NA".equals(addonsDetailsDTO.getAddonsType().getCreditDebitFlag())) {
					totalFare = totalFare.add(addonsDetailsDTO.getValue());
				}
			}
		}

		return totalFare.setScale(2, RoundingMode.HALF_UP);
	}

	private void saveConfirmOrder(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO repoTicketDTO, Map<String, List<TicketDetailsDTO>> seatDetailsMap) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);

				ticketService.UpdateTicketDetailsStatus(connection, authDTO, ticketDTO, Text.AUDIT_EVENT_CONFIRM_PENDING_ORDER);

				ticketService.saveTicketTransaction(connection, authDTO, ticketDTO);

				// update seat seats in trip seat details
				checkAndUpdateTripSeatDetailsStatus(connection, authDTO, ticketDTO, seatDetailsMap);

				// Update old ticket status
				if (ticketDTO.getLookupId() != 0 && repoTicketDTO != null && repoTicketDTO.getId() != 0) {
					ticketService.UpdateTicketStatus(connection, authDTO, repoTicketDTO);

					tripService.updateTripSeatDetailsStatus(connection, authDTO, repoTicketDTO);

					// Cancel Transaction For Old Ticket
					ticketService.saveTicketTransaction(connection, authDTO, repoTicketDTO);

					ticketService.saveTicketCancelTransaction(connection, authDTO, repoTicketDTO);

					ticketService.saveTicketCancellationDetails(connection, authDTO, repoTicketDTO);
				}
			}
			catch (SQLTransactionRollbackException e) {
				slack.sendAlert(authDTO, ticketDTO.getCode() + " DL04- Deadlock found when trying to get lock; try restarting transaction");

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
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
		}

	}

	private Map<String, List<TicketDetailsDTO>> validateTripStageSeats(AuthDTO authDTO, TicketDTO DBTicketDTO, TicketDTO ticketDTO) {
		Map<String, List<TicketDetailsDTO>> finalSeatMap = new HashMap<>();

		Map<String, TicketDetailsDTO> uniqueSeatMap = new HashMap<>();
		List<TicketDetailsDTO> ticketDetailList = tripService.getBookedBlockedSeats(authDTO, DBTicketDTO.getTripDTO());
		if (ticketDetailList != null) {
			for (TicketDetailsDTO ticketDetails : ticketDetailList) {
				if (ticketDetails.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() || ticketDetails.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId()) {
					uniqueSeatMap.put(ticketDetails.getSeatCode() + ticketDetails.getTicketCode(), ticketDetails);
				}
			}
		}

		Map<String, TicketDetailsDTO> updateSeatMap = new HashMap<>();
		Map<String, TicketDetailsDTO> newSeatMap = new HashMap<>();
		for (TicketDetailsDTO DBTicketDetails : DBTicketDTO.getTicketDetails()) {
			if (uniqueSeatMap.get(DBTicketDetails.getSeatCode() + DBTicketDTO.getCode()) != null) {
				updateSeatMap.put(DBTicketDetails.getSeatCode() + DBTicketDTO.getCode(), DBTicketDetails);
			}
			else {
				newSeatMap.put(DBTicketDetails.getSeatCode() + DBTicketDTO.getCode(), DBTicketDetails);
			}
		}
		Map<String, TicketDetailsDTO> oldSeatMap = new HashMap<>();
		for (TicketDetailsDTO ticketDetails : ticketDTO.getTicketDetails()) {
			if (!ticketDetails.getSeatCode().split("-")[0].equals(ticketDetails.getSeatCode().split("-")[1])) {
				ticketDetails.setSeatCode(ticketDetails.getSeatCode().split("-")[0]);
				ticketDetails.setActiveFlag(2);
				oldSeatMap.put(ticketDetails.getSeatCode().split("-")[0], ticketDetails);
			}
		}
		finalSeatMap.put("UPDATE_SEAT", new ArrayList<>(updateSeatMap.values()));
		finalSeatMap.put("NEW_SEAT", new ArrayList<>(newSeatMap.values()));
		finalSeatMap.put("OLD_SEAT", new ArrayList<>(oldSeatMap.values()));
		return finalSeatMap;
	}

	private void checkAndUpdateTripSeatDetailsStatus(Connection connection, AuthDTO authDTO, TicketDTO DBTicketDTO, Map<String, List<TicketDetailsDTO>> seatMap) {
		TicketDTO tempTicket = new TicketDTO();
		tempTicket.setTicketDetails(DBTicketDTO.getTicketDetails());
		List<TicketDetailsDTO> updateTickeDetails = seatMap.get("UPDATE_SEAT");
		List<TicketDetailsDTO> newTickeDetails = seatMap.get("NEW_SEAT");
		List<TicketDetailsDTO> oldTickeDetails = seatMap.get("OLD_SEAT");

		if (updateTickeDetails != null && !updateTickeDetails.isEmpty()) {
			DBTicketDTO.setTicketDetails(updateTickeDetails);
			tripService.updateTripSeatDetailsStatus(connection, authDTO, DBTicketDTO);
		}
		if (oldTickeDetails != null && !oldTickeDetails.isEmpty()) {
			DBTicketDTO.setTicketDetails(oldTickeDetails);
			tripService.updateTripSeatDetails(authDTO, DBTicketDTO);
		}
		if (newTickeDetails != null && !newTickeDetails.isEmpty()) {
			DBTicketDTO.setTicketDetails(newTickeDetails);
			tripService.SaveBookedBlockedSeats(authDTO, DBTicketDTO);
		}
		DBTicketDTO.setTicketDetails(tempTicket.getTicketDetails());
	}

	@Async
	public void checkStatus(AuthDTO authDTO, TicketDTO DBTicketDTO, PaymentPreTransactionDTO preTransactionDTO) {
		try {
			Map<String, String> orderStatusMap = paymentOrderStatusService.getOrderStatus(authDTO, preTransactionDTO.getOrderCode(), authDTO.getNamespaceCode());
			// emailService.sendFailureOrderBookingEmail(authDTO, DBTicketDTO,
			// orderStatusMap);
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public PendingOrderDTO Update(AuthDTO authDTO, PendingOrderDTO dto) {
		try {
			PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();
			PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO();
			preTransactionDTO.setOrderCode(dto.getOrderCode());
			// TODO Have to check payment status
			// boolean status =
			// paymentRequestService.CheckPaymentStatus(authDTO,
			// dto.getOrderCode());
			// if (status) {
			// throw new
			// ServiceException(ErrorCode.TRANSACTION_ALREADY_INITIATED);
			// }

			preTransactionDTO.setStatus(PaymentGatewayStatusEM.ORDER_INITIATED);
			preTransactionDTO.setFailureErrorCode("Failure to Pending");
			// Update payment gateway pre transaction
			paymentPreTransactionDAO.updatePaymentGatewayStatus(authDTO, preTransactionDTO);
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
		}
		return dto;
	}

	@Override
	public List<BookingDTO> getInprogreeTicketTransaction(AuthDTO authDTO) {
		List<BookingDTO> list = getAllBookingDTO(authDTO);
		boolean isSuperUser = ApplicationConfig.getZoneSuperUser().contains(authDTO.getUserCode()) ? true : false;
		hirondelle.date4j.DateTime systemTime = DateUtil.NOW();
		int seatBlockTime = authDTO.getNamespace().getProfile().getSeatBlockTime();
		for (BookingDTO bookingDTO : list) {
			for (Iterator<TicketDTO> itrTicket = bookingDTO.getTicketList().iterator(); itrTicket.hasNext();) {
				TicketDTO ticketDTO = itrTicket.next();

				boolean status = (ticketDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDTO.getTicketAt(), systemTime) > seatBlockTime) ? true : false;
				// Masked mobile number ticket should not expose
				if (StringUtil.isMaskedMobileNumber(ticketDTO.getPassengerMobile()) && !isSuperUser && status) {
					itrTicket.remove();
					continue;
				}

				ticketService.applyMobileNumberMasking(authDTO, ticketDTO);

				if (!ApplicationConfig.getServerZoneCode().equals(authDTO.getNamespaceCode())) {
					ticketDTO.setTicketUser(getUserDTOById(authDTO, ticketDTO.getTicketUser()));
				}
			}
		}
		return list;
	}

	@Override
	public PaymentTransactionDTO rechargeConfirmOrder(AuthDTO authDTO, String orderCode) {
		PaymentTransactionDTO paymentTransactionDTO = new PaymentTransactionDTO();
		try {
			PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO();
			preTransactionDTO.setOrderCode(orderCode);

			PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();
			paymentPreTransactionDAO.getPreTransactionForTicket(authDTO, preTransactionDTO);

			// M2M Verifications
			Map<String, String> responseMap = getPaymentStatus(authDTO, preTransactionDTO);
			if (responseMap == null || StringUtil.isNull(responseMap.get("paymentOrderStatus")) || !PaymentOrderEM.SUCCESS.getCode().equals(responseMap.get("paymentOrderStatus"))) {
				throw new PaymentRequestException(ErrorCode.PAYMENT_DECLINED);
			}

			// Update payment gateway pre transaction
			preTransactionDTO.setFailureErrorCode("Handle by Admin");
			preTransactionDTO.setStatus(PaymentGatewayStatusEM.SUCCESS);
			preTransactionDTO.setUser(userService.getUser(authDTO, preTransactionDTO.getUser()));

			PaymentGatewayTransactionDTO paymentGatewayTransaction = paymentRequestService.getPaymentGatewayTransactionAmount(authDTO, orderCode, PaymentGatewayTransactionTypeEM.PAYMENT);

			PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
			paymentTransactionDTO.setCode(orderCode);
			paymentTransactionDAO.getPaymentTransaction(authDTO, paymentTransactionDTO);

			BigDecimal transactionAmount = preTransactionDTO.getAmount().subtract(preTransactionDTO.getServiceCharge());

			if (StringUtil.isNull(paymentGatewayTransaction.getCode()) && paymentTransactionDTO.getId() == 0) {
				// mapping pre transaction values to gateway transaction
				paymentGatewayTransaction.setCode(preTransactionDTO.getCode());
				paymentGatewayTransaction.setUser(preTransactionDTO.getUser());
				paymentGatewayTransaction.setDeviceMedium(preTransactionDTO.getDeviceMedium());
				paymentGatewayTransaction.setGatewayPartner(preTransactionDTO.getGatewayPartner());
				paymentGatewayTransaction.setGatewayProvider(preTransactionDTO.getGatewayProvider());
				paymentGatewayTransaction.setGatewayCredentials(preTransactionDTO.getGatewayCredentials());
				paymentGatewayTransaction.setAmount(preTransactionDTO.getAmount());
				paymentGatewayTransaction.setTransactionType(PaymentGatewayTransactionTypeEM.PAYMENT);
				paymentGatewayTransaction.setOrderCode(preTransactionDTO.getOrderCode());
				paymentGatewayTransaction.setStatus(PaymentGatewayStatusEM.SUCCESS);
			}
			else if (StringUtil.isNotNull(paymentGatewayTransaction.getCode()) && paymentTransactionDTO.getId() == 0) {
				transactionAmount = paymentGatewayTransaction.getAmount().subtract(paymentGatewayTransaction.getServiceCharge());
				paymentGatewayTransaction.setCode(null);
			}
			else {
				throw new ServiceException(ErrorCode.TRANSACTION_FAIL_BALANCE_ISSUES);
			}

			// Make Payment Transaction
			paymentTransactionDTO.setCode(orderCode);
			paymentTransactionDTO.setTransactionAmount(transactionAmount);
			paymentTransactionDTO.setTransactionType(TransactionTypeEM.RECHARGE);
			paymentTransactionDTO.setTransactionMode(TransactionModeEM.PAYMENT_PAYMENT_GATEWAY);
			paymentTransactionDTO.setCommissionAmount(BigDecimal.ZERO);
			paymentTransactionDTO.setAmountReceivedDate(DateTime.now().toString(DateUtil.JODA_DATE_FORMATE));
			paymentTransactionDTO.setPaymentHandledByUser(authDTO.getUser());
			paymentTransactionDTO.setUser(preTransactionDTO.getUser());
			paymentTransactionDTO.setTdsTax(BigDecimal.ZERO);
			paymentTransactionDTO.setAcBusTax(BigDecimal.ZERO);
			paymentTransactionDTO.setRemarks("Online Recharge: Rs" + preTransactionDTO.getAmount() + " include PG charge:Rs." + preTransactionDTO.getServiceCharge());

			// Update User Balance
			UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
			userTransactionDTO.setTransactionAmount(paymentTransactionDTO.getTransactionAmount());
			userTransactionDTO.setCreditAmount(paymentTransactionDTO.getTransactionAmount());
			userTransactionDTO.setDebitAmount(BigDecimal.ZERO);
			userTransactionDTO.setTdsTax(BigDecimal.ZERO);

			userTransactionDTO.setTransactionMode(paymentTransactionDTO.getTransactionMode());
			userTransactionDTO.setTransactionType(paymentTransactionDTO.getTransactionType());
			userTransactionDTO.setRefferenceCode(paymentTransactionDTO.getCode());
			userTransactionDTO.setRefferenceId(paymentTransactionDTO.getId());

			saveConfirmRechargeOrder(authDTO, paymentGatewayTransaction, paymentTransactionDTO, userTransactionDTO);

			paymentPreTransactionDAO.updateStatusV2(authDTO, preTransactionDTO);

			smsService.sendRechargeSMS(authDTO, paymentTransactionDTO);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
		return paymentTransactionDTO;
	}

	private Map<String, String> getPaymentStatus(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDTO) {
		Map<String, String> responseMap = null;
		try {
			PaymentGatewayProviderDAO paymentGatewayProviderDAO = new PaymentGatewayProviderDAO();
			PaymentGatewayProviderDTO gatewayProviderDTO = paymentGatewayProviderDAO.getPGDetails(preTransactionDTO.getGatewayProvider().getId());
			if (gatewayProviderDTO == null) {
				throw new PaymentResponseException(ErrorCode.NO_GATEWAY_FOUND);
			}

			// Identify PG
			PGInterface gatewayInstance = GatewayFactory.returnPgInstance(gatewayProviderDTO.getServiceName());
			if (gatewayInstance == null) {
				throw new PaymentResponseException(ErrorCode.NO_GATEWAY_FOUND);
			}

			// Gateway Credentials
			PaymentMerchantGatewayCredentialsDAO merchantGatewayDetails = new PaymentMerchantGatewayCredentialsDAO();
			PaymentGatewayCredentialsDTO gatewayCredentials = merchantGatewayDetails.getNamespacePGCredentials(authDTO.getNamespace(), preTransactionDTO.getGatewayProvider().getId());

			if (gatewayCredentials.getId() == 0) {
				throw new PaymentRequestException(ErrorCode.NO_GATEWAY_FOUND);
			}

			OrderDTO transactionDetails = new OrderDTO();
			transactionDetails.setOrderCode(preTransactionDTO.getOrderCode());
			transactionDetails.setGatewayTransactionCode(preTransactionDTO.getGatewayTransactionCode());
			transactionDetails.setGatewayCredentials(gatewayCredentials);

			// M2M Verifications
			responseMap = gatewayInstance.verifyPaymentOrder(authDTO, transactionDetails);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw new ServiceException(e.getMessage());
		}
		return responseMap;
	}

	private void saveConfirmRechargeOrder(AuthDTO authDTO, PaymentGatewayTransactionDTO paymentGatewayTransaction, PaymentTransactionDTO paymentTransactionDTO, UserTransactionDTO userTransactionDTO) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);

				if (StringUtil.isNotNull(paymentGatewayTransaction.getCode())) {
					PaymentGatewayTransactionDAO gatewayTransaction = new PaymentGatewayTransactionDAO();
					gatewayTransaction.insert(connection, authDTO, paymentGatewayTransaction);
				}

				PaymentTransactionDAO paymentTransactionDAO = new PaymentTransactionDAO();
				paymentTransactionDAO.saveRechargeTransaction(connection, authDTO, paymentTransactionDTO);

				userTransactionService.SaveUserTransaction(connection, authDTO, paymentTransactionDTO.getUser(), userTransactionDTO);

				paymentTransactionDTO.setUserTransaction(userTransactionDTO);
				paymentTransactionDTO.setPaymentAcknowledge(PaymentAcknowledgeEM.PAYMENT_ACKNOWLEDGED);
				paymentTransactionDTO.setActiveFlag(Numeric.ONE_INT);
				paymentTransactionDAO.getAcknowledgedPaymentTransactionUpdate(connection, authDTO, paymentTransactionDTO);
			}
			catch (SQLTransactionRollbackException e) {
				slack.sendAlert(authDTO, paymentTransactionDTO.getCode() + " DL10- Deadlock found when trying to get lock; try restarting transaction");

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
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
		}
	}

	@Override
	public void processPendingOrderAutoRefund() {
		PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();
		hirondelle.date4j.DateTime fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), Numeric.ONE_INT);

		List<NamespaceDTO> namespaces = bitsService.getBitsGatewayNamespace();
		for (NamespaceDTO namespaceDTO : namespaces) {
			AuthDTO authDTO = new AuthDTO();
			authDTO.setNamespaceCode(namespaceDTO.getCode());
			authDTO.setDeviceMedium(DeviceMediumEM.WEB_USER);
			authDTO.setUser(new UserDTO());

			List<PaymentPreTransactionDTO> transactions = paymentPreTransactionDAO.getPreTransactions(authDTO, fromDate.getStartOfDay(), fromDate.getEndOfDay());
			for (PaymentPreTransactionDTO preTransactionDTO : transactions) {
				Map<String, String> orderStatusMap = null;
				try {
					orderStatusMap = paymentOrderStatusService.getOrderStatus(authDTO, preTransactionDTO.getOrderCode(), authDTO.getNamespaceCode());
					if (StringUtil.isNotNull(orderStatusMap.get("paymentOrderStatus")) && orderStatusMap.get("paymentOrderStatus").equals(PaymentOrderEM.SUCCESS.getCode())) {
						// Save Payment Gateway Transaction
						savePaymentGatewayTransaction(authDTO, preTransactionDTO);

						// Refund
						doAutoRefundProcess(authDTO, preTransactionDTO);

						// Send SMS
						if (preTransactionDTO.getOrderType().getId() == OrderTypeEM.TICKET.getId()) {
							notificationService.sendPendingOrderCancelSMS(authDTO, preTransactionDTO);
						}
					}
				}
				catch (Exception e) {
					System.out.println("Pending Order Auto Refund Failed: " + preTransactionDTO.getOrderCode() + " PG Status : " + orderStatusMap + " Error : " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void refundOrder(AuthDTO authDTO, RefundDTO refundDTO) {
		try {
			PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();

			PaymentPreTransactionDTO preTransactionDTO = new PaymentPreTransactionDTO();
			preTransactionDTO.setOrderCode(refundDTO.getOrderCode());

			if (refundDTO.getOrderCode().equals(refundDTO.getTransactionCode())) {
				paymentPreTransactionDAO.getPreTransactionForTicket(authDTO, preTransactionDTO);
			}
			else {
				paymentPreTransactionDAO.getPreTransactionForTransaction(authDTO, preTransactionDTO);
			}

			Map<String, String> orderStatusMap = paymentOrderStatusService.getOrderStatus(authDTO, preTransactionDTO.getOrderCode(), authDTO.getNamespaceCode());

			if (StringUtil.isNull(orderStatusMap.get("paymentOrderStatus")) || !orderStatusMap.get("paymentOrderStatus").equals(PaymentOrderEM.SUCCESS.getCode())) {
				throw new ServiceException(ErrorCode.PAYMENT_DECLINED);
			}

			preTransactionDTO.setStatus(PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED);
			preTransactionDTO.setFailureErrorCode("CAN By Admin");

			// Update payment gateway pre transaction
			paymentPreTransactionDAO.updateStatusV2(authDTO, preTransactionDTO);

			// Save Payment Gateway Transaction
			PaymentGatewayTransactionDTO gatewayTransactionDTO = savePaymentGatewayTransaction(authDTO, preTransactionDTO);

			if (!StringUtil.isBigDecimal(String.valueOf(refundDTO.getAmount())) || BigDecimal.ZERO.compareTo(refundDTO.getAmount()) < 1) {
				refundDTO.setAmount(preTransactionDTO.getAmount());
			}

			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(preTransactionDTO.getOrderCode());
			ticketService.showTicket(authDTO, ticketDTO);

			if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
				throw new ServiceException(ErrorCode.AUTO_REFUND_IMPLEMENTATION_NOT_AVAILABLE);
			}

			PaymentGatewayTransactionDAO gatewayTransaction = new PaymentGatewayTransactionDAO();
			gatewayTransactionDTO.setGatewayTransactionCode(orderStatusMap.get("referenceNumber"));
			gatewayTransaction.updateGatewayTransactionCode(authDTO, gatewayTransactionDTO);

			// Refund
			gatewayTransaction.updateGatewayTransactionCode(authDTO, gatewayTransactionDTO);
			refundService.doRefund(authDTO, refundDTO);

			// Email
			emailService.sendPendingOrderCancelMail(authDTO, refundDTO, ticketDTO);
		}
		catch (ServiceException e) {
			e.printStackTrace();
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(e.getMessage());
		}
	}

	private PaymentGatewayTransactionDTO savePaymentGatewayTransaction(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDTO) {
		PaymentGatewayTransactionDTO transactionEntity = new PaymentGatewayTransactionDTO();
		try {
			transactionEntity.setCode(preTransactionDTO.getCode());
			transactionEntity.setUser(preTransactionDTO.getUser());
			transactionEntity.setDeviceMedium(preTransactionDTO.getDeviceMedium());
			transactionEntity.setGatewayPartner(preTransactionDTO.getGatewayPartner());
			transactionEntity.setGatewayProvider(preTransactionDTO.getGatewayProvider());
			transactionEntity.setGatewayCredentials(preTransactionDTO.getGatewayCredentials());
			transactionEntity.setAmount(preTransactionDTO.getAmount());
			transactionEntity.setTransactionType(PaymentGatewayTransactionTypeEM.PAYMENT);
			transactionEntity.setOrderCode(preTransactionDTO.getOrderCode());
			transactionEntity.setStatus(PaymentGatewayStatusEM.SUCCESS);

			PaymentGatewayTransactionDAO gatewayTransaction = new PaymentGatewayTransactionDAO();
			gatewayTransaction.checkAndInsert(authDTO, transactionEntity);
		}
		catch (Exception e) {
			System.out.println("Payment Gateway Transaction Exist!");
		}
		return transactionEntity;
	}

	private void doAutoRefundProcess(AuthDTO authDTO, PaymentPreTransactionDTO preTransactionDTO) {
		RefundDTO refundDTO = new RefundDTO();
		refundDTO.setAmount(preTransactionDTO.getAmount());
		refundDTO.setOrderCode(preTransactionDTO.getOrderCode());
		refundDTO.setOrderType(preTransactionDTO.getOrderType());
		refundService.doRefund(authDTO, refundDTO);
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
				ticketDTO.getBoardingPoint().setNumber(fromStationMobile + " / " + boardingMobile);
			}
			else if (StringUtil.isNotNull(fromStationMobile) || StringUtil.isNotNull(boardingMobile)) {
				ticketDTO.getBoardingPoint().setNumber((StringUtil.isNotNull(fromStationMobile) ? fromStationMobile + " / " : "") + (StringUtil.isNotNull(boardingMobile) ? boardingMobile : ticketDTO.getBoardingPoint().getNumber()));
			}

			if (StringUtil.isNotNull(toStationMobile) && StringUtil.isNotNull(droppingMobile)) {
				ticketDTO.getDroppingPoint().setNumber(toStationMobile + " / " + droppingMobile);
			}
			else if (StringUtil.isNotNull(toStationMobile) || StringUtil.isNotNull(droppingMobile)) {
				ticketDTO.getDroppingPoint().setNumber((StringUtil.isNotNull(toStationMobile) ? toStationMobile + " / " : "") + (StringUtil.isNotNull(droppingMobile) ? droppingMobile : ticketDTO.getDroppingPoint().getNumber()));
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public List<Map<String, String>> getPaymentGatewayAnalyticsReport(AuthDTO authDTO, String fromDate, String toDate) {
		Map<String, Map<String, String>> analyticsMap = new HashMap<>();
		try {
			boolean isSuperNamespaceFlag = false;
			if (authDTO.getNamespaceCode().equals(ApplicationConfig.getServerZoneCode())) {
				isSuperNamespaceFlag = true;
			}

			PaymentGatewayPreTransactionDAO paymentPreTransactionDAO = new PaymentGatewayPreTransactionDAO();
			List<PaymentPreTransactionDTO> transactions = paymentPreTransactionDAO.getPaymentGatewayPreTransactions(authDTO, fromDate, toDate, isSuperNamespaceFlag);
			List<PaymentPreTransactionDTO> refundTransactions = paymentPreTransactionDAO.getPaymentGatewayRefundTransactions(authDTO, fromDate, toDate, isSuperNamespaceFlag);
			transactions.addAll(refundTransactions);

			List<String> refundSuccessOrders = refundTransactions.stream().map(order -> order.getOrderCode()).collect(Collectors.toList());

			PaymentGatewayProviderDAO gatewayProviderDAO = new PaymentGatewayProviderDAO();
			List<String> pendingOrderCencelledOrders = new ArrayList<>();
			AuthDTO auth = new AuthDTO();
			for (PaymentPreTransactionDTO paymentPreTransactionDTO : transactions) {
				paymentPreTransactionDTO.setNamespace(getNamespaceDTObyId(paymentPreTransactionDTO.getNamespace()));
				auth.setNamespace(paymentPreTransactionDTO.getNamespace());
				PaymentGatewayProviderDTO provider = gatewayProviderDAO.getPGDetails(paymentPreTransactionDTO.getGatewayProvider().getId());
				String linkpay = Text.NA;
				if (paymentPreTransactionDTO.getOrderType().getId() == OrderTypeEM.TICKET.getId()) {
					TicketDTO ticketDTO = new TicketDTO();
					ticketDTO.setCode(paymentPreTransactionDTO.getOrderCode());
					TicketExtraDTO ticketExtraDTO = ticketService.getTicketExtra(auth, ticketDTO);
					if (StringUtil.isNotNull(ticketExtraDTO.getLinkPay())) {
						linkpay = "LINKPAY";
					}
				}

				if (!pendingOrderCencelledOrders.isEmpty() && pendingOrderCencelledOrders.contains(paymentPreTransactionDTO.getOrderCode()) && paymentPreTransactionDTO.getTransactionType().getId() == PaymentGatewayTransactionTypeEM.REFUND.getId()) {
					paymentPreTransactionDTO.setStatus(PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED);
				}
				if ("CAN By Admin".equals(paymentPreTransactionDTO.getFailureErrorCode()) && paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() && refundSuccessOrders.contains(paymentPreTransactionDTO.getOrderCode())) {
					paymentPreTransactionDTO.setStatus(PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS);
					pendingOrderCencelledOrders.add(paymentPreTransactionDTO.getOrderCode());
				}

				String key = paymentPreTransactionDTO.getNamespace().getCode() + Text.UNDER_SCORE + provider.getCode() + Text.UNDER_SCORE + paymentPreTransactionDTO.getTransactionType().getCode() + Text.UNDER_SCORE + paymentPreTransactionDTO.getOrderType().getCode() + Text.UNDER_SCORE + linkpay;
				if (analyticsMap.get(key) == null) {
					Map<String, String> orderDetailsMap = new HashMap<>();
					orderDetailsMap.put("namespaceCode", paymentPreTransactionDTO.getNamespace().getCode());
					orderDetailsMap.put("namespaceName", paymentPreTransactionDTO.getNamespace().getName());
					orderDetailsMap.put("providerName", provider.getName());
					orderDetailsMap.put("transactionTypeCode", paymentPreTransactionDTO.getTransactionType().getCode());
					orderDetailsMap.put("transactionTypeName", paymentPreTransactionDTO.getTransactionType().getName());
					orderDetailsMap.put("orderTypeCode", paymentPreTransactionDTO.getOrderType().getCode());
					orderDetailsMap.put("orderTypeName", paymentPreTransactionDTO.getOrderType().getName());
					orderDetailsMap.put("linkpay", linkpay);

					orderDetailsMap.put("totalTransactionCount", Numeric.ONE);

					orderDetailsMap.put("webOrderInProcessCount", Numeric.ZERO);
					orderDetailsMap.put("webResponseFromPgCount", Numeric.ZERO);
					orderDetailsMap.put("webPaymentSuccessCount", Numeric.ZERO);
					orderDetailsMap.put("webOrderFailCount", Numeric.ZERO);
					orderDetailsMap.put("webOrderCancelCount", Numeric.ZERO);
					orderDetailsMap.put("webPaymentSuccessByUserCount", Numeric.ZERO);
					orderDetailsMap.put("webPaymentCancelledByUserCount", Numeric.ZERO);

					orderDetailsMap.put("apiOrderInProcessCount", Numeric.ZERO);
					orderDetailsMap.put("apiResponseFromPgCount", Numeric.ZERO);
					orderDetailsMap.put("apiPaymentSuccessCount", Numeric.ZERO);
					orderDetailsMap.put("apiOrderFailCount", Numeric.ZERO);
					orderDetailsMap.put("apiOrderCancelCount", Numeric.ZERO);
					orderDetailsMap.put("apiPaymentSuccessByUserCount", Numeric.ZERO);
					orderDetailsMap.put("apiPaymentCancelledByUserCount", Numeric.ZERO);

					orderDetailsMap.put("mobOrderInProcessCount", Numeric.ZERO);
					orderDetailsMap.put("mobResponseFromPgCount", Numeric.ZERO);
					orderDetailsMap.put("mobPaymentSuccessCount", Numeric.ZERO);
					orderDetailsMap.put("mobOrderFailCount", Numeric.ZERO);
					orderDetailsMap.put("mobOrderCancelCount", Numeric.ZERO);
					orderDetailsMap.put("mobPaymentSuccessByUserCount", Numeric.ZERO);
					orderDetailsMap.put("mobPaymentCancelledByUserCount", Numeric.ZERO);

					orderDetailsMap.put("appOrderInProcessCount", Numeric.ZERO);
					orderDetailsMap.put("appResponseFromPgCount", Numeric.ZERO);
					orderDetailsMap.put("appPaymentSuccessCount", Numeric.ZERO);
					orderDetailsMap.put("appOrderFailCount", Numeric.ZERO);
					orderDetailsMap.put("appOrderCancelCount", Numeric.ZERO);
					orderDetailsMap.put("appPaymentSuccessByUserCount", Numeric.ZERO);
					orderDetailsMap.put("appPaymentCancelledByUserCount", Numeric.ZERO);

					orderDetailsMap.put("iosOrderInProcessCount", Numeric.ZERO);
					orderDetailsMap.put("iosResponseFromPgCount", Numeric.ZERO);
					orderDetailsMap.put("iosPaymentSuccessCount", Numeric.ZERO);
					orderDetailsMap.put("iosOrderFailCount", Numeric.ZERO);
					orderDetailsMap.put("iosOrderCancelCount", Numeric.ZERO);
					orderDetailsMap.put("iosPaymentSuccessByUserCount", Numeric.ZERO);
					orderDetailsMap.put("iosPaymentCancelledByUserCount", Numeric.ZERO);

					orderDetailsMap.put("andOrderInProcessCount", Numeric.ZERO);
					orderDetailsMap.put("andResponseFromPgCount", Numeric.ZERO);
					orderDetailsMap.put("andPaymentSuccessCount", Numeric.ZERO);
					orderDetailsMap.put("andOrderFailCount", Numeric.ZERO);
					orderDetailsMap.put("andOrderCancelCount", Numeric.ZERO);
					orderDetailsMap.put("andPaymentSuccessByUserCount", Numeric.ZERO);
					orderDetailsMap.put("andPaymentCancelledByUserCount", Numeric.ZERO);

					orderDetailsMap.put("tabOrderInProcessCount", Numeric.ZERO);
					orderDetailsMap.put("tabResponseFromPgCount", Numeric.ZERO);
					orderDetailsMap.put("tabPaymentSuccessCount", Numeric.ZERO);
					orderDetailsMap.put("tabOrderFailCount", Numeric.ZERO);
					orderDetailsMap.put("tabOrderCancelCount", Numeric.ZERO);
					orderDetailsMap.put("tabPaymentSuccessByUserCount", Numeric.ZERO);
					orderDetailsMap.put("tabPaymentCancelledByUserCount", Numeric.ZERO);

					orderDetailsMap.put("otherOrderInProcessCount", Numeric.ZERO);
					orderDetailsMap.put("otherResponseFromPgCount", Numeric.ZERO);
					orderDetailsMap.put("otherPaymentSuccessCount", Numeric.ZERO);
					orderDetailsMap.put("otherOrderFailCount", Numeric.ZERO);
					orderDetailsMap.put("otherOrderCancelCount", Numeric.ZERO);
					orderDetailsMap.put("otherPaymentSuccessByUserCount", Numeric.ZERO);
					orderDetailsMap.put("otherPaymentCancelledByUserCount", Numeric.ZERO);

					if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId()) {
						orderDetailsMap.put("apiOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("apiResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("apiPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("apiOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("apiOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("apiPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("apiPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
					}
					else if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.APP_AND.getId()) {
						orderDetailsMap.put("andOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("andResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("andPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("andOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("andOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("andPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("andPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
					}
					else if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.APP_IOS.getId()) {
						orderDetailsMap.put("iosOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("iosResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("iosPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("iosOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("iosOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("iosPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("iosPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
					}
					else if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.APP_TABLET_POB.getId()) {
						orderDetailsMap.put("tabOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("tabResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("tabPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("tabOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("tabOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("tabPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("tabPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
					}
					else if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.APP_USER.getId()) {
						orderDetailsMap.put("appOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("appResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("appPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("appOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("appOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("appPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("appPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
					}
					else if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.MOB_USER.getId()) {
						orderDetailsMap.put("mobOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("mobResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("mobPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("mobOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("mobOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("mobPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("mobPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
					}
					else if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.WEB_USER.getId()) {
						orderDetailsMap.put("webOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("webResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("webPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("webOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("webOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("webPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("webPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
					}
					else {
						orderDetailsMap.put("otherOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("otherResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("otherPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("otherOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("otherOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("otherPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? Numeric.ONE : Numeric.ZERO);
						orderDetailsMap.put("otherPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? Numeric.ONE : Numeric.ZERO);
					}

					analyticsMap.put(key, orderDetailsMap);
				}
				else {
					Map<String, String> orderDetailsMap = analyticsMap.get(key);
					int totalTransactionCount = Integer.valueOf(orderDetailsMap.get("totalTransactionCount"));

					int webOrderInProcessCount = Integer.valueOf(orderDetailsMap.get("webOrderInProcessCount"));
					int webResponseFromPgCount = Integer.valueOf(orderDetailsMap.get("webResponseFromPgCount"));
					int webPaymentSuccessCount = Integer.valueOf(orderDetailsMap.get("webPaymentSuccessCount"));
					int webOrderFailCount = Integer.valueOf(orderDetailsMap.get("webOrderFailCount"));
					int webOrderCancelCount = Integer.valueOf(orderDetailsMap.get("webOrderCancelCount"));
					int webPaymentSuccessByUserCount = Integer.valueOf(orderDetailsMap.get("webPaymentSuccessByUserCount"));
					int webPaymentCancelledByUserCount = Integer.valueOf(orderDetailsMap.get("webPaymentCancelledByUserCount"));

					int apiOrderInProcessCount = Integer.valueOf(orderDetailsMap.get("apiOrderInProcessCount"));
					int apiResponseFromPgCount = Integer.valueOf(orderDetailsMap.get("apiResponseFromPgCount"));
					int apiPaymentSuccessCount = Integer.valueOf(orderDetailsMap.get("apiPaymentSuccessCount"));
					int apiOrderFailCount = Integer.valueOf(orderDetailsMap.get("apiOrderFailCount"));
					int apiOrderCancelCount = Integer.valueOf(orderDetailsMap.get("apiOrderCancelCount"));
					int apiPaymentSuccessByUserCount = Integer.valueOf(orderDetailsMap.get("apiPaymentSuccessByUserCount"));
					int apiPaymentCancelledByUserCount = Integer.valueOf(orderDetailsMap.get("apiPaymentCancelledByUserCount"));

					int mobOrderInProcessCount = Integer.valueOf(orderDetailsMap.get("mobOrderInProcessCount"));
					int mobResponseFromPgCount = Integer.valueOf(orderDetailsMap.get("mobResponseFromPgCount"));
					int mobPaymentSuccessCount = Integer.valueOf(orderDetailsMap.get("mobPaymentSuccessCount"));
					int mobOrderFailCount = Integer.valueOf(orderDetailsMap.get("mobOrderFailCount"));
					int mobOrderCancelCount = Integer.valueOf(orderDetailsMap.get("mobOrderCancelCount"));
					int mobPaymentSuccessByUserCount = Integer.valueOf(orderDetailsMap.get("mobPaymentSuccessByUserCount"));
					int mobPaymentCancelledByUserCount = Integer.valueOf(orderDetailsMap.get("mobPaymentCancelledByUserCount"));

					int appOrderInProcessCount = Integer.valueOf(orderDetailsMap.get("appOrderInProcessCount"));
					int appResponseFromPgCount = Integer.valueOf(orderDetailsMap.get("appResponseFromPgCount"));
					int appPaymentSuccessCount = Integer.valueOf(orderDetailsMap.get("appPaymentSuccessCount"));
					int appOrderFailCount = Integer.valueOf(orderDetailsMap.get("appOrderFailCount"));
					int appOrderCancelCount = Integer.valueOf(orderDetailsMap.get("appOrderCancelCount"));
					int appPaymentSuccessByUserCount = Integer.valueOf(orderDetailsMap.get("appPaymentSuccessByUserCount"));
					int appPaymentCancelledByUserCount = Integer.valueOf(orderDetailsMap.get("appPaymentCancelledByUserCount"));

					int iosOrderInProcessCount = Integer.valueOf(orderDetailsMap.get("iosOrderInProcessCount"));
					int iosResponseFromPgCount = Integer.valueOf(orderDetailsMap.get("iosResponseFromPgCount"));
					int iosPaymentSuccessCount = Integer.valueOf(orderDetailsMap.get("iosPaymentSuccessCount"));
					int iosOrderFailCount = Integer.valueOf(orderDetailsMap.get("iosOrderFailCount"));
					int iosOrderCancelCount = Integer.valueOf(orderDetailsMap.get("iosOrderCancelCount"));
					int iosPaymentSuccessByUserCount = Integer.valueOf(orderDetailsMap.get("iosPaymentSuccessByUserCount"));
					int iosPaymentCancelledByUserCount = Integer.valueOf(orderDetailsMap.get("iosPaymentCancelledByUserCount"));

					int andOrderInProcessCount = Integer.valueOf(orderDetailsMap.get("andOrderInProcessCount"));
					int andResponseFromPgCount = Integer.valueOf(orderDetailsMap.get("andResponseFromPgCount"));
					int andPaymentSuccessCount = Integer.valueOf(orderDetailsMap.get("andPaymentSuccessCount"));
					int andOrderFailCount = Integer.valueOf(orderDetailsMap.get("andOrderFailCount"));
					int andOrderCancelCount = Integer.valueOf(orderDetailsMap.get("andOrderCancelCount"));
					int andPaymentSuccessByUserCount = Integer.valueOf(orderDetailsMap.get("andPaymentSuccessByUserCount"));
					int andPaymentCancelledByUserCount = Integer.valueOf(orderDetailsMap.get("andPaymentCancelledByUserCount"));

					int tabOrderInProcessCount = Integer.valueOf(orderDetailsMap.get("tabOrderInProcessCount"));
					int tabResponseFromPgCount = Integer.valueOf(orderDetailsMap.get("tabResponseFromPgCount"));
					int tabPaymentSuccessCount = Integer.valueOf(orderDetailsMap.get("tabPaymentSuccessCount"));
					int tabOrderFailCount = Integer.valueOf(orderDetailsMap.get("tabOrderFailCount"));
					int tabOrderCancelCount = Integer.valueOf(orderDetailsMap.get("tabOrderCancelCount"));
					int tabPaymentSuccessByUserCount = Integer.valueOf(orderDetailsMap.get("tabPaymentSuccessByUserCount"));
					int tabPaymentCancelledByUserCount = Integer.valueOf(orderDetailsMap.get("tabPaymentCancelledByUserCount"));

					int otherOrderInProcessCount = Integer.valueOf(orderDetailsMap.get("otherOrderInProcessCount"));
					int otherResponseFromPgCount = Integer.valueOf(orderDetailsMap.get("otherResponseFromPgCount"));
					int otherPaymentSuccessCount = Integer.valueOf(orderDetailsMap.get("otherPaymentSuccessCount"));
					int otherOrderFailCount = Integer.valueOf(orderDetailsMap.get("otherOrderFailCount"));
					int otherOrderCancelCount = Integer.valueOf(orderDetailsMap.get("otherOrderCancelCount"));
					int otherPaymentSuccessByUserCount = Integer.valueOf(orderDetailsMap.get("otherPaymentSuccessByUserCount"));
					int otherPaymentCancelledByUserCount = Integer.valueOf(orderDetailsMap.get("otherPaymentCancelledByUserCount"));

					orderDetailsMap.put("totalTransactionCount", String.valueOf(totalTransactionCount + 1));

					if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId()) {
						orderDetailsMap.put("apiOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? String.valueOf(apiOrderInProcessCount + 1) : String.valueOf(apiOrderInProcessCount));
						orderDetailsMap.put("apiResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? String.valueOf(apiResponseFromPgCount + 1) : String.valueOf(apiResponseFromPgCount));
						orderDetailsMap.put("apiPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? String.valueOf(apiPaymentSuccessCount + 1) : String.valueOf(apiPaymentSuccessCount));
						orderDetailsMap.put("apiOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? String.valueOf(apiOrderFailCount + 1) : String.valueOf(apiOrderFailCount));
						orderDetailsMap.put("apiOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? String.valueOf(apiOrderCancelCount + 1) : String.valueOf(apiOrderCancelCount));
						orderDetailsMap.put("apiPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? String.valueOf(apiPaymentSuccessByUserCount + 1) : String.valueOf(apiPaymentSuccessByUserCount));
						orderDetailsMap.put("apiPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? String.valueOf(apiPaymentCancelledByUserCount + 1) : String.valueOf(apiPaymentCancelledByUserCount));
					}
					else if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.APP_AND.getId()) {
						orderDetailsMap.put("andOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? String.valueOf(andOrderInProcessCount + 1) : String.valueOf(andOrderInProcessCount));
						orderDetailsMap.put("andResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? String.valueOf(andResponseFromPgCount + 1) : String.valueOf(andResponseFromPgCount));
						orderDetailsMap.put("andPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? String.valueOf(andPaymentSuccessCount + 1) : String.valueOf(andPaymentSuccessCount));
						orderDetailsMap.put("andOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? String.valueOf(andOrderFailCount + 1) : String.valueOf(andOrderFailCount));
						orderDetailsMap.put("andOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? String.valueOf(andOrderCancelCount + 1) : String.valueOf(andOrderCancelCount));
						orderDetailsMap.put("andPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? String.valueOf(andPaymentSuccessByUserCount + 1) : String.valueOf(andPaymentSuccessByUserCount));
						orderDetailsMap.put("andPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? String.valueOf(andPaymentCancelledByUserCount + 1) : String.valueOf(andPaymentCancelledByUserCount));
					}
					else if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.APP_IOS.getId()) {
						orderDetailsMap.put("iosOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? String.valueOf(iosOrderInProcessCount + 1) : String.valueOf(iosOrderInProcessCount));
						orderDetailsMap.put("iosResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? String.valueOf(iosResponseFromPgCount + 1) : String.valueOf(iosResponseFromPgCount));
						orderDetailsMap.put("iosPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? String.valueOf(iosPaymentSuccessCount + 1) : String.valueOf(iosPaymentSuccessCount));
						orderDetailsMap.put("iosOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? String.valueOf(iosOrderFailCount + 1) : String.valueOf(iosOrderFailCount));
						orderDetailsMap.put("iosOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? String.valueOf(iosOrderCancelCount + 1) : String.valueOf(iosOrderCancelCount));
						orderDetailsMap.put("iosPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? String.valueOf(iosPaymentSuccessByUserCount + 1) : String.valueOf(iosPaymentSuccessByUserCount));
						orderDetailsMap.put("iosPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? String.valueOf(iosPaymentCancelledByUserCount + 1) : String.valueOf(iosPaymentCancelledByUserCount));
					}
					else if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.APP_TABLET_POB.getId()) {
						orderDetailsMap.put("tabOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? String.valueOf(tabOrderInProcessCount + 1) : String.valueOf(tabOrderInProcessCount));
						orderDetailsMap.put("tabResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? String.valueOf(tabResponseFromPgCount + 1) : String.valueOf(tabResponseFromPgCount));
						orderDetailsMap.put("tabPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? String.valueOf(tabPaymentSuccessCount + 1) : String.valueOf(tabPaymentSuccessCount));
						orderDetailsMap.put("tabOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? String.valueOf(tabOrderFailCount + 1) : String.valueOf(tabOrderFailCount));
						orderDetailsMap.put("tabOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? String.valueOf(tabOrderCancelCount + 1) : String.valueOf(tabOrderCancelCount));
						orderDetailsMap.put("tabPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? String.valueOf(tabPaymentSuccessByUserCount + 1) : String.valueOf(tabPaymentSuccessByUserCount));
						orderDetailsMap.put("tabPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? String.valueOf(tabPaymentCancelledByUserCount + 1) : String.valueOf(tabPaymentCancelledByUserCount));
					}
					else if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.APP_USER.getId()) {
						orderDetailsMap.put("appOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? String.valueOf(appOrderInProcessCount + 1) : String.valueOf(appOrderInProcessCount));
						orderDetailsMap.put("appResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? String.valueOf(appResponseFromPgCount + 1) : String.valueOf(appResponseFromPgCount));
						orderDetailsMap.put("appPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? String.valueOf(appPaymentSuccessCount + 1) : String.valueOf(appPaymentSuccessCount));
						orderDetailsMap.put("appOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? String.valueOf(appOrderFailCount + 1) : String.valueOf(appOrderFailCount));
						orderDetailsMap.put("appOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? String.valueOf(appOrderCancelCount + 1) : String.valueOf(appOrderCancelCount));
						orderDetailsMap.put("appPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? String.valueOf(appPaymentSuccessByUserCount + 1) : String.valueOf(appPaymentSuccessByUserCount));
						orderDetailsMap.put("appPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? String.valueOf(appPaymentCancelledByUserCount + 1) : String.valueOf(appPaymentCancelledByUserCount));
					}
					else if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.MOB_USER.getId()) {
						orderDetailsMap.put("mobOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? String.valueOf(mobOrderInProcessCount + 1) : String.valueOf(mobOrderInProcessCount));
						orderDetailsMap.put("mobResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? String.valueOf(mobResponseFromPgCount + 1) : String.valueOf(mobResponseFromPgCount));
						orderDetailsMap.put("mobPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? String.valueOf(mobPaymentSuccessCount + 1) : String.valueOf(mobPaymentSuccessCount));
						orderDetailsMap.put("mobOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? String.valueOf(mobOrderFailCount + 1) : String.valueOf(mobOrderFailCount));
						orderDetailsMap.put("mobOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? String.valueOf(mobOrderCancelCount + 1) : String.valueOf(mobOrderCancelCount));
						orderDetailsMap.put("mobPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? String.valueOf(mobPaymentSuccessByUserCount + 1) : String.valueOf(mobPaymentSuccessByUserCount));
						orderDetailsMap.put("mobPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? String.valueOf(mobPaymentCancelledByUserCount + 1) : String.valueOf(mobPaymentCancelledByUserCount));
					}
					else if (paymentPreTransactionDTO.getDeviceMedium().getId() == DeviceMediumEM.WEB_USER.getId()) {
						orderDetailsMap.put("webOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? String.valueOf(webOrderInProcessCount + 1) : String.valueOf(webOrderInProcessCount));
						orderDetailsMap.put("webResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? String.valueOf(webResponseFromPgCount + 1) : String.valueOf(webResponseFromPgCount));
						orderDetailsMap.put("webPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? String.valueOf(webPaymentSuccessCount + 1) : String.valueOf(webPaymentSuccessCount));
						orderDetailsMap.put("webOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? String.valueOf(webOrderFailCount + 1) : String.valueOf(webOrderFailCount));
						orderDetailsMap.put("webOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? String.valueOf(webOrderCancelCount + 1) : String.valueOf(webOrderCancelCount));
						orderDetailsMap.put("webPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? String.valueOf(webPaymentSuccessByUserCount + 1) : String.valueOf(webPaymentSuccessByUserCount));
						orderDetailsMap.put("webPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? String.valueOf(webPaymentCancelledByUserCount + 1) : String.valueOf(webPaymentCancelledByUserCount));
					}
					else {
						orderDetailsMap.put("otherOrderInProcessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_INITIATED.getId() ? String.valueOf(otherOrderInProcessCount + 1) : String.valueOf(otherOrderInProcessCount));
						orderDetailsMap.put("otherResponseFromPgCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_PG_RESPONSE.getId() ? String.valueOf(otherResponseFromPgCount + 1) : String.valueOf(otherResponseFromPgCount));
						orderDetailsMap.put("otherPaymentSuccessCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.SUCCESS.getId() ? String.valueOf(otherPaymentSuccessCount + 1) : String.valueOf(otherPaymentSuccessCount));
						orderDetailsMap.put("otherOrderFailCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.FAILURE.getId() ? String.valueOf(otherOrderFailCount + 1) : String.valueOf(otherOrderFailCount));
						orderDetailsMap.put("otherOrderCancelCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.ORDER_CANCELLED.getId() ? String.valueOf(otherOrderCancelCount + 1) : String.valueOf(otherOrderCancelCount));
						orderDetailsMap.put("otherPaymentSuccessByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_SUCCESS.getId() ? String.valueOf(otherPaymentSuccessByUserCount + 1) : String.valueOf(otherPaymentSuccessByUserCount));
						orderDetailsMap.put("otherPaymentCancelledByUserCount", paymentPreTransactionDTO.getStatus().getId() == PaymentGatewayStatusEM.PENDING_ORDER_CANCELLED.getId() ? String.valueOf(otherPaymentCancelledByUserCount + 1) : String.valueOf(otherPaymentCancelledByUserCount));
					}
					analyticsMap.put(key, orderDetailsMap);
				}
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return new ArrayList<>(analyticsMap.values());
	}
}
