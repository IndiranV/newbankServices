package org.in.com.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.in.com.cache.BusCache;
import org.in.com.cache.EhcacheManager;
import org.in.com.cache.ScheduleCache;
import org.in.com.cache.dto.ScheduleSeatPreferenceCacheDTO;
import org.in.com.constants.Numeric;
import org.in.com.constants.Text;
import org.in.com.dao.ScheduleSeatPreferenceDAO;
import org.in.com.dto.AuthDTO;
import org.in.com.dto.BusDTO;
import org.in.com.dto.BusSeatLayoutDTO;
import org.in.com.dto.GroupDTO;
import org.in.com.dto.ScheduleDTO;
import org.in.com.dto.ScheduleSeatPreferenceDTO;
import org.in.com.dto.enumeration.SeatGendarEM;
import org.in.com.service.GroupService;
import org.in.com.service.ScheduleSeatPreferenceService;
import org.in.com.service.UserService;
import org.in.com.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import hirondelle.date4j.DateTime;
import net.sf.ehcache.Element;

@Service
public class ScheduleSeatPreferenceImpl implements ScheduleSeatPreferenceService {
	@Autowired
	GroupService groupService;
	@Autowired
	UserService userService;
	private static String CACHEKEY = "SHSEPE";

	public List<ScheduleSeatPreferenceDTO> get(AuthDTO authDTO, ScheduleSeatPreferenceDTO preferenceDTO) {
		ScheduleSeatPreferenceDAO dao = new ScheduleSeatPreferenceDAO();
		BusCache cache = new BusCache();
		List<ScheduleSeatPreferenceDTO> list = dao.get(authDTO, preferenceDTO);
		for (ScheduleSeatPreferenceDTO scheduleSeatPreferenceDTO : list) {
			if (scheduleSeatPreferenceDTO.getBus() != null) {
				BusDTO busDTO = new BusDTO();
				busDTO.setId(scheduleSeatPreferenceDTO.getBus().getId());
				busDTO = cache.getBusDTObyId(authDTO, busDTO);
				scheduleSeatPreferenceDTO.getBus().setDisplayName(busDTO.getDisplayName());
				scheduleSeatPreferenceDTO.getBus().setCategoryCode(busDTO.getCategoryCode());
				scheduleSeatPreferenceDTO.getBus().setName(busDTO.getName());
				scheduleSeatPreferenceDTO.getBus().setCode(busDTO.getCode());
				scheduleSeatPreferenceDTO.getBus().getBusSeatLayoutDTO().setList(filterByCode(busDTO.getBusSeatLayoutDTO().getList(), scheduleSeatPreferenceDTO.getBus().getBusSeatLayoutDTO().getList()));
			}
			for (GroupDTO groupDTO : scheduleSeatPreferenceDTO.getGroupList()) {
				GroupDTO groupCache = groupService.getGroup(authDTO, groupDTO);
				groupDTO.setCode(groupCache.getCode());
				groupDTO.setName(groupCache.getName());
				groupDTO.setLevel(groupCache.getLevel());
			}
			if (scheduleSeatPreferenceDTO.getAudit().getUser() != null && scheduleSeatPreferenceDTO.getAudit().getUser().getId() != 0) {
				scheduleSeatPreferenceDTO.getAudit().setUser(userService.getUser(authDTO, scheduleSeatPreferenceDTO.getAudit().getUser()));
			}
			for (ScheduleSeatPreferenceDTO overrideSeatPreferenceDTO : scheduleSeatPreferenceDTO.getOverrideList()) {
				if (overrideSeatPreferenceDTO.getBus() != null) {
					BusDTO overrideBusDTO = new BusDTO();
					overrideBusDTO.setId(overrideSeatPreferenceDTO.getBus().getId());
					overrideBusDTO = cache.getBusDTObyId(authDTO, overrideBusDTO);
					overrideSeatPreferenceDTO.getBus().setDisplayName(overrideBusDTO.getDisplayName());
					overrideSeatPreferenceDTO.getBus().setCategoryCode(overrideBusDTO.getCategoryCode());
					overrideSeatPreferenceDTO.getBus().setName(overrideBusDTO.getName());
					overrideSeatPreferenceDTO.getBus().setCode(overrideBusDTO.getCode());
					overrideSeatPreferenceDTO.getBus().getBusSeatLayoutDTO().setList(filterByCode(overrideBusDTO.getBusSeatLayoutDTO().getList(), overrideSeatPreferenceDTO.getBus().getBusSeatLayoutDTO().getList()));
				}
				for (GroupDTO groupDTO : overrideSeatPreferenceDTO.getGroupList()) {
					GroupDTO overrideGroup = groupService.getGroup(authDTO, groupDTO);
					groupDTO.setCode(overrideGroup.getCode());
					groupDTO.setName(overrideGroup.getName());
					groupDTO.setLevel(overrideGroup.getLevel());
				}
				if (overrideSeatPreferenceDTO.getAudit().getUser() != null && overrideSeatPreferenceDTO.getAudit().getUser().getId() != 0) {
					overrideSeatPreferenceDTO.getAudit().setUser(userService.getUser(authDTO, overrideSeatPreferenceDTO.getAudit().getUser()));
				}
			}
		}

		return list;
	}

	public ScheduleSeatPreferenceDTO Update(AuthDTO authDTO, ScheduleSeatPreferenceDTO visibilityDTO) {
		ScheduleSeatPreferenceDAO dao = new ScheduleSeatPreferenceDAO();
		ScheduleSeatPreferenceDTO visibilityDTO2 = dao.getIUD(authDTO, visibilityDTO);
		ScheduleCache scheduleCache = new ScheduleCache();
		scheduleCache.removeScheduleDTO(authDTO, visibilityDTO.getSchedule());
		return visibilityDTO2;
	}

	public List<ScheduleSeatPreferenceDTO> getByScheduleId(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String key = CACHEKEY + scheduleDTO.getCode();
		List<ScheduleSeatPreferenceDTO> seatVisibilityDTOList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleSeatPreferenceCacheDTO> scheduleSeatVisibilityCacheList = (List<ScheduleSeatPreferenceCacheDTO>) element.getObjectValue();
			seatVisibilityDTOList = bindSeatPreferenceFromCacheObject(scheduleSeatVisibilityCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleSeatPreferenceDAO visibilityDAO = new ScheduleSeatPreferenceDAO();
			seatVisibilityDTOList = visibilityDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule station Point Cache
			List<ScheduleSeatPreferenceCacheDTO> scheduleSeatVisibilityCacheList = bindSeatPreferenceToCacheObject(seatVisibilityDTOList);
			element = new Element(key, scheduleSeatVisibilityCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		// Validate all Seat Preference
		for (Iterator<ScheduleSeatPreferenceDTO> iterator = seatVisibilityDTOList.iterator(); iterator.hasNext();) {
			ScheduleSeatPreferenceDTO scheduleSeatPreferenceDTO = iterator.next();
			// common validations
			if (scheduleSeatPreferenceDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(scheduleSeatPreferenceDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (scheduleSeatPreferenceDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(scheduleSeatPreferenceDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (scheduleSeatPreferenceDTO.getDayOfWeek() != null && scheduleSeatPreferenceDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (scheduleSeatPreferenceDTO.getDayOfWeek() != null && scheduleSeatPreferenceDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			// Group List
			if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !scheduleSeatPreferenceDTO.getGroupList().isEmpty() && existGroupInGroupList(scheduleSeatPreferenceDTO.getGroupList(), authDTO.getGroup()) == null) {
				iterator.remove();
				continue;
			}
			if (scheduleSeatPreferenceDTO.getGendar() == null) {
				iterator.remove();
				continue;
			}

			// check any Exception is added
			for (Iterator<ScheduleSeatPreferenceDTO> overrideIterator = scheduleSeatPreferenceDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleSeatPreferenceDTO overrideScheduleSeatPreferenceDTO = overrideIterator.next();

				// common validations
				if (overrideScheduleSeatPreferenceDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(overrideScheduleSeatPreferenceDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideScheduleSeatPreferenceDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(overrideScheduleSeatPreferenceDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideScheduleSeatPreferenceDTO.getDayOfWeek() != null && overrideScheduleSeatPreferenceDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideScheduleSeatPreferenceDTO.getDayOfWeek() != null && overrideScheduleSeatPreferenceDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}
				if (overrideScheduleSeatPreferenceDTO.getGendar() != null && overrideScheduleSeatPreferenceDTO.getGendar().getId() == SeatGendarEM.ALL.getId()) {
					// Group List
					if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !overrideScheduleSeatPreferenceDTO.getGroupList().isEmpty() && existGroupInGroupList(overrideScheduleSeatPreferenceDTO.getGroupList(), authDTO.getGroup()) != null) {
						iterator.remove();
						break;
					}
				}
				else {
					// Group List
					if (authDTO.getNativeNamespaceCode().equals(authDTO.getNamespaceCode()) && !overrideScheduleSeatPreferenceDTO.getGroupList().isEmpty() && existGroupInGroupList(overrideScheduleSeatPreferenceDTO.getGroupList(), authDTO.getGroup()) == null) {
						overrideIterator.remove();
						break;
					}
				}
				// Exception
				if (overrideScheduleSeatPreferenceDTO.getGendar() == null) {
					iterator.remove();
					break;
				}
				// Apply Override
				scheduleSeatPreferenceDTO.setActiveFrom(overrideScheduleSeatPreferenceDTO.getActiveFrom());
				scheduleSeatPreferenceDTO.setActiveTo(overrideScheduleSeatPreferenceDTO.getActiveTo());
				scheduleSeatPreferenceDTO.setGendar(overrideScheduleSeatPreferenceDTO.getGendar());
				scheduleSeatPreferenceDTO.setBus(overrideScheduleSeatPreferenceDTO.getBus());
				scheduleSeatPreferenceDTO.setGroupList(overrideScheduleSeatPreferenceDTO.getGroupList());
			}
		}
		return seatVisibilityDTOList;
	}

	public List<ScheduleSeatPreferenceDTO> getTripScheduleSeatPreference(AuthDTO authDTO, ScheduleDTO scheduleDTO) {
		String key = CACHEKEY + scheduleDTO.getCode();
		List<ScheduleSeatPreferenceDTO> seatVisibilityDTOList = null;
		Element element = EhcacheManager.getScheduleEhCache().get(key);
		if (element != null) {
			List<ScheduleSeatPreferenceCacheDTO> scheduleSeatVisibilityCacheList = (List<ScheduleSeatPreferenceCacheDTO>) element.getObjectValue();
			seatVisibilityDTOList = bindSeatPreferenceFromCacheObject(scheduleSeatVisibilityCacheList);
		}
		else if (scheduleDTO.getId() != 0) {
			ScheduleSeatPreferenceDAO visibilityDAO = new ScheduleSeatPreferenceDAO();
			seatVisibilityDTOList = visibilityDAO.getByScheduleId(authDTO, scheduleDTO);
			// Save to schedule station Point Cache
			List<ScheduleSeatPreferenceCacheDTO> scheduleSeatVisibilityCacheList = bindSeatPreferenceToCacheObject(seatVisibilityDTOList);
			element = new Element(key, scheduleSeatVisibilityCacheList);
			EhcacheManager.getScheduleEhCache().put(element);
		}

		// Validate all Seat Preference
		for (Iterator<ScheduleSeatPreferenceDTO> iterator = seatVisibilityDTOList.iterator(); iterator.hasNext();) {
			ScheduleSeatPreferenceDTO scheduleSeatPreferenceDTO = iterator.next();
			// common validations
			if (scheduleSeatPreferenceDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(scheduleSeatPreferenceDTO.getActiveFrom()))) {
				iterator.remove();
				continue;
			}
			if (scheduleSeatPreferenceDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(scheduleSeatPreferenceDTO.getActiveTo()))) {
				iterator.remove();
				continue;
			}
			if (scheduleSeatPreferenceDTO.getDayOfWeek() != null && scheduleSeatPreferenceDTO.getDayOfWeek().length() != 7) {
				iterator.remove();
				continue;
			}
			if (scheduleSeatPreferenceDTO.getDayOfWeek() != null && scheduleSeatPreferenceDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
				iterator.remove();
				continue;
			}
			if (scheduleSeatPreferenceDTO.getGendar() == null) {
				iterator.remove();
				continue;
			}

			for (Iterator<ScheduleSeatPreferenceDTO> overrideIterator = scheduleSeatPreferenceDTO.getOverrideList().iterator(); overrideIterator.hasNext();) {
				ScheduleSeatPreferenceDTO overrideScheduleSeatPreferenceDTO = overrideIterator.next();

				// common validations
				if (overrideScheduleSeatPreferenceDTO.getActiveFrom() != null && !scheduleDTO.getTripDate().gteq(new DateTime(overrideScheduleSeatPreferenceDTO.getActiveFrom()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideScheduleSeatPreferenceDTO.getActiveTo() != null && !scheduleDTO.getTripDate().lteq(new DateTime(overrideScheduleSeatPreferenceDTO.getActiveTo()))) {
					overrideIterator.remove();
					continue;
				}
				if (overrideScheduleSeatPreferenceDTO.getDayOfWeek() != null && overrideScheduleSeatPreferenceDTO.getDayOfWeek().length() != 7) {
					overrideIterator.remove();
					continue;
				}
				if (overrideScheduleSeatPreferenceDTO.getDayOfWeek() != null && overrideScheduleSeatPreferenceDTO.getDayOfWeek().substring(scheduleDTO.getTripDate().getWeekDay() - 1, scheduleDTO.getTripDate().getWeekDay()).equals("0")) {
					overrideIterator.remove();
					continue;
				}
				// Exception
				if (overrideScheduleSeatPreferenceDTO.getGendar() == null) {
					iterator.remove();
					break;
				}
			}
		}
		return seatVisibilityDTOList;
	}

	private List<ScheduleSeatPreferenceCacheDTO> bindSeatPreferenceToCacheObject(List<ScheduleSeatPreferenceDTO> ScheduleSeatVisibilityDTOList) {
		List<ScheduleSeatPreferenceCacheDTO> seatPreferenceCacheDTOList = new ArrayList<>();
		// copy to cache
		if (ScheduleSeatVisibilityDTOList != null && !ScheduleSeatVisibilityDTOList.isEmpty()) {
			for (ScheduleSeatPreferenceDTO seatVisibilityDTO : ScheduleSeatVisibilityDTOList) {
				ScheduleSeatPreferenceCacheDTO seatVisibilityCacheDTO = new ScheduleSeatPreferenceCacheDTO();
				seatVisibilityCacheDTO.setCode(seatVisibilityDTO.getCode());
				seatVisibilityCacheDTO.setActiveFrom(seatVisibilityDTO.getActiveFrom());
				seatVisibilityCacheDTO.setActiveTo(seatVisibilityDTO.getActiveTo());
				seatVisibilityCacheDTO.setDayOfWeek(seatVisibilityDTO.getDayOfWeek());
				seatVisibilityCacheDTO.setSeatGendarCode(seatVisibilityDTO.getGendar().getCode());
				StringBuilder seatCodes = new StringBuilder();
				if (seatVisibilityDTO.getBus() != null && seatVisibilityDTO.getBus().getBusSeatLayoutDTO() != null && seatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList() != null) {
					for (BusSeatLayoutDTO layoutDTO : seatVisibilityDTO.getBus().getBusSeatLayoutDTO().getList()) {
						if (seatCodes.length() > 0) {
							seatCodes.append(Text.COMMA);
						}
						seatCodes.append(layoutDTO.getCode());
					}
				}
				seatVisibilityCacheDTO.setSeatCodeList(seatCodes.toString());
				seatVisibilityCacheDTO.setBusId(seatVisibilityDTO.getBus().getId());

				List<String> groupCodes = new ArrayList<String>();
				for (GroupDTO groupDTO : seatVisibilityDTO.getGroupList()) {
					groupCodes.add(groupDTO.getCode());
				}
				seatVisibilityCacheDTO.setGroupCodes(groupCodes);

				List<ScheduleSeatPreferenceCacheDTO> seatPreferenceOverrideDTOList = new ArrayList<ScheduleSeatPreferenceCacheDTO>();
				for (ScheduleSeatPreferenceDTO seatPreferenceOverrideDTO : seatVisibilityDTO.getOverrideList()) {
					ScheduleSeatPreferenceCacheDTO seatPreferenceOverrideCacheDTO = new ScheduleSeatPreferenceCacheDTO();
					seatPreferenceOverrideCacheDTO.setCode(seatPreferenceOverrideDTO.getCode());
					seatPreferenceOverrideCacheDTO.setActiveFrom(seatPreferenceOverrideDTO.getActiveFrom());
					seatPreferenceOverrideCacheDTO.setActiveTo(seatPreferenceOverrideDTO.getActiveTo());
					seatPreferenceOverrideCacheDTO.setDayOfWeek(seatPreferenceOverrideDTO.getDayOfWeek());
					seatPreferenceOverrideCacheDTO.setSeatGendarCode(seatPreferenceOverrideDTO.getGendar() != null ? seatPreferenceOverrideDTO.getGendar().getCode() : Text.NA);
					StringBuilder overrideSeatCodes = new StringBuilder();
					if (seatPreferenceOverrideDTO.getBus() != null && seatPreferenceOverrideDTO.getBus().getBusSeatLayoutDTO() != null && seatPreferenceOverrideDTO.getBus().getBusSeatLayoutDTO().getList() != null) {
						for (BusSeatLayoutDTO layoutDTO : seatPreferenceOverrideDTO.getBus().getBusSeatLayoutDTO().getList()) {
							if (overrideSeatCodes.length() > 0) {
								overrideSeatCodes.append(Text.COMMA);
							}
							overrideSeatCodes.append(layoutDTO.getCode());
						}
					}
					seatPreferenceOverrideCacheDTO.setSeatCodeList(overrideSeatCodes.toString());
					seatPreferenceOverrideCacheDTO.setBusId(seatPreferenceOverrideDTO.getBus().getId());

					List<String> groupOverrideCodes = new ArrayList<String>();
					for (GroupDTO groupDTO : seatPreferenceOverrideDTO.getGroupList()) {
						groupOverrideCodes.add(groupDTO.getCode());
					}
					seatPreferenceOverrideCacheDTO.setGroupCodes(groupOverrideCodes);

					seatPreferenceOverrideDTOList.add(seatPreferenceOverrideCacheDTO);
				}
				seatVisibilityCacheDTO.setOverrideList(seatPreferenceOverrideDTOList);
				seatPreferenceCacheDTOList.add(seatVisibilityCacheDTO);
			}
		}
		return seatPreferenceCacheDTOList;
	}

	private List<ScheduleSeatPreferenceDTO> bindSeatPreferenceFromCacheObject(List<ScheduleSeatPreferenceCacheDTO> seatPreferenceCacheDTOList) {
		List<ScheduleSeatPreferenceDTO> scheduleSeatPreferenceDTOList = new ArrayList<>();
		if (seatPreferenceCacheDTOList != null && !seatPreferenceCacheDTOList.isEmpty()) {
			// copy from cache
			for (ScheduleSeatPreferenceCacheDTO seatPreferenceCacheDTO : seatPreferenceCacheDTOList) {
				ScheduleSeatPreferenceDTO seatPreferenceDTO = new ScheduleSeatPreferenceDTO();
				seatPreferenceDTO.setCode(seatPreferenceCacheDTO.getCode());
				seatPreferenceDTO.setActiveFrom(seatPreferenceCacheDTO.getActiveFrom());
				seatPreferenceDTO.setActiveTo(seatPreferenceCacheDTO.getActiveTo());
				seatPreferenceDTO.setDayOfWeek(seatPreferenceCacheDTO.getDayOfWeek());
				seatPreferenceDTO.setGendar(SeatGendarEM.getSeatGendarEM(seatPreferenceCacheDTO.getSeatGendarCode()));
				seatPreferenceDTO.setActiveFlag(Numeric.ONE_INT);
				if (seatPreferenceCacheDTO.getBusId() != 0) {
					BusDTO busDTO = new BusDTO();
					busDTO.setId(seatPreferenceCacheDTO.getBusId());

					List<BusSeatLayoutDTO> seatlist = new ArrayList<>();
					if (StringUtil.isNotNull(seatPreferenceCacheDTO.getSeatCodeList())) {
						String[] seatCodes = seatPreferenceCacheDTO.getSeatCodeList().split(Text.COMMA);
						for (String seatCode : seatCodes) {
							if (StringUtil.isNotNull(seatCode)) {
								BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
								busSeatTypeDTO.setCode(seatCode);
								seatlist.add(busSeatTypeDTO);
							}
						}
					}
					BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
					busSeatTypeDTO.setList(seatlist);
					busDTO.setBusSeatLayoutDTO(busSeatTypeDTO);
					seatPreferenceDTO.setBus(busDTO);

					List<GroupDTO> groupList = new ArrayList<>();
					for (String groupCode : seatPreferenceCacheDTO.getGroupCodes()) {
						GroupDTO groupDTO = new GroupDTO();
						groupDTO.setCode(groupCode);
						groupList.add(groupDTO);
					}
					seatPreferenceDTO.setGroupList(groupList);
				}
				List<ScheduleSeatPreferenceDTO> overrideList = new ArrayList<ScheduleSeatPreferenceDTO>();
				for (ScheduleSeatPreferenceCacheDTO seatPreferenceOverrideCacheDTO : seatPreferenceCacheDTO.getOverrideList()) {
					ScheduleSeatPreferenceDTO scheduleSeatOverrideDTO = new ScheduleSeatPreferenceDTO();
					scheduleSeatOverrideDTO.setCode(seatPreferenceOverrideCacheDTO.getCode());
					scheduleSeatOverrideDTO.setActiveFrom(seatPreferenceOverrideCacheDTO.getActiveFrom());
					scheduleSeatOverrideDTO.setActiveTo(seatPreferenceOverrideCacheDTO.getActiveTo());
					scheduleSeatOverrideDTO.setDayOfWeek(seatPreferenceOverrideCacheDTO.getDayOfWeek());
					scheduleSeatOverrideDTO.setGendar(StringUtil.isNotNull(seatPreferenceOverrideCacheDTO.getSeatGendarCode()) ? SeatGendarEM.getSeatGendarEM(seatPreferenceOverrideCacheDTO.getSeatGendarCode()) : null);
					scheduleSeatOverrideDTO.setActiveFlag(Numeric.ONE_INT);
					if (seatPreferenceOverrideCacheDTO.getBusId() != 0) {
						BusDTO busDTO = new BusDTO();
						busDTO.setId(seatPreferenceOverrideCacheDTO.getBusId());

						List<BusSeatLayoutDTO> seatlist = new ArrayList<>();
						if (StringUtil.isNotNull(seatPreferenceOverrideCacheDTO.getSeatCodeList())) {
							String[] seatCodes = seatPreferenceOverrideCacheDTO.getSeatCodeList().split(Text.COMMA);
							for (String seatCode : seatCodes) {
								if (StringUtil.isNotNull(seatCode)) {
									BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
									busSeatTypeDTO.setCode(seatCode);
									seatlist.add(busSeatTypeDTO);
								}
							}
						}
						BusSeatLayoutDTO busSeatTypeDTO = new BusSeatLayoutDTO();
						busSeatTypeDTO.setList(seatlist);
						busDTO.setBusSeatLayoutDTO(busSeatTypeDTO);
						scheduleSeatOverrideDTO.setBus(busDTO);

						List<GroupDTO> groupList = new ArrayList<>();
						for (String groupCode : seatPreferenceOverrideCacheDTO.getGroupCodes()) {
							GroupDTO groupDTO = new GroupDTO();
							groupDTO.setCode(groupCode);
							groupList.add(groupDTO);
						}
						scheduleSeatOverrideDTO.setGroupList(groupList);
					}
					overrideList.add(scheduleSeatOverrideDTO);
				}
				seatPreferenceDTO.setOverrideList(overrideList);
				scheduleSeatPreferenceDTOList.add(seatPreferenceDTO);
			}
		}
		return scheduleSeatPreferenceDTOList;
	}

	private List<BusSeatLayoutDTO> filterByCode(List<BusSeatLayoutDTO> Orglist, List<BusSeatLayoutDTO> fillerlist) {
		List<BusSeatLayoutDTO> list = new ArrayList<>();
		for (BusSeatLayoutDTO fitterdto : fillerlist) {
			for (BusSeatLayoutDTO dto : Orglist) {
				if (dto.getCode().equals(fitterdto.getCode())) {
					list.add(dto);
					break;
				}
			}
		}
		return list;
	}

	protected GroupDTO existGroupInGroupList(List<GroupDTO> groupList, GroupDTO groupDTO) {
		GroupDTO existingGroup = null;
		for (GroupDTO group : groupList) {
			if (StringUtil.isNotNull(group.getCode()) && StringUtil.isNotNull(groupDTO.getCode()) && group.getCode().equals(groupDTO.getCode())) {
				existingGroup = group;
				break;
			}
		}
		return existingGroup;
	}
}
