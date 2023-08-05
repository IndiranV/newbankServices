package org.in.com.service.impl;

import java.util.Iterator;
import java.util.List;

import org.in.com.cache.TicketHelperCache;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StageStationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.service.NotificationPushService;
import org.in.com.service.NotificationService;
import org.in.com.service.ScheduleService;
import org.in.com.service.TicketHelperService;
import org.in.com.service.TripService;
import org.in.com.service.TripVanInfoService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class TicketHelperServiceImpl extends TicketHelperCache implements TicketHelperService {
	@Autowired
	NotificationService notificationService;
	@Autowired
	NotificationPushService notificationPushService;
	@Autowired
	TripService tripService;
	@Autowired
	TripVanInfoService tripVanInfoService;
	@Autowired
	ScheduleService scheduleService;

	@Override
	@Async
	public void processTicketAfterTripTime(AuthDTO authDTO, BookingDTO bookingDTO) {
		TicketDTO ticketDTO = bookingDTO.getTicketDTO(JourneyTypeEM.ONWARD_TRIP);
		BusDTO busDTO = ticketDTO.getTripDTO().getBus();
		TripDTO tripDTO = tripService.getTrip(authDTO, ticketDTO.getTripDTO());
		if (busDTO != null && busDTO.getId() != 0) {
			ticketDTO.getTripDTO().setBus(busDTO);
		}
		List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, tripDTO);
		DateTime originDateTime = BitsUtil.getOriginStationPointDateTime(stageList, tripDTO.getTripDate());

		int tripMinuties = DateUtil.getMinutiesDifferent(originDateTime, DateUtil.NOW());
		if (tripMinuties > -30) {
			putTicketAfterTripTimeInCache(authDTO, ticketDTO);

			// Push Notification
			notificationService.ticketAfterTripTimeFCM(authDTO, tripDTO, ticketDTO);
			notificationPushService.pushTicketAfterTripTime(authDTO, ticketDTO);
		}
		if (tripMinuties > -70) {
			// Only After Trip Time Notify to Customer
			if (NotificationTypeEM.isNotificationEnabled(authDTO.getNamespace().getProfile().getSmsNotificationFlagCode(), NotificationTypeEM.GPS_TRACKING)) {
				authDTO.getAdditionalAttribute().put(Text.TICKET_AFTER_TRIP_TIME, "TRUE");
				notificationService.sendTripJourneyTrackingSMS(authDTO, ticketDTO);
			}

			// Ticket After Trip Time Notify
			StageStationDTO stageStation = stageList.stream().filter(stage -> stage.getStation().getId() == ticketDTO.getFromStation().getId()).findFirst().orElse(null);
			String boardingMobileNumber = stageStation != null && StringUtil.isNotNull(stageStation.getMobileNumber()) ? stageStation.getMobileNumber() : Text.EMPTY;

			TripVanInfoDTO tripVanInfo = getVanPickupInfo(authDTO, ticketDTO, tripDTO, stageStation);
			try {
				if (tripVanInfo != null && StringUtil.isValidMobileNumber(tripVanInfo.getMobileNumber())) {
					boardingMobileNumber = boardingMobileNumber + Text.COMMA + tripVanInfo.getMobileNumber();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

			notificationService.sendTicketAfterTripTimeNotify(authDTO, ticketDTO, boardingMobileNumber);

		}
	}
	
	@Override
	@Async
	public void processTicketAfterTripTimeCancel(AuthDTO authDTO, TicketDTO ticketDTO) {
		TripDTO tripDTO = tripService.getTrip(authDTO, ticketDTO.getTripDTO());
		List<StageStationDTO> stageList = tripService.getScheduleTripStage(authDTO, tripDTO);
		DateTime originDateTime = BitsUtil.getOriginStationPointDateTime(stageList, tripDTO.getTripDate());

		int tripMinuties = DateUtil.getMinutiesDifferent(originDateTime, DateUtil.NOW());
		if (tripMinuties > -70) {
			// Ticket After Trip Time Notify
			StageStationDTO stageStation = stageList.stream().filter(stage -> stage.getStation().getId() == ticketDTO.getFromStation().getId()).findFirst().orElse(null);
			String boardingMobileNumber = stageStation != null && StringUtil.isNotNull(stageStation.getMobileNumber()) ? stageStation.getMobileNumber() : Text.EMPTY;

			TripVanInfoDTO tripVanInfo = getVanPickupInfo(authDTO, ticketDTO, tripDTO, stageStation);
			try {
				if (tripVanInfo != null && StringUtil.isValidMobileNumber(tripVanInfo.getMobileNumber())) {
					boardingMobileNumber = boardingMobileNumber + Text.COMMA + tripVanInfo.getMobileNumber();
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}
			// send sms notification
			notificationService.sendTicketAfterTripTimeCancelNotify(authDTO, ticketDTO, boardingMobileNumber);
		}
	}
	
	private TripVanInfoDTO getVanPickupInfo(AuthDTO authDTO, TicketDTO ticketDTO, TripDTO tripDTO, StageStationDTO stageStationDTO) {
		StationPointDTO stationPointDTO = stageStationDTO.getStationPoint().stream().filter(stationPoint -> stationPoint.getId() == ticketDTO.getBoardingPoint().getId() && stationPoint.getBusVehicleVanPickup() != null).findFirst().orElse(null);

		TripVanInfoDTO tripVanInfo = null;
		if (stationPointDTO != null && stationPointDTO.getBusVehicleVanPickup() != null) {
			TripVanInfoDTO tripVanInfoDTO = new TripVanInfoDTO();
			tripVanInfoDTO.setVanPickup(stationPointDTO.getBusVehicleVanPickup());
			tripVanInfoDTO.setTripDate(tripDTO.getTripDate());

			tripVanInfo = tripVanInfoService.getTripVanInfo(authDTO, tripVanInfoDTO);
		}
		return tripVanInfo;
	}

	@Override
	public List<TicketDTO> getAllTicketAfterTripTime(AuthDTO authDTO, List<ScheduleDTO> scheduleList) {
		List<TicketDTO> list = getTicketAfterTripTime(authDTO);
		for (ScheduleDTO scheduleDTO : scheduleList) {
			scheduleService.getSchedule(authDTO, scheduleDTO);
		}
		for (Iterator<TicketDTO> ticketItr = list.iterator(); ticketItr.hasNext();) {
			TicketDTO ticketDTO = ticketItr.next();
			if (!scheduleList.isEmpty() && BitsUtil.isScheduleExists(scheduleList, ticketDTO.getTripDTO().getSchedule()) == null) {
				ticketItr.remove();
				continue;
			}
			if (ticketDTO.getNamespaceCode().equals(authDTO.getNamespaceCode())) {
				ticketDTO.setTicketUser(getUserDTOById(authDTO, ticketDTO.getTicketUser()));
				ticketDTO.setBoardingPoint(getStationPointDTObyId(authDTO, ticketDTO.getBoardingPoint()));
				ticketDTO.getTripDTO().setBus(getBusDTO(authDTO, ticketDTO.getTripDTO().getBus()));
			}
			ticketDTO.setFromStation(getStationDTObyId(ticketDTO.getFromStation()));
			ticketDTO.setToStation(getStationDTObyId(ticketDTO.getToStation()));
		}
		return list;
	}

	@Override
	public void acknowledgeTicketAfterTripTime(AuthDTO authDTO, String ticketCode) {
		removeTicketAfterTripTimeInCache(authDTO, ticketCode);
	}

	@Override
	public List<TicketDTO> getInProcegressNotBoardedTicket(AuthDTO authDTO) {
		List<TicketDTO> list = getTicketNotBoarded(authDTO);
		for (TicketDTO ticketDTO : list) {
			if (ticketDTO.getNamespaceCode().equals(authDTO.getNamespaceCode())) {
				ticketDTO.setTicketUser(getUserDTOById(authDTO, ticketDTO.getTicketUser()));
				ticketDTO.setBoardingPoint(getStationPointDTObyId(authDTO, ticketDTO.getBoardingPoint()));
				ticketDTO.getTripDTO().setBus(getBusDTO(authDTO, ticketDTO.getTripDTO().getBus()));
			}
			ticketDTO.setFromStation(getStationDTObyId(ticketDTO.getFromStation()));
			ticketDTO.setToStation(getStationDTObyId(ticketDTO.getToStation()));
		}
		return list;
	}
}
