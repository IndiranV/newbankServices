package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.SectorCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.dao.SectorDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.utils.StringUtil;

import net.sf.ehcache.Element;

public class SectorCache {

	public SectorDTO getUserSector(AuthDTO authDTO, UserDTO userDTO) {
		Element element = EhcacheManager.getSectorCache().get(userDTO.getCode());
		SectorDTO sectorDTO = new SectorDTO();
		if (element != null) {
			SectorCacheDTO cacheDTO = (SectorCacheDTO) element.getObjectValue();
			copySectorFromCache(cacheDTO, sectorDTO);
		}
		else {
			SectorDAO sectorDAO = new SectorDAO();
			List<SectorDTO> sectorList = sectorDAO.getSectorUser(authDTO, userDTO);
			if (!sectorList.isEmpty()) {
				SectorCacheDTO cacheDTO = copySectorToCache(sectorList);
				element = new Element(userDTO.getCode(), cacheDTO);
				EhcacheManager.getSectorCache().put(element);
				copySectorFromCache(cacheDTO, sectorDTO);
			}
		}
		return sectorDTO;
	}

	private SectorCacheDTO copySectorToCache(List<SectorDTO> sectorList) {
		SectorCacheDTO cacheDTO = new SectorCacheDTO();
		List<Integer> scheduleIds = new ArrayList<>();
		List<Integer> vehicleIds = new ArrayList<>();
		List<Integer> stationIds = new ArrayList<>();
		List<Integer> organizationIds = new ArrayList<>();

		for (SectorDTO sectorDTO : sectorList) {
			cacheDTO.setId(sectorDTO.getId());
			cacheDTO.setCode(sectorDTO.getCode());
			cacheDTO.setName(sectorDTO.getName());
			cacheDTO.setActiveFlag(sectorDTO.getActiveFlag());

			for (ScheduleDTO scheduleDTO : sectorDTO.getSchedule()) {
				if (scheduleDTO.getId() == 0) {
					continue;
				}
				scheduleIds.add(scheduleDTO.getId());
			}
			for (BusVehicleDTO vehicleDTO : sectorDTO.getVehicle()) {
				if (vehicleDTO.getId() == 0) {
					continue;
				}
				vehicleIds.add(vehicleDTO.getId());
			}
			for (StationDTO stationDTO : sectorDTO.getStation()) {
				if (stationDTO.getId() == 0) {
					continue;
				}
				stationIds.add(stationDTO.getId());
			}
			for (OrganizationDTO organizationDTO : sectorDTO.getOrganization()) {
				if (organizationDTO.getId() == 0) {
					continue;
				}
				organizationIds.add(organizationDTO.getId());
			}
		}
		cacheDTO.setOrganizations(organizationIds);
		cacheDTO.setSchedules(scheduleIds);
		cacheDTO.setStations(stationIds);
		cacheDTO.setVehicles(vehicleIds);
		return cacheDTO;
	}

	private void copySectorFromCache(SectorCacheDTO cacheDTO, SectorDTO sectorDTO) {
		sectorDTO.setId(cacheDTO.getId());
		sectorDTO.setActiveFlag(cacheDTO.getActiveFlag());
		sectorDTO.setCode(cacheDTO.getCode());
		sectorDTO.setName(cacheDTO.getName());

		List<ScheduleDTO> scheduleList = new ArrayList<>();
		if (StringUtil.isNotNull(cacheDTO.getSchedules())) {
			for (Integer scheduleId : cacheDTO.getSchedules()) {
				if (StringUtil.isNull(scheduleId) || Numeric.ZERO.equals(scheduleId)) {
					continue;
				}

				ScheduleDTO scheduleDTO = new ScheduleDTO();
				scheduleDTO.setId(scheduleId);
				scheduleList.add(scheduleDTO);
			}
		}

		List<BusVehicleDTO> vehicleList = new ArrayList<>();
		if (StringUtil.isNotNull(cacheDTO.getVehicles())) {
			for (Integer vehicleId : cacheDTO.getVehicles()) {
				if (StringUtil.isNull(vehicleId) || Numeric.ZERO.equals(vehicleId)) {
					continue;
				}

				BusVehicleDTO vehicleDTO = new BusVehicleDTO();
				vehicleDTO.setId(vehicleId);
				vehicleList.add(vehicleDTO);
			}
		}

		List<StationDTO> stationList = new ArrayList<>();
		if (StringUtil.isNotNull(cacheDTO.getStations())) {
			for (Integer stationId : cacheDTO.getStations()) {
				if (StringUtil.isNull(stationId) || Numeric.ZERO.equals(stationId)) {
					continue;
				}

				StationDTO stationDTO = new StationDTO();
				stationDTO.setId(stationId);
				stationList.add(stationDTO);
			}
		}

		List<OrganizationDTO> organizationList = new ArrayList<>();
		if (StringUtil.isNotNull(cacheDTO.getOrganizations())) {
			for (Integer organizationId : cacheDTO.getOrganizations()) {
				if (StringUtil.isNull(organizationId) || Numeric.ZERO.equals(organizationId)) {
					continue;
				}

				OrganizationDTO organizationDTO = new OrganizationDTO();
				organizationDTO.setId(organizationId);
				organizationList.add(organizationDTO);
			}
		}

		sectorDTO.setOrganization(organizationList);
		sectorDTO.setSchedule(scheduleList);
		sectorDTO.setStation(stationList);
		sectorDTO.setVehicle(vehicleList);
	}

	public void removeSector(AuthDTO authDTO) {
		EhcacheManager.getSectorCache().removeAll();
	}
}
