package org.in.com.controller.app;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.WordUtils;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.app.io.AddonTypeIO;
import org.in.com.controller.app.io.BookingIO;
import org.in.com.controller.app.io.BusIO;
import org.in.com.controller.app.io.OperatorIO;
import org.in.com.controller.app.io.OrderDetailsIO;
import org.in.com.controller.app.io.OrderIO;
import org.in.com.controller.app.io.PaymentModeIO;
import org.in.com.controller.app.io.PaymentTransactionIO;
import org.in.com.controller.app.io.ResponseIO;
import org.in.com.controller.app.io.SeatStatusIO;
import org.in.com.controller.app.io.StationIO;
import org.in.com.controller.app.io.StationPointIO;
import org.in.com.controller.app.io.TicketAddonsDetailsIO;
import org.in.com.controller.app.io.TicketDetailsIO;
import org.in.com.controller.app.io.TicketIO;
import org.in.com.controller.app.io.TicketStatusIO;
import org.in.com.controller.app.io.TicketTransactionIO;
import org.in.com.controller.app.io.TransactionModeIO;
import org.in.com.controller.app.io.TransactionTypeIO;
import org.in.com.controller.app.io.UserTransactionIO;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.CancellationPolicyIO;
import org.in.com.controller.web.io.CancellationTermIO;
import org.in.com.controller.web.io.NamespaceTaxIO;
import org.in.com.controller.web.io.PaymentGatewayPartnerIO;
import org.in.com.controller.web.io.TicketExtraIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.CancellationPolicyDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.OrderInitRequestDTO;
import org.in.com.dto.OrderInitStatusDTO;
import org.in.com.dto.PaymentGatewayPartnerDTO;
import org.in.com.dto.PaymentGatewayScheduleDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketTransactionDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BlockSeatsService;
import org.in.com.service.BusService;
import org.in.com.service.CancelTicketService;
import org.in.com.service.CancellationTermsService;
import org.in.com.service.ConfirmSeatsService;
import org.in.com.service.PaymentMerchantGatewayScheduleService;
import org.in.com.service.SearchService;
import org.in.com.service.StationService;
import org.in.com.service.TicketFailureService;
import org.in.com.service.TicketService;
import org.in.com.service.pg.PaymentRequestService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.GSTINValidator;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import hirondelle.date4j.DateTime;

//@Controller
//@RequestMapping("/api/3.0/app/{authtoken}/commerce")
public class AppTransactionController extends BaseController {
	@Autowired
	CancellationTermsService termsService;
	@Autowired
	SearchService searchService;
	@Autowired
	StationService stationService;
	@Autowired
	TicketService ticketService;
	@Autowired
	CancelTicketService cancelTicketService;
	@Autowired
	PaymentMerchantGatewayScheduleService gatewayScheduleService;
	@Autowired
	BlockSeatsService blockSeatsService;
	@Autowired
	ConfirmSeatsService confirmSeatsService;
	@Autowired
	PaymentRequestService paymentRequestService;
	@Autowired
	BusService busService;
	@Autowired
	TicketFailureService ticketFailureService;

	private static final Logger logger = LoggerFactory.getLogger(AppTransactionController.class);
	private static final Logger cancelLogger = LoggerFactory.getLogger("org.in.com.controller.cancelticket");

	@RequestMapping(value = "/ticket/{ticketCode}", method = RequestMethod.POST, produces = {})
	@ResponseBody
	public ResponseIO<TicketIO> showTicket(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String passagerEmailId) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		logger.info("Show ticket-" + ticketCode + " email-" + passagerEmailId);
		TicketIO ticketIO = new TicketIO();
		if (authDTO != null) {
			TicketDTO ticketDTO = new TicketDTO();
			if (StringUtil.isValidMobileNumber(ticketCode)) {
				ticketDTO.setPassengerMobile(ticketCode);
			}
			else {
				ticketDTO.setCode(ticketCode);
			}
			ticketDTO.setPassengerEmailId(passagerEmailId);
			ticketService.showTicket(authDTO, ticketDTO);

			StationIO fromStationIO = new StationIO();
			StationIO toStationIO = new StationIO();
			StationPointIO boardingPointIO = new StationPointIO();
			StationPointIO droppingPointIO = new StationPointIO();
			List<StationPointIO> boardingList = new ArrayList<StationPointIO>();
			List<StationPointIO> alightingList = new ArrayList<StationPointIO>();
			List<TicketDetailsIO> ticketDetailsIO = new ArrayList<TicketDetailsIO>();
			CancellationTermIO cancellationTermsIO = new CancellationTermIO();
			List<CancellationPolicyIO> cancelPolicyList = new ArrayList<CancellationPolicyIO>();

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
					travelStatus.setCode(dto.getTravelStatus().getCode());
					travelStatus.setName(dto.getTravelStatus().getName());
					tickDetailsIO.setTravelStatus(travelStatus);
					tickDetailsIO.setPassengerName(dto.getPassengerName());
					tickDetailsIO.setPassengerAge(dto.getPassengerAge());
					tickDetailsIO.setPassengerGender(dto.getSeatGendar().getCode());
					tickDetailsIO.setSeatFare(dto.getSeatFare());
					tickDetailsIO.setServiceTax(dto.getAcBusTax());
					tickDetailsIO.setCancellationCharges(dto.getCancellationCharges());
					tickDetailsIO.setCancellationChargeTax(dto.getCancellationChargeTax());
					tickDetailsIO.setRefundAmount(dto.getRefundAmount());
					ticketDetailsIO.add(tickDetailsIO);
				}
			}
			// Ticket Addons Details
			if (ticketDTO.getTicketAddonsDetails() != null) {
				List<TicketAddonsDetailsIO> ticketAddonsDetailsIO = new ArrayList<TicketAddonsDetailsIO>();
				for (TicketAddonsDetailsDTO dto : ticketDTO.getTicketAddonsDetails()) {
					TicketAddonsDetailsIO addonsDetailsIO = new TicketAddonsDetailsIO();
					addonsDetailsIO.setSeatCode(dto.getSeatCode());

					SeatStatusIO ticketStatus = new SeatStatusIO();
					ticketStatus.setCode(dto.getTicketStatus().getCode());
					ticketStatus.setName(dto.getTicketStatus().getDescription());
					addonsDetailsIO.setAddonStatus(ticketStatus);

					AddonTypeIO addonType = new AddonTypeIO();
					addonType.setCode(dto.getAddonsType().getCode());
					addonType.setName(dto.getAddonsType().getName());
					addonType.setCreditDebitFlag(dto.getAddonsType().getCreditDebitFlag());
					addonsDetailsIO.setAddonType(addonType);

					addonsDetailsIO.setValue(dto.getValue());
					ticketAddonsDetailsIO.add(addonsDetailsIO);
				}
				ticketIO.setTicketAddonsDetails(ticketAddonsDetailsIO);
			}

			List<CancellationPolicyDTO> tmpcancellationPolicyDTOs = ticketDTO.getCancellationTerm().getPolicyList();
			for (CancellationPolicyDTO cancellationPolicyDTO : tmpcancellationPolicyDTOs) {
				CancellationPolicyIO cancellPolicyIO = new CancellationPolicyIO();
				cancellPolicyIO.setFromValue(cancellationPolicyDTO.getFromValue());
				cancellPolicyIO.setToValue(cancellationPolicyDTO.getToValue());
				cancellPolicyIO.setDeductionAmount(cancellationPolicyDTO.getDeductionValue());
				cancellPolicyIO.setPercentageFlag(cancellationPolicyDTO.getPercentageFlag());
				cancellPolicyIO.setPolicyPattern(cancellationPolicyDTO.getPolicyPattern());

				cancellPolicyIO.setTerm(cancellationPolicyDTO.getTerm());
				cancellPolicyIO.setDeductionAmountTxt(cancellationPolicyDTO.getDeductionAmountTxt());
				cancellPolicyIO.setRefundAmountTxt(cancellationPolicyDTO.getRefundAmountTxt());
				cancellPolicyIO.setChargesTxt(cancellationPolicyDTO.getChargesTxt());
				cancelPolicyList.add(cancellPolicyIO);
			}
			cancellationTermsIO.setPolicyList(cancelPolicyList);
			cancellationTermsIO.setCode(ticketDTO.getCancellationTerm().getCode());
			cancellationTermsIO.setName(ticketDTO.getCancellationTerm().getName());
			cancellationTermsIO.setActiveFlag(ticketDTO.getCancellationTerm().getActiveFlag());

			ticketIO.setCode(ticketDTO.getCode());
			ticketIO.setBookingCode(ticketDTO.getBookingCode());
			ticketIO.setTravelDate(ticketDTO.getTripDateTime().format("YYYY-MM-DD hh:mm:ss"));
			// Ticket status
			TicketStatusIO ticketStatusIO = new TicketStatusIO();
			ticketStatusIO.setCode(ticketDTO.getTicketStatus().getCode());
			ticketStatusIO.setName(ticketDTO.getTicketStatus().getDescription());
			ticketIO.setTicketStatus(ticketStatusIO);
			ticketIO.setTravelTime(ticketDTO.getTripTime());
			ticketIO.setTripCode(ticketDTO.getTripDTO().getCode());
			ticketIO.setTripStageCode(ticketDTO.getTripDTO().getStage().getCode());
			ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());
			ticketIO.setPassegerEmailId(ticketDTO.getPassengerEmailId());
			ticketIO.setTotalFare(ticketDTO.getTotalFare());
			ticketIO.setServiceNo(ticketDTO.getServiceNo());
			ticketIO.setReportingTime(ticketDTO.getReportingTime());
			BusIO bus = new BusIO();
			bus.setDisplayName(ticketDTO.getTripDTO().getBus().getDisplayName());
			bus.setName(ticketDTO.getTripDTO().getBus().getName());
			bus.setCategoryCode(ticketDTO.getTripDTO().getBus().getCategoryCode());
			bus.setBusType(busService.getBusCategoryByCode(ticketDTO.getTripDTO().getBus().getCategoryCode()));
			ticketIO.setBus(bus);

			if (ticketDTO.getTax().getId() != 0) {
				NamespaceTaxIO tax = new NamespaceTaxIO();
				tax.setActiveFlag(ticketDTO.getTax().getActiveFlag());
				tax.setCode(ticketDTO.getTax().getCode());
				tax.setGstin(ticketDTO.getTax().getGstin());
				tax.setName(ticketDTO.getTax().getName());
				tax.setSacNumber(ticketDTO.getTax().getSacNumber());
				tax.setCgstValue(ticketDTO.getTax().getCgstValue());
				tax.setSgstValue(ticketDTO.getTax().getSgstValue());
				tax.setUgstValue(ticketDTO.getTax().getUgstValue());
				tax.setIgstValue(ticketDTO.getTax().getIgstValue());
				tax.setTradeName(ticketDTO.getTax().getTradeName());
				ticketIO.setTax(tax);
			}

			ticketIO.setJourneyType(ticketDTO.getJourneyType().getCode());
			ticketIO.setDeviceMedium(ticketDTO.getDeviceMedium().getCode());
			ticketIO.setTicketDetails(ticketDetailsIO);
			ticketIO.setCancellationTerms(cancellationTermsIO);
			ticketIO.setRemarks(ticketDTO.getRemarks());
			ticketIO.setTransactionDate(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));

			TicketExtraIO ticketExtra = new TicketExtraIO();
			if (ticketDTO.getTicketExtra() != null) {
				ticketExtra.setReleaseAt(DateUtil.convertDateTime(ticketDTO.getTicketExtra().getReleaseAt()));
				ticketExtra.setPhoneBookPaymentStatus(ticketDTO.getTicketExtra().getPhoneBookPaymentStatus());
			}
			ticketIO.setTicketExtra(ticketExtra);

			ticketIO.setActiveFlag(ticketDTO.getActiveFlag());
			if (ticketDTO.getTicketUser() != null) {
				UserIO userIO = new UserIO();
				userIO.setName(ticketDTO.getTicketUser().getName());
				userIO.setCode(ticketDTO.getTicketUser().getCode());
				ticketIO.setUser(userIO);
			}
			if (ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId()) {
				List<TicketTransactionIO> ticketTransactionList = new ArrayList<>();
				for (TicketTransactionDTO transactionDTO : ticketDTO.getTicketXaction().getList()) {
					TicketTransactionIO ticketTransIO = new TicketTransactionIO();

					TransactionTypeIO typeIO = new TransactionTypeIO();
					typeIO.setName(transactionDTO.getTransactionType().getName());
					typeIO.setCode(transactionDTO.getTransactionType().getCode());
					typeIO.setCreditDebitFlag(transactionDTO.getTransactionType().getCreditDebitFlag());
					ticketTransIO.setTransactionType(typeIO);

					TransactionModeIO modeIO = new TransactionModeIO();
					modeIO.setCode(transactionDTO.getTransactionMode().getCode());
					modeIO.setName(transactionDTO.getTransactionMode().getName());
					ticketTransIO.setTransactionMode(modeIO);

					PaymentTransactionIO paymentTransaction = new PaymentTransactionIO();
					paymentTransaction.setCode(transactionDTO.getVoucherGenerateStatus());
					ticketTransIO.setPaymentTransaction(paymentTransaction);

					ticketTransIO.setTransSeatCount(transactionDTO.getTransSeatCount());
					ticketTransIO.setTransactionAmount(transactionDTO.getTransactionAmount().subtract(transactionDTO.getTdsTax()));
					ticketTransIO.setCommissionAmount(transactionDTO.getCommissionAmount());
					ticketTransIO.setExtraCommissionAmount(transactionDTO.getExtraCommissionAmount());
					ticketTransIO.setCancelCommissionAmount(transactionDTO.getCancellationCommissionAmount());
					ticketTransIO.setCancelTdsTax(transactionDTO.getCancelTdsTax());
					ticketTransIO.setCancellationChargeTax(transactionDTO.getCancellationChargeTax());
					ticketTransIO.setCancellationChargeAmount(transactionDTO.getCancellationChargeAmount());
					ticketTransIO.setRefundAmount(transactionDTO.getRefundAmount());
					ticketTransIO.setRemarks(transactionDTO.getRemarks());

					ticketTransIO.setServiceTax(transactionDTO.getAcBusTax());
					ticketTransIO.setAcBusTax(transactionDTO.getAcBusTax());
					ticketTransIO.setTdsTax(transactionDTO.getTdsTax());
					ticketTransIO.setActiveFlag(transactionDTO.getActiveFlag());
					ticketTransIO.setTransactionType(typeIO);
					ticketTransIO.setTransactionMode(modeIO);
					ticketTransactionList.add(ticketTransIO);
				}
				ticketIO.setTicketTransaction(ticketTransactionList);
			}
			ticketIO.setRelatedTicketCode(ticketDTO.getRelatedTicketCode());
		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/ticket/cancel/{ticketCode}", method = RequestMethod.POST, produces = {})
	@ResponseBody
	public ResponseIO<TicketIO> isCancelTicket(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, @RequestParam(value = "mobileNumber", required = false) String mobileNumber) throws Exception {
		TicketIO ticketIO = new TicketIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		try {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO.setPassengerMobile(mobileNumber);

			loadUserPermissions(authDTO);

			ticketDTO = cancelTicketService.TicketIsCancel(authDTO, ticketDTO);
			ticketIO.setCode(ticketCode);
			StationIO fromStationIO = new StationIO();
			StationIO toStationIO = new StationIO();
			List<TicketDetailsIO> ticketDetailsIO = new ArrayList<TicketDetailsIO>();

			fromStationIO.setCode(ticketDTO.getFromStation().getCode());
			fromStationIO.setName(ticketDTO.getFromStation().getName());

			toStationIO.setCode(ticketDTO.getToStation().getCode());
			toStationIO.setName(ticketDTO.getToStation().getName());
			ticketIO.setFromStation(fromStationIO);
			ticketIO.setToStation(toStationIO);

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
					tickDetailsIO.setPassengerName(dto.getPassengerName());
					tickDetailsIO.setPassengerAge(dto.getPassengerAge());
					tickDetailsIO.setPassengerGender(dto.getSeatGendar().getCode());
					tickDetailsIO.setSeatFare(dto.getSeatFare());
					tickDetailsIO.setRefundAmount(dto.getRefundAmount());
					tickDetailsIO.setServiceTax(dto.getAcBusTax());
					tickDetailsIO.setCancellationCharges(dto.getCancellationCharges());
					tickDetailsIO.setCancellationChargeTax(dto.getCancellationChargeTax());
					ticketDetailsIO.add(tickDetailsIO);
				}
			}
			// Ticket Addons Details
			if (ticketDTO.getTicketAddonsDetails() != null) {
				List<TicketAddonsDetailsIO> ticketAddonsDetailsIO = new ArrayList<TicketAddonsDetailsIO>();
				for (TicketAddonsDetailsDTO dto : ticketDTO.getTicketAddonsDetails()) {
					TicketAddonsDetailsIO addonsDetailsIO = new TicketAddonsDetailsIO();
					addonsDetailsIO.setSeatCode(dto.getSeatCode());

					SeatStatusIO ticketStatus = new SeatStatusIO();
					ticketStatus.setCode(dto.getTicketStatus().getCode());
					ticketStatus.setName(dto.getTicketStatus().getDescription());
					addonsDetailsIO.setAddonStatus(ticketStatus);

					AddonTypeIO addonType = new AddonTypeIO();
					addonType.setCode(dto.getAddonsType().getCode());
					addonType.setName(dto.getAddonsType().getName());
					addonType.setCreditDebitFlag(dto.getAddonsType().getCreditDebitFlag());
					addonsDetailsIO.setAddonType(addonType);

					addonsDetailsIO.setValue(dto.getValue());
					ticketAddonsDetailsIO.add(addonsDetailsIO);
				}
				ticketIO.setTicketAddonsDetails(ticketAddonsDetailsIO);
			}
			CancellationTermIO cancellationTermsIO = new CancellationTermIO();
			List<CancellationPolicyIO> cancelPolicyList = new ArrayList<CancellationPolicyIO>();

			for (CancellationPolicyDTO cancellationPolicyDTO : ticketDTO.getCancellationTerm().getPolicyList()) {
				CancellationPolicyIO cancellPolicyIO = new CancellationPolicyIO();
				cancellPolicyIO.setFromValue(cancellationPolicyDTO.getFromValue());
				cancellPolicyIO.setToValue(cancellationPolicyDTO.getToValue());
				cancellPolicyIO.setDeductionAmount(cancellationPolicyDTO.getDeductionValue());
				cancellPolicyIO.setPercentageFlag(cancellationPolicyDTO.getPercentageFlag());
				cancellPolicyIO.setPolicyPattern(cancellationPolicyDTO.getPolicyPattern());

				cancellPolicyIO.setTerm(cancellationPolicyDTO.getTerm());
				cancellPolicyIO.setDeductionAmountTxt(cancellationPolicyDTO.getDeductionAmountTxt());
				cancellPolicyIO.setRefundAmountTxt(cancellationPolicyDTO.getRefundAmountTxt());
				cancellPolicyIO.setChargesTxt(cancellationPolicyDTO.getChargesTxt());
				cancelPolicyList.add(cancellPolicyIO);
			}

			cancellationTermsIO.setPolicyList(cancelPolicyList);
			cancellationTermsIO.setCode(ticketDTO.getCancellationTerm().getCode());
			cancellationTermsIO.setName(ticketDTO.getCancellationTerm().getName());
			cancellationTermsIO.setActiveFlag(ticketDTO.getCancellationTerm().getActiveFlag());

			ticketIO.setTravelDate(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes()).format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setTravelTime(ticketDTO.getTripTime());
			ticketIO.setTripCode(ticketDTO.getTripDTO().getCode());
			ticketIO.setTripStageCode(ticketDTO.getTripDTO().getStage().getCode());
			ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());
			// ticketIO.setPassegerEmailId(ticketDTO.getPassengerEmailId());
			ticketIO.setTotalFare(ticketDTO.getTotalFare());
			ticketIO.setServiceNo(ticketDTO.getServiceNo());
			ticketIO.setReportingTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getReportingMinutes()).format("YYYY-MM-DD hh:mm:ss"));

			ticketIO.setJourneyType(ticketDTO.getJourneyType().getCode());
			ticketIO.setDeviceMedium(ticketDTO.getDeviceMedium().getCode());
			ticketIO.setTicketDetails(ticketDetailsIO);
			ticketIO.setCancellationTerms(cancellationTermsIO);
			ticketIO.setRemarks(ticketDTO.getRemarks());
			ticketIO.setTransactionDate(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setActiveFlag(ticketDTO.getActiveFlag());

			TicketStatusIO ticketStatus = new TicketStatusIO();
			ticketStatus.setCode(ticketDTO.getTicketStatus().getCode());
			ticketStatus.setName(ticketDTO.getTicketStatus().getDescription());
			ticketIO.setTicketStatus(ticketStatus);

			BusIO bus = new BusIO();
			bus.setDisplayName(ticketDTO.getTripDTO().getBus().getDisplayName());
			bus.setName(ticketDTO.getTripDTO().getBus().getName());
			bus.setCategoryCode(ticketDTO.getTripDTO().getBus().getCategoryCode());
			bus.setBusType(busService.getBusCategoryByCode(ticketDTO.getTripDTO().getBus().getCategoryCode()));
			ticketIO.setBus(bus);
			if (ticketDTO.getTicketUser() != null) {
				UserIO userIO = new UserIO();
				userIO.setName(ticketDTO.getTicketUser().getName());
				userIO.setCode(ticketDTO.getTicketUser().getCode());
				ticketIO.setUser(userIO);
			}
		}
		catch (ServiceException e) {
			cancelLogger.error("Error is Cancel:" + authDTO.getNamespaceCode() + "-" + ticketCode + e.getErrorCode().toString());
			throw e;
		}
		catch (Exception e) {
			cancelLogger.error("isCancel: " + authDTO.getNamespaceCode() + "-" + ticketCode + e.getMessage());
			throw new ServiceException(ErrorCode.CANCELLATION_NOT_ALLOWED);
		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/ticket/cancel/{ticketCode}/confirm", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TicketIO> ConfirmCancelTicket(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String seatCodeList, @RequestParam(value = "otpNumber", required = false, defaultValue = "0") int otpNumber, String remarks) throws Exception {
		TicketIO ticketIO = new TicketIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		try {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			List<TicketDetailsDTO> detailsList = new ArrayList<>();
			for (int i = 0; i < seatCodeList.split(",").length; i++) {
				TicketDetailsDTO ticketDetailsDTO = new TicketDetailsDTO();
				ticketDetailsDTO.setSeatCode(seatCodeList.split(",")[i]);
				detailsList.add(ticketDetailsDTO);
			}
			ticketDTO.setTicketDetails(detailsList);
			ticketDTO.setOtpNumber(otpNumber);
			ticketDTO.setRemarks(remarks);

			cancelLogger.info("The ticket code for cancel seats : " + ticketDTO.getCode() + " and the Email ID: " + ticketDTO.getPassengerEmailId() + " and  otpNumber: " + otpNumber);
			cancelLogger.info("The list of seat code is: " + ticketDTO.getCode() + " - " + seatCodeList + " -- " + detailsList.toString());
			// Cancellation services call
			if (seatCodeList.isEmpty()) {
				throw new ServiceException(ErrorCode.INVALID_SEAT_CODE, "Seat code is requried Parameter");
			}
			cancelTicketService.TicketConfirmCancel(authDTO, ticketDTO, new HashMap<String, String>());
			ticketIO.setCode(ticketDTO.getCode());
		}
		catch (ServiceException e) {
			cancelLogger.error("Error ConfirmCancel-" + authDTO.getNamespaceCode() + " ticket Code-" + ticketCode + " otpNumber-" + otpNumber + " - " + e.getErrorCode().toString());
			ticketFailureService.saveFailureLog(authDTO, e.getErrorCode().getCode(), "CANCL", e.getErrorCode().getMessage() + (StringUtil.isNotNull(e.getData()) ? ", " + e.getData().toString() : Text.EMPTY), "Ticket Code: " + ticketCode + ",  otpNumber: " + otpNumber);
			throw e;
		}
		catch (Exception e) {
			cancelLogger.error("ConfirmCancel-" + authDTO.getNamespaceCode() + " ticket Code-" + ticketCode + " otpNumber-" + otpNumber + " - " + e.getMessage());
			ticketFailureService.saveFailureLog(authDTO, Text.NA, "CANCL", e.getMessage(), "Ticket Code: " + ticketCode + " otpNumber: " + otpNumber);
			throw e;
		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/ticket/blocking", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<OrderIO> blockSeatsV3(@PathVariable("authtoken") String authtoken, @RequestBody OrderIO orderIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		OrderIO ticketOrderIO = new OrderIO();
		try {
			if (authDTO != null) {
				ValidateBlockingDetails(authDTO, orderIO);
				logger.info("Request for blockSeats-" + authDTO.getNamespaceCode());
				BookingDTO bookingDTO = new BookingDTO();
				for (OrderDetailsIO orderDetails : orderIO.getOrderDetails()) {
					TicketDTO ticketDTO = new TicketDTO();
					ticketDTO.setPassengerMobile(orderIO.getMobileNumber());
					ticketDTO.setPassengerEmailId(orderIO.getEmailId());

					TripDTO tripDTO = new TripDTO();
					tripDTO.setCode(orderDetails.getTripCode());
					SearchDTO searchDTO = new SearchDTO();
					DateTime tripTravelDate = new DateTime(orderDetails.getTravelDate());
					searchDTO.setTravelDate(tripTravelDate);
					StationDTO fromStationDTO = new StationDTO();
					fromStationDTO.setCode(orderDetails.getFromStation().getCode());
					StationDTO toStationDTO = new StationDTO();
					toStationDTO.setCode(orderDetails.getToStation().getCode());
					searchDTO.setFromStation(fromStationDTO);
					searchDTO.setToStation(toStationDTO);
					tripDTO.setSearch(searchDTO);
					ticketDTO.setTripDTO(tripDTO);

					ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(orderDetails.getJourneyType()));
					ticketDTO.setRemarks(orderIO.getRemarks());
					ticketDTO.setTicketStatus(TicketStatusEM.TMP_BLOCKED_TICKET);
					ticketDTO.setDeviceMedium(authDTO.getDeviceMedium());
					StationPointDTO boardingPointDTO = new StationPointDTO();
					StationPointDTO droppingPointDTO = new StationPointDTO();
					boardingPointDTO.setCode(orderDetails.getBoardingPoint().getCode());
					droppingPointDTO.setCode(orderDetails.getDroppingPoint().getCode());

					List<TicketDetailsDTO> passengerDetails = new ArrayList<TicketDetailsDTO>();
					for (TicketDetailsIO passDetails : orderDetails.getTicketDetails()) {
						TicketDetailsDTO tdDTO = new TicketDetailsDTO();
						tdDTO.setSeatCode(passDetails.getSeatCode().trim());
						tdDTO.setPassengerName(StringUtil.substring(StringUtil.removeSymbolWithSpace(passDetails.getPassengerName()), 60));
						tdDTO.setPassengerAge(passDetails.getPassengerAge());
						tdDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(passDetails.getPassengerGender()));
						tdDTO.setTravelStatus(TravelStatusEM.YET_BOARD);
						passengerDetails.add(tdDTO);
					}
					ticketDTO.setTicketDetails(passengerDetails);
					ticketDTO.setBoardingPoint(boardingPointDTO);
					ticketDTO.setDroppingPoint(droppingPointDTO);

					bookingDTO.addTicketDTO(ticketDTO);
				}
				// Guest Booking, not required for Agent
				bookingDTO.setPaymentGatewayPartnerCode(orderIO.getGatewayPartner().getCode());
				// if round trip, common booking Code for both ticket
				if (bookingDTO.getTicketList().size() > 1) {
					bookingDTO.setRoundTripFlag(true);
				}
				bookingDTO.setCouponCode(orderIO.getOfferDiscountCode());
				bookingDTO.setAggregate(new HashMap<String, String>());
				bookingDTO.setAdditionalAttributes(orderIO.getAdditionalAttributes() != null ? orderIO.getAdditionalAttributes() : new HashMap<String, String>());
				bookingDTO.setBookAfterTripTimeFlag(false);

				// Block Ticket Process
				blockSeatsService.blockSeatsV3(authDTO, bookingDTO);

				ticketOrderIO.setCode(bookingDTO.getCode());
				// Payment Gateway Process
				if (orderIO.getGatewayPartner() != null && StringUtil.isNotNull(orderIO.getGatewayPartner().getCode())) {
					OrderInitRequestDTO paymentRequest = new OrderInitRequestDTO();
					paymentRequest.setAmount(bookingDTO.getTransactionAmount().setScale(0, RoundingMode.HALF_UP));
					paymentRequest.setFirstName(StringUtil.isNotNull(orderIO.getFirstName()) ? WordUtils.capitalize(orderIO.getFirstName()) : WordUtils.capitalize(authDTO.getUser().getName()));
					paymentRequest.setLastName(StringUtil.removeSymbol(bookingDTO.getPassengerName()));
					paymentRequest.setPartnerCode(orderIO.getGatewayPartner().getCode());
					paymentRequest.setResponseUrl(orderIO.getResponseUrl());
					paymentRequest.setOrderCode(bookingDTO.getCode());
					paymentRequest.setOrderType(OrderTypeEM.TICKET);
					paymentRequest.setAddress1(WordUtils.capitalize(bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getPassengerName()) + " " + bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getFromStation().getName());
					paymentRequest.setMobile(bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getPassengerMobile());
					paymentRequest.setEmail(bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getPassengerEmailId());
					paymentRequest.setUdf1(bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getFromStation().getName() + " - " + bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getToStation().getName() + " " + bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getTripDateTime().format("YYYY-MM-DD hh:mm:ss"));
					paymentRequest.setUdf2(authDTO.getNamespaceCode());
					paymentRequest.setUdf3(authDTO.getDeviceMedium().getCode());
					paymentRequest.setUdf4(bookingDTO.getJourneyType());
					paymentRequest.setUdf5(ApplicationConfig.getServerZoneCode());
					OrderInitStatusDTO orderInitStatusDTO = paymentRequestService.handlePgService(authDTO, paymentRequest);
					ticketOrderIO.setTransactionCode(orderInitStatusDTO.getTransactionCode());
					ticketOrderIO.setPaymentRequestUrl(orderInitStatusDTO.getPaymentRequestUrl());
				}
				else if (bookingDTO.isPaymentGatewayProcessFlag()) {
					List<PaymentGatewayScheduleDTO> list = gatewayScheduleService.getActiveSchedulePaymentGateway(authDTO, OrderTypeEM.TICKET);
					if (!list.isEmpty()) {
						List<PaymentModeIO> paymentModeList = new ArrayList<>();
						Map<String, List<PaymentGatewayPartnerIO>> mapList = new HashMap<>();
						Map<String, PaymentGatewayPartnerDTO> modeMAP = new HashMap<>();
						for (PaymentGatewayScheduleDTO scheduleDTO : list) {
							if (mapList.get(scheduleDTO.getGatewayPartner().getPaymentMode().getCode()) == null) {
								mapList.put(scheduleDTO.getGatewayPartner().getPaymentMode().getCode(), new ArrayList<PaymentGatewayPartnerIO>());
							}
							PaymentGatewayPartnerIO partnerIO = new PaymentGatewayPartnerIO();
							partnerIO.setCode(scheduleDTO.getGatewayPartner().getCode());
							partnerIO.setName(scheduleDTO.getGatewayPartner().getName());
							partnerIO.setOfferNotes(scheduleDTO.getGatewayPartner().getOfferNotes());
							partnerIO.setOfferTerms(scheduleDTO.getGatewayPartner().getOfferTerms());

							List<PaymentGatewayPartnerIO> partnerList = mapList.get(scheduleDTO.getGatewayPartner().getPaymentMode().getCode());
							partnerList.add(partnerIO);
							mapList.put(scheduleDTO.getGatewayPartner().getPaymentMode().getCode(), partnerList);
							modeMAP.put(scheduleDTO.getGatewayPartner().getPaymentMode().getCode(), scheduleDTO.getGatewayPartner());
						}
						for (String mapKey : mapList.keySet()) {
							PaymentModeIO modeIO = new PaymentModeIO();
							modeIO.setCode(modeMAP.get(mapKey).getCode());
							modeIO.setName(modeMAP.get(mapKey).getName());
							modeIO.setPaymentGatewayPartner(mapList.get(mapKey));
							paymentModeList.add(modeIO);
						}
						ticketOrderIO.setPaymentMode(paymentModeList);
					}
				}
			}
		}
		catch (ServiceException e) {
			logger.error("Error blockSeats: " + authDTO.getNamespaceCode() + " - " + ticketOrderIO.getCode() + " - " + orderIO.toJSON() + " - " + e.getErrorCode().toString());
			ticketFailureService.saveFailureLog(authDTO, e.getErrorCode().getCode(), "BLOCK", e.getErrorCode().getMessage() + (StringUtil.isNotNull(e.getData()) ? ", " + e.getData().toString() : Text.EMPTY), orderIO.toJSON());
			throw e;
		}
		catch (Exception e) {
			logger.error("blockSeats: " + authDTO.getNamespaceCode() + " - " + ticketOrderIO.getCode() + " - " + orderIO.toJSON() + " - " + e.getMessage());
			ticketFailureService.saveFailureLog(authDTO, Text.NA, "BLOCK", e.getMessage(), orderIO.toJSON());
			throw e;
		}
		logger.info("Response for blockSeats done -" + ticketOrderIO.getCode());
		return ResponseIO.success(ticketOrderIO);
	}

	@RequestMapping(value = "/ticket/{ticketCode}/notify", method = RequestMethod.POST, produces = {})
	@ResponseBody
	public ResponseIO<TicketIO> notification(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, @RequestParam(value = "mobilenumber", required = false) String mobileNumber) throws Exception {
		TicketIO ticketIO = new TicketIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TicketDTO ticketDTO = new TicketDTO();
			if (StringUtil.isNotNull(mobileNumber)) {
				ticketDTO.setPassengerMobile(mobileNumber);
			}
			ticketDTO.setCode(ticketCode);
			ticketService.notifyTicket(authDTO, ticketDTO);
			ticketIO.setPassegerEmailId(ticketDTO.getPassengerEmailId());
			ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());
		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/ticket/{ticketCode}/notify/{emailType}/v2", method = RequestMethod.POST, produces = {})
	@ResponseBody
	public ResponseIO<TicketIO> sendMailTicket(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, @PathVariable("emailType") String emailType, @RequestParam(value = "emailId", required = false) String emailId) throws Exception {
		TicketIO ticketIO = new TicketIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO.setPassengerEmailId(emailId);
			ticketService.notifyTicketV2(authDTO, ticketDTO, emailType);
			ticketIO.setPassegerEmailId(ticketDTO.getPassengerEmailId());
			ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());
		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/ticket/confirm/{ticketCode}", method = RequestMethod.POST, produces = {})
	@ResponseBody
	public ResponseIO<BookingIO> ConfirmBlockedSeat(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String bookingCode, String transactionMode) throws Exception {
		BookingIO bookingIO = new BookingIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		try {
			if (authDTO != null) {
				if (StringUtil.isNull(bookingCode)) {
					throw new ServiceException(ErrorCode.INVALID_TICKET_CODE, bookingCode);
				}
				List<TicketIO> ticketIOList = new ArrayList<TicketIO>();
				BookingDTO bookingDTO = confirmSeatsService.confirmBooking(authDTO, bookingCode, transactionMode, null, null);
				for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
					TicketIO ticketIO = new TicketIO();
					ticketIO.setCode(ticketDTO.getCode());
					StationIO FromStationIO = new StationIO();
					FromStationIO.setCode(ticketDTO.getFromStation().getCode());
					FromStationIO.setName(ticketDTO.getFromStation().getName());
					StationPointIO fromStationPointIO = new StationPointIO();
					fromStationPointIO.setCode(ticketDTO.getBoardingPoint().getCode());
					fromStationPointIO.setName(ticketDTO.getBoardingPoint().getName());
					fromStationPointIO.setLandmark(ticketDTO.getBoardingPoint().getLandmark());
					fromStationPointIO.setLongitude(ticketDTO.getBoardingPoint().getLongitude());
					fromStationPointIO.setNumber(ticketDTO.getBoardingPoint().getNumber());
					fromStationPointIO.setDateTime(ticketDTO.getBoardingPointDateTime().format("YYYY-MM-DD hh:mm:ss"));
					fromStationPointIO.setAddress(ticketDTO.getBoardingPoint().getAddress());
					// To Station Point
					StationIO toStationIO = new StationIO();
					toStationIO.setCode(ticketDTO.getFromStation().getCode());
					toStationIO.setName(ticketDTO.getFromStation().getName());
					StationPointIO toStationPointIO = new StationPointIO();
					toStationPointIO.setCode(ticketDTO.getDroppingPoint().getCode());
					toStationPointIO.setName(ticketDTO.getDroppingPoint().getName());
					toStationPointIO.setLandmark(ticketDTO.getDroppingPoint().getLandmark());
					toStationPointIO.setLongitude(ticketDTO.getDroppingPoint().getLongitude());
					toStationPointIO.setNumber(ticketDTO.getDroppingPoint().getNumber());
					toStationPointIO.setDateTime(ticketDTO.getDroppingPointDateTime().format("YYYY-MM-DD hh:mm:ss"));
					toStationPointIO.setAddress(ticketDTO.getDroppingPoint().getAddress());
					FromStationIO.setStationPoints(fromStationPointIO);
					toStationIO.setStationPoints(toStationPointIO);
					ticketIO.setFromStation(FromStationIO);
					ticketIO.setToStation(toStationIO);
					ticketIO.setTravelDate(ticketDTO.getTripDate().format("YYYY-MM-DD hh:mm:ss"));
					ticketIO.setReportingTime(DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getReportingMinutes()).format("YYYY-MM-DD hh:mm:ss"));
					ticketIO.setRemarks(ticketDTO.getRemarks());
					ticketIO.setPassegerEmailId(ticketDTO.getPassengerEmailId());
					ticketIO.setPassegerMobleNo(ticketDTO.getPassengerMobile());
					ticketIO.setTotalFare(ticketDTO.getTotalFare());
					ticketIO.setTransactionDate(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
					// Ticket status
					TicketStatusIO ticketStatusIO = new TicketStatusIO();
					ticketStatusIO.setCode(ticketDTO.getTicketStatus().getCode());
					ticketStatusIO.setName(ticketDTO.getTicketStatus().getDescription());
					ticketIO.setTicketStatus(ticketStatusIO);
					// transaction Payment Mode
					TransactionModeIO transactionModeIO = new TransactionModeIO();
					transactionModeIO.setCode(ticketDTO.getTransactionMode().getCode());
					transactionModeIO.setName(ticketDTO.getTransactionMode().getName());
					ticketIO.setTransactionMode(transactionModeIO);
					// copy User Transaction Details
					List<UserTransactionIO> userTransactionIOList = new ArrayList<>();
					if (ticketDTO.getUserTransaction() != null) {
						UserTransactionIO userTransactionIO = new UserTransactionIO();
						TransactionTypeIO transactionTypeIO = new TransactionTypeIO();
						transactionTypeIO.setName(ticketDTO.getUserTransaction().getTransactionType().getName());
						transactionTypeIO.setCreditDebitFlag(ticketDTO.getUserTransaction().getTransactionType().getCreditDebitFlag());
						transactionTypeIO.setCode(ticketDTO.getUserTransaction().getTransactionType().getCode());
						userTransactionIO.setCreditAmount(ticketDTO.getUserTransaction().getCreditAmount());
						userTransactionIO.setDebitAmount(ticketDTO.getUserTransaction().getDebitAmount());
						userTransactionIO.setTransactionAmount(ticketDTO.getUserTransaction().getTransactionAmount());
						userTransactionIOList.add(userTransactionIO);
					}
					ticketIO.setUsertransaction(userTransactionIOList);

					// copy Cancellation Terms
					CancellationTermIO cancellationTermIO = new CancellationTermIO();
					CancellationTermDTO termDTO = ticketDTO.getCancellationTerm();
					cancellationTermIO.setName(termDTO.getName());
					cancellationTermIO.setCode(termDTO.getCode());
					List<CancellationPolicyIO> policyIOs = new ArrayList<CancellationPolicyIO>();
					for (CancellationPolicyDTO policyDTO : termDTO.getPolicyList()) {
						CancellationPolicyIO policyIO = new CancellationPolicyIO();
						policyIO.setFromValue(policyDTO.getFromValue());
						policyIO.setToValue(policyDTO.getToValue());
						policyIO.setDeductionAmount(policyDTO.getDeductionValue());
						policyIO.setPercentageFlag(policyDTO.getPercentageFlag());
						policyIO.setPolicyPattern(policyDTO.getPolicyPattern());
						policyIOs.add(policyIO);
					}
					cancellationTermIO.setPolicyList(policyIOs);
					ticketIO.setCancellationTerms(cancellationTermIO);
					ticketIOList.add(ticketIO);
				}
				bookingIO.setTicket(ticketIOList);
				bookingIO.setRoundTripFlag(bookingDTO.isRoundTripFlag());
			}
		}
		catch (ServiceException e) {
			logger.error("Error Confirm Seat: " + authDTO.getNamespaceCode() + " - " + bookingCode + " - " + transactionMode + " - " + e.getErrorCode().toString());
			ticketFailureService.saveFailureLog(authDTO, e.getErrorCode().getCode(), "CONFM", e.getErrorCode().getMessage() + (StringUtil.isNotNull(e.getData()) ? ", " + e.getData().toString() : Text.EMPTY), "Ticket Code: " + bookingCode + " and Transaction Mode: " + transactionMode);
			throw e;
		}
		catch (Exception e) {
			logger.error("Confirm Seat: " + authDTO.getNamespaceCode() + " - " + bookingCode + " - " + transactionMode + " - " + e.getMessage());
			ticketFailureService.saveFailureLog(authDTO, Text.NA, "CONFM", e.getMessage(), "Ticket Code: " + bookingCode + " and Transaction Mode: " + transactionMode);
			throw e;
		}
		logger.info("Response for ConfirmSeat done-" + bookingCode);
		return ResponseIO.success(bookingIO);
	}

	@RequestMapping(value = "/ticket/payment/processes", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<OrderIO> processTicketPayment(@PathVariable("authtoken") String authtoken, @RequestBody OrderIO orderIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		OrderIO ticketOrderIO = new OrderIO();
		try {
			if (authDTO != null) {
				BookingDTO bookingDTO = new BookingDTO();
				for (OrderDetailsIO orderDetails : orderIO.getOrderDetails()) {
					TicketDTO ticketDTO = new TicketDTO();
					ticketDTO.setCode(orderDetails.getTicketCode());
					bookingDTO.addTicketDTO(ticketDTO);
				}
				bookingDTO.setPaymentGatewayPartnerCode(orderIO.getGatewayPartner().getCode());

				// Check Availability
				bookingDTO = blockSeatsService.processTicketPayment(authDTO, bookingDTO);

				// Payment Gateway Process
				OrderInitRequestDTO paymentRequest = new OrderInitRequestDTO();
				paymentRequest.setAmount(bookingDTO.getTransactionAmount().setScale(0, RoundingMode.HALF_UP));
				paymentRequest.setFirstName(StringUtil.isNotNull(orderIO.getFirstName()) ? WordUtils.capitalize(orderIO.getFirstName()) : WordUtils.capitalize(authDTO.getUser().getName()));
				paymentRequest.setLastName(StringUtil.removeSymbol(bookingDTO.getPassengerName()));
				paymentRequest.setPartnerCode(orderIO.getGatewayPartner().getCode());
				paymentRequest.setResponseUrl(orderIO.getResponseUrl());
				paymentRequest.setOrderCode(bookingDTO.getCode());
				paymentRequest.setOrderType(OrderTypeEM.TICKET);
				paymentRequest.setAddress1(WordUtils.capitalize(bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getPassengerName()) + " " + bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getFromStation().getName());
				paymentRequest.setMobile(bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getPassengerMobile());
				paymentRequest.setEmail(bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getPassengerEmailId());
				paymentRequest.setUdf1(bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getFromStation().getName() + " - " + bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getToStation().getName() + " " + bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP).getTripDateTime().format("YYYY-MM-DD hh:mm:ss"));
				paymentRequest.setUdf2(authDTO.getNamespaceCode());
				paymentRequest.setUdf3(authDTO.getDeviceMedium().getCode());
				paymentRequest.setUdf4(bookingDTO.getJourneyType());
				paymentRequest.setUdf5(ApplicationConfig.getServerZoneCode());

				OrderInitStatusDTO orderInitStatusDTO = paymentRequestService.handlePgService(authDTO, paymentRequest);
				ticketOrderIO.setTransactionCode(orderInitStatusDTO.getTransactionCode());
				ticketOrderIO.setPaymentRequestUrl(orderInitStatusDTO.getPaymentRequestUrl());
			}
		}
		catch (ServiceException e) {
			logger.error("Error Ticket Payment: " + authDTO.getNamespaceCode() + " - " + orderIO.toJSON() + " - " + e.getErrorCode().toString());
			throw e;
		}
		catch (Exception e) {
			logger.error("Ticket Payment: " + authDTO.getNamespaceCode() + " - " + orderIO.toJSON() + " - " + e.getMessage());
			throw e;
		}
		return ResponseIO.success(ticketOrderIO);
	}

	private boolean ValidateBlockingDetails(AuthDTO authDTO, OrderIO orderIO) throws Exception {
		if (orderIO == null || orderIO.getOrderDetails() == null) {
			throw new ServiceException(ErrorCode.INVALID_ORDER_DETAILS);
		}
		if (orderIO.getAdditionalAttributes() != null && !orderIO.getAdditionalAttributes().isEmpty() && StringUtil.isNotNull(orderIO.getAdditionalAttributes().get(Text.GST_IN)) && !GSTINValidator.validGSTIN(orderIO.getAdditionalAttributes().get(Text.GST_IN))) {
			orderIO.getAdditionalAttributes().remove(Text.GST_IN);
			orderIO.getAdditionalAttributes().remove(Text.GST_TRADE_NAME);
		}

		for (OrderDetailsIO orderDetails : orderIO.getOrderDetails()) {
			if (StringUtil.isNull(orderDetails.getTripCode())) {
				throw new ServiceException(ErrorCode.TRIP_STATGE_CODE);
			}
			if (StringUtil.isNull(orderDetails.getTravelDate())) {
				throw new ServiceException(ErrorCode.INVALID_DATE);
			}
			if (orderDetails.getFromStation() == null || orderDetails.getToStation() == null || StringUtil.isNull(orderDetails.getFromStation().getCode()) || StringUtil.isNull(orderDetails.getToStation().getCode())) {
				throw new ServiceException(ErrorCode.INVALID_STATION);
			}
			if (StringUtil.isNull(orderIO.getEmailId()) || !StringUtil.isValidEmailId(orderIO.getEmailId())) {
				throw new ServiceException(ErrorCode.INVALID_EMAIL_ID);
			}
			if (StringUtil.isNull(orderIO.getMobileNumber()) || !StringUtil.isValidMobileNumber(orderIO.getMobileNumber())) {
				throw new ServiceException(ErrorCode.INVALID_MOBLIE_NUMBER);
			}
			if (Character.toString(orderIO.getMobileNumber().charAt(0)).equals(Numeric.ZERO)) {
				orderIO.setMobileNumber(orderIO.getMobileNumber().substring(1));
			}
			if (orderDetails.getBoardingPoint() == null || StringUtil.isNull(orderDetails.getBoardingPoint().getCode())) {
				throw new ServiceException(ErrorCode.STATION_POINT);
			}
			if (orderDetails.getDroppingPoint() == null || StringUtil.isNull(orderDetails.getDroppingPoint().getCode())) {
				throw new ServiceException(ErrorCode.STATION_POINT);
			}
			if (orderDetails.getTicketDetails() == null || orderDetails.getTicketDetails().isEmpty()) {
				throw new ServiceException(ErrorCode.SEAT_NOT_AVAILABLE);
			}
			if (orderDetails.getTicketDetails().size() > authDTO.getNamespace().getProfile().getMaxSeatPerTransaction()) {
				throw new ServiceException(ErrorCode.MAX_SEAT_PER_TRANSACTION);
			}
			if (orderIO.getGatewayPartner() == null || StringUtil.isNull(orderIO.getGatewayPartner().getCode())) {
				throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
			}
			List<String> uniqueList = new ArrayList<String>();
			for (TicketDetailsIO seatInfo : orderDetails.getTicketDetails()) {

				if (StringUtil.isNull(seatInfo.getSeatCode())) {
					throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
				}
				if (StringUtil.isNull(seatInfo.getPassengerName())) {
					throw new ServiceException(ErrorCode.INVALID_PASSENGER_NAME);
				}
				if (seatInfo.getPassengerAge() < 3 || seatInfo.getPassengerAge() > 100) {
					throw new ServiceException(ErrorCode.INVALID_PASSENGER_AGE);
				}
				if (StringUtil.isNull(seatInfo.getPassengerGender())) {
					throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER);
				}
				if (uniqueList.contains(seatInfo.getSeatCode().trim())) {
					throw new ServiceException(ErrorCode.DUPLICATE_SEAT_CODE);
				}
				uniqueList.add(seatInfo.getSeatCode().trim());

				seatInfo.setPassengerName(StringUtil.substring(StringUtil.removeUnknownSymbol(seatInfo.getPassengerName()), 59));
				seatInfo.setPassengerName(WordUtils.capitalize(seatInfo.getPassengerName()));
			}
		}
		return true;
	}

	private void loadUserPermissions(AuthDTO authDTO) {
		boolean isOverrideAsPolicyFlag = getPrivilege(authDTO, MenuEventEM.BOOKING_CANCEL_OVERRIDE_AS_POLICY);
		boolean isAllowSameGroup = getPrivilege(authDTO, MenuEventEM.BOOKING_CANCEL_SAME_GROUP);
		boolean isAllowSameTripDate = getPrivilege(authDTO, MenuEventEM.ALLOW_BOOKING_CANCEL_SAME_DAY);
		boolean isAllowRescheduleTicketCancellation = getPrivilege(authDTO, MenuEventEM.TRANSFER_BOOKING_CANCEL);

		authDTO.getAdditionalAttribute().put(Text.OVERRIDE_AS_PER_POLICY_FLAG, isOverrideAsPolicyFlag ? Numeric.ONE : Numeric.ZERO);
		authDTO.getAdditionalAttribute().put(Text.BOOKING_CANCEL_ALL_GROUP, isAllowSameGroup ? Numeric.ONE : Numeric.ZERO);
		authDTO.getAdditionalAttribute().put(Text.ALLOW_BOOKING_CANCEL_SAME_DAY, isAllowSameTripDate ? Numeric.ONE : Numeric.ZERO);
		authDTO.getAdditionalAttribute().put(Text.TRANSFER_BOOKING_CANCEL, isAllowRescheduleTicketCancellation ? Numeric.ONE : Numeric.ZERO);
	}

}
