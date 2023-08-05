package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.IntegrationCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.IntegrationDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.IntegrationDTO;
import org.in.com.dto.enumeration.IntegrationTypeEM;

import net.sf.ehcache.Element;

public class IntegrationCache {
	private static String CACHEKEY = "INTEGRATION";

	public List<IntegrationDTO> getIntegraionDTO(AuthDTO authDTO, IntegrationTypeEM integrationTypeDTO) {
		String key = CACHEKEY + Text.UNDER_SCORE + authDTO.getNamespaceCode() + Text.UNDER_SCORE + integrationTypeDTO.getCode();

		List<IntegrationDTO> integrationList = null;
		Element element = EhcacheManager.getNamespaceIntegrationEhCache().get(key);
		if (element != null) {
			List<IntegrationCacheDTO> cachelist = (List<IntegrationCacheDTO>) element.getObjectValue();
			integrationList = copyIntegrationFromCache(cachelist);
		}
		else if (element == null) {
			IntegrationDAO integrationDAO = new IntegrationDAO();
			integrationList = integrationDAO.getIntegration(authDTO, integrationTypeDTO);
			List<IntegrationCacheDTO> cacheList = copyIntegrationToCache(integrationList);
			element = new Element(key, cacheList);
			EhcacheManager.getNamespaceIntegrationEhCache().put(element);

		}
		return integrationList;
	}

	private List<IntegrationCacheDTO> copyIntegrationToCache(List<IntegrationDTO> integrationList) {
		List<IntegrationCacheDTO> cacheList = new ArrayList<IntegrationCacheDTO>();
		for (IntegrationDTO integration : integrationList) {
			IntegrationCacheDTO cacheDTO = new IntegrationCacheDTO();
			cacheDTO.setId(integration.getId());
			cacheDTO.setCode(integration.getCode());
			cacheDTO.setIntegrationType(integration.getIntegrationtype().getCode());
			cacheDTO.setAccount(integration.getAccount());
			cacheDTO.setAccessToken(integration.getAccessToken());
			cacheDTO.setAccessURL(integration.getAccessUrl());
			cacheDTO.setProvider(integration.getProvider());
			cacheList.add(cacheDTO);
		}
		return cacheList;
	}

	private List<IntegrationDTO> copyIntegrationFromCache(List<IntegrationCacheDTO> cacheList) {
		List<IntegrationDTO> integrationList = new ArrayList<IntegrationDTO>();
		for (IntegrationCacheDTO cache : cacheList) {
			IntegrationDTO integrationDTO = new IntegrationDTO();
			integrationDTO.setId(cache.getId());
			integrationDTO.setCode(cache.getCode());
			integrationDTO.setIntegrationtype(IntegrationTypeEM.getIntegrationTypeEM(cache.getIntegrationType()));
			integrationDTO.setAccount(cache.getAccount());
			integrationDTO.setAccessToken(cache.getAccessToken());
			integrationDTO.setAccessUrl(cache.getAccessURL());
			integrationDTO.setProvider(cache.getProvider());
			integrationList.add(integrationDTO);
		}
		return integrationList;
	}

	public void removeIntegraionCache(AuthDTO authDTO, IntegrationTypeEM integrationType) {
		String key = CACHEKEY + Text.UNDER_SCORE + authDTO.getNamespaceCode() + Text.UNDER_SCORE + integrationType.getCode();
		EhcacheManager.getNamespaceIntegrationEhCache().remove(key);
	}
}
