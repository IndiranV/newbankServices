package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.TicketAfterTripTimeCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.NamespaceEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TravelStatusEM;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

public class TicketHelperCache extends TicketCache {

	public List<TicketDTO> getTicketAfterTripTime(AuthDTO authDTO) {
		List<TicketDTO> list = new ArrayList<TicketDTO>();
		List<String> keys = EhcacheManager.getTicketAfterTripTimeCache().getKeys();
		for (String key : keys) {
			Element element = EhcacheManager.getTicketAfterTripTimeCache().get(key);
			if (element != null) {
				TicketAfterTripTimeCacheDTO timeCacheDTO = (TicketAfterTripTimeCacheDTO) element.getObjectValue();
				if (timeCacheDTO.getNamespaceCode().equals(authDTO.getNamespaceCode()) || authDTO.getNamespaceCode().equals(NamespaceEM.BITS_ADMIN.getCode())) {
					TicketDTO ticketDTO = bindFromCacheObject(authDTO, timeCacheDTO);
					list.add(ticketDTO);
				}
			}
		}
		return list;
	}

	public void putTicketAfterTripTimeInCache(AuthDTO authDTO, TicketDTO ticketDTO) {
		Element element = new Element(ticketDTO.getCode(), bindTripAfterDTOToCacheObject(authDTO, ticketDTO));
		EhcacheManager.getTicketAfterTripTimeCache().put(element);
	}

	public void removeTicketAfterTripTimeInCache(AuthDTO authDTO, String ticketCode) {
		EhcacheManager.getTicketAfterTripTimeCache().remove(ticketCode);
	}

	public List<TicketDTO> getTicketNotBoarded(AuthDTO authDTO) {
		List<TicketDTO> list = new ArrayList<TicketDTO>();
		List<String> keys = EhcacheManager.getTicketNotBoardedCache().getKeys();
		for (String key : keys) {
			Element element = EhcacheManager.getTicketNotBoardedCache().get(key);
			if (element != null) {
				TicketAfterTripTimeCacheDTO timeCacheDTO = (TicketAfterTripTimeCacheDTO) element.getObjectValue();
				if (timeCacheDTO.getNamespaceCode().equals(authDTO.getNamespaceCode()) || authDTO.getNamespaceCode().equals(NamespaceEM.BITS_ADMIN.getCode())) {
					TicketDTO ticketDTO = bindTicketFromCacheObject(authDTO, timeCacheDTO);
					list.add(ticketDTO);
				}
			}
		}
		return list;
	}

	public TicketDTO getTicketNotBoarded(AuthDTO authDTO, String ticketCode) {
		TicketDTO ticketDTO = null;
		Element element = EhcacheManager.getTicketNotBoardedCache().get(ticketCode);
		if (element != null) {
			TicketAfterTripTimeCacheDTO timeCacheDTO = (TicketAfterTripTimeCacheDTO) element.getObjectValue();
			if (timeCacheDTO.getNamespaceCode().equals(authDTO.getNamespaceCode()) || authDTO.getNamespaceCode().equals(NamespaceEM.BITS_ADMIN.getCode())) {
				ticketDTO = bindTicketFromCacheObject(authDTO, timeCacheDTO);
			}
		}
		return ticketDTO;
	}

	public void putTicketNotBoardedInCache(AuthDTO authDTO, TicketDTO ticketDTO) {
		Element element = new Element(ticketDTO.getCode(), bindTicketToCacheObject(authDTO, ticketDTO));
		EhcacheManager.getTicketNotBoardedCache().put(element);
	}

	public void removeTicketNotBoardedInCache(AuthDTO authDTO, String ticketCode) {
		EhcacheManager.getTicketNotBoardedCache().remove(ticketCode);
	}

	private TicketDTO bindFromCacheObject(AuthDTO authDTO, TicketAfterTripTimeCacheDTO timeCacheDTO) {
		TicketDTO ticketDTO = new TicketDTO();

		ticketDTO.setTicketAt(new DateTime(timeCacheDTO.getTicketAt()));
		ticketDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(timeCacheDTO.getDeviceMediumCode()));
		StationDTO fromStationDTO = new StationDTO();
		StationDTO toStationDTO = new StationDTO();
		fromStationDTO.setId(timeCacheDTO.getFromStationId());
		toStationDTO.setId(timeCacheDTO.getToStationId());
		ticketDTO.setFromStation(fromStationDTO);
		ticketDTO.setToStation(toStationDTO);
		ticketDTO.setPassengerMobile(timeCacheDTO.getPassengerMobile());
		ticketDTO.setRemarks(timeCacheDTO.getRemarks());
		ticketDTO.setServiceNo(timeCacheDTO.getServiceNo());
		UserDTO userDTO = new UserDTO();
		userDTO.setId(timeCacheDTO.getTicketUserId());
		ticketDTO.setTicketUser(userDTO);

		ticketDTO.setCode(timeCacheDTO.getCode());
		ticketDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(timeCacheDTO.getTicketStatusCode()));
		ticketDTO.setTripDate(new DateTime(timeCacheDTO.getTripDate()));
		ticketDTO.setTravelMinutes(timeCacheDTO.getTravelMinutes());
		ticketDTO.setNamespaceCode(timeCacheDTO.getNamespaceCode());

		StationPointDTO stationPointDTO = new StationPointDTO();
		stationPointDTO.setMinitues(timeCacheDTO.getBoardingPointMinitues());
		stationPointDTO.setCode(timeCacheDTO.getBoardingPointCode());
		ticketDTO.setBoardingPoint(stationPointDTO);
		List<TicketDetailsDTO> seatDetails = new ArrayList<TicketDetailsDTO>();
		for (String seat : timeCacheDTO.getSeatName().split("\\s*,\\s*")) {
			TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
			detailsDTO.setSeatName(seat);
			detailsDTO.setPassengerName(timeCacheDTO.getPassengerName());
			seatDetails.add(detailsDTO);
		}
		ticketDTO.setTicketDetails(seatDetails);

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(timeCacheDTO.getTripCode());
		tripDTO.setTripDate(new DateTime(timeCacheDTO.getTripDate()));
		tripDTO.setTripMinutes(timeCacheDTO.getTripMinutes());
		BusDTO busDTO = new BusDTO();
		busDTO.setCode(timeCacheDTO.getBusCode());
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setId(timeCacheDTO.getScheduleId());
		tripDTO.setSchedule(scheduleDTO);
		tripDTO.setBus(busDTO);
		ticketDTO.setTripDTO(tripDTO);
		return ticketDTO;
	}

	protected TicketAfterTripTimeCacheDTO bindTripAfterDTOToCacheObject(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketAfterTripTimeCacheDTO timeCacheDTO = new TicketAfterTripTimeCacheDTO();
		timeCacheDTO.setNamespaceCode(authDTO.getNamespaceCode());

		timeCacheDTO.setCode(ticketDTO.getCode());

		timeCacheDTO.setTripCode(ticketDTO.getTripDTO().getCode());
		timeCacheDTO.setTripDate(ticketDTO.getTripDTO().getTripDate().format("YYYY-MM-DD"));
		timeCacheDTO.setTripMinutes(ticketDTO.getTripDTO().getTripMinutes());
		timeCacheDTO.setBusCode(ticketDTO.getTripDTO().getBus().getCode());
		timeCacheDTO.setScheduleId(ticketDTO.getTripDTO().getSchedule().getId());

		timeCacheDTO.setTicketAt(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
		timeCacheDTO.setDeviceMediumCode(ticketDTO.getDeviceMedium().getCode());
		timeCacheDTO.setFromStationId(ticketDTO.getFromStation().getId());
		timeCacheDTO.setToStationId(ticketDTO.getToStation().getId());
		timeCacheDTO.setPassengerMobile(ticketDTO.getPassengerMobile());
		timeCacheDTO.setRemarks(ticketDTO.getRemarks());
		timeCacheDTO.setServiceNo(ticketDTO.getServiceNo());
		timeCacheDTO.setTicketUserId(ticketDTO.getTicketUser().getId());
		timeCacheDTO.setTicketStatusCode(ticketDTO.getTicketStatus().getCode());
		timeCacheDTO.setTripDate(ticketDTO.getTripDate().format("YYYY-MM-DD"));
		timeCacheDTO.setTravelMinutes(ticketDTO.getTravelMinutes());

		timeCacheDTO.setBoardingPointCode(ticketDTO.getBoardingPoint().getCode());
		timeCacheDTO.setBoardingPointMinitues(ticketDTO.getBoardingPoint().getMinitues());

		timeCacheDTO.setSeatName(ticketDTO.getSeatNames());
		timeCacheDTO.setPassengerName(ticketDTO.getPassengerNames());
		return timeCacheDTO;
	}

	private TicketDTO bindTicketFromCacheObject(AuthDTO authDTO, TicketAfterTripTimeCacheDTO timeCacheDTO) {
		TicketDTO ticketDTO = new TicketDTO();

		ticketDTO.setTicketAt(new DateTime(timeCacheDTO.getTicketAt()));
		ticketDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(timeCacheDTO.getDeviceMediumCode()));
		StationDTO fromStationDTO = new StationDTO();
		StationDTO toStationDTO = new StationDTO();
		fromStationDTO.setId(timeCacheDTO.getFromStationId());
		toStationDTO.setId(timeCacheDTO.getToStationId());
		ticketDTO.setFromStation(fromStationDTO);
		ticketDTO.setToStation(toStationDTO);
		ticketDTO.setPassengerMobile(timeCacheDTO.getPassengerMobile());
		ticketDTO.setRemarks(timeCacheDTO.getRemarks());
		ticketDTO.setServiceNo(timeCacheDTO.getServiceNo());
		UserDTO userDTO = new UserDTO();
		userDTO.setId(timeCacheDTO.getTicketUserId());
		ticketDTO.setTicketUser(userDTO);

		ticketDTO.setCode(timeCacheDTO.getCode());
		ticketDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(timeCacheDTO.getTicketStatusCode()));
		ticketDTO.setTripDate(new DateTime(timeCacheDTO.getTripDate()));
		ticketDTO.setTravelMinutes(timeCacheDTO.getTravelMinutes());
		ticketDTO.setNamespaceCode(timeCacheDTO.getNamespaceCode());

		StationPointDTO stationPointDTO = new StationPointDTO();
		stationPointDTO.setMinitues(timeCacheDTO.getBoardingPointMinitues());
		stationPointDTO.setCode(timeCacheDTO.getBoardingPointCode());
		ticketDTO.setBoardingPoint(stationPointDTO);
		List<TicketDetailsDTO> seatDetails = new ArrayList<TicketDetailsDTO>();
		for (String seat : timeCacheDTO.getSeatName().split("\\s*,\\s*")) {
			TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
			detailsDTO.setSeatCode(seat.split(Text.HYPHEN)[Numeric.ZERO_INT]);
			detailsDTO.setSeatName(seat.split(Text.HYPHEN)[Numeric.ONE_INT]);
			detailsDTO.setPassengerName(seat.split(Text.HYPHEN)[Numeric.TWO_INT]);
			detailsDTO.setTravelStatus(TravelStatusEM.getTravelStatusEM(seat.split(Text.HYPHEN)[Numeric.THREE_INT]));
			seatDetails.add(detailsDTO);
		}
		ticketDTO.setTicketDetails(seatDetails);

		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(timeCacheDTO.getTripCode());
		tripDTO.setTripDate(new DateTime(timeCacheDTO.getTripDate()));
		tripDTO.setTripMinutes(timeCacheDTO.getTripMinutes());
		BusDTO busDTO = new BusDTO();
		busDTO.setCode(timeCacheDTO.getBusCode());
		tripDTO.setBus(busDTO);
		ticketDTO.setTripDTO(tripDTO);
		return ticketDTO;
	}

	protected TicketAfterTripTimeCacheDTO bindTicketToCacheObject(AuthDTO authDTO, TicketDTO ticketDTO) {
		TicketAfterTripTimeCacheDTO timeCacheDTO = new TicketAfterTripTimeCacheDTO();
		timeCacheDTO.setNamespaceCode(authDTO.getNamespaceCode());

		timeCacheDTO.setCode(ticketDTO.getCode());

		timeCacheDTO.setTripCode(ticketDTO.getTripDTO().getCode());
		timeCacheDTO.setTripDate(ticketDTO.getTripDTO().getTripDate().format("YYYY-MM-DD"));
		timeCacheDTO.setTripMinutes(ticketDTO.getTripDTO().getTripMinutes());
		timeCacheDTO.setBusCode(ticketDTO.getTripDTO().getBus().getCode());

		timeCacheDTO.setTicketAt(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
		timeCacheDTO.setDeviceMediumCode(ticketDTO.getDeviceMedium().getCode());
		timeCacheDTO.setFromStationId(ticketDTO.getFromStation().getId());
		timeCacheDTO.setToStationId(ticketDTO.getToStation().getId());
		timeCacheDTO.setPassengerMobile(ticketDTO.getPassengerMobile());
		timeCacheDTO.setRemarks(ticketDTO.getRemarks());
		timeCacheDTO.setServiceNo(ticketDTO.getServiceNo());
		timeCacheDTO.setTicketUserId(ticketDTO.getTicketUser().getId());
		timeCacheDTO.setTicketStatusCode(ticketDTO.getTicketStatus().getCode());
		timeCacheDTO.setTripDate(ticketDTO.getTripDate().format("YYYY-MM-DD"));
		timeCacheDTO.setTravelMinutes(ticketDTO.getTravelMinutes());

		timeCacheDTO.setBoardingPointCode(ticketDTO.getBoardingPoint().getCode());
		timeCacheDTO.setBoardingPointMinitues(ticketDTO.getBoardingPoint().getMinitues());

		timeCacheDTO.setSeatName(ticketDTO.getSeatCodeNames());
		return timeCacheDTO;
	}

}
