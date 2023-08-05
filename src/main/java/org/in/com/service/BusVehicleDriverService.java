package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleAttendantDTO;
import org.in.com.dto.BusVehicleDriverDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.AttendantCategoryEM;

public interface BusVehicleDriverService extends BaseService<BusVehicleDriverDTO> {
	public BusVehicleDriverDTO getBusVehicleDriver(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriver);

	public void updateVehicleAttendant(AuthDTO authDTO, BusVehicleAttendantDTO attendantDTO);

	public List<BusVehicleAttendantDTO> getAllAttendant(AuthDTO authDTO, AttendantCategoryEM category);

	public BusVehicleAttendantDTO getAttendant(AuthDTO authDTO, BusVehicleAttendantDTO attendantDTO);

	public UserDTO updateVehicleDriverUser(AuthDTO authDTO, UserDTO userDTO);

	public void updateDriverUserMap(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriver, UserDTO userDTO);

	public void updateUser(AuthDTO authDTO, UserDTO userDTO, BusVehicleDriverDTO driverDTO);

	public void updateLastAssignedDate(AuthDTO authDTO, BusVehicleDriverDTO busVehicleDriver);

	public void updateAssignedTripsCount(AuthDTO authDTO, int days);
}