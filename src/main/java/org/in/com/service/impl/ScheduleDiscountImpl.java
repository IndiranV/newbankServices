package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleDiscountCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ScheduleDiscountDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleDiscountDTO;
import org.in.com.dto.enumeration.AuthenticationTypeEM;
import org.in.com.dto.enumeration.DateTypeEM;
import org.in.com.dto.enumeration.DeviceMediumEM;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleDiscountService;
import org.in.com.utils.DateUtil;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

@Service
public class ScheduleDiscountImpl extends ScheduleCache implements ScheduleDiscountService {
	@Autowired
	GroupService groupService;
	private static String CACHEKEY = "SHDIS";

	public List<ScheduleDiscountDTO> get(AuthDTO authDTO) {

		ScheduleDiscountDAO discountDAO = new ScheduleDiscountDAO();
		List<ScheduleDiscountDTO> list = discountDAO.getScheduleDiscount(authDTO);
		for (Iterator<ScheduleDiscountDTO> iterator = list.iterator(); iterator.hasNext();) {
			ScheduleDiscountDTO discountDTO = iterator.next();

			for (ScheduleDTO scheduleDTO : discountDTO.getScheduleList()) {
				scheduleDTO = getScheduleDTO(authDTO, scheduleDTO);
			}
			for (GroupDTO group : discountDTO.getGroupList()) {
				GroupDTO groupCache = groupService.getGroup(authDTO, group);
				group.setName(groupCache.getName());
			}

			if (discountDTO.getOverrideList() != null) {
				for (ScheduleDiscountDTO overrideStationDTO : discountDTO.getOverrideList()) {
					for (ScheduleDTO scheduleDTO : overrideStationDTO.getScheduleList()) {
						scheduleDTO = getScheduleDTO(authDTO, scheduleDTO);
					}
					for (GroupDTO group : overrideStationDTO.getGroupList()) {
						GroupDTO groupCache = groupService.getGroup(authDTO, group);
						group.setName(groupCache.getName());
					}

				}
			}
		}
		return list;
	}

	public boolean Update(AuthDTO authDTO, ScheduleDiscountDTO discountDTO) {

		ScheduleDiscountDAO discountDAO = new ScheduleDiscountDAO();
		boolean status = discountDAO.getIUD(authDTO, discountDTO);

		String key = authDTO.getNamespaceCode() + Text.UNDER_SCORE + CACHEKEY;
		EhcacheManager.getScheduleEhCache().remove(key);
		return status;
	}

	public ScheduleDiscountDTO getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String key = authDTO.getNamespaceCode() + Text.UNDER_SCORE + CACHEKEY;
		List<ScheduleDiscountDTO> list = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleDiscountCacheDTO> cacheList = (List<ScheduleDiscountCacheDTO>) element.getObjectValue();
			list = bindDiscountFromCacheObject(cacheList);
		}
		else {
			ScheduleDiscountDAO discountDAO = new ScheduleDiscountDAO();
			list = discountDAO.getScheduleDiscount(authDTO);
			List<ScheduleDiscountCacheDTO> discountCacheDTOList = bindDiscountToCacheObject(list);
			element = new Element(key, discountCacheDTOList);
			EhcacheManager.getScheduleEhCache().put(element);
		}
		boolean foundAuthenticationType = false;
		int advanceBookingDays = DateUtil.getDayDifferent(DateUtil.NOW().getStartOfDay(), scheduleDTO.getTripDate().getStartOfDay());
		for (Iterator<ScheduleDiscountDTO> iterator = list.iterator(); iterator.hasNext();) {
			ScheduleDiscountDTO discountDTO = iterator.next();

			DateTime dateTime = scheduleDTO.getTripDate();
			if (discountDTO.getDateType().getId() == DateTypeEM.TRANSACTION.getId()) {
				dateTime = DateUtil.NOW();
			}
			if (StringUtil.isNull(discountDTO.getActiveFrom()) || StringUtil.isNull(discountDTO.getActiveTo()) || StringUtil.isNull(discountDTO.getDayOfWeek())) {
				iterator.remove();
				continue;
			}
			// common validations
			if (discountDTO.getActiveFrom() != null && !dateTime.gteq(new DateTime(discountDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (discountDTO.getActiveTo() != null && !dateTime.lteq(new DateTime(discountDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			/** Validate Advance Booking Days */
			if (discountDTO.getDateType().getId() == DateTypeEM.TRIP.getId() && discountDTO.getAdvanceBookingDays() != 0 && discountDTO.getAdvanceBookingDays() > advanceBookingDays) {
				iterator.remove();
				continue;
			}
			// Validate After Booking Date time
			DateTime afterBookingDatetime = DateUtil.addMinituesToDate(DateUtil.NOW().getStartOfDay(), discountDTO.getAfterBookingMinutes());
			if (discountDTO.getAfterBookingMinutes() != 0 && scheduleDTO.getTripDate().getStartOfDay().lteq(afterBookingDatetime)) {
				iterator.remove();
				continue;
			}
			if (discountDTO.getDayOfWeek() != null && discountDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (discountDTO.getDayOfWeek() != null && discountDTO.getDayOfWeek().substring(dateTime.getWeekDay() - 1, dateTime.getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Validate Schedule
			if (!discountDTO.getScheduleList().isEmpty() && existScheduleInScheduleList(discountDTO.getScheduleList(), scheduleDTO) == null) {
				iterator.remove();
				continue;
			}

			if (!discountDTO.getGroupList().isEmpty() && existGroupInGroupList(discountDTO.getGroupList(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}
			if (discountDTO.getAuthenticationType().getId() != AuthenticationTypeEM.ALL_BITS_DEFAULT.getId() && discountDTO.getAuthenticationType().getId() == AuthenticationTypeEM.ALL_REGISTERED_USER.getId() && authDTO.getAuthenticationType().getId() == AuthenticationTypeEM.BITS_GUEST.getId()) {
				iterator.remove();
				continue;
			}
			if (discountDTO.getAuthenticationType().getId() != AuthenticationTypeEM.ALL_BITS_DEFAULT.getId() && discountDTO.getAuthenticationType().getId() == AuthenticationTypeEM.SSO_FACEBOOK.getId() && authDTO.getAuthenticationType().getId() != AuthenticationTypeEM.SSO_FACEBOOK.getId()) {
				iterator.remove();
				continue;
			}
			if (discountDTO.getAuthenticationType().getId() != AuthenticationTypeEM.ALL_BITS_DEFAULT.getId() && discountDTO.getAuthenticationType().getId() == AuthenticationTypeEM.SSO_GMAIL.getId() && authDTO.getAuthenticationType().getId() != AuthenticationTypeEM.SSO_GMAIL.getId()) {
				iterator.remove();
				continue;
			}
			if (discountDTO.getDeviceMedium().getId() != DeviceMediumEM.ALL_USER.getId() && authDTO.getDeviceMedium().getId() != discountDTO.getDeviceMedium().getId()) {
				iterator.remove();
				continue;
			}
			// Exception and override
			for (Iterator<ScheduleDiscountDTO> overrideIterator = discountDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleDiscountDTO overrideStationDTO = overrideIterator.next();

				// Validate Schedule
				if (overrideStationDTO.getScheduleList().isEmpty() || existScheduleInScheduleList(overrideStationDTO.getScheduleList(), scheduleDTO) != null) {
					iterator.remove();
					break;
				}

				if (StringUtil.isNull(overrideStationDTO.getActiveFrom()) || StringUtil.isNull(overrideStationDTO.getActiveTo()) || StringUtil.isNull(overrideStationDTO.getDayOfWeek())) {
					overrideIterator.remove();
					continue;
				}
				// common validations
				if (overrideStationDTO.getActiveFrom() != null && !dateTime.gteq(new DateTime(overrideStationDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideStationDTO.getActiveTo() != null && !dateTime.lteq(new DateTime(overrideStationDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideStationDTO.getDayOfWeek() != null && overrideStationDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideStationDTO.getDayOfWeek() != null && overrideStationDTO.getDayOfWeek().substring(dateTime.getWeekDay() - 1, dateTime.getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}
				// Apply Exceptions
				iterator.remove();
				break;
			}
			if (discountDTO.getAuthenticationType().getId() != AuthenticationTypeEM.ALL_BITS_DEFAULT.getId()) {
				foundAuthenticationType = true;
			}
		}

		ScheduleDiscountDTO scheduleDiscountDTO = null;
		List<ScheduleDiscountDTO> finalList = new ArrayList<>();
		for (ScheduleDiscountDTO discountDTO : list) {
			// Validate Authentication Type
			if (foundAuthenticationType && discountDTO.getAuthenticationType().getId() == AuthenticationTypeEM.ALL_BITS_DEFAULT.getId()) {
				continue;
			}
			// Validate Female Discount
			if (discountDTO.getFemaleDiscountFlag() == Numeric.ONE_INT) {
				if (finalList.isEmpty()) {
					finalList.add(discountDTO);
				}
				continue;
			}
			if (scheduleDiscountDTO == null) {
				scheduleDiscountDTO = discountDTO;
			}
			if (DateUtil.getDayDifferent(new DateTime(discountDTO.getActiveFrom()), new DateTime(discountDTO.getActiveTo())) <= DateUtil.getDayDifferent(new DateTime(scheduleDiscountDTO.getActiveFrom()), new DateTime(scheduleDiscountDTO.getActiveTo()))) {
				scheduleDiscountDTO = discountDTO;
			}
		}

		if (scheduleDiscountDTO != null) {
			finalList.add(scheduleDiscountDTO);
			scheduleDiscountDTO.setList(finalList);
		}
		else if (!finalList.isEmpty()) {
			scheduleDiscountDTO = finalList.get(Numeric.ZERO_INT);
			scheduleDiscountDTO.setList(finalList);
		}

		return scheduleDiscountDTO;
	}

	protected List<ScheduleDiscountCacheDTO> bindDiscountToCacheObject(List<ScheduleDiscountDTO> scheduleDiscountDTOList) {
		List<ScheduleDiscountCacheDTO> discountCacheList = new ArrayList<>();
		// copy to cache
		if (scheduleDiscountDTOList != null && !scheduleDiscountDTOList.isEmpty()) {
			for (ScheduleDiscountDTO discountDTO : scheduleDiscountDTOList) {
				ScheduleDiscountCacheDTO discountCacheDTO = new ScheduleDiscountCacheDTO();
				discountCacheDTO.setActiveFlag(discountDTO.getActiveFlag());
				discountCacheDTO.setId(discountDTO.getId());
				discountCacheDTO.setCode(discountDTO.getCode());
				discountCacheDTO.setActiveFrom(discountDTO.getActiveFrom());
				discountCacheDTO.setActiveTo(discountDTO.getActiveTo());
				discountCacheDTO.setAfterBookingMinutes(discountDTO.getAfterBookingMinutes());
				discountCacheDTO.setDateTypeCode(discountDTO.getDateType().getCode());
				discountCacheDTO.setDayOfWeek(discountDTO.getDayOfWeek());
				discountCacheDTO.setPercentageFlag(discountDTO.getPercentageFlag());
				discountCacheDTO.setAuthenticationTypeId(discountDTO.getAuthenticationType().getId());
				discountCacheDTO.setDeviceMediumId(discountDTO.getDeviceMedium().getId());
				discountCacheDTO.setDiscountValue(discountDTO.getDiscountValue());
				discountCacheDTO.setFemaleDiscountFlag(discountDTO.getFemaleDiscountFlag());
				discountCacheDTO.setAdvanceBookingDays(discountDTO.getAdvanceBookingDays());

				List<String> scheduleList = new ArrayList<String>();
				for (ScheduleDTO scheduleDTO : discountDTO.getScheduleList()) {
					scheduleList.add(scheduleDTO.getCode());
				}
				discountCacheDTO.setScheduleList(scheduleList);

				List<String> groupList = new ArrayList<String>();
				for (GroupDTO groupDTO : discountDTO.getGroupList()) {
					groupList.add(groupDTO.getCode());
				}
				discountCacheDTO.setGroupList(groupList);

				List<ScheduleDiscountCacheDTO> overrideDiscountList = new ArrayList<>();
				if (discountDTO.getOverrideList() != null && !discountDTO.getOverrideList().isEmpty()) {
					for (ScheduleDiscountDTO orverrideDiscountDTO : discountDTO.getOverrideList()) {
						ScheduleDiscountCacheDTO cacheDTO = new ScheduleDiscountCacheDTO();
						cacheDTO.setActiveFlag(orverrideDiscountDTO.getActiveFlag());
						cacheDTO.setId(orverrideDiscountDTO.getId());
						cacheDTO.setCode(orverrideDiscountDTO.getCode());
						cacheDTO.setActiveFrom(orverrideDiscountDTO.getActiveFrom());
						cacheDTO.setActiveTo(orverrideDiscountDTO.getActiveTo());
						cacheDTO.setDateTypeCode(orverrideDiscountDTO.getDateType().getCode());
						cacheDTO.setDayOfWeek(orverrideDiscountDTO.getDayOfWeek());
						cacheDTO.setPercentageFlag(orverrideDiscountDTO.getPercentageFlag());
						cacheDTO.setDiscountValue(orverrideDiscountDTO.getDiscountValue());
						cacheDTO.setFemaleDiscountFlag(orverrideDiscountDTO.getFemaleDiscountFlag());
						cacheDTO.setAdvanceBookingDays(orverrideDiscountDTO.getAdvanceBookingDays());

						List<String> scheduleCacheList = new ArrayList<String>();
						for (ScheduleDTO scheduleDTO : orverrideDiscountDTO.getScheduleList()) {
							scheduleCacheList.add(scheduleDTO.getCode());
						}
						cacheDTO.setScheduleList(scheduleCacheList);

						overrideDiscountList.add(cacheDTO);
					}
					discountCacheDTO.setOverrideList(overrideDiscountList);
				}
				discountCacheList.add(discountCacheDTO);
			}
		}
		return discountCacheList;
	}

	protected List<ScheduleDiscountDTO> bindDiscountFromCacheObject(List<ScheduleDiscountCacheDTO> scheduleDiscountCacheDTOList) {
		List<ScheduleDiscountDTO> scheduleDiscountDTOList = new ArrayList<>();
		if (scheduleDiscountCacheDTOList != null && !scheduleDiscountCacheDTOList.isEmpty()) {
			// copy from cache
			for (ScheduleDiscountCacheDTO discountCache : scheduleDiscountCacheDTOList) {
				ScheduleDiscountDTO discountDTO = new ScheduleDiscountDTO();
				discountDTO.setCode(discountCache.getCode());
				discountDTO.setActiveFrom(discountCache.getActiveFrom());
				discountDTO.setActiveTo(discountCache.getActiveTo());
				discountDTO.setAfterBookingMinutes(discountCache.getAfterBookingMinutes());
				discountDTO.setDayOfWeek(discountCache.getDayOfWeek());
				discountDTO.setDateType(DateTypeEM.getDateTypeEM(discountCache.getDateTypeCode()));
				discountDTO.setDayOfWeek(discountCache.getDayOfWeek());
				discountDTO.setDeviceMedium(DeviceMediumEM.getDeviceMediumEM(discountCache.getDeviceMediumId()));
				discountDTO.setPercentageFlag(discountCache.getPercentageFlag());
				discountDTO.setAuthenticationType(AuthenticationTypeEM.getAuthenticationTypeEM(discountCache.getAuthenticationTypeId()));
				discountDTO.setDiscountValue(discountCache.getDiscountValue());
				discountDTO.setFemaleDiscountFlag(discountCache.getFemaleDiscountFlag());
				discountDTO.setAdvanceBookingDays(discountCache.getAdvanceBookingDays());

				List<ScheduleDTO> scheduleList = new ArrayList<ScheduleDTO>();
				for (String scheduleCache : discountCache.getScheduleList()) {
					ScheduleDTO schedule = new ScheduleDTO();
					schedule.setCode(scheduleCache);
					scheduleList.add(schedule);
				}
				discountDTO.setScheduleList(scheduleList);

				List<GroupDTO> groupList = new ArrayList<GroupDTO>();
				for (String groupCodes : discountCache.getGroupList()) {
					GroupDTO group = new GroupDTO();
					group.setCode(groupCodes);
					groupList.add(group);
				}
				discountDTO.setGroupList(groupList);

				List<ScheduleDiscountDTO> overrideDiscountList = new ArrayList<>();
				if (discountCache.getOverrideList() != null && !discountCache.getOverrideList().isEmpty()) {
					for (ScheduleDiscountCacheDTO overRideCacheDTO : discountCache.getOverrideList()) {
						ScheduleDiscountDTO overrideControlDTO = new ScheduleDiscountDTO();
						overrideControlDTO.setCode(overRideCacheDTO.getCode());
						overrideControlDTO.setActiveFrom(overRideCacheDTO.getActiveFrom());
						overrideControlDTO.setActiveTo(overRideCacheDTO.getActiveTo());
						overrideControlDTO.setDayOfWeek(overRideCacheDTO.getDayOfWeek());
						overrideControlDTO.setDateType(DateTypeEM.getDateTypeEM(overRideCacheDTO.getDateTypeCode()));
						overrideControlDTO.setDayOfWeek(overRideCacheDTO.getDayOfWeek());
						overrideControlDTO.setPercentageFlag(overRideCacheDTO.getPercentageFlag());
						overrideControlDTO.setDiscountValue(overRideCacheDTO.getDiscountValue());
						overrideControlDTO.setFemaleDiscountFlag(overRideCacheDTO.getFemaleDiscountFlag());
						overrideControlDTO.setAdvanceBookingDays(overRideCacheDTO.getAdvanceBookingDays());

						List<ScheduleDTO> scheduleCacheList = new ArrayList<ScheduleDTO>();
						for (String scheduleCodes : overRideCacheDTO.getScheduleList()) {
							ScheduleDTO schedule = new ScheduleDTO();
							schedule.setCode(scheduleCodes);
							scheduleCacheList.add(schedule);
						}
						overrideControlDTO.setScheduleList(scheduleCacheList);

						overrideDiscountList.add(overrideControlDTO);
					}
					discountDTO.setOverrideList(overrideDiscountList);
				}
				scheduleDiscountDTOList.add(discountDTO);
			}
		}
		return scheduleDiscountDTOList;
	}

	protected GroupDTO existGroupInGroupList(List<GroupDTO> groupList, GroupDTO groupDTO) {
		GroupDTO existingGroup = null;
		for (GroupDTO group : groupList) {
			if (group.getId() != 0 && groupDTO.getId() != 0 && group.getId() == groupDTO.getId()) {
				existingGroup = group;
				break;
			}
			else if (StringUtil.isNotNull(group.getCode()) && StringUtil.isNotNull(groupDTO.getCode()) && group.getCode().equals(groupDTO.getCode())) {
				existingGroup = group;
				break;
			}
		}
		return existingGroup;
	}

	protected ScheduleDTO existScheduleInScheduleList(List<ScheduleDTO> scheduleList, ScheduleDTO scheduleDTO) {
		ScheduleDTO existingSchedule = null;
		for (ScheduleDTO schedule : scheduleList) {
			if (schedule.getId() != 0 && scheduleDTO.getId() != 0 && schedule.getId() == scheduleDTO.getId()) {
				existingSchedule = schedule;
				break;
			}
			else if (StringUtil.isNotNull(schedule.getCode()) && StringUtil.isNotNull(scheduleDTO.getCode()) && schedule.getCode().equals(scheduleDTO.getCode())) {
				existingSchedule = schedule;
				break;
			}
		}
		return existingSchedule;
	}
}
