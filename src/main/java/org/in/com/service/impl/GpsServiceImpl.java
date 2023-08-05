package org.in.com.service.impl;

import org.in.com.aggregator.gps.TrackBusService;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GPSLocationDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripInfoDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.GpsService;
import org.in.com.service.TicketService;
import org.in.com.service.TripHelperService;
import org.in.com.service.TripService;
import org.in.com.utils.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;

@Service
public class GpsServiceImpl extends BaseImpl implements GpsService {
	private static final Logger logger = LoggerFactory.getLogger("org.in.com.aggregator.bits.trackbus");

	@Autowired
	TrackBusService trackBusService;
	@Autowired
	TicketService ticketService;
	@Autowired
	TripService tripService;
	@Autowired
	TripHelperService tripHelperService;

	public TripDTO getTicketVechileLocation(AuthDTO authDTO, TicketDTO ticketDTO) {

		ticketService.getTicketTripDetails(authDTO, ticketDTO);

		// if (ticketDTO.getTicketStatus().getId() !=
		// TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
		// throw new ServiceException(ErrorCode.NOT_CONFIRM_BOOKED_TICKET);
		// }

		ErrorCode error = null;
		DateTime arrialDateTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getTravelMinutes() - 180);
		if (DateUtil.NOW().compareTo(arrialDateTime) == -1) {
			error = ErrorCode.GPS_TRIP_JOURNEY_NOT_STARTED;
			// throw new
			// ServiceException(ErrorCode.GPS_TRIP_JOURNEY_NOT_STARTED,
			// arrialDateTime.format("YYYY-MM-DD hh:mm:ss"));
		}
		DateTime depatureDateTime = DateUtil.addMinituesToDate(ticketDTO.getTripDate(), ticketDTO.getDroppingPoint().getMinitues() + 180);
		if (DateUtil.NOW().compareTo(depatureDateTime) == 1) {
			error = ErrorCode.GPS_TRIP_JOURNEY_COMPLETED;
			// throw new ServiceException(ErrorCode.GPS_TRIP_JOURNEY_COMPLETED);
		}

		tripService.getTrip(authDTO, ticketDTO.getTripDTO());
		// if (!ticketDTO.getTripDate().equals(DateUtil.NOW())) {
		// throw new ServiceException(ErrorCode.GPS_TRIP_TRACKING_NOT_ALLOWED);
		// }
		tripHelperService.getTripDetails(authDTO, ticketDTO.getTripDTO());

		TripInfoDTO tripInfoDTO = tripService.getTripInfo(authDTO, ticketDTO.getTripDTO());
		GPSLocationDTO locationDTO = new GPSLocationDTO();
		if (tripInfoDTO != null && tripInfoDTO.getBusVehicle() != null && tripInfoDTO.getBusVehicle().getId() != 0) {
			ticketDTO.getTripDTO().setTripInfo(tripInfoDTO);
			try {
				locationDTO = trackBusService.getVehicleLocation(authDTO.getNamespaceCode(), ticketDTO.getTripDTO().getTripInfo().getBusVehicle().getDeviceVendor(), ticketDTO.getTripDTO().getTripInfo().getBusVehicle().getGpsDeviceCode(), ticketDTO.getTripDTO().getTripInfo().getBusVehicle().getRegistationNumber());
			}
			catch (ServiceException e) {
				error = e.getErrorCode();
			}
			catch (Exception e) {
				error = ErrorCode.GPS_DEVICE_LOCATION_NOT_FOUND;
			}
		}
		locationDTO.setError(error);
		ticketDTO.getTripDTO().setLocation(locationDTO);
		return ticketDTO.getTripDTO();
	}

	@Override
	public TripDTO getTripVechileLocation(AuthDTO authDTO, TripDTO tripDTO) {

		tripService.getTrip(authDTO, tripDTO);
		// if (!tripDTO.getTripDate().equals(DateUtil.NOW())) {
		// throw new ServiceException(ErrorCode.GPS_TRIP_TRACKING_NOT_ALLOWED);
		// }
		tripHelperService.getTripDetails(authDTO, tripDTO);

		ScheduleStationDTO tripFromStation = null;
		ScheduleStationDTO tripToStation = null;

		for (ScheduleStationDTO scheduleStationDTO : tripDTO.getStationList()) {
			if (tripFromStation == null) {
				tripFromStation = scheduleStationDTO;
			}
			if (tripToStation == null) {
				tripToStation = scheduleStationDTO;
			}
			if (tripFromStation.getStationSequence() > scheduleStationDTO.getStationSequence()) {
				tripFromStation = scheduleStationDTO;
			}
			if (tripToStation.getStationSequence() < scheduleStationDTO.getStationSequence()) {
				tripToStation = scheduleStationDTO;
			}
		}

		ErrorCode error = null;
		DateTime arrialDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripFromStation.getMinitues() - 180);
		if (DateUtil.NOW().compareTo(arrialDateTime) == -1) {
			error = ErrorCode.GPS_TRIP_JOURNEY_NOT_STARTED;
			// throw new
			// ServiceException(ErrorCode.GPS_TRIP_JOURNEY_NOT_STARTED,
			// arrialDateTime.format("YYYY-MM-DD hh:mm:ss"));
		}
		DateTime depatureDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripToStation.getMinitues() + 180);
		if (DateUtil.NOW().compareTo(depatureDateTime) == 1) {
			error = ErrorCode.GPS_TRIP_JOURNEY_COMPLETED;
			// throw new ServiceException(ErrorCode.GPS_TRIP_JOURNEY_COMPLETED);
		}

		TripInfoDTO tripInfoDTO = tripService.getTripInfo(authDTO, tripDTO);
		GPSLocationDTO locationDTO = new GPSLocationDTO();
		if (tripInfoDTO != null && tripInfoDTO.getBusVehicle() != null && tripInfoDTO.getBusVehicle().getId() != 0) {
			tripDTO.setTripInfo(tripInfoDTO);
			try {
				locationDTO = trackBusService.getVehicleLocation(authDTO.getNamespaceCode(), tripDTO.getTripInfo().getBusVehicle().getDeviceVendor(), tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode(), tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
			}
			catch (ServiceException e) {
				error = e.getErrorCode();
			}
			catch (Exception e) {
				error = ErrorCode.GPS_DEVICE_LOCATION_NOT_FOUND;
			}
		}
		locationDTO.setError(error);
		tripDTO.setLocation(locationDTO);
		return tripDTO;
	}

	public TripDTO getTripStageVechileLocation(AuthDTO authDTO, TripDTO tripDTO) {

		tripService.getTrip(authDTO, tripDTO);
		// if (!tripDTO.getTripDate().equals(DateUtil.NOW())) {
		// throw new ServiceException(ErrorCode.GPS_TRIP_TRACKING_NOT_ALLOWED);
		// }
		tripHelperService.getTripDetails(authDTO, tripDTO);
		SearchDTO searchDTO = tripDTO.getSearch();

		ScheduleStationDTO tripFromStation = null;
		ScheduleStationDTO tripToStation = null;

		for (ScheduleStationDTO scheduleStationDTO : tripDTO.getStationList()) {
			if (tripFromStation == null) {
				tripFromStation = scheduleStationDTO;
			}
			if (tripToStation == null) {
				tripToStation = scheduleStationDTO;
			}
			if (scheduleStationDTO.getStation().getId() == searchDTO.getFromStation().getId()) {
				tripFromStation = scheduleStationDTO;
			}
			if (scheduleStationDTO.getStation().getId() == searchDTO.getToStation().getId()) {
				tripToStation = scheduleStationDTO;
			}
		}
		for (StageDTO stageDTO : tripDTO.getStageList()) {
			if (stageDTO.getFromStation().getStation().getId() == searchDTO.getFromStation().getId() && stageDTO.getToStation().getStation().getId() == searchDTO.getToStation().getId()) {
				tripDTO.setStage(stageDTO);
			}
		}
		ErrorCode error = null;
		DateTime arrialDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripFromStation.getMinitues() - 180);
		if (DateUtil.NOW().compareTo(arrialDateTime) == -1) {
			error = ErrorCode.GPS_TRIP_JOURNEY_NOT_STARTED;
			// throw new
			// ServiceException(ErrorCode.GPS_TRIP_JOURNEY_NOT_STARTED,
			// arrialDateTime.format("YYYY-MM-DD hh:mm:ss"));
		}
		DateTime depatureDateTime = DateUtil.addMinituesToDate(tripDTO.getTripDate(), tripToStation.getMinitues() + 180);
		if (DateUtil.NOW().compareTo(depatureDateTime) == 1) {
			error = ErrorCode.GPS_TRIP_JOURNEY_COMPLETED;
			// throw new ServiceException(ErrorCode.GPS_TRIP_JOURNEY_COMPLETED);
		}

		TripInfoDTO tripInfoDTO = tripService.getTripInfo(authDTO, tripDTO);
		GPSLocationDTO locationDTO = new GPSLocationDTO();
		if (tripInfoDTO != null && tripInfoDTO.getBusVehicle() != null && tripInfoDTO.getBusVehicle().getId() != 0) {
			tripDTO.setTripInfo(tripInfoDTO);
			try {
				locationDTO = trackBusService.getVehicleLocation(authDTO.getNamespaceCode(), tripDTO.getTripInfo().getBusVehicle().getDeviceVendor(), tripDTO.getTripInfo().getBusVehicle().getGpsDeviceCode(), tripDTO.getTripInfo().getBusVehicle().getRegistationNumber());
			}
			catch (ServiceException e) {
				error = e.getErrorCode();
			}
			catch (Exception e) {
				error = ErrorCode.GPS_DEVICE_LOCATION_NOT_FOUND;
			}
		}
		locationDTO.setError(error);
		tripDTO.setLocation(locationDTO);
		return tripDTO;
	}

}
