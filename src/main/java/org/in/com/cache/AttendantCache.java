package org.in.com.cache;

import org.in.com.dao.BusVehicleDriverDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleAttendantDTO;
import org.in.com.utils.StringUtil;

import net.sf.ehcache.Element;

public class AttendantCache extends NotificationSubscriptionCache {

	private static String CACHEKEY = "VEHAT";

	protected BusVehicleAttendantDTO getVehicleAttendantDTO(AuthDTO authDTO, BusVehicleAttendantDTO attendantDTO) {
		Element element = EhcacheManager.getAttendantCache().get(attendantDTO.getCode());
		if (element != null) {
			attendantDTO = (BusVehicleAttendantDTO) element.getObjectValue();
		}
		else {
			BusVehicleDriverDAO driverDAO = new BusVehicleDriverDAO();
			attendantDTO = driverDAO.getAttendant(authDTO, attendantDTO);
			if (attendantDTO != null && attendantDTO.getId() != 0 && StringUtil.isNotNull(attendantDTO.getCode())) {
				element = new Element(attendantDTO.getCode(), attendantDTO);
				EhcacheManager.getAttendantCache().put(element);
			}
		}
		return attendantDTO;
	}

	protected BusVehicleAttendantDTO getVehicleAttendantDTOById(AuthDTO authDTO, BusVehicleAttendantDTO vehicleAttendantDTO) {
		String attendantCode = null;
		String key = CACHEKEY + authDTO.getNamespace().getId() + "_" + vehicleAttendantDTO.getId();
		Element elementKey = EhcacheManager.getAttendantCache().get(key);
		if (elementKey != null) {
			attendantCode = (String) elementKey.getObjectValue();
			vehicleAttendantDTO.setCode(attendantCode);
		}
		vehicleAttendantDTO = getVehicleAttendantDTO(authDTO, vehicleAttendantDTO);
		if (elementKey == null && StringUtil.isNotNull(vehicleAttendantDTO.getCode()) && vehicleAttendantDTO.getId() != 0) {
			key = CACHEKEY + authDTO.getNamespace().getId() + "_" + vehicleAttendantDTO.getId();
			elementKey = new Element(key, vehicleAttendantDTO.getCode());
			EhcacheManager.getAttendantCache().put(elementKey);
		}
		return vehicleAttendantDTO;
	}

	protected void removeVehicleAttendantDTO(AuthDTO authDTO, BusVehicleAttendantDTO vehicleDriver) {
		EhcacheManager.getAttendantCache().remove(vehicleDriver.getCode());
	}
}
