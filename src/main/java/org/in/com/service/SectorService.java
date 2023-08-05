package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;

public interface SectorService extends BaseService<SectorDTO> {

	public SectorDTO getSector(AuthDTO authDTO, SectorDTO sectorDTO);

	public SectorDTO getSectorV2(AuthDTO authDTO, SectorDTO sector);

	public void updateSectorUser(AuthDTO authDTO, SectorDTO sectorDTO, UserDTO userDTO);

	public List<SectorDTO> getSectorUser(AuthDTO authDTO, UserDTO userDTO);

	public void updateSectorSchedule(AuthDTO authDTO, SectorDTO sectorDTO, ScheduleDTO scheduleDTO);

	public void updateSectorVehicle(AuthDTO authDTO, SectorDTO sectorDTO, BusVehicleDTO vehicleDTO);

	public void updateSectorStation(AuthDTO authDTO, SectorDTO sectorDTO, StationDTO stationDTO);

	public void updateSectorOrganization(AuthDTO authDTO, SectorDTO sectorDT, OrganizationDTO organizationDTO);

	public SectorDTO getActiveUserSectorSchedule(AuthDTO authDTO);

	public SectorDTO getActiveUserSectorOrganization(AuthDTO authDTO);

	public SectorDTO getActiveUserSectorVehicle(AuthDTO authDTO);

	public List<UserDTO> getSectorUsers(AuthDTO authDTO, SectorDTO sectorDTO);
	
	public SectorDTO getActiveSectorScheduleStation(AuthDTO authDTO);
	
	public List<SectorDTO> getUserSectors(AuthDTO authDTO);
	
	public SectorDTO getUserActiveSector(AuthDTO authDTO, UserDTO userDTO);
}
