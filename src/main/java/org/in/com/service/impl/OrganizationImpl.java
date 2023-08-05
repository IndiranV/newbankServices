package org.in.com.service.impl;

import java.util.Iterator;
import java.util.List;

import org.in.com.constants.Numeric;
import org.in.com.dao.OrganizationDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.SectorDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.UserDTO;
import org.in.com.exception.ServiceException;
import org.in.com.service.OrganizationService;
import org.in.com.service.SectorService;
import org.in.com.service.StationService;
import org.in.com.utils.BitsUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OrganizationImpl extends BaseImpl implements OrganizationService {

	@Autowired
	StationService stationService;
	@Autowired
	SectorService sectorService;

	public OrganizationDTO getOrganization(AuthDTO authDTO, OrganizationDTO organization) {
		OrganizationDTO organizationDTO = null;
		if (organization.getId() != 0) {
			organizationDTO = getOrganizationDTObyId(authDTO, organization);
		}
		else if (StringUtil.isNotNull(organization.getCode())) {
			organizationDTO = getOrganizationByCode(authDTO, organization);
		}
		return organizationDTO;
	}

	public List<OrganizationDTO> get(AuthDTO authDTO, OrganizationDTO dto) {
		OrganizationDAO dao = new OrganizationDAO();
		return dao.getOrganization(authDTO, dto);
	}

	public List<OrganizationDTO> getAll(AuthDTO authDTO) {
		SectorDTO sector = sectorService.getActiveUserSectorOrganization(authDTO);

		OrganizationDAO dao = new OrganizationDAO();
		List<OrganizationDTO> organizations = dao.getAllOrganizations(authDTO);
		for (Iterator<OrganizationDTO> iterator = organizations.iterator(); iterator.hasNext();) {
			OrganizationDTO organizationDTO = iterator.next();
			// Apply Sector Organization filter
			if (sector.getActiveFlag() == Numeric.ONE_INT && BitsUtil.isOrganizationExists(sector.getOrganization(), organizationDTO) == null) {
				iterator.remove();
				continue;
			}
		}
		return organizations;
	}

	public OrganizationDTO Update(AuthDTO authDTO, OrganizationDTO organizationDTO) {
		if (organizationDTO.getStation() != null && StringUtil.isNotNull(organizationDTO.getStation().getCode())) {

			StationDTO stationDTO = stationService.getStation(organizationDTO.getStation());
			if (stationDTO.getId() != 0) {
				organizationDTO.getStation().setId(stationDTO.getId());
			}
			else {
				throw new ServiceException(201);
			}
		}
		OrganizationDAO dao = new OrganizationDAO();
		dao.getOrganizationIUD(authDTO, organizationDTO);
		removeOrganizationDTO(authDTO, organizationDTO);
		return organizationDTO;
	}

	public void Update(AuthDTO authDTO, OrganizationDTO organizationDTO, UserDTO userDTO) {
		OrganizationDAO dao = new OrganizationDAO();
		dao.getOrganizationMapUser(authDTO, organizationDTO, userDTO);
		removeUserDTO(authDTO, userDTO);
	}

	@Override
	public OrganizationDTO getOrganizationDTO(AuthDTO authDTO, OrganizationDTO dto) {
		OrganizationDAO dao = new OrganizationDAO();
		return dao.getOrganizationDTO(authDTO, dto);
	}
}
