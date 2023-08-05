package org.in.com.controller.web;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.beanutils.BeanComparator;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.BusIO;
import org.in.com.controller.web.io.OperatorIO;
import org.in.com.controller.web.io.OrderDetailsIO;
import org.in.com.controller.web.io.PaymentGatewayPartnerIO;
import org.in.com.controller.web.io.PaymentTransactionIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.SeatStatusIO;
import org.in.com.controller.web.io.StationIO;
import org.in.com.controller.web.io.StationPointIO;
import org.in.com.controller.web.io.TicketDetailsIO;
import org.in.com.controller.web.io.TicketExtraIO;
import org.in.com.controller.web.io.TicketIO;
import org.in.com.controller.web.io.TicketRefundIO;
import org.in.com.controller.web.io.TicketStatusIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.PendingOrderDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketExtraDTO;
import org.in.com.dto.TicketRefundDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.RefundStatusEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusService;
import org.in.com.service.CancelTicketService;
import org.in.com.service.ConfirmSeatsService;
import org.in.com.service.PaymentTransactionService;
import org.in.com.service.PendingOrderService;
import org.in.com.service.StationService;
import org.in.com.service.TicketHelperService;
import org.in.com.service.TicketPhoneBookAutoReleaseService;
import org.in.com.service.TicketService;
import org.in.com.service.UserTransactionService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hirondelle.date4j.DateTime;

@Controller
@RequestMapping("/{authtoken}/ticket")
public class TicketController extends BaseController {
	private static final Logger cancelLogger = LoggerFactory.getLogger("org.in.com.controller.cancelticket");
	public static Map<String, Integer> ConcurrentRequests = new ConcurrentHashMap<String, Integer>();

	@Autowired
	UserTransactionService transactionService;
	@Autowired
	PaymentTransactionService paymentTransactionService;
	@Autowired
	PendingOrderService pendingOrderService;
	@Autowired
	TicketService ticketService;
	@Autowired
	TicketHelperService ticketHelperService;
	@Autowired
	CancelTicketService cancelTicketService;
	@Autowired
	ConfirmSeatsService confirmSeatsService;
	@Autowired
	BusService busService;
	@Autowired
	StationService stationService;
	@Autowired
	TicketPhoneBookAutoReleaseService autoReleaseService;

	@RequestMapping(value = "/progress", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TicketIO>> getInprogreeTicketTransaction(@PathVariable("authtoken") String authtoken) throws Exception {
		List<TicketIO> ticketIOList = new ArrayList<TicketIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<BookingDTO> list = pendingOrderService.getInprogreeTicketTransaction(authDTO);

			for (BookingDTO bookingDTO : list) {
				try {
					for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
						TicketIO ticketIO = new TicketIO();
						ticketDTO.setBookingCode(bookingDTO.getCode());
						StationIO fromStationIO = new StationIO();
						StationIO toStationIO = new StationIO();
						StationPointIO boardingPointIO = new StationPointIO();
						StationPointIO droppingPointIO = new StationPointIO();
						List<StationPointIO> boardingList = new ArrayList<StationPointIO>();
						List<StationPointIO> alightingList = new ArrayList<StationPointIO>();
						List<TicketDetailsIO> ticketDetailsIO = new ArrayList<TicketDetailsIO>();

						// Mapping from station and boarding point
						boardingPointIO.setCode(ticketDTO.getBoardingPoint().getCode());
						boardingPointIO.setName(ticketDTO.getBoardingPoint().getName());
						boardingPointIO.setAddress(ticketDTO.getBoardingPoint().getAddress());
						boardingPointIO.setLandmark(ticketDTO.getBoardingPoint().getLandmark());
						boardingPointIO.setNumber(ticketDTO.getBoardingPoint().getNumber());
						boardingPointIO.setLongitude(ticketDTO.getBoardingPoint().getLongitude());
						boardingPointIO.setLatitude(ticketDTO.getBoardingPoint().getLatitude());
						boardingPointIO.setDateTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes() + ticketDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						boardingList.add(boardingPointIO);

						fromStationIO.setCode(ticketDTO.getFromStation().getCode());
						fromStationIO.setName(ticketDTO.getFromStation().getName());
						fromStationIO.setStationPoint(boardingList);

						// Mapping to station and alighting point
						droppingPointIO.setCode(ticketDTO.getDroppingPoint().getCode());
						droppingPointIO.setName(ticketDTO.getDroppingPoint().getName());
						droppingPointIO.setAddress(ticketDTO.getDroppingPoint().getAddress());
						droppingPointIO.setLandmark(ticketDTO.getDroppingPoint().getLandmark());
						droppingPointIO.setNumber(ticketDTO.getDroppingPoint().getNumber());
						droppingPointIO.setLongitude(ticketDTO.getDroppingPoint().getLongitude());
						droppingPointIO.setLatitude(ticketDTO.getDroppingPoint().getLatitude());
						droppingPointIO.setDateTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes() + ticketDTO.getDroppingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						alightingList.add(droppingPointIO);

						toStationIO.setCode(ticketDTO.getToStation().getCode());
						toStationIO.setName(ticketDTO.getToStation().getName());
						toStationIO.setStationPoint(alightingList);
						ticketIO.setFromStation(fromStationIO);
						ticketIO.setToStation(toStationIO);

						// Mapping Operator Details
						OperatorIO operatorIO = new OperatorIO();
						operatorIO.setCode(authDTO.getNamespace().getCode());
						operatorIO.setName(authDTO.getNamespace().getName());
						ticketIO.setOperator(operatorIO);

						// Mapping passenger details
						if (ticketDTO.getTicketDetails() != null) {
							for (TicketDetailsDTO dto : ticketDTO.getTicketDetails()) {
								TicketDetailsIO tickDetailsIO = new TicketDetailsIO();
								tickDetailsIO.setSeatName(dto.getSeatName());
								tickDetailsIO.setSeatCode(dto.getSeatCode());
								tickDetailsIO.setSeatType(dto.getSeatType());
								SeatStatusIO ticketStatus = new SeatStatusIO();
								ticketStatus.setCode(dto.getTicketStatus().getCode());
								ticketStatus.setName(dto.getTicketStatus().getDescription());
								tickDetailsIO.setSeatStatus(ticketStatus);
								BaseIO travelStatus = new BaseIO();
								travelStatus.setCode(dto.getTravelStatus() != null ? dto.getTravelStatus().getCode() : null);
								travelStatus.setName(dto.getTravelStatus() != null ? dto.getTravelStatus().getName() : null);
								tickDetailsIO.setTravelStatus(travelStatus);
								tickDetailsIO.setPassengerName(dto.getPassengerName());
								tickDetailsIO.setPassengerAge(dto.getPassengerAge());
								tickDetailsIO.setPassengerGendar(dto.getSeatGendar().getCode());
								tickDetailsIO.setSeatFare(dto.getSeatFare());
								ticketDetailsIO.add(tickDetailsIO);
							}
						}

						if (ticketDTO.getTicketUser() != null) {
							UserIO userIO = new UserIO();
							userIO.setName(ticketDTO.getTicketUser().getName());
							userIO.setCode(ticketDTO.getTicketUser().getCode());
							ticketIO.setUser(userIO);
						}

						TicketStatusIO ticketStatus = new TicketStatusIO();
						ticketStatus.setCode(ticketDTO.getTicketStatus().getCode());
						ticketStatus.setName(ticketDTO.getTicketStatus().getDescription());
						ticketIO.setTicketStatus(ticketStatus);
						ticketIO.setGatewayPaymentCode(ticketDTO.getTicketStatus().getCode());

						ticketIO.setTravelDate(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes()).format("YYYY-MM-DD hh:mm:ss"));
						ticketIO.setTripCode(ticketDTO.getTripDTO().getCode());
						ticketIO.setTripStageCode(ticketDTO.getTripDTO().getStage().getCode());
						ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());
						ticketIO.setPassegerEmailId(ticketDTO.getPassengerEmailId());
						ticketIO.setTotalFare(ticketDTO.getTotalFare());
						ticketIO.setServiceNo(ticketDTO.getServiceNo());
						ticketIO.setJourneyType(ticketDTO.getJourneyType().getCode());
						ticketIO.setDeviceMedium(ticketDTO.getDeviceMedium().getCode());
						ticketIO.setTicketDetails(ticketDetailsIO);
						ticketIO.setRemarks(ticketDTO.getRemarks());
						ticketIO.setBlockingLiveTime(ticketDTO.getBlockingLiveTime().format("YYYY-MM-DD hh:mm:ss"));
						ticketIO.setCode(ticketDTO.getCode());
						PaymentGatewayPartnerIO gatewayPartnerIO = new PaymentGatewayPartnerIO();
						gatewayPartnerIO.setCode(bookingDTO.getPaymentGatewayPartnerCode());
						ticketIO.setPaymentGatewayPartner(gatewayPartnerIO);
						ticketIO.setBookingCode(ticketDTO.getBookingCode());
						BusIO bus = new BusIO();
						bus.setDisplayName(ticketDTO.getTripDTO().getBus().getDisplayName());
						bus.setName(ticketDTO.getTripDTO().getBus().getName());
						bus.setCategoryCode(ticketDTO.getTripDTO().getBus().getCategoryCode());
						bus.setBusType(busService.getBusCategoryByCode(ticketDTO.getTripDTO().getBus().getCategoryCode()));
						ticketIO.setBus(bus);
						ticketIO.setTransactionDate(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
						ticketIO.setActiveFlag(ticketDTO.getActiveFlag());
						ticketIOList.add(ticketIO);
					}

				}
				catch (Exception e) {
					e.printStackTrace();
				}
			}
			// Sorting
			Comparator<TicketIO> comp = new BeanComparator("transactionDate");
			Collections.sort(ticketIOList, comp);
		}
		return ResponseIO.success(ticketIOList);
	}

	@RequestMapping(value = "/transaction/pending/confirm/{ticketcode}/{seatcode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> confirmPendingOrder(@PathVariable("authtoken") String authtoken, @PathVariable("ticketcode") String ticketCode, @PathVariable("seatcode") String seatCode, @RequestParam(value = "namespaceCode", required = true, defaultValue = "NA") String namespaceCode, @RequestBody String remarks) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null && seatCode.contains("-")) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			// Binding modified seat code in ticket details
			validate(seatCode);
			List<TicketDetailsDTO> ticketDetails = new ArrayList<>();
			String[] seatCodes = seatCode.split(",");
			for (int count = 0; count < seatCodes.length; count++) {
				TicketDetailsDTO tckDetails = new TicketDetailsDTO();
				tckDetails.setSeatCode(seatCodes[count]);
				ticketDetails.add(tckDetails);
			}
			ticketDTO.setTicketDetails(ticketDetails);
			ticketDTO.setRemarks(remarks);
			ticketDTO = pendingOrderService.confirmPendingOrder(authDTO, ticketDTO);
			if (ticketDTO.getActiveFlag() != 1) {
				return ResponseIO.failure(ErrorCode.UNABLE_TO_CONFIRM_TICKET);
			}
		}
		return ResponseIO.success();
	}

	private void validate(String seatCode) {
		if (StringUtil.isNull(seatCode)) {
			throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
		}
		for (String data : seatCode.split(",")) {
			if (data.split("-").length != 2) {
				throw new ServiceException(ErrorCode.PENDING_ORDER_CONFIRMATION_SEAT_COUNT_MISMATCH);
			}
		}
	}

	@RequestMapping(value = "/transaction/pending/cancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<PaymentTransactionIO> cancelPendingOrder(@PathVariable("authtoken") String authtoken, String transactionCode, String ticketCode) throws Exception {
		PaymentTransactionIO paymentTransactionIO = new PaymentTransactionIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			PendingOrderDTO dto = new PendingOrderDTO();
			dto.setOrderCode(ticketCode);
			dto.setCode(transactionCode);
			pendingOrderService.cancelPendingOrder(authDTO, dto);
		}
		return ResponseIO.success(paymentTransactionIO);
	}

	@RequestMapping(value = "/find", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TicketIO>> getFindTicket(@PathVariable("authtoken") String authtoken, String ticketCode, String mobileNumber, String emailId) throws Exception {
		List<TicketIO> transactionIOList = new ArrayList<TicketIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TicketDTO ticket = new TicketDTO();
			ticket.setCode(StringUtil.isNotNull(ticketCode) ? ticketCode.trim() : Text.EMPTY);
			ticket.setPassengerMobile(StringUtil.isNotNull(mobileNumber) ? mobileNumber.trim() : Text.EMPTY);
			ticket.setPassengerEmailId(StringUtil.isNotNull(emailId) ? emailId.trim() : Text.EMPTY);

			List<TicketDTO> findTicketList = ticketService.findTicket(authDTO, ticket);
			for (TicketDTO ticketDTO : findTicketList) {

				TicketIO ticketIO = new TicketIO();
				ticketIO.setTicketCode(ticketDTO.getCode());
				ticketIO.setBookingCode(ticketDTO.getBookingCode());
				ticketIO.setRelatedTicketCode(ticketDTO.getRelatedTicketCode());
				StationIO fromStationIO = new StationIO();
				StationIO toStationIO = new StationIO();

				fromStationIO.setCode(ticketDTO.getFromStation().getCode());
				fromStationIO.setName(ticketDTO.getFromStation().getName());

				toStationIO.setCode(ticketDTO.getToStation().getCode());
				toStationIO.setName(ticketDTO.getToStation().getName());
				ticketIO.setFromStation(fromStationIO);
				ticketIO.setToStation(toStationIO);

				// Mapping Operator Details
				OperatorIO operatorIO = new OperatorIO();
				operatorIO.setCode(authDTO.getNamespace().getCode());
				operatorIO.setName(authDTO.getNamespace().getName());
				ticketIO.setOperator(operatorIO);

				ticketIO.setTravelDate(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes()).format("YYYY-MM-DD hh:mm:ss"));
				ticketIO.setTripCode(ticketDTO.getTripDTO().getCode());
				ticketIO.setTripStageCode(ticketDTO.getTripDTO().getStage().getCode());
				ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());
				ticketIO.setPassegerEmailId(ticketDTO.getPassengerEmailId());
				ticketIO.setJourneyType(ticketDTO.getJourneyType().getCode());
				ticketIO.setTransactionDate(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
				ticketIO.setActiveFlag(ticketDTO.getActiveFlag());
				ticketIO.setDeviceMedium(ticketDTO.getDeviceMedium().getCode());

				UserIO userIO = new UserIO();
				if (ticketDTO.getTicketUser() != null) {
					userIO.setName(ticketDTO.getTicketUser().getName());
				}
				ticketIO.setUser(userIO);

				TicketStatusIO ticketStatus = new TicketStatusIO();
				ticketStatus.setCode(ticketDTO.getTicketStatus().getCode());
				ticketStatus.setName(ticketDTO.getTicketStatus().getDescription());
				ticketIO.setTicketStatus(ticketStatus);

				transactionIOList.add(ticketIO);
			}
		}
		return ResponseIO.success(transactionIOList);
	}

	@RequestMapping(value = "/phonebooking/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TicketIO> ConfirmPhoneBooking(@PathVariable("authtoken") String authtoken, @RequestBody OrderDetailsIO orderDetails, String transactionMode) throws Exception {
		checkConcurrentRequests(orderDetails.getTicketCode());
		TicketIO ticketIO = new TicketIO();
		try {
			AuthDTO authDTO = authService.getAuthDTO(authtoken);
			if (authDTO != null) {
				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setCode(orderDetails.getTicketCode());
				ticketDTO.setPassengerMobile(orderDetails.getPassengerMobile());
				ticketDTO.setOverideFlag(orderDetails.getOverrideFlag());
				ticketDTO.setRemarks(orderDetails.getRemarks());
				ticketDTO.setTransactionMode(StringUtil.isNotNull(transactionMode) ? TransactionModeEM.getTransactionModeEM(transactionMode) : null);
				ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(orderDetails.getJourneyType()));
				if (orderDetails.getBoardingPoint() != null) {
					StationPointDTO pointDTO = new StationPointDTO();
					pointDTO.setCode(orderDetails.getBoardingPoint().getCode());
					ticketDTO.setBoardingPoint(pointDTO);
				}
				if (orderDetails.getDroppingPoint() != null) {
					StationPointDTO pointDTO = new StationPointDTO();
					pointDTO.setCode(orderDetails.getDroppingPoint().getCode());
					ticketDTO.setDroppingPoint(pointDTO);
				}
				List<TicketDetailsDTO> passDetails = new ArrayList<TicketDetailsDTO>();
				for (TicketDetailsIO detailsIO : orderDetails.getTicketDetails()) {
					TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
					ticketDetailsDTO.setSeatCode(detailsIO.getSeatCode());
					if (detailsIO.getPassengerAge() != 0) {
						ticketDetailsDTO.setPassengerAge(detailsIO.getPassengerAge());
					}
					if (StringUtil.isNotNull(detailsIO.getPassengerName())) {
						ticketDetailsDTO.setPassengerName(detailsIO.getPassengerName());
						ticketDetailsDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(detailsIO.getPassengerGendar()));
					}
					passDetails.add(ticketDetailsDTO);
				}
				
				TicketExtraDTO extraDTO = new TicketExtraDTO();
				extraDTO.setAdditionalAttributes(orderDetails.getAdditionalAttributes() != null ? orderDetails.getAdditionalAttributes() : new HashMap<>());
				ticketDTO.setTicketExtra(extraDTO);

				// Permission check
				List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
				Eventlist.add(MenuEventEM.BOOKING_PHONE_ALL_USER_TCK);
				MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, Eventlist);
				if (menuEventDTO != null && menuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
					ticketDTO.setOverideFlag(true);
				}

				if (StringUtil.isNotNull(orderDetails.getOfflineUserCode())) {
					UserDTO user = new UserDTO();
					user.setCode(orderDetails.getOfflineUserCode());
					ticketDTO.setTicketForUser(user);
				}

				ticketDTO.setMyAccountFlag(orderDetails.getBookByMyAccountFlag());
				ticketDTO.setTicketDetails(passDetails);
				ticketDTO = confirmSeatsService.confirmPhoneBooking(authDTO, ticketDTO);

				TicketStatusIO ticketStatus = new TicketStatusIO();
				ticketStatus.setCode(ticketDTO.getTicketStatus().getCode());
				ticketStatus.setName(ticketDTO.getTicketStatus().getDescription());
				ticketIO.setTicketStatus(ticketStatus);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw e;
		}
		finally {
			releaseConcurrentRequests(orderDetails.getTicketCode());
		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/phonebooking/cancel", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> CancelPhoneBooking(@PathVariable("authtoken") String authtoken, @RequestBody OrderDetailsIO orderDetails) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(orderDetails.getTicketCode());
			boolean isOverideFlag = getPrivilege(authDTO, MenuEventEM.BOOKING_CANCEL_OVERRIDE_AMOUNT);
			ticketDTO.setOverideFlag(isOverideFlag);
			List<TicketDetailsDTO> passDetails = new ArrayList<TicketDetailsDTO>();
			for (TicketDetailsIO detailsIO : orderDetails.getTicketDetails()) {
				TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
				ticketDetailsDTO.setSeatCode(detailsIO.getSeatCode());
				passDetails.add(ticketDetailsDTO);
			}
			ticketDTO.setTicketDetails(passDetails);
			cancelTicketService.cancelPhoneBooking(authDTO, ticketDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/phonebooking/{fromDate}/{toDate}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TicketIO>> getPhoneBookingTickets(@PathVariable("authtoken") String authtoken, @PathVariable("fromDate") DateTime fromDate, @PathVariable("toDate") DateTime toDate) throws Exception {
		List<TicketIO> list = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		List<TicketDTO> dtoList = new ArrayList<TicketDTO>();
		try {
			dtoList = ticketService.getPhoneBookingTickets(authDTO, fromDate, toDate);
			for (TicketDTO dto : dtoList) {
				TicketIO ticket = new TicketIO();
				ticket.setCode(dto.getCode());
				ticket.setPassegerMobleNo(dto.getPassengerMobile());
				ticket.setTripCode(dto.getTripDTO().getCode());
				ticket.setTripStageCode(dto.getTripDTO().getStage().getCode());
				ticket.setScheduleName(dto.getTripDTO().getSchedule().getName());
				ticket.setTripName(dto.getTripDTO().getName());
				ticket.setServiceNo(dto.getTripDTO().getSchedule().getServiceNumber());
				ticket.setRemarks(dto.getRemarks());
				List<TicketDetailsIO> ticketDetails = new ArrayList<TicketDetailsIO>();
				for (TicketDetailsDTO tckDetDTO : dto.getTicketDetails()) {
					TicketDetailsIO detailsIO = new TicketDetailsIO();
					detailsIO.setSeatName(tckDetDTO.getSeatName());
					detailsIO.setPassengerName(tckDetDTO.getPassengerName());
					detailsIO.setSeatFare(tckDetDTO.getSeatFare());
					detailsIO.setPassengerGendar(tckDetDTO.getSeatGendar().getName());
					SeatStatusIO seatStatus = new SeatStatusIO();
					seatStatus.setCode(tckDetDTO.getTicketStatus().getCode());
					seatStatus.setName(tckDetDTO.getTicketStatus().getDescription());
					detailsIO.setSeatStatus(seatStatus);
					BaseIO travelStatus = new BaseIO();
					travelStatus.setCode(tckDetDTO.getTravelStatus() != null ? tckDetDTO.getTravelStatus().getCode() : null);
					travelStatus.setName(tckDetDTO.getTravelStatus() != null ? tckDetDTO.getTravelStatus().getName() : null);
					detailsIO.setTravelStatus(travelStatus);
					ticketDetails.add(detailsIO);
				}
				ticket.setTicketDetails(ticketDetails);
				ticket.setTransactionDate(dto.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
				ticket.setTravelDate(dto.getTripDate().format("YYYY-MM-DD"));

				TicketExtraIO ticketExtra = new TicketExtraIO();
				if (dto.getTicketExtra() != null) {
					ticketExtra.setSequenceNumber(dto.getTicketExtra().getSequenceNumber());
					ticketExtra.setLinkPay(dto.getTicketExtra().getLinkPay());
					ticketExtra.setOfflineTicketCode(dto.getTicketExtra().getOfflineTicketCode());
					ticketExtra.setReleaseAt(DateUtil.convertDateTime(dto.getTicketExtra().getReleaseAt()));
				}
				ticket.setTicketExtra(ticketExtra);

				if (dto.getTripDTO() != null && dto.getTripDTO().getSchedule() != null && dto.getTripDTO().getSchedule().getStationList() != null) {
					Map<String, ScheduleStationDTO> sequenceMap = new HashMap<>();
					for (ScheduleStationDTO scheduleStationDTO : dto.getTripDTO().getSchedule().getStationList()) {
						if (sequenceMap.get("MIN") == null || sequenceMap.get("MIN").getStationSequence() >= scheduleStationDTO.getStationSequence()) {
							scheduleStationDTO.setStation(stationService.getStation(scheduleStationDTO.getStation()));
							sequenceMap.put("MIN", scheduleStationDTO);
						}
						if (sequenceMap.get("MAX") == null || sequenceMap.get("MAX").getStationSequence() <= scheduleStationDTO.getStationSequence()) {
							scheduleStationDTO.setStation(stationService.getStation(scheduleStationDTO.getStation()));
							sequenceMap.put("MAX", scheduleStationDTO);
						}
					}
					ticket.setTripName(sequenceMap.get("MIN").getStation().getName() + " - " + sequenceMap.get("MAX").getStation().getName());
					ticket.setTripTime(DateUtil.addMinituesToDate(dto.getTripDate(), sequenceMap.get("MIN").getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					ticket.setScheduleName(dto.getTripDTO().getSchedule().getName());
				}
				if (dto.getTicketUser() != null && dto.getTicketUser().getId() != 0) {
					UserIO userIO = new UserIO();
					userIO.setCode(dto.getTicketUser().getCode());
					userIO.setName(dto.getTicketUser().getName());
					ticket.setUser(userIO);
				}
				StationIO fromstationIO = new StationIO();
				StationIO tostationIO = new StationIO();
				StationPointIO boardingIO = new StationPointIO();

				fromstationIO.setCode(dto.getFromStation().getCode());
				fromstationIO.setName(dto.getFromStation().getName());
				boardingIO.setCode(dto.getBoardingPoint().getCode());
				boardingIO.setName(dto.getBoardingPoint().getName());
				fromstationIO.setStationPoints(boardingIO);
				ticket.setFromStation(fromstationIO);

				tostationIO.setCode(dto.getToStation().getCode());
				tostationIO.setName(dto.getToStation().getName());
				ticket.setToStation(tostationIO);

				list.add(ticket);
			}
			// Sorting
			Comparator<TicketIO> comp = new BeanComparator("transactionDate");
			Collections.sort(list, comp);
			Collections.reverse(list);

		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/refund/{statusCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TicketRefundIO>> getRefundTickets(@PathVariable("authtoken") String authtoken, @PathVariable("statusCode") String statusCode) throws Exception {
		List<TicketRefundIO> list = new ArrayList<>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TicketRefundDTO refundDTO = new TicketRefundDTO();
			refundDTO.setRefundStatus(RefundStatusEM.getRefundStatusEM(statusCode));
			ticketService.getRefundTicket(authDTO, refundDTO);
			for (TicketRefundDTO refund : refundDTO.getList()) {
				TicketRefundIO refundIO = new TicketRefundIO();
				refundIO.setTicketCode(refund.getTicketCode());
				refundIO.setBookingCode(refund.getBookingCode());
				refundIO.setTransactionCode(refund.getTransactionCode());
				refundIO.setBookedAt(refund.getBookedAt());
				refundIO.setCanncelledAt(refund.getCanncelledAt());
				refundIO.setPassegerEmailId(refund.getPassegerEmailId());
				refundIO.setPassegerMobleNo(refund.getPassegerMobleNo());
				refundIO.setTravelDateTime(refund.getTripDateTime());
				refundIO.setTotalRefundAmount(refund.getTotalRefundAmount());
				refundIO.setSeatCount(refund.getSeatCount());
				StationIO fromstationIO = new StationIO();
				StationIO tostationIO = new StationIO();
				fromstationIO.setCode(refund.getFromStation().getCode());
				fromstationIO.setName(refund.getFromStation().getName());
				refundIO.setFromStation(fromstationIO);

				tostationIO.setCode(refund.getToStation().getCode());
				tostationIO.setName(refund.getToStation().getName());
				refundIO.setToStation(tostationIO);
				list.add(refundIO);
			}
		}
		return ResponseIO.success(list);
	}

	@RequestMapping(value = "/refund/status/update/{statusCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateRefundTickets(@PathVariable("authtoken") String authtoken, @PathVariable("statusCode") String statusCode, String transactionCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TicketRefundDTO refundDTO = new TicketRefundDTO();
			refundDTO.setTransactionCode(transactionCode);
			refundDTO.setRefundStatus(RefundStatusEM.getRefundStatusEM(statusCode));
			ticketService.updateRefundTicket(authDTO, refundDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/phone/auto/release/{tripCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TicketIO>> getAutoReleaseTicket(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode) throws Exception {
		List<TicketIO> ticketIOList = new ArrayList<TicketIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCode);
		List<TicketDTO> ticketReleaseList = autoReleaseService.releaseTicket(authDTO, tripDTO);
		for (TicketDTO dto : ticketReleaseList) {
			TicketIO ticket = new TicketIO();
			ticket.setCode(dto.getCode());
			List<TicketDetailsIO> ticketDetails = new ArrayList<TicketDetailsIO>();
			for (TicketDetailsDTO tckDetDTO : dto.getTicketDetails()) {
				TicketDetailsIO detailsIO = new TicketDetailsIO();
				detailsIO.setSeatCode(tckDetDTO.getSeatCode());
				detailsIO.setSeatName(tckDetDTO.getSeatName());
				detailsIO.setPassengerName(tckDetDTO.getPassengerName());
				SeatStatusIO seatStatus = new SeatStatusIO();
				seatStatus.setCode(tckDetDTO.getTicketStatus().getCode());
				seatStatus.setName(tckDetDTO.getTicketStatus().getDescription());
				detailsIO.setSeatStatus(seatStatus);
				ticketDetails.add(detailsIO);
			}
			ticket.setTicketDetails(ticketDetails);
		}
		return ResponseIO.success(ticketIOList);
	}

	@RequestMapping(value = "/trip/{tripCode}/cancel/initiate/{ticketCode}/seats", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TicketIO> tripCancelInitiate(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("ticketCode") String ticketCode, String seats, @RequestBody Map<String, String> dataMap) throws Exception {
		TicketIO ticketIO = new TicketIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);
			ticketDTO.setTripDTO(tripDTO);

			List<TicketDetailsDTO> ticketDetails = new ArrayList<>();
			String[] seatCodes = seats.trim().split(",");
			for (int count = 0; count < seatCodes.length; count++) {
				TicketDetailsDTO tckDetails = new TicketDetailsDTO();
				tckDetails.setSeatCode(seatCodes[count].trim());
				ticketDetails.add(tckDetails);
			}
			ticketDTO.setTicketDetails(ticketDetails);

			cancelLogger.info("The ticket code for Trip cancel seats : " + ticketDTO.getCode() + " " + authDTO.getUser().getUsername());
			cancelTicketService.tripCancelInitiate(authDTO, ticketDTO, dataMap);
			ticketIO.setCode(ticketDTO.getCode());
			ticketIO.setActiveFlag(1);

		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/trip/{tripCode}/cancel/acknowledge/update/{ticketCode}/seats", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateTripCancelAcknowledge(@PathVariable("authtoken") String authtoken, @PathVariable("tripCode") String tripCode, @PathVariable("ticketCode") String ticketCode, String seats, String tripStatusCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripCode);
			tripDTO.setTripStatus(TripStatusEM.getTripStatusEM(tripStatusCode));
			ticketDTO.setTripDTO(tripDTO);

			List<TicketDetailsDTO> ticketDetails = new ArrayList<>();
			String[] seatCodes = seats.trim().split(",");
			for (int count = 0; count < seatCodes.length; count++) {
				TicketDetailsDTO tckDetails = new TicketDetailsDTO();
				tckDetails.setSeatCode(seatCodes[count].trim());
				ticketDetails.add(tckDetails);
			}
			ticketDTO.setTicketDetails(ticketDetails);

			cancelLogger.info("The ticket code for Trip cancel seats : " + ticketDTO.getCode() + " " + authDTO.getUser().getUsername());
			cancelTicketService.updateTripCancelAcknowledge(authDTO, ticketDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{ticketCode}/travel/{ticketStatusCode}/change", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> changeTicketStatus(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String remarks) throws Exception {
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{ticketCode}/travel/status/{travelStatus}/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> updateTicketTravelStatus(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, @PathVariable("travelStatus") String travelStatus, String seatCodeList, String remarks) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		List<TicketDetailsDTO> ticketDetails = new ArrayList<TicketDetailsDTO>();
		for (int i = 0; i < seatCodeList.split(",").length; i++) {
			TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
			ticketDetailsDTO.setSeatCode(seatCodeList.split(",")[i]);
			ticketDetailsDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(travelStatus));
			ticketDetailsDTO.setActiveFlag(Numeric.ONE_INT);
			ticketDetails.add(ticketDetailsDTO);
		}
		ticketDTO.setTicketDetails(ticketDetails);
		ticketDTO.setRemarks(remarks);
		ticketService.updateTravelStatus(authDTO, ticketDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/progress/aftertripdepaturetime", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TicketIO>> getTicketAfterTripDepatureTime(@PathVariable("authtoken") String authtoken, String scheduleCodes) throws Exception {
		List<TicketIO> ticketIOList = new ArrayList<TicketIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<ScheduleDTO> scheduleList = new ArrayList<>();
			if (StringUtil.isNotNull(scheduleCodes)) {
				for (String scheduleCode : scheduleCodes.split(Text.COMMA)) {
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setCode(scheduleCode.trim());
					scheduleList.add(scheduleDTO);
				}
			}
			List<TicketDTO> list = ticketHelperService.getAllTicketAfterTripTime(authDTO, scheduleList);

			try {
				for (TicketDTO ticketDTO : list) {
					TicketIO ticketIO = new TicketIO();
					StationIO fromStationIO = new StationIO();
					StationIO toStationIO = new StationIO();
					StationPointIO boardingPointIO = new StationPointIO();
					List<StationPointIO> boardingList = new ArrayList<StationPointIO>();

					// Mapping from station and boarding point
					boardingPointIO.setCode(ticketDTO.getBoardingPoint().getCode());
					boardingPointIO.setName(ticketDTO.getBoardingPoint().getName());
					boardingPointIO.setAddress(ticketDTO.getBoardingPoint().getAddress());
					boardingPointIO.setLandmark(ticketDTO.getBoardingPoint().getLandmark());
					boardingPointIO.setNumber(ticketDTO.getBoardingPoint().getNumber());
					boardingPointIO.setLongitude(ticketDTO.getBoardingPoint().getLongitude());
					boardingPointIO.setLatitude(ticketDTO.getBoardingPoint().getLatitude());
					boardingPointIO.setDateTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
					boardingList.add(boardingPointIO);

					fromStationIO.setCode(ticketDTO.getFromStation().getCode());
					fromStationIO.setName(ticketDTO.getFromStation().getName());
					fromStationIO.setStationPoint(boardingList);

					toStationIO.setCode(ticketDTO.getToStation().getCode());
					toStationIO.setName(ticketDTO.getToStation().getName());
					ticketIO.setFromStation(fromStationIO);
					ticketIO.setToStation(toStationIO);

					if (ticketDTO.getTicketUser() != null) {
						UserIO userIO = new UserIO();
						userIO.setName(ticketDTO.getTicketUser().getName());
						userIO.setCode(ticketDTO.getTicketUser().getCode());
						ticketIO.setUser(userIO);
					}

					TicketStatusIO ticketStatus = new TicketStatusIO();
					ticketStatus.setCode(ticketDTO.getTicketStatus().getCode());
					ticketStatus.setName(ticketDTO.getTicketStatus().getDescription());
					ticketIO.setTicketStatus(ticketStatus);

					ticketIO.setTravelDate(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes()).format("YYYY-MM-DD hh:mm:ss"));
					ticketIO.setTripCode(ticketDTO.getTripDTO().getCode());
					ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());
					ticketIO.setPassegerEmailId(ticketDTO.getPassengerEmailId());
					ticketIO.setServiceNo(ticketDTO.getServiceNo());
					ticketIO.setDeviceMedium(ticketDTO.getDeviceMedium().getCode());
					ticketIO.setRemarks(ticketDTO.getRemarks());
					ticketIO.setCode(ticketDTO.getCode());
					List<TicketDetailsIO> ticketDetails = new ArrayList<TicketDetailsIO>();
					for (TicketDetailsDTO detailsDTO : ticketDTO.getTicketDetails()) {
						TicketDetailsIO tickDetailsIO = new TicketDetailsIO();
						tickDetailsIO.setSeatName(detailsDTO.getSeatName());
						tickDetailsIO.setPassengerName(detailsDTO.getPassengerName());
						ticketDetails.add(tickDetailsIO);
					}
					ticketIO.setTicketDetails(ticketDetails);
					BusIO bus = new BusIO();
					bus.setBusType(busService.getBusCategoryByCode(ticketDTO.getTripDTO().getBus().getCategoryCode()));
					ticketIO.setBus(bus);
					ticketIO.setTransactionDate(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
					ticketIO.setActiveFlag(ticketDTO.getActiveFlag());
					ticketIO.setTripTime(ticketDTO.getTripDTO().getTripDateTimeV2().format("YYYY-MM-DD hh:mm:ss"));
					ticketIOList.add(ticketIO);

				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			// Sorting
			Comparator<TicketIO> comp = new BeanComparator("transactionDate");
			Collections.sort(ticketIOList, comp);
		}
		return ResponseIO.success(ticketIOList);
	}

	@RequestMapping(value = "/progress/aftertriptime/{ticketCode}/acknowledge", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> acknowledgeTicketAfterTripTime(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		ticketHelperService.acknowledgeTicketAfterTripTime(authDTO, ticketCode);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/transaction/failure/pending/update", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<PaymentTransactionIO> failureToPendingOrder(@PathVariable("authtoken") String authtoken, String ticketCode) throws Exception {
		PaymentTransactionIO paymentTransactionIO = new PaymentTransactionIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			PendingOrderDTO dto = new PendingOrderDTO();
			dto.setOrderCode(ticketCode);
			pendingOrderService.Update(authDTO, dto);
		}
		return ResponseIO.success(paymentTransactionIO);
	}

	@RequestMapping(value = "/{bookingCode}/{ticketCode}/tentative/release", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> getReleaseTentative(@PathVariable("authtoken") String authtoken, @PathVariable("bookingCode") String bookingCode, @PathVariable("ticketCode") String ticketCode, String reason) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TicketDTO dto = new TicketDTO();
			dto.setBookingCode(bookingCode);
			dto.setCode(ticketCode);
			dto.setRemarks(reason);
			ticketService.releaseTentativeBlockTicket(authDTO, dto);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/generate/linkpay/{ticketCode}", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> generateLinkPay(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String reason) throws Exception {
		BaseIO extras = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TicketDTO ticket = new TicketDTO();
		ticket.setCode(ticketCode);

		TicketExtraDTO ticketExtra = new TicketExtraDTO();
		ticketExtra.setLinkPay(Text.TRUE_STRING);
		ticket.setTicketExtra(ticketExtra);

		TicketExtraDTO extraDTO = ticketService.generateLinkPayV2(authDTO, ticket);
		extras.setCode(extraDTO.getLinkPay());
		return ResponseIO.success();
	}

	@RequestMapping(value = "/progress/notboard", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TicketIO>> getTicketNotBoarded(@PathVariable("authtoken") String authtoken) throws Exception {
		List<TicketIO> ticketIOList = new ArrayList<TicketIO>();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			List<TicketDTO> list = ticketHelperService.getInProcegressNotBoardedTicket(authDTO);

			try {
				for (TicketDTO ticketDTO : list) {
					for (TicketDetailsDTO detailsDTO : ticketDTO.getTicketDetails()) {
						TicketIO ticketIO = new TicketIO();
						StationIO fromStationIO = new StationIO();
						StationIO toStationIO = new StationIO();
						StationPointIO boardingPointIO = new StationPointIO();
						List<StationPointIO> boardingList = new ArrayList<StationPointIO>();

						// Mapping from station and boarding point
						boardingPointIO.setCode(ticketDTO.getBoardingPoint().getCode());
						boardingPointIO.setName(ticketDTO.getBoardingPoint().getName());
						boardingPointIO.setAddress(ticketDTO.getBoardingPoint().getAddress());
						boardingPointIO.setLandmark(ticketDTO.getBoardingPoint().getLandmark());
						boardingPointIO.setNumber(ticketDTO.getBoardingPoint().getNumber());
						boardingPointIO.setLongitude(ticketDTO.getBoardingPoint().getLongitude());
						boardingPointIO.setLatitude(ticketDTO.getBoardingPoint().getLatitude());
						boardingPointIO.setDateTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
						boardingList.add(boardingPointIO);

						fromStationIO.setCode(ticketDTO.getFromStation().getCode());
						fromStationIO.setName(ticketDTO.getFromStation().getName());
						fromStationIO.setStationPoint(boardingList);

						toStationIO.setCode(ticketDTO.getToStation().getCode());
						toStationIO.setName(ticketDTO.getToStation().getName());
						ticketIO.setFromStation(fromStationIO);
						ticketIO.setToStation(toStationIO);

						if (ticketDTO.getTicketUser() != null) {
							UserIO userIO = new UserIO();
							userIO.setName(ticketDTO.getTicketUser().getName());
							userIO.setCode(ticketDTO.getTicketUser().getCode());
							ticketIO.setUser(userIO);
						}

						TicketStatusIO ticketStatus = new TicketStatusIO();
						ticketStatus.setCode(ticketDTO.getTicketStatus().getCode());
						ticketStatus.setName(ticketDTO.getTicketStatus().getDescription());
						ticketIO.setTicketStatus(ticketStatus);

						ticketIO.setTravelDate(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes()).format("YYYY-MM-DD hh:mm:ss"));
						ticketIO.setTripCode(ticketDTO.getTripDTO().getCode());
						ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());
						ticketIO.setPassegerEmailId(ticketDTO.getPassengerEmailId());
						ticketIO.setServiceNo(ticketDTO.getServiceNo());
						ticketIO.setDeviceMedium(ticketDTO.getDeviceMedium().getCode());
						ticketIO.setRemarks(ticketDTO.getRemarks());
						ticketIO.setCode(ticketDTO.getCode());

						// Ticket Details
						List<TicketDetailsIO> ticketDetails = new ArrayList<TicketDetailsIO>();
						TicketDetailsIO tickDetailsIO = new TicketDetailsIO();
						tickDetailsIO.setSeatCode(detailsDTO.getSeatCode());
						tickDetailsIO.setSeatName(detailsDTO.getSeatName());
						tickDetailsIO.setPassengerName(detailsDTO.getPassengerName());

						BaseIO travelStatus = new BaseIO();
						travelStatus.setCode(detailsDTO.getTravelStatus().getCode());
						travelStatus.setName(detailsDTO.getTravelStatus().getName());
						tickDetailsIO.setTravelStatus(travelStatus);

						ticketDetails.add(tickDetailsIO);
						ticketIO.setTicketDetails(ticketDetails);

						BusIO bus = new BusIO();
						bus.setBusType(busService.getBusCategoryByCode(ticketDTO.getTripDTO().getBus().getCategoryCode()));
						ticketIO.setBus(bus);
						ticketIO.setTransactionDate(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
						ticketIO.setActiveFlag(ticketDTO.getActiveFlag());
						ticketIO.setTripTime(ticketDTO.getTripDTO().getTripDateTimeV2().format("YYYY-MM-DD hh:mm:ss"));
						ticketIOList.add(ticketIO);
					}
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			// Sorting
			Comparator<TicketIO> comp = new BeanComparator("transactionDate");
			Collections.sort(ticketIOList, comp);
		}
		return ResponseIO.success(ticketIOList);
	}

	@RequestMapping(value = "/push/inventory/refresh/event", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> pushInventoryChangesEvent(@PathVariable("authtoken") String authtoken, String ticketCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		authDTO.getAdditionalAttribute().put("activity_type", "manual-push");
		ticketService.pushInventoryChangesEvent(authDTO, ticketDTO);
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{ticketCode}/extras", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TicketExtraIO> getTicketExtra(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		TicketExtraDTO ticketExtraDTO = ticketService.getTicketExtra(authDTO, ticketDTO);

		TicketExtraIO ticketExtra = new TicketExtraIO();
		ticketExtra.setBlockReleaseMinutes(ticketExtraDTO.getBlockReleaseMinutes());
		ticketExtra.setLinkPay(ticketExtraDTO.getLinkPay());
		ticketExtra.setSequenceNumber(ticketExtraDTO.getSequenceNumber());
		ticketExtra.setOfflineTicketCode(ticketExtraDTO.getOfflineTicketCode());

		return ResponseIO.success(ticketExtra);
	}

	public static synchronized boolean releaseConcurrentRequests(String ticketCode) {
		if (ConcurrentRequests.get(ticketCode) != null) {
			if (ConcurrentRequests.get(ticketCode) > 0) {
				ConcurrentRequests.put(ticketCode, ConcurrentRequests.get(ticketCode) - 1);
			}
		}
		return true;
	}

	public static synchronized boolean checkConcurrentRequests(String ticketCode) {
		if (ConcurrentRequests.get(ticketCode) != null && ConcurrentRequests.get(ticketCode) > 0) {
			throw new ServiceException(ErrorCode.REACHED_MAX_CONCURRENT_REQUESTS);
		}
		if (ConcurrentRequests.get(ticketCode) != null) {
			ConcurrentRequests.put(ticketCode, ConcurrentRequests.get(ticketCode) + 1);
		}
		else {
			ConcurrentRequests.put(ticketCode, 1);
		}
		return true;
	}
}