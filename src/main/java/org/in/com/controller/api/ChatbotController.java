package org.in.com.controller.api;

import java.util.ArrayList;
import java.util.List;

import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Text;
import org.in.com.controller.commerce.io.AddonTypeIO;
import org.in.com.controller.commerce.io.OperatorIO;
import org.in.com.controller.commerce.io.ResponseIO;
import org.in.com.controller.commerce.io.SeatStatusIO;
import org.in.com.controller.commerce.io.StationIO;
import org.in.com.controller.commerce.io.StationPointIO;
import org.in.com.controller.commerce.io.TicketAddonsDetailsIO;
import org.in.com.controller.commerce.io.TicketDetailsIO;
import org.in.com.controller.commerce.io.TicketIO;
import org.in.com.controller.commerce.io.TicketStatusIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.NamespaceIO;
import org.in.com.controller.web.io.NamespaceProfileIO;
import org.in.com.controller.web.io.UserIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.UserDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AuthService;
import org.in.com.service.NamespaceService;
import org.in.com.service.TicketService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/api/json/{accessToken}/chatbot")
public class ChatbotController {

	@Autowired
	AuthService authService;
	@Autowired
	NamespaceService namespaceService;
	@Autowired
	TicketService ticketService;

	@RequestMapping(value = "/namespace", method = RequestMethod.GET, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<NamespaceIO> getNamespaceDetails(@PathVariable("accessToken") String accessToken) throws Exception {
		NamespaceDTO namespace = getNamespace(accessToken);
		NamespaceIO namespaceIO = new NamespaceIO();
		namespaceIO.setCode(namespace.getCode());
		namespaceIO.setName(namespace.getName());
		namespaceIO.setActiveFlag(namespace.getActiveFlag());
		return ResponseIO.success(namespaceIO);
	}

	@RequestMapping(value = "/namespace/profile", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<NamespaceProfileIO> getNamespaceProfile(@PathVariable("accessToken") String accessToken) throws Exception {
		NamespaceDTO namespace = getNamespace(accessToken);

		NamespaceDTO namespaceDTO = namespaceService.getNamespace(namespace.getCode());
		NamespaceProfileIO namespaceProfile = new NamespaceProfileIO();
		namespaceProfile.setSupportNumber(namespaceDTO.getProfile().getSupportNumber());
		namespaceProfile.setWhatsappUrl(namespaceDTO.getProfile().getWhatsappUrl());
		return ResponseIO.success(namespaceProfile);
	}

	@RequestMapping(value = "/ticket/mobile/{mobileNumber}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<List<TicketIO>> getTickets(@PathVariable("accessToken") String accessToken, @PathVariable("mobileNumber") String mobileNumber) throws Exception {
		NamespaceDTO namespace = getNamespace(accessToken);

		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespace.getCode());
		authDTO.setUser(new UserDTO());

		TicketDTO ticket = new TicketDTO();
		ticket.setPassengerMobile(mobileNumber);

		List<TicketDTO> ticketList = ticketService.findTicket(authDTO, ticket);

		List<TicketIO> tickets = new ArrayList<>();
		for (TicketDTO ticketDTO : ticketList) {
			TicketIO ticketIO = new TicketIO();
			ticketIO.setTicketCode(ticketDTO.getCode());
			ticketIO.setBookingCode(ticketDTO.getBookingCode());
			ticketIO.setRelatedTicketCode(ticketDTO.getRelatedTicketCode());

			StationIO fromStationIO = new StationIO();
			fromStationIO.setCode(ticketDTO.getFromStation().getCode());
			fromStationIO.setName(ticketDTO.getFromStation().getName());
			ticketIO.setFromStation(fromStationIO);

			StationIO toStationIO = new StationIO();
			toStationIO.setCode(ticketDTO.getToStation().getCode());
			toStationIO.setName(ticketDTO.getToStation().getName());
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
			tickets.add(ticketIO);
		}
		return ResponseIO.success(tickets);
	}

	@RequestMapping(value = "/ticket/{ticketCode}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<TicketIO> getTicket(@PathVariable("accessToken") String accessToken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		NamespaceDTO namespace = getNamespace(accessToken);

		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespace.getCode());

		TicketDTO ticket = new TicketDTO();
		ticket.setCode(ticketCode);

		ticketService.showTicket(authDTO, ticket);

		TicketIO ticketIO = new TicketIO();

		StationIO fromStationIO = new StationIO();
		StationIO toStationIO = new StationIO();
		StationPointIO boardingPointIO = new StationPointIO();
		StationPointIO droppingPointIO = new StationPointIO();
		List<StationPointIO> boardingList = new ArrayList<StationPointIO>();
		List<StationPointIO> alightingList = new ArrayList<StationPointIO>();
		List<TicketDetailsIO> ticketDetailsIO = new ArrayList<TicketDetailsIO>();

		// Mapping from station and boarding point
		boardingPointIO.setCode(ticket.getBoardingPoint().getCode());
		boardingPointIO.setName(ticket.getBoardingPoint().getName());
		boardingPointIO.setAddress(ticket.getBoardingPoint().getAddress());
		boardingPointIO.setLandmark(ticket.getBoardingPoint().getLandmark());
		boardingPointIO.setNumber(ticket.getBoardingPoint().getNumber());
		boardingPointIO.setLongitude(ticket.getBoardingPoint().getLongitude());
		boardingPointIO.setLatitude(ticket.getBoardingPoint().getLatitude());
		boardingPointIO.setDateTime(DateUtil.addMinituesToDate(ticket.getTripDate(), ticket.getBoardingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
		boardingList.add(boardingPointIO);

		fromStationIO.setCode(ticket.getFromStation().getCode());
		fromStationIO.setName(ticket.getFromStation().getName());
		fromStationIO.setStationPoint(boardingList);

		// Mapping to station and alighting point
		droppingPointIO.setCode(ticket.getDroppingPoint().getCode());
		droppingPointIO.setName(ticket.getDroppingPoint().getName());
		droppingPointIO.setAddress(ticket.getDroppingPoint().getAddress());
		droppingPointIO.setLandmark(ticket.getDroppingPoint().getLandmark());
		droppingPointIO.setNumber(ticket.getDroppingPoint().getNumber());
		droppingPointIO.setLongitude(ticket.getDroppingPoint().getLongitude());
		droppingPointIO.setLatitude(ticket.getDroppingPoint().getLatitude());
		droppingPointIO.setDateTime(DateUtil.addMinituesToDate(ticket.getTripDate(), ticket.getTravelMinutes() + ticket.getDroppingPoint().getMinitues()).format("YYYY-MM-DD hh:mm:ss"));
		alightingList.add(droppingPointIO);

		toStationIO.setCode(ticket.getToStation().getCode());
		toStationIO.setName(ticket.getToStation().getName());
		toStationIO.setStationPoint(alightingList);
		ticketIO.setFromStation(fromStationIO);
		ticketIO.setToStation(toStationIO);

		// Mapping Operator Details
		OperatorIO operatorIO = new OperatorIO();
		operatorIO.setCode(authDTO.getNamespace().getCode());
		operatorIO.setName(authDTO.getNamespace().getName());
		ticketIO.setOperator(operatorIO);

		// Mapping passenger details
		if (ticket.getTicketDetails() != null) {
			for (TicketDetailsDTO dto : ticket.getTicketDetails()) {
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
				tickDetailsIO.setPassengerGendar(dto.getSeatGendar().getCode());
				tickDetailsIO.setSeatFare(dto.getSeatFare());
				tickDetailsIO.setServiceTax(dto.getAcBusTax());
				tickDetailsIO.setCancellationCharges(dto.getCancellationCharges());
				tickDetailsIO.setCancellationChargeTax(dto.getCancellationChargeTax());
				tickDetailsIO.setRefundAmount(dto.getRefundAmount());
				ticketDetailsIO.add(tickDetailsIO);
			}
		}
		// Ticket Addons Details
		if (ticket.getTicketAddonsDetails() != null) {
			List<TicketAddonsDetailsIO> ticketAddonsDetailsIO = new ArrayList<TicketAddonsDetailsIO>();
			for (TicketAddonsDetailsDTO dto : ticket.getTicketAddonsDetails()) {
				TicketAddonsDetailsIO addonsDetailsIO = new TicketAddonsDetailsIO();
				addonsDetailsIO.setSeatCode(dto.getSeatCode());
				addonsDetailsIO.setCode(dto.getRefferenceCode());

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

		ticketIO.setCode(ticket.getCode());
		ticketIO.setBookingCode(ticket.getBookingCode());
		ticketIO.setTravelDate(ticket.getTripDateTime().format("YYYY-MM-DD hh:mm:ss"));
		ticketIO.setTripDate(ticket.getTripDTO().getTripDateTimeV2().format("YYYY-MM-DD hh:mm:ss"));
		// Ticket status
		TicketStatusIO ticketStatusIO = new TicketStatusIO();
		ticketStatusIO.setCode(ticket.getTicketStatus().getCode());
		ticketStatusIO.setName(ticket.getTicketStatus().getDescription());
		ticketIO.setTicketStatus(ticketStatusIO);
		ticketIO.setTravelTime(ticket.getTripTime());
		ticketIO.setTripCode(ticket.getTripDTO().getCode());
		ticketIO.setTripStageCode(ticket.getTripDTO().getStage().getCode());
		ticketIO.setPassegerMobleNo(ticket.getPassengerMobile());
		ticketIO.setPassegerEmailId(ticket.getPassengerEmailId());
		ticketIO.setTotalFare(ticket.getTotalFare());
		ticketIO.setServiceNo(ticket.getServiceNo());
		ticketIO.setReportingTime(ticket.getReportingTime());

		ticketIO.setJourneyType(ticket.getJourneyType().getCode());
		ticketIO.setDeviceMedium(ticket.getDeviceMedium().getCode());
		ticketIO.setTicketDetails(ticketDetailsIO);
		ticketIO.setRemarks(StringUtil.isNull(ticket.getRemarks(), Text.EMPTY));
		ticketIO.setTransactionDate(ticket.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));

		ticketIO.setActiveFlag(ticket.getActiveFlag());
		if (ticket.getTicketUser() != null) {
			UserIO userIO = new UserIO();
			userIO.setName(ticket.getTicketUser().getName());
			userIO.setCode(ticket.getTicketUser().getCode());
			userIO.setMobile(ticket.getTicketUser().getMobile());
			ticketIO.setUser(userIO);
		}
		ticketIO.setRelatedTicketCode(ticket.getRelatedTicketCode());
		return ResponseIO.success(ticketIO);
	}

	@RequestMapping(value = "/ticket/{ticketCode}/trackbus", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
	@ResponseBody
	public ResponseIO<BaseIO> getTrackbusLink(@PathVariable("accessToken") String accessToken, @PathVariable("ticketCode") String ticketCode) throws Exception {
		NamespaceDTO namespace = getNamespace(accessToken);

		AuthDTO authDTO = new AuthDTO();
		authDTO.setNamespaceCode(namespace.getCode());

		TicketDTO ticket = new TicketDTO();
		ticket.setCode(ticketCode);

		ticketService.getTicketStatus(authDTO, ticket);

		BaseIO base = new BaseIO();
		base.setCode("http://m.trackbus.in?p=" + ticket.getCode() + "&z=" + ApplicationConfig.getServerZoneCode() + "&zone=" + ApplicationConfig.getServerZoneCode() + "&n=" + namespace.getCode());

		return ResponseIO.success(base);
	}

	private NamespaceDTO getNamespace(String accessToken) {
		NamespaceDTO namespaceDTO = namespaceService.getNamespaceByContextToken(accessToken);
		if (namespaceDTO.getId() == 0) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		return namespaceDTO;
	}
}
