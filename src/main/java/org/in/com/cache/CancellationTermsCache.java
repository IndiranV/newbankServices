package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.CancellationPolicyCacheDTO;
import org.in.com.cache.dto.CancellationTermsCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.CancellationTermsDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.CancellationPolicyDTO;
import org.in.com.dto.CancellationTermDTO;
import org.in.com.utils.StringUtil;

import net.sf.ehcache.Element;

public class CancellationTermsCache {
	private static String CACHEKEY = "TERM";

	public CancellationTermDTO getCancellationTermDTO(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		Element element = EhcacheManager.getCancellationTermsEhCache().get(cancellationTermDTO.getCode());
		if (element != null) {
			CancellationTermsCacheDTO cacheDTO = (CancellationTermsCacheDTO) element.getObjectValue();
			copyCancellationTermFromCache(cacheDTO, cancellationTermDTO);
		}
		else {
			CancellationTermsDAO termsDAO = new CancellationTermsDAO();
			termsDAO.getCancellationTerms(authDTO, cancellationTermDTO);
			if (StringUtil.isNotNull(cancellationTermDTO.getCode())) {
				CancellationTermsCacheDTO cacheDTO = copyCancellationTermToCache(cancellationTermDTO);
				element = new Element(cacheDTO.getCode(), cacheDTO);
				EhcacheManager.getCancellationTermsEhCache().put(element);
			}
		}
		return cancellationTermDTO;
	}

	public void getCancellationTermGroupKeyById(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		String key = CACHEKEY + "_GROUP_" + cancellationTermDTO.getPolicyGroupId();

		Element element = EhcacheManager.getCancellationTermsEhCache().get(key);
		if (element != null) {
			String policyGroupKey = (String) element.getObjectValue();
			cancellationTermDTO.setPolicyGroupKey(policyGroupKey);
		}
		if (cancellationTermDTO.getPolicyGroupId() != 0 && (element == null || StringUtil.isNull(cancellationTermDTO.getPolicyGroupKey()))) {
			CancellationTermsDAO termsDAO = new CancellationTermsDAO();
			termsDAO.getCancellationPolicyGroup(authDTO, cancellationTermDTO);
			if (StringUtil.isNotNull(cancellationTermDTO.getPolicyGroupKey())) {
				element = new Element(key, cancellationTermDTO.getPolicyGroupKey());
				EhcacheManager.getCancellationTermsEhCache().put(element);
			}
		}
	}

	public CancellationTermDTO getCancellationTermByGroupKey(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		getCancellationTermGroupKeyById(authDTO, cancellationTermDTO);

		if (StringUtil.isNotNull(cancellationTermDTO.getPolicyGroupKey())) {
			Element element = EhcacheManager.getCancellationTermsEhCache().get(cancellationTermDTO.getPolicyGroupKey());
			if (element != null) {
				CancellationTermsCacheDTO cacheDTO = (CancellationTermsCacheDTO) element.getObjectValue();
				copyCancellationTermFromCache(cacheDTO, cancellationTermDTO);
			}
			else {
				CancellationTermsDAO termsDAO = new CancellationTermsDAO();
				termsDAO.getCancellationTerms(authDTO, cancellationTermDTO);
				if (StringUtil.isNotNull(cancellationTermDTO.getCode())) {
					CancellationTermsCacheDTO cacheDTO = copyCancellationTermToCache(cancellationTermDTO);
					element = new Element(cacheDTO.getPolicyGroupKey(), cacheDTO);
					EhcacheManager.getCancellationTermsEhCache().put(element);
				}
			}
			if (StringUtil.isNotNull(cancellationTermDTO.getPolicyGroupKey()) && (cancellationTermDTO.getPolicyList() == null || cancellationTermDTO.getPolicyList().isEmpty())) {
				cancellationTermDTO.setCode(StringUtil.isNull(cancellationTermDTO.getCode(), Text.NA));
				CancellationTermsDAO termsDAO = new CancellationTermsDAO();
				termsDAO.getCancellationPolicyByGroupkey(authDTO, cancellationTermDTO);
			}
		}
		return cancellationTermDTO;
	}

	public void getCancellationTermGroupIdByGroupKeyCache(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		String key = CACHEKEY + "_GROUP_" + cancellationTermDTO.getPolicyGroupKey();

		Element element = EhcacheManager.getCancellationTermsEhCache().get(key);
		if (element != null) {
			int policyGroupId = (int) element.getObjectValue();
			cancellationTermDTO.setPolicyGroupId(policyGroupId);
		}
		else {
			CancellationTermsDAO termsDAO = new CancellationTermsDAO();
			termsDAO.getCancellationPolicyGroupId(authDTO, cancellationTermDTO);
			if (cancellationTermDTO.getPolicyGroupId() != 0) {
				element = new Element(key, cancellationTermDTO.getPolicyGroupId());
				EhcacheManager.getCancellationTermsEhCache().put(element);
			}
		}
	}

	private void copyCancellationTermFromCache(CancellationTermsCacheDTO cacheDTO, CancellationTermDTO cancellationTermDTO) {
		cancellationTermDTO.setCode(cacheDTO.getCode());
		cancellationTermDTO.setActiveFlag(cacheDTO.getActiveFlag());
		cancellationTermDTO.setId(cacheDTO.getId());
		cancellationTermDTO.setName(cacheDTO.getName());
		cancellationTermDTO.setPolicyGroupKey(cacheDTO.getPolicyGroupKey());
		List<CancellationPolicyDTO> policyListCache = new ArrayList<>();
		for (CancellationPolicyCacheDTO policyCacheDTODTO : cacheDTO.getPolicyList()) {
			CancellationPolicyDTO policyDTO = new CancellationPolicyDTO();
			policyDTO.setDeductionValue(policyCacheDTODTO.getDeductionValue());
			policyDTO.setFromValue(policyCacheDTODTO.getFromValue());
			policyDTO.setToValue(policyCacheDTODTO.getToValue());
			policyDTO.setPercentageFlag(policyCacheDTODTO.getPercentageFlag());
			policyDTO.setPolicyId(policyCacheDTODTO.getPolicyId());
			policyDTO.setPolicyPattern(policyCacheDTODTO.getPolicyPattern());
			policyListCache.add(policyDTO);
		}
		cancellationTermDTO.setPolicyList(policyListCache);
	}

	public CancellationTermDTO getCancellationTermDTOById(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		String cancellationCode = null;
		String key = CACHEKEY + authDTO.getNamespace().getId() + "_" + cancellationTermDTO.getId();
		Element elementKey = EhcacheManager.getCancellationTermsEhCache().get(key);
		if (elementKey != null) {
			cancellationCode = (String) elementKey.getObjectValue();
			cancellationTermDTO.setCode(cancellationCode);
		}
		cancellationTermDTO = getCancellationTermDTO(authDTO, cancellationTermDTO);
		if (elementKey == null && StringUtil.isNotNull(cancellationTermDTO.getCode()) && cancellationTermDTO.getId() != 0) {
			key = CACHEKEY + authDTO.getNamespace().getId() + "_" + cancellationTermDTO.getId();
			elementKey = new Element(key, cancellationTermDTO.getCode());
			EhcacheManager.getCancellationTermsEhCache().put(elementKey);
		}
		return cancellationTermDTO;
	}

	private CancellationTermsCacheDTO copyCancellationTermToCache(CancellationTermDTO cancellationTermDTO) {
		CancellationTermsCacheDTO cacheDTO = new CancellationTermsCacheDTO();
		cacheDTO.setCode(cancellationTermDTO.getCode());
		cacheDTO.setActiveFlag(cancellationTermDTO.getActiveFlag());
		cacheDTO.setId(cancellationTermDTO.getId());
		cacheDTO.setName(cancellationTermDTO.getName());
		cacheDTO.setPolicyGroupKey(cancellationTermDTO.getPolicyGroupKey());
		List<CancellationPolicyCacheDTO> policyListCache = new ArrayList<>();
		if (cancellationTermDTO.getPolicyList() != null && !cancellationTermDTO.getPolicyList().isEmpty()) {
			for (CancellationPolicyDTO policyDTO : cancellationTermDTO.getPolicyList()) {
				CancellationPolicyCacheDTO cancellationPolicyCacheDTO = new CancellationPolicyCacheDTO();
				cancellationPolicyCacheDTO.setDeductionValue(policyDTO.getDeductionValue());
				cancellationPolicyCacheDTO.setFromValue(policyDTO.getFromValue());
				cancellationPolicyCacheDTO.setToValue(policyDTO.getToValue());
				cancellationPolicyCacheDTO.setPercentageFlag(policyDTO.getPercentageFlag());
				cancellationPolicyCacheDTO.setPolicyId(policyDTO.getPolicyId());
				cancellationPolicyCacheDTO.setPolicyPattern(policyDTO.getPolicyPattern());
				policyListCache.add(cancellationPolicyCacheDTO);
			}
		}
		cacheDTO.setPolicyList(policyListCache);
		return cacheDTO;
	}

	public void removeCancellationTermDTO(AuthDTO authDTO, CancellationTermDTO cancellationTermDTO) {
		EhcacheManager.getCancellationTermsEhCache().remove(cancellationTermDTO.getCode());
	}

}
