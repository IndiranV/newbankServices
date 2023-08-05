package org.in.com.controller.web;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.controller.commerce.io.OrderIO;
import org.in.com.controller.commerce.io.TicketDetailsIO;
import org.in.com.controller.web.io.BaseIO;
import org.in.com.controller.web.io.ResponseIO;
import org.in.com.controller.web.io.TicketMigrationDetailsIO;
import org.in.com.controller.web.io.TicketMigrationIO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketMigrationDTO;
import org.in.com.dto.TicketMigrationDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusmapService;
import org.in.com.service.TicketEditService;
import org.in.com.service.TicketService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import net.sf.json.JSONArray;

@Controller
@RequestMapping("/{authtoken}/ticket")
public class TicketEditController extends BaseController {
	@Autowired
	TicketEditService editService;
	@Autowired
	TicketService ticketService;
	@Autowired
	BusmapService busmapService;

	@RequestMapping(value = "/edit/{ticketCode}/boardingPoint/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> editBoardingPoint(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String oldBoardingPointCode, String newBoardingPointCode, int notificationFlag, String syncId) throws Exception {
		BaseIO response = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {

			if (StringUtil.isNull(ticketCode) || StringUtil.isNull(newBoardingPointCode) || StringUtil.isNull(oldBoardingPointCode)) {
				ResponseIO.failure("ED01", "Requried parameter, should not be null");
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0 || (ticketDTO.getTicketStatus().getId() != 1 && ticketDTO.getTicketStatus().getId() != 5)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			// Permission check
			List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
			Eventlist.add(MenuEventEM.BOOKING_FIND_EDIT_ALL_TCK);
			MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, Eventlist);
			if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.APP_TABLET_POB.getId() && menuEventDTO.getEnabledFlag() != Numeric.ONE_INT && ticketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}

			TripDTO tripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());
			StationPointDTO oldStationPoint = null;
			StationPointDTO newStationPoint = null;
			for (StationPointDTO pointDTO : tripDTO.getStage().getFromStation().getStationPoint()) {
				if (pointDTO.getCode().equals(oldBoardingPointCode)) {
					oldStationPoint = pointDTO;
					continue;
				}
				if (pointDTO.getCode().equals(newBoardingPointCode)) {
					newStationPoint = pointDTO;
					continue;
				}
			}
			if (newStationPoint != null && newStationPoint.getId() != ticketDTO.getBoardingPoint().getId()) {
				ticketDTO.setBoardingPoint(newStationPoint);
				ticketDTO.getBoardingPoint().setMinitues(tripDTO.getStage().getFromStation().getMinitues() + ticketDTO.getBoardingPoint().getMinitues());
				String event = "edit Boarding Point : " + (oldStationPoint != null ? oldStationPoint.getName() : "") + " changed to " + newStationPoint.getName();
				editService.editBoardingPoint(authDTO, ticketDTO, event, notificationFlag);
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}
			response.setCode(syncId);
			response.setActiveFlag(1);
		}
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/edit/{ticketCode}/dropingPoint/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> dropingPoint(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String oldDropingPointCode, String newDropingPointCode, int notificationFlag, String syncId) throws Exception {
		BaseIO response = new BaseIO();
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {

			if (StringUtil.isNull(ticketCode) || StringUtil.isNull(newDropingPointCode) || StringUtil.isNull(oldDropingPointCode)) {
				ResponseIO.failure("ED01", "Requried parameter, should not be null");
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			TripDTO tripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());
			StationPointDTO oldDropingPoint = null;
			StationPointDTO newDropingPoint = null;
			for (StationPointDTO pointDTO : tripDTO.getStage().getToStation().getStationPoint()) {
				if (pointDTO.getCode().equals(oldDropingPointCode)) {
					oldDropingPoint = pointDTO;
					continue;
				}
				if (pointDTO.getCode().equals(newDropingPointCode)) {
					newDropingPoint = pointDTO;
					continue;
				}
			}
			if (oldDropingPoint != null && newDropingPoint != null && oldDropingPoint.getId() == ticketDTO.getDroppingPoint().getId()) {
				ticketDTO.setDroppingPoint(newDropingPoint);
				String event = "edit Droping Point : " + oldDropingPoint.getName() + " changed to " + newDropingPoint.getName();
				editService.editDroppingPoint(authDTO, ticketDTO, event, notificationFlag);
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}
			response.setCode(syncId);
			response.setActiveFlag(1);
		}
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/edit/{ticketCode}/mobile/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> passengerMobile(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String oldMobileNumber, String newMobileNumber, int notificationFlag) throws Exception {

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {

			if (StringUtil.isNull(ticketCode) || StringUtil.isNull(oldMobileNumber) || StringUtil.isNull(newMobileNumber)) {
				ResponseIO.failure("ED01", "Requried parameter, should not be null");
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0 || (ticketDTO.getTicketStatus().getId() != 1 && ticketDTO.getTicketStatus().getId() != 5)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			// Permission check
			List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
			Eventlist.add(MenuEventEM.BOOKING_FIND_EDIT_ALL_TCK);
			MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, Eventlist);
			if (menuEventDTO.getEnabledFlag() != Numeric.ONE_INT && ticketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}
			if (ticketDTO.getPassengerMobile().equals(oldMobileNumber) && StringUtil.isNumeric(newMobileNumber)) {
				ticketDTO.setPassengerMobile(newMobileNumber);
				String event = "edit Mobile Number : " + oldMobileNumber + " changed to " + newMobileNumber;
				editService.editMobileNumber(authDTO, ticketDTO, event, notificationFlag);
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}

		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/edit/{ticketCode}/email/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> editEmailId(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String oldEmailId, String newEmailId) throws Exception {

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {

			if (StringUtil.isNull(ticketCode) || StringUtil.isNull(oldEmailId) || StringUtil.isNull(newEmailId)) {
				ResponseIO.failure("ED01", "Requried parameter, should not be null");
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);

			if (ticketDTO.getId() == 0 || (ticketDTO.getTicketStatus().getId() != 1 && ticketDTO.getTicketStatus().getId() != 5)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}

			/** Permission check */
			List<MenuEventEM> menuEvents = new ArrayList<MenuEventEM>();
			menuEvents.add(MenuEventEM.BOOKING_FIND_EDIT_ALL_TCK);
			MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, menuEvents);

			if (menuEventDTO.getEnabledFlag() != Numeric.ONE_INT && ticketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}
			if (ticketDTO.getPassengerEmailId().equals(oldEmailId)) {
				ticketDTO.setPassengerEmailId(newEmailId);
				String event = "edit Email : " + oldEmailId + " changed to " + newEmailId;
				editService.editEmailId(authDTO, ticketDTO, event);
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/edit/{ticketCode}/alternate/mobile/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> editAlternateMobile(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String alternateMobileNumber, int notificationFlag) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (StringUtil.isNull(ticketCode) || StringUtil.isNull(alternateMobileNumber)) {
			ResponseIO.failure("ED01", "Requried parameter, should not be null");
		}
		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
		if (ticketDTO.getId() == 0 || (ticketDTO.getTicketStatus().getId() != 1 && ticketDTO.getTicketStatus().getId() != 5)) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		// Permission check
		List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
		Eventlist.add(MenuEventEM.BOOKING_FIND_EDIT_ALL_TCK);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, Eventlist);
		if (menuEventDTO.getEnabledFlag() != Numeric.ONE_INT && ticketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}
		if ((StringUtil.isNull(ticketDTO.getAlternateMobile()) || !ticketDTO.getAlternateMobile().equals(alternateMobileNumber)) && StringUtil.isValidMobileNumber(alternateMobileNumber)) {
			ticketDTO.setAlternateMobile(alternateMobileNumber);
			String event = "edit Alternate Mobile Number : " + (StringUtil.isNotNull(ticketDTO.getAlternateMobile()) ? ticketDTO.getAlternateMobile() : Text.EMPTY) + " changed to " + alternateMobileNumber;
			editService.editAlternateMobileNumber(authDTO, ticketDTO, event, notificationFlag);
		}
		else {
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
		}

		return ResponseIO.success();
	}

	@RequestMapping(value = "/edit/{ticketCode}/seat/change/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> changePassengerSeat(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String oldSeatCode, String newSeatCode, int notificationFlag) throws Exception {

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {

			if (StringUtil.isNull(ticketCode) || StringUtil.isNull(oldSeatCode) || StringUtil.isNull(newSeatCode)) {
				ResponseIO.failure("ED01", "Requried parameter, should not be null");
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0 || (ticketDTO.getTicketStatus().getId() != 1 && ticketDTO.getTicketStatus().getId() != 5)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			// Permission check
			List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
			Eventlist.add(MenuEventEM.BOOKING_FIND_EDIT_ALL_TCK);
			MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, Eventlist);
			if (menuEventDTO.getEnabledFlag() != Numeric.ONE_INT && ticketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}
			// Busmap
			TripDTO tripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());
			ticketDTO.getTripDTO().setTripDate(tripDTO.getTripDate());
			ticketDTO.getTripDTO().setTripMinutes(tripDTO.getTripMinutes());
			BusDTO busDTO = tripDTO.getBus();
			BusSeatLayoutDTO oldSeatLayoutDTO = null;
			BusSeatLayoutDTO newSeatLayoutDTO = null;
			for (BusSeatLayoutDTO layoutDTO : busDTO.getBusSeatLayoutDTO().getList()) {
				if (layoutDTO.getCode().equals(oldSeatCode)) {
					oldSeatLayoutDTO = layoutDTO;
				}
				if (layoutDTO.getCode().equals(newSeatCode)) {
					newSeatLayoutDTO = layoutDTO;
				}
			} // update to edit seat
			if (oldSeatLayoutDTO != null && newSeatLayoutDTO != null) {
				if (newSeatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.ALLOCATED_OTHER.getId() || newSeatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.ALLOCATED_YOU.getId() || newSeatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_ALL.getId() || newSeatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_MALE.getId() || newSeatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_FEMALE.getId()) {
					editService.editChangeSeat(authDTO, ticketDTO, oldSeatLayoutDTO, newSeatLayoutDTO, notificationFlag);
				}
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}

		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/edit/v2/seat/change/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> changePassengerSeatV2(@PathVariable("authtoken") String authtoken, int notificationFlag, @RequestBody TicketMigrationIO ticketMigrationIO) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		/** Load Permission */
		loadUserPermissions(authDTO);
		TicketMigrationDTO ticketMigrationDTO = new TicketMigrationDTO();
		List<TicketMigrationDetailsDTO> ticketMigrationDetails = new ArrayList<>();
		for (TicketMigrationDetailsIO ticketMigrationDetailsIO : ticketMigrationIO.getTicketMigrationDetails()) {
			List<TicketDetailsDTO> newSeatDetailsList = new ArrayList<TicketDetailsDTO>();
			for (String seatCode : ticketMigrationDetailsIO.getMigrationSeatCode()) {
				TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
				detailsDTO.setSeatCode(seatCode);
				newSeatDetailsList.add(detailsDTO);
			}

			TicketDTO migrationDTO = new TicketDTO();
			migrationDTO.setTicketDetails(newSeatDetailsList);

			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketMigrationDetailsIO.getTicketCode());

			List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
			for (String seatCode : ticketMigrationDetailsIO.getSeatCode()) {
				TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
				detailsDTO.setSeatCode(seatCode);
				seatDetailsList.add(detailsDTO);
			}
			ticketDTO.setTicketDetails(seatDetailsList);

			if (seatDetailsList.size() != newSeatDetailsList.size()) {
				throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
			}

			TicketMigrationDetailsDTO ticketMigrationDetailsDTO = new TicketMigrationDetailsDTO();
			ticketMigrationDetailsDTO.setTicket(ticketDTO);
			ticketMigrationDetailsDTO.setMigrationTicket(migrationDTO);
			ticketMigrationDetails.add(ticketMigrationDetailsDTO);

			ticketMigrationDTO.setTicketMigrationDetails(ticketMigrationDetails);

			editService.updateSeatDetails(authDTO, notificationFlag, ticketMigrationDTO);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/migration/validate", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<List<TicketMigrationDetailsIO>> validateMigrateTicket(@PathVariable("authtoken") String authtoken, @RequestBody TicketMigrationIO migrationDetails) throws Exception {
		List<TicketMigrationDetailsIO> ticketMigrationDetailsList = new ArrayList<>();
		try {
			AuthDTO authDTO = authService.getAuthDTO(authtoken);

			/** Load Permission */
			loadUserPermissions(authDTO);

			TicketMigrationDTO ticketMigrationDTO = new TicketMigrationDTO();
			ticketMigrationDTO.setTravelDate(DateUtil.getDateTime(migrationDetails.getTravelDate()));

			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setCode(migrationDetails.getFromStation().getCode());
			ticketMigrationDTO.setFromStation(fromStationDTO);

			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setCode(migrationDetails.getToStation().getCode());
			ticketMigrationDTO.setToStation(toStationDTO);

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(migrationDetails.getTripCode());
			ticketMigrationDTO.setTrip(tripDTO);
			List<TicketMigrationDetailsDTO> ticketMigrationDetails = new ArrayList<>();
			for (TicketMigrationDetailsIO ticketMigrationDetailsIO : migrationDetails.getTicketMigrationDetails()) {
				List<TicketDetailsDTO> migrationSeatDetailsList = new ArrayList<TicketDetailsDTO>();
				for (String seatCode : ticketMigrationDetailsIO.getMigrationSeatCode()) {
					TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
					detailsDTO.setSeatCode(seatCode);
					migrationSeatDetailsList.add(detailsDTO);
				}

				TicketDTO migrationDTO = new TicketDTO();
				migrationDTO.setTicketDetails(migrationSeatDetailsList);

				TicketDTO ticketDTO = new TicketDTO();
				ticketDTO.setCode(ticketMigrationDetailsIO.getTicketCode());

				List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
				for (String seatCode : ticketMigrationDetailsIO.getSeatCode()) {
					TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
					detailsDTO.setSeatCode(seatCode);
					seatDetailsList.add(detailsDTO);
				}
				ticketDTO.setTicketDetails(seatDetailsList);

				if (seatDetailsList.size() != migrationSeatDetailsList.size()) {
					throw new ServiceException(ErrorCode.INVALID_SEAT_CODE);
				}

				TicketMigrationDetailsDTO ticketMigrationDetailsDTO = new TicketMigrationDetailsDTO();
				ticketMigrationDetailsDTO.setTicket(ticketDTO);
				ticketMigrationDetailsDTO.setMigrationTicket(migrationDTO);

				ticketMigrationDetails.add(ticketMigrationDetailsDTO);
			}
			ticketMigrationDTO.setTicketMigrationDetails(ticketMigrationDetails);

			editService.validateMigrateTicket(authDTO, ticketMigrationDTO);

			for (TicketMigrationDetailsDTO ticketMigrationDetailsDTO : ticketMigrationDTO.getTicketMigrationDetails()) {
				TicketMigrationDetailsIO ticketMigrationDetailsIO = new TicketMigrationDetailsIO();
				ticketMigrationDetailsIO.setTicketCode(ticketMigrationDetailsDTO.getTicket().getCode());
				ticketMigrationDetailsList.add(ticketMigrationDetailsIO);
			}
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw e;
		}
		return ResponseIO.success(ticketMigrationDetailsList);
	}

	@RequestMapping(value = "/auto/ticket/migration/validate", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<JSONArray> validateTicketAutoMigration(@PathVariable("authtoken") String authtoken, @RequestBody Map<String, String> migrationDetails) throws Exception {
		JSONArray response = new JSONArray();
		try {
			AuthDTO authDTO = authService.getAuthDTO(authtoken);

			TripDTO trip = new TripDTO();
			trip.setCode(migrationDetails.get("tripCode"));

			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(DateUtil.getDateTime(migrationDetails.get("travelDate")));

			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setCode(migrationDetails.get("fromStationCode"));
			searchDTO.setFromStation(fromStationDTO);

			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setCode(migrationDetails.get("toStationCode"));
			searchDTO.setToStation(toStationDTO);

			trip.setSearch(searchDTO);

			String ticketCode = migrationDetails.get("ticketCode");
			List<String> ticketList = new ArrayList<>();
			String[] ticketCodes = ticketCode.split(",");
			for (String code : ticketCodes) {
				ticketList.add(code);
			}

			response = editService.validateTicketAutoMigration(authDTO, trip, ticketList);
		}
		catch (ServiceException e) {
			throw e;
		}
		catch (Exception e) {
			throw e;
		}
		return ResponseIO.success(response);
	}

	@RequestMapping(value = "/edit/{ticketCode}/seat/passengerdetails/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> changeSeatGendar(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String seatCode, String passengerName, String age, String genderCode, int notificationFlag) throws Exception {

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {

			if (StringUtil.isNull(ticketCode) || StringUtil.isNull(seatCode)) {
				ResponseIO.failure("ED01", "Requried parameter, should not be null");
			}
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0 || (ticketDTO.getTicketStatus().getId() != 1 && ticketDTO.getTicketStatus().getId() != 5)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}
			// Permission check
			List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
			Eventlist.add(MenuEventEM.BOOKING_FIND_EDIT_ALL_TCK);
			MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, Eventlist);
			if (menuEventDTO.getEnabledFlag() != Numeric.ONE_INT && ticketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}

			String event = "edit Passenger Details : ";
			for (Iterator<TicketDetailsDTO> iterator = ticketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
				TicketDetailsDTO detailsDTO = iterator.next();
				if (detailsDTO.getSeatCode().equals(seatCode)) {
					if (StringUtil.isNotNull(genderCode)) {
						event = event + " Gender " + detailsDTO.getSeatGendar().getCode() + " changed to " + genderCode;
						detailsDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(genderCode));
					}
					if (StringUtil.isNotNull(passengerName)) {
						event = event + " Name " + detailsDTO.getPassengerName() + " changed to " + StringUtil.substring(passengerName, 59);
						detailsDTO.setPassengerName(StringUtil.substring(passengerName, 59));
					}
					if (StringUtil.isNotNull(age) && StringUtil.isNumeric(age)) {
						event = event + " Age " + detailsDTO.getPassengerAge() + " changed to " + age;
						detailsDTO.setPassengerAge(Integer.parseInt(age));
					}
				}
				else {
					iterator.remove();
				}
			}
			if (ticketDTO.getTicketDetails() != null && ticketDTO.getTicketDetails().size() == 1) {
				editService.editPassengerDetails(authDTO, ticketDTO, event, notificationFlag);
			}
			else {
				throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			}

		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/edit/{ticketCode}/seat/idproof/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> editCustomerIdProof(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, @RequestBody TicketDetailsIO ticketDetails) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);

		if (StringUtil.isNull(ticketCode) || StringUtil.isNull(ticketDetails.getSeatCode()) || StringUtil.isNull(ticketDetails.getIdProof())) {
			ResponseIO.failure("ED01", "Requried parameter, should not be null");
		}

		TicketDTO ticketDTO = new TicketDTO();
		ticketDTO.setCode(ticketCode);
		ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
		if (ticketDTO.getId() == 0 || (ticketDTO.getTicketStatus().getId() != 1 && ticketDTO.getTicketStatus().getId() != 5)) {
			throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		}
		// Permission check
		List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
		Eventlist.add(MenuEventEM.BOOKING_FIND_EDIT_ALL_TCK);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, Eventlist);
		if (menuEventDTO.getEnabledFlag() != Numeric.ONE_INT && ticketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}

		TicketAddonsDetailsDTO ticketAddonsDetails = null;
		StringBuilder event = new StringBuilder();
		if (ticketDTO.getTicketAddonsDetails() != null && !ticketDTO.getTicketAddonsDetails().isEmpty()) {
			event.append("Edit Passenger Id Proof : ");
			for (TicketAddonsDetailsDTO ticketAddonsDetailsDTO : ticketDTO.getTicketAddonsDetails()) {
				if (ticketAddonsDetailsDTO.getAddonsType().getId() == AddonsTypeEM.CUSTOMER_ID_PROOF.getId() && ticketDetails.getSeatCode().equals(ticketAddonsDetailsDTO.getSeatCode())) {
					event.append(ticketAddonsDetailsDTO.getRefferenceCode()).append(" changed to ").append(ticketDetails.getIdProof());
					ticketAddonsDetailsDTO.setRefferenceCode(ticketDetails.getIdProof());
					ticketAddonsDetails = ticketAddonsDetailsDTO;
					break;
				}
			}
		}

		if (ticketAddonsDetails != null) {
			editService.editCustomerIdProof(authDTO, ticketDTO, ticketAddonsDetails, event.toString());
		}
		else {
			TicketStatusEM ticketStatus = null;
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				if (ticketDetails.getSeatCode().equals(ticketDetailsDTO.getSeatCode())) {
					ticketStatus = ticketDetailsDTO.getTicketStatus();
					break;
				}
			}
			if (ticketStatus != null) {
				TicketAddonsDetailsDTO ticketAddonsDetailsDTO = new TicketAddonsDetailsDTO();
				ticketAddonsDetailsDTO.setRefferenceCode(ticketDetails.getIdProof());
				ticketAddonsDetailsDTO.setSeatCode(ticketDetails.getSeatCode());
				ticketAddonsDetailsDTO.setValue(BigDecimal.ZERO);
				ticketAddonsDetailsDTO.setAddonsType(AddonsTypeEM.CUSTOMER_ID_PROOF);
				ticketAddonsDetailsDTO.setTicketStatus(ticketStatus);
				ticketAddonsDetailsDTO.setRefferenceId(ticketDTO.getId());
				ticketAddonsDetailsDTO.setActiveFlag(1);

				event.append("Add Passenger Id Proof : ").append(ticketDetails.getIdProof());
				editService.addCustomerIdProof(authDTO, ticketDTO, ticketAddonsDetailsDTO, event.toString());
			}
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/{ticketCode}/transfer", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> ticketTransfer(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, @RequestBody OrderIO order, int notificationFlag) throws Exception {

		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		// if (authDTO != null) {
		//
		// if (StringUtil.isNull(ticketCode) || StringUtil.isNull(oldSeatCode)
		// || StringUtil.isNull(newSeatCode)) {
		// ResponseIO.failure("ED01", "Requried parameter, should not be null");
		// }
		// TicketDTO ticketDTO = new TicketDTO();
		// ticketDTO.setCode(ticketCode);
		// ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
		// if (ticketDTO.getId() == 0) {
		// throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
		// }
		// // Permission check
		// List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
		// Eventlist.add(MenuEventEM.BOOKING_FIND_EDIT_ALL_TCK);
		// MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, Eventlist);
		// if (menuEventDTO.getEnabledFlag() != Numeric.ONE_INT &&
		// ticketDTO.getTicketUser().getId() != authDTO.getUser().getId())
		// {
		// throw new ServiceException(ErrorCode.UNAUTHORIZED);
		// }
		// // Busmap
		// TripDTO tripDTO = busmapService.getSearchBusmap(authDTO,
		// ticketDTO.getTripDTO().getStageDTO());
		// BusDTO busDTO = tripDTO.getBus();
		// BusSeatLayoutDTO oldSeatLayoutDTO = null;
		// BusSeatLayoutDTO newSeatLayoutDTO = null;
		// for (BusSeatLayoutDTO layoutDTO :
		// busDTO.getBusSeatLayoutDTO().getList()) {
		// if (layoutDTO.getCode().equals(oldSeatCode)) {
		// oldSeatLayoutDTO = layoutDTO;
		// }
		// if (layoutDTO.getCode().equals(newSeatCode)) {
		// newSeatLayoutDTO = layoutDTO;
		// }
		// }// update to edit seat
		// if (oldSeatLayoutDTO != null && newSeatLayoutDTO != null) {
		// if (newSeatLayoutDTO.getSeatStatus().getId() ==
		// SeatStatusEM.ALLOCATED_OTHER.getId() ||
		// newSeatLayoutDTO.getSeatStatus().getId() ==
		// SeatStatusEM.ALLOCATED_YOU.getId() ||
		// newSeatLayoutDTO.getSeatStatus().getId() ==
		// SeatStatusEM.AVAILABLE_ALL.getId() ||
		// newSeatLayoutDTO.getSeatStatus().getId() ==
		// SeatStatusEM.AVAILABLE_MALE.getId() ||
		// newSeatLayoutDTO.getSeatStatus().getId() ==
		// SeatStatusEM.AVAILABLE_FEMALE.getId() &&
		// ticketDTO.getTicketDetails().size() == 1) {
		// editService.editChangeSeat(authDTO, ticketDTO, oldSeatLayoutDTO,
		// newSeatLayoutDTO);
		// }
		// }
		// else {
		// throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
		// }
		//
		// }
		return ResponseIO.success();
	}

	@RequestMapping(value = "/edit/{ticketCode}/remarks/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> remarksUpdate(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, String remarks, int notificationFlag) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			// Permission check
			List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
			Eventlist.add(MenuEventEM.BOOKING_FIND_EDIT_ALL_TCK);
			MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, Eventlist);
			if (menuEventDTO.getEnabledFlag() != Numeric.ONE_INT && ticketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}
			String event = "edit Remarks : " + (StringUtil.isNotNull(ticketDTO.getRemarks()) ? ticketDTO.getRemarks() + " changed to " + remarks : remarks);
			ticketDTO.setRemarks(remarks);
			editService.editRemarks(authDTO, ticketDTO, event, notificationFlag);
		}
		return ResponseIO.success();
	}

	@RequestMapping(value = "/edit/{ticketCode}/extra/{releaseMinutes}/update", method = RequestMethod.POST, produces = { MediaType.APPLICATION_JSON_VALUE })
	@ResponseBody
	public ResponseIO<BaseIO> editTicketExtra(@PathVariable("authtoken") String authtoken, @PathVariable("ticketCode") String ticketCode, @PathVariable("releaseMinutes") int releaseMinutes) throws Exception {
		AuthDTO authDTO = authService.getAuthDTO(authtoken);
		if (authDTO != null) {
			// TODO Have to Fix
			throw new ServiceException(ErrorCode.UNABLE_TO_UPDATE);
			// TicketDTO ticketDTO = new TicketDTO();
			// ticketDTO.setCode(ticketCode);
			//
			// TicketExtraDTO ticketExtra = new TicketExtraDTO();
			// ticketExtra.setBlockReleaseMinutes(releaseMinutes);
			// ticketDTO.setTicketExtra(ticketExtra);
			//
			// ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);

			// Permission check
			// List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
			// Eventlist.add(MenuEventEM.BOOKING_FIND_EDIT_ALL_TCK);
			// MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, Eventlist);
			// if (menuEventDTO.getEnabledFlag() != Numeric.ONE_INT &&
			// ticketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
			// throw new ServiceException(ErrorCode.UNAUTHORIZED);
			// }
			// editService.editTicketExtra(authDTO, ticketDTO);
		}
		return ResponseIO.success();
	}

	private void loadUserPermissions(AuthDTO authDTO) {
		// Permission check
		List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
		eventList.add(MenuEventEM.BOOKING_FIND_EDIT_ALL_TCK);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);

		authDTO.getAdditionalAttribute().put(Text.MIGRATION, menuEventDTO != null ? String.valueOf(menuEventDTO.getEnabledFlag()) : Numeric.ZERO);
	}
}