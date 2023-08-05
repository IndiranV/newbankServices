package org.in.com.cache;

import net.sf.ehcache.Element;

import org.in.com.cache.dto.FareRuleCacheDTO;
import org.in.com.dao.FareRuleDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.FareRuleDTO;
import org.in.com.dto.StateDTO;
import org.in.com.utils.StringUtil;

public class FareRuleCache extends AuthCache {
	private static String CACHEKEY = "FARE_RULE";

	public FareRuleDTO getFareRuleDTO(AuthDTO authDTO, FareRuleDTO fareRuleDTO) {
		Element element = EhcacheManager.getFareRuleCache().get(fareRuleDTO.getCode());
		if (element != null) {
			FareRuleCacheDTO cacheDTO = (FareRuleCacheDTO) element.getObjectValue();
			copyFareRuleFromCache(cacheDTO, fareRuleDTO);
		}
		else if (element == null) {
			FareRuleDAO fareRuleDAO = new FareRuleDAO();
			fareRuleDAO.getFareRule(authDTO, fareRuleDTO);
			if (StringUtil.isNotNull(fareRuleDTO.getCode()) && fareRuleDTO.getId() != 0) {
				FareRuleCacheDTO cacheDTO = copyFareRuleToCache(fareRuleDTO);
				element = new Element(cacheDTO.getCode(), cacheDTO);
				EhcacheManager.getFareRuleCache().put(element);
			}
		}
		return fareRuleDTO;
	}

	public FareRuleDTO getFareRuleById(AuthDTO authDTO, FareRuleDTO fareRuleDTO) {
		String fareRuleCode = null;
		String Key = CACHEKEY + "_" + fareRuleDTO.getId();
		Element elementKey = EhcacheManager.getFareRuleCache().get(Key);
		if (elementKey != null) {
			fareRuleCode = (String) elementKey.getObjectValue();
			fareRuleDTO.setCode(fareRuleCode);
		}
		fareRuleDTO = getFareRuleDTO(authDTO, fareRuleDTO);
		if (elementKey == null && StringUtil.isNotNull(fareRuleDTO.getCode()) && fareRuleDTO.getId() != 0) {
			Key = CACHEKEY + "_" + fareRuleDTO.getId();
			elementKey = new Element(Key, fareRuleDTO.getCode());
			EhcacheManager.getFareRuleCache().put(elementKey);
		}
		return fareRuleDTO;
	}

	private FareRuleCacheDTO copyFareRuleToCache(FareRuleDTO fareRuleDTO) {
		FareRuleCacheDTO fareRuleCacheDTO = new FareRuleCacheDTO();
		fareRuleCacheDTO.setId(fareRuleDTO.getId());
		fareRuleCacheDTO.setCode(fareRuleDTO.getCode());
		fareRuleCacheDTO.setName(fareRuleDTO.getName());
		fareRuleCacheDTO.setActiveFlag(fareRuleDTO.getActiveFlag());
		fareRuleCacheDTO.setStateCode(fareRuleDTO.getState().getCode());
		return fareRuleCacheDTO;

	}

	private void copyFareRuleFromCache(FareRuleCacheDTO cacheDTO, FareRuleDTO fareRuleDTO) {
		fareRuleDTO.setId(cacheDTO.getId());
		fareRuleDTO.setCode(cacheDTO.getCode());
		fareRuleDTO.setName(cacheDTO.getName());
		fareRuleDTO.setActiveFlag(cacheDTO.getActiveFlag());

		StateDTO stateDTO = new StateDTO();
		stateDTO.setCode(cacheDTO.getStateCode());
		fareRuleDTO.setState(stateDTO);
	}
}
