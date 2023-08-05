package org.in.com.cache;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.in.com.cache.dto.BookingCacheDTO;
import org.in.com.cache.dto.ScheduleStationPointCacheDTO;
import org.in.com.cache.dto.TicketAddonsDetailsCacheDTO;
import org.in.com.cache.dto.TicketCacheDTO;
import org.in.com.cache.dto.TicketDetailsCacheDTO;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Text;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BookingDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.dto.StageDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.TicketAddonsDetailsDTO;
import org.in.com.dto.TicketDTO;
import org.in.com.dto.TicketDetailsDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AddonsTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.dto.enumeration.JourneyTypeEM;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.dto.enumeration.TicketStatusEM;
import org.in.com.dto.enumeration.TransactionModeEM;
import org.in.com.dto.enumeration.TransactionTypeEM;
import org.in.com.dto.enumeration.TravelStatusEM;
import org.in.com.utils.StringUtil;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

public class TicketCache extends CacheCentral {

	public BookingDTO getBookingDTO(AuthDTO authDTO, String bookingCode) {
		BookingDTO bookingDTO = null;
		Element element = EhcacheManager.getBookingCache().get(bookingCode);
		if (element != null) {
			BookingCacheDTO bookingCacheDTO = (BookingCacheDTO) element.getObjectValue();
			bookingDTO = bindFromCacheObject(authDTO, bookingCacheDTO);
		}
		return bookingDTO;
	}

	public List<BookingDTO> getAllBookingDTO(AuthDTO authDTO) {
		List<BookingDTO> list = new ArrayList<BookingDTO>();
		List<String> keys = EhcacheManager.getBookingCache().getKeys();
		for (String key : keys) {
			Element element = EhcacheManager.getBookingCache().get(key);
			if (element != null) {
				BookingCacheDTO bookingCacheDTO = (BookingCacheDTO) element.getObjectValue();
				BookingDTO bookingDTO = bindFromCacheObject(authDTO, bookingCacheDTO);
				if (bookingDTO.getNamespace().getId() == authDTO.getNamespace().getId() || authDTO.getNamespaceCode().equals(ApplicationConfig.getServerZoneCode())) {
					list.add(bookingDTO);
				}
			}
		}
		return list;
	}

	public void putBookingDTO(AuthDTO authDTO, BookingDTO bookingDTO) {
		// copy object
		Element element = new Element(bookingDTO.getCode(), bindToCacheObject(bookingDTO));
		EhcacheManager.getBookingCache().put(element);

		// Refresh Fresh Transaction
		EhcacheManager.getFreshTransactionEhCache().remove(bookingDTO.getCode());
	}

	public void putTicketEvent(AuthDTO authDTO, BookingDTO bookingDTO) {
		int timeToLiveSec = authDTO.getNamespace().getProfile().getSeatBlockTime() >= 8 ? (authDTO.getNamespace().getProfile().getSeatBlockTime() - 3) * 60 : authDTO.getNamespace().getProfile().getSeatBlockTime();

		Element element = new Element(bookingDTO.getCode(), authDTO.getNamespaceCode());
		element.setTimeToIdle(timeToLiveSec);
		element.setTimeToLive(timeToLiveSec);

		EhcacheManager.getTicketEventEhCache().put(element);
	}

	public void removeString(AuthDTO authDTO, String bookingCode) {
		EhcacheManager.getBookingCache().remove(bookingCode);
	}

	protected BookingCacheDTO bindToCacheObject(BookingDTO bookingDTO) {
		BookingCacheDTO bookingCacheDTO = new BookingCacheDTO();
		bookingCacheDTO.setCode(bookingDTO.getCode());
		bookingCacheDTO.setNamespaceCode(bookingDTO.getNamespace().getCode());
		bookingCacheDTO.setPaymentGatewayPartnerCode(bookingDTO.getPaymentGatewayPartnerCode());
		bookingCacheDTO.setPaymentGatewayProcessFlag(bookingDTO.isPaymentGatewayProcessFlag());
		bookingCacheDTO.setTransactionDate(bookingDTO.getTransactionDate());

		List<TicketCacheDTO> ticketCacheList = new ArrayList<TicketCacheDTO>();
		for (TicketDTO ticketDTO : bookingDTO.getTicketList()) {
			TicketCacheDTO ticketCacheDTO = new TicketCacheDTO();
			ticketCacheDTO.setCode(ticketDTO.getCode());

			ticketCacheDTO.setTripCode(ticketDTO.getTripDTO().getCode());
			ticketCacheDTO.setTripDate(ticketDTO.getTripDTO().getTripDate().format("YYYY-MM-DD"));
			ticketCacheDTO.setTripMinutes(ticketDTO.getTripDTO().getTripMinutes());
			ticketCacheDTO.setTripStageCode(ticketDTO.getTripDTO().getStage().getCode());
			ticketCacheDTO.setReleatedStageCode(StringUtils.join(ticketDTO.getTripDTO().getReleatedStageCodeList(), ','));
			ticketCacheDTO.setBusCode(ticketDTO.getTripDTO().getBus().getCode());
			ticketCacheDTO.setReportingMinutes(ticketDTO.getReportingMinutes());

			ticketCacheDTO.setBlockingLiveTime(ticketDTO.getBlockingLiveTime().format("YYYY-MM-DD hh:mm:ss"));
			ticketCacheDTO.setTicketAt(ticketDTO.getTicketAt().format("YYYY-MM-DD hh:mm:ss"));
			ticketCacheDTO.setBookingCode(ticketDTO.getBookingCode());
			ticketCacheDTO.setDeviceMediumCode(ticketDTO.getDeviceMedium().getCode());
			ticketCacheDTO.setFromStationId(ticketDTO.getFromStation().getId());
			ticketCacheDTO.setToStationId(ticketDTO.getToStation().getId());
			ticketCacheDTO.setJourneyType(ticketDTO.getJourneyType().getCode());
			ticketCacheDTO.setPassengerEmailId(ticketDTO.getPassengerEmailId());
			ticketCacheDTO.setPassengerMobile(ticketDTO.getPassengerMobile());
			ticketCacheDTO.setAlternateMobile(StringUtil.isNull(ticketDTO.getAlternateMobile(), Text.EMPTY));
			ticketCacheDTO.setRemarks(ticketDTO.getRemarks());
			ticketCacheDTO.setRelatedTicketCode(ticketDTO.getRelatedTicketCode());
			ticketCacheDTO.setServiceNo(ticketDTO.getServiceNo());
			ticketCacheDTO.setTicketForUserId(ticketDTO.getTicketForUser().getId());
			ticketCacheDTO.setTicketUserId(ticketDTO.getTicketUser().getId());
			ticketCacheDTO.setTicketStatusCode(ticketDTO.getTicketStatus().getCode());
			// ticketCacheDTO.setTransactionModeId(ticketDTO.getTransactionModeDTO().getId());
			// ticketCacheDTO.setTransactionTypeId(ticketDTO.getTransactionTypeDTO().getId());
			ticketCacheDTO.setTripDate(ticketDTO.getTripDate().format("YYYY-MM-DD"));
			ticketCacheDTO.setTravelMinutes(ticketDTO.getTravelMinutes());

			ticketCacheDTO.setCancellationTermId(ticketDTO.getCancellationTerm().getId());

			ScheduleStationPointCacheDTO boardingPointCache = new ScheduleStationPointCacheDTO();
			boardingPointCache.setCreditDebitFlag(ticketDTO.getBoardingPoint().getCreditDebitFlag());
			boardingPointCache.setMinitues(ticketDTO.getBoardingPoint().getMinitues());
			boardingPointCache.setStationPointId(ticketDTO.getBoardingPoint().getId());
			ticketCacheDTO.setBoardingPointCacheDTO(boardingPointCache);

			ScheduleStationPointCacheDTO droppingPointCache = new ScheduleStationPointCacheDTO();
			droppingPointCache.setCreditDebitFlag(ticketDTO.getDroppingPoint().getCreditDebitFlag());
			droppingPointCache.setMinitues(ticketDTO.getDroppingPoint().getMinitues());
			droppingPointCache.setStationPointId(ticketDTO.getDroppingPoint().getId());
			ticketCacheDTO.setDroppingPointCacheDTO(droppingPointCache);

			List<TicketDetailsCacheDTO> detailCacheList = new ArrayList<TicketDetailsCacheDTO>();
			for (TicketDetailsDTO ticketDetailsDTO : ticketDTO.getTicketDetails()) {
				TicketDetailsCacheDTO detailsCacheDTO = new TicketDetailsCacheDTO();
				detailsCacheDTO.setPassengerAge(ticketDetailsDTO.getPassengerAge());
				detailsCacheDTO.setPassengerName(ticketDetailsDTO.getPassengerName());
				detailsCacheDTO.setSeatCode(ticketDetailsDTO.getSeatCode());
				detailsCacheDTO.setSeatName(ticketDetailsDTO.getSeatName());
				detailsCacheDTO.setSeatType(ticketDetailsDTO.getSeatType());
				detailsCacheDTO.setSeatGendarCode(ticketDetailsDTO.getSeatGendar().getCode());
				detailsCacheDTO.setSeatFare(ticketDetailsDTO.getSeatFare());
				detailsCacheDTO.setAcBusTax(ticketDetailsDTO.getAcBusTax());
				detailsCacheDTO.setTicketStatusId(ticketDetailsDTO.getTicketStatus().getId());
				detailCacheList.add(detailsCacheDTO);
			}
			ticketCacheDTO.setTicketDetailsCache(detailCacheList);

			List<TicketAddonsDetailsCacheDTO> addonsCacheList = new ArrayList<TicketAddonsDetailsCacheDTO>();
			if (ticketDTO.getTicketAddonsDetails() != null)
				for (TicketAddonsDetailsDTO addonsDetailsDTO : ticketDTO.getTicketAddonsDetails()) {
					TicketAddonsDetailsCacheDTO addonsDetailsCacheDTO = new TicketAddonsDetailsCacheDTO();
					addonsDetailsCacheDTO.setValue(addonsDetailsDTO.getValue());
					addonsDetailsCacheDTO.setAddonsTypeCode(addonsDetailsDTO.getAddonsType().getCode());
					addonsDetailsCacheDTO.setSeatCode(addonsDetailsDTO.getSeatCode());
					addonsDetailsCacheDTO.setRefferenceCode(addonsDetailsDTO.getRefferenceCode());
					addonsDetailsCacheDTO.setRefferenceId(addonsDetailsDTO.getRefferenceId());
					addonsCacheList.add(addonsDetailsCacheDTO);
				}
			ticketCacheDTO.setTicketAddonsDetailsCache(addonsCacheList);
			ticketCacheList.add(ticketCacheDTO);
		}
		bookingCacheDTO.setTicketCacheDTO(ticketCacheList);
		return bookingCacheDTO;
	}

	protected BookingDTO bindFromCacheObject(AuthDTO authDTO, BookingCacheDTO bookingCacheDTO) {
		BookingDTO bookingDTO = new BookingDTO();
		bookingDTO.setNamespace(getNamespaceDTO(bookingCacheDTO.getNamespaceCode()));
		bookingDTO.setCode(bookingCacheDTO.getCode());
		bookingDTO.setCouponCode(bookingCacheDTO.getCouponCode());
		bookingDTO.setPaymentGatewayPartnerCode(bookingCacheDTO.getPaymentGatewayPartnerCode());
		bookingDTO.setPaymentGatewayProcessFlag(bookingCacheDTO.isPaymentGatewayProcessFlag());
		bookingDTO.setTransactionDate(bookingCacheDTO.getTransactionDate());
		bookingDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(bookingCacheDTO.getTransactionModeId()));

		List<TicketDTO> ticketDTOList = new ArrayList<TicketDTO>();
		BusCache busCache = new BusCache();
		CancellationTermsCache termsCache = new CancellationTermsCache();
		for (TicketCacheDTO ticketCacheDTO : bookingCacheDTO.getTicketCacheDTO()) {
			TicketDTO ticketDTO = new TicketDTO();
			ticketDTO.setCode(ticketCacheDTO.getCode());
			TripDTO tripDTO = new TripDTO();
			tripDTO.setCode(ticketCacheDTO.getTripCode());
			tripDTO.setTripDate(new DateTime(ticketCacheDTO.getTripDate()));
			tripDTO.setTripMinutes(ticketCacheDTO.getTripMinutes());

			List<String> ticketStatusCodeList = Arrays.asList(ticketCacheDTO.getReleatedStageCode().split(","));
			StageDTO stageDTO = new StageDTO();
			stageDTO.setCode(ticketCacheDTO.getTripStageCode());
			tripDTO.setReleatedStageCodeList(ticketStatusCodeList);
			tripDTO.setStage(stageDTO);

			BusDTO busDTO = new BusDTO();
			busDTO.setCode(ticketCacheDTO.getBusCode());
			tripDTO.setBus(busCache.getBusDTO(authDTO, busDTO));
			ticketDTO.setTripDTO(tripDTO);

			ticketDTO.setBlockingLiveTime(new DateTime(ticketCacheDTO.getBlockingLiveTime()));
			ticketDTO.setTicketAt(new DateTime(ticketCacheDTO.getTicketAt()));
			ticketDTO.setBookingCode(ticketCacheDTO.getBookingCode());
			ticketDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(ticketCacheDTO.getDeviceMediumCode()));
			StationDTO fromStationDTO = new StationDTO();
			fromStationDTO.setId(ticketCacheDTO.getFromStationId());
			ticketDTO.setFromStation(getStationDTObyId(fromStationDTO));

			StationDTO toStationDTO = new StationDTO();
			toStationDTO.setId(ticketCacheDTO.getToStationId());
			ticketDTO.setToStation(getStationDTObyId(toStationDTO));

			ticketDTO.setJourneyType(JourneyTypeEM.getJourneyTypeEM(ticketCacheDTO.getJourneyType()));
			ticketDTO.setPassengerEmailId(ticketCacheDTO.getPassengerEmailId());
			ticketDTO.setPassengerMobile(ticketCacheDTO.getPassengerMobile());
			ticketDTO.setAlternateMobile(ticketCacheDTO.getAlternateMobile());
			ticketDTO.setRemarks(ticketCacheDTO.getRemarks());
			ticketDTO.setRelatedTicketCode(ticketCacheDTO.getRelatedTicketCode());
			ticketDTO.setServiceNo(ticketCacheDTO.getServiceNo());

			UserDTO forUserDTO = new UserDTO();
			forUserDTO.setId(ticketCacheDTO.getTicketForUserId());
			ticketDTO.setTicketForUser(forUserDTO);

			UserDTO userDTO = new UserDTO();
			userDTO.setId(ticketCacheDTO.getTicketUserId());
			ticketDTO.setTicketUser(userDTO);

			ticketDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketCacheDTO.getTicketStatusCode()));
			ticketDTO.setTransactionMode(TransactionModeEM.getTransactionModeEM(ticketCacheDTO.getTransactionModeId()));
			ticketDTO.setTransactionType(TransactionTypeEM.getTransactionTypeEM(ticketCacheDTO.getTransactionModeId()));
			ticketDTO.setTripDate(new DateTime(ticketCacheDTO.getTripDate()));
			ticketDTO.setTravelMinutes(ticketCacheDTO.getTravelMinutes());
			ticketDTO.setReportingMinutes(ticketCacheDTO.getReportingMinutes());

			CancellationTermDTO cancellationTermDTO = new CancellationTermDTO();
			cancellationTermDTO.setId(ticketCacheDTO.getCancellationTermId());
			ticketDTO.setCancellationTerm(termsCache.getCancellationTermDTOById(authDTO, cancellationTermDTO));

			StationPointDTO boardingPointDTO = new StationPointDTO();
			boardingPointDTO.setId(ticketCacheDTO.getBoardingPointCacheDTO().getStationPointId());
			getStationPointDTObyId(authDTO, boardingPointDTO);
			// boardingPointDTO.setName(ticketCacheDTO.getBoardingPointCacheDTO().getName());
			// boardingPointDTO.setCode(ticketCacheDTO.getBoardingPointCacheDTO().getCode());
			boardingPointDTO.setCreditDebitFlag(ticketCacheDTO.getBoardingPointCacheDTO().getCreditDebitFlag());
			boardingPointDTO.setMinitues(ticketCacheDTO.getBoardingPointCacheDTO().getMinitues());
			ticketDTO.setBoardingPoint(boardingPointDTO);

			StationPointDTO droppingPointDTO = new StationPointDTO();
			droppingPointDTO.setId(ticketCacheDTO.getDroppingPointCacheDTO().getStationPointId());
			getStationPointDTObyId(authDTO, droppingPointDTO);
			// droppingPointDTO.setName(ticketCacheDTO.getDroppingPointCacheDTO().getName());
			// droppingPointDTO.setCode(ticketCacheDTO.getDroppingPointCacheDTO().getCode());
			droppingPointDTO.setCreditDebitFlag(ticketCacheDTO.getDroppingPointCacheDTO().getCreditDebitFlag());
			droppingPointDTO.setMinitues(ticketCacheDTO.getDroppingPointCacheDTO().getMinitues());
			ticketDTO.setDroppingPoint(droppingPointDTO);

			List<TicketDetailsDTO> ticketDetailList = new ArrayList<TicketDetailsDTO>();
			for (TicketDetailsCacheDTO ticketDetailsCacheDTO : ticketCacheDTO.getTicketDetailsCache()) {
				TicketDetailsDTO detailsDTO = new TicketDetailsDTO();
				detailsDTO.setPassengerAge(ticketDetailsCacheDTO.getPassengerAge());
				detailsDTO.setPassengerName(ticketDetailsCacheDTO.getPassengerName());
				detailsDTO.setSeatCode(ticketDetailsCacheDTO.getSeatCode());
				detailsDTO.setSeatName(ticketDetailsCacheDTO.getSeatName());
				detailsDTO.setSeatType(ticketDetailsCacheDTO.getSeatType());
				detailsDTO.setSeatGendar(SeatGendarEM.getSeatGendarEM(ticketDetailsCacheDTO.getSeatGendarCode()));
				detailsDTO.setSeatFare(ticketDetailsCacheDTO.getSeatFare());
				detailsDTO.setAcBusTax(ticketDetailsCacheDTO.getAcBusTax());
				detailsDTO.setTicketStatus(TicketStatusEM.getTicketStatusEM(ticketDetailsCacheDTO.getTicketStatusId()));
				detailsDTO.setTravelStatus(TravelStatusEM.YET_BOARD);
				ticketDetailList.add(detailsDTO);
			}
			ticketDTO.setTicketDetails(ticketDetailList);

			List<TicketAddonsDetailsDTO> addonsList = new ArrayList<TicketAddonsDetailsDTO>();
			for (TicketAddonsDetailsCacheDTO addonsDetailsCacheDTO : ticketCacheDTO.getTicketAddonsDetailsCache()) {
				TicketAddonsDetailsDTO addonsDetailsDTO = new TicketAddonsDetailsDTO();
				addonsDetailsDTO.setValue(addonsDetailsCacheDTO.getValue());
				addonsDetailsDTO.setAddonsType(AddonsTypeEM.getAddonsTypeEM(addonsDetailsCacheDTO.getAddonsTypeCode()));
				addonsDetailsDTO.setSeatCode(addonsDetailsCacheDTO.getSeatCode());
				addonsDetailsDTO.setRefferenceCode(addonsDetailsCacheDTO.getRefferenceCode());
				addonsDetailsDTO.setRefferenceId(addonsDetailsCacheDTO.getRefferenceId());
				addonsList.add(addonsDetailsDTO);
			}
			ticketDTO.setTicketAddonsDetails(addonsList);
			ticketDTOList.add(ticketDTO);
		}
		bookingDTO.setTicketList(ticketDTOList);
		return bookingDTO;
	}

	public BookingDTO bindFromCache(AuthDTO authDTO, BookingCacheDTO bookingCache) {
		return bindFromCacheObject(authDTO, bookingCache);
	}
}
