package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.builder.CompareToBuilder;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleBusOverrideCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.dao.ScheduleBusOverrideDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.NamespaceTaxDTO;
import org.in.com.dto.ScheduleBusOverrideDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.exception.ErrorCode;
import org.in.com.exception.ServiceException;
import org.in.com.service.FareRuleService;
import org.in.com.service.ScheduleBusOverrideService;
import org.in.com.service.ScheduleDynamicStageFareService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

@Service
public class ScheduleBusOverrideServiceImpl extends ScheduleCache implements ScheduleBusOverrideService {
	public static String CACHE_KEY = "SCH_BUS_OR";
	@Lazy
	@Autowired
	FareRuleService fareRuleService;
	@Autowired
	ScheduleDynamicStageFareService dynamicStageFareService;

	@Override
	public void updateScheduleBusOverride(AuthDTO authDTO, ScheduleBusOverrideDTO scheduleBusOverride) {
		ScheduleBusOverrideDAO scheduleBusOverrideDAO = new ScheduleBusOverrideDAO();
		scheduleBusOverrideDAO.updateScheduleBusOverride(authDTO, scheduleBusOverride);
		String key = CACHE_KEY + scheduleBusOverride.getSchedule().getCode();

		EhcacheManager.getScheduleEhCache().remove(key);
		if (scheduleBusOverride.getActiveFlag() == 1 && !authDTO.getNamespace().getProfile().getFareRule().isEmpty()) {
			fareRuleService.applyScheduleBusOverrideInQuickFare(authDTO, authDTO.getNamespace().getProfile().getFareRule(), scheduleBusOverride);
		}
		if (scheduleBusOverride.getActiveFlag() == 1) {
			dynamicStageFareService.notifyBusTypeChange(authDTO, scheduleBusOverride.getSchedule(), scheduleBusOverride);
		}
	}

	@Override
	public ScheduleBusOverrideDTO getBusOverrideBySchedule(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleBusOverrideDTO> scheduleBusOverrideList = getBusOverrideByScheduleV2(authDTO, scheduleDTO);
		ScheduleBusOverrideDTO scheduleBusOverrideDTO = null;
		// Sorting
		Collections.sort(scheduleBusOverrideList, new Comparator<ScheduleBusOverrideDTO>() {
			@Override
			public int compare(ScheduleBusOverrideDTO t1, ScheduleBusOverrideDTO t2) {
				return new CompareToBuilder().append(t2.getActiveFrom(), t1.getActiveFrom()).append(t2.getActiveTo(), t1.getActiveTo()).toComparison();
			}
		});

		if (!scheduleBusOverrideList.isEmpty()) {
			scheduleBusOverrideDTO = scheduleBusOverrideList.get(Numeric.ZERO_INT);
			scheduleBusOverrideDTO.setBus(getBusDTObyId(authDTO, scheduleBusOverrideDTO.getBus()));
		}
		return scheduleBusOverrideDTO;
	}

	@Override
	public List<ScheduleBusOverrideDTO> getBusOverrideByScheduleV2(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleBusOverrideDTO> scheduleBusOverrideList = getBusOverrides(authDTO, scheduleDTO);

		for (Iterator<ScheduleBusOverrideDTO> iterator = scheduleBusOverrideList.iterator(); iterator.hasNext();) {
			ScheduleBusOverrideDTO scheduleBusOverride = iterator.next();
			if (!DateUtil.isDateExist(scheduleBusOverride.getTripDateTimes(), scheduleDTO.getTripDate())) {
				iterator.remove();
				continue;
			}

			for (Iterator<ScheduleBusOverrideDTO> overrideIterator = scheduleBusOverride.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleBusOverrideDTO override = overrideIterator.next();
				if (!DateUtil.isDateExist(override.getTripDateTimes(), scheduleDTO.getTripDate())) {
					iterator.remove();
					continue;
				}

				// Apply Exceptions
				if (override.getBus().getId() == 0 && StringUtil.isNull(override.getBus().getCategoryCode())) {
					iterator.remove();
					break;
				}
			}
		}
		return scheduleBusOverrideList;
	}

	@Override
	public List<ScheduleBusOverrideDTO> getUpcomingBusOverrides(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		List<ScheduleBusOverrideDTO> scheduleBusOverrideList = getBusOverrides(authDTO, scheduleDTO);

		for (Iterator<ScheduleBusOverrideDTO> iterator = scheduleBusOverrideList.iterator(); iterator.hasNext();) {
			ScheduleBusOverrideDTO scheduleBusOverride = iterator.next();

			// common validations
			DateTime activeTo = scheduleBusOverride.getActiveToDateTime();
			if (activeTo == null || (activeTo != null && !scheduleDTO.getTripDate().lteq(activeTo))) {
				iterator.remove();
				continue;
			}

			for (Iterator<ScheduleBusOverrideDTO> overrideIterator = scheduleBusOverride.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleBusOverrideDTO override = overrideIterator.next();

				// common validations
				DateTime overrideActiveTo = override.getActiveToDateTime();
				if (overrideActiveTo != null && !scheduleDTO.getTripDate().lteq(overrideActiveTo)) {
					iterator.remove();
					continue;
				}

				// Apply Exceptions
				if (override.getBus().getId() == 0 && StringUtil.isNull(override.getBus().getCategoryCode())) {
					iterator.remove();
					break;
				}
			}
		}
		return scheduleBusOverrideList;
	}

	@Override
	public ScheduleBusOverrideDTO getScheduleBusOverride(AuthDTO authDTO, ScheduleDTO scheduleDTO, DateTime fromDate, DateTime toDate) {
		List<ScheduleBusOverrideDTO> scheduleBusOverrideList = getBusOverrides(authDTO, scheduleDTO);

		ScheduleBusOverrideDTO conflictBusOverride = null;
		for (Iterator<ScheduleBusOverrideDTO> iterator = scheduleBusOverrideList.iterator(); iterator.hasNext();) {
			ScheduleBusOverrideDTO scheduleBusOverride = iterator.next();

			DateTime activeFrom = scheduleBusOverride.getActiveFromDateTime();
			DateTime activeTo = scheduleBusOverride.getActiveToDateTime();

			// common validations
			if (activeFrom != null && !fromDate.gteq(activeFrom)) {
				iterator.remove();
				continue;
			}
			if (activeTo != null && fromDate.gt(activeTo)) {
				iterator.remove();
				continue;
			}
			if (activeTo != null && !toDate.lteq(activeTo)) {
				conflictBusOverride = scheduleBusOverride;
				break;
			}
			for (Iterator<ScheduleBusOverrideDTO> overrideIterator = scheduleBusOverride.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleBusOverrideDTO override = overrideIterator.next();

				DateTime overrideActiveFrom = override.getActiveFromDateTime();
				DateTime overrideActiveTo = override.getActiveToDateTime();

				// common validations
				if (overrideActiveFrom != null && !fromDate.gteq(overrideActiveFrom)) {
					iterator.remove();
					continue;
				}
				if (overrideActiveTo != null && !toDate.lteq(overrideActiveTo)) {
					iterator.remove();
					continue;
				}

				// Apply Exceptions
				if (override.getBus().getId() == 0 && StringUtil.isNull(override.getBus().getCategoryCode())) {
					iterator.remove();
					break;
				}
			}
		}

		if (conflictBusOverride != null) {
			throw new ServiceException(ErrorCode.BUSMAP_MISSED_MATCHED, "Bus override conflicted, date range is " + conflictBusOverride.getActiveFrom() + " to " + conflictBusOverride.getActiveTo());
		}
		else if (scheduleBusOverrideList.size() > 1) {
			throw new ServiceException(ErrorCode.BUSMAP_MISSED_MATCHED, "Bus override conflicted, multiple overrides added!");
		}

		return !scheduleBusOverrideList.isEmpty() ? scheduleBusOverrideList.get(0) : null;
	}

	private List<ScheduleBusOverrideDTO> getBusOverrides(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String key = CACHE_KEY + scheduleDTO.getCode();
		List<ScheduleBusOverrideDTO> scheduleBusOverrideList = new ArrayList<ScheduleBusOverrideDTO>();
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleBusOverrideCacheDTO> scheduleBusOverrideCacheList = (List<ScheduleBusOverrideCacheDTO>) element.getObjectValue();
			scheduleBusOverrideList = bindScheduleBusOverrideFromCache(scheduleBusOverrideCacheList);
		}
		else {
			ScheduleBusOverrideDAO scheduleBusOverrideDAO = new ScheduleBusOverrideDAO();
			scheduleBusOverrideList = scheduleBusOverrideDAO.getBusOverrideBySchedule(authDTO, scheduleDTO);
			List<ScheduleBusOverrideCacheDTO> scheduleBusOverrideCacheList = bindScheduleBusOverrideToCache(scheduleBusOverrideList);
			element = new Element(key, scheduleBusOverrideCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		return scheduleBusOverrideList;
	}

	public List<ScheduleBusOverrideCacheDTO> bindScheduleBusOverrideToCache(List<ScheduleBusOverrideDTO> list) {
		List<ScheduleBusOverrideCacheDTO> scheduleBusOverrideCacheList = new ArrayList<ScheduleBusOverrideCacheDTO>();
		for (ScheduleBusOverrideDTO scheduleBusOverrideDTO : list) {
			ScheduleBusOverrideCacheDTO scheduleBusOverrideCache = new ScheduleBusOverrideCacheDTO();
			scheduleBusOverrideCache.setCode(scheduleBusOverrideDTO.getCode());
			scheduleBusOverrideCache.setActiveFrom(scheduleBusOverrideDTO.getActiveFrom());
			scheduleBusOverrideCache.setActiveTo(scheduleBusOverrideDTO.getActiveTo());
			scheduleBusOverrideCache.setDayOfWeek(scheduleBusOverrideDTO.getDayOfWeek());
			scheduleBusOverrideCache.setBusId(scheduleBusOverrideDTO.getBus().getId());
			scheduleBusOverrideCache.setTaxId(scheduleBusOverrideDTO.getTax().getId());
			scheduleBusOverrideCache.setCategoryCode(scheduleBusOverrideDTO.getBus().getCategoryCode());
			scheduleBusOverrideCache.setLookupCode(scheduleBusOverrideDTO.getLookupCode());
			List<String> tripDates = new ArrayList<>();
			if (scheduleBusOverrideDTO.getTripDates() != null) {
				for (String tripDate : scheduleBusOverrideDTO.getTripDates()) {
					tripDates.add(tripDate);
				}
			}
			scheduleBusOverrideCache.setTripDates(tripDates);

			if (scheduleBusOverrideDTO.getOverrideList() != null && !scheduleBusOverrideDTO.getOverrideList().isEmpty()) {
				List<ScheduleBusOverrideCacheDTO> overrideList = new ArrayList<ScheduleBusOverrideCacheDTO>();
				for (ScheduleBusOverrideDTO overrideDTO : scheduleBusOverrideDTO.getOverrideList()) {
					ScheduleBusOverrideCacheDTO scheduleBusOverride = new ScheduleBusOverrideCacheDTO();
					scheduleBusOverride.setCode(overrideDTO.getCode());
					scheduleBusOverride.setActiveFrom(overrideDTO.getActiveFrom());
					scheduleBusOverride.setActiveTo(overrideDTO.getActiveTo());
					scheduleBusOverride.setBusId(overrideDTO.getBus().getId());
					scheduleBusOverride.setTaxId(overrideDTO.getTax().getId());
					scheduleBusOverride.setCategoryCode(overrideDTO.getBus().getCategoryCode());
					overrideList.add(scheduleBusOverride);
				}
				scheduleBusOverrideCache.setOverrideList(overrideList);
			}
			scheduleBusOverrideCacheList.add(scheduleBusOverrideCache);
		}
		return scheduleBusOverrideCacheList;
	}

	public List<ScheduleBusOverrideDTO> bindScheduleBusOverrideFromCache(List<ScheduleBusOverrideCacheDTO> scheduleBusOverrideCacheList) {
		List<ScheduleBusOverrideDTO> list = new ArrayList<ScheduleBusOverrideDTO>();
		for (ScheduleBusOverrideCacheDTO scheduleBusOverrideCache : scheduleBusOverrideCacheList) {
			ScheduleBusOverrideDTO scheduleBusOverrideDTO = new ScheduleBusOverrideDTO();
			scheduleBusOverrideDTO.setCode(scheduleBusOverrideCache.getCode());
			scheduleBusOverrideDTO.setActiveFrom(scheduleBusOverrideCache.getActiveFrom());
			scheduleBusOverrideDTO.setActiveTo(scheduleBusOverrideCache.getActiveTo());
			scheduleBusOverrideDTO.setDayOfWeek(scheduleBusOverrideCache.getDayOfWeek());
			scheduleBusOverrideDTO.setLookupCode(scheduleBusOverrideCache.getLookupCode());
			List<String> tripDates = new ArrayList<>();
			if (scheduleBusOverrideCache.getTripDates() != null) {
				for (String tripDate : scheduleBusOverrideCache.getTripDates()) {
					tripDates.add(tripDate);
				}
			}
			scheduleBusOverrideDTO.setTripDates(tripDates);

			BusDTO busDTO = new BusDTO();
			busDTO.setId(scheduleBusOverrideCache.getBusId());
			busDTO.setCategoryCode(scheduleBusOverrideCache.getCategoryCode());
			scheduleBusOverrideDTO.setBus(busDTO);

			NamespaceTaxDTO taxDTO = new NamespaceTaxDTO();
			taxDTO.setId(scheduleBusOverrideCache.getTaxId());
			scheduleBusOverrideDTO.setTax(taxDTO);

			if (scheduleBusOverrideCache.getOverrideList() != null && !scheduleBusOverrideCache.getOverrideList().isEmpty()) {
				List<ScheduleBusOverrideDTO> overrideList = new ArrayList<ScheduleBusOverrideDTO>();
				for (ScheduleBusOverrideCacheDTO overrideDTO : scheduleBusOverrideCache.getOverrideList()) {
					ScheduleBusOverrideDTO scheduleBusOverride = new ScheduleBusOverrideDTO();
					scheduleBusOverride.setCode(overrideDTO.getCode());
					scheduleBusOverride.setActiveFrom(overrideDTO.getActiveFrom());
					scheduleBusOverride.setActiveTo(overrideDTO.getActiveTo());
					scheduleBusOverride.setDayOfWeek(overrideDTO.getDayOfWeek());

					BusDTO bus = new BusDTO();
					bus.setId(overrideDTO.getBusId());
					bus.setCategoryCode(overrideDTO.getCategoryCode());
					scheduleBusOverride.setBus(bus);

					NamespaceTaxDTO taxOverrideDTO = new NamespaceTaxDTO();
					taxOverrideDTO.setId(overrideDTO.getTaxId());
					scheduleBusOverride.setTax(taxOverrideDTO);

					overrideList.add(scheduleBusOverride);
				}
				scheduleBusOverrideDTO.setOverrideList(overrideList);
			}
			list.add(scheduleBusOverrideDTO);
		}
		return list;
	}

	@Override
	public List<ScheduleBusOverrideDTO> getScheduleBusOverride(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		ScheduleBusOverrideDAO scheduleBusOverrideDAO = new ScheduleBusOverrideDAO();
		List<ScheduleBusOverrideDTO> list = scheduleBusOverrideDAO.getBusOverrideBySchedule(authDTO, scheduleDTO);
		for (ScheduleBusOverrideDTO scheduleBusOverrideDTO : list) {
			BusDTO busDTO = new BusDTO();
			if (scheduleBusOverrideDTO.getBus().getId() > Numeric.ZERO_INT) {
				busDTO.setId(scheduleBusOverrideDTO.getBus().getId());
				busDTO = getBusDTObyId(authDTO, busDTO);
			}
			if (StringUtil.isNotNull(scheduleBusOverrideDTO.getBus().getCategoryCode())) {
				busDTO.setCategoryCode(scheduleBusOverrideDTO.getBus().getCategoryCode());
			}
			scheduleBusOverrideDTO.setBus(busDTO);
			if (scheduleBusOverrideDTO.getTax() != null && scheduleBusOverrideDTO.getTax().getId() != Numeric.ZERO_INT) {
				scheduleBusOverrideDTO.setTax(getNamespaceTaxbyId(authDTO, scheduleBusOverrideDTO.getTax()));
			}
		}
		return list;
	}

	@Override
	public BusDTO applyScheduleBusOverride(AuthDTO authDTO, ScheduleDTO scheduleDTO, BusDTO regularBusDTO) {
		ScheduleBusOverrideDTO scheduleBusOverride = getBusOverrideBySchedule(authDTO, scheduleDTO);
		if (scheduleBusOverride != null) {
			if (scheduleBusOverride.getBus() != null && scheduleBusOverride.getBus().getId() != 0) {
				regularBusDTO = scheduleBusOverride.getBus();
			}
			else if (scheduleBusOverride.getBus() != null && StringUtil.isNotNull(scheduleBusOverride.getBus().getCategoryCode())) {
				regularBusDTO.setCategoryCode(getBusCategoryCode(scheduleBusOverride.getBus().getCategoryCode(), scheduleBusOverride.getBus().getCategoryCode()));
			}
			/** Copy Schedule Bus Tax Into Schedule */
			scheduleDTO.setTax(scheduleBusOverride.getTax());
		}
		return regularBusDTO;
	}

	private String getBusCategoryCode(String categoryCode, String overrideCategoryCode) {
		StringBuilder busType = new StringBuilder();
		int index = 0;
		for (String code : categoryCode.split("\\|")) {
			if (StringUtil.isNotNull(code) && index != 1) {
				busType.append(code);
			}
			if (StringUtil.isNotNull(code) && index == 1) {
				busType.append(overrideCategoryCode);
			}
			index = index + 1;

			if (index < categoryCode.split("\\|").length) {
				busType.append("|");
			}
		}
		return busType.toString();

	}

	public Map<String, ScheduleDTO> applyBusOverrideV2(AuthDTO authDTO, BusDTO busDTO) {
		Map<String, ScheduleDTO> scheduleMap = new HashMap<String, ScheduleDTO>();
		ScheduleBusOverrideDAO scheduleBusOverrideDAO = new ScheduleBusOverrideDAO();
		List<ScheduleBusOverrideDTO> scheduleBusOverrideList = scheduleBusOverrideDAO.getBusOverrideByBus(authDTO, busDTO);
		DateTime now = DateUtil.NOW();
		for (Iterator<ScheduleBusOverrideDTO> iterator = scheduleBusOverrideList.iterator(); iterator.hasNext();) {
			ScheduleBusOverrideDTO scheduleBusOverride = iterator.next();

			// common validations
			if (!DateUtil.isDateExist(scheduleBusOverride.getTripDateTimes(), now)) {
				iterator.remove();
				continue;
			}

			for (Iterator<ScheduleBusOverrideDTO> overrideIterator = scheduleBusOverride.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleBusOverrideDTO override = overrideIterator.next();

				// common validations
				if (!DateUtil.isDateExist(override.getTripDateTimes(), now)) {
					iterator.remove();
					continue;
				}
			}
			scheduleMap.put(scheduleBusOverride.getSchedule().getCode(), scheduleBusOverride.getSchedule());
		}
		return scheduleMap;
	}
}
