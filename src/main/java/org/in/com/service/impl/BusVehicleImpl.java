package org.in.com.service.impl;

import java.util.Iterator;
import java.util.List;

import org.in.com.cache.BusCache;
import org.in.com.cache.CacheCentral;
import org.in.com.constants.Numeric;
import org.in.com.dao.BusVehicleDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusVehicleDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.service.BusVehicleService;
import org.in.com.service.SectorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BusVehicleImpl extends CacheCentral implements BusVehicleService {

	@Autowired
	SectorService sectorService;

	public List<BusVehicleDTO> get(AuthDTO authDTO, BusVehicleDTO dto) {

		return null;
	}

	public List<BusVehicleDTO> getAll(AuthDTO authDTO) {
		BusVehicleDAO dao = new BusVehicleDAO();
		List<BusVehicleDTO> list = dao.getAllBusVehicles(authDTO);
		BusCache busCache = new BusCache();
		for (BusVehicleDTO vehicleDTO : list) {
			vehicleDTO.setBus(busCache.getBusDTObyId(authDTO, vehicleDTO.getBus()));
		}
		return list;
	}

	public List<BusVehicleDTO> getActiveSectorBusVehicles(AuthDTO authDTO) {
		SectorDTO sectorDTO = sectorService.getActiveUserSectorVehicle(authDTO);
		List<BusVehicleDTO> list = getAll(authDTO);

		if (sectorDTO.getActiveFlag() == Numeric.ONE_INT) {
			for (Iterator<BusVehicleDTO> iterator = list.iterator(); iterator.hasNext();) {
				BusVehicleDTO vehicleDTO = iterator.next();
				BusVehicleDTO existVehicle = null;
				for (BusVehicleDTO sectorVehicle : sectorDTO.getVehicle()) {
					if (vehicleDTO.getActiveFlag() == Numeric.ONE_INT && vehicleDTO.getId() == sectorVehicle.getId()) {
						existVehicle = vehicleDTO;
						break;
					}
				}
				if (existVehicle == null) {
					iterator.remove();
					continue;
				}
			}
		}
		return list;
	}

	public BusVehicleDTO Update(AuthDTO authDTO, BusVehicleDTO dto) {
		BusVehicleDAO dao = new BusVehicleDAO();
		dao.updateBusVehicle(authDTO, dto);
		return dto;
	}

	public BusVehicleDTO getBusVehicles(AuthDTO authDTO, BusVehicleDTO vehicleDTO) {
		BusVehicleDAO busDAO = new BusVehicleDAO();
		return busDAO.getBusVehicles(authDTO, vehicleDTO);
	}

	@Override
	public List<BusVehicleDTO> getActiveBusVehicles(AuthDTO authDTO) {
		BusVehicleDAO dao = new BusVehicleDAO();
		List<BusVehicleDTO> list = dao.getAllBusVehicles(authDTO);

		return list;
	}

	@Override
	public void updateLastAssignedDate(AuthDTO authDTO, BusVehicleDTO busVehicle) {
		BusVehicleDAO dao = new BusVehicleDAO();
		dao.updateLastAssignedDate(authDTO, busVehicle);
	}

}
