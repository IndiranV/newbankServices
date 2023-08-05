package org.in.com.cache;

import java.util.ArrayList;
import java.util.List;

import org.in.com.cache.dto.DiscountSpecialCriteriaCacheDTO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.DiscountSpecialCriteriaDTO;

import net.sf.ehcache.Element;

public class DiscountSpecialCriteriaCache {
	private static String SPECIAL_DISCOUNT_KEY = "ALL_SPECIAL_DISCOUNT_KEY";

	protected void removeSpecialDiscountCriteria(AuthDTO authDTO) {
		String key = SPECIAL_DISCOUNT_KEY + "_" + authDTO.getNamespace().getCode();
		EhcacheManager.getSpecialDiscountCriteriaEhCache().remove(key);
	}

	protected List<DiscountSpecialCriteriaDTO> getSpecialDiscountCriteria(AuthDTO authDTO) {
		List<DiscountSpecialCriteriaDTO> discountList = null;
		String key = SPECIAL_DISCOUNT_KEY + "_" + authDTO.getNamespace().getCode();
		Element discountElement = EhcacheManager.getSpecialDiscountCriteriaEhCache().get(key);
		if (discountElement != null) {
			List<DiscountSpecialCriteriaCacheDTO> discountCacheList = (List<DiscountSpecialCriteriaCacheDTO>) discountElement.getObjectValue();
			discountList = bindSpecialDiscountCriteriaFromCacheObject(discountCacheList);
		}
		return discountList;

	}

	protected void putSpecialDiscountCriteria(AuthDTO authDTO, List<DiscountSpecialCriteriaDTO> discountList) {
		String key = SPECIAL_DISCOUNT_KEY + "_" + authDTO.getNamespace().getCode();
		List<DiscountSpecialCriteriaCacheDTO> discountCacheList = bindSpecialDiscountCriteriaToCacheObject(discountList);
		EhcacheManager.getSpecialDiscountCriteriaEhCache().put(new Element(key, discountCacheList));
	}

	private List<DiscountSpecialCriteriaCacheDTO> bindSpecialDiscountCriteriaToCacheObject(List<DiscountSpecialCriteriaDTO> discountList) {
		List<DiscountSpecialCriteriaCacheDTO> list = new ArrayList<>();
		if (discountList != null && !discountList.isEmpty()) {
			for (DiscountSpecialCriteriaDTO dto : discountList) {
				DiscountSpecialCriteriaCacheDTO cacheDTO = new DiscountSpecialCriteriaCacheDTO();
				cacheDTO.setCode(dto.getCode());
				List<GroupDTO> groupList = new ArrayList<>();
				for (GroupDTO group : dto.getUserGroups()) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(group.getId());
					groupList.add(groupDTO);
				}
				cacheDTO.setUserGroups(groupList);
				List<ScheduleDTO> scheduleList = new ArrayList<>();
				for (ScheduleDTO schedule : dto.getSchedules()) {
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setId(schedule.getId());
					scheduleList.add(scheduleDTO);
				}
				cacheDTO.setSchedules(scheduleList);
				cacheDTO.setMaxAmount(dto.getMaxAmount());
				cacheDTO.setPercentageFlag(dto.isPercentageFlag());
				cacheDTO.setActiveFlag(dto.getActiveFlag());
				list.add(cacheDTO);
			}
		}
		return list;
	}

	private List<DiscountSpecialCriteriaDTO> bindSpecialDiscountCriteriaFromCacheObject(List<DiscountSpecialCriteriaCacheDTO> discountCacheList) {
		List<DiscountSpecialCriteriaDTO> list = new ArrayList<>();
		if (discountCacheList != null && !discountCacheList.isEmpty()) {
			for (DiscountSpecialCriteriaCacheDTO cacheDTO : discountCacheList) {
				DiscountSpecialCriteriaDTO dto = new DiscountSpecialCriteriaDTO();
				dto.setCode(cacheDTO.getCode());
				List<GroupDTO> groupList = new ArrayList<>();
				for (GroupDTO group : cacheDTO.getUserGroups()) {
					GroupDTO groupDTO = new GroupDTO();
					groupDTO.setId(group.getId());
					groupList.add(groupDTO);
				}
				dto.setUserGroups(groupList);
				List<ScheduleDTO> scheduleList = new ArrayList<>();
				for (ScheduleDTO schedule : cacheDTO.getSchedules()) {
					ScheduleDTO scheduleDTO = new ScheduleDTO();
					scheduleDTO.setId(schedule.getId());
					scheduleList.add(scheduleDTO);
				}
				dto.setSchedules(scheduleList);
				dto.setMaxAmount(cacheDTO.getMaxAmount());
				dto.setPercentageFlag(cacheDTO.isPercentageFlag());
				dto.setActiveFlag(cacheDTO.getActiveFlag());
				list.add(dto);
			}
		}
		return list;
	}
}
