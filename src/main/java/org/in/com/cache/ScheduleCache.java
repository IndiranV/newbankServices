package org.in.com.cache;

import java.util.List;

import org.in.com.cache.dto.ScheduleBusCacheDTO;
import org.in.com.cache.dto.ScheduleCacheDTO;
import org.in.com.cache.dto.ScheduleCancellationTermCacheDTO;
import org.in.com.cache.dto.ScheduleControlCacheDTO;
import org.in.com.cache.dto.ScheduleStageCacheDTO;
import org.in.com.cache.dto.ScheduleStationCacheDTO;
import org.in.com.cache.dto.ScheduleStationPointCacheDTO;
import org.in.com.cache.dto.ScheduleStationPointExceptionCacheDTO;
import org.in.com.cache.dto.ScheduleVirtualSeatBlockCacheDTO;
import org.in.com.constants.Text;
import org.in.com.dao.ScheduleBusDAO;
import org.in.com.dao.ScheduleCancellationTermDAO;
import org.in.com.dao.ScheduleControlDAO;
import org.in.com.dao.ScheduleDAO;
import org.in.com.dao.ScheduleStageDAO;
import org.in.com.dao.ScheduleStationDAO;
import org.in.com.dao.ScheduleStationPointDAO;
import org.in.com.dao.ScheduleVirtualSeatBlockDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleCancellationTermDTO;
import org.in.com.dto.ScheduleControlDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleStageDTO;
import org.in.com.dto.ScheduleStationDTO;
import org.in.com.dto.ScheduleStationPointDTO;
import org.in.com.dto.ScheduleVirtualSeatBlockDTO;
import org.in.com.utils.StringUtil;

import net.sf.ehcache.Element;

public class ScheduleCache extends ScheduleCacheHelper {
	private static String CACHEKEY = "SCHE";

	public ScheduleDTO getScheduleDTO(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleCacheDTO scheduleCacheDTO = null;
		Element element = EhcacheManager.getScheduleEhCache().get(scheduleDTO.getCode());
		if (element != null) {
			scheduleCacheDTO = (ScheduleCacheDTO) element.getObjectValue();
			bindScheduleFromCacheObject(scheduleCacheDTO, scheduleDTO);
		}
		else {
			ScheduleDAO scheduleDAO = new ScheduleDAO();
			scheduleDAO.get(authDTO, scheduleDTO);
			if (scheduleDTO != null && scheduleDTO.getId() != 0 && StringUtil.isNotNull(scheduleDTO.getCode())) {
				scheduleCacheDTO = new ScheduleCacheDTO();
				bindScheduleToCacheObject(scheduleCacheDTO, scheduleDTO);
				element = new Element(scheduleCacheDTO.getCode(), scheduleCacheDTO);
				EhcacheManager.getScheduleEhCache().put(element);
			}
		}
		return scheduleDTO;
	}

	public ScheduleDTO getScheduleDTObyId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String key = CACHEKEY + authDTO.getNamespace().getId() + "_" + scheduleDTO.getId();
		Element elementKey = EhcacheManager.getScheduleEhCache().get(key);
		if (elementKey != null) {
			String scheduleCode = (String) elementKey.getObjectValue();
			scheduleDTO.setCode(scheduleCode);
		}
		scheduleDTO = getScheduleDTO(authDTO, scheduleDTO);
		if (elementKey == null && StringUtil.isNotNull(scheduleDTO.getCode()) && scheduleDTO.getId() != 0) {
			key = CACHEKEY + authDTO.getNamespace().getId() + "_" + scheduleDTO.getId();
			elementKey = new Element(key, scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().put(elementKey);
		}
		return scheduleDTO;
	}

	public List<ScheduleControlDTO> getScheduleControlDTO(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String CACHEKEY = "SHCON" + scheduleDTO.getCode();
		List<ScheduleControlDTO> scheduleControlDTOList = null;
		List<ScheduleControlCacheDTO> scheduleControlCacheList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleControlCacheList = (List<ScheduleControlCacheDTO>) element.getObjectValue();
			scheduleControlDTOList = bindScheduleControlFromCacheObject(scheduleControlCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleControlDAO controlDAO = new ScheduleControlDAO();
			scheduleControlDTOList = controlDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule Cache
			scheduleControlCacheList = bindScheduleControlToCacheObject(scheduleControlDTOList);
			element = new Element(CACHEKEY, scheduleControlCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		else if (scheduleDTO != null) {
			EhcacheManager.getScheduleEhCache().remove(CACHEKEY);
		}
		return scheduleControlDTOList;
	}

	public List<ScheduleStationDTO> getScheduleStation(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String CACHEKEY = "SHSTN" + scheduleDTO.getCode();
		List<ScheduleStationDTO> stationDTOList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			List<ScheduleStationCacheDTO> scheduleStationCacheList = (List<ScheduleStationCacheDTO>) element.getObjectValue();
			stationDTOList = bindScheduleStationFromCacheObject(scheduleStationCacheList);
		}
		return stationDTOList;
	}

	public void putScheduleStation(AuthDTO authDTO, ScheduleDTO scheduleDTO, List<ScheduleStationDTO> stationDTOList) {
		String CACHEKEY = "SHSTN" + scheduleDTO.getCode();
		// Save to schedule Cache
		List<ScheduleStationCacheDTO> scheduleStationCacheList = bindScheduleStationToCacheObject(stationDTOList);
		Element element = new Element(CACHEKEY, scheduleStationCacheList);
		EhcacheManager.getScheduleEhCache().put(element);
	}

	public List<ScheduleStationDTO> getScheduleStationDTO(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String CACHEKEY = "SHSTN" + scheduleDTO.getCode();
		List<ScheduleStationDTO> stationDTOList = null;
		List<ScheduleStationCacheDTO> scheduleStationCacheList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleStationCacheList = (List<ScheduleStationCacheDTO>) element.getObjectValue();
			stationDTOList = bindScheduleStationFromCacheObject(scheduleStationCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleStationDAO StationDAO = new ScheduleStationDAO();
			stationDTOList = StationDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule Cache
			scheduleStationCacheList = bindScheduleStationToCacheObject(stationDTOList);
			element = new Element(CACHEKEY, scheduleStationCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		return stationDTOList;
	}

	public List<ScheduleStationPointDTO> getScheduleStationPoint(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String CACHEKEY = "SHSTPO" + scheduleDTO.getCode();
		List<ScheduleStationPointDTO> stationPointDTOList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			List<ScheduleStationPointCacheDTO> scheduleStationCacheList = (List<ScheduleStationPointCacheDTO>) element.getObjectValue();
			stationPointDTOList = bindScheduleStationPointFromCacheObject(scheduleStationCacheList);
		}
		return stationPointDTOList;

	}

	public void putScheduleStationPoint(AuthDTO authDTO, ScheduleDTO scheduleDTO, List<ScheduleStationPointDTO> stationPointDTOList) {
		String CACHEKEY = "SHSTPO" + scheduleDTO.getCode();
		// Save to schedule station Point Cache
		List<ScheduleStationPointCacheDTO> scheduleStationCacheList = bindScheduleStationPointToCacheObject(stationPointDTOList);
		Element element = new Element(CACHEKEY, scheduleStationCacheList);
		EhcacheManager.getScheduleEhCache().put(element);
	}

	public List<ScheduleStationPointDTO> getScheduleStationPointDTO(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String CACHEKEY = "SHSTPO" + scheduleDTO.getCode();
		List<ScheduleStationPointDTO> stationPointDTOList = null;
		List<ScheduleStationPointCacheDTO> scheduleStationCacheList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleStationCacheList = (List<ScheduleStationPointCacheDTO>) element.getObjectValue();
			stationPointDTOList = bindScheduleStationPointFromCacheObject(scheduleStationCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleStationPointDAO StationPointDAO = new ScheduleStationPointDAO();
			stationPointDTOList = StationPointDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule station Point Cache
			scheduleStationCacheList = bindScheduleStationPointToCacheObject(stationPointDTOList);
			element = new Element(CACHEKEY, scheduleStationCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		return stationPointDTOList;

	}

	public List<ScheduleStageDTO> getScheduleStageDTO(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String CACHEKEY = "SHSTG" + scheduleDTO.getCode();
		List<ScheduleStageDTO> scheduleStageDTOList = null;
		List<ScheduleStageCacheDTO> scheduleStationCacheList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleStationCacheList = (List<ScheduleStageCacheDTO>) element.getObjectValue();
			scheduleStageDTOList = bindScheduleStageFromCacheObject(scheduleStationCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleStageDAO scheduleStageDAO = new ScheduleStageDAO();
			scheduleStageDTOList = scheduleStageDAO.getByScheduleId(authDTO, scheduleDTO);

			// Save to schedule station Point Cache
			scheduleStationCacheList = bindScheduleStageToCacheObject(scheduleStageDTOList);
			element = new Element(CACHEKEY, scheduleStationCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		return scheduleStageDTOList;

	}

	public List<ScheduleCancellationTermDTO> getScheduleCancellationTermDTO(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String CACHEKEY = "SHSTCT" + scheduleDTO.getCode();
		List<ScheduleCancellationTermDTO> scheduleTermDTOList = null;
		List<ScheduleCancellationTermCacheDTO> scheduleTermCacheList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleTermCacheList = (List<ScheduleCancellationTermCacheDTO>) element.getObjectValue();
			scheduleTermDTOList = bindScheduleTermsFromCacheObject(scheduleTermCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleCancellationTermDAO termDAO = new ScheduleCancellationTermDAO();
			scheduleTermDTOList = termDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule station Point Cache
			scheduleTermCacheList = bindScheduleTermsToCacheObject(scheduleTermDTOList);
			element = new Element(CACHEKEY, scheduleTermCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		return scheduleTermDTOList;
	}

	public ScheduleBusDTO getScheduleBusDTO(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String CACHEKEY = "SHSBUS" + scheduleDTO.getCode();
		ScheduleBusDTO busDTO = null;
		ScheduleBusCacheDTO scheduleBusCacheDTO = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleBusCacheDTO = (ScheduleBusCacheDTO) element.getObjectValue();
			busDTO = bindScheduleBusFromCacheObject(scheduleBusCacheDTO);
		}
		else if (scheduleDTO != null && scheduleDTO.getId() != 0 && StringUtil.isNotNull(scheduleDTO.getCode())) {
			ScheduleBusDAO BusDAO = new ScheduleBusDAO();
			busDTO = BusDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule Bus Cache
			if (busDTO != null && busDTO.getId() != 0) {
				scheduleBusCacheDTO = bindScheduleBusToCacheObject(busDTO);
				element = new Element(CACHEKEY, scheduleBusCacheDTO);
				EhcacheManager.getScheduleEhCache().put(element);
			}
		}
		return busDTO;

	}

	public List<ScheduleStationPointDTO> getScheduleStationPointException(AuthDTO authDTO) {
		String CACHEKEY = authDTO.getNamespaceCode() + "_SHSTPOEX";
		List<ScheduleStationPointDTO> stationPointDTOList = null;
		List<ScheduleStationPointExceptionCacheDTO> scheduleStationPointExceptionCacheList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleStationPointExceptionCacheList = (List<ScheduleStationPointExceptionCacheDTO>) element.getObjectValue();
			stationPointDTOList = bindScheduleStationPointExceptionFromCacheObject(scheduleStationPointExceptionCacheList);
		}
		else {
			ScheduleStationPointDAO StationPointDAO = new ScheduleStationPointDAO();
			stationPointDTOList = StationPointDAO.getScheduleStationPointException(authDTO);
			// Save to schedule station Point Exception Cache
			scheduleStationPointExceptionCacheList = bindScheduleStationPointExceptionToCacheObject(stationPointDTOList);
			element = new Element(CACHEKEY, scheduleStationPointExceptionCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		return stationPointDTOList;

	}

	public List<ScheduleVirtualSeatBlockDTO> getScheduleVirtualSeatBlock(AuthDTO authDTO) {
		String CACHEKEY = authDTO.getNamespaceCode() + "_SHVISEBL";
		List<ScheduleVirtualSeatBlockDTO> scheduleVirtualSeatBlockList = null;
		List<ScheduleVirtualSeatBlockCacheDTO> scheduleVirtualSeatBlockCacheList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(CACHEKEY);
		if (element != null) {
			scheduleVirtualSeatBlockCacheList = (List<ScheduleVirtualSeatBlockCacheDTO>) element.getObjectValue();
			scheduleVirtualSeatBlockList = bindScheduleVirtualSeatBlockFromCacheObject(scheduleVirtualSeatBlockCacheList);
		}
		else {
			ScheduleVirtualSeatBlockDAO scheduleVirtualSeatBlockDAO = new ScheduleVirtualSeatBlockDAO();
			scheduleVirtualSeatBlockList = scheduleVirtualSeatBlockDAO.getScheduleVirtualSeatBlock(authDTO);
			scheduleVirtualSeatBlockCacheList = bindScheduleVirtualSeatBlockToCacheObject(scheduleVirtualSeatBlockList);
			element = new Element(CACHEKEY, scheduleVirtualSeatBlockCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		return scheduleVirtualSeatBlockList;

	}

	public void removeScheduleDTO(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		if (scheduleDTO != null && StringUtil.isNotNull(scheduleDTO.getCode())) {
			EhcacheManager.getScheduleEhCache().remove(scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().remove("SHSTN" + scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().remove("SHSTPO" + scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().remove("SHCON" + scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().remove("SHSTG" + scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().remove("SHSTCT" + scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().remove("SHSEVI" + scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().remove("SHSBUS" + scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().remove("SHSEPE" + scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().remove("SHSTFR" + scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().remove("SHFARR" + scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().remove("SHTOR" + scheduleDTO.getCode());
			EhcacheManager.getScheduleEhCache().remove("SHTSTOP" + scheduleDTO.getCode());
		}
		if (scheduleDTO != null && StringUtil.isNotNull(scheduleDTO.getLookupCode())) {
			EhcacheManager.getScheduleEhCache().remove(scheduleDTO.getLookupCode());
			EhcacheManager.getScheduleEhCache().remove("SHSTN" + scheduleDTO.getLookupCode());
			EhcacheManager.getScheduleEhCache().remove("SHSTPO" + scheduleDTO.getLookupCode());
			EhcacheManager.getScheduleEhCache().remove("SHCON" + scheduleDTO.getLookupCode());
			EhcacheManager.getScheduleEhCache().remove("SHSTG" + scheduleDTO.getLookupCode());
			EhcacheManager.getScheduleEhCache().remove("SHSTCT" + scheduleDTO.getLookupCode());
			EhcacheManager.getScheduleEhCache().remove("SHSEVI" + scheduleDTO.getLookupCode());
			EhcacheManager.getScheduleEhCache().remove("SHSBUS" + scheduleDTO.getLookupCode());
			EhcacheManager.getScheduleEhCache().remove("SHSEPE" + scheduleDTO.getLookupCode());
			EhcacheManager.getScheduleEhCache().remove("SHSTFR" + scheduleDTO.getLookupCode());
			EhcacheManager.getScheduleEhCache().remove("SHFARR" + scheduleDTO.getLookupCode());
			EhcacheManager.getScheduleEhCache().remove("SHTOR" + scheduleDTO.getLookupCode());
			EhcacheManager.getScheduleEhCache().remove("SHTSTOP" + scheduleDTO.getLookupCode());
		}
		String activeScheduleKey = "ACTIVE_SCHE_" + authDTO.getNamespaceCode() + Text.UNDER_SCORE;

		List<String> keys = EhcacheManager.getKeys(EhcacheManager.getActiveScheduleEhCache(), activeScheduleKey);
		if (!keys.isEmpty()) {
			EhcacheManager.getActiveScheduleEhCache().removeAll(keys);
		}
	}

	public void removeScheduleStationPointException(AuthDTO authDTO) {
		String CACHEKEY = authDTO.getNamespaceCode() + "_SHSTPOEX";
		EhcacheManager.getScheduleEhCache().remove(CACHEKEY);
	}

	public void removeScheduleVirtualSeatBlock(AuthDTO authDTO) {
		String CACHEKEY = authDTO.getNamespaceCode() + "_SHVISEBL";
		EhcacheManager.getScheduleEhCache().remove(CACHEKEY);
	}

}
