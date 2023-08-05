package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;

public interface BusVehicleService extends BaseService<BusVehicleDTO> {
	public BusVehicleDTO getBusVehicles(AuthDTO authDTO, BusVehicleDTO vehicleDTO);

	public List<BusVehicleDTO> getActiveBusVehicles(AuthDTO authDTO);

	public List<BusVehicleDTO> getActiveSectorBusVehicles(AuthDTO authDTO);
	
	public void updateLastAssignedDate(AuthDTO authDTO, BusVehicleDTO busVehicle);
}
