package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.in.com.cache.EhcacheManager;
import org.in.com.cache.TripCache;
import org.in.com.cache.dto.TripSeatQuotaCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.TripSeatQuotaDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.SearchDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripSeatQuotaDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.SeatStatusEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusmapService;
import org.in.com.service.StationService;
import org.in.com.service.TripSeatQuotaService;
import org.in.com.service.TripService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import net.sf.ehcache.Element;

@Service
public class TripSeatQuotaServiceImpl extends TripCache implements TripSeatQuotaService {
	@Autowired
	TripService tripService;
	@Autowired
	BusmapService busmapService;
	@Autowired
	StationService stationService;

	public void updateTripSeatQuota(AuthDTO authDTO, TripSeatQuotaDTO tripSeatQuotaDTO) {
		TripSeatQuotaDAO tripSeatQuotaDAO = new TripSeatQuotaDAO();

		// From To Station Cache
		if (StringUtil.isNotNull(tripSeatQuotaDTO.getFromStation().getCode()) && StringUtil.isNotNull(tripSeatQuotaDTO.getToStation().getCode())) {
			tripSeatQuotaDTO.setFromStation(stationService.getStation(tripSeatQuotaDTO.getFromStation()));
			tripSeatQuotaDTO.setToStation(stationService.getStation(tripSeatQuotaDTO.getToStation()));
		}

		// Update Remarks On Delete
		if (tripSeatQuotaDTO.getActiveFlag() != Numeric.ONE_INT) {
			List<TripSeatQuotaDTO> seatQuotaList = new ArrayList<TripSeatQuotaDTO>();
			List<TripSeatQuotaDTO> repoTripSeatQuotaList = getAllTripSeatQuota(authDTO, tripSeatQuotaDTO.getTrip());
			for (TripSeatQuotaDTO repoSeatQuota : repoTripSeatQuotaList) {
				for (TicketDetailsDTO ticketDetails : tripSeatQuotaDTO.getTrip().getTicketDetailsList()) {
					if (repoSeatQuota.getSeatDetails().getSeatCode().equals(ticketDetails.getSeatCode()) && repoSeatQuota.getFromStation().getId() == tripSeatQuotaDTO.getFromStation().getId() && repoSeatQuota.getToStation().getId() == tripSeatQuotaDTO.getToStation().getId()) {
						repoSeatQuota.setActiveFlag(tripSeatQuotaDTO.getActiveFlag());
						seatQuotaList.add(repoSeatQuota);
					}
				}
			}
			if (seatQuotaList.isEmpty()) {
				throw new ServiceException(ErrorCode.INVALID_TRIP_SEAT_QUOTA);
			}
			tripSeatQuotaDAO.deleteTripSeatQuotaDetails(authDTO, seatQuotaList);
		}
		else if (tripSeatQuotaDTO.getActiveFlag() == Numeric.ONE_INT) {
			// User Cache
			tripSeatQuotaDTO.setUser(getUserDTO(authDTO, tripSeatQuotaDTO.getUser()));
			// Trip Cache
			tripSeatQuotaDTO.setTrip(getTripDTO(authDTO, tripSeatQuotaDTO.getTrip()));

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(tripSeatQuotaDTO.getTrip().getCode());

			SearchDTO searchDTO = new SearchDTO();
			searchDTO.setTravelDate(tripSeatQuotaDTO.getTrip().getTripDate());
			searchDTO.setFromStation(tripSeatQuotaDTO.getFromStation());
			searchDTO.setToStation(tripSeatQuotaDTO.getToStation());
			tripDTO.setSearch(searchDTO);

			tripDTO = busmapService.getSearchBusmapV3(authDTO, tripDTO);
			List<BusSeatLayoutDTO> seatLayoutDTOList = tripDTO.getBus().getBusSeatLayoutDTO().getList();

			Map<String, BusSeatLayoutDTO> fareMap = new HashMap<String, BusSeatLayoutDTO>();
			for (BusSeatLayoutDTO seatLayoutDTO : seatLayoutDTOList) {
				if (seatLayoutDTO.getSeatStatus() == SeatStatusEM.ALLOCATED_YOU || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_ALL || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_MALE || seatLayoutDTO.getSeatStatus() == SeatStatusEM.AVAILABLE_FEMALE) {
					fareMap.put(seatLayoutDTO.getCode(), seatLayoutDTO);
				}
			}
			for (TicketDetailsDTO ticketDetailsDTO : tripSeatQuotaDTO.getTrip().getTicketDetailsList()) {
				BusSeatLayoutDTO seatLayoutDTO = fareMap.get(ticketDetailsDTO.getSeatCode());
				if (seatLayoutDTO == null) {
					throw new ServiceException(ErrorCode.SEAT_ALREADY_BLOCKED);
				}
			}

			// Update Trip Seat Quota
			tripSeatQuotaDAO.updateTripSeatQuota(authDTO, tripSeatQuotaDTO);
		}

		// Clear Cache
		EhcacheManager.getTripQuotaSeatCache().remove(tripSeatQuotaDTO.getTrip().getCode());
	}

	public List<TripSeatQuotaDTO> getAllTripSeatQuota(AuthDTO authDTO, TripDTO trip) {
		List<TripSeatQuotaDTO> list = null;
		try {

			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(trip.getCode());
			tripDTO = getTripDTO(authDTO, tripDTO);

			Element element = EhcacheManager.getTripQuotaSeatCache().get(tripDTO.getCode());
			if (element != null) {
				List<TripSeatQuotaCacheDTO> tripSeatQuotaCacheList = (List<TripSeatQuotaCacheDTO>) element.getObjectValue();
				list = bindTripSeatQuotaFromCache(tripDTO, tripSeatQuotaCacheList);
			}
			else {
				TripSeatQuotaDAO tripSeatQuotaDAO = new TripSeatQuotaDAO();
				list = tripSeatQuotaDAO.getAllTripSeatQuota(authDTO, tripDTO);
				List<TripSeatQuotaCacheDTO> tripSeatQuotaCacheList = bindTripSeatQuotaToCache(list);
				EhcacheManager.getTripQuotaSeatCache().put(new Element(tripDTO.getCode(), tripSeatQuotaCacheList));
			}

			for (TripSeatQuotaDTO tripSeatQuotaDTO : list) {

				// Release Quota Seats
				int minuties = DateUtil.getMinutiesDifferent(DateUtil.NOW().getStartOfDay(), tripDTO.getTripDateTimeV2());
				if (tripSeatQuotaDTO.getRelaseMinutes() != 0 && minuties > tripSeatQuotaDTO.getRelaseMinutes()) {
					// continue;
				}
				if (tripSeatQuotaDTO.getFromStation().getId() != Numeric.ZERO_INT && tripSeatQuotaDTO.getToStation().getId() != Numeric.ZERO_INT) {
					tripSeatQuotaDTO.setFromStation(getStationDTObyId(tripSeatQuotaDTO.getFromStation()));
					tripSeatQuotaDTO.setToStation(getStationDTObyId(tripSeatQuotaDTO.getToStation()));
				}
				tripSeatQuotaDTO.setUser(getUserDTOById(authDTO, tripSeatQuotaDTO.getUser()));
				tripSeatQuotaDTO.setUpdatedBy(getUserDTOById(authDTO, tripSeatQuotaDTO.getUpdatedBy()));
			}
		}
		catch (ServiceException e) {
			System.out.println(e.getErrorCode().getMessage());
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}


	// Removed user/updatedby to improve performace
	public List<TripSeatQuotaDTO> getAllTripSeatQuotaV2(AuthDTO authDTO, TripDTO trip) {
		List<TripSeatQuotaDTO> list = null;

		Element element = EhcacheManager.getTripQuotaSeatCache().get(trip.getCode());
		if (element != null) {
			List<TripSeatQuotaCacheDTO> tripSeatQuotaCacheList = (List<TripSeatQuotaCacheDTO>) element.getObjectValue();
			list = bindTripSeatQuotaFromCache(trip, tripSeatQuotaCacheList);
		}
		else {
			TripSeatQuotaDAO tripSeatQuotaDAO = new TripSeatQuotaDAO();
			list = tripSeatQuotaDAO.getAllTripSeatQuota(authDTO, trip);
			List<TripSeatQuotaCacheDTO> tripSeatQuotaCacheList = bindTripSeatQuotaToCache(list);
			EhcacheManager.getTripQuotaSeatCache().put(new Element(trip.getCode(), tripSeatQuotaCacheList));
		}

		for (TripSeatQuotaDTO tripSeatQuotaDTO : list) {

			// Release Quota Seats
			int minuties = DateUtil.getMinutiesDifferent(DateUtil.NOW().getStartOfDay(), trip.getTripDateTimeV2());
			if (tripSeatQuotaDTO.getRelaseMinutes() != 0 && minuties > tripSeatQuotaDTO.getRelaseMinutes()) {
				// continue;
			}
			if (tripSeatQuotaDTO.getFromStation().getId() != Numeric.ZERO_INT && tripSeatQuotaDTO.getToStation().getId() != Numeric.ZERO_INT) {
				tripSeatQuotaDTO.setFromStation(stationService.getStation(tripSeatQuotaDTO.getFromStation()));
				tripSeatQuotaDTO.setToStation(stationService.getStation(tripSeatQuotaDTO.getToStation()));
			}
		}
		return list;
	}

	private boolean validateQuota(List<TripSeatQuotaDTO> repoTripSeatQuotaList, List<TicketDetailsDTO> ticketDetailsList) {
		boolean selectedSeatAvailable = Text.FALSE;
		for (TripSeatQuotaDTO repoSeatQuota : repoTripSeatQuotaList) {
			for (TicketDetailsDTO ticketDetails : ticketDetailsList) {
				if (repoSeatQuota.getSeatDetails().getSeatCode().equals(ticketDetails.getSeatCode())) {
					selectedSeatAvailable = Text.TRUE;
					break;
				}
			}
		}
		return selectedSeatAvailable;
	}

	private boolean validateSeatBookings(List<TicketDetailsDTO> repoTripSeatQuotaList, List<TicketDetailsDTO> ticketDetailsList) {
		boolean selectedSeatAvailable = Text.FALSE;
		for (TicketDetailsDTO repoSeatQuota : repoTripSeatQuotaList) {
			for (TicketDetailsDTO ticketDetails : ticketDetailsList) {
				if (repoSeatQuota.getSeatCode().equals(ticketDetails.getSeatCode()) && ticketDetails.getTicketStatus().getId() == TicketStatusEM.CONFIRM_BOOKED_TICKETS.getId()) {
					selectedSeatAvailable = Text.TRUE;
					break;
				}
			}
		}
		return selectedSeatAvailable;
	}

	private List<TripSeatQuotaCacheDTO> bindTripSeatQuotaToCache(List<TripSeatQuotaDTO> list) {
		List<TripSeatQuotaCacheDTO> tripSeatQuotaCacheList = new ArrayList<TripSeatQuotaCacheDTO>();
		for (TripSeatQuotaDTO tripSeatQuotaDTO : list) {
			TripSeatQuotaCacheDTO tripSeatQuotaCacheDTO = new TripSeatQuotaCacheDTO();
			tripSeatQuotaCacheDTO.setSeatCode(tripSeatQuotaDTO.getSeatDetails().getSeatCode());
			tripSeatQuotaCacheDTO.setSeatFare(tripSeatQuotaDTO.getSeatDetails().getSeatFare());
			tripSeatQuotaCacheDTO.setAcBusTax(tripSeatQuotaDTO.getSeatDetails().getAcBusTax());
			tripSeatQuotaCacheDTO.setSeatName(tripSeatQuotaDTO.getSeatDetails().getSeatName());
			tripSeatQuotaCacheDTO.setSeatGendar(tripSeatQuotaDTO.getSeatDetails().getSeatGendar().getCode());
			tripSeatQuotaCacheDTO.setTripCode(tripSeatQuotaDTO.getTrip().getCode());
			tripSeatQuotaCacheDTO.setFromStationId(tripSeatQuotaDTO.getFromStation().getId());
			tripSeatQuotaCacheDTO.setToStationId(tripSeatQuotaDTO.getToStation().getId());
			tripSeatQuotaCacheDTO.setUserId(tripSeatQuotaDTO.getUser().getId());
			tripSeatQuotaCacheDTO.setId(tripSeatQuotaDTO.getId());
			tripSeatQuotaCacheDTO.setRelaseMinutes(tripSeatQuotaDTO.getRelaseMinutes());
			tripSeatQuotaCacheDTO.setRemarks(tripSeatQuotaDTO.getRemarks());
			tripSeatQuotaCacheDTO.setUpdatedAt(tripSeatQuotaDTO.getUpdatedAt());
			tripSeatQuotaCacheDTO.setUpdatedUserId(tripSeatQuotaDTO.getUpdatedBy().getId());
			tripSeatQuotaCacheDTO.setActiveFlag(tripSeatQuotaDTO.getActiveFlag());
			tripSeatQuotaCacheList.add(tripSeatQuotaCacheDTO);
		}
		return tripSeatQuotaCacheList;
	}

	private List<TripSeatQuotaDTO> bindTripSeatQuotaFromCache(TripDTO tripDTO, List<TripSeatQuotaCacheDTO> list) {
		List<TripSeatQuotaDTO> tripSeatQuotaList = new ArrayList<TripSeatQuotaDTO>();
		for (TripSeatQuotaCacheDTO tripSeatQuotaCacheDTO : list) {
			TripSeatQuotaDTO tripSeatQuotaDTO = new TripSeatQuotaDTO();
			tripSeatQuotaDTO.setId(tripSeatQuotaCacheDTO.getId());

			TicketDetailsDTO ticketDetails = new TicketDetailsDTO();
			ticketDetails.setSeatCode(tripSeatQuotaCacheDTO.getSeatCode());
			ticketDetails.setSeatFare(tripSeatQuotaCacheDTO.getSeatFare());
			ticketDetails.setAcBusTax(tripSeatQuotaCacheDTO.getAcBusTax());
			ticketDetails.setSeatName(tripSeatQuotaCacheDTO.getSeatName());
			ticketDetails.setSeatGendar(SeatGendarEM.getSeatGendarEM(tripSeatQuotaCacheDTO.getSeatGendar()));
			tripSeatQuotaDTO.setSeatDetails(ticketDetails);
			// From Station
			StationDTO fromStation = new StationDTO();
			fromStation.setId(tripSeatQuotaCacheDTO.getFromStationId());
			tripSeatQuotaDTO.setFromStation(fromStation);
			// To Station
			StationDTO toStation = new StationDTO();
			toStation.setId(tripSeatQuotaCacheDTO.getToStationId());
			tripSeatQuotaDTO.setToStation(toStation);
			// User
			UserDTO userDTO = new UserDTO();
			userDTO.setId(tripSeatQuotaCacheDTO.getUserId());
			tripSeatQuotaDTO.setUser(userDTO);
			// Updated User
			UserDTO updatedUser = new UserDTO();
			updatedUser.setId(tripSeatQuotaCacheDTO.getUpdatedUserId());
			tripSeatQuotaDTO.setUpdatedBy(updatedUser);

			tripSeatQuotaDTO.setRelaseMinutes(tripSeatQuotaCacheDTO.getRelaseMinutes());
			tripSeatQuotaDTO.setRemarks(tripSeatQuotaCacheDTO.getRemarks());
			tripSeatQuotaDTO.setActiveFlag(tripSeatQuotaCacheDTO.getActiveFlag());
			tripSeatQuotaDTO.setUpdatedAt(tripSeatQuotaCacheDTO.getUpdatedAt());

			tripSeatQuotaDTO.setTrip(tripDTO);
			tripSeatQuotaList.add(tripSeatQuotaDTO);
		}
		return tripSeatQuotaList;
	}
}
