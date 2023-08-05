package org.in.com.cache;

import org.in.com.dao.BusVehicleDriverDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.utils.StringUtil;

import net.sf.ehcache.Element;

public class DriverCache extends AttendantCache {
	private static String CACHEKEY = "VEHDR";

	protected BusVehicleDriverDTO getVehicleDriverDTO(AuthDTO authDTO, BusVehicleDriverDTO driverDTO) {
		Element element = EhcacheManager.getDriverCache().get(driverDTO.getCode());
		if (element != null) {
			driverDTO = (BusVehicleDriverDTO) element.getObjectValue();
		}
		else {
			BusVehicleDriverDAO driverDAO = new BusVehicleDriverDAO();
			driverDTO = driverDAO.getDriver(authDTO, driverDTO);
			if (driverDTO != null && driverDTO.getId() != 0 && StringUtil.isNotNull(driverDTO.getCode())) {
				element = new Element(driverDTO.getCode(), driverDTO);
				EhcacheManager.getDriverCache().put(element);
			}
		}
		return driverDTO;
	}

	protected BusVehicleDriverDTO getVehicleDriverDTOById(AuthDTO authDTO, BusVehicleDriverDTO vehicleDriver) {
		String driverCode = null;
		String key = CACHEKEY + authDTO.getNamespace().getId() + "_" + vehicleDriver.getId();
		Element elementKey = EhcacheManager.getDriverCache().get(key);
		if (elementKey != null) {
			driverCode = (String) elementKey.getObjectValue();
			vehicleDriver.setCode(driverCode);
		}
		vehicleDriver = getVehicleDriverDTO(authDTO, vehicleDriver);
		if (elementKey == null && StringUtil.isNotNull(vehicleDriver.getCode()) && vehicleDriver.getId() != 0) {
			key = CACHEKEY + authDTO.getNamespace().getId() + "_" + vehicleDriver.getId();
			elementKey = new Element(key, vehicleDriver.getCode());
			EhcacheManager.getDriverCache().put(elementKey);
		}
		return vehicleDriver;
	}

	protected void removeVehicleDriverDTO(AuthDTO authDTO, BusVehicleDriverDTO vehicleDriver) {
		EhcacheManager.getDriverCache().remove(vehicleDriver.getCode());
	}
}
