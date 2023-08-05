package org.in.com.cache;

import org.in.com.dao.NamespaceDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.NamespaceDTO;
import org.in.com.dto.NamespaceTabletSettingsDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

import net.sf.ehcache.Element;

public class NamespaceCache extends FareRuleCache {
	private static String CACHEKEY = "NASP";
	private static String ALL_NAMESPACE_KEY = "ALL_NAMESPACE_LIST_KEY";

	public NamespaceDTO getNamespaceDTO(NamespaceDTO namespaceDTO) {
		Element element = EhcacheManager.getNamespaceEhCache().get(namespaceDTO.getCode());
		if (element != null) {
			namespaceDTO = (NamespaceDTO) element.getObjectValue();
		}
		else {
			NamespaceDAO namespaceDAO = new NamespaceDAO();
			namespaceDAO.getNamespaceDTO(namespaceDTO);
			if (element == null && namespaceDTO != null && namespaceDTO.getId() != 0) {
				element = new Element(namespaceDTO.getCode(), namespaceDTO);
				EhcacheManager.getNamespaceEhCache().put(element);
			}
		}
		if (namespaceDTO == null || namespaceDTO.getId() == 0) {
			System.out.println("NS Code:" + (namespaceDTO != null ? namespaceDTO.getCode() : ""));
			throw new ServiceException(ErrorCode.INVALID_NAMESPACE);
		}
		return namespaceDTO;
	}

	protected NamespaceDTO getNamespaceDTObyId(NamespaceDTO namespaceDTO) {
		String code = null;
		String key = CACHEKEY + "_" + namespaceDTO.getId();
		Element elementKey = EhcacheManager.getNamespaceEhCache().get(key);
		if (elementKey != null) {
			code = (String) elementKey.getObjectValue();
			namespaceDTO.setCode(code);
		}
		namespaceDTO = getNamespaceDTO(namespaceDTO);
		if (elementKey == null && StringUtil.isNotNull(namespaceDTO.getCode()) && namespaceDTO.getId() != 0) {
			key = CACHEKEY + "_" + namespaceDTO.getId();
			elementKey = new Element(key, namespaceDTO.getCode());
			EhcacheManager.getNamespaceEhCache().put(elementKey);
		}
		return namespaceDTO;
	}

	protected void removeNamespaceDTO(AuthDTO authDTO, NamespaceDTO namespaceDTO) {
		EhcacheManager.getNamespaceEhCache().remove(namespaceDTO.getCode());
		EhcacheManager.getNamespaceEhCache().remove(ALL_NAMESPACE_KEY);
	}

	public NamespaceDTO getNamespaceDTO(String namespaceCode) {
		NamespaceDTO namespaceDTO = new NamespaceDTO();
		namespaceDTO.setCode(namespaceCode);
		return getNamespaceDTO(namespaceDTO);
	}

	public NamespaceTabletSettingsDTO getNamespaceTabletSettingsCache(AuthDTO authDTO) {
		NamespaceTabletSettingsDTO namespaceTabletSettingsDTO = null;
		Element element = EhcacheManager.getNamespaceEhCache().get("BB_" + authDTO.getNamespaceCode());
		if (element != null) {
			namespaceTabletSettingsDTO = (NamespaceTabletSettingsDTO) element.getObjectValue();
		}
		return namespaceTabletSettingsDTO;
	}

	public void putNamespaceTabletSettingsCache(AuthDTO authDTO, NamespaceTabletSettingsDTO namespaceTabletSettingsDTO) {
		Element element = new Element("BB_" + authDTO.getNamespaceCode(), namespaceTabletSettingsDTO);
		EhcacheManager.getNamespaceEhCache().put(element);
	}

	public void removeNamespaceTabletSettings(AuthDTO authDTO) {
		EhcacheManager.getNamespaceEhCache().remove("BB_" + authDTO.getNamespaceCode());
	}
}
