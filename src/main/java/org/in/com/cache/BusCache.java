package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Element;

import org.in.com.cache.dto.BusCacheDTO;
import org.in.com.cache.dto.BusSeatLayoutCacheDTO;
import org.in.com.dao.BusDAO;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.enumeration.BusSeatTypeEM;
import org.in.com.utils.StringUtil;

public class BusCache extends StationPointCache {
	private static String CACHEKEY = "BUS";

	public BusDTO getBusDTO(AuthDTO authDTO, BusDTO busDTO) {
		Element element = EhcacheManager.getBusEhCache().get(busDTO.getCode());
		if (element != null) {
			BusCacheDTO busCacheDTO = (BusCacheDTO) element.getObjectValue();
			bindBusFromCacheObject(busCacheDTO, busDTO);
		}
		else {
			BusDAO busDAO = new BusDAO();
			busDAO.getBusDTO(authDTO, busDTO);
			if (StringUtil.isNotNull(busDTO.getCode()) && StringUtil.isNotNull(busDTO.getCategoryCode())) {
				BusCacheDTO busCacheDTO = bindBusToCacheObject(busDTO);
				// copy object
				element = new Element(busCacheDTO.getCode(), busCacheDTO);
				EhcacheManager.getBusEhCache().put(element);
			}
		}
		if (StringUtil.isNotNull(busDTO.getCode()) && StringUtil.isNull(busDTO.getCategoryCode())) {
			EhcacheManager.getBusEhCache().remove(busDTO.getCode());
		}
		return busDTO;
	}

	public BusDTO getBusDTObyId(AuthDTO authDTO, BusDTO busDTO) {
		if (busDTO != null && busDTO.getId() != 0) {
			String busCode = null;
			String key = CACHEKEY + authDTO.getNamespace().getId() + "_" + busDTO.getId();
			Element elementKey = EhcacheManager.getBusEhCache().get(key);
			if (elementKey != null) {
				busCode = (String) elementKey.getObjectValue();
				busDTO.setCode(busCode);
			}
			busDTO = getBusDTO(authDTO, busDTO);
			if (elementKey == null && StringUtil.isNotNull(busDTO.getCode()) && busDTO.getId() != 0) {
				key = CACHEKEY + authDTO.getNamespace().getId() + "_" + busDTO.getId();
				elementKey = new Element(key, busDTO.getCode());
				EhcacheManager.getBusEhCache().put(elementKey);
			}
		}
		return busDTO;
	}

	public void removeBusDTO(AuthDTO authDTO, BusDTO busDTO) {
		EhcacheManager.getBusEhCache().remove(busDTO.getCode());
	}

	public AmenitiesDTO getAmenitiesDTO(String amenitiesCode) {
		AmenitiesDTO amentiesDTO = null;
		if (StringUtil.isNotNull(amenitiesCode)) {
			Element element = EhcacheManager.getAmenitiesEhCache().get(amenitiesCode);
			if (element != null) {
				amentiesDTO = (AmenitiesDTO) element.getObjectValue();
			}
		}
		return amentiesDTO;
	}

	protected BusCacheDTO bindBusToCacheObject(BusDTO busDTO) {
		BusCacheDTO cacheDTO = new BusCacheDTO();
		cacheDTO.setActiveFlag(busDTO.getActiveFlag());
		cacheDTO.setId(busDTO.getId());
		cacheDTO.setCode(busDTO.getCode());
		cacheDTO.setName(busDTO.getName());
		cacheDTO.setDisplayName(busDTO.getDisplayName());
		cacheDTO.setCategoryCode(busDTO.getCategoryCode());
		cacheDTO.setSeatCount(busDTO.getSeatCount());
		List<BusSeatLayoutCacheDTO> busSeatLayoutCacheDTOList = new ArrayList<>();
		if (busDTO.getBusSeatLayoutDTO() != null && busDTO.getBusSeatLayoutDTO().getList() != null && !busDTO.getBusSeatLayoutDTO().getList().isEmpty()) {
			for (BusSeatLayoutDTO layoutDTO : busDTO.getBusSeatLayoutDTO().getList()) {
				BusSeatLayoutCacheDTO layoutCacheDTO = new BusSeatLayoutCacheDTO();
				layoutCacheDTO.setActiveFlag(layoutDTO.getActiveFlag());
				layoutCacheDTO.setName(layoutDTO.getName());
				layoutCacheDTO.setCode(layoutDTO.getCode());
				layoutCacheDTO.setColPos(layoutDTO.getColPos());
				layoutCacheDTO.setRowPos(layoutDTO.getRowPos());
				layoutCacheDTO.setLayer(layoutDTO.getLayer());
				layoutCacheDTO.setOrientation(layoutDTO.getOrientation());
				layoutCacheDTO.setId(layoutDTO.getId());
				layoutCacheDTO.setSequence(layoutDTO.getSequence());
				layoutCacheDTO.setBusSeatTypeCode(layoutDTO.getBusSeatType().getCode());
				busSeatLayoutCacheDTOList.add(layoutCacheDTO);
			}
			cacheDTO.setBusSeatLayoutDTO(busSeatLayoutCacheDTOList);
		}
		return cacheDTO;
	}

	protected void bindBusFromCacheObject(BusCacheDTO cacheDTO, BusDTO busDTO) {
		busDTO.setId(cacheDTO.getId());
		busDTO.setActiveFlag(cacheDTO.getActiveFlag());
		busDTO.setCode(cacheDTO.getCode());
		busDTO.setName(cacheDTO.getName());
		busDTO.setDisplayName(cacheDTO.getDisplayName());
		busDTO.setCategoryCode(cacheDTO.getCategoryCode());
		busDTO.setSeatCount(cacheDTO.getSeatCount());
		List<BusSeatLayoutDTO> busSeatLayoutList = new ArrayList<>();
		if (cacheDTO.getBusSeatLayoutDTO() != null && !cacheDTO.getBusSeatLayoutDTO().isEmpty()) {
			for (BusSeatLayoutCacheDTO layoutCacheDTO : cacheDTO.getBusSeatLayoutDTO()) {
				BusSeatLayoutDTO layoutDTO = new BusSeatLayoutDTO();
				layoutDTO.setActiveFlag(layoutCacheDTO.getActiveFlag());
				layoutDTO.setColPos(layoutCacheDTO.getColPos());
				layoutDTO.setCode(layoutCacheDTO.getCode());
				layoutDTO.setName(layoutCacheDTO.getName());
				layoutDTO.setId(layoutCacheDTO.getId());
				layoutDTO.setSequence(layoutCacheDTO.getSequence());
				layoutDTO.setRowPos(layoutCacheDTO.getRowPos());
				layoutDTO.setOrientation(layoutCacheDTO.getOrientation());
				layoutDTO.setLayer(layoutCacheDTO.getLayer());
				layoutDTO.setBusSeatType(BusSeatTypeEM.getBusSeatTypeEM(layoutCacheDTO.getBusSeatTypeCode()));
				busSeatLayoutList.add(layoutDTO);
			}
			BusSeatLayoutDTO layoutDTO = new BusSeatLayoutDTO();
			layoutDTO.setList(busSeatLayoutList);
			busDTO.setBusSeatLayoutDTO(layoutDTO);
		}
	}
}
