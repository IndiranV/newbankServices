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
import java.util.Stack;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.in.com.aggregator.backup.DRService;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.slack.SlackService;
import org.in.com.aggregator.sms.SMSService;
import org.in.com.cache.CacheCentral;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.TicketCache;
import org.in.com.cache.TripCache;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ConnectDAO;
import org.in.com.dao.PaymentMerchantGatewayScheduleDAO;
import org.in.com.dao.TicketDAO;
import org.in.com.dao.TicketEditDAO;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.CommissionDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.OrderInitRequestDTO;
import org.in.com.dto.PaymentGatewayScheduleDTO;
import org.in.com.dto.PaymentGatewayTransactionDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDiscountDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.TicketTransferDTO;
import org.in.com.dto.TicketTransferDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.UserTransactionDTO;
import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.CommissionTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.MinutesTypeEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.PNRGenerateTypeEM;
import org.in.com.dto.enumeration.PaymentGatewayTransactionTypeEM;
import org.in.com.dto.enumeration.PaymentTypeEM;
import org.in.com.dto.enumeration.RefundStatusEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuditService;
import org.in.com.service.AuthService;
import org.in.com.service.BlockSeatsHelperService;
import org.in.com.service.BusService;
import org.in.com.service.BusmapService;
import org.in.com.service.CancelTicketHelperService;
import org.in.com.service.CancellationTermsService;
import org.in.com.service.CommissionService;
import org.in.com.service.GroupService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.ScheduleTicketTransferTermsService;
import org.in.com.service.TicketService;
import org.in.com.service.TicketTransferService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.service.UserTransactionService;
import org.in.com.service.UtilService;
import org.in.com.service.pg.PaymentRequestService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.in.com.utils.TokenGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Maps;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class TicketTransferImpl extends CacheCentral implements TicketTransferService {
	private static final Logger LOGGER = LoggerFactory.getLogger("tickettransferlogger");

	@Autowired
	BusmapService busmapService;
	@Autowired
	TicketService ticketService;
	@Autowired
	CancellationTermsService cancellationTermsService;
	@Autowired
	UserTransactionService transactionService;
	@Autowired
	TripService tripService;
	@Autowired
	SMSService smsService;
	@Autowired
	EmailService emailService;
	@Autowired
	DRService drService;
	@Autowired
	AuditService auditService;
	@Autowired
	ScheduleTicketTransferTermsService scheduleTicketTransferTermsService;
	@Autowired
	CancelTicketHelperService ticketHelperService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	SlackService slack;
	@Autowired
	UtilService utilService;
	@Autowired
	BlockSeatsHelperService blockSeatsHelperService;
	@Autowired
	PaymentRequestService paymentRequestService;
	@Autowired
	ScheduleDynamicStageFareService dynamicFareService;
	@Autowired
	CommissionService commissionService;
	@Autowired
	UserService userService;
	@Autowired
	GroupService groupService;
	@Autowired
	AuthService authService;
	@Autowired
	BusService busService;
	@Autowired
	NotificationPushService notificationPushService;

	@Override
	public TicketDTO transferTicket(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO transferDTO, Map<String, Boolean> additionalAttribute) {
		try {
			TicketDTO repoTicketDTO = new TicketDTO();
			repoTicketDTO.setCode(ticketDTO.getCode());
			Stack<TicketDetailsDTO> stack = new Stack<TicketDetailsDTO>();

			ticketService.getTicketStatus(authDTO, repoTicketDTO);
			// auditService.addTicketAuditFullLog(authDTO, repoTicketDTO);
			if (repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.POSTPONE.getId() || repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId()) {
				transferDTO.setLookupId(repoTicketDTO.getLookupId());
			}
			else {
				transferDTO.setLookupId(repoTicketDTO.getId());
			}

			TripDTO oldTripDTO = repoTicketDTO.getTripDTO();

			TripCache tripCache = new TripCache();
			oldTripDTO = tripCache.getTripDTO(authDTO, oldTripDTO);

			List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
			for (Iterator<TicketDetailsDTO> iterator = repoTicketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
				TicketDetailsDTO detailsDTO = iterator.next();
				if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
						if (ticketDetailsDTO.getSeatCode().equals(detailsDTO.getSeatCode())) {
							seatDetailsList.add(detailsDTO);
							stack.push(detailsDTO);
							break;
						}
					}
				}
				else {
					iterator.remove();
				}
			}

			if (seatDetailsList.size() != ticketDTO.getTicketDetails().size()) {
				LOGGER.error("{} ErrorCode 308 DB Old Seats {} Given Old Seats {}", repoTicketDTO.getCode(), seatDetailsList.stream().map(repoTicket -> repoTicket.getSeatCode()).collect(Collectors.toList()), ticketDTO.getTicketDetails().stream().map(ticket -> ticket.getSeatCode()).collect(Collectors.toList()));
				throw new ServiceException(ErrorCode.SELECTED_STEAT_NOT_FOR_RESERVATION);
			}
			if (seatDetailsList.size() != transferDTO.getTicketDetails().size()) {
				LOGGER.error("{} ErrorCode 308 Old Seats {} Transferred Seats {}", repoTicketDTO.getCode(), seatDetailsList.stream().map(repoTicket -> repoTicket.getSeatCode()).collect(Collectors.toList()), transferDTO.getTicketDetails().stream().map(ticket -> ticket.getSeatCode()).collect(Collectors.toList()));
				throw new ServiceException(ErrorCode.SELECTED_STEAT_NOT_FOR_RESERVATION);
			}
			// copy Seat status and name
			Map<String, TicketDetailsDTO> seatCodes = new HashMap<>();
			for (TicketDetailsDTO transferSeatDetailsDTO : transferDTO.getTicketDetails()) {
				TicketDetailsDTO ticketDetailsDTO = stack.pop();
				transferSeatDetailsDTO.setSeatGendar(ticketDetailsDTO.getSeatGendar());
				transferSeatDetailsDTO.setTicketStatus(ticketDetailsDTO.getTicketStatus());
				transferSeatDetailsDTO.setPassengerAge(ticketDetailsDTO.getPassengerAge());
				transferSeatDetailsDTO.setPassengerName(ticketDetailsDTO.getPassengerName());
				seatCodes.put(transferSeatDetailsDTO.getSeatCode(), ticketDetailsDTO);
			}

			TripDTO tripDTO = getTripDetails(authDTO, transferDTO, repoTicketDTO);

			// To Check Free Ticket
			checkApplyFreeService(authDTO, repoTicketDTO, transferDTO, additionalAttribute);

			// Schedule Ticket Transfer Terms
			ScheduleCache scheduleCache = new ScheduleCache();
			ScheduleDTO oldSchedule = scheduleCache.getScheduleDTObyId(authDTO, oldTripDTO.getSchedule());
			oldSchedule.setTripDate(oldTripDTO.getTripDate());
			repoTicketDTO.setFromStation(getStationDTObyId(repoTicketDTO.getFromStation()));
			repoTicketDTO.setToStation(getStationDTObyId(repoTicketDTO.getToStation()));
			ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO = scheduleTicketTransferTermsService.getScheduleTicketTransferTermsBySchedule(authDTO, oldSchedule, repoTicketDTO.getFromStation(), repoTicketDTO.getToStation());

			if (scheduleTicketTransferTermsDTO != null && !additionalAttribute.get("FREE_TICKET")) {
				if (scheduleTicketTransferTermsDTO.getAllowBookedUser() == 1 && repoTicketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
					throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Only the booked user can reschedule the ticket");
				}

				repoTicketDTO.getTicketUser().setGroup(groupService.getGroup(authDTO, repoTicketDTO.getTicketUser().getGroup()));
				if (!scheduleTicketTransferTermsDTO.getBookedUserGroups().isEmpty() && existGroupInGroupList(scheduleTicketTransferTermsDTO.getBookedUserGroups(), repoTicketDTO.getTicketUser().getGroup()) == null) {
					throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED_TO_GROUP);
				}

				if (scheduleTicketTransferTermsDTO.getMinutes() != 0) {
					oldTripDTO.setSchedule(oldSchedule);
					List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, oldTripDTO);
					DateTime originStationDateTime = BitsUtil.getTicketTransferDateTime(authDTO, scheduleTicketTransferTermsDTO, repoTicketDTO.getTripDateTime(), BitsUtil.getOriginStationTime(stageList, oldTripDTO.getTripDate()));

					// Validate Minutes
					if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.MINUTES.getId()) {
						scheduleTicketTransferTermsDTO.setDateTime(DateUtil.minusMinituesToDate(originStationDateTime, scheduleTicketTransferTermsDTO.getMinutes()));
						if (scheduleTicketTransferTermsDTO != null && DateUtil.NOW().gt(scheduleTicketTransferTermsDTO.getDateTime())) {
							LOGGER.error("{} ErrorCode 319 | 1 {}", repoTicketDTO.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
							throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
						}
					}
					else if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.AM.getId()) {
						DateTime checkTime = DateUtil.addMinituesToDate(oldTripDTO.getTripDate().getStartOfDay(), scheduleTicketTransferTermsDTO.getMinutes());
						Integer check = DateUtil.getMinutiesDifferent(DateUtil.NOW(), checkTime);
						if (check >= 0) {
							LOGGER.error("{} ErrorCode 319 | 1a {}", repoTicketDTO.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
							throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
						}
					}
					else if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.PM.getId()) {
						DateTime checkTime = DateUtil.addMinituesToDate(oldTripDTO.getTripDate().getStartOfDay(), 720 + scheduleTicketTransferTermsDTO.getMinutes());
						Integer check = DateUtil.getMinutiesDifferent(DateUtil.NOW(), checkTime);
						if (check < 0) {
							LOGGER.error("{} ErrorCode 319 | 1b {}", repoTicketDTO.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
							throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
						}
					}
				}
			}

			// Station Point Mapping
			for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
				if (transferDTO.getBoardingPoint() != null) {
					if (pointDTO.getCode().equals(transferDTO.getBoardingPoint().getCode())) {
						transferDTO.setBoardingPoint(pointDTO);
						transferDTO.getBoardingPoint().setMinitues(tripDTO.getStage().getFromStation().getMinitues() + pointDTO.getMinitues());
						break;
					}
				}
				else if (repoTicketDTO.getBoardingPoint() != null) {
					if (pointDTO.getCode().equals(repoTicketDTO.getBoardingPoint().getCode())) {
						transferDTO.setBoardingPoint(pointDTO);
						transferDTO.getBoardingPoint().setMinitues(tripDTO.getStage().getFromStation().getMinitues() + pointDTO.getMinitues());
						break;
					}
				}
			}
			if (transferDTO.getBoardingPoint().getId() == 0) {
				throw new ServiceException(ErrorCode.STATION_POINT_CODE_INVALID);
			}
			// Station Dropping Point Mapping
			for (StationPointDTO pointDTO : tripDTO.getStage().getToStation().getStationPoint()) {
				if (transferDTO.getDroppingPoint() != null) {
					if (pointDTO.getCode().equals(transferDTO.getDroppingPoint().getCode())) {
						transferDTO.setDroppingPoint(pointDTO);
						transferDTO.getDroppingPoint().setMinitues(tripDTO.getStage().getToStation().getMinitues() + pointDTO.getMinitues());
						break;
					}
				}
				else if (repoTicketDTO.getDroppingPoint() != null) {
					if (pointDTO.getCode().equals(repoTicketDTO.getDroppingPoint().getCode())) {
						transferDTO.setDroppingPoint(pointDTO);
						transferDTO.getDroppingPoint().setMinitues(tripDTO.getStage().getToStation().getMinitues() + pointDTO.getMinitues());
						break;
					}
				}
			}
			if (transferDTO.getDroppingPoint() == null || transferDTO.getDroppingPoint().getId() == 0) {
				transferDTO.setDroppingPoint(tripDTO.getStage().getToStation().getStationPoint().get(tripDTO.getStage().getToStation().getStationPoint().size() - 1));
			}
			transferDTO.setTripDTO(tripDTO);
			int reportingMin = transferDTO.getBoardingPoint().getMinitues() - authDTO.getNamespace().getProfile().getBoardingReportingMinitues();
			transferDTO.setTravelMinutes(tripDTO.getStage().getFromStation().getMinitues());
			transferDTO.setReportingMinutes(reportingMin);
			transferDTO.setFromStation(tripDTO.getStage().getFromStation().getStation());
			transferDTO.setToStation(tripDTO.getStage().getToStation().getStation());
			transferDTO.setServiceNo(tripDTO.getSchedule().getServiceNumber());
			transferDTO.setTicketAt(DateUtil.NOW());

			// validate the From,To and Trip Date
			validateTicketTransfer(authDTO, repoTicketDTO, transferDTO);

			// Same Day Travel Date, just migrating seat Details
			if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId() && !additionalAttribute.get("captureFareDifferece") && !additionalAttribute.get("captureTransferCharge") && tripDTO.getTripDate().compareTo(repoTicketDTO.getTripDate()) == 0) {
				applyScheduleTicketTransferTerms(authDTO, transferDTO, scheduleTicketTransferTermsDTO, additionalAttribute);
				transferDTO = migrateTicketDetails(authDTO, repoTicketDTO, transferDTO);

				// retain the status for DPE
				repoTicketDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
				for (TicketDetailsDTO seatDetailsDTO : repoTicketDTO.getTicketDetails()) {
					seatDetailsDTO.setTicketStatus(repoTicketDTO.getTicketStatus());
				}
			}
			else {
				/** Multiple Reschedule Permission Check */
				if (!authDTO.isMultipleRescheduleEnabled() && (repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.POSTPONE.getId() || repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId())) {
					LOGGER.error("{} ErrorCode 319 | 002", repoTicketDTO.getCode());
					throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED);
				}
				/** Validate Reschedule Count */
				if (authDTO.isMultipleRescheduleEnabled() && authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId()) {
					validateTicketRescheduleMaxCount(authDTO, transferDTO);
				}
				if (repoTicketDTO.getFromStation().getId() != tripDTO.getStage().getFromStation().getStation().getId() && repoTicketDTO.getFromStation().getId() != tripDTO.getStage().getToStation().getStation().getId()) {
					LOGGER.error("{} ErrorCode PG14 01 {}", repoTicketDTO.getCode());
					throw new ServiceException(ErrorCode.MANDATORY_PARAMETERS_MISSING);
				}
				DateTime previousDate = DateUtil.NOW().minusDays(authDTO.getNamespace().getProfile().getRescheduleOverrideAllowDays());
				if (authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && previousDate.gt(oldTripDTO.getTripDateTimeV2())) {
					LOGGER.error("{} ErrorCode TR01 {}", repoTicketDTO.getCode(), oldTripDTO.getTripDateTimeV2());
					throw new ServiceException(ErrorCode.TRIP_CLOSED_NOT_ALLOW_BOOKING);
				}
				// Transfer ticket
				transferDTO.setJourneyType(JourneyTypeEM.POSTPONE);
				if (tripDTO.getTripDate().compareTo(repoTicketDTO.getTripDate()) == -1) {
					transferDTO.setJourneyType(JourneyTypeEM.PREPONE);
				}
				repoTicketDTO.setJourneyType(transferDTO.getJourneyType());
				transferDTO.setDeviceMedium(repoTicketDTO.getDeviceMedium());
				transferDTO.setPassengerEmailId(repoTicketDTO.getPassengerEmailId());
				transferDTO.setPassengerMobile(repoTicketDTO.getPassengerMobile());
				transferDTO.setAlternateMobile(repoTicketDTO.getAlternateMobile());
				transferDTO = TransferTicket(authDTO, repoTicketDTO, transferDTO, scheduleTicketTransferTermsDTO, additionalAttribute, seatCodes);
			}
			// Clear Trip block Seats
			tripService.clearBookedBlockedSeatsCache(authDTO, repoTicketDTO.getTripDTO());
			tripService.clearBookedBlockedSeatsCache(authDTO, transferDTO.getTripDTO());
			tripService.clearBookedBlockedSeatsCache(authDTO, oldTripDTO);

			// Update API Ticket
			if (authDTO.getNamespace().getProfile().isAliasNamespaceFlag()) {
				List<NamespaceDTO> aliasNamespaceList = getAliasNamespaceList(authDTO);
				for (NamespaceDTO namespace : aliasNamespaceList) {
					ticketHelperService.notifyTicketTransfer(authDTO, namespace, repoTicketDTO);
				}
			}
			else {
				ticketHelperService.notifyTicketTransfer(authDTO, null, repoTicketDTO);
			}

			// Send SMS
			if (additionalAttribute != null && additionalAttribute.get("notification") != null && additionalAttribute.get("notification")) {
				notificationService.sendTicketUpdateSMS(authDTO, transferDTO);
			}
			// Dynamic Pricing Call back
			if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
				authDTO.getAdditionalAttribute().put(Text.TRANSFER_BOOKING_CANCEL, "Transfer");
				dynamicFareService.updateTicketStatus(authDTO, transferDTO);
				dynamicFareService.updateTicketStatus(authDTO, repoTicketDTO);
			}

		}
		catch (ServiceException e) {
			LOGGER.error("{} {}", ticketDTO.getCode(), e);
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("{} {}", ticketDTO.getCode(), e);
			throw new ServiceException(ErrorCode.UNABLE_TO_TRANSFER_TICKET);
		}
		return transferDTO;
	}

	private void applyScheduleTicketTransferTerms(AuthDTO authDTO, TicketDTO transferDTO, ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO, Map<String, Boolean> additionalAttribute) {
		List<TicketAddonsDetailsDTO> discountList = new ArrayList<>();
		if (scheduleTicketTransferTermsDTO != null && !additionalAttribute.get("FREE_TICKET")) {
			if (additionalAttribute.get("captureTransferCharge")) {
				for (TicketDetailsDTO ticketDetailsDTO : transferDTO.getTicketDetails()) {
					TicketAddonsDetailsDTO transferChargeDetailsDTO = new TicketAddonsDetailsDTO();
					if (scheduleTicketTransferTermsDTO.getChargeType().getId() == FareTypeEM.PERCENTAGE.getId()) {
						transferChargeDetailsDTO.setValue(ticketDetailsDTO.getSeatFare().divide(Numeric.ONE_HUNDRED).multiply(scheduleTicketTransferTermsDTO.getChargeAmount()));
					}
					else if (scheduleTicketTransferTermsDTO.getChargeType().getId() == FareTypeEM.FLAT.getId()) {
						transferChargeDetailsDTO.setValue(scheduleTicketTransferTermsDTO.getChargeAmount());
					}
					transferChargeDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
					transferChargeDetailsDTO.setAddonsType(AddonsTypeEM.TICKET_TRANSFER_CHARGE);
					transferChargeDetailsDTO.setActiveFlag(1);
					transferChargeDetailsDTO.setRefferenceId(scheduleTicketTransferTermsDTO.getId());
					transferChargeDetailsDTO.setRefferenceCode(Text.NA);
					discountList.add(transferChargeDetailsDTO);
				}
			}
		}
		transferDTO.setTicketAddonsDetails(discountList);
	}

	private void recalculateServiceTaxAmount(AuthDTO authDTO, TicketDTO repoTicketDTO, TicketDTO transferDTO, Map<String, TicketDetailsDTO> seatCodes) {
		List<TicketAddonsDetailsDTO> addonsDetailsList = new ArrayList<>();
		for (TicketDetailsDTO transferTicketDetails : transferDTO.getTicketDetails()) {
			TicketDetailsDTO ticketDetailsDTO = seatCodes.get(transferTicketDetails.getSeatCode());
//			BigDecimal seatAddonAmount = repoTicketDTO.getAddonsValue(ticketDetailsDTO);
			BigDecimal seatAddonAmount = BigDecimal.ZERO;

			transferTicketDetails.setAcBusTax((transferTicketDetails.getSeatFare().subtract(seatAddonAmount)).divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(transferDTO.getTripDTO().getSchedule().getTax().getServiceTax()));

			if (seatAddonAmount.compareTo(BigDecimal.ZERO) < 1) {
				continue;
			}
			TicketAddonsDetailsDTO ticketAddonsDetails = new TicketAddonsDetailsDTO();
			ticketAddonsDetails.setSeatCode(transferTicketDetails.getSeatCode());
			ticketAddonsDetails.setRefferenceId(ticketDetailsDTO.getId());
			ticketAddonsDetails.setRefferenceCode(repoTicketDTO.getCode());
			ticketAddonsDetails.setActiveFlag(Numeric.ONE_INT);
			ticketAddonsDetails.setValue(seatAddonAmount);
			ticketAddonsDetails.setAddonsType(AddonsTypeEM.DISCOUNT_AMOUNT);
			addonsDetailsList.add(ticketAddonsDetails);
		}

		if (transferDTO.getTicketAddonsDetails() == null && !addonsDetailsList.isEmpty()) {
			transferDTO.setTicketAddonsDetails(addonsDetailsList);
		}
		else if (transferDTO.getTicketAddonsDetails() != null && !addonsDetailsList.isEmpty()) {
			transferDTO.getTicketAddonsDetails().addAll(addonsDetailsList);
		}
	}

	private void validateTicketTransfer(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO transferDTO) {
		// Once one pre/post pone allowed
		if (authDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId() && transferDTO.getTripDTO().getTripDate().compareTo(ticketDTO.getTripDate()) != 0 && ticketDTO.getJourneyType().getId() != JourneyTypeEM.ONWARD_TRIP.getId() && ticketDTO.getJourneyType().getId() != JourneyTypeEM.RETURN_TRIP.getId()) {
			LOGGER.error("{} ErrorCode 319 | 2", ticketDTO.getCode());
			throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED);
		}

		int condition = 0;
		if (ticketDTO.getFromStation().getId() == transferDTO.getFromStation().getId()) {
			condition++;
		}
		if (ticketDTO.getToStation().getId() == transferDTO.getToStation().getId()) {
			condition++;
		}

		if (ticketDTO.getTripDate().compareTo(transferDTO.getTripDTO().getTripDate()) == 0) {
			condition++;
		}

		if (condition < 2) {
			LOGGER.error("{} ErrorCode PG14 02 {}", ticketDTO.getCode());
			throw new ServiceException(ErrorCode.MANDATORY_PARAMETERS_MISSING);
		}
	}

	private TicketDTO TransferTicket(AuthDTO authDTO, TicketDTO repoTicketDTO, TicketDTO transferDTO, ScheduleTicketTransferTermsDTO scheduleTicketTransferTerms, Map<String, Boolean> additionalAttribute, Map<String, TicketDetailsDTO> seatCodes) {
		TripDAO tripDAO = new TripDAO();

		/** Get trip stage seat details & convert ticket extra */
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, repoTicketDTO);
		Map<String, TicketExtraDTO> repoTicketExtraMap = new HashMap<String, TicketExtraDTO>();
		for (TicketDetailsDTO detailsDTO : seatList) {
			repoTicketExtraMap.put(detailsDTO.getSeatCode(), detailsDTO.getTicketExtra());
			detailsDTO.setActiveFlag(0);
		}

		/** Cancellation term */
		CancellationTermDTO cancellationTermDTO = cancellationTermsService.getCancellationTermsByTripDTO(authDTO, authDTO.getUser(), transferDTO.getTripDTO());
		if (cancellationTermDTO == null) {
			LOGGER.error("{} ErrorCode CA01 | 1", repoTicketDTO.getCode());
			throw new ServiceException(ErrorCode.CANCELLATION_TERMS_NOT_FOUND);
		}
		cancellationTermsService.getCancellationTermGroupIdByGroupKey(authDTO, cancellationTermDTO);
		transferDTO.setCancellationTerm(cancellationTermDTO);

		transferDTO.setTicketUser(repoTicketDTO.getTicketUser());
		transferDTO.setTicketForUser(repoTicketDTO.getTicketForUser());
		transferDTO.setFromStation(transferDTO.getTripDTO().getStage().getFromStation().getStation());
		transferDTO.setToStation(transferDTO.getTripDTO().getStage().getToStation().getStation());
		transferDTO.setTripDate(transferDTO.getTripDTO().getTripDate());

		if (StringUtil.isNull(repoTicketDTO.getRemarks())) {
			String remarks = "Transfered from " + StringUtil.substring(repoTicketDTO.getFromStation().getName(), 3) + " - " + StringUtil.substring(repoTicketDTO.getToStation().getName(), 3) + "@" + repoTicketDTO.getTripDateTime().format(Text.DATE_TIME_DATE4J) + " :" + repoTicketDTO.getTripDTO().getCode() + "#:" + repoTicketDTO.getSeatNames();
			transferDTO.setRemarks(StringUtil.substring(remarks, 240));
		}
		/** Check seat duplicate entry */
		boolean seatDouplicateEntry = ticketService.checkSeatDuplicateEntryV2(authDTO, transferDTO);
		if (seatDouplicateEntry) {
			LOGGER.error("{} ErrorCode 611 | 1", repoTicketDTO.getCode());
			throw new ServiceException(ErrorCode.DOUBLE_ENTRY_VALIDATION_FAIL);
		}

		if (!authDTO.isMultipleRescheduleEnabled() && StringUtil.isNull(transferDTO.getCode())) {
			transferDTO.setCode(repoTicketDTO.getCode() + "-" + repoTicketDTO.getJourneyType().getCode());
		}
		else if (StringUtil.isNull(transferDTO.getCode())) {
			// Generate ticket code
			if (authDTO.getNamespace().getProfile().getPnrGenerateType().getId() == PNRGenerateTypeEM.ENCODE_ALPHANUMERIC.getId()) {
				String PNRstartCode = StringUtil.isNotNull(transferDTO.getPnrStartCode()) ? StringUtil.removeSymbol(transferDTO.getPnrStartCode()).length() == 3 ? transferDTO.getPnrStartCode() : authDTO.getNamespace().getProfile().getPnrStartCode() : authDTO.getNamespace().getProfile().getPnrStartCode();
				String ticketCode = TokenGenerator.generateCode(PNRstartCode);
				transferDTO.setCode(ticketCode);
			}
			else if (authDTO.getNamespace().getProfile().getPnrGenerateType().getId() == PNRGenerateTypeEM.SEQUENCE_NUMBERIC.getId()) {
				String ticketCode = utilService.getGenerateSequenceNumber(authDTO);
				transferDTO.setCode(ticketCode);

				boolean status = ticketService.checkDuplicateTicketCodeEntry(authDTO, transferDTO.getCode());
				if (status) {
					LOGGER.error("{} ErrorCode 611 | 4", repoTicketDTO.getCode());
					throw new ServiceException(ErrorCode.DOUPLICATE_ENTRY_VALIDATION_FAIL);
				}
			}
		}
		if (StringUtil.isNull(transferDTO.getCode())) {
			transferDTO.setBookingCode(transferDTO.getCode());
		}

		/** Check ticket duplicate entry */
		if (StringUtil.isNotNull(transferDTO.getCode())) {
			boolean status = ticketService.checkDuplicateTicketCodeEntry(authDTO, transferDTO.getCode());
			if (status) {
				LOGGER.error("{} ErrorCode 611 | 2", repoTicketDTO.getCode());
				throw new ServiceException(ErrorCode.DOUPLICATE_ENTRY_VALIDATION_FAIL);
			}
		}

		transferDTO.setTicketStatus(repoTicketDTO.getTicketStatus());
		transferDTO.setTicketAt(DateTime.now(TimeZone.getDefault()));

		BookingDTO bookingDTO = new BookingDTO();
		bookingDTO.setCode(transferDTO.getCode());

		int fareDifferenceType = fareDifferenceType(repoTicketDTO, transferDTO);
		if ((fareDifferenceType == 2 && additionalAttribute.get("captureFareDifferece") && !additionalAttribute.get("captureLowFareDifferece")) || (!additionalAttribute.get("captureFareDifferece"))) {
			for (TicketDetailsDTO ticketDetailsDTO : transferDTO.getTicketDetails()) {
				TicketDetailsDTO repoTicketDetails = seatCodes.get(ticketDetailsDTO.getSeatCode());
				ticketDetailsDTO.setSeatFare(repoTicketDetails.getSeatFare());
				ticketDetailsDTO.setAcBusTax(repoTicketDetails.getAcBusTax());
			}
		}

		/** Apply schedule ticket transfer terms */
		applyScheduleTicketTransferTerms(authDTO, transferDTO, scheduleTicketTransferTerms, additionalAttribute);

		/** Previous Ticket Addon */
		BigDecimal transferTicketAddon = getTransferTicketAddon(repoTicketDTO, transferDTO, additionalAttribute);
		if (transferTicketAddon.compareTo(BigDecimal.ZERO) == 1) {
			BigDecimal transferSeatAddon = transferTicketAddon.divide(BigDecimal.valueOf(transferDTO.getTicketDetails().size()), RoundingMode.CEILING);
			for (TicketDetailsDTO transferTicketDetails : transferDTO.getTicketDetails()) {
				TicketDetailsDTO ticketDetailsDTO = seatCodes.get(transferTicketDetails.getSeatCode());
				TicketAddonsDetailsDTO ticketAddonsDetails = new TicketAddonsDetailsDTO();
				ticketAddonsDetails.setSeatCode(transferTicketDetails.getSeatCode());
				ticketAddonsDetails.setRefferenceId(ticketDetailsDTO.getId());
				ticketAddonsDetails.setRefferenceCode(repoTicketDTO.getCode());
				ticketAddonsDetails.setActiveFlag(Numeric.ONE_INT);
				ticketAddonsDetails.setValue(transferSeatAddon);
				ticketAddonsDetails.setAddonsType(AddonsTypeEM.TRANSFER_PREVIOUS_TICKET_AMOUNT);
				transferDTO.getTicketAddonsDetails().add(ticketAddonsDetails);
			}
		}

		/** Re-calculate service tax - GST for discount */
		recalculateServiceTaxAmount(authDTO, repoTicketDTO, transferDTO, seatCodes);

		BigDecimal debitAmount = BigDecimal.ZERO;
		String toPayAmount = Text.EMPTY;
		if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId() && additionalAttribute.get("captureFareDifferece")) {
			debitAmount = getPayableAmount(repoTicketDTO, transferDTO, additionalAttribute.get("captureTransferCharge"));
			if (debitAmount.compareTo(BigDecimal.ZERO) == 1) {
				toPayAmount = " Topay " + debitAmount.setScale(0, RoundingMode.HALF_UP);
			}
		}
		if (StringUtil.isNotNull(toPayAmount)) {
			transferDTO.setRemarks(composeTicketRemarks(transferDTO.getRemarks(), toPayAmount));
		}

		/** Get ticket transaction */
		TicketTransactionDTO previousTicketTransaction = new TicketTransactionDTO();
		List<UserTransactionDTO> previousUserTransactions = new ArrayList<>();
		if (repoTicketDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && additionalAttribute.get("captureFareDifferece") && fareDifferenceType == 1 && repoTicketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
			checkAndUpdateTicketTransactionsV2(authDTO, repoTicketDTO, transferDTO, previousTicketTransaction, previousUserTransactions);
		}
		else if (repoTicketDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
			checkAndUpdateTicketTransactions(authDTO, repoTicketDTO, transferDTO, previousUserTransactions);
		}

		/** Change old ticket status */
		for (TicketDetailsDTO ticketDetailsDTO : repoTicketDTO.getTicketDetails()) {
			ticketDetailsDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
			ticketDetailsDTO.setTicketExtra(repoTicketExtraMap.get(ticketDetailsDTO.getSeatCode()));
			ticketDetailsDTO.getTicketExtra().setNetAmount(BigDecimal.ZERO);
		}
		repoTicketDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
		for (TicketAddonsDetailsDTO addonsDetailsDTO : repoTicketDTO.getTicketAddonsDetails()) {
			addonsDetailsDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
		}

		/** Change ticket addons status */
		if (transferDTO.getTicketAddonsDetails() != null && !transferDTO.getTicketAddonsDetails().isEmpty()) {
			for (TicketAddonsDetailsDTO discountDetailsDTO : transferDTO.getTicketAddonsDetails()) {
				discountDetailsDTO.setTicketStatus(transferDTO.getTicketStatus());
			}
		}

		if (transferDTO.getTicketExtra() != null) {
			transferDTO.getTicketExtra().setTicketTransfer(1);
		}
		else {
			transferDTO.setTicketExtra(new TicketExtraDTO());
			transferDTO.getTicketExtra().setTicketTransfer(1);
		}

		/** save ticket, and it's transaction */
		saveTicket(authDTO, bookingDTO, repoTicketDTO, transferDTO, previousTicketTransaction, previousUserTransactions);

		/** Notifications */
		if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) && transferDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
			smsService.sendMTicketTransferSMS(authDTO, transferDTO);
		}

		/** Push Notification */
		if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.RESCHEDULE)) {
			notificationPushService.pushTicketRescheduleNotification(authDTO, transferDTO, additionalAttribute);
		}
		/** Upload to backup server */
		drService.flushTicketDetails(authDTO, transferDTO);

		tripService.clearBookedBlockedSeatsCache(authDTO, repoTicketDTO.getTripDTO());
		tripService.clearBookedBlockedSeatsCache(authDTO, transferDTO.getTripDTO());
		return transferDTO;
	}

	private TicketDTO migrateTicketDetails(AuthDTO authDTO, TicketDTO repoTicketDTO, TicketDTO transferDTO) {
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setId(repoTicketDTO.getId());
		ticketDTO.setCode(repoTicketDTO.getCode());
		ticketDTO.setServiceNo(repoTicketDTO.getServiceNo());

		TripDAO tripDAO = new TripDAO();
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, ticketDTO);
		Stack<TicketDetailsDTO> transferstack = new Stack<TicketDetailsDTO>();
		transferstack.addAll(transferDTO.getTicketDetails());
		List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
		List<TicketDetailsDTO> oldSeatDetailsList = new ArrayList<TicketDetailsDTO>();
		// Change Trip Stage Seat Details
		StringBuilder migrateSeats = new StringBuilder();
		StringBuilder oldSeats = new StringBuilder();
		StringBuilder newSeats = new StringBuilder();
		for (TicketDetailsDTO detailsDTO : seatList) {
			if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				for (TicketDetailsDTO ticketDetailsDTO : repoTicketDTO.getTicketDetails()) {
					if (ticketDetailsDTO.getSeatCode().equals(detailsDTO.getSeatCode())) {

						TicketDetailsDTO transferDetailsDTO = transferstack.pop();
						TicketDetailsDTO transferOldSeatDetails = new TicketDetailsDTO();
						oldSeats.append((oldSeats.length() > 0 ? ", " : "") + ticketDetailsDTO.getSeatName());
						newSeats.append((newSeats.length() > 0 ? ", " : "") + transferDetailsDTO.getSeatName());
						detailsDTO.setSeatGendar(detailsDTO.getSeatGendar());
						detailsDTO.setTicketStatus(detailsDTO.getTicketStatus());
						detailsDTO.setSeatCode(transferDetailsDTO.getSeatCode());
						detailsDTO.setSeatName(transferDetailsDTO.getSeatName());
						detailsDTO.setSeatType(ticketDetailsDTO.getSeatType());
						transferOldSeatDetails.setId(ticketDetailsDTO.getId());
						transferOldSeatDetails.setSeatCode(transferDetailsDTO.getSeatCode());
						transferOldSeatDetails.setSeatName(transferDetailsDTO.getSeatName());
						transferOldSeatDetails.setTicketStatus(transferDetailsDTO.getTicketStatus());
						detailsDTO.setBoardingPointName(transferDTO.getBoardingPoint().getName());
						detailsDTO.setStationPoint(BitsUtil.convertStationPoint(transferDTO.getBoardingPoint(), transferDTO.getDroppingPoint()));
						detailsDTO.getTicketExtra().setEditChangeSeat(1);
						seatDetailsList.add(detailsDTO);
						oldSeatDetailsList.add(transferOldSeatDetails);
						break;
					}
				}
			}
		}

		boolean seatDouplicateEntry = ticketService.checkSeatDuplicateEntryV2(authDTO, transferDTO);
		if (seatDouplicateEntry) {
			LOGGER.error("{} ErrorCode 611 | 2", ticketDTO.getCode());
			throw new ServiceException(ErrorCode.DOUBLE_ENTRY_VALIDATION_FAIL);
		}

		if (StringUtil.isNull(repoTicketDTO.getRemarks())) {
			String remarks = "Transfered from " + StringUtil.substring(repoTicketDTO.getFromStation().getName(), 3) + " - " + StringUtil.substring(repoTicketDTO.getToStation().getName(), 3) + " @ " + repoTicketDTO.getTripDateTime().format("YY-MM-DD hh:mm") + " tripCode: " + repoTicketDTO.getTripDTO().getCode() + "#:" + repoTicketDTO.getSeatNames();
			ticketDTO.setRemarks(StringUtil.substring(remarks, 240));
		}
		migrateSeats.append("(" + ticketDTO.getServiceNo() + ") " + oldSeats + " - " + "(" + transferDTO.getServiceNo() + ") " + newSeats);
		migrateSeats.append(Text.COMMA + Text.SINGLE_SPACE + Text.NEW_LINE);
		if (repoTicketDTO.getTripDTO().getCode().equals(transferDTO.getTripDTO().getCode())) {
			migrateSeats.append("TC: " + repoTicketDTO.getTripDTO().getCode() + " -> " + transferDTO.getTripDTO().getCode() + Text.COMMA + Text.NEW_LINE);
		}
		// Migrate From Station
		if (repoTicketDTO.getFromStation().getId() != transferDTO.getFromStation().getId()) {
			migrateSeats.append("From: " + repoTicketDTO.getFromStation().getName() + " -> " + transferDTO.getFromStation().getName() + Text.COMMA + Text.NEW_LINE);
		}
		ticketDTO.setFromStation(transferDTO.getFromStation());

		// Migrate To Station
		if (repoTicketDTO.getToStation().getId() != transferDTO.getToStation().getId()) {
			migrateSeats.append("To: " + repoTicketDTO.getToStation().getName() + " -> " + transferDTO.getToStation().getName() + Text.COMMA + Text.NEW_LINE);
		}
		ticketDTO.setToStation(transferDTO.getToStation());

		if (repoTicketDTO.getBoardingPoint().getId() != transferDTO.getBoardingPoint().getId()) {
			migrateSeats.append("Boarding: " + StringUtil.substring(repoTicketDTO.getBoardingPoint().getName(), Numeric.SIX_INT) + " -> " + StringUtil.substring(transferDTO.getBoardingPoint().getName(), Numeric.SIX_INT) + Text.COMMA + Text.NEW_LINE);
		}
		if (repoTicketDTO.getDroppingPoint().getId() != transferDTO.getDroppingPoint().getId()) {
			migrateSeats.append("Dropping: " + StringUtil.substring(repoTicketDTO.getDroppingPoint().getName(), Numeric.SIX_INT) + " -> " + StringUtil.substring(transferDTO.getDroppingPoint().getName(), Numeric.SIX_INT) + Text.COMMA + Text.NEW_LINE);
		}
		ticketDTO.setTicketUser(repoTicketDTO.getTicketUser());
		ticketDTO.setDeviceMedium(repoTicketDTO.getDeviceMedium());
		ticketDTO.setTicketStatus(repoTicketDTO.getTicketStatus());
		ticketDTO.setTripDTO(transferDTO.getTripDTO());
		int reportingMin = repoTicketDTO.getBoardingPoint().getMinitues() - authDTO.getNamespace().getProfile().getBoardingReportingMinitues();
		ticketDTO.setTravelMinutes(transferDTO.getTripDTO().getStage().getFromStation().getMinitues());
		ticketDTO.setReportingMinutes(reportingMin);
		ticketDTO.setBoardingPoint(transferDTO.getBoardingPoint());
		ticketDTO.setDroppingPoint(transferDTO.getDroppingPoint());
		ticketDTO.setTripDate(repoTicketDTO.getTripDate());
		ticketDTO.setTicketAt(repoTicketDTO.getTicketAt());
		ticketDTO.setServiceNo(transferDTO.getServiceNo());
		ticketDTO.setTicketDetails(oldSeatDetailsList);
		// Save data
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);

				// update Ticket table
				TicketEditDAO editDAO = new TicketEditDAO();
				editDAO.editMigrateTicket(authDTO, ticketDTO, connection, migrateSeats.toString());

				// update Trip stage seat Details table
				ticketDTO.setTicketDetails(seatDetailsList);

				tripService.updateTripTransferSeatDetails(connection, authDTO, ticketDTO);
			}
			catch (SQLTransactionRollbackException e) {
				slack.sendAlert(authDTO, ticketDTO.getCode() + " DL03 - " + " -  Deadlock found when trying to get lock; try restarting transaction");

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
			LOGGER.error("{} {}", ticketDTO.getCode(), e);
			throw new ServiceException(ErrorCode.UNABLE_TO_TRANSFER_TICKET);
		}
		return ticketDTO;

	}

	private TripDTO getTripDetails(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO repoTicketDTO) {
		TripDTO returnTripDTO = null;
		returnTripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());
		if (returnTripDTO == null || returnTripDTO.getBus() == null || returnTripDTO.getBus().getBusSeatLayoutDTO() == null) {
			SearchDTO searchDTO = ticketDTO.getTripDTO().getSearch();
			System.out.println("Error:0098A0 " + searchDTO.getFromStation().getCode() + "-" + searchDTO.getToStation().getCode() + "-" + searchDTO.getTravelDate());
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
		List<BusSeatLayoutDTO> seatList = returnTripDTO.getSchedule().getScheduleBus().getBus().getBusSeatLayoutDTO().getList();
		for (BusSeatLayoutDTO dto : seatList) {
			seatCodeMap.put(dto.getCode(), dto.getLayer() + "_" + dto.getRowPos() + "_" + dto.getColPos());
			if (dto.getSeatStatus() != null) {
				seatGenderMap.put(dto.getLayer() + "_" + dto.getRowPos() + "_" + dto.getColPos(), dto);
			}
		}

		// Get seatFare
		for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
			BusSeatLayoutDTO seatLayoutDTO = fareMap.get(ticketDetailsDTO.getSeatCode());
			if (seatLayoutDTO == null) {
				LOGGER.error("{} ErrorCode BO01 Seat Code {}", repoTicketDTO.getCode(), ticketDetailsDTO.getSeatCode());
				throw new ServiceException(ErrorCode.SEAT_ALREADY_BLOCKED);
			}
			ticketDetailsDTO.setSeatFare(seatLayoutDTO.getFare());
			ticketDetailsDTO.setAcBusTax(seatLayoutDTO.getFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(returnTripDTO.getSchedule().getTax().getServiceTax()));
			ticketDetailsDTO.setSeatCode(seatLayoutDTO.getCode());
			ticketDetailsDTO.setSeatName(seatLayoutDTO.getName());
			ticketDetailsDTO.setSeatType(seatLayoutDTO.getBusSeatType().getCode());
			ticketDetailsDTO.setActiveFlag(1);

			/** adjacent seat validation */

			String seatPos = seatCodeMap.get(ticketDetailsDTO.getSeatCode());
			if (seatPos != null) {
				int layer = Integer.parseInt(seatPos.split("_")[0]);
				int rowCount = Integer.parseInt(seatPos.split("_")[1]);
				int colCount = Integer.parseInt(seatPos.split("_")[2]);
				// Female Seat validations
				if (ticketDetailsDTO.getSeatGendar().getId() == SeatGendarEM.FEMALE.getId()) {
					// Right Side seat
					if (seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId()) && !layoutDTO.isAllowBlockedSeatBooking()) {
							LOGGER.error("{} ErrorCode 405 | 1 Seat Code {} ( {} ) {}", repoTicketDTO.getCode(), layoutDTO.getCode(), layoutDTO.getName(), layoutDTO.getSeatStatus().getCode());
							throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
						}
					}
					// Left Side seat
					else if (seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId()) && !layoutDTO.isAllowBlockedSeatBooking()) {
							LOGGER.error("{} ErrorCode 405 | 2 Seat Code {} ( {} ) {}", repoTicketDTO.getCode(), layoutDTO.getCode(), layoutDTO.getName(), layoutDTO.getSeatStatus().getCode());
							throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
						}
					}
				}
				// Male Seat validations
				else if (ticketDetailsDTO.getSeatGendar().getId() == SeatGendarEM.MALE.getId()) {
					// Right Side seat
					if (seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId()) && !layoutDTO.isAllowBlockedSeatBooking()) {
							LOGGER.error("{} ErrorCode 405 | 3 Seat Code {} ( {} ) {}", repoTicketDTO.getCode(), layoutDTO.getCode(), layoutDTO.getName(), layoutDTO.getSeatStatus().getCode());
							throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
						}
					}
					// Left Side seat
					else if (seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId()) && !layoutDTO.isAllowBlockedSeatBooking()) {
							LOGGER.error("{} ErrorCode 405 | 4 Seat Code {} ( {} ) {}", repoTicketDTO.getCode(), layoutDTO.getCode(), layoutDTO.getName(), layoutDTO.getSeatStatus().getCode());
							throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
						}
					}
				}
			}

			// Seat Preference
			if (seatLayoutDTO.getSeatGendar() != null && !seatLayoutDTO.isAllowBlockedSeatBooking()) {
				if (seatLayoutDTO.getSeatGendar().getId() != ticketDetailsDTO.getSeatGendar().getId()) {
					LOGGER.error("{} ErrorCode 405A Bus Layout Seat {} ( {} ) {} Transferred Seat {} ( {} ) {}", repoTicketDTO.getCode(), seatLayoutDTO.getCode(), seatLayoutDTO.getName(), seatLayoutDTO.getSeatGendar().getCode(), ticketDetailsDTO.getCode(), ticketDetailsDTO.getName(), ticketDetailsDTO.getSeatGendar().getCode());
					throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER_PREFERENCE);
				}
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
		return returnTripDTO;
	}

	private void saveTicket(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO, TicketDTO transferTicketDTO, TicketTransactionDTO previousTicketTransaction, List<UserTransactionDTO> previousUserTransactions) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);
				TicketDAO dao = new TicketDAO();

				String oldPnrEvent = "Old PNR: " + ticketDTO.getCode() + Text.COMMA + Text.SINGLE_SPACE;
				/** Insert Ticket table */
				dao.insertTicket(authDTO, bookingDTO, transferTicketDTO, connection, oldPnrEvent);

				if (transferTicketDTO.getId() == 0) {
					throw new ServiceException(ErrorCode.UNABLE_TO_TRANSFER_TICKET);
				}

				/** Insert Ticket Details table */
				dao.insertTicketDetails(authDTO, transferTicketDTO, connection);

				/** Insert Ticket Addons Details table */
				dao.insertTicketAddonsDetails(authDTO, transferTicketDTO, connection);

				tripService.SaveBookedBlockedSeats(connection, authDTO, transferTicketDTO);

				if (transferTicketDTO.getTicketXaction() != null && transferTicketDTO.getUserTransaction() != null) {
					transferTicketDTO.setTicketUser(transferTicketDTO.getTicketXaction().getUserDTO());
					ticketService.saveTicketTransaction(connection, authDTO, transferTicketDTO);

					if (transferTicketDTO.getTicketUser().getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId()) {
						transferTicketDTO.getUserTransaction().setRefferenceId(transferTicketDTO.getTicketXaction().getId());
						transactionService.SaveUserTransaction(connection, authDTO, transferTicketDTO.getTicketUser(), transferTicketDTO.getUserTransaction());
					}
				}
				String event = composeAuditEvent(ticketDTO, transferTicketDTO);
				ticketDTO.setRemarks(StringUtil.substring(event, 240));

				/** Update old ticket status */
				ticketService.UpdateTicketStatus(connection, authDTO, ticketDTO);

				tripService.updateTripSeatDetailsWithExtras(connection, authDTO, ticketDTO);

				/** Cancel Transaction For Old Ticket */
				if (ticketDTO.getTicketXaction() != null) {
					ticketService.saveTicketTransaction(connection, authDTO, ticketDTO);

					ticketService.saveTicketCancelTransaction(connection, authDTO, ticketDTO);

					ticketService.saveTicketCancellationDetails(connection, authDTO, ticketDTO);
				}
				if (previousTicketTransaction != null && !previousUserTransactions.isEmpty() && previousTicketTransaction.getActiveFlag() == 1) {
					transferTicketDTO.setTicketUser(previousTicketTransaction.getUserDTO());
					transferTicketDTO.setTicketXaction(previousTicketTransaction);
					ticketService.saveTicketTransaction(connection, authDTO, transferTicketDTO);

					for (UserTransactionDTO previousUserTransaction : previousUserTransactions) {
						if (previousUserTransaction.getUser().getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId()) {
							previousUserTransaction.setRefferenceId(transferTicketDTO.getTicketXaction().getId());
							transactionService.SaveUserTransaction(connection, authDTO, previousUserTransaction.getUser(), previousUserTransaction);
						}
					}
				}
				else if (!previousUserTransactions.isEmpty()) {
					for (UserTransactionDTO previousUserTransaction : previousUserTransactions) {
						if (previousUserTransaction.getUser().getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId()) {
							previousUserTransaction.setRefferenceId(ticketDTO.getTicketXaction().getId());
							transactionService.SaveUserTransaction(connection, authDTO, previousUserTransaction.getUser(), previousUserTransaction);
						}
					}
				}
			}
			catch (SQLTransactionRollbackException e) {
				slack.sendAlert(authDTO, ticketDTO.getCode() + " DL02 - Deadlock found when trying to get lock; try restarting transaction");

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
			LOGGER.error("{} {} {}", ticketDTO.getCode(), transferTicketDTO.getCode(), e);
			throw new ServiceException(ErrorCode.UNABLE_TO_TRANSFER_TICKET);
		}
	}

	@Override
	public ScheduleTicketTransferTermsDTO isTransferTicket(AuthDTO authDTO, TicketDTO ticketDTO) {
		ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO = null;
		// Ticket Details
		ticketService.getTicketStatus(authDTO, ticketDTO);
		/** Multiple Reschedule Permission Check */
		if (!authDTO.isMultipleRescheduleEnabled() && (ticketDTO.getJourneyType().getId() == JourneyTypeEM.POSTPONE.getId() || ticketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId())) {
			LOGGER.error("{} ErrorCode 319 | 002", ticketDTO.getCode());
			throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED);
		}

		if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() && (ticketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId() || ticketDTO.getJourneyType().getId() == JourneyTypeEM.RETURN_TRIP.getId())) {
			// Get Ticket Trip
			TripCache tripCache = new TripCache();
			TripDTO tripDTO = tripCache.getTripDTO(authDTO, ticketDTO.getTripDTO());

			if (authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && DateUtil.NOW().gt(tripDTO.getTripDateTimeV2())) {
				LOGGER.error("{} ErrorCode TR01 {}", ticketDTO.getCode(), tripDTO.getTripDateTimeV2());
				throw new ServiceException(ErrorCode.TRIP_CLOSED_NOT_ALLOW_BOOKING);
			}

			// Get Ticket Trip Schedule
			ScheduleCache scheduleCache = new ScheduleCache();
			ScheduleDTO scheduleDTO = scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule());
			scheduleDTO.setTripDate(tripDTO.getTripDate());

			// Schedule Ticket Transfer Terms
			ticketDTO.setFromStation(getStationDTObyId(ticketDTO.getFromStation()));
			ticketDTO.setToStation(getStationDTObyId(ticketDTO.getToStation()));
			scheduleTicketTransferTermsDTO = scheduleTicketTransferTermsService.getScheduleTicketTransferTermsBySchedule(authDTO, scheduleDTO, ticketDTO.getFromStation(), ticketDTO.getToStation());

			if (scheduleTicketTransferTermsDTO == null) {
				LOGGER.error("{} ErrorCode 319 | 3", ticketDTO.getCode());
				throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED);
			}
			if (scheduleTicketTransferTermsDTO.getAllowBookedUser() == 1 && ticketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
				throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Only the booked user can reschedule the ticket");
			}
			ticketDTO.getTicketUser().setGroup(groupService.getGroup(authDTO, ticketDTO.getTicketUser().getGroup()));
			if (!scheduleTicketTransferTermsDTO.getBookedUserGroups().isEmpty() && existGroupInGroupList(scheduleTicketTransferTermsDTO.getBookedUserGroups(), ticketDTO.getTicketUser().getGroup()) == null) {
				throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED_TO_GROUP);
			}

			if (scheduleTicketTransferTermsDTO.getMinutes() != 0) {
				tripDTO.setSchedule(scheduleDTO);
				List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, tripDTO);
				DateTime originStationDateTime = BitsUtil.getTicketTransferDateTime(authDTO, scheduleTicketTransferTermsDTO, ticketDTO.getTripDateTime(), BitsUtil.getOriginStationTime(stageList, tripDTO.getTripDate()));

				// Validate Minutes
				if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.MINUTES.getId()) {
					scheduleTicketTransferTermsDTO.setDateTime(DateUtil.minusMinituesToDate(originStationDateTime, scheduleTicketTransferTermsDTO.getMinutes()));
					if (scheduleTicketTransferTermsDTO != null && DateUtil.NOW().gt(scheduleTicketTransferTermsDTO.getDateTime())) {
						LOGGER.error("{} ErrorCode 319 | 1 {}", ticketDTO.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
						throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
					}
				}
				else if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.AM.getId()) {
					DateTime checkTime = DateUtil.addMinituesToDate(tripDTO.getTripDate().getStartOfDay(), scheduleTicketTransferTermsDTO.getMinutes());
					scheduleTicketTransferTermsDTO.setDateTime(checkTime);
					Integer check = DateUtil.getMinutiesDifferent(DateUtil.NOW(), checkTime);
					if (check >= 0) {
						LOGGER.error("{} ErrorCode 319 | 1a {}", ticketDTO.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
						throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
					}
				}
				else if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.PM.getId()) {
					DateTime checkTime = DateUtil.addMinituesToDate(tripDTO.getTripDate().getStartOfDay(), 720 + scheduleTicketTransferTermsDTO.getMinutes());
					scheduleTicketTransferTermsDTO.setDateTime(checkTime);
					Integer check = DateUtil.getMinutiesDifferent(DateUtil.NOW(), checkTime);
					if (check < 0) {
						LOGGER.error("{} ErrorCode 319 | 1b {}", ticketDTO.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
						throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
					}
				}
			}
		}
		else {
			LOGGER.error("{} ErrorCode 319 | 5", ticketDTO.getCode());
			throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED);
		}
		return scheduleTicketTransferTermsDTO;
	}

	private void checkApplyFreeService(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO ticketTransferDTO, Map<String, Boolean> additionalAttribute) {
		boolean isFreeTicket = Text.FALSE;

		// FS Ticket
		for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
			if (ticketDetailsDTO.getSeatFare().compareTo(BigDecimal.ZERO) == 0 && ticketDetailsDTO.getAcBusTax().compareTo(BigDecimal.ZERO) == 0) {
				isFreeTicket = Text.TRUE;
				break;
			}
		}
		if (isFreeTicket) {
			for (TicketDetailsDTO ticketDetailsDTO : ticketTransferDTO.getTicketDetails()) {
				ticketDetailsDTO.setSeatFare(BigDecimal.ZERO);
				ticketDetailsDTO.setAcBusTax(BigDecimal.ZERO);
			}
			if (ticketTransferDTO.getTicketAddonsDetails() != null) {
				ticketTransferDTO.getTicketAddonsDetails().clear();
			}
		}
		additionalAttribute.put("FREE_TICKET", isFreeTicket);
	}

	private String composeTicketRemarks(String oldRemarks, String newRemarks) {
		String remark = oldRemarks + newRemarks;
		if (remark.length() > 240) {
			remark = oldRemarks;
		}
		return remark;
	}

	@Override
	public BookingDTO blockTicket(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO transferDTO, String paymentGatewayPartnerCode) {
		BookingDTO bookingDTO = new BookingDTO();
		try {
			TicketDTO repoTicketDTO = new TicketDTO();
			repoTicketDTO.setCode(ticketDTO.getCode());

			ticketService.getTicketStatus(authDTO, repoTicketDTO);
			/** Multiple Reschedule Permission Check */
			if (!authDTO.isMultipleRescheduleEnabled() && (repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.POSTPONE.getId() || repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId())) {
				LOGGER.error("{} ErrorCode 319 | 002", ticketDTO.getCode());
				throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED);
			}
			if (repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.POSTPONE.getId() || repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId()) {
				transferDTO.setLookupId(repoTicketDTO.getLookupId());
			}
			else {
				transferDTO.setLookupId(repoTicketDTO.getId());
			}
			/** Validate Reschedule Count */
			if (authDTO.isMultipleRescheduleEnabled() && authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId()) {
				validateTicketRescheduleMaxCount(authDTO, transferDTO);
			}

			TripDTO oldTripDTO = repoTicketDTO.getTripDTO();

			oldTripDTO = tripService.getTrip(authDTO, oldTripDTO);

			if (authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && DateUtil.NOW().gt(oldTripDTO.getTripDateTimeV2())) {
				LOGGER.error("{} ErrorCode TR01 {}", ticketDTO.getCode(), oldTripDTO.getTripDateTimeV2());
				throw new ServiceException(ErrorCode.TRIP_CLOSED_NOT_ALLOW_BOOKING);
			}

			List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
			Stack<TicketDetailsDTO> stack = new Stack<TicketDetailsDTO>();
			for (Iterator<TicketDetailsDTO> iterator = repoTicketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
				TicketDetailsDTO detailsDTO = iterator.next();
				if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
						if (ticketDetailsDTO.getSeatCode().equals(detailsDTO.getSeatCode())) {
							seatDetailsList.add(detailsDTO);
							stack.push(detailsDTO);
							break;
						}
					}
				}
				else {
					iterator.remove();
				}
			}

			if (seatDetailsList.size() != ticketDTO.getTicketDetails().size()) {
				LOGGER.error("{} ErrorCode 308 | 1 DB Old Seats {} Given Old Seats {}", repoTicketDTO.getCode(), seatDetailsList.stream().map(repoTicket -> repoTicket.getSeatCode()).collect(Collectors.toList()), ticketDTO.getTicketDetails().stream().map(ticket -> ticket.getSeatCode()).collect(Collectors.toList()));
				throw new ServiceException(ErrorCode.SELECTED_STEAT_NOT_FOR_RESERVATION);
			}
			if (seatDetailsList.size() != transferDTO.getTicketDetails().size()) {
				LOGGER.error("{} ErrorCode 308 | 2 DB Old Seats {} Given Old Seats {}", repoTicketDTO.getCode(), seatDetailsList.stream().map(repoTicket -> repoTicket.getSeatCode()).collect(Collectors.toList()), transferDTO.getTicketDetails().stream().map(ticket -> ticket.getSeatCode()).collect(Collectors.toList()));
				throw new ServiceException(ErrorCode.SELECTED_STEAT_NOT_FOR_RESERVATION);
			}

			// copy Seat status and name
			Map<String, TicketDetailsDTO> seatCodes = new HashMap<>();
			for (TicketDetailsDTO transferSeatDetailsDTO : transferDTO.getTicketDetails()) {
				TicketDetailsDTO ticketDetailsDTO = stack.pop();
				transferSeatDetailsDTO.setSeatGendar(ticketDetailsDTO.getSeatGendar());
				transferSeatDetailsDTO.setTicketStatus(ticketDetailsDTO.getTicketStatus());
				transferSeatDetailsDTO.setPassengerAge(ticketDetailsDTO.getPassengerAge());
				transferSeatDetailsDTO.setPassengerName(ticketDetailsDTO.getPassengerName());
				seatCodes.put(transferSeatDetailsDTO.getSeatCode(), ticketDetailsDTO);
			}

			TripDTO tripDTO = getTripDetails(authDTO, transferDTO, repoTicketDTO);

			ScheduleCache scheduleCache = new ScheduleCache();
			ScheduleDTO oldSchedule = scheduleCache.getScheduleDTObyId(authDTO, oldTripDTO.getSchedule());
			oldSchedule.setTripDate(oldTripDTO.getTripDate());
			repoTicketDTO.setFromStation(getStationDTObyId(repoTicketDTO.getFromStation()));
			repoTicketDTO.setToStation(getStationDTObyId(repoTicketDTO.getToStation()));

			int fareDifferenceType = fareDifferenceType(repoTicketDTO, transferDTO);
			if (fareDifferenceType == 0) {
				for (TicketDetailsDTO ticketDetailsDTO : transferDTO.getTicketDetails()) {
					TicketDetailsDTO repoTicketDetails = seatCodes.get(ticketDetailsDTO.getSeatCode());
					ticketDetailsDTO.setSeatFare(repoTicketDetails.getSeatFare());
					ticketDetailsDTO.setAcBusTax(repoTicketDetails.getAcBusTax());
				}
			}

			// Schedule Ticket Transfer Terms
			ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO = scheduleTicketTransferTermsService.getScheduleTicketTransferTermsBySchedule(authDTO, oldSchedule, repoTicketDTO.getFromStation(), repoTicketDTO.getToStation());

			if (scheduleTicketTransferTermsDTO == null) {
				LOGGER.error("{} ErrorCode 319 | 6", ticketDTO.getCode());
				throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED);
			}
			if (scheduleTicketTransferTermsDTO.getAllowBookedUser() == 1 && repoTicketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
				throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Only the booked user can reschedule the ticket");
			}
			repoTicketDTO.getTicketUser().setGroup(groupService.getGroup(authDTO, repoTicketDTO.getTicketUser().getGroup()));
			if (!scheduleTicketTransferTermsDTO.getBookedUserGroups().isEmpty() && existGroupInGroupList(scheduleTicketTransferTermsDTO.getBookedUserGroups(), repoTicketDTO.getTicketUser().getGroup()) == null) {
				throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED_TO_GROUP);
			}

			// Validate Minutes
			if (scheduleTicketTransferTermsDTO.getMinutes() != 0) {
				oldTripDTO.setSchedule(oldSchedule);
				List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, oldTripDTO);
				DateTime originStationDateTime = BitsUtil.getTicketTransferDateTime(authDTO, scheduleTicketTransferTermsDTO, repoTicketDTO.getTripDateTime(), BitsUtil.getOriginStationTime(stageList, oldTripDTO.getTripDate()));

				// Validate Minutes
				if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.MINUTES.getId()) {
					scheduleTicketTransferTermsDTO.setDateTime(DateUtil.minusMinituesToDate(originStationDateTime, scheduleTicketTransferTermsDTO.getMinutes()));
					if (scheduleTicketTransferTermsDTO != null && DateUtil.NOW().gt(scheduleTicketTransferTermsDTO.getDateTime())) {
						LOGGER.error("{} ErrorCode 319 | 1 {}", ticketDTO.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
						throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
					}
				}
				else if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.AM.getId()) {
					DateTime checkTime = DateUtil.addMinituesToDate(oldTripDTO.getTripDate().getStartOfDay(), scheduleTicketTransferTermsDTO.getMinutes());
					scheduleTicketTransferTermsDTO.setDateTime(checkTime);
					Integer check = DateUtil.getMinutiesDifferent(DateUtil.NOW(), checkTime);
					if (check >= 0) {
						LOGGER.error("{} ErrorCode 319 | 1a {}", ticketDTO.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
						throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
					}
				}
				else if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.PM.getId()) {
					DateTime checkTime = DateUtil.addMinituesToDate(oldTripDTO.getTripDate().getStartOfDay(), 720 + scheduleTicketTransferTermsDTO.getMinutes());
					scheduleTicketTransferTermsDTO.setDateTime(checkTime);
					Integer check = DateUtil.getMinutiesDifferent(DateUtil.NOW(), checkTime);
					if (check < 0) {
						LOGGER.error("{} ErrorCode 319 | 1b {}", ticketDTO.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
						throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
					}
				}
			}

			// Calculate Transfer Charge
			List<TicketAddonsDetailsDTO> discountList = new ArrayList<>();
			for (TicketDetailsDTO ticketDetailsDTO : transferDTO.getTicketDetails()) {
				TicketAddonsDetailsDTO transferChargeDetailsDTO = new TicketAddonsDetailsDTO();
				if (scheduleTicketTransferTermsDTO.getChargeType().getId() == FareTypeEM.PERCENTAGE.getId()) {
					transferChargeDetailsDTO.setValue(ticketDetailsDTO.getSeatFare().divide(Numeric.ONE_HUNDRED).multiply(scheduleTicketTransferTermsDTO.getChargeAmount()));
				}
				else if (scheduleTicketTransferTermsDTO.getChargeType().getId() == FareTypeEM.FLAT.getId()) {
					transferChargeDetailsDTO.setValue(scheduleTicketTransferTermsDTO.getChargeAmount());
				}

				transferChargeDetailsDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
				transferChargeDetailsDTO.setAddonsType(AddonsTypeEM.TICKET_TRANSFER_CHARGE);
				transferChargeDetailsDTO.setActiveFlag(Numeric.ONE_INT);
				transferChargeDetailsDTO.setRefferenceId(scheduleTicketTransferTermsDTO.getId());
				transferChargeDetailsDTO.setRefferenceCode(Text.NA);
				discountList.add(transferChargeDetailsDTO);
			}
			transferDTO.setTicketAddonsDetails(discountList);

			// Station Point Mapping
			for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
				if (transferDTO.getBoardingPoint() != null) {
					if (pointDTO.getCode().equals(transferDTO.getBoardingPoint().getCode())) {
						transferDTO.setBoardingPoint(pointDTO);
						transferDTO.getBoardingPoint().setMinitues(tripDTO.getStage().getFromStation().getMinitues() + pointDTO.getMinitues());
						break;
					}
				}
				else if (repoTicketDTO.getBoardingPoint() != null) {
					if (pointDTO.getCode().equals(repoTicketDTO.getBoardingPoint().getCode())) {
						transferDTO.setBoardingPoint(pointDTO);
						transferDTO.getBoardingPoint().setMinitues(tripDTO.getStage().getFromStation().getMinitues() + pointDTO.getMinitues());
						break;
					}
				}
			}
			if (transferDTO.getBoardingPoint().getId() == 0) {
				LOGGER.error("{} ErrorCode 301", ticketDTO.getCode());
				throw new ServiceException(ErrorCode.STATION_POINT_CODE_INVALID);
			}

			// Station Dropping Point Mapping
			for (StationPointDTO pointDTO : tripDTO.getStage().getToStation().getStationPoint()) {
				if (transferDTO.getDroppingPoint() != null) {
					if (pointDTO.getCode().equals(transferDTO.getDroppingPoint().getCode())) {
						transferDTO.setDroppingPoint(pointDTO);
						transferDTO.getDroppingPoint().setMinitues(tripDTO.getStage().getToStation().getMinitues() + pointDTO.getMinitues());
						break;
					}
				}
				else if (repoTicketDTO.getDroppingPoint() != null) {
					if (pointDTO.getCode().equals(repoTicketDTO.getDroppingPoint().getCode())) {
						transferDTO.setDroppingPoint(pointDTO);
						transferDTO.getDroppingPoint().setMinitues(tripDTO.getStage().getToStation().getMinitues() + pointDTO.getMinitues());
						break;
					}
				}
			}
			if (transferDTO.getDroppingPoint() == null || transferDTO.getDroppingPoint().getId() == 0) {
				transferDTO.setDroppingPoint(tripDTO.getStage().getToStation().getStationPoint().get(tripDTO.getStage().getToStation().getStationPoint().size() - 1));
			}
			if (repoTicketDTO.getFromStation().getId() != tripDTO.getStage().getFromStation().getStation().getId() && repoTicketDTO.getFromStation().getId() != tripDTO.getStage().getToStation().getStation().getId()) {
				LOGGER.error("{} ErrorCode PG14 03 {}", repoTicketDTO.getCode());
				throw new ServiceException(ErrorCode.MANDATORY_PARAMETERS_MISSING);
			}

			transferDTO.setTripDTO(tripDTO);
			int reportingMin = transferDTO.getBoardingPoint().getMinitues() - authDTO.getNamespace().getProfile().getBoardingReportingMinitues();
			transferDTO.setTravelMinutes(tripDTO.getStage().getFromStation().getMinitues());
			transferDTO.setReportingMinutes(reportingMin);
			transferDTO.setFromStation(tripDTO.getStage().getFromStation().getStation());
			transferDTO.setToStation(tripDTO.getStage().getToStation().getStation());
			transferDTO.setServiceNo(tripDTO.getSchedule().getServiceNumber());
			transferDTO.setTicketAt(DateUtil.NOW());

			// validate the From,To and Trip Date
			validateTicketTransfer(authDTO, repoTicketDTO, transferDTO);

			// Transfer ticket
			transferDTO.setJourneyType(JourneyTypeEM.POSTPONE);
			if (tripDTO.getTripDate().compareTo(repoTicketDTO.getTripDate()) == -1) {
				transferDTO.setJourneyType(JourneyTypeEM.PREPONE);
			}
			repoTicketDTO.setJourneyType(transferDTO.getJourneyType());
			transferDTO.setDeviceMedium(repoTicketDTO.getDeviceMedium());
			transferDTO.setPassengerEmailId(repoTicketDTO.getPassengerEmailId());
			transferDTO.setPassengerMobile(repoTicketDTO.getPassengerMobile());

			// Get Cancellation for schedule/group/User
			CancellationTermDTO cancellationTermDTO = cancellationTermsService.getCancellationTermsByTripDTO(authDTO, authDTO.getUser(), transferDTO.getTripDTO());
			if (cancellationTermDTO == null) {
				LOGGER.error("{} ErrorCode CA01 | 2", repoTicketDTO.getCode());
				throw new ServiceException(ErrorCode.CANCELLATION_TERMS_NOT_FOUND);
			}
			cancellationTermsService.getCancellationTermGroupIdByGroupKey(authDTO, cancellationTermDTO);
			transferDTO.setCancellationTerm(cancellationTermDTO);
			transferDTO.setTicketUser(repoTicketDTO.getTicketUser());
			transferDTO.setTicketForUser(repoTicketDTO.getTicketForUser());
			transferDTO.setFromStation(transferDTO.getTripDTO().getStage().getFromStation().getStation());
			transferDTO.setToStation(transferDTO.getTripDTO().getStage().getToStation().getStation());
			transferDTO.setTripDate(transferDTO.getTripDTO().getTripDate());

			if (StringUtil.isNotNull(ticketDTO.getRemarks())) {
				String remarks = "Transfered from " + StringUtil.substring(repoTicketDTO.getFromStation().getName(), 3) + " - " + StringUtil.substring(repoTicketDTO.getToStation().getName(), 3) + "@" + repoTicketDTO.getTripDateTime().format(Text.DATE_TIME_DATE4J) + " :" + repoTicketDTO.getTripDTO().getCode() + " #:" + repoTicketDTO.getSeatNames();
				transferDTO.setRemarks(StringUtil.substring(remarks, 240));
			}

			// Re-calculate Service Tax - GST for Discount
			recalculateServiceTaxAmount(authDTO, repoTicketDTO, transferDTO, seatCodes);

			boolean seatDouplicateEntry = ticketService.checkSeatDuplicateEntryV2(authDTO, transferDTO);
			if (seatDouplicateEntry) {
				LOGGER.error("{} ErrorCode 611 | 3", repoTicketDTO.getCode());
				throw new ServiceException(ErrorCode.DOUBLE_ENTRY_VALIDATION_FAIL);
			}
			if (StringUtil.isNull(transferDTO.getCode())) {
				// Generate ticket code
				if (authDTO.getNamespace().getProfile().getPnrGenerateType().getId() == PNRGenerateTypeEM.ENCODE_ALPHANUMERIC.getId()) {
					String PNRstartCode = StringUtil.isNotNull(ticketDTO.getPnrStartCode()) ? StringUtil.removeSymbol(ticketDTO.getPnrStartCode()).length() == 3 ? ticketDTO.getPnrStartCode() : authDTO.getNamespace().getProfile().getPnrStartCode() : authDTO.getNamespace().getProfile().getPnrStartCode();
					String ticketCode = TokenGenerator.generateCode(PNRstartCode);
					transferDTO.setCode(ticketCode);
				}
				else if (authDTO.getNamespace().getProfile().getPnrGenerateType().getId() == PNRGenerateTypeEM.SEQUENCE_NUMBERIC.getId()) {
					String ticketCode = utilService.getGenerateSequenceNumber(authDTO);
					transferDTO.setCode(ticketCode);

					boolean status = ticketService.checkDuplicateTicketCodeEntry(authDTO, transferDTO.getCode());
					if (status) {
						LOGGER.error("{} ErrorCode 611 | 4", repoTicketDTO.getCode());
						throw new ServiceException(ErrorCode.DOUPLICATE_ENTRY_VALIDATION_FAIL);
					}
				}
			}
			transferDTO.setBookingCode(transferDTO.getCode());

			if (StringUtil.isNotNull(transferDTO.getCode())) {
				boolean status = ticketService.checkDuplicateTicketCodeEntry(authDTO, transferDTO.getCode());
				if (status) {
					LOGGER.error("{} ErrorCode 611 | 4", repoTicketDTO.getCode());
					throw new ServiceException(ErrorCode.DOUPLICATE_ENTRY_VALIDATION_FAIL);
				}
			}

			DateTime now = DateTime.now(TimeZone.getDefault());
			transferDTO.setTicketAt(now);

			bookingDTO.setCode(transferDTO.getCode());
			bookingDTO.setNamespace(authDTO.getNamespace());
			bookingDTO.setPaymentGatewayPartnerCode(paymentGatewayPartnerCode);
			bookingDTO.setPaymentGatewayProcessFlag(Text.TRUE);
			bookingDTO.setTransactionDate(DateUtil.convertDateTime(DateUtil.NOW()));

			List<TicketDTO> tickets = new ArrayList<>();
			tickets.add(transferDTO);
			bookingDTO.setTicketList(tickets);

			// Calculate Order Details
			BigDecimal transferTicketAddon = getTransferTicketAddon(repoTicketDTO, transferDTO, null);
			if (transferTicketAddon.compareTo(BigDecimal.ZERO) > 0 && transferDTO.getTicketDetails().size() > 0) {
				BigDecimal transferSeatAddon = (transferTicketAddon.divide(BigDecimal.valueOf(transferDTO.getTicketDetails().size()), 2, RoundingMode.CEILING));
				for (TicketDetailsDTO transferTicketDetails : transferDTO.getTicketDetails()) {
					TicketDetailsDTO ticketDetailsDTO = seatCodes.get(transferTicketDetails.getSeatCode());
					TicketAddonsDetailsDTO ticketAddonsDetails = new TicketAddonsDetailsDTO();
					ticketAddonsDetails.setSeatCode(transferTicketDetails.getSeatCode());
					ticketAddonsDetails.setRefferenceId(ticketDetailsDTO.getId());
					ticketAddonsDetails.setRefferenceCode(repoTicketDTO.getCode());
					ticketAddonsDetails.setActiveFlag(Numeric.ONE_INT);
					ticketAddonsDetails.setValue(transferSeatAddon);
					ticketAddonsDetails.setAddonsType(AddonsTypeEM.TRANSFER_PREVIOUS_TICKET_AMOUNT);
					transferDTO.getTicketAddonsDetails().add(ticketAddonsDetails);
				}
			}

			/** Calculate PG Service Charge */
			calculatePaymentGatewayCharge(authDTO, bookingDTO, transferDTO);

			blockSeatsHelperService.blockSeats(authDTO, bookingDTO, tripDTO, transferDTO);

			if (bookingDTO.getId() != 0) {
				TicketCache ticketCache = new TicketCache();
				ticketCache.putBookingDTO(authDTO, bookingDTO);
				ticketCache.putTicketEvent(authDTO, bookingDTO);
			}
		}
		catch (ServiceException e) {
			LOGGER.error("{} {}", ticketDTO.getCode(), e);
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("{} {}", ticketDTO.getCode(), e);
			throw new ServiceException(ErrorCode.UNABLE_TO_TRANSFER_TICKET);
		}
		return bookingDTO;
	}

	private BigDecimal getTransferTicketAddon(TicketDTO repoTicketDTO, TicketDTO transferDTO, Map<String, Boolean> additionalAttribute) {
		BigDecimal totalTransferFare = getTicketFareWithAddons(transferDTO);
		BigDecimal totalTicketFare = repoTicketDTO.getTicketFareWithAddons();
		BigDecimal addonAmount = BigDecimal.ZERO;

		if (totalTransferFare.compareTo(totalTicketFare) >= 0) {
			addonAmount = addonAmount.add(totalTicketFare);
		}
		else if (totalTicketFare.compareTo(totalTransferFare) >= 0 && (additionalAttribute == null || !additionalAttribute.get("captureLowFareDifferece"))) {
			addonAmount = addonAmount.add(totalTransferFare);
		}
		else if (totalTicketFare.compareTo(totalTransferFare) >= 0 && additionalAttribute != null && additionalAttribute.get("captureLowFareDifferece")) {
			addonAmount = addonAmount.add(totalTicketFare);
		}
		return addonAmount;
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

	private BigDecimal getTransferChargeAmount(TicketDTO transferTicket) {
		BigDecimal totalTransferChargeAmount = BigDecimal.ZERO;
		if (transferTicket.getTicketAddonsDetails() != null && !transferTicket.getTicketAddonsDetails().isEmpty()) {
			for (TicketAddonsDetailsDTO addonsDetailsDTO : transferTicket.getTicketAddonsDetails()) {
				if (addonsDetailsDTO.getAddonsType().getId() != AddonsTypeEM.TICKET_TRANSFER_CHARGE.getId()) {
					continue;
				}
				totalTransferChargeAmount = totalTransferChargeAmount.add(addonsDetailsDTO.getValue());
			}
		}
		return totalTransferChargeAmount.setScale(2, RoundingMode.HALF_UP);
	}

	private BigDecimal getPayableAmount(TicketDTO repoTicketDTO, TicketDTO transferDTO, boolean captureTransferCharge) {
		BigDecimal totalTransferFare = getTicketFareWithAddons(transferDTO);
		BigDecimal totalTicketFare = repoTicketDTO.getTicketFareWithAddons();
		BigDecimal totalTransferChargeAmount = getTransferChargeAmount(transferDTO);
		BigDecimal orderAmount = BigDecimal.ZERO;

		if (totalTransferFare.compareTo(totalTicketFare) == 1) {
			orderAmount = totalTransferFare.subtract(totalTicketFare);
		}

		if (captureTransferCharge) {
			orderAmount = orderAmount.add(totalTransferChargeAmount);
		}
		return orderAmount;
	}

	private int fareDifferenceType(TicketDTO repoTicketDTO, TicketDTO transferDTO) {
		BigDecimal totalTransferFare = getTicketFareWithAddons(transferDTO);
		BigDecimal totalTicketFare = repoTicketDTO.getTicketFareWithAddons();

		int fareDifferenceType = 0;
		if (totalTransferFare.compareTo(totalTicketFare) > 0) {
			fareDifferenceType = 1;
		}
		else if (totalTransferFare.compareTo(totalTicketFare) == 0) {
			fareDifferenceType = -1;
		}
		else if (totalTransferFare.compareTo(totalTicketFare) < 0) {
			fareDifferenceType = 2;
		}

		return fareDifferenceType;
	}

	private BigDecimal getRevenueAmount(TicketDTO repoTicketDTO, TicketDTO transferDTO) {
		BigDecimal totalTransferFare = getTicketFareWithAddons(transferDTO);
		BigDecimal totalTicketFare = repoTicketDTO.getTicketFareWithAddons();
		BigDecimal revenueAmount = BigDecimal.ZERO;

		if (totalTicketFare.compareTo(totalTransferFare) == 1) {
			revenueAmount = totalTicketFare.subtract(totalTransferFare);
		}

		return revenueAmount;
	}

	private void checkAndUpdateTicketTransactions(AuthDTO authDTO, TicketDTO repoTicketDTO, TicketDTO transferDTO, List<UserTransactionDTO> previousUserTransactions) {
		/** Get ticket transaction */
		List<TicketTransactionDTO> transactions = ticketService.getTicketTransactionV2(authDTO, repoTicketDTO);
		repoTicketDTO.setTicketXaction(getTicketTransaction(transactions, TransactionTypeEM.TICKETS_BOOKING));
		TicketTransactionDTO cancelTransactionDTO = getTicketTransaction(transactions, TransactionTypeEM.TICKETS_CANCEL);

		BigDecimal subTotalAmount = cancelTransactionDTO.getTransactionAmount().subtract(cancelTransactionDTO.getCancellationChargeCommissionAmount());
		BigDecimal chargeAndCommissionAmount = cancelTransactionDTO.getCancellationChargeAmount().add(cancelTransactionDTO.getCommissionAmount().add(cancelTransactionDTO.getExtraCommissionAmount()));
		BigDecimal transactionAmount = subTotalAmount.add(chargeAndCommissionAmount).subtract(cancelTransactionDTO.getAddonsAmount());

		repoTicketDTO.getTicketXaction().setUserDTO(userService.getUser(authDTO, repoTicketDTO.getTicketXaction().getUserDTO()));

		TicketTransactionDTO previousTicketTransaction = new TicketTransactionDTO();
		previousTicketTransaction.setTransactionAmount(repoTicketDTO.getTicketXaction().getTransactionAmount().subtract(transactionAmount));
		previousTicketTransaction.setAcBusTax(repoTicketDTO.getTicketXaction().getAcBusTax().subtract(cancelTransactionDTO.getAcBusTax()));
		previousTicketTransaction.setTdsTax(repoTicketDTO.getTicketXaction().getTdsTax().subtract(cancelTransactionDTO.getTdsTax()));
		previousTicketTransaction.setCommissionAmount(repoTicketDTO.getTicketXaction().getCommissionAmount().subtract(cancelTransactionDTO.getCommissionAmount()));
		previousTicketTransaction.setExtraCommissionAmount(repoTicketDTO.getTicketXaction().getExtraCommissionAmount().subtract(cancelTransactionDTO.getExtraCommissionAmount()));
		previousTicketTransaction.setAddonsAmount(repoTicketDTO.getTicketXaction().getAddonsAmount().subtract(cancelTransactionDTO.getAddonsAmount()));
		previousTicketTransaction.setTransactionMode(repoTicketDTO.getTicketXaction().getTransactionMode());
		previousTicketTransaction.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
		previousTicketTransaction.setUserDTO(repoTicketDTO.getTicketXaction().getUserDTO());
		previousTicketTransaction.setActiveFlag(1);

		/** Old Book & Cancel User transaction */
		UserTransactionDTO previousCancelUserTransaction = new UserTransactionDTO();
		previousCancelUserTransaction.setRefferenceCode(repoTicketDTO.getCode());
		previousCancelUserTransaction.setDebitAmount(previousTicketTransaction.getCommissionAmount().add(previousTicketTransaction.getAddonsAmount()));
		previousCancelUserTransaction.setTdsTax(previousTicketTransaction.getTdsTax());
		previousCancelUserTransaction.setCommissionAmount(previousTicketTransaction.getCommissionAmount());
		previousCancelUserTransaction.setCreditAmount(repoTicketDTO.getTotalFare().add(previousTicketTransaction.getTdsTax()).add(repoTicketDTO.getDebitAddonsValue()));
		previousCancelUserTransaction.setTransactionAmount(previousCancelUserTransaction.getCreditAmount().subtract(previousCancelUserTransaction.getDebitAmount()));
		previousCancelUserTransaction.setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
		previousCancelUserTransaction.setTransactionMode(previousTicketTransaction.getTransactionMode());
		previousCancelUserTransaction.setUser(repoTicketDTO.getTicketXaction().getUserDTO());
		previousCancelUserTransaction.setActiveFlag(1);
		previousUserTransactions.add(previousCancelUserTransaction);

		// UserTransactionDTO previousBookUserTransaction = new
		// UserTransactionDTO();
		// previousBookUserTransaction.setRefferenceCode(repoTicketDTO.getCode());
		// previousBookUserTransaction.setDebitAmount(repoTicketDTO.getTotalFare().add(previousTicketTransaction.getTdsTax()).add(repoTicketDTO.getDebitAddonsValue()));
		// previousBookUserTransaction.setTdsTax(previousTicketTransaction.getTdsTax());
		// previousBookUserTransaction.setCommissionAmount(previousTicketTransaction.getCommissionAmount());
		// previousBookUserTransaction.setCreditAmount(previousTicketTransaction.getCommissionAmount().add(previousTicketTransaction.getAddonsAmount()));
		// previousBookUserTransaction.setTransactionAmount(previousBookUserTransaction.getCreditAmount().subtract(previousBookUserTransaction.getDebitAmount()));
		// previousBookUserTransaction.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
		// previousBookUserTransaction.setTransactionMode(previousTicketTransaction.getTransactionMode());
		// previousBookUserTransaction.setUser(repoTicketDTO.getTicketXaction().getUserDTO());
		// previousBookUserTransaction.setActiveFlag(1);
		// previousUserTransactions.add(previousBookUserTransaction);

		CommissionDTO commissionDTO = commissionService.getCommission(authDTO, repoTicketDTO.getTicketUser(), CommissionTypeEM.TICKETS_BOOKING);

		BigDecimal creditCommissionAmount = BigDecimal.ZERO;
		BigDecimal creditExtraCommissionAmount = BigDecimal.ZERO;
		BigDecimal debitTdsTax = BigDecimal.ZERO;
		BigDecimal creditAddonsAmount = transferDTO.getAddonsValue();
		BigDecimal commissionServiceTaxAmount = BigDecimal.ZERO;

		/** Commission */
		if (commissionDTO != null && commissionDTO.getCommissionValue().compareTo(BigDecimal.ZERO) > 0) {
			if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
				creditCommissionAmount = transferDTO.getTotalSeatFare().subtract(creditAddonsAmount).multiply(commissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2);
			}
			else if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
				creditCommissionAmount = commissionDTO.getCommissionValue().multiply(new BigDecimal(transferDTO.getTicketDetails().size()));
			}
			/** Extra commission processes */
			CommissionDTO extraCommissionDTO = commissionService.getBookingExtraCommission(authDTO, repoTicketDTO.getTicketUser(), commissionDTO, transferDTO);
			if (extraCommissionDTO != null && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
				creditExtraCommissionAmount = transferDTO.getTotalSeatFare().subtract(creditAddonsAmount).multiply(extraCommissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2);
			}
			else if (extraCommissionDTO != null && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
				creditExtraCommissionAmount = extraCommissionDTO.getCommissionValue().multiply(new BigDecimal(transferDTO.getTicketDetails().size()));
			}
			/** Override the base commission if set in extra commission */
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

		/** User transaction */
		UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
		userTransactionDTO.setRefferenceCode(transferDTO.getCode());
		userTransactionDTO.setDebitAmount(transferDTO.getTotalFare().add(debitTdsTax).add(transferDTO.getDebitAddonsValue()));
		userTransactionDTO.setTdsTax(debitTdsTax);
		userTransactionDTO.setCommissionAmount(creditCommissionAmount);
		userTransactionDTO.setCreditAmount(creditCommissionAmount.add(creditAddonsAmount));
		userTransactionDTO.setTransactionAmount(userTransactionDTO.getCreditAmount().subtract(userTransactionDTO.getDebitAmount()));
		userTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
		userTransactionDTO.setTransactionMode(repoTicketDTO.getTicketXaction().getTransactionMode());
		userTransactionDTO.setUser(transferDTO.getTicketUser());

		transferDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
		transferDTO.setUserTransaction(userTransactionDTO);

		/** Ticket transaction */
		TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
		ticketTransactionDTO.setTransactionAmount(transferDTO.getTotalFare().add(debitTdsTax).subtract(creditAddonsAmount));
		ticketTransactionDTO.setAcBusTax(transferDTO.getAcBusTax());
		ticketTransactionDTO.setTdsTax(debitTdsTax);
		ticketTransactionDTO.setCommissionAmount(creditCommissionAmount);
		ticketTransactionDTO.setExtraCommissionAmount(creditExtraCommissionAmount);
		ticketTransactionDTO.setAddonsAmount(creditAddonsAmount);
		ticketTransactionDTO.setTransactionMode(repoTicketDTO.getTicketXaction().getTransactionMode());
		ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
		ticketTransactionDTO.setUserDTO(transferDTO.getTicketUser());

		transferDTO.setTicketXaction(ticketTransactionDTO);

		/** Change old ticket status */
		BigDecimal revenueAmount = getRevenueAmount(repoTicketDTO, transferDTO);
		BigDecimal cancellationCharges = revenueAmount.divide(StringUtil.getBigDecimalValue(String.valueOf(repoTicketDTO.getTicketDetails().size())), BigDecimal.ROUND_CEILING);
		BigDecimal refundAmount = (repoTicketDTO.getTotalSeatFare().add(repoTicketDTO.getTicketXaction().getAcBusTax())).subtract(repoTicketDTO.getTicketXaction().getCommissionAmount().add(repoTicketDTO.getTicketXaction().getExtraCommissionAmount()).add(repoTicketDTO.getAddonsValue()));
		BigDecimal seatRefundAmount = refundAmount.subtract(BigDecimal.valueOf(repoTicketDTO.getTicketDetails().size()));
		for (TicketDetailsDTO ticketDetailsDTO : repoTicketDTO.getTicketDetails()) {
			ticketDetailsDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
			ticketDetailsDTO.setCancellationCharges(cancellationCharges);
			ticketDetailsDTO.setRefundAmount(seatRefundAmount.setScale(2, BigDecimal.ROUND_CEILING));
		}

		repoTicketDTO.getTicketXaction().setTransactionAmount(previousTicketTransaction.getTransactionAmount());
		repoTicketDTO.getTicketXaction().setAcBusTax(previousTicketTransaction.getAcBusTax());
		repoTicketDTO.getTicketXaction().setTdsTax(previousTicketTransaction.getTdsTax());
		repoTicketDTO.getTicketXaction().setCommissionAmount(previousTicketTransaction.getCommissionAmount());
		repoTicketDTO.getTicketXaction().setExtraCommissionAmount(previousTicketTransaction.getExtraCommissionAmount());
		repoTicketDTO.getTicketXaction().setAddonsAmount(previousTicketTransaction.getAddonsAmount());
		repoTicketDTO.getTicketXaction().setTransactionMode(previousTicketTransaction.getTransactionMode());
		repoTicketDTO.getTicketXaction().setUserDTO(previousTicketTransaction.getUserDTO());
		repoTicketDTO.getTicketXaction().setCancellationCommissionAmount(BigDecimal.ZERO);
		repoTicketDTO.getTicketXaction().setRefundAmount(refundAmount.setScale(2, BigDecimal.ROUND_CEILING));
		repoTicketDTO.getTicketXaction().setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
		repoTicketDTO.getTicketXaction().setRefundStatus(RefundStatusEM.PROCESSED_BY_BANK);
		repoTicketDTO.setTicketUser(previousTicketTransaction.getUserDTO());

		calculateNetRevenueBookAmount(authDTO, creditCommissionAmount, commissionDTO, transferDTO);
	}

	private void checkAndUpdateTicketTransactionsV2(AuthDTO authDTO, TicketDTO repoTicketDTO, TicketDTO transferDTO, TicketTransactionDTO previousTicketTransaction, List<UserTransactionDTO> previousUserTransactions) {
		/** Get ticket transaction */
		List<TicketTransactionDTO> transactions = ticketService.getTicketTransactionV2(authDTO, repoTicketDTO);
		repoTicketDTO.setTicketXaction(getTicketTransaction(transactions, TransactionTypeEM.TICKETS_BOOKING));
		TicketTransactionDTO cancelTransactionDTO = getTicketTransaction(transactions, TransactionTypeEM.TICKETS_CANCEL);

		BigDecimal subTotalAmount = cancelTransactionDTO.getTransactionAmount().subtract(cancelTransactionDTO.getCancellationChargeCommissionAmount());
		BigDecimal chargeAndCommissionAmount = cancelTransactionDTO.getCancellationChargeAmount().add(cancelTransactionDTO.getCommissionAmount().add(cancelTransactionDTO.getExtraCommissionAmount()));
		BigDecimal transactionAmount = subTotalAmount.add(chargeAndCommissionAmount).subtract(cancelTransactionDTO.getAddonsAmount());

		repoTicketDTO.getTicketXaction().setUserDTO(userService.getUser(authDTO, repoTicketDTO.getTicketXaction().getUserDTO()));

		/** Old Ticket transaction */
		previousTicketTransaction.setTransactionAmount(repoTicketDTO.getTicketXaction().getTransactionAmount().subtract(transactionAmount));
		previousTicketTransaction.setAcBusTax(repoTicketDTO.getTicketXaction().getAcBusTax().subtract(cancelTransactionDTO.getAcBusTax()));
		previousTicketTransaction.setTdsTax(repoTicketDTO.getTicketXaction().getTdsTax().subtract(cancelTransactionDTO.getTdsTax()));
		previousTicketTransaction.setCommissionAmount(repoTicketDTO.getTicketXaction().getCommissionAmount().subtract(cancelTransactionDTO.getCommissionAmount()));
		previousTicketTransaction.setExtraCommissionAmount(repoTicketDTO.getTicketXaction().getExtraCommissionAmount().subtract(cancelTransactionDTO.getExtraCommissionAmount()));
		previousTicketTransaction.setAddonsAmount(repoTicketDTO.getTicketXaction().getAddonsAmount().subtract(cancelTransactionDTO.getAddonsAmount()));
		previousTicketTransaction.setTransactionMode(repoTicketDTO.getTicketXaction().getTransactionMode());
		previousTicketTransaction.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
		previousTicketTransaction.setUserDTO(repoTicketDTO.getTicketXaction().getUserDTO());
		previousTicketTransaction.setActiveFlag(1);

		/** Old Book & Cancel User transaction */
		UserTransactionDTO previousCancelUserTransaction = new UserTransactionDTO();
		previousCancelUserTransaction.setRefferenceCode(repoTicketDTO.getCode());
		previousCancelUserTransaction.setDebitAmount(previousTicketTransaction.getCommissionAmount().add(previousTicketTransaction.getAddonsAmount()));
		previousCancelUserTransaction.setTdsTax(previousTicketTransaction.getTdsTax());
		previousCancelUserTransaction.setCommissionAmount(previousTicketTransaction.getCommissionAmount());
		previousCancelUserTransaction.setCreditAmount(repoTicketDTO.getTotalFare().add(previousTicketTransaction.getTdsTax()).add(repoTicketDTO.getDebitAddonsValue()));
		previousCancelUserTransaction.setTransactionAmount(previousCancelUserTransaction.getCreditAmount().subtract(previousCancelUserTransaction.getDebitAmount()));
		previousCancelUserTransaction.setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
		previousCancelUserTransaction.setTransactionMode(previousTicketTransaction.getTransactionMode());
		previousCancelUserTransaction.setUser(repoTicketDTO.getTicketXaction().getUserDTO());
		previousCancelUserTransaction.setActiveFlag(1);
		previousUserTransactions.add(previousCancelUserTransaction);

		UserTransactionDTO previousBookUserTransaction = new UserTransactionDTO();
		previousBookUserTransaction.setRefferenceCode(transferDTO.getCode());
		previousBookUserTransaction.setDebitAmount(repoTicketDTO.getTotalFare().add(previousTicketTransaction.getTdsTax()).add(repoTicketDTO.getDebitAddonsValue()));
		previousBookUserTransaction.setTdsTax(previousTicketTransaction.getTdsTax());
		previousBookUserTransaction.setCommissionAmount(previousTicketTransaction.getCommissionAmount());
		previousBookUserTransaction.setCreditAmount(previousTicketTransaction.getCommissionAmount().add(previousTicketTransaction.getAddonsAmount()));
		previousBookUserTransaction.setTransactionAmount(previousBookUserTransaction.getCreditAmount().subtract(previousBookUserTransaction.getDebitAmount()));
		previousBookUserTransaction.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
		previousBookUserTransaction.setTransactionMode(previousTicketTransaction.getTransactionMode());
		previousBookUserTransaction.setUser(repoTicketDTO.getTicketXaction().getUserDTO());
		previousBookUserTransaction.setActiveFlag(1);
		previousUserTransactions.add(previousBookUserTransaction);

		BigDecimal newTicketExcessAmount = transferDTO.getTotalSeatFare().subtract(repoTicketDTO.getTotalSeatFare());
		BigDecimal acBusTax = transferDTO.getAcBusTax().subtract(repoTicketDTO.getAcBusTax());
		BigDecimal totalFare = newTicketExcessAmount.add(acBusTax);

		CommissionDTO commissionDTO = commissionService.getCommission(authDTO, authDTO.getUser(), CommissionTypeEM.TICKETS_BOOKING);

		BigDecimal creditCommissionAmount = BigDecimal.ZERO;
		BigDecimal creditExtraCommissionAmount = BigDecimal.ZERO;
		BigDecimal debitTdsTax = BigDecimal.ZERO;
		BigDecimal creditAddonsAmount = BigDecimal.ZERO;
		BigDecimal commissionServiceTaxAmount = BigDecimal.ZERO;

		/** Commission */
		if (commissionDTO != null && commissionDTO.getCommissionValue().compareTo(BigDecimal.ZERO) > 0) {
			if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
				creditCommissionAmount = newTicketExcessAmount.subtract(creditAddonsAmount).multiply(commissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2);
			}
			else if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
				creditCommissionAmount = commissionDTO.getCommissionValue().multiply(new BigDecimal(transferDTO.getTicketDetails().size()));
			}
			/** Extra commission processes */
			CommissionDTO extraCommissionDTO = commissionService.getBookingExtraCommission(authDTO, authDTO.getUser(), commissionDTO, transferDTO);
			if (extraCommissionDTO != null && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
				creditExtraCommissionAmount = newTicketExcessAmount.subtract(creditAddonsAmount).multiply(extraCommissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2);
			}
			else if (extraCommissionDTO != null && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
				creditExtraCommissionAmount = extraCommissionDTO.getCommissionValue().multiply(new BigDecimal(transferDTO.getTicketDetails().size()));
			}
			/** Override the base commission if set in extra commission */
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

		if (totalFare.compareTo(BigDecimal.ZERO) != 0) {
			BigDecimal transferChargeAmount = BigDecimal.ZERO;
			for (TicketAddonsDetailsDTO ticketAddonsDetailsDTO : transferDTO.getTicketAddonsDetails()) {
				if (AddonsTypeEM.TICKET_TRANSFER_CHARGE.getId() != ticketAddonsDetailsDTO.getAddonsType().getId()) {
					continue;
				}
				transferChargeAmount = transferChargeAmount.add(ticketAddonsDetailsDTO.getValue());
			}

			/** User transaction */
			UserTransactionDTO userTransactionDTO = new UserTransactionDTO();
			userTransactionDTO.setRefferenceCode(transferDTO.getCode());
			userTransactionDTO.setDebitAmount(totalFare.add(debitTdsTax).add(transferDTO.getDebitAddonsValue().add(transferChargeAmount)));
			userTransactionDTO.setTdsTax(debitTdsTax);
			userTransactionDTO.setCommissionAmount(creditCommissionAmount);
			userTransactionDTO.setCreditAmount(creditCommissionAmount.add(creditAddonsAmount));
			userTransactionDTO.setTransactionAmount(userTransactionDTO.getCreditAmount().subtract(userTransactionDTO.getDebitAmount()));
			userTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
			userTransactionDTO.setTransactionMode(repoTicketDTO.getTicketXaction().getTransactionMode());
			userTransactionDTO.setUser(authDTO.getUser());

			transferDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
			transferDTO.setUserTransaction(userTransactionDTO);

			/** Ticket transaction */
			TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
			ticketTransactionDTO.setTransactionAmount(totalFare.add(debitTdsTax).subtract(creditAddonsAmount));
			ticketTransactionDTO.setAcBusTax(acBusTax);
			ticketTransactionDTO.setTdsTax(debitTdsTax);
			ticketTransactionDTO.setCommissionAmount(creditCommissionAmount);
			ticketTransactionDTO.setExtraCommissionAmount(creditExtraCommissionAmount);
			ticketTransactionDTO.setAddonsAmount(creditAddonsAmount);
			ticketTransactionDTO.setTransactionMode(repoTicketDTO.getTicketXaction().getTransactionMode());
			ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
			ticketTransactionDTO.setUserDTO(authDTO.getUser());
			transferDTO.setTicketXaction(ticketTransactionDTO);
		}

		/** Change old ticket status */
		BigDecimal revenueAmount = getRevenueAmount(repoTicketDTO, transferDTO);
		BigDecimal cancellationCharges = revenueAmount.divide(StringUtil.getBigDecimalValue(String.valueOf(repoTicketDTO.getTicketDetails().size())));
		BigDecimal refundAmount = (repoTicketDTO.getTotalSeatFare().add(repoTicketDTO.getTicketXaction().getAcBusTax())).subtract(repoTicketDTO.getTicketXaction().getCommissionAmount().add(repoTicketDTO.getTicketXaction().getExtraCommissionAmount()).add(repoTicketDTO.getAddonsValue()));
		BigDecimal seatRefundAmount = refundAmount.subtract(BigDecimal.valueOf(repoTicketDTO.getTicketDetails().size()));
		for (TicketDetailsDTO ticketDetailsDTO : repoTicketDTO.getTicketDetails()) {
			ticketDetailsDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
			ticketDetailsDTO.setCancellationCharges(cancellationCharges);
			ticketDetailsDTO.setRefundAmount(seatRefundAmount.setScale(2, BigDecimal.ROUND_CEILING));
		}

		repoTicketDTO.getTicketXaction().setTransactionAmount(previousTicketTransaction.getTransactionAmount());
		repoTicketDTO.getTicketXaction().setAcBusTax(previousTicketTransaction.getAcBusTax());
		repoTicketDTO.getTicketXaction().setTdsTax(previousTicketTransaction.getTdsTax());
		repoTicketDTO.getTicketXaction().setCommissionAmount(previousTicketTransaction.getCommissionAmount());
		repoTicketDTO.getTicketXaction().setExtraCommissionAmount(previousTicketTransaction.getExtraCommissionAmount());
		repoTicketDTO.getTicketXaction().setAddonsAmount(previousTicketTransaction.getAddonsAmount());
		repoTicketDTO.getTicketXaction().setTransactionMode(previousTicketTransaction.getTransactionMode());
		repoTicketDTO.getTicketXaction().setUserDTO(previousTicketTransaction.getUserDTO());
		repoTicketDTO.getTicketXaction().setCancellationCommissionAmount(BigDecimal.ZERO);
		repoTicketDTO.getTicketXaction().setRefundAmount(refundAmount.setScale(2, BigDecimal.ROUND_CEILING));
		repoTicketDTO.getTicketXaction().setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
		repoTicketDTO.getTicketXaction().setRefundStatus(RefundStatusEM.PROCESSED_BY_BANK);
		repoTicketDTO.setTicketUser(previousTicketTransaction.getUserDTO());

		calculateNetRevenueBookAmount(authDTO, creditCommissionAmount.add(previousTicketTransaction.getCommissionAmount()), commissionDTO, transferDTO);
	}

	private TicketTransactionDTO getTicketTransaction(List<TicketTransactionDTO> transactions, TransactionTypeEM transactionTypeEM) {
		TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
		ticketTransactionDTO.setTransactionAmount(BigDecimal.ZERO);
		ticketTransactionDTO.setCommissionAmount(BigDecimal.ZERO);
		ticketTransactionDTO.setExtraCommissionAmount(BigDecimal.ZERO);
		ticketTransactionDTO.setTdsTax(BigDecimal.ZERO);
		ticketTransactionDTO.setAcBusTax(BigDecimal.ZERO);
		ticketTransactionDTO.setAddonsAmount(BigDecimal.ZERO);
		ticketTransactionDTO.setCancellationChargeAmount(BigDecimal.ZERO);
		ticketTransactionDTO.setCancellationCommissionAmount(BigDecimal.ZERO);
		ticketTransactionDTO.setCancellationChargeCommissionAmount(BigDecimal.ZERO);
		ticketTransactionDTO.setCancelTdsTax(BigDecimal.ZERO);
		ticketTransactionDTO.setCancellationChargeTax(BigDecimal.ZERO);
		ticketTransactionDTO.setRefundAmount(BigDecimal.ZERO);

		for (TicketTransactionDTO transactionDTO : transactions) {
			if (transactionTypeEM.getId() != transactionDTO.getTransactionType().getId()) {
				continue;
			}

			ticketTransactionDTO.setTransSeatCount(ticketTransactionDTO.getTransSeatCount() + transactionDTO.getTransSeatCount());
			ticketTransactionDTO.setTransactionAmount(ticketTransactionDTO.getTransactionAmount().add(transactionDTO.getTransactionAmount()));
			ticketTransactionDTO.setCommissionAmount(ticketTransactionDTO.getCommissionAmount().add(transactionDTO.getCommissionAmount()));
			ticketTransactionDTO.setExtraCommissionAmount(ticketTransactionDTO.getExtraCommissionAmount().add(transactionDTO.getExtraCommissionAmount()));
			ticketTransactionDTO.setTdsTax(ticketTransactionDTO.getTdsTax().add(transactionDTO.getTdsTax()));
			ticketTransactionDTO.setAcBusTax(ticketTransactionDTO.getAcBusTax().add(transactionDTO.getAcBusTax()));
			ticketTransactionDTO.setAddonsAmount(ticketTransactionDTO.getAddonsAmount().add(transactionDTO.getAddonsAmount()));
			if (TransactionTypeEM.TICKETS_CANCEL.getId() == transactionDTO.getTransactionType().getId()) {
				ticketTransactionDTO.setCancellationChargeAmount(ticketTransactionDTO.getCancellationChargeAmount().add(transactionDTO.getCancellationChargeAmount()));
				ticketTransactionDTO.setCancellationCommissionAmount(ticketTransactionDTO.getCancellationCommissionAmount().add(transactionDTO.getCancellationCommissionAmount()));
				ticketTransactionDTO.setCancellationChargeCommissionAmount(ticketTransactionDTO.getCancellationChargeCommissionAmount().add(transactionDTO.getCancellationChargeCommissionAmount()));
				ticketTransactionDTO.setCancelTdsTax(ticketTransactionDTO.getCancelTdsTax().add(transactionDTO.getCancelTdsTax()));
				ticketTransactionDTO.setCancellationChargeTax(ticketTransactionDTO.getCancellationChargeTax().add(transactionDTO.getCancellationChargeTax()));
				ticketTransactionDTO.setRefundAmount(ticketTransactionDTO.getRefundAmount().add(transactionDTO.getRefundAmount()));
			}
			ticketTransactionDTO.setTransactionType(transactionDTO.getTransactionType());
			ticketTransactionDTO.setTransactionMode(transactionDTO.getTransactionMode());
			ticketTransactionDTO.setUserDTO(transactionDTO.getUserDTO());
		}
		return ticketTransactionDTO;
	}

	@Override
	public TicketDTO transferTicketConfirm(AuthDTO authDTO, TicketDTO transferDTO) {
		ticketService.getTicketStatus(authDTO, transferDTO);

		TicketDTO repoTicketDTO = new TicketDTO();
		repoTicketDTO.setId(transferDTO.getLookupId());
		ticketService.getTicketStatus(authDTO, repoTicketDTO);

		if (transferDTO.getTicketStatus().getId() != TicketStatusEM.TMP_BLOCKED_TICKET.getId()) {
			LOGGER.error("{} ErrorCode PG21 {}", repoTicketDTO.getCode(), transferDTO.getTicketStatus().getCode());
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
		}

		for (Iterator<TicketDetailsDTO> iterator = repoTicketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
			TicketDetailsDTO detailsDTO = iterator.next();
			if (detailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() && detailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				iterator.remove();
				continue;
			}
		}

		/** Get trip stage seat details & convert ticket extra */
		TripDAO tripDAO = new TripDAO();
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, repoTicketDTO);
		Map<String, TicketExtraDTO> repoTicketExtraMap = new HashMap<String, TicketExtraDTO>();
		for (TicketDetailsDTO detailsDTO : seatList) {
			repoTicketExtraMap.put(detailsDTO.getSeatCode(), detailsDTO.getTicketExtra());
			detailsDTO.setActiveFlag(0);
		}

		/** Get transfered trip stage seat details & convert ticket extra */
		List<TicketDetailsDTO> transferedSeatList = tripDAO.getTripStageSeatsDetails(authDTO, transferDTO);
		Map<String, TicketExtraDTO> transferedTicketExtraMap = new HashMap<String, TicketExtraDTO>();
		for (TicketDetailsDTO detailsDTO : transferedSeatList) {
			transferedTicketExtraMap.put(detailsDTO.getSeatCode(), detailsDTO.getTicketExtra());
		}

		BookingDTO bookingDTO = new BookingDTO();
		bookingDTO.setCode(transferDTO.getBookingCode());
		bookingDTO.addTicketDTO(transferDTO);

		int fareDifferenceType = fareDifferenceType(repoTicketDTO, transferDTO);

		PaymentGatewayTransactionDTO gatewayTransactionDTO = paymentRequestService.getPaymentGatewayTransactionAmount(authDTO, bookingDTO.getCode(), PaymentGatewayTransactionTypeEM.PAYMENT);
		if (fareDifferenceType == 1 && gatewayTransactionDTO.getAmount().compareTo(BigDecimal.ZERO) == 0) {
			LOGGER.error("{} ErrorCode PG18 {} {}", repoTicketDTO.getCode(), gatewayTransactionDTO.getCode(), gatewayTransactionDTO.getAmount());
			throw new ServiceException(ErrorCode.PAYMENT_GATEWAY_TRANSACTION_FAIL_NOT_COMPLETED);
		}
		else if (gatewayTransactionDTO.getAmount().compareTo(bookingDTO.getTransactionAmount().setScale(0, RoundingMode.HALF_UP)) == -1) {
			LOGGER.error("{} ErrorCode PG19 {} {}", repoTicketDTO.getCode(), gatewayTransactionDTO.getAmount(), bookingDTO.getTransactionAmount());
			throw new ServiceException(ErrorCode.PAYMENT_GATEWAY_TRANSACTION_AMOUNT_TICKET_AMOUNT_MISMATCH);
		}

		/** Get ticket transaction */
		TicketTransactionDTO previousTicketTransaction = new TicketTransactionDTO();
		List<UserTransactionDTO> previousUserTransactions = new ArrayList<>();
		if (repoTicketDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && fareDifferenceType == 1 && repoTicketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
			checkAndUpdateTicketTransactionsV2(authDTO, repoTicketDTO, transferDTO, previousTicketTransaction, previousUserTransactions);
		}
		else if (repoTicketDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
			checkAndUpdateTicketTransactions(authDTO, repoTicketDTO, transferDTO, previousUserTransactions);
		}

		repoTicketDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
		transferDTO.getTripDTO().setBus(busService.getBus(authDTO, transferDTO.getTripDTO().getBus()));

		/** Change old ticket status */
		for (TicketDetailsDTO ticketDetailsDTO : repoTicketDTO.getTicketDetails()) {
			ticketDetailsDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
			ticketDetailsDTO.setTicketExtra(repoTicketExtraMap.get(ticketDetailsDTO.getSeatCode()));
			ticketDetailsDTO.getTicketExtra().setNetAmount(BigDecimal.ZERO);
		}

		for (TicketDetailsDTO ticketDetailsDTO : transferDTO.getTicketDetails()) {
			ticketDetailsDTO.setTicketExtra(transferedTicketExtraMap.get(ticketDetailsDTO.getSeatCode()));
			ticketDetailsDTO.getTicketExtra().setNetAmount(ticketDetailsDTO.getNetAmount());
		}

		for (TicketAddonsDetailsDTO addonsDetailsDTO : repoTicketDTO.getTicketAddonsDetails()) {
			addonsDetailsDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
		}

		transferDTO.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);
		for (TicketDetailsDTO ticketDetailsDTO : transferDTO.getTicketDetails()) {
			ticketDetailsDTO.setTicketStatus(TicketStatusEM.CONFIRM_BOOKED_TICKETS);
		}

		/** change ticket addons status */
		if (transferDTO.getTicketAddonsDetails() != null && !transferDTO.getTicketAddonsDetails().isEmpty()) {
			for (TicketAddonsDetailsDTO discountDetailsDTO : transferDTO.getTicketAddonsDetails()) {
				discountDetailsDTO.setTicketStatus(transferDTO.getTicketStatus());
			}
		}

		/** Save Transfer Ticket */
		SaveConfirmTicket(authDTO, repoTicketDTO, transferDTO, previousTicketTransaction, previousUserTransactions);

		/** Notifications */
		if (transferDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
			notificationService.sendTicketBookingNotification(authDTO, transferDTO);
		}
		Map<String, Boolean> additionalAttribute = new HashMap<>();
		additionalAttribute.put("captureTransferCharge", true);
		/** Push Notification */
		if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.RESCHEDULE)) {
			notificationPushService.pushTicketRescheduleNotification(authDTO, transferDTO, additionalAttribute);
		}
		/** Upload to backup server */
		drService.flushTicketDetails(authDTO, transferDTO);

		tripService.clearBookedBlockedSeatsCache(authDTO, repoTicketDTO.getTripDTO());
		tripService.clearBookedBlockedSeatsCache(authDTO, transferDTO.getTripDTO());
		return transferDTO;
	}

	private void SaveConfirmTicket(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO transferTicketDTO, TicketTransactionDTO previousTicketTransaction, List<UserTransactionDTO> previousUserTransactions) {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);

				transferTicketDTO.setTicketUser(transferTicketDTO.getTicketXaction().getUserDTO());
				ticketService.saveTicketTransaction(connection, authDTO, transferTicketDTO);

				ticketService.UpdateTicketStatus(connection, authDTO, transferTicketDTO);

				// update Trip seat Block
				tripService.updateTripSeatDetailsWithExtras(connection, authDTO, transferTicketDTO);

				if (transferTicketDTO.getTicketUser().getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId()) {
					transferTicketDTO.getUserTransaction().setRefferenceId(transferTicketDTO.getTicketXaction().getId());
					transactionService.SaveUserTransaction(connection, authDTO, transferTicketDTO.getTicketUser(), transferTicketDTO.getUserTransaction());
				}

				String event = composeAuditEvent(ticketDTO, transferTicketDTO);
				ticketDTO.setRemarks(StringUtil.substring(event, 240));
				// Update old ticket status
				ticketService.UpdateTicketStatus(connection, authDTO, ticketDTO);

				tripService.updateTripSeatDetailsWithExtras(connection, authDTO, ticketDTO);

				// Cancel Transaction For Old Ticket
				ticketService.saveTicketTransaction(connection, authDTO, ticketDTO);

				ticketService.saveTicketCancelTransaction(connection, authDTO, ticketDTO);

				ticketService.saveTicketCancellationDetails(connection, authDTO, ticketDTO);

				if (previousTicketTransaction != null && !previousUserTransactions.isEmpty() && previousTicketTransaction.getActiveFlag() == 1) {
					transferTicketDTO.setTicketUser(previousTicketTransaction.getUserDTO());
					transferTicketDTO.setTicketXaction(previousTicketTransaction);
					ticketService.saveTicketTransaction(connection, authDTO, transferTicketDTO);

					for (UserTransactionDTO previousUserTransaction : previousUserTransactions) {
						if (previousUserTransaction.getUser().getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId()) {
							previousUserTransaction.setRefferenceId(transferTicketDTO.getTicketXaction().getId());
							transactionService.SaveUserTransaction(connection, authDTO, previousUserTransaction.getUser(), previousUserTransaction);
						}
					}
				}
				else if (!previousUserTransactions.isEmpty()) {
					for (UserTransactionDTO previousUserTransaction : previousUserTransactions) {
						if (previousUserTransaction.getUser().getPaymentType().getId() != PaymentTypeEM.PAYMENT_UNLIMITED.getId()) {
							previousUserTransaction.setRefferenceId(ticketDTO.getTicketXaction().getId());
							transactionService.SaveUserTransaction(connection, authDTO, previousUserTransaction.getUser(), previousUserTransaction);
						}
					}
				}
			}
			catch (ServiceException e) {
				connection.rollback();
				LOGGER.error("{} {}", ticketDTO.getCode(), e);
				throw e;
			}
			catch (SQLTransactionRollbackException e) {
				slack.sendAlert(authDTO, transferTicketDTO.getCode() + " DL06 - Deadlock found when trying to get lock; try restarting transaction");

				e.printStackTrace();
				LOGGER.error("{} {}", ticketDTO.getCode(), e);
				connection.rollback();
				throw e;
			}
			catch (Exception e) {
				e.printStackTrace();
				LOGGER.error("{} {}", ticketDTO.getCode(), e);
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
			LOGGER.error("{} {}", ticketDTO.getCode(), e);
			throw new ServiceException(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
		}
	}

	private String composeAuditEvent(TicketDTO oldTicketDTO, TicketDTO transferTicketDTO) {
		StringBuilder transferTickets = new StringBuilder();
		transferTickets.append("New PNR: " + transferTicketDTO.getCode() + Text.COMMA + Text.SINGLE_SPACE);
		transferTickets.append("Travel date: " + oldTicketDTO.getTripDate().format("DD-MM-YYYY"));
		transferTickets.append(oldTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId() ? " Preponed to " : " Postponed to ");
		transferTickets.append(transferTicketDTO.getTripDate().format("DD-MM-YYYY") + Text.COMMA + Text.NEW_LINE);
		transferTickets.append("Seat(s): " + oldTicketDTO.getSeatNames() + " -> " + transferTicketDTO.getSeatNames() + Text.COMMA + Text.NEW_LINE);

		if (oldTicketDTO.getFromStation().getId() != transferTicketDTO.getFromStation().getId()) {
			transferTickets.append("From: " + oldTicketDTO.getFromStation().getName() + " -> " + transferTicketDTO.getFromStation().getName() + Text.COMMA + Text.NEW_LINE);
		}
		if (oldTicketDTO.getToStation().getId() != transferTicketDTO.getToStation().getId()) {
			transferTickets.append("To: " + oldTicketDTO.getToStation().getName() + " -> " + transferTicketDTO.getToStation().getName() + Text.COMMA + Text.NEW_LINE);
		}
		if (oldTicketDTO.getBoardingPoint().getId() != transferTicketDTO.getBoardingPoint().getId()) {
			transferTickets.append("Boarding: " + StringUtil.substring(oldTicketDTO.getBoardingPoint().getName(), Numeric.SIX_INT) + " -> " + StringUtil.substring(transferTicketDTO.getBoardingPoint().getName(), Numeric.SIX_INT) + Text.COMMA + Text.NEW_LINE);
		}
		if (oldTicketDTO.getDroppingPoint().getId() != transferTicketDTO.getDroppingPoint().getId()) {
			transferTickets.append("Dropping: " + StringUtil.substring(oldTicketDTO.getDroppingPoint().getName(), Numeric.SIX_INT) + " -> " + StringUtil.substring(transferTicketDTO.getDroppingPoint().getName(), Numeric.SIX_INT) + Text.COMMA + Text.NEW_LINE);
		}
		return transferTickets.toString();
	}

	private void calculateNetRevenueBookAmount(AuthDTO authDTO, BigDecimal creditCommissionAmount, CommissionDTO commissionDTO, TicketDTO ticketDTO) {
		try {
			BigDecimal seatCommissionAmount = BigDecimal.ZERO;
			BigDecimal serviceTaxAmount = BigDecimal.ZERO;
			if (commissionDTO != null && creditCommissionAmount.compareTo(BigDecimal.ZERO) > 0) {
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
			LOGGER.error("{} {}", ticketDTO.getCode(), e);
			System.out.println("Block NTRA Error:" + ticketDTO.getCode());
			e.printStackTrace();
		}
	}

	private List<NamespaceDTO> getAliasNamespaceList(AuthDTO authDTO) {
		List<NamespaceDTO> aliasNamespaceList = new ArrayList<>();
		Map<String, String> aliasMap = Maps.newHashMap();
		List<UserDTO> userList = userService.getAllUserV2(authDTO, UserTagEM.API_USER_RB);
		for (UserDTO userDTO : userList) {
			if (userDTO.getAdditionalAttribute() == null || !userDTO.getAdditionalAttribute().containsKey(Constants.ALIAS_NAMESPACE)) {
				continue;
			}
			String aliasNamespaceCode = userDTO.getAdditionalAttribute().get(Constants.ALIAS_NAMESPACE);
			aliasMap.put(aliasNamespaceCode, aliasNamespaceCode);
		}

		for (String aliasCode : aliasMap.values()) {
			NamespaceDTO namespace = authService.getAliasNamespace(aliasCode);
			aliasNamespaceList.add(namespace);
		}
		return aliasNamespaceList;
	}

	protected GroupDTO existGroupInGroupList(List<GroupDTO> groupList, GroupDTO groupDTO) {
		GroupDTO existingGroup = null;
		for (GroupDTO group : groupList) {
			if (StringUtil.isNotNull(group.getCode()) && StringUtil.isNotNull(groupDTO.getCode()) && group.getCode().equals(groupDTO.getCode())) {
				existingGroup = group;
				break;
			}
		}
		return existingGroup;
	}

	@Override
	public void isTransferTicket(AuthDTO authDTO, TicketTransferDTO ticketTransferDTO) {
		/** Trip */
		TripCache tripCache = new TripCache();
		TripDTO trip = tripCache.getTripDTO(authDTO, ticketTransferDTO.getTransferTrip());
		if (authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && DateUtil.NOW().gt(trip.getTripDateTimeV2())) {
			LOGGER.error("{} ErrorCode TR001 {}", trip.getCode(), trip.getTripDateTimeV2());
			throw new ServiceException(ErrorCode.TRIP_CLOSED_NOT_ALLOW_BOOKING);
		}
		/** Schedule */
		ScheduleCache scheduleCache = new ScheduleCache();
		ScheduleDTO scheduleDTO = scheduleCache.getScheduleDTObyId(authDTO, trip.getSchedule());
		scheduleDTO.setTripDate(trip.getTripDate());

		/** Schedule Ticket Transfer Terms */
		ticketTransferDTO.setFromStation(getStationDTObyId(ticketTransferDTO.getFromStation()));
		ticketTransferDTO.setToStation(getStationDTObyId(ticketTransferDTO.getToStation()));
		ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO = scheduleTicketTransferTermsService.getScheduleTicketTransferTermsBySchedule(authDTO, scheduleDTO, ticketTransferDTO.getFromStation(), ticketTransferDTO.getToStation());
		if (scheduleTicketTransferTermsDTO == null) {
			LOGGER.error("ErrorCode 319 | 01", trip.getCode());
			throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED);
		}

		if (scheduleTicketTransferTermsDTO.getMinutes() != 0) {
			trip.setSchedule(scheduleDTO);
			List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, trip);
			DateTime originStationDateTime = BitsUtil.getOriginStationTime(stageList, trip.getTripDate());

			scheduleTicketTransferTermsDTO.setDateTime(DateUtil.minusMinituesToDate(originStationDateTime, scheduleTicketTransferTermsDTO.getMinutes()));
			// Validate Minutes
			if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.MINUTES.getId()) {
				scheduleTicketTransferTermsDTO.setDateTime(DateUtil.minusMinituesToDate(originStationDateTime, scheduleTicketTransferTermsDTO.getMinutes()));
				if (scheduleTicketTransferTermsDTO != null && DateUtil.NOW().gt(scheduleTicketTransferTermsDTO.getDateTime())) {
					LOGGER.error("{} ErrorCode 319 | 02 {}", trip.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
					throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
				}
			}
			else if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.AM.getId()) {
				DateTime checkTime = DateUtil.addMinituesToDate(trip.getTripDate().getStartOfDay(), scheduleTicketTransferTermsDTO.getMinutes());
				Integer check = DateUtil.getMinutiesDifferent(DateUtil.NOW(), checkTime);
				if (check >= 0) {
					LOGGER.error("{} ErrorCode 319 | 02a {}", trip.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
					throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
				}
			}
			else if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.PM.getId()) {
				DateTime checkTime = DateUtil.addMinituesToDate(trip.getTripDate().getStartOfDay(), 720 + scheduleTicketTransferTermsDTO.getMinutes());
				Integer check = DateUtil.getMinutiesDifferent(DateUtil.NOW(), checkTime);
				if (check < 0) {
					LOGGER.error("{} ErrorCode 319 | 02b {}", trip.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
					throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
				}
			}
		}

		TripDTO oldTripDTO = ticketTransferDTO.getTrip();
		oldTripDTO = tripCache.getTripDTO(authDTO, oldTripDTO);

		Map<String, BusSeatLayoutDTO> fareMap = new HashMap<String, BusSeatLayoutDTO>();
		Map<String, String> seatCodeMap = new HashMap<>();
		Map<String, BusSeatLayoutDTO> seatGenderMap = new HashMap<>();
		TripDTO tripDTO = getTripDetails(authDTO, ticketTransferDTO, fareMap, seatCodeMap, seatGenderMap);

		StringBuilder errors = new StringBuilder();
		for (TicketTransferDetailsDTO ticketTransferDetailsDTO : ticketTransferDTO.getTicketTransferDetails()) {
			TicketDTO repoTicketDTO = new TicketDTO();
			repoTicketDTO.setCode(ticketTransferDetailsDTO.getTicket().getCode());
			Stack<TicketDetailsDTO> stack = new Stack<TicketDetailsDTO>();

			ticketService.getTicketStatus(authDTO, repoTicketDTO);
			/** Multiple Reschedule Permission Check */
			if (!authDTO.isMultipleRescheduleEnabled() && (repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.POSTPONE.getId() || repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId())) {
				errors.append(repoTicketDTO.getCode()).append(" Prepond/Postpond not allowed to the channel|");
				continue;
			}

			if (!authDTO.isMultipleRescheduleEnabled() && repoTicketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() && (repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId() || repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.RETURN_TRIP.getId())) {
				if (scheduleTicketTransferTermsDTO.getAllowBookedUser() == 1 && repoTicketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
					errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" Only the booked user can reschedule the ticket|");
					continue;
				}
				repoTicketDTO.getTicketUser().setGroup(groupService.getGroup(authDTO, repoTicketDTO.getTicketUser().getGroup()));
				if (!scheduleTicketTransferTermsDTO.getBookedUserGroups().isEmpty() && existGroupInGroupList(scheduleTicketTransferTermsDTO.getBookedUserGroups(), repoTicketDTO.getTicketUser().getGroup()) == null) {
					errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" Prepond/Postpond not allowed to the channel|");
					continue;
				}
			}
			else if (authDTO.isMultipleRescheduleEnabled() && repoTicketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() && repoTicketDTO.getJourneyType().getId() != JourneyTypeEM.ALL_TRIP.getId()) {
				if (scheduleTicketTransferTermsDTO.getAllowBookedUser() == 1 && repoTicketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
					errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" Only the booked user can reschedule the ticket|");
					continue;
				}
				repoTicketDTO.getTicketUser().setGroup(groupService.getGroup(authDTO, repoTicketDTO.getTicketUser().getGroup()));
				if (!scheduleTicketTransferTermsDTO.getBookedUserGroups().isEmpty() && existGroupInGroupList(scheduleTicketTransferTermsDTO.getBookedUserGroups(), repoTicketDTO.getTicketUser().getGroup()) == null) {
					errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" Prepond/Postpond not allowed to the channel|");
					continue;
				}
			}
			else {
				errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" Prepond/Postpond not allowed to this PNR|");
				continue;
			}
			ticketTransferDetailsDTO.setScheduleTicketTransferTerms(scheduleTicketTransferTermsDTO);

			TicketDTO transferDTO = ticketTransferDetailsDTO.getTransferTicket();
			if (repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.POSTPONE.getId() || repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId()) {
				transferDTO.setLookupId(repoTicketDTO.getLookupId());
			}
			else {
				transferDTO.setLookupId(repoTicketDTO.getId());
			}
			List<TicketDTO> tickets = ticketService.findTicketByLookupIdV2(authDTO, transferDTO);
			if (authDTO.isMultipleRescheduleEnabled() && tickets.size() >= authDTO.getNamespace().getProfile().getTicketRescheduleMaxCount()) {
				errors.append(repoTicketDTO.getCode()).append(" Ticket booking not allowed, reached or beyond limits" + authDTO.getNamespace().getProfile().getTicketRescheduleMaxCount() + " |");
				continue;
			}

			List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
			for (Iterator<TicketDetailsDTO> iterator = repoTicketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
				TicketDetailsDTO detailsDTO = iterator.next();
				if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					for (TicketDetailsDTO ticketDetailsDTO : ticketTransferDetailsDTO.getTicket().getTicketDetails()) {
						if (ticketDetailsDTO.getSeatCode().equals(detailsDTO.getSeatCode())) {
							seatDetailsList.add(detailsDTO);
							stack.push(detailsDTO);
							break;
						}
					}
				}
				else {
					iterator.remove();
				}
			}

			if (seatDetailsList.size() != ticketTransferDetailsDTO.getTicket().getTicketDetails().size()) {
				errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" The selected seats are not valid|");
				continue;
			}
			if (seatDetailsList.size() != ticketTransferDetailsDTO.getTransferTicket().getTicketDetails().size()) {
				errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" The selected transferred seats are not valid|");
				continue;
			}
			// copy Seat status and name
			for (TicketDetailsDTO transferSeatDetailsDTO : ticketTransferDetailsDTO.getTransferTicket().getTicketDetails()) {
				TicketDetailsDTO ticketDetailsDTO = stack.pop();
				transferSeatDetailsDTO.setSeatGendar(ticketDetailsDTO.getSeatGendar());
				transferSeatDetailsDTO.setTicketStatus(ticketDetailsDTO.getTicketStatus());
				transferSeatDetailsDTO.setPassengerAge(ticketDetailsDTO.getPassengerAge());
				transferSeatDetailsDTO.setPassengerName(ticketDetailsDTO.getPassengerName());
			}

			validateSeatGender(authDTO, tripDTO, ticketTransferDetailsDTO, fareMap, seatCodeMap, seatGenderMap, errors);

			// Station Point Mapping
			for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
				if (ticketTransferDetailsDTO.getBoardingPoint() != null) {
					if (pointDTO.getCode().equals(ticketTransferDetailsDTO.getBoardingPoint().getCode())) {
						transferDTO.setBoardingPoint(pointDTO);
						transferDTO.getBoardingPoint().setMinitues(tripDTO.getStage().getFromStation().getMinitues() + pointDTO.getMinitues());
						break;
					}
				}
				else if (repoTicketDTO.getBoardingPoint() != null) {
					if (pointDTO.getCode().equals(repoTicketDTO.getBoardingPoint().getCode())) {
						transferDTO.setBoardingPoint(pointDTO);
						transferDTO.getBoardingPoint().setMinitues(tripDTO.getStage().getFromStation().getMinitues() + pointDTO.getMinitues());
						break;
					}
				}
			}
			if (transferDTO.getBoardingPoint() == null || transferDTO.getBoardingPoint().getId() == 0) {
				errors.append(repoTicketDTO.getCode()).append(" Boarding Point Code not found|");
				continue;
			}
			if (StringUtil.isNull(errors.toString())) {
				validateTicketTransfer(authDTO, repoTicketDTO, ticketTransferDTO, errors);
			}
		}
		if (StringUtil.isNotNull(errors.toString())) {
			throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, errors.toString());
		}
	}

	@Override
	public void transferTicket(AuthDTO authDTO, TicketTransferDTO ticketTransferDTO) {
		try {
			/** Trip */
			TripCache tripCache = new TripCache();
			TripDTO transferTrip = tripCache.getTripDTO(authDTO, ticketTransferDTO.getTransferTrip());
			if (authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && DateUtil.NOW().gt(transferTrip.getTripDateTimeV2())) {
				LOGGER.error("{} ErrorCode TR001 {}", transferTrip.getCode(), transferTrip.getTripDateTimeV2());
				throw new ServiceException(ErrorCode.TRIP_CLOSED_NOT_ALLOW_BOOKING);
			}
			/** Schedule */
			ScheduleCache scheduleCache = new ScheduleCache();
			ScheduleDTO scheduleDTO = scheduleCache.getScheduleDTObyId(authDTO, transferTrip.getSchedule());
			scheduleDTO.setTripDate(transferTrip.getTripDate());

			/** Schedule Ticket Transfer Terms */
			ticketTransferDTO.setFromStation(getStationDTObyId(ticketTransferDTO.getFromStation()));
			ticketTransferDTO.setToStation(getStationDTObyId(ticketTransferDTO.getToStation()));
			ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO = scheduleTicketTransferTermsService.getScheduleTicketTransferTermsBySchedule(authDTO, scheduleDTO, ticketTransferDTO.getFromStation(), ticketTransferDTO.getToStation());
			if (scheduleTicketTransferTermsDTO == null) {
				LOGGER.error("ErrorCode 319 | 01", transferTrip.getCode());
				throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED);
			}

			if (scheduleTicketTransferTermsDTO.getMinutes() != 0 && !ticketTransferDTO.getAdditionalAttribute().get("FREE_TICKET")) {
				transferTrip.setSchedule(scheduleDTO);
				List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, transferTrip);
				DateTime originStationDateTime = BitsUtil.getOriginStationTime(stageList, transferTrip.getTripDate());

				scheduleTicketTransferTermsDTO.setDateTime(DateUtil.minusMinituesToDate(originStationDateTime, scheduleTicketTransferTermsDTO.getMinutes()));
				// Validate Minutes
				if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.MINUTES.getId()) {
					scheduleTicketTransferTermsDTO.setDateTime(DateUtil.minusMinituesToDate(originStationDateTime, scheduleTicketTransferTermsDTO.getMinutes()));
					if (scheduleTicketTransferTermsDTO != null && DateUtil.NOW().gt(scheduleTicketTransferTermsDTO.getDateTime())) {
						LOGGER.error("{} ErrorCode 319 | 02 {}", transferTrip.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
						throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
					}
				}
				else if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.AM.getId()) {
					DateTime checkTime = DateUtil.addMinituesToDate(transferTrip.getTripDate().getStartOfDay(), scheduleTicketTransferTermsDTO.getMinutes());
					Integer check = DateUtil.getMinutiesDifferent(DateUtil.NOW(), checkTime);
					if (check >= 0) {
						LOGGER.error("{} ErrorCode 319 | 02a {}", transferTrip.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
						throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
					}
				}
				else if (scheduleTicketTransferTermsDTO.getMinutesType().getId() == MinutesTypeEM.PM.getId()) {
					DateTime checkTime = DateUtil.addMinituesToDate(transferTrip.getTripDate().getStartOfDay(), 720 + scheduleTicketTransferTermsDTO.getMinutes());
					Integer check = DateUtil.getMinutiesDifferent(DateUtil.NOW(), checkTime);
					if (check < 0) {
						LOGGER.error("{} ErrorCode 319 | 02b {}", transferTrip.getCode(), scheduleTicketTransferTermsDTO.getDateTime());
						throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED, "Schedule Ticket Transfer Terms DateTime expired");
					}
				}
			}

			TripDTO oldTripDTO = ticketTransferDTO.getTrip();
			oldTripDTO = tripCache.getTripDTO(authDTO, oldTripDTO);

			Map<String, BusSeatLayoutDTO> fareMap = new HashMap<String, BusSeatLayoutDTO>();
			Map<String, String> seatCodeMap = new HashMap<>();
			Map<String, BusSeatLayoutDTO> seatGenderMap = new HashMap<>();
			TripDTO tripDTO = getTripDetails(authDTO, ticketTransferDTO, fareMap, seatCodeMap, seatGenderMap);

			/** Cancellation term */
			CancellationTermDTO cancellationTermDTO = cancellationTermsService.getCancellationTermsByTripDTO(authDTO, authDTO.getUser(), tripDTO);
			if (cancellationTermDTO == null) {
				throw new ServiceException(ErrorCode.CANCELLATION_TERMS_NOT_FOUND);
			}
			cancellationTermsService.getCancellationTermGroupIdByGroupKey(authDTO, cancellationTermDTO);

			List<TicketDTO> migrateTickets = new ArrayList<>();
			List<TicketTransferDetailsDTO> transferTickets = new ArrayList<>();
			Map<String, String> evenLogMap = new HashMap<>();
			Map<String, List<TicketDetailsDTO>> ticketDetailsMap = new HashMap<>();
			Map<String, TicketTransactionDTO> previousTicketTransactionMap = new HashMap<>();
			Map<String, List<UserTransactionDTO>> previousUserTransactionMap = new HashMap<>();
			StringBuilder errors = new StringBuilder();
			for (TicketTransferDetailsDTO ticketTransferDetailsDTO : ticketTransferDTO.getTicketTransferDetails()) {
				try {
					TicketDTO repoTicketDTO = new TicketDTO();
					repoTicketDTO.setCode(ticketTransferDetailsDTO.getTicket().getCode());
					Stack<TicketDetailsDTO> stack = new Stack<TicketDetailsDTO>();

					ticketService.getTicketStatus(authDTO, repoTicketDTO);
					/** Multiple Reschedule Permission Check */
					if (!authDTO.isMultipleRescheduleEnabled() && (repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.POSTPONE.getId() || repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId())) {
						LOGGER.error("{} ErrorCode 319 | 002", repoTicketDTO.getCode());
						throw new ServiceException(ErrorCode.PREPOSTPONE_NOT_ALLOWED);
					}

					TicketDTO transferDTO = ticketTransferDetailsDTO.getTransferTicket();
					if (repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.POSTPONE.getId() || repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId()) {
						transferDTO.setLookupId(repoTicketDTO.getLookupId());
					}
					else {
						transferDTO.setLookupId(repoTicketDTO.getId());
					}
					/** Validate Reschedule Count */
					if (authDTO.isMultipleRescheduleEnabled()) {
						validateTicketRescheduleMaxCount(authDTO, transferDTO);
					}

					List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
					for (Iterator<TicketDetailsDTO> iterator = repoTicketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
						TicketDetailsDTO detailsDTO = iterator.next();
						if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
							for (TicketDetailsDTO ticketDetailsDTO : ticketTransferDetailsDTO.getTicket().getTicketDetails()) {
								if (ticketDetailsDTO.getSeatCode().equals(detailsDTO.getSeatCode())) {
									seatDetailsList.add(detailsDTO);
									stack.push(detailsDTO);
									break;
								}
							}
						}
						else {
							iterator.remove();
						}
					}

					if (seatDetailsList.size() != ticketTransferDetailsDTO.getTicket().getTicketDetails().size()) {
						LOGGER.error("{} ErrorCode 308 DB Old Seats {} Given Old Seats {}", repoTicketDTO.getCode(), seatDetailsList.stream().map(repoTicket -> repoTicket.getSeatCode()).collect(Collectors.toList()), ticketTransferDetailsDTO.getTicket().getTicketDetails().stream().map(ticket -> ticket.getSeatCode()).collect(Collectors.toList()));
						throw new ServiceException(ErrorCode.SELECTED_STEAT_NOT_FOR_RESERVATION);
					}
					if (seatDetailsList.size() != ticketTransferDetailsDTO.getTransferTicket().getTicketDetails().size()) {
						LOGGER.error("{} ErrorCode 308 Old Seats {} Transferred Seats {}", repoTicketDTO.getCode(), seatDetailsList.stream().map(repoTicket -> repoTicket.getSeatCode()).collect(Collectors.toList()), ticketTransferDetailsDTO.getTransferTicket().getTicketDetails().stream().map(ticket -> ticket.getSeatCode()).collect(Collectors.toList()));
						throw new ServiceException(ErrorCode.SELECTED_STEAT_NOT_FOR_RESERVATION);
					}
					// copy Seat status and name
					Map<String, TicketDetailsDTO> seatCodes = new HashMap<>();
					for (TicketDetailsDTO transferSeatDetailsDTO : ticketTransferDetailsDTO.getTransferTicket().getTicketDetails()) {
						TicketDetailsDTO ticketDetailsDTO = stack.pop();
						transferSeatDetailsDTO.setSeatGendar(ticketDetailsDTO.getSeatGendar());
						transferSeatDetailsDTO.setTicketStatus(ticketDetailsDTO.getTicketStatus());
						transferSeatDetailsDTO.setPassengerAge(ticketDetailsDTO.getPassengerAge());
						transferSeatDetailsDTO.setPassengerName(ticketDetailsDTO.getPassengerName());
						seatCodes.put(transferSeatDetailsDTO.getSeatCode(), ticketDetailsDTO);
					}

					validateSeatGender(authDTO, tripDTO, ticketTransferDetailsDTO, fareMap, seatCodeMap, seatGenderMap);

					// To Check Free Ticket
					checkApplyFreeService(authDTO, repoTicketDTO, transferDTO, ticketTransferDTO.getAdditionalAttribute());

					repoTicketDTO.setFromStation(getStationDTObyId(repoTicketDTO.getFromStation()));
					repoTicketDTO.setToStation(getStationDTObyId(repoTicketDTO.getToStation()));

					// Station Point Mapping
					for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
						if (ticketTransferDetailsDTO.getBoardingPoint() != null) {
							if (pointDTO.getCode().equals(ticketTransferDetailsDTO.getBoardingPoint().getCode())) {
								transferDTO.setBoardingPoint(pointDTO);
								transferDTO.getBoardingPoint().setMinitues(tripDTO.getStage().getFromStation().getMinitues() + pointDTO.getMinitues());
								break;
							}
						}
						else if (repoTicketDTO.getBoardingPoint() != null) {
							if (pointDTO.getCode().equals(repoTicketDTO.getBoardingPoint().getCode())) {
								transferDTO.setBoardingPoint(pointDTO);
								transferDTO.getBoardingPoint().setMinitues(tripDTO.getStage().getFromStation().getMinitues() + pointDTO.getMinitues());
								break;
							}
						}
					}
					if (transferDTO.getBoardingPoint().getId() == 0) {
						throw new ServiceException(ErrorCode.STATION_POINT_CODE_INVALID);
					}
					// Station Dropping Point Mapping
					for (StationPointDTO pointDTO : tripDTO.getStage().getToStation().getStationPoint()) {
						if (ticketTransferDetailsDTO.getDroppingPoint() != null) {
							if (pointDTO.getCode().equals(ticketTransferDetailsDTO.getDroppingPoint().getCode())) {
								transferDTO.setDroppingPoint(pointDTO);
								transferDTO.getDroppingPoint().setMinitues(tripDTO.getStage().getToStation().getMinitues() + pointDTO.getMinitues());
								break;
							}
						}
						else if (repoTicketDTO.getDroppingPoint() != null) {
							if (pointDTO.getCode().equals(repoTicketDTO.getDroppingPoint().getCode())) {
								transferDTO.setDroppingPoint(pointDTO);
								transferDTO.getDroppingPoint().setMinitues(tripDTO.getStage().getToStation().getMinitues() + pointDTO.getMinitues());
								break;
							}
						}
					}
					if (transferDTO.getDroppingPoint() == null || transferDTO.getDroppingPoint().getId() == 0) {
						transferDTO.setDroppingPoint(tripDTO.getStage().getToStation().getStationPoint().get(tripDTO.getStage().getToStation().getStationPoint().size() - 1));
					}
					transferDTO.setTripDTO(tripDTO);
					int reportingMin = transferDTO.getBoardingPoint().getMinitues() - authDTO.getNamespace().getProfile().getBoardingReportingMinitues();
					transferDTO.setTravelMinutes(tripDTO.getStage().getFromStation().getMinitues());
					transferDTO.setReportingMinutes(reportingMin);
					transferDTO.setFromStation(tripDTO.getStage().getFromStation().getStation());
					transferDTO.setToStation(tripDTO.getStage().getToStation().getStation());
					transferDTO.setServiceNo(tripDTO.getSchedule().getServiceNumber());
					transferDTO.setTicketAt(DateUtil.NOW());

					// validate the From,To and Trip Date
					validateTicketTransfer(authDTO, repoTicketDTO, transferDTO);

					// Same Day Travel Date, just migrating seat Details
					if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId() && !ticketTransferDTO.getAdditionalAttribute().get("captureFareDifferece") && !ticketTransferDTO.getAdditionalAttribute().get("captureTransferCharge") && tripDTO.getTripDate().compareTo(repoTicketDTO.getTripDate()) == 0) {
						applyScheduleTicketTransferTerms(authDTO, transferDTO, scheduleTicketTransferTermsDTO, ticketTransferDTO.getAdditionalAttribute());
						transferDTO = migrateTicketDetailsV2(authDTO, repoTicketDTO, transferDTO, evenLogMap, ticketDetailsMap);
						migrateTickets.add(transferDTO);
					}
					else {
						if (repoTicketDTO.getFromStation().getId() != tripDTO.getStage().getFromStation().getStation().getId() && repoTicketDTO.getFromStation().getId() != tripDTO.getStage().getToStation().getStation().getId()) {
							LOGGER.error("{} ErrorCode PG14 01 {}", repoTicketDTO.getCode());
							throw new ServiceException(ErrorCode.MANDATORY_PARAMETERS_MISSING);
						}
						DateTime previousDate = DateUtil.NOW().minusDays(authDTO.getNamespace().getProfile().getRescheduleOverrideAllowDays());
						if (authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && previousDate.gt(oldTripDTO.getTripDateTimeV2())) {
							LOGGER.error("{} ErrorCode TR01 {}", repoTicketDTO.getCode(), oldTripDTO.getTripDateTimeV2());
							throw new ServiceException(ErrorCode.TRIP_CLOSED_NOT_ALLOW_BOOKING);
						}
						// Transfer ticket
						transferDTO.setJourneyType(JourneyTypeEM.POSTPONE);
						if (tripDTO.getTripDate().compareTo(repoTicketDTO.getTripDate()) == -1) {
							transferDTO.setJourneyType(JourneyTypeEM.PREPONE);
						}
						repoTicketDTO.setJourneyType(transferDTO.getJourneyType());
						transferDTO.setDeviceMedium(repoTicketDTO.getDeviceMedium());
						transferDTO.setPassengerEmailId(repoTicketDTO.getPassengerEmailId());
						transferDTO.setPassengerMobile(repoTicketDTO.getPassengerMobile());
						transferDTO.setAlternateMobile(repoTicketDTO.getAlternateMobile());
						TicketTransferDetailsDTO ticketTransferDetails = transferTicket(authDTO, repoTicketDTO, transferDTO, scheduleTicketTransferTermsDTO, cancellationTermDTO, ticketTransferDTO.getAdditionalAttribute(), seatCodes, previousTicketTransactionMap, previousUserTransactionMap);
						transferTickets.add(ticketTransferDetails);
					}
				}
				catch (ServiceException e) {
					errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(Text.SINGLE_SPACE).append(e.getMessage()).append(Text.VERTICAL_BAR);
				}
				catch (Exception e) {
					errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(Text.HYPHEN).append(Text.VERTICAL_BAR);
				}
			}

			if (!migrateTickets.isEmpty()) {
				for (TicketDTO ticketDTO : migrateTickets) {
					saveMigrateTicketDetails(authDTO, ticketDTO, evenLogMap.get(ticketDTO.getCode()), ticketDetailsMap.get(ticketDTO.getCode()), errors);
				}
			}
			if (!transferTickets.isEmpty()) {
				for (TicketTransferDetailsDTO ticketTransferDetails : transferTickets) {
					saveTransferTicket(authDTO, ticketTransferDetails.getTicket(), ticketTransferDetails.getTransferTicket(), previousTicketTransactionMap.get(ticketTransferDetails.getTicket().getCode()), previousUserTransactionMap.get(ticketTransferDetails.getTicket().getCode()), errors, ticketTransferDTO.getAdditionalAttribute());
				}
			}

			// Clear Trip block Seats
			tripService.clearBookedBlockedSeatsCache(authDTO, transferTrip);
			tripService.clearBookedBlockedSeatsCache(authDTO, oldTripDTO);

			if (StringUtil.isNotNull(errors.toString())) {
				throw new ServiceException(ErrorCode.UNABLE_TO_TRANSFER_TICKET, errors.toString());
			}
		}
		catch (ServiceException e) {
			LOGGER.error("", e);
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			LOGGER.error("", e);
			throw new ServiceException(ErrorCode.UNABLE_TO_TRANSFER_TICKET);
		}
	}

	private TripDTO getTripDetails(AuthDTO authDTO, TicketTransferDTO ticketTransferDTO, Map<String, BusSeatLayoutDTO> fareMap, Map<String, String> seatCodeMap, Map<String, BusSeatLayoutDTO> seatGenderMap) {
		SearchDTO searchDTO = new SearchDTO();
		searchDTO.setTravelDate(ticketTransferDTO.getTravelDate());
		searchDTO.setFromStation(ticketTransferDTO.getFromStation());
		searchDTO.setToStation(ticketTransferDTO.getToStation());
		ticketTransferDTO.getTransferTrip().setSearch(searchDTO);

		TripDTO returnTripDTO = busmapService.getSearchBusmapV3(authDTO, ticketTransferDTO.getTransferTrip());
		if (returnTripDTO == null || returnTripDTO.getBus() == null || returnTripDTO.getBus().getBusSeatLayoutDTO() == null) {
			System.out.println("Error:0098A0 " + ticketTransferDTO.getFromStation().getCode() + "-" + ticketTransferDTO.getToStation().getCode() + "-" + ticketTransferDTO.getTravelDate());
		}

		// Get seatFare
		List<BusSeatLayoutDTO> seatLayoutDTOList = returnTripDTO.getBus().getBusSeatLayoutDTO().getList();

		Map<String, StageFareDTO> seatFareMap = new HashMap<String, StageFareDTO>();
		// Group Wise Fare and Default Fare
		for (StageFareDTO fareDTO : returnTripDTO.getStage().getStageFare()) {
			if (fareDTO.getGroup().getId() != 0) {
				seatFareMap.put(fareDTO.getGroup().getId() + fareDTO.getBusSeatType().getCode(), fareDTO);
			}
			else {
				seatFareMap.put(fareDTO.getBusSeatType().getCode(), fareDTO);
			}
		}
		// Get Group Wise Fare and Default Fare
		for (BusSeatLayoutDTO seatLayoutDTO : seatLayoutDTOList) {
			if (seatLayoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_YOU || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_ALL || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_MALE || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_FEMALE) {
				if (seatFareMap.get(authDTO.getGroup().getId() + seatLayoutDTO.getBusSeatType().getCode()) != null) {

					// Seat Fare
					if (seatLayoutDTO.getFare() == null) {
						seatLayoutDTO.setFare(seatFareMap.get(authDTO.getGroup().getId() + seatLayoutDTO.getBusSeatType().getCode()).getFare());
					}
					fareMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
				}
				else if (seatFareMap.get(seatLayoutDTO.getBusSeatType().getCode()) != null) {
					if (seatLayoutDTO.getFare() == null) {
						seatLayoutDTO.setFare(seatFareMap.get(seatLayoutDTO.getBusSeatType().getCode()).getFare());
					}
					fareMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
				}
			}
		}

		/** Adjacent seat gender validation */
		List<BusSeatLayoutDTO> seatList = returnTripDTO.getSchedule().getScheduleBus().getBus().getBusSeatLayoutDTO().getList();
		for (BusSeatLayoutDTO dto : seatList) {
			seatCodeMap.put(dto.getCode(), dto.getLayer() + "_" + dto.getRowPos() + "_" + dto.getColPos());
			if (dto.getSeatStatus() != null) {
				seatGenderMap.put(dto.getLayer() + "_" + dto.getRowPos() + "_" + dto.getColPos(), dto);
			}
		}
		return returnTripDTO;
	}

	private void validateSeatGender(AuthDTO authDTO, TripDTO returnTripDTO, TicketTransferDetailsDTO ticketTransferDetailsDTO, Map<String, BusSeatLayoutDTO> fareMap, Map<String, String> seatCodeMap, Map<String, BusSeatLayoutDTO> seatGenderMap) {
		ticketTransferDetailsDTO.getTransferTicket().setPnrStartCode(returnTripDTO.getSchedule().getPnrStartCode());

		List<BusSeatLayoutDTO> bookedSeats = new ArrayList<>();
		for (TicketDetailsDTO ticketDetailsDTO : ticketTransferDetailsDTO.getTransferTicket().getTicketDetails()) {
			BusSeatLayoutDTO seatLayoutDTO = fareMap.get(ticketDetailsDTO.getSeatCode());
			if (seatLayoutDTO == null) {
				LOGGER.error("{} ErrorCode BO001 Seat Code {}", ticketTransferDetailsDTO.getTicket().getCode(), ticketDetailsDTO.getSeatCode());
				throw new ServiceException(ErrorCode.SEAT_ALREADY_BLOCKED);
			}
			ticketDetailsDTO.setSeatFare(seatLayoutDTO.getFare());
			ticketDetailsDTO.setAcBusTax(seatLayoutDTO.getFare().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING).multiply(returnTripDTO.getSchedule().getTax().getServiceTax()));
			ticketDetailsDTO.setSeatCode(seatLayoutDTO.getCode());
			ticketDetailsDTO.setSeatName(seatLayoutDTO.getName());
			ticketDetailsDTO.setSeatType(seatLayoutDTO.getBusSeatType().getCode());
			ticketDetailsDTO.setActiveFlag(1);

			/** adjacent seat validation */
			String seatPos = seatCodeMap.get(ticketDetailsDTO.getSeatCode());
			if (seatPos != null) {
				int layer = Integer.parseInt(seatPos.split("_")[0]);
				int rowCount = Integer.parseInt(seatPos.split("_")[1]);
				int colCount = Integer.parseInt(seatPos.split("_")[2]);
				/** Female Seat validations */
				if (ticketDetailsDTO.getSeatGendar().getId() == SeatGendarEM.FEMALE.getId()) {
					/** Right Side seat */
					if (seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId())) {
							LOGGER.error("{} ErrorCode 405 | 01 Seat Code {} ( {} ) {}", ticketTransferDetailsDTO.getTicket().getCode(), layoutDTO.getCode(), layoutDTO.getName(), layoutDTO.getSeatStatus().getCode());
							throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
						}
					}
					/** Left Side seat */
					else if (seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId())) {
							LOGGER.error("{} ErrorCode 405 | 02 Seat Code {} ( {} ) {}", ticketTransferDetailsDTO.getTicket().getCode(), layoutDTO.getCode(), layoutDTO.getName(), layoutDTO.getSeatStatus().getCode());
							throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
						}
					}
				}
				/** Male Seat validations */
				else if (ticketDetailsDTO.getSeatGendar().getId() == SeatGendarEM.MALE.getId()) {
					/** Right Side seat */
					if (seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId())) {
							LOGGER.error("{} ErrorCode 405 | 03 Seat Code {} ( {} ) {}", ticketTransferDetailsDTO.getTicket().getCode(), layoutDTO.getCode(), layoutDTO.getName(), layoutDTO.getSeatStatus().getCode());
							throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
						}
					}
					/** Left Side seat */
					else if (seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId())) {
							LOGGER.error("{} ErrorCode 405 | 04 Seat Code {} ( {} ) {}", ticketTransferDetailsDTO.getTicket().getCode(), layoutDTO.getCode(), layoutDTO.getName(), layoutDTO.getSeatStatus().getCode());
							throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
						}
					}
				}

				BusSeatLayoutDTO layoutDTO = new BusSeatLayoutDTO();
				layoutDTO.setSeatGendar(ticketDetailsDTO.getSeatGendar());
				layoutDTO.setSeatStatus(SeatStatusEM.BOOKED);
				layoutDTO.setCode(ticketDetailsDTO.getSeatCode());
				layoutDTO.setLayer(layer);
				layoutDTO.setRowPos(rowCount);
				layoutDTO.setColPos(colCount);
				bookedSeats.add(layoutDTO);
			}

			/** Seat Preference */
			if (seatLayoutDTO.getSeatGendar() != null) {
				if (seatLayoutDTO.getSeatGendar().getId() != ticketDetailsDTO.getSeatGendar().getId()) {
					LOGGER.error("{} ErrorCode 405A0 Bus Layout Seat {} ( {} ) {} Transferred Seat {} ( {} ) {}", ticketTransferDetailsDTO.getTicket().getCode(), seatLayoutDTO.getCode(), seatLayoutDTO.getName(), seatLayoutDTO.getSeatGendar().getCode(), ticketDetailsDTO.getCode(), ticketDetailsDTO.getName(), ticketDetailsDTO.getSeatGendar().getCode());
					throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER_PREFERENCE);
				}
			}
		}

		for (BusSeatLayoutDTO busSeatLayoutDTO : bookedSeats) {
			BusSeatLayoutDTO layoutDTO = seatGenderMap.get(busSeatLayoutDTO.getLayer() + "_" + busSeatLayoutDTO.getRowPos() + "_" + busSeatLayoutDTO.getColPos());
			layoutDTO.setSeatGendar(busSeatLayoutDTO.getSeatGendar());
			layoutDTO.setSeatStatus(SeatStatusEM.BOOKED);
			seatGenderMap.put(busSeatLayoutDTO.getLayer() + "_" + busSeatLayoutDTO.getRowPos() + "_" + busSeatLayoutDTO.getColPos(), layoutDTO);
		}

		/** Schedule Discount */
		List<TicketAddonsDetailsDTO> discountList = new ArrayList<>();
		if (returnTripDTO.getSchedule().getScheduleDiscount() != null && returnTripDTO.getSchedule().getScheduleDiscount().getList() != null) {
			for (TicketDetailsDTO ticketDetailsDTO : ticketTransferDetailsDTO.getTransferTicket().getTicketDetails()) {
				for (ScheduleDiscountDTO scheduleDiscountDTO : returnTripDTO.getSchedule().getScheduleDiscount().getList()) {
					/** Female Discount */
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
			// ticketTransferDetailsDTO.getTransferTicket().setTicketAddonsDetails(discountList);
		}
	}

	private void validateSeatGender(AuthDTO authDTO, TripDTO returnTripDTO, TicketTransferDetailsDTO ticketTransferDetailsDTO, Map<String, BusSeatLayoutDTO> fareMap, Map<String, String> seatCodeMap, Map<String, BusSeatLayoutDTO> seatGenderMap, StringBuilder errors) {
		ticketTransferDetailsDTO.getTransferTicket().setPnrStartCode(returnTripDTO.getSchedule().getPnrStartCode());

		List<BusSeatLayoutDTO> bookedSeats = new ArrayList<>();
		for (TicketDetailsDTO ticketDetailsDTO : ticketTransferDetailsDTO.getTransferTicket().getTicketDetails()) {
			BusSeatLayoutDTO seatLayoutDTO = fareMap.get(ticketDetailsDTO.getSeatCode());
			if (seatLayoutDTO == null) {
				errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" ").append(ticketDetailsDTO.getSeatName()).append(" [").append(ticketDetailsDTO.getSeatCode()).append("] is invalid|");
				continue;
			}

			/** adjacent seat validation */
			String seatPos = seatCodeMap.get(ticketDetailsDTO.getSeatCode());
			if (seatPos != null) {
				int layer = Integer.parseInt(seatPos.split("_")[0]);
				int rowCount = Integer.parseInt(seatPos.split("_")[1]);
				int colCount = Integer.parseInt(seatPos.split("_")[2]);
				BusSeatLayoutDTO busSeatLayoutDTO = seatGenderMap.get(layer + "_" + rowCount + "_" + colCount);

				/** Female Seat validations */
				if (ticketDetailsDTO.getSeatGendar().getId() == SeatGendarEM.FEMALE.getId()) {
					/** Right Side seat */
					if (seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId())) {
							errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" ").append(busSeatLayoutDTO.getName()).append(" [").append(ticketDetailsDTO.getSeatCode()).append("] invalid gender|");
							continue;
						}
					}
					/** Left Side seat */
					else if (seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId())) {
							errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" ").append(busSeatLayoutDTO.getName()).append(" [").append(ticketDetailsDTO.getSeatCode()).append("] invalid gender|");
							continue;
						}
					}
				}
				/** Male Seat validations */
				else if (ticketDetailsDTO.getSeatGendar().getId() == SeatGendarEM.MALE.getId()) {
					/** Right Side seat */
					if (seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId())) {
							errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" ").append(busSeatLayoutDTO.getName()).append(" [").append(ticketDetailsDTO.getSeatCode()).append("] invalid gender|");
							continue;
						}
					}
					/** Left Side seat */
					else if (seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId())) {
							errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" ").append(busSeatLayoutDTO.getName()).append(" [").append(ticketDetailsDTO.getSeatCode()).append("] invalid gender|");
							continue;
						}
					}
				}

				BusSeatLayoutDTO layoutDTO = new BusSeatLayoutDTO();
				layoutDTO.setSeatGendar(ticketDetailsDTO.getSeatGendar());
				layoutDTO.setSeatStatus(SeatStatusEM.BOOKED);
				layoutDTO.setCode(ticketDetailsDTO.getSeatCode());
				layoutDTO.setLayer(layer);
				layoutDTO.setRowPos(rowCount);
				layoutDTO.setColPos(colCount);
				bookedSeats.add(layoutDTO);
			}

			/** Seat Preference */
			if (seatLayoutDTO.getSeatGendar() != null) {
				if (seatLayoutDTO.getSeatGendar().getId() != ticketDetailsDTO.getSeatGendar().getId()) {
					errors.append(ticketTransferDetailsDTO.getTicket().getCode()).append(" ").append(ticketDetailsDTO.getSeatName()).append(" [").append(ticketDetailsDTO.getSeatCode()).append("] invalid gender|");
					continue;
				}
			}
		}

		for (BusSeatLayoutDTO busSeatLayoutDTO : bookedSeats) {
			BusSeatLayoutDTO layoutDTO = seatGenderMap.get(busSeatLayoutDTO.getLayer() + "_" + busSeatLayoutDTO.getRowPos() + "_" + busSeatLayoutDTO.getColPos());
			layoutDTO.setSeatGendar(busSeatLayoutDTO.getSeatGendar());
			layoutDTO.setSeatStatus(SeatStatusEM.BOOKED);
			seatGenderMap.put(busSeatLayoutDTO.getLayer() + "_" + busSeatLayoutDTO.getRowPos() + "_" + busSeatLayoutDTO.getColPos(), layoutDTO);
		}
	}

	private void validateTicketTransfer(AuthDTO authDTO, TicketDTO ticketDTO, TicketTransferDTO transferDTO, StringBuilder errors) {
		// Once one pre/post pone allowed
		if (authDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId() && transferDTO.getTrip().getTripDate().compareTo(ticketDTO.getTripDate()) != 0 && ticketDTO.getJourneyType().getId() != JourneyTypeEM.ONWARD_TRIP.getId() && ticketDTO.getJourneyType().getId() != JourneyTypeEM.RETURN_TRIP.getId()) {
			errors.append(ticketDTO.getCode()).append(" Ticket transfer is not allowed to the selected trip |");
		}
		else {
			int condition = 0;
			if (ticketDTO.getFromStation().getId() == transferDTO.getFromStation().getId()) {
				condition++;
			}
			if (ticketDTO.getToStation().getId() == transferDTO.getToStation().getId()) {
				condition++;
			}

			if (ticketDTO.getTripDate().compareTo(transferDTO.getTransferTrip().getTripDate()) == 0) {
				condition++;
			}

			if (condition < 2) {
				errors.append(ticketDTO.getCode()).append(" Ticket transfer is not allowed to the selected trip|");
			}
		}
	}

	private TicketDTO migrateTicketDetailsV2(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO transferDTO, Map<String, String> evenLogMap, Map<String, List<TicketDetailsDTO>> ticketDetailsMap) {
		TripDAO tripDAO = new TripDAO();
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, ticketDTO);
		Stack<TicketDetailsDTO> transferstack = new Stack<TicketDetailsDTO>();
		transferstack.addAll(transferDTO.getTicketDetails());
		List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
		// Change Trip Stage Seat Details
		StringBuilder migrateSeats = new StringBuilder();
		StringBuilder oldSeats = new StringBuilder();
		StringBuilder newSeats = new StringBuilder();
		for (TicketDetailsDTO detailsDTO : seatList) {
			if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
					if (ticketDetailsDTO.getSeatCode().equals(detailsDTO.getSeatCode())) {

						TicketDetailsDTO transferDetailsDTO = transferstack.pop();
						oldSeats.append((oldSeats.length() > 0 ? ", " : "") + ticketDetailsDTO.getSeatName());
						newSeats.append((newSeats.length() > 0 ? ", " : "") + transferDetailsDTO.getSeatName());
						detailsDTO.setSeatGendar(detailsDTO.getSeatGendar());
						detailsDTO.setTicketStatus(detailsDTO.getTicketStatus());
						detailsDTO.setSeatCode(transferDetailsDTO.getSeatCode());
						detailsDTO.setSeatName(transferDetailsDTO.getSeatName());
						ticketDetailsDTO.setSeatCode(transferDetailsDTO.getSeatCode());
						ticketDetailsDTO.setSeatName(transferDetailsDTO.getSeatName());
						detailsDTO.setBoardingPointName(transferDTO.getBoardingPoint().getName());
						detailsDTO.setStationPoint(BitsUtil.convertStationPoint(transferDTO.getBoardingPoint(), transferDTO.getDroppingPoint()));
						detailsDTO.getTicketExtra().setEditChangeSeat(1);
						seatDetailsList.add(detailsDTO);
						break;
					}
				}
			}
		}

		boolean seatDouplicateEntry = ticketService.checkSeatDuplicateEntryV2(authDTO, transferDTO);
		if (seatDouplicateEntry) {
			LOGGER.error("{} ErrorCode 611 | 2", ticketDTO.getCode());
			throw new ServiceException(ErrorCode.DOUBLE_ENTRY_VALIDATION_FAIL);
		}

		if (StringUtil.isNull(ticketDTO.getRemarks())) {
			String remarks = "Transfered from " + StringUtil.substring(ticketDTO.getFromStation().getName(), 3) + " - " + StringUtil.substring(ticketDTO.getToStation().getName(), 3) + " @ " + ticketDTO.getTripDateTime().format("YY-MM-DD hh:mm") + " :" + ticketDTO.getTripDTO().getCode() + " #:" + ticketDTO.getSeatNames();
			ticketDTO.setRemarks(StringUtil.substring(remarks, 240));
		}
		migrateSeats.append("(" + ticketDTO.getServiceNo() + ") " + oldSeats + " - " + "(" + transferDTO.getServiceNo() + ") " + newSeats);
		migrateSeats.append(Text.COMMA + Text.SINGLE_SPACE + Text.NEW_LINE);
		if (ticketDTO.getTripDTO().getCode().equals(transferDTO.getTripDTO().getCode())) {
			migrateSeats.append("TC: " + ticketDTO.getTripDTO().getCode() + " -> " + transferDTO.getTripDTO().getCode() + Text.COMMA + Text.NEW_LINE);
		}
		// Migrate From Station
		if (ticketDTO.getFromStation().getId() != transferDTO.getFromStation().getId()) {
			migrateSeats.append("From: " + ticketDTO.getFromStation().getName() + " -> " + transferDTO.getFromStation().getName() + Text.COMMA + Text.NEW_LINE);
			ticketDTO.setFromStation(transferDTO.getFromStation());
		}
		// Migrate To Station
		if (ticketDTO.getToStation().getId() != transferDTO.getToStation().getId()) {
			migrateSeats.append("To: " + ticketDTO.getToStation().getName() + " -> " + transferDTO.getToStation().getName() + Text.COMMA + Text.NEW_LINE);
			ticketDTO.setToStation(transferDTO.getToStation());
		}
		if (ticketDTO.getBoardingPoint().getId() != transferDTO.getBoardingPoint().getId()) {
			migrateSeats.append("Boarding: " + StringUtil.substring(ticketDTO.getBoardingPoint().getName(), Numeric.SIX_INT) + " -> " + StringUtil.substring(transferDTO.getBoardingPoint().getName(), Numeric.SIX_INT) + Text.COMMA + Text.NEW_LINE);
		}
		if (ticketDTO.getDroppingPoint().getId() != transferDTO.getDroppingPoint().getId()) {
			migrateSeats.append("Dropping: " + StringUtil.substring(ticketDTO.getDroppingPoint().getName(), Numeric.SIX_INT) + " -> " + StringUtil.substring(transferDTO.getDroppingPoint().getName(), Numeric.SIX_INT) + Text.COMMA + Text.NEW_LINE);
		}
		ticketDTO.setTripDTO(transferDTO.getTripDTO());
		int reportingMin = ticketDTO.getBoardingPoint().getMinitues() - authDTO.getNamespace().getProfile().getBoardingReportingMinitues();
		ticketDTO.setTravelMinutes(transferDTO.getTripDTO().getStage().getFromStation().getMinitues());
		ticketDTO.setReportingMinutes(reportingMin);
		ticketDTO.setBoardingPoint(transferDTO.getBoardingPoint());
		ticketDTO.setDroppingPoint(transferDTO.getDroppingPoint());
		ticketDTO.setServiceNo(transferDTO.getServiceNo());
		evenLogMap.put(ticketDTO.getCode(), migrateSeats.toString());
		ticketDetailsMap.put(ticketDTO.getCode(), seatDetailsList);
		return ticketDTO;
	}

	private TicketTransferDetailsDTO transferTicket(AuthDTO authDTO, TicketDTO repoTicketDTO, TicketDTO transferDTO, ScheduleTicketTransferTermsDTO scheduleTicketTransferTerms, CancellationTermDTO cancellationTermDTO, Map<String, Boolean> additionalAttribute, Map<String, TicketDetailsDTO> seatCodes, Map<String, TicketTransactionDTO> previousTicketTransactionMap, Map<String, List<UserTransactionDTO>> previousUserTransactionMap) {
		TripDAO tripDAO = new TripDAO();

		/** Get trip stage seat details & convert ticket extra */
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, repoTicketDTO);
		Map<String, TicketExtraDTO> repoTicketExtraMap = new HashMap<String, TicketExtraDTO>();
		for (TicketDetailsDTO detailsDTO : seatList) {
			repoTicketExtraMap.put(detailsDTO.getSeatCode(), detailsDTO.getTicketExtra());
			detailsDTO.setActiveFlag(0);
		}

		transferDTO.setCancellationTerm(cancellationTermDTO);

		transferDTO.setTicketUser(repoTicketDTO.getTicketUser());
		transferDTO.setTicketForUser(repoTicketDTO.getTicketForUser());
		transferDTO.setFromStation(transferDTO.getTripDTO().getStage().getFromStation().getStation());
		transferDTO.setToStation(transferDTO.getTripDTO().getStage().getToStation().getStation());
		transferDTO.setTripDate(transferDTO.getTripDTO().getTripDate());

		if (StringUtil.isNull(repoTicketDTO.getRemarks())) {
			String remarks = "Transfered from " + StringUtil.substring(repoTicketDTO.getFromStation().getName(), 3) + " - " + StringUtil.substring(repoTicketDTO.getToStation().getName(), 3) + "@" + repoTicketDTO.getTripDateTime().format(Text.DATE_TIME_DATE4J) + " :" + repoTicketDTO.getTripDTO().getCode() + " #:" + repoTicketDTO.getSeatNames();
			transferDTO.setRemarks(StringUtil.substring(remarks, 240));
		}
		/** Check seat duplicate entry */
		boolean seatDouplicateEntry = ticketService.checkSeatDuplicateEntryV2(authDTO, transferDTO);
		if (seatDouplicateEntry) {
			LOGGER.error("{} ErrorCode 611 | 01", repoTicketDTO.getCode());
			throw new ServiceException(ErrorCode.DOUBLE_ENTRY_VALIDATION_FAIL);
		}

		if (!authDTO.isMultipleRescheduleEnabled() && StringUtil.isNull(transferDTO.getCode()) && (repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId() || repoTicketDTO.getJourneyType().getId() == JourneyTypeEM.PREPONE.getId())) {
			transferDTO.setCode(repoTicketDTO.getCode() + "-" + repoTicketDTO.getJourneyType().getCode());
		}
		else if (StringUtil.isNull(transferDTO.getCode())) {
			/** Generate ticket code */
			if (authDTO.getNamespace().getProfile().getPnrGenerateType().getId() == PNRGenerateTypeEM.ENCODE_ALPHANUMERIC.getId()) {
				String PNRstartCode = StringUtil.isNotNull(transferDTO.getPnrStartCode()) ? StringUtil.removeSymbol(transferDTO.getPnrStartCode()).length() == 3 ? transferDTO.getPnrStartCode() : authDTO.getNamespace().getProfile().getPnrStartCode() : authDTO.getNamespace().getProfile().getPnrStartCode();
				String ticketCode = TokenGenerator.generateCode(PNRstartCode);
				transferDTO.setCode(ticketCode);
			}
			else if (authDTO.getNamespace().getProfile().getPnrGenerateType().getId() == PNRGenerateTypeEM.SEQUENCE_NUMBERIC.getId()) {
				String ticketCode = utilService.getGenerateSequenceNumber(authDTO);
				transferDTO.setCode(ticketCode);

				boolean status = ticketService.checkDuplicateTicketCodeEntry(authDTO, transferDTO.getCode());
				if (status) {
					LOGGER.error("{} ErrorCode 611 | 04", repoTicketDTO.getCode());
					throw new ServiceException(ErrorCode.DOUPLICATE_ENTRY_VALIDATION_FAIL);
				}
			}
		}
		if (StringUtil.isNull(transferDTO.getCode())) {
			transferDTO.setBookingCode(transferDTO.getCode());
		}

		/** Check ticket duplicate entry */
		if (StringUtil.isNotNull(transferDTO.getCode())) {
			boolean status = ticketService.checkDuplicateTicketCodeEntry(authDTO, transferDTO.getCode());
			if (status) {
				LOGGER.error("{} ErrorCode 611 | 02", repoTicketDTO.getCode());
				throw new ServiceException(ErrorCode.DOUPLICATE_ENTRY_VALIDATION_FAIL);
			}
		}

		transferDTO.setTicketStatus(repoTicketDTO.getTicketStatus());
		transferDTO.setTicketAt(DateTime.now(TimeZone.getDefault()));

		int fareDifferenceType = fareDifferenceType(repoTicketDTO, transferDTO);
		if ((fareDifferenceType == 2 && additionalAttribute.get("captureFareDifferece") && !additionalAttribute.get("captureLowFareDifferece")) || (!additionalAttribute.get("captureFareDifferece"))) {
			for (TicketDetailsDTO ticketDetailsDTO : transferDTO.getTicketDetails()) {
				TicketDetailsDTO repoTicketDetails = seatCodes.get(ticketDetailsDTO.getSeatCode());
				ticketDetailsDTO.setSeatFare(repoTicketDetails.getSeatFare());
				ticketDetailsDTO.setAcBusTax(repoTicketDetails.getAcBusTax());
			}
		}

		/** Apply schedule ticket transfer terms */
		applyScheduleTicketTransferTerms(authDTO, transferDTO, scheduleTicketTransferTerms, additionalAttribute);

		/** Previous Ticket Addon */
		BigDecimal transferTicketAddon = getTransferTicketAddon(repoTicketDTO, transferDTO, additionalAttribute);
		if (transferTicketAddon.compareTo(BigDecimal.ZERO) == 1) {
			BigDecimal transferSeatAddon = transferTicketAddon.divide(BigDecimal.valueOf(transferDTO.getTicketDetails().size()), RoundingMode.CEILING);
			for (TicketDetailsDTO transferTicketDetails : transferDTO.getTicketDetails()) {
				TicketDetailsDTO ticketDetailsDTO = seatCodes.get(transferTicketDetails.getSeatCode());
				TicketAddonsDetailsDTO ticketAddonsDetails = new TicketAddonsDetailsDTO();
				ticketAddonsDetails.setSeatCode(transferTicketDetails.getSeatCode());
				ticketAddonsDetails.setRefferenceId(ticketDetailsDTO.getId());
				ticketAddonsDetails.setRefferenceCode(repoTicketDTO.getCode());
				ticketAddonsDetails.setActiveFlag(Numeric.ONE_INT);
				ticketAddonsDetails.setValue(transferSeatAddon);
				ticketAddonsDetails.setAddonsType(AddonsTypeEM.TRANSFER_PREVIOUS_TICKET_AMOUNT);
				transferDTO.getTicketAddonsDetails().add(ticketAddonsDetails);
			}
		}

		/** Re-calculate service tax - GST for discount */
		recalculateServiceTaxAmount(authDTO, repoTicketDTO, transferDTO, seatCodes);

		BigDecimal debitAmount = BigDecimal.ZERO;
		String toPayAmount = Text.EMPTY;
		if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId() && additionalAttribute.get("captureFareDifferece")) {
			debitAmount = getPayableAmount(repoTicketDTO, transferDTO, additionalAttribute.get("captureTransferCharge"));
			if (debitAmount.compareTo(BigDecimal.ZERO) == 1) {
				toPayAmount = " Topay " + debitAmount.setScale(0, RoundingMode.HALF_UP);
			}
		}
		if (StringUtil.isNotNull(toPayAmount)) {
			transferDTO.setRemarks(composeTicketRemarks(transferDTO.getRemarks(), toPayAmount));
		}

		/** Get ticket transaction */
		TicketTransactionDTO previousTicketTransaction = new TicketTransactionDTO();
		List<UserTransactionDTO> previousUserTransactions = new ArrayList<>();
		if (repoTicketDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && additionalAttribute.get("captureFareDifferece") && fareDifferenceType == 1 && repoTicketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
			checkAndUpdateTicketTransactionsV2(authDTO, repoTicketDTO, transferDTO, previousTicketTransaction, previousUserTransactions);
		}
		else if (repoTicketDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
			checkAndUpdateTicketTransactions(authDTO, repoTicketDTO, transferDTO, previousUserTransactions);
		}
		previousTicketTransactionMap.put(repoTicketDTO.getCode(), previousTicketTransaction);
		previousUserTransactionMap.put(repoTicketDTO.getCode(), previousUserTransactions);

		/** Change old ticket status */
		for (TicketDetailsDTO ticketDetailsDTO : repoTicketDTO.getTicketDetails()) {
			ticketDetailsDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
			ticketDetailsDTO.setTicketExtra(repoTicketExtraMap.get(ticketDetailsDTO.getSeatCode()));
			ticketDetailsDTO.getTicketExtra().setNetAmount(BigDecimal.ZERO);
		}
		repoTicketDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
		for (TicketAddonsDetailsDTO addonsDetailsDTO : repoTicketDTO.getTicketAddonsDetails()) {
			addonsDetailsDTO.setTicketStatus(TicketStatusEM.TICKET_TRANSFERRED);
		}

		/** Change ticket addons status */
		if (transferDTO.getTicketAddonsDetails() != null && !transferDTO.getTicketAddonsDetails().isEmpty()) {
			for (TicketAddonsDetailsDTO discountDetailsDTO : transferDTO.getTicketAddonsDetails()) {
				discountDetailsDTO.setTicketStatus(transferDTO.getTicketStatus());
			}
		}

		if (transferDTO.getTicketExtra() != null) {
			transferDTO.getTicketExtra().setTicketTransfer(1);
		}
		else {
			transferDTO.setTicketExtra(new TicketExtraDTO());
			transferDTO.getTicketExtra().setTicketTransfer(1);
		}

		TicketTransferDetailsDTO ticketTransferDetailsDTO = new TicketTransferDetailsDTO();
		ticketTransferDetailsDTO.setTicket(repoTicketDTO);
		ticketTransferDetailsDTO.setTransferTicket(transferDTO);
		return ticketTransferDetailsDTO;
	}

	private void saveMigrateTicketDetails(AuthDTO authDTO, TicketDTO ticketDTO, String event, List<TicketDetailsDTO> ticketDetails, StringBuilder errors) {
		TicketEditDAO editDAO = new TicketEditDAO();
		// Save data
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);
				// update ticket table
				editDAO.editMigrateTicket(authDTO, ticketDTO, connection, event);

				// update Trip stage seat details table
				ticketDTO.setTicketDetails(ticketDetails);

				tripService.updateTripTransferSeatDetails(connection, authDTO, ticketDTO);
			}
			catch (SQLTransactionRollbackException e) {
				slack.sendAlert(authDTO, ticketDTO.getCode() + " DL03 - " + " -  Deadlock found when trying to get lock; try restarting transaction");
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
			errors.append(ticketDTO.getCode()).append(" ").append(e.getMessage()).append("|");
			e.printStackTrace();
			LOGGER.error("{} {}", ticketDTO.getCode(), e);
		}
	}

	private void saveTransferTicket(AuthDTO authDTO, TicketDTO repoTicketDTO, TicketDTO transferDTO, TicketTransactionDTO previousTicketTransaction, List<UserTransactionDTO> previousUserTransactions, StringBuilder errors, Map<String, Boolean> map) {
		try {
			BookingDTO bookingDTO = new BookingDTO();
			bookingDTO.setCode(transferDTO.getCode());

			/** save ticket, and it's transaction */
			saveTicket(authDTO, bookingDTO, repoTicketDTO, transferDTO, previousTicketTransaction, previousUserTransactions);

			/** Notifications */
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.CONFIRM_BOOKING) && transferDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
				smsService.sendMTicketTransferSMS(authDTO, transferDTO);
			}
			/** Push Notification */
			if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.RESCHEDULE)) {
				notificationPushService.pushTicketRescheduleNotification(authDTO, transferDTO, map);
			}
			/** Upload to backup server */
			drService.flushTicketDetails(authDTO, transferDTO);

			tripService.clearBookedBlockedSeatsCache(authDTO, repoTicketDTO.getTripDTO());
			tripService.clearBookedBlockedSeatsCache(authDTO, transferDTO.getTripDTO());
		}
		catch (Exception e) {
			errors.append(repoTicketDTO.getCode()).append(" ").append(e.getMessage()).append("|");
			e.printStackTrace();
			LOGGER.error("{} {}", repoTicketDTO.getCode(), e);
		}
	}

	private void validateTicketRescheduleMaxCount(AuthDTO authDTO, TicketDTO ticketDTO) {
		List<TicketDTO> tickets = ticketService.findTicketByLookupIdV2(authDTO, ticketDTO);
		if (tickets.size() >= authDTO.getNamespace().getProfile().getTicketRescheduleMaxCount()) {
			throw new ServiceException(ErrorCode.BOOK_TICKET_NOT_ALLOW_LIMITS, "Reschedule Ticket can't reschedule Again");
		}
	}

	private void calculatePaymentGatewayCharge(AuthDTO authDTO, BookingDTO bookingDTO, TicketDTO ticketDTO) {
		try {
			OrderInitRequestDTO orderInitRequest = new OrderInitRequestDTO();
			orderInitRequest.setPartnerCode(bookingDTO.getPaymentGatewayPartnerCode());
			orderInitRequest.setOrderType(OrderTypeEM.TICKET);

			PaymentMerchantGatewayScheduleDAO gatewayScheduleDAO = new PaymentMerchantGatewayScheduleDAO();
			PaymentGatewayScheduleDTO gatewayScheduleDTO = gatewayScheduleDAO.getPaymentGatewayForNamespace(authDTO, orderInitRequest);
			if (gatewayScheduleDTO.getServiceCharge().compareTo(BigDecimal.ZERO) > 0) {
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
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public JSONArray validateTicketAutoTransfer(AuthDTO authDTO, TripDTO trip, TripDTO transferTrip) {
		JSONArray response = new JSONArray();
		try {
			trip = busmapService.getSearchBusmapV3(authDTO, trip);
			if (trip == null || trip.getBus() == null || trip.getBus().getBusSeatLayoutDTO() == null) {
				SearchDTO searchDTO = trip.getSearch();
				throw new ServiceException(ErrorCode.INVALID_TRIP_CODE, searchDTO.getFromStation().getCode() + "-" + searchDTO.getToStation().getCode() + "-" + searchDTO.getTravelDate());
			}

			transferTrip = busmapService.getSearchBusmapV3(authDTO, transferTrip);
			if (transferTrip == null || transferTrip.getBus() == null || transferTrip.getBus().getBusSeatLayoutDTO() == null) {
				SearchDTO searchDTO = transferTrip.getSearch();
				throw new ServiceException(ErrorCode.INVALID_TRIP_CODE, searchDTO.getFromStation().getCode() + "-" + searchDTO.getToStation().getCode() + "-" + searchDTO.getTravelDate());
			}

			/** Adjacent seat gender validation */
			Map<String, String> seatNameMap = new HashMap<>();
			Map<String, BusSeatLayoutDTO> seatStatusMap = new HashMap<>();
			Map<String, BusSeatLayoutDTO> seatGenderMap = new HashMap<>();
			List<BusSeatLayoutDTO> transferSeatList = transferTrip.getSchedule().getScheduleBus().getBus().getBusSeatLayoutDTO().getList();
			for (BusSeatLayoutDTO dto : transferSeatList) {
				seatNameMap.put(dto.getName(), dto.getLayer() + "_" + dto.getRowPos() + "_" + dto.getColPos());
				seatStatusMap.put(dto.getName(), dto);
				if (dto.getSeatStatus() != null) {
					seatGenderMap.put(dto.getLayer() + "_" + dto.getRowPos() + "_" + dto.getColPos(), dto);
				}
			}

			List<BusSeatLayoutDTO> seatList = trip.getSchedule().getScheduleBus().getBus().getBusSeatLayoutDTO().getList();
			for (BusSeatLayoutDTO dto : seatList) {
				String seatPos = seatNameMap.get(dto.getName());
				BusSeatLayoutDTO busSeatLayoutDTO = seatStatusMap.get(dto.getName());
				if (dto.getSeatGendar() == null || busSeatLayoutDTO == null || dto.getBusSeatType().getId() != busSeatLayoutDTO.getBusSeatType().getId()) {
					continue;
				}
				JSONObject seatJson = new JSONObject();
				seatJson.put("seatCode", dto.getCode());
				seatJson.put("seatName", dto.getName());
				seatJson.put("transferSeatCode", busSeatLayoutDTO.getCode());
				seatJson.put("transferSeatName", dto.getName());
				seatJson.put("seatStatus", busSeatLayoutDTO.getSeatStatus() != null ? busSeatLayoutDTO.getSeatStatus().getCode() : SeatStatusEM.AVAILABLE_ALL.getCode());

				if (seatPos != null) {
					int layer = Integer.parseInt(seatPos.split("_")[0]);
					int rowCount = Integer.parseInt(seatPos.split("_")[1]);
					int colCount = Integer.parseInt(seatPos.split("_")[2]);

					// Female Seat validations
					if (dto.getSeatGendar().getId() == SeatGendarEM.FEMALE.getId()) {
						// Right Side seat
						if (seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null) {
							BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
							if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId())) {
								seatJson.put("seatStatus", SeatStatusEM.AVAILABLE_MALE.getCode());
							}
						}
						// Left Side seat
						else if (seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
							BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
							if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId())) {
								seatJson.put("seatStatus", SeatStatusEM.AVAILABLE_MALE.getCode());
							}
						}
					}
					// Male Seat validations
					else if (dto.getSeatGendar().getId() == SeatGendarEM.MALE.getId()) {
						// Right Side seat
						if (seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null) {
							BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
							if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId())) {
								seatJson.put("seatStatus", SeatStatusEM.AVAILABLE_FEMALE.getCode());
							}
						}
						// Left Side seat
						else if (seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
							BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
							if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId())) {
								seatJson.put("seatStatus", SeatStatusEM.AVAILABLE_FEMALE.getCode());
							}
						}
					}
				}
				response.add(seatJson);
			}

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}
}
