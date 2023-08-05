package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import java.util.stream.Collectors;

import org.in.com.cache.CacheCentral;
import org.in.com.cache.TripCache;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.TicketEditDAO;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageFareDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TicketMigrationDTO;
import org.in.com.dto.TicketMigrationDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.enumeration.BusCategoryTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.NotificationSubscriptionTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.dto.enumeration.TripActivitiesEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusService;
import org.in.com.service.BusmapService;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.service.TicketEditService;
import org.in.com.service.TicketService;
import org.in.com.service.TripHelperService;
import org.in.com.service.TripService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Iterables;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Service
public class TicketEditImpl extends CacheCentral implements TicketEditService {
	private static final Logger logger = LoggerFactory.getLogger(TicketEditImpl.class);
	@Autowired
	TripService tripService;
	@Autowired
	TicketService ticketService;
	@Autowired
	NotificationService notificationService;
	@Autowired
	BusService busService;
	@Autowired
	ScheduleDynamicStageFareService dynamicFareService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	BusmapService busmapService;
	@Autowired
	TripHelperService tripHelperService;

	@Override
	public boolean editBoardingPoint(AuthDTO authDTO, TicketDTO ticketDTO, String event, int notificationFlag) {
		logger.info("Edit boarding point");
		TicketEditDAO editDAO = new TicketEditDAO();
		editDAO.editBoardingPoint(authDTO, ticketDTO, event);
		// Update trip seat details
		TripDAO tripDAO = new TripDAO();
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, ticketDTO);
		// Change required data
		for (Iterator<TicketDetailsDTO> iterator = seatList.iterator(); iterator.hasNext();) {
			TicketDetailsDTO seatDetailsDTO = iterator.next();
			seatDetailsDTO.setBoardingPointName(ticketDTO.getBoardingPoint().getName());

			if (StringUtil.isNotNull(seatDetailsDTO.getStationPoint())) {
				String stationPoint = BitsUtil.updateStationPoint(seatDetailsDTO.getStationPoint(), "BOARDING", ticketDTO.getBoardingPoint());
				seatDetailsDTO.setStationPoint(stationPoint);
			}
			seatDetailsDTO.getTicketExtra().setEditBoardingPoint(1);
		}
		// Save data
		ticketDTO.setTicketDetails(seatList);
		tripDAO.updateTripSeatDetails(authDTO, ticketDTO);
		// Clear Redis Trip cache
		tripService.clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());

		// Fire SMS
		if (notificationFlag == Numeric.ONE_INT) {
			notificationService.sendTicketUpdateSMS(authDTO, ticketDTO);
		}
		return true;
	}

	@Override
	public boolean editDroppingPoint(AuthDTO authDTO, TicketDTO ticketDTO, String event, int notificationFlag) {
		logger.info("Edit Droping point");
		TicketEditDAO editDAO = new TicketEditDAO();
		editDAO.editDropingPoint(authDTO, ticketDTO, event);

		// Update trip seat details
		TripDAO tripDAO = new TripDAO();
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, ticketDTO);

		// Change required data
		for (Iterator<TicketDetailsDTO> iterator = seatList.iterator(); iterator.hasNext();) {
			TicketDetailsDTO seatDetailsDTO = iterator.next();
			if (StringUtil.isNotNull(seatDetailsDTO.getStationPoint())) {
				String stationPoint = BitsUtil.updateStationPoint(seatDetailsDTO.getStationPoint(), "DROPPING", ticketDTO.getDroppingPoint());
				seatDetailsDTO.setStationPoint(stationPoint);
			}
			seatDetailsDTO.getTicketExtra().setEditDroppingPoint(1);
		}

		// Save data
		ticketDTO.setTicketDetails(seatList);
		tripDAO.updateTripSeatDetails(authDTO, ticketDTO);

		// Clear Redis Trip cache
		tripService.clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
		// Fire SMS
		if (notificationFlag == Numeric.ONE_INT) {
			notificationService.sendTicketUpdateSMS(authDTO, ticketDTO);
		}
		return true;
	}

	@Override
	public boolean editPassengerDetails(AuthDTO authDTO, TicketDTO ticketDTO, String event, int notificationFlag) {
		logger.info("Edit Seat Gender");
		TicketEditDAO editDAO = new TicketEditDAO();
		editDAO.editPassengerDetails(authDTO, ticketDTO, event);
		// Update trip seat details
		TripDAO tripDAO = new TripDAO();
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, ticketDTO);
		// Change required data
		for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
			for (Iterator<TicketDetailsDTO> iterator = seatList.iterator(); iterator.hasNext();) {
				TicketDetailsDTO seatDetailsDTO = iterator.next();
				if (seatDetailsDTO.getSeatCode().equals(ticketDetailsDTO.getSeatCode())) {
					seatDetailsDTO.setSeatGendar(ticketDetailsDTO.getSeatGendar());
					seatDetailsDTO.setPassengerName(ticketDetailsDTO.getPassengerName());
					seatDetailsDTO.setPassengerAge(ticketDetailsDTO.getPassengerAge());
					seatDetailsDTO.getTicketExtra().setEditPassengerDetails(1);
				}
				else {
					iterator.remove();
				}
			}
		}
		// Save data
		ticketDTO.setTicketDetails(seatList);
		tripDAO.updateTripSeatDetails(authDTO, ticketDTO);
		// Clear Redis Trip cache
		tripService.clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
		busService.getBus(authDTO, ticketDTO.getTripDTO().getBus());

		// Fire SMS
		if (notificationFlag == Numeric.ONE_INT) {
			notificationService.sendTicketUpdateSMS(authDTO, ticketDTO);
		}
		return true;
	}

	@Override
	public boolean editChangeSeat(AuthDTO authDTO, TicketDTO ticketDTO, BusSeatLayoutDTO oldSeatLayoutDTO, BusSeatLayoutDTO newSeatLayoutDTO, int notificationFlag) {
		List<String> stageCodeList = new ArrayList<>();
		stageCodeList.add(ticketDTO.getTripDTO().getStage().getCode());
		ticketDTO.getTripDTO().setReleatedStageCodeList(stageCodeList);
		boolean seatFound = false;
		logger.info("Change seat");
		for (Iterator<TicketDetailsDTO> iterator = ticketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
			TicketDetailsDTO detailsDTO = iterator.next();
			if (detailsDTO.getSeatCode().equals(oldSeatLayoutDTO.getCode())) {
				detailsDTO.setSeatCode(newSeatLayoutDTO.getCode());
				detailsDTO.setSeatName(newSeatLayoutDTO.getName());
				seatFound = true;
			}
			else {
				iterator.remove();
			}
		}
		boolean seatDouplicateEntry = ticketService.checkSeatDuplicateEntryV2(authDTO, ticketDTO);
		if (seatDouplicateEntry) {
			throw new ServiceException(ErrorCode.DOUBLE_ENTRY_VALIDATION_FAIL);
		}
		String event = "edit Change Seat : " + oldSeatLayoutDTO.getName() + " changed to " + newSeatLayoutDTO.getName();
		// Update Ticket details
		if (seatFound) {
			TicketEditDAO editDAO = new TicketEditDAO();
			editDAO.editChangeSeat(authDTO, ticketDTO, event);
		}

		// Update trip seat details
		TripDAO tripDAO = new TripDAO();
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, ticketDTO);
		seatFound = false; // Change required data
		for (Iterator<TicketDetailsDTO> iterator = seatList.iterator(); iterator.hasNext();) {
			TicketDetailsDTO seatDetailsDTO = iterator.next();
			if (seatDetailsDTO.getSeatCode().equals(oldSeatLayoutDTO.getCode())) {
				seatDetailsDTO.setSeatCode(newSeatLayoutDTO.getCode());
				seatDetailsDTO.setSeatName(newSeatLayoutDTO.getName());
				seatDetailsDTO.getTicketExtra().setEditChangeSeat(1);
				seatFound = true;
			}
			else {
				iterator.remove();
			}
		}
		// Save data
		if (seatFound) {
			ticketDTO.setTicketDetails(seatList);
			tripDAO.updateTripSeatDetails(authDTO, ticketDTO);
			// Clear Redis Trip cache
			tripService.clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());

			// Fire SMS
			if (notificationFlag == Numeric.ONE_INT) {
				notificationService.sendTicketUpdateSMS(authDTO, ticketDTO);
			}
		}
		return true;
	}

	@Override
	public boolean editCustomerIdProof(AuthDTO authDTO, TicketDTO ticketDTO, TicketAddonsDetailsDTO ticketAddonsDetails, String event) {
		logger.info("Edit Customer Id Proof");

		TicketEditDAO editDAO = new TicketEditDAO();
		editDAO.editCustomerIdProof(authDTO, ticketDTO, ticketAddonsDetails, event);
		return true;
	}

	@Override
	public void addCustomerIdProof(AuthDTO authDTO, TicketDTO ticketDTO, TicketAddonsDetailsDTO ticketAddonsDetails, String event) {
		logger.info("Add Customer Id Proof");

		TicketEditDAO editDAO = new TicketEditDAO();
		editDAO.addCustomerIdProof(authDTO, ticketDTO, ticketAddonsDetails, event);
	}

	public boolean editChangeSeatV2(AuthDTO authDTO, TicketDTO ticketDTO, Map<String, BusSeatLayoutDTO> seatLayoutMap, int notificationFlag) {
		List<String> stageCodeList = new ArrayList<>();
		stageCodeList.add(ticketDTO.getTripDTO().getStage().getCode());
		ticketDTO.getTripDTO().setReleatedStageCodeList(stageCodeList);
		boolean seatFound = false;
		logger.info("Change seat");
		StringBuilder seatLayout = new StringBuilder();
		for (Iterator<TicketDetailsDTO> iterator = ticketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
			TicketDetailsDTO detailsDTO = iterator.next();
			BusSeatLayoutDTO newSeatLayoutDTO = seatLayoutMap.get(detailsDTO.getSeatCode());
			if (newSeatLayoutDTO != null) {
				if (seatLayout.length() > 0) {
					seatLayout.append(Text.COMMA);
				}
				seatLayout.append(detailsDTO.getSeatName() + " changed to " + newSeatLayoutDTO.getName());
				detailsDTO.setName(detailsDTO.getSeatName());
				detailsDTO.setTicketStatus(TicketStatusEM.TICKET_SEAT_SWAP);

				detailsDTO.setSeatCode(newSeatLayoutDTO.getCode());
				detailsDTO.setSeatName(newSeatLayoutDTO.getName());
				seatFound = true;
			}
			else {
				iterator.remove();
			}
		}
		String event = "edit Change Seat : " + seatLayout.toString();
		// Update Ticket details
		if (seatFound) {
			TicketEditDAO editDAO = new TicketEditDAO();
			editDAO.editChangeSeat(authDTO, ticketDTO, event);
		}

		// Update trip seat details
		TripDAO tripDAO = new TripDAO();
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, ticketDTO);
		seatFound = false;
		// Change required data
		for (Iterator<TicketDetailsDTO> iterator = seatList.iterator(); iterator.hasNext();) {
			TicketDetailsDTO seatDetailsDTO = iterator.next();
			BusSeatLayoutDTO newSeatLayoutDTO = seatLayoutMap.get(seatDetailsDTO.getSeatCode());
			if (newSeatLayoutDTO != null) {
				seatDetailsDTO.setSeatCode(newSeatLayoutDTO.getCode());
				seatDetailsDTO.setSeatName(newSeatLayoutDTO.getName());
				seatDetailsDTO.getTicketExtra().setEditChangeSeat(1);
				seatFound = true;
			}
			else {
				iterator.remove();
			}
		}
		// Save data
		if (seatFound) {
			List<TicketDetailsDTO> ticketSeatDetails = ticketDTO.getTicketDetails();
			ticketDTO.setTicketDetails(seatList);
			tripDAO.updateTripSeatDetails(authDTO, ticketDTO);
			// Clear Redis Trip cache
			tripService.clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());

			// Fire SMS
			if (notificationFlag == Numeric.ONE_INT) {
				notificationService.sendTicketUpdateSMS(authDTO, ticketDTO);
			}
			// Push to DPE
			if (authDTO.getNamespace().getProfile().getDynamicPriceProviders().size() != 0) {
				ticketDTO.setTicketDetails(ticketSeatDetails);
				dynamicFareService.updateTicketStatus(authDTO, ticketDTO);
			}

			// Push Notification
			if (authDTO.getNamespace().getProfile().isNotificationSubscriptionEnabled(NotificationSubscriptionTypeEM.SEAT_EDIT)) {
				notificationPushService.pushSeatEditNotification(authDTO, ticketDTO, seatLayout.toString());
			}
		}
		return true;
	}

	@Override
	public boolean editMobileNumber(AuthDTO authDTO, TicketDTO ticketDTO, String event, int notificationFlag) {
		// Update Ticket details
		TicketEditDAO editDAO = new TicketEditDAO();
		editDAO.editMobileNumber(authDTO, ticketDTO, event);

		// Update trip seat details
		TripDAO tripDAO = new TripDAO();
		List<TicketDetailsDTO> seatList = tripDAO.getTripStageSeatsDetails(authDTO, ticketDTO);
		// Change required data
		for (Iterator<TicketDetailsDTO> iterator = seatList.iterator(); iterator.hasNext();) {
			TicketDetailsDTO seatDetailsDTO = iterator.next();
			seatDetailsDTO.setContactNumber(ticketDTO.getPassengerMobile());
			seatDetailsDTO.getTicketExtra().setEditMobileNumber(1);
		}
		// Save data
		ticketDTO.setTicketDetails(seatList);
		tripDAO.updateTripSeatDetails(authDTO, ticketDTO);

		// Clear Redis Trip cache
		tripService.clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
		busService.getBus(authDTO, ticketDTO.getTripDTO().getBus());
		// Fire SMS
		if (notificationFlag == Numeric.ONE_INT) {
			notificationService.sendTicketUpdateSMS(authDTO, ticketDTO);
		}
		return true;
	}

	@Override
	public boolean editEmailId(AuthDTO authDTO, TicketDTO ticketDTO, String event) {
		// Update Ticket details
		TicketEditDAO editDAO = new TicketEditDAO();
		editDAO.editEmailId(authDTO, ticketDTO, event);

		// Clear Redis Trip cache
		tripService.clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
		return true;
	}

	@Override
	public boolean editAlternateMobileNumber(AuthDTO authDTO, TicketDTO ticketDTO, String event, int notificationFlag) {
		// Update Ticket details
		TicketEditDAO editDAO = new TicketEditDAO();
		editDAO.editAlternateMobileNumber(authDTO, ticketDTO, event);

		// Clear Redis Trip cache
		tripService.clearBookedBlockedSeatsCache(authDTO, ticketDTO.getTripDTO());
		busService.getBus(authDTO, ticketDTO.getTripDTO().getBus());
		// Fire SMS
		// if (notificationFlag == Numeric.ONE_INT) {
		// notificationService.sendTicketUpdateSMS(authDTO, ticketDTO);
		// }
		return true;
	}

	@Override
	public boolean editRemarks(AuthDTO authDTO, TicketDTO ticketDTO, String event, int notificationFlag) {
		// Update Ticket details
		TicketEditDAO editDAO = new TicketEditDAO();
		editDAO.editRemarks(authDTO, ticketDTO, event);

		// Fire SMS
		if (notificationFlag == Numeric.ONE_INT) {
			notificationService.sendTicketUpdateSMS(authDTO, ticketDTO);
		}
		return true;

	}

	@Override
	public void editTicketExtra(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketEditDAO editDAO = new TicketEditDAO();
		editDAO.editTicketExtra(authDTO, ticketDTO);
	}

	// update migrated seats and edited seats
	@Override
	public void updateSeatDetails(AuthDTO authDTO, int notificationFlag, TicketMigrationDTO ticketMigrationDTO) {
		TripDTO tripDTO = null;
		BusDTO busDTO = null;
		for (TicketMigrationDetailsDTO ticketDetails : ticketMigrationDTO.getTicketMigrationDetails()) {
			List<String> newSeatCodes = ticketDetails.getMigrationTicket().getTicketDetails().stream().map(newSeatCode -> newSeatCode.getSeatCode()).collect(Collectors.toList());
			List<String> oldSeatCodes = ticketDetails.getTicket().getTicketDetails().stream().map(oldSeatCode -> oldSeatCode.getSeatCode()).collect(Collectors.toList());

			if (StringUtil.isNull(ticketDetails.getTicket().getCode()) || StringUtil.isNull(ticketDetails.getTicket().getCode()) || StringUtil.isNull(newSeatCodes)) {
				throw new ServiceException(ErrorCode.REQUIRED_PARAMAETER_CANNOT_BE_NULL);
			}

			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketDetails.getTicket().getCode());
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			if (ticketDTO.getId() == 0 || (ticketDTO.getTicketStatus().getId() != 1 && ticketDTO.getTicketStatus().getId() != 5)) {
				throw new ServiceException(ErrorCode.INVALID_TICKET_CODE);
			}

			if (!Numeric.ONE.equals(authDTO.getAdditionalAttribute().get(Text.MIGRATION)) && ticketDTO.getTicketUser().getId() != authDTO.getUser().getId()) {
				throw new ServiceException(ErrorCode.UNAUTHORIZED);
			}

			// Allow only BO and PBL Seats
			for (Iterator<TicketDetailsDTO> iterator = ticketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
				TicketDetailsDTO ticketDetailsDTO = iterator.next();
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					continue;
				}
				iterator.remove();
			}

			// Busmap
			if (tripDTO == null) {
				tripDTO = busmapService.getSearchBusmapV3(authDTO, ticketDTO.getTripDTO());
				busDTO = tripDTO.getBus();
			}
			Map<String, BusSeatLayoutDTO> seatLayoutMap = new HashMap<>();

			if (oldSeatCodes.size() == newSeatCodes.size()) {
				Iterator<String> newSeatIterator = newSeatCodes.iterator();
				for (String oldSeat : oldSeatCodes) {
					if (newSeatIterator.hasNext()) {
						String newSeat = newSeatIterator.next();
						BusSeatLayoutDTO newSeatLayoutDTO = null;
						for (BusSeatLayoutDTO layoutDTO : busDTO.getBusSeatLayoutDTO().getList()) {
							if (layoutDTO.getCode().equals(newSeat)) {
								newSeatLayoutDTO = layoutDTO;
								break;
							}
						}
						if (newSeatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.ALLOCATED_OTHER.getId() || newSeatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.ALLOCATED_YOU.getId() || newSeatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_ALL.getId() || newSeatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_MALE.getId() || newSeatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.AVAILABLE_FEMALE.getId()) {
							seatLayoutMap.put(oldSeat, newSeatLayoutDTO);
						}
					}
				}
			}
			editChangeSeatV2(authDTO, ticketDTO, seatLayoutMap, notificationFlag);
		}
	}

	@Override
	public void validateMigrateTicket(AuthDTO authDTO, TicketMigrationDTO ticketMigrationDTO) {
		/** Trip */
		TripCache tripCache = new TripCache();
		TripDTO trip = tripCache.getTripDTO(authDTO, ticketMigrationDTO.getTrip());
		if (authDTO.getNamespaceCode().equals(authDTO.getNativeNamespaceCode()) && DateUtil.NOW().gt(trip.getTripDateTimeV2())) {
			throw new ServiceException(ErrorCode.TRIP_CLOSED_NOT_ALLOW_BOOKING);
		}

		if (!Numeric.ONE.equals(authDTO.getAdditionalAttribute().get(Text.MIGRATION))) {
			throw new ServiceException(ErrorCode.UNAUTHORIZED);
		}

		Map<String, BusSeatLayoutDTO> fareMap = new HashMap<String, BusSeatLayoutDTO>();
		Map<String, String> seatCodeMap = new HashMap<>();
		Map<String, BusSeatLayoutDTO> seatGenderMap = new HashMap<>();
		TripDTO tripDTO = getTripDetails(authDTO, ticketMigrationDTO, fareMap, seatCodeMap, seatGenderMap);

		StringBuilder errors = new StringBuilder();
		for (TicketMigrationDetailsDTO ticketMigrationDetailsDTO : ticketMigrationDTO.getTicketMigrationDetails()) {
			TicketDTO repoTicketDTO = new TicketDTO();
			repoTicketDTO.setCode(ticketMigrationDetailsDTO.getTicket().getCode());
			Stack<TicketDetailsDTO> stack = new Stack<TicketDetailsDTO>();

			ticketService.getTicketStatus(authDTO, repoTicketDTO);

			List<TicketDetailsDTO> seatDetailsList = new ArrayList<TicketDetailsDTO>();
			for (Iterator<TicketDetailsDTO> iterator = repoTicketDTO.getTicketDetails().iterator(); iterator.hasNext();) {
				TicketDetailsDTO detailsDTO = iterator.next();
				if (detailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId() || detailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
					for (TicketDetailsDTO ticketDetailsDTO : ticketMigrationDetailsDTO.getTicket().getTicketDetails()) {
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

			if (seatDetailsList.size() != ticketMigrationDetailsDTO.getTicket().getTicketDetails().size()) {
				errors.append(ticketMigrationDetailsDTO.getTicket().getCode()).append(" The selected seats are not valid|");
				continue;
			}
			if (seatDetailsList.size() != ticketMigrationDetailsDTO.getMigrationTicket().getTicketDetails().size()) {
				errors.append(ticketMigrationDetailsDTO.getTicket().getCode()).append(" The selected migrated seats are not valid|");
				continue;
			}
			// copy Seat status and name
			for (TicketDetailsDTO transferSeatDetailsDTO : ticketMigrationDetailsDTO.getMigrationTicket().getTicketDetails()) {
				TicketDetailsDTO ticketDetailsDTO = stack.pop();
				transferSeatDetailsDTO.setSeatGendar(ticketDetailsDTO.getSeatGendar());
				transferSeatDetailsDTO.setTicketStatus(ticketDetailsDTO.getTicketStatus());
				transferSeatDetailsDTO.setPassengerAge(ticketDetailsDTO.getPassengerAge());
				transferSeatDetailsDTO.setPassengerName(ticketDetailsDTO.getPassengerName());
			}
			validateSeatGender(authDTO, tripDTO, ticketMigrationDetailsDTO, fareMap, seatCodeMap, seatGenderMap, errors);
			if (StringUtil.isNotNull(errors.toString())) {
				throw new ServiceException(ErrorCode.INVALID_PASSENGER_GENDER, errors.toString());
			}
		}
	}

	private void validateSeatGender(AuthDTO authDTO, TripDTO tripDTO, TicketMigrationDetailsDTO ticketMigrationDetailsDTO, Map<String, BusSeatLayoutDTO> fareMap, Map<String, String> seatCodeMap, Map<String, BusSeatLayoutDTO> seatGenderMap, StringBuilder errors) {
		List<BusSeatLayoutDTO> bookedSeats = new ArrayList<>();
		for (TicketDetailsDTO ticketDetailsDTO : ticketMigrationDetailsDTO.getMigrationTicket().getTicketDetails()) {
			BusSeatLayoutDTO seatLayoutDTO = fareMap.get(ticketDetailsDTO.getSeatCode());
			if (seatLayoutDTO == null) {
				errors.append(ticketMigrationDetailsDTO.getTicket().getCode()).append(" ").append(ticketDetailsDTO.getSeatName()).append(" [").append(ticketDetailsDTO.getSeatCode()).append("] is invalid|");
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
							errors.append(ticketMigrationDetailsDTO.getTicket().getCode()).append(" ").append(busSeatLayoutDTO.getName()).append(" [").append(ticketDetailsDTO.getSeatCode()).append("] invalid gender|");
							continue;
						}
					}
					/** Left Side seat */
					else if (seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_FEMALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.FEMALE.getId())) {
							errors.append(ticketMigrationDetailsDTO.getTicket().getCode()).append(" ").append(busSeatLayoutDTO.getName()).append(" [").append(ticketDetailsDTO.getSeatCode()).append("] invalid gender|");
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
							errors.append(ticketMigrationDetailsDTO.getTicket().getCode()).append(" ").append(busSeatLayoutDTO.getName()).append(" [").append(ticketDetailsDTO.getSeatCode()).append("] invalid gender|");
							continue;
						}
					}
					/** Left Side seat */
					else if (seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
						BusSeatLayoutDTO layoutDTO = seatGenderMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
						if (layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_ALL.getId() && layoutDTO.getSeatStatus().getId() != SeatStatusEM.AVAILABLE_MALE.getId() && (layoutDTO.getSeatGendar() != null && layoutDTO.getSeatGendar().getId() != SeatGendarEM.MALE.getId())) {
							errors.append(ticketMigrationDetailsDTO.getTicket().getCode()).append(" ").append(busSeatLayoutDTO.getName()).append(" [").append(ticketDetailsDTO.getSeatCode()).append("] invalid gender|");
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
					errors.append(ticketMigrationDetailsDTO.getTicket().getCode()).append(" ").append(ticketDetailsDTO.getSeatName()).append(" [").append(ticketDetailsDTO.getSeatCode()).append("] invalid gender|");
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

	private TripDTO getTripDetails(AuthDTO authDTO, TicketMigrationDTO ticketMigrationDTO, Map<String, BusSeatLayoutDTO> fareMap, Map<String, String> seatCodeMap, Map<String, BusSeatLayoutDTO> seatGenderMap) {
		SearchDTO searchDTO = new SearchDTO();
		searchDTO.setTravelDate(ticketMigrationDTO.getTravelDate());
		searchDTO.setFromStation(ticketMigrationDTO.getFromStation());
		searchDTO.setToStation(ticketMigrationDTO.getToStation());
		ticketMigrationDTO.getTrip().setSearch(searchDTO);

		TripDTO returnTripDTO = busmapService.getSearchBusmapV3(authDTO, ticketMigrationDTO.getTrip());
		if (returnTripDTO == null || returnTripDTO.getBus() == null || returnTripDTO.getBus().getBusSeatLayoutDTO() == null) {
			System.out.println("Error:0098A0 " + ticketMigrationDTO.getFromStation().getCode() + "-" + ticketMigrationDTO.getToStation().getCode() + "-" + ticketMigrationDTO.getTravelDate());
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

	@Override
	public JSONArray validateTicketAutoMigration(AuthDTO authDTO, TripDTO trip, List<String> ticketList) {
		JSONArray response = new JSONArray();
		try {
			String ticketCode = Iterables.getFirst(ticketList, null);
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCode);
			ticketDTO = ticketService.getTicketStatus(authDTO, ticketDTO);
			TripDTO tripDTO = ticketDTO.getTripDTO();
			BusDTO busDTO = busService.getBus(authDTO, tripDTO.getBus());
			tripDTO = busmapService.getSearchBusmapV3(authDTO, tripDTO);
			tripDTO.setBus(busDTO);
			applyBookedBlockedSeat(authDTO, tripDTO);

			trip = busmapService.getSearchBusmapV3(authDTO, trip);
			if (trip == null || trip.getBus() == null || trip.getBus().getBusSeatLayoutDTO() == null) {
				SearchDTO searchDTO = trip.getSearch();
				throw new ServiceException(ErrorCode.INVALID_TRIP_CODE, searchDTO.getFromStation().getCode() + "-" + searchDTO.getToStation().getCode() + "-" + searchDTO.getTravelDate());
			}

			/** Adjacent seat gender validation */
			Map<String, String> seatNameMap = new HashMap<>();
			Map<String, BusSeatLayoutDTO> seatStatusMap = new HashMap<>();
			Map<String, BusSeatLayoutDTO> seatGenderMap = new HashMap<>();
			List<BusSeatLayoutDTO> migratedSeatList = trip.getSchedule().getScheduleBus().getBus().getBusSeatLayoutDTO().getList();
			for (BusSeatLayoutDTO dto : migratedSeatList) {
				seatNameMap.put(dto.getName(), dto.getLayer() + "_" + dto.getRowPos() + "_" + dto.getColPos());
				seatStatusMap.put(dto.getName(), dto);
				if (dto.getSeatStatus() != null) {
					seatGenderMap.put(dto.getLayer() + "_" + dto.getRowPos() + "_" + dto.getColPos(), dto);
				}
			}

			List<BusSeatLayoutDTO> seatList = tripDTO.getBus().getBusSeatLayoutDTO().getList();
			for (BusSeatLayoutDTO dto : seatList) {
				String seatPos = seatNameMap.get(dto.getName());
				BusSeatLayoutDTO busSeatLayoutDTO = seatStatusMap.get(dto.getName());
				if (dto.getSeatGendar() == null || busSeatLayoutDTO == null || dto.getBusSeatType().getId() != busSeatLayoutDTO.getBusSeatType().getId()) {
					continue;
				}
				JSONObject seatJson = new JSONObject();
				seatJson.put("seatCode", dto.getCode());
				seatJson.put("seatName", dto.getName());
				seatJson.put("migrationSeatCode", busSeatLayoutDTO.getCode());
				seatJson.put("migrationSeatName", dto.getName());
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

	protected void applyBookedBlockedSeat(AuthDTO authDTO, TripDTO tripDTO) {
		Map<String, List<TicketDetailsDTO>> statusMAP = new HashMap<String, List<TicketDetailsDTO>>();
		if (tripDTO != null && tripDTO.getTicketDetailsList() != null && !tripDTO.getTicketDetailsList().isEmpty()) {
			for (TicketDetailsDTO ticketDetailsDTO : tripDTO.getTicketDetailsList()) {
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId() && DateUtil.getMinutiesDifferent(ticketDetailsDTO.getUpdatedAt(), DateUtil.NOW()) > authDTO.getNamespace().getProfile().getSeatBlockTime()) {
					continue;
				}
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TENTATIVE_BLOCK_CANCELLED.getId()) {
					continue;
				}
				// Validate PBL Block Live Time
				if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && BitsUtil.validateBlockReleaseTime(ticketDetailsDTO.getBlockReleaseMinutes(), tripDTO.getTripDateTime(), ticketDetailsDTO.getUpdatedAt())) {
					continue;
				}
				if (ticketDetailsDTO.getTicketExtra() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus() != null && ticketDetailsDTO.getTicketExtra().getTravelStatus().getId() == TravelStatusEM.NOT_TRAVELED.getId()) {
					continue;
				}

				if (tripDTO.getReleatedStageCodeList().contains(ticketDetailsDTO.getTripStageCode())) {
					if (ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.CONFIRM_CANCELLED_TICKETS.getId() && ticketDetailsDTO.getTicketStatus().getId() != TicketStatusEM.PHONE_BOOKING_CANCELLED.getId()) {
						List<TicketDetailsDTO> list = null;
						if (statusMAP.get(ticketDetailsDTO.getSeatCode()) == null) {
							list = new ArrayList<TicketDetailsDTO>();
						}
						else {
							list = statusMAP.get(ticketDetailsDTO.getSeatCode());
						}
						list.add(ticketDetailsDTO);
						statusMAP.put(ticketDetailsDTO.getSeatCode(), list);
					}
				}
			}
		}

		Map<String, String> seatCodeMap = new HashMap<>();
		Map<String, BusSeatLayoutDTO> socialDistanceMap = new HashMap<>();
		Map<String, Integer> seatStatusMap = new HashMap<>();
		boolean isLayout1X1 = tripDTO.getBus().checkLayoutCategory(BusCategoryTypeEM.LAYOUT_1X1);

		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			List<TicketDetailsDTO> list = statusMAP.get(seatLayoutDTO.getCode());
			if (list != null && !list.isEmpty()) {
				for (TicketDetailsDTO ticketDetailsDTO : list) {
					if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.BOOKED);
					}
					else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.TMP_BLOCKED_TICKET.getId()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.TEMP_BLOCKED);
						seatLayoutDTO.setUpdatedAt(ticketDetailsDTO.getUpdatedAt());
					}
					else if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId()) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.PHONE_BLOCKED);
					}
					else if (seatLayoutDTO.getSeatStatus() == null) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
					}

					// Copy ticket details
					if ((SeatStatusEM.BOOKED.getId() == seatLayoutDTO.getSeatStatus().getId() || SeatStatusEM.BLOCKED.getId() == seatLayoutDTO.getSeatStatus().getId() || SeatStatusEM.TEMP_BLOCKED.getId() == seatLayoutDTO.getSeatStatus().getId() || SeatStatusEM.PHONE_BLOCKED.getId() == seatLayoutDTO.getSeatStatus().getId()) && StringUtil.isNull(seatLayoutDTO.getTicketCode())) {
						seatLayoutDTO.setSeatGendar(ticketDetailsDTO.getSeatGendar());
						seatLayoutDTO.setPassengerAge(ticketDetailsDTO.getPassengerAge());
						seatLayoutDTO.setPassengerName(ticketDetailsDTO.getPassengerName());
						seatLayoutDTO.setContactNumber(ticketDetailsDTO.getContactNumber());
						seatLayoutDTO.setTicketCode(ticketDetailsDTO.getTicketCode());
						seatLayoutDTO.setBoardingPointName(ticketDetailsDTO.getBoardingPointName());
						seatLayoutDTO.setStationPoint(ticketDetailsDTO.getStationPoint());
						seatLayoutDTO.setUser(getUserDTOById(authDTO, ticketDetailsDTO.getUser()));
						seatLayoutDTO.setGroup(getGroupDTOById(authDTO, seatLayoutDTO.getUser().getGroup()));
						seatLayoutDTO.setFromStation(getStationDTObyId(ticketDetailsDTO.getFromStation()));
						seatLayoutDTO.setToStation(getStationDTObyId(ticketDetailsDTO.getToStation()));
						seatLayoutDTO.setUpdatedAt(ticketDetailsDTO.getUpdatedAt());
						if (authDTO.getDeviceMedium().getId() != DeviceMediumEM.API_USER.getId()) {
							seatLayoutDTO.setFare(ticketDetailsDTO.getSeatFare());
						}

						if (ticketDetailsDTO.getTicketStatus().getId() == TicketStatusEM.PHONE_BLOCKED_TICKET.getId() && ticketDetailsDTO.getTicketExtra() != null && ticketDetailsDTO.getTicketExtra().getBlockReleaseMinutes() != 0) {
							seatLayoutDTO.setReleaseAt(BitsUtil.getBlockReleaseDateTime(ticketDetailsDTO.getTicketExtra().getBlockReleaseMinutes(), tripDTO.getTripDateTimeV2(), ticketDetailsDTO.getUpdatedAt()));
						}
					}
				}
			}

			seatCodeMap.put(seatLayoutDTO.getCode(), seatLayoutDTO.getLayer() + "_" + seatLayoutDTO.getRowPos() + "_" + seatLayoutDTO.getColPos());
			if (seatLayoutDTO.getSeatStatus() != null) {
				socialDistanceMap.put(seatLayoutDTO.getLayer() + "_" + seatLayoutDTO.getRowPos() + "_" + seatLayoutDTO.getColPos(), seatLayoutDTO);
				seatStatusMap.put(seatLayoutDTO.getCode(), seatLayoutDTO.getSeatStatus().getId());
			}
		}

		/** Validate Social Distance Seats */
		boolean enableSocialDistanceAmenities = Text.FALSE;
		Map<String, String> additionalAttributes = new HashMap<String, String>();
		for (BusSeatLayoutDTO seatLayoutDTO : tripDTO.getBus().getBusSeatLayoutDTO().getList()) {
			if (!isLayout1X1 && seatLayoutDTO.getSeatStatus() != null && seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId()) {
				String seatPos = seatCodeMap.get(seatLayoutDTO.getCode());
				enableSocialDistanceAmenities = Text.TRUE;
				if (seatPos != null) {
					int layer = Integer.parseInt(seatPos.split("_")[0]);
					int rowCount = Integer.parseInt(seatPos.split("_")[1]);
					int colCount = Integer.parseInt(seatPos.split("_")[2]);

					BusSeatLayoutDTO adjucentSeat = null;
					if (seatLayoutDTO.getOrientation() == 0) {
						/** Left Side seat */
						if (socialDistanceMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null && socialDistanceMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) == null) {
							adjucentSeat = socialDistanceMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
						}
						/** Right Side seat */
						else if (socialDistanceMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) == null && socialDistanceMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
							adjucentSeat = socialDistanceMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
						}
						/** Middle seat */
						else if (socialDistanceMap.get(layer + "_" + (rowCount + 1) + "_" + colCount) != null && socialDistanceMap.get(layer + "_" + (rowCount - 1) + "_" + colCount) != null) {
							adjucentSeat = socialDistanceMap.get(layer + "_" + (rowCount - 1) + "_" + colCount);
							if (adjucentSeat != null && adjucentSeat.getSeatStatus() != null && adjucentSeat.getSeatStatus().getId() != SeatStatusEM.BOOKED.getId() && adjucentSeat.getSeatStatus().getId() != SeatStatusEM.PHONE_BLOCKED.getId()) {
								adjucentSeat = socialDistanceMap.get(layer + "_" + (rowCount + 1) + "_" + colCount);
							}
						}
					}
					else if (seatLayoutDTO.getOrientation() == 1) {
						/** Left Side seat */
						if (socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount + 1)) != null && socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount - 1)) == null) {
							adjucentSeat = socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount + 1));
						}
						/** Right Side seat */
						else if (socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount + 1)) == null && socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount - 1)) != null) {
							adjucentSeat = socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount - 1));
						}
						/** Middle seat */
						else if (socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount - 1)) != null && socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount + 1)) != null) {
							adjucentSeat = socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount - 1));
							if (adjucentSeat != null && adjucentSeat.getSeatStatus() != null && adjucentSeat.getSeatStatus().getId() != SeatStatusEM.BOOKED.getId() && adjucentSeat.getSeatStatus().getId() != SeatStatusEM.PHONE_BLOCKED.getId()) {
								adjucentSeat = socialDistanceMap.get(layer + "_" + rowCount + "_" + (colCount + 1));
							}
						}
					}

					if (adjucentSeat != null && adjucentSeat.getSeatStatus() != null && adjucentSeat.getSeatStatus().getId() == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId() && seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId()) {
						additionalAttributes.put(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode(), "DYNAMIC");
					}
					else if (adjucentSeat != null && adjucentSeat.getSeatStatus() != null && adjucentSeat.getSeatStatus().getId() != SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId() && seatLayoutDTO.getSeatStatus().getId() == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId() && additionalAttributes.get(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode()) == null) {
						additionalAttributes.put(SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getCode(), "STATIC");
					}

					/**
					 * Change seat status, if social distance blocked
					 * seat booked
					 */
					if (adjucentSeat != null && adjucentSeat.getSeatStatus() != null && (adjucentSeat.getSeatStatus().getId() == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId() || seatStatusMap.get(adjucentSeat.getCode()) == SeatStatusEM.SOCIAL_DISTANCE_BLOCK.getId())) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
					}
					else if (adjucentSeat == null) {
						seatLayoutDTO.setSeatStatus(SeatStatusEM.AVAILABLE_ALL);
					}

				}
			}
			if (seatLayoutDTO.getUser() != null && seatLayoutDTO.getUser().getId() != 0) {
				seatLayoutDTO.setUser(getUserDTOById(authDTO, seatLayoutDTO.getUser()));
			}
			if (seatLayoutDTO.getGroup() != null && seatLayoutDTO.getGroup().getId() != 0) {
				seatLayoutDTO.setGroup(getGroupDTOById(authDTO, seatLayoutDTO.getGroup()));
			}
		}

		/** If social distance applied, append social distance amenities */
		if (enableSocialDistanceAmenities) {
			tripDTO.getActivities().add(TripActivitiesEM.SOCIAL_DISTANCING);
			tripDTO.getAdditionalAttributes().putAll(additionalAttributes);
		}
	}
}
