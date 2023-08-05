package org.in.com.service;

import java.util.List;

import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.enumeration.IntegrationTypeEM;

public interface IntegrationService extends BaseService<IntegrationDTO> {

	public IntegrationDTO getIntegration(AuthDTO authDTO, IntegrationTypeEM integrationTypeEM, String provider);

	public List<IntegrationDTO> getIntegration(AuthDTO authDTO, IntegrationTypeEM integrationTypeEM);

	public void getIntegrationV2(NamespaceDTO namespaceDTO, IntegrationDTO integrationDTO);

}
