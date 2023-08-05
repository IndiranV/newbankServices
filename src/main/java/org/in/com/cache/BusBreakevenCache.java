package org.in.com.cache;

import org.in.com.cache.dto.BusBreakevenCacheDTO;
import org.in.com.dao.BusBreakevenDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusBreakevenSettingsDTO;
import org.in.com.dto.BusDTO;
import org.in.com.utils.StringUtil;

import net.sf.ehcache.Element;
import net.sf.json.JSONObject;

public class BusBreakevenCache {
	private static String CACHEKEY = "BRKEVN";

	public BusBreakevenSettingsDTO getBusBreakeven(AuthDTO authDTO, BusBreakevenSettingsDTO breakevenSettingsDTO) {
		Element element = EhcacheManager.getBusBreakevenEhCache().get(breakevenSettingsDTO.getCode());
		if (element != null) {
			BusBreakevenCacheDTO breakevenCacheDTO = (BusBreakevenCacheDTO) element.getObjectValue();
			bindBusBreakevenFromCacheObject(breakevenCacheDTO, breakevenSettingsDTO);
		}
		else {
			BusBreakevenDAO breakevenDAO = new BusBreakevenDAO();
			breakevenDAO.getBreakevenSettingsDetails(authDTO, breakevenSettingsDTO);
			if (StringUtil.isNotNull(breakevenSettingsDTO.getCode()) && breakevenSettingsDTO.getId() != 0) {
				BusBreakevenCacheDTO breakevenCacheDTO = bindBusBreakevenToCacheObject(breakevenSettingsDTO);
				// copy object
				element = new Element(breakevenCacheDTO.getCode(), breakevenCacheDTO);
				EhcacheManager.getBusBreakevenEhCache().put(element);
			}
		}
		return breakevenSettingsDTO;
	}

	public BusBreakevenSettingsDTO getBusBreakevenById(AuthDTO authDTO, BusBreakevenSettingsDTO breakevenSettingsDTO) {
		if (breakevenSettingsDTO != null && breakevenSettingsDTO.getId() != 0) {
			String breakevenCode = null;
			String key = CACHEKEY + "_" + breakevenSettingsDTO.getId();
			Element elementKey = EhcacheManager.getBusBreakevenEhCache().get(key);
			if (elementKey != null) {
				breakevenCode = (String) elementKey.getObjectValue();
				breakevenSettingsDTO.setCode(breakevenCode);
			}
			breakevenSettingsDTO = getBusBreakeven(authDTO, breakevenSettingsDTO);
			if (elementKey == null && StringUtil.isNotNull(breakevenSettingsDTO.getCode()) && breakevenSettingsDTO.getId() != 0) {
				key = CACHEKEY + "_" + breakevenSettingsDTO.getId();
				elementKey = new Element(key, breakevenSettingsDTO.getCode());
				EhcacheManager.getBusBreakevenEhCache().put(elementKey);
			}
		}
		return breakevenSettingsDTO;
	}

	public void removeBusBreakeven(AuthDTO authDTO, BusBreakevenSettingsDTO breakevenDTO) {
		EhcacheManager.getBusBreakevenEhCache().remove(breakevenDTO.getCode());
	}

	protected void bindBusBreakevenFromCacheObject(BusBreakevenCacheDTO breakevenCacheDTO, BusBreakevenSettingsDTO breakevenSettingsDTO) {
		breakevenSettingsDTO.setId(breakevenCacheDTO.getId());
		breakevenSettingsDTO.setCode(breakevenCacheDTO.getCode());
		breakevenSettingsDTO.setName(breakevenCacheDTO.getName());

		BusDTO bus = new BusDTO();
		bus.setId(breakevenCacheDTO.getBusId());
		breakevenSettingsDTO.setBus(bus);

		breakevenSettingsDTO.setBreakevenDetails(JSONObject.fromObject(breakevenCacheDTO.getBreakevenDetails()));
		breakevenSettingsDTO.setActiveFlag(breakevenCacheDTO.getActiveFlag());
	}

	protected BusBreakevenCacheDTO bindBusBreakevenToCacheObject(BusBreakevenSettingsDTO breakevenSettingsDTO) {
		BusBreakevenCacheDTO breakevenCacheDTO = new BusBreakevenCacheDTO();
		breakevenCacheDTO.setId(breakevenSettingsDTO.getId());
		breakevenCacheDTO.setCode(breakevenSettingsDTO.getCode());
		breakevenCacheDTO.setName(breakevenSettingsDTO.getName());
		breakevenCacheDTO.setBusId(breakevenSettingsDTO.getBus().getId());
		breakevenCacheDTO.setBreakevenDetails(breakevenSettingsDTO.getBreakeven());
		breakevenCacheDTO.setActiveFlag(breakevenSettingsDTO.getActiveFlag());
		return breakevenCacheDTO;
	}
}
