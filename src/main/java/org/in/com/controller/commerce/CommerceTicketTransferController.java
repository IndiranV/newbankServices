package org.in.com.controller.commerce;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.WordUtils;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.api_v3.io.ScheduleTicketTransferTermsIO;
import org.in.com.controller.commerce.io.OrderIO;
import org.in.com.controller.commerce.io.OrderTransferDetailsIO;
import org.in.com.controller.commerce.io.ResponseIO;
import org.in.com.controller.commerce.io.StationIO;
import org.in.com.controller.commerce.io.StationPointIO;
import org.in.com.controller.commerce.io.TicketIO;
import org.in.com.controller.commerce.io.TicketStatusIO;
import org.in.com.controller.commerce.io.TicketTransferDetailsIO;
import org.in.com.controller.commerce.io.TicketTransferIO;
import org.in.com.controller.commerce.io.TransactionTypeIO;
import org.in.com.controller.commerce.io.UserTransactionIO;
import org.in.com.controller.web.BaseController;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.CancellationPolicyIO;
import org.in.com.controller.web.io.CancellationTermIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.CancellationPolicyDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.OrderInitRequestDTO;
import org.in.com.dto.OrderInitStatusDTO;
import org.in.com.dto.ScheduleTicketTransferTermsDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketTransferDTO;
import org.in.com.dto.TicketTransferDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.OrderTypeEM;
import org.in.com.dto.enumeration.UserRoleEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.TicketFailureService;
import org.in.com.service.TicketTransferService;
import org.in.com.service.pg.PaymentRequestService;
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
import org.springframework.web.bind.annotation.ResponseBody;

import hirondelle.date4j.DateTime;
import net.sf.json.JSONArray;

@Controller
@RequestMapping("/{authtoken}/commerce")
public class CommerceTicketTransferController extends BaseController {
	private static final Logger LOGGER = LoggerFactory.getLogger("tickettransferlogger");

	@Autowired
	TicketTransferService transferService;
	@Autowired
	PaymentRequestService paymentRequestService;
	@Autowired
	TicketFailureService ticketFailureService;

	@RequestMapping(value = "/ticket/{ticketCode}/transfer", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TicketIO> transferTicket(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, @RequestBody OrderTransferDetailsIO transferDetails) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		TicketIO ticketIO = new TicketIO();
		try {
			LOGGER.info("TRS001 Ticket reschedule initiated by {} PNR {} Request {}", authDTO.getUser().getName(), ticketCode, transferDetails.toString());
			/** Load Permission */
			loadUserPermissions(authDTO);

			TicketDTO transferDTO = new TicketDTO();
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(transferDetails.getTransferTripCode());
			// V3
			SearchDTO searchDTO = new SearchDTO();
			DateTime tripTravelDate = new DateTime(transferDetails.getTransferTravelDate());
			searchDTO.setTravelDate(tripTravelDate);

			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setCode(transferDetails.getTransferFromStation().getCode());
			searchDTO.setFromStation(fromStationDTO);

			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setCode(transferDetails.getTransferToStation().getCode());
			searchDTO.setToStation(toStationDTO);

			tripDTO.setSearch(searchDTO);
			transferDTO.setTripDTO(tripDTO);
			List<TicketDetailsDTO> transferSeatDetailsList = new ArrayList<TicketDetailsDTO>();
			for (String seatCode : transferDetails.getTransferSeatCode()) {
				TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
				detailsDTO.setSeatCode(seatCode);
				transferSeatDetailsList.add(detailsDTO);

			}
			transferDTO.setTicketDetails(transferSeatDetailsList);
			transferDTO.setMyAccountFlag(transferDetails.getMyAccountFlag());

			if (transferDetails.getTransferBoardingPoint() != null) {
				StationPointDTO boardingPointDTO = new StationPointDTO();
				boardingPointDTO.setCode(transferDetails.getTransferBoardingPoint().getCode());
				transferDTO.setBoardingPoint(boardingPointDTO);
			}
			if (transferDetails.getTransferDroppingPoint() != null) {
				StationPointDTO droppingPointDTO = new StationPointDTO();
				droppingPointDTO.setCode(transferDetails.getTransferDroppingPoint().getCode());
				transferDTO.setDroppingPoint(droppingPointDTO);
			}

			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(transferDetails.getTicketCode());
			List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
			for (String seatCode : transferDetails.getSeatCode()) {
				TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
				detailsDTO.setSeatCode(seatCode);
				seatDetailsList.add(detailsDTO);
			}
			ticketDTO.setTicketDetails(seatDetailsList);
			if (seatDetailsList.size() != transferSeatDetailsList.size()) {
				LOGGER.error("{} ErrorCode 406 Old Seats {} Given Old Seats {}", ticketDTO.getCode(), seatDetailsList.stream().map(repoTicket -> repoTicket.getSeatCode()).collect(Collectors.toList()), transferSeatDetailsList.stream().map(ticket -> ticket.getSeatCode()).collect(Collectors.toList()));
				throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
			}

			Map<String, Boolean> additionalAttribute = new HashMap<String, Boolean>();
			additionalAttribute.put("captureFareDifferece", transferDetails.getCaptureFareDifferece() != null ? transferDetails.getCaptureFareDifferece() : Text.FALSE);
			additionalAttribute.put("captureLowFareDifferece", transferDetails.getCaptureLowFareDifferece() != null ? transferDetails.getCaptureLowFareDifferece() : Text.FALSE);
			additionalAttribute.put("captureTransferCharge", transferDetails.getCaptureTransferCharge() != null ? transferDetails.getCaptureTransferCharge() : Text.TRUE);
			additionalAttribute.put("notification", transferDetails.getNotificationFlag() != null ? transferDetails.getNotificationFlag() : Text.FALSE);

			// super user allow to transfer but no user balance
			if (!authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode())) {
				additionalAttribute.put("captureFareDifferece", Text.FALSE);
				additionalAttribute.put("captureTransferCharge", Text.FALSE);
			}
			
			// check permission - Allow Blocked seats booking
			if (authDTO.getUser().getUserRole().getId() == UserRoleEM.USER_ROLE.getId()) {
				List<MenuEventEM> eventlist = new ArrayList<MenuEventEM>();
				eventlist.add(MenuEventEM.ALLOW_BLOCKED_SEAT_BOOKING);
				MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventlist);
				
				authDTO.getAdditionalAttribute().put(Text.ALLOW_BLOCKED_SEAT_BOOKING_FLAG, String.valueOf(menuEventDTO.getEnabledFlag()));
			}
			
			TicketDTO transferedDTO = transferService.transferTicket(authDTO, ticketDTO, transferDTO, additionalAttribute);

			ticketIO.setCode(transferedDTO.getCode());
			StationIO FromStationIO = new StationIO();
			FromStationIO.setCode(transferedDTO.getFromStation().getCode());
			FromStationIO.setName(transferedDTO.getFromStation().getName());
			StationPointIO fromStationPointIO = new StationPointIO();
			fromStationPointIO.setCode(transferedDTO.getBoardingPoint().getCode());
			fromStationPointIO.setName(transferedDTO.getBoardingPoint().getName());
			fromStationPointIO.setLandmark(transferedDTO.getBoardingPoint().getLandmark());
			fromStationPointIO.setLongitude(transferedDTO.getBoardingPoint().getLongitude());
			fromStationPointIO.setNumber(transferedDTO.getBoardingPoint().getNumber());
			fromStationPointIO.setDateTime(transferedDTO.getBoardingPointDateTime().format("YYYY-MM-DD hh:mm:ss"));
			fromStationPointIO.setAddress(transferedDTO.getBoardingPoint().getAddress());
			// To Station Point
			StationIO toStationIO = new StationIO();
			toStationIO.setCode(transferedDTO.getFromStation().getCode());
			toStationIO.setName(transferedDTO.getFromStation().getName());
			StationPointIO toStationPointIO = new StationPointIO();
			toStationPointIO.setCode(transferedDTO.getDroppingPoint().getCode());
			toStationPointIO.setName(transferedDTO.getDroppingPoint().getName());
			toStationPointIO.setLandmark(transferedDTO.getDroppingPoint().getLandmark());
			toStationPointIO.setLongitude(transferedDTO.getDroppingPoint().getLongitude());
			toStationPointIO.setNumber(transferedDTO.getDroppingPoint().getNumber());
			toStationPointIO.setDateTime(transferedDTO.getDroppingPointDateTime().format("YYYY-MM-DD hh:mm:ss"));
			toStationPointIO.setAddress(transferedDTO.getDroppingPoint().getAddress());
			FromStationIO.setStationPoints(fromStationPointIO);
			toStationIO.setStationPoints(toStationPointIO);
			ticketIO.setFromStation(FromStationIO);
			ticketIO.setToStation(toStationIO);
			ticketIO.setTravelDate(transferedDTO.getTripDate().format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setReportingTime(DateUtil.addMinituesToDate(transferedDTO.getTripDate(), transferedDTO.getReportingMinutes()).format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setRemarks(transferedDTO.getRemarks());
			ticketIO.setPassegerEmailId(transferedDTO.getPassengerEmailId());
			ticketIO.setPassegerMobleNo(transferedDTO.getPassengerMobile());

			ticketIO.setTransactionDate(transferedDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
			// Ticket status
			TicketStatusIO ticketStatusIO = new TicketStatusIO();
			ticketStatusIO.setCode(transferedDTO.getTicketStatus().getCode());
			ticketStatusIO.setName(transferedDTO.getTicketStatus().getDescription());
			ticketIO.setTicketStatus(ticketStatusIO);
			LOGGER.info("TRS001 Ticket reschedule completed by {} PNR {} New PNR {}", authDTO.getUser().getName(), ticketCode, transferedDTO.getCode());
		}
		catch (ServiceException e) {
			LOGGER.error("{} {}", ticketCode, e);
			ticketFailureService.saveFailureLog(authDTO, StringUtil.isNull(e.getErrorCode().getCode(), "Code-NA"), "TRNSR", StringUtil.isNull(e.getErrorCode().getMessage(), "Msg-NA") + (StringUtil.isNotNull(e.getData()) ? ", " + e.getData().toString() : Text.EMPTY), transferDetails.toJSON());
			throw e;
		}
		catch (Exception e) {
			LOGGER.error("{} {}", ticketCode, e);
			ticketFailureService.saveFailureLog(authDTO, Text.NA, "TRNSR", e.getMessage(), transferDetails.toJSON());
			throw e;
		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/ticket/{ticketCode}/transfer/v2", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<ScheduleTicketTransferTermsIO> isTransferTicket(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		ScheduleTicketTransferTermsIO scheduleTicketTransferTerms = new ScheduleTicketTransferTermsIO();
		try {
			AuthDTO authDTO = authService.getAuthDTO(authtoken);
			LOGGER.info("TRS002 Ticket reschedule istransfer by {} PNR {}  ", authDTO.getUser().getName(), ticketCode);
			if (StringUtil.isNull(ticketCode)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE, ticketCode);
			}
			/** Load Permission */
			loadUserPermissions(authDTO);

			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ScheduleTicketTransferTermsDTO scheduleTicketTransferTermsDTO = transferService.isTransferTicket(authDTO, ticketDTO);
			scheduleTicketTransferTerms.setChargeAmount(scheduleTicketTransferTermsDTO.getChargeAmount());
			scheduleTicketTransferTerms.setChargeType(scheduleTicketTransferTermsDTO.getChargeType().getCode());
			scheduleTicketTransferTerms.setAllowedTill(scheduleTicketTransferTermsDTO.getDateTime().format(Text.DATE_TIME_DATE4J));
			scheduleTicketTransferTerms.setTransferable(1);
		}
		catch (ServiceException e) {
			LOGGER.error("{} {}", ticketCode, e);
			throw e;
		}
		catch (Exception e) {
			LOGGER.error("{} {}", ticketCode, e);
			throw e;
		}
		return ResponseIO.success(scheduleTicketTransferTerms);
	}

	@RequestMapping(value = "/ticket/transfer/validate", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TicketTransferDetailsIO>> isTransferTicket(@PathVariable("authtoken") String authtoken, @RequestBody TicketTransferIO transferDetails) throws Exception {
		List<TicketTransferDetailsIO> ticketTransferDetailsList = new ArrayList<>();
		try {
			AuthDTO authDTO = authService.getAuthDTO(authtoken);
			LOGGER.info("TRS005 Ticket reschedule initiated by {} Trip Code {} Request {}", authDTO.getUser().getName(), transferDetails.getTransferTripCode(), transferDetails.toString());

			/** Load Permission */
			loadUserPermissions(authDTO);

			TicketTransferDTO ticketTransferDTO = new TicketTransferDTO();
			ticketTransferDTO.setTravelDate(DateUtil.getDateTime(transferDetails.getTransferTravelDate()));

			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setCode(transferDetails.getTransferFromStation().getCode());
			ticketTransferDTO.setFromStation(fromStationDTO);

			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setCode(transferDetails.getTransferToStation().getCode());
			ticketTransferDTO.setToStation(toStationDTO);

			TripDTO transferTripDTO = new TripDTO();
			transferTripDTO.setCode(transferDetails.getTransferTripCode());
			ticketTransferDTO.setTransferTrip(transferTripDTO);

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(transferDetails.getTripCode());
			ticketTransferDTO.setTrip(tripDTO);

			List<TicketTransferDetailsDTO> ticketTransferDetails = new ArrayList<>();
			for (TicketTransferDetailsIO ticketTransferDetailsIO : transferDetails.getTicketTransferDetails()) {
				List<TicketDetailsDTO> transferSeatDetailsList = new ArrayList<TicketDetailsDTO>();
				for (String seatCode : ticketTransferDetailsIO.getTransferSeatCode()) {
					TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
					detailsDTO.setSeatCode(seatCode);
					transferSeatDetailsList.add(detailsDTO);
				}

				TicketDTO transferDTO = new TicketDTO();
				transferDTO.setTicketDetails(transferSeatDetailsList);

				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setCode(ticketTransferDetailsIO.getTicketCode());

				List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
				for (String seatCode : ticketTransferDetailsIO.getSeatCode()) {
					TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
					detailsDTO.setSeatCode(seatCode);
					seatDetailsList.add(detailsDTO);
				}
				ticketDTO.setTicketDetails(seatDetailsList);

				if (seatDetailsList.size() != transferSeatDetailsList.size()) {
					LOGGER.error("{} ErrorCode 406 Old Seats {} Given Old Seats {}", ticketDTO.getCode(), seatDetailsList.stream().map(repoTicket -> repoTicket.getSeatCode()).collect(Collectors.toList()), transferSeatDetailsList.stream().map(ticket -> ticket.getSeatCode()).collect(Collectors.toList()));
					throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
				}

				TicketTransferDetailsDTO ticketTransferDetailsDTO = new TicketTransferDetailsDTO();
				ticketTransferDetailsDTO.setTicket(ticketDTO);
				ticketTransferDetailsDTO.setTransferTicket(transferDTO);

				if (transferDetails.getTransferBoardingPoint() != null) {
					StationPointDTO boardingPointDTO = new StationPointDTO();
					boardingPointDTO.setCode(transferDetails.getTransferBoardingPoint().getCode());
					ticketTransferDetailsDTO.setBoardingPoint(boardingPointDTO);
				}
				if (transferDetails.getTransferDroppingPoint() != null) {
					StationPointDTO droppingPointDTO = new StationPointDTO();
					droppingPointDTO.setCode(transferDetails.getTransferDroppingPoint().getCode());
					ticketTransferDetailsDTO.setDroppingPoint(droppingPointDTO);
				}
				ticketTransferDetails.add(ticketTransferDetailsDTO);
			}
			ticketTransferDTO.setTicketTransferDetails(ticketTransferDetails);

			Map<String, Boolean> additionalAttribute = new HashMap<String, Boolean>();
			additionalAttribute.put("captureFareDifferece", authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && transferDetails.getCaptureFareDifferece() != null ? transferDetails.getCaptureFareDifferece() : Text.FALSE);
			additionalAttribute.put("captureLowFareDifferece", authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && transferDetails.getCaptureLowFareDifferece() != null ? transferDetails.getCaptureLowFareDifferece() : Text.FALSE);
			additionalAttribute.put("captureTransferCharge", authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && transferDetails.getCaptureTransferCharge() != null ? transferDetails.getCaptureTransferCharge() : Text.TRUE);
			additionalAttribute.put("notification", transferDetails.getNotificationFlag() != null ? transferDetails.getNotificationFlag() : Text.FALSE);
			additionalAttribute.put("myAccountFlag", transferDetails.getMyAccountFlag() != null ? transferDetails.getMyAccountFlag() : Text.FALSE);
			ticketTransferDTO.setAdditionalAttribute(additionalAttribute);

			transferService.isTransferTicket(authDTO, ticketTransferDTO);

			for (TicketTransferDetailsDTO ticketTransferDetailsDTO : ticketTransferDTO.getTicketTransferDetails()) {
				TicketTransferDetailsIO ticketTransferDetailsIO = new TicketTransferDetailsIO();
				ticketTransferDetailsIO.setTicketCode(ticketTransferDetailsDTO.getTicket().getCode());

				org.in.com.controller.commerce.io.ScheduleTicketTransferTermsIO scheduleTicketTransferTerms = new org.in.com.controller.commerce.io.ScheduleTicketTransferTermsIO();
				scheduleTicketTransferTerms.setChargeAmount(ticketTransferDetailsDTO.getScheduleTicketTransferTerms().getChargeAmount());
				scheduleTicketTransferTerms.setChargeType(ticketTransferDetailsDTO.getScheduleTicketTransferTerms().getChargeType().getCode());
				scheduleTicketTransferTerms.setAllowedTill(ticketTransferDetailsDTO.getScheduleTicketTransferTerms().getDateTime().format(Text.DATE_TIME_DATE4J));
				scheduleTicketTransferTerms.setTransferable(1);
				ticketTransferDetailsIO.setScheduleTicketTransferTerms(scheduleTicketTransferTerms);
				ticketTransferDetailsList.add(ticketTransferDetailsIO);
			}
			LOGGER.info("TRS006 Ticket reschedule completed by {} Trip {}", authDTO.getUser().getName(), transferDetails.getTransferTripCode());
		}
		catch (ServiceException e) {
			LOGGER.error("", e);
			throw e;
		}
		catch (Exception e) {
			LOGGER.error("", e);
			throw e;
		}
		return ResponseIO.success(ticketTransferDetailsList);
	}

	@RequestMapping(value = "/ticket/transfer", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> transferTicket(@PathVariable("authtoken") String authtoken, @RequestBody TicketTransferIO transferDetails) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		try {
			LOGGER.info("TRS005 Ticket reschedule initiated by {} Trip Code {} Request {}", authDTO.getUser().getName(), transferDetails.getTransferTripCode(), transferDetails.toString());

			/** Load Permission */
			loadUserPermissions(authDTO);

			TicketTransferDTO ticketTransferDTO = new TicketTransferDTO();
			ticketTransferDTO.setTravelDate(DateUtil.getDateTime(transferDetails.getTransferTravelDate()));

			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setCode(transferDetails.getTransferFromStation().getCode());
			ticketTransferDTO.setFromStation(fromStationDTO);

			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setCode(transferDetails.getTransferToStation().getCode());
			ticketTransferDTO.setToStation(toStationDTO);

			TripDTO transferTripDTO = new TripDTO();
			transferTripDTO.setCode(transferDetails.getTransferTripCode());
			ticketTransferDTO.setTransferTrip(transferTripDTO);

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(transferDetails.getTripCode());
			ticketTransferDTO.setTrip(tripDTO);

			List<TicketTransferDetailsDTO> ticketTransferDetails = new ArrayList<>();
			for (TicketTransferDetailsIO ticketTransferDetailsIO : transferDetails.getTicketTransferDetails()) {
				List<TicketDetailsDTO> transferSeatDetailsList = new ArrayList<TicketDetailsDTO>();
				for (String seatCode : ticketTransferDetailsIO.getTransferSeatCode()) {
					TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
					detailsDTO.setSeatCode(seatCode);
					transferSeatDetailsList.add(detailsDTO);
				}

				TicketDTO transferDTO = new TicketDTO();
				transferDTO.setTicketDetails(transferSeatDetailsList);

				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setCode(ticketTransferDetailsIO.getTicketCode());

				List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
				for (String seatCode : ticketTransferDetailsIO.getSeatCode()) {
					TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
					detailsDTO.setSeatCode(seatCode);
					seatDetailsList.add(detailsDTO);
				}
				ticketDTO.setTicketDetails(seatDetailsList);

				if (seatDetailsList.size() != transferSeatDetailsList.size()) {
					LOGGER.error("{} ErrorCode 406 Old Seats {} Given Old Seats {}", ticketDTO.getCode(), seatDetailsList.stream().map(repoTicket -> repoTicket.getSeatCode()).collect(Collectors.toList()), transferSeatDetailsList.stream().map(ticket -> ticket.getSeatCode()).collect(Collectors.toList()));
					throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
				}

				TicketTransferDetailsDTO ticketTransferDetailsDTO = new TicketTransferDetailsDTO();
				ticketTransferDetailsDTO.setTicket(ticketDTO);
				ticketTransferDetailsDTO.setTransferTicket(transferDTO);

				if (transferDetails.getTransferBoardingPoint() != null) {
					StationPointDTO boardingPointDTO = new StationPointDTO();
					boardingPointDTO.setCode(transferDetails.getTransferBoardingPoint().getCode());
					ticketTransferDetailsDTO.setBoardingPoint(boardingPointDTO);
				}
				if (transferDetails.getTransferDroppingPoint() != null) {
					StationPointDTO droppingPointDTO = new StationPointDTO();
					droppingPointDTO.setCode(transferDetails.getTransferDroppingPoint().getCode());
					ticketTransferDetailsDTO.setDroppingPoint(droppingPointDTO);
				}
				ticketTransferDetails.add(ticketTransferDetailsDTO);
			}
			ticketTransferDTO.setTicketTransferDetails(ticketTransferDetails);

			Map<String, Boolean> additionalAttribute = new HashMap<String, Boolean>();
			additionalAttribute.put("captureFareDifferece", authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && transferDetails.getCaptureFareDifferece() != null ? transferDetails.getCaptureFareDifferece() : Text.FALSE);
			additionalAttribute.put("captureLowFareDifferece", authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && transferDetails.getCaptureLowFareDifferece() != null ? transferDetails.getCaptureLowFareDifferece() : Text.FALSE);
			additionalAttribute.put("captureTransferCharge", authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && transferDetails.getCaptureTransferCharge() != null ? transferDetails.getCaptureTransferCharge() : Text.TRUE);
			additionalAttribute.put("notification", transferDetails.getNotificationFlag() != null ? transferDetails.getNotificationFlag() : Text.FALSE);
			additionalAttribute.put("myAccountFlag", transferDetails.getMyAccountFlag() != null ? transferDetails.getMyAccountFlag() : Text.FALSE);
			additionalAttribute.put("FREE_TICKET", Text.FALSE);
			ticketTransferDTO.setAdditionalAttribute(additionalAttribute);

			transferService.transferTicket(authDTO, ticketTransferDTO);

			LOGGER.info("TRS006 Ticket reschedule completed by {} Trip {}", authDTO.getUser().getName(), transferDetails.getTransferTripCode());
		}
		catch (ServiceException e) {
			LOGGER.error("", e);
			ticketFailureService.saveFailureLog(authDTO, StringUtil.isNull(e.getErrorCode().getCode(), "Code-NA"), "TRNSR", StringUtil.isNull(e.getErrorCode().getMessage(), "Msg-NA") + (StringUtil.isNotNull(e.getData()) ? ", " + e.getData().toString() : Text.EMPTY), transferDetails.toJSON());
			throw e;
		}
		catch (Exception e) {
			LOGGER.error("", e);
			ticketFailureService.saveFailureLog(authDTO, Text.NA, "TRNSR", e.getMessage(), transferDetails.toJSON());
			throw e;
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/ticket/transfer/block", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<OrderIO> blockTicket(@PathVariable("authtoken") String authtoken, @RequestBody OrderTransferDetailsIO transferDetails) throws Exception {
		OrderIO ticketOrderIO = new OrderIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		try {
			LOGGER.info("TRS003 Ticket reschedule initiated by {} PNR {} Request {}", authDTO.getUser().getName(), transferDetails.getTicketCode(), transferDetails.toString());

			if (StringUtil.isNull(transferDetails.getPaymentPartnerCode())) {
				throw new ServiceException(ErrorCode.NO_GATEWAY_FOUND);
			}
			/** Load Permission */
			loadUserPermissions(authDTO);

			TicketDTO transferDTO = new TicketDTO();
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(transferDetails.getTransferTripCode());

			SearchDTO searchDTO = new SearchDTO();
			DateTime tripTravelDate = new DateTime(transferDetails.getTransferTravelDate());
			searchDTO.setTravelDate(tripTravelDate);

			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setCode(transferDetails.getTransferFromStation().getCode());
			searchDTO.setFromStation(fromStationDTO);

			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setCode(transferDetails.getTransferToStation().getCode());
			searchDTO.setToStation(toStationDTO);

			tripDTO.setSearch(searchDTO);
			transferDTO.setTripDTO(tripDTO);

			List<TicketDetailsDTO> transferSeatDetailsList = new ArrayList<TicketDetailsDTO>();
			for (String seatCode : transferDetails.getTransferSeatCode()) {
				TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
				detailsDTO.setSeatCode(seatCode);
				transferSeatDetailsList.add(detailsDTO);

			}
			transferDTO.setTicketDetails(transferSeatDetailsList);
			transferDTO.setMyAccountFlag(transferDetails.getMyAccountFlag());

			if (transferDetails.getTransferBoardingPoint() != null) {
				StationPointDTO boardingPointDTO = new StationPointDTO();
				boardingPointDTO.setCode(transferDetails.getTransferBoardingPoint().getCode());
				transferDTO.setBoardingPoint(boardingPointDTO);
			}
			if (transferDetails.getTransferDroppingPoint() != null) {
				StationPointDTO droppingPointDTO = new StationPointDTO();
				droppingPointDTO.setCode(transferDetails.getTransferDroppingPoint().getCode());
				transferDTO.setDroppingPoint(droppingPointDTO);
			}

			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(transferDetails.getTicketCode());
			List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
			for (String seatCode : transferDetails.getSeatCode()) {
				TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
				detailsDTO.setSeatCode(seatCode);
				seatDetailsList.add(detailsDTO);
			}
			ticketDTO.setTicketDetails(seatDetailsList);
			if (seatDetailsList.size() != transferSeatDetailsList.size()) {
				LOGGER.error("{} ErrorCode 406 Old Seats {} Given Old Seats {}", ticketDTO.getCode(), seatDetailsList.stream().map(repoTicket -> repoTicket.getSeatCode()).collect(Collectors.toList()), transferSeatDetailsList.stream().map(ticket -> ticket.getSeatCode()).collect(Collectors.toList()));
				throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
			}

			BookingDTO bookingDTO = transferService.blockTicket(authDTO, ticketDTO, transferDTO, transferDetails.getPaymentPartnerCode());
			if (bookingDTO.getTransactionAmount().compareTo(BigDecimal.ZERO) == 1) {
				TicketDTO ticket = bookingDTO.getTicketList().get(Numeric.ZERO_INT);

				OrderInitRequestDTO paymentRequest = new OrderInitRequestDTO();
				paymentRequest.setAmount(bookingDTO.getTransactionAmount().setScale(0, RoundingMode.HALF_UP));
				paymentRequest.setFirstName(ticket.getPassengerName());
				paymentRequest.setLastName(StringUtil.removeSymbol(bookingDTO.getPassengerName()));
				paymentRequest.setPartnerCode(transferDetails.getPaymentPartnerCode());
				paymentRequest.setResponseUrl(transferDetails.getResponseUrl());
				paymentRequest.setOrderCode(bookingDTO.getCode());
				paymentRequest.setOrderType(OrderTypeEM.TICKET);
				paymentRequest.setAddress1(WordUtils.capitalize(ticket.getPassengerName()) + " " + ticket.getFromStation().getName());
				paymentRequest.setMobile(ticket.getPassengerMobile());
				paymentRequest.setEmail(ticket.getPassengerEmailId());
				paymentRequest.setUdf1(ticket.getFromStation().getName() + " - " + ticket.getToStation().getName() + " " + ticket.getTripDateTime().format("YYYY-MM-DD hh:mm:ss"));
				paymentRequest.setUdf2(authDTO.getNamespaceCode());
				paymentRequest.setUdf3(authDTO.getDeviceMedium().getCode());
				paymentRequest.setUdf4(ticket.getJourneyType().getCode());
				paymentRequest.setUdf5(ApplicationConfig.getServerZoneCode());

				OrderInitStatusDTO orderInitStatusDTO = paymentRequestService.handlePgService(authDTO, paymentRequest);
				ticketOrderIO.setTransactionCode(orderInitStatusDTO.getTransactionCode());
				ticketOrderIO.setPaymentRequestUrl(orderInitStatusDTO.getPaymentRequestUrl());
				ticketOrderIO.setGatewayInputDetails(orderInitStatusDTO.getGatewayInputDetails());
				ticketOrderIO.setPaymentGatewayProcessFlag(true);
				ticketOrderIO.setGatewayCode(orderInitStatusDTO.getGatewayCode());
			}
			else {
				ticketOrderIO.setTransactionCode(bookingDTO.getCode());
				ticketOrderIO.setPaymentRequestUrl(transferDetails.getResponseUrl());
				ticketOrderIO.setPaymentGatewayProcessFlag(false);
			}
			ticketOrderIO.setCode(transferDTO.getCode());
		}
		catch (ServiceException e) {
			LOGGER.error("{} {}", transferDetails.getTicketCode(), e);
			ticketFailureService.saveFailureLog(authDTO, StringUtil.isNull(e.getErrorCode().getCode(), "Code-NA"), "TRNSR", StringUtil.isNull(e.getErrorCode().getMessage(), "Msg-NA") + (StringUtil.isNotNull(e.getData()) ? ", " + e.getData().toString() : Text.EMPTY), transferDetails.toJSON());
			throw e;
		}
		catch (Exception e) {
			LOGGER.error("{} {}", transferDetails.getTicketCode(), e);
			ticketFailureService.saveFailureLog(authDTO, Text.NA, "TRNSR", e.getMessage(), transferDetails.toJSON());
			throw e;
		}
		return ResponseIO.success(ticketOrderIO);
	}

	@RequestMapping(value = "/ticket/transfer/{transferTicketCode}/confirm", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<TicketIO> confirmTicket(@PathVariable("authtoken") String authtoken, @PathVariable("transferTicketCode") String transferTicketCode) throws Exception {
		TicketIO ticketIO = new TicketIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		try {
			LOGGER.info("TRS004 Ticket reschedule initiated by {} PNR {} ", authDTO.getUser().getName(), transferTicketCode);
			/** Load Permission */
			loadUserPermissions(authDTO);

			TicketDTO transferDTO = new TicketDTO();
			transferDTO.setCode(transferTicketCode);

			transferDTO = transferService.transferTicketConfirm(authDTO, transferDTO);

			ticketIO.setCode(transferDTO.getCode());
			StationIO fromStation = new StationIO();
			fromStation.setCode(transferDTO.getFromStation().getCode());
			fromStation.setName(transferDTO.getFromStation().getName());
			StationPointIO fromStationPoint = new StationPointIO();
			fromStationPoint.setCode(transferDTO.getBoardingPoint().getCode());
			fromStationPoint.setName(transferDTO.getBoardingPoint().getName());
			fromStationPoint.setLandmark(transferDTO.getBoardingPoint().getLandmark());
			fromStationPoint.setLongitude(transferDTO.getBoardingPoint().getLongitude());
			fromStationPoint.setNumber(transferDTO.getBoardingPoint().getNumber());
			fromStationPoint.setDateTime(transferDTO.getBoardingPointDateTime().format("YYYY-MM-DD hh:mm:ss"));
			fromStationPoint.setAddress(transferDTO.getBoardingPoint().getAddress());
			// To Station Point
			StationIO toStation = new StationIO();
			toStation.setCode(transferDTO.getFromStation().getCode());
			toStation.setName(transferDTO.getFromStation().getName());
			StationPointIO toStationPoint = new StationPointIO();
			toStationPoint.setCode(transferDTO.getDroppingPoint().getCode());
			toStationPoint.setName(transferDTO.getDroppingPoint().getName());
			toStationPoint.setLandmark(transferDTO.getDroppingPoint().getLandmark());
			toStationPoint.setLongitude(transferDTO.getDroppingPoint().getLongitude());
			toStationPoint.setNumber(transferDTO.getDroppingPoint().getNumber());
			toStationPoint.setDateTime(transferDTO.getDroppingPointDateTime().format("YYYY-MM-DD hh:mm:ss"));
			toStationPoint.setAddress(transferDTO.getDroppingPoint().getAddress());
			fromStation.setStationPoints(fromStationPoint);
			toStation.setStationPoints(toStationPoint);
			ticketIO.setFromStation(fromStation);
			ticketIO.setToStation(toStation);
			ticketIO.setTravelDate(transferDTO.getTripDate().format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setReportingTime(DateUtil.addMinituesToDate(transferDTO.getTripDate(), transferDTO.getReportingMinutes()).format("YYYY-MM-DD hh:mm:ss"));
			ticketIO.setRemarks(transferDTO.getRemarks());
			ticketIO.setPassegerEmailId(transferDTO.getPassengerEmailId());
			ticketIO.setPassegerMobleNo(transferDTO.getPassengerMobile());
			ticketIO.setTotalFare(transferDTO.getTotalFare());
			ticketIO.setTransactionDate(transferDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
			// Ticket status
			TicketStatusIO ticketStatus = new TicketStatusIO();
			ticketStatus.setCode(transferDTO.getTicketStatus().getCode());
			ticketStatus.setName(transferDTO.getTicketStatus().getDescription());
			ticketIO.setTicketStatus(ticketStatus);

			// copy User Transaction Details
			List<UserTransactionIO> userTransactionList = new ArrayList<>();
			if (transferDTO.getUserTransaction() != null) {
				UserTransactionIO userTransaction = new UserTransactionIO();
				TransactionTypeIO transactionType = new TransactionTypeIO();
				transactionType.setName(transferDTO.getUserTransaction().getTransactionType().getName());
				transactionType.setCreditDebitFlag(transferDTO.getUserTransaction().getTransactionType().getCreditDebitFlag());
				transactionType.setCode(transferDTO.getUserTransaction().getTransactionType().getCode());
				userTransaction.setCreditAmount(transferDTO.getUserTransaction().getCreditAmount());
				userTransaction.setDebitAmount(transferDTO.getUserTransaction().getDebitAmount());
				userTransaction.setTransactionAmount(transferDTO.getUserTransaction().getTransactionAmount());
				userTransactionList.add(userTransaction);
			}
			ticketIO.setUsertransaction(userTransactionList);

			// copy Cancellation Terms
			CancellationTermIO cancellationTerm = new CancellationTermIO();
			CancellationTermDTO termDTO = transferDTO.getCancellationTerm();
			cancellationTerm.setName(termDTO.getName());
			cancellationTerm.setCode(termDTO.getCode());
			List<CancellationPolicyIO> policys = new ArrayList<CancellationPolicyIO>();
			for (CancellationPolicyDTO policyDTO : termDTO.getPolicyList()) {
				CancellationPolicyIO policyIO = new CancellationPolicyIO();
				policyIO.setFromValue(policyDTO.getFromValue());
				policyIO.setToValue(policyDTO.getToValue());
				policyIO.setDeductionAmount(policyDTO.getDeductionValue());
				policyIO.setPercentageFlag(policyDTO.getPercentageFlag());
				policyIO.setPolicyPattern(policyDTO.getPolicyPattern());
				policys.add(policyIO);
			}
			cancellationTerm.setPolicyList(policys);
			ticketIO.setCancellationTerms(cancellationTerm);
		}
		catch (ServiceException e) {
			LOGGER.error("{} {}", transferTicketCode, e);
			ticketFailureService.saveFailureLog(authDTO, StringUtil.isNull(e.getErrorCode().getCode(), "Code-NA"), "TRNSR", StringUtil.isNull(e.getErrorCode().getMessage(), "Msg-NA") + (StringUtil.isNotNull(e.getData()) ? ", " + e.getData().toString() : Text.EMPTY), transferTicketCode);
			throw e;
		}
		catch (Exception e) {
			LOGGER.error("{} {}", transferTicketCode, e);
			ticketFailureService.saveFailureLog(authDTO, Text.NA, "TRNSR", e.getMessage(), transferTicketCode);
			throw e;
		}
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/auto/ticket/transfer/validate", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONArray> validateTicketAutoTransfer(@PathVariable("authtoken") String authtoken, @RequestBody Map<String, String> transferDetails) throws Exception {
		JSONArray response = new JSONArray();
		try {
			AuthDTO authDTO = authService.getAuthDTO(authtoken);

			TripDTO trip = new TripDTO();
			trip.setCode(transferDetails.get("tripCode"));

			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(DateUtil.getDateTime(transferDetails.get("travelDate")));

			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setCode(transferDetails.get("fromStationCode"));
			searchDTO.setFromStation(fromStationDTO);

			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setCode(transferDetails.get("toStationCode"));
			searchDTO.setToStation(toStationDTO);

			trip.setSearch(searchDTO);

			TripDTO transferTrip = new TripDTO();
			transferTrip.setCode(transferDetails.get("transferTripCode"));

			SearchDTO transferSearchDTO = new SearchDTO();
			transferSearchDTO.setTravelDate(DateUtil.getDateTime(transferDetails.get("transferTravelDate")));

			StationDTO transferFromStationDTO = new StationDTO();
			transferFromStationDTO.setCode(transferDetails.get("transferFromStationCode"));
			transferSearchDTO.setFromStation(transferFromStationDTO);

			StationDTO transferToStationDTO = new StationDTO();
			transferToStationDTO.setCode(transferDetails.get("transferToStationCode"));
			transferSearchDTO.setToStation(transferToStationDTO);

			transferTrip.setSearch(transferSearchDTO);

			response = transferService.validateTicketAutoTransfer(authDTO, trip, transferTrip);
		}
		catch (ServiceException e) {
			LOGGER.error("", e);
			throw e;
		}
		catch (Exception e) {
			LOGGER.error("", e);
			throw e;
		}
		return ResponseIO.success(response);
	}

	private void loadUserPermissions(AuthDTO authDTO) {
		// Permission check
		List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
		eventList.add(MenuEventEM.TICKET_MULTIPLE_TRANSFER);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);

		authDTO.getAdditionalAttribute().put(Text.RESCHEDULE, menuEventDTO != null ? String.valueOf(menuEventDTO.getEnabledFlag()) : Numeric.ZERO);
	}
}
