package org.in.com.service;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.UserDTO;

public interface OrganizationService extends BaseService<OrganizationDTO> {
	public OrganizationDTO getOrganization(AuthDTO authDTO, OrganizationDTO organization);

	void Update(AuthDTO authDTO, OrganizationDTO organizationDTO, UserDTO userDTO);

	public OrganizationDTO getOrganizationDTO(AuthDTO authDTO, OrganizationDTO dto);

}
