package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.in.com.cache.SectorCache;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.SectorDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.MenuEventDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.dto.enumeration.MenuEventEM;
import org.in.com.service.BusVehicleService;
import org.in.com.service.MenuService;
import org.in.com.service.OrganizationService;
import org.in.com.service.ScheduleService;
import org.in.com.service.ScheduleVisibilityService;
import org.in.com.service.SectorService;
import org.in.com.service.StationService;
import org.in.com.service.UserService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SectorServiceImpl extends BaseImpl implements SectorService {

	@Autowired
	ScheduleService scheduleService;
	@Autowired
	BusVehicleService vehicleService;
	@Autowired
	StationService stationService;
	@Autowired
	OrganizationService organizationService;
	@Autowired
	UserService userService;
	@Autowired
	ScheduleVisibilityService visibilityService;
	@Autowired
	MenuService menuService;

	public List<SectorDTO> get(AuthDTO authDTO, SectorDTO sectorDTO) {
		return null;
	}

	public List<SectorDTO> getAll(AuthDTO authDTO) {
		SectorDAO sectorDAO = new SectorDAO();
		List<SectorDTO> sectorList = sectorDAO.getAllSector(authDTO);
		for (SectorDTO sectorDTO : sectorList) {
			for (ScheduleDTO scheduleDTO : sectorDTO.getSchedule()) {
				scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);
			}
			for (BusVehicleDTO vehicleDTO : sectorDTO.getVehicle()) {
				vehicleDTO = vehicleService.getBusVehicles(authDTO, vehicleDTO);
			}
			for (StationDTO stationDTO : sectorDTO.getStation()) {
				stationDTO = stationService.getStation(stationDTO);
			}
			for (OrganizationDTO organizationDTO : sectorDTO.getOrganization()) {
				organizationDTO = organizationService.getOrganization(authDTO, organizationDTO);
			}
		}
		return sectorList;
	}

	public List<SectorDTO> getAllV2(AuthDTO authDTO) {
		SectorDAO sectorDAO = new SectorDAO();
		List<SectorDTO> sectorList = sectorDAO.getAllSector(authDTO);
		return sectorList;
	}

	public SectorDTO Update(AuthDTO authDTO, SectorDTO sectorDTO) {
		SectorDAO sectorDAO = new SectorDAO();
		SectorDTO sector = sectorDAO.updateSector(authDTO, sectorDTO);
		// clear cache
		removeSector(authDTO);
		return sector;
	}

	public void updateSectorSchedule(AuthDTO authDTO, SectorDTO sectorDTO, ScheduleDTO scheduleDTO) {
		SectorDAO sectorDAO = new SectorDAO();
		SectorDTO repoSectorDTO = sectorDAO.getSector(authDTO, sectorDTO);
		scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);

		if (sectorDTO.getActiveFlag() == Numeric.ONE_INT) {
			boolean isExistSchedule = Text.FALSE;
			for (ScheduleDTO existScheduleDTO : repoSectorDTO.getSchedule()) {
				if (existScheduleDTO.getId() == scheduleDTO.getId()) {
					isExistSchedule = Text.TRUE;
					break;
				}
			}
			if (!isExistSchedule) {
				repoSectorDTO.getSchedule().add(scheduleDTO);
				sectorDTO.setSchedule(repoSectorDTO.getSchedule());
				sectorDAO.updateSectorSchedule(authDTO, sectorDTO);
				// clear cache
				removeSector(authDTO);
			}
		}
		else if (sectorDTO.getActiveFlag() != Numeric.ONE_INT) {
			for (Iterator<ScheduleDTO> iterator = repoSectorDTO.getSchedule().iterator(); iterator.hasNext();) {
				ScheduleDTO existScheduleDTO = iterator.next();
				if (existScheduleDTO.getId() == scheduleDTO.getId()) {
					iterator.remove();
					break;
				}
			}

			sectorDTO.setSchedule(repoSectorDTO.getSchedule());
			sectorDAO.updateSectorSchedule(authDTO, sectorDTO);
			// clear cache
			removeSector(authDTO);
		}
	}

	public void updateSectorVehicle(AuthDTO authDTO, SectorDTO sectorDTO, BusVehicleDTO vehicleDTO) {
		SectorDAO sectorDAO = new SectorDAO();
		SectorDTO repoSectorDTO = sectorDAO.getSector(authDTO, sectorDTO);
		vehicleDTO = vehicleService.getBusVehicles(authDTO, vehicleDTO);

		if (sectorDTO.getActiveFlag() == Numeric.ONE_INT) {
			boolean isExistVehicle = Text.FALSE;
			for (BusVehicleDTO existVehicleDTO : repoSectorDTO.getVehicle()) {
				if (existVehicleDTO.getId() == vehicleDTO.getId()) {
					isExistVehicle = Text.TRUE;
					break;
				}
			}
			if (!isExistVehicle) {
				repoSectorDTO.getVehicle().add(vehicleDTO);
				sectorDTO.setVehicle(repoSectorDTO.getVehicle());
				sectorDAO.updateSectorVehicle(authDTO, sectorDTO);
				// clear cache
				removeSector(authDTO);
			}
		}
		else if (sectorDTO.getActiveFlag() != Numeric.ONE_INT) {
			for (Iterator<BusVehicleDTO> iterator = repoSectorDTO.getVehicle().iterator(); iterator.hasNext();) {
				BusVehicleDTO existVehicleDTO = iterator.next();
				if (existVehicleDTO.getId() == vehicleDTO.getId()) {
					iterator.remove();
					break;
				}
			}
			sectorDTO.setVehicle(repoSectorDTO.getVehicle());
			sectorDAO.updateSectorVehicle(authDTO, sectorDTO);
			// clear cache
			removeSector(authDTO);
		}
	}

	public void updateSectorStation(AuthDTO authDTO, SectorDTO sectorDTO, StationDTO stationDTO) {
		SectorDAO sectorDAO = new SectorDAO();
		SectorDTO repoSectorDTO = sectorDAO.getSector(authDTO, sectorDTO);
		stationDTO = stationService.getStation(stationDTO);

		if (sectorDTO.getActiveFlag() == Numeric.ONE_INT) {
			boolean isExistStation = Text.FALSE;
			for (StationDTO existStationDTO : repoSectorDTO.getStation()) {
				if (existStationDTO.getId() == stationDTO.getId()) {
					isExistStation = Text.TRUE;
					break;
				}
			}
			if (!isExistStation) {
				repoSectorDTO.getStation().add(stationDTO);
				sectorDTO.setStation(repoSectorDTO.getStation());
				sectorDAO.updateSectorStation(authDTO, sectorDTO);
				// clear cache
				removeSector(authDTO);
			}
		}
		else if (sectorDTO.getActiveFlag() != Numeric.ONE_INT) {
			for (Iterator<StationDTO> iterator = repoSectorDTO.getStation().iterator(); iterator.hasNext();) {
				StationDTO existStationDTO = iterator.next();
				if (existStationDTO.getId() == stationDTO.getId()) {
					iterator.remove();
					break;
				}
			}
			sectorDTO.setStation(repoSectorDTO.getStation());
			sectorDAO.updateSectorStation(authDTO, sectorDTO);
			// clear cache
			removeSector(authDTO);
		}
	}

	public void updateSectorOrganization(AuthDTO authDTO, SectorDTO sectorDTO, OrganizationDTO organizationDTO) {
		SectorDAO sectorDAO = new SectorDAO();
		SectorDTO repoSectorDTO = sectorDAO.getSector(authDTO, sectorDTO);
		organizationDTO = organizationService.getOrganization(authDTO, organizationDTO);

		if (sectorDTO.getActiveFlag() == Numeric.ONE_INT) {
			boolean isExistOrganization = Text.FALSE;
			for (OrganizationDTO existOrganizationDTO : repoSectorDTO.getOrganization()) {
				if (existOrganizationDTO.getId() == organizationDTO.getId()) {
					isExistOrganization = Text.TRUE;
					break;
				}
			}
			if (!isExistOrganization) {
				repoSectorDTO.getOrganization().add(organizationDTO);
				sectorDTO.setOrganization(repoSectorDTO.getOrganization());
				sectorDAO.updateSectorOrganization(authDTO, sectorDTO);
				// clear cache
				removeSector(authDTO);
			}
		}
		else if (sectorDTO.getActiveFlag() != Numeric.ONE_INT) {
			for (Iterator<OrganizationDTO> iterator = repoSectorDTO.getOrganization().iterator(); iterator.hasNext();) {
				OrganizationDTO existOrganizationDTO = iterator.next();
				if (existOrganizationDTO.getId() == organizationDTO.getId()) {
					iterator.remove();
					break;
				}
			}
			sectorDTO.setOrganization(repoSectorDTO.getOrganization());
			sectorDAO.updateSectorOrganization(authDTO, sectorDTO);
			// clear cache
			removeSector(authDTO);
		}
	}

	public SectorDTO getSector(AuthDTO authDTO, SectorDTO sector) {
		SectorDAO sectorDAO = new SectorDAO();
		SectorDTO sectorDTO = sectorDAO.getSector(authDTO, sector);
		if (sectorDTO.getSchedule() != null && sectorDTO.getVehicle() != null && sectorDTO.getStation() != null && sectorDTO.getOrganization() != null) {
			for (ScheduleDTO scheduleDTO : sectorDTO.getSchedule()) {
				scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);
			}
			for (BusVehicleDTO vehicleDTO : sectorDTO.getVehicle()) {
				vehicleDTO = vehicleService.getBusVehicles(authDTO, vehicleDTO);
			}
			for (StationDTO stationDTO : sectorDTO.getStation()) {
				stationDTO = stationService.getStation(stationDTO);
			}
			for (OrganizationDTO organizationDTO : sectorDTO.getOrganization()) {
				organizationDTO = organizationService.getOrganization(authDTO, organizationDTO);
			}
		}
		return sectorDTO;

	}

	public SectorDTO getSectorV2(AuthDTO authDTO, SectorDTO sector) {
		SectorDAO sectorDAO = new SectorDAO();
		SectorDTO sectorDTO = sectorDAO.getSector(authDTO, sector);
		return sectorDTO;
	}

	public void updateSectorUser(AuthDTO authDTO, SectorDTO sectorDTO, UserDTO userDTO) {
		SectorDAO sectorDAO = new SectorDAO();
		sectorDAO.updateSectorUser(authDTO, sectorDTO, userDTO);
		// clear cache
		removeSector(authDTO);
	}

	public List<SectorDTO> getSectorUser(AuthDTO authDTO, UserDTO userDTO) {
		SectorDAO sectorDAO = new SectorDAO();
		userDTO = userService.getUser(authDTO, userDTO);
		List<SectorDTO> sectorList = sectorDAO.getSectorUser(authDTO, userDTO);
		for (SectorDTO sectorDTO : sectorList) {
			for (ScheduleDTO scheduleDTO : sectorDTO.getSchedule()) {
				scheduleDTO = scheduleService.getSchedule(authDTO, scheduleDTO);
			}
			for (BusVehicleDTO vehicleDTO : sectorDTO.getVehicle()) {
				vehicleDTO = vehicleService.getBusVehicles(authDTO, vehicleDTO);
			}
			for (StationDTO stationDTO : sectorDTO.getStation()) {
				stationDTO = stationService.getStation(stationDTO);
			}
			for (OrganizationDTO organizationDTO : sectorDTO.getOrganization()) {
				organizationDTO = organizationService.getOrganization(authDTO, organizationDTO);
			}
		}
		return sectorList;
	}

	public List<SectorDTO> getUserSectors(AuthDTO authDTO) {
		SectorDAO sectorDAO = new SectorDAO();
		List<SectorDTO> sectorList = new ArrayList<SectorDTO>();
		if (Numeric.ONE_INT == StringUtil.getIntegerValue(authDTO.getAdditionalAttribute().get(Text.SECTOR))) {
			List<SectorDTO> sector = sectorDAO.getUserSectors(authDTO, authDTO.getUser());
			sectorList.addAll(sector);
		}
		else {
			List<SectorDTO> sector = getAllV2(authDTO);
			sectorList.addAll(sector);
		}
		return sectorList;
	}

	public SectorDTO getActiveUserSectorSchedule(AuthDTO authDTO) {
		SectorDTO sector = new SectorDTO();
		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		sector.setActiveFlag(Numeric.ZERO_INT);

		List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
		eventList.add(MenuEventEM.SECTOR);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);

		if (menuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
			sector.setActiveFlag(Numeric.ONE_INT);
			SectorDAO sectorDAO = new SectorDAO();
			List<SectorDTO> sectorList = sectorDAO.getSectorUser(authDTO, authDTO.getUser());
			for (SectorDTO sectorDTO : sectorList) {
				scheduleList.addAll(sectorDTO.getSchedule());
			}
			for (ScheduleDTO schedule : scheduleList) {
				scheduleService.getSchedule(authDTO, schedule);
			}
		}
		// schedule visibility and Permission check
		List<MenuEventEM> Eventlist = new ArrayList<MenuEventEM>();
		Eventlist.add(MenuEventEM.REPORT_TRIP_CHART_RIGHTS_ALL);
		MenuEventDTO MinsMenuEventDTO = getPrivilegeV2(authDTO, Eventlist);

		if (MinsMenuEventDTO != null && MinsMenuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
			sector.setActiveFlag(Numeric.ONE_INT);
			List<ScheduleDTO> visibilitySchedule = visibilityService.getUserActiveSchedule(authDTO);
			scheduleList.addAll(visibilitySchedule);
		}
		sector.setSchedule(scheduleList);
		return sector;
	}

	public SectorDTO getActiveUserSectorOrganization(AuthDTO authDTO) {
		SectorDTO sector = new SectorDTO();
		sector.setActiveFlag(Numeric.ZERO_INT);

		List<OrganizationDTO> organizationList = new ArrayList<OrganizationDTO>();

		List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
		eventList.add(MenuEventEM.SECTOR);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);

		if (menuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
			sector.setActiveFlag(Numeric.ONE_INT);
			SectorDAO sectorDAO = new SectorDAO();
			List<SectorDTO> sectorList = sectorDAO.getSectorUser(authDTO, authDTO.getUser());
			for (SectorDTO sectorDTO : sectorList) {
				organizationList.addAll(sectorDTO.getOrganization());
			}
		}
		sector.setOrganization(organizationList);
		return sector;
	}

	public SectorDTO getActiveUserSectorVehicle(AuthDTO authDTO) {
		SectorDTO sector = new SectorDTO();
		List<BusVehicleDTO> vehicleList = new ArrayList<BusVehicleDTO>();
		sector.setActiveFlag(Numeric.ZERO_INT);

		List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
		eventList.add(MenuEventEM.SECTOR);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);

		if (menuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
			sector.setActiveFlag(Numeric.ONE_INT);
			SectorDAO sectorDAO = new SectorDAO();
			List<SectorDTO> sectorList = sectorDAO.getSectorUser(authDTO, authDTO.getUser());
			for (SectorDTO sectorDTO : sectorList) {
				vehicleList.addAll(sectorDTO.getVehicle());
			}
		}
		sector.setVehicle(vehicleList);
		return sector;
	}

	public SectorDTO getActiveUserSectorStation(AuthDTO authDTO) {
		SectorDTO sector = new SectorDTO();
		List<StationDTO> stationList = new ArrayList<StationDTO>();
		sector.setActiveFlag(Numeric.ZERO_INT);

		List<MenuEventEM> eventList = new ArrayList<MenuEventEM>();
		eventList.add(MenuEventEM.SECTOR);
		MenuEventDTO menuEventDTO = getPrivilegeV2(authDTO, eventList);

		if (menuEventDTO.getEnabledFlag() == Numeric.ONE_INT) {
			sector.setActiveFlag(Numeric.ONE_INT);
			SectorDAO sectorDAO = new SectorDAO();
			List<SectorDTO> sectorList = sectorDAO.getSectorUser(authDTO, authDTO.getUser());
			for (SectorDTO sectorDTO : sectorList) {
				stationList.addAll(sectorDTO.getStation());
			}
			for (StationDTO stationDTO : stationList) {
				stationService.getStation(stationDTO);
			}
		}
		sector.setStation(stationList);
		return sector;
	}

	public SectorDTO getActiveSectorScheduleStation(AuthDTO authDTO) {
		SectorDTO sector = new SectorDTO();
		SectorDTO scheduleSector = getActiveUserSectorSchedule(authDTO);
		SectorDTO stationSector = getActiveUserSectorStation(authDTO);

		sector.setSchedule(scheduleSector.getSchedule());
		sector.setStation(stationSector.getStation());
		sector.setActiveFlag(scheduleSector.getActiveFlag());
		return sector;
	}

	public SectorDTO getUserActiveSector(AuthDTO authDTO, UserDTO userDTO) {
		List<MenuEventDTO> menuEvents = menuService.getUserPreDefinedPrivileges(authDTO, userDTO);
		boolean isPermissionEnabled = BitsUtil.isPermissionEnabled(menuEvents, MenuEventEM.SECTOR);

		SectorDTO sector = new SectorDTO();
		List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
		List<StationDTO> stationList = new ArrayList<StationDTO>();
		List<BusVehicleDTO> vehicleList = new ArrayList<BusVehicleDTO>();
		List<OrganizationDTO> organizationList = new ArrayList<OrganizationDTO>();

		if (isPermissionEnabled) {
			sector.setActiveFlag(Numeric.ONE_INT);
			SectorCache sectorCache = new SectorCache();
			SectorDTO sectorDTO = sectorCache.getUserSector(authDTO, userDTO);
			if (StringUtil.isNotNull(sectorDTO.getCode())) {
				scheduleList.addAll(sectorDTO.getSchedule());
				stationList.addAll(sectorDTO.getStation());
				vehicleList.addAll(sectorDTO.getVehicle());
				organizationList.addAll(sectorDTO.getOrganization());
			}
		}

		isPermissionEnabled = BitsUtil.isPermissionEnabled(menuEvents, MenuEventEM.REPORT_TRIP_CHART_RIGHTS_ALL);
		if (isPermissionEnabled) {
			sector.setActiveFlag(Numeric.ONE_INT);
			List<ScheduleDTO> visibilitySchedule = visibilityService.getUserActiveSchedule(authDTO);
			scheduleList.addAll(visibilitySchedule);
		}

		for (ScheduleDTO schedule : scheduleList) {
			scheduleService.getSchedule(authDTO, schedule);
		}
		for (StationDTO stationDTO : stationList) {
			stationService.getStation(stationDTO);
		}

		sector.setSchedule(scheduleList);
		sector.setStation(stationList);
		sector.setVehicle(vehicleList);
		sector.setOrganization(organizationList);
		return sector;
	}

	public List<UserDTO> getSectorUsers(AuthDTO authDTO, SectorDTO sectorDTO) {
		SectorDAO sectorDAO = new SectorDAO();
		return sectorDAO.getSectorUsers(authDTO, sectorDTO);
	}
	
	private void removeSector(AuthDTO authDTO) {
		SectorCache sectorCache = new SectorCache();
		sectorCache.removeSector(authDTO);
	}
}
