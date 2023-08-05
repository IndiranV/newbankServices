package org.in.com.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.SQLTransactionRollbackException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.aggregator.bits.BitsService;
import org.in.com.aggregator.mail.EmailService;
import org.in.com.aggregator.sms.SMSService;
import org.in.com.cache.BusCache;
import org.in.com.cache.CancellationTermsCache;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.TicketCache;
import org.in.com.cache.TicketHelperCache;
import org.in.com.cache.TripCache;
import org.in.com.cache.redis.RedisTripCacheService;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.CommissionDAO;
import org.in.com.dao.ConnectDAO;
import org.in.com.dao.StationDAO;
import org.in.com.dao.TicketDAO;
import org.in.com.dao.TicketEditDAO;
import org.in.com.dao.TicketTaxDAO;
import org.in.com.dao.TicketTransactionDAO;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.CommissionDTO;
import org.in.com.dto.ExtraCommissionDTO;
import org.in.com.dto.ExtraCommissionSlabDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.NamespaceTabletSettingsDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.RouteDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TicketPhoneBookCancelControlDTO;
import org.in.com.dto.TicketRefundDTO;
import org.in.com.dto.TicketTaxDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripInfoDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AuthenticationTypeEM;
import org.in.com.dto.enumeration.CommissionTypeEM;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.EventNotificationEM;
import org.in.com.dto.enumeration.FareTypeEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.NotificationMediumEM;
import org.in.com.dto.enumeration.SlabCalenderModeEM;
import org.in.com.dto.enumeration.SlabCalenderTypeEM;
import org.in.com.dto.enumeration.SlabModeEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.dto.enumeration.UserTagEM;
import org.in.com.dto.enumeration.WalletAccessEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusService;
import org.in.com.service.NamespaceTabletSettingsService;
import org.in.com.service.NamespaceTaxService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleStationPointService;
import org.in.com.service.ScheduleStationService;
import org.in.com.service.ScheduleTicketTransferTermsService;
import org.in.com.service.ScheduleTripService;
import org.in.com.service.SearchService;
import org.in.com.service.StationPointService;
import org.in.com.service.StationService;
import org.in.com.service.TicketPhoneBookControlService;
import org.in.com.service.TicketService;
import org.in.com.service.TicketTaxService;
import org.in.com.service.TripService;
import org.in.com.service.UserService;
import org.in.com.service.UserWalletService;
import org.in.com.utils.BitsEnDecrypt;
import org.in.com.utils.BitsShortURL;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.EmailUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import hirondelle.date4j.DateTime;
import lombok.Cleanup;
import net.sf.ehcache.Element;
import net.sf.json.JSONObject;

@Service
public class TicketImpl extends BaseImpl implements TicketService {
	private static final Logger logger = LoggerFactory.getLogger(TicketImpl.class);

	@Autowired
	CancelTicketImpl cancelTicketImpl;
	@Autowired
	TripService tripService;
	@Autowired
	SMSService smsService;
	@Autowired
	EmailService emailService;
	@Autowired
	NamespaceTaxService taxService;
	@Autowired
	ScheduleTripService scheduleTripService;
	@Autowired
	TicketTaxService ticketTaxService;
	@Autowired
	UserWalletService userWalletService;
	@Autowired
	BitsService bitsService;
	@Autowired
	ScheduleService scheduleService;
	@Autowired
	ScheduleStationService scheduleStationService;
	@Autowired
	StationService stationService;
	@Autowired
	UserService userService;
	@Autowired
	TicketPhoneBookControlService phoneBookControlService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	ScheduleStationPointService scheduleStationPointService;
	@Autowired
	SearchService searchService;
	@Autowired
	private RedisTripCacheService cacheService;
	@Autowired
	ScheduleTicketTransferTermsService scheduleTicketTransferTermsService;
	@Autowired
	NamespaceTabletSettingsService namespaceTabletSettingsService;
	@Autowired
	StationPointService stationPointService;
	@Autowired
	BusService busService;
	@Lazy
	@Autowired
	NotificationService notificationService;

	public TicketDTO showTicket(AuthDTO authDTO, TicketDTO ticketDTO) {

		try {
			if (StringUtil.isNull(ticketDTO.getCode()) && StringUtil.isNotNull(ticketDTO.getPassengerMobile())) {
				ticketDTO = showTicketV2(authDTO, ticketDTO);
			}
			if (StringUtil.isNotNull(ticketDTO.getCode()) && ticketDTO.getCode().length() > 20) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			TicketDAO dao = new TicketDAO();
			// ticketDTO.setBookingCode(ticketDTO.getCode().substring(0,
			// 3).equals("BTC") ? ticketDTO.getCode() : "");
			dao.showTicket(authDTO, ticketDTO);
			CancellationTermsCache termsCache = new CancellationTermsCache();
			BusCache busCache = new BusCache();

			ticketDTO.setFromStation(stationService.getStation(ticketDTO.getFromStation()));
			ticketDTO.setToStation(stationService.getStation(ticketDTO.getToStation()));
			tripService.getTrip(authDTO, ticketDTO.getTripDTO());
			ticketDTO.setCancellationTerm(termsCache.getCancellationTermByGroupKey(authDTO, ticketDTO.getCancellationTerm()));

			DateTime travelDateTime = null;
			if (authDTO.getNamespace().getProfile().getCancellationTimeType().equals(Constants.STAGE)) {
				travelDateTime = ticketDTO.getTripDateTime();
			}
			else {
				int firstStageStationMinutes = ticketDTO.getTripDTO().getTripMinutes();
				travelDateTime = DateUtil.addMinituesToDate(ticketDTO.getTripDTO().getTripDate(), firstStageStationMinutes);
			}

			ticketDTO.setCancellationTerm(cancelTicketImpl.getCancellationPolicyConvention(authDTO, ticketDTO.getTicketUser(), ticketDTO.getCancellationTerm(), ticketDTO.getFromStation().getState(), travelDateTime, ticketDTO.getSeatFareUniqueList()));
			ticketDTO.setTicketUser(userService.getUser(authDTO, ticketDTO.getTicketUser()));
			/** Apply GST Exception to OTA, Validate User Tag */
			if (BitsUtil.isTagExists(ticketDTO.getTicketUser().getUserTags(), Constants.GST_EXCEPTION_TAG) && ticketDTO.getTicketAt().compareTo(Constants.CGST_EFFECTIVE_DATE) > 0) {
				ticketDTO.setTax(new NamespaceTaxDTO());
			}
			else {
				ticketDTO.setTax(taxService.getTaxbyId(authDTO, ticketDTO.getTax()));
			}
			TicketExtraDTO ticketExtraDTO = dao.getTicketExtra(authDTO, ticketDTO.getId());
			if (ticketExtraDTO != null && ticketExtraDTO.getBlockReleaseMinutes() != 0 && ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
				ticketExtraDTO.setReleaseAt(BitsUtil.getBlockReleaseDateTime(ticketExtraDTO.getBlockReleaseMinutes(), ticketDTO.getTripDTO().getTripDateTimeV2(), ticketDTO.getTicketAt()));
			}
			ticketDTO.setTicketExtra(ticketExtraDTO);

			if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
				ScheduleDTO scheduleDTO = ticketDTO.getTripDTO().getSchedule();
				TicketPhoneBookCancelControlDTO phoneBookControl = phoneBookControlService.getActivePhoneBookCancelControl(authDTO, scheduleDTO, ticketDTO);
				ticketDTO.setCancellationTerm(phoneBookControlService.getCancellationPolicyConvention(authDTO, phoneBookControl, ticketDTO));
			}

			ticketDTO.getTripDTO().setBus(busCache.getBusDTObyId(authDTO, ticketDTO.getTripDTO().getBus()));
			// Tentative mobile number masking
			applyMobileNumberMasking(authDTO, ticketDTO);
			applyScheduleStationPointContact(authDTO, ticketDTO);

			if ((ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) && (ticketDTO.getJourneyType().getId() == JourneyTypeEM.ONWARD_TRIP.getId() || ticketDTO.getJourneyType().getId() == JourneyTypeEM.RETURN_TRIP.getId())) {
				ticketDTO.setScheduleTicketTransferTerms(scheduleTicketTransferTermsService.getScheduleTicketTransferTermsByTicket(authDTO, ticketDTO));
				if (ticketDTO.getScheduleTicketTransferTerms() != null && ticketDTO.getScheduleTicketTransferTerms().getId() != 0) {
					ticketDTO.getScheduleTicketTransferTerms().setDateTime(BitsUtil.getTicketTransferTermsDateTime(authDTO, ticketDTO.getScheduleTicketTransferTerms(), ticketDTO.getTripDateTime(), ticketDTO.getTripDTO().getTripDateTimeV2()));
				}
			}
		}
		catch (ServiceException e) {
			System.out.println("Print Ticket: " + authDTO.getNamespaceCode() + " - " + authDTO.getUser().getName() + " - " + ticketDTO.getCode() + " - " + authDTO.getAuthToken() + e.getErrorCode().toString());
			throw e;
		}
		catch (Exception e) {
			System.out.println(authDTO.getNamespaceCode() + " - " + authDTO.getUser().getName() + " - " + ticketDTO.getCode() + " - " + authDTO.getAuthToken());
			e.printStackTrace();
		}
		return ticketDTO;
	}

	public TicketDTO showTicketV2(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO ticketDAO = new TicketDAO();
		List<TicketDTO> ticketList = ticketDAO.getTicketByMobile(authDTO, ticketDTO.getPassengerMobile());
		if (ticketList.isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		for (TicketDTO ticket : ticketList) {
			getTicketStatus(authDTO, ticket);
			tripService.getTripDTO(authDTO, ticket.getTripDTO());
		}

		// Sort
		Comparator<TicketDTO> comp = new BeanComparator("tripDate");
		Collections.sort(ticketList, comp);

		TicketDTO ticketRepo = null;
		DateTime now = DateUtil.NOW();
		if (ticketList.size() > Numeric.ONE_INT) {
			for (TicketDTO ticket : ticketList) {

				// After Travel DateTime
				if (ticketRepo == null && now.compareTo(ticket.getTripDateTime()) == 1 && now.getHour() <= 12 && DateUtil.getMinutiesDifferent(ticket.getTripDateTime(), now) <= 480) {
					ticketRepo = ticket;
					continue;
				}
				else if (ticketRepo == null && now.compareTo(ticket.getTripDateTime()) == 1 && now.getHour() > 12 && DateUtil.getMinutiesDifferent(ticket.getTripDateTime(), now) <= 180) {
					ticketRepo = ticket;
					continue;
				}
				// Before Travel DateTime
				if (ticketRepo == null && ticket.getTripDateTime().compareTo(now) == 1) {
					ticketRepo = ticket;
					continue;
				}
				// Before Travel DateTime & Recent Ticket
				if (ticketRepo != null && ticket.getTripDateTime().compareTo(now) == 1 && DateUtil.getMinutiesDifferent(now, ticket.getTripDateTime()) <= DateUtil.getMinutiesDifferent(now, ticketRepo.getTripDateTime())) {
					ticketRepo = ticket;
				}
			}
		}
		else {
			ticketRepo = ticketList.get(Numeric.ZERO_INT);
		}
		if (ticketRepo == null) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE, ticketList.size() > 0 ? "Morethan a Ticket found the mobile number, please give PNR" : "");
		}
		ticketDTO.setCode(ticketRepo.getCode());
		return ticketDTO;
	}

	public TicketDTO notifyTicket(AuthDTO authDTO, TicketDTO ticketDTO) {
		String mobileNumber = ticketDTO.getPassengerMobile();
		if (EhcacheManager.getFreshRequestEhCache().get(authDTO.getUserCode() + ticketDTO.getCode()) == null) {
			showTicket(authDTO, ticketDTO);
			if (StringUtil.isNotNull(mobileNumber)) {
				ticketDTO.setPassengerMobile(mobileNumber);
			}
			if (ticketDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
				throw new ServiceException(ErrorCode.NOT_CONFIRM_BOOKED_TICKET);
			}
			if (DateUtil.getDayDifferent(ticketDTO.getTripDate(), DateUtil.getDateFromDateTime(DateUtil.NOW())) > 0) {
				throw new ServiceException(ErrorCode.TRIP_DATE_OVER);
			}
			notificationService.sendTicketBookingNotification(authDTO, ticketDTO);
			/** send Ticket Event To Customer */
			notificationService.sendCustomerTicketEvent(authDTO, ticketDTO, EventNotificationEM.TICKET_BOOKING);

			if (ticketDTO.getTicketUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
				emailService.sendBookingEmail(authDTO, ticketDTO);
			}
			logger.info("Sms send,The ticket id is: " + ticketDTO.getId());
			Element element = new Element(authDTO.getUserCode() + ticketDTO.getCode(), ticketDTO.getCode());
			EhcacheManager.getFreshRequestEhCache().put(element);
		}
		return ticketDTO;
	}

	public void notifyTicketV2(AuthDTO authDTO, TicketDTO ticketDTO, String emailType) {
		String emailId = ticketDTO.getPassengerEmailId();
		String key = NotificationMediumEM.E_MAIL.getCode() + authDTO.getUserCode() + ticketDTO.getCode();

		// Check Fresh
		BitsUtil.checkTransactionValidityCache(key);

		ticketDTO.setPassengerEmailId(null);
		showTicket(authDTO, ticketDTO);

		if (StringUtil.isNotNull(emailId)) {
			ticketDTO.setPassengerEmailId(emailId);
		}
		// Validate Email
		if (!EmailUtil.isValid(ticketDTO.getPassengerEmailId())) {
			throw new ServiceException(ErrorCode.INVALID_EMAIL_DOMAIN);
		}
		if (ticketDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
			throw new ServiceException(ErrorCode.NOT_CONFIRM_BOOKED_TICKET);
		}
		if (DateUtil.getDayDifferent(ticketDTO.getTripDate(), DateUtil.getDateFromDateTime(DateUtil.NOW())) > 0) {
			throw new ServiceException(ErrorCode.TRIP_DATE_OVER);
		}

		if (emailType.equals(NotificationMediumEM.E_MAIL.getCode())) {
			emailService.sendBookingEmail(authDTO, ticketDTO);
		}
		else if (emailType.equals("INVOICE") && ticketDTO.getTax() != null) {
			TicketTaxDAO ticketTaxDAO = new TicketTaxDAO();
			TicketTaxDTO ticketTaxDTO = ticketTaxDAO.getTicketTax(authDTO, ticketDTO);
			emailService.sendTaxInvoiceEmail(authDTO, ticketDTO, ticketTaxDTO);
		}
		else if (emailType.equals("MAILINVOICE")) {
			emailService.sendBookingEmail(authDTO, ticketDTO);

			TicketTaxDAO ticketTaxDAO = new TicketTaxDAO();
			TicketTaxDTO ticketTaxDTO = ticketTaxDAO.getTicketTax(authDTO, ticketDTO);
			if (ticketDTO.getTax() != null) {
				emailService.sendTaxInvoiceEmail(authDTO, ticketDTO, ticketTaxDTO);
			}
		}
		logger.info("Email send, The ticket id is: " + ticketDTO.getId());
	}

	public TicketDTO getTicketStatus(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			TicketDAO dao = new TicketDAO();
			dao.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			ticketDTO.setTicketUser(userService.getUser(authDTO, ticketDTO.getTicketUser()));
			ticketDTO.setFromStation(stationService.getStation(ticketDTO.getFromStation()));
			ticketDTO.setToStation(stationService.getStation(ticketDTO.getToStation()));
			ticketDTO.setBoardingPoint(getStationPointDTObyId(authDTO, ticketDTO.getBoardingPoint()));
			ticketDTO.setDroppingPoint(getStationPointDTObyId(authDTO, ticketDTO.getDroppingPoint()));
			CancellationTermsCache termsCache = new CancellationTermsCache();
			ticketDTO.setCancellationTerm(termsCache.getCancellationTermByGroupKey(authDTO, ticketDTO.getCancellationTerm()));

			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(ticketDTO.getTripDate());
			searchDTO.setFromStation(ticketDTO.getFromStation());
			searchDTO.setToStation(ticketDTO.getToStation());
			ticketDTO.getTripDTO().setSearch(searchDTO);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error("Error in retrieving the ticket status: " + authDTO.getNamespaceCode() + "-" + ticketDTO.getCode() + "-" + e.getMessage());
		}
		return ticketDTO;
	}

	public TicketDTO getTicketStatus(AuthDTO authDTO, String ticketCode) {
		TicketDAO dao = new TicketDAO();
		TicketDTO ticketDTO = dao.getTicketStatusV2(authDTO, ticketCode);
		if (ticketDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		ticketDTO.setBoardingPoint(getStationPointDTObyId(authDTO, ticketDTO.getBoardingPoint()));
		ticketDTO.setDroppingPoint(getStationPointDTObyId(authDTO, ticketDTO.getDroppingPoint()));
		return ticketDTO;
	}

	public void UpdateTicketStatus(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO dao = new TicketDAO();
		dao.UpdateTicketStatus(connection, authDTO, ticketDTO);
	}

	public void updateTicketStatusV2(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO dao = new TicketDAO();
		dao.updateTicketStatusV2(connection, authDTO, ticketDTO);
	}

	public void UpdateTicketDetailsStatus(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO, String auditEvent) {
		TicketDAO dao = new TicketDAO();
		dao.UpdateTicketDetailsStatus(connection, authDTO, ticketDTO, auditEvent);
	}

	public List<TicketDTO> findTicket(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO dao = new TicketDAO();
		UserDTO userDTO = new UserDTO();
		userDTO.setId(authDTO.getUser().getId());
		// Permission check
		List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
		Eventlist.add(MenuEventEM.BOOKING_FIND_EDIT_ALL_TCK);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, Eventlist);
		if (menuEventDTO != null && menuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
			userDTO.setId(Numeric.ZERO_INT);
		}

		List<TicketDTO> ticketList = dao.findTicket(authDTO, ticketDTO, userDTO);
		for (Iterator<TicketDTO> itrTicket = ticketList.iterator(); itrTicket.hasNext();) {
			TicketDTO ticket = itrTicket.next();
			// Masked mobile number ticket should not expose
			if (StringUtil.isMaskedMobileNumber(ticket.getPassengerMobile())) {
				itrTicket.remove();
				continue;
			}
			ticket.setFromStation(stationService.getStation(ticket.getFromStation()));
			ticket.setToStation(stationService.getStation(ticket.getToStation()));
			ticket.setTicketUser(userService.getUser(authDTO, ticket.getTicketUser()));

			applyMobileNumberMasking(authDTO, ticket);

		}
		// Sorting
		Comparator<TicketDTO> comp = new BeanComparator("ticketAt");
		Collections.sort(ticketList, comp);
		Collections.reverse(ticketList);

		return ticketList;
	}

	public TicketDTO getAutoPassengerDetails(AuthDTO authDTO, String mobileNumber, int seatCount) {
		TicketDAO dao = new TicketDAO();
		TicketDTO ticket = dao.getAutoPassengerDetails(authDTO, mobileNumber, seatCount);
		String blockedMailId = ",noemail@redbus.com,viabookings@abhibus.net,";
		if (StringUtil.isNotNull(ticket.getPassengerEmailId()) && blockedMailId.contains(ticket.getPassengerEmailId())) {
			ticket.setPassengerEmailId(Text.EMPTY);
		}
		return ticket;
	}

	public void getTicketTransaction(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketTransactionDAO dao = new TicketTransactionDAO();
		dao.getTicketTransaction(authDTO, ticketDTO);
	}

	public void getTicketCancelTransactionDetails(AuthDTO authDTO, TicketTransactionDTO transactionDTO, UserDTO userDTO) {
		TicketTransactionDAO dao = new TicketTransactionDAO();
		dao.getTicketCancelTransactionDetails(authDTO, transactionDTO, userDTO);
	}

	public List<TicketDTO> getPhoneBookingTickets(AuthDTO authDTO, DateTime fromDate, DateTime toDate) {
		TicketDAO dao = new TicketDAO();
		UserDTO userDTO = new UserDTO();
		userDTO.setId(authDTO.getUser().getId());
		// Permission check
		List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
		Eventlist.add(MenuEventEM.BOOKING_PHONE_ALL_USER_TCK);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, Eventlist);
		if (menuEventDTO != null && menuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
			userDTO.setId(Numeric.ZERO_INT);
		}
		List<TicketDTO> phoneBookingTickets = dao.getPhoneBookingTickets(authDTO, fromDate, toDate, userDTO);
		ScheduleCache scheduleCache = new ScheduleCache();
		Map<String, TripDTO> tripMap = new HashMap<String, TripDTO>();
		for (TicketDTO dto : phoneBookingTickets) {
			dto.setFromStation(stationService.getStation(dto.getFromStation()));
			dto.setToStation(stationService.getStation(dto.getToStation()));
			dto.setBoardingPoint(getStationPointDTObyId(authDTO, dto.getBoardingPoint()));
			dto.setTicketUser(userService.getUser(authDTO, dto.getTicketUser()));
			if (tripMap.get(dto.getTripDTO().getCode()) == null) {
				TripDTO tripDTO = tripService.getTripDTO(authDTO, dto.getTripDTO());
				tripDTO.setSchedule(scheduleCache.getScheduleDTObyId(authDTO, tripDTO.getSchedule()));
				tripDTO.getSchedule().setStationList(scheduleStationService.getScheduleStation(authDTO, tripDTO.getSchedule()));
				tripMap.put(dto.getTripDTO().getCode(), tripDTO);
			}
			dto.setTripDTO(tripMap.get(dto.getTripDTO().getCode()));

			TicketExtraDTO ticketExtraDTO = dao.getTicketExtra(authDTO, dto.getId());
			if (ticketExtraDTO != null) {
				ticketExtraDTO.setReleaseAt(BitsUtil.getBlockReleaseDateTime(ticketExtraDTO.getBlockReleaseMinutes(), dto.getTripDTO().getTripDateTimeV2(), dto.getTicketAt()));
				dto.setTicketExtra(ticketExtraDTO);
			}
		}
		return phoneBookingTickets;
	}

	public void saveTicketTransaction(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketTransactionDAO ticketDAO = new TicketTransactionDAO();
		ticketDAO.insertTicketTransaction(connection, authDTO, ticketDTO);
	}

	@Override
	public void saveTicketAddons(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			TicketDTO repoTicket = new TicketDTO();
			repoTicket.setCode(ticketDTO.getCode());
			repoTicket = getTicketStatus(authDTO, repoTicket);
			if (repoTicket.getId() == 0 || repoTicket.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}

			TripDTO tripDTO = tripService.getTrip(authDTO, repoTicket.getTripDTO());
			List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, tripDTO);
			DateTime originDateTime = BitsUtil.getDestinationStationTime(stageList, tripDTO.getTripDate());
			if (DateUtil.NOW().gt(originDateTime)) {
				throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
			}

			BigDecimal transactionAmount = BigDecimal.ZERO;
			for (TicketAddonsDetailsDTO addonsDetailsDTO : ticketDTO.getTicketAddonsDetails()) {
				transactionAmount = transactionAmount.add(addonsDetailsDTO.getValue());
				addonsDetailsDTO.setTicketStatus(repoTicket.getTicketStatus());
				addonsDetailsDTO.setRefferenceId(repoTicket.getId());
			}
			if (transactionAmount.compareTo(BigDecimal.ZERO) <= 0) {
				throw new ServiceException(ErrorCode.TRANSACTION_AMOUNT_INVALID);
			}

			TicketTransactionDTO ticketTransactionDTO = new TicketTransactionDTO();
			ticketTransactionDTO.setTransactionAmount(transactionAmount);
			ticketTransactionDTO.setAcBusTax(BigDecimal.ZERO);
			ticketTransactionDTO.setTdsTax(BigDecimal.ZERO);
			ticketTransactionDTO.setCommissionAmount(BigDecimal.ZERO);
			ticketTransactionDTO.setExtraCommissionAmount(BigDecimal.ZERO);
			ticketTransactionDTO.setAddonsAmount(BigDecimal.ZERO);
			ticketTransactionDTO.setTransactionMode(TransactionModeEM.PAYMENT_CASH);
			ticketTransactionDTO.setTransactionType(TransactionTypeEM.OTHER_CHARGES);

			repoTicket.setTicketXaction(ticketTransactionDTO);
			repoTicket.setTicketAddonsDetails(ticketDTO.getTicketAddonsDetails());

			saveTicket(authDTO, repoTicket);
		}
		catch (ServiceException e) {
			e.printStackTrace();
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new ServiceException(ErrorCode.UPDATE_FAIL);
		}
	}

	public List<TicketAddonsDetailsDTO> getTicketAddonsDetails(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO dao = new TicketDAO();
		return dao.getTicketAddonsDetails(authDTO, ticketDTO);
	}

	private void saveTicket(AuthDTO authDTO, TicketDTO ticketDTO) throws Exception {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);
				TicketDAO dao = new TicketDAO();

				// Insert Ticket Addons Details table
				dao.insertTicketAddonsDetails(authDTO, ticketDTO, connection);

				saveTicketTransaction(connection, authDTO, ticketDTO);

			}
			catch (SQLTransactionRollbackException e) {
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

	public void saveTicketCancelTransaction(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketTransactionDAO ticketDAO = new TicketTransactionDAO();
		ticketDAO.insertTicketCancelTransaction(connection, authDTO, ticketDTO);
	}

	public void saveTicketCancellationDetails(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketTransactionDAO ticketDAO = new TicketTransactionDAO();
		ticketDAO.insertTicketCancellationDetails(connection, authDTO, ticketDTO);
	}

	public boolean checkSeatDuplicateEntry(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO ticketDAO = new TicketDAO();
		return ticketDAO.checkSeatDuplicateEntry(authDTO, ticketDTO);
	}

	public boolean checkSeatDuplicateEntryV2(AuthDTO authDTO, TicketDTO ticketDTO) {
		boolean status = false;

		try {
			TicketDAO ticketDAO = new TicketDAO();
			List<TicketDetailsDTO> ticketDetailsList = ticketDAO.checkSeatDuplicateEntryV2(authDTO, ticketDTO);

			for (TicketDetailsDTO detailsDTO : ticketDetailsList) {
				int ticketId = detailsDTO.getTicketId();

				if (detailsDTO.getTicketStatus() == null) {
					status = true;
					break;
				}
				else if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() && detailsDTO.getTravelStatus().getId() != TravelStatusEM.NOT_TRAVELED.getId()) {
					status = true;
					break;
				}
				else if (ticketId != ticketDTO.getId() && detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && detailsDTO.getTravelStatus().getId() != TravelStatusEM.NOT_TRAVELED.getId()) {
					TicketExtraDTO ticketExtraDTO = ticketDAO.getTicketExtra(authDTO, ticketId);

					if (ticketExtraDTO != null && !BitsUtil.validateBlockReleaseTime(ticketExtraDTO.getBlockReleaseMinutes(), ticketDTO.getTripDTO().getTripDateTimeV2(), detailsDTO.getUpdatedAt())) {
						status = true;
						break;
					}
				}
				else if (ticketId != ticketDTO.getId() && detailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(detailsDTO.getUpdatedAt(), DateUtil.NOW()) < authDTO.getNamespace().getProfile().getSeatBlockTime()) {
					status = true;
					break;
				}
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			status = true;
		}
		return status;
	}

	public boolean checkDuplicateTicketCodeEntry(AuthDTO authDTO, String ticketCode) {
		if (StringUtil.isNull(ticketCode)) {
			throw new ServiceException(ErrorCode.UNABLE_TO_BLOCK_TICKET);
		}
		TicketDAO ticketDAO = new TicketDAO();
		return ticketDAO.checkDuplicateTicketCodeEntry(authDTO, ticketCode);
	}

	public TicketRefundDTO getRefundTicket(AuthDTO authDTO, TicketRefundDTO ticketRefundDTO) {
		TicketDAO ticketDAO = new TicketDAO();
		ticketRefundDTO = ticketDAO.getRefundTicket(authDTO, ticketRefundDTO);
		for (TicketRefundDTO refund : ticketRefundDTO.getList()) {
			refund.setFromStation(stationService.getStation(refund.getFromStation()));
			refund.setToStation(stationService.getStation(refund.getToStation()));
		}
		return ticketRefundDTO;
	}

	public void updateRefundTicket(AuthDTO authDTO, TicketRefundDTO ticketRefundDTO) {
		TicketDAO ticketDAO = new TicketDAO();
		ticketDAO.updateRefundTicketStatus(authDTO, ticketRefundDTO);
	}

	public List<TicketDTO> getTicketStatusByBookingCode(AuthDTO authDTO, String bookingCode) {
		List<TicketDTO> ticketList = new ArrayList<TicketDTO>();
		try {
			TicketDAO dao = new TicketDAO();
			ticketList = dao.getTicketStatusByBookingCode(authDTO, bookingCode);

			if (ticketList == null || ticketList.isEmpty()) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			for (TicketDTO ticketDTO : ticketList) {
				ticketDTO.setTicketUser(userService.getUser(authDTO, ticketDTO.getTicketUser()));
				ticketDTO.setFromStation(stationService.getStation(ticketDTO.getFromStation()));
				ticketDTO.setToStation(stationService.getStation(ticketDTO.getToStation()));
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error("Error in retrieving the ticket status: " + authDTO.getNamespaceCode() + "-" + bookingCode + "-" + e.getMessage());
		}
		return ticketList;
	}

	public void getTicketTripDetails(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO ticketDAO = new TicketDAO();
		ticketDAO.getTicketTripDetails(authDTO, ticketDTO);
		if (ticketDTO.getActiveFlag() != 1) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		// ticketDTO.setBoardingPoint(getStationPointDTObyId(authDTO,
		// ticketDTO.getBoardingPoint()));
		ticketDTO.setFromStation(stationService.getStation(ticketDTO.getFromStation()));
		ticketDTO.setToStation(stationService.getStation(ticketDTO.getToStation()));
	}

	public List<TicketDTO> findTicketbyMobileCouponHistory(AuthDTO authDTO, String passengerMobile, String coupon) {
		TicketDAO ticketDAO = new TicketDAO();
		return ticketDAO.findTicketbyMobileCouponHistory(authDTO, passengerMobile, coupon);
	}

	public void updateTicketLookup(AuthDTO authDTO, BookingDTO bookingDTO) {
		for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
			ticketDTO.setLookupId(bookingDTO.getLookupId(ticketDTO));
		}
		TicketDAO ticketDAO = new TicketDAO();
		ticketDAO.updateTicketLookup(authDTO, bookingDTO);
	}

	@Override
	public void updateTravelStatus(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO ticketDAO = new TicketDAO();

		TicketDTO repositoryTicket = getTicketStatus(authDTO, ticketDTO.getCode());
		ticketDTO.setId(repositoryTicket.getId());
		ticketDTO.setPassengerMobile(repositoryTicket.getPassengerMobile());
		ticketDTO.setTicketUser(repositoryTicket.getTicketUser());

		// Check all seat travel status are updated
		List<TicketDetailsDTO> seatList = new ArrayList<TicketDetailsDTO>();
		List<TicketDetailsDTO> newSeatList = new ArrayList<TicketDetailsDTO>();
		// to avoid multiple travel status update SMS
		boolean freshTravelStatusfound = true;
		// check boarded travel status
		boolean boardTravelStatusfound = false;

		TravelStatusEM travelStatus = Iterables.getFirst(ticketDTO.getTicketDetails(), null).getTravelStatus();
		NamespaceTabletSettingsDTO namespaceTabletSettingsDTO = namespaceTabletSettingsService.getNamespaceTabletSettings(authDTO);
		if (authDTO.getDeviceMedium().getId() == DeviceMediumEM.APP_TABLET_POB.getId() && namespaceTabletSettingsDTO.getForceReleaseFlag() == 1 && TravelStatusEM.NOT_BOARDED.getId() == travelStatus.getId()) {
			travelStatus = TravelStatusEM.NOT_TRAVELED;
		}
		for (TicketDetailsDTO repositoryTicketDetails : repositoryTicket.getTicketDetails()) {
			if (!ticketDTO.getSeatCodeList().contains(repositoryTicketDetails.getSeatCode())) {
				if (repositoryTicketDetails.getTravelStatus().getId() != TravelStatusEM.YET_BOARD.getId()) {
					seatList.add(repositoryTicketDetails);
				}
				if (!boardTravelStatusfound && repositoryTicketDetails.getTravelStatus().getId() == TravelStatusEM.BOARDED.getId()) {
					boardTravelStatusfound = true;
				}
			}
			for (TicketDetailsDTO ticketDetails : ticketDTO.getTicketDetails()) {
				ticketDetails.setTravelStatus(travelStatus);
				if (repositoryTicketDetails.getSeatCode().equals(ticketDetails.getSeatCode())) {
					ticketDetails.setTicketId(repositoryTicketDetails.getTicketId());
					ticketDetails.setId(repositoryTicketDetails.getId());
					ticketDetails.setPassengerName(repositoryTicketDetails.getPassengerName());
					ticketDetails.setSeatName(repositoryTicketDetails.getSeatName());
					ticketDetails.setTicketStatus(repositoryTicketDetails.getTicketStatus());
					newSeatList.add(ticketDetails);
					if (repositoryTicketDetails.getTravelStatus().getId() != TravelStatusEM.YET_BOARD.getId() && freshTravelStatusfound) {
						freshTravelStatusfound = false;
					}
					if (!boardTravelStatusfound && ticketDetails.getTravelStatus().getId() == TravelStatusEM.BOARDED.getId()) {
						boardTravelStatusfound = true;
					}
				}
			}
		}
		if (newSeatList.size() != ticketDTO.getTicketDetails().size()) {
			throw new ServiceException(ErrorCode.INVALID_SEAT_CODE, "some of Seat travels status already updated");
		}

		// Save Not Boarded Seats In Cache
		TripCache tripCache = new TripCache();
		ticketDTO.setTripDTO(tripCache.getTripDTO(authDTO, repositoryTicket.getTripDTO()));
		if (travelStatus.getId() == TravelStatusEM.NOT_BOARDED.getId() || travelStatus.getId() == TravelStatusEM.NOT_TRAVELED.getId()) {
			putTicketNotBoardedInCache(authDTO, ticketDTO, repositoryTicket);
			notificationPushService.pushTicketNotBoardedNotification(authDTO, ticketDTO);
		}
		else {
			TicketHelperCache ticketHelperCache = new TicketHelperCache();
			TicketDTO ticketCache = ticketHelperCache.getTicketNotBoarded(authDTO, ticketDTO.getCode());
			if (ticketCache != null) {
				Map<String, TicketDetailsDTO> ticketDetailsMap = new HashMap<String, TicketDetailsDTO>();
				for (TicketDetailsDTO ticketDetails : ticketDTO.getTicketDetails()) {
					ticketDetailsMap.put(ticketDetails.getSeatCode(), ticketDetails);
				}

				for (Iterator<TicketDetailsDTO> iterator = ticketCache.getTicketDetails().iterator(); iterator.hasNext();) {
					TicketDetailsDTO ticketDetailsDTO = iterator.next();
					if (ticketDetailsMap.get(ticketDetailsDTO.getSeatCode()) != null) {
						iterator.remove();
						continue;
					}
					// ticketDetailsDTO.setPassengerName(ticketDetails.getPassengerName());
				}

				if (ticketCache.getTicketDetails() == null || ticketCache.getTicketDetails().isEmpty()) {
					ticketHelperCache.removeTicketNotBoardedInCache(authDTO, ticketDTO.getCode());
				}
				else {
					ticketHelperCache.putTicketNotBoardedInCache(authDTO, ticketCache);
				}
			}
		}

		tripService.updateTripSeatDetailsWithExtras(authDTO, ticketDTO);
		ticketDAO.updateTravelStatus(authDTO, ticketDTO, travelStatus);

		if (StringUtil.isNotNull(ticketDTO.getRemarks())) {
			editTicketRemarks(authDTO, ticketDTO, repositoryTicket);
		}
		// Fire All Seats Boarded
		if (seatList.size() + newSeatList.size() == repositoryTicket.getTicketDetails().size() && freshTravelStatusfound && boardTravelStatusfound) {

			// Wallet Update
			if (WalletAccessEM.getWalletAccessEM(authDTO.getNamespaceCode()) != null && !StringUtil.isContains(Constants.TICKET_AFTER_TRAVEL_WALLET, authDTO.getNamespace().getCode())) {
				userWalletService.addAfterTravelTransaction(authDTO, repositoryTicket, Text.AFTER_TRAVEL);
			}

			// Fire SMS
			firebusbuddyAfterboard(authDTO, repositoryTicket);
		}
		// Push Ticket Event to API Callback
		if (travelStatus.getId() == TravelStatusEM.NOT_TRAVELED.getId()) {
			repositoryTicket.setFromStation(stationService.getStation(repositoryTicket.getFromStation()));
			repositoryTicket.setToStation(stationService.getStation(repositoryTicket.getToStation()));
			authDTO.getAdditionalAttribute().put("activity_type", "not-travel");
			pushInventoryChangesEvent(authDTO, repositoryTicket);
		}

	}

	private void putTicketNotBoardedInCache(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO repositoryTicket) {
		TicketHelperCache ticketHelperCache = new TicketHelperCache();
		TicketDTO ticketCache = ticketHelperCache.getTicketNotBoarded(authDTO, ticketDTO.getCode());
		if (ticketCache != null) {

			Map<String, TicketDetailsDTO> ticketDetailsCacheMap = new HashMap<String, TicketDetailsDTO>();
			for (TicketDetailsDTO ticketDetailsCache : ticketCache.getTicketDetails()) {
				ticketDetailsCacheMap.put(ticketDetailsCache.getSeatCode(), ticketDetailsCache);
			}

			List<TicketDetailsDTO> newSeatList = new ArrayList<TicketDetailsDTO>();
			for (TicketDetailsDTO ticketDetails : ticketDTO.getTicketDetails()) {
				if (ticketDetailsCacheMap.get(ticketDetails.getSeatCode()) != null) {
					TicketDetailsDTO ticketDetailsCache = ticketDetailsCacheMap.get(ticketDetails.getSeatCode());
					ticketDetailsCache.setTravelStatus(ticketDetails.getTravelStatus());
					ticketDetailsCacheMap.put(ticketDetailsCache.getSeatCode(), ticketDetailsCache);
				}
				else {
					ticketDetailsCacheMap.put(ticketDetails.getSeatCode(), ticketDetails);
					newSeatList.add(ticketDetails);
				}
			}
			if (!newSeatList.isEmpty()) {
				ticketCache.getTicketDetails().addAll(newSeatList);
			}
			List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>(ticketDetailsCacheMap.values());
			ticketCache.setTicketDetails(ticketDetails);
			ticketHelperCache.putTicketNotBoardedInCache(authDTO, ticketCache);
		}
		else {
			ticketDTO.setFromStation(repositoryTicket.getFromStation());
			ticketDTO.setToStation(repositoryTicket.getToStation());

			TripCache tripCache = new TripCache();
			TripDTO tripDTO = tripCache.getTripDTO(authDTO, repositoryTicket.getTripDTO());
			tripDTO.setBus(getBusDTObyId(authDTO, tripDTO.getBus()));
			ticketDTO.setTripDTO(tripCache.getTripDTO(authDTO, repositoryTicket.getTripDTO()));

			ticketDTO.setTicketAt(repositoryTicket.getTicketAt());
			ticketDTO.setDeviceMedium(repositoryTicket.getDeviceMedium());
			ticketDTO.setRemarks(repositoryTicket.getRemarks());
			ticketDTO.setServiceNo(repositoryTicket.getServiceNo());
			ticketDTO.setTicketUser(repositoryTicket.getTicketUser());
			ticketDTO.setTicketStatus(repositoryTicket.getTicketStatus());
			ticketDTO.setTripDate(repositoryTicket.getTripDate());
			ticketDTO.setTravelMinutes(repositoryTicket.getTravelMinutes());
			ticketDTO.setBoardingPoint(repositoryTicket.getBoardingPoint());
			ticketHelperCache.putTicketNotBoardedInCache(authDTO, ticketDTO);
		}

	}

	private void firebusbuddyAfterboard(AuthDTO authDTO, TicketDTO ticketDTO) {
		String vehicleNumber = Text.EMPTY;
		// Get Trip Info
		TripInfoDTO tripInfoDTO = tripService.getTripInfo(authDTO, ticketDTO.getTripDTO());
		if (tripInfoDTO != null && tripInfoDTO.getBusVehicle() != null) {
			vehicleNumber = tripInfoDTO.getBusVehicle().getRegistationNumber();
		}

		if (ticketDTO.getId() == 0 || ticketDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		smsService.sendBusbuddyAfterboard(authDTO, ticketDTO.getCode(), vehicleNumber, ticketDTO.getPassengerMobile());
	}

	@Override
	public void updateTicketRemarks(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO ticketDAO = new TicketDAO();
		ticketDAO.updateTicketRemarks(authDTO, ticketDTO);
	}

	@Override
	public void updatePhoneBookTicketPaymentStatus(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO ticketDAO = new TicketDAO();
		TicketDTO ticket = ticketDAO.getTicketStatusV2(authDTO, ticketDTO.getCode());

		if (ticket.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE, "This feature only for phone blocked tickets!");
		}

		ticketDTO.setId(ticket.getId());

		/** Updating Phone Block Ticket Payment Status */
		TicketExtraDTO ticketExtraDTO = ticketDAO.getTicketExtra(authDTO, ticketDTO.getId());
		if (ticketExtraDTO != null) {
			if (ticketExtraDTO.getPhoneBookPaymentStatus() > 0) {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE, "Payment status updated already!");
			}
			ticketDAO.updatePhoneBookTicketPaymentStatus(authDTO, ticketDTO);
		}
		else {
			ticketDAO.insertTicketExtrasV2(authDTO, ticketDTO);
		}

		ticketDTO.setRemarks(StringUtil.isNotNull(ticket.getRemarks()) ? ticket.getRemarks() + Text.SINGLE_SPACE + ticketDTO.getRemarks() : ticketDTO.getRemarks());

		/** Updating Ticket Remarks */
		ticketDAO.updateTicketRemarks(authDTO, ticketDTO);
	}

	@Override
	public List<TicketDTO> getTicketsForFeedback(AuthDTO authDTO, TicketDTO ticket) {
		TicketDAO ticketDAO = new TicketDAO();
		return ticketDAO.getTicketsForFeedback(authDTO, ticket);
	}

	@Override
	public void addAfterTravelCoupon(AuthDTO authDTO, List<String> ticketList) {
		if (WalletAccessEM.getWalletAccessEM(authDTO.getNamespaceCode()) != null && !StringUtil.isContains(Constants.TICKET_AFTER_TRAVEL_WALLET, authDTO.getNamespace().getCode())) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}

		for (String ticketCode : ticketList) {
			TicketDTO repositoryTicket = new TicketDTO();
			repositoryTicket.setCode(ticketCode);
			try {
				repositoryTicket = getTicketStatus(authDTO, repositoryTicket.getCode());
				// Wallet Update
				userWalletService.addAfterTravelTransaction(authDTO, repositoryTicket, Text.AFTER_TRAVEL);
			}
			catch (Exception e) {
				System.out.println("ERR01 AFTER_TRAVEL " + repositoryTicket.getCode() + Text.SINGLE_SPACE + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	@Override
	public void rejectTripCancelTransaction(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketTransactionDAO dao = new TicketTransactionDAO();
		dao.rejectTripCancelTransaction(connection, authDTO, ticketDTO);
	}

	@Override
	public void insertTicketAuditLog(AuthDTO authDTO, TicketDTO ticketDTO, Connection connection, String actionEvent) {
		TicketDAO ticketDAO = new TicketDAO();
		ticketDAO.insertTicketAuditLog(authDTO, ticketDTO, connection, actionEvent);
	}

	@Override
	public void updateTripCancelTicketDetail(Connection connection, AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketTransactionDAO dao = new TicketTransactionDAO();
		dao.updateTripCancelTicketDetail(connection, authDTO, ticketDTO);
	}

	@Override
	public boolean isTripCancelInitiated(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketTransactionDAO dao = new TicketTransactionDAO();
		boolean isTripCancelInitiated = dao.isTripCancelInitiated(authDTO, ticketDTO);
		return isTripCancelInitiated;
	}

	@Override
	public void releaseTentativeBlockTicket(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketCache ticketCache = new TicketCache();
		BookingDTO bookingDTO = ticketCache.getBookingDTO(authDTO, ticketDTO.getBookingCode());
		if (bookingDTO == null || bookingDTO.getTicketList().isEmpty()) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		for (TicketDTO ticket : bookingDTO.getTicketList()) {
			TicketDTO ticketRepo = getTicketStatus(authDTO, ticket.getCode());
			if (ticketRepo.getTicketStatus().getId() == ticket.getTicketStatus().getId() && ticket.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticket.getTicketAt(), DateUtil.NOW()) < authDTO.getNamespace().getProfile().getSeatBlockTime()) {
				ticket.setTicketStatus(TicketStatusEM.TENTATIVE_BLOCK_CANCELLED);
				for (TicketDetailsDTO detailsDTO : ticket.getTicketDetails()) {
					detailsDTO.setTicketStatus(TicketStatusEM.TENTATIVE_BLOCK_CANCELLED);
				}
				ticketRepo.setTicketStatus(TicketStatusEM.TENTATIVE_BLOCK_CANCELLED);
				for (TicketDetailsDTO detailsDTO : ticketRepo.getTicketDetails()) {
					detailsDTO.setTicketStatus(TicketStatusEM.TENTATIVE_BLOCK_CANCELLED);
				}
				updateReleaseTentativeBlockTicket(authDTO, ticketRepo);
			}
			else if (DateUtil.getMinutiesDifferent(ticket.getTicketAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
				throw new ServiceException(ErrorCode.TENTATIVE_BLOCK_TICKET_ALREADY_RELEASED);
			}
			else if (ticketRepo.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
				throw new ServiceException(ErrorCode.TENTATIVE_BLOCK_TICKET_ALREADY_CONFIRMED);
			}
			else if (ticketRepo.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId()) {
				throw new ServiceException(ErrorCode.TENTATIVE_BLOCK_TICKET_ALREADY_RELEASED);
			}
		}
		ticketCache.putBookingDTO(authDTO, bookingDTO);
	}

	public void applyMobileNumberMasking(AuthDTO authDTO, TicketDTO ticketDTO) {
		if (authDTO.getNamespace().getProfile().isMobileNumberMaskingEnabled() && ticketDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && ticketDTO.getDeviceMedium().getId() == DeviceMediumEM.API_USER.getId() && DateUtil.getMinutiesDifferent(ticketDTO.getTicketAt(), DateUtil.NOW()) < authDTO.getNamespace().getProfile().getSeatBlockTime()) {
			ticketDTO.setPassengerMobile(StringUtil.getMobileNumberMasking(ticketDTO.getPassengerMobile()));
		}
	}

	private void updateReleaseTentativeBlockTicket(AuthDTO authDTO, TicketDTO ticketDTO) {

		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);
				UpdateTicketDetailsStatus(connection, authDTO, ticketDTO, Text.RELEASE_TENTATIVE_BLOCK_TICKET);
				tripService.updateTripSeatDetailsStatus(connection, authDTO, ticketDTO);

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
	public TicketAddonsDetailsDTO checkTicketUsed(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO ticketDAO = new TicketDAO();
		TicketAddonsDetailsDTO ticketAddonsDetailsDTO = ticketDAO.checkTicketUsed(authDTO, ticketDTO);
		return ticketAddonsDetailsDTO;
	}

	public void generateLinkPay(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			if (ticketDTO.getTicketExtra() != null && StringUtil.isNotNull(ticketDTO.getTicketExtra().getLinkPay())) {
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("z", ApplicationConfig.getServerZoneCode());
				jsonObject.put("n", authDTO.getNamespaceCode());
				jsonObject.put("p", ticketDTO.getCode());

				String encryptLink = BitsEnDecrypt.getEncoder(jsonObject.toString());

				String link = Constants.LINK_PAY + "/" + encryptLink;
				String shortURL = BitsShortURL.getUrlshortener(link, BitsShortURL.TYPE.TMP);
				ticketDTO.getTicketExtra().setLinkPay(shortURL);

				// Update Link Pay
				TicketDAO ticketDAO = new TicketDAO();
				TicketExtraDTO repoTicketExtra = ticketDAO.getTicketExtra(authDTO, ticketDTO.getId());
				if (repoTicketExtra == null) {
					ticketDAO.insertTicketExtrasV2(authDTO, ticketDTO);
				}
				else {
					ticketDAO.updateTicketExtra(authDTO, ticketDTO);
				}

				// Send SMS
				smsService.sendLinkPaySMS(authDTO, ticketDTO);
			}
		}
		catch (Exception e) {
			throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
		}
	}

	public TicketExtraDTO generateLinkPayV2(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketExtraDTO ticketExtra = new TicketExtraDTO();
		if (ticketDTO.getTicketExtra() != null && StringUtil.isNotNull(ticketDTO.getTicketExtra().getLinkPay())) {
			try {
				TicketDTO repoTicket = new TicketDTO();
				repoTicket.setCode(ticketDTO.getCode());
				repoTicket = getTicketStatus(authDTO, repoTicket);

				if (repoTicket.getTicketStatus().getId() != TicketStatusEM.TMP_BLOCKED_TICKET.getId() && repoTicket.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					throw new ServiceException(ErrorCode.ALREADY_CONFIRM_BOOKED_TICKET);
				}
				if (DateUtil.getDayDifferent(repoTicket.getTripDate().getStartOfDay(), DateUtil.NOW().getEndOfDay()) > 1) {
					throw new ServiceException(ErrorCode.TRIP_DATE_OVER);
				}

				TicketDAO ticketDAO = new TicketDAO();
				TicketExtraDTO repoTicketExtra = ticketDAO.getTicketExtra(authDTO, repoTicket.getId());
				repoTicket.setTripDTO(tripService.getTrip(authDTO, repoTicket.getTripDTO()));
				repoTicket.getTripDTO().setBus(getBusDTObyId(authDTO, repoTicket.getTripDTO().getBus()));
				repoTicket.setTicketExtra(repoTicketExtra);

				if (repoTicketExtra == null || StringUtil.isNull(repoTicketExtra.getLinkPay()) || Text.TRUE_STRING.equals(repoTicketExtra.getLinkPay())) {
					JSONObject jsonObject = new JSONObject();
					jsonObject.put("z", ApplicationConfig.getServerZoneCode());
					jsonObject.put("n", authDTO.getNamespaceCode());
					jsonObject.put("p", repoTicket.getCode());

					String encryptLink = BitsEnDecrypt.getEncoder(jsonObject.toString());

					String link = Constants.LINK_PAY + "/" + encryptLink;
					String shortURL = BitsShortURL.getUrlshortener(link, BitsShortURL.TYPE.TMP);
					ticketExtra.setLinkPay(shortURL);
					repoTicket.setTicketExtra(ticketExtra);

					// Update Link Pay
					if (repoTicketExtra == null) {
						ticketDAO.insertTicketExtrasV2(authDTO, repoTicket);
					}
					else {
						ticketDAO.updateTicketExtra(authDTO, repoTicket);
					}
				}
				else if (repoTicketExtra != null && StringUtil.isNotNull(repoTicketExtra.getLinkPay())) {
					ticketExtra.setLinkPay(repoTicketExtra.getLinkPay());
				}

				ticketDTO.setTicketExtra(ticketExtra);

				// Send SMS
				smsService.sendLinkPaySMS(authDTO, repoTicket);
			}
			catch (Exception e) {
				throw new ServiceException(ErrorCode.UNABLE_TO_PROVIDE_DATA);
			}
		}
		return ticketExtra;
	}

	public void pushInventoryChangesEvent(AuthDTO authDTO, TicketDTO ticket) {
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticket.getCode());
		if (ticket.getTripDTO() == null || StringUtil.isNull(ticket.getTripDTO().getCode()) || ticket.getTripDate() == null || ticket.getFromStation() == null || StringUtil.isNull(ticket.getFromStation().getCode()) || ticket.getToStation() == null || StringUtil.isNull(ticket.getToStation().getCode())) {
			ticketDTO = getTicketStatus(authDTO, ticketDTO);
		}
		else {
			ticketDTO = ticket;
		}
		if (DateUtil.getDayDifferent(DateUtil.NOW().getStartOfDay(), ticketDTO.getTripDate().getStartOfDay()) <= 5) {
			tripService.getTrip(authDTO, ticketDTO.getTripDTO());

			getBusDTObyId(authDTO, ticketDTO.getTripDTO().getBus());

			List<TicketDetailsDTO> ticketDetails = tripService.getBookedBlockedSeats(authDTO, ticketDTO.getTripDTO());

			Map<String, String> uniqueSeatMap = new HashMap<>();
			for (TicketDetailsDTO detailsDTO : ticketDetails) {
				uniqueSeatMap.put(detailsDTO.getSeatCode(), detailsDTO.getSeatCode());
			}

			int availableSeatCount = ticketDTO.getTripDTO().getBus().getSeatLayoutCount() - uniqueSeatMap.size();

			// Update RB OTA
			if (availableSeatCount <= 8 && authDTO.getNamespace().getProfile().getOtaPartnerCode().get(UserTagEM.API_USER_RB.getCode()) != null) {
				searchService.pushInventoryChangesEvent(authDTO, ticketDTO.getTripDTO());
			}
		}
	}

	private void applyScheduleStationPointContact(AuthDTO authDTO, TicketDTO ticketDTO) {
		Map<Integer, String> stationPointMap = new HashMap<Integer, String>();
		Map<Integer, String> stationPointAddressMap = new HashMap<Integer, String>();
		Map<Integer, String> stationMap = new HashMap<Integer, String>();
		ScheduleDTO scheduleDTO = scheduleService.getSchedule(authDTO, ticketDTO.getTripDTO().getSchedule());

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

	@Override
	public TicketExtraDTO getTicketExtra(AuthDTO authDTO, TicketDTO ticket) {
		TicketDAO ticketDAO = new TicketDAO();
		return ticketDAO.getTicketExtra(authDTO, ticket);
	}

	@Override
	public List<TicketTransactionDTO> getTicketTransactionV2(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketTransactionDAO dao = new TicketTransactionDAO();
		List<TicketTransactionDTO> transactions = dao.getTicketTransactionV2(authDTO, ticketDTO);
		for (TicketTransactionDTO ticketTransactionDTO : transactions) {
			if (TransactionTypeEM.TICKETS_CANCEL.getId() == ticketTransactionDTO.getTransactionType().getId()) {
				getTicketCancelTransactionDetails(authDTO, ticketTransactionDTO, ticketTransactionDTO.getUserDTO());
			}
		}
		return transactions;
	}

	public boolean isServiceFirstTicket(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO ticketDAO = new TicketDAO();
		return ticketDAO.isServiceFirstTicket(authDTO, ticketDTO);
	}

	@Override
	public void updateTicketForUser(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketDAO ticketDAO = new TicketDAO();
		ticketDAO.updateTicketForUser(authDTO, ticketDTO);
	}

	@Override
	public void findTicketByLookupId(AuthDTO authDTO, TicketDTO ticketDTO) {
		try {
			TicketDAO dao = new TicketDAO();
			dao.findTicketByLookupId(authDTO, ticketDTO);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error("Error in retrieving the ticket status: " + authDTO.getNamespaceCode() + "-" + ticketDTO.getCode() + "-" + e.getMessage());
		}
	}

	@Override
	public List<TicketDTO> findTicketByLookupIdV2(AuthDTO authDTO, TicketDTO ticketDTO) {
		List<TicketDTO> tickets = new ArrayList<>();
		try {
			TicketDAO dao = new TicketDAO();
			tickets = dao.findTicketByLookupIdV2(authDTO, ticketDTO);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			logger.error("Error in retrieving the ticket status: " + authDTO.getNamespaceCode() + "-" + ticketDTO.getCode() + "-" + e.getMessage());
		}
		return tickets;
	}

	public void migrateTicketNetRevenue(AuthDTO authDTO, DateTime fromDate) {
		DateTime toDate = DateUtil.addDaysToDate(DateUtil.NOW(), 3);
		List<DateTime> tripDates = DateUtil.getDateListV3(fromDate, toDate, "1111111");
		SearchDTO searchDTO = new SearchDTO();

		TripDAO tripDAO = new TripDAO();

		Map<String, TicketTransactionDTO> transactions = null;
		Map<String, TicketDTO> tickets = null;

		for (DateTime tripDate : tripDates) {
			searchDTO.setTravelDate(tripDate);
			List<TripDTO> trips = searchService.getAllTrips(authDTO, searchDTO);

			for (TripDTO tripDTO : trips) {
				transactions = new HashMap<>();
				tickets = new HashMap<>();

				for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {

					TicketDTO ticketDTO = new TicketDTO();
					if (tickets.get(ticketDetailsDTO.getTicketCode()) == null) {
						ticketDTO.setCode(ticketDetailsDTO.getTicketCode());
						getTicketStatus(authDTO, ticketDTO);
						tickets.put(ticketDetailsDTO.getTicketCode(), ticketDTO);
					}
					else {
						ticketDTO = tickets.get(ticketDetailsDTO.getTicketCode());
					}

					TicketTransactionDTO ticketTransactionDTO = null;
					if (transactions.get(ticketDetailsDTO.getTicketCode() + "_" + ticketDetailsDTO.getTicketStatus().getCode()) == null) {
						ticketTransactionDTO = new TicketTransactionDTO();
						ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_CANCEL);
						if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
							ticketTransactionDTO.setTransactionType(TransactionTypeEM.TICKETS_BOOKING);
						}
						ticketDTO.setTicketXaction(ticketTransactionDTO);
						if (ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
							getTicketTransaction(authDTO, ticketDTO);
							getTicketCancelTransactionDetails(authDTO, ticketDTO.getTicketXaction(), ticketDTO.getTicketXaction().getUserDTO());
						}
						else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
							UserDTO userDTO = new UserDTO();
							userDTO.setId(ticketDTO.getTicketUser().getId());
							userDTO = userService.getUser(authDTO, userDTO);

							CommissionDTO commissionDTO = null;
							if (UserRoleEM.USER_ROLE.getId() == userDTO.getUserRole().getId()) {
								CommissionDAO dao = new CommissionDAO();
								commissionDTO = dao.getTransactionCommissionDetails(authDTO, userDTO, CommissionTypeEM.TICKETS_BOOKING);
							}
							// Apply Agent Commission
							BigDecimal creditCommissionAmount = BigDecimal.ZERO;
							if (userDTO.getUserRole().getId() == UserRoleEM.USER_ROLE.getId() && commissionDTO != null && commissionDTO.getCommissionValue().compareTo(BigDecimal.ZERO) > 0) {
								creditCommissionAmount = applyCommission(authDTO, commissionDTO, ticketDTO, userDTO);
							}

							BigDecimal totalCommissionAmount = calculateNetRevenueBookAmount(authDTO, creditCommissionAmount, commissionDTO, ticketDTO);
							ticketDTO.getTicketXaction().setCommissionAmount(totalCommissionAmount);
						}
						transactions.put(ticketDetailsDTO.getTicketCode() + "_" + ticketDetailsDTO.getTicketStatus().getCode(), ticketDTO.getTicketXaction());
						ticketTransactionDTO = ticketDTO.getTicketXaction();
					}
					else {
						ticketTransactionDTO = transactions.get(ticketDetailsDTO.getTicketCode() + "_" + ticketDetailsDTO.getTicketStatus().getCode());
					}

					if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
						BigDecimal seatCommissionAmount = BigDecimal.ZERO;
						if (ticketTransactionDTO.getCommissionAmount().compareTo(BigDecimal.ZERO) > 0) {
							seatCommissionAmount = ticketTransactionDTO.getCommissionAmount().divide(BigDecimal.valueOf(ticketDTO.getTicketDetails().size()), 2, RoundingMode.CEILING);
						}
						BigDecimal addonsAmount = ticketDTO.getAddonsValue(ticketDetailsDTO);

						BigDecimal netRevenueAmount = ticketDetailsDTO.getSeatFare().subtract(seatCommissionAmount).subtract(addonsAmount);
						ticketDetailsDTO.getTicketExtra().setNetAmount(netRevenueAmount.setScale(2, BigDecimal.ROUND_CEILING));
					}
					else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TICKET_TRANSFERRED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TRIP_CANCELLED.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
						ticketDetailsDTO.getTicketExtra().setNetAmount(BigDecimal.ZERO);
					}
					else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
						BigDecimal cancelCommissionAmount = ticketTransactionDTO.getCancellationCommissionAmount().divide(BigDecimal.valueOf(ticketTransactionDTO.getTransSeatCount()), 2);
						BigDecimal netRefundAmount = ticketDetailsDTO.getCancellationCharges().divide(BigDecimal.valueOf(ticketTransactionDTO.getTransSeatCount()), 2).subtract(cancelCommissionAmount);
						ticketDetailsDTO.getTicketExtra().setNetAmount(netRefundAmount.setScale(2, BigDecimal.ROUND_CEILING));
					}
					if (ticketDetailsDTO.getTicketExtra().getTicketAt() == null) {
						ticketDetailsDTO.getTicketExtra().setTicketAt(ticketDetailsDTO.getTicketAt());
					}

					tripDAO.updateTripSeatDetailsWithExtras(authDTO, ticketDetailsDTO);
				}
			}
		}
	}

	private BigDecimal calculateNetRevenueBookAmount(AuthDTO authDTO, BigDecimal creditCommissionAmount, CommissionDTO commissionDTO, TicketDTO ticketDTO) {
		BigDecimal totalCommissionAmount = BigDecimal.ZERO;
		try {
			BigDecimal seatCommissionAmount = BigDecimal.ZERO;
			BigDecimal serviceTaxAmount = BigDecimal.ZERO;
			if (creditCommissionAmount.compareTo(BigDecimal.ZERO) > 0) {
				seatCommissionAmount = creditCommissionAmount.divide(BigDecimal.valueOf(ticketDTO.getTicketDetails().size()), 2, RoundingMode.CEILING);
				serviceTaxAmount = seatCommissionAmount.multiply(commissionDTO.getServiceTax().divide(Numeric.ONE_HUNDRED, 2, RoundingMode.CEILING));
			}

			totalCommissionAmount = totalCommissionAmount.add(seatCommissionAmount).add(serviceTaxAmount);
		}
		catch (Exception e) {
			System.out.println("Block NTRA Error:" + ticketDTO.getCode());
			e.printStackTrace();
		}
		return totalCommissionAmount;
	}

	private BigDecimal applyCommission(AuthDTO authDTO, CommissionDTO commissionDTO, TicketDTO ticketDTO, UserDTO userDTO) {
		BigDecimal creditCommissionAmount = BigDecimal.ZERO;
		BigDecimal creditExtraCommissionAmount = BigDecimal.ZERO;

		if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId()) {
			creditCommissionAmount = ticketDTO.getTotalSeatFare().subtract(ticketDTO.getAddonsValue()).multiply(commissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2);
		}
		else if (commissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
			creditCommissionAmount = commissionDTO.getCommissionValue().multiply(new BigDecimal(ticketDTO.getTicketDetails().size()));
		}

		// Extra Commission Processes
		CommissionDTO extraCommissionDTO = getBookingExtraCommission(authDTO, userDTO, commissionDTO, ticketDTO);
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

		return creditCommissionAmount;
	}

	public CommissionDTO getBookingExtraCommission(AuthDTO authDTO, UserDTO userDTO, CommissionDTO userCommissionDTO, TicketDTO ticketDTO) {
		CommissionDTO commissionDTO = null;
		List<ExtraCommissionDTO> commissionList = null;
		try {
			commissionList = cacheService.getAllExtraCommissionCache(authDTO);
			if (commissionList == null) {
				CommissionDAO commissionDAO = new CommissionDAO();
				commissionList = commissionDAO.getAllExtraCommission(authDTO);
				cacheService.putgetAllExtraCommissionCache(authDTO, commissionList);
			}
			List<RouteDTO> routeList = null;
			boolean userExtraCommissionFound = false;
			TripDTO tripDTO = ticketDTO.getTripDTO();
			for (Iterator<ExtraCommissionDTO> iterator = commissionList.iterator(); iterator.hasNext();) {
				ExtraCommissionDTO discountDTO = iterator.next();
				DateTime dateTime = ticketDTO.getTripDate();
				if (discountDTO.getDateType().getId() == DateTypeEM.TRANSACTION.getId()) {
					dateTime = DateUtil.NOW();
				}
				if (StringUtil.isNull(discountDTO.getActiveFrom()) || StringUtil.isNull(discountDTO.getActiveTo()) || StringUtil.isNull(discountDTO.getDayOfWeek())) {
					iterator.remove();
					continue;
				}
				// common validations
				if (discountDTO.getActiveFrom() != null && !dateTime.gteq(new DateTime(discountDTO.getActiveFrom()).getStartOfDay())) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getActiveTo() != null && !dateTime.lteq(new DateTime(discountDTO.getActiveTo()).getEndOfDay())) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getDayOfWeek() != null && discountDTO.getDayOfWeek().length() != 7) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getDayOfWeek() != null && discountDTO.getDayOfWeek().substring(dateTime.getWeekDay() - 1, dateTime.getWeekDay()).equals("0")) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getCommissionValue().compareTo(BigDecimal.ZERO) == 0) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getGroup() != null && !discountDTO.getGroup().isEmpty() && BitsUtil.isGroupExists(discountDTO.getGroup(), userDTO.getGroup()) == null) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getUser() != null && !discountDTO.getUser().isEmpty() && BitsUtil.isUserExists(discountDTO.getUser(), userDTO) == null) {
					iterator.remove();
					continue;
				}
				if (StringUtil.isNotNull(discountDTO.getScheduleCode())) {
					if (tripDTO == null || tripDTO.getSchedule() == null) {
						tripDTO = tripService.getTripDTOwithScheduleDetails(authDTO, ticketDTO.getTripDTO());
					}
					if (!discountDTO.getScheduleCode().contains(tripDTO.getSchedule().getCode())) {
						iterator.remove();
						continue;
					}
				}
				if (!discountDTO.getRouteList().isEmpty() && ticketDTO.getTripDTO().getSearch().getFromStation() != null && ticketDTO.getTripDTO().getSearch().getToStation() != null) {
					if (routeList == null) {
						routeList = stationService.getRoute(authDTO);
					}
					boolean status = applyCommissionRouteFilter(authDTO, routeList, discountDTO, ticketDTO.getTripDTO());
					if (!status) {
						iterator.remove();
						continue;
					}
				}
				if (ticketDTO.getTotalFare().compareTo(discountDTO.getMinTicketFare()) == -1) {
					iterator.remove();
					continue;
				}
				if (discountDTO.getMinSeatCount() > Numeric.ZERO_INT && ticketDTO.getTicketDetails().size() < discountDTO.getMinSeatCount()) {
					iterator.remove();
					continue;
				}
				// Exception and override
				for (Iterator<ExtraCommissionDTO> OverrideIterator = discountDTO.getOverrideList().iterator(); OverrideIterator.hasNext();) {
					ExtraCommissionDTO overrideStationDTO = OverrideIterator.next();
					if (StringUtil.isNull(overrideStationDTO.getActiveFrom()) || StringUtil.isNull(overrideStationDTO.getActiveTo()) || StringUtil.isNull(overrideStationDTO.getDayOfWeek())) {
						OverrideIterator.remove();
						continue;
					}
					// common validations
					if (overrideStationDTO.getActiveFrom() != null && !dateTime.gteq(new DateTime(overrideStationDTO.getActiveFrom()))) {
						OverrideIterator.remove();
						continue;
					}
					if (overrideStationDTO.getActiveTo() != null && !dateTime.lteq(new DateTime(overrideStationDTO.getActiveTo()))) {
						OverrideIterator.remove();
						continue;
					}
					if (overrideStationDTO.getDayOfWeek() != null && overrideStationDTO.getDayOfWeek().length() != 7) {
						OverrideIterator.remove();
						continue;
					}
					if (overrideStationDTO.getDayOfWeek() != null && overrideStationDTO.getDayOfWeek().substring(dateTime.getWeekDay() - 1, dateTime.getWeekDay()).equals("0")) {
						OverrideIterator.remove();
						continue;
					}
					// Remove if Exceptions
					iterator.remove();
					break;
				}

				if (discountDTO.getUser() != null && !discountDTO.getUser().isEmpty() && BitsUtil.isUserExists(discountDTO.getUser(), userDTO) != null) {
					userExtraCommissionFound = true;
				}
			}
			// remove if User wise assigned
			List<TripDTO> seatCountlist = null;
			for (Iterator<ExtraCommissionDTO> iterator = commissionList.iterator(); iterator.hasNext();) {
				ExtraCommissionDTO discountDTO = iterator.next();

				if (userExtraCommissionFound && discountDTO.getRefferenceType().equals("GR")) {
					iterator.remove();
					break;
				}
				if (discountDTO.getCommissionSlab() != null && seatCountlist == null) {
					seatCountlist = tripService.getTripWiseBookedSeatCount(authDTO, userDTO, getSlabDateRange(ticketDTO, commissionList));
				}
				if (discountDTO.getCommissionSlab() != null && seatCountlist != null) {
					boolean status = applyExtraCommissionSlab(seatCountlist, ticketDTO, discountDTO.getCommissionSlab());
					if (!status) {
						iterator.remove();
						continue;
					}
				}
			}

			ExtraCommissionDTO extraCommissionDTO = null;
			for (ExtraCommissionDTO discountDTO : commissionList) {
				if (extraCommissionDTO == null) {
					extraCommissionDTO = discountDTO;
				}
				if (DateUtil.getDayDifferent(new DateTime(discountDTO.getActiveFrom()), new DateTime(discountDTO.getActiveTo())) <= DateUtil.getDayDifferent(new DateTime(extraCommissionDTO.getActiveFrom()), new DateTime(extraCommissionDTO.getActiveTo()))) {
					extraCommissionDTO = discountDTO;
				}
			}

			// Max Commission Limit
			if (extraCommissionDTO != null && extraCommissionDTO.getMaxCommissionLimit().compareTo(BigDecimal.ZERO) == 1 && userCommissionDTO.getCommissionValue().add(extraCommissionDTO.getCommissionValue()).compareTo(extraCommissionDTO.getMaxCommissionLimit()) == 1) {
				extraCommissionDTO.setCommissionValue(extraCommissionDTO.getMaxCommissionLimit().subtract(userCommissionDTO.getCommissionValue()));
			}

			// Max Extra Commission Amount
			if (extraCommissionDTO != null && extraCommissionDTO.getMaxExtraCommissionAmount().compareTo(BigDecimal.ZERO) == 1 && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.PERCENTAGE.getId() && (ticketDTO.getTotalSeatFare().multiply(extraCommissionDTO.getCommissionValue()).divide(Numeric.ONE_HUNDRED, 2)).compareTo(extraCommissionDTO.getMaxExtraCommissionAmount()) == 1) {
				extraCommissionDTO.setCommissionValue(extraCommissionDTO.getMaxExtraCommissionAmount().multiply(Numeric.ONE_HUNDRED).divide(ticketDTO.getTotalSeatFare(), RoundingMode.CEILING));
			}
			else if (extraCommissionDTO != null && extraCommissionDTO.getMaxExtraCommissionAmount().compareTo(BigDecimal.ZERO) == 1 && extraCommissionDTO.getCommissionValueType().getId() == FareTypeEM.FLAT.getId()) {
				extraCommissionDTO.setCommissionValue(extraCommissionDTO.getMaxExtraCommissionAmount());
			}

			if (extraCommissionDTO != null) {
				commissionDTO = new CommissionDTO();
				commissionDTO.setCommissionValue(extraCommissionDTO.getCommissionValue());
				commissionDTO.setCommissionValueType(extraCommissionDTO.getCommissionValueType());
				// Marking to Override Base commission value
				commissionDTO.setActiveFlag(-1);
			}
		}
		catch (Exception e) {
			System.out.println("Error in Get Extra Commssion" + authDTO.getNamespaceCode() + " " + userDTO.getCode());
			e.printStackTrace();
		}
		return commissionDTO;
	}

	private boolean applyCommissionRouteFilter(AuthDTO authDTO, List<RouteDTO> routeList, ExtraCommissionDTO discountDTO, TripDTO tripDTO) {
		for (RouteDTO routeDTO : routeList) {
			for (RouteDTO commRouteDTO : discountDTO.getRouteList()) {
				if (commRouteDTO.getCode().equals(routeDTO.getCode()) && routeDTO.getFromStation().getId() == tripDTO.getStage().getFromStation().getStation().getId() && routeDTO.getFromStation().getId() == tripDTO.getStage().getFromStation().getStation().getId()) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean applyExtraCommissionSlab(List<TripDTO> seatCountlist, TicketDTO ticketDTO, ExtraCommissionSlabDTO commissionSlab) {
		int seatCount = 0;
		int seatAmount = 0;

		DateTime fromDate = null, toDate = null;
		if (commissionSlab.getSlabCalenderMode().getId() == SlabCalenderModeEM.FLEXI.getId()) {
			if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.DAY.getId()) {
				fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getStartOfDay();
				toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
			}
			else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.WEEK.getId()) {
				fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 8).getStartOfDay();
				toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
			}
			else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.MONTH.getId()) {
				fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 31).getStartOfDay();
				toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
			}
		}
		else if (commissionSlab.getSlabCalenderMode().getId() == SlabCalenderModeEM.STRICT.getId()) {
			if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.DAY.getId()) {
				fromDate = ticketDTO.getTripDTO().getTripDate().getStartOfDay();
				toDate = ticketDTO.getTripDTO().getTripDate().getEndOfDay();
			}
			else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.WEEK.getId()) {
				fromDate = DateUtil.getWeekStartDate(ticketDTO.getTripDTO().getTripDate()).getStartOfDay();
				toDate = DateUtil.getWeekEndDate(ticketDTO.getTripDTO().getTripDate()).getEndOfDay();
			}
			else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.MONTH.getId()) {
				fromDate = ticketDTO.getTripDTO().getTripDate().getStartOfMonth();
				toDate = fromDate.plusDays(ticketDTO.getTripDTO().getTripDate().getNumDaysInMonth() - 1).getEndOfDay();
			}
		}
		for (TripDTO tripDTO : seatCountlist) {
			if (tripDTO.getTripDate().getStartOfDay().gteq(fromDate) && tripDTO.getTripDate().getStartOfDay().lteq(toDate)) {
				seatCount += tripDTO.getBookedSeatCount();
				seatAmount += tripDTO.getId();
			}
		}
		if (commissionSlab.getSlabMode().getId() == SlabModeEM.COUNT.getId() && seatCount >= commissionSlab.getSlabFromValue() && seatCount <= commissionSlab.getSlabToValue()) {
			return true;
		}
		else if (commissionSlab.getSlabMode().getId() == SlabModeEM.AMOUNT.getId() && seatAmount >= commissionSlab.getSlabFromValue() && seatAmount <= commissionSlab.getSlabToValue()) {
			return true;
		}
		return false;
	}

	private ExtraCommissionDTO getSlabDateRange(TicketDTO ticketDTO, List<ExtraCommissionDTO> commissionList) {
		ExtraCommissionDTO commission = null;
		for (ExtraCommissionDTO extraCommissionDTO : commissionList) {
			if (extraCommissionDTO.getCommissionSlab() == null) {
				continue;
			}
			DateTime fromDate = null, toDate = null;
			ExtraCommissionSlabDTO commissionSlab = extraCommissionDTO.getCommissionSlab();
			if (commissionSlab.getSlabCalenderMode().getId() == SlabCalenderModeEM.FLEXI.getId()) {
				if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.DAY.getId()) {
					fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getStartOfDay();
					toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
				}
				else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.WEEK.getId()) {
					fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 8).getStartOfDay();
					toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
				}
				else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.MONTH.getId()) {
					fromDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 31).getStartOfDay();
					toDate = DateUtil.minusDaysToDate(DateUtil.NOW(), 1).getEndOfDay();
				}
			}
			else if (commissionSlab.getSlabCalenderMode().getId() == SlabCalenderModeEM.STRICT.getId()) {
				if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.DAY.getId()) {
					fromDate = ticketDTO.getTripDTO().getTripDate().getStartOfDay();
					toDate = ticketDTO.getTripDTO().getTripDate().getEndOfDay();
				}
				else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.WEEK.getId()) {
					fromDate = DateUtil.getWeekStartDate(ticketDTO.getTripDTO().getTripDate()).getStartOfDay();
					toDate = DateUtil.getWeekEndDate(ticketDTO.getTripDTO().getTripDate()).getEndOfDay();
				}
				else if (commissionSlab.getSlabCalenderType().getId() == SlabCalenderTypeEM.MONTH.getId()) {
					fromDate = ticketDTO.getTripDTO().getTripDate().getStartOfMonth();
					toDate = fromDate.plusDays(ticketDTO.getTripDTO().getTripDate().getNumDaysInMonth() - 1).getEndOfDay();
				}
			}
			if (commission == null) {
				commission = new ExtraCommissionDTO();
				commission.setActiveFrom(fromDate.format("YYYY-MM-DD"));
				commission.setActiveTo(toDate.format("YYYY-MM-DD"));
			}
			if (commission.getActiveFromDate().gt(fromDate)) {
				commission.setActiveFrom(fromDate.format("YYYY-MM-DD"));
			}
			if (commission.getActiveToDate().lt(toDate)) {
				commission.setActiveTo(toDate.format("YYYY-MM-DD"));
			}
		}
		return commission;
	}

	@Override
	public List<TripDTO> getTripsForPhoneBlockForceRelease(AuthDTO authDTO, DateTime travelDate) {
		TicketDAO ticketDAO = new TicketDAO();
		List<TripDTO> trips = ticketDAO.getTripsForPhoneBlockForceRelease(authDTO, travelDate);
		for (TripDTO tripDTO : trips) {
			tripService.getTrip(authDTO, tripDTO);
		}
		return trips;
	}

	@Override
	public void updateTopRoute(AuthDTO authDTO) {
		List<RouteDTO> routeList = new ArrayList<>();
		TicketDAO ticketDAO = new TicketDAO();

		DateTime currentDatetime = DateUtil.NOW();
		DateTime fromDate = currentDatetime.minusDays(7).getStartOfDay();
		DateTime toDate = currentDatetime;

		Map<Integer, Map<String, Integer>> topRouteMap = ticketDAO.getPastDaysBookedRoute(authDTO, fromDate, toDate);
		if (!topRouteMap.isEmpty()) {
			Map<Object, Map<String, Object>> sortedTopRouteMap = topRouteMap.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue().entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder())).limit(6).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> a, LinkedHashMap::new)), (a, b) -> a, LinkedHashMap::new));

			Map<String, List<String>> commerceRoutes = stationService.getCommerceRoutes(authDTO);
			for (Map.Entry<String, List<String>> routeMap : commerceRoutes.entrySet()) {
				String fromStationCode = routeMap.getKey();
				for (String toStationCode : routeMap.getValue()) {
					RouteDTO routeDTO = new RouteDTO();

					StationDTO fromStation = new StationDTO();
					fromStation.setCode(fromStationCode);
					stationService.getStation(fromStation);
					routeDTO.setFromStation(fromStation);

					StationDTO toStation = new StationDTO();
					toStation.setCode(toStationCode);
					stationService.getStation(toStation);
					routeDTO.setToStation(toStation);

					Map<String, Object> topRoute = sortedTopRouteMap.get(fromStation.getId());
					if (topRoute != null && topRoute.get(fromStation.getId() + Text.UNDER_SCORE + toStation.getId()) != null) {
						routeDTO.setTopRouteFlag(Numeric.ONE_INT);
						int bookingCount = Integer.valueOf(String.valueOf(topRoute.get(fromStation.getId() + Text.UNDER_SCORE + toStation.getId())));
						routeDTO.setBookingCount(bookingCount);
						routeList.add(routeDTO);
					}
				}
			}

			if (!routeList.isEmpty()) {
				StationDAO dao = new StationDAO();
				dao.removeTopRouteFlag(authDTO);
				dao.updateTopRouteFlag(authDTO, routeList);

				String key = authDTO.getNamespace().getCode() + Constants.TOP_ROUTE;
				EhcacheManager.getCommerceStaticEhCache().remove(key);
			}
		}
	}

	/** CRON Job - Update ticket status to temporary block status */
	@Override
	public void updateTicketStatusToBlock(AuthDTO authDTO, List<GroupDTO> groups, List<TicketStatusEM> ticketStatusList, int numberOfDays) {

		TicketDAO dao = new TicketDAO();
		for (GroupDTO groupDTO : groups) {
			groupDTO = getGroupDTO(authDTO, groupDTO);
			if (groupDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_GROUP);
			}
		}

		DateTime toDate = DateUtil.NOW().minusDays(numberOfDays).getEndOfDay();
		DateTime fromDate = toDate.minusDays(7).getStartOfDay();

		List<TicketDTO> ticketList = dao.getTicketDetails(authDTO, groups, ticketStatusList, fromDate, toDate);

		Map<String, TripDTO> tripMap = new HashMap<>();
		for (TicketDTO tickeDTO : ticketList) {
			try {
				tickeDTO.setTicketStatus(TicketStatusEM.TMP_BLOCKED_TICKET);
				for (TicketDetailsDTO ticketDetailsDTO : tickeDTO.getTicketDetails()) {
					ticketDetailsDTO.setTicketStatus(TicketStatusEM.TMP_BLOCKED_TICKET);
				}
				for (TicketAddonsDetailsDTO ticketAddonsDetailsDTO : tickeDTO.getTicketAddonsDetails()) {
					ticketAddonsDetailsDTO.setTicketStatus(TicketStatusEM.TMP_BLOCKED_TICKET);
				}
				changeTicketStatus(authDTO, tickeDTO);

				if (tripMap.get(tickeDTO.getTripDTO().getCode()) == null) {
					tripMap.put(tickeDTO.getTripDTO().getCode(), tickeDTO.getTripDTO());
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}

		// Clear Redis Trip cache
		List<TripDTO> tripList = new ArrayList<>(tripMap.values());
		for (TripDTO tripDTO : tripList) {
			tripService.clearBookedBlockedSeatsCache(authDTO, tripDTO);
		}
	}

	private void changeTicketStatus(AuthDTO authDTO, TicketDTO ticketDTO) throws Exception {
		try {
			@Cleanup
			Connection connection = ConnectDAO.getConnection();
			try {
				connection.setAutoCommit(false);

				TicketDAO dao = new TicketDAO();
				dao.UpdateTicketStatus(connection, authDTO, ticketDTO);

				tripService.updateTripSeatDetailsStatus(connection, authDTO, ticketDTO);
			}
			catch (SQLTransactionRollbackException e) {
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

	private void editTicketRemarks(AuthDTO authDTO, TicketDTO ticketDTO, TicketDTO repoTicketDTO) {
		String event = "edit Remarks : " + (StringUtil.isNotNull(repoTicketDTO.getRemarks()) ? repoTicketDTO.getRemarks() + " changed to " + ticketDTO.getRemarks() : ticketDTO.getRemarks());
		repoTicketDTO.setRemarks(ticketDTO.getRemarks());

		TicketEditDAO ticketEditDAO = new TicketEditDAO();
		ticketEditDAO.editRemarks(authDTO, repoTicketDTO, event);
	}

	@Override
	public List<TicketDTO> getRecentTicketUserCustomer(AuthDTO auth) {
		List<TicketDTO> list = new ArrayList<>();
		TicketDAO ticketDAO = new TicketDAO();
		if (auth.getAuthenticationType().getId() == AuthenticationTypeEM.BITS_GUEST.getId()) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		if (auth.getUser().getUserRole().getId() == UserRoleEM.CUST_ROLE.getId()) {
			List<TicketDTO> custTickets = ticketDAO.getTicketByUserCustomer(auth, auth.getUserCustomer());
			list.addAll(custTickets);
		}
		else if (auth.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
			List<TicketDTO> custTickets = ticketDAO.getBookedTicketByUser(auth, auth.getUser());
			list.addAll(custTickets);
		}

		for (TicketDTO ticket : list) {
			ticket.setTicketUser(userService.getUser(auth, ticket.getTicketUser()));
			ticket.setFromStation(stationService.getStation(ticket.getFromStation()));
			ticket.setToStation(stationService.getStation(ticket.getToStation()));
			ticket.getTripDTO().setBus(busService.getBus(auth, ticket.getTripDTO().getBus()));
			ticket.setBoardingPoint(stationPointService.getStationPoint(auth, ticket.getBoardingPoint()));
			ticket.setDroppingPoint(stationPointService.getStationPoint(auth, ticket.getDroppingPoint()));
		}
		return list;
	}
}
