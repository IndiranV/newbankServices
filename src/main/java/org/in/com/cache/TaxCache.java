package org.in.com.cache;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Element;

import org.in.com.cache.dto.NamespaceTaxCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.NamespaceTaxDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.StateDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class TaxCache extends OrganizationCache {
	private static String CACHEKEY = "TAX_";
	private static String STATE_CACHE_KEY = "STATE_TAX_";

	protected NamespaceTaxDTO getNamespaceTaxDTO(AuthDTO authDTO, NamespaceTaxDTO taxDTO) {
		Element element = EhcacheManager.getNamespaceTaxCache().get(taxDTO.getCode());
		if (element != null) {
			NamespaceTaxCacheDTO cacheDTO = (NamespaceTaxCacheDTO) element.getObjectValue();
			copyTaxFromCache(cacheDTO, taxDTO);
		}
		else {
			NamespaceTaxDAO taxDAO = new NamespaceTaxDAO();
			taxDAO.getNamespaceTaxIdCode(authDTO, taxDTO);
			if (taxDTO.getId() != 0) {
				NamespaceTaxCacheDTO cacheDTO = copyTaxToCache(taxDTO);
				element = new Element(cacheDTO.getCode(), cacheDTO);
				EhcacheManager.getNamespaceTaxCache().put(element);
			}
			else {
				System.out.println("ERRORTAX01 ---" + taxDTO.getCode());
				throw new ServiceException(ErrorCode.INVALID_GSTIN);
			}
		}
		return taxDTO;
	}

	private NamespaceTaxCacheDTO copyTaxToCache(NamespaceTaxDTO taxDTO) {
		NamespaceTaxCacheDTO cacheDTO = new NamespaceTaxCacheDTO();
		cacheDTO.setId(taxDTO.getId());
		cacheDTO.setStateId(taxDTO.getState().getId());
		cacheDTO.setCode(taxDTO.getCode());
		cacheDTO.setGstin(taxDTO.getGstin());
		cacheDTO.setCgstValue(String.valueOf(taxDTO.getCgstValue()));
		cacheDTO.setSgstValue(String.valueOf(taxDTO.getSgstValue()));
		cacheDTO.setUgstValue(String.valueOf(taxDTO.getUgstValue()));
		cacheDTO.setIgstValue(String.valueOf(taxDTO.getIgstValue()));
		cacheDTO.setTradeName(taxDTO.getTradeName());
		return cacheDTO;
	}

	private void copyTaxFromCache(NamespaceTaxCacheDTO cacheDTO, NamespaceTaxDTO taxDTO) {
		taxDTO.setId(cacheDTO.getId());
		taxDTO.setActiveFlag(1);
		taxDTO.setCode(cacheDTO.getCode());
		taxDTO.setGstin(cacheDTO.getGstin());
		taxDTO.setCgstValue(new BigDecimal(cacheDTO.getCgstValue()));
		taxDTO.setSgstValue(new BigDecimal(cacheDTO.getSgstValue()));
		taxDTO.setUgstValue(new BigDecimal(cacheDTO.getUgstValue()));
		taxDTO.setIgstValue(new BigDecimal(cacheDTO.getIgstValue()));
		taxDTO.setTradeName(cacheDTO.getTradeName());
		StateDTO state = new StateDTO();
		state.setId(cacheDTO.getStateId());
		taxDTO.setState(state);
	}

	protected NamespaceTaxDTO getNamespaceTaxbyId(AuthDTO authDTO, NamespaceTaxDTO taxDTO) {
		String stationCode = null;
		String key = CACHEKEY + taxDTO.getId();
		Element elementKey = EhcacheManager.getNamespaceTaxCache().get(key);
		if (elementKey != null) {
			stationCode = (String) elementKey.getObjectValue();
			taxDTO.setCode(stationCode);
		}
		taxDTO = getNamespaceTaxDTO(authDTO, taxDTO);
		if (elementKey == null && StringUtil.isNotNull(taxDTO.getCode()) && taxDTO.getId() != 0) {
			key = CACHEKEY + taxDTO.getId();
			elementKey = new Element(key, taxDTO.getCode());
			EhcacheManager.getNamespaceTaxCache().put(elementKey);
		}
		return taxDTO;
	}

	protected void removegetNamespaceTaxDTO(AuthDTO authDTO, NamespaceTaxDTO taxDTO) {
		EhcacheManager.getNamespaceTaxCache().remove(taxDTO.getCode());
		EhcacheManager.getNamespaceTaxCache().remove(STATE_CACHE_KEY + authDTO.getNamespaceCode() + "_" + taxDTO.getState() != null ? taxDTO.getState().getCode() : Text.NA);
	}

	protected List<NamespaceTaxDTO> getNamespaceTaxbyStateCode(AuthDTO authDTO, StateDTO stateDTO) {
		List<NamespaceTaxDTO> list = new ArrayList<>();
		String key = STATE_CACHE_KEY + authDTO.getNamespaceCode() + "_" + stateDTO.getCode();
		Element elementKey = EhcacheManager.getNamespaceTaxCache().get(key);
		List<String> taxList = null;
		if (elementKey != null) {
			taxList = (List<String>) elementKey.getObjectValue();
		}
		else {
			NamespaceTaxDAO taxDAO = new NamespaceTaxDAO();
			taxList = taxDAO.getNamespaceTaxbyStateCode(authDTO, stateDTO);

			elementKey = new Element(key, taxList);
			EhcacheManager.getNamespaceTaxCache().put(elementKey);
		}
		for (String tax : taxList) {
			if (StringUtil.isNotNull(tax)) {
				NamespaceTaxDTO taxDTO = new NamespaceTaxDTO();
				taxDTO.setCode(tax);
				taxDTO = getNamespaceTaxDTO(authDTO, taxDTO);
				list.add(taxDTO);
			}
		}
		return list;
	}
}
