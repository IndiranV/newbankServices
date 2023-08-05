package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Element;

import org.apache.commons.lang.ArrayUtils;
import org.in.com.cache.EhcacheManager;
import org.in.com.config.ApplicationConfig;
import org.in.com.constants.Constants;
import org.in.com.dao.AmentiesDAO;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.AmentiesService;
import org.springframework.stereotype.Service;

@Service
public class AmentiesImpl implements AmentiesService {

	@Override
	public void reloadAmenties() {
		AmentiesDAO dao = new AmentiesDAO();
		List<AmenitiesDTO> list = dao.getAllAmenties(null);
		EhcacheManager.getAmenitiesEhCache().removeAll();
		for (AmenitiesDTO amentiesDTO : list) {
			Element element = new Element(amentiesDTO.getCode(), amentiesDTO);
			EhcacheManager.getAmenitiesEhCache().put(element);
		}
	}

	public List<AmenitiesDTO> get(AuthDTO authDTO, AmenitiesDTO dto) {
		return null;
	}

	@SuppressWarnings("unchecked")
	public List<AmenitiesDTO> getAll(AuthDTO authDTO) {
		List<AmenitiesDTO> list = new ArrayList<AmenitiesDTO>();
		List<String> keys = EhcacheManager.getAmenitiesEhCache().getKeys();
		for (String key : keys) {
			Element element = EhcacheManager.getAmenitiesEhCache().get(key);
			if (element != null) {
				list.add((AmenitiesDTO) element.getObjectValue());
			}
		}
		return list;
	}

	public List<AmenitiesDTO> getAllforZoneSync(AuthDTO authDTO, String syncDate) {
		AmentiesDAO dao = new AmentiesDAO();
		return dao.getAllforZoneSync(syncDate);
	}

	public AmenitiesDTO Update(AuthDTO authDTO, AmenitiesDTO amentiesDTO) {
		if (!ArrayUtils.contains(Constants.SUPER_REGIONS_ZONE, ApplicationConfig.getServerZoneCode())) {
			throw new ServiceException(ErrorCode.INVALID_APPLICATION_ZONE);
		}
		AmentiesDAO dao = new AmentiesDAO();
		dao.getAmentiesUpdate(authDTO, amentiesDTO);
		reloadAmenties();
		return amentiesDTO;
	}

}
