package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.TripVanInfoCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.TripVanInfoDAO;
import org.in.com.dto.AuditDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.BusVehicleVanPickupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.TripVanExceptionDTO;
import org.in.com.dto.TripVanInfoDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.NotificationTypeEM;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

import net.sf.ehcache.Element;

public class TripVanInfoCache {

	private static String CACHEKEY = "TRP_VAN_INFO_";
	
	protected TripVanInfoDTO getTripVanInfoCache(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO) {
		TripVanInfoDTO tripVanInfo = null;
		if (tripVanInfoDTO.getVanPickup().getId() != 0 && tripVanInfoDTO.getTripDate() != null) {
			String key = CACHEKEY + authDTO.getNamespaceCode() + "_" + tripVanInfoDTO.getVanPickup().getId() + "_" + DateUtil.getCompressDate(tripVanInfoDTO.getTripDate());
			Element element = EhcacheManager.getTripEhCache().get(key);
			if (element != null) {
				TripVanInfoCacheDTO tripInfoCacheDTO = (TripVanInfoCacheDTO) element.getObjectValue();
				tripVanInfo = bindTripInfoFromCacheObject(tripInfoCacheDTO);
			}
			else {
				TripVanInfoDAO tripDAO = new TripVanInfoDAO();
				tripVanInfo = tripDAO.getTripVanInfo(authDTO, tripVanInfoDTO);
				if (StringUtil.isNotNull(tripVanInfo.getCode()) || tripVanInfo.getTripVanException() != null) {
					element = new Element(key, bindTripInfoToCacheObject(tripVanInfo));
					EhcacheManager.getTripEhCache().put(element);
				}
			}
		}
		return tripVanInfo;
	}
	
	protected void removeTripVanInfoCache(AuthDTO authDTO, TripVanInfoDTO tripVanInfoDTO) {
		String key = CACHEKEY + authDTO.getNamespaceCode() + "_" + tripVanInfoDTO.getVanPickup().getId() + "_" + DateUtil.getCompressDate(tripVanInfoDTO.getTripDate());
		EhcacheManager.getTripEhCache().remove(key);
	}
	
	protected void removeTripVanExceptionCache(AuthDTO authDTO, TripVanExceptionDTO tripVanExceptionDTO) {
		String key = CACHEKEY + authDTO.getNamespaceCode() + "_" + tripVanExceptionDTO.getVanPickup().getId() + "_" + DateUtil.getCompressDate(tripVanExceptionDTO.getTripDate());
		EhcacheManager.getTripEhCache().remove(key);
	}
	
	protected TripVanInfoCacheDTO bindTripInfoToCacheObject(TripVanInfoDTO tripVanInfoDTO) {
		TripVanInfoCacheDTO tripInfoCacheDTO = new TripVanInfoCacheDTO();
		if (StringUtil.isNotNull(tripVanInfoDTO.getCode())) {
			tripInfoCacheDTO.setCode(tripVanInfoDTO.getCode());
			tripInfoCacheDTO.setMobileNumber(tripVanInfoDTO.getMobileNumber());
			tripInfoCacheDTO.setNotificationTypeCode(tripVanInfoDTO.getNotificationType() != null ? tripVanInfoDTO.getNotificationType().getCode() : Text.NA);
			tripInfoCacheDTO.setTripDate(DateUtil.convertDate(tripVanInfoDTO.getTripDate()));
			tripInfoCacheDTO.setVehicleId(tripVanInfoDTO.getVehicle().getId());
			tripInfoCacheDTO.setDriverId(tripVanInfoDTO.getDriver().getId());
			tripInfoCacheDTO.setVanPickupId(tripVanInfoDTO.getVanPickup().getId());
		}
		
		if (tripVanInfoDTO.getTripVanException() != null && StringUtil.isNotNull(tripVanInfoDTO.getTripVanException().getCode())) {
			TripVanInfoCacheDTO tripInfoExceptionCacheDTO = new TripVanInfoCacheDTO();
			tripInfoExceptionCacheDTO.setCode(tripVanInfoDTO.getTripVanException().getCode());
			tripInfoExceptionCacheDTO.setTripDate(DateUtil.convertDate(tripVanInfoDTO.getTripVanException().getTripDate()));
			
			List<String> exceptionScheduleList = new ArrayList<>();
			for (ScheduleDTO exceptionScheduleDTO : tripVanInfoDTO.getTripVanException().getSchedules()) {
				exceptionScheduleList.add(exceptionScheduleDTO.getCode());
			}
			tripInfoExceptionCacheDTO.setVanPickupId(tripVanInfoDTO.getTripVanException().getVanPickup().getId());
			tripInfoExceptionCacheDTO.setScheduleList(exceptionScheduleList);
			tripInfoExceptionCacheDTO.setUpdatedBy(tripVanInfoDTO.getTripVanException().getAudit() != null ? tripVanInfoDTO.getTripVanException().getAudit().getUser().getId() : 0);
			tripInfoExceptionCacheDTO.setUpdatedAt(tripVanInfoDTO.getTripVanException().getAudit() != null ? tripVanInfoDTO.getTripVanException().getAudit().getUpdatedAt() : "");
			tripInfoCacheDTO.setTripVanExceptionCache(tripInfoExceptionCacheDTO);
		}
		return tripInfoCacheDTO;
	}
	
	protected TripVanInfoDTO bindTripInfoFromCacheObject(TripVanInfoCacheDTO tripInfoCacheDTO) {
		TripVanInfoDTO tripVanInfoDTO = new TripVanInfoDTO();
		if (StringUtil.isNotNull(tripInfoCacheDTO.getCode())) {
			tripVanInfoDTO.setCode(tripInfoCacheDTO.getCode());
			tripVanInfoDTO.setMobileNumber(tripInfoCacheDTO.getMobileNumber());
			tripVanInfoDTO.setNotificationType(NotificationTypeEM.getNotificationTypeEM(tripInfoCacheDTO.getNotificationTypeCode()));
			tripVanInfoDTO.setTripDate(DateUtil.getDateTime(tripInfoCacheDTO.getTripDate()));
			
			BusVehicleDTO vehicleDTO = new BusVehicleDTO();
			vehicleDTO.setId(tripInfoCacheDTO.getVehicleId());
			tripVanInfoDTO.setVehicle(vehicleDTO);

			BusVehicleDriverDTO driverDTO = new BusVehicleDriverDTO();
			driverDTO.setId(tripInfoCacheDTO.getDriverId());
			tripVanInfoDTO.setDriver(driverDTO);

			BusVehicleVanPickupDTO vanPickupDTO = new BusVehicleVanPickupDTO();
			vanPickupDTO.setId(tripInfoCacheDTO.getVanPickupId());
			tripVanInfoDTO.setVanPickup(vanPickupDTO);
			
			UserDTO updatedBy = new UserDTO();
			updatedBy.setId(tripInfoCacheDTO.getUpdatedBy());

			AuditDTO auditDTO = new AuditDTO();
			auditDTO.setUser(updatedBy);
			auditDTO.setUpdatedAt(tripInfoCacheDTO.getUpdatedAt());
			
			tripVanInfoDTO.setAudit(auditDTO);
		}
		
		if (tripInfoCacheDTO.getTripVanExceptionCache() != null && StringUtil.isNotNull(tripInfoCacheDTO.getTripVanExceptionCache().getCode())) {
			TripVanExceptionDTO tripInfoExceptionDTO = new TripVanExceptionDTO();
			tripInfoExceptionDTO.setCode(tripInfoCacheDTO.getTripVanExceptionCache().getCode());
			tripInfoExceptionDTO.setTripDate(DateUtil.getDateTime(tripInfoCacheDTO.getTripVanExceptionCache().getTripDate()));
			
			List<ScheduleDTO> exceptionScheduleList = new ArrayList<ScheduleDTO>();
			for (String exceptionScheduleCode : tripInfoCacheDTO.getTripVanExceptionCache().getScheduleList()) {
				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setCode(exceptionScheduleCode);
				exceptionScheduleList.add(scheduleDTO);
			}
			
			BusVehicleVanPickupDTO vanPickupExceptionDTO = new BusVehicleVanPickupDTO();
			vanPickupExceptionDTO.setId(tripInfoCacheDTO.getTripVanExceptionCache().getVanPickupId());
			tripInfoExceptionDTO.setSchedules(exceptionScheduleList);
			tripInfoExceptionDTO.setVanPickup(vanPickupExceptionDTO);
			
			UserDTO updatedBy = new UserDTO();
			updatedBy.setId(tripInfoCacheDTO.getTripVanExceptionCache().getUpdatedBy());
			
			AuditDTO auditDTO = new AuditDTO();
			auditDTO.setUser(updatedBy);
			auditDTO.setUpdatedAt(tripInfoCacheDTO.getTripVanExceptionCache().getUpdatedAt());
			tripInfoExceptionDTO.setAudit(auditDTO);
			tripVanInfoDTO.setTripVanException(tripInfoExceptionDTO);
		}
		return tripVanInfoDTO;
	}
}
