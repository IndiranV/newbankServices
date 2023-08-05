package org.in.com.service.impl;

import java.util.List;

import org.in.com.cache.IntegrationCache;
import org.in.com.dao.IntegrationDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.enumeration.IntegrationTypeEM;
import org.in.com.service.IntegrationService;
import org.springframework.stereotype.Service;

@Service
public class IntegrationServiceImpl extends IntegrationCache implements IntegrationService {

	@Override
	public List<IntegrationDTO> get(AuthDTO authDTO, IntegrationDTO integrationDTO) {
		IntegrationDAO integrationDAO = new IntegrationDAO();
		integrationDAO.getIntegration(authDTO, integrationDTO);
		return null;
	}

	@Override
	public List<IntegrationDTO> getAll(AuthDTO authDTO) {
		IntegrationDAO integrationDAO = new IntegrationDAO();
		return integrationDAO.getAllIntegration(authDTO);
	}

	@Override
	public IntegrationDTO Update(AuthDTO authDTO, IntegrationDTO integrationDTO) {
		IntegrationDAO integrationDAO = new IntegrationDAO();
		integrationDAO.integrationUpdate(authDTO, integrationDTO);

		removeIntegraionCache(authDTO, integrationDTO.getIntegrationtype());
		return null;
	}

	@Override
	public IntegrationDTO getIntegration(AuthDTO authDTO, IntegrationTypeEM integrationTypeEM, String provider) {
		List<IntegrationDTO> integrationList = getIntegraionDTO(authDTO, integrationTypeEM);
		IntegrationDTO integrationDTO = new IntegrationDTO();
		if (integrationList != null && !integrationList.isEmpty()) {
			for (IntegrationDTO integration : integrationList) {
				if (integration.getProvider().equalsIgnoreCase(provider)) {
					integrationDTO = integration;
					break;
				}
			}
		}
		return integrationDTO;
	}

	public List<IntegrationDTO> getIntegration(AuthDTO authDTO, IntegrationTypeEM integrationTypeEM) {
		List<IntegrationDTO> integrationList = getIntegraionDTO(authDTO, integrationTypeEM);
		return integrationList;
	}

	@Override
	public void getIntegrationV2(NamespaceDTO namespaceDTO, IntegrationDTO integrationDTO) {
		IntegrationDAO integrationDAO = new IntegrationDAO();
		integrationDAO.getIntegrationV2(namespaceDTO, integrationDTO);
	}

}
