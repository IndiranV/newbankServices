package org.in.com.cache;

import hirondelle.date4j.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.sf.ehcache.Element;

import org.in.com.cache.dto.TripCacheDTO;
import org.in.com.cache.dto.TripInfoCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.TripDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusVehicleAttendantDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TripDTO;
import org.in.com.dto.TripInfoDTO;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.dto.enumeration.TripStatusEM;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

public class TripCache extends CacheCentral {

	public TripDTO getTripDTO(AuthDTO authDTO, String tripCode) {
		TripDTO tripDTO = null;
		Element element = EhcacheManager.getTripEhCache().get(tripCode);
		if (element != null) {
			TripCacheDTO tripCacheDTO = (TripCacheDTO) element.getObjectValue();
			tripDTO = bindFromCacheObject(authDTO, tripCacheDTO);
		}
		return tripDTO;
	}

	public TripDTO getTripDTO(AuthDTO authDTO, TripDTO tripDTO) {
		Element element = EhcacheManager.getTripEhCache().get(tripDTO.getCode());
		if (element != null) {
			TripCacheDTO tripCacheDTO = (TripCacheDTO) element.getObjectValue();
			bindFromCacheObject(authDTO, tripDTO, tripCacheDTO);
		}
		else {
			TripDAO tripDAO = new TripDAO();
			tripDAO.getTripDTO(authDTO, tripDTO);
			if (tripDTO == null || tripDTO.getId() == 0) {
				throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
			}
			tripDTO.setSyncTime(DateUtil.NOW().format("YYYYMMDDhhmmss"));
			element = new Element(tripDTO.getCode(), bindToCacheObject(tripDTO));
			EhcacheManager.getTripEhCache().put(element);
		}
		return tripDTO;
	}

	public TripInfoDTO getTripInfo(AuthDTO authDTO, TripDTO tripDTO) {
		TripInfoDTO tripInfoDTO = null;

		if (tripDTO.getId() != 0 && StringUtil.isNull(tripDTO.getCode())) {
			TripDTO tripCache = getTripById(authDTO, tripDTO);
			tripDTO.setCode(tripCache.getCode());
		}

		String key = "TINFO_" + tripDTO.getCode();
		Element element = EhcacheManager.getTripEhCache().get(key);

		if (element != null) {
			TripInfoCacheDTO tripInfoCacheDTO = (TripInfoCacheDTO) element.getObjectValue();
			tripInfoDTO = bindTripInfoFromCacheObject(tripInfoCacheDTO);
			tripDTO.setTripInfo(tripInfoDTO);
		}
		else {
			TripDAO tripDAO = new TripDAO();
			tripInfoDTO = tripDAO.getTripInfoDTO(authDTO, tripDTO);

			if (tripInfoDTO != null) {
				element = new Element(key, bindTripInfoToCacheObject(tripInfoDTO));
				EhcacheManager.getTripEhCache().put(element);
			}
		}
		return tripInfoDTO;
	}

	protected TripDTO getTripById(AuthDTO authDTO, TripDTO tripDTO) {
		String tripCode = null;
		String key = "TRIP" + "_" + tripDTO.getId();
		Element elementKey = EhcacheManager.getTripEhCache().get(key);
		if (elementKey != null) {
			tripCode = (String) elementKey.getObjectValue();
			tripDTO.setCode(tripCode);
		}
		tripDTO = getTripDTO(authDTO, tripDTO);
		if (elementKey == null && StringUtil.isNotNull(tripDTO.getCode()) && tripDTO.getId() != 0) {
			key = "TRIP" + "_" + tripDTO.getId();
			elementKey = new Element(key, tripDTO.getCode());
			EhcacheManager.getTripEhCache().put(elementKey);
		}
		return tripDTO;
	}

	private void bindFromCacheObject(AuthDTO authDTO, TripDTO tripDTO, TripCacheDTO tripCacheDTO) {
		tripDTO.setCode(tripCacheDTO.getTripCode());
		tripDTO.setId(tripCacheDTO.getId());
		tripDTO.setTripStatus(TripStatusEM.getTripStatusEM(tripCacheDTO.getTripStatusId()));
		BusDTO busDTO = new BusDTO();
		busDTO.setId(tripCacheDTO.getBusId());
		tripDTO.setBus(busDTO);
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setId(tripCacheDTO.getScheduleId());
		scheduleDTO.setTripDate(new DateTime(tripCacheDTO.getTripDate()));
		tripDTO.setSchedule(scheduleDTO);
		tripDTO.setTripDate(new DateTime(tripCacheDTO.getTripDate()));
		tripDTO.setTripMinutes(tripCacheDTO.getTripMinutes());
		tripDTO.setSyncTime(tripCacheDTO.getSyncTime());
		tripDTO.setRemarks(tripCacheDTO.getRemarks());
	}

	public void CheckAndGetTripDTO(AuthDTO authDTO, List<TripDTO> tripList) {
		for (TripDTO tripDTO : tripList) {
			TripDTO trip = getTripDTO(authDTO, tripDTO.getCode());
			if (trip != null && trip.getId() != 0 && trip.getSchedule().getId() == tripDTO.getSchedule().getId() && trip.getTripDate().format("YYYY-MM-DD").equals(tripDTO.getTripDate().format("YYYY-MM-DD")) && trip.getBus().getId() == tripDTO.getBus().getId() && trip.getTripMinutes() == tripDTO.getTripOriginMinutes()) {
				if (trip.getTripStatus().getId() == TripStatusEM.TRIP_CLOSED.getId() || trip.getTripStatus().getId() == TripStatusEM.TRIP_CANCELLED.getId()) {
					tripDTO.setTripStatus(trip.getTripStatus());
				}
				tripDTO.setId(trip.getId());
				tripDTO.setTripMinutes(trip.getTripMinutes());
				tripDTO.setRemarks(trip.getRemarks());
			}
		}
	}

	public void putAllTripDTO(AuthDTO authDTO, List<TripDTO> tripList) {
		for (TripDTO tripDTO : tripList) {
			// Assumption already in cache
			if (tripDTO.getId() == 0 || tripDTO.getActiveFlag() == -1) {
				continue;
			}
			tripDTO.setSyncTime(DateUtil.NOW().format("YYYYMMDDhhmmss"));
			Element element = new Element(tripDTO.getCode(), bindToCacheObject(tripDTO));
			EhcacheManager.getTripEhCache().put(element);
		}
	}

	public void updateTripSyncTime(AuthDTO authDTO, TripDTO tripDTO) {
		TripDTO trip = new TripDTO();
		trip.setCode(tripDTO.getCode());

		getTripDTO(authDTO, trip);

		putTripDTO(authDTO, trip);
		tripDTO.setSyncTime(trip.getSyncTime());
	}

	public void putTripDTO(AuthDTO authDTO, TripDTO tripDTO) {
		tripDTO.setSyncTime(DateUtil.NOW().format("YYYYMMDDhhmmss"));
		if (tripDTO == null || tripDTO.getId() == 0 || StringUtil.isNull(tripDTO.getCode())) {
			throw new ServiceException(ErrorCode.INVALID_TRIP_CODE);
		}
		Element element = new Element(tripDTO.getCode(), bindToCacheObject(tripDTO));
		EhcacheManager.getTripEhCache().put(element);
	}

	public void removeTrip(AuthDTO authDTO, String tripCode) {
		EhcacheManager.getTripEhCache().remove(tripCode);
		EhcacheManager.getTripEhCache().remove("TINFO_" + tripCode);
	}

	public void removeTripInfo(AuthDTO authDTO, TripDTO tripDTO) {
		EhcacheManager.getTripEhCache().remove("TINFO_" + tripDTO.getCode());
	}

	private TripCacheDTO bindToCacheObject(TripDTO tripDTO) {
		TripCacheDTO tripCacheDTO = new TripCacheDTO();
		tripCacheDTO.setTripCode(tripDTO.getCode());
		tripCacheDTO.setId(tripDTO.getId());
		tripCacheDTO.setScheduleId(tripDTO.getSchedule().getId());
		tripCacheDTO.setBusId(tripDTO.getBus().getId());
		tripCacheDTO.setTripDate(tripDTO.getTripDate().format(Text.DATE_DATE4J));
		tripCacheDTO.setTripStatusId(tripDTO.getTripStatus() != null ? tripDTO.getTripStatus().getId() : TripStatusEM.TRIP_NA.getId());
		tripCacheDTO.setTripMinutes(tripDTO.getTripMinutes());
		tripCacheDTO.setSyncTime(tripDTO.getSyncTime());
		tripCacheDTO.setRemarks(tripDTO.getRemarks());
		return tripCacheDTO;
	}

	protected TripDTO bindFromCacheObject(AuthDTO authDTO, TripCacheDTO tripCacheDTO) {
		TripDTO tripDTO = new TripDTO();
		tripDTO.setCode(tripCacheDTO.getTripCode());
		tripDTO.setId(tripCacheDTO.getId());
		tripDTO.setTripStatus(TripStatusEM.getTripStatusEM(tripCacheDTO.getTripStatusId()));
		BusDTO busDTO = new BusDTO();
		busDTO.setId(tripCacheDTO.getBusId());
		tripDTO.setBus(busDTO);
		ScheduleDTO scheduleDTO = new ScheduleDTO();
		scheduleDTO.setId(tripCacheDTO.getScheduleId());
		tripDTO.setSchedule(scheduleDTO);
		tripDTO.setTripDate(new DateTime(tripCacheDTO.getTripDate()));
		tripDTO.setTripMinutes(tripCacheDTO.getTripMinutes());
		tripDTO.setSyncTime(tripCacheDTO.getSyncTime());
		tripDTO.setRemarks(tripCacheDTO.getRemarks());
		return tripDTO;
	}

	protected TripInfoCacheDTO bindTripInfoToCacheObject(TripInfoDTO tripInfoDTO) {
		TripInfoCacheDTO tripInfoCacheDTO = new TripInfoCacheDTO();
		tripInfoCacheDTO.setDriverName(tripInfoDTO.getDriverName());
		tripInfoCacheDTO.setDriverMobile(tripInfoDTO.getDriverMobile());
		tripInfoCacheDTO.setDriverName2(tripInfoDTO.getDriverName2());
		tripInfoCacheDTO.setDriverMobile2(tripInfoDTO.getDriverMobile2());
		tripInfoCacheDTO.setAttenderName(tripInfoDTO.getAttenderName());
		tripInfoCacheDTO.setAttenderMobile(tripInfoDTO.getAttenderMobile());
		tripInfoCacheDTO.setCaptainName(tripInfoDTO.getCaptainName());
		tripInfoCacheDTO.setCaptainMobile(tripInfoDTO.getCaptainMobile());
		tripInfoCacheDTO.setPrimaryDriverId(tripInfoDTO.getPrimaryDriverId());
		tripInfoCacheDTO.setSecondaryDriverId(tripInfoDTO.getSecondaryDriverId());
		tripInfoCacheDTO.setAttendantId(tripInfoDTO.getAttendantId());
		tripInfoCacheDTO.setCaptainId(tripInfoDTO.getCaptainId());
		tripInfoCacheDTO.setTripStartDateTime(DateUtil.convertDateTime(tripInfoDTO.getTripStartDateTime()));
		tripInfoCacheDTO.setTripCloseDateTime(DateUtil.convertDateTime(tripInfoDTO.getTripCloseDateTime()));
		tripInfoCacheDTO.setRemarks(tripInfoDTO.getRemarks());
		tripInfoCacheDTO.setBusVehicleId(tripInfoDTO.getBusVehicle().getId());
		tripInfoCacheDTO.setExtras(tripInfoDTO.getExtras());
		tripInfoCacheDTO.setNotificationStatus(tripInfoDTO.getNotificationStatusCodes());
		return tripInfoCacheDTO;
	}

	protected TripInfoDTO bindTripInfoFromCacheObject(TripInfoCacheDTO tripInfoCacheDTO) {
		TripInfoDTO tripInfoDTO = new TripInfoDTO();
		tripInfoDTO.setDriverName(tripInfoCacheDTO.getDriverName());
		tripInfoDTO.setDriverMobile(tripInfoCacheDTO.getDriverMobile());
		tripInfoDTO.setDriverName2(tripInfoCacheDTO.getDriverName2());
		tripInfoDTO.setDriverMobile2(tripInfoCacheDTO.getDriverMobile2());
		tripInfoDTO.setAttenderName(tripInfoCacheDTO.getAttenderName());
		tripInfoDTO.setAttenderMobile(tripInfoCacheDTO.getAttenderMobile());
		tripInfoDTO.setCaptainName(tripInfoCacheDTO.getCaptainName());
		tripInfoDTO.setCaptainMobile(tripInfoCacheDTO.getCaptainMobile());

		BusVehicleDriverDTO primaryDriver = new BusVehicleDriverDTO();
		primaryDriver.setId(tripInfoCacheDTO.getPrimaryDriverId());
		tripInfoDTO.setPrimaryDriver(primaryDriver);

		BusVehicleDriverDTO secondaryDriver = new BusVehicleDriverDTO();
		secondaryDriver.setId(tripInfoCacheDTO.getSecondaryDriverId());
		tripInfoDTO.setSecondaryDriver(secondaryDriver);

		BusVehicleAttendantDTO attendant = new BusVehicleAttendantDTO();
		attendant.setId(tripInfoCacheDTO.getAttendantId());
		tripInfoDTO.setAttendant(attendant);

		BusVehicleAttendantDTO captain = new BusVehicleAttendantDTO();
		captain.setId(tripInfoCacheDTO.getCaptainId());
		tripInfoDTO.setCaptain(captain);

		tripInfoDTO.setTripStartDateTime(DateUtil.getDateTime(tripInfoCacheDTO.getTripStartDateTime()));
		tripInfoDTO.setTripCloseDateTime(DateUtil.getDateTime(tripInfoCacheDTO.getTripCloseDateTime()));
		tripInfoDTO.setRemarks(tripInfoCacheDTO.getRemarks());
		tripInfoDTO.setExtras(tripInfoCacheDTO.getExtras());

		if (StringUtil.isNotNull(tripInfoCacheDTO.getNotificationStatus())) {
			List<NotificationTypeEM> notificationTypeList = new ArrayList<NotificationTypeEM>();
			for (String type : tripInfoCacheDTO.getNotificationStatus().split(",")) {
				if (NotificationTypeEM.getNotificationTypeEM(type) != null) {
					notificationTypeList.add(NotificationTypeEM.getNotificationTypeEM(type));
				}
			}
			tripInfoDTO.setNotificationStatus(notificationTypeList);
		}

		BusVehicleDTO busVehicleDTO = new BusVehicleDTO();
		busVehicleDTO.setId(tripInfoCacheDTO.getBusVehicleId());
		tripInfoDTO.setBusVehicle(busVehicleDTO);

		return tripInfoDTO;
	}

	public void putTripDataCountEhCache(AuthDTO authDTO, String tripDate, Map<String, String> tripDataMap) {
		String key = "TRIP_DATA" + "_" + authDTO.getNamespaceCode() + "_" + tripDate;
		Element elementKey = new Element(key, tripDataMap);
		EhcacheManager.getTripEhCache().put(elementKey);
	}

	public Map<String, String> getTripDataCountEhCache(AuthDTO authDTO, String tripDate) {
		Map<String, String> tripDataMap = null;
		String key = "TRIP_DATA" + "_" + authDTO.getNamespaceCode() + "_" + tripDate;
		Element element = EhcacheManager.getTripEhCache().get(key);
		if (element != null) {
			tripDataMap = (Map<String, String>) element.getObjectValue();
		}
		return tripDataMap;
	}
}
