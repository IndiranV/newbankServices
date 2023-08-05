package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.in.com.cache.BusCache;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleBusCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.dao.ScheduleBusDAO;
import org.in.com.dto.AmenitiesDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusBreakevenSettingsDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.ScheduleBusDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.BusBreakevenService;
import org.in.com.service.FareRuleService;
import org.in.com.service.ScheduleActivityService;
import org.in.com.service.ScheduleBusOverrideService;
import org.in.com.service.ScheduleBusService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import net.sf.ehcache.Element;

@Service
public class ScheduleBusImpl extends BusCache implements ScheduleBusService {
	@Autowired
	ScheduleActivityService scheduleActivityService;
	@Autowired
	ScheduleBusOverrideService busOverrideService;
	@Autowired
	BusBreakevenService busBreakevenService;
	@Lazy
	@Autowired
	FareRuleService fareRuleService;

	public List<ScheduleBusDTO> get(AuthDTO authDTO, ScheduleBusDTO dto) {
		ScheduleBusDAO busDAO = new ScheduleBusDAO();
		List<ScheduleBusDTO> list = busDAO.get(authDTO, dto);
		for (ScheduleBusDTO busDTO : list) {
			if (busDTO.getAmentiesList() != null && !busDTO.getAmentiesList().isEmpty()) {
				for (AmenitiesDTO amentiesDTO : busDTO.getAmentiesList()) {
					AmenitiesDTO amenities = getAmenitiesDTO(amentiesDTO.getCode());
					if (amenities != null) {
						amentiesDTO.setName(amenities.getName());
						amentiesDTO.setCode(amenities.getCode());
					}
				}
			}
			busDTO.setBus(getBusDTObyId(authDTO, busDTO.getBus()));
			if (busDTO.getTax().getId() != Numeric.ZERO_INT) {
				busDTO.setTax(getNamespaceTaxbyId(authDTO, busDTO.getTax()));
			}
			if (busDTO.getBreakevenSettings().getId() != Numeric.ZERO_INT) {
				busDTO.setBreakevenSettings(busBreakevenService.getBreakeven(authDTO, busDTO.getBreakevenSettings()));
			}
		}
		return list;
	}

	public ScheduleBusDTO Update(AuthDTO authDTO, ScheduleBusDTO scheduleBusDTO) {
		ScheduleBusDAO busDAO = new ScheduleBusDAO();
		ScheduleCache scheduleCache = new ScheduleCache();
		ScheduleDTO scheduleDTO = scheduleCache.getScheduleDTO(authDTO, scheduleBusDTO.getSchedule());
		ScheduleBusDTO busDTO = busDAO.getByScheduleId(authDTO, scheduleDTO);
		if (scheduleBusDTO.getBreakevenSettings() != null) {
			busBreakevenService.getBreakeven(authDTO, scheduleBusDTO.getBreakevenSettings());
		}
		if (busDTO != null && !getBusDTO(authDTO, busDTO.getBus()).getCode().equals(scheduleBusDTO.getBus().getCode())) {
			int exists = busDAO.checkScheduleBusChange(authDTO, scheduleDTO);
			if (exists != 0) {
				throw new ServiceException(ErrorCode.SCHEDULE_BUSMAP_USED_TICKET);
			}
			// Activity Activity Log
			scheduleActivityService.scheduleBusActivity(authDTO, scheduleBusDTO);

			busDAO.getIUD(authDTO, scheduleBusDTO);
			scheduleCache.removeScheduleDTO(authDTO, scheduleBusDTO.getSchedule());

			// Validate Schedule route & fare based on fare rule
			if (scheduleBusDTO.getActiveFlag() == 1 && !authDTO.getNamespace().getProfile().getFareRule().isEmpty()) {
				fareRuleService.applyChangeOfScheduleBusInStages(authDTO, authDTO.getNamespace().getProfile().getFareRule(), scheduleDTO);
			}
		}
		else if (busDTO != null && getBusDTO(authDTO, busDTO.getBus()).getCode().equals(scheduleBusDTO.getBus().getCode()) && StringUtil.isNotNull(scheduleBusDTO.getCode())) {
			// Activity Activity Log
			scheduleActivityService.scheduleBusActivity(authDTO, scheduleBusDTO);

			busDAO.getIUD(authDTO, scheduleBusDTO);
			scheduleCache.removeScheduleDTO(authDTO, scheduleBusDTO.getSchedule());
		}
		else if (busDTO == null) {
			// Activity Activity Log
			scheduleActivityService.scheduleBusActivity(authDTO, scheduleBusDTO);

			busDAO.getIUD(authDTO, scheduleBusDTO);
			scheduleCache.removeScheduleDTO(authDTO, scheduleBusDTO.getSchedule());
		}

		return scheduleBusDTO;
	}

	public ScheduleBusDTO getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
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
		if (busDTO != null && busDTO.getBus() != null && busDTO.getBus().getId() != 0) {
			busDTO.setBus(getBusDTObyId(authDTO, busDTO.getBus()));
		}
		if (busDTO != null && busDTO.getTax() != null && busDTO.getTax().getId() != 0) {
			busDTO.setTax(getNamespaceTaxbyId(authDTO, busDTO.getTax()));
		}
		return busDTO;

	}

	private ScheduleBusCacheDTO bindScheduleBusToCacheObject(ScheduleBusDTO busDTO) {
		ScheduleBusCacheDTO busCacheDTO = new ScheduleBusCacheDTO();
		busCacheDTO.setActiveFlag(busDTO.getActiveFlag());
		busCacheDTO.setBusId(busDTO.getBus().getId());
		busCacheDTO.setTaxId(busDTO.getTax().getId());
		busCacheDTO.setBreakevenId(busDTO.getBreakevenSettings().getId());
		busCacheDTO.setDistance(busDTO.getDistance());

		List<String> amentiesList = new ArrayList<>();
		if (busDTO.getAmentiesList() != null) {
			for (AmenitiesDTO amenitiesDTO : busDTO.getAmentiesList()) {
				amentiesList.add(amenitiesDTO.getCode());
			}
		}
		busCacheDTO.setAmentiesList(amentiesList);
		return busCacheDTO;
	}

	private ScheduleBusDTO bindScheduleBusFromCacheObject(ScheduleBusCacheDTO scheduleBusCacheDTO) {
		ScheduleBusDTO scheduleBusDTO = new ScheduleBusDTO();
		scheduleBusDTO.setActiveFlag(scheduleBusCacheDTO.getActiveFlag());
		BusDTO busDTO = new BusDTO();
		busDTO.setId(scheduleBusCacheDTO.getBusId());
		scheduleBusDTO.setBus(busDTO);

		NamespaceTaxDTO taxDTO = new NamespaceTaxDTO();
		taxDTO.setId(scheduleBusCacheDTO.getTaxId());
		scheduleBusDTO.setTax(taxDTO);

		BusBreakevenSettingsDTO breakevenSettings = new BusBreakevenSettingsDTO();
		breakevenSettings.setId(scheduleBusCacheDTO.getBreakevenId());
		scheduleBusDTO.setBreakevenSettings(breakevenSettings);
		scheduleBusDTO.setDistance(scheduleBusCacheDTO.getDistance());

		List<AmenitiesDTO> amentiesList = new ArrayList<AmenitiesDTO>();
		for (String amenties : scheduleBusCacheDTO.getAmentiesList()) {
			AmenitiesDTO amenitiesDTO = new AmenitiesDTO();
			amenitiesDTO.setCode(amenties);
			amentiesList.add(amenitiesDTO);
		}
		scheduleBusDTO.setAmentiesList(amentiesList);
		return scheduleBusDTO;
	}

	@Override
	public void checkScheduleBusmapChange(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleBusDAO busDAO = new ScheduleBusDAO();
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleDTO = scheduleCache.getScheduleDTO(authDTO, scheduleDTO);
		int exists = busDAO.checkScheduleBusChange(authDTO, scheduleDTO);
		if (exists != 0) {
			throw new ServiceException(ErrorCode.SCHEDULE_BUSMAP_USED_TICKET);
		}
	}

	@Override
	public List<ScheduleDTO> getScheduleByBus(AuthDTO authDTO, BusDTO busDTO) {
		ScheduleBusDAO busDAO = new ScheduleBusDAO();
		Map<String, ScheduleDTO> scheduleMap = busDAO.getScheduleByBus(authDTO, busDTO);
		Map<String, ScheduleDTO> overrideMap = busOverrideService.applyBusOverrideV2(authDTO, busDTO);
		scheduleMap.putAll(overrideMap);
		return new ArrayList<ScheduleDTO>(scheduleMap.values());
	}

	@Override
	public ScheduleBusDTO getActiveScheduleBus(AuthDTO authDTO, ScheduleDTO schedulRe) {
		// Schedule Bus
		ScheduleBusDTO scheduleBusDTO = getByScheduleId(authDTO, schedulRe);
		BusDTO busDTO = busOverrideService.applyScheduleBusOverride(authDTO, schedulRe, scheduleBusDTO.getBus());
		scheduleBusDTO.setBus(busDTO);
		return scheduleBusDTO;
	}
}
