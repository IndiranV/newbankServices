package org.in.com.cache;

import net.sf.ehcache.Element;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.StationPointCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.StationPointDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.StationDTO;
import org.in.com.dto.StationPointDTO;
import org.in.com.dto.enumeration.StationPointAmenitiesEM;
import org.in.com.utils.StringUtil;

public class StationPointCache extends StationCache {
	private static String CACHEKEY = "STPO";

	protected StationPointDTO getStationPointDTO(AuthDTO authDTO, StationPointDTO stationPointDTO) {
		Element element = EhcacheManager.getStationPointEhCache().get(stationPointDTO.getCode());
		if (element != null) {
			StationPointCacheDTO cacheDTO = (StationPointCacheDTO) element.getObjectValue();
			copyStationPointFromCache(cacheDTO, stationPointDTO);
		}
		else {
			StationPointDAO pointDAO = new StationPointDAO();
			pointDAO.getStationPointbyIdCode(authDTO, stationPointDTO);
			if (stationPointDTO.getId() != 0 && StringUtil.isNotNull(stationPointDTO.getName())) {
				StationPointCacheDTO cacheDTO = copyStationPointToCache(stationPointDTO);
				element = new Element(cacheDTO.getCode(), cacheDTO);
				EhcacheManager.getStationPointEhCache().put(element);
			}
			else {
				System.out.println("ERRORSP02 -- " + authDTO.getNamespaceCode() + " - " + stationPointDTO.getCode() + " - " + stationPointDTO.getId());
			}
		}
		if (StringUtil.isNull(stationPointDTO.getName())) {
			System.out.println("ERRORSP01 ---" + stationPointDTO.getCode());
			removeStationPointDTO(authDTO, stationPointDTO);
		}
		return stationPointDTO;
	}

	private StationPointCacheDTO copyStationPointToCache(StationPointDTO stationPointDTO) {
		StationPointCacheDTO cacheDTO = new StationPointCacheDTO();
		cacheDTO.setActiveFlag(stationPointDTO.getActiveFlag());
		cacheDTO.setId(stationPointDTO.getId());
		cacheDTO.setCode(stationPointDTO.getCode());
		cacheDTO.setName(stationPointDTO.getName());
		cacheDTO.setAddress(stationPointDTO.getAddress());
		cacheDTO.setLandmark(stationPointDTO.getLandmark());
		cacheDTO.setMapUrl(stationPointDTO.getMapUrl());
		cacheDTO.setNumber(stationPointDTO.getNumber());
		cacheDTO.setLatitude(stationPointDTO.getLatitude());
		cacheDTO.setLongitude(stationPointDTO.getLongitude());
		cacheDTO.setAmenitiesCodes(stationPointDTO.getAmenities());
		if (stationPointDTO.getStation() != null) {
			cacheDTO.setStationId(stationPointDTO.getStation().getId());
		}
		return cacheDTO;
	}

	private void copyStationPointFromCache(StationPointCacheDTO cacheDTO, StationPointDTO stationPointDTO) {
		stationPointDTO.setActiveFlag(cacheDTO.getActiveFlag());
		stationPointDTO.setId(cacheDTO.getId());
		stationPointDTO.setCode(cacheDTO.getCode());
		stationPointDTO.setName(cacheDTO.getName());
		stationPointDTO.setAddress(cacheDTO.getAddress());
		stationPointDTO.setLandmark(cacheDTO.getLandmark());
		stationPointDTO.setNumber(cacheDTO.getNumber());
		stationPointDTO.setLatitude(cacheDTO.getLatitude());
		stationPointDTO.setLongitude(cacheDTO.getLongitude());
		stationPointDTO.setMapUrl(cacheDTO.getMapUrl());
		stationPointDTO.setAmenities(cacheDTO.getAmenitiesCodes());

		StationDTO stationDTO = new StationDTO();
		stationDTO.setId(cacheDTO.getStationId());
		stationPointDTO.setStation(stationDTO);
	}

	public StationPointDTO getStationPointDTObyId(AuthDTO authDTO, StationPointDTO stationPointDTO) {
		String stationCode = null;
		String key = CACHEKEY + "_" + stationPointDTO.getId();
		Element elementKey = EhcacheManager.getStationPointEhCache().get(key);
		if (elementKey != null) {
			stationCode = (String) elementKey.getObjectValue();
			stationPointDTO.setCode(stationCode);
		}
		stationPointDTO = getStationPointDTO(authDTO, stationPointDTO);
		if (elementKey == null && StringUtil.isNotNull(stationPointDTO.getCode()) && stationPointDTO.getId() != 0) {
			key = CACHEKEY + "_" + stationPointDTO.getId();
			elementKey = new Element(key, stationPointDTO.getCode());
			EhcacheManager.getStationPointEhCache().put(elementKey);
		}
		return stationPointDTO;
	}

	protected String getStationPointCodebyId(AuthDTO authDTO, StationPointDTO stationPointDTO) {
		String stationPointCode = null;
		String key = CACHEKEY + "_" + stationPointDTO.getId();
		Element elementKey = EhcacheManager.getStationPointEhCache().get(key);
		if (elementKey != null) {
			stationPointCode = (String) elementKey.getObjectValue();
			stationPointDTO.setCode(stationPointCode);
		}
		return stationPointCode;
	}
	protected void removeStationPointDTO(AuthDTO authDTO, StationPointDTO pointDTO) {
		EhcacheManager.getStationPointEhCache().remove(pointDTO.getCode());
	}

}
