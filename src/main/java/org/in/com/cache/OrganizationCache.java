package org.in.com.cache;

import net.sf.ehcache.Element;

import org.in.com.cache.dto.OrganizationCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.OrganizationDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.OrganizationDTO;
import org.in.com.dto.StationDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.StringUtil;

public class OrganizationCache extends NamespaceCache {
	private static String CACHEKEY = "ORGA";

	protected OrganizationDTO getOrganizationByCode(AuthDTO authDTO, OrganizationDTO organizationDTO) {
		Element element = EhcacheManager.getOrganizationEhCache().get(organizationDTO.getCode());
		if (element != null) {
			OrganizationCacheDTO cacheDTO = (OrganizationCacheDTO) element.getObjectValue();
			copyOrganizationFromCache(cacheDTO, organizationDTO);
		}
		else {
			OrganizationDAO organizationDAO = new OrganizationDAO();
			organizationDAO.getOrganizationDTO(authDTO, organizationDTO);
			if (organizationDTO.getId() != 0 && StringUtil.isNotNull(organizationDTO.getCode())) {
				OrganizationCacheDTO cacheDTO = copyOrganizationToCache(organizationDTO);
				element = new Element(cacheDTO.getCode(), cacheDTO);
				EhcacheManager.getOrganizationEhCache().put(element);
			}
			else {
				System.out.println(authDTO.getNamespaceCode() + Text.UNDER_SCORE + ErrorCode.INVALID_ORGANIZATION.getMessage() + organizationDTO.getCode() + " - " + organizationDTO.getId());
				throw new ServiceException(ErrorCode.INVALID_ORGANIZATION);
			}
		}
		return organizationDTO;
	}

	private OrganizationCacheDTO copyOrganizationToCache(OrganizationDTO organizationDTO) {
		OrganizationCacheDTO cacheDTO = new OrganizationCacheDTO();
		cacheDTO.setActiveFlag(organizationDTO.getActiveFlag());
		cacheDTO.setId(organizationDTO.getId());
		cacheDTO.setCode(organizationDTO.getCode());
		cacheDTO.setName(organizationDTO.getName());
		cacheDTO.setShortCode(organizationDTO.getShortCode());
		cacheDTO.setAddress1(organizationDTO.getAddress1());
		cacheDTO.setAddress2(organizationDTO.getAddress2());
		cacheDTO.setContact(organizationDTO.getContact());
		cacheDTO.setPincode(organizationDTO.getPincode());
		cacheDTO.setLatLon(organizationDTO.getLatLon());
		cacheDTO.setWorkingMinutes(organizationDTO.getWorkingMinutes());

		if (organizationDTO.getStation() != null) {
			cacheDTO.setStationCode(organizationDTO.getStation().getCode());
		}
		return cacheDTO;
	}

	private void copyOrganizationFromCache(OrganizationCacheDTO cacheDTO, OrganizationDTO organizationDTO) {
		organizationDTO.setActiveFlag(cacheDTO.getActiveFlag());
		organizationDTO.setId(cacheDTO.getId());
		organizationDTO.setCode(cacheDTO.getCode());
		organizationDTO.setName(cacheDTO.getName());
		organizationDTO.setShortCode(cacheDTO.getShortCode());
		organizationDTO.setAddress1(cacheDTO.getAddress1());
		organizationDTO.setAddress2(cacheDTO.getAddress2());
		organizationDTO.setContact(cacheDTO.getContact());
		organizationDTO.setPincode(cacheDTO.getPincode());
		organizationDTO.setLatLon(cacheDTO.getLatLon());
		organizationDTO.setWorkingMinutes(cacheDTO.getWorkingMinutes());

		if (StringUtil.isNotNull(cacheDTO.getStationCode())) {
			StationDTO stationDTO = new StationDTO();
			stationDTO.setCode(cacheDTO.getStationCode());
			organizationDTO.setStation(stationDTO);
		}
	}

	public OrganizationDTO getOrganizationDTObyId(AuthDTO authDTO, OrganizationDTO organizationDTO) {
		String stationCode = null;
		String key = CACHEKEY + "_" + organizationDTO.getId();
		Element elementKey = EhcacheManager.getOrganizationEhCache().get(key);
		if (elementKey != null) {
			stationCode = (String) elementKey.getObjectValue();
			organizationDTO.setCode(stationCode);
		}
		organizationDTO = getOrganizationByCode(authDTO, organizationDTO);
		if (elementKey == null && StringUtil.isNotNull(organizationDTO.getCode()) && organizationDTO.getId() != 0) {
			key = CACHEKEY + "_" + organizationDTO.getId();
			elementKey = new Element(key, organizationDTO.getCode());
			EhcacheManager.getOrganizationEhCache().put(elementKey);
		}
		return organizationDTO;
	}

	protected void removeOrganizationDTO(AuthDTO authDTO, OrganizationDTO organizationDTO) {
		EhcacheManager.getOrganizationEhCache().remove(organizationDTO.getCode());
	}

}
