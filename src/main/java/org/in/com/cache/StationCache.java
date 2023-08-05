package org.in.com.cache;

import net.sf.ehcache.Element;

import org.in.com.cache.dto.StationCacheDTO;
import org.in.com.dao.StationDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.StateDTO;
import org.in.com.dto.StationDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;

public class StationCache extends UserCache {
	private static String CACHEKEY = "STAT";

	protected StationDTO getStationDTO(StationDTO stationDTO) {
		Element element = EhcacheManager.getStationEhCache().get(stationDTO.getCode());
		if (element != null) {
			StationCacheDTO cacheDTO = (StationCacheDTO) element.getObjectValue();
			copyStationFromCache(cacheDTO, stationDTO);
		}
		else {
			StationDAO stationDAO = new StationDAO();
			stationDAO.getStationDTO(stationDTO);
			if (stationDTO.getId() != 0 && StringUtil.isNotNull(stationDTO.getCode())) {
				StationCacheDTO cacheDTO = copyStationToCache(stationDTO);
				element = new Element(cacheDTO.getCode(), cacheDTO);
				EhcacheManager.getStationEhCache().put(element);
			}
			else {
				System.out.println(DateUtil.NOW() + CACHEKEY + ErrorCode.INVALID_STATION.getMessage() + stationDTO.getCode());
				Thread.dumpStack();
				throw new ServiceException(ErrorCode.INVALID_STATION);
			}
		}
		return stationDTO;
	}

	private StationCacheDTO copyStationToCache(StationDTO stationDTO) {
		StationCacheDTO cacheDTO = new StationCacheDTO();
		cacheDTO.setActiveFlag(stationDTO.getActiveFlag());
		cacheDTO.setId(stationDTO.getId());
		cacheDTO.setCode(stationDTO.getCode());
		cacheDTO.setName(stationDTO.getName());
		cacheDTO.setLatitude(stationDTO.getLatitude());
		cacheDTO.setLongitude(stationDTO.getLongitude());
		if (stationDTO.getState() != null) {
			cacheDTO.setStateCode(stationDTO.getState().getCode());
		}
		return cacheDTO;
	}

	private void copyStationFromCache(StationCacheDTO cacheDTO, StationDTO stationDTO) {
		stationDTO.setActiveFlag(cacheDTO.getActiveFlag());
		stationDTO.setId(cacheDTO.getId());
		stationDTO.setCode(cacheDTO.getCode());
		stationDTO.setName(cacheDTO.getName());
		stationDTO.setLatitude(cacheDTO.getLatitude());
		stationDTO.setLongitude(cacheDTO.getLongitude());
		if (StringUtil.isNotNull(cacheDTO.getStateCode())) {
			StateDTO stateDTO = new StateDTO();
			stateDTO.setCode(cacheDTO.getStateCode());
			stationDTO.setState(stateDTO);
		}
	}

	protected StationDTO getStationDTObyId(StationDTO stationDTO) {
		String stationCode = null;
		String key = CACHEKEY + "_" + stationDTO.getId();
		Element elementKey = EhcacheManager.getStationEhCache().get(key);
		if (elementKey != null) {
			stationCode = (String) elementKey.getObjectValue();
			stationDTO.setCode(stationCode);
		}
		stationDTO = getStationDTO(stationDTO);
		if (elementKey == null && StringUtil.isNotNull(stationDTO.getCode()) && stationDTO.getId() != 0) {
			key = CACHEKEY + "_" + stationDTO.getId();
			elementKey = new Element(key, stationDTO.getCode());
			EhcacheManager.getStationEhCache().put(elementKey);
		}
		return stationDTO;
	}

	protected void removeStationDTO(AuthDTO authDTO, StationDTO stationDTO) {
		EhcacheManager.getStationEhCache().remove(stationDTO.getCode());
	}

}
